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
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class SVGMetaPostTest {
	
	public SVGMetaPostTest() {
	}
	
	@BeforeClass
	public static void setUpClass() throws IOException {
		String pdfFile = "/mnt/store/data/Texts/Словари/work/Тихонов.pdf";
		document = PDDocument.load( new File(pdfFile) );
		engine = new MorphemStreamEngine(null);
	}

	@Test
	public void testParsePage() throws Exception {
		System.out.println("parsePage");
		int pageInd = 32;
		SVGMetaPost.parsePage(document, pageInd, engine);
		Set<WGroup> groups = engine.pageGroups.get(pageInd);
		assertNotNull(groups);
		int[] exp_sizes = {
			11,	//glob
			0, // ->
			3,	//аварийщик /2
			1,//авр-ть
			0, // ->
			2, // безавар
			1, // авг-ский /6
			2, // авитатор
			1, // авит-ский / 8
			0, // ->
			9,	//авиа/ба´за /10
			0,	// ->
			1,	//авиапочт-о´в-ый
			0,	// ->
			1,	//авиаспорт-сме´н /14
			0,	// ->
			2,	//авто´бус-н-ый /16
			0,	// ->
			3,	//автогра´ф([-иj-a] /18
			1,	//автограф-и´ческ-ий¹
			0,	// -> /20
			8,	// автоматчик /21
			0,	// ->
			2,	// автоматично /23
			1,	//автоматически /24
			0,	// ->
			3,	// автоматизироваться /26
			5,	// автомобильчик, автомоби´ль-н(ый)
			1,	//автомобилист-к-а /28
			4,	// авто/гон(ка) /29
			0,	// ->
			1,	//автогонщик /31
			0,	// ->
			0, 0,
			1,	// автодорожный /33
		};
		int[] act_sizes = groups.stream().mapToInt(wg -> wg.words.size()).toArray();
		assertArrayEquals(exp_sizes, act_sizes);
		for (WGroup group: groups){
			assertTrue("Inconsistent group " + group.toString(), group.words.size()>1 || group.getGroupLine().rowSym);
		}
	}

	static PDDocument document;
	static MorphemStreamEngine engine;
}
