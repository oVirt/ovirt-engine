package org.ovirt.engine.api.extensions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Extension map key.
 * Provides type safe mapping between key and value.
 */
public class ExtKey implements Cloneable, Serializable {

    public static class Flags {
        public static final int SENSITIVE = 1<<0;
        public static final int SKIP_DUMP = 1<<1;
    }

    private static final long serialVersionUID = 7373830750276947742L;

    private transient Class<?> type;
    private String typeName;
    private ExtUUID uuid;
    private int flags;

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        try {
            type = Class.forName(typeName);
        } catch(ClassNotFoundException e) {
            throw new IOException(
                String.format("Cannot resolve ExtKey type '%s'", typeName),
                e
            );
        }
    }

    /**
     * Constructor.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param flags key flags see {@link Flags}.
     */
    public ExtKey(Class<?> type, ExtUUID uuid, int flags) {
        this.type = type;
        this.uuid = uuid;
        this.flags = flags;

        this.typeName = this.type.getName();
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param flags key flags see {@link Flags}.
     */
    public ExtKey(String name, Class<?> type, UUID uuid, int flags) {
        this(type, new ExtUUID(name, uuid), flags);
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param flags key flags see {@link Flags}.
     */
    public ExtKey(String name, Class<?> type, String uuid, int flags) {
        this(name, type, UUID.fromString(uuid), flags);
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     */
    public ExtKey(String name, Class<?> type, String uuid) {
        this(name, type, uuid, 0);
    }

    /**
     * Constructor, default. For serialization.
     */
    public ExtKey() {
        this(String.class, new ExtUUID(), 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExtKey)) {
            return false;
        }
        ExtKey other = (ExtKey) obj;
        return Objects.equals(uuid, other.uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "Extkey[name=%s;type=%s;uuid=%s;]",
            uuid.getName(),
            type,
            uuid
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtKey clone() throws CloneNotSupportedException {
        return new ExtKey(
            type,
            uuid,
            flags
        );
    }

    /**
     * Returns the type of the key.
     * @return Type of key.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the uuid of the key.
     * @return Uuid of key.
     */
    public ExtUUID getUuid() {
        return uuid;
    }

    /**
     * Returns flags see {@link Flags}.
     * @return flags.
     */
    public int getFlags() {
        return flags;
    }
}
