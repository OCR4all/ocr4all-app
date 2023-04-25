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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.ocr.OpticalCharacterRecognitionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.olr.OpticalLayoutRecognitionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.PostcorrectionService;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.preprocessing.PreprocessingService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Entity;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Metadata;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Processor;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.View;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider;

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
	 * The preprocessing service.
	 */
	private final PreprocessingService preprocessingService;

	/**
	 * The optical layout recognition (OLR) service.
	 */
	private final OpticalLayoutRecognitionService olrService;

	/**
	 * The optical character recognition (OCR) service.
	 */
	private final OpticalCharacterRecognitionService ocrService;

	/**
	 * The post-correction service.
	 */
	private final PostcorrectionService postcorrectionService;

	/**
	 * Creates a workflow service.
	 * 
	 * @param configurationService  The configuration service.
	 * @param securityService       The security service.
	 * @param preprocessingService  The preprocessing service.
	 * @param olrService            The optical layout recognition (OLR) service.
	 * @param ocrService            The optical character recognition (OCR) service.
	 * @param postcorrectionService The post-correction service.
	 * @since 1.8
	 */
	public WorkflowService(ConfigurationService configurationService, SecurityService securityService,
			PreprocessingService preprocessingService, OpticalLayoutRecognitionService olrService,
			OpticalCharacterRecognitionService ocrService, PostcorrectionService postcorrectionService) {
		super(WorkflowService.class, configurationService);

		this.securityService = securityService;

		this.preprocessingService = preprocessingService;
		this.olrService = olrService;
		this.ocrService = ocrService;
		this.postcorrectionService = postcorrectionService;
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

					if (Files.exists(path))
						metadata = getPersistenceManager(uuid).getEntity(Type.workflow_metadata_v1, Metadata.class,
								message -> logger.warn(message));
				} catch (Exception e) {
					logger.warn(e.getMessage());
				}

				if (metadata == null)
					return null;

				metadata.setUpdated(new Date());
				metadata.setUpdateUser(securityService.getUser());

				metadata.setLabel(label);
				metadata.setDescription(description);
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
				return getPersistenceManager(uuid).getEntity(Type.workflow_v1, Workflow.class,
						message -> logger.warn(message));
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}

		return null;
	}

	/**
	 * Returns the job workflow service provider.
	 * 
	 * @param processor The processor.
	 * @return The job workflow provider. Null if the service provider is unknown or
	 *         inactive or not of type process.
	 * @since 1.8
	 */
	private de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow.Provider getActiveProcessServiceProvider(
			Processor processor) {
		Snapshot.Type snapshotType = null;

		ServiceProvider provider = preprocessingService.getActiveProvider(processor.getId());

		if (provider != null)
			snapshotType = Snapshot.Type.preprocessing;
		else {
			provider = olrService.getActiveProvider(processor.getId());

			if (provider != null)
				snapshotType = Snapshot.Type.olr;
			else {
				provider = ocrService.getActiveProvider(processor.getId());

				if (provider != null)
					snapshotType = Snapshot.Type.ocr;
				else {
					provider = postcorrectionService.getActiveProvider(processor.getId());

					if (provider != null)
						snapshotType = Snapshot.Type.postcorrection;
				}
			}
		}

		return provider == null || !(provider instanceof ProcessServiceProvider) ? null
				: new de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow.Provider(
						(ProcessServiceProvider) provider, snapshotType, processor);
	}

	/**
	 * Returns the number of steps of the workflow job.
	 * 
	 * @param providers The job workflow providers.
	 * @param paths     The workflow paths.
	 * @param steps     The current number of steps.
	 * @return The actual number of steps taking into accout the given path.
	 * @throws IllegalArgumentException Throws on unknown workflow provider.
	 * @since 1.8
	 */
	private int getJobSteps(
			Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow.Provider> providers,
			List<de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path> paths, int steps)
			throws IllegalArgumentException {
		if (paths != null)
			for (de.uniwuerzburg.zpd.ocr4all.application.persistence.workflow.Path path : paths)
				if (path != null) {
					if (!providers.containsKey(path.getId()))
						throw new IllegalArgumentException(
								"WorkflowService: no workflow processor available for path id '" + path.getId() + "'.");

					steps = getJobSteps(providers, path.getChildren(), steps++);
				}

		return steps;
	}

	/**
	 * Returns the job workflow with given UUID.
	 * 
	 * @param locale      The application locale.
	 * @param project     The project.
	 * @param sandbox     The sandbox.
	 * @param trackParent The track to the parent snapshot.
	 * @param uuid        The UUID.
	 * @return The job workflow. Null if the workflow is not available.
	 * @throws IllegalArgumentException Throws on workflow troubles.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow getJobWorkflow(Locale locale, Project project,
			Sandbox sandbox, List<Integer> trackParent, String uuid) throws IllegalArgumentException {
		Workflow workflow = getWorkflow(uuid);

		if (workflow == null)
			return null;

		if (workflow.getPaths() == null || workflow.getPaths().isEmpty())
			throw new IllegalArgumentException("WorkflowService: no workflow path available.");

		if (workflow.getProcessors() == null || workflow.getProcessors().isEmpty())
			throw new IllegalArgumentException("WorkflowService: no workflow processor available.");

		/*
		 * Loads the process service providers.
		 */
		Hashtable<String, de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow.Provider> providers = new Hashtable<>();
		for (Processor processor : workflow.getProcessors())
			if (processor != null) {
				if (providers.containsKey(processor.getIdPath()))
					throw new IllegalArgumentException("WorkflowService: workflow processor path ID '"
							+ processor.getIdPath() + "' is not unique.");
				de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow.Provider provider = getActiveProcessServiceProvider(
						processor);

				if (provider == null)
					throw new IllegalArgumentException("WorkflowService: process service provider ID '"
							+ processor.getId() + "' is unknown or inactive or not of type process.");
				else
					providers.put(processor.getIdPath(), provider);
			}

		int steps = getJobSteps(providers, workflow.getPaths(), 0);

		if (steps == 0)
			throw new IllegalArgumentException("WorkflowService: no workflow path available.");

		return new de.uniwuerzburg.zpd.ocr4all.application.core.job.Workflow(configurationService, locale,
				Job.Processing.parallel, steps, project, sandbox, sandbox.getSnapshot(trackParent), uuid, providers,
				workflow.getPaths());
	}

}
