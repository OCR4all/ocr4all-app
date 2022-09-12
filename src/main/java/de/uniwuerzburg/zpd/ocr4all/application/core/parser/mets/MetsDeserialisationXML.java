/**
 * File:     MetsDeserialisationXML.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import de.uniwuerzburg.zpd.ocr4all.application.core.util.MetsUtils;

/**
 * Insert your text here
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class MetsDeserialisationXML {

	/**
	 * Insert your text here
	 * 
	 * @param file
	 * @throws IOException
	 * @since 1.8
	 */
	public MetsDeserialisationXML(Path file) throws IOException {
		super();

	}

	/**
	 * Insert your text here
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	@JacksonXmlRootElement(namespace = "mets", localName = "mets")
	public static class Root {
		/**
		 * The header.
		 */
		@JacksonXmlProperty(localName = "metsHdr")
		private Header header;

		@JacksonXmlElementWrapper(localName = "fileSec")
		@JacksonXmlProperty(localName = "fileGrp")
		private List<FileGroup> fileGroups;

		@JacksonXmlProperty(localName = "structMap")
		private StructureMap structureMap;

		/**
		 * Returns the header.
		 *
		 * @return The header.
		 * @since 1.8
		 */
		public Header getHeader() {
			return header;
		}

		/**
		 * Set the header.
		 *
		 * @param header The header to set.
		 * @since 1.8
		 */
		public void setHeader(Header header) {
			this.header = header;
		}

		/**
		 * Returns the fileGroups.
		 *
		 * @return The fileGroups.
		 * @since 1.8
		 */
		public List<FileGroup> getFileGroups() {
			return fileGroups;
		}

		/**
		 * Set the fileGroups.
		 *
		 * @param fileGroups The fileGroups to set.
		 * @since 1.8
		 */
		public void setFileGroups(List<FileGroup> fileGroups) {
			this.fileGroups = fileGroups;
		}

		/**
		 * Returns the structureMap.
		 *
		 * @return The structureMap.
		 * @since 1.8
		 */
		public StructureMap getStructureMap() {
			return structureMap;
		}

		/**
		 * Set the structureMap.
		 *
		 * @param structureMap The structureMap to set.
		 * @since 1.8
		 */
		public void setStructureMap(StructureMap structureMap) {
			this.structureMap = structureMap;
		}

		public static class Header {
			/**
			 * The create date.
			 */
			@JacksonXmlProperty(localName = "CREATEDATE")
			private Date created;

			/**
			 * The agents.
			 */
			@JacksonXmlProperty(localName = "agent")
			@JacksonXmlElementWrapper(useWrapping = false)
			private List<Agent> agents;

			/**
			 * Returns the created.
			 *
			 * @return The created.
			 * @since 1.8
			 */
			public Date getCreated() {
				return created;
			}

			/**
			 * Set the created.
			 *
			 * @param created The created to set.
			 * @since 1.8
			 */
			public void setCreated(String created) {
				try {
					this.created = MetsUtils.getDate(created);
				} catch (ParseException e) {
					this.created = null;
				}
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

			public static class Agent {
				@JacksonXmlProperty(localName = "ROLE")
				private String role;

				@JacksonXmlProperty(localName = "OTHERROLE")
				private String otherRole;

				private String name;

				/**
				 * The notes.
				 */
				@JacksonXmlProperty(localName = "note")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<Note> notes;

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
				 * Returns the otherRole.
				 *
				 * @return The otherRole.
				 * @since 1.8
				 */
				public String getOtherRole() {
					return otherRole;
				}

				/**
				 * Set the otherRole.
				 *
				 * @param otherRole The otherRole to set.
				 * @since 1.8
				 */
				public void setOtherRole(String otherRole) {
					this.otherRole = otherRole;
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
				 * Returns the notes.
				 *
				 * @return The notes.
				 * @since 1.8
				 */
				public List<Note> getNotes() {
					return notes;
				}

				/**
				 * Set the notes.
				 *
				 * @param notes The notes to set.
				 * @since 1.8
				 */
				public void setNotes(List<Note> notes) {
					this.notes = notes;
				}

				public static class Note {
					@JacksonXmlProperty(namespace = "ocrd")
					private String option;

					@JacksonXmlText
					private String value;

					/**
					 * Returns the option.
					 *
					 * @return The option.
					 * @since 1.8
					 */
					public String getOption() {
						return option;
					}

					/**
					 * Set the option.
					 *
					 * @param option The option to set.
					 * @since 1.8
					 */
					public void setOption(String option) {
						this.option = option;
					}

					/**
					 * Returns the value.
					 *
					 * @return The value.
					 * @since 1.8
					 */
					public String getValue() {
						return value;
					}

					/**
					 * Set the value.
					 *
					 * @param value The value to set.
					 * @since 1.8
					 */
					public void setValue(String value) {
						this.value = value;
					}

				}

			}

		}

		public static class FileGroup {
			@JacksonXmlProperty(localName = "USE")
			private String use;

			@JacksonXmlProperty(localName = "file")
			@JacksonXmlElementWrapper(useWrapping = false)
			private List<File> files;

			/**
			 * Returns the use.
			 *
			 * @return The use.
			 * @since 1.8
			 */
			public String getUse() {
				return use;
			}

			/**
			 * Set the use.
			 *
			 * @param use The use to set.
			 * @since 1.8
			 */
			public void setUse(String use) {
				this.use = use;
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

			public static class File {
				@JacksonXmlProperty(localName = "ID")
				private String id;

				@JacksonXmlProperty(localName = "MIMETYPE")
				private String mimeType;

				@JacksonXmlProperty(localName = "FLocat")
				private Location location;

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
				 * Returns the mimeType.
				 *
				 * @return The mimeType.
				 * @since 1.8
				 */
				public String getMimeType() {
					return mimeType;
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
				 * Set the mimeType.
				 *
				 * @param mimeType The mimeType to set.
				 * @since 1.8
				 */
				public void setMimeType(String mimeType) {
					this.mimeType = mimeType;
				}

				public static class Location {
					@JacksonXmlProperty(namespace = "xlink", localName = "href")
					private String path;

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
		}

		public static class StructureMap {
			@JacksonXmlProperty(localName = "div")
			private PhysicalSequence physicalSequence;

			/**
			 * Returns the physicalSequence.
			 *
			 * @return The physicalSequence.
			 * @since 1.8
			 */
			public PhysicalSequence getPhysicalSequence() {
				return physicalSequence;
			}

			/**
			 * Set the physicalSequence.
			 *
			 * @param physicalSequence The physicalSequence to set.
			 * @since 1.8
			 */
			public void setPhysicalSequence(PhysicalSequence physicalSequence) {
				this.physicalSequence = physicalSequence;
			}

			public static class PhysicalSequence {
				@JacksonXmlProperty(localName = "div")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<Page> pages;

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

			}

			public static class Page {
				@JacksonXmlProperty(localName = "ID")
				private String id;

				@JacksonXmlProperty(localName = "fptr")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<FileId> fileIds;

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
				 * Returns the fileIds.
				 *
				 * @return The fileIds.
				 * @since 1.8
				 */
				public List<FileId> getFileIds() {
					return fileIds;
				}

				/**
				 * Set the fileIds.
				 *
				 * @param fileIds The fileIds to set.
				 * @since 1.8
				 */
				public void setFileIds(List<FileId> fileIds) {
					this.fileIds = fileIds;
				}

				public static class FileId {

					@JacksonXmlProperty(localName = "FILEID")
					private String id;

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

				}
			}
		}

	}

	/**
	 * Insert your text here
	 * 
	 * @param args
	 * @since 1.8
	 */
	public static void main(String[] args) {
		File mets = new File("/home/baier/ocr4all/workspace/projects/project_01/workflows/ws_01/snapshots/mets.xml");
		XmlMapper xmlMapper = new XmlMapper();

		// xmlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			Root root = xmlMapper.readValue(mets, Root.class);

			System.out.println("created: " + root.getHeader().getCreated());

			System.out.println("Header: ");
			for (Root.Header.Agent agent : root.getHeader().getAgents()) {
				System.out.println(
						"\tagent: " + agent.getRole() + " -> " + agent.getOtherRole() + " -> " + agent.getName());

				for (Root.Header.Agent.Note note : agent.getNotes())
					System.out.println("\t\tnote: " + note.getOption() + " -> " + note.getValue());

			}

			System.out.println("File groups: ");
			for (Root.FileGroup fileGroup : root.getFileGroups()) {
				System.out.println("\tgroup: " + fileGroup.getUse());

				for (Root.FileGroup.File file : fileGroup.getFiles()) {
					System.out.println("\t\tfile: " + file.getId() + " -> " + file.getMimeType() + " -> "
							+ file.getLocation().getPath());
				}
			}

			System.out.println("Structure map: ");
			for (Root.StructureMap.Page page : root.getStructureMap().getPhysicalSequence().getPages()) {
				System.out.println("\tpage: " + page.getId());

				for (Root.StructureMap.Page.FileId fileId : page.getFileIds())
					System.out.println("\t\tid: " + fileId.getId());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
