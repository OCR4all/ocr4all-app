/**
 * File:     CoreFolder.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.configuration
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     14.04.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CoreFolder is an immutable class that defines core folders.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class CoreFolder {
	/**
	 * The folder.
	 */
	protected final Path folder;

	/**
	 * Creates a core folder.
	 * 
	 * @param folder The folder.
	 * @since 1.8
	 */
	public CoreFolder(Path folder) {
		super();

		this.folder = folder.normalize();
	}

	/**
	 * Returns the path for given file.
	 * 
	 * @param file The file.
	 * @return The path.
	 * @since 1.8
	 */
	protected Path getPath(String file) {
		return Paths.get(folder.toString(), file).normalize();
	}

	/**
	 * Deletes the folder.
	 *
	 * @return True if the folder could be deleted.
	 * 
	 * @since 1.8
	 */
	protected boolean delete() {
		return delete(folder);
	}

	/**
	 * Deletes the folder.
	 *
	 * @param folder The folder to delete.
	 * @return True if the folder could be deleted.
	 * 
	 * @since 1.8
	 */
	public static boolean delete(Path folder) {
		try {
			Files.walk(folder).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Deletes the contents of the folder if it is a directory.
	 *
	 * @return True if the contents of the folder could be deleted.
	 * 
	 * @since 1.8
	 */
	protected boolean deleteContents() {
		return deleteContents(folder);
	}

	/**
	 * Deletes the contents of the folder if it is a directory.
	 *
	 * @param folder The folder to delete the contents.
	 * @return True if the contents of the folder could be deleted.
	 * @since 1.8
	 */
	public static boolean deleteContents(Path folder) {
		if (Files.isDirectory(folder))
			try {
				Files.walk(folder).sorted(Comparator.reverseOrder()).filter(Predicate.not(folder::equals))
						.map(Path::toFile).forEach(File::delete);

				return true;
			} catch (IOException e) {
				// Nothing to do
			}

		return false;
	}

	/**
	 * Returns true if the folder is a directory.
	 *
	 * @return True if the folder is a directory; false if the folder does not
	 *         exist, is not a directory, or it cannot be determined if the folder
	 *         is a directory or not.
	 * 
	 * @since 1.8
	 */
	public boolean isFolderDirectory() {
		return Files.isDirectory(folder);
	}

	/**
	 * Returns true if the folder is empty.
	 * 
	 * @return True if the folder is empty. On troubles returns false.
	 * @since 1.8
	 */
	public boolean isFolderEmpty() {
		if (isFolderDirectory())
			try (Stream<Path> entries = Files.list(folder)) {
				return !entries.findFirst().isPresent();
			} catch (IOException ioe) {
				// Nothing to do
			}

		return false;
	}

	/**
	 * Returns the folder.
	 *
	 * @return The folder.
	 * @since 1.8
	 */
	public Path getFolder() {
		return folder;
	}

	/**
	 * Returns the files in the folder. The folder path part is removed.
	 * 
	 * @return The files in the folder. Null on troubles.
	 * @since 1.8
	 */
	public List<String> listFiles() {
		return listFiles(1);
	}

	/**
	 * Returns the all files in the folder and its sub directories. The folder path
	 * part is removed.
	 * 
	 * @return The files in the folder and its sub directories. Null on troubles.
	 * @since 1.8
	 */
	public List<String> listAllFiles() {
		return listFiles(Integer.MAX_VALUE);
	}

	/**
	 * Returns the files in the folder and its sub directories till desired depth.
	 * The folder path part is removed.
	 * 
	 * @param depth The maximum number of directory levels to visit.
	 * @return The files in the folder. Null on troubles.
	 * @since 1.8
	 */
	public List<String> listFiles(int depth) {
		final int length = folder.toString().length() + 1;

		try (Stream<Path> stream = Files.walk(folder, depth)) {
			List<String> list = stream.filter(file -> !Files.isDirectory(file)).map(Path::toString)
					.map(path -> path.substring(length)).collect(Collectors.toList());

			Collections.sort(list, (o1, o2) -> o1.compareToIgnoreCase(o2));

			return list;
		} catch (IOException e) {
			return null;
		}
	}

}
