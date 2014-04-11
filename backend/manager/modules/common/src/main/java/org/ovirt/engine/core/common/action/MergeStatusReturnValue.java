package org.ovirt.engine.core.common.action;

import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.compat.Guid;

public class MergeStatusReturnValue {
    private VmBlockJobType blockJobType;
    private Set<Guid> imagesToRemove;

    public MergeStatusReturnValue() {
    }

    public MergeStatusReturnValue(VmBlockJobType jobType, Set<Guid> imagesToRemove) {
        this.blockJobType = jobType;
        this.imagesToRemove = imagesToRemove;
    }

    public VmBlockJobType getBlockJobType() {
        return blockJobType;
    }

    public Set<Guid> getImagesToRemove() {
        return imagesToRemove;
    }
}
