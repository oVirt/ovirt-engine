package org.ovirt.engine.core.utils.serialization.json;

import java.util.List;

import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.compat.Guid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class JsonDestroyImageParametersMixIn extends DestroyImageParameters {

    public JsonDestroyImageParametersMixIn(Guid vdsId,
            Guid vmId,
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            List<Guid> images,
            boolean postZero,
            boolean force) {
        super(vdsId, vmId, storagePoolId, storageDomainId, imageGroupId, images, postZero, force);
    }

    @JsonIgnore
    @Override
    public abstract boolean isLiveMerge();
}
