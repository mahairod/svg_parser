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
		MorphemStreamEngine.PRINT_RESULT = true;
		MorphemStreamEngine.LOAD_DB = true;
		engine = new MorphemStreamEngine(document);
	}

	@Test
	public void testParsePage() throws Exception {
		System.out.println("parsePage");
		int pageInd = 496;
		MorphemStreamEngine.str_first = 0;
		MorphemStreamEngine.SAVE_DB = true;
		MorphemStreamEngine.PRINT_INPUT = true;
		SVGMetaPost.parsePage(pageInd, engine);
		Set<Bunch> groups = engine.pageGroups.get(pageInd);
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
			0, // ->
			10,	//авиа/ба´за /11
//			0,	// ->
			1,	//авиапочт-о´в-ый
//			0,	// ->
			1,	//авиаспорт-сме´н /13
			0,	// ->
			2,	//авто´бус-н-ый /15
			3,	//автогра´ф([-иj-a]
			1,	//автограф-и´ческ-ий¹	/17
			0,	// ->
			0,	// ->
			8,	// автоматчик /20
			0,	// ->
			2,	// автоматично
			1,	//автоматически	/23
			0,	// ->
			3,	// автоматизироваться /25
			5,	// автомобильчик, автомоби´ль-н(ый)
			1,	//автомобилист-к-а /27
			0,	// ->
			0,
			5,	// авто/гон(ка) /30
			1,	//автогонщик /31
			1,	// автодорожный /32
		};
		int[] act_sizes = groups.stream().mapToInt(wg -> wg.words.size()).toArray();

//		assertArrayEquals(exp_sizes, act_sizes);
		for (Bunch group: groups){
			assertTrue("Inconsistent group " + group.toString(), group.words.size()>1 || group.getGroupLine().isRowSym());
		}
	}

	static PDDocument document;
	static MorphemStreamEngine engine;

	@Test
	public void testReparseWords() {
		System.out.println("reparseWords");
		SVGMetaPost.reparseWords(engine);
	}

	@Test
	public void testFindComments() {
		System.out.println("findComments");
		SVGMetaPost.findComments(engine);
	}
}
