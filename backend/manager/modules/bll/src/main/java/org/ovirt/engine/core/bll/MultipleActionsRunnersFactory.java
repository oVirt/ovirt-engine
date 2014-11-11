package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
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
    public static MultipleActionsRunner createMultipleActionsRunner(VdcActionType actionType,
                                                                    ArrayList<VdcActionParametersBase> parameters,
                                                                    boolean isInternal, CommandContext commandContext) {
        MultipleActionsRunner runner;
        switch (actionType) {
        case DeactivateStorageDomainWithOvfUpdate: {
            runner = new DeactivateStorageDomainsMultipleActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        }
        case AttachStorageDomainToPool: {
            runner = new AttachStorageDomainsMultipleActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        }

        case RunVm: {
            runner = new RunVMActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        }
        case MigrateVm: {
            runner = new MigrateVMActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        }
        case RemoveVmFromPool: {
            runner = new RemoveVmFromPoolRunner(actionType, parameters, commandContext, isInternal);
            break;
        }

        case StartGlusterVolume:
        case StopGlusterVolume:
        case DeleteGlusterVolume:
        case SetGlusterVolumeOption:
        case ResetGlusterVolumeOptions:
        case AddVds: // AddVds is called with multiple actions *only* in case of gluster clusters
        case RemoveGlusterServer:
        case EnableGlusterHook:
        case DisableGlusterHook: {
            runner = new GlusterMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        }

        case RemoveVds: {
            if (containsGlusterServer(parameters)) {
                runner = new GlusterMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            } else {
                runner = new MultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            }

            break;
        }

        case PersistentSetupNetworks: {
            runner = new ParallelMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        }

        case AttachNetworkToVdsGroup:
        case DetachNetworkToVdsGroup:
        case UpdateNetworkOnCluster:
            throw new UnsupportedOperationException("Multiple network attachments/detachments/updates should be run through ManageNetworkClustersCommand!");

        default:
            runner = new MultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        }
        return runner;
    }

    private static boolean containsGlusterServer(ArrayList<VdcActionParametersBase> parameters) {
        Set<Guid> processed = new HashSet<Guid>();
        for (VdcActionParametersBase param : parameters) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(((RemoveVdsParameters) param).getVdsId());
            if (vds != null && !processed.contains(vds.getVdsGroupId())) {
                VDSGroup cluster = DbFacade.getInstance().getVdsGroupDao().get(vds.getVdsGroupId());
                if (cluster.supportsGlusterService()) {
                    return true;
                }
                processed.add(vds.getVdsGroupId());
            }
        }

        return false;
    }
}
