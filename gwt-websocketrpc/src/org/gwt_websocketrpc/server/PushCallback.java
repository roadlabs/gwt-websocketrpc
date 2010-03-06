package org.gwt_websocketrpc.server;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface PushCallback<T> extends AsyncCallback<T> {
  Class<T> getResponseType();
  boolean isCanceled();
}