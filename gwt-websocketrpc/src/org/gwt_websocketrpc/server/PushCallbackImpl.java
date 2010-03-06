package org.gwt_websocketrpc.server;

import static org.gwt_websocketrpc.shared.WsRpcConstants.WsRpcControlString;

import java.io.OutputStream;
import java.lang.reflect.Method;

import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.gwt_websocketrpc.server.websocket.WsOutboundStream;

import com.google.gwt.rpc.server.ClientOracle;
import com.google.gwt.rpc.server.RPC;
import com.google.gwt.user.client.rpc.SerializationException;

class PushCallbackImpl<T> implements PushCallback<T> {
  private final Class<T> responseClass;
  private final Outbound out;
  private final int requestid;
  private final ClientOracle oracle;
  private final Method requestMethod;
  private boolean canceled = false;
  
  private WsOutboundStream os = null;

  public PushCallbackImpl(Class<T> responseClass, ClientOracle oracle,
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
  
  protected final OutputStream getOutputStream(){
    return (os == null)
      ? os = new WsOutboundStream(out)
      : os;
  }

  public void onSuccess(T responseObject) {

    // Null check
    if (responseObject == null) {
      // TODO: log
      new SerializationException("responseObject == null")
        .printStackTrace();

      // Send response
    } else {
      final OutputStream os = getOutputStream();

      // Prefix response with request id
      try {
        os.write(
            (Integer.toHexString(requestid) + WsRpcControlString)
                .getBytes());
      
        RPC.streamResponseForSuccess(oracle, os, responseObject);
        os.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void cancel(){
    canceled = true;
  }
  
  public boolean isCanceled() {
    return canceled;
  }

  public void onFailure(Throwable caught) {
    final OutputStream os = getOutputStream();
    
    // Prefix response with request id
    try {
      os.write(
          (Integer.toHexString(requestid) + WsRpcControlString)
              .getBytes());
    
      RPC.streamResponseForFailure(oracle, os, caught);
      os.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  
}