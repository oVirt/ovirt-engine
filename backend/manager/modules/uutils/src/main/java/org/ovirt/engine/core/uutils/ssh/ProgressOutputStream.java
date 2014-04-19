package org.ovirt.engine.core.uutils.ssh;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Monitor progress of input stream.
 *
 * The underlying ssh library does not provide
 * any mean of monitoring progress.
 */
class ProgressOutputStream extends FilterOutputStream {
    private int _index;

    public ProgressOutputStream(OutputStream out) {
        super(out);
        _index = 0;
    }

    @Override
    public void write(byte[] b, int off, int len)
    throws IOException {
        out.write(b, off, len);
        _index += len;
    }

    @Override
    public void write(int b)
    throws IOException {
        out.write(b);
        _index++;
    }

    public boolean wasProgress() {
        if (_index == 0) {
            return false;
        }
        else {
            _index = 0;
            return true;
        }
    }
}
