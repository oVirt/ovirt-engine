package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class VmIconIdSizePair implements Serializable {

    private Guid small;
    private Guid large;

    /**
     * For GWT serialization;
     */
    private VmIconIdSizePair() {
    }

    public VmIconIdSizePair( Guid small, Guid large) {
        if (large == null || small == null) {
            throw new IllegalArgumentException("Argument should not be null");
        }
        this.large = large;
        this.small = small;
    }

    public Guid getSmall() {
        return small;
    }

    public Guid getLarge() {
        return large;
    }

    public Guid get(boolean small) {
        return small ? getSmall() : getLarge();
    }
}
