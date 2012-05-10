package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VmNetworkInterface</code> defines a type of {@link BaseNetworkInterface} for instances of {@link VM}.
 *
 */
public class VmNetworkInterface extends NetworkInterface<VmNetworkStatistics> {
    private static final long serialVersionUID = 7428150502868988886L;

    protected static final String VALIDATION_MESSAGE_MAC_ADDRESS_INVALID = "VALIDATION.VM.NETWORK.MAC.ADDRESS.INVALID";

    private NGuid vmId;
    private String vmName;
    private NGuid vmTemplateId;
    /* status of the nic. Active nic is one that is plugged to its VM */
    private boolean active = true;

    private static final ArrayList<String> _changeablePropertiesList =
            new ArrayList<String>(Arrays.asList(new String[] {
                    "Id", "Name", "MacAddress", "NetworkName", "Type", "Speed", "Statistics", "VmId", "VmName",
                    "VmTemplateId"
            }));

    public VmNetworkInterface() {
        super(new VmNetworkStatistics(), VmInterfaceType.pv.getValue());
    }

    /**
     * Sets the VM instance id.
     *
     * @param vmId
     *            the id
     */
    public void setVmId(NGuid vmId) {
        this.vmId = vmId;
        this.statistics.setVmId(vmId);
    }

    /**
     * Returns the VM instance id.
     *
     * @return the id
     */
    public NGuid getVmId() {
        return vmId;
    }

    /**
     * Sets the VM name.
     *
     * @param vmName
     *            the name
     */
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    /**
     * Returns the VM name.
     *
     * @return the name
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * Sets the VM template instance id.
     *
     * @param vmTemplateId
     *            the id
     */
    public void setVmTemplateId(NGuid vmTemplateId) {
        this.vmTemplateId = vmTemplateId;
    }

    /**
     * Returns the VM template instance id.
     *
     * @return the id
     */
    public NGuid getVmTemplateId() {
        return vmTemplateId;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _changeablePropertiesList;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Pattern(regexp = "(\\p{XDigit}{2}:){5}\\p{XDigit}{2}",
            message = VmNetworkInterface.VALIDATION_MESSAGE_MAC_ADDRESS_INVALID, groups = { CreateEntity.class,
                    UpdateEntity.class })
    @Override
    public String getMacAddress() {
        return super.getMacAddress();
    }
}
