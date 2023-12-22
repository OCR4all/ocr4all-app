/**
 * File:     ImageFormat.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     13.07.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines image formats.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public enum ImageFormat {
	tif("TIFF", false, "tiff"), jpg("JPEG", true, "jpeg"), png("PNG", true);

	/**
	 * The mime type prefix.
	 */
	private static final String mimeTypePrefix = "image/";

	/**
	 * The label.
	 */
	private final String label;

	/**
	 * True if the image is suitable for using on web pages.
	 */
	private final boolean isWebPages;

	/**
	 * The extensions.
	 */
	private final Set<String> extensions = new HashSet<>();

	/**
	 * The path matcher.
	 */
	private final PathMatcher matcher;

	/**
	 * Creates an image format.
	 * 
	 * @param label      The label.
	 * @param isWebPages True if the image is suitable for using on web pages.
	 * @param extensions The extensions.
	 * @since 1.8
	 */
	private ImageFormat(String label, boolean isWebPages, String... extensions) {
		this.label = label;
		this.isWebPages = isWebPages;

		StringBuffer pattern = new StringBuffer(extensions.length == 0 ? "" : "{");

		this.extensions.add(this.name().toLowerCase());
		pattern.append(this.name().toLowerCase());

		for (String extension : extensions) {
			this.extensions.add(extension.toLowerCase());
			pattern.append("," + extension.toLowerCase());
		}

		if (extensions.length > 0)
			pattern.append("}");

		matcher = FileSystems.getDefault().getPathMatcher("glob:**." + pattern.toString());
	}

	/**
	 * Returns the mime type.
	 * 
	 * @return The mime type.
	 * @since 1.8
	 */
	public String getMimeType() {
		return mimeTypePrefix + name();
	}

	/**
	 * Returns the label.
	 *
	 * @return The label.
	 * @since 1.8
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns true if the image is suitable for using on web pages.
	 *
	 * @return True if the image is suitable for using on web pages.
	 * @since 1.8
	 */
	public boolean isWebPages() {
		return isWebPages;
	}

	/**
	 * Returns true if the given extension is valid for this image format.
	 * 
	 * @param extension The extension.
	 * @return True if the given extension is valid for this image format.
	 * @since 1.8
	 */
	public boolean isExtension(String extension) {
		return extension != null && extensions.contains(extension.trim().toLowerCase());
	}

	/**
	 * Returns true if the given file name matches the image format.
	 * 
	 * @param filename The file name.
	 * @return True if the given file name matches the image format.
	 * @since 1.8
	 */
	private boolean matches(String filename) {
		return filename != null && matcher.matches(Paths.get(filename.toLowerCase()));
	}

	/**
	 * Returns true if the given file name matches the image format.
	 * 
	 * @param filename The file name.
	 * @return True if the given file name matches the image format.
	 * @since 1.8
	 */
	public boolean matches(Path filename) {
		return filename != null && matches(filename.toString());
	}

	/**
	 * Returns the respective persistence image format.
	 * 
	 * @return The respective persistence image format. Null if not available.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat getPersistence() {
		switch (this) {
		case tif:
			return de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat.tif;

		case jpg:
			return de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat.jpg;

		case png:
			return de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat.png;

		default:
			return null;
		}
	}

	/**
	 * Returns the respective image format for given persistence image format.
	 * 
	 * @param format The persistence image format.
	 * @return The respective image format. Null if not available.
	 * @since 1.8
	 */
	public static ImageFormat getImageFormat(
			de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat format) {
		if (format != null)
			switch (format) {
			case jpg:
				return ImageFormat.jpg;

			case png:
				return ImageFormat.png;

			case tif:
				return ImageFormat.tif;
			}

		return null;
	}

	/**
	 * Returns the respective spi image format.
	 * 
	 * @return The respective persistence spi format. Null if not available.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.spi.util.ImageFormat getSPI() {
		switch (this) {
		case tif:
			return de.uniwuerzburg.zpd.ocr4all.application.spi.util.ImageFormat.tif;

		case jpg:
			return de.uniwuerzburg.zpd.ocr4all.application.spi.util.ImageFormat.jpg;

		case png:
			return de.uniwuerzburg.zpd.ocr4all.application.spi.util.ImageFormat.png;

		default:
			return null;
		}
	}

	/**
	 * Returns the respective image format for given spi image format.
	 * 
	 * @param format The spi image format.
	 * @return The respective image format. Null if not available.
	 * @since 1.8
	 */
	public static ImageFormat getImageFormat(de.uniwuerzburg.zpd.ocr4all.application.spi.util.ImageFormat format) {
		if (format != null)
			switch (format) {
			case jpg:
				return ImageFormat.jpg;

			case png:
				return ImageFormat.png;

			case tif:
				return ImageFormat.tif;
			}

		return null;
	}

	/**
	 * Returns the image format for given extension.
	 * 
	 * @param extension The extension.
	 * @return The image format for given extension. Null if no image format matches
	 *         the extension.
	 * @since 1.8
	 */
	public static ImageFormat getImageFormat(String extension) {
		return getImageFormat(extension, null);
	}

	/**
	 * Returns the image format for given extension.
	 * 
	 * @param extension     The extension.
	 * @param defaultFormat The default format to return if no image format matches
	 *                      the extension. If null, then returns null if no image
	 *                      format matches the extension.
	 * @return The image format for given extension.
	 * @since 1.8
	 */
	public static ImageFormat getImageFormat(String extension, ImageFormat defaultFormat) {
		for (ImageFormat imageFormat : ImageFormat.values())
			if (imageFormat.isExtension(extension))
				return imageFormat;

		return defaultFormat;
	}

	/**
	 * Returns the image format for given file name.
	 * 
	 * @param name The file name.
	 * @return The image format for given file name. Null if not supported.
	 * @since 1.8
	 */
	public static ImageFormat getImageFormatFilename(String name) {
		if (name != null && !name.isBlank()) {
			name = name.trim();
			
			for (ImageFormat imageFormat : ImageFormat.values())
				if (imageFormat.matches(name))
					return imageFormat;
		}

		return null;
	}

}
