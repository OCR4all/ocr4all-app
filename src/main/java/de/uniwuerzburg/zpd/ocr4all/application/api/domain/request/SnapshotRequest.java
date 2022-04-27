/**
 * File:     SnapshotRequest.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.request
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     21.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.request;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Defines snapshot requests for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SnapshotRequest implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The track. The track for a root snapshot is an empty list.
	 */
	@NotNull
	private List<Integer> track;

	/**
	 * Returns the track. The track for a root snapshot is an empty list.
	 *
	 * @return The track.
	 * @since 1.8
	 */
	public List<Integer> getTrack() {
		return track;
	}

	/**
	 * Set the track. The track for a root snapshot is an empty list.
	 *
	 * @param track The track to set.
	 * @since 1.8
	 */
	public void setTrack(List<Integer> track) {
		this.track = track;
	}

}
