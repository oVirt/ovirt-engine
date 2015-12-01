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

public class VmTemplateParametersBase extends VdcActionParametersBase implements Serializable, HasGraphicsDevices {
    private static final long serialVersionUID = -8930994274659598061L;
    private Guid vmTemplateId;
    private Guid quotaId;
    private boolean privateCheckDisksExists;
    private VmWatchdog watchdog;
    private Boolean virtioScsiEnabled;
    private Boolean balloonEnabled;

   /**
    * This attribute contains information about graphics devices.
    *
    * Graphics device of VM is touched only if there is an entry in this map (non-null for adding/updating,
    * null for removing the device. If the map doesn't contain entry for graphics type, VM's graphics card
    * of this type is not modified.
    */
    private Map<GraphicsType, GraphicsDevice> graphicsDevices;

    private VmRngDevice rngDevice;
    /*
     * see VmManagementParametersBase#updateWatchdog for details
     */
    private boolean updateWatchdog;

    /*
     * see VmManagementParametersBase#updateRngDevice for details
     */
    private boolean updateRngDevice;

    private Guid cpuProfileId;

    public boolean getCheckDisksExists() {
        return privateCheckDisksExists;
    }

    public void setCheckDisksExists(boolean value) {
        privateCheckDisksExists = value;
    }

    public VmTemplateParametersBase(Guid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
        graphicsDevices = new HashMap<>();
    }

    public Guid getVmTemplateId() {
        return vmTemplateId;
    }

    private List<Guid> privateStorageDomainsList;

    public List<Guid> getStorageDomainsList() {
        return privateStorageDomainsList;
    }

    public void setStorageDomainsList(List<Guid> value) {
        privateStorageDomainsList = value;
    }

    public VmTemplateParametersBase() {
        this(Guid.Empty);
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid value) {
        quotaId = value;
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
    public boolean isUpdateRngDevice() {
        return updateRngDevice;
    }

    public void setVirtioScsiEnabled(Boolean virtioScsiEnabled) {
        this.virtioScsiEnabled = virtioScsiEnabled;
    }

    public Boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(Boolean balloonEnabled) {
        this.balloonEnabled = balloonEnabled;
    }
    public void setUpdateRngDevice(boolean updateRngDevice) {
        this.updateRngDevice = updateRngDevice;
        if (this.rngDevice != null) {
            this.rngDevice.setVmId(getVmTemplateId());
        }
    }

    public VmRngDevice getRngDevice() {
        return rngDevice;
    }

    public void setRngDevice(VmRngDevice rngDevice) {
        this.rngDevice = rngDevice;
    }

    public Guid getCpuProfileId() {
        return cpuProfileId;
    }

    public void setCpuProfileId(Guid cpuProfileId) {
        this.cpuProfileId = cpuProfileId;
    }

    @Override
    public Map<GraphicsType, GraphicsDevice> getGraphicsDevices() {
        return graphicsDevices;
    }

}
