/**
 * File:     ProjectSecurity.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     08.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.project.ProjectConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.Project;

/**
 * Defines securities for projects.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ProjectSecurity implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The id.
	 */
	private String id;

	/**
	 * The user grants.
	 */
	private Set<Grant> users;

	/**
	 * The groups grants.
	 */
	private Set<Grant> groups;

	/**
	 * The other rights.
	 */
	private Right other = null;

	/**
	 * Default constructor for a project security.
	 * 
	 * @since 1.8
	 */
	public ProjectSecurity() {
		super();
	}

	/**
	 * Creates a project security.
	 * 
	 * @param project The project.
	 * @since 1.8
	 */
	public ProjectSecurity(Project project) {
		super();

		id = project.getId();

		Set<Grant> users = new HashSet<>();
		for (ProjectConfiguration.Grant grant : project.getConfiguration().getConfiguration().getUserGrants())
			users.add(new Grant(grant));
		setUsers(users);

		Set<Grant> groups = new HashSet<>();
		for (ProjectConfiguration.Grant grant : project.getConfiguration().getConfiguration().getGroupGrants())
			groups.add(new Grant(grant));
		setGroups(groups);

		setOther(new Right(project.getConfiguration().getConfiguration().getRights()));
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
	 * Set the id.
	 *
	 * @param id The id to set.
	 * @since 1.8
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the user grants.
	 *
	 * @return The user grants.
	 * @since 1.8
	 */
	public Set<Grant> getUsers() {
		return users;
	}

	/**
	 * Set the user grants.
	 *
	 * @param grants The grants to set.
	 * @since 1.8
	 */
	public void setUsers(Set<Grant> grants) {
		users = grants;
	}

	/**
	 * Returns the group grants.
	 *
	 * @return The group grants.
	 * @since 1.8
	 */
	public Set<Grant> getGroups() {
		return groups;
	}

	/**
	 * Set the group grants.
	 *
	 * @param grants The grants to set.
	 * @since 1.8
	 */
	public void setGroups(Set<Grant> grants) {
		groups = grants;
	}

	/**
	 * Returns the other rights.
	 *
	 * @return The other rights.
	 * @since 1.8
	 */
	public Right getOther() {
		return other;
	}

	/**
	 * Set the other rights.
	 *
	 * @param rights The rights to set.
	 * @since 1.8
	 */
	public void setOther(Right rights) {
		other = rights;
	}

	/**
	 * Defines rights.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Right implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if read right is available.
		 */
		private boolean isRead;

		/**
		 * True if write right is available.
		 */
		private boolean isWrite;

		/**
		 * True if execute or special right is available.
		 */
		private boolean isExecute;

		/**
		 * True if special right is available.
		 */
		private boolean isSpecial;

		/**
		 * Default constructor for rights.
		 * 
		 * @since 1.8
		 */
		public Right() {
			super();
		}

		/**
		 * Creates a user rights for the project.
		 * 
		 * @param project The project.
		 * @since 1.8
		 */
		public Right(Project project) {
			super();

			if (project != null) {
				isRead = project.isRead();
				isWrite = project.isWrite();
				isExecute = project.isExecute();
				isSpecial = project.isSpecial();
			}

		}

		/**
		 * Creates a rights for the project configuration.
		 * 
		 * @param configurationRight The configuration right.
		 * @since 1.8
		 */
		public Right(ProjectConfiguration.Right configurationRight) {
			super();

			if (configurationRight != null) {
				isRead = configurationRight.isRead();
				isWrite = configurationRight.isWrite();
				isExecute = configurationRight.isExecute();
				isSpecial = configurationRight.isSpecial();
			}

		}

		/**
		 * Returns true if read right is available.
		 *
		 * @return True if read right is available.
		 * @since 1.8
		 */
		public boolean isRead() {
			return isRead;
		}

		/**
		 * Set to true if read right is available.
		 *
		 * @param isRead The read flag to set.
		 * @since 1.8
		 */
		public void setRead(boolean isRead) {
			this.isRead = isRead;
		}

		/**
		 * Returns true if write right is available.
		 *
		 * @return True if write right is available.
		 * @since 1.8
		 */
		public boolean isWrite() {
			return isWrite;
		}

		/**
		 * Set to true if write right is available.
		 *
		 * @param isWrite The write flag to set.
		 * @since 1.8
		 */
		public void setWrite(boolean isWrite) {
			this.isWrite = isWrite;
		}

		/**
		 * Returns true if execute or special right is available.
		 *
		 * @return True if execute or special right is available.
		 * @since 1.8
		 */
		public boolean isExecute() {
			return isExecute;
		}

		/**
		 * Set true if execute or special right is available.
		 *
		 * @param isExecute The execute flag to set.
		 * @since 1.8
		 */
		public void setExecute(boolean isExecute) {
			this.isExecute = isExecute;
		}

		/**
		 * Returns true if special right is available.
		 *
		 * @return True if special right is available.
		 * @since 1.8
		 */
		public boolean isSpecial() {
			return isSpecial;
		}

		/**
		 * Set to true if special right is available.
		 *
		 * @param isSpecial The special flag to set.
		 * @since 1.8
		 */
		public void setSpecial(boolean isSpecial) {
			this.isSpecial = isSpecial;
		}
	}

	/**
	 * Defines grants for project securities.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Grant extends Right {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The targets.
		 */
		private Set<String> targets;

		/**
		 * Default constructor for a grant for project security.
		 * 
		 * @since 1.8
		 */
		public Grant() {
			super();
		}

		/**
		 * Creates a grant for project security.
		 * 
		 * @param grant The project configuration grant.
		 * @since 1.8
		 */
		public Grant(ProjectConfiguration.Grant grant) {
			super(grant);

			if (grant != null)
				setTargets(grant.getTargets());
		}

		/**
		 * Returns the targets.
		 *
		 * @return The targets.
		 * @since 1.8
		 */
		public Set<String> getTargets() {
			return targets;
		}

		/**
		 * Set the targets.
		 *
		 * @param targets The targets to set.
		 * @since 1.8
		 */
		public void setTargets(Set<String> targets) {
			if (targets == null)
				this.targets = null;
			else {
				Set<String> objectives = new HashSet<String>();

				for (String target : targets)
					if (target != null && !target.isBlank())
						objectives.add(target.trim().toLowerCase());

				this.targets = objectives;
			}
		}
	}

}
