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
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import me.astafiev.веб.сущности.JPAEntity;
import net.elliptica.ling.RangeArrConverter;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "composed_affix_appl")
@NamedQueries({
	@NamedQuery(name = "ComposedAffixAppl.findAll", query = "SELECT c FROM ComposedAffixAppl c")})
public class ComposedAffixAppl extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;

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
	private AffixApplication affappl1;

	@JoinColumn(name = "affappl2", referencedColumnName = "id")
	@ManyToOne
	private AffixApplication affappl2;

	@JoinColumn(name = "affappl3", referencedColumnName = "id")
	@ManyToOne
	private AffixApplication affappl3;

	@OneToMany(mappedBy = "parent")
	private Set<ComposedAffixAppl> children;

	@JoinColumn(name = "word", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Слово word;

	@JoinColumn(name = "parent", referencedColumnName = "id")
	@ManyToOne
	private ComposedAffixAppl parent;

	public ComposedAffixAppl() {
	}

	public ComposedAffixAppl(Integer id) {
		this.id = id;
	}

	public ComposedAffixAppl(Integer id, Слово word, NumRange[] valLocs, NumRange[] affLocs) {
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

	public AffixApplication getAffappl1() {
		return affappl1;
	}

	public void setAffappl1(AffixApplication affappl1) {
		if (this.affappl1 == affappl1) return;
		this.affappl1.getComposedAffixApplSet1().remove(this);
		this.affappl1 = affappl1;
		affappl1.getComposedAffixApplSet1().add(this);
	}

	public AffixApplication getAffappl2() {
		return affappl2;
	}

	public void setAffappl2(AffixApplication affappl2) {
		if (this.affappl2 == affappl2) return;
		this.affappl2.getComposedAffixApplSet1().remove(this);
		this.affappl2 = affappl2;
		affappl2.getComposedAffixApplSet2().add(this);
	}

	public AffixApplication getAffappl3() {
		return affappl3;
	}

	public void setAffappl3(AffixApplication affappl3) {
		if (this.affappl3 == affappl3) return;
		this.affappl3.getComposedAffixApplSet1().remove(this);
		this.affappl3 = affappl3;
		affappl3.getComposedAffixApplSet3().add(this);
	}

	public Set<ComposedAffixAppl> getChildren() {
		return children;
	}

	public void setChildren(Set<ComposedAffixAppl> children) {
		this.children = children;
	}

	public ComposedAffixAppl getParent() {
		return parent;
	}

	public void setParent(ComposedAffixAppl parent) {
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
		if (!(object instanceof ComposedAffixAppl)) {
			return false;
		}
		ComposedAffixAppl other = (ComposedAffixAppl) object;
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
