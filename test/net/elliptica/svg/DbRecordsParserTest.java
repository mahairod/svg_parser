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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class DbRecordsParserTest {

	@BeforeClass
	public static void setUpClass() throws IOException {
		MorphemStreamEngine.PRINT_RESULT = true;
		MorphemStreamEngine.LOAD_DB = true;
		engine = new DbRecordsParser();
	}

	@Test
	public void testCommonClean(){
		System.out.println("commonClean");
		engine.commonClean();
	}

	@Test
	public void testMoveComments(){
		System.out.println("moveComments");
		engine.moveComments();
	}

	@Test
	public void testFixAlters(){
		System.out.println("fixAlters");
		engine.fixAlters();
	}

	@Test
	public void testFixHyphens(){
		System.out.println("fixHyphens");
		engine.fixHyphens();
	}

	@Test
	public void testDeleteNotes(){
		System.out.println("deleteNotes");
		engine.deleteNotes();
	}

	@Test
	public void testReplaceLatins(){
		System.out.println("replaceLatins");
		engine.replaceLatins();
	}

	@Test
	public void testLatins(){
		char sym[] = {'a'};
		Stream.generate(() -> {
			return sym[0]++;
		})
			.limit(30)
			.forEach(System.out::print
/*
					c -> 
System.out.print(
"		case '" + c + "':\n" +
"			chars[i] = '';\n" +
"")*/
			);
	}

	@Test
	public void testShowWordTree(){
		System.out.println("showWordTree");
		engine.showWordTree(149684);
	}

	@Test
	public void testExtractAlters(){
		System.out.println("extractAlters");
		engine.extractAlters();
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
	static DbRecordsParser engine;

	@Test
	public void testFixWrongLinks() {
		System.out.println("fixWrongLinks");
		engine.fixWrongLinks();
	}

	@Test
	public void testCountLinkedWords(){
		System.out.println("countLinkedWords");
		engine.countLinkedWords();
		Integer a;
	}

	@Test
	public void testFixDelayedLinks(){
		System.out.println("fixDelayedLinks");
		engine.fixDelayedLinks();
	}

	@Test
	public void testMergeIsolatedAlters() throws Exception{
		System.out.println("mergeIsolatedAlters");
		engine.mergeIsolatedAlters();
	}

	@Test
	public void testShowFlags() throws Exception{
		System.out.println("showFlags");
		engine.showFlags();
	}
}
