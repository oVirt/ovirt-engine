package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.compat.Guid;

public class GetSignedWebsocketProxyTicketParams extends QueryParametersBase implements Serializable {

    private Guid vmId;
    private GraphicsType graphicsType;

    public GetSignedWebsocketProxyTicketParams(Guid vmId,
            GraphicsType graphicsType) {
        this.vmId = vmId;
        this.graphicsType = graphicsType;
    }

    private GetSignedWebsocketProxyTicketParams() {}

    public Guid getVmId() {
        return vmId;
    }

    public GraphicsType getGraphicsType() {
        return graphicsType;
    }
}
