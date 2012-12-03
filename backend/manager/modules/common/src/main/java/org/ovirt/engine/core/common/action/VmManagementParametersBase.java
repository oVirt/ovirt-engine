package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class VmManagementParametersBase extends VmOperationParameterBase {

    private static final long serialVersionUID = -7695630335738521510L;

    @Valid
    private VmStatic _vmStatic;
    private boolean makeCreatorExplicitOwner;
    private Guid privateStorageDomainId = Guid.Empty;
    private boolean privateDontCheckTemplateImages;
    private HashMap<Guid, DiskImage> diskInfoDestinationMap;
    private VmPayload payload;
    private boolean clearPayload;
    private boolean balloonEnabled = true;
    private VM vm = null;

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
        if (vm == null) {
            vm = new VM();
            vm.setStaticData(_vmStatic);
        }
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

    public HashMap<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(HashMap<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }

    public VmPayload getVmPayload() {
        return this.payload;
    }

    public void setVmPayload(VmPayload value) {
        this.payload = value;
    }

    public boolean isClearPayload() {
        return clearPayload;
    }

    public void setClearPayload(boolean clearPayload) {
        this.clearPayload = clearPayload;
    }

    public boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(boolean isBallonEnabled) {
        this.balloonEnabled = isBallonEnabled;
    }
}
