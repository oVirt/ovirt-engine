package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class VmPoolHandler {

    /**
     * VM should be return to pool after it stopped unless Manual Return VM To Pool chosen.
     *
     * @param vmId
     *            The VM's id.
     */
    public static void ProcessVmPoolOnStopVm(Guid vmId) {
        vm_pool_map map = DbFacade.getInstance().getVmPoolDAO().getVmPoolMapByVmGuid(vmId);
        if (map != null) {
            vm_pools pool = DbFacade.getInstance().getVmPoolDAO().get(map.getvm_pool_id());
            if (pool != null && pool.getvm_pool_type() == VmPoolType.Automatic) {
                List<DbUser> users = DbFacade.getInstance().getDbUserDAO()
                        .getAllForVm(vmId);
                // shuld be only one user in the collection
                for (DbUser dbUser : users) {
                    Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromVmFromPool,
                            new VmPoolSimpleUserParameters(map.getvm_pool_id(), dbUser.getuser_id()));
                }
            }
        }

        removeVmStatelessImages(vmId);
    }

    public static void removeVmStatelessImages(Guid vmId) {
        if (DbFacade.getInstance().getDiskImageDAO().getAllStatelessVmImageMapsForVm(vmId).size() > 0) {
            log.infoFormat("VdcBll.VmPoolHandler.ProcessVmPoolOnStopVm - Deleting snapshot for stateless vm {0}", vmId);
            Backend.getInstance().runInternalAction(VdcActionType.RestoreStatelessVm,
                    new VmOperationParameterBase(vmId));
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(VmPoolHandler.class);
}
