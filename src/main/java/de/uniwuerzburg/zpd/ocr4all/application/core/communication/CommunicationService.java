/**
 * File:     CommunicationService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.communication
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.communication;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.uniwuerzburg.zpd.ocr4all.application.communication.message.spi.EventSPI;
import de.uniwuerzburg.zpd.ocr4all.application.core.CoreService;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ApplicationConfiguration;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ConfigurationService;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture;
import de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture.EventHandler;

/**
 * Defines communication services.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
@Service
@ApplicationScope
public class CommunicationService extends CoreService
		implements EventSPIMessageStompSessionHandler.EventHandler, MicroserviceArchitecture.EventController {
	/**
	 * The STOMP message handlers.
	 */
	private final List<EventSPIMessageStompSessionHandler> stompSessionHandler = new ArrayList<>();

	/**
	 * The microservice architecture.
	 */
	private final MicroserviceArchitecture microserviceArchitecture;

	/**
	 * The id of the registered event handler.
	 */
	private int id = 0;

	/**
	 * The handler id to key mapping. The key is the ID of the registered handler
	 * and the value is its key.
	 */
	private final Hashtable<Integer, String> handlerMapping = new Hashtable<>();

	/**
	 * The registered handlers. The key is the event key and the value the handlers
	 * registered under this key.
	 */
	private final Hashtable<String, List<Handler>> handlers = new Hashtable<>();

	/**
	 * Creates a communication service.
	 * 
	 * @param configurationService The configuration service.
	 * @since 17
	 */
	public CommunicationService(ConfigurationService configurationService) {
		super(CommunicationService.class, configurationService);

		final Hashtable<String, MicroserviceArchitecture.Host> hosts = new Hashtable<>();

		for (ApplicationConfiguration.SPI.MSA msa : configurationService.getApplication().getSpi().getMsa())
			if (msa.isWebSocketSet()) {
				final String url = "ws://" + msa.getUrl() + msa.getWebSocket().getEndPoint();

				final String message = "ID '" + msa.getId() + "', url " + url + ", topic "
						+ msa.getWebSocket().getTopic();

				if (hosts.containsKey(msa.getId()))
					logger.warn("ignored SPI microservice architecture " + message + " - duplicated ID");
				else
					try {
						stompSessionHandler.add(connectWebSocket(msa.getId(), url, msa.getWebSocket().getTopic(),
								msa.getWebSocket().getResilience()));

						hosts.put(msa.getId(), new MicroserviceArchitecture.Host(msa.getId(), msa.getUrl()));

						logger.info("registered SPI microservice architecture " + message);
					} catch (Exception e) {
						logger.warn("ignored SPI microservice architecture " + message + " - " + e.getMessage());
					}
			}

		microserviceArchitecture = new MicroserviceArchitecture(this, hosts.values());
	}

	/**
	 * Connects to the WebSocket.
	 * 
	 * @param id         The id.
	 * @param url        The url.
	 * @param topic      The topic.
	 * @param resilience The WebSocket resilience.
	 * @return The STOMP message handler.
	 * @since 17
	 */
	private EventSPIMessageStompSessionHandler connectWebSocket(String id, String url, String topic,
			ApplicationConfiguration.SPI.MSA.WebSocket.Resilience resilience) {
		WebSocketClient client = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(client);

		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		EventSPIMessageStompSessionHandler sessionHandler = new EventSPIMessageStompSessionHandler(stompClient, id, url,
				topic, this, resilience);
		stompClient.connectAsync(url, sessionHandler);

		return sessionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.communication.
	 * EventSPIMessageStompSessionHandler.EventHandler#handle(de.uniwuerzburg.zpd.
	 * ocr4all.application.communication.message.spi.EventSPI)
	 */
	@Override
	synchronized public void handle(EventSPI event) {
		List<Handler> workers;
		synchronized (handlers) {
			workers = handlers.get(event.getKey());

			if (workers != null)
				workers = new ArrayList<>(workers);
		}

		if (workers != null)
			for (Handler handler : new ArrayList<>(workers))
				handler.getEventHandler().handle(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture.
	 * EventController#register(java.lang.String,
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture.
	 * EventHandler)
	 */
	@Override
	synchronized public int register(String key, MicroserviceArchitecture.EventHandler handler) {
		if (key != null && handler != null) {
			int id = ++this.id;

			synchronized (handlerMapping) {
				synchronized (handlers) {
					handlerMapping.put(id, key);

					List<Handler> workers = handlers.get(key);
					if (workers == null) {
						workers = new ArrayList<>();
						handlers.put(key, workers);
					}

					workers.add(new Handler(id, handler));
				}
			}

			return id;
		} else
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture.
	 * EventController#unregister(int)
	 */
	@Override
	synchronized public void unregister(int id) {
		String key = handlerMapping.get(id);

		if (key != null)
			synchronized (handlerMapping) {
				synchronized (handlers) {
					List<Handler> workers = handlers.get(key);
					workers.removeIf(handler -> handler.isId(id));

					if (workers.isEmpty()) {
						handlerMapping.remove(id);
						handlers.remove(key);
					}
				}
			}
	}

	/**
	 * Returns the STOMP message handler monitors.
	 * 
	 * @return The STOMP message handler monitors.
	 * @since 17
	 */
	public List<EventSPIMessageStompSessionHandler.Monitor> getStompSessionHandlerMonitors() {
		List<EventSPIMessageStompSessionHandler.Monitor> monitors = new ArrayList<>();

		for (EventSPIMessageStompSessionHandler handler : stompSessionHandler)
			monitors.add(handler.getMonitor());

		return monitors;
	}

	/**
	 * Returns the microservice architecture.
	 *
	 * @return The microservice architecture.
	 * @since 17
	 */
	public MicroserviceArchitecture getMicroserviceArchitecture() {
		return microserviceArchitecture;
	}

	/**
	 * Handler is an immutable class that defines handlers.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	private static class Handler {
		/**
		 * The id.
		 */
		private final int id;

		/**
		 * The event handler.
		 */
		private final MicroserviceArchitecture.EventHandler eventHandler;

		/**
		 * Create a handler.
		 * 
		 * @param id           The id.
		 * @param eventHandler The event handler.
		 * @since 17
		 */
		Handler(int id, EventHandler eventHandler) {
			super();
			this.id = id;
			this.eventHandler = eventHandler;
		}

		/**
		 * Returns true if the given id matches the handler id.
		 *
		 * @return True if the given id matches the handler id.
		 * @since 17
		 */
		boolean isId(int id) {
			return id == this.id;
		}

		/**
		 * Returns the event handler.
		 *
		 * @return The event handler.
		 * @since 17
		 */
		MicroserviceArchitecture.EventHandler getEventHandler() {
			return eventHandler;
		}

	}
}
