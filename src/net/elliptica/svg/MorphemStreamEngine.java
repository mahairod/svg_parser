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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.state.PDTextState;
import org.apache.pdfbox.util.Matrix;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class MorphemStreamEngine extends PDFGraphicsStreamEngine {

	public MorphemStreamEngine(PDPage page) {
		super(page);
	}

	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
		System.out.printf("appendRectangle %.2f %.2f, %.2f %.2f, %.2f %.2f, %.2f %.2f\n",
				p0.getX(), p0.getY(), p1.getX(), p1.getY(),
				p2.getX(), p2.getY(), p3.getX(), p3.getY());
	}

	@Override
	public void drawImage(PDImage pdImage) throws IOException {
		System.out.println("drawImage");
	}

	@Override
	public void clip(int windingRule) throws IOException {
		System.out.println("clip");
	}
	
	@Override
	public void moveTo(float x, float y) throws IOException {
		if (lineStart != null && LEFT_BORDER<x && x<RIGHT_BORDER){
			lineStart.setLocation(x, y);
			System.out.printf("moveTo %.2f %.2f\n", x, y);
			return;
		}
		System.err.printf("moveTo %.2f %.2f\n", x, y);
	}

	@Override
	public void lineTo(float x, float y) throws IOException {
		if (lineStart != null && abs(lineStart.x)>1){
			if (abs(x - lineStart.x) < 0.5){
				verticalSeparators.add(new Line(lineStart, new Point(x, y)));
				System.out.printf("lineTo %.2f %.2f\n", x, y);
				lineStart = null;
				return;
			}
		}
		lineStart = null;
		System.err.printf("lineTo %.2f %.2f\n", x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
		System.out.printf("curveTo %.2f %.2f, %.2f %.2f, %.2f %.2f\n", x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException {
		// if you want to build paths, you'll need to keep track of this like PageDrawer does
		return new Point2D.Float(0, 0);
	}

	@Override
	public void closePath() throws IOException {
		System.out.println("closePath");
	}

	@Override
	public void endPath() throws IOException {
		lineStart = null;
		System.out.println("endPath");
	}

	@Override
	public void strokePath() throws IOException {
		System.out.println("strokePath");
		lineStart = new Point(0, 0);
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		System.out.println("fillPath");
	}

	@Override
	public void fillAndStrokePath(int windingRule) throws IOException {
		System.out.println("fillAndStrokePath");
	}

	@Override
	public void shadingFill(COSName shadingName) throws IOException {
		System.out.println("shadingFill " + shadingName.toString());
	}

	/**
	 * Overridden from PDFStreamEngine.
	 */
	@Override
	public void showTextString(byte[] string) throws IOException {
		PDTextState textState = this.getGraphicsState().getTextState();
		PDFont font = textState.getFont();
		if (font.getName().contains("PragmaticaC")) return;

		char[] chars = new char[string.length];

		int style = 0;
		style |= font.getFontDescriptor().isItalic() ? 2 : 0;
		style |= font.getFontDescriptor().isForceBold()? 1 : 0;

		CharMapper mapper = CharMapper.MAPPERS[style];
		for (int i=0; i<string.length; i++){
			int code = 0xFF & string[i];
			chars[i] = mapper.map(code, font);
		}

		String orig = new String(chars);
		String text = orig.trim();

		boolean startNL = false;
		if (isReference(text)){
			int refNum = Integer.parseInt(text);
			text = REF_SYMBOLS.substring(refNum, refNum+1);
		} else {
			startNL = goingDown();
		}

		
		double span = 0.;
		if (breakNext){
			String spanText = orig.substring(0, orig.indexOf(text));
			span = spanText.length() * 2.0;
		}
		refreshAccum(mapper.getSing() + text);
		if (startNL){
			resetContinue();
			System.out.println();
		}
		coord.x += span;
		System.out.print(mapper.getFormat() + text);

	}
	
	private boolean goingDown(){
		double newY = getTextLineMatrix().getTranslateY();
		if (coord == null){
			coord = new Point(getTextLineMatrix().getTranslateX(), newY);
			return false;
		} else {
			if (abs(newY - coord.y) > 0.05){
				breakNext = true;
				return true;
			} else {
				return false;
			}
		}
	}
	
	private void resetContinue(){
		if (!contLocker){
			continuation = false;
		} else {
			contLocker = false;
		}
	}
	
	private void refreshAccum(String newVal){
		if (!breakNext){
			if (newVal.trim().endsWith("→")){
				breakNext = true;
				continuation = true;
				contLocker = true;
			} else {
				accumulator += newVal;
			}
		} else {
			Word w = new Word(accumulator, continuation? previous:null);
			// save state
			textsRegions.put(coord, w);
			accumulator = newVal;

			// reset state
			contLocker = false;
//			continuation = false;
			previous = continuation ? w : null;
			breakNext = false;
			Matrix trM = getTextLineMatrix();
			coord = new Point(trM.getTranslateX(), trM.getTranslateY());
		}
	}
	
	private boolean isReference(String text){
		boolean ref = isReference_(text);
		if (!ref){
			avgFontScale.increment( getTextLineMatrix().getScalingFactorX() );
		}
		return ref;
	}
	private boolean isReference_(String text){
		if (avgFontScale == null){
			avgFontScale = new Mean();
			return false;
		}
		if (text.length()>1) return false;
		try{
			Integer.parseInt(text);
		} catch (NumberFormatException ex){
			return false;
		}
		double scaleX = getTextLineMatrix().getScalingFactorX();
		return coord.y < getTextLineMatrix().getTranslateY() && scaleX / avgFontScale.getResult() < 0.7;
	}

	private final List<Line2D> verticalSeparators = new ArrayList<>();
	private Point lineStart;
	private boolean contLocker = false;
	private boolean continuation = false;
	private Word previous;
	private static final double LEFT_BORDER = 38.0;
	private static final double RIGHT_BORDER = 301.0;


	private String accumulator = "";
	Point coord;
	boolean breakNext = false;
	Map<Point,Word> textsRegions = new TreeMap<>();
	Mean avgFontScale;
	private final static String REF_SYMBOLS = "⁰¹²³⁴⁵⁶⁷⁸⁹";
	
	
	@Override
	public void showTextStrings(COSArray array) throws IOException {
		for (COSBase base: array){
			if (base instanceof COSString){
				showTextString(((COSString)base).getBytes());
			}
		}
	}

}
