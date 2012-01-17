package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainQueryParametersBase")
public class StorageDomainQueryParametersBase extends VdcQueryParametersBase {
    private static final long serialVersionUID = -1267869804833489615L;

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    private void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public StorageDomainQueryParametersBase(Guid storageDomainId) {
        setStorageDomainId(storageDomainId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        switch (queryType) {
        case GetStorageDomainListById:
        case GetStoragePoolsByStorageDomainId:
            return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
        default:
            return RegisterableQueryReturnDataType.IQUERYABLE;
        }
    }

    public StorageDomainQueryParametersBase() {
    }
}
