/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.svg;

import java.io.PrintStream;
import static java.lang.Math.abs;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import org.eclipse.persistence.jpa.JpaCriteriaBuilder;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class DbRecordsParser implements DataProcessor {

	public DbRecordsParser() {
		initJPA();
	}

	public void extractAlters() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Word> cq = cb.createQuery(Word.class);
		Root<Word> root = cq.from(Word.class);
		cq.select(root).where(LineProcessor.noteAlternPred(cb, root), cb.ge(root.get("id"), 126735));
		LineProcessor lp = new LineProcessor();
		int offs = 0;
		while (true) {
			List<Word> words = em.createQuery(cq).setMaxResults(100).setFirstResult(offs).getResultList();
			if (words == null || words.isEmpty()) {
				break;
			}
			lp.extractAlternations(words);
			if (!words.isEmpty()) {
			}
		}
	}

	public void deleteNotes() {
		TypedQuery<Word> q = makeEntitySelect(Word.class, (cb, root) -> {
			Path<String> line = root.get(Word_.line);
			return cb.like(line, "\u001b\u001f[%]");
		});
		LineProcessor lp = new LineProcessor();
		lp.setFormatType(LineProcessor.FormatType.Clean);
		doWordProcessing(q, (Word w) -> {
			final String note = lp.format(w.getLine())
				.replaceAll("^\\[", "")
				.replaceAll("\\]$", "")
				.replaceFirst("´", "")
				.replaceAll("\\|", "")
				.replaceFirst("’j-э", "ье")
				.replaceFirst("([’" + consonants + "])j-э", "$1ье")
				.replaceFirst("([’" + consonants + "])j-о", "$1ьё")
				.replaceFirst("j’?-э", "е")
				.replaceFirst("([" + vowels + "])j-а", "$1я")
				.replaceFirst("’j-а", "ья")
				.replaceFirst("еj-а", "ея")
				.replaceFirst("j-а", "ия")
				.replace("jа", "я")
				.replace("яjи", "яи")
				.replaceAll("-", "");
			EqualLineMatcher matcher = new EqualLineMatcher(note, w);
			Word parent = findRelatedWord(w, w.getGroup(), matcher);
			parent.setNotes(w.getLine());
			relinkDerives(parent, w);
			w.deprecate();
			em.merge(w);
			em.merge(parent);
		});
	}

	public void replaceLatins() {
		TypedQuery<Word> q = makeEntitySelect(Word.class, (cb, root) -> {
			Path<String> line = root.get(Word_.line);
			JpaCriteriaBuilder ecb = (JpaCriteriaBuilder) cb;
			return cb.isTrue(ecb.fromExpression(ecb.toExpression(line).regexp(".*[a-ik-z].*")));
		});
		doWordProcessing(q, (Word w) -> {
			String ln = w.getLine();
			w.updateLine(replaceLat(ln));
			em.merge(w);
		});
	}

	public void fixHyphens() {
		Integer[] brokenGroups = {};
		TypedQuery<Bunch> q = makeEntitySelect(Bunch.class, (cb, root) -> {
			Path<Long> grId = root.get(Bunch_.id);
			return grId.in(brokenGroups);
		});
		doProcessEach(q, (Bunch gr) -> {
			String original = gr.parent == null ? null : cleanLine(gr.parent.getLine());
			NavigableSet<Word> words = new TreeSet<>((Word l, Word r) -> (int) (100 * (l.y - r.y)));
			words.addAll(gr.words);
			for (Word w : words) {
				if (w.isDeprecated()) {
					continue;
				}
				String chLn = cleanLine(w.getLine());
				ParentSubLineMatcher matcher = new ParentSubLineMatcher(original, chLn, w);
				String parLn = original == null ? "---------" : original;
				// common prefix
				int prefLen;
				int minLen = Math.min(parLn.length(), chLn.length());
				for (prefLen = 0; prefLen < minLen && parLn.charAt(prefLen) == chLn.charAt(prefLen); prefLen++) {
					;
				}
				int maxLen = Math.max(parLn.length(), chLn.length());
				if (prefLen < (minLen + maxLen * 2 / 3) / 4) {
					try {
						Word target = findRelatedWord(w, gr, matcher);
						if (target == null) {
							continue;
						}
						target.updateLine(target.getLine() + w.getLine());
						relinkDerives(target, w);
						w.deprecate();
					} catch (IllegalStateException ise) {
						if (ise.getCause() == NON_MATCHING_LINE) {
							continue;
						}
						throw ise;
					}
				}
			}
		});
	}

	private Word findRelatedWord(final Word noteword, final Bunch group, final Function<Word, Boolean> lineMatcher) {
		Word[] result = {null};
		visitNearestNeighbours(noteword, group, (Word bw) -> {
			if (bw.y < noteword.y || bw.isDeprecated()) {
				return false;
			}
			Bunch derived = bw.getDerived();
			RuntimeException ex = null;
			if (bw.x + bw.len / 1.5 < noteword.x - 6.5 || (derived != null && bw.x + bw.len * 0.8 > derived.getGroupLine().x1 + 4.0 && 0 >= derived.getGroupLine().compareTo(new Line(new Point(bw.x, bw.y), new Point(bw.x, bw.y + 9.0))))) {
				ex = new IllegalStateException("Related word is out of bounds");
			}
			if (ex == null && !lineMatcher.apply(bw)) {
				ex = new IllegalStateException("Related word is not the same", NON_MATCHING_LINE);
			}
			if (ex != null) {
				if (bw.getDerived() != null) {
					result[0] = findRelatedWord(noteword, bw.getDerived(), lineMatcher);
				}
				if (result[0] == null) {
					throw ex;
				}
			} else {
				result[0] = bw;
			}
			return result[0] != null;
		});
		return result[0];
	}

	private void visitNearestNeighbours(Word word, Bunch group, Function<Word, Boolean> proc) {
		NavigableSet<Word> neighbours = new TreeSet<>((Word w1, Word w2) -> (int) (5000 * (abs(word.y - w1.y) - abs(word.y - w2.y))));
		neighbours.addAll(group.words);
		neighbours.remove(word);
		for (Word bw : neighbours) {
			if (proc.apply(bw)) {
				break;
			}
		}
	}

	private <T> void doProcessEach(TypedQuery<T> q, Consumer<T> proc) {
		List<T> beans = q.getResultList();
		for (T b : beans) {
			em.getTransaction().begin();
			proc.accept(b);
			em.getTransaction().commit();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="DB ops">
	private <T> TypedQuery<T> makeEntitySelect(Class<T> type, Conditions conditions) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(type);
		Root<T> root = cq.from(type);
		cq.select(root);
		cq.where(conditions.make(cb, root));
		return em.createQuery(cq);
	}
	interface EntityProcessor<T> {
		void process(Consumer<T> proc);
	}
	private <T> EntityProcessor<T> select(Class<T> type, Conditions conditions) {
		TypedQuery<T> query = makeEntitySelect(type, conditions);
		return (action) -> doProcessEach(query, action);
	}
	private <T> EntityProcessor<T> selectOne(Class<T> type, Conditions conditions) {
		TypedQuery<T> q = makeEntitySelect(type, conditions);
		return (action) -> action.accept(q.getSingleResult());
	}

	private <T> EntityProcessor<T> selectFunc(Class<T> type, Conditions conditions) {
		return select_(type, conditions, this::doProcessEach);
	}
	private <T> EntityProcessor<T> selectOneFunc(Class<T> type, Conditions conditions) {
		return select_(type, conditions, (q, a) -> a.accept(q.getSingleResult()) );
	}
	private <T> EntityProcessor<T> select_(Class<T> type, Conditions conditions, BiConsumer<TypedQuery<T>, Consumer<T>> applier) {
		TypedQuery<T> query = makeEntitySelect(type, conditions);
		return (action) -> applier.accept(query, action);
	}
	// </editor-fold>

	public void commonClean() {
		TypedQuery<Word> q = makeEntitySelect(Word.class, (cb, root) -> {
			Path<String> line = root.get(Word_.line);
			return cb.like(line, "%→%");
		});
		doWordProcessing(q, (Word w) -> {
			String[] parts = w.getLine().split("→");
			String head = parts[0];
			if (head.trim().isEmpty()) {
				return;
			}
			if (head.lastIndexOf('(') > head.lastIndexOf(')')) {
				return;
			}
			boolean saveTail = true;
			if (saveTail) {
				Word tail = w.splitRight(head.length() + 1);
				w.updateLine(head.substring(0, head.length() - 1));
				Bunch g = new Bunch(new Line(tail.x, tail.x + tail.len, tail.y));
				g.page = w.getGroup().page;
				g.setParent(w);
				w.setDerived(g);
				tail.setGroup(g);
				em.persist(g);
				em.persist(tail);
				em.merge(w);
			}
		});
	}

	private void doWordProcessing(TypedQuery<Word> q, Consumer<Word> proc) {
		em.getTransaction().begin();
		List<Word> words = q.getResultList();
		for (Word w : words) {
			printWord(w, 1);
			proc.accept(w);
		}
		em.getTransaction().commit();
	}

	public void showWordTree(int id) {
		selectOne(Word.class, (cb, root) -> cb.equal(root.get("id"), id))
		.process(w -> printWord(w, 0));
	}

	private void tabs(int tabs) {
		for (int i = 0; i < tabs; i++) {
			System.out.print('\t');
		}
	}

	public void fixAlters() {
		TypedQuery<Word> q = makeEntitySelect(Word.class, (cb, root) -> {
			Path<String> line = root.get(Word_.line);
			return cb.and(cb.like(line, "\u001b\u001f(%)_%"));
		});
		doWordProcessing(q, (Word w) -> {
			Word tail = fixWord2(w);
			if (tail != null) {
				em.persist(tail);
				em.merge(tail.getBase());
			}
		});
	}

	private Word fixWord2(Word word) {
		String line = word.getLine();
		String patt = "\u001b\u001f\\(|\\)\u001b\u001f";
		String[] parts = line.split(patt);
		String[] par = {parts[1], line.substring(line.indexOf(parts[2]))};
		return fixWord_(word, par);
	}

	private Word fixWord(Word word) {
		LineProcessor lp = new LineProcessor();
		String[] parts = lp.splitAlterLinePrefix(word.getLine());
		return fixWord_(word, parts);
	}

	private Word fixWord_(Word word, String[] parts) {
		double cutLen = word.len * parts[0].length() / word.getLine().length();
		double splitPos = word.x + cutLen;
		Point groupLocation = new Point(splitPos, word.y);
		Bunch derived = findNearestGroup(word, word.getGroup(), groupLocation, cutLen * 1.5);
		int id = word.getId();
		if (derived == null) {
			switch (id) {
				case 117370:
				case 121137:
				case 127042:
				case 143699:
					return null;
				default:
					derived.getClass();
					return null;
			}
		} else {
			Word extract = word.splitRight(parts[0].length());
			extract.setGroup(derived);
			return extract;
		}
	}

	private Bunch findNearestGroup(Word word, Bunch group, Point groupLocation, double cutLen) {
		Bunch[] derived = {null};
		visitNearestNeighbours(word, group, (Word bw) -> {
			Bunch cand = bw.getDerived();
			if (cand == null) {
				return false;
			}
			if (cand.getGroupLine().isCovered(groupLocation, cutLen)) {
				derived[0] = cand;
				return true;
			} else {
				derived[0] = findNearestGroup(word, cand, groupLocation, cutLen * 1.2);
			}
			return derived[0] != null;
		});
		return derived[0];
	}

    // <editor-fold defaultstate="collapsed" desc="Old parsers">                          
	public void reparseWords() {
		CriteriaQuery<Word> cq = em.getCriteriaBuilder().createQuery(Word.class);
		cq.select(cq.from(Word.class));
		List<Word> words = em.createQuery(cq).getResultList();
		List<Word> prepared = new ArrayList<>(20);
		for (Word word : words) {
			String line = word.getLine();
			String[] parts = line.split("\u001b[\u001c-\u001f]");
			String text = "";
			for (String part : parts) {
				text += part.replaceAll("´", "");
			}
			String modif = text.replaceAll("-", "")
				.replace("|", "")
				.replace("/", "")
				.replaceAll("(.+)\\(([а-я]+)\\)$", "$1$2")
				.replace("и[jэ]", "ие")
				.replace("и[jа]", "ия")
				.replace("[jи]", "ьи")
				.replaceAll("^•", "");
			ps.print(text);
			ps.print("\t\t");
			ps.println(modif);
			word.setText(modif);
			prepared.add(word);
			if (prepared.size() > 19) {
				saveWords(prepared);
			}
		}
		if (!prepared.isEmpty()) {
			saveWords(prepared);
		}
	}

	public void findComments() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
		Root<Bunch> broot = cq.from(Bunch.class);
		SetJoin<Bunch, Word> word = broot.join(Bunch_.words);
		cq.select(broot.get(Bunch_.page)).where(LineProcessor.excludingParan(cb, word));
		List<Integer> pagesIds = em.createQuery(cq).getResultList();
		for (int pageId : pagesIds) {
			boolean m = findCommentsOnPage(pageId);
			if (m) {
			}
		}
	}

	boolean findCommentsOnPage(int page) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Bunch> cq = cb.createQuery(Bunch.class);
		Path<Bunch> root = cq.from(Bunch.class);
		cq.select(root).where(cb.equal(root.get(Bunch_.page), page));
		List<Bunch> bunches = em.createQuery(cq).getResultList();
		LineProcessor lp = new LineProcessor();
		boolean m = false;
		try {
			for (Bunch bunch : bunches) {
				if (bunch.words.isEmpty()) {
					continue;
				}
				NavigableSet<Word> words = new TreeSet<>(bunchSorter);
				words.addAll(bunch.words);
				boolean res = lp.process(words);
				if (res) {
					saveWords(words);
				}
				m = m || res;
			}
		} catch (LineProcessor.MergeError error) {
			lp.reportError("Page " + page + ": " + error.getMessage());
			m = true;
		}
		return m;
	}

	private void saveWords(Collection<Word> words){
		em.getTransaction().begin();
		try{
			for (Word w: words){
				em.persist(w);
			}
			em.getTransaction().commit();
			words.clear();
		} catch (Exception ex){
			em.getTransaction().rollback();
			LOG.log(Level.SEVERE, null, ex);
		}
	}
// </editor-fold>

	private void relinkDerives(Word parent, Word child) {
		Bunch pder = parent.getDerived();
		Bunch der = child.getDerived();
		if (pder != null && der != null && pder != der) {
			throw new IllegalStateException("Multiple groups to same word (while merging comment)");
		}
		MorphemStreamEngine.relinkInher(parent, child.getDerived());
	}

	private String replaceLat(String lat) {
		char[] chars = lat.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if ('a' <= c && c <= 'z') {
				int ind = c - 'a';
				c = CYR_CHARS.charAt(ind);
				if (c != '.') {
					chars[i] = c;
				}
			}
		}
		return new String(chars);
	}

	private void printWord(Word root, int tabs) {
		tabs(tabs);
		ps.print(root.getId());
		ps.print(":  " + Integer.valueOf((int) root.x) + " = ");
		ps.println(root.getLine());
		if (root.getDerived() == null) {
			return;
		}
		tabs(tabs + 1);
		ps.println("\t=== " + root.getDerived().id);
		Set<Word> words = new TreeSet<>((Word wl, Word wr) -> -(int) (wl.y - wr.y));
		words.addAll(root.getDerived().words);
		for (Word w : words) {
			printWord(w, tabs + 2);
		}
	}

	private Comparator<Word> bunchSorter = (Word wl, Word wr) -> (int) ((wr.y - wl.y) * 100);

	static String cleanLine(String orig) {
		String res = CLEAN_PATT.matcher(orig).replaceAll("");
		Matcher m = PACK_PATT.matcher(res);
		StringBuffer sb = new StringBuffer(res.length());
		while (m.find()) {
			String g = m.group();
			switch (g.charAt(1)) {
				case 'а':
					g = "я";
					break;
				case 'э':
					g = "е";
					break;
				case 'о':
					g = "ё";
					break;
				case 'у':
					g = "ю";
					break;
			}
			m.appendReplacement(sb, g);
		}
		return m.appendTail(sb).toString();
	}

	private void initJPA(){
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("SVGParserPU");
		em = emf.createEntityManager();
	}

	interface Conditions<T> {
		Predicate make(CriteriaBuilder cb, Root<T> root);
	}

	private EntityManager em;
	private final PrintStream ps = new FormatStream();

	private static Pattern CLEAN_PATT = Pattern.compile("[^а-ёj]+");
	private static Pattern PACK_PATT = Pattern.compile("j[аэоу]");

	static final String vowels = "аоуэыяёюеи";
	static final String consonants;
	private static Exception NON_MATCHING_LINE = new Exception("Lines do not match");

	private static final String CYR_CHARS = "а.с.е.....к.тпор.г.....ху.";
	private static final String LAT_CHARS = "abcdefghijklmnopqrstuvwxyz";

	static {
		String consonants_ = "";
		for (char сим = 'а'; сим <= 'я'; сим++){
			if (vowels.indexOf(сим)<0){
				consonants_ += сим;
			}
		}
		consonants = consonants_;
	}

	static final Logger LOG = Logger.getLogger(DbRecordsParser.class.getName());
}
