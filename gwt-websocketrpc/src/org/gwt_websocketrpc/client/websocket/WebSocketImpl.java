package org.gwt_websocketrpc.client.websocket;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.HandlerRegistration;

public class WebSocketImpl implements WebSocket {
        
    public HandlerRegistration attachHandler(final StringSocketHandler handler) {
        assert handler != null;
        
        if(handler!=callback){        
            if(callback != null)
                callback.onDisconnect();
            
            callback = handler;
            if(_isOpen()){
                cbConnectCalled = true;
                callback.onConnect(outbound);
            }
            return new HandlerRegistration() {
                public void removeHandler() {
                    handler.onDisconnect();
                    if(callback == handler){
                        cbConnectCalled = false;
                        callback=null; 
                    }
                }
            };            
        }
        return null;   
    }

    private final String location; 
    private boolean cbConnectCalled = false;
    private StringSocketHandler callback;
    private final StringOutbound outbound = new StringOutbound(){
        public boolean isOpen() { return _isOpen(); }
        public void send(String s) { WebSocketImpl.this.send(s); }
    };
    
    @SuppressWarnings("unused") // Used in JSNI, see _init
    private JavaScriptObject _ws = null; //TODO: Check GWT issue: 4469
    private ReadyState state = ReadyState.Connecting;
    
    /**
     * Creates and attempts to open a new WebSocket to the given 
     * location. 
     * "ws://" should not prefix the location.
     *  
     * @param location
     */
    public WebSocketImpl(String location) {
        assert location!=null;
        this.location = location;
        _init(location);
    }
    
    public final ReadyState getReadyState(){
        return state;
    }
    
    public final String getURL() {
        return location;
    }

    @SuppressWarnings("unused") // Used in JSNI, see _init
    private final void _onConnect(){
        System.out.println("WebSocket["+location+"]._onConnect()");
        state = ReadyState.Open;
        if(callback != null && !cbConnectCalled){
            cbConnectCalled = true;
            callback.onConnect(outbound);
        }
    }

    @SuppressWarnings("unused") // Used in JSNI, see _init
    private final void _onDisconnect(){
        System.out.println("WebSocket["+location+"]._onDisconnect()");
        state = ReadyState.Closed;
        if(callback != null)
            callback.onDisconnect();
    }
    
    @SuppressWarnings("unused") // Used in JSNI, see _init
    private final void _onMessage(String msg){
        System.out.println("WebSocket["+location+"]._onMessage(): data="+msg);
        if(callback != null)
            callback.onMessage(msg);
    }
    
    private final boolean _isOpen(){
        return state == ReadyState.Open;
    }
    
    public native void close() /*-{
        if(!this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws){
            this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws.close();
            this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws = null;
        }
    }-*/;

    private native void send(String msg) /*-{
        if(this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_isOpen()())
            this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws.send(msg);
    }-*/;
    
    private native void _init(String loc) /*-{
        // Initialize WebSocket if it wasn't done already
        if(!this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws){
            var lws = this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_ws = new WebSocket(loc);
            var cb = this.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::callback;
            var that = this;
                    
            console.log("WebSocket["+loc+"].init()");
            
            lws.onopen = function() {
                console.log("WebSocket["+loc+"]._ws.onopen()");
                that.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_onConnect()();
            };
        
            lws.onmessage = function(wsresp) {
                console.log("WebSocket["+loc+"]_ws.onmessage(): data="+wsresp.data);
                if(wsresp.data)
                    that.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_onMessage(Ljava/lang/String;)(wsresp.data);
            };
            
            lws.onclose = function(m) {
                console.log("WebSocket["+loc+"]_ws.onclose()");
                that.@org.gwt_websocketrpc.client.websocket.WebSocketImpl::_onDisconnect()();
            };
        }
    }-*/ ;
}
