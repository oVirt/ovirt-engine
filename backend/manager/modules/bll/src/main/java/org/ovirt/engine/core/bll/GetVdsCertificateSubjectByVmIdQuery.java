package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.utils.CertificateSubjectHelper;
import org.ovirt.engine.core.compat.Guid;

public class GetVdsCertificateSubjectByVmIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsCertificateSubjectByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // Initially we set the command as failed:
        VdcQueryReturnValue queryReturnValue = getQueryReturnValue();
        queryReturnValue.setSucceeded(false);

        // Check if the virtual machine is running on a host, and if does then retrieve the host and copy the subject
        // of the certificate to the value returned by the query:
        Guid vmId = getParameters().getId();
        if (vmId != null) {
            VmDynamic vm = getDbFacade().getVmDynamicDao().get(vmId);
            if (vm != null) {
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    VdsStatic vds = getDbFacade().getVdsStaticDao().get(vdsId);
                    queryReturnValue.setSucceeded(true);
                    queryReturnValue.setReturnValue(CertificateSubjectHelper.getCertificateSubject(vds.getHostName()));
                }
            }
        }
    }
}
