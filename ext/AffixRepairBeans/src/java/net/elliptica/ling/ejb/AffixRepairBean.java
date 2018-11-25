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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import net.elliptica.ling.RangeArrConverter;
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
	public void updateWordLine(final Слово word, final String newLine) throws AffixConstraintsFailure {
		repairer.updateWordLine(word, newLine);
	}

	@Override
	@Transactional
	public void updateWordLine(String newLine, Слово word) throws AffixConstraintsFailure {
		if (word == null || word.getCompAffixApplications()==null) {
			throw new AffixConstraintsFailure("Not valid word object: " + word);
		}

		try {
			repairer.updateWordLine(word, newLine);
		} catch (StringIndexOutOfBoundsException ex) {
			final String msg = "Incorrect string supplied";
			LOG.log(Level.SEVERE, msg, ex);
			throw new AffixConstraintsFailure(msg, ex);
		}
	}

	private static final Logger LOG = Logger.getLogger(RangeArrConverter.class.getName());
}
