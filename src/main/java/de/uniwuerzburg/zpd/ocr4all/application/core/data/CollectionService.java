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
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.CollectionConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.data.CollectionConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrant;

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

				// store the files
				List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = new ArrayList<>();
				Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> names = new Hashtable<>();
				Set<String> setFiles = new HashSet<>();

				for (MultipartFile file : files)
					if (file != null && !file.isEmpty()) {
						NameExtension nameExtension = getNameExtension(file.getOriginalFilename());

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
	 * Returns the set with given IDs.
	 *
	 * @param collection The collection.
	 * @param uuid       The set uuid.
	 * @return The set. Null if the collection is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the sets metadata file can not be read.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set getSet(Collection collection, String uuid)
			throws IOException {
		if (uuid == null)
			return null;
		else {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = getSets(collection, Set.of(uuid));

			return sets == null || sets.isEmpty() ? null : sets.get(0);
		}

	}

	/**
	 * Returns the sets.
	 *
	 * @param collection The collection.
	 * @return The sets. Null if the collection is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the sets metadata file can not be read.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> getSets(Collection collection)
			throws IOException {
		return getSets(collection, null);
	}

	/**
	 * Returns the sets that are restricted to the specified IDs.
	 *
	 * @param collection The collection.
	 * @param uuids      The sets uuids. If null, returns all sets.
	 * @return The sets. Null if the collection is null or the read right is not
	 *         fulfilled.
	 * @throws IOException Throws if the sets metadata file can not be read.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> getSets(Collection collection,
			Set<String> uuids) throws IOException {
		if (collection != null && collection.getRight().isReadFulfilled()) {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = new ArrayList<>();

			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : (new PersistenceManager(
					collection.getConfiguration().getConfiguration().getSetsFile(), Type.data_collection_set_v1))
					.getEntities(de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set.class))
				if (uuids == null || uuids.contains(set.getId()))
					sets.add(set);

			return sets;
		} else
			return null;
	}

	/**
	 * Returns the file extensions of a set.
	 *
	 * @param collection The collection.
	 * @param uuid       The set uuid.
	 * @return The file extensions of the set. Null if the collection is null or the
	 *         read right is not fulfilled.
	 * @throws IOException Throws if an I/O error occurs when opening the collection
	 *                     folder.
	 * @since 1.8
	 */
	public List<String> getFileExtensions(Collection collection, String uuid) throws IOException {
		if (collection != null && collection.getRight().isReadFulfilled() && uuid != null || !uuid.isBlank()) {
			List<String> extensions = new ArrayList<>();

			for (String filename : OCR4allUtils.getFileNames(collection.getConfiguration().getFolder(),
					uuid.trim() + ".", null))
				extensions.add(getNameExtension(filename).getExtension());

			Collections.sort(extensions, String.CASE_INSENSITIVE_ORDER);

			return extensions;
		} else
			return null;
	}

	/**
	 * Persist the sets.
	 * 
	 * @param collection The collection.
	 * @param sets       The sets to persist.
	 * @return The number of persisted sets.
	 * @throws IOException Throws if the sets metadata file can not be persisted.
	 * @since 1.8
	 */
	private int persist(Collection collection, List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets)
			throws IOException {
		return (new PersistenceManager(collection.getConfiguration().getConfiguration().getSetsFile(),
				Type.data_collection_set_v1)).persist(sets);
	}

	/**
	 * Sorts the sets in the given order.
	 * 
	 * @param sets    The sets to sort.
	 * @param order   The order to sort, that is list of sets ids.
	 * @param isAfter True if the sets that do not belong to the order are to be
	 *                inserted after the sets that belong to the order. Otherwise,
	 *                they are placed at the beginning.
	 * @return The sorted sets.
	 * @since 1.8
	 */
	private static List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sort(
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets, List<String> order,
			boolean isAfter) {
		if (sets == null || sets.isEmpty() || order == null || order.isEmpty())
			return sets;

		List<String> parsedOrder = new ArrayList<>();
		Set<String> parsedIds = new HashSet<>();
		for (String id : order)
			if (id != null && !id.isBlank()) {
				id = id.trim();

				if (parsedIds.add(id))
					parsedOrder.add(id);
			}

		if (parsedOrder.isEmpty())
			return sets;

		Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> idSets = new Hashtable<>();
		for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : sets)
			idSets.put(set.getId(), set);

		// Set the new order
		List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> newOrder = new ArrayList<>();
		for (String id : parsedOrder)
			if (idSets.containsKey(id))
				newOrder.add(idSets.get(id));

		// The remainder sets
		Set<String> orderSet = new HashSet<>(parsedOrder);
		List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> remainderOrder = new ArrayList<>();
		for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : sets)
			if (!orderSet.contains(set.getId()))
				remainderOrder.add(set);

		if (!remainderOrder.isEmpty()) {
			if (isAfter)
				newOrder.addAll(remainderOrder);
			else
				newOrder.addAll(0, remainderOrder);
		}

		return newOrder;
	}

	/**
	 * Sorts the sets.
	 * 
	 * @param collection The collection.
	 * @param order      The order to sort, that is list of sets ids.
	 * @param isAfter    True if the sets that do not belong to the order are to be
	 *                   inserted after the sets that belong to the order.
	 *                   Otherwise, they are placed at the beginning.
	 * @return The sorted sets. Null if the collection is null or the write right is
	 *         not fulfilled.
	 * @throws IOException Throws if the sets metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sortSets(Collection collection,
			List<String> order, boolean isAfter) throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {

			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> folios = sort(getSets(collection), order,
					isAfter);

			persist(collection, folios);

			return folios;
		} else
			return null;

	}

	/**
	 * Update the sets metadata.
	 * 
	 * @param sets     The sets.
	 * @param metadata The metadata of the sets to update.
	 * @return The sets.
	 * @since 1.8
	 */
	private static List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> update(
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets,
			java.util.Collection<Metadata> metadata) {
		if (sets == null || sets.isEmpty() || metadata == null || metadata.isEmpty())
			return sets;

		// index the sets
		Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> indexed = new Hashtable<>();
		for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set : sets)
			indexed.put(set.getId(), set);

		// update metadata
		for (Metadata update : metadata)
			if (update != null && update.getId() != null && !update.getId().isBlank()) {
				de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set set = indexed.get(update.getId().trim());

				if (set != null) {
					if (update.getName() != null && !update.getName().isBlank())
						set.setName(update.getName().trim());

					if (update.getKeywords() == null)
						set.setKeywords(null);
					else {
						Set<String> keywords = new HashSet<>();
						for (String keyword : update.getKeywords())
							if (keyword != null && !keyword.isBlank())
								keywords.add(keyword.trim());

						set.setKeywords(keywords.isEmpty() ? null : keywords);
					}
				}
			}

		return sets;
	}

	/**
	 * Update the sets metadata.
	 * 
	 * @param collection The collection.
	 * @param metadata   The metadata of the sets to update.
	 * @return The sets.
	 * @throws IOException Throws if the sets metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> updateSets(Collection collection,
			java.util.Collection<Metadata> metadata) throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = update(getSets(collection),
					metadata);

			persist(collection, sets);

			return sets;
		} else
			return null;
	}

	/**
	 * Removed the sets.
	 * 
	 * @param collection The collection.
	 * @param ids        The ids of the sets to remove. If null, remove all sets.
	 * @return The sets. Null if the collection is null or the write right is not
	 *         fulfilled.
	 * @throws IOException Throws if the sets metadata file can not be read or
	 *                     persisted.
	 * @since 1.8
	 */
	public List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> removeSets(Collection collection,
			java.util.Collection<String> ids) throws IOException {
		if (collection != null && collection.getRight().isWriteFulfilled()) {
			final List<de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set> sets = new ArrayList<>();

			final Path folder = collection.getConfiguration().getFolder();

			if (ids == null) {
				// Clear all sets
				OCR4allUtils.delete(OCR4allUtils.getFiles(folder, null, null));
			} else {
				// Clear desired sets and the respective derivatives
				final Set<String> removeIds = new HashSet<>();

				for (String id : ids)
					if (id != null && !id.isBlank())
						removeIds.add(id.trim());

				for (de.uniwuerzburg.zpd.ocr4all.application.persistence.data.Set folio : getSets(collection))
					if (removeIds.contains(folio.getId()))
						try {
							OCR4allUtils.delete(OCR4allUtils.getFiles(folder, folio.getId() + ".", null));
						} catch (Exception e) {
							// Ignore troubles removing files
						}
					else
						sets.add(folio);
			}

			persist(collection, sets);

			return sets;
		} else
			return null;
	}

	/**
	 * Returns the set name with its extension of the given file name.
	 * 
	 * @param fileName The file name.
	 * @return The set name of given file name.
	 * @since 17
	 */
	private static NameExtension getNameExtension(String fileName) {
		if (fileName == null)
			return null;
		else {
			fileName = fileName.trim();

			int index = fileName.indexOf(".");

			return new NameExtension((index < 0 ? fileName : fileName.substring(0, index)),
					(index < 0 || index == fileName.length() - 1 ? "" : fileName.substring(index + 1)));
		}
	}

	/**
	 * NameExtension is an immutable class the defines names with extensions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	private static class NameExtension {
		/**
		 * The name.
		 */
		private final String name;

		/**
		 * The extension.
		 */
		private final String extension;

		/**
		 * Creates a name with extension.
		 * 
		 * @param name      The name.
		 * @param extension The extension.
		 * @since 17
		 */
		public NameExtension(String name, String extension) {
			super();

			this.name = name;
			this.extension = extension;
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 17
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the extension.
		 *
		 * @return The extension.
		 * @since 17
		 */
		public String getExtension() {
			return extension;
		}

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

	/**
	 * Metadata is an immutable class that defines metadata for update.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Metadata implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The id.
		 */
		private final String id;

		/**
		 * The name without extension.
		 */
		private final String name;

		/**
		 * The keywords.
		 */
		private final Set<String> keywords;

		/**
		 * Creates a metadata.
		 * 
		 * @param id       The id.
		 * @param name     The name without extension.
		 * @param keywords The keywords.
		 * @since 1.8
		 */
		public Metadata(String id, String name, Set<String> keywords) {
			super();

			this.id = id;
			this.name = name;
			this.keywords = keywords;
		}

		/**
		 * Returns the id.
		 *
		 * @return The id.
		 * @since 1.8
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the name.
		 *
		 * @return The name.
		 * @since 1.8
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the keywords.
		 *
		 * @return The keywords.
		 * @since 1.8
		 */
		public Set<String> getKeywords() {
			return keywords;
		}

	}
}
