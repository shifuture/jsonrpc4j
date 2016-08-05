package com.googlecode.jsonrpc4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by kenneth on 7/28/16.
 */
public class RSAOutputStream extends OutputStream {

    private OutputStream out;
    private ByteBuffer buffer;
    private int incSize = 1000;
    private int bufSize = 0;

    public RSAOutputStream(OutputStream out) {
        this.out = out;
        this.buffer = ByteBuffer.allocate(incSize);
        this.bufSize = incSize;
    }

    private void incBufSize(int size) {
        buffer = ByteBuffer.allocate(bufSize + size).put(buffer.array(), 0, buffer.position());
        bufSize += size;
    }

    @Override
    public void write(int b) throws IOException {
        if(buffer.position()>=bufSize) {
            incBufSize(incSize);
        }
        buffer.put((byte) b);
    }

    @Override
    public void flush() throws IOException {
        if ( buffer.position() > 0 ) {
            byte[] buf = null;
            try {
                buf = RSAUtils.RSAEncode(new String(buffer.array(), 0, buffer.position())).getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.buffer = ByteBuffer.allocate(incSize);
            this.bufSize = incSize;
            out.write(buf);
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
