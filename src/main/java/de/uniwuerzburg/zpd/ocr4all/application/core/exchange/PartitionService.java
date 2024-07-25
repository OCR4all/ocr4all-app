/**
 * File:     PartitionService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.exchange
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     24.11.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.exchange;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange.PartitionConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.exchange.PartitionConfiguration.Configuration;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.security.SecurityGrantRW;

/**
 * Defines partition services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
public class PartitionService extends CoreService {
	/**
	 * The security service.
	 */
	private final SecurityService securityService;

	/**
	 * The exchange service.
	 */
	private final ExchangeService exchangeService;

	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a partition service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @param exchangeService      The exchange service.
	 * @since 17
	 */
	public PartitionService(ConfigurationService configurationService, SecurityService securityService,
			ExchangeService exchangeService) {
		super(PartitionService.class, configurationService);

		this.securityService = securityService;
		this.exchangeService = exchangeService;

		folder = configurationService.getExchange().getFolder().normalize();
	}

	/**
	 * Returns the partition.
	 * 
	 * @param configuration The partition configuration.
	 * @return The partition.
	 * @since 17
	 */
	private Partition getPartition(PartitionConfiguration configuration) {
		return new Partition(exchangeService.isAdministrator() ? SecurityGrantRW.Right.maximal
				: configuration.getConfiguration().getRight(securityService.getUser(),
						securityService.getActiveGroups()),
				configuration);
	}

	/**
	 * Returns the partition folder.
	 * 
	 * @param uuid The partition uuid.
	 * @return The partition folder. If the uuid is invalid, null is returned.
	 * @since 17
	 */
	private Path getPath(String uuid) {
		if (uuid == null || uuid.isBlank())
			return null;
		else {
			Path folder = this.folder.resolve(uuid.trim()).normalize();

			// Ignore directories beginning with a dot
			return Files.isDirectory(folder) && folder.getParent().equals(this.folder)
					&& !folder.getFileName().toString().startsWith(".") ? folder : null;
		}
	}

	/**
	 * Returns the partition.
	 * 
	 * @param path The partition path.
	 * @return The partition.
	 * @since 17
	 */
	private Partition getPartition(Path path) {
		return getPartition(new PartitionConfiguration(configurationService.getExchange().getPartition(), path));
	}

	/**
	 * Returns true if the administrator security permission is achievable by the
	 * session user.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 17
	 */
	public boolean isAdministrator() {
		return exchangeService.isAdministrator();
	}

	/**
	 * Returns true if a partition can be created.
	 * 
	 * @return True if a partition can be created.
	 * @since 17
	 */
	public boolean isCreate() {
		return exchangeService.isCreatePartition();
	}

	/**
	 * Creates a partition.
	 * 
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The partition configuration. Null if the partition can not be
	 *         created.
	 * @since 17
	 */
	public Partition create(String name, String description, Set<String> keywords) {
		if (isCreate()) {
			final String user = securityService.getUser();
			final Path folder = PartitionService.this.folder.resolve(OCR4allUtils.getUUID()).normalize();

			try {
				Files.createDirectory(folder);

				logger.info("Created partition folder '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + ".");
			} catch (Exception e) {
				logger.warn("Cannot create partition '" + folder.toString() + "'"
						+ (user == null ? "" : ", user=" + user) + " - " + e.getMessage() + ".");

				return null;
			}

			return getPartition(new PartitionConfiguration(configurationService.getExchange().getPartition(), folder,
					new Configuration.CoreData(user, name, description, keywords)));
		} else
			return null;
	}

	/**
	 * Returns the partition.
	 * 
	 * @param uuid The partition uuid.
	 * @return The partition. Null if unknown.
	 * @since 17
	 */
	public Partition getPartition(String uuid) {
		Path path = getPath(uuid);

		return path == null ? null : getPartition(path);
	}

	/**
	 * Returns the partitions sorted by name.
	 * 
	 * @return The partitions.
	 * @since 17
	 */
	public List<Partition> getPartitions() {
		List<Partition> partitions = new ArrayList<>();

		try {
			Files.list(PartitionService.this.folder).filter(Files::isDirectory).forEach(path -> {
				// Ignore directories beginning with a dot
				if (!path.getFileName().toString().startsWith("."))
					partitions.add(getPartition(path));
			});
		} catch (IOException e) {
			logger.warn("Cannot not load partitions - " + e.getMessage());
		}

		Collections.sort(partitions, (p1, p2) -> p1.getConfiguration().getConfiguration().getName()
				.compareToIgnoreCase(p2.getConfiguration().getConfiguration().getName()));

		return partitions;
	}

	/**
	 * Removes the partition if the special right is fulfilled.
	 * 
	 * @param uuid The partition uuid.
	 * @return True if the partition could be removed.
	 * @since 17
	 */
	public boolean remove(String uuid) {
		if (isAdministrator()) {
			Path path = getPath(uuid);

			if (path != null) {
				try {
					Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

					if (!Files.exists(path)) {
						logger.info("Removed partition '" + path.toString() + "'.");

						return true;
					} else
						logger.warn("Troubles removing the partition '" + path.toString() + "'.");
				} catch (Exception e) {
					logger.warn("Cannot remove partition '" + path.toString() + "' - " + e.getMessage() + ".");
				}
			} else {
				logger.warn("Cannot remove partition '" + uuid + "'.");
			}
		}

		return false;
	}

	/**
	 * Updates a partition.
	 * 
	 * @param uuid        The partition uuid.
	 * @param name        The name.
	 * @param description The description.
	 * @param keywords    The keywords.
	 * @return The partition. Null if the partition can not be updated.
	 * @since 17
	 */
	public Partition update(String uuid, String name, String description, Set<String> keywords) {
		if (isAdministrator()) {
			Path path = getPath(uuid);

			if (path != null) {
				Partition partition = getPartition(path);

				if (partition.getConfiguration().getConfiguration().update(securityService.getUser(),
						new PartitionConfiguration.Configuration.Information(name, description, keywords)))
					return partition;
			}
		}

		return null;
	}

	/**
	 * Updates the security.
	 *
	 * @param uuid     The partition uuid.
	 * @param security The partition security.
	 * @return The updated partition. Null if it can not be updated.
	 * @since 17
	 */
	public Partition update(String uuid, SecurityGrantRW security) {
		if (isAdministrator()) {
			Path path = getPath(uuid);

			if (path != null) {
				Partition partition = getPartition(path);

				if (partition.getConfiguration().getConfiguration().update(securityService.getUser(), security))
					return partition;
			}
		}

		return null;
	}

	/**
	 * Returns the partition files.
	 * 
	 * @param uuid   The partition uuid.
	 * @param folder The sub folder of the partition data folder. If null or blank
	 *               use the partition data folder.
	 * @return The partition files. The files whose name start with a dot are
	 *         ignores. Null if unknown or the user is not allowed to read this
	 *         information.
	 * @since 17
	 */
	public List<PartitionFile> getFiles(String uuid, String folder) {
		Path path = getPath(uuid);

		if (path != null) {
			Partition partition = getPartition(path);

			if (partition.getRight().isReadFulfilled()) {
				folder = folder == null ? "" : folder.trim();

				Path directory = partition.getConfiguration().getData().resolve(folder).normalize();

				if (directory.startsWith(partition.getConfiguration().getFolder()) && Files.isDirectory(directory))
					try (Stream<Path> stream = Files.list(directory)) {
						return stream.filter(file -> !file.getFileName().toString().startsWith("."))
								.map(file -> new PartitionFile(file)).collect(Collectors.toList());
					} catch (Exception e) {
						// Nothing to do
					}
			}
		}

		return null;
	}

	/**
	 * Partition is an immutable class that defines partitions.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class Partition {
		/**
		 * The right.
		 */
		private final SecurityGrantRW.Right right;

		/**
		 * The configuration.
		 */
		private final PartitionConfiguration configuration;

		/**
		 * Creates a partition.
		 * 
		 * @param right         The right.
		 * @param configuration The configuration.
		 * @since 17
		 */
		public Partition(SecurityGrantRW.Right right, PartitionConfiguration configuration) {
			super();

			this.right = right;
			this.configuration = configuration;
		}

		/**
		 * Returns the right.
		 *
		 * @return The right.
		 * @since 17
		 */
		public SecurityGrantRW.Right getRight() {
			return right;
		}

		/**
		 * Returns the configuration.
		 *
		 * @return The configuration.
		 * @since 17
		 */
		public PartitionConfiguration getConfiguration() {
			return configuration;
		}
	}

	/**
	 * PartitionFile is an immutable class that defines partition files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class PartitionFile {
		/**
		 * The default content type for a directory.
		 */
		private static final String defaultContentTypeDirectory = "inode/directory";

		/**
		 * The name.
		 */
		private final String name;

		/**
		 * The content type.
		 */
		private final String contentType;

		/**
		 * Creates a file response for the api.
		 * 
		 * @param file The file.
		 * @since 17
		 */
		public PartitionFile(Path file) {
			super();
			name = file.getFileName().toString();

			if (Files.isDirectory(file))
				contentType = defaultContentTypeDirectory;
			else {
				String contentType;
				try {
					contentType = Files.probeContentType(file);
				} catch (IOException e) {
					contentType = null;
				}

				this.contentType = contentType;
			}
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
		 * Returns the content type.
		 *
		 * @return The content type.
		 * @since 17
		 */
		public String getContentType() {
			return contentType;
		}

	}
}
