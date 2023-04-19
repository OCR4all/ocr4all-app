/**
 * File:     SecureServer.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.security
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     06.09.2021
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import de.uniwuerzburg.zpd.ocr4all.application.core.util.Priority;

/**
 * Secures the server.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
public interface SecureServer extends Priority.Entity {
	/**
	 * Returns the module label for logging.
	 * 
	 * @return The module label.
	 * @since 1.8
	 */
	public String getModuleLabel();

	/**
	 * Returns the priority level. Beans implementing this interface are called in
	 * an order according to this priority. High priority is called first.
	 * 
	 * @return The priority level. If null, lowest priority is assumed.
	 * @since 1.8
	 */
	@Override
	public Priority getPriority();

	/**
	 * Authorize requests by restricting access based on
	 * {@link javax.servlet.http.HttpServletRequest} using
	 * {@link org.springframework.security.web.util.matcher.RequestMatcher}
	 * implementations (i.e. via URL patterns). The application roles are defined in
	 * {@link de.uniwuerzburg.zpd.ocr4all.application.core.security.AccountService.Role}.
	 *
	 * Example
	 * 
	 * <pre>
	 * return authorize.antMatchers(&quot;/administration/**&quot;).hasRole(&quot;COORD&quot;).antMatchers(&quot;/project/**&quot;).hasRole(&quot;USER&quot;);
	 * </pre>
	 * 
	 * @param authorize The {@link ExpressionUrlAuthorizationConfigurer} for the
	 *                  customization.
	 * @since 1.8
	 */
	public void authorizeRequests(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorize);
}
