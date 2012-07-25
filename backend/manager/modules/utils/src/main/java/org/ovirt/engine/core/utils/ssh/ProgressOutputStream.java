package org.ovirt.engine.core.utils.ssh;

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
    int index;

    public ProgressOutputStream(OutputStream out) {
        super(out);
        this.index = 0;
    }

    @Override
    public void write(byte[] b, int off, int len)
    throws IOException {
        super.write(b, off, len);
        this.index += len;
    }

    public boolean wasProgress() {
        if (this.index == 0) {
            return false;
        }
        else {
            this.index = 0;
            return true;
        }
    }
}
