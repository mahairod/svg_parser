/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import me.astafiev.веб.инструменты.MultiLinkUpdate;
import me.astafiev.веб.сущности.JPAEntity;
import net.elliptica.ling.RangeArrConverter;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "composed_affix_appl")
@NamedQueries({
	@NamedQuery(name = "КомпозитАффиксов.findAll", query = "SELECT c FROM КомпозитноеПриложениеАффиксов c")})
public class КомпозитноеПриложениеАффиксов extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public static MultiLinkUpdate<КомпозитноеПриложениеАффиксов,АффиксаПриложение> affappl1Setter
			= new MultiLinkUpdate<>(КомпозитноеПриложениеАффиксов::getAffappl1, КомпозитноеПриложениеАффиксов::setAffappl1, АффиксаПриложение::getComposedAffixApplSet1);
	public static MultiLinkUpdate<КомпозитноеПриложениеАффиксов,АффиксаПриложение> affappl2Setter
			= new MultiLinkUpdate<>(КомпозитноеПриложениеАффиксов::getAffappl2, КомпозитноеПриложениеАффиксов::setAffappl2, АффиксаПриложение::getComposedAffixApplSet2);
	public static MultiLinkUpdate<КомпозитноеПриложениеАффиксов,АффиксаПриложение> affappl3Setter
			= new MultiLinkUpdate<>(КомпозитноеПриложениеАффиксов::getAffappl3, КомпозитноеПриложениеАффиксов::setAffappl3, АффиксаПриложение::getComposedAffixApplSet3);
	public static MultiLinkUpdate<КомпозитноеПриложениеАффиксов,Слово> wordSetter
			= new MultiLinkUpdate<>(КомпозитноеПриложениеАффиксов::getWord, КомпозитноеПриложениеАффиксов::setWord, Слово::getCompAffixApplications);
	public static MultiLinkUpdate<КомпозитноеПриложениеАффиксов,КомпозитноеПриложениеАффиксов> parentSetter
			= new MultiLinkUpdate<>(КомпозитноеПриложениеАффиксов::getParent, КомпозитноеПриложениеАффиксов::setParent, КомпозитноеПриложениеАффиксов::getChildren);

	@Convert(converter = RangeArrConverter.class)
	@Basic(optional = false)
	@NotNull
	@Column(name = "val_locs")
	private NumRange[] valLocs;

	@Convert(converter = RangeArrConverter.class)
	@Basic(optional = false)
	@NotNull
	@Column(name = "aff_locs")
	private NumRange[] affLocs;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Integer id;

	@JoinColumn(name = "affix1", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Аффикс affix1;

	@JoinColumn(name = "affix2", referencedColumnName = "id")
	@ManyToOne
	private Аффикс affix2;

	@JoinColumn(name = "affix3", referencedColumnName = "id")
	@ManyToOne
	private Аффикс affix3;

	@JoinColumn(name = "affappl1", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private АффиксаПриложение affappl1;

	@JoinColumn(name = "affappl2", referencedColumnName = "id")
	@ManyToOne
	private АффиксаПриложение affappl2;

	@JoinColumn(name = "affappl3", referencedColumnName = "id")
	@ManyToOne
	private АффиксаПриложение affappl3;

	@OneToMany(mappedBy = "parent")
	private Set<КомпозитноеПриложениеАффиксов> children = new HashSet<>();

	@JoinColumn(name = "word", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Слово word;

	@JoinColumn(name = "parent", referencedColumnName = "id")
	@ManyToOne
	private КомпозитноеПриложениеАффиксов parent;

	public КомпозитноеПриложениеАффиксов() {
	}

	public КомпозитноеПриложениеАффиксов(Integer id) {
		this.id = id;
	}

	public КомпозитноеПриложениеАффиксов(Integer id, Слово word, NumRange[] valLocs, NumRange[] affLocs) {
		this.id = id;
		this.word = word;
		this.valLocs = valLocs;
		this.affLocs = affLocs;
	}

	public NumRange[] getValLocs() {
		return valLocs;
	}

	public void setValLocs(NumRange[] valLocs) {
		this.valLocs = valLocs;
	}

	public NumRange[] getAffLocs() {
		return affLocs;
	}

	public void setAffLocs(NumRange[] affLocs) {
		this.affLocs = affLocs;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Аффикс getAffix1() {
		return affix1;
	}

	public void setAffix1(Аффикс affix1) {
		this.affix1 = affix1;
	}

	public Аффикс getAffix2() {
		return affix2;
	}

	public void setAffix2(Аффикс affix2) {
		this.affix2 = affix2;
	}

	public Аффикс getAffix3() {
		return affix3;
	}

	public void setAffix3(Аффикс affix3) {
		this.affix3 = affix3;
	}

	public АффиксаПриложение getAffappl1() {
		return affappl1;
	}

	private void setAffappl1(АффиксаПриложение affappl1) {
		this.affappl1 = affappl1;
	}

	public АффиксаПриложение getAffappl2() {
		return affappl2;
	}

	private void setAffappl2(АффиксаПриложение affappl2) {
		this.affappl2 = affappl2;
	}

	public АффиксаПриложение getAffappl3() {
		return affappl3;
	}

	private void setAffappl3(АффиксаПриложение affappl3) {
		this.affappl3 = affappl3;
	}

	public Set<КомпозитноеПриложениеАффиксов> getChildren() {
		return children;
	}

	private void setChildren(Set<КомпозитноеПриложениеАффиксов> children) {
		this.children = children;
	}

	public КомпозитноеПриложениеАффиксов getParent() {
		return parent;
	}

	private void setParent(КомпозитноеПриложениеАффиксов parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof КомпозитноеПриложениеАффиксов)) {
			return false;
		}
		КомпозитноеПриложениеАффиксов other = (КомпозитноеПриложениеАффиксов) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "net.elliptica.ling.db.ComposedAffixAppl[ id=" + id + " ]";
	}

	public Слово getWord() {
		return word;
	}

	public void setWord(Слово word) {
		this.word = word;
	}

}
