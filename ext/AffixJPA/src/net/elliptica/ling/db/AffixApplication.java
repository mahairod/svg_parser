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
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import me.astafiev.веб.сущности.JPAEntity;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "affix_appl")
@NamedQueries({
	@NamedQuery(name = "AffixApplication.findAll", query = "SELECT \u0430 FROM AffixApplication \u0430"),
	@NamedQuery(name = "AffixApplication.findByOffs", query = "SELECT \u0430 FROM AffixApplication \u0430 WHERE \u0430.offs = :offs"),
	@NamedQuery(name = "AffixApplication.findByLen", query = "SELECT \u0430 FROM AffixApplication \u0430 WHERE \u0430.len = :len"),
	@NamedQuery(name = "AffixApplication.findByOrig", query = "SELECT \u0430 FROM AffixApplication \u0430 WHERE \u0430.orig = :orig"),
	@NamedQuery(name = "AffixApplication.findById", query = "SELECT \u0430 FROM AffixApplication \u0430 WHERE \u0430.id = :id")})
public class AffixApplication extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;
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

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
	private Integer id;
	@JoinColumn(name = "word", referencedColumnName = "id")
    @ManyToOne(optional = false)
	private Слово word;
	@JoinColumn(name = "parent_word", referencedColumnName = "id")
    @ManyToOne(optional = false)
	private Слово parentWord;

	public AffixApplication() {
	}

	public AffixApplication(Integer id) {
		this.id = id;
	}

	public AffixApplication(Integer id, short offs, short len, String orig) {
		this.id = id;
		this.offs = offs;
		this.len = len;
		this.orig = orig;
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

	public void setWord(Слово word) {
		this.word = word;
	}

	public Слово getParentWord() {
		return parentWord;
	}

	public void setParentWord(Слово parentWord) {
		this.parentWord = parentWord;
	}

	public Аффикс getАффикс() {
		return аффикс;
	}

	public void setАффикс(Аффикс аффикс) {
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
		if (!(object instanceof AffixApplication)) {
			return false;
		}
		AffixApplication other = (AffixApplication) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "net.elliptica.svg.db.AffixApplication[ id=" + id + " ]";
	}

}
