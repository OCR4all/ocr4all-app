/**
 * File:     AdministrationServerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.administration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     07.06.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.administration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Group;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.Password;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityDesktopService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.User;

/**
 * Defines administration services for desktop profiles.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("desktop")
@Service
public class AdministrationDesktopService extends AdministrationService {
	/**
	 * Creates an administration service for a desktop profile.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public AdministrationDesktopService(ConfigurationService configurationService,
			SecurityDesktopService securityService) {
		super(AdministrationDesktopService.class, configurationService, securityService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getUsers()
	 */
	@Override
	public List<User> getUsers() {
		return new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isUserAvailable(java.lang.String)
	 */
	@Override
	public boolean isUserAvailable(String login) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String login) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isPasswordAvailable(java.lang.String)
	 */
	@Override
	public boolean isPasswordAvailable(String login) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getGroups()
	 */
	@Override
	public List<Group> getGroups() {
		return new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#isGroupAvailable(java.lang.String)
	 */
	@Override
	public boolean isGroupAvailable(String label) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.administration.
	 * AdministrationService#getGroup(java.lang.String)
	 */
	@Override
	public Group getGroup(String label) {
		return null;
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
		return false;
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
		return false;
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
		return false;
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
		return false;
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
		return false;
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
		return false;
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
		return false;
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
		return false;
	}

}
