package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainAndPoolQueryParameters")
public class StorageDomainAndPoolQueryParameters extends StorageDomainQueryParametersBase {
    private static final long serialVersionUID = -1397159559995940530L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public StorageDomainAndPoolQueryParameters(Guid storageDomainId, Guid storagePoolId) {
        super(storageDomainId);
        setStoragePoolId(storagePoolId);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

    public StorageDomainAndPoolQueryParameters() {
    }
}
