/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
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
public class SingleWordBunch {
	private Bunch bunch;
	private final Word child;

	public SingleWordBunch(Word child) {
		this.child = child;
	}

	public Bunch getBunch() {
		if (bunch!=null) {
			return bunch;
		}
		Point wPos = child.getPoint();
		bunch = new Bunch(new Line(wPos, new Point(wPos.x, wPos.y+8)));
		Bunch curGroup = child.getGroup();
		bunch.page = curGroup.page;
//		bunch.words.add(child);
		child.setGroup(bunch);
		return bunch;
	}
}
