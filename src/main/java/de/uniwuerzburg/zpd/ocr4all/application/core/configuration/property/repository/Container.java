/**
 * File:     Container.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.repository;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.FolderDefault;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines ocr4all container properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Container {
	/**
	 * The default folios folder.
	 */
	private static final String defaultFoliosFolder = "folios";

	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The folios.
	 */
	private FolderDefault folios = new FolderDefault(defaultFoliosFolder);

	/**
	 * The derivatives.
	 */
	private Derivatives derivatives = new Derivatives();

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration.
	 *
	 * @param configuration The configuration to set.
	 * @since 1.8
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Returns the folios.
	 *
	 * @return The folios.
	 * @since 1.8
	 */
	public FolderDefault getFolios() {
		return folios;
	}

	/**
	 * Set the folios.
	 *
	 * @param folios The folios to set.
	 * @since 1.8
	 */
	public void setFolios(FolderDefault folios) {
		this.folios = folios;
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
	 * Set the derivatives.
	 *
	 * @param derivatives The derivatives to set.
	 * @since 1.8
	 */
	public void setDerivatives(Derivatives derivatives) {
		this.derivatives = derivatives;
	}

	/**
	 * Defines configuration properties for ocr4all repository.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = ".container";

		/**
		 * The folder. The default value is .container.
		 */
		private String folder = defaultFolder;

		/**
		 * The files.
		 */
		private Files files = new Files();

		/**
		 * Returns the folder.
		 *
		 * @return The folder.
		 * @since 1.8
		 */
		public String getFolder() {
			return OCR4all.getNotEmpty(folder, defaultFolder);
		}

		/**
		 * Set the folder.
		 *
		 * @param folder The folder to set.
		 * @since 1.8
		 */
		public void setFolder(String folder) {
			this.folder = folder;
		}

		/**
		 * Returns the files.
		 *
		 * @return The files.
		 * @since 1.8
		 */
		public Files getFiles() {
			return files;
		}

		/**
		 * Set the files.
		 *
		 * @param files The files to set.
		 * @since 1.8
		 */
		public void setFiles(Files files) {
			this.files = files;
		}
	}

	/**
	 * Defines files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Files {
		/**
		 * The default main file name.
		 */
		private static final String defaultMainFileName = "container";

		/**
		 * The default folio file name.
		 */
		private static final String defaultFolioFileName = "folio";

		/**
		 * The main file name. The default value is container.
		 */
		private String main = defaultMainFileName;

		/**
		 * The folio file name. The default value is folio.
		 */
		private String folio = defaultFolioFileName;

		/**
		 * Returns the main file name.
		 *
		 * @return The main file name.
		 * @since 1.8
		 */
		public String getMain() {
			return OCR4all.getNotEmpty(main, defaultMainFileName);
		}

		/**
		 * Set the main file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setMain(String fileName) {
			main = fileName;
		}

		/**
		 * Returns the folio file name.
		 *
		 * @return The folio file name.
		 * @since 1.8
		 */
		public String getFolio() {
			return OCR4all.getNotEmpty(folio, defaultFolioFileName);
		}

		/**
		 * Set the folio file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setFolio(String fileName) {
			folio = fileName;
		}

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
		 * The default folder.
		 */
		private static final String defaultFolder = "derivatives";

		/**
		 * The default format.
		 */
		private static final String defaultFormat = "jpg";

		/**
		 * The folder. The default value is derivatives.
		 */
		private String folder = defaultFolder;

		/**
		 * The format. The default value is jpg.
		 */
		private String format = defaultFormat;

		/**
		 * The quality.
		 */
		private Quality quality = new Quality();

		/**
		 * Returns the folder.
		 *
		 * @return The folder.
		 * @since 1.8
		 */
		public String getFolder() {
			return OCR4all.getNotEmpty(folder, defaultFolder);
		}

		/**
		 * Set the folder.
		 *
		 * @param folder The folder to set.
		 * @since 1.8
		 */
		public void setFolder(String folder) {
			this.folder = folder;
		}

		/**
		 * Returns the format.
		 *
		 * @return The format.
		 * @since 1.8
		 */
		public String getFormat() {
			return OCR4all.getNotEmpty(format, defaultFormat);
		}

		/**
		 * Set the format.
		 *
		 * @param format The format to set.
		 * @since 1.8
		 */
		public void setFormat(String format) {
			this.format = format;
		}

		/**
		 * Returns the quality.
		 *
		 * @return The quality.
		 * @since 1.8
		 */
		public Quality getQuality() {
			return quality;
		}

		/**
		 * Set the quality.
		 *
		 * @param quality The quality to set.
		 * @since 1.8
		 */
		public void setQuality(Quality quality) {
			this.quality = quality;
		}

	}

	/**
	 * Defines image derivatives qualities.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Quality {
		/**
		 * The default thumbnail folder.
		 */
		private static final String defaultThumbnailFolder = "thumbnail";

		/**
		 * The default detail folder.
		 */
		private static final String defaultDetailFolder = "detail";

		/**
		 * The default best folder.
		 */
		private static final String defaultBestFolder = "best";

		/**
		 * The thumbnail.
		 */
		private FolderDefault thumbnail = new FolderDefault(defaultThumbnailFolder);

		/**
		 * The detail.
		 */
		private FolderDefault detail = new FolderDefault(defaultDetailFolder);

		/**
		 * The best.
		 */
		private FolderDefault best = new FolderDefault(defaultBestFolder);

		/**
		 * Returns the thumbnail.
		 *
		 * @return The thumbnail.
		 * @since 1.8
		 */
		public FolderDefault getThumbnail() {
			return thumbnail;
		}

		/**
		 * Set the thumbnail.
		 *
		 * @param thumbnail The thumbnail to set.
		 * @since 1.8
		 */
		public void setThumbnail(FolderDefault thumbnail) {
			this.thumbnail = thumbnail;
		}

		/**
		 * Returns the detail.
		 *
		 * @return The detail.
		 * @since 1.8
		 */
		public FolderDefault getDetail() {
			return detail;
		}

		/**
		 * Set the detail.
		 *
		 * @param detail The detail to set.
		 * @since 1.8
		 */
		public void setDetail(FolderDefault detail) {
			this.detail = detail;
		}

		/**
		 * Returns the best.
		 *
		 * @return The best.
		 * @since 1.8
		 */
		public FolderDefault getBest() {
			return best;
		}

		/**
		 * Set the best.
		 *
		 * @param best The best to set.
		 * @since 1.8
		 */
		public void setBest(FolderDefault best) {
			this.best = best;
		}

	}

}
