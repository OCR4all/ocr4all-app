/**
 * File:     ProjectResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.Date;

/**
 * Defines tracking responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class TrackingResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The user.
	 */
	private String user;

	/**
	 * The created time stamp.
	 */
	private Date created;

	/**
	 * The updated time stamp.
	 */
	private Date updated;

	/**
	 * Creates a tracking response for the api.
	 * 
	 * @param user    The user.
	 * @param created The created time stamp.
	 * @param updated The updated time stamp.
	 * @since 1.8
	 */
	public TrackingResponse(String user, Date created, Date updated) {
		super();

		this.user = user;
		this.created = created;
		this.updated = updated;

	}

	/**
	 * Returns the user.
	 *
	 * @return The user.
	 * @since 1.8
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Set the user.
	 *
	 * @param user The user to set.
	 * @since 1.8
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Returns the created time stamp.
	 *
	 * @return The created time stamp.
	 * @since 1.8
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Set the created time stamp.
	 *
	 * @param created The created time stamp to set.
	 * @since 1.8
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Returns the updated time stamp.
	 *
	 * @return The updated time stamp.
	 * @since 1.8
	 */
	public Date getUpdated() {
		return updated;
	}

	/**
	 * Set the updated time stamp.
	 *
	 * @param updated The updated time stamp to set.
	 * @since 1.8
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

}
