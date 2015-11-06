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
        if (obj == null) {
            return false;
        }
        if(!super.equals(obj)) {
            return false;
        }
        BrickProfileDetails brickDetails = (BrickProfileDetails) obj;
        if (brickDetails.getBrickId() == null) {
            return false;
        }
        if (!(Objects.equals(getBrickId(), brickDetails.getBrickId()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * super.hashCode() + ((getBrickId() == null) ? 0 : getBrickId().hashCode());
        return result;
    }
}
