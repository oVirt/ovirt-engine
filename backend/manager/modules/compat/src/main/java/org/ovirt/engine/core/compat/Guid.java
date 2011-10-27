package org.ovirt.engine.core.compat;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

// This is a wrapper for a java.util.UUID
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "guid", namespace = "http://service.engine.ovirt.org")
public class Guid extends NGuid implements Serializable {
    public static final Guid SYSTEM = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    public static final Guid EVERYONE = new Guid("EEE00000-0000-0000-0000-123456789EEE");

    /**
     * Needed for the serialization/deserialization mechanism.
     */
    private static final long serialVersionUID = -7279324026588251446L;

    public Guid() {
        super();
    }

    public Guid(byte[] guid, boolean keepByteOrder) {
        super(guid, keepByteOrder);
    }

    public Guid(String candidate) {
        super(candidate);
    }

    public Guid(UUID uuid) {
        super(uuid);
    }

    public static Guid createGuidFromString(String candidate) {
        if (candidate == null) {
            return Guid.Empty;
        } else {
            return new Guid(candidate);
        }
    }

    public static boolean OpEquality(Guid g1, Guid g2) {
        return (g1 == null && g2 == null) || (g1 != null && g1.equals(g2));
    }

}
