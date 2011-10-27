package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsStoragePoolWithSameNameExistParameters")
public class IsStoragePoolWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8408352366968606070L;

    public IsStoragePoolWithSameNameExistParameters(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    @XmlElement(name = "StoragePoolName")
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
