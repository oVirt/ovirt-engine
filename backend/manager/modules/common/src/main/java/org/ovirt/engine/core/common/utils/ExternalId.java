package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents an identifier used by an external system. We can't make
 * any assumption about those identifiers, so we store them as plain arrays of
 * bytes.
 */
public class ExternalId implements Serializable {
    // Serialization identifier:
    private static final long serialVersionUID = 7859034308053227906L;

    // The plain bytes of the identifier:
    private byte[] bytes;

    // The hash code is computed when the identifier is created in order to
    // avoid having to compute it repeatedly when the identifier is used as
    // a key in a hash map:
    private int hash;

    public ExternalId(byte[] values) {
        this.bytes = values;
        this.hash = Arrays.hashCode(values);
    }

    /**
     * This constructor is handy for tests, where one wants to create an
     * external id without having to write the cumbersome syntax of creation
     * of byte arrays, so instead of this:
     *
     * <code>
     * ExternalId externalId = new ExternalId(
     *     new byte[] {
     *         (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x03,
     *         (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
     *     }
     * );
     * </code>
     *
     * One can write this:
     *
     * <code>
     * ExternalId externalId = new ExternalId(
     *     0x01, 0x02, 0x03, 0x04,
     *     0x05, 0x06, 0x07, 0x08
     * );
     * </code>
     *
     * The parameters are integers in order to avoid the cast to {@code byte} as
     * well.
     */
    public ExternalId(int... values) {
        bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xff);
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(2 * bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            String text = Integer.toHexString(bytes[i] & 0xff);
            if (text.length() == 1) {
                buffer.append('0');
            }
            buffer.append(text);
        }
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof ExternalId) {
            final ExternalId that = (ExternalId) object;
            return Arrays.equals(this.bytes, that.bytes);
        }
        return false;
    }
}
