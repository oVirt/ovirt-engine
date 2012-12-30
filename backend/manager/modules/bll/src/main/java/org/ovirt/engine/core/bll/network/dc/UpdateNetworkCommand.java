package org.ovirt.engine.core.bll.network.dc;

import java.util.List;

import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@SuppressWarnings("serial")
public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    private List<VDSGroup> clusters;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getNetworkDao().update(getNetwork());

        for (VDSGroup cluster : clusters) {
            NetworkClusterHelper.setStatus(cluster.getId(), getNetwork());
        }
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
    }

    @Override
    protected boolean canDoAction() {
        List<Network> networks = DbFacade.getInstance().getNetworkDao().getAll();

        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }

        if (!validate(vmNetworkSetCorrectly())) {
            return false;
        }

        if (!validate(stpForVmNetworkOnly())) {
            return false;
        }

        if (!validate(mtuValid())) {
            return false;
        }

        // check that network name not start with 'bond'
        if (getNetworkName().toLowerCase().startsWith("bond")) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME);
            return false;
        }

        if (!validate(vlanIsFree(networks))) {
            return false;
        }

        // check that network not exsits
        Network oldNetwork = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network n) {
                return n.getId().equals(getNetwork().getId());
            }
        });
        if (oldNetwork == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NOT_EXISTS);
            return false;
        }

        // check defalut network name is not renamed
        String defaultNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        if (oldNetwork.getname().equals(defaultNetwork) &&
                !getNetworkName().equals(defaultNetwork)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK);
            return false;
        }

        Network net = LinqUtils.firstOrNull(networks, new Predicate<Network>() {
            @Override
            public boolean eval(Network n) {
                return n.getname().trim().toLowerCase()
                        .equals(getNetworkName().trim().toLowerCase())
                        && !n.getId().equals(getNetwork().getId())
                        && getNetwork().getstorage_pool_id().equals(n.getstorage_pool_id());
            }
        });
        if (net != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_IN_USE);
            return false;
        }

        // check if the network in use with running vm
        clusters = DbFacade.getInstance().getVdsGroupDao().getAllForStoragePool(getStoragePool().getId());
        for (VDSGroup cluster : clusters) {
            List<VmStatic> vms = DbFacade.getInstance().getVmStaticDao().getAllByGroupAndNetworkName(cluster.getId(),
                    getNetworkName());
            if (vms.size() > 0) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
                return false;
            }
        }

        return validate(networkNotAttachedToCluster(oldNetwork));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_NETWORK : AuditLogType.NETWORK_UPDATE_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
