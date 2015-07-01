package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPUPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CPUPolicyUnit.class);

    public CPUPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        List<VDS> list = new ArrayList<>();
        for (VDS vds : hosts) {
            VDSGroup cluster = getVdsGroupDao().get(vds.getVdsGroupId());
            Integer cores = SlaValidator.getEffectiveCpuCores(vds,
                    cluster != null && cluster.getCountThreadsAsCores());
            if (cores != null && vm.getNumOfCpus() > cores) {
                messages.addMessage(vds.getId(), VdcBllMessages.VAR__DETAIL__NOT_ENOUGH_CORES.toString());
                log.debug("Host '{}' has less cores ({}) than vm cores ({})",
                        vds.getName(),
                        cores,
                        vm.getNumOfCpus());
                continue;
            }
            list.add(vds);
        }
        return list;
    }

    protected VdsGroupDao getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }
}
