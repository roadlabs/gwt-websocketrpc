package org.gwt_websocketrpc.server;

import static com.google.gwt.user.client.rpc.RpcRequestBuilder.MODULE_BASE_HEADER;
import static com.google.gwt.user.client.rpc.RpcRequestBuilder.STRONG_NAME_HEADER;
import static org.gwt_websocketrpc.shared.WsRpcConstants.WsRpcControlString;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.gwt_websocketrpc.server.websocket.WsOutboundStream;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.google.gwt.rpc.server.ClientOracle;
import com.google.gwt.rpc.server.RPC;
import com.google.gwt.rpc.server.RpcServlet;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.UnexpectedException;

class WsRpcServletWrapper extends RpcServlet implements WebSocket {

    private static class RequestWrapper extends HttpServletRequestWrapper {
        private String permStrongName;
        private String reqModuleBasePath;
        private final String contextPath;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
            contextPath = request.getContextPath();
        }

        void setPermStrongName(String permStrongName) {
            this.permStrongName = permStrongName;
        }

        void setReqModuleBasePath(String reqModuleBasePath) {
            this.reqModuleBasePath = reqModuleBasePath;
        }

        @Override
        public String getHeader(final String name) {
            return (name.equals(STRONG_NAME_HEADER)) ? permStrongName : (name
                    .equals(MODULE_BASE_HEADER)) ? reqModuleBasePath : super
                    .getHeader(name);
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }
    }

    private final Map<Integer, PushCallback> reqCallbackMap = new ConcurrentHashMap<Integer, PushCallback>();

    private boolean[] wsInitialized = { false };
    private final WsRpcServletWrapper.RequestWrapper wrapReq;

    private final Class serviceClass;
    private final Object serviceInst;
    private Outbound o;
    private ClientOracle oracle;

    private final ThreadLocal<PushCallback[]> tlrcb;

    public WsRpcServletWrapper(ServletConfig sc,
            ThreadLocal<PushCallback[]> tlrcb, Object instance,
            HttpServletRequest req) {
        assert sc != null;
        assert instance != null;
        assert tlrcb != null;
        assert req != null;

        try {
            init(sc);
        } catch (ServletException e) {
            e.printStackTrace();
        }

        this.tlrcb = tlrcb;
        this.serviceInst = instance;
        this.serviceClass = instance.getClass();

        this.wrapReq = new RequestWrapper(req);

        validateThreadLocalData();
    }

    public void onConnect(Outbound arg0) {
        o = arg0;
        ServerUtils.d("onConnect");
    }

    public void onDisconnect() {
        ServerUtils.d("onDisconnect");
        o = null;
    }

    private void validateThreadLocalData() {
        if (perThreadRequest == null) {
            perThreadRequest = new ThreadLocal<HttpServletRequest>();
        }
        if (perThreadResponse == null) {
            perThreadResponse = new ThreadLocal<HttpServletResponse>();
        }
    }

    public void onMessage(byte arg0, byte[] arg1, int arg2, int arg3) {
    }

    public void onMessage(byte arg0, String arg1) {
        ServerUtils.d("onMessage:");
        ServerUtils.d(arg1);
        ServerUtils.d("\n");

        // First message encodes Strong Name and Module Base Path.
        // (Needs to be synchronized in case multiple messages
        // are received at the sametime)
        synchronized (wsInitialized) {
            if (!wsInitialized[0]) {
                final int snId = arg1.indexOf('!');

                // Usually these pieces of information are captured
                // from the HTTPServletRequest Headers of XHRs:
                // 1)
                // com.google.gwt.user.client.rpc.RpcRequestBuilder.STRONG_NAME_HEADER
                // 2)
                // com.google.gwt.user.client.rpc.RpcRequestBuilder.MODULE_BASE_HEADER

                // Instead we'll just grab it from the WebSocket message.
                final String permStrongName = arg1.substring(0, snId);
                final String reqModuleBasePath = arg1.substring(snId + 1);

                wrapReq.setPermStrongName(permStrongName);
                wrapReq.setReqModuleBasePath(reqModuleBasePath);

                ServerUtils.d("init message: [permStrongName='"
                        + permStrongName + "', reqModuleBasePath'"
                        + reqModuleBasePath + "']");

                try {
                    // Unfortunate...
                    perThreadRequest.set(wrapReq);

                    oracle = getClientOracle();
                    wsInitialized[0] = true;
                } catch (Throwable e) {
                    // Give a subclass a chance to either handle the exception
                    // or
                    // rethrow it
                    //
                    doUnexpectedFailure(e);
                } finally {
                    // null the thread-locals to avoid holding request/response
                    //
                    perThreadRequest.set(null);
                }

                return;
            }
        }

        // ... Subsequent messages
        try {
            synchronized (this) {
                perThreadRequest.set(wrapReq);
            }

            // Parse Request id
            int lastCtrlCharId = arg1.indexOf('!');
            final int rid = Integer.parseInt(arg1.substring(0, lastCtrlCharId),
                    16);

            // RPC call?
            if (arg1.charAt(lastCtrlCharId + 1) != '!') {

                processRequest(rid, arg1.substring(lastCtrlCharId + 1));

                // RPC cancel?
            } else {
                ++lastCtrlCharId;
            }

        } catch (Throwable e) {
            e.printStackTrace();

            // Give a subclass a chance to either handle the exception or
            // rethrow it
            //
            doUnexpectedFailure(e);
        } finally {
            // null the thread-locals to avoid holding request/response
            //
            perThreadRequest.set(null);
        }

    }

    protected void processRequest(final int rid, String msg)
            throws SerializationException, IOException {
        final ClassLoader oldcl = Thread.currentThread()
                .getContextClassLoader();

        final WsOutboundStream os = new WsOutboundStream(o);
        try {
            // Set the TCCL...
            // Possibly a bug in Jetty, WebSocketServlet's appear
            // to have the incorrect TCCL set. The Java App
            // ClassLoader is set, as opposed to the WebApp's
            // ClassLoader (More investigation is necessary).
            Thread.currentThread().setContextClassLoader(
                    serviceClass.getClassLoader());

            final RPCRequest rpcRequest = RPC.decodeRequest(msg, serviceClass,
                    oracle);
            onAfterRequestDeserialized(rpcRequest);

            final PushCallback[] cb = tlrcb.get();
            try {
                cb[0] = new HandlerCallbackImpl(oracle, rpcRequest.getMethod(),
                        o, rid);

                // Add new request -> handler callback entry
                reqCallbackMap.put(rid, cb[0]);

                // Prefix response with Request ID
                os.write((Integer.toHexString(rid) + WsRpcControlString)
                        .getBytes());

                // Send
                RPC.invokeAndStreamResponse(serviceInst,
                        rpcRequest.getMethod(), rpcRequest.getParameters(),
                        oracle, os);

                os.flush();
            } catch (UnexpectedException e) {

                // Allow Async Callback for handlers
                if (e.getCause().getClass() != ResponseSentLater.class)
                    throw e;

            } finally {
                cb[0] = null;
            }

        } catch (RemoteException ex) {
            throw new SerializationException(
                    "An exception was sent from the client", ex.getCause());

        } catch (IncompatibleRemoteServiceException ex) {
            log(
                    "An IncompatibleRemoteServiceException was thrown while processing this call.",
                    ex);
            RPC.streamResponseForFailure(oracle, os, ex);

        } finally {
            // Reset the TCCL
            Thread.currentThread().setContextClassLoader(oldcl);
        }
    }

}