package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllFromExportDomainQueryParamenters")
public class GetAllFromExportDomainQueryParamenters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 5436719744430725750L;
    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement(name = "IdsGuidArray")
    private java.util.ArrayList<Guid> privateIds;

    public java.util.ArrayList<Guid> getIds() {
        return privateIds;
    }

    public void setIds(java.util.ArrayList<Guid> value) {
        privateIds = value;
    }

    @XmlElement(name = "GetAll")
    private boolean privateGetAll;

    public boolean getGetAll() {
        return privateGetAll;
    }

    public void setGetAll(boolean value) {
        privateGetAll = value;
    }

    public GetAllFromExportDomainQueryParamenters(Guid storagePoolId, Guid storageDomainId) {
        // for getting existing Vm as well
        setGetAll(false);
        this.setStoragePoolId(storagePoolId);
        this.setStorageDomainId(storageDomainId);
    }

    public GetAllFromExportDomainQueryParamenters() {
    }
}
