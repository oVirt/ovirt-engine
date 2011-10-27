package org.ovirt.engine.core.bll;

import java.util.Collections;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public final class HighAvailableVmsDirector {
    public static void TryRunHighAvailableVmsOnVmDown(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDAO().getById(vmId);
        TryRunHighAvailableVm(vm.getvds_group_name());
    }

    public static void TryRunHighAvailableVdsUp(Guid vdsId) {
        VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
        TryRunHighAvailableVm(vds.getvds_group_name());
    }

    private static void TryRunHighAvailableVm(String vdsGroupName) {
        String searchStatement = String.format("Vms: status=down and cluster =%1$s", vdsGroupName);
        SearchParameters p = new SearchParameters(searchStatement, SearchType.VM);
        p.setMaxCount(Integer.MAX_VALUE);
        java.util.ArrayList<IVdcQueryable> vmsFromDb = (java.util.ArrayList<IVdcQueryable>) Backend.getInstance()
                .runInternalQuery(VdcQueryType.Search, p).getReturnValue();
        if (vmsFromDb != null && vmsFromDb.size() != 0) {
            java.util.ArrayList<VM> highlyAvailableVms = new java.util.ArrayList<VM>();
            for (IVdcQueryable vm : vmsFromDb) {
                VM currVm = (VM) ((vm instanceof VM) ? vm : null);
                if (currVm != null && currVm.getauto_startup() && currVm.getExitStatus().getValue() != 0) {
                    highlyAvailableVms.add(currVm);
                }
            }
            Collections.sort(highlyAvailableVms, Collections.reverseOrder(new VmsComparer()));
            for (VM vm : highlyAvailableVms) {
                if (!Backend.getInstance().runInternalAction(VdcActionType.RunVm, new RunVmParams(vm.getvmt_guid()))
                        .getSucceeded()) {
                    break;
                }

            }
        }
    }
}
