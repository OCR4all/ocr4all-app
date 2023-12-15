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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ImageConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.ContainerConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.repository.ContainerConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageUtils;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

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

	/**
	 * Delete the supplied Path — for directories, recursively delete any nested
	 * directories or files as well.
	 * 
	 * @param path the root Path to delete
	 * @since 1.8
	 */
	private void deleteRecursively(Path path) {
		try {
			FileSystemUtils.deleteRecursively(path);
		} catch (IOException e) {
			logger.warn("cannot delete directory " + path + " - " + e.getMessage() + ".");
		}

	}

	/**
	 * Move the files from source folder to the target folder.
	 * 
	 * @param fileNames The file names.
	 * @param source    The source folder.
	 * @param target    The target folder.
	 * @throws IOException Throws if an I/O error occurs.
	 * @since 1.8
	 */
	private void move(List<String> fileNames, Path source, Path target) throws IOException {
		for (String fileName : fileNames)
			Files.move(Paths.get(source.toString(), fileName), Paths.get(target.toString(), fileName),
					StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * Removes the files from the folder.
	 * 
	 * @param fileNames The file names.
	 * @param folder    The folder.
	 * @return The number of files that could not be removed.
	 * @since 1.8
	 */
	private int remove(List<String> fileNames, Path folder) {
		int notRemoved = 0;
		for (String fileName : fileNames)
			try {
				Files.delete(Paths.get(folder.toString(), fileName));
			} catch (NoSuchFileException e) {
				// Nothing to do
			} catch (IOException e) {
				notRemoved++;
			}

		return notRemoved;
	}

	/**
	 * Store the folios.
	 * 
	 * @param container The container.
	 * @param files     The folios.
	 * @return The stored folios. Null if container is unknown or the write right is
	 *         not fulfilled.
	 * @throws IOException Throws on storage troubles.
	 * @since 1.8
	 */
	public List<Folio> store(Container container, MultipartFile[] files) throws IOException {
		if (container != null && files != null) {
			if (container.getRight().isWriteFulfilled()) {
				// The system commands
				final String identifyCommand = configurationService.getSystemCommand().getIdentify();
				if (!configurationService.getSystemCommand().isIdentifyAvailable())
					throw new IOException("the system identify command " + identifyCommand + " is not available.");

				final String convertCommand = configurationService.getSystemCommand().getConvert();
				if (!configurationService.getSystemCommand().isConvertAvailable())
					throw new IOException("the system convert command " + convertCommand + " is not available.");

				// create tmp directories
				Path temporaryDirectory = configurationService.getTemporary().getTemporaryDirectory();

				Path folderFolios = Paths.get(temporaryDirectory.toString(), "folios");
				Path folderThumbnail = Paths.get(temporaryDirectory.toString(), "thumbnail");
				Path folderDetail = Paths.get(temporaryDirectory.toString().toString(), "detail");
				Path folderBest = Paths.get(temporaryDirectory.toString().toString(), "best");

				try {
					Files.createDirectory(folderFolios);
					Files.createDirectory(folderThumbnail);
					Files.createDirectory(folderDetail);
					Files.createDirectory(folderBest);
				} catch (IOException e) {
					deleteRecursively(temporaryDirectory);

					throw e;
				}

				SystemProcess identifyJob = new SystemProcess(folderFolios, identifyCommand);

				// store the files
				List<Folio> folios = new ArrayList<>();
				for (MultipartFile file : files)
					if (file != null && !file.isEmpty()) {
						String fileName = file.getOriginalFilename();

						final ImageFormat imageFormat = ImageFormat.getImageFormatFilename(fileName);

						if (imageFormat != null)
							try {
								final de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat format = imageFormat
										.getPersistence();

								if (format == null) {
									logger.warn(
											"The folio does not supports the image type " + imageFormat.name() + ".");

									continue;
								}

								final String id = OCR4allUtils.getUUID();
								final Path destinationFile = folderFolios
										.resolve(Paths.get(id + "." + imageFormat.name()));

								try (InputStream inputStream = file.getInputStream()) {
									Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									logger.warn("Failed to store image '" + destinationFile + "' with uuid " + id
											+ " - " + e.getMessage());

									continue;
								}

								final Folio.Size size;
								try {
									size = ImageUtils.getSize(identifyJob, fileName, id + "." + imageFormat.name());
								} catch (IOException e) {
									Files.delete(destinationFile);

									continue;
								}

								folios.add(new Folio(new Date(), securityService.getUser(), id,
										FilenameUtils.removeExtension(fileName), format, size, null));

							} catch (Exception e) {
								logger.warn("Troubles to store image '" + fileName + "' - " + e.getMessage());
							}
					}

				if (folios.isEmpty())
					return folios;

				/*
				 * Create derivatives
				 */
				final ContainerConfiguration.Images.Derivatives derivatives = container.getConfiguration().getImages()
						.getDerivatives();
				try {
					final ImageConfiguration.Derivatives derivativeResolution = configurationService.getImage()
							.getDerivatives();

					// quality best
					ImageUtils.createDerivatives(new SystemProcess(folderFolios, convertCommand),
							derivatives.getFormat().name(), folderBest, derivativeResolution.getBest().getMaxSize(),
							derivativeResolution.getBest().getQuality());

					// quality detail
					ImageUtils.createDerivatives(new SystemProcess(folderBest, convertCommand),
							derivatives.getFormat().name(), folderDetail, derivativeResolution.getDetail().getMaxSize(),
							derivativeResolution.getDetail().getQuality());

					// quality thumbnail
					ImageUtils.createDerivatives(new SystemProcess(folderDetail, convertCommand),
							derivatives.getFormat().name(), folderThumbnail,
							derivativeResolution.getThumbnail().getMaxSize(),
							derivativeResolution.getThumbnail().getQuality());
				} catch (Exception e) {
					// deleteRecursively(temporaryDirectory);
					logger.warn("Path folios " + folderFolios.toString());
					logger.warn("Path best " + folderBest.toString());

					throw new IOException("Cannot create derivatives for container - " + e.getMessage() + ".");
				}

				// set sizes
				SystemProcess identifyThumbnailJob = new SystemProcess(folderThumbnail, identifyCommand);
				SystemProcess identifyDetailJob = new SystemProcess(folderDetail, identifyCommand);
				SystemProcess identifyBestJob = new SystemProcess(folderBest, identifyCommand);

				List<String> foliosFiles = new ArrayList<>();
				List<String> derivativeFiles = new ArrayList<>();

				final String foliosDerivativesImageFormat = derivatives.getFormat().name();
				for (Folio folio : folios) {
					try {
						foliosFiles.add(folio.getId() + "." + folio.getFormat().name());

						String target = folio.getId() + "." + foliosDerivativesImageFormat;
						derivativeFiles.add(target);

						folio.setDerivatives(
								new Folio.Derivatives(ImageUtils.getSize(identifyThumbnailJob, folio.getName(), target),
										ImageUtils.getSize(identifyDetailJob, folio.getName(), target),
										ImageUtils.getSize(identifyBestJob, folio.getName(), target)));
					} catch (IOException e) {
						deleteRecursively(temporaryDirectory);

						throw new IOException("Cannot restore derivatives information - " + e.getMessage() + ".");
					}
				}

				/*
				 * Move the folios to the project
				 */
				try {
					move(foliosFiles, folderFolios, container.getConfiguration().getImages().getFolios());

					move(derivativeFiles, folderThumbnail, derivatives.getThumbnail());

					move(derivativeFiles, folderDetail, derivatives.getDetail());

					move(derivativeFiles, folderBest, derivatives.getBest());
				} catch (IOException e) {
					int remain = remove(foliosFiles, container.getConfiguration().getImages().getFolios());
					remain += remove(derivativeFiles, derivatives.getThumbnail());
					remain += remove(derivativeFiles, derivatives.getDetail());
					remain += remove(derivativeFiles, derivatives.getBest());

					final String message = "Cannot move the folios to container"
							+ (remain == 0 ? "" : " (" + remain + " could not cleaned up)") + " - " + e.getMessage()
							+ ".";

					deleteRecursively(temporaryDirectory);

					throw new IOException(message);
				}

				// remove temporary data
				deleteRecursively(temporaryDirectory);

				/*
				 * Persist the configuration
				 */
				PersistenceManager folioManager = new PersistenceManager(
						container.getConfiguration().getConfiguration().getFolioFile(), Type.folio_v1);
				try {
					folioManager.persist(true, folios);
				} catch (Exception e) {
					throw new IOException(
							"Cannot persist container folios configuration file - " + e.getMessage() + ".");
				}

				return folios;
			}
		}

		return null;
	}

	/**
	 * Returns the folios.
	 *
	 * @param container The container.
	 * @return The folios. Null if the container is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> getFolios(Container container) throws IOException {
		return getFolios(container, null);
	}

	/**
	 * Returns the folios that are restricted to the specified IDs.
	 *
	 * @param container The container.
	 * @param uuids     The folios uuids. If null, returns all folios.
	 * @return The folios. Null if the container is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> getFolios(Container container, Set<String> uuids) throws IOException {
		if (container != null && container.getRight().isReadFulfilled()) {
			List<Folio> folios = new ArrayList<>();

			for (Folio folio : (new PersistenceManager(container.getConfiguration().getConfiguration().getFolioFile(),
					Type.folio_v1)).getEntities(Folio.class))
				if (uuids == null || uuids.contains(folio.getId()))
					folios.add(folio);

			return folios;
		} else
			return null;
	}

	/**
	 * Persist the folios.
	 * 
	 * @param container The container.
	 * @param folios    The folios to persist.
	 * @return The number of persisted folios.
	 * @throws IOException Throws if the folios metadata file can not be persisted.
	 * @since 1.8
	 */
	private int persist(Container container, List<Folio> folios) throws IOException {
		return (new PersistenceManager(container.getConfiguration().getConfiguration().getFolioFile(), Type.folio_v1))
				.persist(folios);
	}

	/**
	 * Sorts the folios.
	 * 
	 * @param container The container.
	 * @param order     The order to sort, that is list of folios ids.
	 * @param isAfter   True if the folios that do not belong to the order are to be
	 *                  inserted after the folios that belong to the order.
	 *                  Otherwise, they are placed at the beginning.
	 * @return The sorted folios. Null if the container is null or the write right
	 *         is not fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> sortFolios(Container container, List<String> order, boolean isAfter) throws IOException {
		if (container != null && container.getRight().isWriteFulfilled()) {
			List<Folio> folios = ImageUtils.sort(getFolios(container), order, isAfter);

			persist(container, folios);

			return folios;
		} else
			return null;

	}

	/**
	 * Update the folios metadata.
	 * 
	 * @param container The container.
	 * @param metadata  The metadata of the folios to update.
	 * @return The folios.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> updateFolios(Container container, Collection<ImageUtils.Metadata> metadata) throws IOException {
		if (container != null && container.getRight().isWriteFulfilled()) {
			List<Folio> folios = ImageUtils.update(getFolios(container), metadata);

			persist(container, folios);

			return folios;
		} else
			return null;
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
