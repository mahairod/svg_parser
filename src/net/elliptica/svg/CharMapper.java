/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2017 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.svg;

import java.io.IOException;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
public abstract class CharMapper {
	public final String getFormat(){
		return "\u001b[" + getFormat_();
	}
	protected abstract String getFormat_();
	protected abstract String getSing();

	char map(int sym, PDFont font) throws IOException{
		int code = 0xFF & sym;
		String s = font.toUnicode( code );
		if (s!=null){
			return s.charAt(0);
		} else {
			return '?';
		}
	}
	static final CharMapper BOLD = new BoldMapper();
	static final CharMapper REGULAR = new RegularMapper();
	static final CharMapper ITALIC = new ItalicMapper();
	static final CharMapper BOLDIT = new BoldItalicMapper();

	static final CharMapper MAPPERS[] = new CharMapper[]{
		REGULAR, BOLD, ITALIC, BOLDIT
	};

}

class RegularMapper extends CharMapper {

	@Override
	public String getFormat_() {
		return "0m";
	}

	public String getSing() {
		return "\u001b\u001f";
	}

	@Override
	char map(int code, PDFont font) throws IOException {
		switch (code){
			case 76:
				return '´';
			case 9:
				return '-';
			default:
				return super.map(code, font);
		}
	}

}

class BoldMapper extends CharMapper {

	@Override
	public String getFormat_() {
		return "31m";
	}

	public String getSing() {
		return "\u001b\u001e";
	}

	@Override
	char map(int code, PDFont font) throws IOException {
		switch (code){
			case 55:
				return '´';
			default:
				return super.map(code, font);
		}
	}

}

class ItalicMapper extends CharMapper {

	@Override
	public String getFormat_() {
		return "32m";
	}

	public String getSing() {
		return "\u001b\u001d";
	}

	@Override
	char map(int code, PDFont font) throws IOException {
		switch (code){
			default:
				return super.map(code, font);
		}
	}

}

class BoldItalicMapper extends CharMapper {

	@Override
	public String getFormat_() {
		return "34m";
	}

	public String getSing() {
		return "\u001b\u001c";
	}

	@Override
	char map(int code, PDFont font) throws IOException {
		switch (code){
			default:
				return super.map(code, font);
		}
	}

}
