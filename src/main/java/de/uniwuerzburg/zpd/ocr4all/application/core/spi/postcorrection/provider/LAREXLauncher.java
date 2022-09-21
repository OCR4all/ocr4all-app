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
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser;
import de.uniwuerzburg.zpd.ocr4all.application.core.parser.mets.MetsParser.Root.FileGroup.File;
import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProviderWorker;
import de.uniwuerzburg.zpd.ocr4all.application.spi.PostcorrectionServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.StringField;
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
	 * The default page xml mime type.
	 */
	private static final String pageXmlMimeType = "application/vnd.prima.page+xml";

	/**
	 * Defines fields.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Field {
		mimeType("mime-type");

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
	 * Default constructor for a service provider for workflow launcher.
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
		return new Model(new StringField(Field.mimeType.getName(), pageXmlMimeType,
				locale -> getString(locale, "mimeType.description"),
				locale -> getString(locale, "mimeType.placeholder")));
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

				String mimeType = null;
				try {
					StringArgument mimeTypeArgument = modelArgument.getArgument(StringArgument.class,
							Field.mimeType.getName());

					if (mimeTypeArgument != null) {
						mimeType = mimeTypeArgument.getValue().orElse(null);

						mimeType = mimeType == null || mimeType.isBlank() ? null : mimeType.trim();
					}
				} catch (ClassCastException e) {
					updatedStandardError("The argument '" + Field.mimeType.getName() + "' is not of string type.");

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

				callback.updatedProgress(0.05F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

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

				// Select required files and copy then to temporary directory
				final Path processorWorkspace = framework.getProcessorWorkspace();

				List<LarexFile> larexFiles = new ArrayList<>();
				int index = 0;
				for (MetsParser.Root.FileGroup.File file : inputFileGroup.getFiles()) {
					callback.updatedProgress(0.05F + (0.75F * (++index) / inputFileGroup.getFiles().size()));

					if (mimeType == null || mimeType.equals(file.getMimeType())) {
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
									Paths.get(framework.getTemporary().toString(), larexFile.getTargetFilename()),
									StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							updatedStandardError("Cannot copy the required input file '" + inputFile.toString()
									+ "' to temporary directory - " + e.getMessage() + ".");

							return ProcessServiceProvider.Processor.State.interrupted;
						}

						larexFiles.add(larexFile);
					}
				}

				callback.updatedProgress(0.80F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Moves the images to workflow
				 */
				int numberFiles = 0;
				try {
					updatedStandardOutput(
							"Move the images to workflow sandbox " + framework.getOutput().toString() + ".");

					for (LarexFile larexFile : larexFiles)
						Files.move(Paths.get(framework.getTemporary().toString(), larexFile.getTargetFilename()),
								Paths.get(framework.getOutput().toString(), larexFile.getTargetFilename()),
								StandardCopyOption.REPLACE_EXISTING);

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
					// TODO: Updates mets file
				} catch (Exception e) {
					updatedStandardError("Can not update mets file - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				/*
				 * Ends the process
				 */
				updatedStandardOutput((numberFiles == 1 ? "One file" : numberFiles + " files") + " available.");

				return complete();
			}
		};
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
		 * The mets framework file group.
		 */
		private final MetsUtils.FrameworkFileGroup metsFrameworkFileGroup;

		/**
		 * The mets file.
		 */
		private final MetsParser.Root.FileGroup.File file;

		/**
		 * The file name in the target snapshot.
		 */
		private final String targetFilename;

		/**
		 * Creates a LAREX file.
		 * 
		 * @param metsFrameworkFileGroup The mets framework file group.
		 * @param file                   The mets file.
		 * @since 1.8
		 */
		public LarexFile(MetsUtils.FrameworkFileGroup metsFrameworkFileGroup, File file) {
			super();

			this.metsFrameworkFileGroup = metsFrameworkFileGroup;
			this.file = file;

			String sourceFilename = Paths.get(file.getLocation().getPath()).getFileName().toString();

			targetFilename = sourceFilename.startsWith(metsFrameworkFileGroup.getInput())
					? metsFrameworkFileGroup.getOutput()
							+ sourceFilename.substring(metsFrameworkFileGroup.getInput().length())
					: sourceFilename;
		}

		/**
		 * Returns the file name in the target snapshot.
		 * 
		 * @return The file name in the target snapshot.
		 * @since 1.8
		 */
		public String getTargetFilename() {
			return targetFilename;
		}
	}
}
