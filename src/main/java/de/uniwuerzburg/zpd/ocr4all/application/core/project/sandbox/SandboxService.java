/**
 * File:     SandboxService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.SnapshotConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;

/**
 * Defines sandbox services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Service
public class SandboxService extends CoreService {

	/**
	 * Creates a sandbox service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 1.8
	 */
	public SandboxService(ConfigurationService configurationService) {
		super(SandboxService.class, configurationService);
	}

	/**
	 * Returns the sandbox path. Creates it if required.
	 * 
	 * @return The sandbox path. Empty, if the path is not a directory.
	 * @since 1.8
	 */
	private Optional<Path> getSandboxesPath(Project project) {
		return ConfigurationService.initializeFolder(false,
				configurationService.getWorkspace().getProjects()
						.getProject(project.getConfiguration().getFolder(), project.getUser())
						.getSandboxesConfiguration().getFolder(),
				"sandboxes");
	}

	/**
	 * Returns the authorized sandboxes of given project sorted by name.
	 * 
	 * @param project The project.
	 * @return The sandboxes.
	 * @since 1.8
	 */
	public List<Sandbox> getSandboxes(Project project) {
		List<Sandbox> sandboxes = new ArrayList<>();

		if (project != null)
			try {
				Optional<Path> sandboxesPath = getSandboxesPath(project);
				if (sandboxesPath.isPresent()) {
					Files.list(sandboxesPath.get()).filter(Files::isDirectory).forEach(path -> {
						Sandbox sandbox = authorize(project, new Sandbox(path, project));
						if (sandbox != null)
							sandboxes.add(sandbox);
					});

					Collections.sort(sandboxes, (w1, w2) -> w1.getConfiguration().getConfiguration().getName()
							.compareToIgnoreCase(w2.getConfiguration().getConfiguration().getName()));
				}

			} catch (IOException e) {
				logger.warn("Cannot not load sandboxes - " + e.getMessage());
			}

		return sandboxes;
	}

	/**
	 * Returns true if the sandbox is available.
	 * 
	 * @param project The project.
	 * @param id      The sandbox id. This is the folder name.
	 * @return True if the sandbox is available.
	 * @since 1.8
	 */
	public boolean isAvailable(Project project, String id) {
		if (project != null && id != null && !id.isBlank()) {
			Optional<Path> sandboxesPath = getSandboxesPath(project);
			if (sandboxesPath.isPresent())
				return Files.exists(Paths.get(sandboxesPath.get().toString(), id.trim()).normalize());
		}

		return false;
	}

	/**
	 * Authorizes the sandbox.
	 * 
	 * @param project The project.
	 * @param id      The sandbox id. This is the folder name.
	 * @return The sandbox if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Sandbox authorize(Project project, String id) {
		if (project != null && id != null && !id.isBlank()) {
			Optional<Path> sandboxesPath = getSandboxesPath(project);
			if (sandboxesPath.isPresent()) {
				Path path = Paths.get(sandboxesPath.get().toString(), id.trim()).normalize();

				if (path.startsWith(sandboxesPath.get()) && !path.equals(sandboxesPath.get())
						&& Files.isDirectory(path))
					return authorize(project, new Sandbox(path, project));
			}
		}

		return null;
	}

	/**
	 * Authorizes the sandbox.
	 * 
	 * @param project The project.
	 * @param sandbox The sandbox.
	 * @return The sandbox if authorized. Otherwise returns null.
	 * @since 1.8
	 */
	public Sandbox authorize(Project project, Sandbox sandbox) {
		return project != null && sandbox != null
				&& (project.isSpecial() || !sandbox.getConfiguration().getConfiguration().isSpecialRightRequired())
						? sandbox
						: null;
	}

	/**
	 * Return the snapshot tree.
	 * 
	 * @param project The project.
	 * @param sandbox The sandbox.
	 * @return The snapshot tree if authorized. Otherwise returns null. If no
	 *         snapshots are available, the root snapshot and its children are null.
	 * @since 17
	 */
	public SnapshotTree getSnapshotTree(Project project, Sandbox sandbox) {
		sandbox = authorize(project, sandbox);

		return sandbox == null ? null : new SnapshotTree(sandbox);
	}

	/**
	 * SnapshotTree is an immutable class that defines the snapshot trees.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class SnapshotTree {
		/**
		 * The snapshot.
		 */
		private final Snapshot snapshot;

		/**
		 * The children.
		 */
		private final List<SnapshotTree> children;

		/**
		 * Creates a snapshot tree.
		 * 
		 * @param sandbox The sandbox.
		 * @since 17
		 */
		SnapshotTree(Sandbox sandbox) {
			super();

			snapshot = sandbox.getSnapshot();

			children = snapshot == null ? null : build(sandbox, snapshot);
		}

		/**
		 * Creates a snapshot tree.
		 * 
		 * @param snapshot The snapshot.
		 * @param children The children.
		 * @since 17
		 */
		private SnapshotTree(Sandbox sandbox, Snapshot snapshot) {
			super();

			this.snapshot = snapshot;
			this.children = build(sandbox, snapshot);
		}

		/**
		 * Build the snapshot tree.
		 * 
		 * @param sandbox  The sandbox.
		 * @param snapshot The snapshot to return the children.
		 * @return The snapshot children.
		 * @since 17
		 */
		private List<SnapshotTree> build(Sandbox sandbox, Snapshot snapshot) {
			List<SnapshotTree> tree = new ArrayList<>();

			for (SnapshotConfiguration derived : snapshot.getConfiguration().getDerived())
				tree.add(new SnapshotTree(sandbox, new Snapshot(derived, sandbox)));

			return tree;
		}

		/**
		 * Returns the snapshot.
		 *
		 * @return The snapshot.
		 * @since 17
		 */
		public Snapshot getSnapshot() {
			return snapshot;
		}

		/**
		 * Returns the children.
		 *
		 * @return The children.
		 * @since 17
		 */
		public List<SnapshotTree> getChildren() {
			return children;
		}

	}
}
