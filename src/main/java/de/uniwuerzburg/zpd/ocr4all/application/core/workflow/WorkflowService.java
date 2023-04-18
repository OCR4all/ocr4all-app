/**
 * File:     WorkflowService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.workflow
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     17.04.2023
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.workflow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Entity;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Metadata;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.View;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Workflow;

/**
 * Defines workflow services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class WorkflowService extends CoreService {
	/**
	 * The security service.
	 */
	protected final SecurityService securityService;

	/**
	 * Creates a workflow service.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public WorkflowService(ConfigurationService configurationService, SecurityService securityService) {
		super(WorkflowService.class, configurationService);

		this.securityService = securityService;
	}

	/**
	 * Returns the path of the workflow configuration file with given UUID.
	 * 
	 * @param uuid The UUID.
	 * @return The path of the workflow configuration file.
	 * @throws SecurityException Throws when there is a security violation.
	 * @since 1.8
	 */
	private Path getPath(String uuid) throws SecurityException {
		Path path = Paths
				.get(configurationService.getWorkspace().getWorkflows().getFolder().toString(),
						uuid.trim() + configurationService.getWorkspace().getWorkflows().getFileExtension())
				.normalize();

		if (path.startsWith(configurationService.getWorkspace().getWorkflows().getFolder()))
			return path;
		else
			throw new SecurityException("The workflow UUID '" + uuid + "' is not valid.");
	}

	/**
	 * Return the persistence manager for workflow configuration file with given
	 * UUID.
	 * 
	 * @param uuid The UUID.
	 * @return The persistence manager.
	 * @throws SecurityException Throws when there is a security violation.
	 * @since 1.8
	 */
	private PersistenceManager getPersistenceManager(String uuid) throws SecurityException {
		return new PersistenceManager(getPath(uuid), Type.workflow_metadata_v1, Type.workflow_view_v1,
				Type.workflow_v1);
	}

	/**
	 * Creates the workflow.
	 * 
	 * @param label       The label. It can not be null or blank.
	 * @param description The description.
	 * @param viewModel   The view model.
	 * @param workflow    The workflow.
	 * @return The workflow metadata. Null if the workflow can not be created.
	 * @since 1.8
	 */
	public Metadata create(String label, String description, String viewModel, Workflow workflow) {
		return persist(null, label, description, viewModel, workflow);
	}

	/**
	 * Persists the workflow.
	 * 
	 * @param uuid        The UUID of the workflow. If null or blank, then a new
	 *                    workflow is created with a new generated UUID. Otherwise,
	 *                    the workflow with this UUID is updated.
	 * @param label       The label. It can not be null or blank.
	 * @param description The description.
	 * @param viewModel   The view model.
	 * @param workflow    The workflow.
	 * @return The workflow metadata. Null if the workflow can not be persisted.
	 * @since 1.8
	 */
	public Metadata persist(String uuid, String label, String description, String viewModel, Workflow workflow) {
		if (!securityService.isCoordinator() || label == null || label.isBlank())
			return null;
		else {
			Metadata metadata = null;
			if (uuid == null || uuid.isBlank()) {
				uuid = UUID.randomUUID().toString();

				metadata = new Metadata(securityService.getUser(), uuid, label.trim(), description);
			} else {
				try {
					Path path = getPath(uuid);

					if (Files.exists(path)) {
						List<Entity> entities = getPersistenceManager(uuid).getEntities(null,
								message -> logger.warn(message), 1, null, Type.workflow_metadata_v1);
						if (!entities.isEmpty() && entities.get(0) instanceof Metadata)
							metadata = (Metadata) entities.get(0);
					}
				} catch (Exception e) {
					logger.warn(e.getMessage());
				}

				if (metadata == null)
					return null;

				metadata.setUpdated(new Date());
				metadata.setUpdateUser(securityService.getUser());
			}

			List<Entity> entities = new ArrayList<>();
			entities.add(metadata);

			if (viewModel != null && !viewModel.isBlank())
				entities.add(new View(viewModel.trim()));

			if (workflow != null)
				entities.add(workflow);

			try {
				getPersistenceManager(uuid).persist(entities);
			} catch (Exception e) {
				logger.error(e.getMessage());

				return null;
			}

			return metadata;
		}
	}

	/**
	 * Removes the workflow configuration file with given UUID.
	 * 
	 * @param uuid The UUID.
	 * @return True iff the workflow configuration file was removed.
	 * @since 1.8
	 */
	public boolean remove(String uuid) {
		if (securityService.isCoordinator() && uuid != null && !uuid.isBlank())
			try {
				return Files.deleteIfExists(getPath(uuid));
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

		return false;
	}

	/**
	 * Returns the metadata of the available workflows.
	 * 
	 * @return The metadata of the available workflows. Null on troubles.
	 * @since 1.8
	 */
	public List<Metadata> getMetadata() {
		try (Stream<Path> stream = Files.list(configurationService.getWorkspace().getWorkflows().getFolder())) {
			List<Metadata> list = new ArrayList<>();

			for (Path path : stream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toList())) {
				PersistenceManager persistenceManager = new PersistenceManager(path, Type.workflow_metadata_v1,
						Type.workflow_view_v1, Type.workflow_v1);

				List<Entity> entities = persistenceManager.getEntities(null, message -> logger.warn(message), 1, null,
						Type.workflow_metadata_v1);
				if (!entities.isEmpty() && entities.get(0) instanceof Metadata)
					list.add((Metadata) entities.get(0));
			}

			return list;
		} catch (Exception e) {
			logger.warn("can not recover workflows - " + e.getMessage());

			return null;
		}
	}

	/**
	 * Returns the core data of the workflow with given UUID.
	 * 
	 * @param uuid The UUID.
	 * @return The core data. Null if the core data is not available.
	 * @since 1.8
	 */
	public WorkflowCoreData getCoreData(String uuid) {
		if (uuid != null && !uuid.isBlank())
			try {
				Metadata metadata = null;
				View view = null;

				for (Entity entity : getPersistenceManager(uuid).getEntities(null, message -> logger.warn(message), 0,
						null, Type.workflow_metadata_v1, Type.workflow_view_v1)) {
					if (entity instanceof Metadata && metadata == null)
						metadata = (Metadata) entity;
					else if (entity instanceof View && view == null)
						view = (View) entity;

					if (metadata != null && view != null)
						break;
				}

				return new WorkflowCoreData(metadata, view);
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

		return null;
	}

	/**
	 * Returns the workflow with given UUID.
	 * 
	 * @param uuid The UUID.
	 * @return The workflow. Null if the workflow is not available.
	 * @since 1.8
	 */
	public Workflow getWorkflow(String uuid) {
		if (uuid != null && !uuid.isBlank())
			try {
				List<Entity> entities = getPersistenceManager(uuid).getEntities(null, message -> logger.warn(message),
						1, null, Type.workflow_v1);
				return !entities.isEmpty() && entities.get(0) instanceof Workflow ? (Workflow) entities.get(0) : null;
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

		return null;
	}

}
