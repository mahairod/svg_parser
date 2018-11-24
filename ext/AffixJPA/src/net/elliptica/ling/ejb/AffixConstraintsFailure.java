/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */
package net.elliptica.ling.ejb;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class AffixConstraintsFailure extends Exception {

	/**
	 * Creates a new instance of <code>AffixConstraintsFailure</code> without detail message.
	 */
	public AffixConstraintsFailure() {
	}

	/**
	 * Constructs an instance of <code>AffixConstraintsFailure</code> with the specified detail message.
	 *
	 * @param msg the detail message.
	 */
	public AffixConstraintsFailure(String msg) {
		super(msg);
	}

	public AffixConstraintsFailure(String message, Throwable cause) {
		super(message, cause);
	}
}
