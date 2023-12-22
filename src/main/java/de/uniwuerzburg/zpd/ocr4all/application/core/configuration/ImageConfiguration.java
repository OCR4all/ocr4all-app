/**
 * File:     ImageConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     30.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Image;

/**
 * Defines configurations for the images.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ImageConfiguration {
	/**
	 * The derivatives.
	 */
	private final Derivatives derivatives;

	/**
	 * Creates a configuration for the images.
	 * 
	 * @param properties The image properties.
	 * @since 1.8
	 */
	public ImageConfiguration(Image properties) {
		super();

		derivatives = new Derivatives(properties);
	}

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
	 * Defines image derivatives.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Derivatives {
		/**
		 * The best.
		 */
		private final Resolution best;

		/**
		 * The best.
		 */
		private Resolution detail;

		/**
		 * The best.
		 */
		private Resolution thumbnail;

		/**
		 * Creates a configuration for the images.
		 * 
		 * @param properties The image properties.
		 * @since 1.8
		 */
		public Derivatives(Image properties) {
			super();

			best = new Resolution(properties.getDerivatives().getBest().getQuality(),
					properties.getDerivatives().getBest().getMaxSize());
			detail = new Resolution(properties.getDerivatives().getDetail().getQuality(),
					properties.getDerivatives().getDetail().getMaxSize());
			thumbnail = new Resolution(properties.getDerivatives().getThumbnail().getQuality(),
					properties.getDerivatives().getThumbnail().getMaxSize());
		}

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
		 * Returns the detail.
		 *
		 * @return The detail.
		 * @since 1.8
		 */
		public Resolution getDetail() {
			return detail;
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
		 * Returns the maximal size.
		 *
		 * @return The maximal size.
		 * @since 1.8
		 */
		public String getMaxSize() {
			return maxSize;
		}

	}

}
