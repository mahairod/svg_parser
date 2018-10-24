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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import static net.elliptica.svg.DbRecordsParser.cleanLine;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
class ParentSubLineMatcher implements Function<Word, Boolean> {
	private final String parentLine;
	private final String lostLine;
	private final Word lostWord;
	private LineProcessor lp;

	public ParentSubLineMatcher(String parentLine, String lostLine, Word lostWord) {
		this.parentLine = parentLine;
		this.lostLine = lostLine;
		this.lostWord = lostWord;
	}

	@Override
	public Boolean apply(Word t) {
		if (t.y - lostWord.y > 15.) {
			return false;
		}
		String upper = cleanLine(t.getLine());
		boolean toMerge = true;
		// check length first
		toMerge = toMerge && upper.length() + 1 >= lostLine.length() * 2 / 3;
		// check for substrings
		Set<String> substrings = longestCS(upper, lostLine);
		if (substrings.isEmpty()) {
			toMerge = toMerge && true;
		} else {
			int subLen = substrings.iterator().next().length();
			if (subLen < upper.length() / 3) {
				toMerge = toMerge && true;
			} else {
				toMerge = toMerge && !upper.startsWith(lostLine.substring(0, subLen));
			}
		}
		return toMerge;
	}

	private Set<String> longestCS(String left, String right) {
		int[][] table = new int[left.length()][right.length()];
		int longest = 0;
		Set<String> result = new HashSet<>();
		for (int i = 0; i < left.length(); i++) {
			for (int j = 0; j < right.length(); j++) {
				if (left.charAt(i) != right.charAt(j)) {
					continue;
				}
				table[i][j] = (i * j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(left.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result;
	}

}
