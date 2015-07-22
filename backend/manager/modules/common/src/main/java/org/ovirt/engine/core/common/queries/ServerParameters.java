package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

public class ServerParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3121291409184710688L;

    @NotNull(message = "VALIDATION_VDS_HOSTNAME_HOSTNAME_OR_IP")
    private String server;

    public ServerParameters() {
    }

    public ServerParameters(String server) {
        this.server = server;
    }

    public String getServer() {
        return server;
    }

}
