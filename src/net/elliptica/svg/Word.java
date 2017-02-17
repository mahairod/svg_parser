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

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class Word implements Comparable<Word> {

	public Word(String line, Word base) {
		this.line = line;
		this.base = base;
	}
	private final String line;
	private final Word base;
	private WGroup group;
	

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

}
