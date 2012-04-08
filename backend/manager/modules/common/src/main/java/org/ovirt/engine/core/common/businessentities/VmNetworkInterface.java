package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VmNetworkInterface</code> defines a type of {@link BaseNetworkInterface} for instances of {@link VM}.
 *
 */
public class VmNetworkInterface extends NetworkInterface<VmNetworkStatistics> {
    private static final long serialVersionUID = 7428150502868988886L;

    private static final ArrayList<String> _changeablePropertiesList =
            new ArrayList<String>(Arrays.asList(new String[] {
                    "Id", "Name", "MacAddress", "NetworkName", "Type", "Speed", "Statistics", "VmId", "VmName",
                    "VmTemplateId"
            }));

    private NGuid vmId;
    private String vmName;
    private NGuid vmTemplateId;

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
}
