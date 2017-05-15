package org.ovirt.engine.core.common.action;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

public class SyncAttachedDirectLunsParameters extends SyncLunsParameters {

    private static final long serialVersionUID = 1355548631007794516L;

    private Set<Guid> attachedDirectLunDisksIds;

    public SyncAttachedDirectLunsParameters() {
        this(null);
    }

    public SyncAttachedDirectLunsParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public SyncAttachedDirectLunsParameters(Guid storagePoolId, Guid attachedDirectLunDiskId) {
        this(storagePoolId, new HashSet<>(Collections.singletonList(attachedDirectLunDiskId)));
    }

    public SyncAttachedDirectLunsParameters(Guid storagePoolId, Set<Guid> attachedDirectLunDisksIds) {
        super(storagePoolId);
        this.attachedDirectLunDisksIds = attachedDirectLunDisksIds;
    }

    public Set<Guid> getAttachedDirectLunDisksIds() {
        return attachedDirectLunDisksIds;
    }

    public void setAttachedDirectLunDisksIds(Set<Guid> attachedDirectLunDisksIds) {
        this.attachedDirectLunDisksIds = attachedDirectLunDisksIds;
    }
}
