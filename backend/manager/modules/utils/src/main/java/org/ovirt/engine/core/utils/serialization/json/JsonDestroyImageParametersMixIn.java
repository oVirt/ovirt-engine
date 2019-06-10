package org.ovirt.engine.core.utils.serialization.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("serial")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
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
