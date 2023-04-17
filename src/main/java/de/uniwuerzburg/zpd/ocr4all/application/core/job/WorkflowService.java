/**
 * File:     WorkflowService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.job
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.job;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;

/**
 * Defines workflow services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class WorkflowService extends CoreService {
	/**
	 * The security service.
	 */
	protected final SecurityService securityService;

	/**
	 * Creates a workflow service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public WorkflowService(ConfigurationService configurationService, SecurityService securityService) {
		super(WorkflowService.class, configurationService);

		this.securityService = securityService;
		
		// TODO: securityService.isCoordinator()
	}

}
