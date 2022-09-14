/**
 * File:     WorkflowLauncher.java
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProviderWorker;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.ImageFormat;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.PersistenceManager;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Type;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.project.Folio;
import de.uniwuerzburg.zpd.ocr4all.application.spi.LauncherServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.CoreProcessorServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.core.ProcessServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.ConfigurationServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Premise;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.SystemCommand;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.Target;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Group;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.ImageField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.IntegerField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.Model;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.SelectField;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ImageArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.IntegerArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.ModelArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.model.argument.SelectArgument;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.MetsUtils;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.SystemProcess;

/**
 * Defines service providers for workflow launchers. The following properties of
 * the service provider collection <b>ocr4all-workflow-launcher</b> override the
 * local default settings (<b>key</b>: <i>default value</i>):
 * <ul>
 * <li>file_id: ocr4all</li>
 * <li>page_id: ocr4all-id</li>
 * </ul>
 * 
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class WorkflowLauncher extends CoreServiceProviderWorker implements LauncherServiceProvider {
	/**
	 * The prefix of the message keys in the resource bundle.
	 */
	private static final String messageKeyPrefix = "launcher.workflow.launcher.";

	/**
	 * The service provider identifier.
	 */
	private static final String identifier = "ocr4all-workflow-launcher";

	/**
	 * Defines templates.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Template {
		mets_core("mets-core"), mets_file("mets-file"), mets_page("mets-page");

		/**
		 * The folder.
		 */
		private static final String folder = "templates/spi/launcher/workflow-launcher/";

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
	private enum MetsPattern implements ConfigurationServiceProvider.CollectionKey {
		create_date, software_creator, parameter, file_group, file_template, page_template,

		file_mime_type, file_id("ocr4all"), file_name,

		page_id("ocr4all-id");

		/**
		 * The default value.
		 */
		private final String value;

		/**
		 * Default constructor for a pattern for mets templates.
		 * 
		 * @since 1.8
		 */
		private MetsPattern() {
			value = null;
		}

		/**
		 * Creates a pattern for mets templates.
		 * 
		 * @param value The default value.
		 * @since 1.8
		 */
		private MetsPattern(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework.
		 * ServiceProviderCollectionKey#getName()
		 */
		@Override
		public String getName() {
			return identifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework.
		 * ServiceProviderCollectionKey#getKey()
		 */
		@Override
		public String getKey() {
			return name();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.env.Framework.
		 * ServiceProviderCollectionKey#getDefaultValue()
		 */
		@Override
		public String getDefaultValue() {
			return value;
		}

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
	 * Defines fields.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Field {
		method, methodThreshold("method-threshold"), imageFormat("image-format"),
		imageMaximumWidth("image-maximum-width"), imageMaximumHeight("image-maximum-height"), images;

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
	 * Defines methods.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	private enum Method {
		none, monochrome(true), threshold(true), colorSpace(false), channel(false), combine(false);

		/**
		 * The default method.
		 */
		public static Method defaultMethod = none;

		/**
		 * True if it is a method.
		 */
		private final boolean isMethod;

		/**
		 * True if it is a binary method. Otherwise it is a gray scale method.
		 */
		private final boolean isBinary;

		/**
		 * Creates a method.
		 * 
		 * @since 1.8
		 */
		private Method() {
			isMethod = false;
			isBinary = false;
		}

		/**
		 * Creates a method.
		 * 
		 * @param isBinary True if it is a binary method. Otherwise it is a gray scale
		 *                 method.
		 * @since 1.8
		 */
		private Method(boolean isBinary) {
			isMethod = true;
			this.isBinary = isBinary;
		}

		/**
		 * Returns true if it is a method.
		 *
		 * @return True if it is a method.
		 * @since 1.8
		 */
		public boolean isMethod() {
			return isMethod;
		}

		/**
		 * Returns true if it is a binary method. Otherwise it is a gray scale method.
		 *
		 * @return True if it is a binary method. Otherwise it is a gray scale method.
		 * @since 1.8
		 */
		public boolean isBinary() {
			return isBinary;
		}

		/**
		 * Returns the method with given name.
		 * 
		 * @param name The method name.
		 * @return The method with given name. Null if unknown.
		 * @since 1.8
		 */
		public static Method getMethod(String name) {
			if (name != null && !name.isBlank()) {
				name = name.trim();

				for (Method method : Method.values())
					if (method.name().equals(name))
						return method;
			}

			return null;
		}

	}

	/**
	 * Default constructor for a service provider for workflow launcher.
	 * 
	 * @since 1.8
	 */
	public WorkflowLauncher() {
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
		return target.getWorkflow().isLaunched()
				? new Premise(Premise.State.block, locale -> getString(locale, "already.launched"))
				: (configuration.isSystemCommandAvailable(SystemCommand.Type.convert) ? new Premise()
						: new Premise(Premise.State.block, locale -> getString(locale, "no.command.convert")));
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
		// Use launcher argument to set the default values
		LauncherArgumentMethodThreshold argument = new LauncherArgumentMethodThreshold();

		// The method
		List<SelectField.Option> monochromeMethods = new ArrayList<SelectField.Option>();
		List<SelectField.Option> grayScaleMethods = new ArrayList<SelectField.Option>();
		for (Method method : Method.values())
			if (method.isMethod()) {
				boolean isSelected = method.equals(argument.getMethod());

				if (method.isBinary())
					monochromeMethods.add(new SelectField.Option(isSelected, method.name(),
							locale -> getString(locale, "convert.method." + method.name())));
				else
					grayScaleMethods.add(new SelectField.Option(isSelected, method.name(),
							locale -> getString(locale, "convert.method." + method.name())));
			}

		// The image format
		ImageFormat targetImageFormat = ImageFormat.valueOf(argument.getImageFormat());
		List<SelectField.Item> imageFormatOptions = new ArrayList<SelectField.Item>();
		for (ImageFormat imageFormat : ImageFormat.values())
			imageFormatOptions.add(new SelectField.Option(targetImageFormat.equals(imageFormat), imageFormat.name(),
					locale -> imageFormat.getLabel()));

		return new Model(
				new SelectField(Field.imageFormat.getName(), locale -> getString(locale, "image.format"), null, false,
						imageFormatOptions, false),
				new IntegerField(Field.imageMaximumWidth.getName(), argument.getImageMaximumWidth(),
						locale -> getString(locale, "image.maximum.width"),
						locale -> getString(locale, "image.maximum.not.set"), null, 1, 0, null, locale -> "px", false),
				new IntegerField(Field.imageMaximumHeight.getName(), argument.getImageMaximumHeight(),
						locale -> getString(locale, "image.maximum.height"),
						locale -> getString(locale, "image.maximum.not.set"), null, 1, 0, null, locale -> "px", false),
				new ImageField(Field.images.getName(), locale -> getString(locale, "image.selection")),

				new Group(true, locale -> getString(locale, "convert"),
						new SelectField(Field.method.getName(), locale -> getString(locale, "convert.method"),
								new SelectField.Option(Method.none.equals(argument.getMethod()), Method.none.name(),
										locale -> getString(locale, "convert.method." + Method.none.name())),
								new SelectField.Association(monochromeMethods,
										locale -> getString(locale, "convert.method.group.binary")),
								new SelectField.Association(grayScaleMethods,
										locale -> getString(locale, "convert.method.group.gray.scale"))),
						new IntegerField(Field.methodThreshold.getName(), argument.getMethodThreshold(),
								locale -> getString(locale, "convert.method.threshold.parameter"),
								locale -> getString(locale, "convert.method.threshold.parameter.description"), null, 1,
								0, 100, locale -> "%", false)));
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
			 * Persists the mets file.
			 * 
			 * @param path             The mets file path.
			 * @param coreTemplate     The mets core template.
			 * @param launcherArgument The launcher arguments.
			 * @param group            The file group.
			 * @param file             The files.
			 * @param page             The pages.
			 * @throws JsonProcessingException Throws when processing (parsing, generating)
			 *                                 JSON content that are not pure I/O problems.
			 * @throws IOException             Throws if an I/O error occurs writing to or
			 *                                 creating the file.
			 * @since 1.8
			 */
			private void persistMets(Path path, String coreTemplate, LauncherArgument launcherArgument, String group,
					String file, String page) throws JsonProcessingException, IOException {
				Files.write(path, coreTemplate
						.replace(MetsPattern.create_date.getPattern(), MetsUtils.getFormattedDate())

						.replace(MetsPattern.software_creator.getPattern(), identifier + " v" + getVersion())

						.replace(MetsPattern.parameter.getPattern(), objectMapper.writeValueAsString(launcherArgument))

						.replace(MetsPattern.file_group.getPattern(), group)

						.replace(MetsPattern.file_template.getPattern(), file)

						.replace(MetsPattern.page_template.getPattern(), page).getBytes());
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
			public State execute(Callback callback, Framework framework, ModelArgument modelArgument) {
				if (!initialize(identifier, callback, framework))
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Loads core data
				 */

				// Processor workspace path
				final Path processorWorkspaceRelativePath = framework.getOutputRelativeProcessorWorkspace();
				if (processorWorkspaceRelativePath == null) {
					updatedStandardError("Internal error: inconsistent framework processor workspace path.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// mets
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

				final String metsCoreTemplate;
				try {
					metsCoreTemplate = Template.mets_core.getResourceAsText();
				} catch (Exception e) {
					updatedStandardError(
							"Internal error: missed mets core resource '" + Template.mets_core.getResourceName() + ".");

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

				// Convert system command
				String convertCommand = configuration.getSystemCommand(SystemCommand.Type.convert).getCommand()
						.toString();

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Available arguments
				 */
				Set<String> availableArguments = modelArgument.getArgumentNames();

				updatedStandardOutput("Parse parameters.");

				/*
				 * The arguments depends on method
				 */
				Method method = Method.defaultMethod;
				if (availableArguments.remove(Field.method.getName()))
					try {
						final SelectArgument argument = modelArgument.getArgument(SelectArgument.class,
								Field.method.getName());

						if (argument.getValues().isPresent()) {
							List<String> values = argument.getValues().get();

							if (values.size() == 1) {
								method = Method.getMethod(values.get(0));

								if (method == null) {
									updatedStandardError("Unknown method value "
											+ (values.get(0) == null || values.get(0).isBlank() ? ""
													: " '" + values.get(0).trim() + "'")
											+ ".");

									return ProcessServiceProvider.Processor.State.interrupted;
								}
							} else if (values.size() > 1) {
								updatedStandardError("Only one method can be selected.");

								return ProcessServiceProvider.Processor.State.interrupted;
							}
						}

					} catch (ClassCastException e) {
						updatedStandardError("The argument '" + Field.method.getName() + "' is not of selection type.");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				/*
				 * Launcher arguments
				 */
				LauncherArgument launcherArgument = new LauncherArgument();

				switch (method) {
				case threshold:
					launcherArgument = new LauncherArgumentMethodThreshold();
					break;
				case none:
				case monochrome:
				case colorSpace:
				case channel:
				case combine:
				default:
					launcherArgument = new LauncherArgument();
					break;
				}

				launcherArgument.setMethod(method);

				// Image format
				if (availableArguments.remove(Field.imageFormat.getName()))
					try {
						final SelectArgument argument = modelArgument.getArgument(SelectArgument.class,
								Field.imageFormat.getName());

						if (argument.getValues().isPresent()) {
							List<String> extensions = argument.getValues().get();

							if (extensions.size() == 1)
								launcherArgument.setImageFormat(extensions.get(0));
							else if (extensions.size() > 1) {
								updatedStandardError("Only one image format can be selected.");

								return ProcessServiceProvider.Processor.State.interrupted;
							}
						}
					} catch (ClassCastException e) {
						updatedStandardError(
								"The argument '" + Field.imageFormat.getName() + "' is not of selection type.");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				final ImageFormat imageFormat = ImageFormat.getImageFormat(launcherArgument.getImageFormat());
				if (imageFormat == null) {
					updatedStandardError(
							"The image format"
									+ (launcherArgument.getImageFormat() == null
											|| launcherArgument.getImageFormat().isBlank() ? ""
													: " '" + launcherArgument.getImageFormat().trim() + "'")
									+ " is not supported.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// Image maximum width
				if (availableArguments.remove(Field.imageMaximumWidth.getName()))
					try {
						final IntegerArgument argument = modelArgument.getArgument(IntegerArgument.class,
								Field.imageMaximumWidth.getName());

						if (argument.getValue().isPresent())
							launcherArgument.setImageMaximumWidth(argument.getValue().get());
					} catch (ClassCastException e) {
						updatedStandardError(
								"The argument '" + Field.imageMaximumWidth.getName() + "' is not of integer type.");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				// Image maximum height
				if (availableArguments.remove(Field.imageMaximumHeight.getName()))
					try {
						final IntegerArgument argument = modelArgument.getArgument(IntegerArgument.class,
								Field.imageMaximumHeight.getName());

						if (argument.getValue().isPresent())
							launcherArgument.setImageMaximumHeight(argument.getValue().get());
					} catch (ClassCastException e) {
						updatedStandardError(
								"The argument '" + Field.imageMaximumHeight.getName() + "' is not of integer type.");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				// Images for the launcher
				if (availableArguments.remove(Field.images.getName()))
					try {
						final ImageArgument argument = modelArgument.getArgument(ImageArgument.class,
								Field.images.getName());

						if (argument.getValues().isPresent())
							launcherArgument.setImages(argument.getValues().get());
					} catch (ClassCastException e) {
						updatedStandardError("The argument '" + Field.images.getName() + "' is not of image type.");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				// Threshold parameter
				if (launcherArgument instanceof LauncherArgumentMethodThreshold) {
					LauncherArgumentMethodThreshold launcherArgumentMethodThreshold = (LauncherArgumentMethodThreshold) launcherArgument;

					if (availableArguments.remove(Field.methodThreshold.getName()))
						try {
							final IntegerArgument argument = modelArgument.getArgument(IntegerArgument.class,
									Field.methodThreshold.getName());

							if (argument.getValue().isPresent()) {
								launcherArgumentMethodThreshold.setMethodThreshold(argument.getValue().get());

								if (launcherArgumentMethodThreshold.getMethodThreshold() < 0
										|| launcherArgumentMethodThreshold.getMethodThreshold() > 100) {
									updatedStandardError("The threshold value "
											+ launcherArgumentMethodThreshold.getMethodThreshold()
											+ " for the threshold method is out of range [0..100].");

									return ProcessServiceProvider.Processor.State.interrupted;
								}
							}
						} catch (ClassCastException e) {
							updatedStandardError(
									"The argument '" + Field.methodThreshold.getName() + "' is not of integer type.");

							return ProcessServiceProvider.Processor.State.interrupted;
						}
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

				Hashtable<Integer, Folio> folios = new Hashtable<>();
				try {
					for (Folio folio : (new PersistenceManager(framework.getTarget().getProject().getFolio(),
							Type.project_folio_v1)).getEntities(Folio.class))
						folios.put(folio.getId(), folio);
				} catch (Exception e) {
					updatedStandardError("Cannot read project folios - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (folios.isEmpty()) {
					updatedStandardOutput("There are no project images for workflow launcher.");

					callback.updatedProgress(1);
					return ProcessServiceProvider.Processor.State.completed;
				}

				// The images
				updatedStandardOutput("Determine workflow launcher images.");

				Set<Integer> images = new HashSet<>();
				for (int id : launcherArgument.getImages())
					if (folios.containsKey(id))
						images.add(id);

				if (images.isEmpty()) {
					updatedStandardOutput("There are no images for workflow launcher.");

					// Persist mets file
					try {
						persistMets(metsPath, metsCoreTemplate, launcherArgument, metsGroup, "", "");
					} catch (Exception e) {
						updatedStandardError("Can not create mets file - " + e.getMessage() + ".");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

					callback.updatedProgress(1);
					return ProcessServiceProvider.Processor.State.completed;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Process images in temporary directories
				 */
				updatedStandardOutput(
						"Process images in temporary directory (" + framework.getTemporary().toString() + ").");

				Path folderProjectFolios = Paths.get(framework.getTemporary().toString(), "project");
				Path folderWorkflowFolios = Paths.get(framework.getTemporary().toString(), "workflow");
				try {
					Files.createDirectory(folderProjectFolios);
					Files.createDirectory(folderWorkflowFolios);
				} catch (IOException e) {
					updatedStandardError("could not create required temporary directory - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				// Copy required project images to temporary directory
				int index = 0;
				for (int id : images) {
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

						return ProcessServiceProvider.Processor.State.interrupted;
					}
				}

				callback.updatedProgress(0.35F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				try {
					SystemProcess preprocessJob = new SystemProcess(folderProjectFolios, convertCommand);
					List<String> arguments = new ArrayList<>(Arrays.asList("*", "-format", imageFormat.name()));

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
							folderWorkflowFolios.toString() + "/%[filename:t]." + imageFormat.name()));

					updatedStandardOutput("Preprocess images.");
					preprocessJob.execute(arguments);

					if (preprocessJob.getExitValue() != 0) {
						String error = preprocessJob.getStandardError();
						updatedStandardError(
								"Cannot preprocess images" + (error.isBlank() ? "" : " - " + error.trim()) + ".");

						return ProcessServiceProvider.Processor.State.interrupted;
					}
				} catch (IOException e) {
					updatedStandardError("Workflow images cannot be created - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				callback.updatedProgress(0.90F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Moves the images to workflow and creates the mets file
				 */
				updatedStandardOutput("Move the images to workflow sandbox " + framework.getOutput().toString() + ".");

				int preprocessedImages = 0;
				try {
					final String metsFilePath = processorWorkspaceRelativePath.toString()
							+ (processorWorkspaceRelativePath.equals(Paths.get("")) ? "" : "/");
					final String fileIdPrefix = configuration.getValue(MetsPattern.file_id);
					final String pageIdPrefix = configuration.getValue(MetsPattern.page_id);

					final StringBuffer metsFileBuffer = new StringBuffer();
					final StringBuffer metsPageBuffer = new StringBuffer();

					for (Path image : Files.list(folderWorkflowFolios).collect(Collectors.toList())) {
						String fileName = image.getFileName().toString();
						String metsFileId = "_" + OCR4allUtils.getNameWithoutExtension(fileName);

						Path target = Paths.get(framework.getOutput().toString(), fileName);

						metsFileBuffer.append(metsFileTemplate
								.replace(MetsPattern.file_mime_type.getPattern(), imageFormat.getMimeType())

								.replace(MetsPattern.file_id.getPattern(), fileIdPrefix + metsFileId)

								.replace(MetsPattern.file_name.getPattern(), metsFilePath + fileName));
						metsPageBuffer.append(
								metsPageTemplate.replace(MetsPattern.page_id.getPattern(), pageIdPrefix + metsFileId)

										.replace(MetsPattern.file_id.getPattern(), fileIdPrefix + metsFileId));

						Files.move(image, target, StandardCopyOption.REPLACE_EXISTING);

						preprocessedImages++;
					}

					// Persist mets file
					try {
						updatedStandardOutput("Create mets file " + metsPath.toString() + ".");
						persistMets(metsPath, metsCoreTemplate, launcherArgument, metsGroup, metsFileBuffer.toString(),
								metsPageBuffer.toString());
					} catch (Exception e) {
						updatedStandardError("Can not create mets file - " + e.getMessage() + ".");

						return ProcessServiceProvider.Processor.State.interrupted;
					}

				} catch (Exception e) {
					updatedStandardError("Can not move preprocessed images to sandbox - " + e.getMessage() + ".");

					return ProcessServiceProvider.Processor.State.interrupted;
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
		 * The image format.
		 */
		@JsonProperty("image-format")
		private String imageFormat = ImageFormat.png.name();

		/**
		 * The image maximum width.
		 */
		@JsonProperty("image-maximum-width")
		private int imageMaximumWidth = 0;

		/**
		 * The image maximum height.
		 */
		@JsonProperty("image-maximum-height")
		private int imageMaximumHeight = 0;

		/**
		 * The images.
		 */
		private List<Integer> images = new ArrayList<Integer>();

		/**
		 * The method.
		 */
		private Method method = Method.defaultMethod;

		/**
		 * Returns the image format.
		 *
		 * @return The image format.
		 * @since 1.8
		 */
		public String getImageFormat() {
			return imageFormat;
		}

		/**
		 * Set the image format.
		 *
		 * @param imageFormat The image format to set.
		 * @since 1.8
		 */
		public void setImageFormat(String imageFormat) {
			this.imageFormat = imageFormat;
		}

		/**
		 * Returns the image maximum width.
		 *
		 * @return The image maximum width.
		 * @since 1.8
		 */
		public int getImageMaximumWidth() {
			return imageMaximumWidth;
		}

		/**
		 * Set the image maximum width. If the width is not positive, it is set to 0.
		 *
		 * @param width The image width to set.
		 * @since 1.8
		 */
		public void setImageMaximumWidth(int width) {
			if (width > 0)
				imageMaximumWidth = width;
			else
				imageMaximumWidth = 0;
		}

		/**
		 * Returns the image maximum height.
		 *
		 * @return The image maximum height.
		 * @since 1.8
		 */
		public int getImageMaximumHeight() {
			return imageMaximumHeight;
		}

		/**
		 * Set the image maximum height. If the height is not positive, it is set to 0.
		 *
		 * @param height The image height to set.
		 * @since 1.8
		 */
		public void setImageMaximumHeight(int height) {
			if (height > 0)
				imageMaximumHeight = height;
			else
				imageMaximumHeight = 0;
		}

		/**
		 * Returns the images.
		 *
		 * @return The images.
		 * @since 1.8
		 */
		public List<Integer> getImages() {
			return images;
		}

		/**
		 * Set the images.
		 *
		 * @param images The images to set.
		 * @since 1.8
		 */
		public void setImages(List<Integer> images) {
			this.images = images;
		}

		/**
		 * Returns the method.
		 *
		 * @return The method.
		 * @since 1.8
		 */
		public Method getMethod() {
			return method;
		}

		/**
		 * Set the method.
		 *
		 * @param method The method to set.
		 * @since 1.8
		 */
		public void setMethod(Method method) {
			this.method = method;
		}

	}

	/**
	 * Defines launcher arguments with default values for method threshold.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class LauncherArgumentMethodThreshold extends LauncherArgument {
		/**
		 * The method threshold.
		 */
		@JsonProperty("method-threshold")
		private int methodThreshold = 50;

		/**
		 * Returns the method threshold.
		 *
		 * @return The method threshold.
		 * @since 1.8
		 */
		public int getMethodThreshold() {
			return methodThreshold;
		}

		/**
		 * Set the method threshold.
		 *
		 * @param threshold The threshold to set.
		 * @since 1.8
		 */
		public void setMethodThreshold(int threshold) {
			methodThreshold = threshold;
		}

	}

}
