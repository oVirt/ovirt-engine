package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class BrickProfileDetails extends GlusterVolumeProfileStats {

    private static final long serialVersionUID = 3609367118733238971L;

    private Guid brickId;

    public BrickProfileDetails() {
    }

    public Guid getBrickId() {
        return brickId;
    }

    public void setBrickId(Guid brickId) {
        this.brickId = brickId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BrickProfileDetails)) {
            return false;
        }
        BrickProfileDetails other = (BrickProfileDetails) obj;
        return super.equals(obj)
                && Objects.equals(brickId, other.brickId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                brickId
        );
    }
}
