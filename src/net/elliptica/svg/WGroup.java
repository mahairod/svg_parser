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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class WGroup implements Comparable<WGroup> {

	@XmlID
	@XmlElement
	protected final String id = Long.toString(SEQUENCE++);
	
	private final Line groupLine;

//	@XmlJavaTypeAdapter(type=long.class, value=WSLongAdapter.class)
	public String getId() {
		return id;
	}

	@XmlElement(name = "word")
	@XmlElementWrapper
	final Set<Word> words;

	@XmlIDREF
	Word parent;

	public WGroup() {
		words = null;
		groupLine = null;
	}

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

	void setParent(Word word){
		parent = word;
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
	
	private static long SEQUENCE = 0;

}
