/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.svg;

import java.util.function.Function;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class SimpleCloseParentMatcher implements Function<Word, Boolean> {
	private final Word origin;

	public SimpleCloseParentMatcher(Word origin) {
		this.origin = origin;
	}

	@Override
	public Boolean apply(Word cand) {
		return (cand.y - origin.y < 19.);
	}

}
