/**
 * File:     Priority.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     08.09.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Defines priority levels. Higher order priorities have a small ordinal number.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public enum Priority {
	/**
	 * The critical priority.
	 */
	critical,
	/**
	 * The high priority.
	 */
	high,
	/**
	 * The medium priority.
	 */
	medium,
	/**
	 * The low priority.
	 */
	low;

	/**
	 * Insert your text here
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@FunctionalInterface
	public interface Entity {
		/**
		 * Returns the priority level.
		 * 
		 * @return The priority level.
		 * @since 1.8
		 */
		public Priority getPriority();
	}

	/**
	 * Sorts the priorities with the higher priority at the top. Null priorities are
	 * at the end.
	 * 
	 * @param <T>        The entity type.
	 * @param priorities The priorities to sort.
	 * @since 1.8
	 */
	public static <T extends Entity> void sort(List<T> priorities) {
		sort(priorities, true);
	}

	/**
	 * Sorts the priorities. Null priorities are at the end.
	 * 
	 * @param <T>         The entity type.
	 * @param priorities  The priorities to sort.
	 * @param isAscending True if the sort order is ascending, i.e. the higher
	 *                    priorities come first. Otherwise, the sort order is
	 *                    descending, i.e. the lower priorities come first.
	 * @since 1.8
	 */
	public static <T extends Entity> void sort(List<T> priorities, boolean isAscending) {
		if (priorities != null) {
			final int flag = isAscending ? 1 : -1;

			Collections.sort(priorities, new Comparator<T>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				@Override
				public int compare(T o1, T o2) {
					if (o2 == null || o2.getPriority() == null)
						return -1;
					else if (o1 == null || o1.getPriority() == null)
						return 1;
					else
						return flag * (o1.getPriority().ordinal() - o2.getPriority().ordinal());
				}
			});
		}
	}
}
