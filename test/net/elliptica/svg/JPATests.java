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

import java.io.File;
import java.io.IOException;
import static net.elliptica.svg.SVGMetaPostTest.document;
import static net.elliptica.svg.SVGMetaPostTest.engine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class JPATests {

	@BeforeClass
	public static void setUpClass() throws IOException {
		MorphemStreamEngine.PRINT_RESULT = true;
		MorphemStreamEngine.LOAD_DB = true;
		engine = new MorphemStreamEngine(null);
	}


	@Test
	void testExtractAlters(){
		System.out.println("findAlters");
		engine.findAlters();
		
	}

	@Test
	public void testReparseWords() {
		System.out.println("reparseWords");
		engine.reparseWords();
	}

	@Test
	public void testFindComments() {
		System.out.println("findComments");
		engine.findComments();
	}

	static PDDocument document;
	static DataProcessor engine;

}
