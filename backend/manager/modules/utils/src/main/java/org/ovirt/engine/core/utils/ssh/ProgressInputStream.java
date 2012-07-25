package org.ovirt.engine.core.utils.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Monitor progress of output stream.
 *
 * The underlying ssh library does not provide
 * any mean of monitoring progress.
 */
public class ProgressInputStream extends FilterInputStream {
    int index;

    public ProgressInputStream(InputStream in) {
        super(in);
        this.index = 0;
    }

    @Override
    public int read(byte[] b, int off, int len)
    throws IOException {
        int ret = super.read(b, off, len);
        if (ret != -1) {
            this.index += ret;
        }
        return ret;
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
