package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetStorageDomainsByConnectionParameters extends QueryParametersBase {
    private static final long serialVersionUID = -5734691653801921062L;

    private Guid storagePoolId;

    private String connection;

    public GetStorageDomainsByConnectionParameters() {
    }

    /**
     * Instantiate the parameter class by storage pool id and connection
     * @param storagePoolId
     *            the storage pool id to set
     * @param connection
     *            the connection to set
     */
    public GetStorageDomainsByConnectionParameters(Guid storagePoolId, String connection) {
        setStoragePoolId(storagePoolId);
        setConnection(connection);
    }

    /**
     * Returns the storage pool id associated with the command parameters
     * @return the storage pool id
     */
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    /**
     * Sets the storage pool id associated with the command parameters
     * @param storagePoolId
     *            the storage pool id to set
     */
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    /**
     * @return the connection
     */
    public String getConnection() {
        return connection;
    }

    /**
     * @param connection
     *            the connection to set
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }
}
