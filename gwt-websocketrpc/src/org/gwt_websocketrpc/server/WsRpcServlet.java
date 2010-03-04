package org.gwt_websocketrpc.server;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

@SuppressWarnings("serial")
public class WsRpcServlet extends WebSocketServlet {

  private final ThreadLocal<PushCallback<?>[]> threadHandlerCallback = new ThreadLocal<PushCallback<?>[]>() {
    @Override
    protected PushCallback<?>[] initialValue() {
      return new PushCallback[1];
    }
  };

  @Override
  protected WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
    return new WsRpcServletWrapper(getServletConfig(), threadHandlerCallback,
        this, arg0);
  }

  protected final <T> PushCallback<T> getPushCallback(Class<T> respType) {
    return (threadHandlerCallback.get()[0].getResponseType() == respType) 
        ? (PushCallback<T>) threadHandlerCallback.get()[0]
        : null;
    }
}
