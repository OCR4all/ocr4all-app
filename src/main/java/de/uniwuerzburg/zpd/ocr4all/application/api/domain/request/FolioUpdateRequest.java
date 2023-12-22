/**
 * File:     FolioUpdateRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.request
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     15.12.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Defines folio update requests for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class FolioUpdateRequest implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The metadata.
	 */
	@NotNull
	private List<Metadata> metadata;

	/**
	 * Returns the metadata.
	 *
	 * @return The metadata.
	 * @since 1.8
	 */
	public List<Metadata> getMetadata() {
		return metadata;
	}

	/**
	 * Set the metadata.
	 *
	 * @param metadata The metadata to set.
	 * @since 1.8
	 */
	public void setMetadata(List<Metadata> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Defines metadata for update.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Metadata extends IdentifierRequest {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The name without extension.
		 */
		@NotBlank
		private String name;

		/**
		 * The keywords.
		 */
		private Set<String> keywords;

		/**
		 * The PAGE XML type.
		 */
		@JsonProperty("page-xml-type")
		private Folio.PageXMLType pageXMLType;

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
		 * Set the name.
		 *
		 * @param name The name to set.
		 * @since 1.8
		 */
		public void setName(String name) {
			this.name = name;
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
		 * Set the keywords.
		 *
		 * @param keywords The keywords to set.
		 * @since 1.8
		 */
		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
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

		/**
		 * Set the PAGE XML type.
		 *
		 * @param pageXMLType The type to set.
		 * @since 1.8
		 */
		public void setPageXMLType(Folio.PageXMLType pageXMLType) {
			this.pageXMLType = pageXMLType;
		}

	}
}
