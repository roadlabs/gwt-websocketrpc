/**
 * 
 */
package org.gwt_websocketrpc.server.websocket;

import static org.gwt_websocketrpc.shared.WsRpcConstants.Frame;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.gwt_websocketrpc.server.ServerUtils;

public class WsOutboundStream extends OutputStream {
    private final Outbound o;
    private final ByteBuffer buf = ByteBuffer.allocate(8192);

    public WsOutboundStream(Outbound o) {
        this.o = o;
    }
    
    @Override
    public void flush() throws IOException {
        if(buf.position() != 0){
            final byte[] send = new byte[buf.position()];
            buf.rewind();
            buf.get(send);

            ServerUtils.d("Sending msg='"+send+"'");
            o.sendMessage(Frame, send);

            buf.clear();
        }
    }

    @Override
    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buf.put(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        buf.put(b);
    }
}