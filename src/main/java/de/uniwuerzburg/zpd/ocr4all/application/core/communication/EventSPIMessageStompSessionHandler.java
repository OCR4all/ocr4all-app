/**
 * File:     EventSPIMessageStompSessionHandler.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.communication
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.communication;

import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.uniwuerzburg.zpd.ocr4all.application.communication.message.spi.EventSPI;

/**
 * Defines message handler for Stream Text-Oriented Messaging Protocol (STOMP).
 * Once a connection is established, its subscribe to the desired topic.
 *
 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
 * @version 1.0
 * @since 17
 */
public class EventSPIMessageStompSessionHandler extends StompSessionHandlerAdapter {
	/**
	 * The logger.
	 */
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EventSPIMessageStompSessionHandler.class);

	/**
	 * The STOMP over WebSocket client.
	 */
	private final WebSocketStompClient stompClient;

	/**
	 * The url.
	 */
	private final String url;

	/**
	 * The topic.
	 */
	private final String topic;

	/**
	 * The SPI event handler. Null if no events are to be handled.
	 */
	private final EventHandler eventHandler;

	/**
	 * Creates a message handler for STOMP.
	 * 
	 * @param stompClient  The STOMP over WebSocket client.
	 * @param url          The url.
	 * @param topic        The topic to subscribe once a connection is established.
	 * @param eventHandler The SPI event handler. Null if no events are to be
	 *                     handled.
	 * @since 17
	 */
	public EventSPIMessageStompSessionHandler(WebSocketStompClient stompClient, String url, String topic,
			EventHandler eventHandler) {
		super();

		this.stompClient = stompClient;
		this.url = url;
		this.topic = topic;
		this.eventHandler = eventHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter#
	 * afterConnected(org.springframework.messaging.simp.stomp.StompSession,
	 * org.springframework.messaging.simp.stomp.StompHeaders)
	 */
	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		logger.info("STOMP session established: " + session.getSessionId());

		session.subscribe(topic, this);

		logger.info("STOMP session " + session.getSessionId() + ": subscribed to topic " + topic);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter#
	 * handleException(org.springframework.messaging.simp.stomp.StompSession,
	 * org.springframework.messaging.simp.stomp.StompCommand,
	 * org.springframework.messaging.simp.stomp.StompHeaders, byte[],
	 * java.lang.Throwable)
	 */
	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		logger.warn("STOMP session " + session.getSessionId() + ": exception", exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter#
	 * handleTransportError(org.springframework.messaging.simp.stomp.StompSession,
	 * java.lang.Throwable)
	 */
	@Override
	public void handleTransportError(StompSession session, Throwable exception) {
		logger.warn("STOMP session " + session.getSessionId() + ": transport error - " + exception.getMessage());

		new Thread(new Runnable() {
			/*
			 * (non-Javadoc)
			 *
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// Nothing to do
				}
				stompClient.connectAsync(url, EventSPIMessageStompSessionHandler.this);
			}
		}).start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter#
	 * getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders)
	 */
	@Override
	public Type getPayloadType(StompHeaders headers) {
		return EventSPI.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter#
	 * handleFrame(org.springframework.messaging.simp.stomp.StompHeaders,
	 * java.lang.Object)
	 */
	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		EventSPI event = (EventSPI) payload;

		logger.debug("STOMP SPI event " + event.getType().name() + " (" + event.getCreatedAt() + "): key "
				+ event.getKey() + " / content - " + event.getMessage().getContent());

		if (eventHandler != null)
			eventHandler.handle(event);
	}

	/**
	 * Defines functional interfaces to handle spi events.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	@FunctionalInterface
	public interface EventHandler {
		/**
		 * Handles the spi event.
		 * 
		 * @param event The spi event to handle.
		 * @since 17
		 */
		public void handle(EventSPI event);
	}
}
