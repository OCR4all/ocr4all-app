/**
 * File:     HistoryResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ActionHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ProcessHistory;

/**
 * Defines history responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class HistoryResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The history entries.
	 */
	private List<Entry> entries;

	/**
	 * Creates a history response for the api.
	 * 
	 * @param history The history.
	 * @since 1.8
	 */
	public HistoryResponse(List<History> history) {
		super();

		if (history != null) {
			entries = new ArrayList<>();
			for (History entry : history)
				if (entry != null)
					entries.add(new Entry(entry));
		}
	}

	/**
	 * Returns the entries.
	 *
	 * @return The entries.
	 * @since 1.8
	 */
	public List<Entry> getEntries() {
		return entries;
	}

	/**
	 * Set the entries.
	 *
	 * @param entries The entries to set.
	 * @since 1.8
	 */
	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	/**
	 * Defines history entries.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Entry implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Defines history types.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		private enum Type {
			unknown, action, process;

			/**
			 * Returns the history type.
			 * 
			 * @param history The history.
			 * @return The history type.
			 * @since 1.8
			 */
			public static Type getType(History history) {
				return (history instanceof ActionHistory) ? action
						: ((history instanceof ProcessHistory) ? process : unknown);

			}
		}

		/**
		 * The entry type.
		 */
		private String type;

		/**
		 * The history.
		 */
		private History history;

		/**
		 * Creates a history entry.
		 * 
		 * @param history The history.
		 * @since 1.8
		 */
		public Entry(History history) {
			super();

			type = Type.getType(history).name();

			this.history = history;
		}

		/**
		 * Returns the type.
		 *
		 * @return The type.
		 * @since 1.8
		 */
		public String getType() {
			return type;
		}

		/**
		 * Set the type.
		 *
		 * @param type The type to set.
		 * @since 1.8
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Returns the history.
		 *
		 * @return The history.
		 * @since 1.8
		 */
		public History getHistory() {
			return history;
		}

		/**
		 * Set the history.
		 *
		 * @param history The history to set.
		 * @since 1.8
		 */
		public void setHistory(History history) {
			this.history = history;
		}

	}

}
