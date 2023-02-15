/**
 * File:     OCR4allApplication.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application
 * 
 * Author:   Herbert Baier
 * Date:     03.11.2020
 */
package de.uniwuerzburg.zpd.ocr4all.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Defines Spring boot-based tests. Launches the main configuration class
 * {@link OCR4allApplication}, this means, the class with annotation
 * {@code @SpringBootApplication}.
 *
 * @author Herbert Baier
 * @version 1.0
 * @since 1.8
 */
@SpringBootTest
class OCR4allApplicationTests {

	/**
	 * Test if the application is able to load the Spring context successfully and
	 * if the required beans for active profiles are available.
	 * 
	 * @since 1.8
	 */
	@Test
	void contextLoads() {
	}

}
