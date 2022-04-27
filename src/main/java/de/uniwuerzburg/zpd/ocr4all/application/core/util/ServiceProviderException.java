/**
 * File:     ServiceProviderException.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.util
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     01.04.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.util;

/**
 * Defines service provider exceptions.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public class ServiceProviderException extends Exception {
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new service provider exception with {@code null} as its detail
	 * message. The cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 * 
	 * @since 1.8
	 */
	public ServiceProviderException() {
		super();
	}

	/**
	 * Constructs a new service provider exception with the specified detail
	 * message. The cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for later
	 *                retrieval by the {@link #getMessage()} method.
	 * 
	 * @since 1.8
	 */
	public ServiceProviderException(String message) {
		super(message);
	}

	/**
	 * Constructs a new service provider exception with the specified detail message
	 * and cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the
	 *                {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method). (A {@code null} value is
	 *                permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 * 
	 * @since 1.8
	 */
	public ServiceProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new service provider exception with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}). This
	 * constructor is useful for exceptions that are little more than wrappers for
	 * other throwables (for example,
	 * {@link java.security.PrivilegedActionException}).
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method). (A {@code null} value is permitted,
	 *              and indicates that the cause is nonexistent or unknown.)
	 * 
	 * @since 1.8
	 */
	public ServiceProviderException(Throwable cause) {
		super(cause);
	}

}
