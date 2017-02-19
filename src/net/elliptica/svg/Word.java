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

import com.sun.xml.txw2.annotation.XmlCDATA;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
		x= y= 0;
	}

	public Word(String line, Word base, Point p) {
		this.line = line;
		this.base = base;
		x = p.x;
		y = p.y;
	}

	@Id
	@XmlID
	@XmlElement
	protected final int id = SEQUENCE++;
	
	@Column
	private final String line;

	@JoinColumn
	@OneToOne
	private WGroup derived;

	@Transient
	private final Word base;

	@ManyToOne(cascade = CascadeType.PERSIST)
	private WGroup group;
	
	@Column
	private final double x,y;

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

	@XmlIDREF
	public WGroup getDerived() {
		return derived;
	}
	

	@Override
	public String toString() {
		String main = "w{" + line + '}';
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

	void setGroup(WGroup gr){
		if (group!=null){
			group.deleteWord(this);
		}
		this.group = gr;
		group.addWord(this);
	}

	WGroup getGroup() {
		return group;
	}

	public Word getBase() {
		return base;
	}

	public void setDerived(WGroup derived) {
		this.derived = derived;
	}

	private static int SEQUENCE = 0;

	private static final long serialVersionUID = 1L;

}
