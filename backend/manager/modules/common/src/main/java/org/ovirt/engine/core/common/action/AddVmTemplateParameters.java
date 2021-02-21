package org.ovirt.engine.core.common.action;

import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateParameters extends VmTemplateManagementParameters implements HasVmIcon {

    public enum Phase {
        CREATE_TEMPLATE,
        ASSIGN_ILLEGAL,
        SEAL,
        ASSIGN_LEGAL_SHARED
    }

    private static final long serialVersionUID = 2114985552063499069L;

    private VmStatic masterVm;

    private Guid vmTemplateId;
    private VmEntityType templateType;
    private String templateVersionName;
    private Guid baseTemplateId;

    private Guid destinationStorageDomainId;
    private Map<Guid, DiskImage> diskInfoDestinationMap;
    private String vmLargeIcon;

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE, message = "VALIDATION_VM_TEMPLATE_NAME_MAX", groups = { CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String name;

    @Pattern(regexp = ValidationUtils.ONLY_I18N_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS")
    private String description;

    private boolean publicUse;
    private boolean copyVmPermissions;
    private boolean sealTemplate;

    /*
     * This parameter is used to decide if to create sound device or not if it is null then legacy logic will be used:
     * create device for desktop type
     */
    private Boolean soundDeviceEnabled;
    private Boolean tpmEnabled;
    private Boolean consoleEnabled;

    private Phase phase = Phase.CREATE_TEMPLATE;

    public AddVmTemplateParameters() {
        vmTemplateId = Guid.Empty;
        templateType = VmEntityType.TEMPLATE;
    }

    public AddVmTemplateParameters(VmStatic masterVm, String name, String description) {
        this();
        this.masterVm = masterVm;
        this.name = name;
        this.description = description;
    }

    public AddVmTemplateParameters(VM vm, String name, String description) {
        this(vm.getStaticData(), name, description);
    }

    public VmStatic getMasterVm() {
        return masterVm;
    }

    public void setMasterVm(VmStatic masterVm) {
        this.masterVm = masterVm;
    }

    public VM getVm() {
        VM vm = new VM();
        vm.setStaticData(masterVm);
        return vm;
    }

    public void setVm(VM vm) {
        masterVm = vm.getStaticData();
    }

    @Override
    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    public void setVmTemplateId(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
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

    public Guid getDestinationStorageDomainId() {
        return destinationStorageDomainId;
    }

    public void setDestinationStorageDomainId(Guid destinationStorageDomainId) {
        this.destinationStorageDomainId = destinationStorageDomainId;
    }

    public Map<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }

    @Override
    public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    @Override
    public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public void setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
    }

    public boolean isCopyVmPermissions() {
        return copyVmPermissions;
    }

    public void setCopyVmPermissions(boolean copyVmPermissions) {
        this.copyVmPermissions = copyVmPermissions;
    }

    public boolean isSealTemplate() {
        return sealTemplate;
    }

    public void setSealTemplate(boolean sealTemplate) {
        this.sealTemplate = sealTemplate;
    }

    public Boolean isSoundDeviceEnabled() {
        return soundDeviceEnabled;
    }

    public void setSoundDeviceEnabled(boolean soundDeviceEnabled) {
        this.soundDeviceEnabled = soundDeviceEnabled;
    }

    public Boolean isTpmEnabled() {
        return tpmEnabled;
    }

    public void setTpmEnabled(Boolean tpmEnabled) {
        this.tpmEnabled = tpmEnabled;
    }

    public Boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(Boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

}
