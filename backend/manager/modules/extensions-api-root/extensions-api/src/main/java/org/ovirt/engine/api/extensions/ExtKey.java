package org.ovirt.engine.api.extensions;

import java.util.UUID;

/**
 * Extension map key.
 * Provides type safe mapping between key and value.
 */
public class ExtKey implements Cloneable {

    private Class type;
    private ExtUUID uuid;
    private boolean sensitive;

    /**
     * Constructor.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param sensitive do not print value.
     */
    public ExtKey(Class type, ExtUUID uuid, boolean sensitive) {
        this.type = type;
        this.uuid = uuid;
        this.sensitive = sensitive;
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param sensitive do not print value.
     */
    public ExtKey(String name, Class type, UUID uuid, boolean sensitive) {
        this(type, new ExtUUID(name, uuid), sensitive);
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     * @param sensitive do not print value.
     */
    public ExtKey(String name, Class type, String uuid, boolean sensitive) {
        this(name, type, UUID.fromString(uuid), sensitive);
    }

    /**
     * Constructor.
     * @param name key name, used only for debugging.
     * @param type value type.
     * @param uuid unique identifier for key.
     */
    public ExtKey(String name, Class type, String uuid) {
        this(name, type, uuid, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        return (
            other != null &&
            getClass() == other.getClass() &&
            uuid.equals(((ExtKey)other).uuid)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
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
    public Object clone() throws CloneNotSupportedException {
        return new ExtKey(
            type,
            uuid,
            sensitive
        );
    }

    /**
     * Returns the type of the key.
     * @return Type of key.
     */
    public Class getType() {
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
     * Returns true if sensitive.
     * @return true if sensitive.
     */
    public boolean isSensitive() {
        return sensitive;
    }
}
