package org.gwt_websocketrpc.client;

import org.gwt_websocketrpc.client.WebSocketController.RequestCallbackRegistration;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;


public class WsRpcBuilder extends RpcRequestBuilder {

    private static class WsRequest extends Request {
        private RequestCallbackRegistration reg;
        public WsRequest(RequestCallbackRegistration reg) { 
            super();
            this.reg = reg; 
        }

        @Override
        public void cancel() {
            if(reg != null){
                reg.removeHandler();
                reg = null;
            }
        }

        @Override
        public boolean isPending() {
            if(reg!=null)
                if(reg.isPending()) return true;
                else reg = null;
            return false;
        }
    }
    
    protected static class WsRequestBuilder extends RequestBuilder {
        private final WebSocketController wsc;
        int reqid;
        private final boolean isHandler;

        public WsRequestBuilder(boolean isHandler, String url) {
            super(RequestBuilder.POST.toString(),  url);
            
            this.isHandler = isHandler;
            wsc = createWebSocketController(url);
        }
        
        protected WebSocketController createWebSocketController(String url) {
            return WebSocketController.createOrGetWebSocketController(url);
        }
        
        @Override
        public Request send() throws RequestException {
            return doSend(getRequestData(), getCallback());
        }

        @Override
        public Request sendRequest(String requestData, RequestCallback callback)
                throws RequestException {
            return doSend(requestData, callback);
        }

        protected Request doSend(final String data, final RequestCallback callback){
            RequestCallbackRegistration reg = wsc.sendRequest(isHandler, reqid, data,callback);
            return new WsRequest(reg);
        }
        
    }
    
    private boolean isHandler = false; 
    void setIsHandler(boolean isHandler){
        this.isHandler = isHandler;
    }
    
    @Override
    protected RequestBuilder doCreate(String serviceEntryPoint) {
        // Reset isHandler 
        boolean h = isHandler;
        isHandler = false;
        
        return new WsRequestBuilder(h, serviceEntryPoint);
    }

    @Override
    protected void doSetRequestId(RequestBuilder rb, int id) {
        super.doSetRequestId(rb, id);
        ((WsRequestBuilder)rb).reqid = id;
    }
    
}