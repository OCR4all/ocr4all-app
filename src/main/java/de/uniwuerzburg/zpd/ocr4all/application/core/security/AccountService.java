/**
 * File:     AccountService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.WorkspaceConfiguration;

/**
 * Defines account services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("server")
@Service
@ApplicationScope
@Configuration
public class AccountService extends CoreService implements UserDetailsService {
	/**
	 * Defines user roles.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Role {
		ADMIN, COORD, USER;

		/**
		 * The role prefix.
		 */
		public static final String grantedAuthorityPrefix = "ROLE_";

		/**
		 * Returns the granted authority representation of the role.
		 * 
		 * @return The granted authority representation of the role.
		 * @since 1.8
		 */
		public String getGrantedAuthority() {
			return grantedAuthorityPrefix + name();
		}

		/**
		 * Returns true if the given granted authority is represented by the role.
		 * 
		 * @param grantedAuthority The granted authority.
		 * @return True if the given granted authority is represented by the role.
		 * @since 1.8
		 */
		public boolean isGrantedAuthority(String grantedAuthority) {
			return getGrantedAuthority().equals(grantedAuthority);
		}
	}

	/**
	 * The users. The key is the user login and the value the respective user.
	 */
	private final Hashtable<String, User> users = new Hashtable<>();

	/**
	 * The passwords. The key is the user login and the value the respective
	 * password.
	 */
	private final Hashtable<String, Password> passwords = new Hashtable<>();

	/**
	 * The groups. The key is the group label and the value the respective group.
	 */
	private final Hashtable<String, Group> groups = new Hashtable<>();

	/**
	 * The active groups. The key is the user login and the value are the respective
	 * groups.
	 */
	private final Hashtable<String, Set<String>> activeGroups = new Hashtable<>();

	/**
	 * Creates an account service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public AccountService(ConfigurationService configurationService) {
		super(AccountService.class, configurationService);

		if (configurationService.getWorkspace().getConfiguration().isFolderDirectory()) {
			loadUsers();
			loadPasswords();
			loadGroups();

			startApplicationFileMonitor(configurationService.getWorkspace().getConfiguration().getFolder().toFile());
		}

		/*
		 * Security groups
		 */
		if (configurationService.getApplication().isAdministratorGroupSet())
			logger.info("Administrator group " + configurationService.getApplication().getAdministratorGroup() + ".");
		else
			logger.warn("No administrator group available.");

		if (configurationService.getApplication().isCoordinatorGroupSet())
			logger.info("Coordinator group " + configurationService.getApplication().getCoordinatorGroup() + ".");
		else
			logger.warn("No coordinator group available.");

		/*
		 * Default administrator user
		 */
		if (configurationService.getApplication().getDefaultAdministrator().isCreate()
				&& configurationService.getApplication().isAdministratorGroupSet())
			try {
				createDefaultAdministrator(configurationService.getApplication().getDefaultAdministrator().getLogin(),
						configurationService.getApplication().getDefaultAdministrator().getPassword());
			} catch (Exception e) {
				logger.warn("The default administrator cannot be created because his login is not available.");
			}
	}

	/**
	 * Creates the default administrator user if not available.
	 * 
	 * @param login    The login.
	 * @param password The password.
	 * @throws IllegalArgumentException Thrown if the login is null or empty.
	 * @since 1.8
	 */
	private void createDefaultAdministrator(String login, String password) throws IllegalArgumentException {
		final String administratorGroup = SecurityEntity
				.filter(configurationService.getApplication().getAdministratorGroup());

		User user = null;
		Group group = getGroup(administratorGroup);

		boolean isPersistGroup = group == null;
		if (group == null)
			group = new Group(administratorGroup, "Administrator group", null, null);
		else
			for (String administrator : group.getUsers()) {
				user = users.get(administrator);

				if (user != null)
					break;
			}

		if (user == null && getUser(login) == null) {
			user = new User(login, "Administrator user", null, null);
			persist(user);

			if (isPasswordAvailable(login))
				logger.info("Created default administrator user '" + user.getLogin() + "'.");
			else {
				persist(new Password(login, password));

				logger.info("Created default administrator user '" + user.getLogin() + "' with password '"
						+ (password == null ? "" : password) + "'.");
			}

			Set<String> userGroups = new HashSet<>(Arrays.asList(administratorGroup));
			if (isPersistGroup) {
				persist(group);

				logger.info("Created administrator group '" + group.getLabel() + "'.");
			} else
				for (Group userGroup : groups.values())
					if (userGroup.getUsers().contains(user.getLogin()))
						userGroups.add(userGroup.getLabel());

			setGroups(user, userGroups);
		}
	}

	/**
	 * Returns the service to encode the passwords.
	 * 
	 * @return The service to encode the passwords.
	 * @since 1.8
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * Returns the service to encode the passwords.
	 * 
	 * @return The service to encode the passwords.
	 * @since 1.8
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder();
	}

	/**
	 * Returns the user authentication.
	 * 
	 * @return The user authentication. Null if no authentication information is
	 *         available.
	 * @since 1.8
	 */
	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * Returns the user authentication name.
	 * 
	 * @return The user authentication name. Null if anonymous, this means, no user
	 *         is authenticated.
	 * @since 1.8
	 */
	public String getAuthenticationName() {
		Authentication authentication = getAuthentication();

		return authentication == null || (authentication instanceof AnonymousAuthenticationToken) ? null
				: (authentication.getPrincipal() instanceof Credential)
						? ((Credential) authentication.getPrincipal()).getUsername()
						: authentication.getName();
	}

	/**
	 * Adds the configuration file to the monitoring and file filters if it is
	 * available.
	 * 
	 * @param monitoring The monitoring buffer.
	 * @param files      The file filters.
	 * @param file       The file to add.
	 * @return The configuration file path. Empty, if the file is not available.
	 * @since 1.8
	 */
	private Path add(StringBuffer monitoring, List<IOFileFilter> files, Path file) {
		String filename = file.getFileName().toString();
		files.add(FileFilterUtils.nameFileFilter(filename));

		monitoring.append((monitoring.length() == 0 ? "" : ", ") + filename);

		return file;
	}

	/**
	 * Starts the application file monitor.
	 * 
	 * @param directory The directory to observe.
	 * @since 1.8
	 */
	private void startApplicationFileMonitor(File directory) {
		StringBuffer monitoring = new StringBuffer();
		List<IOFileFilter> files = new ArrayList<>();

		final Path user = add(monitoring, files, configurationService.getWorkspace().getConfiguration().getUserFile());
		final Path password = add(monitoring, files,
				configurationService.getWorkspace().getConfiguration().getPasswordFile());
		final Path group = add(monitoring, files,
				configurationService.getWorkspace().getConfiguration().getGroupFile());

		if (files.isEmpty())
			logger.warn("No configuration files to monitor in directory '" + directory + "'.");
		else {
			FileAlterationObserver observer = new FileAlterationObserver(directory,
					FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
							FileFilterUtils.or(files.toArray(new IOFileFilter[files.size()]))));
			observer.addListener(new FileAlterationListenerAdaptor() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.apache.commons.io.monitor.FileAlterationListenerAdaptor#onFileCreate(java
				 * .io.File)
				 */
				@Override
				public void onFileCreate(File file) {
					logger.debug("Created monitored file '" + file.getAbsolutePath() + "'.");

					onFileChange(file);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.apache.commons.io.monitor.FileAlterationListenerAdaptor#onFileDelete(java
				 * .io.File)
				 */
				@Override
				public void onFileDelete(File file) {
					logger.debug("Deleted monitored file '" + file.getAbsolutePath() + "'.");

					if (user.getFileName().toString().equals(file.getName())) {
						users.clear();

						logger.info("Removed users.");
					} else if (group.getFileName().toString().equals(file.getName())) {
						groups.clear();
						activeGroups.clear();

						logger.info("Removed groups.");
					} else if (password.getFileName().toString().equals(file.getName())) {
						passwords.clear();

						logger.info("Removed passwords.");
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.apache.commons.io.monitor.FileAlterationListenerAdaptor#onFileChange(java
				 * .io.File)
				 */
				@Override
				public void onFileChange(File file) {
					Path path = file.toPath();

					try {
						if (Files.isSameFile(path, user))
							loadUsers();
						else if (Files.isSameFile(path, group))
							loadGroups();
						else if (Files.isSameFile(path, password))
							loadPasswords();
					} catch (IOException e) {
						logger.warn("Cannot compare application file paths - " + e.getMessage());
					}
				}
			});

			FileAlterationMonitor monitor = new FileAlterationMonitor(
					configurationService.getApplication().getMonitorInterval());
			monitor.addObserver(observer);
			try {
				monitor.start();

				logger.info("Started configuration file monitor in directory '" + directory + "': "
						+ monitoring.toString() + " (monitoring interval "
						+ configurationService.getApplication().getMonitorInterval() + "ms).");
			} catch (Exception e) {
				logger.warn("Cannot start application file monitor - " + e.getMessage());
			}
		}
	}

	/**
	 * Loads the users.
	 * 
	 * @since 1.8
	 */
	private synchronized void loadUsers() {
		if (!users.isEmpty()) {
			users.clear();

			logger.debug("Removed previous users.");
		}

		final Path path = configurationService.getWorkspace().getConfiguration().getUserFile();
		final WorkspaceConfiguration.Version version = configurationService.getWorkspace().getConfiguration()
				.getVersion();

		configurationService.getWorkspace().load(Optional.of(path), WorkspaceConfiguration.getApplicationFileHeader(
				path, "workspace user configuration file", version, User.getParseSyntax(version)), (line, entry) -> {
					try {
						User user = new User(version, entry);

						if (users.containsKey(user.getLogin())) {
							logger.warn("Ignored duplicated user: " + user + " (line " + line + ").");

							return false;
						} else {
							users.put(user.getLogin(), user);

							logger.debug("Added user " + users.size() + ": " + user + " (line " + line + ").");

							return true;
						}
					} catch (Exception e) {
						logger.warn("Cannot parse user entry '" + entry + "' (line " + line + ") - " + e.getMessage());

						return false;
					}
				}, (file, lines, added, total) -> logger
						.info("Loaded " + added + "/" + total + " users: " + file + " (lines " + lines + ")."));
	}

	/**
	 * Loads the passwords.
	 * 
	 * @since 1.8
	 */
	private synchronized void loadPasswords() {
		if (!passwords.isEmpty()) {
			passwords.clear();

			logger.debug("Removed previous passwords.");
		}

		final Path path = configurationService.getWorkspace().getConfiguration().getPasswordFile();
		final WorkspaceConfiguration.Version version = configurationService.getWorkspace().getConfiguration()
				.getVersion();

		List<String> description = new ArrayList<>();
		description.add("Currently bcrypt encryption is considered to be very secure:");
		description.add("  Synopsis: htpasswd -BC 10 " + path + " login");
		description.add("  Password algorithm: {bcrypt}");

		configurationService.getWorkspace().load(Optional.of(path),
				WorkspaceConfiguration.getApplicationFileHeader(path, "workspace password configuration file", version,
						description, Password.getParseSyntax(version)),
				(line, entry) -> {
					try {
						Password password = new Password(version, entry);

						if (passwords.containsKey(password.getLogin())) {
							logger.warn("Ignored duplicated password: " + password + " (line " + line + ").");

							return false;
						} else {
							passwords.put(password.getLogin(), password);

							logger.debug(
									"Added password " + passwords.size() + ": " + password + " (line " + line + ").");

							return true;
						}
					} catch (Exception e) {
						logger.warn(
								"Cannot parse password entry '" + entry + "' (line " + line + ") - " + e.getMessage());

						return false;
					}
				}, (file, lines, added, total) -> logger
						.info("Loaded " + added + "/" + total + " passwords: " + file + " (lines " + lines + ")."));
	}

	/**
	 * Loads the groups.
	 * 
	 * @since 1.8
	 */
	private synchronized void loadGroups() {
		if (!groups.isEmpty()) {
			groups.clear();
			activeGroups.clear();

			logger.debug("Removed previous groups.");
		}

		List<String> description = null;

		if (configurationService.getApplication().isAdministratorGroupSet()
				|| configurationService.getApplication().isCoordinatorGroupSet()) {
			description = new ArrayList<>();
			description.add("Synopsis security groups: ");

			if (configurationService.getApplication().isAdministratorGroupSet())
				description.add("  " + configurationService.getApplication().getAdministratorGroup()
						+ ":active::administrator group");

			if (configurationService.getApplication().isCoordinatorGroupSet())
				description.add("  " + configurationService.getApplication().getCoordinatorGroup()
						+ ":active::coordinator group");
		}

		final Path path = configurationService.getWorkspace().getConfiguration().getGroupFile();
		final WorkspaceConfiguration.Version version = configurationService.getWorkspace().getConfiguration()
				.getVersion();

		configurationService.getWorkspace()
				.load(Optional.of(path), WorkspaceConfiguration.getApplicationFileHeader(path,
						"workspace group configuration file", version, description, Group.getParseSyntax(version)),
						(line, entry) -> {
							try {
								Group group = new Group(version, entry);

								if (groups.containsKey(group.getLabel())) {
									logger.warn("Ignored duplicated group: " + group + " (line " + line + ").");

									return false;
								} else {
									groups.put(group.getLabel(), group);

									if (State.active.equals(group.getState()))
										for (String user : group.getUsers()) {
											Set<String> currentGroups = activeGroups.get(user);
											if (currentGroups == null) {
												currentGroups = new HashSet<>();
												activeGroups.put(user, currentGroups);
											}

											currentGroups.add(group.getLabel());
										}

									logger.debug(
											"Added group " + groups.size() + ": " + group + " (line " + line + ").");

									return true;
								}
							} catch (Exception e) {
								logger.warn("Cannot parse group entry '" + entry + "' (line " + line + ") - "
										+ e.getMessage());

								return false;
							}
						}, (file, lines, added, total) -> logger.info(
								"Loaded " + added + "/" + total + " groups: " + file + " (lines " + lines + ")."));
	}

	/**
	 * Returns the users sorted by login.
	 * 
	 * @return The users sorted by login.
	 * @since 1.8
	 */
	public List<User> getUsers() {
		List<User> list = new ArrayList<>(users.values());

		Collections.sort(list, (u1, u2) -> u1.getLogin().compareTo(u2.getLogin()));

		return list;
	}

	/**
	 * Returns true if the user is available.
	 * 
	 * @param login The user login.
	 * @return True if the user is available.
	 * @since 1.8
	 */
	public boolean isUserAvailable(String login) {
		login = SecurityEntity.filter(login);

		return login != null && users.containsKey(login.toLowerCase());
	}

	/**
	 * Returns the user.
	 * 
	 * @param login The user login.
	 * @return The user. Null if unknown.
	 * @since 1.8
	 */
	public User getUser(String login) {
		login = SecurityEntity.filter(login);

		return login == null ? null : users.get(login.toLowerCase());
	}

	/**
	 * Returns true if the password is available.
	 * 
	 * @param login The user login.
	 * @return True if the password is available.
	 * @since 1.8
	 */
	public boolean isPasswordAvailable(String login) {
		login = SecurityEntity.filter(login);

		return login != null && passwords.containsKey(login.toLowerCase());
	}

	/**
	 * Returns the groups sorted by label.
	 * 
	 * @return The groups sorted by label.
	 * @since 1.8
	 */
	public List<Group> getGroups() {
		List<Group> list = new ArrayList<>(groups.values());

		Collections.sort(list, (g1, g2) -> g1.getLabel().compareTo(g2.getLabel()));

		return list;
	}

	/**
	 * Returns true if the group is available.
	 * 
	 * @param label The group label.
	 * @return True if the group is available.
	 * @since 1.8
	 */
	public boolean isGroupAvailable(String label) {
		label = SecurityEntity.filter(label);

		return label != null && groups.containsKey(label.toLowerCase());
	}

	/**
	 * Returns the group.
	 * 
	 * @param label The group label.
	 * @return The group. Null if unknown.
	 * @since 1.8
	 */
	public Group getGroup(String label) {
		label = SecurityEntity.filter(label);

		return label == null ? null : groups.get(label.toLowerCase());
	}

	/**
	 * Returns the active groups for given user.
	 * 
	 * @param login The user login.
	 * @return The active groups.
	 * @since 1.8
	 */
	public Set<String> getActiveGroups(String login) {
		return activeGroups.containsKey(login) ? new HashSet<>(activeGroups.get(login)) : new HashSet<>();
	}

	/**
	 * Returns true if the user belongs to the administrator groups.
	 * 
	 * @param login The user login.
	 * @return True if the user belongs to the administrator groups.
	 * @since 1.8
	 */
	public boolean isAdministrator(String login) {
		if (login == null)
			return false;
		else {
			login = login.trim().toLowerCase();

			return configurationService.getApplication().isAdministratorGroupSet() && activeGroups.containsKey(login)
					&& activeGroups.get(login).contains(configurationService.getApplication().getAdministratorGroup());
		}
	}

	/**
	 * Returns true if the user belongs to the coordinator groups.
	 * 
	 * @param login The user login.
	 * @return True if the user belongs to the coordinator groups.
	 * @since 1.8
	 */
	public boolean isCoordinator(String login) {
		if (login == null)
			return false;
		else {
			login = login.trim().toLowerCase();

			return configurationService.getApplication().isCoordinatorGroupSet() && activeGroups.containsKey(login)
					&& activeGroups.get(login).contains(configurationService.getApplication().getCoordinatorGroup());
		}
	}

	/**
	 * Persists the item in the file. If the item is available, then updates it.
	 * 
	 * @param label    The file label.
	 * @param file     The file to be updated. If it is not available, then it will
	 *                 be created with the item.
	 * @param entityID The entity ID to be updated. This is the term before the
	 *                 first delimiter in the file lines.
	 * @param item     The item to persist.
	 * @return True if the file was persisted.
	 * @since 1.8
	 */
	private boolean persist(String label, Path file, String entityID, String item) {
		StringBuffer buffer = new StringBuffer();
		boolean isUpdated = false;

		// Searches for the item in the file and updates it if it exists
		if (Files.exists(file))
			try {
				for (String line : Files.readAllLines(file, configurationService.getApplication().getCharset())) {
					String entry = line.trim();

					if (!entry.isEmpty() && !entry.startsWith(WorkspaceConfiguration.applicationFileCommentCharacter)
							&& entityID.equals(
									entry.split(SecurityEntity.configurationSeparator, 2)[0].trim().toLowerCase())) {
						// Updates the available element, but ignores duplicates
						if (!isUpdated) {
							buffer.append(item + System.lineSeparator());

							isUpdated = true;
						}
					} else
						buffer.append(line + System.lineSeparator());
				}

			} catch (IOException ioe) {
				logger.warn("Cannot read " + label + " file '" + file + "' - " + ioe.getMessage());

				return false;
			}

		// This is a new item
		if (!isUpdated)
			buffer.append(item + System.lineSeparator());

		// Save the file
		try (BufferedWriter writer = Files.newBufferedWriter(file,
				configurationService.getApplication().getCharset())) {
			writer.write(buffer.toString());

		} catch (IOException ioe) {
			logger.warn("Cannot persist " + label + " file '" + file + "' - " + ioe.getMessage());

			return false;
		}

		return true;

	}

	/**
	 * Persists the user.
	 * 
	 * @param user The user to persist.
	 * @return True iff the user could be persisted.
	 * @since 1.8
	 */
	public synchronized boolean persist(User user) {
		if (user != null && persist("user", configurationService.getWorkspace().getConfiguration().getUserFile(),
				user.getLogin(),
				user.getConfigurationEntry(configurationService.getWorkspace().getConfiguration().getVersion()))) {
			loadUsers();

			return true;
		} else
			return false;
	}

	/**
	 * Persists the password.
	 * 
	 * @param password The password to persist.
	 * @return True iff the password could be persisted.
	 * @since 1.8
	 */
	public synchronized boolean persist(Password password) {
		if (password != null && persist("password",
				configurationService.getWorkspace().getConfiguration().getPasswordFile(), password.getLogin(),
				password.getConfigurationEntry(configurationService.getWorkspace().getConfiguration().getVersion(),
						passwordEncoder()))) {
			loadPasswords();

			return true;
		} else
			return false;
	}

	/**
	 * Persists the group.
	 * 
	 * @param group The group to persist.
	 * @return True iff the group could be persisted.
	 * @since 1.8
	 */
	public synchronized boolean persist(Group group) {
		if (group != null && persist("group", configurationService.getWorkspace().getConfiguration().getGroupFile(),
				group.getLabel(),
				group.getConfigurationEntry(configurationService.getWorkspace().getConfiguration().getVersion()))) {
			loadGroups();

			return true;
		} else
			return false;
	}

	/**
	 * Reset the groups of the given user.
	 * 
	 * @param user The user.
	 * @return True if updated.
	 * @since 1.8
	 */
	public boolean resetGroups(User user) {
		return setGroups(user, null);
	}

	/**
	 * Set the groups of the given user.
	 * 
	 * @param user   The user.
	 * @param groups The group labels to set. If null or empty, remove all groups
	 *               from user.
	 * @return True if updated.
	 * @since 1.8
	 */
	public synchronized boolean setGroups(User user, Collection<String> groups) {
		if (user == null)
			return false;

		Set<String> labels = new HashSet<>();
		if (groups != null)
			for (String label : groups) {
				label = SecurityEntity.filter(label);
				if (label != null)
					labels.add(label);
			}

		StringBuffer buffer = new StringBuffer();
		boolean isUpdated = false;

		// Searches for the item in the file and updates it if it exists
		Path file = configurationService.getWorkspace().getConfiguration().getGroupFile();
		if (Files.exists(file))
			try {
				WorkspaceConfiguration.Version version = configurationService.getWorkspace().getConfiguration()
						.getVersion();
				for (String line : Files.readAllLines(file, configurationService.getApplication().getCharset())) {
					String entry = line.trim();

					if (entry.isEmpty() || entry.startsWith(WorkspaceConfiguration.applicationFileCommentCharacter))
						buffer.append(line + System.lineSeparator());
					else {
						Group group = new Group(version, entry);
						Set<String> users = group.getUsers();
						if (users.contains(user.getLogin())) {
							if (labels.contains(group.getLabel()))
								buffer.append(line + System.lineSeparator());
							else {
								users.remove(user.getLogin());
								buffer.append((new Group(group.getLabel(), group.getName(), users, group.getState()))
										.getConfigurationEntry(version) + System.lineSeparator());

								isUpdated = true;
							}
						} else {
							if (labels.contains(group.getLabel())) {
								users.add(user.getLogin());
								buffer.append((new Group(group.getLabel(), group.getName(), users, group.getState()))
										.getConfigurationEntry(version) + System.lineSeparator());

								isUpdated = true;
							} else
								buffer.append(line + System.lineSeparator());
						}
					}
				}

			} catch (IOException ioe) {
				logger.warn("Cannot read group file '" + file + "' - " + ioe.getMessage());

				return false;
			}

		// Save the file
		if (isUpdated) {
			try (BufferedWriter writer = Files.newBufferedWriter(file,
					configurationService.getApplication().getCharset())) {
				writer.write(buffer.toString());

			} catch (IOException ioe) {
				logger.warn("Cannot persist group file '" + file + "' - " + ioe.getMessage());

				return false;
			}

			loadGroups();
		}

		return true;
	}

	/**
	 * Removes the item from the file if available.
	 * 
	 * @param label    The file label.
	 * @param file     The file.
	 * @param entityID The entity ID to be removed. This is the term before the
	 *                 first delimiter in the file lines.
	 * @return True if the item was removed from the file.
	 * @since 1.8
	 */
	private boolean remove(String label, Path file, String entityID) {
		StringBuffer buffer = new StringBuffer();
		boolean isUpdated = false;

		// Searches for the item in the file and updates it if it exists
		if (Files.exists(file))
			try {
				for (String line : Files.readAllLines(file, configurationService.getApplication().getCharset())) {
					String entry = line.trim();

					if (!entry.isEmpty() && !entry.startsWith(WorkspaceConfiguration.applicationFileCommentCharacter)
							&& entityID.equals(
									entry.split(SecurityEntity.configurationSeparator, 2)[0].trim().toLowerCase())) {
						isUpdated = true;
					} else
						buffer.append(line + System.lineSeparator());
				}

			} catch (IOException ioe) {
				logger.warn("Cannot read " + label + " file '" + file + "' - " + ioe.getMessage());

				return false;
			}

		// Save the file
		if (isUpdated)
			try (BufferedWriter writer = Files.newBufferedWriter(file,
					configurationService.getApplication().getCharset())) {
				writer.write(buffer.toString());

			} catch (IOException ioe) {
				logger.warn("Cannot persist " + label + " file '" + file + "' - " + ioe.getMessage());

				return false;
			}

		return true;

	}

	/**
	 * Removes the user.
	 * 
	 * @param user The user to remove.
	 * @return True iff the user could be removed.
	 * @since 1.8
	 */
	public synchronized boolean remove(User user) {
		if (user != null && remove("user", configurationService.getWorkspace().getConfiguration().getUserFile(),
				user.getLogin())) {
			loadUsers();

			return true;
		} else
			return false;
	}

	/**
	 * Removes the password.
	 * 
	 * @param password The password to remove.
	 * @return True iff the password could be removed.
	 * @since 1.8
	 */
	public synchronized boolean remove(Password password) {
		if (password != null && remove("password",
				configurationService.getWorkspace().getConfiguration().getPasswordFile(), password.getLogin())) {
			loadPasswords();

			return true;
		} else
			return false;
	}

	/**
	 * Removes the group.
	 * 
	 * @param group The group to remove.
	 * @return True iff the group could be removed.
	 * @since 1.8
	 */
	public synchronized boolean remove(Group group) {
		if (group != null && remove("group", configurationService.getWorkspace().getConfiguration().getGroupFile(),
				group.getLabel())) {
			loadGroups();

			return true;
		} else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.core.userdetails.UserDetailsService#
	 * loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		final String login = username.toLowerCase();

		if (!users.containsKey(login)) {
			logger.info("No available user '" + login + "'.");

			throw new UsernameNotFoundException("User '" + login + "' not found.");
		} else if (!passwords.containsKey(login)) {
			logger.info("No credentials found for user '" + login + "'.");

			throw new UsernameNotFoundException("Credentials for user '" + login + "' not found.");
		}

		final User user = users.get(login);
		final Password password = passwords.get(login);

		logger.info("Requested credentials for user '" + login + "' - state " + user.getState().name() + ".");

		return new UserDetails() {
			/**
			 * The serial version id number.
			 */
			private static final long serialVersionUID = 1L;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
			 */
			@Override
			public boolean isEnabled() {
				return State.active.equals(user.getState());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.springframework.security.core.userdetails.UserDetails#
			 * isCredentialsNonExpired()
			 */
			@Override
			public boolean isCredentialsNonExpired() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked(
			 * )
			 */
			@Override
			public boolean isAccountNonLocked() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired
			 * ()
			 */
			@Override
			public boolean isAccountNonExpired() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
			 */
			@Override
			public String getUsername() {
				return user.getLogin();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
			 */
			@Override
			public String getPassword() {
				return password.getHash();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.springframework.security.core.userdetails.UserDetails#getAuthorities()
			 */
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				List<SimpleGrantedAuthority> authorities = new ArrayList<>();

				authorities.add(new SimpleGrantedAuthority(Role.USER.getGrantedAuthority()));

				if (isCoordinator(login) || isAdministrator(login))
					authorities.add(new SimpleGrantedAuthority(Role.COORD.getGrantedAuthority()));

				if (isAdministrator(login))
					authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getGrantedAuthority()));

				return authorities;
			}
		};
	}

	/**
	 * Defines credentials.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public interface Credential {
		/**
		 * Returns the user name.
		 * 
		 * @return The user name.
		 * @since 1.8
		 */
		public String getUsername();
	}
}
