/**
 * File:     LAREXLauncher.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     2ß.09.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.postcorrection.provider;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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
		return new Model(
				new StringField(Field.mimeType.getName(), null, locale -> getString(locale, "mimeType.description"),
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

				// TODO: load mets

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * mime type argument
				 */
				updatedStandardOutput("Parse parameters.");

				StringArgument mimeType = null;
				try {
					mimeType = modelArgument.getArgument(StringArgument.class, Field.mimeType.getName());
				} catch (ClassCastException e) {
					updatedStandardError("The argument '" + Field.mimeType.getName() + "' is not of string type.");

					return ProcessServiceProvider.Processor.State.interrupted;
				}

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				// TODO: work
				updatedStandardOutput("WORK.");
				callback.updatedProgress(0.05F);

				callback.updatedProgress(0.90F);

				if (isCanceled())
					return ProcessServiceProvider.Processor.State.canceled;

				/*
				 * Moves the images to workflow and creates the mets file
				 */
				updatedStandardOutput("Move the images to workflow sandbox " + framework.getOutput().toString() + ".");

				int preprocessedImages = 0;
				try {

					// Persist mets file
					try {
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

}
