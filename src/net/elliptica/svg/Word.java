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
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import net.elliptica.ling.FlagsConveter;
import net.elliptica.ling.PartOfSpeach;
import net.elliptica.ling.PoSConverter;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table
public class Word implements Comparable<Word>, Serializable {
	static int SEQUENCE = 1001990;

	public Word() {
		this.id = 0;
		this.line = null;
		this.base = null;
		x= y= len= 0;
		hyphen = false;
		this.altRest = null;
	}

	public Word(MorphemStreamEngine.LineState ls) {
		this(ls.accumulator, ls.previous, ls.coord.x, ls.coord.y, ls.len, ls.hyphen);
	}

	private Word(String line, Word base, double x, double y, double len, boolean hyphen) {
		this.id = SEQUENCE++;
		this.line = line;
		this.base = base;
		this.x = x;
		this.y = y;
		this.len = len;
		this.hyphen = hyphen;
		this.altRest = null;
	}

	private Word(String line, Word orig, double koeff) {
		this(line, orig,
				orig.x + (orig.len - orig.len * koeff),
				orig.y,
				orig.len * koeff,
				orig.hyphen
		);
	}

	@Id
	@XmlID
	@XmlElement
	protected final int id;

	@Column
	private String line;

	@Column
	private String text, notes;

//	@Transient
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "derived_id", referencedColumnName = "id")
	private Bunch derived;

	@Transient
	private final Word base;

//	@Transient
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	@JoinColumn(name = "bunch_id", referencedColumnName = "id")
	private Bunch bunch;

	@Column
	final double x,y;

	@Column
	double len;

	final boolean hyphen;
	@Column(nullable = true)
	private Boolean deprecated;

	private String alternation;

	@Column(name = "alt_rest")
	private String altRest;

	private Character variant;
	private Character version;

	@Lob
	@Convert(converter = FlagsConveter.class)
	private byte[] flags;

	@Convert(converter = PoSConverter.class)
	private PartOfSpeach pos;

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

	void setText(String text) {
		this.text = text;
	}

	@XmlIDREF
	public Bunch getDerived() {
		return derived;
	}

	public double getLen() {
		return len;
	}

	public Integer getId() {
		return id;
	}

	public String getNotes() {
		return notes;
	}

	void setNotes(String notes) {
		this.notes = notes;
	}

	public void deprecate(){
		deprecated = true;
	}

	void setNotDeprecated(){
		deprecated = false;
	}

	public Boolean isDeprecated() {
		return deprecated == null ? false : deprecated;
	}

	@Override
	public String toString() {
		String main = "{" + Integer.toString(id) + ":" + line + '}' + (int)x + "/" + (int)y;
		if (base!=null){
			main = main + " -> " + base.toString();
		}
		if (derived!=null){
			main = main + " --> " + derived.toString();
		}
		return main;
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

	Word splitRight(int pos) {
		double lenFull = line.length();
		double rLenCf = (1. - pos / lenFull);

		Word rWord = new Word(line.substring(pos), this, rLenCf);
		len -= rWord.len;
		line = line.substring(0, pos);
		rWord.derived = derived;
		if (derived!=null) {
			derived.parent = rWord;
		}
		this.derived = null;
		return rWord;
	}
	
	void mergeWith(Word t) {
		line += t.line;
		len += t.len/3;
		
		class Setter {
			<T> void set(String name, Consumer<T> setter, Supplier<T> getter, Supplier<T> o_getter) {
				T val = o_getter.get();
				if (val != null) {
					if (getter.get() != null) {
						throw new IllegalStateException(name + " already present");
					}
					setter.accept(val);
				}
			}
		}
		
		Setter setter = new Setter();

		setter.set("Derived", this::setDerived, this::getDerived, t::getDerived);
		setter.set("Notes", this::setNotes, this::getNotes, t::getNotes);
		setter.set("AltRest", this::setAltRest, this::getAltRest, t::getAltRest);
		setter.set("Alternation", this::setAlternation, this::getAlternation, t::getAlternation);
		setter.set("Version", this::setVersion, this::getVersion, t::getVersion);
		setter.set("Variant", this::setVariant, this::getVariant, t::getVariant);
		setter.set("PoS", this::setPos, this::getPos, t::getPos);
		setter.set("Flags", this::setFlags, this::getFlags, t::getFlags);

	}

	public Point getPoint(){
		return new Point(x, y);
	}

	public Point getMiddleEnd(){
		return new Point(x + len, y - 4.5);
	}

	public Bunch getGroup() {
		return bunch;
	}

	public Word getBase() {
		return base;
	}

	Bunch setDerived(Bunch derived) {
		Bunch old = this.derived;
		if (old != null && old != derived ) {
			Word p = old.setParent(null);
			if (p!= null){
				p.derived = null;
			}
		}
		this.derived = derived;
		if (derived != null) {
			Word p = this.derived.setParent(this);
			if (p!= null){
				p.derived = null;
			}
		}
		return old;
	}

	private static final long serialVersionUID = 1L;

	public String getText() {
		return text;
	}

	public String getAlternation() {
		return alternation;
	}

	void setAlternation(String alternation) {
		this.alternation = alternation;
	}

	public String getAltRest() {
		return altRest;
	}

	void setAltRest(String altRest) {
		this.altRest = altRest;
	}

	public Character getVersion() {
		return version;
	}

	void setVersion(Character version) {
		this.version = version;
	}

	public Character getVariant() {
		return variant;
	}

	void setVariant(Character variant) {
		this.variant = variant;
	}

	public byte[] getFlags() {
		return flags;
	}

	void setFlags(byte[] flags) {
		this.flags = flags;
	}

	public PartOfSpeach getPos() {
		return pos;
	}

	void setPos(PartOfSpeach pos) {
		this.pos = pos;
	}

}
