package org.gwt_websocketrpc.client;

import java.util.HashMap;
import java.util.HashSet;

import org.gwt_websocketrpc.client.websocket.WebSocketImpl;
import org.gwt_websocketrpc.client.websocket.WebSocket.ReadyState;
import org.gwt_websocketrpc.client.websocket.WebSocket.StringOutbound;
import org.gwt_websocketrpc.client.websocket.WebSocket.StringSocketHandler;
import org.gwt_websocketrpc.shared.WsRpcConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class WebSocketController {
    private static final HashMap<String, WebSocketController> urlWsMap = new HashMap<String,WebSocketController>();
    
    public static WebSocketController createOrGetWebSocketController(String url){        
        WebSocketController ws = urlWsMap.get(url);
        if(ws == null || !ws.isOpen()){
            ws = new WebSocketController(url);
            urlWsMap.put(url, ws);
        }
        return ws;
    }
    
    private final HashMap<Integer,RequestCallback> reqCallbackMap = new HashMap<Integer,RequestCallback>();
    private final HashSet<Integer> handlerRequests = new HashSet<Integer>();
    private final WebSocketImpl ws;
    private StringOutbound so =null;
    private String bufData = null;

    public WebSocketController(String url) {
        this.ws = new WebSocketImpl(url);
        ws.attachHandler(socketHandler);
    }
    
    protected boolean isOpen(){
        return ws.getReadyState() == ReadyState.Open;
    }
    
    private final StringSocketHandler socketHandler = new StringSocketHandler(){

        public void onConnect(StringOutbound out) {
            so = out;
            
            // Send initialization data:
            //  1) Strong Name
            //  2) Module Base Path
            so.send(GWT.getPermutationStrongName() + "!" + GWT.getModuleBaseURL());

            // Send any buffered requests
            if(bufData != null){
                so.send(bufData);
                bufData = null;
            }
        }

        public void onDisconnect() {
            so = null;
            urlWsMap.remove(ws.getURL());
        }

        public void onMessage(final String message) {            
            // Parse Request ID
            int controlCharId = message.indexOf(WsRpcConstants.WsRpcControlString);
            int rid = Integer.parseInt(
                    message.substring(
                            0, 
                            controlCharId),
                    16);
            
            final String actualMessage = message.substring(controlCharId+1); 
            
            RequestCallback callback = reqCallbackMap.get(rid); 
            if(callback != null){
                // If this is a response to a regular request (not a handler),
                // then remove callback from the callback map.
                if(!handlerRequests.contains(rid))
                    reqCallbackMap.remove(rid);
                
                callback.onResponseReceived(null, new Response(){
                    public String getHeader(String header) {return null;}
                    public Header[] getHeaders() {return null;}
                    public String getHeadersAsString() {return null;}
                    public int getStatusCode() {
                        return Response.SC_OK;
                    }
                    public String getStatusText() {return null;}
                    public String getText() {return actualMessage;}
                });
            }else{
                GWT.log("[WebSocketRPC WebSocketController.onMessage]  Request id="+rid, new Throwable());
            }
        }
    };
    
    
    public class RequestCallbackRegistration implements HandlerRegistration {
        private final int reqid;
        
        RequestCallbackRegistration(int reqid) {
            this.reqid = reqid;
        }

        public void removeHandler() {
            reqCallbackMap.remove(reqid);
            handlerRequests.remove(reqid);
        }
        
        public boolean isPending(){
            return reqCallbackMap.containsKey(reqid);
        }
    }

    public RequestCallbackRegistration sendRequest(boolean isHandler, final int reqid, String s, RequestCallback cb){
        reqCallbackMap.put(reqid, cb);
        if(isHandler)
            handlerRequests.add(reqid);
        
        // Add reqid;
        final String data = Integer.toHexString(reqid)+WsRpcConstants.WsRpcControlString+s;
        
        if(so!=null) so.send(data);                
        else bufData = data;
        
        return new RequestCallbackRegistration(reqid); 
    }  
    
}