package org.gwt_websocketrpc.server;

import java.io.IOException;

import com.google.gwt.user.client.rpc.SerializationException;

public interface HandlerCallback {
    public void call(Object responseObject) throws SerializationException,
            IOException;
}