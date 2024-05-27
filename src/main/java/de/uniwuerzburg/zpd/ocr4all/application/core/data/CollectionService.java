/**
 * File:     CollectionService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.data
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     27.05.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.data;

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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ImageConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.CollectionConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.CollectionConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.CollectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.data.DataService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageUtils;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines collection services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class CollectionService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The data service.
	 */
	private final DataService dataService;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a collection service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param dataService          The data service.
	 * @since 1.8
	 */
	public CollectionService(ConfigurationService configurationService, SecurityService securityService,
			DataService dataService) {
		super(CollectionService.class, configurationService);

		this.securityService = securityService;
		this.dataService = dataService;

		folder = configurationService.getData().getFolder().normalize();
	}

	/**
	 * Returns the collection.
	 * 
	 * @param configuration The collection configuration.
	 * @return The collection.
	 * @since 1.8
	 */
	private Collection getCollection(CollectionConfiguration configuration) {
		return new Collection(dataService.isAdministrator() ? SecurityGrant.Right.maximal
				: configuration.getConfiguration().getRight(securityService.getUser(),
						securityService.getActiveGroups()),
				configuration);
	}

	/**
	 * Returns the collection folder.
	 * 
	 * @param uuid The collection uuid.
	 * @return The collection folder. If the uuid is invalid, null is returned.
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
	 * Returns the collection.
	 * 
	 * @param path The collection path.
	 * @return The collection.
	 * @since 1.8
	 */
	private Collection getCollection(Path path) {
		return getCollection(new CollectionConfiguration(configurationService.getData().getCollection(), path));
	}

	/**
	 * Returns true if a collection can be created.
	 * 
	 * @return True if a collection can be created.
	 * @since 1.8
	 */
	public boolean isCreate() {
		return dataService.isCreateCollection();
	}

	/**
	 * Creates a collection.
	 * 
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The collection configuration. Null if the collection can not be
	 *         created.
	 * @since 1.8
	 */
	public Collection create(String name, String description, Set<String> keywords) {
		if (isCreate()) {
			final String user = securityService.getUser();
			final Path folder = Paths.get(CollectionService.this.folder.toString(), OCR4allUtils.getUUID()).normalize();

			try {
				Files.createDirectory(folder);

				logger.info("Created collection folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot create collection '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + " - " + e.getMessage() + ".");

				return null;
			}

			return getCollection(new CollectionConfiguration(configurationService.getData().getCollection(), folder,
					new Configuration.CoreData(user, name, description, keywords)));
		} else
			return null;
	}

	/**
	 * Returns the collection.
	 * 
	 * @param uuid The collection uuid.
	 * @return The collection. Null if unknown.
	 * @since 1.8
	 */
	public Collection getCollection(String uuid) {
		Path path = getPath(uuid);

		return path == null ? null : getCollection(path);
	}

	/**
	 * Returns the collections sorted by name.
	 * 
	 * @return The collections.
	 * @since 1.8
	 */
	public List<Collection> getCollections() {
		List<Collection> collections = new ArrayList<>();

		try {
			Files.list(CollectionService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith("."))
					collections.add(getCollection(path));
			});
		} catch (IOException e) {
			logger.warn("Cannot not load collections - " + e.getMessage());
		}

		Collections.sort(collections, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return collections;
	}

	/**
	 * Removes the collection if the special right is fulfilled.
	 * 
	 * @param uuid The collection uuid.
	 * @return True if the collection could be removed.
	 * @since 1.8
	 */
	public boolean remove(String uuid) {
		Path path = getPath(uuid);

		if (path != null && getCollection(path).getRight().isSpecialFulfilled()) {
			try {
				Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

				if (!Files.exists(path)) {
					logger.info("Removed collection '" + path.toString() + "'.");

					return true;
				} else
					logger.warn("Troubles removing the collection '" + path.toString() + "'.");
			} catch (Exception e) {
				logger.warn("Cannot remove collection '" + path.toString() + "' - " + e.getMessage() + ".");
			}
		} else {
			logger.warn("Cannot remove collection '" + uuid + "'.");
		}

		return false;
	}

	/**
	 * Updates a collection.
	 * 
	 * @param uuid        The collection uuid.
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The collection. Null if the collection can not be updated.
	 * @since 1.8
	 */
	public Collection update(String uuid, String name, String description, Set<String> keywords) {
		Path path = getPath(uuid);

		if (path != null) {
			Collection collection = getCollection(path);

			if (collection.getRight().isSpecialFulfilled()
					&& collection.getConfiguration().getConfiguration().update(securityService.getUser(),
							new CollectionConfiguration.Configuration.Information(name, description, keywords)))
				return collection;
		}

		return null;
	}

	/**
	 * Updates the security.
	 *
	 * @param uuid     The collection uuid.
	 * @param security The collection security.
	 * @return The updated collection. Null if it can not be updated.
	 * @since 1.8
	 */
	public Collection update(String uuid, SecurityGrant security) {
		Path path = getPath(uuid);

		if (path != null) {
			Collection collection = getCollection(path);

			if (collection.getRight().isSpecialFulfilled()
					&& collection.getConfiguration().getConfiguration().update(securityService.getUser(), security))
				return collection;
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
	private void move(java.util.Collection<String> fileNames, Path source, Path target) throws IOException {
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
	 * Store the sets.
	 * 
	 * @param collection The collection.
	 * @param files      The sets. The prefix of the file name up to the first dot
	 *                   defines a set.
	 * @return The stored sets. Null if collection is unknown or the write right is
	 *         not fulfilled.
	 * @throws IOException Throws on storage troubles.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> store(Collection collection,
			MultipartFile[] files) throws IOException {
		if (collection != null && files != null) {
			if (collection.getRight().isWriteFulfilled()) {
				// create tmp folder
				Path temporaryFolder = configurationService.getTemporary().getTemporaryDirectory();

				try {
					Files.createDirectory(temporaryFolder);
				} catch (IOException e) {
					throw e;
				}

				// store the files
				List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = new ArrayList<>();
				Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> names = new Hashtable<>();
				Set<String> setFiles = new HashSet<>();

				for (MultipartFile file : files)
					if (file != null && !file.isEmpty()) {
						de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set.NameExtension nameExtension = de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set
								.getNameExtension(file.getOriginalFilename());

						de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set = names
								.get(nameExtension.getName());
						if (set == null) {
							set = new de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set(new Date(),
									securityService.getUser(), OCR4allUtils.getUUID(), nameExtension.getName());

							sets.add(set);
							names.put(nameExtension.getName(), set);
						}

						final String name = set.getId() + "." + nameExtension.getExtension();
						setFiles.add(name);

						final Path destinationFile = temporaryFolder.resolve(Paths.get(name));

						try (InputStream inputStream = file.getInputStream()) {
							Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							logger.warn("Failed to store file '" + file.getOriginalFilename() + "' with uuid "
									+ set.getId() + " - " + e.getMessage());

							continue;
						}
					}

				if (sets.isEmpty()) {
					deleteRecursively(temporaryFolder);

					return sets;
				}

				/*
				 * Move the files to the collection
				 */
				try {
					move(setFiles, temporaryFolder, collection.getConfiguration().getFolder());
				} catch (IOException e) {
					final String message = "Cannot move the files to collection - " + e.getMessage() + ".";

					deleteRecursively(temporaryFolder);

					throw new IOException(message);
				}

				// remove temporary data
				deleteRecursively(temporaryFolder);

				// Persist the configuration
				try {
					(new PersistenceManager(collection.getConfiguration().getConfiguration().getSetsFile(),
							Type.data_collection_set_v1)).persist(true, sets);
				} catch (Exception e) {
					throw new IOException(
							"Cannot persist collection sets configuration file - " + e.getMessage() + ".");
				}

				return sets;
			}
		}

		return null;
	}

	/**
	 * Returns the folios.
	 *
	 * @param collection The collection.
	 * @return The folios. Null if the collection is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> getFolios(Collection collection) throws IOException {
		return getFolios(collection, null);
	}

	/**
	 * Returns the folios that are restricted to the specified IDs.
	 *
	 * @param collection The collection.
	 * @param uuids      The folios uuids. If null, returns all folios.
	 * @return The folios. Null if the collection is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read.
	 * @since 1.8
	 */
	public List<Folio> getFolios(Collection collection, Set<String> uuids) throws IOException {
		if (collection != null && collection.getRight().isReadFulfilled()) {
			List<Folio> folios = new ArrayList<>();

			for (Folio folio : (new PersistenceManager(collection.getConfiguration().getConfiguration().getFolioFile(),
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
	 * @param collection The collection.
	 * @param folios     The folios to persist.
	 * @return The number of persisted folios.
	 * @throws IOException Throws if the folios metadata file can not be persisted.
	 * @since 1.8
	 */
	private int persist(Collection collection, List<Folio> folios) throws IOException {
		return (new PersistenceManager(collection.getConfiguration().getConfiguration().getFolioFile(), Type.folio_v1))
				.persist(folios);
	}

	/**
	 * Sorts the folios.
	 * 
	 * @param collection The collection.
	 * @param order      The order to sort, that is list of folios ids.
	 * @param isAfter    True if the folios that do not belong to the order are to
	 *                   be inserted after the folios that belong to the order.
	 *                   Otherwise, they are placed at the beginning.
	 * @return The sorted folios. Null if the collection is null or the write right
	 *         is not fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<Folio> sortFolios(Collection collection, List<String> order, boolean isAfter) throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {
			List<Folio> folios = ImageUtils.sort(getFolios(collection), order, isAfter);

			persist(collection, folios);

			return folios;
		} else
			return null;

	}

	/**
	 * Update the folios metadata.
	 * 
	 * @param collection The collection.
	 * @param metadata   The metadata of the folios to update.
	 * @return The folios.
	 * @throws IOException Throws if the folios metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<Folio> updateFolios(Collection collection, Collection<ImageUtils.Metadata> metadata)
			throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {
			List<Folio> folios = ImageUtils.update(getFolios(collection), metadata);

			persist(collection, folios);

			return folios;
		} else
			return null;
	}

	/**
	 * Removed the folios.
	 * 
	 * @param collection The collection.
	 * @param ids        The ids of the folios to remove. If null, remove all
	 *                   folios.
	 * @return The folios. Null if the collection is null or the write right is not
	 *         fulfilled.
	 * @throws IOException Throws if the folios metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<Folio> removeFolios(Collection collection, Collection<String> ids) throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {
			final List<Folio> folios = new ArrayList<>();

			final Path foliosFolder = collection.getConfiguration().getImages().getFolios();

			final CollectionConfiguration.Images.Derivatives derivatives = collection.getConfiguration().getImages()
					.getDerivatives();
			final Path thumbnailFolder = derivatives.getThumbnail();
			final Path detailFolder = derivatives.getDetail();
			final Path bestFolder = derivatives.getBest();

			if (ids == null) {
				// Clear all folios and derivatives
				FileUtils.cleanDirectory(foliosFolder.toFile());

				FileUtils.cleanDirectory(thumbnailFolder.toFile());
				FileUtils.cleanDirectory(detailFolder.toFile());
				FileUtils.cleanDirectory(bestFolder.toFile());
			} else {
				// Clear desired folios and the respective derivatives
				final Set<String> removeIds = new HashSet<>();

				for (String id : ids)
					if (id != null && !id.isBlank())
						removeIds.add(id.trim());

				final String derivativesFormat = derivatives.getFormat().name();

				for (Folio folio : getFolios(collection))
					if (removeIds.contains(folio.getId()))
						try {
							Files.delete(
									Paths.get(foliosFolder.toString(), folio.getId() + "." + folio.getFormat().name()));

							Files.delete(
									Paths.get(thumbnailFolder.toString(), folio.getId() + "." + derivativesFormat));
							Files.delete(Paths.get(detailFolder.toString(), folio.getId() + "." + derivativesFormat));
							Files.delete(Paths.get(bestFolder.toString(), folio.getId() + "." + derivativesFormat));
						} catch (Exception e) {
							// Ignore troubles removing files
						}
					else
						folios.add(folio);
			}

			persist(collection, folios);

			return folios;
		} else
			return null;

	}

	/**
	 * Collection is an immutable class that defines collections .
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Collection {
		/**
		 * The right.
		 */
		private final SecurityGrant.Right right;

		/**
		 * The configuration.
		 */
		private final CollectionConfiguration configuration;

		/**
		 * Creates a collection.
		 * 
		 * @param right         The right.
		 * @param configuration The configuration.
		 * @since 1.8
		 */
		public Collection(SecurityGrant.Right right, CollectionConfiguration configuration) {
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
		public SecurityGrant.Right getRight() {
			return right;
		}

		/**
		 * Returns the configuration.
		 *
		 * @return The configuration.
		 * @since 1.8
		 */
		public CollectionConfiguration getConfiguration() {
			return configuration;
		}

	}

}
