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

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class Line extends Line2D.Double implements Comparable<Line> {

	public Line() {
	}

	public Line(Point p1, Point p2) {
		super(p1, p2);
		if (p1.compareTo(p2) >0){
			setLine(x2, y2, x1, y1);
		}
	}

	public Line(Point p1, Point p2, boolean root) {
		super(p1, p2);
		if (p1.compareTo(p2) >0){
			setLine(x2, y2, x1, y1);
		}
		this.root = root;
	}
	@Override
	public Point getP1() {
		return new Point(x1, y1);
	}

	@Override
	public Point getP2() {
		return new Point(x2, y2);
	}

	@Override
	public String toString() {
		return String.format("(%05.1f, %05.1f) - (%05.1f, %05.1f):%.0f", y1, x1, y2, x2, y2-y1);

	}

	boolean isCovered(Point p, double len){
		return y1 -1.3 < p.y && p.y < y2 && x1 < p.x + len / 3;
	}

	public boolean isRoot() {
		return root;
	}

	boolean rowSym;
	private boolean root;

/*
	@Override
	public int compareTo(Line o) {
		if (getP1().compareTo(o.getP2())>0){
			return 1;
		}
		if (getP2().compareTo(o.getP1())<0){
			return -1;
		}
		return (int)(x1 - o.x1);
	}

/**/
	@Override
	public int compareTo(Line o) {
		if (y2 < o.y1){
			return 1;
		}
		if (y1 > o.y2){
			return -1;
		}
		return (int)(x1 - o.x1);
	}
	
	int precXComp(Line o){
		return (int)((x1 - o.x1)/2.5);
	}

}
