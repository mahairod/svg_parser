/*
 * Авторское право принадлежит Антону Александровичу Астафьеву <anton@astafiev.me> (Anton Astafiev) ѱ.
 * Все права защищены и охраняются законом.
 * Copyright (c) 2018 Антон Александрович Астафьев <anton@astafiev.me> (Anton Astafiev). All rights reserved.
 * 
 *  Собственная лицензия Астафьева
 * Данный программный код является собственностью Астафьева Антона Александровича
 * и может быть использован только с его личного разрешения
 */

package net.elliptica.svg;

import java.util.function.Function;

/**
 *
 * @author Антон Астафьев <anton@astafiev.me> (Anton Astafiev)
 */
class EqualLineMatcher implements Function<Word, Boolean> {

	public EqualLineMatcher(String note, Word noteword) {
		this.note = note;
		this.noteword = noteword;
		this.lp = new LineProcessor();
		lp.setFormatType(LineProcessor.FormatType.Clean);
	}
	private final String note;
	private final Word noteword;
	private final LineProcessor lp;

	@Override
	public Boolean apply(Word bw) {
		String line = lp.format(bw.getLine())
			.replaceFirst("^•", "")
			.replaceFirst("´", "")
			.replaceFirst(" I+$", "")
			.replace("(", "")
			.replace(")", "")
			.replace("[j]-и", "и")
			.replaceFirst("\\[([" + DbRecordsParser.consonants + "])’?j\\]-и", "$1ьи")
			.replaceFirst("([" + DbRecordsParser.consonants + "])\\[j-э\\]", "$1ье")
			.replaceFirst("([" + DbRecordsParser.consonants + "])\\[j-о\\]", "$1ьё")
			.replaceFirst("([" + DbRecordsParser.consonants + "])\\[j-и\\]", "$1ьи")
			.replace("[j-э]", "е")
			.replaceAll("-", "")
			.replaceAll("\\(", "")
			.replaceAll("\\)", "")
			.replaceAll("\\|", "");
		if (note != null && !note.equals(line)) {
			switch (noteword.getId()) {
				case 31208:
				case 51314:
				case 73043:
				case 137966:
					return true;
				default:
					return false;
			}
		}
		return false;
	}

}
