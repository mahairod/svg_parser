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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class Word implements Comparable<Word> {

	public Word() {
		this.line = null;
		this.base = null;
	}

	public Word(String line, Word base) {
		this.line = line;
		this.base = base;
	}

	private final String line;
	private WGroup derived;

	private final Word base;

	private WGroup group;

	@XmlID
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

	void setGroup(WGroup group){
		this.group = group;
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

}
