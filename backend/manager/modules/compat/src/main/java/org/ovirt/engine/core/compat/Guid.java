package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Guid implements Serializable, Comparable<Guid> {
    /**
     * Needed for the serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = 27305745737022810L;

    private static final byte[] CHANGE_BYTE_ORDER_INDICES = { 3, 2, 1, 0,
            5, 4, 7, 6, 8, 9, 10, 11, 12, 13, 14, 15 };
    private static final byte[] KEEP_BYTE_ORDER_INDICES = { 0, 1, 2, 3,
            4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

    public static final Guid SYSTEM = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    public static final Guid EVERYONE = new Guid("EEE00000-0000-0000-0000-123456789EEE");
    public static final Guid Empty = new Guid("00000000-0000-0000-0000-000000000000");

    private UUID uuid;

    /**
     * This constructor should never be used directly - use {@link #Empty} instead.
     * It is left here only because GWT requires it.
     */
    @Deprecated
    private Guid() {
        this(Empty.getUuid());
    }

    public Guid(UUID uuid) {
        this.uuid = uuid;
    }

    public Guid(byte[] guid, boolean keepByteOrder) {
        // Note that the indexes are computed modulo the length of the input
        // array because this is how they used to be calculated in the past,
        // and some components (the REST API, for example) build GUIDs from
        // array of bytes created from arbitrary strings, for example, in the
        // BackendCapabilitiesResource class a GUID is built from the version
        // string with this code:
        //
        //    public String generateId(Version v) {
        //      Guid guid = new Guid((v.getMajor()+"."+v.getMinor()).getBytes(),true);
        //      return guid.toString();
        //    }
        //
        // This may result in an array of bytes shorter than the 16 bytes
        // needed to build a GUID, thus the modulo operation is required.
        byte[] indexes = keepByteOrder? KEEP_BYTE_ORDER_INDICES: CHANGE_BYTE_ORDER_INDICES;
        long msb = 0;
        long lsb = 0;
        int length = guid.length;
        for (int i = 0; i <= 7; i++) {
            msb = (msb << 8) | (guid[indexes[i] % length] & 0xff);
        }
        for (int i = 8; i <= 15; i++) {
            lsb = (lsb << 8) | (guid[indexes[i] % length] & 0xff);
        }
        uuid = new UUID(msb, lsb);
    }

    public Guid(String candidate) {
        if (candidate == null) {
            throw new NullPointerException(
                    "candidate can not be null please use static method createGuidFromString");
        }
        if (candidate.isEmpty()) {
            uuid = Empty.getUuid();
        } else {
            uuid = UUID.fromString(candidate);
        }
    }

    public static Guid newGuid() {
        return new Guid(UUID.randomUUID());
    }

    public static Guid createGuidFromString(String candidate) {
        return createGuidFromStringWithDefault(candidate, null);
    }

    public static Guid createGuidFromStringDefaultEmpty(String candidate) {
        return createGuidFromStringWithDefault(candidate, Guid.Empty);
    }

    private static Guid createGuidFromStringWithDefault(String candidate, Guid defaultValue) {
        if (candidate == null) {
            return defaultValue;
        }
        return new Guid(candidate);
    }

    public static boolean isNullOrEmpty(Guid id) {
        return id == null || id.equals(Empty);
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Guid)) {
            return false;
        }
        Guid other = (Guid) obj;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public int compareTo(Guid other) {
        return this.getUuid().compareTo(other.getUuid());
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    public byte[] toByteArray() {
        byte[] data = new byte[16];
        long msb = uuid.getMostSignificantBits();
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (msb & 0xff);
            msb >>= 8;
        }
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 15; i >= 8; i--) {
            data[i] = (byte) (lsb & 0xff);
            lsb >>= 8;
        }
        return data;
    }
}
