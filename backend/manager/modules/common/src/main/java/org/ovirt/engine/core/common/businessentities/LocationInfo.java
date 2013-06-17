package org.ovirt.engine.core.common.businessentities;

public abstract class LocationInfo {
    ConnectionMethod connectionMethod;

    public LocationInfo(ConnectionMethod connectionMethod) {
        super();
        this.connectionMethod = connectionMethod;
    }

    public ConnectionMethod getConnectionMethod() {
        return connectionMethod;
    }

    public void setConnectionMethod(ConnectionMethod connectionMethod) {
        this.connectionMethod = connectionMethod;
    }

    @Override
    public abstract String toString();
}
