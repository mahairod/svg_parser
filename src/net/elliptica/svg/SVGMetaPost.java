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
import java.io.PrintStream;

import java.net.URI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
//    setSVGDocument( createSVGDocument( uri ) );
  }

  public static void parsePage(PDDocument document, int pageInd, MorphemStreamEngine engine) throws IOException{
		PDPage page = document.getPage(pageInd);
		engine.setPage(pageInd);
		engine.processPage(page);
		engine.saveState();
  }

  /**
   * Reads a file and parses the path elements.
   * 
   * @param args args[0] - Filename to parse.
   * @throws IOException Error reading the SVG file.
   */
  public static void run( String args[] ) throws IOException {
	String pdfFile = args[1];
	PDDocument document = PDDocument.load( new File(pdfFile) );

	MorphemStreamEngine engine = new MorphemStreamEngine(null);
	for (int i=32; i < 584 ; i++) {
		parsePage(document, i, engine);
	}

    URI uri = new File( args[0] ).toURI();

	Node node = null;//svgDocument.getChildNodes().item(0);
	NodeList nl = node.getChildNodes();
	NodeList symbols = c(c(node)).getChildNodes();
	NodeList groups = c(node, 3).getChildNodes();
		for (int i=0; i < groups.getLength()/2; i++){
//			printNode(nl.item(i*2+1), 1 );
		}

//	printNode(svgDocument.getChildNodes().item(0), 0);
  }
  static void printNode(Node node, int level){
	  ident(level);
	  ps.println(node.getNodeName());
	  NamedNodeMap nnm = node.getAttributes();
	  if (nnm != null){
		for (int i=0; i < nnm.getLength(); i++){
//			printNode(nnm.item(i), level+1 );
		}
	  }
	  NodeList nl = node.getChildNodes();
	  if (nl != null){
		for (int i=0; i < nl.getLength()/2; i++){
			printNode(nl.item(i*2+1), level+1 );
		}
	  }
  }
  
  static void ident(int l){
	  for (int i=0; i<l; i++){
		  ps.print('\t');
	  }
  }
  
  static Node c(Node n){
	  return n.getChildNodes().item(1);
  }

  static Node c(Node n, int ind){
	  return n.getChildNodes().item(ind);
  }

  static PrintStream ps = System.out;

}