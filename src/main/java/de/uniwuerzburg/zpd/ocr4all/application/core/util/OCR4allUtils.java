/**
 * File:     OCR4allUtils.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Defines ocr4all utilities.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class OCR4allUtils {

	/**
	 * Returns the exception stack trace as string.
	 * 
	 * @param exception The exception.
	 * @return The exception stack trace as string. Null if the given exception is
	 *         null.
	 * @since 1.8
	 */
	public static String getStackTrace(Exception exception) {
		if (exception == null)
			return null;
		else {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			exception.printStackTrace(printWriter);

			return stringWriter.toString();
		}
	}

	/**
	 * Returns the resource content as text.
	 * 
	 * @param name The resource name.
	 * @return The resource content as text.
	 * @throws IllegalArgumentException Throws if the resource could not be found.
	 * @throws IOException              If an I/O error occurs.
	 * @since 1.8
	 */
	public static String getResourceAsText(String name) throws IllegalArgumentException, IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream(name)))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}

		return resultStringBuilder.toString();
	}

	/**
	 * Returns an input stream for reading the specified resource.
	 * 
	 * @param name The resource name.
	 * @return An input stream for reading the specified resource.
	 * @throws IllegalArgumentException Throws if the resource could not be found.
	 * @since 1.8
	 */
	public static InputStream getResourceAsStream(String name) throws IllegalArgumentException {
		ClassLoader classLoader = OCR4allUtils.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(name);

		if (inputStream == null) {
			throw new IllegalArgumentException("resource not found: " + name);
		} else {
			return inputStream;
		}
	}

	/**
	 * Returns the input stream content.
	 * 
	 * @param inputStream The input stream.
	 * @return The input stream content.
	 * @since 1.8
	 */
	public static String getString(InputStream inputStream) {
		StringBuffer buffer = new StringBuffer();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line = null;
			while ((line = reader.readLine()) != null)
				if (line != null) {
					if (buffer.length() > 0)
						buffer.append(System.lineSeparator());

					buffer.append(line);
				}
		} catch (IOException e) {
			// The stream was closed
		}

		return buffer.toString();
	}

	/**
	 * Returns the file name without extension. If the file name is already an
	 * extension, then returns the file name.
	 * 
	 * @param fileName The file name.
	 * @return The file name without extension.
	 * @since 1.8
	 */
	public static String getNameWithoutExtension(String fileName) {
		if (fileName == null)
			return null;
		else {
			int dotIndex = fileName.lastIndexOf('.');
			return (dotIndex <= 0) ? fileName : fileName.substring(0, dotIndex);
		}
	}

	/**
	 * Returns the file names in the given folder.
	 * 
	 * @param folder The folder.
	 * @return The file names.
	 * @throws IOException Throws if an I/O error occurs when opening the folder.
	 * @since 17
	 */
	public static Set<String> getFileNames(Path folder) throws IOException {
		try (Stream<Path> stream = Files.list(folder)) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName).map(Path::toString)
					.collect(Collectors.toSet());
		}
	}

	/**
	 * Returns the files walking the file tree rooted at a given starting folder.
	 * 
	 * @param folder    The starting folder.
	 * @param extension If non null, filter the files with this extension. The upper
	 *                  and lower case does not matter.
	 * @return The files walking the file tree rooted at a given starting folder.
	 * @throws IOException Throws if an I/O error is thrown when accessing the
	 *                     starting file.
	 * @since 1.8
	 */
	public static List<Path> getFiles(Path folder, String extension) throws IOException {
		final String fileExtension = extension == null || extension.isBlank() ? null
				: "." + extension.toLowerCase().trim();

		try (Stream<Path> walk = Files.walk(folder)) {
			return walk.filter(p -> {
				return !Files.isDirectory(p)
						&& (fileExtension == null || p.toString().toLowerCase().endsWith(fileExtension));
			}).collect(Collectors.toList());
		}
	}

	/**
	 * Returns the directories in given folder.
	 * 
	 * @param folder The folder to returns the directories.
	 * @return The directories in given folder.
	 * @throws IOException Throws if an I/O error occurs when opening the directory.
	 * @since 1.8
	 */
	public static List<Path> getDirectories(Path folder) throws IOException {
		return Files.list(folder).filter(file -> Files.isDirectory(file)).collect(Collectors.toList());
	}

	/**
	 * Returns an immutable universally unique identifier ({@code UUID}). The
	 * {@code UUID} represents a 128-bit value and is generated using a
	 * cryptographically strong pseudo random number generator.
	 * 
	 * @return A randomly generated {@code UUID}.
	 * @since 1.8
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Defines the metadata to be compressed in a zipped file.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public static class ZipMetadata {
		/**
		 * The metadata. The key is the file name, which must not contain the character
		 * "/". The value is an input stream that is not null.
		 */
		private final Hashtable<String, InputStream> metadata = new Hashtable<>();

		/**
		 * Default constructor for metadata to be compressed in a zipped file.
		 * 
		 * @since 17
		 */
		public ZipMetadata() {
			super();
		}

		/**
		 * Creates metadata to be compressed in a zipped file.
		 * 
		 * @param fileName The non blank file name, which must not contain the character
		 *                 "/".
		 * @param data     The input stream that is not null.
		 * @throws IllegalArgumentException Throws if the the arguments are not
		 *                                  consistent.
		 * @since 17
		 */
		public ZipMetadata(String fileName, InputStream data) throws IllegalArgumentException {
			super();

			if (!add(fileName, data))
				throw new IllegalArgumentException("ZipMetadata: the arguments are not consistent.");
		}

		/**
		 * Adds the metadata.
		 * 
		 * @param fileName The non blank file name, which must not contain the character
		 *                 "/".
		 * @param data     The input stream that is not null.
		 * @return True if the metadata is consistent.
		 * @since 17
		 */
		public boolean add(String fileName, InputStream data) {
			if (fileName != null && !fileName.isBlank() && !fileName.contains("/") && data != null) {
				metadata.put(fileName.trim(), data);

				return true;
			} else
				return false;
		}

		/**
		 * Return the sorted file names.
		 * 
		 * @return The sorted file names.
		 * @since 17
		 */
		List<String> getFileNames() {
			List<String> fileNames = new ArrayList<>(metadata.keySet());

			Collections.sort(fileNames, String.CASE_INSENSITIVE_ORDER);

			return fileNames;
		}

		/**
		 * Returns the input stream of given file name.
		 * 
		 * @param fileName The file name.
		 * @return Null if unknown.
		 * @since 17
		 */
		InputStream getInputStream(String fileName) {
			return fileName == null ? null : metadata.get(fileName);
		}
	}

	/**
	 * Defines functional interfaces to filter zip entries.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	@FunctionalInterface
	public interface ZipFilter {
		/**
		 * Returns true if the entry is to be compressed.
		 * 
		 * @param entry The entry to compress.
		 * @return True if the entry is to be compressed.
		 * @since 17
		 */
		public boolean accept(File entry);
	}

	/**
	 * Zips the entry and writes it to the output stream.
	 * 
	 * @param entry               The entry to zip if non null.
	 * @param isSkipRootDirectory True if skip the root directory.
	 * @param outputStream        The output stream for writing the zipped entry. if
	 *                            no filter is used..
	 * @throws IOException Throws if the entry can not be zipped.
	 * @since 17
	 */
	public static void zip(Path entry, boolean isSkipRootDirectory, OutputStream outputStream) throws IOException {
		zip(entry, isSkipRootDirectory, outputStream, null);
	}

	/**
	 * Zips the entry and writes it to the output stream.
	 * 
	 * @param entry               The entry to zip if non null.
	 * @param isSkipRootDirectory True if skip the root directory.
	 * @param outputStream        The output stream for writing the zipped entry.
	 * @param filter              The filter for the entries to be compressed. Null
	 *                            if no filter is used..
	 * @throws IOException Throws if the entry can not be zipped.
	 * @since 17
	 */
	public static void zip(Path entry, boolean isSkipRootDirectory, OutputStream outputStream, ZipFilter filter)
			throws IOException {
		zip(entry, isSkipRootDirectory, outputStream, filter, null);
	}

	/**
	 * Zips the entry and writes it to the output stream.
	 * 
	 * @param entry               The entry to zip if non null.
	 * @param isSkipRootDirectory True if skip the root directory.
	 * @param outputStream        The output stream for writing the zipped entry.
	 * @param filter              The filter for the entries to be compressed. Null
	 *                            if no filter is used.
	 * @param metadata            The metadata to be compressed in a zipped file.
	 *                            Null if no metadata is required.
	 * @throws IOException Throws if the entry can not be zipped.
	 * @since 17
	 */
	public static void zip(Path entry, boolean isSkipRootDirectory, OutputStream outputStream, ZipFilter filter,
			ZipMetadata metadata) throws IOException {
		if (entry != null)
			try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);) {
				if (metadata != null)
					for (String fileName : metadata.getFileNames())
						zipEntry(metadata.getInputStream(fileName), fileName, zipOutputStream);

				File file = entry.toFile();
				if (isSkipRootDirectory && file.isDirectory())
					for (File child : file.listFiles())
						zipEntry(child, child.getName(), zipOutputStream, filter);
				else
					zipEntry(file, entry.getFileName().toString(), zipOutputStream, filter);

				outputStream.flush();
			}
	}

	/**
	 * Zips the entry.
	 * 
	 * @param inputStream     The entry input stream.
	 * @param fileName        The file name.
	 * @param zipOutputStream The output stream filter for writing files in the ZIP.
	 * @throws IOException Throws if the entry can not be zipped.
	 * @since 17
	 */
	private static void zipEntry(InputStream inputStream, String fileName, ZipOutputStream zipOutputStream)
			throws IOException {
		zipOutputStream.putNextEntry(new ZipEntry(fileName));

		inputStream.transferTo(zipOutputStream);

		zipOutputStream.closeEntry();
	}

	/**
	 * Zips the entry.
	 * 
	 * @param file            The source file.
	 * @param fileName        The source file name.
	 * @param zipOutputStream The output stream filter for writing files in the ZIP.
	 * @param filter          The filter for the entries to be compressed.
	 * @throws IOException Throws if the entry can not be zipped.
	 * @since 17
	 */
	private static void zipEntry(File file, String fileName, ZipOutputStream zipOutputStream, ZipFilter filter)
			throws IOException {
		if (filter == null || filter.accept(file)) {
			if (file.isDirectory()) {
				zipOutputStream.putNextEntry(new ZipEntry(fileName + (fileName.endsWith("/") ? "" : "/")));
				zipOutputStream.closeEntry();

				for (File child : file.listFiles())
					zipEntry(child, fileName + "/" + child.getName(), zipOutputStream, filter);
			} else {
				zipOutputStream.putNextEntry(new ZipEntry(fileName));

				if (Files.isReadable(file.toPath()))
					Files.copy(file.toPath(), zipOutputStream);

				zipOutputStream.closeEntry();
			}
		}
	}
}
