package org.ovirt.engine.core.common.action;

import java.util.Collections;
import java.util.Set;

import org.ovirt.engine.core.compat.Guid;

public class SyncDirectLunsParameters extends SyncLunsParameters {

    private static final long serialVersionUID = -8932897991614156001L;

    private Set<Guid> directLunIds;

    public SyncDirectLunsParameters() {
        this(null);
    }

    public SyncDirectLunsParameters(Guid storagePoolId) {
        super(storagePoolId);
        setDirectLunIds(Collections.emptySet());
    }

    public SyncDirectLunsParameters(Guid vdsId, Set<Guid> directLunIds) {
        this(Guid.Empty);
        setVdsId(vdsId);
        setDirectLunIds(directLunIds);
    }

    public Set<Guid> getDirectLunIds() {
        return directLunIds;
    }

    public void setDirectLunIds(Set<Guid> directLunIds) {
        this.directLunIds = directLunIds;
    }
}
