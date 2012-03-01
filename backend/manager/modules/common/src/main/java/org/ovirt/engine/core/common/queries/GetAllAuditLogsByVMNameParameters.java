package org.ovirt.engine.core.common.queries;

public class GetAllAuditLogsByVMNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7548846146921429465L;

    /** The name of the VM to query Audit Logs by */
    private String vmName;

    public GetAllAuditLogsByVMNameParameters(String vmName) {
        this.vmName = vmName;
    }

    public GetAllAuditLogsByVMNameParameters() {
    }

    /** @return The name of the VM to query by */
    public String getVmName() {
        return vmName;
    }

}
