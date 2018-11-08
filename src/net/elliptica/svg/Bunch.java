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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false)
	protected Integer id;
	
	@Transient
	private final Line groupLine;

//	@XmlJavaTypeAdapter(type=long.class, value=WSLongAdapter.class)
	public Integer getId() {
		return id;
	}

	@XmlElement(name = "word")
	@XmlElementWrapper
	@OneToMany(mappedBy = "bunch", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	final Set<Word> words;

	@XmlIDREF
	@OneToOne(mappedBy = "derived", cascade = CascadeType.PERSIST)
	Word parent;

	@Column
	int page;

	@Column
	private final boolean root;

	@Column
	private final double x, y, height, width;

	public Bunch() {
//		id = 0;
		words = null;
		groupLine = null;
		root = false;
		x= y= height= width= 0;
	}

	public Bunch(Line groupLine) {
//		this.id = SEQUENCE++;
		this.groupLine = groupLine;
		this.words = new HashSet<>();
		root = groupLine.isRoot();
		x = groupLine.x1;
		y = groupLine.y1;
		width = 0;
		height = groupLine.y2 - groupLine.y1;
	}

	@Override
	public int compareTo(Bunch o) {
		return groupLine.compareTo(o.groupLine);
	}

	void addWord(Word word){
		words.add(word);
	}

	Word setParent(Word word){
		Word w = this.parent;
		parent = word;
		return w;
	}

	void deleteWord(Word word){
		words.remove(word);
	}

	public boolean isRoot() {
		return root;
	}

	public Set<Word> getWords() {
		return Collections.unmodifiableSet(words);
	}

	@Override
	public String toString() {
		Object[] wordArr = words.stream().filter(w-> !w.isDeprecated()).map(Word::toShortString).toArray();
		return "WG{" + "id=" + getId()+ ", " + wordArr.length + " words=" + Arrays.toString(wordArr) + '}';
//		return "WG{" + "line=" + getGroupLine() + ", " + wordArr.length + " words=" + Arrays.toString(wordArr) + '}';
	}

	Line getGroupLine() {
		if (groupLine!=null) {
			return groupLine;
		}
		return new Line(new Point(x, y), new Point(x, y+height));
	}
	
	static int SEQUENCE = 1001010;

	private static final long serialVersionUID = 1L;

}
