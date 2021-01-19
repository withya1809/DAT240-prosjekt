/**
 * Composed class containing all websocket functionality in abstract form.
 */
class SocketConnector {
    /**
     * @param endpoint {string} - Endpoint, defaulted to "/ws"
     */
    constructor(endpoint = '/ws') {
        this.connected = false;
        this.subscriptions = {};
        this.socket = new SockJS(endpoint);
        this.stomp = Stomp.over(this.socket);
        const _this = this;
        this.stomp.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            _this.connected = true;
            for (let listener of _this.stompListenerQueue) {
                _this.subscribe(listener.destination, listener.callback);
            }
            _this.onConnect();
            _this.stompListenerQueue = null; // Delete the queue since all listeners have been added!
        });

        this.stompListenerQueue = [];
    }

    /**
     * Send arbitrary data to the server with a given destination.
     * @param data {object} - Data to be sent to the server
     * @param route {string} - Destination URL
     * @author Alan Rostem
     */
    send(data, route) {
        if (data) {
            if (typeof data === "object") {
                this.stomp.send(route, {}, JSON.stringify(data));
            } else {
                this.stomp.send(route, {}, JSON.stringify({data: data}));
            }
        }
    }

    /**
     * Add a listener to a socket event and do something with the received data
     * @param destination {string} - Destination URL
     * @param callback {function} - Function that takes the data as parameter
     * @author Alan Rostem
     */
    addStompListener(destination, callback) {
        if (!this.connected) {
            this.stompListenerQueue.push({
                destination: destination,
                callback: callback
            });
        } else {
            this.subscribe(destination, callback);
        }
    }

    /**
     * Subscribe to a destination with a callback.
     * @param destination
     * @param callback
     */
    subscribe(destination, callback) {
        if (destination[0] !== "/") {
            destination = "/" + destination
        }
        if (destination[destination.length - 1] === "/") {
            destination[destination.length - 1] = "";
        }
        this.subscriptions[destination] =
            this.stomp.subscribe(destination, data => {
                if (data.body) {
                    callback(JSON.parse(data.body));
                }
            });
        return this.subscriptions[destination];
    }

    /**
     * Unsubscribe from an existing destination
     * @param destination
     */
    unsubscribe(destination) {
        if (this.subscriptions[destination]) {
            this.subscriptions[destination].unsubscribe();
            delete this.subscriptions[destination];
        }
    }

    onConnect() {

    }

    /**
     * Disconnect the socket from the server
     * @author Alan Rostem
     */
    disconnect() {
        if (this.stomp !== null) {
            this.stomp.disconnect();
        }
        console.log("Disconnected.");
    }
}