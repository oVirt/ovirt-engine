package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmManagementParametersBase")
public class VmManagementParametersBase extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = -7695630335738521510L;

    public VmManagementParametersBase() {
    }

    @Valid
    private VmStatic _vmStatic;

    @XmlElement(name = "MakeCreatorExplicitOwner")
    private boolean makeCreatorExplicitOwner;

    public VmManagementParametersBase(VmStatic vmStatic) {
        super(vmStatic.getId());
        _vmStatic = vmStatic;
    }

    public VmManagementParametersBase(VM vm) {
        this(vm.getStaticData());
    }

    public VmStatic getVmStaticData() {
        return _vmStatic;
    }

    public void setVmStaticData(VmStatic value) {
        _vmStatic = value;
    }

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement(name = "DontCheckTemplateImages")
    private boolean privateDontCheckTemplateImages;

    public boolean getDontCheckTemplateImages() {
        return privateDontCheckTemplateImages;
    }

    public void setDontCheckTemplateImages(boolean value) {
        privateDontCheckTemplateImages = value;
    }

    private boolean privateDontAttachToDefaultTag;

    public boolean getDontAttachToDefaultTag() {
        return privateDontAttachToDefaultTag;
    }

    public void setDontAttachToDefaultTag(boolean value) {
        privateDontAttachToDefaultTag = value;
    }

    @XmlElement(name = "_vm")
    public VM getVm() {
        VM vm = new VM();
        vm.setStaticData(_vmStatic);
        return vm;
    }

    public void setVm(VM value) {
        _vmStatic = value.getStaticData();
    }

    public void setMakeCreatorExplicitOwner(boolean makeCreatorExplicitOwner) {
        this.makeCreatorExplicitOwner = makeCreatorExplicitOwner;
    }

    public boolean isMakeCreatorExplicitOwner() {
        return makeCreatorExplicitOwner;
    }

}
