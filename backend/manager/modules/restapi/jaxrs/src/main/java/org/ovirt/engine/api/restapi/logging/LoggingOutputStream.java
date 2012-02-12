package org.ovirt.engine.api.restapi.logging;

import java.io.IOException;
import java.io.OutputStream;

import org.ovirt.engine.core.utils.log.Log;

public class LoggingOutputStream extends OutputStream {

    protected OutputStream wrapped;
    protected Log log;
    protected StringBuilder line;

    protected LoggingOutputStream(OutputStream wrapped, Log log) {
        this.wrapped = wrapped;
        this.log = log;
        line = new StringBuilder();
    }

    @Override
    public void write(int c) throws IOException {
        line.append((char)c);
        if (c == '\n') {
            dump();
        }
        wrapped.write(c);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        String b = new String(bytes, offset, length);
        line.append(b);
        if (b.indexOf('\n') != -1) {
            dump();
        }
        wrapped.write(bytes, offset, length);
    }

    @Override
    public void flush() throws IOException {
        dump();
        wrapped.flush();
    }

    @Override
    public void close() throws IOException {
        dump();
        // wrapped stream will be independently closed
    }

    protected void dump() {
        log(line.toString());
        line = new StringBuilder();
    }

    protected void log(String line) {
        log.debug(line);
    }
}
