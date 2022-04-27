/**
 * File:     TrackingData.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     27.07.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.util.Date;

/**
 * Defines tracking data.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public interface TrackingData {
	/**
	 * Returns true if the user is set.
	 *
	 * @return True if the user is set.
	 * @since 1.8
	 */
	public boolean isUserSet();

	/**
	 * Returns the user.
	 *
	 * @return The user.
	 * @since 1.8
	 */
	public String getUser();

	/**
	 * Returns true if the created time is set.
	 *
	 * @return True if the created time is set.
	 * @since 1.8
	 */
	public boolean isCreatedSet();

	/**
	 * Returns the created time.
	 *
	 * @return The created time.
	 * @since 1.8
	 */
	public Date getCreated();

	/**
	 * Returns true if the updated time is set.
	 *
	 * @return True if the updated time is set.
	 * @since 1.8
	 */
	public boolean isUpdatedSet();

	/**
	 * Returns the updated time.
	 *
	 * @return The updated time.
	 * @since 1.8
	 */
	public Date getUpdated();

}
