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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.net.URI;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDCIDFont;
import org.apache.pdfbox.pdmodel.font.PDCIDFontType2;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.w3c.dom.DOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Responsible for converting all SVG path elements into MetaPost curves.
 */
public class SVGMetaPost {
  private static final String PATH_ELEMENT_NAME = "path";

  private Document svgDocument;

  /**
   * Creates an SVG Document given a URI.
   *
   * @param uri Path to the file.
   * @throws Exception Something went wrong parsing the SVG file.
   */
  public SVGMetaPost( String uri ) throws IOException {
//    setSVGDocument( createSVGDocument( uri ) );
  }

  /**
   * Finds all the path nodes and converts them to MetaPost code.
   */
  public void run() {
    NodeList pathNodes = getPathElements();
    int pathNodeCount = pathNodes.getLength();

    for( int iPathNode = 0; iPathNode < pathNodeCount; iPathNode++ ) {
      MetaPostPath mpp = new MetaPostPath( pathNodes.item( iPathNode ) );
      System.out.println( mpp.toCode() );
    }
  }

  /**
   * Returns a list of elements in the SVG document with names that
   * match PATH_ELEMENT_NAME.
   * 
   * @return The list of "path" elements in the SVG document.
   */
  private NodeList getPathElements() {
    return getSVGDocumentRoot().getElementsByTagName( PATH_ELEMENT_NAME );
  }

  /**
   * Returns an SVGOMSVGElement that is the document's root element.
   * 
   * @return The SVG document typecast into an SVGOMSVGElement.
   */
  private SVGOMSVGElement getSVGDocumentRoot() {
    return (SVGOMSVGElement)getSVGDocument().getDocumentElement();
  }

  /**
   * This will set the document to parse. This method also initializes
   * the SVG DOM enhancements, which are necessary to perform SVG and CSS
   * manipulations. The initialization is also required to extract information
   * from the SVG path elements.
   *
   * @param document The document that contains SVG content.
   */
  public void setSVGDocument( Document document ) {
    initSVGDOM( document );
    this.svgDocument = document;
  }

  /**
   * Returns the SVG document parsed upon instantiating this class.
   * 
   * @return A valid, parsed, non-null SVG document instance.
   */
  public Document getSVGDocument() {
    return this.svgDocument;
  }

  /**
   * Enhance the SVG DOM for the given document to provide CSS- and SVG-specific
   * DOM interfaces.
   * 
   * @param document The document to enhance.
   * @link http://wiki.apache.org/xmlgraphics-batik/BootSvgAndCssDom
   */
  private void initSVGDOM( Document document ) {
    UserAgent userAgent = new UserAgentAdapter();
    DocumentLoader loader = new DocumentLoader( userAgent );
    BridgeContext bridgeContext = new BridgeContext( userAgent, loader );
    bridgeContext.setDynamicState( BridgeContext.DYNAMIC );

    // Enable CSS- and SVG-specific enhancements.
    (new GVTBuilder()).build( bridgeContext, document );
  }

  /**
   * Use the SVGDocumentFactory to parse the given URI into a DOM.
   * 
   * @param uri The path to the SVG file to read.
   * @return A Document instance that represents the SVG file.
   * @throws Exception The file could not be read.
   */
  private Document createSVGDocument( String uri ) throws IOException {
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
    return factory.createSVGDocument( uri );
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

	String prefix = pdfFile.substring(0, pdfFile.length() - 4);
	MorphemStreamEngine engine = new MorphemStreamEngine(null);
	int curPage = 1;
	engine.processPage(document.getPage(32));
	for (PDPage page : document.getPages()) {
		if (curPage > 31)
//		engine.processPage(page);
		engine.saveState();
		curPage++;
//		PDResources resources = page.getResources();
		// extract all fonts which are part of the page resources
//		new SVGMetaPost(null).
//			processResources(resources, prefix, false);
	}

//	DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();	

    URI uri = new File( args[0] ).toURI();
    String parser = XMLResourceDescriptor.getXMLParserClassName();
    SVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
	Document svgDocument = factory.createSVGDocument( uri.toString() );

	Node node = svgDocument.getChildNodes().item(0);
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


    private void processResources(PDResources resources, String prefix, boolean addKey) throws IOException
    {
        if (resources == null)
        {
            return;
        }

        for (COSName key : resources.getFontNames())
        {
            PDFont font = resources.getFont(key);
            // write the font
            if (font instanceof PDTrueTypeFont)
            {
                String name = null;
                if (addKey)
                {
                    name = getUniqueFileName(prefix + "_" + key, "ttf");
                }
                else
                {
                    name = getUniqueFileName(prefix, "ttf");
                }
                writeFont(font.getFontDescriptor(), name);
            }
            else if (font instanceof PDType0Font)
            {
                PDCIDFont descendantFont = ((PDType0Font) font).getDescendantFont();
                if (descendantFont instanceof PDCIDFontType2)
                {
                    String name = null;
                    if (addKey)
                    {
                        name = getUniqueFileName(prefix + "_" + key, "ttf");
                    }
                    else
                    {
                        name = getUniqueFileName(prefix, "ttf");
                    }
                    writeFont(descendantFont.getFontDescriptor(), name);
                }
            }
        }

        for (COSName name : resources.getXObjectNames())
        {
            PDXObject xobject = resources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                PDFormXObject xObjectForm = (PDFormXObject) xobject;
                PDResources formResources = xObjectForm.getResources();
                processResources(formResources, prefix, addKey);
            }
        }

    }

    private void writeFont(PDFontDescriptor fd, String name) throws IOException
    {
        if (fd != null)
        {
            PDStream ff2Stream = fd.getFontFile2();
            if (ff2Stream != null)
            {
                FileOutputStream fos = null;
                try
                {
                    System.out.println("Writing font:" + name);
                    fos = new FileOutputStream(new File(name + ".ttf"));
                    IOUtils.copy(ff2Stream.createInputStream(), fos);
                }
                finally
                {
                    if (fos != null)
                    {
                        fos.close();
                    }
                }
            }
        }
    }

    private String getUniqueFileName(String prefix, String suffix)
    {
        String uniqueName = null;
        File f = null;
        while (f == null || f.exists())
        {
            uniqueName = prefix + "-" + fontCounter;
            f = new File(uniqueName + "." + suffix);
            fontCounter++;
        }
        return uniqueName;
    }

    private int fontCounter = 1;

}