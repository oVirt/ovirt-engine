package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateParameters extends VmTemplateParametersBase {

    private static final long serialVersionUID = 2114985552063499069L;

    public AddVmTemplateParameters() {
    }

    private VmStatic _masterVm;
    private Guid privateVmTemplateID = Guid.Empty;
    private Guid destinationStorageDomainId;
    private HashMap<Guid, DiskImage> diskInfoDestinationMap;

    @Size(max = 40, message = "VALIDATION.VM_TEMPLATE.NAME.MAX", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _name;

    @Pattern(regexp = ValidationUtils.ONLY_I18N_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _description;

    private boolean publicUse = false;

    public AddVmTemplateParameters(VmStatic masterVm, String name, String description) {
        _masterVm = masterVm;
        _name = name;
        _description = description;
    }

    public AddVmTemplateParameters(VM vm, String name, String description) {
        _masterVm = vm.getStaticData();
        _name = name;
        _description = description;
    }

    public VmStatic getMasterVm() {
        return _masterVm;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    @Override
    public Guid getVmTemplateId() {
        return privateVmTemplateID;
    }

    public void setVmTemplateId(Guid value) {
        privateVmTemplateID = value;
    }

    public VM getVm() {
        VM vm = new VM();
        vm.setStaticData(_masterVm);
        return vm;
    }

    public void setVm(VM value) {
        _masterVm = value.getStaticData();
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public void setDestinationStorageDomainId(Guid destinationStorageDomainId) {
        this.destinationStorageDomainId = destinationStorageDomainId;
    }

    public Guid getDestinationStorageDomainId() {
        return destinationStorageDomainId;
    }

    public HashMap<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(HashMap<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }
}
