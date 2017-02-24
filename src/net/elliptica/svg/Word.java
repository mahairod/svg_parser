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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table
public class Word implements Comparable<Word>, Serializable {

	public Word() {
		this.line = null;
		this.base = null;
		x= y= len= 0;
		hyphen = false;
	}

	public Word(MorphemStreamEngine.LineState ls) {
		this.line = ls.accumulator;
		this.base = ls.previous;
		x = ls.coord.x;
		y = ls.coord.y;
		len = ls.len;
		hyphen = ls.hyphen;
		
	}

	@Id
	@XmlID
	@XmlElement
	protected final int id = SEQUENCE++;
	
	@Column
	private String line;

	@Column
	private String text, notes;

//	@Transient
	@OneToOne(fetch = FetchType.LAZY)
	private Bunch derived;

	@Transient
	private final Word base;

//	@Transient
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private Bunch bunch;
	
	@Column
	final double x,y, len;
	
	final boolean hyphen;
	@Column(nullable = true)
	private Boolean deprecated;

//	@XmlID
	@XmlElement
	public String getLine() {
		return line
/*
				.replaceAll("\u001b\u001e", "\u001b[31m")
				.replaceAll("\u001b\u001f", "\u001b[0m")
				.replaceAll("\u001b\u001d", "\u001b[32m")
				.replaceAll("\u001b\u001c", "\u001b[34m")
				+ "\u001b[0m"
*/
			;
	}

	public void updateLine(String line) {
		this.line = line;
	}

	public void setText(String text) {
		this.text = text;
	}

	@XmlIDREF
	public Bunch getDerived() {
		return derived;
	}

	public double getLen() {
		return len;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void deprecate(){
		deprecated = true;
	}

	@Override
	public String toString() {
		String main = "{" + line + '}' + Integer.toString((int)x) + "/" + Integer.toString((int)y);
		if (base==null){
			return main;
		}
		return main + " -> " + base.toString();
	}

	public String toShortString() {
		String main = "\nw{" + line + "}";
		return main;
	}

	@Override
	public int compareTo(Word o) {
		return line.compareTo(o.line);
	}

	public int compHPos(Word o) {
		return (int)(x - o.x);
	}

	void setGroup(Bunch gr){
		if (bunch!=null){
			bunch.deleteWord(this);
		}
		this.bunch = gr;
		bunch.addWord(this);
	}
	
	private Point getPoint(){
		return new Point(x, y);
	}

	Bunch getGroup() {
		return bunch;
	}

	public Word getBase() {
		return base;
	}

	public Bunch setDerived(Bunch derived) {
		Bunch old = this.derived;
		this.derived = derived;
		return old;
	}

	static int SEQUENCE = 0;

	private static final long serialVersionUID = 1L;

}
