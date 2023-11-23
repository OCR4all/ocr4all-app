/**
 * File:     Repository.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.repository;

import java.util.Date;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.RepositoryConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

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
	 * The container service.
	 */
	private final Container container = new Container();

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

		configuration = configurationService.getRepository().getConfiguration();
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
	public de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security getSecurity() {
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
	public boolean updateSecurity(
			de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Repository.Security security) {
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
	 * Returns true if the tracking user is set and the administrator security
	 * permission is achievable by the session user.
	 *
	 * @return True if the tracking user is set and the administrator security
	 *         permission is achievable by the session user.
	 * @since 1.8
	 */
	public boolean isUserSet() {
		return isAdministrator() && configuration.isUserSet();
	}

	/**
	 * Returns the tracking user.
	 *
	 * @return The tracking user. Null if no administrator security permission is
	 *         achievable by the session user.
	 * @since 1.8
	 */
	public String getUser() {
		return isAdministrator() ? configuration.getUser() : null;
	}

	/**
	 * Returns true if the created time is set and the administrator security
	 * permission is achievable by the session user.
	 *
	 * @return True if the created time is set and the administrator security
	 *         permission is achievable by the session user.
	 * @since 1.8
	 */
	public boolean isCreatedSet() {
		return isAdministrator() && configuration.isCreatedSet();
	}

	/**
	 * Returns the created time.
	 *
	 * @return The created time. Null if no administrator security permission is
	 *         achievable by the session user.
	 * @since 1.8
	 */
	public Date getCreated() {
		return isAdministrator() ? configuration.getCreated() : null;
	}

	/**
	 * Returns true if the updated time is set and the administrator security
	 * permission is achievable by the session user.
	 *
	 * @return True if the updated time is set and the administrator security
	 *         permission is achievable by the session user.
	 * @since 1.8
	 */
	public boolean isUpdatedSet() {
		return isAdministrator() && configuration.isUpdatedSet();
	}

	/**
	 * Returns the updated time.
	 *
	 * @return The updated time. Null if no administrator security permission is
	 *         achievable by the session user.
	 * @since 1.8
	 */
	public Date getUpdated() {
		return isAdministrator() ? configuration.getUpdated() : null;
	}

	/**
	 * Returns the container service.
	 *
	 * @return The container service.
	 * @since 1.8
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * Defines container services.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public class Container {
		/**
		 * Returns true if a container can be created.
		 * 
		 * @return True if a container can be created.
		 * @since 1.8
		 */
		public boolean isCreate() {
			return securityService.isCoordinator()
					|| configuration.isCreateContainer(securityService.getUser(), securityService.getActiveGroups());
		}
	}
}
