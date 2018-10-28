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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import net.elliptica.svg.DbRecordsParser.Conditions;
import net.elliptica.svg.DbRecordsParser.EntityProcessor;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public class RuntimeCode {
	public static EntityManager em;
	public static TypedQuery<Word> tq;

	public static int procedure0 (Word w) {
		Word[] par = {null};
		Line curGrPos = w.getGroup().getGroupLine();
		selectOne(Word.class, (cb, root) -> cb.and(
			cb.equal(root.get(Word_.bunch).get(Bunch_.page), w.getGroup().page),
			cb.gt(root.get(Word_.y), curGrPos.y1),
			cb.lt(root.get(Word_.y), curGrPos.y2),
			cb.lt(root.get(Word_.x), w.x -10.)
		)).process(parent -> {
			par[0] = parent;
		});

		List<Bunch> groups = groups((cb, root) -> cb.and(
			cb.equal(root.get(Bunch_.page), w.getGroup().page),
			cb.gt(root.get(Bunch_.x), w.x + 10),
			cb.gt(root.get(Bunch_.y), curGrPos.y1),
			cb.lt(cb.sum(root.get(Bunch_.y), root.get(Bunch_.height)), curGrPos.y2)
		));
		boolean skip = true;
		for (Bunch candGr: groups) {
			if (skip) continue;;

//			w.setGroup(candGr);
			w.setDerived(candGr);
			break;
		}
		
		Word p = par[0];
		p.setDerived(w.getGroup());
		
		return -1;
	}
	
	private static List<Bunch> groups(Conditions conditions) {
		List<Bunch> groups = new ArrayList<>();
		selectList(Bunch.class, 500, conditions).accept(gs-> groups.addAll(gs));
		return groups;
	}
	public static int procedure1 (Word w) {
		Word[] par = {null};
		selectOne(Word.class, (cb, root) -> cb.and(
			cb.equal(root.get(Word_.bunch).get(Bunch_.page), w.getGroup().page),
			cb.gt(root.get(Word_.y), w.getGroup().getGroupLine().y1),
			cb.lt(root.get(Word_.y), w.getGroup().getGroupLine().y2),
			cb.lt(root.get(Word_.x), w.x-9)
		)).process(parent -> {
			par[0] = parent;
		});
		// new group
		SingleWordBunch der = null;
		for (Word ch: w.getGroup().words) {
			boolean apply = false;
			if (apply) {
				der = new SingleWordBunch(ch);
				break;
			}
		}
		Bunch.SEQUENCE = maxGroup() + 1;
		w.setDerived(der== null? null : der.getBunch());

		// cur group should be derived, and word's derive should be created
		Word p = par[0];
		p.setDerived(w.getGroup());
		return -1;
	}
	public static int procedure2 (Word w) {
		Bunch curGr = w.getGroup();
		Line curGrPos = curGr.getGroupLine();

		List<Bunch> roots = groups((cb, root) -> cb.and(
			cb.equal(root.get(Bunch_.page), w.getGroup().page),
			cb.isTrue(root.get(Bunch_.root))
		));

		List<Bunch> groups = groups((cb, root) -> cb.and(
			cb.equal(root.get(Bunch_.page), curGr.page),
			cb.gt(root.get(Bunch_.x), w.x + 10),
			cb.gt(root.get(Bunch_.y), curGrPos.y1),
			cb.lt(cb.sum(root.get(Bunch_.y), root.get(Bunch_.height)), curGrPos.y2 + 5.)
		));
		boolean skip = true;
		for (Bunch candGr: groups) {
			if (skip) continue;;

			w.setDerived(candGr);
			break;
		}
		w.setGroup(roots.get(0));

		return -1;
	}
	public static int procedure3 (Word w) {
		return -1;
	}
	public static int procedure4 (Word w) {
		return -1;
	}
	public static int procedure5 (Word w) {
		return -1;
	}
	public static int procedure6 (Word w) {
		return -1;
	}
	public static int procedure7 (Word w) {
		return -1;
	}
	public static int procedure8 (Word w) {
		return -1;
	}
	public static int procedure9 (Word w) {
		return -1;
	}
	
	private static int maxGroup() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
		Root<Bunch> root = cq.from(Bunch.class);
		cq.select(cb.max(root.get(Bunch_.id)));
		return em.createQuery(cq).getSingleResult();
	}
	
	interface WordAction extends Function<Word, Integer> {}
	
	static final WordAction[] wordProcessors = {
		RuntimeCode::procedure0,
		RuntimeCode::procedure1,
		RuntimeCode::procedure2,
		RuntimeCode::procedure3,
		RuntimeCode::procedure4,
		RuntimeCode::procedure5,
		RuntimeCode::procedure6,
		RuntimeCode::procedure7,
		RuntimeCode::procedure8,
		RuntimeCode::procedure9,
	};

	static <T> TypedQuery<T> makeEntitySelect(Class<T> type, DbRecordsParser.Conditions conditions) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(type);
		Root<T> root = cq.from(type);
		cq.select(root);
		cq.where(conditions.make(cb, root));
		return em.createQuery(cq);
	}

	static EntityProcessor<Word> select(DbRecordsParser.Conditions conditions) {
		TypedQuery<Word> query = makeEntitySelect(Word.class, conditions);
		return (action) -> doProcessEach(query, action);
	}
	static <T> EntityProcessor<T> selectOne(Class<T> type, DbRecordsParser.Conditions conditions) {
		TypedQuery<T> q = makeEntitySelect(type, conditions);
		return (action) -> action.accept(q.getSingleResult());
	}
	private static <T> Consumer<Consumer<List<T>>> selectList(Class<T> type, int limit, DbRecordsParser.Conditions conditions) {
		TypedQuery<T> q = makeEntitySelect(type, conditions);
		q.setMaxResults(limit);
		return (action) -> action.accept(q.getResultList());
	}
	static <T> void doProcessEach(TypedQuery<T> q, Consumer<T> proc) {
		List<T> beans = q.getResultList();
		for (T b : beans) {
			em.getTransaction().begin();
			proc.accept(b);
			em.getTransaction().commit();
		}
	}


}
