package org.ovirt.engine.api.restapi.logging;

import java.io.IOException;
import java.io.InputStream;

import org.ovirt.engine.core.utils.log.Log;

public class LoggingInputStream extends InputStream {

    protected InputStream wrapped;
    protected Log log;
    protected StringBuilder line;

    protected LoggingInputStream(InputStream wrapped, Log log) {
        this.wrapped = wrapped;
        this.log = log;
        line = new StringBuilder();
    }

    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }

    @Override
    public void mark(int readlimit) {
        wrapped.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    @Override
    public synchronized int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public synchronized int read(byte[] bytes, int offset, int length) throws IOException {
        int count = wrapped.read(bytes, offset, length);
        if (count > 0) {
            String b = new String(bytes, offset, count);
            line.append(b);
            if (b.indexOf('\n') != -1) {
                dump();
            }
        }
        return count;
    }

    @Override
    public synchronized int read() throws IOException {
        int c = wrapped.read();
        line.append((char)c);
        if (c == '\n') {
            dump();
        }
        return c;
    }

    @Override
    public void reset() throws IOException {
        wrapped.reset();
    }

    @Override
    public long skip(long n)  throws IOException {
        return wrapped.skip(n);
    }

    protected void dump() {
        log.debug(line.toString());
        line = new StringBuilder();
    }
}
