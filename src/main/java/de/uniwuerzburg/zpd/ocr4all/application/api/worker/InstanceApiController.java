/**
 * File:     InstanceApiController.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.api.worker
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     03.02.2022
 */
package de.uniwuerzburg.zpd.ocr4all.application.api.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.core.security.SecurityService;
import de.uniwuerzburg.zpd.ocr4all.application.persistence.Instance;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Defines instance controllers for the api.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 1.8
 */
@Profile("api")
@Tag(name = "instance", description = "the instance API")
@RestController
@RequestMapping(path = InstanceApiController.contextPath, produces = CoreApiController.applicationJson)
public class InstanceApiController extends CoreApiController {
	/**
	 * The context path.
	 */
	public static final String contextPath = apiContextPathVersion_1_0 + "/instance";

	/**
	 * Creates an instance controller for the api.
	 * 
	 * @param configurationService The configuration service.
	 * @param securityService      The security service.
	 * @since 1.8
	 */
	public InstanceApiController(ConfigurationService configurationService, SecurityService securityService) {
		super(InstanceApiController.class, configurationService, securityService);
	}

	/**
	 * Returns the instance main information.
	 * 
	 * @return The instance main information.
	 * @since 1.8
	 */
	@Operation(summary = "returns the instance main information")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Information", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = InstanceResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping
	public ResponseEntity<InstanceResponse> info() {
		try {
			return ResponseEntity.ok().body(new InstanceResponse(configurationService.getInstance(),
					securityService.isSecured(), configurationService.getApplication().getViewLanguages()));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Returns the instance configuration.
	 * 
	 * @return The instance configuration.
	 * @since 1.8
	 */
	@Operation(summary = "returns the instance configuration")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Configuration", content = {
			@Content(mediaType = CoreApiController.applicationJson, schema = @Schema(implementation = ConcfigurationResponse.class)) }),
			@ApiResponse(responseCode = "503", description = "Service Unavailable", content = @Content) })
	@GetMapping(configurationRequestMapping)
	public ResponseEntity<ConcfigurationResponse> configuration() {
		try {
			return ResponseEntity.ok().body(new ConcfigurationResponse(configurationService));
		} catch (Exception ex) {
			log(ex);

			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * Defines instance responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class InstanceResponse extends Instance {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * True if the application is secured.
		 */
		private boolean isSecured;

		/**
		 * The view languages.
		 */
		@JsonProperty("view-languages")
		private List<String> viewLanguages;

		/**
		 * Creates an instance responses for the api.
		 * 
		 * @param instance      The instance.
		 * @param isSecured     True if the application is secured.
		 * @param viewLanguages The view languages.
		 * @since 1.8
		 */
		public InstanceResponse(Instance instance, boolean isSecured, List<String> viewLanguages) {
			super(instance.getId(), instance.getName());

			this.isSecured = isSecured;
			this.viewLanguages = viewLanguages;
		}

		/**
		 * Returns true if the application is secured.
		 *
		 * @return True if the application is secured.
		 * @since 1.8
		 */
		public boolean isSecured() {
			return isSecured;
		}

		/**
		 * Set to true if the application is secured.
		 *
		 * @param isSecured The secured flag to set.
		 * @since 1.8
		 */
		public void setSecured(boolean isSecured) {
			this.isSecured = isSecured;
		}

		/**
		 * Returns the view languages.
		 *
		 * @return The view languages.
		 * @since 1.8
		 */
		public List<String> getViewLanguages() {
			return viewLanguages;
		}

		/**
		 * Set the view languages.
		 *
		 * @param viewLanguages The view languages to set.
		 * @since 1.8
		 */
		public void setViewLanguages(List<String> viewLanguages) {
			this.viewLanguages = viewLanguages;
		}

	}

	/**
	 * Defines configuration responses for the api.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 1.8
	 */
	public static class ConcfigurationResponse implements Serializable {
		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The charset.
		 */
		private String charset;

		/**
		 * The locale.
		 */
		private String locale;

		/**
		 * The folders.
		 */
		private List<FolderResponse> folders;

		/**
		 * Creates a configuration responses for the api.
		 * 
		 * @param service       The configuration service.
		 * @param isSecured     True if the application is secured.
		 * @param viewLanguages The view languages.
		 * @since 1.8
		 */
		public ConcfigurationResponse(ConfigurationService service) {
			super();

			charset = service.getApplication().getCharset().displayName();
			locale = service.getApplication().getLocale().toString();

			folders = new ArrayList<>();

			folders.add(
					new FolderResponse(FolderResponse.Type.workspace, service.getWorkspace().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.projects,
					service.getWorkspace().getProjects().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.workflows,
					service.getWorkspace().getWorkflows().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.exchange, service.getExchange().getFolder().toString()));
			folders.add(
					new FolderResponse(FolderResponse.Type.repository, service.getRepository().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.data, service.getData().getFolder().toString()));
			folders.add(new FolderResponse(FolderResponse.Type.opt, service.getOpt().getFolder().toString()));
			folders.add(
					new FolderResponse(FolderResponse.Type.temporary, service.getTemporary().getFolder().toString()));
		}

		/**
		 * Returns the charset.
		 *
		 * @return The charset.
		 * @since 17
		 */
		public String getCharset() {
			return charset;
		}

		/**
		 * Set the charset.
		 *
		 * @param charset The charset to set.
		 * @since 17
		 */
		public void setCharset(String charset) {
			this.charset = charset;
		}

		/**
		 * Returns the locale.
		 *
		 * @return The locale.
		 * @since 17
		 */
		public String getLocale() {
			return locale;
		}

		/**
		 * Set the locale.
		 *
		 * @param locale The locale to set.
		 * @since 17
		 */
		public void setLocale(String locale) {
			this.locale = locale;
		}

		/**
		 * Returns the folders.
		 *
		 * @return The folders.
		 * @since 17
		 */
		public List<FolderResponse> getFolders() {
			return folders;
		}

		/**
		 * Set the folders.
		 *
		 * @param folders The folders to set.
		 * @since 17
		 */
		public void setFolders(List<FolderResponse> folders) {
			this.folders = folders;
		}

		/**
		 * Defines folder responses for the api.
		 *
		 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
		 * @version 1.0
		 * @since 1.8
		 */
		public static class FolderResponse implements Serializable {
			/**
			 * The serial version UID.
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * The types.
			 */
			public enum Type {
				workspace, projects, workflows, exchange, repository, data, opt, temporary
			}

			/**
			 * The type.
			 */
			private Type type;

			/**
			 * The folder.
			 */
			private String folder;

			/**
			 * Creates a folder responses for the api.
			 * 
			 * @param type   The type.
			 * @param folder The folder.
			 * @since 17
			 */
			public FolderResponse(Type type, String folder) {
				super();

				this.type = type;
				this.folder = folder;
			}

			/**
			 * Returns the type.
			 *
			 * @return The type.
			 * @since 17
			 */
			public Type getType() {
				return type;
			}

			/**
			 * Set the type.
			 *
			 * @param type The type to set.
			 * @since 17
			 */
			public void setType(Type type) {
				this.type = type;
			}

			/**
			 * Returns the folder.
			 *
			 * @return The folder.
			 * @since 17
			 */
			public String getFolder() {
				return folder;
			}

			/**
			 * Set the folder.
			 *
			 * @param folder The folder to set.
			 * @since 17
			 */
			public void setFolder(String folder) {
				this.folder = folder;
			}

		}

	}
}
