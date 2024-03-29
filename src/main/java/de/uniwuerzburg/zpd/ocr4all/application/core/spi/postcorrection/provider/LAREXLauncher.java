/**
 * File:     LAREXLauncher.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     2ß.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser;
import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser.Root.FileGroup.File;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProviderWorker;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.spi.PostcorrectionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.BooleanField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.StringField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.BooleanArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.StringArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.MetsUtils;

/**
 * Defines service providers for LAREX launchers.
 * 
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class LAREXLauncher extends CoreServiceProviderWorker implements PostcorrectionServiceProvider {
	/**
	 * The prefix of the message keys in the resource bundle.
	 */
	private static final String messageKeyPrefix = "postcorrection.larex.launcher.";

	/**
	 * The service provider identifier.
	 */
	private static final String identifier = "ocr4all-LAREX-launcher";

	/**
	 * Defines templates.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Template {
		mets_agent("mets-agent"), mets_file_group("mets-file-group"), mets_file("mets-file"), mets_page("mets-page");

		/**
		 * The folder.
		 */
		private static final String folder = "templates/spi/postcorrection/larex-launcher/";

		/**
		 * The suffix.
		 */
		private static final String suffix = ".template";

		/**
		 * The name.
		 */
		private final String name;

		/**
		 * Creates a template.
		 * 
		 * @param name The name.
		 * @since 1.8
		 */
		private Template(String name) {
			this.name = name;
		}

		/**
		 * Returns the resource name.
		 * 
		 * @return The resource name.
		 * @since 1.8
		 */
		public String getResourceName() {
			return folder + name + suffix;
		}

		/**
		 * Returns the template content.
		 * 
		 * @return The template content
		 * @throws IllegalArgumentException Throws if the resource could not be found.
		 * @throws IOException              If an I/O error occurs.
		 * @since 1.8
		 */
		public String getResourceAsText() throws IllegalArgumentException, IOException {
			return OCR4allUtils.getResourceAsText(getResourceName());
		}
	}

	/**
	 * Define patterns for mets templates.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum MetsPattern {
		other_role, software_name, input_file_group, output_file_group, parameter,

		file_group, file_template,

		file_id, file_mime_type, file_name,

		page_id;

		/**
		 * The other role value.
		 */
		private static final String otherRoleValue = "postcorrection/larex";

		/**
		 * Returns the pattern.
		 * 
		 * @return The pattern.
		 * @since 1.8
		 */
		public String getPattern() {
			return "[ocr4all-" + name() + "]";
		}
	}

	/**
	 * Defines mets xml tags.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum MetsTag {
		header("metsHdr"),

		fileSection("fileSec"),

		structureMap("structMap"), physicalSequence(false, "div"), pages(false, "div"), fileId(false, "fptr");

		/**
		 * True if it is a main tag.
		 */
		private final boolean isMainTag;

		/**
		 * The name.
		 */
		private final String name;

		/**
		 * Creates a main mets xml tag.
		 * 
		 * @param name The name.
		 * @since 1.8
		 */
		private MetsTag(String name) {
			this.isMainTag = true;
			this.name = name;
		}

		/**
		 * Creates a mets xml tag.
		 * 
		 * @param isMainTag True if it is a main tag.
		 * @param name      The name.
		 * @since 1.8
		 */
		private MetsTag(boolean isMainTag, String name) {
			this.isMainTag = isMainTag;
			this.name = name;
		}

		/**
		 * Returns true if it is a main tag.
		 *
		 * @return True if it is a main tag.
		 * @since 1.8
		 */
		public boolean isMainTag() {
			return isMainTag;
		}

		/**
		 * Returns the tag.
		 * 
		 * @return The tag.
		 * @since 1.8
		 */
		public String getTag() {
			return "mets:" + name;
		}

		/**
		 * Returns true if the given line contains the open tag.
		 * 
		 * @param line The line.
		 * @return True if the given line contains the open tag.
		 * @since 1.8
		 */
		public boolean isOpenTag(String line) {
			return line != null && line.contains("<" + getTag());
		}

		/**
		 * Returns true if the given line contains the close tag.
		 * 
		 * @param line The line.
		 * @return True if the given line contains the close tag.
		 * @since 1.8
		 */
		public boolean isCloseTag(String line) {
			return line != null && line.contains("</" + getTag() + ">");
		}

		/**
		 * Returns the main mets xml tag in the given line.
		 * 
		 * @param isOpen True if it is an open tag. Otherwise it is a close tag.
		 * @param line   The line to search for the tag.
		 * @return The main mets xml tag. Null if no tag is found.
		 * @since 1.8
		 */
		public static MetsTag getMainOpenTag(String line) {
			if (line != null)
				for (MetsTag tag : MetsTag.values())
					if (tag.isMainTag() && tag.isOpenTag(line))
						return tag;

			return null;
		}
	}

	/**
	 * Defines fields.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Field {
		mimeTypeFilter("mime-type-filter"), copyImages("copy-images");

		/**
		 * The name.
		 */
		private final String name;

		/**
		 * Creates a model argument.
		 * 
		 * @param name The name.
		 * @since 1.8
		 */
		private Field() {
			this.name = this.name();
		}

		/**
		 * Creates a model argument.
		 * 
		 * @param name The name.
		 * @since 1.8
		 */
		private Field(String name) {
			this.name = name;
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

	}

	/**
	 * Default constructor for a service provider for LAREX launcher.
	 * 
	 * @since 1.8
	 */
	public LAREXLauncher() {
		super(messageKeyPrefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.ServiceProvider#
	 * getName(java.util.Locale)
	 */
	@Override
	public String getName(Locale locale) {
		return getString(locale, "name");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.ServiceProvider#
	 * getVersion()
	 */
	@Override
	public float getVersion() {
		return 1.0F;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.ServiceProvider#
	 * getDescription(java.util.Locale)
	 */
	@Override
	public Optional<String> getDescription(Locale locale) {
		return Optional.of(getString(locale, "description"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#
	 * getCategories()
	 */
	@Override
	public List<String> getCategories() {
		return Arrays.asList("Post-correction");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getSteps()
	 */
	@Override
	public List<String> getSteps() {
		return Arrays.asList("postcorrection/ground truth");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.ServiceProvider#
	 * getIcon()
	 */
	@Override
	public Optional<String> getIcon() {
		return Optional.of("far fa-images");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.ServiceProvider#
	 * getIndex()
	 */
	@Override
	public int getIndex() {
		return 100;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getPremise(
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target)
	 */
	@Override
	public Premise getPremise(Target target) {
		return new Premise();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getModel(de.
	 * uniwuerzburg.zpd.ocr4all.application.spi.env.Target)
	 */
	@Override
	public Model getModel(Target target) {
		LauncherArgument argument = new LauncherArgument();

		return new Model(
				new StringField(Field.mimeTypeFilter.getName(), argument.getMimeTypeFilter(),
						locale -> getString(locale, "mime.type.filter.description"),
						locale -> getString(locale, "mime.type.filter.placeholder")),
				new BooleanField(Field.copyImages.getName(), argument.isCopyImages(),
						locale -> getString(locale, "copy.images"),
						locale -> getString(locale, "copy.images.description"), false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.
	 * ProcessServiceProvider#newProcessor()
	 */
	@Override
	public ProcessServiceProvider.Processor newProcessor() {
		return new CoreProcessorServiceProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * de.uniwuerzburg.zpd.ocr4all.application.spi.ProcessServiceProvider.Processor#
			 * execute(de.uniwuerzburg.zpd.ocr4all.application.spi.ProcessServiceProvider.
			 * Processor.Callback, de.uniwuerzburg.zpd.ocr4all.application.spi.Framework,
			 * de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument)
			 */
			@Override
			public State execute(Callback callback, Framework framework, ModelArgument modelArgument) {
				if (!initialize(identifier, callback, framework))
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Loads core data
				 */

				// mime type argument
				updatedStandardOutput("Parse parameter.");

				LauncherArgument argument = new LauncherArgument();
				try {
					StringArgument mimeTypeArgument = modelArgument.getArgument(StringArgument.class,
							Field.mimeTypeFilter.getName());

					if (mimeTypeArgument != null)
						argument.setMimeTypeFilter(mimeTypeArgument.getValue().orElse(null));
				} catch (ClassCastException e) {
					updatedStandardError(
							"The argument '" + Field.mimeTypeFilter.getName() + "' is not of string type.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// copy images argument
				try {
					final BooleanArgument copyImagesArgument = modelArgument.getArgument(BooleanArgument.class,
							Field.copyImages.getName());

					if (copyImagesArgument != null && copyImagesArgument.getValue().isPresent())
						argument.setCopyImages(copyImagesArgument.getValue().get());
				} catch (ClassCastException e) {
					updatedStandardError("The argument '" + Field.copyImages.getName() + "' is not of boolean type.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				// mets file
				updatedStandardOutput("Load mets xml file.");

				final Path metsPath = framework.getMets();
				if (metsPath == null) {
					updatedStandardError("Missed required mets file path.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final String metsGroup = framework.getMetsGroup();
				if (metsGroup == null) {
					updatedStandardError("Missed required mets file group.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final MetsUtils.FrameworkFileGroup metsFrameworkFileGroup = MetsUtils.getFileGroup(framework);
				final MetsParser.Root root;

				// parse mets file
				if (Files.exists(metsPath))
					try {
						root = (new MetsParser()).deserialise(metsPath.toFile());
					} catch (Exception e) {
						updatedStandardError("Could not parse the mets file - " + e.getMessage());

						return ProcessServiceProvider.Processor.State.interrupted;
					}
				else {
					updatedStandardError("The mets file is not available.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// mets templates
				final String metsAgentTemplate;
				try {
					metsAgentTemplate = Template.mets_agent.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError("Internal error: missed mets agent resource '"
							+ Template.mets_agent.getResourceName() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final String metsFileGroupTemplate;
				try {
					metsFileGroupTemplate = Template.mets_file_group.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError("Internal error: missed mets file group resource '"
							+ Template.mets_file_group.getResourceName() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final String metsFileTemplate;
				try {
					metsFileTemplate = Template.mets_file.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed mets file resource '" + Template.mets_file.getResourceName() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final String metsPageTemplate;
				try {
					metsPageTemplate = Template.mets_page.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed page file resource '" + Template.mets_page.getResourceName() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				callback.updatedProgress(0.05F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Select required files and copy then to temporary directory
				 */

				// search for input file group
				MetsParser.Root.FileGroup inputFileGroup = null;
				for (MetsParser.Root.FileGroup fileGroup : root.getFileGroups())
					if (metsFrameworkFileGroup.getInput().equals(fileGroup.getId())) {
						inputFileGroup = fileGroup;

						break;
					}

				if (inputFileGroup == null) {
					updatedStandardError("The required mets input file group '" + metsFrameworkFileGroup.getInput()
							+ "' is not available.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				final Path processorWorkspace = framework.getProcessorWorkspace();

				List<LarexFile> larexFiles = new ArrayList<>();
				int index = 0;
				final String mimeTypeFilter = argument.getMimeTypeFilter() == null
						|| argument.getMimeTypeFilter().isEmpty() ? null : argument.getMimeTypeFilter();
				for (MetsParser.Root.FileGroup.File file : inputFileGroup.getFiles()) {
					callback.updatedProgress(0.05F + (0.75F * (++index) / inputFileGroup.getFiles().size()));

					if (mimeTypeFilter == null || mimeTypeFilter.equals(file.getMimeType())) {
						if (!file.getId().startsWith(metsFrameworkFileGroup.getInput())) {
							updatedStandardError("Wrong input file id '" + file.getId()
									+ "', since it is not a prefix of file group id '"
									+ metsFrameworkFileGroup.getInput() + "'.");

							return ProcessServiceProvider.Processor.State.interrupted;
						}

						final Path inputFile = Paths.get(processorWorkspace.toString(), file.getLocation().getPath());
						if (!Files.exists(inputFile) || Files.isDirectory(inputFile)) {
							updatedStandardError(
									"The required input file '" + inputFile.toString() + "' is not available.");

							return ProcessServiceProvider.Processor.State.interrupted;
						}

						LarexFile larexFile = new LarexFile(metsFrameworkFileGroup, file);

						try {
							Files.copy(inputFile,
									Paths.get(framework.getTemporary().toString(),
											larexFile.getXmlContainer().getTargetFilename()),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							updatedStandardError("Cannot copy the required input file '" + inputFile.toString()
									+ "' to temporary directory - " + e.getMessage() + ".");

							return ProcessServiceProvider.Processor.State.interrupted;
						}

						larexFiles.add(larexFile);
					}
				}

				if (argument.isCopyImages()) {
					setImages(framework, metsFrameworkFileGroup, root, larexFiles);

					for (LarexFile larexFile : larexFiles)
						if (larexFile.isImageContainerSet()) {
							final Path inputFile = Paths.get(processorWorkspace.toString(),
									larexFile.getImageContainer().getSourceFile().getLocation().getPath());

							if (!Files.exists(inputFile) || Files.isDirectory(inputFile)) {
								updatedStandardError(
										"The required input image '" + inputFile.toString() + "' is not available.");

								return ProcessServiceProvider.Processor.State.interrupted;
							}

							try {
								Files.copy(inputFile,
										Paths.get(framework.getTemporary().toString(),
												larexFile.getImageContainer().getTargetFilename()),
										StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								updatedStandardError("Cannot copy the required input image '" + inputFile.toString()
										+ "' to temporary directory - " + e.getMessage() + ".");

								return ProcessServiceProvider.Processor.State.interrupted;
							}

						}
				}

				callback.updatedProgress(0.80F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Moves the images to snapshot
				 */
				try {
					updatedStandardOutput(
							"Move the images to sandbox snapshot " + framework.getOutput().toString() + ".");

					for (LarexFile larexFile : larexFiles) {
						Files.move(
								Paths.get(framework.getTemporary().toString(),
										larexFile.getXmlContainer().getTargetFilename()),
								Paths.get(framework.getOutput().toString(),
										larexFile.getXmlContainer().getTargetFilename()),
								StandardCopyOption.REPLACE_EXISTING);

						if (larexFile.isImageContainerSet())
							Files.move(
									Paths.get(framework.getTemporary().toString(),
											larexFile.getImageContainer().getTargetFilename()),
									Paths.get(framework.getOutput().toString(),
											larexFile.getImageContainer().getTargetFilename()),
									StandardCopyOption.REPLACE_EXISTING);
					}

				} catch (Exception e) {
					updatedStandardError("Can not move files to sandbox - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				callback.updatedProgress(0.95F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Updates the mets file
				 */
				try {
					updatedStandardOutput("Update mets xml file.");

					// build mets sections
					final StringBuffer metsFileBuffer = new StringBuffer();
					final String sandboxRelativePath = framework.getOutputRelativeProcessorWorkspace().toString();
					final Hashtable<String, List<String>> targetPages = new Hashtable<>();
					for (LarexFile larexFile : larexFiles) {
						final LarexFile.Container xmlContainer = larexFile.getXmlContainer();

						List<String> targetPage = new ArrayList<>();
						targetPage.add(xmlContainer.getTargetFileID());
						targetPages.put(xmlContainer.getSourceFile().getId(), targetPage);

						metsFileBuffer.append(metsFileTemplate
								.replace(MetsPattern.file_id.getPattern(), xmlContainer.getTargetFileID())

								.replace(MetsPattern.file_mime_type.getPattern(),
										xmlContainer.getSourceFile().getMimeType())

								.replace(MetsPattern.file_name.getPattern(),
										sandboxRelativePath + "/" + xmlContainer.getTargetFilename()));

						if (larexFile.isImageContainerSet()) {
							final LarexFile.Container imageContainer = larexFile.getImageContainer();
							targetPage.add(imageContainer.getTargetFileID());

							metsFileBuffer.append(metsFileTemplate
									.replace(MetsPattern.file_id.getPattern(), imageContainer.getTargetFileID())

									.replace(MetsPattern.file_mime_type.getPattern(),
											imageContainer.getSourceFile().getMimeType())

									.replace(MetsPattern.file_name.getPattern(),
											sandboxRelativePath + "/" + imageContainer.getTargetFilename()));
						}
					}

					// update mets file
					final StringBuffer buffer = new StringBuffer();
					MetsTag handlingTag = null;
					final Set<MetsTag> handledMainTags = new HashSet<>();
					final List<String> pageFileIds = new ArrayList<>();
					for (String line : Files.readAllLines(metsPath)) {
						if (handlingTag == null) {
							handlingTag = MetsTag.getMainOpenTag(line);

							if (handlingTag != null && !handledMainTags.add(handlingTag)) {
								updatedStandardError("Duplicated main Mets XML tag '" + handlingTag.getTag()
										+ "', file '" + metsPath.toString() + ".");

								return ProcessServiceProvider.Processor.State.interrupted;
							}
						} else {
							final boolean isCloseTag = handlingTag.isCloseTag(line);

							switch (handlingTag) {
							case header:
								if (isCloseTag) {
									buffer.append(metsAgentTemplate
											.replace(MetsPattern.other_role.getPattern(), MetsPattern.otherRoleValue)
											.replace(MetsPattern.software_name.getPattern(),
													identifier + " v" + getVersion())
											.replace(MetsPattern.input_file_group.getPattern(),
													metsFrameworkFileGroup.getInput())
											.replace(MetsPattern.output_file_group.getPattern(),
													metsFrameworkFileGroup.getOutput())
											.replace(MetsPattern.parameter.getPattern(),
													objectMapper.writeValueAsString(argument)));

									handlingTag = null;
								}

								break;
							case fileSection:
								if (isCloseTag) {
									buffer.append(metsFileGroupTemplate
											.replace(MetsPattern.file_group.getPattern(),
													metsFrameworkFileGroup.getOutput())
											.replace(MetsPattern.file_template.getPattern(),
													metsFileBuffer.toString()));

									handlingTag = null;
								}

								break;
							case structureMap:
								if (isCloseTag)
									handlingTag = null;
								else if (MetsTag.physicalSequence.isOpenTag(line))
									handlingTag = MetsTag.physicalSequence;

								break;
							case physicalSequence:
								if (isCloseTag)
									handlingTag = null;
								else if (MetsTag.pages.isOpenTag(line))
									handlingTag = MetsTag.pages;

								break;
							case pages:
								if (isCloseTag) {
									for (String fileGroup : pageFileIds)
										buffer.append(
												metsPageTemplate.replace(MetsPattern.file_id.getPattern(), fileGroup));

									pageFileIds.clear();
									handlingTag = MetsTag.physicalSequence;
								} else if (MetsTag.fileId.isOpenTag(line))
									for (String fileGroup : targetPages.keySet())
										if (line.contains("FILEID=\"" + fileGroup + "\""))
											pageFileIds.addAll(targetPages.get(fileGroup));

								break;
							case fileId:
							default:
								break;
							}
						}

						buffer.append(line + "\n");
					}

					// persist mets file
					Files.write(metsPath, buffer.toString().getBytes());
				} catch (Exception e) {
					e.printStackTrace();
					updatedStandardError("Can not update mets file - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				callback.lockSnapshot(
						(framework.isUserSet() ? framework.getUser() + " has locked the snapshot" : "Snapshot locked")
								+ " for LAREX post-correction.");

				/*
				 * Ends the process
				 */
				updatedStandardOutput((larexFiles.isEmpty() ? "No file"
						: (larexFiles.size() == 1 ? "One file" : larexFiles.size() + " files")) + " available.");

				return complete();
			}
		};
	}

	/**
	 * Set the images for the LAREX files.
	 * 
	 * @param framework              The framework for the processor.
	 * @param metsFrameworkFileGroup The mets file group for the framework.
	 * @param root                   The mets deserialised root object.
	 * @param larexFiles             The LAREX files.
	 * @since 1.8
	 */
	private void setImages(Framework framework, MetsUtils.FrameworkFileGroup metsFrameworkFileGroup,
			MetsParser.Root root, List<LarexFile> larexFiles) {
		// mets pages
		final List<Set<String>> pages = new ArrayList<>();

		for (MetsParser.Root.StructureMap.PhysicalSequence.Page page : root.getStructureMap().getPhysicalSequence()
				.getPages())
			try {
				Set<String> fieldIds = new HashSet<>();
				for (MetsParser.Root.StructureMap.PhysicalSequence.Page.FileId fieldId : page.getFileIds())
					fieldIds.add(fieldId.getId());

				pages.add(fieldIds);
			} catch (Exception e) {
				// Ignore malformed mets page
			}

		final Hashtable<String, Hashtable<String, MetsParser.Root.FileGroup.File>> fileGroups = new Hashtable<>();
		for (MetsParser.Root.FileGroup fileGroup : root.getFileGroups()) {
			Hashtable<String, MetsParser.Root.FileGroup.File> files = new Hashtable<>();
			for (MetsParser.Root.FileGroup.File file : fileGroup.getFiles())
				files.put(file.getId(), file);

			fileGroups.put(fileGroup.getId(), files);
		}

		for (LarexFile larexFile : larexFiles) {
			final LarexFile.Container xmlContainer = larexFile.getXmlContainer();

			/*
			 * Search the page.
			 */
			Set<String> page = null;
			for (Set<String> search : pages)
				if (search.remove(xmlContainer.getSourceFile().getId())) {
					page = search;

					break;
				}

			if (page != null) {
				LarexImage larexImage = getImage(metsFrameworkFileGroup, fileGroups, page,
						new ArrayList<>(framework.getTarget().getSandbox().getSnapshotTrack()));

				if (larexImage != null)
					larexFile.setImageContainer(larexImage.getSnapshotTrack(), larexImage.getFile());
			}
		}
	}

	/**
	 * Search for the Larex image.
	 * 
	 * @param metsFrameworkFileGroup The framework for the processor.
	 * @param fileGroups             The mets file groups.
	 * @param page                   The mets page.
	 * @param snapshotTrack          The snapshot track.
	 * @return The Larex image. Null if no image was found.
	 * @since 1.8
	 */
	private LarexImage getImage(MetsUtils.FrameworkFileGroup metsFrameworkFileGroup,
			Hashtable<String, Hashtable<String, MetsParser.Root.FileGroup.File>> fileGroups, Set<String> page,
			List<Integer> snapshotTrack) {

		Hashtable<String, MetsParser.Root.FileGroup.File> fileGroup = fileGroups
				.get(metsFrameworkFileGroup.getFileGroup(snapshotTrack));
		if (fileGroup != null)
			for (String fileId : page)
				if (fileGroup.containsKey(fileId) && fileGroup.get(fileId).isMimeTypeImage())
					return new LarexImage(snapshotTrack, fileGroup.get(fileId));

		// Search recursively if not found in fileGroups
		if (snapshotTrack.isEmpty())
			return null;
		else {
			snapshotTrack.remove(snapshotTrack.size() - 1);

			return getImage(metsFrameworkFileGroup, fileGroups, page, snapshotTrack);
		}

	}

	/**
	 * LarexImage is an immutable class that defines larex images.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private static class LarexImage {
		/**
		 * The snapshot track.
		 */
		private final List<Integer> snapshotTrack;

		/**
		 * The mets file.
		 */
		private final MetsParser.Root.FileGroup.File file;

		/**
		 * Creates a larex image.
		 * 
		 * @param snapshotTrack The snapshot track.
		 * @param file          The mets file.
		 * @since 1.8
		 */
		public LarexImage(List<Integer> snapshotTrack, File file) {
			super();

			this.snapshotTrack = snapshotTrack;
			this.file = file;
		}

		/**
		 * Returns the snapshot track.
		 *
		 * @return The snapshot track.
		 * @since 1.8
		 */
		public List<Integer> getSnapshotTrack() {
			return snapshotTrack;
		}

		/**
		 * Returns the mets file.
		 *
		 * @return The mets file.
		 * @since 1.8
		 */
		public MetsParser.Root.FileGroup.File getFile() {
			return file;
		}

	}

	/**
	 * Defines launcher arguments with default values.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class LauncherArgument {
		/**
		 * The default page xml mime type.
		 */
		private static final String pageXmlMimeType = "application/vnd.prima.page+xml";

		/**
		 * The mime type filter.
		 */
		@JsonProperty("mime-type-filter")
		private String mimeTypeFilter = pageXmlMimeType;

		/**
		 * True if copy the images.
		 */
		@JsonProperty("copy-images")
		private boolean isCopyImages = true;

		/**
		 * Returns the mime type filter.
		 *
		 * @return The mime type filter.
		 * @since 1.8
		 */
		public String getMimeTypeFilter() {
			return mimeTypeFilter;
		}

		/**
		 * Set the mime type filter.
		 *
		 * @param mimeTypeFilter The mime type filter to set.
		 * @since 1.8
		 */
		public void setMimeTypeFilter(String mimeTypeFilter) {
			this.mimeTypeFilter = mimeTypeFilter == null ? null : mimeTypeFilter.trim();
		}

		/**
		 * Returns true if copy the images.
		 *
		 * @return True if copy the images.
		 * @since 1.8
		 */
		@JsonGetter("copy-images")
		public boolean isCopyImages() {
			return isCopyImages;
		}

		/**
		 * Set to true if copy the images.
		 *
		 * @param isCopyImages The copy flag to set.
		 * @since 1.8
		 */
		public void setCopyImages(boolean isCopyImages) {
			this.isCopyImages = isCopyImages;
		}

	}

	/**
	 * LarexFile is an immutable class that defines LAREX files.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private static class LarexFile {
		/**
		 * The container type.
		 */
		private enum ContainerType {
			xml, image
		}

		/**
		 * The next file index.
		 */
		private static int nextFileIndex = 0;

		/**
		 * The mets framework file group.
		 */
		private final MetsUtils.FrameworkFileGroup metsFrameworkFileGroup;

		/**
		 * The xml container.
		 */
		private final Container xmlContainer;

		/**
		 * The image container.
		 */
		private Container imageContainer = null;

		/**
		 * Creates a LAREX file.
		 * 
		 * @param metsFrameworkFileGroup The mets framework file group.
		 * @param sourceFile             The source mets file.
		 * @since 1.8
		 */
		public LarexFile(MetsUtils.FrameworkFileGroup metsFrameworkFileGroup,
				MetsParser.Root.FileGroup.File sourceFile) {
			super();
			this.metsFrameworkFileGroup = metsFrameworkFileGroup;

			xmlContainer = new Container(ContainerType.xml, sourceFile, null, this.metsFrameworkFileGroup.getInput(),
					this.metsFrameworkFileGroup.getOutput());
		}

		/**
		 * Returns the xml container.
		 *
		 * @return The xml container.
		 * @since 1.8
		 */
		public Container getXmlContainer() {
			return xmlContainer;
		}

		/**
		 * Returns true if the image container is set.
		 *
		 * @return True if the image container is set.
		 * @since 1.8
		 */
		public boolean isImageContainerSet() {
			return imageContainer != null;
		}

		/**
		 * Returns the image container.
		 *
		 * @return The image container.
		 * @since 1.8
		 */
		public Container getImageContainer() {
			return imageContainer;
		}

		/**
		 * Set the image container.
		 *
		 * @param imageContainer The image container to set.
		 * @since 1.8
		 */
		public void setImageContainer(List<Integer> track, MetsParser.Root.FileGroup.File sourceFile) {
			imageContainer = new Container(ContainerType.image, sourceFile, xmlContainer.getTargetFileCoreID(),
					metsFrameworkFileGroup.getFileGroup(track), metsFrameworkFileGroup.getOutput());
		}

		/**
		 * Container is an immutable class that defines containers.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public class Container {
			/**
			 * The file index.
			 */
			private final int fileIndex;

			/**
			 * The container type.
			 */
			private final ContainerType type;

			/**
			 * The source mets file.
			 */
			private final MetsParser.Root.FileGroup.File sourceFile;

			/**
			 * The target file core id.
			 */
			private final String targetFileCoreID;

			/**
			 * The target file name.
			 */
			private final String targetFilename;

			/**
			 * Creates a container.
			 * 
			 * @param type             The container type.
			 * @param sourceFile       The source mets file.
			 * @param targetFileCoreID The source mets file.
			 * @param targetFileCoreID The target file id.
			 * @param targetFilename   The target file name.
			 * @since 1.8
			 */
			private Container(ContainerType type, MetsParser.Root.FileGroup.File sourceFile, String targetFileCoreID,
					String inputFileGroup, String outputFileGroup) {
				super();

				this.fileIndex = ++nextFileIndex;
				this.type = type;
				this.sourceFile = sourceFile;

				this.targetFileCoreID = targetFileCoreID != null ? targetFileCoreID
						: outputFileGroup + (sourceFile.getId().startsWith(inputFileGroup)
								? sourceFile.getId().substring(inputFileGroup.length())
								: "_" + fileIndex);

				final String sourceFilename = Paths.get(sourceFile.getLocation().getPath()).getFileName().toString();
				targetFilename = sourceFilename.startsWith(inputFileGroup)
						? outputFileGroup + sourceFilename.substring(inputFileGroup.length())
						: sourceFilename;
			}

			/**
			 * Returns the source mets file.
			 *
			 * @return The source mets file.
			 * @since 1.8
			 */
			public MetsParser.Root.FileGroup.File getSourceFile() {
				return sourceFile;
			}

			/**
			 * Returns the target file core id.
			 *
			 * @return The target file core id.
			 * @since 1.8
			 */
			public String getTargetFileCoreID() {
				return targetFileCoreID;
			}

			/**
			 * Returns the target file id.
			 *
			 * @return The target file id.
			 * @since 1.8
			 */
			public String getTargetFileID() {
				return targetFileCoreID + "_" + type.name();
			}

			/**
			 * Returns the target file name.
			 * 
			 * @return The target file name.
			 * @since 1.8
			 */
			public String getTargetFilename() {
				return targetFilename;
			}
		}
	}
}
