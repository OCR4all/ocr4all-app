/**
 * File:     DataService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.data
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     22.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.data;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.DataConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.DataService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;

/**
 * Defines data services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class DataService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The data configuration.
	 */
	private final DataConfiguration.Configuration configuration;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a data service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public DataService(ConfigurationService configurationService, SecurityService securityService) {
		super(DataService.class, configurationService);

		this.securityService = securityService;

		folder = configurationService.getData().getFolder().normalize();
		configuration = configurationService.getData().getConfiguration();
	}

	/**
	 * Returns true if the administrator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isAdministrator() {
		return securityService.isAdministrator();
	}

	/**
	 * Returns the data security if the administrator security permission is
	 * achievable by the session user.
	 * 
	 * @return The data security.
	 * @since 1.8
	 */
	public SecurityOwner getSecurity() {
		if (isAdministrator())
			return configuration.getSecurity();
		else
			return null;
	}

	/**
	 * updates the security and persists the main configuration if it is available
	 * and the administrator security permission is achievable by the session user.
	 *
	 * @param security The new security.
	 * @return True if the security was updated and persisted.
	 * @since 1.8
	 */
	public boolean update(SecurityOwner security) {
		return isAdministrator() && configuration.updateSecurity(securityService.getUser(), security);
	}

	/**
	 * Secures the data and persists the main configuration if it is available and
	 * the administrator security permission is achievable by the session user.
	 *
	 * @param isSecured True if the data is secured.
	 * @return True if the security was updated and persisted.
	 * @since 1.8
	 */
	public boolean secure(boolean isSecured) {
		return isAdministrator() && configuration.secure(securityService.getUser(), isSecured);
	}

	/**
	 * Returns the data configuration.
	 * 
	 * @return The data configuration. Null if no administrator security permission
	 *         is achievable by the session user.
	 * @since 1.8
	 */
	public DataConfiguration.Configuration getConfiguration() {
		return isAdministrator() ? configuration : null;
	}

	/**
	 * Returns true if a collection can be created.
	 * 
	 * @return True if a collection can be created.
	 * @since 1.8
	 */
	public boolean isCreateCollection() {
		return isAdministrator()
				|| configuration.isCreateCollection(securityService.getUser(), securityService.getActiveGroups());
	}

}
