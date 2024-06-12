/**
 * File:     AssembleService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.assemble
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.06.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.assemble;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.assemble.AssembleConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityOwner;

/**
 * Defines assemble services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class AssembleService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The assemble configuration.
	 */
	private final AssembleConfiguration.Configuration configuration;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates an assemble service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public AssembleService(ConfigurationService configurationService, SecurityService securityService) {
		super(AssembleService.class, configurationService);

		this.securityService = securityService;

		folder = configurationService.getAssemble().getFolder().normalize();
		configuration = configurationService.getAssemble().getConfiguration();
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
	 * Returns the assemble security if the administrator security permission is
	 * achievable by the session user.
	 * 
	 * @return The assemble security.
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
	 * Secures the assemble and persists the main configuration if it is available
	 * and the administrator security permission is achievable by the session user.
	 *
	 * @param isSecured True if the assemble is secured.
	 * @return True if the security was updated and persisted.
	 * @since 1.8
	 */
	public boolean secure(boolean isSecured) {
		return isAdministrator() && configuration.secure(securityService.getUser(), isSecured);
	}

	/**
	 * Returns the assemble configuration.
	 * 
	 * @return The assemble configuration. Null if no administrator security
	 *         permission is achievable by the session user.
	 * @since 1.8
	 */
	public AssembleConfiguration.Configuration getConfiguration() {
		return isAdministrator() ? configuration : null;
	}

	/**
	 * Returns true if a model can be created.
	 * 
	 * @return True if a model can be created.
	 * @since 1.8
	 */
	public boolean isCreateModel() {
		return isAdministrator()
				|| configuration.isCreateModel(securityService.getUser(), securityService.getActiveGroups());
	}

}
