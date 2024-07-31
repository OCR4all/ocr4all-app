/**
 * File:     SetResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     10.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;

/**
 * Defines set responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class SetResponse extends de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The files.
	 */
	private List<FileResponse> files;

	/**
	 * Creates a set response for the api.
	 * 
	 * @param set      The set.
	 * @param setFiles The files for the sets.
	 * @since 1.8
	 */
	public SetResponse(de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set,
			Hashtable<String, List<Path>> setFiles) {
		super(set.getDate(), set.getUser(), set.getKeywords(), set.getId(), set.getName());

		if (setFiles == null)
			files = null;
		else {
			List<Path> list = setFiles.get(set.getId());

			if (list == null)
				files = null;
			else {
				files = new ArrayList<>();

				for (Path file : list)
					if (file != null)
						files.add(new FileResponse(file));
			}
		}
	}

	/**
	 * Returns the files.
	 *
	 * @return The files.
	 * @since 17
	 */
	public List<FileResponse> getFiles() {
		return files;
	}

	/**
	 * Set the files.
	 *
	 * @param files The files to set.
	 * @since 17
	 */
	public void setFiles(List<FileResponse> files) {
		this.files = files;
	}

	/**
	 * Defines file responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class FileResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The name.
		 */
		private String name;

		/**
		 * The content type.
		 */
		@JsonProperty("content-type")
		private String contentType;

		/**
		 * The extension.
		 */
		private String extension;

		/**
		 * The sub type.
		 */
		@JsonProperty("sub-type")
		private String subType;

		/**
		 * Creates a file response for the api.
		 * 
		 * @param file The file.
		 * @since 17
		 */
		public FileResponse(Path file) {
			super();
			name = file.getFileName().toString();

			try {
				contentType = Files.probeContentType(file);
			} catch (IOException e) {
				contentType = null;
			}

			String[] split = name.split("\\.", 2);
			if (split.length == 2 && !split[0].isEmpty() && !split[1].isEmpty()) {
				extension = split[1];
				subType = OCR4allUtils.getNameWithoutExtension(split[1]);

				if (split[1].equals(subType))
					subType = null;
			} else {
				extension = null;
				subType = null;
			}
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the name.
		 *
		 * @param name The name to set.
		 * @since 17
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the content type.
		 *
		 * @return The content type.
		 * @since 17
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * Set the content type.
		 *
		 * @param contentType The content type to set.
		 * @since 17
		 */
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		/**
		 * Returns the extension.
		 *
		 * @return The extension.
		 * @since 17
		 */
		public String getExtension() {
			return extension;
		}

		/**
		 * Set the extension.
		 *
		 * @param extension The extension to set.
		 * @since 17
		 */
		public void setExtension(String extension) {
			this.extension = extension;
		}

		/**
		 * Returns the sub type.
		 *
		 * @return The sub type.
		 * @since 17
		 */
		public String getSubType() {
			return subType;
		}

		/**
		 * Set the sub type.
		 *
		 * @param subType The sub type to set.
		 * @since 17
		 */
		public void setSubType(String subType) {
			this.subType = subType;
		}

	}

}
