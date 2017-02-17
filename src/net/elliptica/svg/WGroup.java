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

import java.util.Arrays;
import java.util.Set;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class WGroup implements Comparable<WGroup> {
	private final Line groupLine;
	final Set<Word> words;

	public WGroup(Line groupLine, Set<Word> words) {
		this.groupLine = groupLine;
		this.words = words;
	}

	@Override
	public int compareTo(WGroup o) {
		return groupLine.compareTo(o.groupLine);
	}

	void addWord(Word word){
		words.add(word);
	}

	void deleteWord(Word word){
		words.remove(word);
	}

	@Override
	public String toString() {
		Object[] wordArr = words.stream().map(Word::toShortString).toArray();
		return "WG{" + "line=" + groupLine + ", " + wordArr.length + " words=" + Arrays.toString(wordArr) + '}';
	}

	Line getGroupLine() {
		return groupLine;
	}

}
