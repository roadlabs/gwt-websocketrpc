package org.gwt_websocketrpc.server;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

@SuppressWarnings("serial")
public class WsRpcServlet extends WebSocketServlet {

    private final ThreadLocal<HandlerCallback[]> threadHandlerCallback = new ThreadLocal<HandlerCallback[]>() {
        @Override
        protected HandlerCallback[] initialValue() {
            return new HandlerCallback[1];
        }
    };

    @Override
    protected WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
        return new WsRpcServletWrapper(getServletConfig(),
                threadHandlerCallback, this, arg0);
    }

    protected final HandlerCallback getHandlerCallback() {
        return threadHandlerCallback.get()[0];
    }
}
