/**
 * File:     AdministrationService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.administration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.06.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.administration;

import java.util.Collection;
import java.util.List;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Password;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;

/**
 * Defines administration services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public abstract class AdministrationService extends CoreService {
	/**
	 * The security service.
	 */
	protected final SecurityService securityService;

	/**
	 * Creates an administration service.
	 * 
	 * @param logger               The logger class.
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public AdministrationService(Class<?> logger, ConfigurationService configurationService,
			SecurityService securityService) {
		super(logger, configurationService);

		this.securityService = securityService;
	}

	/**
	 * Returns the users sorted by login.
	 * 
	 * @return The users sorted by login.
	 * @since 1.8
	 */
	public abstract List<User> getUsers();

	/**
	 * Returns true if the user is available.
	 * 
	 * @param login The user login.
	 * @return True if the user is available.
	 * @since 1.8
	 */
	public abstract boolean isUserAvailable(String login);

	/**
	 * Returns the user.
	 * 
	 * @param login The user login.
	 * @return The user. Null if unknown.
	 * @since 1.8
	 */
	public abstract User getUser(String login);

	/**
	 * Returns true if the password is available.
	 * 
	 * @param login The user login.
	 * @return True if the password is available.
	 * @since 1.8
	 */
	public abstract boolean isPasswordAvailable(String login);

	/**
	 * Returns the groups sorted by label.
	 * 
	 * @return The groups sorted by label.
	 * @since 1.8
	 */
	public abstract List<Group> getGroups();

	/**
	 * Returns true if the group is available.
	 * 
	 * @param label The group label.
	 * @return True if the group is available.
	 * @since 1.8
	 */
	public abstract boolean isGroupAvailable(String label);

	/**
	 * Returns the group.
	 * 
	 * @param label The group label.
	 * @return The group. Null if unknown.
	 * @since 1.8
	 */
	public abstract Group getGroup(String label);

	/**
	 * Persists the user.
	 * 
	 * @param user The user to persist.
	 * @return True iff the user could be persisted.
	 * @since 1.8
	 */
	public abstract boolean persist(User user);

	/**
	 * Persists the password.
	 * 
	 * @param password The password to persist.
	 * @return True iff the password could be persisted.
	 * @since 1.8
	 */
	public abstract boolean persist(Password password);

	/**
	 * Persists the group.
	 * 
	 * @param group The group to persist.
	 * @return True iff the group could be persisted.
	 * @since 1.8
	 */
	public abstract boolean persist(Group group);

	/**
	 * Reset the groups of the given user.
	 * 
	 * @param user The user.
	 * @return True if updated.
	 * @since 1.8
	 */
	public abstract boolean resetGroups(User user);

	/**
	 * Set the groups of the given user.
	 * 
	 * @param user   The user.
	 * @param groups The group labels to set. If null or empty, remove all groups
	 *               from user.
	 * @return True if updated.
	 * @since 1.8
	 */
	public abstract boolean setGroups(User user, Collection<String> groups);

	/**
	 * Removes the user.
	 * 
	 * @param user The user to remove.
	 * @return True iff the user could be removed.
	 * @since 1.8
	 */
	public abstract boolean remove(User user);

	/**
	 * Removes the password.
	 * 
	 * @param password The password to remove.
	 * @return True iff the password could be removed.
	 * @since 1.8
	 */
	public abstract boolean remove(Password password);

	/**
	 * Removes the group.
	 * 
	 * @param group The group to remove.
	 * @return True iff the group could be removed.
	 * @since 1.8
	 */
	public abstract boolean remove(Group group);

}
