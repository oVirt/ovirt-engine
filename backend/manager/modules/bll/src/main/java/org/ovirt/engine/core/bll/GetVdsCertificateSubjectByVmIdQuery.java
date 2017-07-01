package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.CertificateSubjectHelper;

public class GetVdsCertificateSubjectByVmIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    public GetVdsCertificateSubjectByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Initially we set the command as failed:
        QueryReturnValue queryReturnValue = getQueryReturnValue();
        queryReturnValue.setSucceeded(false);

        // Check if the virtual machine is running on a host, and if does then retrieve the host and copy the subject
        // of the certificate to the value returned by the query:
        Guid vmId = getParameters().getId();
        if (vmId != null) {
            VmDynamic vm = vmDynamicDao.get(vmId);
            if (vm != null) {
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    VdsStatic vds = vdsStaticDao.get(vdsId);
                    queryReturnValue.setSucceeded(true);
                    queryReturnValue.setReturnValue(CertificateSubjectHelper.getCertificateSubject(vds.getHostName()));
                }
            }
        }
    }
}
