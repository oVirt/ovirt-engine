package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmsComparer;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public final class HighAvailableVmsDirector {
    public static void TryRunHighAvailableVmsOnVmDown(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        TryRunHighAvailableVm(vm.getVdsGroupName());
    }

    public static void TryRunHighAvailableVdsUp(Guid vdsId) {
        VDS vds = DbFacade.getInstance().getVdsDao().get(vdsId);
        TryRunHighAvailableVm(vds.getvds_group_name());
    }

    private static void TryRunHighAvailableVm(String vdsGroupName) {
        String searchStatement = String.format("Vms: status=down and cluster =%1$s", vdsGroupName);
        SearchParameters p = new SearchParameters(searchStatement, SearchType.VM);
        p.setMaxCount(Integer.MAX_VALUE);
        ArrayList<IVdcQueryable> vmsFromDb = (ArrayList<IVdcQueryable>) Backend.getInstance()
                .runInternalQuery(VdcQueryType.Search, p).getReturnValue();
        if (vmsFromDb != null && vmsFromDb.size() != 0) {
            ArrayList<VM> highlyAvailableVms = new ArrayList<VM>();
            for (IVdcQueryable vm : vmsFromDb) {
                VM currVm = (VM) ((vm instanceof VM) ? vm : null);
                if (currVm != null && currVm.isAutoStartup() && currVm.getExitStatus().getValue() != 0) {
                    highlyAvailableVms.add(currVm);
                }
            }
            Collections.sort(highlyAvailableVms, Collections.reverseOrder(new VmsComparer()));
            for (VM vm : highlyAvailableVms) {
                if (!Backend.getInstance().runInternalAction(VdcActionType.RunVm, new RunVmParams(vm.getVmtGuid()))
                        .getSucceeded()) {
                    break;
                }

            }
        }
    }
}
