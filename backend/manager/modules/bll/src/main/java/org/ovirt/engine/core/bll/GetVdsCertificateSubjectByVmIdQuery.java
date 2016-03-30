package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;

public class GetVdsCertificateSubjectByVmIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsCertificateSubjectByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);
        VdcQueryReturnValue returnValue = null;
        Guid vmId = getParameters().getId();
        if (vmId != null) {
            VM vm = getDbFacade().getVmDao().get(vmId);
            if (vm != null) {
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    VDS vds = getDbFacade().getVdsDao().get(vdsId);
                    returnValue = new VdcQueryReturnValue();
                    returnValue.setReturnValue(vds.getCertificateSubject());
                }
            }
        }
        if (returnValue != null) {
            getQueryReturnValue().setSucceeded(returnValue.getSucceeded());
            getQueryReturnValue().setReturnValue(returnValue.getReturnValue());
        }
    }
}
