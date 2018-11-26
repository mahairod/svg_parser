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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import me.astafiev.веб.сущности.JPAEntity;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "affix")
@NamedQueries({
	@NamedQuery(name = "Аффикс.findAll", query = "SELECT a FROM Аффикс a"),
	@NamedQuery(name = "Аффикс.findById", query = "SELECT a FROM Аффикс a WHERE a.id = :id"),
	@NamedQuery(name = "Аффикс.findByVal", query = "SELECT a FROM Аффикс a WHERE a.val like :val"),
	@NamedQuery(name = "Аффикс.findByQty", query = "SELECT a FROM Аффикс a WHERE a.qty = :qty"),
	@NamedQuery(name = "Аффикс.findByKind", query = "SELECT a FROM Аффикс a WHERE a.kind = :kind")})
public class Аффикс extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Integer id;

	@Basic(optional = false)
	@Column(name = "value")
	private String val;

	@Basic(optional = false)
	private int qty;

	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 6)
	private String kind;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "аффикс")
	private Set<АффиксаПриложение> аффиксаПриложения = new HashSet<>();

	public Аффикс() {
	}

	public Аффикс(Integer id) {
		this.id = id;
	}

	public Аффикс(Integer id, String value, int qty, String kind) {
		this.id = id;
		this.val = value;
		this.qty = qty;
		this.kind = kind;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String value) {
		this.val = value;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Set<АффиксаПриложение> getАффиксаПриложения() {
		return аффиксаПриложения;
	}

	private void setАффиксаПриложения(Set<АффиксаПриложение> аффиксаПриложения) {
		this.аффиксаПриложения = аффиксаПриложения;
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
		if (!(object instanceof Аффикс)) {
			return false;
		}
		Аффикс other = (Аффикс) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "net.elliptica.svg.db.Аффикс[ id=" + id + " ]";
	}

	public Аффикс(Integer id, int qty, String kind) {
		this.id = id;
		this.qty = qty;
		this.kind = kind;
	}

}
