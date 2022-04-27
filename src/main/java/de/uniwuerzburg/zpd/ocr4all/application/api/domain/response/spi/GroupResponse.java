/**
 * File:     GroupResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.util.List;
import java.util.Locale;

import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Group;

/**
 * Defines group responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class GroupResponse extends EntryResponse {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * True if the collapsible group container is open at the beginning. Otherwise,
	 * it is closed.
	 */
	private boolean isOpen;

	/**
	 * The entries.
	 */
	private List<EntryResponse> entries;

	/**
	 * Creates a group response for the api.
	 * 
	 * @param locale The locale.
	 * @param group  The group.
	 * @since 1.8
	 */
	public GroupResponse(Locale locale, Group group) {
		super(locale, Type.group, group);

		isOpen = group.isOpen();

		entries = ServiceProviderResponse.getEntryResponses(locale, group.getEntries());
	}

	/**
	 * Returns true if the collapsible group container is open at the beginning.
	 * Otherwise, it is closed.
	 *
	 * @return True if the collapsible group container is open at the beginning.
	 *         Otherwise, it is closed.
	 * @since 1.8
	 */
	public boolean isOpen() {
		return isOpen;
	}

	/**
	 * Set to true if the collapsible group container is open at the beginning.
	 * Otherwise, it is closed.
	 *
	 * @param isOpen The open flag to set.
	 * @since 1.8
	 */
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	/**
	 * Returns the entries.
	 *
	 * @return The entries.
	 * @since 1.8
	 */
	public List<EntryResponse> getEntries() {
		return entries;
	}

	/**
	 * Set the entries.
	 *
	 * @param entries The entries to set.
	 * @since 1.8
	 */
	public void setEntries(List<EntryResponse> entries) {
		this.entries = entries;
	}

}
