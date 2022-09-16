/**
 * File:     MetsResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     16.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser;
import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser.Root;
import de.uniwuerzburg.zpd.ocr4all.application.core.project.workflow.Workflow;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.MetsUtils;

/**
 * Defines mets responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class MetsResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The mets XML file.
	 */
	private String file;

	/**
	 * True if the mets XML file is available.
	 */
	@JsonProperty("available")
	private boolean isAvailable;

	/**
	 * The creation time.
	 */
	@JsonProperty("creation-time")
	private Date creationTime = null;

	/**
	 * The agents.
	 */
	private List<Agent> agents = new ArrayList<>();

	/**
	 * The file groups.
	 */
	private List<FileGroup> fileGroups = new ArrayList<>();

	/**
	 * The pages.
	 */
	private List<Page> pages = new ArrayList<>();

	/**
	 * Creates a mets response for the api.
	 * 
	 * @param workflow The workflow.
	 * @throws JsonParseException   Throws on parsing problems, used when
	 *                              non-well-formed content is encountered.
	 * @throws JsonMappingException Throws to signal fatal problems with mapping of
	 *                              content.
	 * @throws IOException          Throws if an I/O exception of some sort has
	 *                              occurred.
	 * @since 1.8
	 */
	public MetsResponse(Workflow workflow) throws JsonParseException, JsonMappingException, IOException {
		super();

		Path mets = Paths.get(workflow.getConfiguration().getSnapshots().getRoot().getFolder().toString(),
				workflow.getConfiguration().getMetsFileName());
		file = mets.toString();

		if (Files.exists(mets)) {
			isAvailable = true;

			final MetsParser.Root root = (new MetsParser()).deserialise(mets.toFile());

			try {
				creationTime = MetsUtils.getDate(root.getHeader().getCreated());
			} catch (Exception e) {
				creationTime = null;
			}

			for (MetsParser.Root.Header.Agent agent : root.getHeader().getAgents())
				agents.add(new Agent(agent));

			for (MetsParser.Root.FileGroup fileGroup : root.getFileGroups())
				fileGroups.add(new FileGroup(fileGroup));

			for (MetsParser.Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap().getPhysicalSequence()
					.getPages())
				pages.add(new Page(page));
		} else
			isAvailable = false;
	}

	/**
	 * Returns the mets XML file.
	 *
	 * @return The mets XML file.
	 * @since 1.8
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Set the mets XML file.
	 *
	 * @param file The file to set.
	 * @since 1.8
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Returns true if the mets XML file is available.
	 *
	 * @return True if the mets XML file is available.
	 * @since 1.8
	 */
	@JsonGetter("available")
	public boolean isAvailable() {
		return isAvailable;
	}

	/**
	 * Set to true if the mets XML file is available.
	 *
	 * @param isAvailable The available flag to set.
	 * @since 1.8
	 */
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	/**
	 * Returns the creation time.
	 *
	 * @return The creation time.
	 * @since 1.8
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * Set the creation time.
	 *
	 * @param creationTime The creation time to set.
	 * @since 1.8
	 */
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * Returns the agents.
	 *
	 * @return The agents.
	 * @since 1.8
	 */
	public List<Agent> getAgents() {
		return agents;
	}

	/**
	 * Set the agents.
	 *
	 * @param agents The agents to set.
	 * @since 1.8
	 */
	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}

	/**
	 * Returns the file groups.
	 *
	 * @return The file groups.
	 * @since 1.8
	 */
	public List<FileGroup> getFileGroups() {
		return fileGroups;
	}

	/**
	 * Set the file groups.
	 *
	 * @param fileGroups The file groups to set.
	 * @since 1.8
	 */
	public void setFileGroups(List<FileGroup> fileGroups) {
		this.fileGroups = fileGroups;
	}

	/**
	 * Returns the pages.
	 *
	 * @return The pages.
	 * @since 1.8
	 */
	public List<Page> getPages() {
		return pages;
	}

	/**
	 * Set the pages.
	 *
	 * @param pages The pages to set.
	 * @since 1.8
	 */
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	/**
	 * Defines mets agent responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Agent implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Defines types.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public enum Type {
			software, unknown
		}

		/**
		 * The type.
		 */
		private Type type;

		/**
		 * The role.
		 */
		private String role;

		/**
		 * The name.
		 */
		private String name;

		/**
		 * The input file group.
		 */
		@JsonProperty("input-file-group")
		private String inputFileGroup = null;

		/**
		 * The output file group.
		 */
		@JsonProperty("output-file-group")
		private String outputFileGroup = null;;

		/**
		 * The parameter.
		 */
		private String parameter = null;;

		/**
		 * The page id.
		 */
		@JsonProperty("page-id")
		private String pageId = null;;

		/**
		 * Creates a mets agent response for the api.
		 * 
		 * @param agent The mets agent.
		 * @since 1.8
		 */
		public Agent(MetsParser.Root.Header.Agent agent) {
			super();

			try {
				type = Type.valueOf(agent.getOtherType().toLowerCase());
			} catch (Exception e) {
				type = Type.unknown;
			}

			role = MetsUtils.getAgentRole(agent.getRole(), agent.getOtherRole());
			name = agent.getName();

			for (MetsParser.Root.Header.Agent.Note note : agent.getNotes()) {
				MetsUtils.Note noteType = MetsUtils.Note.getNote(note.getOption());
				if (noteType != null)
					switch (noteType) {
					case inputFileGroup:
						inputFileGroup = note.getValue();

						break;
					case outputFileGroup:
						outputFileGroup = note.getValue();

						break;
					case parameter:
						parameter = note.getValue();

						break;
					case pageId:
						pageId = note.getValue();

						break;
					}
			}
		}

		/**
		 * Returns the type.
		 *
		 * @return The type.
		 * @since 1.8
		 */
		public Type getType() {
			return type;
		}

		/**
		 * Set the type.
		 *
		 * @param type The type to set.
		 * @since 1.8
		 */
		public void setType(Type type) {
			this.type = type;
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
		 * Returns the input file group.
		 *
		 * @return The input file group.
		 * @since 1.8
		 */
		public String getInputFileGroup() {
			return inputFileGroup;
		}

		/**
		 * Set the input file group.
		 *
		 * @param inputFileGroup The input file group to set.
		 * @since 1.8
		 */
		public void setInputFileGroup(String inputFileGroup) {
			this.inputFileGroup = inputFileGroup;
		}

		/**
		 * Returns the output file group.
		 *
		 * @return The output file group.
		 * @since 1.8
		 */
		public String getOutputFileGroup() {
			return outputFileGroup;
		}

		/**
		 * Set the output file group.
		 *
		 * @param outputFileGroup The output file group to set.
		 * @since 1.8
		 */
		public void setOutputFileGroup(String outputFileGroup) {
			this.outputFileGroup = outputFileGroup;
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
		 * Returns the page id.
		 *
		 * @return The page id.
		 * @since 1.8
		 */
		public String getPageId() {
			return pageId;
		}

		/**
		 * Set the page id.
		 *
		 * @param pageId The page id to set.
		 * @since 1.8
		 */
		public void setPageId(String pageId) {
			this.pageId = pageId;
		}
	}

	/**
	 * Defines mets file group responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class FileGroup implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The id.
		 */
		private String id;

		/**
		 * The files.
		 */
		private List<File> files = new ArrayList<>();

		/**
		 * Creates a mets file group response for the api.
		 * 
		 * @param fileGroup The mets file group.
		 * @since 1.8
		 */
		public FileGroup(MetsParser.Root.FileGroup fileGroup) {
			super();

			id = fileGroup.getId();

			for (MetsParser.Root.FileGroup.File file : fileGroup.getFiles())
				files.add(new File(file));

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
		 * Defines mets file responses for the api.
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
			 * Defines locations.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public enum Location {
				file, unknown
			}

			/**
			 * The id.
			 */
			private String id;

			/**
			 * The mime type.
			 */
			@JsonProperty("mime-type")
			private String mimeType;

			/**
			 * The location.
			 */
			private Location location;

			/**
			 * The path.
			 */
			private String path;

			/**
			 * Creates a mets file response for the api.
			 * 
			 * @param file The mets file.
			 * @since 1.8
			 */
			public File(MetsParser.Root.FileGroup.File file) {
				super();

				id = file.getId();
				mimeType = file.getMimeType();

				location = "OTHER".equalsIgnoreCase(file.getLocation().getType())
						&& Location.file.name().equalsIgnoreCase(file.getLocation().getOtherType()) ? Location.file
								: Location.unknown;

				path = file.getLocation().getPath();

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
			 * Returns the mime type.
			 *
			 * @return The mime type.
			 * @since 1.8
			 */
			public String getMimeType() {
				return mimeType;
			}

			/**
			 * Set the mime type.
			 *
			 * @param mimeType The mime type to set.
			 * @since 1.8
			 */
			public void setMimeType(String mimeType) {
				this.mimeType = mimeType;
			}

			/**
			 * Returns the location.
			 *
			 * @return The location.
			 * @since 1.8
			 */
			public Location getLocation() {
				return location;
			}

			/**
			 * Set the location.
			 *
			 * @param location The location to set.
			 * @since 1.8
			 */
			public void setLocation(Location location) {
				this.location = location;
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

		}
	}

	/**
	 * Defines mets page responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class Page implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The file ids.
		 */
		@JsonProperty("file-ids")
		private List<String> fileIds = new ArrayList<>();

		/**
		 * Creates a mets page response for the api.
		 * 
		 * @param page The mets page.
		 * @since 1.8
		 */
		public Page(MetsParser.Root.StructureMap.PhysicalSequence.Page page) {
			super();

			for (Root.StructureMap.PhysicalSequence.Page.FileId fileId : page.getFileIds())
				fileIds.add(fileId.getId());
		}

		/**
		 * Returns the file ids.
		 *
		 * @return The file ids.
		 * @since 1.8
		 */
		public List<String> getFileIds() {
			return fileIds;
		}

		/**
		 * Set the file ids.
		 *
		 * @param fileIds The file ids to set.
		 * @since 1.8
		 */
		public void setFileIds(List<String> fileIds) {
			this.fileIds = fileIds;
		}
	}
}
