/**
 * File:     ImageUtils.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.io.IOException;
import java.nio.file.Path;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines image utilities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ImageUtils {

	/**
	 * Returns the folio size.
	 * 
	 * @param identifyJob The identify job.
	 * @param source      The source name.
	 * @param target      The target name.
	 * @return The folio size.
	 * @throws IOException Throws if the size could not be determined.
	 * @since 1.8
	 */
	public static Folio.Size getSize(SystemProcess identifyJob, String source, String target) throws IOException {
		identifyJob.execute("-format", "%[fx:w]x%[fx:h]", target);

		if (identifyJob.getExitValue() != 0) {
			String error = identifyJob.getStandardError();

			throw new IOException("Could not determine the folio size of '" + source + "'"
					+ (error.isBlank() ? "" : " - " + error.trim()) + ".");
		}

		Folio.Size size = null;
		final String[] split = identifyJob.getStandardOutput().split("x");
		if (split.length == 2)
			try {
				size = new Folio.Size(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()));
			} catch (Exception e) {
				// Nothing to do
			}

		if (size == null) {
			final String error = identifyJob.getStandardError();

			throw new IOException("Could not determine the size of the folio '" + source + "'"
					+ (error.isBlank() ? "" : " - " + error.trim()) + ".");
		} else
			return size;
	}

	/**
	 * Creates the derivatives quality image for folios.
	 * 
	 * @param convertJob The convert job.
	 * @param format     The folios derivatives format.
	 * @param target     The target folder.
	 * @param resize     The maximal size.
	 * @param quality    The compression quality.
	 * @throws IOException Throws on troubles creating the derivatives quality images.
	 * @since 1.8
	 */
	public static void createDerivatives(SystemProcess convertJob, String format, Path target,
			String resize, int quality) throws IOException {
		final String label = target.getFileName().toString();

		try {
			convertJob.execute("*", "-format", format, "-resize", resize + ">", "-quality", "" + quality, "-set",
					"filename:t", "%t", "+adjoin", target.toString() + "/%[filename:t]." + format);

		} catch (IOException e) {
			throw new IOException(
					"Cannot create derivatives " + label + " quality image for folios - " + e.getMessage() + ".");
		}
		
		if (convertJob.getExitValue() != 0) {
			final String error = convertJob.getStandardError();
			
			throw new IOException("Cannot create derivatives " + label + " quality image for folios"
					+ (error.isBlank() ? "" : " - " + error.trim()) + ".");
		}

	}

}
