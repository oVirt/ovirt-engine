package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StoragePoolQueryParametersBase")
public class StoragePoolQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7256579055993119209L;

    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public StoragePoolQueryParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        switch (queryType) {
        case GetStoragePoolById:
            return RegisterableQueryReturnDataType.IQUERYABLE;

        case GetVdsGroupsByStoragePoolId:
        case GetStorageDomainsByStoragePoolId:
            return RegisterableQueryReturnDataType.LIST_IQUERYABLE;

        default:
            return RegisterableQueryReturnDataType.UNDEFINED;
        }
    }

    public StoragePoolQueryParametersBase() {
    }
}
