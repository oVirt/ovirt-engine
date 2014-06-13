package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;

public class VmManagementParametersBase extends VmOperationParameterBase {

    private static final long serialVersionUID = -7695630335738521510L;

    @Valid
    private VmStatic _vmStatic;
    private boolean makeCreatorExplicitOwner;
    private Guid privateStorageDomainId;
    private HashMap<Guid, DiskImage> diskInfoDestinationMap;
    private VmPayload payload;
    private boolean clearPayload;
    private Boolean balloonEnabled;
    private VM vm;
    private VmWatchdog watchdog;
    private VmRngDevice rngDevice;
    private boolean copyTemplatePermissions;
    private boolean applyChangesLater;

    /*
     * This parameter is needed at update to make sure that when we get a null watchdog from rest-api it is not meant to
     * be removing the watchdog, rest-api will simply call watchdog commands directly. Default is false so to avoid
     * breaking rest-api.
     */
    private boolean updateWatchdog;
    /*
     * This parameter is used to decide if to create sound device or not if it is null then: for add vm legacy logic
     * will be used: create device for desktop type for update the current configuration will remain
     * Used by rest-api.
     */
    private boolean updateRngDevice;
    /*
     * This parameter is used to decide if to create sound device or not
     * if it is null then:
     * for add vm legacy logic will be used: create device for desktop type
     * for update the current configuration will remain
     */
    private Boolean soundDeviceEnabled;
    /*
     * This parameter is used to decide if to create console device or not if it is null then: for add vm don't add
     * console device for update the current configuration will remain
     */
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.CONSOLE, type = VmDeviceType.CONSOLE)
    private Boolean consoleEnabled;

    /*
     * This parameter is used to decide whether to attach a VirtIO-SCSI controller or not.
     * When value is null:
     * - Add VM - defaulted to true for cluster >= 3.3
     * - Update VM - preserve current configuration
     */
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.CONTROLLER, type = VmDeviceType.VIRTIOSCSI)
    private Boolean virtioScsiEnabled;

    public VmManagementParametersBase() {
        privateStorageDomainId = Guid.Empty;
        consoleEnabled = Boolean.FALSE;
    }

    public VmManagementParametersBase(VmStatic vmStatic) {
        super(vmStatic.getId());
        _vmStatic = vmStatic;
        privateStorageDomainId = Guid.Empty;
        consoleEnabled = Boolean.FALSE;
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
        // to make the getVm() use the new value
        vm = null;
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

    public Boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(Boolean isBallonEnabled) {
        this.balloonEnabled = isBallonEnabled;
    }

    public VmWatchdog getWatchdog() {
        return watchdog;
    }

    public void setWatchdog(VmWatchdog watchdog) {
        this.watchdog = watchdog;
    }

    public VmRngDevice getRngDevice() {
        return rngDevice;
    }

    public void setRngDevice(VmRngDevice rngDevice) {
        this.rngDevice = rngDevice;
        if (this.rngDevice != null) {
            this.rngDevice.setVmId(getVmId());
        }
    }

    public boolean isUpdateRngDevice() {
        return updateRngDevice;
    }

    public void setUpdateRngDevice(boolean updateRngDevice) {
        this.updateRngDevice = updateRngDevice;
    }

    public boolean isUpdateWatchdog() {
        return updateWatchdog;
    }

    public void setUpdateWatchdog(boolean updateWatchdog) {
        this.updateWatchdog = updateWatchdog;
    }

    public Boolean isConsoleEnabled() {
        return consoleEnabled;
    }

    public void setConsoleEnabled(Boolean consoleEnabled) {
        this.consoleEnabled = consoleEnabled;
    }

    public Boolean isSoundDeviceEnabled() {
        return soundDeviceEnabled;
    }

    public void setSoundDeviceEnabled(boolean soundDeviceEnabled) {
        this.soundDeviceEnabled = soundDeviceEnabled;
    }

    public boolean isCopyTemplatePermissions() {
        return copyTemplatePermissions;
    }

    public void setCopyTemplatePermissions(boolean copyTemplatePermissions) {
        this.copyTemplatePermissions = copyTemplatePermissions;
    }

    public Boolean isVirtioScsiEnabled() {
        return virtioScsiEnabled;
    }

    public void setVirtioScsiEnabled(Boolean virtioScsiEnabled) {
        this.virtioScsiEnabled = virtioScsiEnabled;
    }

    public boolean isApplyChangesLater() {
        return applyChangesLater;
    }

    public void setApplyChangesLater(boolean applyChangesLater) {
        this.applyChangesLater = applyChangesLater;
    }
}
