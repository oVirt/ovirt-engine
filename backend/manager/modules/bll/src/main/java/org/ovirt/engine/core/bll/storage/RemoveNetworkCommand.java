package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class RemoveNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public RemoveNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getNetworkDAO().remove(getParameters().getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);

        boolean retVal = true;
        if (retVal) {
            retVal = CommonNetworkValidation(getParameters().getNetwork(), getReturnValue().getCanDoActionMessages());
        }
        return retVal;
    }

    public static boolean CommonNetworkValidation(final Network network, java.util.ArrayList<String> canDoActionMessages) {
        // check that network is not in use by cluster
        if (network.getstorage_pool_id() != null) {
            List<VDSGroup> groups = DbFacade.getInstance().getVdsGroupDAO().getAllForStoragePool(
                    network.getstorage_pool_id().getValue());
            for (VDSGroup cluster : groups) {
                List<Network> networks = DbFacade.getInstance().getNetworkDAO()
                        .getAllForCluster(cluster.getId());

                if (null != LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                    @Override
                    public boolean eval(Network n) {
                        return n.getname().equals(network.getname());
                    }
                })) {
                    canDoActionMessages.add(VdcBllMessages.NETWORK_CLUSTER_NETWORK_IN_USE.toString());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_NETWORK : AuditLogType.NETWORK_REMOVE_NETWORK_FAILED;
    }
}
