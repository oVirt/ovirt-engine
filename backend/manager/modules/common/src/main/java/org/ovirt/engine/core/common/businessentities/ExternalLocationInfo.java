package org.ovirt.engine.core.common.businessentities;

public abstract class ExternalLocationInfo extends LocationInfo {
    ConnectionMethod connectionMethod;

    public ExternalLocationInfo(ConnectionMethod connectionMethod) {
        super();
        this.connectionMethod = connectionMethod;
    }

    public ConnectionMethod getConnectionMethod() {
        return connectionMethod;
    }

    public void setConnectionMethod(ConnectionMethod connectionMethod) {
        this.connectionMethod = connectionMethod;
    }
}
