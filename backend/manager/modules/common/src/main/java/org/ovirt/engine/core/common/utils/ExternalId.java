package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents an identifier used by an external system. We can't make
 * any assumption about those identifiers, so we store them as plain arrays of
 * bytes.
 */
public class ExternalId implements Serializable {
    private static final long serialVersionUID = 7859034308053227906L;

    /**
     * Creates an identifier from an array of bytes.
     */
    public static ExternalId fromBytes(byte[] bytes) {
        return new ExternalId(bytes);
    }

    /**
     * Convers an hexadecimal representation of an array of bytes into an external identifier ignoring any separators
     * or spaces that may be present. Anything that isn't an hexadecimal character is considered a separator and thus
     * ignored.
     *
     * @param text the hexadecimal representation of the identifier, containing hexadedimal digits in lower or upper
     *     case and optional separators
     */
    public static ExternalId fromHex(String text) {
        byte[] buffer = new byte[text.length() / 2];
        int j = 0;
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            char digit = text.charAt(i);
            int value;
            if (digit >= '0' && digit <= '9') {
                value = digit - '0';
            }
            else if (digit >= 'a' && digit <= 'f') {
                value = 10 + (digit - 'a');
            }
            else if (digit >= 'A' && digit <= 'F') {
                value = 10 + (digit - 'A');
            }
            else {
                continue;
            }
            if ((j & 1) == 0) {
                buffer[k] = (byte) (value << 4);
            }
            else {
                buffer[k] |= value;
                k++;
            }
            j++;
        }
        if (buffer.length > k) {
            byte[] tmp = new byte[k];
            System.arraycopy(buffer, 0, tmp, 0, k);
            buffer = tmp;
        }
        return new ExternalId(buffer);
    }

    /**
     * The plain bytes of the identifier.
     */
    private byte[] bytes;

    /**
     * The hash code is computed when the identifier is created in order to avoid having to compute it repeatedly when
     * the identifier is used as a key in a hash map.
     */
    private int hash;

    /**
     * This constructor is intended only for serialization support, please don't use it directly.
     */
    public ExternalId() {
        bytes = new byte[0];
        hash = Arrays.hashCode(bytes);
    }

    /**
     * Creates a new identifier from the given bytes.
     *
     * @param values the bytes
     */
    public ExternalId(byte[] values) {
        bytes = values;
        hash = Arrays.hashCode(bytes);
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
        hash = Arrays.hashCode(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Generates the hexadecimal representation of the identifier. Each byte is represented by two lowercase hexadecimal
     * characters, without any separator.
     *
     * @return a string containing the hexadecimal representation of the identifier
     */
    public String toHex() {
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

    /**
     * Creates a string representation of the identifier. The actual representation may vary from version to version, so
     * avoid relying on it. If you need to rely on the representation use other methods, like {@link #toHex()}.
     *
     * @return a string containing a representation of the identifier
     */
    @Override
    public String toString() {
        return toHex();
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
