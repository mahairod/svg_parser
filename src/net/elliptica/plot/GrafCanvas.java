/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import static net.elliptica.plot.GrafCanvas.OPERATION_MODE.*;
import net.elliptica.svg.DbRecordsParser;
import net.elliptica.svg.Word;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class GrafCanvas extends JPanel {
	public GrafCanvas(MorphemsFrame frame) {
		dao = new DbRecordsParser();
		parent = frame;
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				Word word = findWord(e.getX(), e.getY());
				selectWord(word);
			}
			
		});
	}
	
	enum OPERATION_MODE {
		SELECTION("выбор слова"),
		MOVE("перемещение в группу"),
		LINKAGE("призывка к предку"),
		DELINK("отвязка"),
		DESTROY("удаление", true),
		GROUP("группировка");

		private OPERATION_MODE(String descr) {
			this(descr, false);
		}
		private OPERATION_MODE(String descr, boolean singleOp) {
			this.descr = descr;
			this.singleOp = singleOp;
		}
		private final String descr;
		final boolean singleOp;

		@Override
		public String toString() {
			return " «" + descr + "» ";
		}
	}
	
	private OPERATION_MODE currentOperationMode = OPERATION_MODE.SELECTION;

	private final DbRecordsParser dao;
	private final MorphemsFrame parent;
	private volatile List<Word> words;
	private volatile int page;
	private volatile Word currentWord;
	
	private Map<Integer, Color> colors = new HashMap<>();
	private List<Word> wordCoordGrid[][];
	private static int COORD_GRID_STEP = 20;

	private Color color;
	private Color[] colrArr = {};
	
	private void clean(){
		wordCoordGrid = null;
		colors.clear();
		LOG.log(Level.INFO, "clean");
	}
	
	public void setOperationMode(OPERATION_MODE operation_mode) {
		currentOperationMode = operation_mode;
	}

	void finishOperation(boolean commit) {
		LOG.log(Level.INFO, "finishOperation");
		if (commit) {
			dao.doCommit();
		} else {
			dao.doCancel();
		}
		fullUpdate();
	}

	{
		int [] colorLevels = {50, 150, 180, 210};
		List<Color> colorList = new ArrayList<>(27);
		for (int i=0; i<4*4*4; i++) {
			int r = colorLevels[ i&3 ];
			int g = colorLevels[ (i>>2)&3 ];
			int b = colorLevels[ (i>>4)&3 ];
			if (r+g+b > 300) {
				colorList.add(new Color(r, g, b));
			}
		}
		colrArr = colorList.toArray(colrArr);
	}
	
	public boolean hasSelection() {
		return currentWord != null;
	}
	
	void selectWord(Word w) {
		switch (currentOperationMode) {
		case SELECTION:
			currentWord = w;
			GrafCanvas.this.revalidate();
			GrafCanvas.this.repaint();
			parent.onSelected(w!=null);
			return;
		case MOVE:
		case LINKAGE:
		case DELINK:
		case DESTROY:
		case GROUP:
			if (!doOperation(currentOperationMode, w)) {
				if (w!=null) {
					// reset
					currentOperationMode = SELECTION;
					parent.resetOperation();
				} else {
					parent.onSelected(currentWord != null);
				}
			} else {
				currentOperationMode = SELECTION;
				parent.onSelected(false);
				this.revalidate();
				this.repaint();
			}
			return;
		}
	}
	
	private boolean doOperation(OPERATION_MODE operMode, Word w) {
		if ((!operMode.singleOp && currentWord==null) || w==null || operMode==SELECTION) {
			return false;
		}
		String text = "Вы хотите провести операцию" + operMode + "с параметрами " +
				currentWord + " -> " + w + "?";
		int answer = JOptionPane.showConfirmDialog(null, text,
				"Подтвердите операцию", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) {
			return false;
		}
		try {
			switch (operMode) {
			case MOVE:
				dao.doMove(currentWord, w);
				break;
			case LINKAGE:
				dao.doLink(currentWord, w);
				break;
			case GROUP:
				dao.doGroup(currentWord, w);
				break;
			case DESTROY:
				dao.doDestroy(w);
				break;
			default:
				return false;
			}
		} catch (IllegalStateException | IllegalArgumentException ex) {
			parent.enableAbort();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка при сохранении", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		parent.onConfirmRequest();
		return true;
	}

	public void setPage(int page) {
		this.page = page;
		words = dao.getWordsFromPage(page);
		clean();
		revalidate();
		repaint();
	}

	private void fullUpdate() {
		LOG.log(Level.INFO, "fullUpdate");
		setPage(page);
	}

	void flushDB() {
		dao.doFlush();
		fullUpdate();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		String label;
		if (currentWord != null) {
			label = "Текущее слово: " + currentWord.toString();
		} else {
			label = "Не выбрано";
		}
		g.drawString(label, 40, 30);
		List<Word> data = words;
		if (words == null || words.isEmpty()){
			return;
		}
		{
			int height = getHeight();
			int width = getWidth();
			wordCoordGrid = new List[width/COORD_GRID_STEP+1][height/COORD_GRID_STEP+1];
		}
		for (Word w: data) {
			if (w.getDerived()!=null) {
				for (Word ch: w.getDerived().getWords()) {
					if (ch.isDeprecated()) continue;
					Point2D pLink = w.getMiddleEnd();
					Point2D chLink = ch.getPoint();
					chLink = new Point2D.Double(chLink.getX(), chLink.getY()-4.0);
					if (pLink.getX() > chLink.getX() && (abs(pLink.getX()-chLink.getX())/(pLink.getY()-chLink.getY())) > 3 ) {
						chLink = ch.getMiddleEnd();
					}
					drawLink(g, pLink, chLink);
				}
			}
			getColorForGroup(w.getGroup().getId());
			drawWord(g, w);
		}
	}
	
	private Color getColorForGroup(Integer grId) {
		grId = grId==null? 0 : grId;
		color = colors.get(grId);
		if (color == null) {
			int ind = (int) Math.floor(Math.random()*colrArr.length);
			color = colrArr[ind];
			colors.put(grId, color);
		}
		return color;
	}

	private void drawWord(Graphics g, Word w) {
		Point2D p = w.getPoint();
		PointI sp = translateCoord(w.getPoint());
		PointI ep = translateSize(w.getLen(), 9);
		{
			int height = getHeight();
			int width = getWidth();
			int ex = sp.x + ep.x;
			int ey = sp.y + ep.y;
			for (int i = sp.x/COORD_GRID_STEP; i <= ex/COORD_GRID_STEP && i < width/COORD_GRID_STEP; i++) {
				for (int j = sp.y/COORD_GRID_STEP; j <= ey/COORD_GRID_STEP && j < height/COORD_GRID_STEP; j++) {
					List<Word> indexList = wordCoordGrid[i][j];
					if (indexList == null) {
						wordCoordGrid[i][j] = indexList = new ArrayList<>();
					}
					indexList.add(w);
				}
			}
		}
		g.setColor(color.brighter());
		g.fillRoundRect(sp.x, sp.y, ep.x, ep.y, 4, 4);
		g.setColor(w != currentWord ? color : Color.MAGENTA);
		g.drawRoundRect(sp.x, sp.y, ep.x, ep.y, 4, 4);
		g.setColor(Color.BLACK);
		g.drawString(w.getLine().replaceAll("[´]", ""), sp.x, sp.y+11);
	}
	
	private void drawLink(Graphics g, Point2D src, Point2D dst) {
		PointI sp = translateCoord(src);
		PointI dp = translateCoord(dst);
		g.drawLine(sp.x, sp.y, dp.x, dp.y);
	}

	PointI translateSize(double x, double y) {
		int bottom = this.getHeight();
		double koeff = bottom / 470.;
		Point2D p = new Point2D.Double(koeff * 1.8 * x, koeff * y);
		return new PointI(p);
	}

	PointI translateCoord(double x, double y) {
		PointI sz = translateSize(x, y);
		int bottom = this.getHeight();
		sz.y = bottom - sz.y;
		return sz;
	}

	PointI translateCoord(Point2D logical) {
		return translateCoord(logical.getX(), logical.getY());
	}
	
	private Word findWord(int x, int y) {
		List<Word> cellWords = wordCoordGrid[x/COORD_GRID_STEP][y/COORD_GRID_STEP];
		if (cellWords == null || cellWords.isEmpty()) {
			return null;
		}
		for (Word w: cellWords) {
			PointI sp = translateCoord(w.getPoint());
			PointI ep = translateSize(w.getLen(), 9);
			int x_ = x - sp.x;
			int y_ = y - sp.y;
			if (0 <= x_ && x_ <= ep.x && 0 <= y_ && y_ <= ep.y) {
				return w;
			}
		}
		return null;
	}
	
	private void guiWork(Runnable task) {
		SwingUtilities.invokeLater(task);
	}

	static class PointI {
		int x,y;

		public PointI(Point2D p) {
			this.x = (int) p.getX();
			this.y = (int) p.getY();
		}

		@Override
		public String toString() {
			return "PointI{" + x + ", " + y + '}';
		}
		
		
	}
	static final Logger LOG = Logger.getLogger(GrafCanvas.class.getName());
}
