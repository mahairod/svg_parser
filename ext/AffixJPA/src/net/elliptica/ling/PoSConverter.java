/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> ѱ 2017.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2017 Anton Astafiev <anton@astafiev.me>. All rights reserved.
 *
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.ling;

import java.sql.SQLException;
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
public class PoSConverter implements AttributeConverter<PartOfSpeach, Object> {

	@Override
	public PGobject convertToDatabaseColumn(PartOfSpeach атрибут) {
		if (атрибут == null){
			return null;
		}
		PGobject зн = new PGobject();
		try {
			зн.setType("partofspeech");
			зн.setValue(атрибут.name());
		} catch (SQLException ex) {
			ЖУРНАЛ.log(Level.SEVERE, null, ex);
		}
		return зн;
	}

	@Override
	public PartOfSpeach convertToEntityAttribute(Object данныеБД) {
		if (данныеБД instanceof PGobject) {
			return PartOfSpeach.valueOf(((PGobject)данныеБД).getValue());
		} else if (данныеБД != null) {
			return PartOfSpeach.valueOf(данныеБД.toString());
		} else {
			return null;
		}
	}

	private static final Logger ЖУРНАЛ = Logger.getLogger(PoSConverter.class.getName());
}
