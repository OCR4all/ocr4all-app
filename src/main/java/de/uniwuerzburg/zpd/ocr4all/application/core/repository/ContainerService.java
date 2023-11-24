/**
 * File:     ContainerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.ContainerConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.ContainerConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;

/**
 * Defines container services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class ContainerService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The repository service.
	 */
	private final RepositoryService repositoryService;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a container service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param repositoryService    The repository service.
	 * @since 1.8
	 */
	public ContainerService(ConfigurationService configurationService, SecurityService securityService,
			RepositoryService repositoryService) {
		super(ContainerService.class, configurationService);

		this.securityService = securityService;
		this.repositoryService = repositoryService;

		folder = configurationService.getRepository().getFolder().normalize();
	}

	/**
	 * Returns true if the administrator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isAdministrator() {
		return repositoryService.isAdministrator();
	}

	/**
	 * Returns true if a container can be created.
	 * 
	 * @return True if a container can be created.
	 * @since 1.8
	 */
	public boolean isCreate() {
		return repositoryService.isCreateContainer();
	}

	/**
	 * Creates a container.
	 * 
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The container configuration. Null if the container can not be
	 *         created.
	 * @since 1.8
	 */
	public ContainerConfiguration create(String name, String description, Set<String> keywords) {
		if (isCreate()) {
			final String user = securityService.getUser();
			final Path folder = Paths.get(ContainerService.this.folder.toString(), OCR4allUtils.getUUID()).normalize();

			try {
				Files.createDirectory(folder);

				logger.info("Created container folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot create project '" + folder.toString() + "'" + (user == null ? "" : ", user=" + user)
						+ " - " + e.getMessage() + ".");

				return null;
			}

			return new ContainerConfiguration(configurationService.getRepository().getContainer(), folder,
					new Configuration.BasicData(user, name, description, keywords));
		} else
			return null;
	}

	/**
	 * Returns the container sorted by name.
	 * 
	 * @param isRightExist True if consider the given rights.
	 * @param rights       The required rights.
	 * @return The projects.
	 * @since 1.8
	 */
	public List<Container> getContainers() {
		List<Container> containers = new ArrayList<>();

		try {
			Files.list(ContainerService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith(".")) {
					ContainerConfiguration containerConfiguration = new ContainerConfiguration(
							configurationService.getRepository().getContainer(), path);
					containers.add(new Container(isAdministrator()
							? de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right
									.getMaximal()
							: containerConfiguration.getConfiguration().getMaxRight(securityService.getUser(),
									securityService.getActiveGroups()),
							containerConfiguration));
				}
			});
		} catch (IOException e) {
			logger.warn("Cannot not load containers - " + e.getMessage());
		}

		Collections.sort(containers, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return containers;
	}

	/**
	 * Container is an immutable class that defines containers .
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Container {
		/**
		 * The right.
		 */
		private final de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right right;

		/**
		 * The configuration.
		 */
		private final ContainerConfiguration configuration;

		/**
		 * Creates a container.
		 * 
		 * @param right         The right.
		 * @param configuration The configuration.
		 * @since 1.8
		 */
		public Container(de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right right,
				ContainerConfiguration configuration) {
			super();
			this.right = right;
			this.configuration = configuration;
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 1.8
		 */
		public de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right getRight() {
			return right;
		}

		/**
		 * Returns the configuration.
		 *
		 * @return The configuration.
		 * @since 1.8
		 */
		public ContainerConfiguration getConfiguration() {
			return configuration;
		}

	}

}
