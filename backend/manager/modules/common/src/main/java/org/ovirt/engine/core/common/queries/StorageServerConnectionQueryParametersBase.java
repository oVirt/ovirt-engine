package org.ovirt.engine.core.common.queries;

public class StorageServerConnectionQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2686760857776133215L;

    private String privateServerConnectionId;

    public String getServerConnectionId() {
        return privateServerConnectionId;
    }

    private void setServerConnectionId(String value) {
        privateServerConnectionId = value;
    }

    public StorageServerConnectionQueryParametersBase(String serverConnectionId) {
        setServerConnectionId(serverConnectionId);
    }

    public StorageServerConnectionQueryParametersBase() {
    }
}
