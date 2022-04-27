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
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.api.domain.ProjectSecurity;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;

/**
 * Defines project responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ProjectResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id.
	 */
	private String id;

	/**
	 * The folder for exchange.
	 */
	private String exchange;

	/**
	 * True if the folder for exchange is available.
	 */
	private boolean isExchangeAvailable;

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The description.
	 */
	private String description;

	/**
	 * The state.
	 */
	private String state;

	/**
	 * The tracking.
	 */
	private TrackingResponse tracking;

	/**
	 * The done time stamp.
	 */
	private Date done;

	/**
	 * The keywords.
	 */
	private Set<String> keywords;

	/**
	 * The rights.
	 */
	private ProjectSecurity.Right right;

	/**
	 * Creates a project response for the api.
	 * 
	 * @param project The project.
	 * @since 1.8
	 */
	public ProjectResponse(Project project) {
		super();

		id = project.getId();

		exchange = project.getConfiguration().getConfiguration().getExchangeSubfolder();
		isExchangeAvailable = project.getConfiguration().getConfiguration().isExchangeDirectory();

		name = project.getName();
		description = project.getDescription();

		state = project.getState().name();

		tracking = new TrackingResponse(project.getUser(), project.getConfiguration().getConfiguration().getCreated(),
				project.getConfiguration().getConfiguration().getUpdated());

		done = project.getConfiguration().getConfiguration().getDone();
		keywords = project.getConfiguration().getConfiguration().getKeywords();

		right = new ProjectSecurity.Right(project);
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 *
	 * @param id The id to set.
	 * @since 1.8
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the folder for exchange.
	 *
	 * @return The folder for exchange.
	 * @since 1.8
	 */
	public String getExchange() {
		return exchange;
	}

	/**
	 * Set the folder for exchange.
	 *
	 * @param exchange The folder for exchange to set.
	 * @since 1.8
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	/**
	 * Returns true if the folder for exchange is available.
	 *
	 * @return True if the folder for exchange is available.
	 * @since 1.8
	 */
	public boolean isExchangeAvailable() {
		return isExchangeAvailable;
	}

	/**
	 * Set true if the folder for exchange is available.
	 *
	 * @param isExchangeAvailable The exchange available flag to set.
	 * @since 1.8
	 */
	public void setExchangeAvailable(boolean isExchangeAvailable) {
		this.isExchangeAvailable = isExchangeAvailable;
	}

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
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description.
	 *
	 * @param description The description to set.
	 * @since 1.8
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public String getState() {
		return state;
	}

	/**
	 * Set the state.
	 *
	 * @param state The state to set.
	 * @since 1.8
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Returns the tracking.
	 *
	 * @return The tracking.
	 * @since 1.8
	 */
	public TrackingResponse getTracking() {
		return tracking;
	}

	/**
	 * Set the tracking.
	 *
	 * @param tracking The tracking to set.
	 * @since 1.8
	 */
	public void setTracking(TrackingResponse tracking) {
		this.tracking = tracking;
	}

	/**
	 * Returns the done time stamp.
	 *
	 * @return The done time stamp.
	 * @since 1.8
	 */
	public Date getDone() {
		return done;
	}

	/**
	 * Set the done time stamp.
	 *
	 * @param done The done time stamp to set.
	 * @since 1.8
	 */
	public void setDone(Date done) {
		this.done = done;
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
	 * Returns the right.
	 *
	 * @return The right.
	 * @since 1.8
	 */
	public ProjectSecurity.Right getRight() {
		return right;
	}

	/**
	 * Set the right.
	 *
	 * @param right The right to set.
	 * @since 1.8
	 */
	public void setRight(ProjectSecurity.Right right) {
		this.right = right;
	}

}
