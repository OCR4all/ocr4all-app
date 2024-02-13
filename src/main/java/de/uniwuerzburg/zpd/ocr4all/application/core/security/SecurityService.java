/**
 * File:     SecurityService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.application
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     25.05.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;

/**
 * Defines security services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class SecurityService extends CoreService {
	/**
	 * Defines security levels. Higher security levels have lesser enumeration order
	 * number.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Level {
		/**
		 * The running security level.
		 */
		administrator,
		/**
		 * The coordinator security level.
		 */
		coordinator,
		/**
		 * The user security level.
		 */
		user;

		/**
		 * Returns true if the required security level is achievable on actual security
		 * level.
		 * 
		 * @param actual   The actual security level.
		 * @param required The required security level.
		 * @return True if the required security level is achievable on actual security
		 *         level.
		 * @since 1.8
		 */
		public static boolean isAchievable(Level actual, Level required) {
			return actual != null && required != null && actual.ordinal() <= required.ordinal();
		}
	}

	/**
	 * Creates a security service.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	protected SecurityService(Class<? extends SecurityService> logger, ConfigurationService configurationService) {
		super(logger, configurationService);
	}

	/**
	 * Returns the user.
	 *
	 * @return The user. Null if not set, this means, it is either running in the
	 *         desktop profile or no user is logged in.
	 * @since 1.8
	 */
	public abstract String getUser();

	/**
	 * Returns true if the application is secured.
	 * 
	 * @return True if the application is secured.
	 * @since 1.8
	 */
	public abstract boolean isSecured();

	/**
	 * Returns the security level of the session user.
	 *
	 * @return The security level of the session user.
	 * @since 1.8
	 */
	public abstract Level getSecurityLevel();

	/**
	 * Returns true if the administrator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isAdministrator() {
		return Level.isAchievable(getSecurityLevel(), Level.administrator);
	}

	/**
	 * Returns true if the coordinator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the coordinator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isCoordinator() {
		return Level.isAchievable(getSecurityLevel(), Level.coordinator);
	}

	/**
	 * Returns true if the user security permission is achievable by the session
	 * user.
	 *
	 * @return True if the user security permission is achievable.
	 * @since 1.8
	 */
	public boolean isUser() {
		return Level.isAchievable(getSecurityLevel(), Level.user);
	}

	/**
	 * Returns the active groups of the session user.
	 * 
	 * @return The active groups.
	 * @since 1.8
	 */
	public abstract Set<String> getActiveGroups();

}
