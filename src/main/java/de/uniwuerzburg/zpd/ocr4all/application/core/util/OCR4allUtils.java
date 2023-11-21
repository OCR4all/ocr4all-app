/**
 * File:     OCR4allUtils.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     23.03.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}
