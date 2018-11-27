/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> ѱ 2017.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Anton Astafiev <anton@astafiev.me>. All rights reserved.
 *
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling;

import java.io.Serializable;
import net.elliptica.ling.db.NumRange;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.postgresql.util.PGobject;


/**
 *
 * @автор Антон Александрович Астафьев {@буквально <anton@astafiev.me>} (Anton Astafiev)
 */
@Converter
public class RangeArrConverter implements AttributeConverter<NumRange[], Object>, Serializable {

	@Override
	public PGobject convertToDatabaseColumn(NumRange[] атрибут) {
		if (атрибут == null){
			return null;
		}
		PGobject зн = new PGobject();
		try {
			зн.setType("numrange[]");
			String value = Stream.of(атрибут)
					.map(r->'"'+r.toString()+'"')
					.collect(Collectors.toList())
					.toString().replaceFirst("^\\[", "{").replaceFirst("\\]$", "}");
			зн.setValue(value);
		} catch (SQLException ex) {
			ЖУРНАЛ.log(Level.SEVERE, null, ex);
		}
		return зн;
	}

	@Override
	public NumRange[] convertToEntityAttribute(Object данныеБД) {
		if (данныеБД instanceof PGobject) {
			return convertToEntityAttribute(((PGobject)данныеБД).getValue());
		} else if (данныеБД instanceof Object[]) {
			Object[] data = (Object[]) данныеБД;
			NumRange[] res = new NumRange[data.length];
			for (int i=0; i< res.length; i++) {
				res[i] = convertRange(data[i].toString());
			}
			return res;
		} else if (данныеБД instanceof String) {
			return convertToEntityAttribute(данныеБД.toString());
		} else {
			throw new IllegalArgumentException("Unsuported array format:" + данныеБД.toString());
		}
	}
	
	private NumRange[] convertToEntityAttribute(String данныеБД) {
		List<NumRange> res = new ArrayList<>();
		Matcher m = SPLIT_PATT.matcher(данныеБД);
		while (m.find()) {
			res.add(convertRange(m.group(1)));
		}
		return res.toArray(RANGES);
	}
	
	private NumRange convertRange(String данныеБД) {
		if (данныеБД.equals("empty")) {
			return new NumRange(0, 0);
		}
		Matcher m = SPLIT_PATT.matcher(данныеБД);
		if (m.find()) {
			return new NumRange(m.group(2), m.group(3));
		} else {
			throw new IllegalArgumentException("Unsuported array format:" + данныеБД.toString());
		}
	}
	
	private final Pattern SPLIT_PATT = Pattern.compile("(\\[(\\d+)?,(\\d+)?\\))(,|$)");
	private static final NumRange[] RANGES = {};

	private static final Logger ЖУРНАЛ = Logger.getLogger(RangeArrConverter.class.getName());
}
