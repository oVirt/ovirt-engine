package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.network.NetworkProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolMap;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VmPoolHandler {

    /**
     * VM should be return to pool after it stopped unless Manual Return VM To Pool chosen.
     *
     * @param vmId
     *            The VM's id.
     * @FIXME BLL commands should invoke IVDSEventListener.processOnVmStop instead of directly calling this class. This
     *        is not duable now since callers which aren't on BLL don't know CommandContext to avid bugs this method
     *        must be treated as the real implementor of VdsEventListener.processOnVmStop meanwhile till a better
     *        solution supplied
     */
    public static void ProcessVmPoolOnStopVm(Guid vmId, CommandContext context) {
        VmPoolMap map = DbFacade.getInstance().getVmPoolDao().getVmPoolMapByVmGuid(vmId);
        List<DbUser> users = DbFacade.getInstance().getDbUserDao().getAllForVm(vmId);
        // Check if this is a Vm from a Vm pool, and is attached to a user
        if (map != null && users != null && !users.isEmpty()) {
            VmPool pool = DbFacade.getInstance().getVmPoolDao().get(map.getvm_pool_id());
            if (pool != null && pool.getVmPoolType() == VmPoolType.Automatic) {
                // should be only one user in the collection
                for (DbUser dbUser : users) {
                    Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromVmFromPool,
                            new VmPoolSimpleUserParameters(map.getvm_pool_id(), dbUser.getuser_id(), vmId), context);
                }
            }
        } else {
            // If we are dealing with a prestarted Vm or a regular Vm - clean stateless images
            // Otherwise this was already done in DetachUserFromVmFromPoolCommand
            removeVmStatelessImages(vmId, context);
        }

        QuotaManager.getInstance().rollbackQuotaByVmId(vmId);
        VmHandler.removeStatelessVmUnmanagedDevices(vmId);
        handleProviderNetworks(vmId);
    }

    private static void handleProviderNetworks(Guid vmId) {
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao().getAllForVm(vmId);
        VmStatic vm = DbFacade.getInstance().getVmStaticDao().get(vmId);
        Map<String, Network> clusterNetworks =
                Entities.entitiesByName(DbFacade.getInstance().getNetworkDao().getAllForCluster(vm.getVdsGroupId()));

        for (VmNetworkInterface iface : interfaces) {
            String networkName = iface.getNetworkName();

            if (networkName != null
                    && clusterNetworks.get(networkName).getProvidedBy() != null) {
                NetworkProviderProxy providerProxy = ProviderProxyFactory.getInstance().create(
                        DbFacade.getInstance().getProviderDao().get(
                                clusterNetworks.get(networkName).getProvidedBy().getProviderId()));
                providerProxy.deallocate(iface);
            }
        }
    }

    public static void removeVmStatelessImages(Guid vmId, CommandContext context) {
        if (DbFacade.getInstance().getSnapshotDao().exists(vmId, SnapshotType.STATELESS)) {
            log.infoFormat("VdcBll.VmPoolHandler.ProcessVmPoolOnStopVm - Deleting snapshot for stateless vm {0}", vmId);
            Backend.getInstance().runInternalAction(VdcActionType.RestoreStatelessVm,
                    new VmOperationParameterBase(vmId),
                    context);
        }
    }

    private static Log log = LogFactory.getLog(VmPoolHandler.class);
}
