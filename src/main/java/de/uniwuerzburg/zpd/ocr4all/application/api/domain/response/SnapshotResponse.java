/**
 * File:     SnapshotResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.api.worker.spi.CoreServiceProviderApiController;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Snapshot;

/**
 * Defines snapshot responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SnapshotResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The project id.
	 */
	private String projectId;

	/**
	 * The sandbox id.
	 */
	private String sandboxId;

	/**
	 * True if the snapshot is consistent.
	 */
	private boolean isConsistent;

	/**
	 * The track.
	 */
	private List<Integer> track;

	/**
	 * The configuration.
	 */
	private SnapshotConfigurationResponse configuration;

	/**
	 * The process.
	 */
	private SnapshotProcessResponse process;

	/**
	 * Creates a snapshot response for the api.
	 * 
	 * @param snapshot The snapshot.
	 * @since 1.8
	 */
	public SnapshotResponse(Snapshot snapshot) {
		super();

		projectId = snapshot.getSandbox().getProject().getId();
		sandboxId = snapshot.getSandbox().getId();

		isConsistent = snapshot.getConfiguration().isConsistent();
		track = snapshot.getConfiguration().getTrack();

		configuration = isConsistent
				? new SnapshotConfigurationResponse(
						snapshot.getConfiguration().getConfiguration().getMainConfiguration())
				: null;

		process = isConsistent
				? new SnapshotProcessResponse(snapshot.getConfiguration().getConfiguration().getProcessConfiguration())
				: null;
	}

	/**
	 * Returns the project id.
	 *
	 * @return The project id.
	 * @since 1.8
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Set the project id.
	 *
	 * @param projectId The project id to set.
	 * @since 1.8
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Returns the Sandbox id.
	 *
	 * @return The Sandbox id.
	 * @since 1.8
	 */
	public String getSandboxId() {
		return sandboxId;
	}

	/**
	 * Set the Sandbox id.
	 *
	 * @param SandboxId The Sandbox id to set.
	 * @since 1.8
	 */
	public void setSandboxId(String SandboxId) {
		this.sandboxId = SandboxId;
	}

	/**
	 * Returns true if the snapshot is consistent.
	 *
	 * @return True if the snapshot is consistent.
	 * @since 1.8
	 */
	public boolean isConsistent() {
		return isConsistent;
	}

	/**
	 * Set to true if the snapshot is consistent.
	 *
	 * @param isConsistent The consistent flag to set.
	 * @since 1.8
	 */
	public void setConsistent(boolean isConsistent) {
		this.isConsistent = isConsistent;
	}

	/**
	 * Returns the track.
	 *
	 * @return The track.
	 * @since 1.8
	 */
	public List<Integer> getTrack() {
		return track;
	}

	/**
	 * Set the track.
	 *
	 * @param track The track to set.
	 * @since 1.8
	 */
	public void setTrack(List<Integer> track) {
		this.track = track;
	}

	/**
	 * Returns the configuration.
	 *
	 * @return The configuration.
	 * @since 1.8
	 */
	public SnapshotConfigurationResponse getConfiguration() {
		return configuration;
	}

	/**
	 * Set the configuration.
	 *
	 * @param configuration The configuration to set.
	 * @since 1.8
	 */
	public void setConfiguration(SnapshotConfigurationResponse configuration) {
		this.configuration = configuration;
	}

	/**
	 * Returns the process.
	 *
	 * @return The process.
	 * @since 1.8
	 */
	public SnapshotProcessResponse getProcess() {
		return process;
	}

	/**
	 * Set the process.
	 *
	 * @param process The process to set.
	 * @since 1.8
	 */
	public void setProcess(SnapshotProcessResponse process) {
		this.process = process;
	}

	/**
	 * Defines snapshot configuration responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotConfigurationResponse
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The type label.
		 */
		@JsonProperty("type-label")
		private String typeLabel;

		/**
		 * Default constructor for a snapshot configuration response for the api.
		 * 
		 * @since 1.8
		 */
		public SnapshotConfigurationResponse() {
			super();
		}

		/**
		 * Creates a snapshot configuration response for the api.
		 * 
		 * @param snapshot The snapshot.
		 * @since 1.8
		 */
		public SnapshotConfigurationResponse(
				de.uniwuerzburg.zpd.ocr4all.application.persistence.project.sandbox.Snapshot snapshot) {
			super(snapshot.getType(), snapshot.getLabel(), snapshot.getDescription(), snapshot.getServiceProvider(),
					snapshot.getInstance(), snapshot.getDate(), snapshot.getUpdated(), snapshot.getUser(),
					snapshot.getLock());

			switch (snapshot.getType()) {
			case launcher:
				typeLabel = CoreServiceProviderApiController.Type.launcher.getLabel();
				break;
			case ocr:
				typeLabel = CoreServiceProviderApiController.Type.ocr.getLabel();
				break;
			case olr:
				typeLabel = CoreServiceProviderApiController.Type.olr.getLabel();
				break;
			case postcorrection:
				typeLabel = CoreServiceProviderApiController.Type.postcorrection.getLabel();
				break;
			case preprocessing:
				typeLabel = CoreServiceProviderApiController.Type.preprocessing.getLabel();
				break;
			case tool:
				typeLabel = CoreServiceProviderApiController.Type.tool.getLabel();
				break;
			default:
				typeLabel = null;
				break;
			}
		}

		/**
		 * Returns the typeLabel.
		 *
		 * @return The typeLabel.
		 * @since 17
		 */
		public String getTypeLabel() {
			return typeLabel;
		}

		/**
		 * Set the typeLabel.
		 *
		 * @param typeLabel The typeLabel to set.
		 * @since 17
		 */
		public void setTypeLabel(String typeLabel) {
			this.typeLabel = typeLabel;
		}

	}

	/**
	 * Defines snapshot process responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotProcessResponse
			extends de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Default constructor for a snapshot process response for the api.
		 * 
		 * @since 1.8
		 */
		public SnapshotProcessResponse() {
			super();
		}

		/**
		 * Creates a snapshot process response for the api.
		 * 
		 * @param process The process.
		 * @since 1.8
		 */
		public SnapshotProcessResponse(de.uniwuerzburg.zpd.ocr4all.application.persistence.job.Process process) {
			super(process.getState(), process.getProgress(), process.getStandardOutput(), process.getStandardError(),
					process.getNote(), process.getDate(), process.getUpdated(), process.getUser());
		}
	}
}
