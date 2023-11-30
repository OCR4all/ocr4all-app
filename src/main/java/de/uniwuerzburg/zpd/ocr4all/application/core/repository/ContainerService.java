/**
 * File:     ContainerService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.repository
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	 * Returns the container.
	 * 
	 * @param configuration The container configuration.
	 * @return The container.
	 * @since 1.8
	 */
	private Container getContainer(ContainerConfiguration configuration) {
		return new Container(repositoryService.isAdministrator()
				? de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right.maximal
				: configuration.getConfiguration().getRight(securityService.getUser(),
						securityService.getActiveGroups()),
				configuration);
	}

	/**
	 * Returns the container folder.
	 * 
	 * @param uuid The container uuid.
	 * @return The container folder. If the uuid is invalid, null is returned.
	 * @since 1.8
	 */
	private Path getPath(String uuid) {
		if (uuid == null || uuid.isBlank())
			return null;
		else {
			Path folder = Paths.get(this.folder.toString(), uuid.trim()).normalize();

			// Ignore directories beginning with a dot
			return Files.isDirectory(folder) && folder.getParent().equals(this.folder)
					&& !folder.getFileName().toString().startsWith(".") ? folder : null;
		}
	}

	/**
	 * Returns the container.
	 * 
	 * @param path The container path.
	 * @return The container.
	 * @since 1.8
	 */
	private Container getContainer(Path path) {
		return getContainer(new ContainerConfiguration(configurationService.getRepository().getContainer(), path));
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
	public Container create(String name, String description, Set<String> keywords) {
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

			ContainerConfiguration configuration = new ContainerConfiguration(
					configurationService.getRepository().getContainer(), folder,
					new Configuration.CoreData(user, name, description, keywords));

			return new Container(repositoryService.isAdministrator()
					? de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security.Right.maximal
					: configuration.getConfiguration().getRight(securityService.getUser(),
							securityService.getActiveGroups()),
					configuration);
		} else
			return null;
	}

	/**
	 * Returns the container.
	 * 
	 * @param uuid The container uuid.
	 * @return The container. Null if unknown.
	 * @since 1.8
	 */
	public Container getContainer(String uuid) {
		Path path = getPath(uuid);

		return path == null ? null : getContainer(path);
	}

	/**
	 * Returns the containers sorted by name.
	 * 
	 * @return The containers.
	 * @since 1.8
	 */
	public List<Container> getContainers() {
		List<Container> containers = new ArrayList<>();

		try {
			Files.list(ContainerService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith("."))
					containers.add(getContainer(path));
			});
		} catch (IOException e) {
			logger.warn("Cannot not load containers - " + e.getMessage());
		}

		Collections.sort(containers, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return containers;
	}

	/**
	 * Removes the container if the special right is fulfilled.
	 * 
	 * @param uuid The container uuid.
	 * @return True if the container could be removed.
	 * @since 1.8
	 */
	public boolean remove(String uuid) {
		Path path = getPath(uuid);

		if (path != null && getContainer(path).getRight().isSpecialFulfilled()) {
			try {
				Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

				if (!Files.exists(path)) {
					logger.info("Removed container '" + path.toString() + "'.");

					return true;
				} else
					logger.warn("Troubles removing the container '" + path.toString() + "'.");
			} catch (Exception e) {
				logger.warn("Cannot remove container '" + path.toString() + "' - " + e.getMessage() + ".");
			}
		} else {
			logger.warn("Cannot remove container '" + uuid + "'.");
		}

		return false;
	}

	/**
	 * Updates a container.
	 * 
	 * @param uuid        The container uuid.
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The container. Null if the container can not be updated.
	 * @since 1.8
	 */
	public Container update(String uuid, String name, String description, Set<String> keywords) {
		Path path = getPath(uuid);

		if (path != null) {
			Container container = getContainer(path);

			if (container.getRight().isSpecialFulfilled()
					&& container.getConfiguration().getConfiguration().update(securityService.getUser(),
							new ContainerConfiguration.Configuration.Information(name, description, keywords)))
				return container;
		}

		return null;
	}

	/**
	 * Updates the security.
	 *
	 * @param uuid     The container uuid.
	 * @param security The container security.
	 * @return The updated container. Null if it can not be updated.
	 * @since 1.8
	 */
	public Container update(String uuid,
			de.uniwuerzburg.zpd.ocr4all.application.persistence.repository.Container.Security security) {
		Path path = getPath(uuid);

		if (path != null) {
			Container container = getContainer(path);

			if (container.getRight().isSpecialFulfilled()
					&& container.getConfiguration().getConfiguration().update(securityService.getUser(), security))
				return container;
		}

		return null;
	}

	public int store(String uuid, MultipartFile[] files) {
		int stored = 0;

		if (files != null) {
			Path path = getPath(uuid);

			if (path != null) {
				Container container = getContainer(path);

				if (container.getRight().isWriteFulfilled()) {
					Path foliosRootLocation = container.getConfiguration().getImages().getFolios();

					for (MultipartFile file : files)
						if (!file.isEmpty())
							try {
								Path destinationFile = foliosRootLocation
										.resolve(Paths.get(file.getOriginalFilename()));
								try (InputStream inputStream = file.getInputStream()) {
									Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
									stored++;
								}
							} catch (IOException e) {
								logger.warn("Failed to store file '" + file.getOriginalFilename() + "' - "
										+ e.getMessage());
							}
				}
			}
		}

		return stored;
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
