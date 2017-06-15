package org.ovirt.engine.core.uutils.ssh;

import java.io.ByteArrayOutputStream;

import javax.validation.constraints.NotNull;

/**
 * Soft constraint byte array output stream.
 */
public class ConstraintByteArrayOutputStream extends ByteArrayOutputStream {
    private int max;
    private boolean truncated = false;

    /**
     * Constructor.
     *
     * @param max
     *            soft limit of buffer.
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
        return truncated;
    }

    @Override
    public void write(int b) {
        if (count < max) {
            super.write(b);
        } else {
            truncated = true;
        }
    }

    @Override
    public void write(@NotNull byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (count < max) {
            super.write(b, off, len);
        } else {
            truncated = true;
        }
    }
}
