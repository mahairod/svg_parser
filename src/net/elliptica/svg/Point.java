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

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class Point extends java.awt.geom.Point2D.Double implements Comparable<Point> {

	public Point() {
	}

	public Point(double x, double y) {
		super(x, y);
	}

	@Override
	public int compareTo(Point o) {
		if (Math.abs(y-o.y)>0.05){
			return (int)(20*(y-o.y));
		}
		return (int)(x-o.x);
	}

	@Override
	public String toString() {
		return String.format("(%06.2f, %06.2f)", x, y);
	}

	Line toLine(){
		return new Line(this, this);
	}
}
