/**
 * File:     Project.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project.projects
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.project;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.FolderDefault;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.OCR4all;

/**
 * Defines ocr4all project properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Project {
	/**
	 * The configuration.
	 */
	private Configuration configuration = new Configuration();

	/**
	 * The images.
	 */
	private Images images = new Images();

	/**
	 * The models.
	 */
	private Models models = new Models();

	/**
	 * The sandboxes.
	 */
	private Sandboxes sandboxes = new Sandboxes();

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
	 * Returns the images.
	 *
	 * @return The images.
	 * @since 1.8
	 */
	public Images getImages() {
		return images;
	}

	/**
	 * Set the images.
	 *
	 * @param images The images to set.
	 * @since 1.8
	 */
	public void setImages(Images images) {
		this.images = images;
	}

	/**
	 * Returns the models.
	 *
	 * @return The models.
	 * @since 1.8
	 */
	public Models getModels() {
		return models;
	}

	/**
	 * Set the models.
	 *
	 * @param models The models to set.
	 * @since 1.8
	 */
	public void setModels(Models models) {
		this.models = models;
	}

	/**
	 * Returns the sandboxes.
	 *
	 * @return The sandboxes.
	 * @since 1.8
	 */
	public Sandboxes getSandboxes() {
		return sandboxes;
	}

	/**
	 * Set the sandboxes.
	 *
	 * @param sandboxes The sandboxes to set.
	 * @since 1.8
	 */
	public void setSandboxes(Sandboxes sandboxes) {
		this.sandboxes = sandboxes;
	}

	/**
	 * Defines configuration properties for ocr4all projects.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Configuration {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = ".project";

		/**
		 * The folder. The default value is .project.
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
		private static final String defaultMainFileName = "project";

		/**
		 * The default folio file name.
		 */
		private static final String defaultFolioFileName = "folio";

		/**
		 * The default history file name.
		 */
		private static final String defaultHistoryFileName = "history";

		/**
		 * The main file name. The default value is project.
		 */
		private String main = defaultMainFileName;

		/**
		 * The folio file name. The default value is folio.
		 */
		private String folio = defaultFolioFileName;

		/**
		 * The history file name. The default value is history.
		 */
		private String history = defaultHistoryFileName;

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

		/**
		 * Returns the history file name.
		 *
		 * @return The history file name.
		 * @since 1.8
		 */
		public String getHistory() {
			return OCR4all.getNotEmpty(history, defaultHistoryFileName);
		}

		/**
		 * Set the history file name.
		 *
		 * @param fileName The file name to set.
		 * @since 1.8
		 */
		public void setHistory(String fileName) {
			history = fileName;
		}
	}

	/**
	 * Defines images.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Images {
		/**
		 * The default folder.
		 */
		private static final String defaultFolder = "images";

		/**
		 * The default folios folder.
		 */
		private static final String defaultFoliosFolder = "folios";

		/**
		 * The default normalized folder.
		 */
		private static final String defaultNormalizedFolder = "normalized";

		/**
		 * The folder. The default folder is images.
		 */
		private String folder = defaultFolder;

		/**
		 * The folios.
		 */
		private FolderDefault folios = new FolderDefault(defaultFoliosFolder);

		/**
		 * The normalized.
		 */
		private FolderDefault normalized = new FolderDefault(defaultNormalizedFolder);

		/**
		 * The derivatives.
		 */
		private Derivatives derivatives = new Derivatives();

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
		 * Returns the normalized.
		 *
		 * @return The normalized.
		 * @since 17
		 */
		public FolderDefault getNormalized() {
			return normalized;
		}

		/**
		 * Set the normalized.
		 *
		 * @param normalized The normalized to set.
		 * @since 17
		 */
		public void setNormalized(FolderDefault normalized) {
			this.normalized = normalized;
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
