/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */
package net.elliptica.ling.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import net.elliptica.ling.impl.AffixRepairer;
import net.elliptica.ling.db.Слово;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Stateless
public class AffixRepairBean implements AffixRepairBeanRemote, AffixRepairBeanLocal {
	@PersistenceContext(unitName = "AffixRepairBeansPU")
	private EntityManager em;

	private AffixRepairer repairer;

	@PostConstruct
	public void init() {
		repairer = new AffixRepairer(em);
	}

	@Override
	@Transactional
	public void updateWordLine(final Слово word, final String newLine) {
		repairer.updateWordLine(word, newLine);
	}

}
