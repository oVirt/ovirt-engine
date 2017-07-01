package org.ovirt.engine.core.common.queries;

import javax.validation.constraints.NotNull;

public class ServerParameters extends QueryParametersBase {
    private static final long serialVersionUID = -3121291409184710688L;

    @NotNull(message = "VALIDATION_VDS_HOSTNAME_HOSTNAME_OR_IP")
    private String server;

    private Integer port;

    public ServerParameters() {
    }

    public ServerParameters(String server) {
        this(server, null);
    }

    public ServerParameters(String server, Integer port) {
        this.server = server;
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public Integer getPort() {
        return port;
    }
}
