package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class AddVmPoolParameters extends VmPoolOperationParameters implements HasGraphicsDevices {
    private static final long serialVersionUID = 4826143612049185740L;

    @Valid
    private VmStatic vmStaticData;
    private int vmsCount;
    private Map<Guid, DiskImage> diskInfoDestinationMap;
    private Boolean soundDeviceEnabled;
    private Boolean tpmEnabled;
    private Boolean consoleEnabled;
    private Boolean virtioScsiEnabled;
    private VmRngDevice rngDevice;
    private Map<GraphicsType, GraphicsDevice> graphicsDevices;
    private String vmLargeIcon;
    private Boolean seal;

    public AddVmPoolParameters() {
    }

    public AddVmPoolParameters(VmPool vmPool, VM vm, int vmsCount) {
        super(vmPool);
        graphicsDevices = new HashMap<>();
        this.vmStaticData = vm.getStaticData();
        this.vmsCount = vmsCount;
    }

    public VmStatic getVmStaticData() {
        return vmStaticData;
    }

    public void setVmStaticData(VmStatic vmStaticData) {
        this.vmStaticData = vmStaticData;
    }

    public int getVmsCount() {
        return vmsCount;
    }

    public Map<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
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

    public Boolean isVirtioScsiEnabled() {
        return virtioScsiEnabled;
    }

    public void setVirtioScsiEnabled(Boolean virtioScsiEnabled) {
        this.virtioScsiEnabled = virtioScsiEnabled;
    }

    public VmRngDevice getRngDevice() {
        return rngDevice;
    }

    public void setRngDevice(VmRngDevice rngDevice) {
        this.rngDevice = rngDevice;
    }

    public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }

    public Boolean getSeal() {
        return seal;
    }

    public void setSeal(Boolean seal) {
        this.seal = seal;
    }

    @Override
    public Map<GraphicsType, GraphicsDevice> getGraphicsDevices() {
        return graphicsDevices;
    }

}
