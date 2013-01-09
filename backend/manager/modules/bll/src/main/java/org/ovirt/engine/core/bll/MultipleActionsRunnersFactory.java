package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.storage.AttachStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.bll.storage.DeactivateStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public final class MultipleActionsRunnersFactory {
    public static MultipleActionsRunner CreateMultipleActionsRunner(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal) {
        MultipleActionsRunner runner;
        switch (actionType) {
        case DeactivateStorageDomain: {
            runner = new DeactivateStorageDomainsMultipleActionRunner(actionType, parameters, isInternal);
            break;
        }
        case AttachStorageDomainToPool: {
            runner = new AttachStorageDomainsMultipleActionRunner(actionType, parameters, isInternal);
            break;
        }

        case RunVm: {
            runner = new RunVMActionRunner(actionType, parameters, isInternal);
            break;
        }
        case MigrateVm: {
            runner = new MigrateVMActionRunner(actionType, parameters, isInternal);
            break;
        }
        case RemoveVmFromPool: {
            runner = new RemoveVmFromPoolRunner(actionType, parameters, isInternal);
            break;
        }

        case StartGlusterVolume:
        case StopGlusterVolume:
        case DeleteGlusterVolume:
        case SetGlusterVolumeOption:
        case ResetGlusterVolumeOptions:
        case AddVds: // AddVds is called with multiple actions *only* in case of gluster clusters
        case RemoveGlusterServer: {
            runner = new GlusterMultipleActionsRunner(actionType, parameters, isInternal);
            break;
        }

        case RemoveVds: {
            if (containsGlusterServer(parameters)) {
                runner = new GlusterMultipleActionsRunner(actionType, parameters, isInternal);
            } else {
                runner = new MultipleActionsRunner(actionType, parameters, isInternal);
            }

            break;
        }

        default: {
            runner = new MultipleActionsRunner(actionType, parameters, isInternal);
            break;
        }
        }
        return runner;
    }

    private static boolean containsGlusterServer(ArrayList<VdcActionParametersBase> parameters) {
        Set<Guid> processed = new HashSet<Guid>();
        for (VdcActionParametersBase param : parameters) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(((RemoveVdsParameters) param).getVdsId());
            if (vds != null && !processed.contains(vds.getvds_group_id())) {
                VDSGroup cluster = DbFacade.getInstance().getVdsGroupDao().get(vds.getvds_group_id());
                if (cluster.supportsGlusterService()) {
                    return true;
                }
                processed.add(vds.getvds_group_id());
            }
        }

        return false;
    }
}
