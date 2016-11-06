package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmTemplateInterfaceParameters extends VmTemplateParameters {
    private static final long serialVersionUID = 5177618608953713542L;

    @Valid
    private VmNetworkInterface nic;

    private String networkName;
    private boolean portMirroring;

    public AddVmTemplateInterfaceParameters() {
    }

    public AddVmTemplateInterfaceParameters(Guid templateId, VmNetworkInterface nic) {
        super(templateId);
        this.nic = nic;
    }

    /**
     * This c'tor is used only for backward compatibility of the rest api for adding or updating vnic, where the old api
     * expects network name and optionally port mirroring which were replaced by the vnic profile id in the new API.
     *
     * @param templateId
     *            the template's ID
     * @param iface
     *            the interface entity to add/update
     * @param networkName
     *            the network name which a vnic profile will be searched for. {@code ""} represents an empty network.
     * @param portMirroring
     *            indicates if port mirroring should be set for the network
     */
    @Deprecated
    public AddVmTemplateInterfaceParameters(Guid templateId,
            VmNetworkInterface nic,
            String networkName,
            boolean portMirroring) {
        super(templateId);
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
}
