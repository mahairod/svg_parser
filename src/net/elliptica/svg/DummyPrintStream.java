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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class DummyPrintStream extends PrintStream {

	public DummyPrintStream(String fileName) throws FileNotFoundException {
		super(fileName);
	}
	
	static PrintStream instanse(String fileName){
		try {
			return new DummyPrintStream(fileName);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(DummyPrintStream.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public void println(String x) {
	}

	@Override
	public void print(String s) {
	}

}
