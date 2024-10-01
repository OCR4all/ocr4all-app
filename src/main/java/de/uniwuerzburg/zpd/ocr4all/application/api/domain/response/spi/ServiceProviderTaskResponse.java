/**
 * File:     ServiceProviderTaskResponse.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.09.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.domain.response.spi;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.communication.spi.ServiceProviderTask;
import de.uniwuerzburg.zpd.ocr4all.application.communication.spi.ServiceProviderTask.State;

/**
 * Defines boolean responses for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class ServiceProviderTaskResponse implements Serializable {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The state.
	 */
	private final ServiceProviderTask.State state;

	/**
	 * The throwable and its backtrace stack trace that indicates conditions that an
	 * application might want to examine.
	 */
	@JsonProperty("stack-trace")
	private final String stackTrace;

	/**
	 * The task standard output.
	 */
	@JsonProperty("standard-output")
	private final String standardOutput;

	/**
	 * The task standard error.
	 */
	@JsonProperty("standard-error")
	private final String standardError;

	/**
	 * Insert your text here
	 * 
	 * @since 17
	 */
	public ServiceProviderTaskResponse(ServiceProviderTask task) {
		super();

		state = task.getState();
		stackTrace = task.getStackTrace();
		standardOutput = task.getStandardOutput();
		standardError = task.getStandardError();
	}

	/**
	 * Returns the state.
	 *
	 * @return The state.
	 * @since 17
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the throwable and its backtrace stack trace that indicates conditions
	 * that an application might want to examine.
	 *
	 * @return The stack trace.
	 * @since 17
	 */
	public String getStackTrace() {
		return stackTrace;
	}

	/**
	 * Returns the task standard output.
	 *
	 * @return The task standard output.
	 * @since 17
	 */
	public String getStandardOutput() {
		return standardOutput;
	}

	/**
	 * Returns the task standard error.
	 *
	 * @return The task standard error.
	 * @since 17
	 */
	public String getStandardError() {
		return standardError;
	}

}
