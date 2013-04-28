package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.UUID;

public class NGuid implements Serializable, Comparable<NGuid> {

    protected static final String EMPTY_GUID_VALUE = "00000000-0000-0000-0000-000000000000";

    /**
     * Needed for the serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 27305745737022810L;

    private static final byte[] CHANGE_BYTE_ORDER_INDICES = { 3, 2, 1, 0,
            5, 4, 7, 6, 8, 9, 10, 11, 12, 13, 14, 15 };
    private static final byte[] KEEP_BYTE_ORDER_INDICES = { 0, 1, 2, 3,
            4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    public final static Guid Empty = new Guid();

    public static Guid NewGuid() {
        return new Guid(UUID.randomUUID());
    }

    public static boolean isNullOrEmpty(NGuid id) {
        return id == null || id.equals(Empty);
    }

    protected UUID uuid;
    protected Guid guid;

    public NGuid() {
        this(EMPTY_GUID_VALUE);

    }

    public NGuid(byte[] guid, boolean keepByteOrder) {
        String guidAsStr = getStrRepresentationOfGuid(guid, keepByteOrder);
        if (guidAsStr.isEmpty()) {
            uuid = UUID.fromString(EMPTY_GUID_VALUE);
        } else {
            uuid = UUID.fromString(guidAsStr);
        }
    }

    public NGuid(String candidate) {
        if (candidate == null) {
            throw new NullPointerException(
                    "candidate can not be null please use static method createGuidFromString");
        }
        if (candidate.isEmpty()) {
            uuid = UUID.fromString(EMPTY_GUID_VALUE);
        } else {
            uuid = UUID.fromString(candidate);
        }
    }

    public static NGuid createGuidFromString(String candidate) {
        if (candidate == null) {
            return null;
        } else {
            return new NGuid(candidate);
        }
    }

    public NGuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Guid getValue() {
        if (this.guid == null) {
            this.guid = new Guid(uuid);
        }

        return this.guid;
    }

    @Override
    public String toString() {
        if (uuid == null)
            return "[null]";
        else
            return uuid.toString();
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof NGuid)) {
            return false;
        }
        NGuid otherGuid = (NGuid) other;
        return uuid.equals(otherGuid.getUuid());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * Gets a string representation of GUID
     *
     * @param inguid
     *            byte array containing the GUID data.
     * @param keepByteOrder
     *            determines if to keep the byte order in the string representation or not. For some systems as MSSQL
     *            the bytes order should be swapped before converting to String, and for other systems (such as
     *            ActiveDirectory) it should be kept.
     * @return String representation of GUID
     */
    public String getStrRepresentationOfGuid(byte[] inguid,
            boolean keepByteOrder) {

        StringBuilder strGUID = new StringBuilder();

        byte[] byteOrderIndices = null;

        if (keepByteOrder) {
            byteOrderIndices = KEEP_BYTE_ORDER_INDICES;
        } else {
            byteOrderIndices = CHANGE_BYTE_ORDER_INDICES;
        }

        int length = inguid.length;
        // A GUID format looks like xxxx-xx-xx-xx-xxxxxx where each "x"
        // represents a byte in hexadecimal format

        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[0 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[1 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[2 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[3 % length]] & 0xFF));
        strGUID.append("-");
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[4 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[5 % length]] & 0xFF));
        strGUID.append("-");
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[6 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[7 % length]] & 0xFF));
        strGUID.append("-");
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[8 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[9 % length]] & 0xFF));
        strGUID.append("-");
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[10 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[11 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[12 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[13 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[14 % length]] & 0xFF));
        strGUID.append(AddLeadingZero((int) inguid[byteOrderIndices[15 % length]] & 0xFF));

        return strGUID.toString();

    }

    private static String AddLeadingZero(int k) {
        return (k <= 0xF) ? "0" + Integer.toHexString(k) : Integer
                .toHexString(k);
    }

    @Override
    public int compareTo(NGuid other) {
        return this.getUuid().compareTo(other.getUuid());
    }

}
