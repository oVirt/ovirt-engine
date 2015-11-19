package org.ovirt.engine.api.extensions;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Extension UUID.
 * Provides named GUID.
 */
public class ExtUUID implements Comparable<ExtUUID>, Cloneable, Serializable {

    private static final long serialVersionUID = -1381185377164713737L;

    private String name;
    private UUID uuid;

    /**
     * Constructor.
     * @param name uuid name, used only for debugging.
     * @param uuid uuid.
     */
    public ExtUUID(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Constructor.
     * @param name uuid name, used only for debugging.
     * @param uuid uuid.
     */
    public ExtUUID(String name, String uuid) {
        this(name, UUID.fromString(uuid));
    }

    /**
     * Constructor, default. For serialization.
     */
    public ExtUUID() {
        this("UNINITIALIZED", "00000000-0000-0000-0000-000000000000");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExtUUID)) {
            return false;
        }
        ExtUUID other = (ExtUUID) obj;
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
        return String.format("%s[%s]", name, uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ExtUUID o) {
        return uuid.compareTo(o.uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtUUID clone() throws CloneNotSupportedException {
        return new ExtUUID(
            name,
            uuid
        );
    }

    /**
     * Returns the name of the key.
     * @return Name of key.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the uuid of the key.
     * @return Uuid of key.
     */
    public UUID getUuid() {
        return uuid;
    }
}
