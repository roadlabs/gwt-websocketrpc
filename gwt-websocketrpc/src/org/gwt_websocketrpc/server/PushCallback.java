package org.gwt_websocketrpc.server;

import java.io.IOException;

import com.google.gwt.user.client.rpc.SerializationException;

public interface PushCallback<T> {
  Class<T> getResponseType();
  void call(T responseObject) throws SerializationException, IOException;
}