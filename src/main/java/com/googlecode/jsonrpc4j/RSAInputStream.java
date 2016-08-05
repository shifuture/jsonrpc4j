package com.googlecode.jsonrpc4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kenneth on 7/28/16.
 */
public class RSAInputStream extends InputStream {

    private InputStream in;
    public RSAInputStream(InputStream in) {
        this.in = in;
    }
    private byte[] buffer = null;
    private int offset = 0;

    @Override
    public int read() throws IOException {
        if ( buffer == null ) {
            int b = -1;
            StringBuffer strBf = new StringBuffer();
            while ((b = in.read()) != -1) {
                strBf.append(new String(new byte[]{(byte)b}));
            }
            try {
                buffer = RSAUtils.RSADecode(strBf.toString()).getBytes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ( offset >= buffer.length) {
            return -1;
        }
        return buffer[offset++];
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
