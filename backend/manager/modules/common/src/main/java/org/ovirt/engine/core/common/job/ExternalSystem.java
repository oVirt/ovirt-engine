package org.ovirt.engine.core.common.job;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Represents an external system task associated with a Step.
 */
public class ExternalSystem implements Serializable{

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = -4875960108024526122L;

    /**
     * An external Id which associated with the step (e.g. VDSM Task Id)
     */
    private Guid externalId;

    /**
     * An external system type (e.g. VDSM) which its execution unit is identified by {@link #externalId}
     */
    private ExternalSystemType externalSystemType;

    public Guid getId() {
        return externalId;
    }

    public void setId(Guid externalId) {
        this.externalId = externalId;
    }

    public ExternalSystemType getType() {
        return externalSystemType;
    }

    public void setType(ExternalSystemType externalSystemType) {
        this.externalSystemType = externalSystemType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                externalId,
                externalSystemType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExternalSystem)) {
            return false;
        }
        ExternalSystem other = (ExternalSystem) obj;
        return Objects.equals(externalId, other.externalId)
                && externalSystemType == other.externalSystemType;
    }

}
