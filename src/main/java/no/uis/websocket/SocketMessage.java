package no.uis.websocket;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Data structure sent via a websocket connection.
 *
 * @author Alan Rostem
 */
public class SocketMessage {
    private String messageType;
    private Object content;
    private String sender;

    /**
     * @return Message type string pseudo enumerator
     * @author Alan Rostem
     */
    public String getType() {
        return messageType;
    }

    /**
     * Set the pseudo message type enumerator when creating the message
     *
     * @param messageType Message type
     * @author Alan Rostem
     */
    public void setType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Retrieve the message content
     *
     * @return Any object
     * @author Alan Rostem
     */
    public Object getContent() {
        return content;
    }

    /**
     * Set the message content, preferably a map, string or primitive types.
     * @param content Content object
     * @author Alan Rostem
     */
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     * Retrieve the sender of the message
     * @return Sender ID string
     */
    public String getSender() {
        return sender;
    }

    /**
     * Configure the sender of the object
     * @param sender ID of the sender (or "server" when explicitly sending from the server)
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Convert the JSON string (if syntactically correct) to a map and return it
     * @return Map with string as key and object as value
     */
    public Map contentToMap() {
        if (content instanceof Map) {
            return (Map) content;
        }
        try {
            return new Gson().fromJson((String) content, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}