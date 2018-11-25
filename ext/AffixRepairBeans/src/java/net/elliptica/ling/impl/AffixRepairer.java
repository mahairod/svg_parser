/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import net.elliptica.ling.db.AffixApplication;
import net.elliptica.ling.db.ComposedAffixAppl;
import net.elliptica.ling.db.NumRange;
import net.elliptica.ling.db.Аффикс;
import net.elliptica.ling.db.Слово;
import net.elliptica.ling.ejb.AffixConstraintsFailure;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class AffixRepairer {

	public AffixRepairer(EntityManager em) {
		this.em = em;
	}
	private final EntityManager em;

	private static final Pattern AFFIX_PATT;
	static {
		String blstr = "[]((?:[^\\[|]+|(?:[(\\]\\[]|[(´]\\[))+)(?:[]|\\||$)";
		AFFIX_PATT = Pattern.compile("([-\\|][\\[(]?" + blstr + "-(?![])|^" + blstr + "-|-" + blstr + ")");
	}
	private static final Pattern CLEAN_PATT = Pattern.compile("[^а-ёj]+");

	public void updateWordLine(Слово word, final String newLine_) throws AffixConstraintsFailure {
		if (word.getCompAffixApplications().size() != 1) {
			throw new AffixConstraintsFailure("Wrong number of composed affices: " + word.getCompAffixApplications().size());
		}
		final String newLine = newLine_.startsWith("") ? newLine_ : "" + newLine_;

		word = em.find(Слово.class, word.getId());

		Map<String,AffixApplication> curAffices = new HashMap<>();
		Map<String,String> typeMap = new HashMap<>();
		Matcher m = AFFIX_PATT.matcher(newLine);
		String type;
		final String[] types = {null, null, "infix","prefix","suffix"};
		while (m.find()) {
			String fullM = m.group(1);
			String[] groups = {null, null, m.group(2), m.group(3), m.group(4)};
			int ind = 0;
			for (; groups[ind] == null; ind++) {}
			short offs = (short) m.start(ind);
			type = types[ind];
			AffixApplication ап = AffixApplication.fromJava(offs, (short)(m.end(ind)-offs), groups[ind]);
			String key = key(newLine, ап.getOffs(), ап.getLen());
			typeMap.put(key, type);
			curAffices.put(key, ап);
		}
		Map<String,AffixApplication> oldAffices = word.getАффиксаПриложения().stream()
				.collect(Collectors.toMap(aa -> key(aa.getWord().getLine(), aa.getOffs(), aa.getLen()), aa->aa));

		Set<String> oldRest = new HashSet<>(oldAffices.keySet());
		Set<String> curCross = new HashSet<>(curAffices.keySet());
		oldRest.removeAll(curAffices.keySet());
		curCross.retainAll(oldAffices.keySet());

		Set<AffixApplication> restAas = oldRest.stream()
				.map(oldAffices::get)
				.collect(Collectors.toSet());
		for (AffixApplication aa: restAas) {
			em.remove(aa);
			word.getАффиксаПриложения().remove(aa);
		}

		Слово parentWord = em.createNamedQuery("Слово.findById", Слово.class).setParameter("id", 0).getSingleResult();

		Map<String,AffixApplication> resultAffApps = new TreeMap<>();
		Map<String,AffixApplication> newAffApps = new HashMap<>(curCross.size());
		for (Entry<String,AffixApplication> en: curAffices.entrySet()) {
			AffixApplication maa = en.getValue();
			if (curCross.contains(en.getKey())) {
				AffixApplication oaa = oldAffices.get(en.getKey());
				// just fill new info
				oaa.setLen(maa.getLen());
				oaa.setOffs(maa.getOffs());
				oaa.setOrig(maa.getOrig());
				resultAffApps.put(en.getKey(), oaa);
			} else {
				// add new
				Аффикс aff = findNewAffix(maa, typeMap.get(en.getKey()));
				newAffApps.put(en.getKey(), AffixApplication.copy(maa, aff, word));
			}
		}
		for (Entry<String,AffixApplication> en: newAffApps.entrySet()) {
			AffixApplication naa = en.getValue();
			// add new
			AffixApplication aa = new AffixApplication(null, naa.getOffs(), naa.getLen(), naa.getOrig());
			AffixApplication.AFF_SETTER.execute(aa, naa.getАффикс());
			AffixApplication.WORD_SETTER.execute(aa, naa.getWord());
			AffixApplication.PAR_WORD_SETTER.execute(aa, parentWord);
			em.persist(aa);
			resultAffApps.put(en.getKey(), aa);
		}

		{
			ComposedAffixAppl caa = word.getCompAffixApplications().iterator().next();
			int i=0;
			int prevEnd = 1;
			NumRange[] aff_locs = new NumRange[resultAffApps.size()];
			NumRange[] val_locs = new NumRange[resultAffApps.size()+1];
			for (Entry<String,AffixApplication> aaen: resultAffApps.entrySet()) {
				AffixApplication aa = aaen.getValue();
				aas[i].accept(caa, aa);
				as[i].accept(caa, aa.getАффикс());
				val_locs[i] = new NumRange(prevEnd, (int)aa.getOffs());
				aff_locs[i] = new NumRange((int)aa.getOffs(), prevEnd = aa.getOffs() + aa.getLen());
				i++;
			}
			val_locs[i] = new NumRange(prevEnd, null);
			for (; i< aas.length; i++) {
				aas[i].accept(caa, null);
				as[i].accept(caa, null);
			}
			caa.setAffLocs(aff_locs);
			caa.setValLocs(val_locs);
		}
		
		word.setLine(newLine);
	}

	private final BiConsumer<ComposedAffixAppl,AffixApplication> aas[] = fillSetters(ComposedAffixAppl.affappl1Setter, ComposedAffixAppl.affappl2Setter, ComposedAffixAppl.affappl3Setter);
	private final BiConsumer<ComposedAffixAppl,Аффикс> as[] = fillSetters(ComposedAffixAppl::setAffix1, ComposedAffixAppl::setAffix2, ComposedAffixAppl::setAffix3);

	private <R,P> BiConsumer<R,P>[] fillSetters(BiConsumer<R,P>... setters) {
		return setters;
	}

	private Аффикс findNewAffix(AffixApplication naa, String type) {
		String val = CLEAN_PATT.matcher(naa.getOrig()).replaceAll("");
		List<Аффикс> affs = em.createNamedQuery("Аффикс.findByVal").setParameter("val", val).getResultList();
		return affs.stream().filter(a-> a.getKind().equals(type))
				.findFirst().orElseGet(()-> new Аффикс(null, val, 1, type));
	}

	private String key(String src, int offs, int len) {
		offs--;
		String pref = src.substring(0, offs);
		String val = src.substring(offs, offs+len);
		pref = CLEAN_PATT.matcher(pref).replaceAll("");
		val = CLEAN_PATT.matcher(val).replaceAll("");
		return String.format("%1$2d-", pref.length()) + val;
	}
}
