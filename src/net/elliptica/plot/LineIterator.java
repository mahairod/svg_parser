/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.plot;

import java.awt.font.TextAttribute;
import static java.lang.Integer.max;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import static java.util.Arrays.binarySearch;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
class LineIterator implements AttributedCharacterIterator {
	private final String origLine;
	private final String cleanLine;
	private ListIterator<Character> origIt;
	private ListIterator<Character> clIt;
	private int curState = 0x3;
	
	private Map<Attribute,Object> attrs = new HashMap<>(2);
	{
		attrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
		attrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
	}
	private boolean isItalic(char c) {
		return (c & 1) != 1;
	}
	private boolean isBold(char c) {
		return (c & 2) != 2;
	}

	LineIterator(String origLine) {
		this.origLine = origLine;
		cleanLine = origLine.replaceAll("[´]", "");
		reset();
	}
	private List<Character> tolist(String ln) {
		return Stream.iterate(0, v->v+1).limit(ln.length()).map(i->ln.charAt(i)).collect(Collectors.toList());
	}
	
	char[] FORMAT_CHARS = "´".toCharArray();
	{
		Arrays.sort(FORMAT_CHARS);
	}
	static final char base = '\u001b';
	int[] FORM_STARTS = {
		0,0,0,0
	};

	private void reset() {
		origIt = tolist(origLine).listIterator();
		clIt = tolist(cleanLine).listIterator();
	}
	private char inc(){
		char c;
		while(origIt.hasNext() && binarySearch(FORMAT_CHARS, c=origIt.next())>0 ){
			if (binarySearch(FORMAT_CHARS, c)>0 && c!='´') {
				int format = c&0x3;
				if (format != curState) {
					curState = format;
					FORM_STARTS[format] = clIt.nextIndex();
					attrs.put(TextAttribute.WEIGHT, isBold(c)? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
					attrs.put(TextAttribute.POSTURE, isItalic(c)? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
				}
			}
		}
		return clIt.next();
	}

	private char dec(){
		char c;
		while(origIt.hasPrevious() && binarySearch(FORMAT_CHARS, c=origIt.previous())>0 ){
		}
		return clIt.previous();
	}

	@Override
	public int getRunStart() {
		return FORM_STARTS[curState];
	}

	@Override
	public int getRunStart(Attribute attribute) {
		if (TextAttribute.WEIGHT.equals(attribute) || TextAttribute.POSTURE.equals(attribute)) {
			return max( max(FORM_STARTS[0], FORM_STARTS[0]), max(FORM_STARTS[0], FORM_STARTS[0]) );
		} else return 0;
	}

	@Override
	public int getRunStart(Set<? extends Attribute> attributes) {
		return getRunStart(attributes.iterator().next());
	}

	@Override
	public int getRunLimit() {
		String rest = origLine.substring(origIt.nextIndex()).replaceFirst(base + "[^" + (base|curState) +"].*", "");
		rest = rest.replaceAll("[´]", "");
		return this.getIndex() + rest.length() + 1;
	}

	@Override
	public int getRunLimit(Attribute attribute) {
		return getRunLimit();
	}

	@Override
	public int getRunLimit(Set<? extends Attribute> attributes) {
		return getRunLimit();
	}

	// <editor-fold defaultstate="collapsed" desc="simple ops">
	@Override
	public Map<Attribute, Object> getAttributes() {
		return attrs;
	}

	@Override
	public Object getAttribute(Attribute attribute) {
		return attrs.get(attribute);
	}

	@Override
	public Set<Attribute> getAllAttributeKeys() {
		return attrs.keySet();
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="base ops">
	@Override
	public char first() {
		reset();
		return inc();
	}

	@Override
	public char last() {
		return setIndex(cleanLine.length()-1);
	}

	@Override
	public char current() {
		if (!clIt.hasNext()) {
			return CharacterIterator.DONE;
		}
		char c = clIt.next();
		clIt.previous();
		return c;
	}

	@Override
	public char next() {
		if (!clIt.hasNext()) {
			return CharacterIterator.DONE;
		}
		return inc();
	}

	@Override
	public char previous() {
		if (!clIt.hasPrevious()) {
			return CharacterIterator.DONE;
		}
		return dec();
	}

	@Override
	public char setIndex(int position) {
		reset();
		char c=0;
		while (position-->0) {
			c = inc();
		}
		return c;
	}

	@Override
	public int getBeginIndex() {
		return 0;
	}

	@Override
	public int getEndIndex() {
		return cleanLine.length();
	}

	@Override
	public int getIndex() {
		return clIt.previousIndex() + 1;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(LineIterator.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	// </editor-fold>
}
