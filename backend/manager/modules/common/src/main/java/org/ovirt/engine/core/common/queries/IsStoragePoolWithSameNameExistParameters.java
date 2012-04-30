package org.ovirt.engine.core.common.queries;

public class IsStoragePoolWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8408352366968606070L;

    public IsStoragePoolWithSameNameExistParameters(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    private String storagePoolName;

    public String getStoragePoolName() {
        return storagePoolName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsStoragePoolWithSameNameExistParameters() {
    }
}
