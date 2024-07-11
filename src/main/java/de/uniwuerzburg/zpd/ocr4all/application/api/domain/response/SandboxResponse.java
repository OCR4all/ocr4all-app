/**
 * File:     SandboxResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     09.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.project.sandbox.Sandbox;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.MetsParser;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.MetsUtils;

/**
 * Defines sandbox responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SandboxResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The project id.
	 */
	private String projectId;

	/**
	 * The id.
	 */
	private String id;

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The description.
	 */
	private String description;

	/**
	 * The state.
	 */
	private String state;

	/**
	 * The tracking.
	 */
	private TrackingResponse tracking;

	/**
	 * The done time stamp.
	 */
	private Date done;

	/**
	 * The keywords.
	 */
	private Set<String> keywords;

	/**
	 * True if the user can access the snapshots.
	 */
	@JsonProperty("snapshot-access")
	private boolean isSnapshotAccess;

	/**
	 * True if there are snapshots.
	 */
	@JsonProperty("snapshot-available")
	private boolean isSnapshotAvailable;

	/**
	 * The snapshot synopsis.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("snapshot-synopsis")
	private SnapshotSynopsisResponse snapshotSynopsis;

	/**
	 * Creates a sandbox response for the api without sandbox synopsis.
	 * 
	 * @param sandbox The sandbox.
	 * @since 1.8
	 */
	public SandboxResponse(Sandbox sandbox) {
		this(sandbox, false);
	}

	/**
	 * Creates a sandbox response for the api.
	 * 
	 * @param sandbox           The sandbox.
	 * @param isSandboxSynopsis True if the sandbox synopsis is required.
	 * @since 1.8
	 */
	public SandboxResponse(Sandbox sandbox, boolean isSandboxSynopsis) {
		super();

		projectId = sandbox.getProject().getId();
		id = sandbox.getId();

		name = sandbox.getName();
		description = sandbox.getDescription();

		state = sandbox.getState().name();

		tracking = new TrackingResponse(sandbox.getConfiguration().getConfiguration());
		keywords = sandbox.getConfiguration().getConfiguration().getKeywords();

		done = sandbox.getConfiguration().getConfiguration().getDone();

		isSnapshotAccess = sandbox.isSnapshotAccess();
		isSnapshotAvailable = isSnapshotAccess && sandbox.isSnapshotAvailable();
		snapshotSynopsis = isSandboxSynopsis && isSnapshotAvailable ? new SnapshotSynopsisResponse(sandbox) : null;
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
	 * Returns the name.
	 *
	 * @return The name.
	 * @since 1.8
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name.
	 *
	 * @param name The name to set.
	 * @since 1.8
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the description.
	 *
	 * @return The description.
	 * @since 1.8
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description.
	 *
	 * @param description The description to set.
	 * @since 1.8
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 1.8
	 */
	public String getState() {
		return state;
	}

	/**
	 * Set the state.
	 *
	 * @param state The state to set.
	 * @since 1.8
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Returns the tracking.
	 *
	 * @return The tracking.
	 * @since 1.8
	 */
	public TrackingResponse getTracking() {
		return tracking;
	}

	/**
	 * Set the tracking.
	 *
	 * @param tracking The tracking to set.
	 * @since 1.8
	 */
	public void setTracking(TrackingResponse tracking) {
		this.tracking = tracking;
	}

	/**
	 * Returns the done.
	 *
	 * @return The done.
	 * @since 1.8
	 */
	public Date getDone() {
		return done;
	}

	/**
	 * Set the done.
	 *
	 * @param done The done to set.
	 * @since 1.8
	 */
	public void setDone(Date done) {
		this.done = done;
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

	/**
	 * Set the keywords.
	 *
	 * @param keywords The keywords to set.
	 * @since 1.8
	 */
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

	/**
	 * Returns true if the user can access the snapshots.
	 *
	 * @return True if the user can access the snapshots.
	 * @since 1.8
	 */
	@JsonGetter("snapshot-access")
	public boolean isSnapshotAccess() {
		return isSnapshotAccess;
	}

	/**
	 * Set to true if the user can access the snapshots.
	 *
	 * @param isSnapshotAccess The snapshots access flag to set.
	 * @since 1.8
	 */
	public void setSnapshotAccess(boolean isSnapshotAccess) {
		this.isSnapshotAccess = isSnapshotAccess;
	}

	/**
	 * Returns true if there are snapshots.
	 *
	 * @return True if there are snapshots.
	 * @since 1.8
	 */
	@JsonGetter("snapshot-available")
	public boolean isSnapshotAvailable() {
		return isSnapshotAvailable;
	}

	/**
	 * Set to true if there are snapshots.
	 *
	 * @param isSnapshotAvailable The available flag to set.
	 * @since 1.8
	 */
	public void setSnapshotAvailable(boolean isSnapshotAvailable) {
		this.isSnapshotAvailable = isSnapshotAvailable;
	}

	/**
	 * Returns the snapshot synopsis.
	 *
	 * @return The snapshot synopsis.
	 * @since 1.8
	 */
	public SnapshotSynopsisResponse getSnapshotSynopsis() {
		return snapshotSynopsis;
	}

	/**
	 * Set the snapshot synopsis.
	 *
	 * @param snapshotSynopsis The snapshot synopsis to set.
	 * @since 1.8
	 */
	public void setSnapshotSynopsis(SnapshotSynopsisResponse snapshotSynopsis) {
		this.snapshotSynopsis = snapshotSynopsis;
	}

	/**
	 * Defines sandbox synopsis responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class SnapshotSynopsisResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The standard error message.
		 */
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JsonProperty("standard-error")
		private String standardError = null;

		/**
		 * The home directory.
		 */
		private String home;

		/**
		 * The root processor.
		 */
		@JsonProperty("root-processor")
		private Processor rootProcessor;

		/**
		 * Creates a snapshot synopsis response for the api.
		 * 
		 * @param sandbox The sandbox.
		 * @since 1.8
		 */
		public SnapshotSynopsisResponse(Sandbox sandbox) {
			super();

			home = sandbox.getConfiguration().getSnapshots().getRoot().getFolder().toString();

			Path mets = Paths.get(home, sandbox.getConfiguration().getMetsFileName());
			if (Files.exists(mets))
				try {
					final MetsParser.Root root = (new MetsParser()).deserialise(mets.toFile());
					final String metsGroup = sandbox.getConfiguration().getMetsGroup();

					// mets file groups
					final Hashtable<String, MetsParser.Root.FileGroup> fileGroups = new Hashtable<>();

					for (MetsParser.Root.FileGroup fileGroup : root.getFileGroups())
						fileGroups.put(fileGroup.getId(), fileGroup);

					// mets pages
					final Hashtable<String, String> images = new Hashtable<>();

					MetsUtils.Page metsPageUtils = MetsUtils.getPage(metsGroup);
					for (MetsParser.Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap()
							.getPhysicalSequence().getPages())
						try {
							String id = metsPageUtils.getGroupId(page.getId());
							for (MetsParser.Root.StructureMap.PhysicalSequence.Page.FileId fieldId : page.getFileIds())
								images.put(fieldId.getId(), id);
						} catch (Exception e) {
							// Ignore malformed mets page
						}

					// mets agents
					final MetsUtils.FileGroup fileGroup = MetsUtils.getFileGroup(metsGroup);

					Hashtable<String, Processor> processors = new Hashtable<>();
					for (MetsParser.Root.Header.Agent agent : root.getHeader().getAgents()) {
						Processor processor = new Processor(agent, fileGroup, fileGroups, images);

						if (processor.getTrack() != null)
							processors.put(getTrackId(processor.getTrack()), processor);
					}

					// build the processor tree and sort the derived processors
					for (Processor processor : processors.values())
						if (processor.getTrack().isEmpty())
							rootProcessor = processor;
						else {
							Processor parent = processors.get(getTrackId(true, processor.getTrack()));

							if (parent != null)
								parent.getDerived().add(processor);
						}

					for (Processor processor : processors.values())
						sortDerived(processor);
				} catch (Exception e) {
					standardError = addMessage(standardError,
							"Trouble parsing mets XML file '" + mets.toString() + "' - " + e.getMessage() + ".");
				}
		}

		/**
		 * Returns the sandbox track id.
		 * 
		 * @param track The track.
		 * @return The sandbox track id.
		 * @since 1.8
		 */
		private String getTrackId(List<Integer> track) {
			return getTrackId(false, track);
		}

		/**
		 * Returns the sandbox track id.
		 * 
		 * @param isParent True if determine the sandbox track id of parent sandbox.
		 * @param track    The track.
		 * @return The sandbox track id.
		 * @since 1.8
		 */
		private String getTrackId(boolean isParent, List<Integer> track) {
			int size = track.size() - (isParent ? 1 : 0);

			if (size < 0)
				return null;
			else {
				StringBuffer buffer = new StringBuffer();
				for (int index = 0; index < size; index++) {
					if (buffer.length() > 0)
						buffer.append("#");

					buffer.append(track.get(index));
				}

				return buffer.toString();
			}
		}

		/**
		 * Sort the derived processors by snapshot id.
		 * 
		 * @param processor The processor to sort the derived processors.
		 * @since 1.8
		 */
		private void sortDerived(Processor processor) {
			Collections.sort(processor.getDerived(), (p1, p2) -> p1.getTrack().get(p1.getTrack().size() - 1)
					- p2.getTrack().get(p2.getTrack().size() - 1));
		}

		/**
		 * Adds the message to the source message adn returns it.
		 * 
		 * @param source  The source.
		 * @param message The message to add.
		 * @return The new message.
		 * @since 1.8
		 */
		private String addMessage(String source, String message) {
			return (source == null ? "" : source + "\n") + message;
		}

		/**
		 * Returns the standard error message.
		 *
		 * @return The standard error message.
		 * @since 1.8
		 */
		public String getStandardError() {
			return standardError;
		}

		/**
		 * Set the standard error message.
		 *
		 * @param message The message to set.
		 * @since 1.8
		 */
		public void setStandardError(String message) {
			standardError = message;
		}

		/**
		 * Returns the home directory.
		 *
		 * @return The home directory.
		 * @since 1.8
		 */
		public String getHome() {
			return home;
		}

		/**
		 * Set the home directory.
		 *
		 * @param home The home directory to set.
		 * @since 1.8
		 */
		public void setHome(String home) {
			this.home = home;
		}

		/**
		 * Returns the root processor.
		 *
		 * @return The root processor.
		 * @since 1.8
		 */
		public Processor getRootProcessor() {
			return rootProcessor;
		}

		/**
		 * Set the root processor.
		 *
		 * @param rootProcessor The root processor to set.
		 * @since 1.8
		 */
		public void setRootProcessor(Processor rootProcessor) {
			this.rootProcessor = rootProcessor;
		}

		/**
		 * Defines processor responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Processor implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The role.
			 */
			private String role;

			/**
			 * The name.
			 */
			private String name;

			/**
			 * The parameter.
			 */
			private String parameter = null;

			/**
			 * The track.
			 */
			private List<Integer> track = null;

			/**
			 * The derived processors.
			 */
			@JsonProperty("derived-processors")
			private List<Processor> derived = new ArrayList<>();

			/**
			 * The files.
			 */
			private List<File> files = new ArrayList<>();

			/**
			 * Creates a processor response for the api.
			 * 
			 * @param agent      The mets agent.
			 * @param fileGroup  The file group utility.
			 * @param fileGroups The mets file groups. The key is the group id.
			 * @param images     The mets image id map to the ocr4all image id.
			 * @since 1.8
			 */
			public Processor(MetsParser.Root.Header.Agent agent, MetsUtils.FileGroup fileGroup,
					Hashtable<String, MetsParser.Root.FileGroup> fileGroups, Hashtable<String, String> images) {
				super();

				role = MetsUtils.getAgentRole(agent.getRole(), agent.getOtherRole());
				name = agent.getName();

				for (MetsParser.Root.Header.Agent.Note note : agent.getNotes()) {
					MetsUtils.Note noteType = MetsUtils.Note.getNote(note.getOption());

					if (noteType != null)
						switch (noteType) {
						case outputFileGroup:
							track = fileGroup.getTrack(note.getValue());

							if (fileGroups.containsKey(note.getValue()))
								for (MetsParser.Root.FileGroup.File file : fileGroups.get(note.getValue()).getFiles())
									files.add(new File(file, images));

							break;
						case parameter:
							parameter = note.getValue();

							break;
						default:
							break;
						}
				}

			}

			/**
			 * Returns the role.
			 *
			 * @return The role.
			 * @since 1.8
			 */
			public String getRole() {
				return role;
			}

			/**
			 * Set the role.
			 *
			 * @param role The role to set.
			 * @since 1.8
			 */
			public void setRole(String role) {
				this.role = role;
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
			 * Set the name.
			 *
			 * @param name The name to set.
			 * @since 1.8
			 */
			public void setName(String name) {
				this.name = name;
			}

			/**
			 * Returns the parameter.
			 *
			 * @return The parameter.
			 * @since 1.8
			 */
			public String getParameter() {
				return parameter;
			}

			/**
			 * Set the parameter.
			 *
			 * @param parameter The parameter to set.
			 * @since 1.8
			 */
			public void setParameter(String parameter) {
				this.parameter = parameter;
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
			 * Returns the derived processors.
			 *
			 * @return The derived processors.
			 * @since 1.8
			 */
			public List<Processor> getDerived() {
				return derived;
			}

			/**
			 * Set the derived processors.
			 *
			 * @param derived The derived processors to set.
			 * @since 1.8
			 */
			public void setDerived(List<Processor> derived) {
				this.derived = derived;
			}

			/**
			 * Returns the files.
			 *
			 * @return The files.
			 * @since 1.8
			 */
			public List<File> getFiles() {
				return files;
			}

			/**
			 * Set the files.
			 *
			 * @param files The files to set.
			 * @since 1.8
			 */
			public void setFiles(List<File> files) {
				this.files = files;
			}

			/**
			 * Defines file responses for the api.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public static class File implements Serializable {
				/**
				 * The serial version UID.
				 */
				private static final long serialVersionUID = 1L;

				/**
				 * The mime type.
				 */
				@JsonProperty("mime-type")
				private String mimeType;

				/**
				 * The path.
				 */
				private String path;

				/**
				 * The image id.
				 */
				@JsonProperty("image-id")
				private String imageId;

				/**
				 * Creates a file response for the api.
				 * 
				 * @param file   The mets file.
				 * @param images The mets image id map to the ocr4all image id.
				 * @since 1.8
				 */
				public File(MetsParser.Root.FileGroup.File file, Hashtable<String, String> images) {
					super();

					mimeType = file.getMimeType();
					path = file.getLocation().getPath();
					imageId = images.containsKey(file.getId()) ? images.get(file.getId()) : null;
				}

				/**
				 * Returns the mimeType.
				 *
				 * @return The mimeType.
				 * @since 1.8
				 */
				public String getMimeType() {
					return mimeType;
				}

				/**
				 * Set the mimeType.
				 *
				 * @param mimeType The mimeType to set.
				 * @since 1.8
				 */
				public void setMimeType(String mimeType) {
					this.mimeType = mimeType;
				}

				/**
				 * Returns the path.
				 *
				 * @return The path.
				 * @since 1.8
				 */
				public String getPath() {
					return path;
				}

				/**
				 * Set the path.
				 *
				 * @param path The path to set.
				 * @since 1.8
				 */
				public void setPath(String path) {
					this.path = path;
				}

				/**
				 * Returns the image id.
				 *
				 * @return The image id.
				 * @since 1.8
				 */
				public String getImageId() {
					return imageId;
				}

				/**
				 * Set the image id.
				 *
				 * @param imageId The image id to set.
				 * @since 1.8
				 */
				public void setImageId(String imageId) {
					this.imageId = imageId;
				}

			}
		}
	}

}
