/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling.db;

import java.io.Serializable;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class NumRange implements Serializable {

	Integer start;
	Integer stop;

	public NumRange(Integer start, Integer stop) {
		this.start = start;
		this.stop = stop;
	}

	public NumRange(String start, String stop) {
		this.start = toInt(start);
		this.stop = toInt(stop);
	}
	
	private static Integer toInt(String v) {
		return v==null? null : Integer.decode(v);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(10);
		sb.append('[');
		if (start != null) sb.append(start);
		sb.append(',');
		if (stop != null) sb.append(stop);
		sb.append(')');
		return sb.toString();
	}

}
