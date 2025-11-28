/**
 * SocketManager - Handles WebSocket connection and events
 * Implements Singleton pattern and Auto-reconnect
 */
const SocketManager = (function () {
    let instance;
    let socket;
    let listeners = {};
    let reconnectInterval = 1000;
    let maxReconnectInterval = 30000;
    let shouldReconnect = true;

    function createInstance() {
        let wsPort = '8887';

        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
            const hostname = window.location.hostname;
            const WS_URL = `${protocol}://${hostname}:${wsPort}`;
            console.log("Connecting to Socket at:", WS_URL);

            socket = new WebSocket(WS_URL);

            socket.onopen = function () {
                console.log("WebSocket Connected");
                reconnectInterval = 1000; // Reset backoff
                if (listeners['open']) {
                    listeners['open'].forEach(callback => callback());
                }
            };

            socket.onmessage = function (event) {
                try {
                    const data = JSON.parse(event.data);
                    console.log("WS Message:", data);

                    if (data.type && listeners[data.type]) {
                        listeners[data.type].forEach(callback => callback(data));
                    }
                } catch (e) {
                    console.error("Error parsing WS message:", e);
                }
            };

            socket.onclose = function (event) {
                console.log("WebSocket Closed. Reconnecting in", reconnectInterval, "ms");
                if (listeners['close']) {
                    listeners['close'].forEach(callback => callback());
                }
                if (shouldReconnect) {
                    setTimeout(() => {
                        reconnectInterval = Math.min(reconnectInterval * 2, maxReconnectInterval);
                        connect();
                    }, reconnectInterval);
                }
            };

            socket.onerror = function (error) {
                console.error("WebSocket Error:", error);
                socket.close();
            };
        }

        // Fetch config then connect
        fetch('/api/config')
            .then(response => response.json())
            .then(config => {
                if (config.wsPort) {
                    wsPort = config.wsPort;
                    console.log("Configured WebSocket port:", wsPort);
                }
                connect();
            })
            .catch(error => {
                console.warn("Failed to fetch config, using default port:", wsPort, error);
                connect();
            });

        return {
            /**
             * Listen for a specific event type
             * @param {string} eventType - The type of event to listen for (e.g., 'TABLE_UPDATE')
             * @param {function} callback - The function to call when event is received
             */
            on: function (eventType, callback) {
                if (!listeners[eventType]) {
                    listeners[eventType] = [];
                }
                listeners[eventType].push(callback);
            },

            /**
             * Stop listening for an event
             */
            off: function (eventType, callback) {
                if (listeners[eventType]) {
                    listeners[eventType] = listeners[eventType].filter(cb => cb !== callback);
                }
            },

            /**
             * Manually close connection
             */
            disconnect: function () {
                shouldReconnect = false;
                if (socket) {
                    socket.close();
                }
            }
        };
    }

    return {
        getInstance: function () {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();
