package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateParameters extends VmTemplateParametersBase implements HasVmIcon {

    private static final long serialVersionUID = 2114985552063499069L;

    public AddVmTemplateParameters() {
        privateVmTemplateID = Guid.Empty;
        templateType = VmEntityType.TEMPLATE;
    }

    private VmStatic _masterVm;
    private Guid privateVmTemplateID;
    private Guid destinationStorageDomainId;
    private HashMap<Guid, DiskImage> diskInfoDestinationMap;
    private VmEntityType templateType;
    private String vmLargeIcon;

    @Size(min = 1, max = 40, message = "VALIDATION_VM_TEMPLATE_NAME_MAX", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _name;

    @Pattern(regexp = ValidationUtils.ONLY_I18N_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String _description;

    private boolean publicUse;

    private boolean copyVmPermissions;
    /*
     * This parameter is used to decide if to create sound device or not if it is null then legacy logic will be used:
     * create device for desktop type
     */
    private Boolean soundDeviceEnabled;

    private Boolean consoleEnabled;

    private String templateVersionName;

    private Guid baseTemplateId;

    public AddVmTemplateParameters(VmStatic masterVm, String name, String description) {
        this();
        _masterVm = masterVm;
        _name = name;
        _description = description;
    }

    public AddVmTemplateParameters(VM vm, String name, String description) {
        this(vm.getStaticData(), name, description);
    }

    public VmStatic getMasterVm() {
        return _masterVm;
    }

    public void setMasterVm(VmStatic value) {
        _masterVm = value;
    }

    public String getName() {
        return _name;
    }

    public void setName(String value) {
        _name = value;
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

    public Boolean isSoundDeviceEnabled() {
        return soundDeviceEnabled;
    }

    public void setSoundDeviceEnabled(boolean soundDeviceEnabled) {
        this.soundDeviceEnabled = soundDeviceEnabled;
    }

    public Boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(Boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

    public boolean isCopyVmPermissions() {
        return copyVmPermissions;
    }

    public void setCopyVmPermissions(boolean copyVmPermissions) {
        this.copyVmPermissions = copyVmPermissions;
    }

    public VmEntityType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(VmEntityType templateType) {
        this.templateType = templateType;
    }

    public String getTemplateVersionName() {
        return templateVersionName;
    }

    public void setTemplateVersionName(String templateVersionName) {
        this.templateVersionName = templateVersionName;
    }

    public Guid getBaseTemplateId() {
        return baseTemplateId;
    }

    public void setBaseTemplateId(Guid baseTemplateId) {
        this.baseTemplateId = baseTemplateId;
    }

    @Override public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    @Override public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }
}
