package org.gwt_websocketrpc.rebind;

import org.gwt_websocketrpc.client.ServerPushEnabled;
import org.gwt_websocketrpc.client.WsRpcProxy;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.rpc.rebind.RpcProxyCreator;
import com.google.gwt.rpc.rebind.RpcServiceGenerator;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.ProxyCreator;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracle;

public class WsRpcServiceGenerator extends RpcServiceGenerator {
    
    public static class WsRpcProxyCreator extends RpcProxyCreator {
        
        public WsRpcProxyCreator(JClassType type) {
            super(type);
        }

        @Override
        protected Class<? extends RemoteServiceProxy> getProxySupertype() {
            return WsRpcProxy.class;
        }

        @Override
        protected void generateProxyMethod(SourceWriter w,
                SerializableTypeOracle serializableTypeOracle,
                JMethod syncMethod, JMethod asyncMethod) {
            super.generateProxyMethod(
                    
                // If this a handler method replace the 
                //    "return doInvoke(" (asyncMethod return Request), with
                //    "return doHandlerInvoke("
                    
                // TODO: Figure out a way around NASTY, NaSTy, Nasty Hijack hack
                (syncMethod.getAnnotation(ServerPushEnabled.class)!=null)
                    ? new SourceWriterHijack(w, "return doInvoke(", "return doHandlerInvoke(")
                    : w,
                    
                serializableTypeOracle, 
                syncMethod,
                asyncMethod);
        }   
    }
    
    @Override
    protected ProxyCreator createProxyCreator(JClassType remoteService) {
        return new WsRpcProxyCreator(remoteService);
    }
}
