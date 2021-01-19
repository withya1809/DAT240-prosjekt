package no.uis.websocket;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;

/**
 * Spring component class which listens to socket events and logs out to the console.
 * @author Alan Rostem
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Event listener for when a socket has been connected.
     * @param event Automated Spring event object
     * @author Alan Rostem
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection.");
    }

    /**
     * Event listener for when a socket disconnects and erases it from the session.
     * @param event Automated Spring event object
     * @author Alan Rostem
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("room_id");
        if (username != null) {
            logger.info("User Disconnected: " + username);

            SocketMessage socketMessage = new SocketMessage();
            socketMessage.setType("LEAVE");
            socketMessage.setSender(username);

            messagingTemplate.convertAndSend(format("/channel/%s", roomId), socketMessage);
        }
    }
}
