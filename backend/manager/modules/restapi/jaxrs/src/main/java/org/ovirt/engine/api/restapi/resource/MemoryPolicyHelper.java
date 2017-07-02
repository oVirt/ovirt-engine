package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.VmBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class MemoryPolicyHelper {

    public static void setupMemoryBalloon(VmBase vmBase, BackendResource parentResource) {
        Boolean balloonEnabled = parentResource.getEntity(Boolean.class,
                QueryType.IsBalloonEnabled,
                new IdQueryParameters(new Guid(vmBase.getId())),
                null,
                true);
        if (!vmBase.isSetMemoryPolicy()) {
            vmBase.setMemoryPolicy(new MemoryPolicy());
        }
        vmBase.getMemoryPolicy().setBallooning(balloonEnabled);
    }
}
