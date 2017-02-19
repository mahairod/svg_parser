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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table
public class Bunch implements Serializable, Comparable<Bunch> {

	@Id
	@XmlID
	@XmlElement
	protected final int id = SEQUENCE++;
	
	@Transient
	private final Line groupLine;

//	@XmlJavaTypeAdapter(type=long.class, value=WSLongAdapter.class)
	public int getId() {
		return id;
	}

	@XmlElement(name = "word")
	@XmlElementWrapper
	@OneToMany(mappedBy = "group", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	final Set<Word> words;

	@XmlIDREF
	@OneToOne(mappedBy = "derived", cascade = CascadeType.PERSIST)
	Word parent;

	@Column
	int page;

	@Column
	private final boolean root;

	public Bunch() {
		words = null;
		groupLine = null;
		root = false;
	}

	public Bunch(Line groupLine) {
		this.groupLine = groupLine;
		this.words = new HashSet<>();
		root = groupLine.isRoot();
	}

	@Override
	public int compareTo(Bunch o) {
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

	public boolean isRoot() {
		return root;
	}

	@Override
	public String toString() {
		Object[] wordArr = words.stream().map(Word::toShortString).toArray();
		return "WG{" + "line=" + groupLine + ", " + wordArr.length + " words=" + Arrays.toString(wordArr) + '}';
	}

	Line getGroupLine() {
		return groupLine;
	}
	
	private static int SEQUENCE = 0;

	private static final long serialVersionUID = 1L;

}