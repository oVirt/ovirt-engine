package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class StoragePoolQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7256579055993119209L;

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
