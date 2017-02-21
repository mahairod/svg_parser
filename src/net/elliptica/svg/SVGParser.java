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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class SVGParser {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			if (arg.equals("SAVEDB")){
				MorphemStreamEngine.SAVE_DB = true;
			}
			if (arg.equals("SAVEXML")){
				MorphemStreamEngine.SAVE_XML = true;
			}
		}
		try {
			SVGMetaPost.run(args);
		} catch (IOException ex) {
			Logger.getLogger(SVGParser.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
