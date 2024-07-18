/**
 * File:     SandboxCoreLauncher.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     18.07.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.spi.launcher.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.uniwuerzburg.zpd.ocr4all.application.core.spi.CoreServiceProviderWorker;
import de.uniwuerzburg.zpd.ocr4all.application.core.util.OCR4allUtils;
import de.uniwuerzburg.zpd.ocr4all.application.spi.LauncherServiceProvider;
import de.uniwuerzburg.zpd.ocr4all.application.spi.util.mets.MetsUtils;

/**
 * Defines service providers for sandbox folio launchers.
 * 
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public abstract class SandboxCoreLauncher extends CoreServiceProviderWorker implements LauncherServiceProvider {
	/**
	 * The prefix of the message keys in the resource bundle.
	 */
	protected static final String messageKeyPrefix = "launcher.sandbox.launcher.";

	/**
	 * Defines mets templates.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	protected enum Template {
		mets_core("mets-core"), mets_file("mets-file"), mets_page("mets-page");

		/**
		 * The folder.
		 */
		private static final String folder = "templates/spi/launcher/sandbox-launcher/";

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
		 * @since 17
		 */
		private Template(String name) {
			this.name = name;
		}

		/**
		 * Returns the resource name.
		 * 
		 * @return The resource name.
		 * @since 17
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
		 * @since 17
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
	 * @since 17
	 */
	protected enum MetsPattern {
		create_date, software_creator, parameter, file_group, file_template, page_template,

		file_mime_type, file_id, file_name,

		page_id;

		/**
		 * Returns the pattern.
		 * 
		 * @return The pattern.
		 * @since 17
		 */
		public String getPattern() {
			return "[ocr4all-" + name() + "]";
		}
	}

	/**
	 * The service provider identifier.
	 */
	private final String identifier;

	/**
	 * Default constructor for a service provider for sandbox folio launcher.
	 * 
	 * @param resourceBundleKeyPrefix The prefix of the keys in the resource bundle.
	 * @param identifier              The identifier.
	 * @since 17
	 */
	public SandboxCoreLauncher(String resourceBundleKeyPrefix, String identifier) {
		super(resourceBundleKeyPrefix);

		this.identifier = identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#
	 * getCategories()
	 */
	@Override
	public List<String> getCategories() {
		return Arrays.asList("Sandbox launcher");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.core.ServiceProvider#getSteps()
	 */
	@Override
	public List<String> getSteps() {
		return Arrays.asList("initialization/sandbox/launcher");
	}

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
	protected void persistMets(Path path, String coreTemplate, String launcherArgument, String group, String file,
			String page) throws JsonProcessingException, IOException {
		Files.write(path, coreTemplate.replace(MetsPattern.create_date.getPattern(), MetsUtils.getFormattedDate())

				.replace(MetsPattern.software_creator.getPattern(), identifier + " v" + getVersion())

				.replace(MetsPattern.parameter.getPattern(), launcherArgument == null ? "" : launcherArgument.trim())

				.replace(MetsPattern.file_group.getPattern(), group)

				.replace(MetsPattern.file_template.getPattern(), file)

				.replace(MetsPattern.page_template.getPattern(), page).getBytes());
	}

}
