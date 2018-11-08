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
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
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
	
	public List<Word> getWordsFromPage(int page) {
		List<Word> result = new ArrayList<>();
		selectList(Word.class, 1000, (cb, root) -> cb.and(
			cb.equal(root.get(Word_.bunch).get(Bunch_.page), page),
			cb.or(root.get(Word_.deprecated).isNull(),
				cb.equal(root.get(Word_.deprecated), false)
			)
		)).accept(lst -> result.addAll(lst));
		return result;
	}

	public void deleteNotes() {
		LineProcessor lp = new LineProcessor();
		lp.setFormatType(LineProcessor.FormatType.Clean);
		select(Word.class, (cb, root) -> {
			Path<String> line = root.get(Word_.line);
			return cb.like(line, "\u001b\u001f[%]");
		}).process((Word w) -> {
			final String note = lp.format(w.getLine())
				.replaceAll("^\\[", "")
				.replaceAll("\\]$", "")
				.replaceFirst("[´|]", "")
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

	public void countLinkedWords() {
		selectList(Bunch.class, 1000, (cb, root) -> cb.isTrue(root.get(Bunch_.root))
		).accept(lst -> {
			for (Bunch g: lst) {
				for (Word w: g.words) {
					visitLinkedWord(w);
				}
			}
		});
		ps.println("Accessible words: " + totalLeaves);
	}
	
	private int totalLeaves = 0;

	private void visitLinkedWord(Word root) {
		if (!root.isDeprecated()) {
			totalLeaves++;
		}
		if (root.getDerived() == null) {
			return;
		}
		for (Word w: root.getDerived().words) {
			visitLinkedWord(w);
		}
	}

	public void showFlags() {
		selectList(Word.class, 5000, (cb, root) -> cb.and(
			root.get(Word_.flags).isNotNull()
		)).accept(lst -> {
			lst.getClass();
		});
	}

	public boolean doMergeWords(Word src, Word dst){
		em.getTransaction().begin();
		dst.mergeWith(src);
		src.deprecate();
		return true;
	}
	
	static class Alters {
		public Alters(String alts, String old, String novel) {
			this.alts = alts;
			this.old = old;
			this.novel = novel;
			oldPatt = Pattern.compile(old);
			novelPatt = Pattern.compile(novel);
		}
		
		void setDerivedAlters(String derivedAlts) {
			novelPattExt = "(" + derivedAlts + "|)";
		}

		@Override
		public boolean equals(Object obj) {
			Alters op = (Alters) obj;
			return (
				novelPatt.matcher(op.novel).matches() ||
				op.novelPattExt!=null && Pattern.compile(novel + op.novelPattExt).matcher(op.novel).matches()
			) && oldPatt.matcher(op.old).matches();
		}
		final String alts;
		private final String old;
		private final String novel;
		private final Pattern oldPatt;
		private final Pattern novelPatt;
		private String novelPattExt;
	}

	private Alters extractAlters(Word w) {
		String alter = w.getAltRest();
		Matcher m = ALTERN_PATT.matcher(alter);
		String old = "";
		String novel = "";
		String alternation = null;
		while (m.find()) {
			String l = m.group(1);
			String r = m.group(3);
			String lr = l+r;
			if (alternation==null) {
				alternation = "";
			} else {
				alternation = alternation + ":";
			}
			alternation += l + "-" + r;
			if ("кч".equals(lr)) {
				r = "[ое]?" + r;
			} else if ("оа".equals(lr)) {
				r += "г?";
			} else if ("ео".equals(lr)) {
				r = "[оё][тк]?";
			} else if ("ио".equals(lr)) {
				r = "[оё]т?";
			} else if ("ое".equals(lr)) {
				l = "е?[оё]к?";
			} else if ("аим".equals(lr)) {
				l = "[ая]";
			}
			old += l;
			novel += r;
		}
		if (old.length()<2) {
			old = "[а-ё]?" + old + "[тхс]?";
		}
		return new Alters(alternation, old, novel);
	}

	private final String pattStr = "(?:черед.|; |, |,|)(?:|)([а-ё]+)(?:(|)|)(?:–|-)(?:|)([а-ё]+)";
	final Pattern ALTERN_PATT = Pattern.compile(pattStr);
	private int failCount = 0;

	public void mergeIsolatedAlters(){
		Function<Word, String> alterLocFun =
				word -> Arrays.asList(word.getLine().split("\u001b")).stream()
						.filter(p->p.startsWith("\u001c"))
						.map(p -> p.substring(1).replace("´", ""))
						.reduce((l, r) -> l+r).orElse("");
		select(Word.class, (cb, root) -> cb.and(
			cb.like(root.get(Word_.line), "==="),
			root.get(Word_.deprecated).isNull()
		)).process(w -> {
			int page = w.getGroup().page;
			ps.println("Processing " + w.toShortString().replace("\n", "") + " with " + w.getAltRest() + " \u001b\u001f/ p." + page);
			Alters alters = extractAlters(w);
			Word target = null;
			try {
				target = findRelatedWord(w, w.getGroup(), wcand->wcand.y>=w.y, wcand -> {
					if (wcand.y < w.y - 2. || wcand.y-w.y>19.) {
						return false;
					}
					ps.print("\t\t\u001b\u001ftrying candidate ");
					ps.println(wcand.toShortString().replace("\n", ""));
					Word parent = wcand.getGroup().parent;
					if (parent == null) {
						return false;
					}
					Alters alterLocs = new Alters(null, alterLocFun.apply(parent), alterLocFun.apply(wcand));
					if (wcand.getDerived() != null) {
						Optional<String> alternations = wcand.getDerived().words.stream()
							.filter(dw -> dw.getAltRest() != null)
							.map(dw -> extractAlters(dw).old)
							.filter(al -> !al.isEmpty())
							.reduce((l,r)-> l+"|"+r);
						if (alternations.isPresent()) {
							alterLocs.setDerivedAlters(alternations.get());
						}
					}
					return alters.equals(alterLocs);
				});
			} catch (IllegalStateException ex) {
				if (ex.getCause() != NON_MATCHING_LINE) {
					throw ex;
				}
			}
			if (target == null) {
				ps.println("---Target not found");
				failCount++;
				return;
			} else {
				ps.println("+++Found " + target.toShortString());
			}
/*
			target.setAltRest(w.getAltRest());
			target.setAlternation(alters.alts);
			if (w.getNotes()!= null) {
				if (target.getNotes()!=null) {
					throw new IllegalStateException("Notes already present");
				}
				target.setNotes(w.getNotes());
			}
			if (w.getDerived()!= null) {
				if (target.getDerived()!=null) {
					throw new IllegalStateException("Derived already present");
				}
				target.setDerived(w.getDerived());
			}
			w.deprecate();
/**/
		});
	}

	public void fixDelayedLinks() {
		em.getTransaction().begin();
		selectBatch(Word.class, (cb, root) -> cb.and(
			cb.like(root.get(Word_.line), "__"),
			root.get(Word_.deprecated).isNull(),
			root.get(Word_.bunch).get(Bunch_.parent).isNotNull(),
			cb.equal(cb.size(root.get(Word_.bunch).get(Bunch_.words)), 1)
		)).process(w->{
			ps.println("Processing word =======================");
			Bunch g = w.getGroup();
			final Word parent;
			printWord(g.parent, 0, 2);
			if (g.parent.getLine().startsWith("===")) {
				ps.println("Это чередование: " + g.parent.getAltRest() + ", пробуем слово выше");
				parent = findNearestRealWord(g.parent);
				if (parent == null) {
					ps.println("Не нашли, уходим");
					return;
				}
				printWord(parent, 0, 2);
			} else {
				parent = g.parent;
			}
			List<Bunch> candGroups = new ArrayList<>();
			selectList(Bunch.class, 20, (cb,broot) -> cb.and(
				cb.equal(broot.get(Bunch_.page), g.page),
					// p.x - 20 < bx
				cb.gt(broot.get(Bunch_.x), parent.x - 20),
					// x < bx < x+20
				cb.gt(broot.get(Bunch_.x), w.x ),
				cb.lt(broot.get(Bunch_.x), w.x + 30),
					// by1 < y < by2
				cb.lt(broot.get(Bunch_.y), w.y + 5.),
				cb.gt(cb.sum(broot.get(Bunch_.y), broot.get(Bunch_.height)), w.y - 5.)
			)).accept(blist -> {
				candGroups.addAll(blist);
			});
			if (candGroups.size() > 1) {
//				candGroups.removeIf(b -> b.getGroupLine().x1 < parent.x);
				candGroups.removeIf(b -> {
					for (Word ch: b.words) {
						if (ch == parent){
							return true;
						}
					}
					Line pos = b.getGroupLine();
					double h = pos.y2 - pos.y1;
					return pos.x1>w.x+13 && h < 10;
				});
			}
			if (candGroups.size() != 1) {
				ps.println("Wrong number of groups near: " + candGroups.size());
				return;
			}
			Bunch cand = candGroups.get(0);
			ps.println("Found group " + cand.toString());
			if (cand.parent != null) {
				ps.println("Group already has parent word: " + cand.parent);
				return;
			}
			if (cand.words.isEmpty() || cand.words.size() > 4) {
				ps.println("Unusual number of words in group, skipping: " + cand.words.size());
				return;
			}
			
			Line grPos = cand.getGroupLine();
			if (parent.y < grPos.getY2() || parent.x + parent.len < grPos.x1) {
				ps.println("Incorrect group position relative to parent: " + grPos + " and " + parent.getPoint());
				return;
			}
			if (parent.y > grPos.getY2() + 15.) {
				// check for closer words
				List<Word> others = findWordsInRegion(w, parent.y - grPos.getY2());
				if (!others.isEmpty()) {
					ps.println("Other words present upwards");
					return;
				}
			}
			ps.println("Applying link !!");
			ps.println();
			
			if (parent != g.parent) {
				g.parent.setDerived(null);
			}
			parent.setDerived(cand);
			w.deprecate();
		});
		em.getTransaction().rollback();
	}
	
	private List<Word> findWordsInRegion(final Word base, double height) {
		List<Word> result = new ArrayList<>();
		selectList(Word.class, 30, (cb, wroot) -> {
			Expression<Double> wordMiddle = cb.sum(
					wroot.get(Word_.x),
					cb.quot(wroot.get(Word_.len), 2.0)
			).as(Double.class);
			return cb.and(
				cb.equal(wroot.get(Word_.bunch).get(Bunch_.page), base.getGroup().page),
				cb.between(wroot.get(Word_.y), base.y + 5., base.y + height),
				cb.between(wordMiddle, base.x, base.x + 100)
			);
		}).accept(lst -> result.addAll(lst));
		result.removeIf(w -> w.getLine().startsWith("==="));
		return result;
	}
	
	interface Fun {
		Double v(Word w);
	}
	
	private Word findNearestRealWord(Word alter) {
		Fun d = w -> abs(w.y - alter.y);
		Fun dist = w -> d.v(w) - 1e3 * (Math.signum(d.v(w))-1);
		NavigableSet<Word> neighbs = new TreeSet<>((lw, rw) -> (int)(dist.v(lw) - dist.v(rw)));
		neighbs.addAll(alter.getGroup().words);
		neighbs.remove(alter);
		return neighbs.pollFirst();
	}

	public void fixWrongLinks() {
		RuntimeCode.em = em;
		select(Word.class, (cb, root) -> cb.equal(
			root.get(Word_.bunch),
			root.get(Word_.derived)
		)).process(w->{
			Bunch g = w.getGroup();
			int page = g.page;
			Bunch der = w.getDerived();
			int curFunc = 0;
			do {
				curFunc = RuntimeCode.wordProcessors[curFunc].apply(w);
			} while(curFunc>=0);
		});
	}

	public void moveComments() {
		selectFunc(Word.class, (cb, root) -> cb.and(
			root.get(Word_.deprecated).isNull(),
			cb.like(root.get(Word_.line), "(%)")
		)).process(w->{
			ps.println("______________________________________________________________");
			printWord(w, 0, 1);
			Word parent = findRelatedWord(w, w.getGroup(), new SimpleCloseParentMatcher(w));
			if (parent != null) {
				if (parent.getNotes()!=null) {
					ps.println("Has note already: " + parent.getNotes());
				}
				ps.println("Goes to: ");
				printWord(parent, 1, 1);
				String ln = w.getLine().replaceFirst("^", "");
				if (parent.getNotes()!=null) {
					String oldNote = parent.getNotes();
					ln = oldNote + "# " + ln;
				}
				parent.setNotes(ln);
				w.deprecate();
			} else {
				ps.println("Not found");
			}
		});
	}

	private Word findRelatedWord(final Word noteword, final Bunch group, final Function<Word, Boolean> lineMatcher) {
		return findRelatedWord(noteword, group, null, lineMatcher);
	}

	private Word findRelatedWord(final Word noteword, final Bunch group, java.util.function.Predicate<Word> filter, final Function<Word, Boolean> lineMatcher) {
		Word[] result = {null};
		visitNearestNeighbours(noteword, group, filter, (Word bw) -> {
			if (bw.y < noteword.y || bw.isDeprecated()) {
				return false;
			}
			Bunch derived = bw.getDerived();
			RuntimeException ex = null;
			if (bw.x + bw.len / 1.5 < noteword.x - 156.5 || (derived != null && bw.x + bw.len * 0.8 > derived.getGroupLine().x1 + 4.0 && 0 >= derived.getGroupLine().compareTo(new Line(new Point(bw.x, bw.y), new Point(bw.x, bw.y + 9.0))))) {
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
	
	class CloseComparator implements Comparator<Word> {
		private final Fun dist;
		public CloseComparator(Fun dist) {
			this.dist = dist;
		}
		@Override
		public int compare(Word w1, Word w2) {
			int preres = (int) (5e12 * (dist.v(w1) - dist.v(w2)));
			return preres != 0 ? preres : w1.id - w2.id;
		}
	}

	private void visitNearestNeighbours(Word word, Bunch group, java.util.function.Predicate<Word> filter, Function<Word, Boolean> proc) {
		Fun d = w -> abs(w.y - word.y);
		Fun dist = w -> d.v(w) - 1e3 * (Math.signum(d.v(w))-1);
//		NavigableSet<Word> neighbours = new TreeSet<>((lw, rw) -> (int)(dist.v(lw) - dist.v(rw)));
		NavigableSet<Word> neighbours = new TreeSet<>(new CloseComparator(d));
		Set<Word> words;
		if (filter != null) {
			words = new HashSet<>(group.words.size());
			group.words.forEach(w -> {if (filter.test(w)) words.add(w);});
		} else {
			words = group.words;
		}
		neighbours.addAll(words);
		neighbours.remove(word);
		for (Word bw : neighbours) {
			if (proc.apply(bw)) {
				break;
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="DB ops">
	private <T> void doProcessEach(TypedQuery<T> q, Consumer<T> proc) {
		List<T> beans = q.getResultList();
		for (T b : beans) {
			em.getTransaction().begin();
			proc.accept(b);
			em.getTransaction().commit();
		}
	}

	public void doMove(Word src, Word dst) {
		em.getTransaction().begin();
		src.setGroup(dst.getGroup());
	}
	public void doGroup(Word src, Word dst) {
		SingleWordBunch swb = new SingleWordBunch(src);
		Bunch gr = swb.getBunch();
		em.persist(gr);
		doLink(src, dst);
	}
	public boolean doLink(Word src, Word dst) {
		em.getTransaction().begin();
		dst.setDerived(src.getGroup());
		LOG.log(Level.INFO, "Link done: " + dst.getDerived());
		return false;
	}
	public boolean doDelink(Word src, Word dst) {
		em.getTransaction().begin();
		dst.setDerived(null);
		LOG.log(Level.INFO, "Delink done: " + dst.getDerived());
		return false;
	}
	public boolean doDestroy(Word dst) {
		em.getTransaction().begin();
		dst.deprecate();
		LOG.log(Level.INFO, "Destroy done: " + dst);
		return false;
	}
	public void doCommit() throws RollbackException {
		em.getTransaction().commit();
		LOG.log(Level.INFO, "Saved db state");
	}
	public void doCancel() throws RollbackException {
		try {
			em.getTransaction().rollback();
		} catch (IllegalStateException ex) {
			LOG.log(Level.SEVERE, "Seems transaction is not in progress?", ex);
		} finally {
			em.clear();
		}
	}
	public void doFlush() throws RollbackException {
		em.clear();
		em.getEntityManagerFactory().getCache().evictAll();
		LOG.log(Level.INFO, "Flushed db state");
	}

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
	private <T> EntityProcessor<T> select(Class<T> type, Conditions<T> conditions) {
		TypedQuery<T> query = makeEntitySelect(type, conditions);
		return (action) -> doProcessEach(query, action);
	}
	private <T> EntityProcessor<T> selectBatch(Class<T> type, Conditions<T> conditions) {
		TypedQuery<T> query = makeEntitySelect(type, conditions);
		return (action) -> query.getResultList().forEach(action);
	}
	private <T> EntityProcessor<T> selectOne(Class<T> type, Conditions<T> conditions) {
		TypedQuery<T> q = makeEntitySelect(type, conditions);
		return (action) -> action.accept(q.getSingleResult());
	}
	private <T> Consumer<Consumer<List<T>>> selectList(Class<T> type, int limit, Conditions<T> conditions) {
		TypedQuery<T> q = makeEntitySelect(type, conditions);
		q.setMaxResults(limit);
		return (action) -> action.accept(q.getResultList());
	}

	private <T> EntityProcessor<T> selectFunc(Class<T> type, Conditions<T> conditions) {
		return select_(type, conditions, this::doProcessEach);
	}
	private <T> EntityProcessor<T> selectOneFunc(Class<T> type, Conditions<T> conditions) {
		return select_(type, conditions, (q, a) -> a.accept(q.getSingleResult()) );
	}
	private <T> EntityProcessor<T> select_(Class<T> type, Conditions conditions, BiConsumer<TypedQuery<T>, Consumer<T>> applier) {
		TypedQuery<T> query = makeEntitySelect(type, conditions);
		return (action) -> applier.accept(query, action);
	}
	// </editor-fold>

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

	public void extractAlters() {
		selectList(Word.class, 100, (cb, root) -> cb.and(
			LineProcessor.noteAlternPred(cb, root),
			cb.ge(root.get("id"), 126735))
		)
			.accept(new LineProcessor()::extractAlternations);
	}

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
			return root.get(Word_.id).in(77582, 94656, 96342);
		});
		doWordProcessing(q, (Word w) -> {
			Word tail = fixWordT(w);
			if (tail != null) {
				em.persist(tail);
				em.merge(tail.getBase());
			}
		});
	}

	private Word fixWordT(Word word) {
		String line = word.getLine();
		String patt = "\u001b\u001f\\(|\\)\u001b\u001f";
		String[] parts = {
			"===",
			"выно´с-лив(ый)"
		};
		return fixWord_(word, parts);
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
//			Word extract = word.splitRight(parts[0].length());
			Word extract = word.splitRight(word.getLine().length()/2);
			extract.setGroup(derived);
			return extract;
		}
	}

	private Bunch findNearestGroup(Word word, Bunch group, Point groupLocation, double cutLen) {
		Bunch[] derived = {null};
		visitNearestNeighbours(word, group, null, (Word bw) -> {
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
		printWord(root, tabs, Integer.MAX_VALUE);
	}
	private void printWord(Word root, int tabs, int depth) {
		if (tabs < 1) {
			ps.print("p." + root.getGroup().page + " ");
		}
		tabs(tabs);
		ps.println(root);
		
		depth--;
		if (depth<1) return;
		if (root.getDerived() == null) {
			return;
		}
		tabs(tabs + 1);
		ps.println("==== " + root.getDerived().id);
		Set<Word> words = new TreeSet<>((Word wl, Word wr) -> -(int) (wl.y - wr.y));
		words.addAll(root.getDerived().words);
		for (Word w : words) {
			printWord(w, tabs + 1, --depth);
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
