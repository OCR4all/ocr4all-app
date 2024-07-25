/**
 * File:     ExchangeService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.exchange
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.exchange;

import java.nio.file.Path;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines exchange services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class ExchangeService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a exchange service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 17
	 */
	public ExchangeService(ConfigurationService configurationService, SecurityService securityService) {
		super(ExchangeService.class, configurationService);

		this.securityService = securityService;

		folder = configurationService.getExchange().getFolder().normalize();
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
	 * @since 17
	 */
	public boolean isAdministrator() {
		return securityService.isAdministrator();
	}

	/**
	 * Returns true if a partition can be created.
	 * 
	 * @return True if a partition can be created.
	 * @since 17
	 */
	public boolean isCreatePartition() {
		return isAdministrator();
	}

}
