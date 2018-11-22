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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import net.elliptica.ling.db.AffixApplication;
import net.elliptica.ling.db.Аффикс;
import net.elliptica.ling.db.Слово;

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

	public void updateWordLine(final Слово word, final String newLine) {
		Map<String,AffixApplication> curAffices = new HashMap<>();
		Map<String,String> typeMap = new HashMap<>();
		Matcher m = AFFIX_PATT.matcher(newLine);
		String type;
		final String[] types = {"infix ","prefix","suffix"};
		while (m.find()) {
			String fullM = m.group(1);
			String[] groups = {m.group(1), m.group(2), m.group(3)};
			int ind = 0;
			for (; groups[ind] == null; ind++) {}
			short offs = (short) m.start(ind);
			type = types[ind];
			AffixApplication ап = new AffixApplication(null, offs, (short)(m.end(ind)-offs), groups[ind]);
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

		Set<Integer> restIds = oldRest.stream()
				.map(oldAffices::get).map(AffixApplication::getId)
				.collect(Collectors.toSet());
		if (!restIds.isEmpty()) {
			word.getАффиксаПриложения().removeIf(of->restIds.contains(of.getId()));
		}
		for (Map.Entry<String,AffixApplication> en: curAffices.entrySet()) {
			AffixApplication maa = en.getValue();
			if (curCross.contains(en.getKey())) {
				AffixApplication oaa = oldAffices.get(en.getKey());
				// just fill new info
				oaa.setLen(maa.getLen());
				oaa.setOffs(maa.getOffs());
				oaa.setOrig(maa.getOrig());
			} else {
				// add new
				setNewAffix(maa, typeMap.get(en.getKey()));
				maa.setWord(word);
				word.getАффиксаПриложения().add(maa);
			}
		}
	}
	
	private void setNewAffix(AffixApplication naa, String type) {
		String val = CLEAN_PATT.matcher(naa.getOrig()).replaceAll("");
		List<Аффикс> affs = em.createNamedQuery("Аффикс.findByVal").setParameter("val", val).getResultList();
		Аффикс aff = affs.stream().filter(a-> a.getKind().equals(type))
				.findFirst().orElseGet(()-> new Аффикс(null, val, 1, type));
		naa.setАффикс(aff);
	}

	private String key(String src, int offs, int len) {
		String pref = src.substring(0, offs);
		String val = src.substring(offs, offs+len);
		pref = CLEAN_PATT.matcher(pref).replaceAll("");
		val = CLEAN_PATT.matcher(val).replaceAll("");
		return String.format("%1$2d-", pref.length()) + val;
	}
}
