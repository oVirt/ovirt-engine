package org.ovirt.engine.core.utils.ssh;

import java.io.ByteArrayOutputStream;

/**
 * Soft constraint byte array output stream.
 */
public class ConstraintByteArrayOutputStream extends ByteArrayOutputStream {
    int max;
    boolean truncated = false;

    /**
     * Constructor.
     *
     * @param max soft limit of buffer.
     */
    public ConstraintByteArrayOutputStream(int max) {
        super();
        this.max = max;
    }

    /**
     * Check if data was truncated.
     *
     * @return true if truncated.
     */
    public boolean wasTruncated() {
        return this.truncated;
    }

    @Override
    public void write(int b) {
        if (count < this.max) {
            super.write(b);
        }
        else {
            this.truncated = true;
        }
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (count < this.max) {
            super.write(b, off, len);
        }
        else {
            this.truncated = true;
        }
    }
}
