/**
 * File:     SetResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     10.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.util.Hashtable;
import java.util.List;

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
	private List<String> files;

	/**
	 * Creates a set response for the api.
	 * 
	 * @param set      The set.
	 * @param setFiles The files for the sets.
	 * @since 1.8
	 */
	public SetResponse(de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set,
			Hashtable<String, List<String>> setFiles) {
		super(set.getDate(), set.getUser(), set.getKeywords(), set.getId(), set.getName());

		files = setFiles == null ? null : setFiles.get(set.getId());
	}

	/**
	 * Returns the files.
	 *
	 * @return The files.
	 * @since 17
	 */
	public List<String> getFiles() {
		return files;
	}

	/**
	 * Set the files.
	 *
	 * @param files The files to set.
	 * @since 17
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

}
