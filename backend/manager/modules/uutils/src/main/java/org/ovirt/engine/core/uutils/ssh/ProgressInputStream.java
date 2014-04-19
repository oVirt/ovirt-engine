package org.ovirt.engine.core.uutils.ssh;

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
    private int _index;

    public ProgressInputStream(InputStream in) {
        super(in);
        _index = 0;
    }

    @Override
    public int read(byte[] b, int off, int len)
    throws IOException {
        int ret = in.read(b, off, len);
        if (ret != -1) {
            _index += ret;
        }
        return ret;
    }

    @Override
    public int read()
    throws IOException {
        int ret = in.read();
        if (ret != -1) {
            _index++;
        }
        return ret;
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
