package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public RemoveNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getNetworkDAO().remove(getParameters().getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);

        return CommonNetworkValidation(getParameters().getNetwork(), getReturnValue().getCanDoActionMessages());
    }

    public static boolean CommonNetworkValidation(final Network network, java.util.ArrayList<String> canDoActionMessages) {
        // check that network is not in use by cluster
        if (network.getstorage_pool_id() != null) {
            List<VDSGroup> groups = DbFacade.getInstance().getVdsGroupDao().getAllForStoragePool(
                    network.getstorage_pool_id().getValue());
            for (VDSGroup cluster : groups) {
                Network removedNetwork =
                        DbFacade.getInstance().getNetworkDao().getByNameAndCluster(network.getName(), cluster.getId());

                if (removedNetwork != null) {
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
