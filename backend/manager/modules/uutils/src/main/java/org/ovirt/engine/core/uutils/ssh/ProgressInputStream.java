package org.ovirt.engine.core.uutils.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.NotNull;

/**
 * Monitor progress of output stream.
 *
 * The underlying ssh library does not provide any mean of monitoring progress.
 */
public class ProgressInputStream extends FilterInputStream {
    private int index;

    public ProgressInputStream(InputStream in) {
        super(in);
        index = 0;
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        int ret = in.read(b, off, len);
        if (ret != -1) {
            index += ret;
        }
        return ret;
    }

    @Override
    public int read() throws IOException {
        int ret = in.read();
        if (ret != -1) {
            index++;
        }
        return ret;
    }

    public boolean wasProgress() {
        if (index == 0) {
            return false;
        } else {
            index = 0;
            return true;
        }
    }
}
