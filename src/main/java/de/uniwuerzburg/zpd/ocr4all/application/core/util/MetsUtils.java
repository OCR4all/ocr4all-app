/**
 * File:     MetsUtils.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Defines mets utilities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class MetsUtils {
	/**
	 * The pattern describing mets the date and time format.
	 */
	public static final String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	/**
	 * The mets date format.
	 */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);

	/**
	 * Returns the current date formatted for mets.
	 * 
	 * @return The current date formatted for mets.
	 * @since 1.8
	 */
	public static String getFormattedDate() {
		return getFormattedDate(null);
	}

	/**
	 * Returns the date formatted for mets.
	 * 
	 * @param date The date. If null, use the current date.
	 * @return The date formatted for mets.
	 * @since 1.8
	 */
	public static String getFormattedDate(Date date) {
		return dateFormat.format(date == null ? new Date() : date);
	}

	/**
	 * Returns the respective date of the given mets formatted date.
	 * 
	 * @param formattedDate The mets formatted date.
	 * @return The date.
	 * @throws ParseException Throws if the beginning of the specified string cannot
	 *                        be parsed.
	 * @since 1.8
	 */
	public static Date getDate(String formattedDate) throws ParseException {
		return dateFormat.parse(formattedDate);
	}

}
