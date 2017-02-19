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

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Vector;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class MorphemStreamEngine extends PDFGraphicsStreamEngine {

	public MorphemStreamEngine(PDDocument document) {
		super(null);
		this.document = document;
//		initJAXB();
		initJPA();
	}

	private void initJAXB(){
		try {
			JAXBContext jaxbc = JAXBContext.newInstance(xmlTypes);
			marshaller = jaxbc.createMarshaller();
//			marshaller.setProperty("jaxb.encoding", "Unicode");
		} catch (JAXBException ex) {
			Logger.getLogger(MorphemStreamEngine.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	private void initJPA(){
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("SVGParserPU");
		em = emf.createEntityManager();
	}

	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException {
/*
		ps.printf("appendRectangle %.2f %.2f, %.2f %.2f, %.2f %.2f, %.2f %.2f\n",
				p0.getX(), p0.getY(), p1.getX(), p1.getY(),
				p2.getX(), p2.getY(), p3.getX(), p3.getY());
*/
	}

	@Override
	public void drawImage(PDImage pdImage) throws IOException {
		ps.println("drawImage");
	}

	@Override
	public void clip(int windingRule) throws IOException {
//		ps.println("clip");
	}
	
	@Override
	public void moveTo(float x, float y) throws IOException {
		if (lineStart != null && LEFT_BORDER<x && x<RIGHT_BORDER){
			lineStart.setLocation(x, y);
//			ps.printf("moveTo %.2f %.2f\n", x, y);
			return;
		}
//		System.err.printf("moveTo %.2f %.2f\n", x, y);
	}

	@Override
	public void lineTo(float x, float y) throws IOException {
		if (lineStart != null && abs(lineStart.x)>1){
			if (abs(x - lineStart.x) < 0.5){
				verticalSeparators.add(new Line(lineStart, new Point(x, y)));
				ps.printf("line %.2f/%.2f %.2f/%.2f\n", lineStart.x, lineStart.y, x, y);
				lineStart = null;
				return;
			}
		}
		lineStart = null;
		System.err.printf("lineTo %.2f %.2f\n", x, y);
	}

	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {
		ps.printf("curveTo %.2f %.2f, %.2f %.2f, %.2f %.2f\n", x1, y1, x2, y2, x3, y3);
	}

	@Override
	public Point2D getCurrentPoint() throws IOException {
		// if you want to build paths, you'll need to keep track of this like PageDrawer does
		return new Point2D.Float(0, 0);
	}

	@Override
	public void closePath() throws IOException {
//		ps.println("closePath");
	}

	@Override
	public void endPath() throws IOException {
		lineStart = null;
//		ps.println("endPath");
	}

	@Override
	public void strokePath() throws IOException {
//		ps.println("strokePath");
		lineStart = new Point(0, 0);
	}

	@Override
	public void fillPath(int windingRule) throws IOException {
		ps.println("fillPath");
	}

	@Override
	public void fillAndStrokePath(int windingRule) throws IOException {
		ps.println("fillAndStrokePath");
	}

	@Override
	public void shadingFill(COSName shadingName) throws IOException {
		ps.println("shadingFill " + shadingName.toString());
	}

	/**
	 * Overridden from PDFStreamEngine.
	 */
	@Override
	public void showTextString(byte[] string) throws IOException {
		PDFont font = getGraphicsState().getTextState().getFont();
		if (font.getName().contains("PragmaticaC")) return;

		CharMapper mapper;
		{
			int style = 0;
			style |= font.getFontDescriptor().isItalic() ? 2 : 0;
			style |= font.getFontDescriptor().isForceBold()? 1 : 0;
			mapper = CharMapper.MAPPERS[style];
		}

		char[] chars = new char[string.length];
		for (int i=0; i<string.length; i++){
			int code = 0xFF & string[i];
			chars[i] = mapper.map(code, font);
		}

		String orig = new String(chars);
		String text = orig.trim();
		if (text.isEmpty()){
			return;
		}

		double x = getTextMatrix().getTranslateX();

		boolean startNL = false;
		if (isReference(text)){
			int refNum = Integer.parseInt(text);
			text = REF_SYMBOLS.substring(refNum, refNum+1);
		} else {
			startNL = goingDown();
		}
		if (!startNL /*&& !linestate.wordFinished()*/){
			if (x - linestate.coord.x > linestate.len + 20.){
				refreshAccum_("\t");
				printTabs(x-linestate.coord.x);
			}
			
		}

		double span = 0.;
		if (linestate.wordFinished()){
			String spanText = orig.substring(0, orig.indexOf(text));
			span = spanText.length() * 2.0;
		}
		if (startNL && !text.startsWith("→") && !isSeparatedWord(text)){
			if (!linestate.contLocker || x < linestate.coord.x){
				if (linestate.contLocker && x < linestate.coord.x){
					//save state for next found continuation
//					linestate.finishWord(true);
					pushState();
				}
				refreshAccum_("\n");
				ps.println();
				ps.print("\u001b[34;47m\t\t\t\u001b[0m");
				printTabs(x);
			}
		}
		// case of new line continuation
		boolean sep = false;
		if (linestate.wordFinished() && text.startsWith("→")){
			if (midState()){
				refreshAccum_("");
				popState();
			}
			sep = true;
			text = "";
		}

		linestate.coord.x += span;
		refreshAccum(mapper.getSing() + text, string);
		ps.print(mapper.getFormat() + text);
		if (sep){
			linestate.contin = true;
			sep = false;
		}
	}
	
	private PrintStream ps = System.out;
	
	private boolean isSeparatedWord(String text){
		double newX = getTextMatrix().getTranslateX();
		if (linestate.coord.x > newX || newX > linestate.coord.x+10. || linestate.accumulator.isEmpty() || text.isEmpty() ){
			return false;
		}
		return linestate.accumulator.endsWith("-");
		
	}

	private boolean goingDown(){
		Matrix lineM = getTextMatrix();
		Point curCoord = new Point(lineM.getTranslateX(), lineM.getTranslateY());
		boolean res;
		if (linestate.coord == null){
			linestate.coord = new Point(getTextMatrix().getTranslateX(), curCoord.y);
			printCoord(curCoord);
			res = false;
		} else {
			res = abs(curCoord.y - linestate.coord.y) > 0.05;
		}
		if (res) printCoord(curCoord);
		return res;
	}
	
	void printCoord(Point p){
//		ps.print("\u001b[35;47m\n" + p.toString() + "\u001b[0m");
	}

	void printTabs(double x){
		int offs = (int)(x/70);
		for (int i=0; i<offs; i++){
			ps.print("\t\t");
		}
	}

	private void insertSep(double offs) {
		Line l = new Line(linestate.coord, linestate.coord);
		l.x1 += offs;
		l.x2 += offs;
		l.y2 += 5;
		l.rowSym = true;
		verticalSeparators.add(l);
		if (linestate.contin){
			verticalSeparators.remove(linestate.savedGroup);
			linestate.contin = false;
		}
		linestate.savedGroup = l;
	}

	class LineState implements Cloneable {
		private boolean contLocker = false;
		private boolean wordFinished = false;
		private boolean contin = false;
		private String accumulator = "";
		private String prepared = null;
		Line savedGroup;
		private double len = 0;
		private Word previous;
		Point coord;

		public LineState clone() throws CloneNotSupportedException {
			return (LineState) super.clone();
		}
		
		double calcLength(byte[] codes) throws IOException {
			if (codes == null){
				return 0.;
			}
			double res = 0.;
	        PDTextState textState = getGraphicsState().getTextState();
			PDFont font = textState.getFont();
			float fontSize = textState.getFontSize();
			float horizontalScaling = textState.getHorizontalScaling() / 100f;
			float charSpacing = textState.getCharacterSpacing();
			double scale = getTextMatrix().getScaleX();
			for (byte code: codes){
	            Vector w = font.getDisplacement(code);
                double tx = scale * (w.getX() * fontSize + charSpacing) * horizontalScaling;
				res += tx;
			}
			return res;
		}
		void appendCodes(byte[] codes) throws IOException{
			len += calcLength(codes);
		}
		void setCodes(byte[] codes) throws IOException{
			len = calcLength(codes);
		}

		void resetContinue(){
			if (wordFinished && !contLocker){
				this.previous = null;
			} else {
				contLocker = false;
			}
			wordFinished = false;
			contin = false;
		}
		void finishWord(boolean contLine){
			wordFinished = true;
			contLocker = contLine;
		}
		boolean wordFinished(){
			return wordFinished;
		}

		@Override
		public String toString() {
			return (wordFinished? "word":"w̶o̶r̶d̶") + ","+
					(contLocker? "locked" : "l̶o̶c̶k̶e̶d̶") +
					"[" + accumulator + "]->" +
					previous;
		}
		
	}

	private void refreshAccum_(String newVal) throws IOException{
		refreshAccum(newVal, null);
	}
	private void refreshAccum(String newVal, byte[] codes) throws IOException{
		if (!linestate.wordFinished()){
			String v = newVal.trim();
			if (newVal.equals("\t")){
				linestate.finishWord(true);
			} else if (v.startsWith("→")){
				linestate.finishWord(true);
				insertSep(linestate.len);
				if (v.length()>1){
					String next = v.substring(1).trim();
					if (!"•".equals(next)){
						linestate.prepared = v.substring(1);
					}
				}
			} else if (newVal.endsWith("\n")){
				linestate.finishWord(false);
			} else {
				linestate.accumulator += newVal;
				linestate.appendCodes(codes);
			}
		} else {
			Word w = new Word(linestate.accumulator, linestate.previous, linestate.coord, linestate.len);
			// save state
			textsRegions.put(linestate.coord, w);
			if ("\t".equals(newVal)) newVal = "";
			if (linestate.prepared != null){
				linestate.accumulator = linestate.prepared + newVal;
				linestate.prepared = null;
			} else {
				linestate.accumulator = newVal;
			}
			linestate.previous = w;
			linestate.setCodes(codes);
			if (linestate.contin){
				insertSep(0);
			}

			// reset state
			linestate.resetContinue();
			Matrix trM = getTextMatrix();
			if (trM==null) return;
			linestate.coord = new Point(trM.getTranslateX(), trM.getTranslateY());
		}
	}
	
	private boolean isReference(String text){
		boolean ref = isReference_(text);
		if (!ref){
			avgFontScale.increment( getTextMatrix().getScalingFactorX() );
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
		double scaleX = getTextMatrix().getScalingFactorX();
		return linestate.coord.y < getTextMatrix().getTranslateY() && scaleX / avgFontScale.getResult() < 0.7;
	}

	private final NavigableSet<Line> verticalSeparators = new TreeSet<>();
	private Point lineStart;
	LineState linestate = new LineState();
	private static final double LEFT_BORDER = 38.0;
	private static final double RIGHT_BORDER = 301.0;

	Stack<LineState> lineStates = new Stack<>();
	
	NavigableMap<Point,Word> textsRegions = new TreeMap<>();
	Mean avgFontScale;
	private final static String REF_SYMBOLS = "⁰¹²³⁴⁵⁶⁷⁸⁹";
	
	void pushState(){
		try {
			lineStates.push(linestate.clone());
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(MorphemStreamEngine.class.getName()).log(Level.SEVERE, null, ex);
		}
		linestate.resetContinue();
		linestate.accumulator = "";
		linestate.previous = null;
	}
	void popState(){
		linestate = lineStates.pop();
	}
	boolean midState(){
		return !lineStates.empty();
	}
	
	private void shiftRight(double offset) throws IOException{
        PDTextState textState = getGraphicsState().getTextState();
        float fontSize = textState.getFontSize();
        float horizontalScaling = textState.getHorizontalScaling() / 100f;
        float tx = (float)(offset * fontSize * horizontalScaling);
		applyTextAdjustment(tx, 0);
		if (offset > 1.0){
			if (offset > 5.0){
//				linestate.finishWord(true);
//				printTabs(offset*13);
			}
		}
	}
	
	@Override
	public void showTextStrings(COSArray array) throws IOException {
		for (COSBase base: array){
			if (base instanceof COSNumber){
				double dx = -((COSNumber)base).doubleValue() / 1000.0;
				shiftRight(dx);
			}
			if (base instanceof COSString){
				byte codes[] = ((COSString)base).getBytes();
				showTextString(codes);
				float dx = (float) (linestate.calcLength(codes) / getTextMatrix().getScaleX());
				applyTextAdjustment(dx, 0);
			}
		}
	}

	void saveState() throws IOException{
		refreshAccum_("\n");
		refreshAccum_("");
		NavigableSet<Bunch> groups = new TreeSet<>();
		verticalSeparators.add(new Line(new Point(0, 0), new Point(0, 470.), true));

		for (Line l: verticalSeparators){
//			ps.println(l);

			Bunch group = new Bunch(l);

			int counter = 0;
			for (Map.Entry<Point,Word> entry: textsRegions.entrySet()){
				Point loc = entry.getKey();
				counter++;
				Word w = entry.getValue();
				if (!l.isCovered(loc, w.getLen())){
					continue;
				}
				Bunch og = w.getGroup();
				if (og== null || og.getGroupLine().compareTo(l) <0){
					w.setGroup(group);
				}
			}
			if (!group.words.isEmpty()){
				groups.add(group);
			}
		}

		Iterator<Bunch> bIt = groups.iterator();
		while (bIt.hasNext()) {
			Bunch bunch = bIt.next();
			if (bunch.words.isEmpty()){
				bIt.remove();
			}
			bunch.page = page;
		}
		
		int regroupTryCount = 10;

	do{
		for (Bunch bunch: groups){
			Word parent = null;
			Word grandpa = null;
			for (Word word: bunch.words){
				if (word.getBase()!=null){
					Word parentCand = word.getBase();
					if (parentCand.getDerived()!=null){
						continue;
					}
					if (parent==null){
						parent = parentCand;
					}else if (parent.getGroup().compareTo(parentCand.getGroup()) <0 ){
						grandpa = parent;
						parent = parentCand;
					} else {
						if (grandpa==null || grandpa.getGroup().compareTo(parentCand.getGroup()) <0 ){
							grandpa = parentCand;
						}
					}
				}
			}
			if (parent!=null){
				parent.setDerived(bunch);
				bunch.setParent(parent);

				Bunch parBunch = parent.getGroup();
				if (grandpa != null && !parBunch.isRoot() && parBunch.parent == null){
					parBunch.parent = grandpa;
				}
			}
		}
		List<Bunch> freeBunches = groups.stream().filter(g -> g.parent == null).collect(Collectors.toList());
		if (freeBunches.size() > 1){
			LOG.log(Level.WARNING, "Too many root groups on page {0}", page);
			regroupTryCount--;
			for (Bunch bunch: freeBunches){
				if (bunch.isRoot()){
					continue;
				}
				Word w = searchRightmostWord(bunch);
				if (w==null) continue;
				Bunch prev = w.getDerived();
				if (prev==null || bunch.compareTo(prev) <=0 || prev==bunch ){
					if (prev!=null && prev!=bunch){
						prev.setParent(null);
					}
					w.setDerived(bunch);
					bunch.setParent(w);
				}
			}
		} else {
			regroupTryCount = 0;
		}
	} while (regroupTryCount>0);
		
		int overall = groups.stream().mapToInt(g -> g.words.size()).sum();
		pageGroups.put(page, groups);

//		saveXML();
		saveDB();

		verticalSeparators.clear();
		textsRegions.clear();
		linestate = new LineState();
		lineStates.clear();
	}

	private Word searchRightmostWord(Bunch bunch){
		Line bline = bunch.getGroupLine();
		Word word = null;
		for (Map.Entry<Point,Word> entry: textsRegions.entrySet()){
			Word w = entry.getValue();
			if (w.getGroup() == bunch) continue;
			Point loc = entry.getKey();
			Line wline = new Line(loc, loc);
			if (wline.compareTo(bline) >=0 || loc.y > bline.y2 || wline.x1 > bline.x1) continue;
			if (word==null || w.compHPos(word) >0){
				word = w;
			}
		}
		return word;
	}
	private void saveXML(){
		Set<Bunch> groups = pageGroups.get(page);
		try {
			Root r = new Root();
			r.groups = new ArrayList<>(groups);
			marshaller.marshal(r, new File("xml/output-" + page + ".xml"));
		} catch (JAXBException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	private void saveDB(){
		Set<Bunch> groups = pageGroups.get(page);
		em.getTransaction().begin();
		try{
			for (Bunch group: groups){
				if (group.words.isEmpty()){
					continue;
				}
//					em.persist(group);
			}
			em.getTransaction().commit();
		} catch (Exception ex){
			em.getTransaction().rollback();
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	Map<Integer,Set<Bunch>> pageGroups = new HashMap<>(600);
	private Integer page;
	private PDPage pageContent;
	private final PDDocument document;
	
	@XmlRootElement
	static class Root {
		@XmlElement(name = "group")
		@XmlElementWrapper
		List<Bunch> groups;
	}

	private final Class[] xmlTypes = {Bunch.class, Word.class, ArrayList.class, Root.class};

	private Marshaller marshaller;
	private EntityManager em;

	static final Logger LOG = Logger.getLogger(MorphemStreamEngine.class.getName());

	void setPage(int page){
		this.page = page;
		pageContent = document.getPage(page);
	}
	
	void process() throws IOException{
		processPage(pageContent);
	}
	
	void reparseWords(){
		CriteriaQuery<Word> cq = em.getCriteriaBuilder().createQuery(Word.class);
		cq.select(cq.from(Word.class));
		List<Word> words = em.createQuery(cq).getResultList();

		List<Word> prepared = new ArrayList<>(20);
		
		for (Word word: words){
			String line = word.getLine();
			String[] parts = line.split("\u001b[\u001c-\u001f]");
			String text = "";
			for (String part: parts){
				text += part.replaceAll("´", "");
			}
			String modif = text
					.replaceAll("-", "")
					.replace("|", "")
					.replace("/", "")
					.replaceAll("(.+)\\(([а-я]+)\\)$", "$1$2")
					.replace("и[jэ]", "ие")
					.replace("и[jа]", "ия")
					.replace("[jи]", "ьи")
					.replaceAll("^•", "");
			ps.print(text);
			ps.print("\t\t");
			ps.println(modif);

			word.setText(modif);
			prepared.add(word);

			if (prepared.size()>19){
				saveWords(prepared);
			}
		}
		if (!prepared.isEmpty()){
			saveWords(prepared);
		}
	}
	
	private void saveWords(List<Word> words){
		em.getTransaction().begin();
		try{
			for (Word w: words){
				em.persist(w);
			}
			em.getTransaction().commit();
			words.clear();
		} catch (Exception ex){
			em.getTransaction().rollback();
			LOG.log(Level.SEVERE, null, ex);
		}
	}
}
