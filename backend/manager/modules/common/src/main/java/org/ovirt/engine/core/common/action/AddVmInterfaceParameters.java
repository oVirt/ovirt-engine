package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AddVmInterfaceParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -816057026613138800L;

    @Valid
    private VmNetworkInterface nic;

    private String networkName;
    private boolean portMirroring;

    @Valid
    private List<VmNicFilterParameter> filterParameters;

    public AddVmInterfaceParameters() {
    }

    public AddVmInterfaceParameters(Guid vmId, VmNetworkInterface iface) {
        super(vmId);
        nic = iface;
    }

    /**
     * This c'tor is used only for backward compatibility of the rest api for adding or updating vnic, where the old api
     * expects network name and optionally port mirroring which were replaced by the vnic profile id in the new API.
     *
     * @param vmId
     *            the VM's ID
     * @param iface
     *            the interface entity to add/update
     * @param networkName
     *            the network name which a vnic profile will be searched for. {@code ""} represents an empty network.
     * @param portMirroring
     *            indicates if port mirroring should be set for the network
     */
    @Deprecated
    public AddVmInterfaceParameters(Guid vmId, VmNetworkInterface nic, String networkName, boolean portMirroring) {
        super(vmId);
        this.nic = nic;
        this.networkName = networkName;
        this.portMirroring = portMirroring;
    }

    public VmNetworkInterface getInterface() {
        return nic;
    }

    public String getNetworkName() {
        return networkName;
    }

    public boolean isPortMirroring() {
        return portMirroring;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb).
                append("nic", getInterface()).
                append("network", getNetworkName());
    }

    public List<VmNicFilterParameter> getFilterParameters() {
        return filterParameters;
    }

    public void setFilterParameters(List<VmNicFilterParameter> filterParameters) {
        this.filterParameters = filterParameters;
    }
}
