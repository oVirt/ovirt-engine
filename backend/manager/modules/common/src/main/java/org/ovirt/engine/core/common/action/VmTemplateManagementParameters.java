package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.compat.Guid;

public class VmTemplateManagementParameters extends VmTemplateParameters implements Serializable, HasGraphicsDevices {

    private static final long serialVersionUID = -5210192426813531486L;

    private boolean checkDisksExists;
    private VmWatchdog watchdog;
    private boolean updateWatchdog;
    private Boolean virtioScsiEnabled;

    /**
     * This attribute contains information about graphics devices.
     *
     * Graphics device of VM is touched only if there is an entry in this map (non-null for adding/updating,
     * null for removing the device. If the map doesn't contain entry for graphics type, VM's graphics card
     * of this type is not modified.
     */
    private Map<GraphicsType, GraphicsDevice> graphicsDevices = new HashMap<>();

    private VmRngDevice rngDevice;
    private boolean updateRngDevice;

    private List<Guid> storageDomainsList;

    public VmTemplateManagementParameters() {
    }

    public VmTemplateManagementParameters(Guid vmTemplateId) {
        super(vmTemplateId);
    }

    public boolean isCheckDisksExists() {
        return checkDisksExists;
    }

    public void setCheckDisksExists(boolean checkDisksExists) {
        this.checkDisksExists = checkDisksExists;
    }

    public VmWatchdog getWatchdog() {
        return watchdog;
    }

    public void setWatchdog(VmWatchdog watchdog) {
        this.watchdog = watchdog;
    }

    public boolean isUpdateWatchdog() {
        return updateWatchdog;
    }

    public void setUpdateWatchdog(boolean updateWatchdog) {
        this.updateWatchdog = updateWatchdog;
    }

    public Boolean isVirtioScsiEnabled() {
        return virtioScsiEnabled;
    }

    public void setVirtioScsiEnabled(Boolean virtioScsiEnabled) {
        this.virtioScsiEnabled = virtioScsiEnabled;
    }

    @Override
    public Map<GraphicsType, GraphicsDevice> getGraphicsDevices() {
        return graphicsDevices;
    }

    public VmRngDevice getRngDevice() {
        return rngDevice;
    }

    public void setRngDevice(VmRngDevice rngDevice) {
        this.rngDevice = rngDevice;
    }

    public boolean isUpdateRngDevice() {
        return updateRngDevice;
    }

    public void setUpdateRngDevice(boolean updateRngDevice) {
        this.updateRngDevice = updateRngDevice;
        if (this.rngDevice != null) {
            this.rngDevice.setVmId(getVmTemplateId());
        }
    }

    public List<Guid> getStorageDomainsList() {
        return storageDomainsList;
    }

    public void setStorageDomainsList(List<Guid> storageDomainsList) {
        this.storageDomainsList = storageDomainsList;
    }

}
