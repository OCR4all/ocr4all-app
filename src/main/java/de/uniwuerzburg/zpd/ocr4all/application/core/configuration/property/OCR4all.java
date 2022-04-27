/**
 * File:     OCR4all.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Defines ocr4all properties.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Component
@ConfigurationProperties(prefix = "ocr4all")
@Validated
public class OCR4all {
	/**
	 * The application properties.
	 */
	private Application application = new Application();

	/**
	 * The system properties.
	 */
	private System system = new System();

	/**
	 * The exchange properties.
	 */
	private FolderRequired exchange = new FolderRequired();

	/**
	 * The workspace properties.
	 */
	private Workspace workspace = new Workspace();

	/**
	 * The opt properties.
	 */
	private FolderRequired opt = new FolderRequired();

	/**
	 * The temporary properties.
	 */
	private Temporary temporary = new Temporary();

	/**
	 * The api properties.
	 */
	private Api api = new Api();

	/**
	 * Returns the application properties.
	 *
	 * @return The application properties.
	 * @since 1.8
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * Set the application properties.
	 *
	 * @param application The application properties to set.
	 * @since 1.8
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * Returns the system properties.
	 *
	 * @return The system properties.
	 * @since 1.8
	 */
	public System getSystem() {
		return system;
	}

	/**
	 * Set the system properties.
	 *
	 * @param system The system properties to set.
	 * @since 1.8
	 */
	public void setSystem(System system) {
		this.system = system;
	}

	/**
	 * Returns the exchange properties.
	 *
	 * @return The exchange properties.
	 * @since 1.8
	 */
	public FolderRequired getExchange() {
		return exchange;
	}

	/**
	 * Set the exchange properties.
	 *
	 * @param exchange The exchange properties to set.
	 * @since 1.8
	 */
	public void setExchange(FolderRequired exchange) {
		this.exchange = exchange;
	}

	/**
	 * Returns the workspace properties.
	 *
	 * @return The workspace properties.
	 * @since 1.8
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Set the workspace properties.
	 *
	 * @param workspace The workspace properties to set.
	 * @since 1.8
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Returns the opt properties.
	 *
	 * @return The opt properties.
	 * @since 1.8
	 */
	public FolderRequired getOpt() {
		return opt;
	}

	/**
	 * Set the opt properties.
	 *
	 * @param opt The opt properties to set.
	 * @since 1.8
	 */
	public void setOpt(FolderRequired opt) {
		this.opt = opt;
	}

	/**
	 * Returns the temporary properties.
	 *
	 * @return The temporary properties.
	 * @since 1.8
	 */
	public Temporary getTemporary() {
		return temporary;
	}

	/**
	 * Set the temporary properties.
	 *
	 * @param temporary The temporary properties to set.
	 * @since 1.8
	 */
	public void setTemporary(Temporary temporary) {
		this.temporary = temporary;
	}

	/**
	 * Returns the api properties.
	 *
	 * @return The api properties.
	 * @since 1.8
	 */
	public Api getApi() {
		return api;
	}

	/**
	 * Set the api properties.
	 *
	 * @param api The api properties to set.
	 * @since 1.8
	 */
	public void setApi(Api api) {
		this.api = api;
	}

	/**
	 * Returns the given value it it is non null nor empty. Otherwise, returns the
	 * default value.
	 * 
	 * @param value        The value.
	 * @param defaultValue The default value.
	 * @return The value it it is non null nor empty. Otherwise, returns the default
	 *         value.
	 * @since 1.8
	 */
	public static String getNotEmpty(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}

}
