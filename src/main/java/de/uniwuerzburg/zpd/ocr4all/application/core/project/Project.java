/**
 * File:     Project.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.project
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.project;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.SnapshotConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Job;
import de.uniwuerzburg.zpd.ocr4all.application.core.job.Process;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.History;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ActionHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.ProjectHistory;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.util.PersistenceTools;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;

/**
 * Defines projects.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class Project implements Job.Cluster {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Project.class);

	/**
	 * Defines project rights. They are defined in a similar way as under Linux. The
	 * special permission extends the rights of the execute permission.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public enum Right {
		read('r'), write('w'), execute('x'), special('s');

		/**
		 * The label.
		 */
		private final char label;

		/**
		 * Creates a project right.
		 * 
		 * @param label The label.
		 * @since 1.8
		 */
		private Right(char label) {
			this.label = label;
		}

		/**
		 * Returns true if the given label matches the version label.
		 *
		 * @return True if the given label matches the version label.
		 * @since 1.8
		 */
		public boolean isLabel(char label) {
			return this.label == label;
		}

		/**
		 * Returns the label.
		 *
		 * @return The label.
		 * @since 1.8
		 */
		public char getLabel() {
			return label;
		}

		/**
		 * Returns the right for given label.
		 * 
		 * @param label The label. The label is case insensitive.
		 * @return The right for given label. Empty, if the right is not defined.
		 * @since 1.8
		 */
		public static Optional<Right> getRight(char label) {
			label = Character.toLowerCase(label);

			for (Right right : Right.values())
				if (right.isLabel(label))
					return Optional.of(right);

			return Optional.empty();
		}

		/**
		 * Returns the labels for given rights.
		 * 
		 * @param rights The rights.
		 * @return The labels.
		 * @since 1.8
		 */
		public static String getLabels(Set<Right> rights) {
			if (rights == null)
				return null;

			StringBuffer buffer = new StringBuffer();
			for (Right right : Right.values()) {
				if ((Right.execute.equals(right) && rights.contains(Right.special))
						|| (Right.special.equals(right) && !rights.contains(right)))
					continue;

				buffer.append(rights.contains(right) ? right.getLabel() : "-");
			}

			return buffer.toString();
		}

		/**
		 * Returns the labels for given rights.
		 * 
		 * @param rights The rights.
		 * @return The labels.
		 * @since 1.8
		 */
		public static String getLabels(Right... rights) {
			return getLabels(new HashSet<>(Arrays.asList(rights)));
		}

		/**
		 * Returns the synopsis.
		 * 
		 * @return The synopsis.
		 * @since 1.8
		 */
		public static String getSynopsis() {
			return "rw[x|s]";
		}
	}

	/**
	 * The configuration.
	 */
	private final ProjectConfiguration configuration;

	/**
	 * The security level. The default level is user.
	 */
	private SecurityService.Level securityLevel = SecurityService.Level.user;

	/**
	 * The rights.
	 */
	private final Set<Right> rights = new HashSet<>();

	/**
	 * The history persistence manager.
	 */
	private PersistenceManager historyManager = null;

	/**
	 * Creates a project.
	 * 
	 * @param configuration The configuration.
	 * @throws IllegalArgumentException Thrown if the configuration is not
	 *                                  available.
	 * @since 1.8
	 */
	public Project(ProjectConfiguration configuration) throws IllegalArgumentException {
		super();

		if (configuration == null)
			throw new IllegalArgumentException("the configuration is a required argument.");

		this.configuration = configuration;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public ProjectConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Returns the id.
	 *
	 * @return The id.
	 * @since 1.8
	 */
	public String getId() {
		return configuration.getConfiguration().getId();
	}

	/**
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return configuration.getConfiguration().getName();
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return configuration.getConfiguration().getDescription();
	}

	/**
	 * Returns the state.
	 *
	 * @return The state. Null if not set or the main configuration is not
	 *         available.
	 * @since 1.8
	 */
	public de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Project.State getState() {
		return configuration.getConfiguration().getState();
	}

	/**
	 * Returns true if the user is set.
	 *
	 * @return True if the user is set.
	 * @since 1.8
	 */
	public boolean isUserSet() {
		return getUser() != null;
	}

	/**
	 * Returns the user.
	 *
	 * @return The user. Null if not set or the main configuration is not available.
	 * @since 1.8
	 */
	public String getUser() {
		return configuration.getConfiguration().getUser();
	}

	/**
	 * Returns the history persistence manager. Create a new one if necessary.
	 * 
	 * @return The history persistence manager.
	 * @since 1.8
	 */
	private PersistenceManager getHistoryManager() {
		if (historyManager == null)
			historyManager = new PersistenceManager(configuration.getConfiguration().getHistoryFile(),
					Type.project_action_history_v1, Type.project_process_history_v1);

		return historyManager;
	}

	/**
	 * Adds the history to the project.
	 * 
	 * @param history The history to be added.
	 * @since 1.8
	 */
	public void add(ProjectHistory history) {
		if (history != null)
			try {
				history.setDate(new Date());
				history.setUser(getUser());

				getHistoryManager().persist(true, history);
			} catch (Exception e) {
				logger.warn("Could not add the history to the project '" + configuration.getConfiguration().getName()
						+ "' - " + e.getMessage());
			}
	}

	/**
	 * Returns the history.
	 * 
	 * @return The history.
	 * @since 1.8
	 */
	public List<History> getHistory() {
		try {
			return getHistoryManager().getEntities(History.class, message -> logger.warn(message),
					PersistenceTools.getTrackingDateComparator(false));
		} catch (IOException e) {
			logger.warn(e.getMessage());

			return new ArrayList<>();
		}
	}

	/**
	 * Zips the history.
	 * 
	 * @param outputStream The output stream for writing the zipped history.
	 * @since 1.8
	 */
	public void zipHistory(OutputStream outputStream) {
		try {
			getHistoryManager().zip(outputStream);
		} catch (NullPointerException | IOException e) {
			logger.warn(e.getMessage());
		}
	}

	/**
	 * Returns the service provider target.
	 * 
	 * @param workflow              The workflow. Null no workflow is selected.
	 * @param snapshotConfiguration The configuration of the selected snapshot. Null
	 *                              if no snapshot is selected.
	 * @return The service provider target.
	 * @since 1.8
	 */
	public Target getTarget(Workflow workflow, SnapshotConfiguration snapshotConfiguration) {
		Target.Project.Images images = new Target.Project.Images(configuration.getImages().getFolios(),
				new Target.Project.Images.Derivatives(configuration.getImages().getDerivatives().getFormat().getSPI(),
						configuration.getImages().getDerivatives().getThumbnail(),
						configuration.getImages().getDerivatives().getDetail(),
						configuration.getImages().getDerivatives().getBest()));

		return new Target(configuration.getConfiguration().getExchange(), configuration.getConfiguration().getOpt(),
				new Target.Project(configuration.getConfiguration().getFolder(),
						configuration.getConfiguration().getFolioFile(), images),
				workflow == null && snapshotConfiguration == null ? null
						: new Target.Workflow(workflow == null ? null : workflow.getConfiguration().getFolder(),
								workflow == null ? null : workflow.getConfiguration().getSnapshots().getFolder(),
								workflow == null ? false : workflow.isLaunched(),
								snapshotConfiguration == null ? null : snapshotConfiguration.getSandbox().getFolder(),
								snapshotConfiguration == null ? null : snapshotConfiguration.getTrack(),
								new Target.Workflow.Mets(configuration.getWorkflowsConfiguration().getMetsFileName(),
										configuration.getWorkflowsConfiguration().getMetsGroup())));
	}

	/**
	 * Set the security level.
	 *
	 * @param level The level to set if it is non null.
	 * @since 1.8
	 */
	public void setSecurityLevel(SecurityService.Level level) {
		if (level != null)
			securityLevel = level;
	}

	/**
	 * Returns true if the administrator security permission is achievable.
	 *
	 * @return True if the administrator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isAdministrator() {
		return SecurityService.Level.isAchievable(securityLevel, SecurityService.Level.administrator);
	}

	/**
	 * Returns true if the coordinator security permission is achievable.
	 *
	 * @return True if the coordinator security permission is achievable.
	 * @since 1.8
	 */
	public boolean isCoordinator() {
		return SecurityService.Level.isAchievable(securityLevel, SecurityService.Level.coordinator);
	}

	/**
	 * Returns true if the user security permission is achievable.
	 *
	 * @return True if the user security permission is achievable.
	 * @since 1.8
	 */
	public boolean isUser() {
		return SecurityService.Level.isAchievable(securityLevel, SecurityService.Level.user);
	}

	/**
	 * Returns true if the given rights are available.
	 * 
	 * @param rights The required rights.
	 * @return True if the given rights are available.
	 * @since 1.8
	 */
	public boolean isRights(Right... rights) {
		if (rights == null)
			return false;
		else {
			for (Right right : rights) {
				if (Right.execute.equals(right) && this.rights.contains(Right.special))
					continue;

				if (right == null || !this.rights.contains(right))
					return false;
			}

			return true;
		}
	}

	/**
	 * Returns true if at least one right is available.
	 * 
	 * @return True if at least one right is available.
	 * @since 1.8
	 */
	public boolean isRightExist() {
		return !rights.isEmpty();
	}

	/**
	 * Returns true if all rights are available.
	 * 
	 * @return True if all rights are available.
	 * @since 1.8
	 */
	public boolean isAllRights() {
		return rights.size() == Right.values().length - 1;
	}

	/**
	 * Returns the rights.
	 * 
	 * @return The rights.
	 * @since 1.8
	 */
	public Set<Right> getRights() {
		return new HashSet<>(rights);
	}

	/**
	 * Returns true if read right is available.
	 *
	 * @return True if read right is available.
	 * @since 1.8
	 */
	public boolean isRead() {
		return rights.contains(Right.read);
	}

	/**
	 * Returns true if write is available.
	 *
	 * @return True if write is available.
	 * @since 1.8
	 */
	public boolean isWrite() {
		return rights.contains(Right.write);
	}

	/**
	 * Returns true if execute or special right is available.
	 *
	 * @return True if execute or special right is available.
	 * @since 1.8
	 */
	public boolean isExecute() {
		return rights.contains(Right.execute) || rights.contains(Right.special);
	}

	/**
	 * Returns true if special right is available.
	 *
	 * @return True if special right is available.
	 * @since 1.8
	 */
	public boolean isSpecial() {
		return rights.contains(Right.special);
	}

	/**
	 * Adds the project right.
	 * 
	 * @param right The right to add.
	 * @since 1.8
	 */
	public void addRight(Right right) {
		if (right != null)
			switch (right) {
			case read:
			case write:
				rights.add(right);

				break;
			case execute:
				if (!isSpecial())
					rights.add(right);

				break;
			case special:
				rights.remove(Right.execute);
				rights.add(right);

				break;
			}
	}

	/**
	 * Adds all project rights.
	 *
	 * @since 1.8
	 */
	public void addAllRights() {
		for (Right right : Right.values())
			addRight(right);
	}

	/**
	 * Removes the project right.
	 * 
	 * @param right The right to reset.
	 * @since 1.8
	 */
	public void removeRight(Right right) {
		if (right != null)
			rights.remove(right);

	}

	/**
	 * Removes all project rights.
	 *
	 * @since 1.8
	 */
	public void removeAllRights() {
		rights.clear();
	}

	/**
	 * Returns the label for the project rights.
	 * 
	 * @return The label for the project rights.
	 * @since 1.8
	 */
	public String getRightLabels() {
		return Right.getLabels(rights);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.job.Job.Cluster#associated(java.
	 * util.Collection)
	 */
	@Override
	public Set<Job> associated(Collection<Job> jobs) {
		Set<Job> associated = new HashSet<>();

		if (jobs != null)
			for (Job job : jobs)
				if (job != null && (job instanceof Process) && isSame(((Process) job).getProject()))
					associated.add(job);

		return associated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "name=" + configuration.getConfiguration().getName()
				+ (configuration.getConfiguration().isCreatedSet()
						? ", created=" + configuration.getConfiguration().getCreated()
						: "")
				+ (configuration.getConfiguration().isUpdatedSet()
						? ", updated=" + configuration.getConfiguration().getUpdated()
						: "")
				+ (configuration.getConfiguration().isDoneSet() ? ", done=" + configuration.getConfiguration().getDone()
						: "")
				+ ", state=" + configuration.getConfiguration().getState().name()
				+ (isUserSet() ? ", user=" + getUser() : "") + ", path=" + configuration.getFolder() + ", exchange"
				+ (configuration.getConfiguration().isExchangeDirectory() ? "" : "[missed folder]") + "="
				+ configuration.getConfiguration().getExchange()
				+ (configuration.getConfiguration().isDescriptionSet()
						? ", description=" + configuration.getConfiguration().getDescription()
						: "");
	}

	/**
	 * Return true if the current and given project are the same.
	 * 
	 * @param project The project to test.
	 * @return True if the current and given project are the same.
	 * @since 1.8
	 */
	public boolean isSame(Project project) {
		try {
			return project != null
					&& Files.isSameFile(configuration.getFolder(), project.getConfiguration().getFolder());
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns the folios.
	 *
	 * @return The folios. On troubles returns an empty array.
	 * @since 1.8
	 */
	public List<Folio> getFolios() {
		return getFolios(null);
	}

	/**
	 * Returns the folios that are restricted to the specified IDs.
	 *
	 * @param ids The folios IDs. If null, returns all folios.
	 * @return The folios. On troubles returns an empty array.
	 * @since 1.8
	 */
	public List<Folio> getFolios(Set<Integer> ids) {
		try {
			List<Folio> folios = new ArrayList<>();

			for (Folio folio : (new PersistenceManager(configuration.getConfiguration().getFolioFile(),
					Type.project_folio_v1)).getEntities(Folio.class))
				if (ids == null || ids.contains(folio.getId()))
					folios.add(folio);

			return folios;
		} catch (Exception e) {
			logger.warn(e.getMessage());

			return new ArrayList<>();
		}
	}

	/**
	 * Writes the folios order to given output stream.
	 *
	 * @param outputStream The output stream for writing the folios order.
	 * @since 1.8
	 */
	public void foliosOrder(OutputStream outputStream) {
		if (outputStream != null) {
			PrintWriter writer = new PrintWriter(outputStream);

			for (Folio folio : getFolios())
				writer.println(folio.getId() + "\t" + folio.getName());

			writer.flush();
		}
	}

	/**
	 * Persist the folios.
	 * 
	 * @param folios The folios to persist.
	 * @return The number of persisted folios. On troubles returns -1.
	 * @since 1.8
	 */
	public int persist(List<Folio> folios) {
		try {
			return (new PersistenceManager(configuration.getConfiguration().getFolioFile(), Type.project_folio_v1))
					.persist(folios);
		} catch (Exception e) {
			logger.warn(e.getMessage());

			return -1;
		}
	}

	/**
	 * Reset the project. The workflow are deleted.
	 * 
	 * @return True if the project could be reseted.
	 * @since 1.8
	 */
	public boolean reset() {
		if (configuration.getWorkflowsConfiguration().reset()) {
			add(new ActionHistory("reset workflow", null, null));

			return true;
		} else
			add(new ActionHistory(History.Level.error, "reset workflow", "problems resetting the project workflow",
					null));

		return false;
	}

	/**
	 * Initializes the project, this means, the folios and the workflow are deleted.
	 * 
	 * @return True if the project could be initialized.
	 * @since 1.8
	 */
	public boolean initialize() {
		if (reset()) {
			if (configuration.resetFolios()) {
				add(new ActionHistory("reset folios", null, null));

				return true;
			} else
				add(new ActionHistory(History.Level.error, "reset folios", "problems reseting the project folios",
						null));
		}

		return false;
	}

}
