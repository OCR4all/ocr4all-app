/**
 * File:     Image.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     30.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

/**
 * Defines ocr4all image properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Image {
	/**
	 * The derivatives.
	 */
	private Derivatives derivatives = new Derivatives();

	/**
	 * Returns the derivatives.
	 *
	 * @return The derivatives.
	 * @since 1.8
	 */
	public Derivatives getDerivatives() {
		return derivatives;
	}

	/**
	 * Set the derivatives.
	 *
	 * @param derivatives The derivatives to set.
	 * @since 1.8
	 */
	public void setDerivatives(Derivatives derivatives) {
		this.derivatives = derivatives;
	}

	/**
	 * Defines image derivatives.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Derivatives {
		/**
		 * The default quality.
		 */
		private static final int defaultQuality = 50;

		/**
		 * The default best maximal size.
		 */
		private static final String defaultBestMaximalSize = "1536x1536";

		/**
		 * The default detail maximal size.
		 */
		private static final String defaultDetailMaximalSize = "768x768";

		/**
		 * The default thumbnail maximal size.
		 */
		private static final String defaultThumbnailMaximalSize = "128x128";

		/**
		 * The best.
		 */
		private Resolution best = new Resolution(defaultQuality, defaultBestMaximalSize);

		/**
		 * The best.
		 */
		private Resolution detail = new Resolution(defaultQuality, defaultDetailMaximalSize);

		/**
		 * The best.
		 */
		private Resolution thumbnail = new Resolution(defaultQuality, defaultThumbnailMaximalSize);

		/**
		 * Returns the best.
		 *
		 * @return The best.
		 * @since 1.8
		 */
		public Resolution getBest() {
			return best;
		}

		/**
		 * Set the best.
		 *
		 * @param best The best to set.
		 * @since 1.8
		 */
		public void setBest(Resolution best) {
			this.best = best;
		}

		/**
		 * Returns the detail.
		 *
		 * @return The detail.
		 * @since 1.8
		 */
		public Resolution getDetail() {
			return detail;
		}

		/**
		 * Set the detail.
		 *
		 * @param detail The detail to set.
		 * @since 1.8
		 */
		public void setDetail(Resolution detail) {
			this.detail = detail;
		}

		/**
		 * Returns the thumbnail.
		 *
		 * @return The thumbnail.
		 * @since 1.8
		 */
		public Resolution getThumbnail() {
			return thumbnail;
		}

		/**
		 * Set the thumbnail.
		 *
		 * @param thumbnail The thumbnail to set.
		 * @since 1.8
		 */
		public void setThumbnail(Resolution thumbnail) {
			this.thumbnail = thumbnail;
		}

	}

	/**
	 * Defines image resolutions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Resolution {
		/**
		 * The quality.
		 */
		private int quality;

		/**
		 * The maximal size.
		 */
		private String maxSize;

		/**
		 * Default constructor for an image resolution.
		 * 
		 * @since 1.8
		 */
		public Resolution() {
			super();
		}

		/**
		 * Creates an image resolution.
		 * 
		 * @param quality The quality.
		 * @param maxSize The maximal size.
		 * @since 1.8
		 */
		public Resolution(int quality, String maxSize) {
			super();
			this.quality = quality;
			this.maxSize = maxSize;
		}

		/**
		 * Returns the quality.
		 *
		 * @return The quality.
		 * @since 1.8
		 */
		public int getQuality() {
			return quality;
		}

		/**
		 * Set the quality.
		 *
		 * @param quality The quality to set.
		 * @since 1.8
		 */
		public void setQuality(int quality) {
			this.quality = quality;
		}

		/**
		 * Returns the maximal size.
		 *
		 * @return The maximal size.
		 * @since 1.8
		 */
		public String getMaxSize() {
			return maxSize;
		}

		/**
		 * Set the maximal size.
		 *
		 * @param maxSize The maximal size to set.
		 * @since 1.8
		 */
		public void setMaxSize(String maxSize) {
			this.maxSize = maxSize;
		}

	}
}
