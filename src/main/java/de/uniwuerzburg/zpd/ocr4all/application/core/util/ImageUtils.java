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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.request.IdentificationRequest;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio.PageXMLType;
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
	 * @throws IOException Throws on troubles creating the derivatives quality
	 *                     images.
	 * @since 1.8
	 */
	public static void createDerivatives(SystemProcess convertJob, String format, Path target, String resize,
			int quality) throws IOException {
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

	/**
	 * Sorts the folios in the given order.
	 * 
	 * @param folios  The folios to sort.
	 * @param order   The order to sort, that is list of folios ids.
	 * @param isAfter True if the folios that do not belong to the order are to be
	 *                inserted after the folios that belong to the order. Otherwise,
	 *                they are placed at the beginning.
	 * @return The sorted folios.
	 * @since 1.8
	 */
	public static List<Folio> sort(List<Folio> folios, List<String> order, boolean isAfter) {
		if (folios == null || folios.isEmpty() || order == null || order.isEmpty())
			return folios;

		List<String> parsedOrder = new ArrayList<>();
		Set<String> parsedIds = new HashSet<>();
		for (String id : order)
			if (id != null && !id.isBlank()) {
				id = id.trim();

				if (parsedIds.add(id))
					parsedOrder.add(id);
			}

		if (parsedOrder.isEmpty())
			return folios;

		Hashtable<String, Folio> idFolios = new Hashtable<>();
		for (Folio folio : folios)
			idFolios.put(folio.getId(), folio);

		// Set the new order
		List<Folio> newOrder = new ArrayList<>();
		for (String id : parsedOrder)
			if (idFolios.containsKey(id))
				newOrder.add(idFolios.get(id));

		// The remainder folios
		Set<String> orderSet = new HashSet<>(parsedOrder);
		List<Folio> remainderOrder = new ArrayList<>();
		for (Folio folio : folios)
			if (!orderSet.contains(folio.getId()))
				remainderOrder.add(folio);

		if (!remainderOrder.isEmpty()) {
			if (isAfter)
				newOrder.addAll(remainderOrder);
			else
				newOrder.addAll(0, remainderOrder);
		}

		return newOrder;
	}

	/**
	 * Update the folios metadata.
	 * 
	 * @param folios   The folios.
	 * @param metadata The metadata of the folios to update.
	 * @return The folios.
	 * @since 1.8
	 */
	public static List<Folio> update(List<Folio> folios, Collection<Metadata> metadata) {
		if (folios == null || folios.isEmpty() || metadata == null || metadata.isEmpty())
			return folios;

		// index the folios
		Hashtable<String, Folio> indexed = new Hashtable<>();
		for (Folio folio : folios)
			indexed.put(folio.getId(), folio);

		// update metadata
		for (Metadata update : metadata)
			if (update != null && update.getId() != null && !update.getId().isBlank()) {
				Folio folio = indexed.get(update.getId().trim());

				if (folio != null) {
					if (update.getName() != null && !update.getName().isBlank())
						folio.setName(update.getName().trim());

					if (update.getKeywords() == null)
						folio.setKeywords(null);
					else {
						Set<String> keywords = new HashSet<>();
						for (String keyword : update.getKeywords())
							if (keyword != null && !keyword.isBlank())
								keywords.add(keyword.trim());

						folio.setKeywords(keywords.isEmpty() ? null : keywords);
					}

					folio.setPageXMLType(update.getPageXMLType());
				}
			}

		return folios;
	}

	/**
	 * Metadata is an immutable class that defines metadata for update.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Metadata extends IdentificationRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The id.
		 */
		private final String id;

		/**
		 * The name without extension.
		 */
		private final String name;

		/**
		 * The keywords.
		 */
		private final Set<String> keywords;

		/**
		 * The PAGE XML type.
		 */
		private final Folio.PageXMLType pageXMLType;

		/**
		 * Creates a metadata.
		 * 
		 * @param id          The id.
		 * @param name        The name without extension.
		 * @param keywords    The keywords.
		 * @param pageXMLType The PAGE XML type.
		 * @since 1.8
		 */
		public Metadata(String id, String name, Set<String> keywords, PageXMLType pageXMLType) {
			super();

			this.id = id;
			this.name = name;
			this.keywords = keywords;
			this.pageXMLType = pageXMLType;
		}

		/**
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 1.8
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the keywords.
		 *
		 * @return The keywords.
		 * @since 1.8
		 */
		public Set<String> getKeywords() {
			return keywords;
		}

		/**
		 * Returns the PAGE XML type.
		 *
		 * @return The PAGE XML type.
		 * @since 1.8
		 */
		public Folio.PageXMLType getPageXMLType() {
			return pageXMLType;
		}

	}
}
