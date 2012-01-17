package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplateParametersBase")
public class VmTemplateParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -8930994274659598061L;
    private boolean removeTemplateFromDb;
    @XmlElement
    private Guid _vmTemplateId = new Guid();
    @XmlElement(name = "CheckDisksExists")
    private boolean privateCheckDisksExists;

    public boolean getCheckDisksExists() {
        return privateCheckDisksExists;
    }

    public void setCheckDisksExists(boolean value) {
        privateCheckDisksExists = value;
    }

    public VmTemplateParametersBase(Guid vmTemplateId) {
        _vmTemplateId = vmTemplateId;
    }

    public Guid getVmTemplateId() {
        return _vmTemplateId;
    }

    @XmlElement(name = "StorageDomainsListGuidArray")
    private List<Guid> privateStorageDomainsList;

    public List<Guid> getStorageDomainsList() {
        return privateStorageDomainsList;
    }

    public void setStorageDomainsList(List<Guid> value) {
        privateStorageDomainsList = value;
    }

    public VmTemplateParametersBase() {
    }

    public void setRemoveTemplateFromDb(boolean removeTemplateFromDb) {
        this.removeTemplateFromDb = removeTemplateFromDb;
    }

    public boolean isRemoveTemplateFromDb() {
        return removeTemplateFromDb;
    }
}
