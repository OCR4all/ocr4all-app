/**
 * File:     TemporaryConfiguration.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     31.03.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.property.Temporary;

/**
 * Defines configurations for the temporary files/directories.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class TemporaryConfiguration extends CoreFolder {
	/**
	 * The prefix.
	 */
	private final String prefix;

	/**
	 * Creates a configuration for the temporary files/directories.
	 * 
	 * @param properties The ocr4all properties.
	 * @since 1.8
	 */
	public TemporaryConfiguration(Temporary properties) {
		super(Paths.get(properties.getFolder()));

		prefix = properties.getPrefix();

		ConfigurationService.initializeFolder(true, folder, "temporary");
	}

	/**
	 * Returns the prefix.
	 *
	 * @return The prefix.
	 * @since 1.8
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Creates a new empty file in the specified directory, using the prefix and
	 * suffix ".tmp" to generate its name. The resulting Path is associated with the
	 * same FileSystem as the given directory.
	 * 
	 * @return The path to the newly created file that did not exist before this
	 *         method was invoked.
	 * @throws IOException IOException Signals that an I/O exception of some sort
	 *                     has occurred.
	 * @since 1.8
	 */
	public Path getTemporaryFile() throws IOException {
		return Files.createTempFile(folder, prefix, null);
	}

	/**
	 * Creates a new empty file in the specified directory, using the prefix to
	 * generate its name. The resulting Path is associated with the same FileSystem
	 * as the given directory.
	 * 
	 * @param suffix The suffix string to be used in generating the file's name; may
	 *               be null, in which case ".tmp" is used.
	 * @return The path to the newly created file that did not exist before this
	 *         method was invoked.
	 * @throws IOException IOException Signals that an I/O exception of some sort
	 *                     has occurred.
	 * @since 1.8
	 */
	public Path getTemporaryFile(String suffix) throws IOException {
		return Files.createTempFile(folder, prefix, suffix);
	}

	/**
	 * Creates a new directory in the temporary directory, using the prefix to
	 * generate its name. The resulting Path is associated with the same FileSystem
	 * as the given directory.
	 * 
	 * @return The path to the newly created directory that did not exist before
	 *         this method was invoked
	 * @throws IOException Signals that an I/O exception of some sort has occurred.
	 * @since 1.8
	 */
	public Path getTemporaryDirectory() throws IOException {
		return Files.createTempDirectory(folder, prefix);
	}

}
