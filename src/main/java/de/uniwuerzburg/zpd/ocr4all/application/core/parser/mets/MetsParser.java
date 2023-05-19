/**
 * File:     MetsParser.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     12.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Defines mets XML document parsers.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class MetsParser {
	/**
	 * The xml mapper that does not fails on unknown properties.
	 */
	private final XmlMapper xmlMapper = new XmlMapper();
	{
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Default constructor for a mets XML document parser.
	 * 
	 * @since 1.8
	 */
	public MetsParser() {
		super();
	}

	/**
	 * Deserialise the given mets content.
	 * 
	 * @param content The mets content.
	 * @return The deserialised mets.
	 * @throws JsonProcessingException Throws if problems are encountered when
	 *                                 processing (parsing, generating) JSON/XML
	 *                                 content that are not pure I/O problems. .
	 * @throws JsonMappingException    Throws if the input JSON/XML structure does
	 *                                 not match structure expected for result type
	 *                                 (or has other mismatch issues).
	 * @since 1.8
	 */
	public Root deserialise(String content) throws JsonProcessingException, JsonMappingException {
		return xmlMapper.readValue(content, Root.class);
	}

	/**
	 * Deserialise the given mets file.
	 * 
	 * @param file The mets file.
	 * @return The deserialised mets.
	 * @throws JsonParseException   Throws if underlying input contains invalid
	 *                              content of type JsonParser supports (JSON/XML
	 *                              for default case).
	 * @throws JsonMappingException Throws if the input JSON/XML structure does not
	 *                              match structure expected for result type (or has
	 *                              other mismatch issues).
	 * @throws IOException          Throws if a low-level I/O problem (unexpected
	 *                              end-of-input, network error) occurs.
	 * @since 1.8
	 */
	public Root deserialise(File file) throws JsonParseException, JsonMappingException, IOException {
		return xmlMapper.readValue(file, Root.class);
	}

	/**
	 * Defines mets deserialised root objects.
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

		/**
		 * The file groups.
		 */
		@JacksonXmlElementWrapper(localName = "fileSec")
		@JacksonXmlProperty(localName = "fileGrp")
		private List<FileGroup> fileGroups;

		/**
		 * The structure map.
		 */
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
		 * Returns the structure map.
		 *
		 * @return The structure map.
		 * @since 1.8
		 */
		public StructureMap getStructureMap() {
			return structureMap;
		}

		/**
		 * Set the structure map.
		 *
		 * @param structureMap The structure map to set.
		 * @since 1.8
		 */
		public void setStructureMap(StructureMap structureMap) {
			this.structureMap = structureMap;
		}

		/**
		 * Defines mets deserialised header objects.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class Header {
			/**
			 * The create date.
			 */
			@JacksonXmlProperty(localName = "CREATEDATE")
			private String created;

			/**
			 * The agents.
			 */
			@JacksonXmlProperty(localName = "agent")
			@JacksonXmlElementWrapper(useWrapping = false)
			private List<Agent> agents;

			/**
			 * Returns the create date.
			 *
			 * @return The create date.
			 * @since 1.8
			 */
			public String getCreated() {
				return created;
			}

			/**
			 * Set the create date.
			 *
			 * @param created The create date to set.
			 * @since 1.8
			 */
			public void setCreated(String created) {
				this.created = created;
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
			 * Defines mets deserialised agent objects.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public static class Agent {
				/**
				 * The type.
				 */
				@JacksonXmlProperty(localName = "TYPE")
				private String type;

				/**
				 * The other type.
				 */
				@JacksonXmlProperty(localName = "OTHERTYPE")
				private String otherType;

				/**
				 * The role.
				 */
				@JacksonXmlProperty(localName = "ROLE")
				private String role;

				/**
				 * The other role.
				 */
				@JacksonXmlProperty(localName = "OTHERROLE")
				private String otherRole;

				/**
				 * The name.
				 */
				private String name;

				/**
				 * The notes.
				 */
				@JacksonXmlProperty(localName = "note")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<Note> notes;

				/**
				 * Returns the type.
				 *
				 * @return The type.
				 * @since 1.8
				 */
				public String getType() {
					return type;
				}

				/**
				 * Set the type.
				 *
				 * @param type The type to set.
				 * @since 1.8
				 */
				public void setType(String type) {
					this.type = type;
				}

				/**
				 * Returns the other type.
				 *
				 * @return The other type.
				 * @since 1.8
				 */
				public String getOtherType() {
					return otherType;
				}

				/**
				 * Set the other type.
				 *
				 * @param otherType The other type to set.
				 * @since 1.8
				 */
				public void setOtherType(String otherType) {
					this.otherType = otherType;
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
				 * Returns the other role.
				 *
				 * @return The other role.
				 * @since 1.8
				 */
				public String getOtherRole() {
					return otherRole;
				}

				/**
				 * Set the other role.
				 *
				 * @param otherRole The other role to set.
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

				/**
				 * Defines mets deserialised note objects.
				 *
				 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
				 * @version 1.0
				 * @since 1.8
				 */
				public static class Note {
					/**
					 * The option.
					 */
					@JacksonXmlProperty(namespace = "ocrd")
					private String option;

					/**
					 * The value.
					 */
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

		/**
		 * Defines mets deserialised file group objects.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class FileGroup {
			/**
			 * The id.
			 */
			@JacksonXmlProperty(localName = "USE")
			private String id;

			/**
			 * The files.
			 */
			@JacksonXmlProperty(localName = "file")
			@JacksonXmlElementWrapper(useWrapping = false)
			private List<File> files;

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
			 * Defines mets deserialised file objects.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public static class File {
				/**
				 * The id.
				 */
				@JacksonXmlProperty(localName = "ID")
				private String id;

				/**
				 * The mime type.
				 */
				@JacksonXmlProperty(localName = "MIMETYPE")
				private String mimeType;

				/**
				 * The file location.
				 */
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
				 * Returns true if the mime type is an image.
				 *
				 * @return True if the mime type is an image.
				 * @since 1.8
				 */
				public boolean isMimeTypeImage() {
					return mimeType != null && mimeType.startsWith("image/");
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
				 * Returns the file location.
				 *
				 * @return The file location.
				 * @since 1.8
				 */
				public Location getLocation() {
					return location;
				}

				/**
				 * Set the file location.
				 *
				 * @param location The file location to set.
				 * @since 1.8
				 */
				public void setLocation(Location location) {
					this.location = location;
				}

				/**
				 * Defines mets deserialised file location objects.
				 *
				 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
				 * @version 1.0
				 * @since 1.8
				 */
				public static class Location {
					/**
					 * The type.
					 */
					@JacksonXmlProperty(localName = "LOCTYPE")
					private String type;

					/**
					 * The other type.
					 */
					@JacksonXmlProperty(localName = "OTHERLOCTYPE")
					private String otherType;

					/**
					 * The path.
					 */
					@JacksonXmlProperty(namespace = "xlink", localName = "href")
					private String path;

					/**
					 * Returns the type.
					 *
					 * @return The type.
					 * @since 1.8
					 */
					public String getType() {
						return type;
					}

					/**
					 * Set the type.
					 *
					 * @param type The type to set.
					 * @since 1.8
					 */
					public void setType(String type) {
						this.type = type;
					}

					/**
					 * Returns the other type.
					 *
					 * @return The other type.
					 * @since 1.8
					 */
					public String getOtherType() {
						return otherType;
					}

					/**
					 * Set the other type.
					 *
					 * @param otherType The other type to set.
					 * @since 1.8
					 */
					public void setOtherType(String otherType) {
						this.otherType = otherType;
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
		}

		/**
		 * Defines mets deserialised structure map objects.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class StructureMap {
			/**
			 * The type.
			 */
			@JacksonXmlProperty(localName = "TYPE")
			private String type;

			/**
			 * The physical sequence.
			 */
			@JacksonXmlProperty(localName = "div")
			private PhysicalSequence physicalSequence;

			/**
			 * Returns the type.
			 *
			 * @return The type.
			 * @since 1.8
			 */
			public String getType() {
				return type;
			}

			/**
			 * Set the type.
			 *
			 * @param type The type to set.
			 * @since 1.8
			 */
			public void setType(String type) {
				this.type = type;
			}

			/**
			 * Returns the physical sequence.
			 *
			 * @return The physical sequence.
			 * @since 1.8
			 */
			public PhysicalSequence getPhysicalSequence() {
				return physicalSequence;
			}

			/**
			 * Set the physical sequence.
			 *
			 * @param physicalSequence The physical sequence to set.
			 * @since 1.8
			 */
			public void setPhysicalSequence(PhysicalSequence physicalSequence) {
				this.physicalSequence = physicalSequence;
			}

			/**
			 * Defines mets deserialised physical sequence objects.
			 *
			 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
			 * @version 1.0
			 * @since 1.8
			 */
			public static class PhysicalSequence {
				/**
				 * The type.
				 */
				@JacksonXmlProperty(localName = "TYPE")
				private String type;

				/**
				 * The pages.
				 */
				@JacksonXmlProperty(localName = "div")
				@JacksonXmlElementWrapper(useWrapping = false)
				private List<Page> pages;

				/**
				 * Returns the type.
				 *
				 * @return The type.
				 * @since 1.8
				 */
				public String getType() {
					return type;
				}

				/**
				 * Set the type.
				 *
				 * @param type The type to set.
				 * @since 1.8
				 */
				public void setType(String type) {
					this.type = type;
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
				 * Defines mets deserialised page objects.
				 *
				 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
				 * @version 1.0
				 * @since 1.8
				 */
				public static class Page {
					/**
					 * The type.
					 */
					@JacksonXmlProperty(localName = "TYPE")
					private String type;

					/**
					 * The id.
					 */
					@JacksonXmlProperty(localName = "ID")
					private String id;

					/**
					 * The file ids.
					 */
					@JacksonXmlProperty(localName = "fptr")
					@JacksonXmlElementWrapper(useWrapping = false)
					private List<FileId> fileIds;

					/**
					 * Returns the type.
					 *
					 * @return The type.
					 * @since 1.8
					 */
					public String getType() {
						return type;
					}

					/**
					 * Set the type.
					 *
					 * @param type The type to set.
					 * @since 1.8
					 */
					public void setType(String type) {
						this.type = type;
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
					 * Returns the file ids.
					 *
					 * @return The file ids.
					 * @since 1.8
					 */
					public List<FileId> getFileIds() {
						return fileIds;
					}

					/**
					 * Set the file ids.
					 *
					 * @param fileIds The file ids to set.
					 * @since 1.8
					 */
					public void setFileIds(List<FileId> fileIds) {
						this.fileIds = fileIds;
					}

					/**
					 * Defines mets deserialised file id objects.
					 *
					 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
					 * @version 1.0
					 * @since 1.8
					 */
					public static class FileId {
						/**
						 * The id.
						 */
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
	}

	/**
	 * Prints the mets deserialised object using the given stream.
	 * 
	 * @param stream The stream to print the mets deserialised object.
	 * @param root   The mets deserialised root object.
	 * @since 1.8
	 */
	public static void print(PrintStream stream, Root root) {
		stream.println("created: " + root.getHeader().getCreated());

		stream.println("header: ");
		for (Root.Header.Agent agent : root.getHeader().getAgents()) {
			stream.println("\tagent: type (" + agent.getType() + " -> " + agent.getOtherType() + ") -> role ("
					+ agent.getRole() + " -> " + agent.getOtherRole() + ") -> name " + agent.getName());

			for (Root.Header.Agent.Note note : agent.getNotes())
				stream.println("\t\tnote: " + note.getOption() + " -> " + note.getValue());

		}

		stream.println("file groups: ");
		for (Root.FileGroup fileGroup : root.getFileGroups()) {
			stream.println("\tgroup: " + fileGroup.getId());

			for (Root.FileGroup.File file : fileGroup.getFiles()) {
				stream.println("\t\tfile: " + file.getId() + " -> " + file.getMimeType() + " -> location ("
						+ file.getLocation().getType() + " -> " + file.getLocation().getOtherType() + " -> "
						+ file.getLocation().getPath() + ")");
			}
		}

		stream.println("structure map: " + root.getStructureMap().getType() + " -> "
				+ root.getStructureMap().getPhysicalSequence().getType());
		for (Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap().getPhysicalSequence().getPages()) {
			stream.println("\t" + page.getType() + ": " + page.getId());

			for (Root.StructureMap.PhysicalSequence.Page.FileId fileId : page.getFileIds())
				stream.println("\t\tid: " + fileId.getId());
		}
	}

	/**
	 * The main method that deserialise mets XML files and print them to the
	 * "standard" output stream.
	 * 
	 * @param args The mets XML files.
	 * @since 1.8
	 */
	public static void main(String[] args) {
		int number = 0;
		for (String file : args)
			if (args.length == 1) {
				if (number > 0)
					System.out.println();

				System.out.println("begin #" + (++number) + ": " + file);
				try {
					print(System.out, (new MetsParser()).deserialise(new File(file)));
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("end #" + number + ": " + file);
			}
	}

}
