/**
 * File:     AdministrationServerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.administration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.06.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.administration;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Password;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityServerService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;

/**
 * Defines administration services for server profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("server")
@Service
public class AdministrationServerService extends AdministrationService {
	/**
	 * The account service.
	 */
	private final AccountService accountService;

	/**
	 * Creates an administration service for a server profile.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param accountService       The account service.
	 * @since 1.8
	 */
	public AdministrationServerService(ConfigurationService configurationService, SecurityServerService securityService,
			AccountService accountService) {
		super(AdministrationServerService.class, configurationService, securityService);

		this.accountService = accountService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getUsers()
	 */
	@Override
	public List<User> getUsers() {
		return accountService.getUsers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isUserAvailable(java.lang.String)
	 */
	@Override
	public boolean isUserAvailable(String login) {
		return accountService.isUserAvailable(login);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String login) {
		return accountService.getUser(login);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isPasswordAvailable(java.lang.String)
	 */
	@Override
	public boolean isPasswordAvailable(String login) {
		return accountService.isPasswordAvailable(login);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getGroups()
	 */
	@Override
	public List<Group> getGroups() {
		return accountService.getGroups();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isGroupAvailable(java.lang.String)
	 */
	@Override
	public boolean isGroupAvailable(String label) {
		return accountService.isGroupAvailable(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getGroup(java.lang.String)
	 */
	@Override
	public Group getGroup(String label) {
		return accountService.getGroup(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#persist(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.User)
	 */
	@Override
	public boolean persist(User user) {
		return accountService.persist(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#persist(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.Password)
	 */
	@Override
	public boolean persist(Password password) {
		return accountService.persist(password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#persist(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.Group)
	 */
	@Override
	public boolean persist(Group group) {
		return accountService.persist(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#resetGroups(de.uniwuerzburg.zpd.ocr4all.application.
	 * core.security.User)
	 */
	@Override
	public boolean resetGroups(User user) {
		return accountService.resetGroups(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#setGroups(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.User, java.util.Collection)
	 */
	@Override
	public boolean setGroups(User user, Collection<String> groups) {
		return accountService.setGroups(user, groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#remove(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.User)
	 */
	@Override
	public boolean remove(User user) {
		return accountService.remove(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#remove(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.Password)
	 */
	@Override
	public boolean remove(Password password) {
		return accountService.remove(password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#remove(de.uniwuerzburg.zpd.ocr4all.application.core.
	 * security.Group)
	 */
	@Override
	public boolean remove(Group group) {
		return accountService.remove(group);
	}

}
