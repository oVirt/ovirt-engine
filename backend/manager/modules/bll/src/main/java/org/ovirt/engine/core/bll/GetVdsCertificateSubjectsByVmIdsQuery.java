package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.CertificateSubjectHelper;


public class GetVdsCertificateSubjectsByVmIdsQuery<P extends IdsQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDynamicDao vmDynamicDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    public GetVdsCertificateSubjectsByVmIdsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        // Initially we set the command as failed:
        QueryReturnValue queryReturnValue = getQueryReturnValue();
        queryReturnValue.setSucceeded(false);

        // Check if the virtual machines are running on hosts, and if so then retrieve the hosts and copy the subject
        // of the certificate to the value returned by the query:
        List<VmDynamic> vms = vmDynamicDao.getByIds(getParameters().getIds());
        List<Guid> vdsIds = vms.stream()
                .map(VmDynamic::getRunOnVds)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!vdsIds.isEmpty()) {
            List<VdsStatic> vdss = vdsStaticDao.getByIds(vdsIds);
            // Collect certificate subjects for all hosts running the VMs
            Map<Guid, String> certificateSubjects = vdss.stream()
                    .collect(Collectors.toMap(
                            VdsStatic::getId,
                            vds -> CertificateSubjectHelper.getCertificateSubject(vds.getHostName())));
            // Populate the certificate subjects for corresponding VMs
            Map<Guid, String> certificateForVms = new HashMap<>();
            for (VmDynamic vm : vms) {
                Guid vdsId = vm.getRunOnVds();
                if (vdsId != null) {
                    String subject = certificateSubjects.get(vdsId);
                    if (subject != null) {
                        certificateForVms.put(vm.getId(), subject);
                    }
                }
            }
            if (!certificateForVms.isEmpty()) {
                queryReturnValue.setSucceeded(true);
                queryReturnValue.setReturnValue(certificateForVms);
            }
        }
    }
}
