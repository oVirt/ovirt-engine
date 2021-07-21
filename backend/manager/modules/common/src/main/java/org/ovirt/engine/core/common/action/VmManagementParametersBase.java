package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.EditableDeviceOnVmStatusField;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.annotation.HostedEngineUpdate;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@HostedEngineUpdate(groups = UpdateEntity.class)
public class VmManagementParametersBase extends VmOperationParameterBase
        implements HasGraphicsDevices, HasVmIcon, HasRngDevice {

    private static final long serialVersionUID = -1956210836775846184L;

    /**
     * This class combines a value and update flag. If update flag is false, the value is not used to update the VM.
     * This is used to maintain backward compatibility in REST API: when null value comes from REST API it doesn't mean
     * the value must be cleaned in the VM. REST API has separate commands to update values marked as Optional&lt;T&gt;
     * here.
     *
     * @param T type of the value
     */
    public static class Optional<T> implements Serializable {

        private static final long serialVersionUID = 2456445711176920294L;

        private boolean update;
        private T value;

        public Optional() {
        }

        public boolean isUpdate() {
            return update;
        }

        public void setUpdate(boolean update) {
            this.update = update;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

    }

    @Valid
    private VmStatic vmStatic;
    private boolean makeCreatorExplicitOwner;
    private Guid storageDomainId = Guid.Empty;
    private Map<Guid, DiskImage> diskInfoDestinationMap;
    private VmPayload payload;
    private boolean clearPayload;
    private VM vm;
    private boolean copyTemplatePermissions;
    private boolean applyChangesLater;
    private boolean updateNuma;
    private String vmLargeIcon;
    private Version clusterLevelChangeFromVersion;
    private Map<VmExternalDataKind, String> vmExternalData;

    /**
     * Extra flag to allow memory hot unplug. Memory hot unplug requires both this flag and {@link #applyChangesLater}
     * to be set or unset, respectively.
     *
     * <p>Hot unplug memory should only be allowed from REST API.</p>
     */
    private boolean memoryHotUnplugEnabled;

    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.WATCHDOG, type = VmDeviceType.WATCHDOG)
    private Optional<VmWatchdog> watchdog = new Optional<>();

    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.RNG, type = VmDeviceType.VIRTIO, name="rng")
    private Optional<VmRngDevice> rngDevice = new Optional<>();

    /*
     * This parameter is used to decide if to create sound device or not
     * if it is null then:
     * for add vm legacy logic will be used: create device for desktop type
     * for update the current configuration will remain
     */
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.SOUND, type = VmDeviceType.UNKNOWN, name="sound", isReadOnly = true)
    private Boolean soundDeviceEnabled;

    /*
     * This parameter is used to decide if to create TPM device or not if it is null then:
     * for add vm don't add TPM device
     * for unsupported configuration don't add TPM device
     * for other update the current configuration will remain
     */
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.TPM, type = VmDeviceType.TPM, name="tpm")
    private Boolean tpmEnabled;

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
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.CONTROLLER, type = VmDeviceType.VIRTIOSCSI, name="virtioscsi")
    private Boolean virtioScsiEnabled;

    /**
     * This attribute contains information about graphics devices.
     *
     * Graphics device of VM is touched only if there is an entry in this map (non-null for adding/updating,
     * null for removing the device. If the map doesn't contain entry for graphics type, VM's graphics card
     * of this type is not modified.
     */
    @EditableDeviceOnVmStatusField(generalType = VmDeviceGeneralType.GRAPHICS, type = VmDeviceType.UNKNOWN,
                                   name = "graphicsProtocol")
    private Map<GraphicsType, GraphicsDevice> graphicsDevices = new HashMap<>();

    private List<AffinityGroup> affinityGroups;

    /**
     * This attribute contains information about affinity labels.
     *
     *  Update VM - if null preserve current configuration
     */
    private List<Label> affinityLabels;

    public VmManagementParametersBase() {
    }

    public VmManagementParametersBase(VmStatic vmStatic) {
        super(vmStatic.getId());
        this.vmStatic = vmStatic;
    }

    public VmManagementParametersBase(VM vm) {
        this(vm.getStaticData());
    }

    public VmManagementParametersBase(VmManagementParametersBase baseParams) {
        this(baseParams.getVmStaticData());
        setMakeCreatorExplicitOwner(baseParams.isMakeCreatorExplicitOwner());
        setStorageDomainId(baseParams.getStorageDomainId());
        setVmPayload(baseParams.getVmPayload());
        setClearPayload(baseParams.isClearPayload());
        setCopyTemplatePermissions(baseParams.isCopyTemplatePermissions());
        setApplyChangesLater(baseParams.applyChangesLater);
        setClusterLevelChangeFromVersion(baseParams.getClusterLevelChangeFromVersion());
        setMemoryHotUnplugEnabled(baseParams.isMemoryHotUnplugEnabled());

        setSoundDeviceEnabled(baseParams.isSoundDeviceEnabled());
        setConsoleEnabled(baseParams.isConsoleEnabled());
        setVirtioScsiEnabled(baseParams.isVirtioScsiEnabled());
        setUpdateNuma(baseParams.isUpdateNuma());
        setUpdateRngDevice(baseParams.isUpdateRngDevice());
        setRngDevice(baseParams.getRngDevice());
        setUpdateWatchdog(baseParams.isUpdateWatchdog());
        setWatchdog(baseParams.getWatchdog());
        setAffinityGroups(baseParams.getAffinityGroups());
        setAffinityLabels(baseParams.getAffinityLabels());

        getGraphicsDevices().putAll(baseParams.getGraphicsDevices());
        setDiskInfoDestinationMap(baseParams.getDiskInfoDestinationMap());

        setVmLargeIcon(baseParams.getVmLargeIcon());
    }

    public VmStatic getVmStaticData() {
        return vmStatic;
    }

    public void setVmStaticData(VmStatic value) {
        vmStatic = value;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public VM getVm() {
        if (vm == null) {
            vm = new VM();
            vm.setStaticData(vmStatic);
        }
        return vm;
    }

    public void setVm(VM value) {
        // to make the getVm() use the new value
        vm = null;
        vmStatic = value.getStaticData();
    }

    public void setMakeCreatorExplicitOwner(boolean makeCreatorExplicitOwner) {
        this.makeCreatorExplicitOwner = makeCreatorExplicitOwner;
    }

    public boolean isMakeCreatorExplicitOwner() {
        return makeCreatorExplicitOwner;
    }

    public Map<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<Guid, DiskImage> diskInfoDestinationMap) {
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

    public VmWatchdog getWatchdog() {
        return watchdog.getValue();
    }

    public void setWatchdog(VmWatchdog watchdog) {
        this.watchdog.setValue(watchdog);
    }

    public VmRngDevice getRngDevice() {
        return rngDevice.getValue();
    }

    public void setRngDevice(VmRngDevice rngDevice) {
        this.rngDevice.setValue(rngDevice);
        if (this.rngDevice.getValue() != null) {
            this.rngDevice.getValue().setVmId(getVmId());
        }
    }

    public boolean isUpdateRngDevice() {
        return rngDevice.isUpdate();
    }

    public void setUpdateRngDevice(boolean updateRngDevice) {
        this.rngDevice.setUpdate(updateRngDevice);
    }

    public boolean isUpdateWatchdog() {
        return watchdog.isUpdate();
    }

    public void setUpdateWatchdog(boolean updateWatchdog) {
        this.watchdog.setUpdate(updateWatchdog);
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

    public Boolean isTpmEnabled() {
        return tpmEnabled;
    }

    public void setTpmEnabled(Boolean tpmEnabled) {
        this.tpmEnabled = tpmEnabled;
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

    /**
     * Since NUMA configuration can be updated, this flag indicates whether client
     * sends NUMA info that needs to be updated.
     */
    public boolean isUpdateNuma() {
        return updateNuma;
    }

    public void setUpdateNuma(boolean updateNuma) {
        this.updateNuma = updateNuma;
    }

    public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }

    public List<AffinityGroup> getAffinityGroups() {
        return affinityGroups;
    }

    public void setAffinityGroups(List<AffinityGroup> affinityGroups) {
        this.affinityGroups = affinityGroups;
    }

    public List<Label> getAffinityLabels() {
        return affinityLabels;
    }

    public void setAffinityLabels(List<Label> affinityLabels) {
        this.affinityLabels = affinityLabels;
    }

    @Override
    public Map<GraphicsType, GraphicsDevice> getGraphicsDevices() {
        return graphicsDevices;
    }

    public Version getClusterLevelChangeFromVersion() {
        return clusterLevelChangeFromVersion;
    }

    public void setClusterLevelChangeFromVersion(Version clusterLevelChangeFromVersion) {
        this.clusterLevelChangeFromVersion = clusterLevelChangeFromVersion;
    }

    public boolean isMemoryHotUnplugEnabled() {
        return memoryHotUnplugEnabled;
    }

    public void setMemoryHotUnplugEnabled(boolean memoryHotUnplugEnabled) {
        this.memoryHotUnplugEnabled = memoryHotUnplugEnabled;
    }

    public Map<VmExternalDataKind, String> getVmExternalData() {
        return vmExternalData;
    }

    public void setVmExternalData(Map<VmExternalDataKind, String> vmExternalData) {
        this.vmExternalData = vmExternalData;
    }
}
