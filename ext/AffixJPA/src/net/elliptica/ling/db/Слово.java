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
import javax.persistence.Column;
import javax.persistence.Convert;
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
import net.elliptica.ling.FlagsConveter;
import net.elliptica.ling.PartOfSpeach;
import net.elliptica.ling.PoSConverter;
import org.eclipse.persistence.annotations.IdValidation;
import org.eclipse.persistence.annotations.PrimaryKey;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
@Entity
@Table(name = "word")
@PrimaryKey(validation = IdValidation.NULL)
@NamedQueries({
	@NamedQuery(name = "Слово.findAll", query = "SELECT сл FROM Слово сл"),
	@NamedQuery(name = "Слово.findById", query = "SELECT сл FROM Слово сл WHERE сл.id = :id"),
	@NamedQuery(name = "Слово.findByLine", query = "SELECT сл FROM Слово сл WHERE сл.line = :line"),
	@NamedQuery(name = "Слово.findByX", query = "SELECT сл FROM Слово сл WHERE сл.x = :x"),
	@NamedQuery(name = "Слово.findByY", query = "SELECT сл FROM Слово сл WHERE сл.y = :y"),
	@NamedQuery(name = "Слово.findByText", query = "SELECT сл FROM Слово сл WHERE сл.text = :text"),
	@NamedQuery(name = "Слово.findByLen", query = "SELECT сл FROM Слово сл WHERE сл.len = :len"),
	@NamedQuery(name = "Слово.findByHyphen", query = "SELECT сл FROM Слово сл WHERE сл.hyphen = :hyphen"),
	@NamedQuery(name = "Слово.findByDeprecated", query = "SELECT сл FROM Слово сл WHERE сл.deprecated = :deprecated"),
	@NamedQuery(name = "Слово.findByNotes", query = "SELECT сл FROM Слово сл WHERE сл.notes = :notes"),
	@NamedQuery(name = "Слово.findByAlternation", query = "SELECT сл FROM Слово сл WHERE сл.alternation = :alternation"),
	@NamedQuery(name = "Слово.findByAltRest", query = "SELECT сл FROM Слово сл WHERE сл.altRest = :altRest"),
	@NamedQuery(name = "Слово.findByVariant", query = "SELECT сл FROM Слово сл WHERE сл.variant = :variant"),
	@NamedQuery(name = "Слово.findByVersion", query = "SELECT сл FROM Слово сл WHERE сл.version = :version"),
//	@NamedQuery(name = "Слово.findByFlags", query = "SELECT сл FROM Слово сл WHERE сл.flags = :flags")
})
public class Слово extends JPAEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Convert(converter = PoSConverter.class)
	private PartOfSpeach pos;

	@Convert(converter = PoSConverter.class)
	private PartOfSpeach pos2;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	private Integer id;

	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 2147483647)
	private String line;

	@Basic(optional = false)
	@NotNull
	private double x;

	@Basic(optional = false)
	@NotNull
	private double y;

	@Size(max = 2147483647)
	private String text;

	@Basic(optional = false)
	@NotNull
	private double len;

	private Boolean hyphen;

	private Boolean deprecated;

	@Size(max = 2147483647)
	private String notes;

	@Size(max = 2147483647)
	private String alternation;

	@Size(max = 2147483647)
	@Column(name = "alt_rest")
	private String altRest;

	private Character variant;

	private Character version;

//	@Convert(converter = FlagsConveter.class)
//	private Boolean[] flags;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "word")
	private Set<КомпозитноеПриложениеАффиксов> compAffixApplications = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "word")
	private Set<АффиксаПриложение> аффиксаПриложения = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parentWord")
	private Set<АффиксаПриложение> аффиксаПорождения = new HashSet<>();

	public Слово() {
	}

	public Слово(Integer id) {
		this.id = id;
	}

	public Слово(Integer id, String line, double x, double y, double len) {
		this.id = id;
		this.line = line;
		this.x = x;
		this.y = y;
		this.len = len;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public double getLen() {
		return len;
	}

	public void setLen(double len) {
		this.len = len;
	}

	public Boolean getHyphen() {
		return hyphen;
	}

	public void setHyphen(Boolean hyphen) {
		this.hyphen = hyphen;
	}

	public Boolean getDeprecated() {
		return deprecated;
	}

	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getAlternation() {
		return alternation;
	}

	public void setAlternation(String alternation) {
		this.alternation = alternation;
	}

	public String getAltRest() {
		return altRest;
	}

	public void setAltRest(String altRest) {
		this.altRest = altRest;
	}

	public Character getVariant() {
		return variant;
	}

	public void setVariant(Character variant) {
		this.variant = variant;
	}

	public Character getVersion() {
		return version;
	}

	public void setVersion(Character version) {
		this.version = version;
	}
/*
	public Boolean[] getFlags() {
		return flags;
	}

	public void setFlags(Boolean[] flags) {
		this.flags = flags;
	}
*/
	public Set<АффиксаПриложение> getАффиксаПриложения() {
		return аффиксаПриложения;
	}

	void setАффиксаПриложения(Set<АффиксаПриложение> аффиксаПриложения) {
		this.аффиксаПриложения = аффиксаПриложения;
	}

	public Set<АффиксаПриложение> getАффиксаПорождения() {
		return аффиксаПорождения;
	}

	void setАффиксаПорождения(Set<АффиксаПриложение> аффиксаПорождения) {
		this.аффиксаПорождения = аффиксаПорождения;
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
		if (!(object instanceof Слово)) {
			return false;
		}
		Слово other = (Слово) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "net.elliptica.svg.db.Слово[ id=" + id + " ]";
	}

	public PartOfSpeach getPos() {
		return pos;
	}

	public void setPos(PartOfSpeach pos) {
		this.pos = pos;
	}

	public PartOfSpeach getPos2() {
		return pos2;
	}

	public void setPos2(PartOfSpeach pos2) {
		this.pos2 = pos2;
	}

	public Set<КомпозитноеПриложениеАффиксов> getCompAffixApplications() {
		return compAffixApplications;
	}

	private void setCompAffixApplications(Set<КомпозитноеПриложениеАффиксов> compAffixApplications) {
		this.compAffixApplications = compAffixApplications;
	}

}
