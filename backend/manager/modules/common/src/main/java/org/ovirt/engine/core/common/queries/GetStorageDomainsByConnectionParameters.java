package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetStorageDomainsByConnectionParameters")
public class GetStorageDomainsByConnectionParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5734691653801921062L;

    @XmlElement(name = "StoragePoolId")
    private Guid storagePoolId;

    @XmlElement(name = "Connection")
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

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
    }
}
