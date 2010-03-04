package org.gwt_websocketrpc.server;

import static org.gwt_websocketrpc.shared.WsRpcConstants.WsRpcControlString;

import java.io.IOException;
import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.gwt_websocketrpc.server.websocket.WsOutboundStream;

import com.google.gwt.rpc.server.ClientOracle;
import com.google.gwt.rpc.server.RPC;
import com.google.gwt.user.client.rpc.SerializationException;

class HandlerCallbackImpl<T> implements PushCallback<T> {
  private final Class<T> responseClass;
  private final Outbound out;
  private final int requestid;
  private final ClientOracle oracle;
  private final Method requestMethod;

  public HandlerCallbackImpl(Class<T> responseClass, ClientOracle oracle,
      Method requestMethod, Outbound o, int rid) {
    this.responseClass = responseClass;
    this.oracle = oracle;
    this.requestMethod = requestMethod;
    this.out = o;
    this.requestid = rid;
  }

  public Class<T> getResponseType() {
    return responseClass;
  }

  public void call(T responseObject) throws SerializationException, IOException {

    // Null check
    if (responseObject == null) {
      throw new SerializationException(requestMethod.getName()
          + " sent a null as an asynchronous response to callback");

      // Make sure the response is the correct type
    } else if (requestMethod.getReturnType() != responseObject.getClass()) {
      throw new SerializationException(requestMethod.getName()
          + " sent an object (of type=" + responseObject.getClass()
          + ") as an asynchronous response to callback expecting type="
          + requestMethod.getReturnType());

      // Send response
    } else {
      final WsOutboundStream os = new WsOutboundStream(out);

      // Prefix response with request id
      os
          .write((Integer.toHexString(requestid) + WsRpcControlString)
              .getBytes());
      RPC.streamResponseForSuccess(oracle, os, responseObject);
      os.flush();
    }
  }
}