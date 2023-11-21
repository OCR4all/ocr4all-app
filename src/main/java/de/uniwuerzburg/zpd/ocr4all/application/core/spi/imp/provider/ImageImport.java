/**
 * File:     ImageImport.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.imp.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProviderWorker;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.folio.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.ImportServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.SystemCommand;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.SelectField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.StringField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.SelectArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.StringArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines service providers for images import.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ImageImport extends CoreServiceProviderWorker implements ImportServiceProvider {
	/**
	 * The prefix of the message keys in the resource bundle.
	 */
	private static final String messageKeyPrefix = "import.image.import.";

	/**
	 * The service provider identifier.
	 */
	private static final String identifier = "ocr4all-image-import";

	/**
	 * Defines fields.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Field {
		sourceFolder("source-folder"), imageFormats("image-formats");

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
	 * The image format options for the select field.
	 */
	private static final List<SelectField.Item> imageFormatOptions = new ArrayList<SelectField.Item>();
	{
		for (ImageFormat imageFormat : ImageFormat.values())
			imageFormatOptions.add(new SelectField.Option(ImageFormat.tif.equals(imageFormat), imageFormat.name(),
					locale -> imageFormat.getLabel()));
	}

	/**
	 * Default constructor for a service provider for image import.
	 * 
	 * @since 1.8
	 */
	public ImageImport() {
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
		return Arrays.asList("Image import");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getSteps()
	 */
	@Override
	public List<String> getSteps() {
		return Arrays.asList("initialization/import");
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
		if (!configuration.isSystemCommandAvailable(SystemCommand.Type.convert)
				&& !configuration.isSystemCommandAvailable(SystemCommand.Type.identify))
			return new Premise(Premise.State.block, locale -> getString(locale, "image.no.command.convert.identify"));
		else if (!configuration.isSystemCommandAvailable(SystemCommand.Type.convert))
			return new Premise(Premise.State.block, locale -> getString(locale, "image.no.command.convert"));
		else if (!configuration.isSystemCommandAvailable(SystemCommand.Type.identify))
			return new Premise(Premise.State.block, locale -> getString(locale, "image.no.command.identify"));

		try {
			return target.getProject().getImages().isFoliosEmpty() ? new Premise()
					: new Premise(Premise.State.warn, locale -> getString(locale, "folios.non.empty"));
		} catch (Exception e) {
			return new Premise(Premise.State.block, locale -> getString(locale, "folios.no.access"));
		}
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
		return new Model(
				new StringField(Field.sourceFolder.getName(), "images",
						locale -> getString(locale, "source.folder.description"),
						locale -> getString(locale, "source.folder.placeholder")),
				new SelectField(Field.imageFormats.getName(), locale -> getString(locale, "image.format.description"),
						locale -> getString(locale, "image.format.select.multiple"), true, imageFormatOptions, false));
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
			/**
			 * The image formats sorted by enumeration order.
			 */
			private List<ImageFormat> imageFormats;

			/**
			 * The folio persistence manager.
			 */
			private PersistenceManager folioManager;

			/**
			 * Returns true if the image should be processed.
			 * 
			 * @param image The image.
			 * @return True if the image should be processed.
			 * @since 1.8
			 */
			private boolean isProcess(Path image) {
				if (!Files.isDirectory(image))
					for (ImageFormat imageFormat : imageFormats)
						if (imageFormat.matches(image))
							return true;

				return false;
			}

			/**
			 * Returns the folio size.
			 * 
			 * @param size The size in the form [width]x[height].
			 * @return The folio size.
			 * @since 1.8
			 */
			/**
			 * Returns the folio size.
			 * 
			 * @param identifyJob The identify job.
			 * @param source      The source name.
			 * @param target      The target name.
			 * @return The folio size.
			 * @throws IOException Throws if the size could not be determined.
			 * @since 1.8
			 */
			private Folio.Size getSize(SystemProcess identifyJob, String source, String target) throws IOException {
				identifyJob.execute("-format", "%[fx:w]x%[fx:h]", target);

				if (identifyJob.getExitValue() != 0) {
					String error = identifyJob.getStandardError();
					updatedStandardError("Could not determine the folio size of '" + source + "'"
							+ (error.isBlank() ? "" : " - " + error.trim()) + ".");

					throw new IOException(
							"could not determine the folio size" + (error.isBlank() ? "" : " - " + error.trim()));
				}

				Folio.Size size = null;
				String[] split = identifyJob.getStandardOutput().split("x");
				if (split.length == 2)
					try {
						size = new Folio.Size(Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()));
					} catch (Exception e) {
						// Nothing to do
					}

				if (size == null) {
					String error = identifyJob.getStandardError();
					updatedStandardError("Could not determine the size of the folio '" + source + "'"
							+ (error.isBlank() ? "" : " - " + error.trim()) + ".");

					throw new IOException(
							"could not determine the folio size" + (error.isBlank() ? "" : " - " + error.trim()));
				} else
					return size;
			}

			/**
			 * Creates the derivatives quality image for folios.
			 * 
			 * @param convertJob The convert job.
			 * @param format     The folios derivatives format.
			 * @param target     The target folder.
			 * @param resize     The maximal size.
			 * @param quality    The compression quality.
			 * @return Null the main process can continue. Otherwise returns the state
			 *         interrupted on troubles or canceled if the process was canceled.
			 * @since 1.8
			 */
			private ProcessServiceProvider.Processor.State createDerivatives(SystemProcess convertJob, Path target,
					String resize, int quality) {
				final String format = getFramework().getTarget().getProject().getImages().getDerivatives().getFormat()
						.name();
				final String label = target.getFileName().toString();

				try {
					convertJob.execute("*", "-format", format, "-resize", resize + ">", "-quality", "" + quality,
							"-set", "filename:t", "%t", "+adjoin", target.toString() + "/%[filename:t]." + format);

					if (convertJob.getExitValue() != 0) {
						String error = convertJob.getStandardError();
						updatedStandardError("Cannot create derivatives " + label + " quality image for folios"
								+ (error.isBlank() ? "" : " - " + error.trim()) + ".");

						return ProcessServiceProvider.Processor.State.interrupted;
					}
				} catch (IOException e) {
					updatedStandardError("Cannot create derivatives " + label + " quality image for folios - "
							+ e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				return isCanceled() ? ProcessServiceProvider.Processor.State.canceled : null;
			}

			/**
			 * Move the files from source folder to the target folder.
			 * 
			 * @param fileNames The file names.
			 * @param source    The source folder.
			 * @param target    The target folder.
			 * @throws IOException Throws if an I/O error occurs.
			 * @since 1.8
			 */
			private void move(List<String> fileNames, Path source, Path target) throws IOException {
				for (String fileName : fileNames)
					Files.move(Paths.get(source.toString(), fileName), Paths.get(target.toString(), fileName),
							StandardCopyOption.REPLACE_EXISTING);
			}

			/**
			 * Removes the files from the folder.
			 * 
			 * @param fileNames The file names.
			 * @param folder    The folder.
			 * @return The number of files that could not be removed.
			 * @since 1.8
			 */
			private int remove(List<String> fileNames, Path folder) {
				int notRemoved = 0;
				for (String fileName : fileNames)
					try {
						Files.delete(Paths.get(folder.toString(), fileName));
					} catch (NoSuchFileException e) {
						// Nothing to do
					} catch (IOException e) {
						notRemoved++;
					}

				return notRemoved;
			}

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
			public ProcessServiceProvider.Processor.State execute(ProcessServiceProvider.Processor.Callback callback,
					Framework framework, ModelArgument modelArgument) {
				if (!initialize(identifier, callback, framework))
					return ProcessServiceProvider.Processor.State.canceled;

				// The system commands
				String convertCommand = configuration.getSystemCommand(SystemCommand.Type.convert).getCommand()
						.toString();
				String identifyCommand = configuration.getSystemCommand(SystemCommand.Type.identify).getCommand()
						.toString();

				/*
				 * Test for missed arguments
				 */
				updatedStandardOutput("Parse parameters.");

				List<String> missedArguments = modelArgument.getMissedArguments(Field.sourceFolder.getName(),
						Field.imageFormats.getName());
				if (!missedArguments.isEmpty()) {
					StringBuffer buffer = new StringBuffer();
					for (String name : missedArguments) {
						if (buffer.length() > 0)
							buffer.append(", ");
						buffer.append(name);
					}

					updatedStandardError("Missed required '"
							+ (missedArguments.size() == 1 ? "argument: " : "arguments: ") + buffer.toString() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Recover the arguments and test for right type
				 */
				SelectArgument imageFormats;
				try {
					imageFormats = modelArgument.getArgument(SelectArgument.class, Field.imageFormats.getName());
				} catch (ClassCastException e) {
					updatedStandardError(
							"The argument '" + Field.imageFormats.getName() + "' is not of selection type.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				StringArgument sourceFolder;
				try {
					sourceFolder = modelArgument.getArgument(StringArgument.class, Field.sourceFolder.getName());
				} catch (ClassCastException e) {
					updatedStandardError("The argument '" + Field.sourceFolder.getName() + "' is not of string type.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * The image formats sorted by enumeration order
				 */
				Optional<List<String>> values = imageFormats.getValues();

				Set<ImageFormat> formats = new HashSet<>();
				if (values.isPresent())
					for (String extension : values.get()) {
						ImageFormat imageFormat = ImageFormat.getImageFormat(extension);
						if (imageFormat != null)
							formats.add(imageFormat);
					}

				if (formats.isEmpty()) {
					updatedStandardError("No image formats have been selected for the import.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				this.imageFormats = new ArrayList<>(formats);
				Collections.sort(this.imageFormats, (o1, o2) -> o1.ordinal() - o2.ordinal());

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * The source folder
				 */
				Optional<String> value = sourceFolder.getValue();

				Path source = value.isEmpty() ? framework.getTarget().getExchange()
						: Paths.get(framework.getTarget().getExchange().toString(), value.get()).normalize();
				if (!source.startsWith(framework.getTarget().getExchange())) {
					updatedStandardError("The folios are located outside the project exchange folder.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * The folio configuration.
				 */
				updatedStandardOutput("Load project folio configuration.");

				folioManager = new PersistenceManager(framework.getTarget().getProject().getFolio(), Type.folio_v1);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Select the the folios to be imported
				 */
				updatedStandardOutput("Select the the folios to be imported.");

				List<Path> importFolios;

				try (Stream<Path> stream = Files.list(source)) {
					importFolios = stream.filter(image -> isProcess(image)).collect(Collectors.toList());
				} catch (Exception e) {
					updatedStandardError("The folios cannot be read from the project exchange folder '"
							+ source.toString() + "' - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// sort the folios
				Collections.sort(importFolios, new Comparator<Path>() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
					 */
					@Override
					public int compare(Path o1, Path o2) {
						String fileName1 = o1.getFileName().toString();
						String fileName2 = o2.getFileName().toString();
						int compare = FilenameUtils.removeExtension(fileName1)
								.compareToIgnoreCase(FilenameUtils.removeExtension(fileName2));

						return compare == 0
								? ImageFormat.getImageFormat(FilenameUtils.getExtension(fileName1)).ordinal()
										- ImageFormat.getImageFormat(FilenameUtils.getExtension(fileName2)).ordinal()
								: compare;
					}
				});

				if (importFolios.isEmpty()) {
					updatedStandardOutput("There are no folios to be imported from the project exchange folder '"
							+ source.toString() + "'.");

					callback.updatedProgress(1);
					return ProcessServiceProvider.Processor.State.completed;
				}

				updatedStandardOutput((importFolios.size() == 1 ? "One folio" : importFolios.size() + " folios")
						+ " will be imported from the project exchange folder '" + source.toString() + "'.");

				/*
				 * Create temporary directories
				 */
				updatedStandardOutput(
						"Process folios in temporary directory (" + framework.getTemporary().toString() + ").");

				Path folderFolios = Paths.get(framework.getTemporary().toString(), "folios");
				Path folderThumbnail = Paths.get(framework.getTemporary().toString(), "thumbnail");
				Path folderDetail = Paths.get(framework.getTemporary().toString(), "detail");
				Path folderBest = Paths.get(framework.getTemporary().toString(), "best");

				try {
					Files.createDirectory(folderFolios);
					Files.createDirectory(folderThumbnail);
					Files.createDirectory(folderDetail);
					Files.createDirectory(folderBest);
				} catch (IOException e) {
					updatedStandardError("could not create required temporary directory - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				SystemProcess identifyJob = new SystemProcess(folderFolios, identifyCommand);

				List<Folio> folios = new ArrayList<>();
				int index = 0;
				for (Path file : importFolios)
					try {
						String id = OCR4allUtils.getUUID();
						String fileName = file.getFileName().toString();

						ImageFormat imageFormat = ImageFormat.getImageFormat(FilenameUtils.getExtension(fileName));

						Files.copy(file, Paths.get(folderFolios.toString(), id + "." + imageFormat.name()),
								StandardCopyOption.REPLACE_EXISTING);

						Folio.Size size;
						try {
							size = getSize(identifyJob, file.toString(), id + "." + imageFormat.name());
						} catch (IOException e) {
							return ProcessServiceProvider.Processor.State.interrupted;
						}

						de.uniwuerzburg.zpd.ocr4all.application.persistence.util.ImageFormat format = imageFormat
								.getPersistence();

						if (format == null) {
							updatedStandardError(
									"The folio does not supports the image type " + imageFormat.name() + ".");

							return ProcessServiceProvider.Processor.State.interrupted;
						}

						folios.add(new Folio(new Date(), framework.getUser(), id,
								FilenameUtils.removeExtension(fileName), format, size, null));

						callback.updatedProgress(0.20F * (++index) / importFolios.size());
					} catch (IOException e) {
						updatedStandardError(
								"Cannot copy the folio '" + file.toString() + "' - " + e.getMessage() + ".");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Create derivatives
				 */
				updatedStandardOutput("Create derivatives.");

				// quality best
				ProcessServiceProvider.Processor.State state = createDerivatives(
						new SystemProcess(folderFolios, convertCommand), folderBest, "1536x1536", 50);

				if (state != null)
					return state;

				callback.updatedProgress(0.40F);

				// quality detail
				state = createDerivatives(new SystemProcess(folderBest, convertCommand), folderDetail, "768x768", 50);

				if (state != null)
					return state;

				callback.updatedProgress(0.50F);

				// quality thumbnail
				state = createDerivatives(new SystemProcess(folderDetail, convertCommand), folderThumbnail, "128x128",
						50);

				if (state != null)
					return state;

				callback.updatedProgress(0.55F);

				// set sizes
				SystemProcess identifyThumbnailJob = new SystemProcess(folderThumbnail, identifyCommand);
				SystemProcess identifyDetailJob = new SystemProcess(folderDetail, identifyCommand);
				SystemProcess identifyBestJob = new SystemProcess(folderBest, identifyCommand);

				List<String> foliosFiles = new ArrayList<>();
				List<String> derivativeFiles = new ArrayList<>();

				final Target.Project.Images images = framework.getTarget().getProject().getImages();
				final String foliosDerivativesImageFormat = images.getDerivatives().getFormat().name();
				for (Folio folio : folios) {
					try {
						foliosFiles.add(folio.getId() + "." + folio.getFormat().name());

						String target = folio.getId() + "." + foliosDerivativesImageFormat;
						derivativeFiles.add(target);

						folio.setDerivatives(
								new Folio.Derivatives(getSize(identifyThumbnailJob, folio.getName(), target),
										getSize(identifyDetailJob, folio.getName(), target),
										getSize(identifyBestJob, folio.getName(), target)));
					} catch (IOException e) {
						return ProcessServiceProvider.Processor.State.interrupted;
					}
				}

				callback.updatedProgress(0.6F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Move the folios to the project
				 */
				updatedStandardOutput(
						"Move the folios to project " + framework.getTarget().getProject().getRoot().toString() + ".");

				try {
					move(foliosFiles, folderFolios, images.getFolios());
					callback.updatedProgress(0.7F);
					if (isCanceled())
						return ProcessServiceProvider.Processor.State.canceled;

					move(derivativeFiles, folderThumbnail, images.getDerivatives().getThumbnail());
					callback.updatedProgress(0.75F);
					if (isCanceled())
						return ProcessServiceProvider.Processor.State.canceled;

					move(derivativeFiles, folderDetail, images.getDerivatives().getDetail());
					callback.updatedProgress(0.8F);
					if (isCanceled())
						return ProcessServiceProvider.Processor.State.canceled;

					move(derivativeFiles, folderBest, images.getDerivatives().getBest());
					callback.updatedProgress(0.9F);
					if (isCanceled())
						return ProcessServiceProvider.Processor.State.canceled;
				} catch (IOException e) {
					int remain = remove(foliosFiles, images.getFolios());
					remain += remove(derivativeFiles, images.getDerivatives().getThumbnail());
					remain += remove(derivativeFiles, images.getDerivatives().getDetail());
					remain += remove(derivativeFiles, images.getDerivatives().getBest());

					updatedStandardError("Cannot move the folios to project"
							+ (remain == 0 ? "" : " (" + remain + " could not cleaned up)") + " - " + e.getMessage()
							+ ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				/*
				 * Persist the configuration
				 */
				updatedStandardOutput("Persist the folio configuration.");

				try {
					folioManager.persist(true, folios);
				} catch (Exception e) {
					updatedStandardError("Cannot persist project folios configuration file - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				updatedStandardOutput((foliosFiles.size() == 1 ? "One folio" : foliosFiles.size() + " folios")
						+ " imported from the project exchange folder '" + source.toString() + "'.");

				return complete();
			}
		};
	}

}
