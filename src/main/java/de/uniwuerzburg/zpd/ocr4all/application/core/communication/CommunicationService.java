/**
 * File:     CommunicationService.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.communication
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.communication;

import java.util.Hashtable;

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
	 * The microservice architecture.
	 */
	private final MicroserviceArchitecture microserviceArchitecture;
	
	/**
	 * The id of the registered event handler.
	 */
	private int id = 0;
	
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
			if (msa.isWebsocketSet()) {
				final String message = "ID '" + msa.getId() + "' (url " + msa.getUrl() + " / Websocket "
						+ msa.getWebsocket() + ")";

				if (hosts.containsKey(msa.getId()))
					logger.warn("ignored SPI microservice architecture - duplicate " + message);
				else {
					connectWebSocket(msa.getUrl(), msa.getWebsocket());
					hosts.put(msa.getId(), new MicroserviceArchitecture.Host(msa.getId(), msa.getUrl()));

					logger.info("registered SPI microservice architecture " + message);
				}
			}

		microserviceArchitecture = new MicroserviceArchitecture(this, hosts.values());
	}

	/**
	 * Connects to the WebSocket.
	 * 
	 * @param url   The url.
	 * @param topic The topic.
	 * @since 17
	 */
	private void connectWebSocket(String url, String topic) {
		WebSocketClient client = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(client);

		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		EventSPIMessageStompSessionHandler sessionHandler = new EventSPIMessageStompSessionHandler(topic, this);
		stompClient.connectAsync(url, sessionHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uniwuerzburg.zpd.ocr4all.application.core.communication.
	 * EventSPIMessageStompSessionHandler.EventHandler#handle(de.uniwuerzburg.zpd.
	 * ocr4all.application.communication.message.spi.EventSPI)
	 */
	@Override
	public void handle(EventSPI event) {
		// TODO: send event to registered clients
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
	public int register(String key, MicroserviceArchitecture.EventHandler handler) {
		// TODO Auto-generated method stub
		int id = ++this.id;
		
		
		
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uniwuerzburg.zpd.ocr4all.application.spi.env.MicroserviceArchitecture.
	 * EventController#unregister(int)
	 */
	@Override
	public void unregister(int id) {
		// TODO Auto-generated method stub

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

}
