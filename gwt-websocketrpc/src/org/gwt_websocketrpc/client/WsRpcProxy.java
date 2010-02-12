/**
 * 
 */
package org.gwt_websocketrpc.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.rpc.client.impl.RpcServiceProxy;
import com.google.gwt.rpc.client.impl.TypeOverrides;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.impl.RequestCallbackAdapter.ResponseReader;

public class WsRpcProxy extends RpcServiceProxy {
    
    // Converts the any protocol url (http://) to a 
    // WebSocket protocol (ws://)
    protected static String convertToWSProtocol(String url){
        return "ws"+url.substring(url.indexOf("://"));
    }
    
    private final WsRpcBuilder builder;
    
    public WsRpcProxy(String moduleBaseURL, String remoteServiceRelativePath, TypeOverrides typeOverrides) {
        super(convertToWSProtocol(moduleBaseURL), remoteServiceRelativePath, typeOverrides);
        builder = new WsRpcBuilder();
        setRpcRequestBuilder(builder);
    }

    protected <T> Request doHandlerInvoke(
            ResponseReader responseReader,
            String methodName, int invocationCount, String requestData,
            AsyncCallback<T> callback) {
        builder.setIsHandler(true);
        return doInvoke(responseReader, methodName, invocationCount, requestData,callback);
    }
    
    
}