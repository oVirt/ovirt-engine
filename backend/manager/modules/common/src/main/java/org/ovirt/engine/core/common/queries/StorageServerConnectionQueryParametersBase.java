package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionQueryParametersBase extends QueryParametersBase {
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

    /**
     * Used by REST because AbstractBackendResource has id member
     * that is always assumed to be Guid
     */
    public StorageServerConnectionQueryParametersBase(Guid serverConnectionId) {
        this(serverConnectionId.toString());
    }

    public StorageServerConnectionQueryParametersBase() {
    }
}
