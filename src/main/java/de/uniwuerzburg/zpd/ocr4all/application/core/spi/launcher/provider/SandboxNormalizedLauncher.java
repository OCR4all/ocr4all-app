/**
 * File:     SandboxNormalizedLauncher.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     02.07.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorCore;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ProcessFramework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.ImageField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ImageArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
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
				Path processorWorkspace = framework.getProcessorWorkspace();
				if (processorWorkspace == null) {
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
					updatedStandardError("Internal error: missed mets page file resource '"
							+ Template.mets_page.getResourceName() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessorServiceProvider.Processor.State.canceled;

				callback.updatedProgress(0.01F);

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

				callback.updatedProgress(0.02F);

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
					updatedStandardOutput("There are no project images for sandbox normalized launcher.");

					callback.updatedProgress(1);
					return ProcessorServiceProvider.Processor.State.completed;
				}

				// The images
				updatedStandardOutput("Determine sandbox normalized launcher images.");

				Path normalizedPath = framework.getTarget().getProject().getImages().getNormalized().getFolder();
				ImageFormat normalizedImageFormat = ImageFormat
						.getImageFormat(framework.getTarget().getProject().getImages().getNormalized().getFormat());

				Set<String> images = new HashSet<>();
				for (String id : launcherArgument.getImages())
					if (folios.containsKey(id)
							&& Files.isRegularFile(normalizedPath.resolve(id + "." + normalizedImageFormat.name())))
						images.add(id);

				if (images.isEmpty()) {
					updatedStandardOutput("There are no images for sandbox normalized launcher.");

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

				callback.updatedProgress(0.90F);

				/*
				 * Creates the mets file
				 */
				updatedStandardOutput("Create mets file " + metsPath.toString() + ".");

				final Path metsFilePath = processorWorkspace.relativize(normalizedPath);
				final String fileIdPrefix = framework.getMetsGroup();

				MetsUtils.Page metsPage = MetsUtils.getPage(framework.getMetsGroup());

				final StringBuffer metsFileBuffer = new StringBuffer();
				final StringBuffer metsPageBuffer = new StringBuffer();

				for (String id : images) {
					String fileId = fileIdPrefix + "_" + id;
					String fileName = id + "." + normalizedImageFormat.name();

					metsFileBuffer.append(metsFileTemplate
							.replace(MetsPattern.file_mime_type.getPattern(), normalizedImageFormat.getMimeType())

							.replace(MetsPattern.file_id.getPattern(), fileId)

							.replace(MetsPattern.file_name.getPattern(), metsFilePath.resolve(fileName).toString()));
					metsPageBuffer.append(metsPageTemplate.replace(MetsPattern.page_id.getPattern(), metsPage.getId(id))

							.replace(MetsPattern.file_id.getPattern(), fileId));
				}

				// Persist mets file
				try {
					persistMets(metsPath, metsCoreTemplate, objectMapper.writeValueAsString(launcherArgument),
							metsGroup, metsFileBuffer.toString(), metsPageBuffer.toString());
				} catch (Exception e) {
					updatedStandardError("Can not create mets file - " + e.getMessage() + ".");

					return ProcessorServiceProvider.Processor.State.interrupted;
				}

				/*
				 * Ends the process
				 */
				updatedStandardOutput(
						(images.size() == 1 ? "One image" : images.size() + " images") + " preprocessed.");

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
