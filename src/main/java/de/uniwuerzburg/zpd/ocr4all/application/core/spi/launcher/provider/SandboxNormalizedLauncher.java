/**
 * File:     SandboxNormalizedLauncher.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.07.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider;

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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider.SandboxFolioLauncher.LauncherArgumentMethodThreshold;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ProcessFramework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.SystemCommand;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.ImageField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ImageArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.mets.MetsUtils;

/**
 * Defines service providers for sandbox normalized launchers.
 * 
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class SandboxNormalizedLauncher extends SandboxCoreLauncher {
	/**
	 * The service provider identifier.
	 */
	private static final String identifier = "ocr4all-sandbox-normalized-launcher";

	/**
	 * Defines fields.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Field {
		images;

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
	 * Default constructor for a service provider for sandbox normalized launcher.
	 * 
	 * @since 1.8
	 */
	public SandboxNormalizedLauncher() {
		super(messageKeyPrefix, identifier);
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
		return getString(locale, "name.normalized");
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
		return Optional.of(getString(locale, "description.normalized"));
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
		return target.getSandbox() == null ? null
				: (target.getSandbox().isLaunched()
						? new Premise(Premise.State.block, locale -> getString(locale, "already.launched"))
						: new Premise());
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
		return new Model(new ImageField(Field.images.getName(), locale -> getString(locale, "image.selection")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.spi.provider.
	 * ProcessServiceProvider#newProcessor()
	 */
	@Override
	public ProcessorServiceProvider.Processor<ProcessorCore.LockSnapshotCallback, ProcessFramework> newProcessor() {
		return new CoreProcessorServiceProvider<ProcessorCore.LockSnapshotCallback, ProcessFramework>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.ProcessorServiceProvider.
			 * Processor#
			 * execute(de.uniwuerzburg.zpd.ocr4all.application.spi.ProcessServiceProvider.
			 * Processor.Callback, de.uniwuerzburg.zpd.ocr4all.application.spi.Framework,
			 * de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument)
			 */
			@Override
			public State execute(LockSnapshotCallback callback, ProcessFramework framework,
					ModelArgument modelArgument) {
				if (!initialize(identifier, callback, framework))
					return ProcessorServiceProvider.Processor.State.canceled;

				/*
				 * Loads core data
				 */

				// Processor workspace path
				final Path processorWorkspaceRelativePath = framework.getOutputRelativeProcessorWorkspace();
				if (processorWorkspaceRelativePath == null) {
					updatedStandardError("Internal error: inconsistent framework processor workspace path.");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				// mets
				final Path metsPath = framework.getMets();
				if (metsPath == null) {
					updatedStandardError("Missed required mets file path.");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				final String metsGroup = framework.getMetsGroup();
				if (metsGroup == null) {
					updatedStandardError("Missed required mets file group.");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				final String metsCoreTemplate;
				try {
					metsCoreTemplate = Template.mets_core.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed mets core resource '" + Template.mets_core.getResourceName() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				final String metsFileTemplate;
				try {
					metsFileTemplate = Template.mets_file.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed mets file resource '" + Template.mets_file.getResourceName() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				final String metsPageTemplate;
				try {
					metsPageTemplate = Template.mets_page.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed page file resource '" + Template.mets_page.getResourceName() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				// Convert system command
				String convertCommand = configuration.getSystemCommand(SystemCommand.Type.convert).getCommand()
						.toString();

				if (isCanceled())
					return ProcessorServiceProvider.Processor.State.canceled;

				/*
				 * Available arguments
				 */
				Set<String> availableArguments = modelArgument.getArgumentNames();

				updatedStandardOutput("Parse parameters.");

				/*
				 * Launcher arguments
				 */
				LauncherArgument launcherArgument = new LauncherArgument();

				// Images for the launcher
				if (availableArguments.remove(Field.images.getName()))
					try {
						final ImageArgument argument = modelArgument.getArgument(ImageArgument.class,
								Field.images.getName());

						if (argument.getValues().isPresent())
							launcherArgument.setImages(argument.getValues().get());
					} catch (ClassCastException e) {
						updatedStandardError("The argument '" + Field.images.getName() + "' is not of image type.");

						return ProcessorServiceProvider.Processor.State.interrupted;
					}

				try {
					updatedStandardOutput(
							"Using parameters " + objectMapper.writeValueAsString(launcherArgument) + ".");
				} catch (JsonProcessingException ex) {
					updatedStandardError("Troubles creating JSON from parameters - " + ex.getMessage() + ".");
				}

				if (!availableArguments.isEmpty())
					updatedStandardOutput("Ignored unnecessary parameters: " + availableArguments + ".");

				/*
				 * Read project images
				 */
				updatedStandardOutput("Load project images.");

				Hashtable<String, Folio> folios = new Hashtable<>();
				try {
					for (Folio folio : (new PersistenceManager(framework.getTarget().getProject().getFolio(),
							Type.folio_v1)).getEntities(Folio.class))
						folios.put(folio.getId(), folio);
				} catch (Exception e) {
					updatedStandardError("Cannot read project folios - " + e.getMessage() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				if (folios.isEmpty()) {
					updatedStandardOutput("There are no project images for sandbox launcher.");

					callback.updatedProgress(1);
					return ProcessorServiceProvider.Processor.State.completed;
				}

				// The images
				updatedStandardOutput("Determine sandbox launcher images.");

				Set<String> images = new HashSet<>();
				for (String id : launcherArgument.getImages())
					if (folios.containsKey(id))
						images.add(id);

				if (images.isEmpty()) {
					updatedStandardOutput("There are no images for sandbox launcher.");

					// Persist mets file
					try {
						persistMets(metsPath, metsCoreTemplate, objectMapper.writeValueAsString(launcherArgument),
								metsGroup, "", "");
					} catch (Exception e) {
						updatedStandardError("Can not create mets file - " + e.getMessage() + ".");

						return ProcessorServiceProvider.Processor.State.interrupted;
					}

					callback.updatedProgress(1);
					return ProcessorServiceProvider.Processor.State.completed;
				}

				if (isCanceled())
					return ProcessorServiceProvider.Processor.State.canceled;

				// TODO: continue
				/*
				 * Process images in temporary directories
				 */
				updatedStandardOutput(
						"Process images in temporary directory (" + framework.getTemporary().toString() + ").");

				Path folderProjectFolios = Paths.get(framework.getTemporary().toString(), "project");
				Path folderSandboxFolios = Paths.get(framework.getTemporary().toString(), "sandbox");
				try {
					Files.createDirectory(folderProjectFolios);
					Files.createDirectory(folderSandboxFolios);
				} catch (IOException e) {
					updatedStandardError("could not create required temporary directory - " + e.getMessage() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				// Copy required project images to temporary directory
				int index = 0;
				for (String id : images) {
					callback.updatedProgress(0.35F * (++index) / images.size());

					String fileName = id + "." + ImageFormat.getImageFormat(folios.get(id).getFormat()).name();
					Path projectFolio = Paths.get(framework.getTarget().getProject().getImages().getFolios().toString(),
							fileName);

					if (!Files.exists(projectFolio) || Files.isDirectory(projectFolio))
						continue;

					try {
						Files.copy(projectFolio, Paths.get(folderProjectFolios.toString(), fileName),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						updatedStandardError("Cannot copy the project folio '" + projectFolio.toString() + "' - "
								+ e.getMessage() + ".");

						return ProcessorServiceProvider.Processor.State.interrupted;
					}
				}

				callback.updatedProgress(0.35F);

				if (isCanceled())
					return ProcessorServiceProvider.Processor.State.canceled;

				try {
					List<String> arguments = new ArrayList<>(
							Arrays.asList("REPLACE_IMAGE_NAME", "-format", imageFormat.name()));

					switch (method) {
					case monochrome:
						arguments.add("-monochrome");

						break;
					case threshold:
						arguments.addAll(Arrays.asList("-threshold",
								((LauncherArgumentMethodThreshold) launcherArgument).getMethodThreshold() + "%"));

						break;
					case colorSpace:
						arguments.addAll(Arrays.asList("-colorspace", "Gray"));

						break;
					case channel:
						arguments.add("-separate");

						break;
					case combine:
						arguments.addAll(Arrays.asList("-set", "colorspace", "Gray", "-separate"));

						break;
					case none:
						break;
					}

					if (launcherArgument.getImageMaximumWidth() > 0 || launcherArgument.getImageMaximumHeight() > 0)
						arguments.addAll(Arrays.asList("-resize",
								(launcherArgument.getImageMaximumWidth() > 0
										? "" + launcherArgument.getImageMaximumWidth()
										: "")
										+ "x"
										+ (launcherArgument.getImageMaximumHeight() > 0
												? "" + launcherArgument.getImageMaximumHeight()
												: "")
										+ ">"));

					arguments.addAll(Arrays.asList("-set", "filename:t", "%t", "+adjoin",
							folderSandboxFolios.toString() + "/%[filename:t]." + imageFormat.name()));

					updatedStandardOutput("Preprocess images.");
					SystemProcess preprocessJob = new SystemProcess(folderProjectFolios, convertCommand);

					for (String fileName : OCR4allUtils.getFileNames(folderProjectFolios)) {
						// set image name
						arguments.set(0, fileName);

						// process image
						preprocessJob.execute(arguments);

						if (preprocessJob.getExitValue() != 0) {
							String error = preprocessJob.getStandardError();
							updatedStandardError(
									"Cannot preprocess images" + (error.isBlank() ? "" : " - " + error.trim()) + ".");

							return ProcessorServiceProvider.Processor.State.interrupted;
						}
					}
				} catch (IOException e) {
					updatedStandardError("Sandbox images cannot be created - " + e.getMessage() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				callback.updatedProgress(0.90F);

				if (isCanceled())
					return ProcessorServiceProvider.Processor.State.canceled;

				/*
				 * Moves the images to snapshot and creates the mets file
				 */
				updatedStandardOutput("Move the images to sandbox snapshot " + framework.getOutput().toString() + ".");

				int preprocessedImages = 0;
				try {
					final String metsFilePath = processorWorkspaceRelativePath.toString()
							+ (processorWorkspaceRelativePath.equals(Paths.get("")) ? "" : "/");
					final String fileIdPrefix = framework.getMetsGroup();

					MetsUtils.Page metsPage = MetsUtils.getPage(framework.getMetsGroup());

					final StringBuffer metsFileBuffer = new StringBuffer();
					final StringBuffer metsPageBuffer = new StringBuffer();

					for (Path image : Files.list(folderSandboxFolios).collect(Collectors.toList())) {
						String fileName = image.getFileName().toString();
						String metsFileId = OCR4allUtils.getNameWithoutExtension(fileName);
						String fileId = fileIdPrefix + "_" + metsFileId;

						Path target = Paths.get(framework.getOutput().toString(), fileName);

						metsFileBuffer.append(metsFileTemplate
								.replace(MetsPattern.file_mime_type.getPattern(), imageFormat.getMimeType())

								.replace(MetsPattern.file_id.getPattern(), fileId)

								.replace(MetsPattern.file_name.getPattern(), metsFilePath + fileName));
						metsPageBuffer.append(
								metsPageTemplate.replace(MetsPattern.page_id.getPattern(), metsPage.getId(metsFileId))

										.replace(MetsPattern.file_id.getPattern(), fileId));

						Files.move(image, target, StandardCopyOption.REPLACE_EXISTING);

						preprocessedImages++;
					}

					// Persist mets file
					try {
						updatedStandardOutput("Create mets file " + metsPath.toString() + ".");

						persistMets(metsPath, metsCoreTemplate, objectMapper.writeValueAsString(launcherArgument),
								metsGroup, metsFileBuffer.toString(), metsPageBuffer.toString());
					} catch (Exception e) {
						updatedStandardError("Can not create mets file - " + e.getMessage() + ".");

						return ProcessorServiceProvider.Processor.State.interrupted;
					}

				} catch (Exception e) {
					updatedStandardError("Can not move preprocessed images to sandbox - " + e.getMessage() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				/*
				 * Ends the process
				 */
				updatedStandardOutput(
						(preprocessedImages == 1 ? "One image" : preprocessedImages + " images") + " preprocessed.");

				return complete();
			}
		};
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
		 * The images.
		 */
		private List<String> images = new ArrayList<>();

		/**
		 * Returns the images.
		 *
		 * @return The images.
		 * @since 1.8
		 */
		public List<String> getImages() {
			return images;
		}

		/**
		 * Set the images.
		 *
		 * @param images The images to set.
		 * @since 1.8
		 */
		public void setImages(List<String> images) {
			this.images = images;
		}

	}

}
