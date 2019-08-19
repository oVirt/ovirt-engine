package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

public class MergeStatusReturnValue implements Serializable {
    private static final long serialVersionUID = 4187915855755614378L;
    private Set<Guid> imagesToRemove;

    public MergeStatusReturnValue() {
    }

    public MergeStatusReturnValue(Set<Guid> imagesToRemove) {
        this.imagesToRemove = imagesToRemove;
    }

    public Set<Guid> getImagesToRemove() {
        return imagesToRemove;
    }
}
