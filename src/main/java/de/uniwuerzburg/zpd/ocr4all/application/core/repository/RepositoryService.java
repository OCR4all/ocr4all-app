/**
 * File:     RepositoryService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.repository;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.RepositoryConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;

/**
 * Defines repository services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class RepositoryService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The repository configuration.
	 */
	private final RepositoryConfiguration.Configuration configuration;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a repository service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public RepositoryService(ConfigurationService configurationService, SecurityService securityService) {
		super(RepositoryService.class, configurationService);

		this.securityService = securityService;

		folder = configurationService.getRepository().getFolder().normalize();
		configuration = configurationService.getRepository().getConfiguration();
	}

	/**
	 * Returns the folder.
	 *
	 * @return The folder.
	 * @since 17
	 */
	public Path getFolder() {
		return folder;
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
	 * Returns the repository security if the administrator security permission is
	 * achievable by the session user.
	 * 
	 * @return The repository security.
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
	 * Secures the repository and persists the main configuration if it is available
	 * and the administrator security permission is achievable by the session user.
	 *
	 * @param isSecured True if the repository is secured.
	 * @return True if the security was updated and persisted.
	 * @since 1.8
	 */
	public boolean secure(boolean isSecured) {
		return isAdministrator() && configuration.secure(securityService.getUser(), isSecured);
	}

	/**
	 * Returns the repository configuration.
	 * 
	 * @return The repository configuration. Null if no administrator security
	 *         permission is achievable by the session user.
	 * @since 1.8
	 */
	public RepositoryConfiguration.Configuration getConfiguration() {
		return isAdministrator() ? configuration : null;
	}

	/**
	 * Returns true if a container can be created.
	 * 
	 * @return True if a container can be created.
	 * @since 1.8
	 */
	public boolean isCreateContainer() {
		return isAdministrator()
				|| configuration.isCreateContainer(securityService.getUser(), securityService.getActiveGroups());
	}

}
