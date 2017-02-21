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

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Responsible for converting all SVG path elements into MetaPost curves.
 */
public class SVGMetaPost {

  /**
   * Creates an SVG Document given a URI.
   *
   * @param uri Path to the file.
   * @throws Exception Something went wrong parsing the SVG file.
   */
  public SVGMetaPost( String uri ) throws IOException {
  }

  public static void parsePage(int pageInd, MorphemStreamEngine engine) throws IOException{
		engine.setPage(pageInd);
		engine.process();
		engine.saveState();
  }
  
  public static void reparseWords(MorphemStreamEngine engine){
	  engine.reparseWords();
  }

  /**
   * Reads a file and parses the path elements.
   * 
   * @param args args[0] - Filename to parse.
   * @throws IOException Error reading the SVG file.
   */
  static void run( String args[] ) throws IOException {
	String pdfFile = args[0];
	PDDocument document = PDDocument.load( new File(pdfFile) );
	MorphemStreamEngine engine = new MorphemStreamEngine(document);

	for (int i=32; i < 584 ; i++) {
		parsePage(i, engine);
	}

  }

}