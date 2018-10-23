/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2017 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.svg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import static net.elliptica.svg.LineProcessor.Format.*;
import static net.elliptica.svg.LineProcessor.FormatClean.*;
import static net.elliptica.svg.LineProcessor.FormatConsole.*;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class LineProcessor {

	boolean extractAlternations(List<Word> words){
		boolean altered = false;
		for (Iterator<Word> it = words.iterator(); it.hasNext(); ){
			String line = it.next().getLine();
			Matcher m = ALTERN_PATT.matcher(line);
			if (!m.matches()){
				it.remove();
				continue;
			}
			totalAlters++;
		}
		return altered;
	}

	void setFormatType(FormatType formatType) {
		this.formatType = formatType;
	}

	String[] splitAlterLinePrefix(String line) {
		Matcher m = ALTERN_PATT.matcher(line);
		if (m.find()) {
			int mainPos = m.start(0);
			int mainEnd = m.end(0);
			String match = m.group(0);
			String[] res = {line.substring(0, mainEnd), line.substring(mainEnd)};
			return res;
		} else {
			return null;
		}
	}

	private int totalAlters = 0;
	private FormatType formatType = FormatType.Inplace;

	enum FormatType {
		Console, Inplace, Clean
	}

	private static final String ALTERN_PATT_STR = ".*\\(черед.((|)[а-ё’]+((|)|)(–|-)(|)[а-ё]+(; |, |))+\\)";
	private final Pattern ALTERN_PATT = Pattern.compile(ALTERN_PATT_STR);

	boolean process(NavigableSet<Word> words) throws MergeError {
		StringBuilder sb = null;
		List<Word> merged = new ArrayList<>();
		int state = 0;
		boolean altered = false;
		for (Word w: words){
			String line = w.getLine();
			for (char ch: line.toCharArray()){
				if ((ch & 0xfffe) == '('){
					state += (1&~ch) - (1&ch);
					if ( 0>state || state>1 ){
						throw new MergeError("Incorrect comments in «" + line +"» of group " + w.getGroup().id  );
					}
				}
			}
			if (sb!=null){
				{
					int fPos = sb.lastIndexOf("\u001b");
					Character begForm = line.charAt(0)=='\u001b' ? line.charAt(1) : null;
					if (fPos >= 0 && begForm != null && begForm.equals(sb.charAt(fPos+1)) ){
						line = line.substring(2);
					}
				}
				if (sb.charAt(sb.length()-1)=='-'){
					sb.deleteCharAt(sb.length()-1);
					sb.append(line);
				} else {
					sb.append('\t').append(line);
				}
				merged.add(w);
				if (state == 0){
					Word parent;
					if (merged.get(0).getLine().trim().startsWith("(")){
						// separate case
						parent = words.lower(merged.get(0));
						if (parent==null){
							throw new MergeError("Parent no found for «" + line +"» of group " + w.getGroup().id  );
						}
					} else {
						parent = merged.get(0);
						String head = parent.getLine();
						int cutPos = head.lastIndexOf('(');
						if (cutPos>1 && head.charAt(cutPos-2)=='\u001b'){
							cutPos -= 2;
						}
						sb.delete(0, cutPos);
						head = head.substring(0, cutPos);
						if (head.endsWith(" ")){
							head = head.substring(0, head.length()-1);
						}
						parent.updateLine(head);
					}
					postmerge(sb);
					parent.setNotes(sb.toString());
					for (Word npart: merged){
						Bunch derived = parent.getDerived();
						if (npart == parent) continue;
						npart.deprecate();
						Bunch derN = npart.getDerived();
						if (derN == null ){
							continue;
						}
						if (derived!=null && derN != derived){
							throw new MergeError("Conflict derivation " + derN.toString() +" vs " + derived.toString() );
						}
						parent.setDerived(derN);
					}
					System.out.println(String.format("\t Merging into %s \t\tfrom:\t %s\n\n => %s\n",
						sb,
						Arrays.toString(merged.toArray()),
						parent
					));
					sb = null;
					merged.clear();
					altered = true;
				}
			} else {
				if (state == 1){
					sb = new StringBuilder(line);
					merged.add(w);
				} else {
					System.out.println(String.format("\t Merging into %s \t\tfrom:\t %s\n\n",
						sb,
						Arrays.toString(merged.toArray())
					));
				}
			}
		}
		return altered;
	}

	void postmerge(StringBuilder sb){
		int len = sb.length();
		int offs = (sb.charAt(0)=='\u001b') ? 2 : 0;
		if (len>4 &&
				sb.charAt(offs+0)=='(' &&
				sb.charAt(offs+1)=='‘' &&
				sb.charAt(len-2)=='’' &&
				sb.charAt(len-1)==')'
		){
			sb.replace(len-2, len, "»");
			len -= 2;
			sb.replace(offs, offs+2, "«");
			//sb.chars().filter(c -> c!='\t').mapToObj(c -> new Character((char)c));
			for (int i=len-1; i>=0; i--){
				if (sb.charAt(i)=='\t'){
					sb.setCharAt(i, ' ');
				}
			}
		}
	}

	class MergeError extends Exception {
		public MergeError(String message) {
			super(message);
		}
	}
	void parseComments(String line){
		String ln = format(line);
		String alter = "(" + Italic.f("черед.");
		if (ln.contains(alter)){

		}

		String flex = "(" + RU_STR + ")";
		if (ln.contains(flex)){
		}

	}

	String format(String line){
		String[] pts = line.split("\u001b");
		String res = "";
		for (String pt: pts){
			if (pt.isEmpty()) continue;
			int f = pt.charAt(0) - '\u001c';
			res += getFormats()[f].f(pt.substring(1));
		}
		return res;
	}

	FormatInterface[] getFormats() {
		switch (formatType) {
			case Inplace:
				return formats;
			case Console:
				return consoleFormats;
			case Clean:
				return cleanFormats;
			default:
				return null;
		}
	}

	static final Format[] formats = {
		BoldItalic, Italic, Bold, Clean
	};
	static final FormatConsole[] consoleFormats = {
		BoldItalicCF, ItalicCF, BoldCF, CleanCF
	};
	static final FormatClean[] cleanFormats = {
		BoldItalicCL, ItalicCL, BoldCL, CleanCL
	};

	static final String RU_STR = "[а-ё\\-]+";
	static final String STYLE_BOLDIT =	"\u001b\u001c";
	static final String STYLE_ITALIC =	"\u001b\u001d";
	static final String STYLE_BOLD =	"\u001b\u001e";
	static final String STYLE_CLEAN =	"\u001b\u001f";
	static final String STYLE_ANY =		"\u001b.";

	interface FormatInterface {
		String f(String is);
	}

	enum Format implements FormatInterface {
		Clean, Italic, Bold, BoldItalic;

		public String f(String is){
			char[] chars = is.toCharArray();
			for (int i=0; i<chars.length; i++){
				chars[i] |= m;
			}
			return new String(chars);
		}

		private final int m = ordinal() << 12;
	}

	enum FormatConsole implements FormatInterface {
		CleanCF, BoldCF, ItalicCF, BoldItalicCF;
		@Override
		public String f(String is) {
			return CharMapper.MAPPERS[ordinal()].getFormat() + is;
		}
	}

	enum FormatClean implements FormatInterface {
		CleanCL, BoldCL, ItalicCL, BoldItalicCL;
		@Override
		public String f(String is) {
			return is;
		}
	}

	void reportError(String msg){
		System.err.println(msg);
	}

	static Predicate excludingParan(CriteriaBuilder cb, SetJoin<Bunch,Word> word){
		Expression<String> linePath = word.get(Word_.line);

		Predicate openLike = cb.like(linePath, "%(%");
		Predicate closeLike = cb.like(linePath, "%)%");
		Predicate openOnly = cb.and(openLike, cb.not(closeLike));
		Predicate closeOnly = cb.and(closeLike, cb.not(openLike));
		Predicate singlePar = cb.or(openOnly, closeOnly);

		Predicate deprec = cb.isNull(word.get(Word_.deprecated));

		return cb.and(deprec, singlePar);
	}
	static Predicate noteAlternPred(CriteriaBuilder cb, Root<Word> word){
		Predicate deprec = cb.isNull(word.get(Word_.deprecated));

		Expression<String> linePath = word.get(Word_.line);

		Predicate likeMinus = cb.like(linePath, "%\\(черед.%-%\\)");
		Predicate likeHyphen = cb.like(linePath, "%\\(черед.%–%\\)");
		Predicate like = cb.or(likeMinus, likeHyphen);

		return cb.and(deprec, like);
	}

}
