package org.ovirt.engine.core.common.queries;

public class GetAllAuditLogsByVMTemplateNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -2914402501834292594L;

    /** The name of the VM to query Audit Logs by */
    private String vmTemplateName;

    public GetAllAuditLogsByVMTemplateNameParameters(String vmTemplateName) {
        this.vmTemplateName = vmTemplateName;
    }

    public GetAllAuditLogsByVMTemplateNameParameters() {
    }

    /** @return The name of the VM to query by */
    public String getVmTemplateName() {
        return vmTemplateName;
    }

}
