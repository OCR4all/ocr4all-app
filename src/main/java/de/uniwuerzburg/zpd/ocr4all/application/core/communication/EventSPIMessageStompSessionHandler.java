/**
 * File:     EventSPIMessageStompSessionHandler.java
 * Package:  de.uniwuerzburg.zpd.ocr4all.application.core.communication
 * 
 * Author:   Herbert Baier (herbert.baier@uni-wuerzburg.de)
 * Date:     20.03.2024
 */
package de.uniwuerzburg.zpd.ocr4all.application.core.communication;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Hashtable;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.uniwuerzburg.zpd.ocr4all.application.communication.message.spi.EventSPI;
import de.uniwuerzburg.zpd.ocr4all.application.core.configuration.ApplicationConfiguration;

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
	 * The id.
	 */
	private final String id;

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
	 * The resilience configurations.
	 */
	private final ApplicationConfiguration.SPI.MSA.WebSocket.Resilience resilience;

	/**
	 * The number of connection attempts, since creation or last successful
	 * connection.
	 */
	private int connectionAttempts = 1;

	/**
	 * The STOMP session.
	 */
	private StompSession session = null;

	/**
	 * The creation time.
	 */
	private final Date createdAt = new Date();

	/**
	 * The connected time.
	 */
	private Date connectedAt = null;

	/**
	 * The error time.
	 */
	private Date errorAt = null;

	/**
	 * The number of events.
	 */
	private int[] numberEvents = new int[EventSPI.Type.values().length];

	/**
	 * Creates a message handler for STOMP.
	 * 
	 * @param stompClient  The STOMP over WebSocket client.
	 * @param id           The id.
	 * @param url          The url.
	 * @param topic        The topic to subscribe once a connection is established.
	 * @param eventHandler The SPI event handler. Null if no events are to be
	 *                     handled.
	 * @param resilience   The WebSocket resilience.
	 * @since 17
	 */
	public EventSPIMessageStompSessionHandler(WebSocketStompClient stompClient, String id, String url, String topic,
			EventHandler eventHandler, ApplicationConfiguration.SPI.MSA.WebSocket.Resilience resilience) {
		super();

		this.stompClient = stompClient;

		this.id = id;
		this.url = url;
		this.topic = topic;

		this.eventHandler = eventHandler;
		this.resilience = resilience;
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
		this.session = session;

		connectedAt = new Date();
		connectionAttempts = 0;

		session.subscribe(topic, this);

		logger.info("STOMP session " + session.getSessionId() + "established: subscribed to topic " + topic);
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
		errorAt = new Date();
		++connectionAttempts;

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
					Thread.sleep(
							connectionAttempts <= resilience.getMaxAttempts() ? resilience.getDelayBetweenAttempts()
									: resilience.getWaitDuration());
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

		numberEvents[event.getType().ordinal()]++;

		logger.debug("STOMP SPI event " + event.getType().name() + " (" + event.getCreatedAt() + "): key "
				+ event.getKey() + " / content - " + event.getMessage().getContent());

		if (eventHandler != null)
			eventHandler.handle(event);
	}

	/**
	 * Returns the monitor.
	 * 
	 * @return The monitor.
	 * @since 17
	 */
	public Monitor getMonitor() {
		return new Monitor();
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

	/**
	 * Defines monitors.
	 *
	 * @author <a href="mailto:herbert.baier@uni-wuerzburg.de">Herbert Baier</a>
	 * @version 1.0
	 * @since 17
	 */
	public class Monitor {

		/**
		 * The id.
		 */
		private final String id = EventSPIMessageStompSessionHandler.this.id;

		/**
		 * The url.
		 */
		private final String url = EventSPIMessageStompSessionHandler.this.url;

		/**
		 * The topic.
		 */
		private final String topic = EventSPIMessageStompSessionHandler.this.topic;

		/**
		 * The creation time.
		 */
		private final Date createdAt = EventSPIMessageStompSessionHandler.this.createdAt;

		/**
		 * The connected time.
		 */
		private final Date connectedAt = EventSPIMessageStompSessionHandler.this.connectedAt;

		/**
		 * The error time.
		 */
		private final Date errorAt = EventSPIMessageStompSessionHandler.this.errorAt;

		/**
		 * The number of connection attempts, since creation or last successful
		 * connection.
		 */
		private final int connectionAttempts = EventSPIMessageStompSessionHandler.this.connectionAttempts;

		/**
		 * The session id.
		 */
		private final String sessionId = EventSPIMessageStompSessionHandler.this.session == null ? null
				: EventSPIMessageStompSessionHandler.this.session.getSessionId();

		/**
		 * True if session is connected.
		 */
		private final boolean isConnected = EventSPIMessageStompSessionHandler.this.session != null
				&& EventSPIMessageStompSessionHandler.this.session.isConnected();

		/**
		 * The number of events by type.
		 */
		private final Hashtable<EventSPI.Type, Integer> events = new Hashtable<>();
		{
			for (int i = 0; i < numberEvents.length; i++)
				events.put(EventSPI.Type.values()[i], numberEvents[i]);
		}

		/**
		 * Returns the session id.
		 *
		 * @return The session id.
		 * @since 17
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the url.
		 *
		 * @return The url.
		 * @since 17
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Returns the topic.
		 *
		 * @return The topic.
		 * @since 17
		 */
		public String getTopic() {
			return topic;
		}

		/**
		 * Returns the creation time.
		 *
		 * @return The creation time.
		 * @since 17
		 */
		public Date getCreatedAt() {
			return createdAt;
		}

		/**
		 * Returns the connected time.
		 *
		 * @return The connected time.
		 * @since 17
		 */
		public Date getConnectedAt() {
			return connectedAt;
		}

		/**
		 * Returns the error time.
		 *
		 * @return The error time.
		 * @since 17
		 */
		public Date getErrorAt() {
			return errorAt;
		}

		/**
		 * Returns the number of connection attempts, since creation or last successful
		 * connection.
		 *
		 * @return The number of connection attempts.
		 * @since 17
		 */
		public int getConnectionAttempts() {
			return connectionAttempts;
		}

		/**
		 * Returns the sessionId.
		 *
		 * @return The sessionId.
		 * @since 17
		 */
		public String getSessionId() {
			return sessionId;
		}

		/**
		 * Returns true if session is connected.
		 *
		 * @return True if session is connected.
		 * @since 17
		 */
		public boolean isConnected() {
			return isConnected;
		}

		/**
		 * Returns the number of events by type.
		 *
		 * @return The number of events by type.
		 * @since 17
		 */
		public Hashtable<EventSPI.Type, Integer> getEvents() {
			return events;
		}

	}
}
