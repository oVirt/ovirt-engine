package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class VmManagementParametersBase extends VmOperationParameterBase {

    private static final long serialVersionUID = -7695630335738521510L;

    @Valid
    private VmStatic _vmStatic;
    private boolean makeCreatorExplicitOwner;
    private Guid privateStorageDomainId = Guid.Empty;
    private boolean privateDontCheckTemplateImages;
    private HashMap<Guid, Guid> imageToDestinationDomainMap;

    public VmManagementParametersBase() {
    }

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

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

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

    public void setImageToDestinationDomainMap(HashMap<Guid, Guid> imageToDestinationDomainMap) {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    public HashMap<Guid, Guid> getImageToDestinationDomainMap() {
        return imageToDestinationDomainMap;
    }

}
