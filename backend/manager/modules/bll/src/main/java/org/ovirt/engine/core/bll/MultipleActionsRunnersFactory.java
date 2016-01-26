package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.AttachStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.bll.storage.domain.DeactivateStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.common.action.RemoveVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;

public final class MultipleActionsRunnersFactory {
    public static MultipleActionsRunner createMultipleActionsRunner(VdcActionType actionType,
                                                                    ArrayList<VdcActionParametersBase> parameters,
                                                                    boolean isInternal, CommandContext commandContext) {
        MultipleActionsRunner runner;
        switch (actionType) {
        case DeactivateStorageDomainWithOvfUpdate:
            runner = new DeactivateStorageDomainsMultipleActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        case AttachStorageDomainToPool:
            runner = new AttachStorageDomainsMultipleActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        case RunVm:
            runner = new RunVMActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        case MigrateVm:
            runner = new MigrateVMActionRunner(actionType, parameters, commandContext, isInternal);
            break;
        case RemoveVmFromPool:
            runner = new RemoveVmFromPoolRunner(actionType, parameters, commandContext, isInternal);
            break;
        case StartGlusterVolume:
        case StopGlusterVolume:
        case DeleteGlusterVolume:
        case SetGlusterVolumeOption:
        case ResetGlusterVolumeOptions:
        case AddVds: // AddVds is called with multiple actions *only* in case of gluster clusters
        case RemoveGlusterServer:
        case EnableGlusterHook:
        case DisableGlusterHook:
        case DeleteGlusterVolumeSnapshot:
            runner = new GlusterMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        case RemoveVds:
            if (containsGlusterServer(parameters)) {
                runner = new GlusterMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            } else {
                runner = new PrevalidatingMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            }

            break;
        case PersistentHostSetupNetworks:
            runner = new ParallelMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        case AttachNetworkToCluster:
        case DetachNetworkToCluster:
        case UpdateNetworkOnCluster:
            throw new UnsupportedOperationException("Multiple network attachments/detachments/updates should be run through ManageNetworkClustersCommand!");

        case AddNetworkAttachment:
        case UpdateNetworkAttachment:
        case RemoveNetworkAttachment:
            throw new UnsupportedOperationException("AddNetworkAttachment, UpdateNetworkAttachment, and RemoveNetworkAttachment cannot be run using MultipleActionsRunner");
        case RemoveDiskProfile:
        case RemoveCpuProfile:
            runner = new SequentialMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        default:
            runner = new PrevalidatingMultipleActionsRunner(actionType, parameters, commandContext, isInternal);
            break;
        }
        return Injector.injectMembers(runner);
    }

    private static boolean containsGlusterServer(ArrayList<VdcActionParametersBase> parameters) {
        Set<Guid> processed = new HashSet<>();
        for (VdcActionParametersBase param : parameters) {
            VDS vds = DbFacade.getInstance().getVdsDao().get(((RemoveVdsParameters) param).getVdsId());
            if (vds != null && !processed.contains(vds.getClusterId())) {
                Cluster cluster = DbFacade.getInstance().getClusterDao().get(vds.getClusterId());
                if (cluster.supportsGlusterService()) {
                    return true;
                }
                processed.add(vds.getClusterId());
            }
        }

        return false;
    }
}
