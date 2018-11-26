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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import me.astafiev.веб.инструменты.MultiLinkUpdate;
import me.astafiev.веб.сущности.JPAEntity;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "affix_appl")
@NamedQueries({
	@NamedQuery(name = "АффиксаПриложение.findAll", query = "SELECT ап FROM АффиксаПриложение ап"),
	@NamedQuery(name = "АффиксаПриложение.findByOffs", query = "SELECT ап FROM АффиксаПриложение ап WHERE ап.offs = :offs"),
	@NamedQuery(name = "АффиксаПриложение.findByLen", query = "SELECT ап FROM АффиксаПриложение ап WHERE ап.len = :len"),
	@NamedQuery(name = "АффиксаПриложение.findByOrig", query = "SELECT ап FROM АффиксаПриложение ап WHERE ап.orig = :orig"),
	@NamedQuery(name = "АффиксаПриложение.findById", query = "SELECT ап FROM АффиксаПриложение ап WHERE ап.id = :id")})
public class АффиксаПриложение extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	public static MultiLinkUpdate<АффиксаПриложение,Аффикс> AFF_SETTER
			= new MultiLinkUpdate<>(АффиксаПриложение::getАффикс, АффиксаПриложение::setАффикс, Аффикс::getАффиксаПриложения);
	public static MultiLinkUpdate<АффиксаПриложение,Слово> WORD_SETTER
			= new MultiLinkUpdate<>(АффиксаПриложение::getWord, АффиксаПриложение::setWord, Слово::getАффиксаПриложения);
	public static MultiLinkUpdate<АффиксаПриложение,Слово> PAR_WORD_SETTER
			= new MultiLinkUpdate<>(АффиксаПриложение::getParentWord, АффиксаПриложение::setParentWord, Слово::getАффиксаПорождения);

	@Basic(optional = false)
	@NotNull
	private short offs;

	@Basic(optional = false)
	@NotNull
	private short len;

	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 40)
	private String orig;

	@JoinColumn(name = "affix", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Аффикс аффикс;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "affappl1")
	private Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet1 = new HashSet<>();

	@OneToMany(mappedBy = "affappl2")
	private Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet2 = new HashSet<>();

	@OneToMany(mappedBy = "affappl3")
	private Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet3 = new HashSet<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Integer id;

	@JoinColumn(name = "word", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private Слово word;

	@JoinColumn(name = "parent_word", referencedColumnName = "id")
	@ManyToOne(optional = true)
	private Слово parentWord;

	public АффиксаПриложение() {
	}

	public АффиксаПриложение(Integer id) {
		this.id = id;
	}

	public АффиксаПриложение(Integer id, short offs, short len, String orig) {
		this.id = id;
		this.offs = offs;
		this.len = len;
		this.orig = orig;
	}

	public static АффиксаПриложение fromJava(short offs, short len, String orig) {
		return new АффиксаПриложение(null, (short) (offs + 1), len, orig);
	}

	public static АффиксаПриложение copy(АффиксаПриложение orig, Аффикс aff, Слово word) {
		АффиксаПриложение aa = new АффиксаПриложение(null, orig.offs, orig.len, orig.orig);
		aa.setАффикс(aff);
		aa.setWord(word);
		return aa;
	}

	public short getOffs() {
		return offs;
	}

	public void setOffs(short offs) {
		this.offs = offs;
	}

	public short getLen() {
		return len;
	}

	public void setLen(short len) {
		this.len = len;
	}

	public String getOrig() {
		return orig;
	}

	public void setOrig(String orig) {
		this.orig = orig;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Слово getWord() {
		return word;
	}

	private void setWord(Слово word) {
		this.word = word;
	}

	public Слово getParentWord() {
		return parentWord;
	}

	private void setParentWord(Слово parentWord) {
		this.parentWord = parentWord;
	}

	public Аффикс getАффикс() {
		return аффикс;
	}

	private void setАффикс(Аффикс аффикс) {
		this.аффикс = аффикс;
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
		if (!(object instanceof АффиксаПриложение)) {
			return false;
		}
		АффиксаПриложение other = (АффиксаПриложение) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "net.elliptica.svg.db.АффиксаПриложение[ id=" + id + " ]";
	}

	public Set<КомпозитноеПриложениеАффиксов> getComposedAffixApplSet1() {
		return composedAffixApplSet1;
	}

	private void setComposedAffixApplSet1(Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet1) {
		this.composedAffixApplSet1 = composedAffixApplSet1;
	}

	public Set<КомпозитноеПриложениеАффиксов> getComposedAffixApplSet2() {
		return composedAffixApplSet2;
	}

	private void setComposedAffixApplSet2(Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet2) {
		this.composedAffixApplSet2 = composedAffixApplSet2;
	}

	public Set<КомпозитноеПриложениеАффиксов> getComposedAffixApplSet3() {
		return composedAffixApplSet3;
	}

	private void setComposedAffixApplSet3(Set<КомпозитноеПриложениеАффиксов> composedAffixApplSet3) {
		this.composedAffixApplSet3 = composedAffixApplSet3;
	}

}
