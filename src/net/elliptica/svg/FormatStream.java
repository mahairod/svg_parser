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

import java.io.PrintStream;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class FormatStream extends PrintStream {
	LineProcessor lp = new LineProcessor();

	public FormatStream() {
		super(System.out);
		lp.setFormatType(LineProcessor.FormatType.Console);
	}
	
	private String format(String param) {
		if (param.indexOf('\u001b')<0){
			return param;
		}
		return lp.format(param);
	}

	@Override
	public void print(String s) {
		super.print(format(s));
	}

}
