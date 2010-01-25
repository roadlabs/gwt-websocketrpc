package org.gwt_websocketrpc.client.websocket;

import com.google.gwt.event.shared.HandlerRegistration;

public interface WebSocket {
    public static enum ReadyState {Connecting,Open,Closed}
    
    public static interface StringSocketHandler {
        void onConnect(StringOutbound out);
        void onMessage(String message);
        void onDisconnect();
    }
    
    public static interface StringOutbound {
        boolean isOpen();
        void send(String s);
    }

    HandlerRegistration attachHandler(StringSocketHandler handler);
    
    public ReadyState getReadyState();
    public String getURL();
    public void close();
}
