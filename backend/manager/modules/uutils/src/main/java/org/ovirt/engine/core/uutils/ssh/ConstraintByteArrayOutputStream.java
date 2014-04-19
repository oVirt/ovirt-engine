package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayOutputStream;

/**
 * Soft constraint byte array output stream.
 */
public class ConstraintByteArrayOutputStream extends ByteArrayOutputStream {
    private int _max;
    private boolean _truncated = false;

    /**
     * Constructor.
     *
     * @param max soft limit of buffer.
     */
    public ConstraintByteArrayOutputStream(int max) {
        super();
        _max = max;
    }

    /**
     * Check if data was truncated.
     *
     * @return true if truncated.
     */
    public boolean wasTruncated() {
        return _truncated;
    }

    @Override
    public void write(int b) {
        if (count < _max) {
            super.write(b);
        }
        else {
            _truncated = true;
        }
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (count < _max) {
            super.write(b, off, len);
        }
        else {
            _truncated = true;
        }
    }
}
