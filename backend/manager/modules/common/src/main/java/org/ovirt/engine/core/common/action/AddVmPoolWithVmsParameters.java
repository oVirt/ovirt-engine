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

public class AddVmPoolWithVmsParameters extends VmPoolOperationParameters
        implements HasGraphicsDevices {
    private static final long serialVersionUID = 4826143612049185740L;

    @Valid
    private VmStatic vmStaticData;
    private int vmsCount;
    private int diskSize;
    private HashMap<Guid, DiskImage> diskInfoDestinationMap;
    private Boolean soundDeviceEnabled;
    private Boolean consoleEnabled;
    private Boolean virtioScsiEnabled;
    private boolean balloonEnabled;
    private VmRngDevice rngDevice;
    private Map<GraphicsType, GraphicsDevice> graphicsDevices;
    private String vmLargeIcon;

    public AddVmPoolWithVmsParameters() {
    }

    public AddVmPoolWithVmsParameters(VmPool vmPool, VM vm, int vmsCount, int diskSize) {
        super(vmPool);
        graphicsDevices = new HashMap<>();
        this.vmStaticData = vm.getStaticData();
        this.vmsCount = vmsCount;
        this.diskSize = diskSize;
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

    public int getDiskSize() {
        return diskSize;
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

    public boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(boolean isBallonEnabled) {
        this.balloonEnabled = isBallonEnabled;
    }

    public String getVmLargeIcon() {
        return vmLargeIcon;
    }

    public void setVmLargeIcon(String vmLargeIcon) {
        this.vmLargeIcon = vmLargeIcon;
    }

    @Override
    public Map<GraphicsType, GraphicsDevice> getGraphicsDevices() {
        return graphicsDevices;
    }

}
