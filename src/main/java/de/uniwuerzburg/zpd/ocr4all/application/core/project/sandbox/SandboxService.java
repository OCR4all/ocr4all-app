/**
 * File:     WorkflowService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;

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
	 * Creates a workflow service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	@Autowired
	public WorkflowService(ConfigurationService configurationService) {
		super(WorkflowService.class, configurationService);
	}

	/**
	 * Returns the workflow path. Creates it if required.
	 * 
	 * @return The workflow path. Empty, if the path is not a directory.
	 * @since 1.8
	 */
	private Optional<Path> getWorkflowsPath(Project project) {
		return ConfigurationService.initializeFolder(false,
				configurationService.getWorkspace().getProjects()
						.getProject(project.getConfiguration().getFolder(), project.getUser())
						.getWorkflowsConfiguration().getFolder(),
				"workflows");
	}

	/**
	 * Returns the authorized workflows of given project sorted by name.
	 * 
	 * @param project The project.
	 * @return The workflows.
	 * @since 1.8
	 */
	public List<Workflow> getWorkflows(Project project) {
		List<Workflow> workflows = new ArrayList<>();

		if (project != null)
			try {
				Optional<Path> workflowsPath = getWorkflowsPath(project);
				if (workflowsPath.isPresent()) {
					Files.list(workflowsPath.get()).filter(Files::isDirectory).forEach(path -> {
						Workflow workflow = authorize(project, new Workflow(path, project));
						if (workflow != null)
							workflows.add(workflow);
					});

					Collections.sort(workflows, (w1, w2) -> w1.getConfiguration().getConfiguration().getName()
							.compareToIgnoreCase(w2.getConfiguration().getConfiguration().getName()));
				}

			} catch (IOException e) {
				logger.warn("Cannot not load workflows - " + e.getMessage());
			}

		return workflows;
	}

	/**
	 * Returns true if the workflow is available.
	 * 
	 * @param project The project.
	 * @param id      The workflow id. This is the folder name.
	 * @return True if the workflow is available.
	 * @since 1.8
	 */
	public boolean isAvailable(Project project, String id) {
		if (project != null && id != null && !id.isBlank()) {
			Optional<Path> workflowsPath = getWorkflowsPath(project);
			if (workflowsPath.isPresent())
				return Files.exists(Paths.get(workflowsPath.get().toString(), id.trim()).normalize());
		}

		return false;
	}

	/**
	 * Authorizes the workflow.
	 * 
	 * @param project The project.
	 * @param id      The workflow id. This is the folder name.
	 * @return The workflow if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Workflow authorize(Project project, String id) {
		if (project != null && id != null && !id.isBlank()) {
			Optional<Path> workflowsPath = getWorkflowsPath(project);
			if (workflowsPath.isPresent()) {
				Path path = Paths.get(workflowsPath.get().toString(), id.trim()).normalize();

				if (path.startsWith(workflowsPath.get()) && !path.equals(workflowsPath.get())
						&& Files.isDirectory(path))
					return authorize(project, new Workflow(path, project));
			}
		}

		return null;
	}

	/**
	 * Authorizes the workflow.
	 * 
	 * @param project  The project.
	 * @param workflow The workflow.
	 * @return The workflow if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Workflow authorize(Project project, Workflow workflow) {
		return project != null && workflow != null
				&& (project.isSpecial() || !workflow.getConfiguration().getConfiguration().isSpecialRightRequired())
						? workflow
						: null;
	}

}
