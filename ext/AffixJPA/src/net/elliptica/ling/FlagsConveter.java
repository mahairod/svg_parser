/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> ѱ 2018.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Anton Astafiev <anton@astafiev.me>. All rights reserved.
 *
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.postgresql.util.PGobject;


/**
 *
 * @автор Антон Александрович Астафьев {@буквально <anton@astafiev.me>} (Anton Astafiev)
 */
@Converter
public class FlagsConveter implements AttributeConverter<Boolean[], Object> {

	@Override
	public Object convertToDatabaseColumn(Boolean[] атрибут) {
		if (атрибут == null){
			return null;
		}
		PGobject зн = new PGobject();
		try {
			зн.setType("boolean[]");
			зн.setValue(Arrays.toString(атрибут));
		} catch (SQLException ex) {
			ЖУРНАЛ.log(Level.SEVERE, null, ex);
		}
		return зн;
	}

	@Override
	public Boolean[] convertToEntityAttribute(Object данныеБД) {
		if (данныеБД instanceof Boolean[]){
			return (Boolean[]) данныеБД;
		} else if (данныеБД instanceof Object){
			String val = данныеБД.toString();
			String[] parts = val.replaceAll("\\{|\\}", "").split(", +", 0);
			Boolean[] vals = new Boolean[parts.length];
			for (int i=0; i< parts.length; i++) {
				vals[i] = Boolean.valueOf(parts[i]);
			}
			return vals;
		} else {
			return null;
		}
	}
	
	private static final Logger ЖУРНАЛ = Logger.getLogger(FlagsConveter.class.getName());
}
