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
    private int index;

    public ProgressOutputStream(OutputStream out) {
        super(out);
        index = 0;
    }

    @Override
    public void write(byte[] b, int off, int len)
    throws IOException {
        out.write(b, off, len);
        index += len;
    }

    @Override
    public void write(int b)
    throws IOException {
        out.write(b);
        index++;
    }

    public boolean wasProgress() {
        if (index == 0) {
            return false;
        }
        else {
            index = 0;
            return true;
        }
    }
}
