package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.AttachNetworkToVdsGroupCommand;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class UpdateNetworkCommand<T extends AddNetworkStoragePoolParameters> extends StorageHandlingCommandBase<T> {
    private List<VDSGroup> _clusters;

    public UpdateNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getNetworkDAO().update(getParameters().getNetwork());

        for (VDSGroup cluster : _clusters) {
            AttachNetworkToVdsGroupCommand.SetNetworkStatus(cluster.getID(), getParameters().getNetwork());
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        List<network> networks = DbFacade.getInstance().getNetworkDAO().getAll();

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);

        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }

        // check that network name not start with 'bond'
        if (getParameters().getNetwork().getname().toLowerCase().startsWith("bond")) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME);
            return false;
        }

        // check vlan is valid
        if (getParameters().getNetwork().getvlan_id() != null) {
            if (!AddNetworkCommand.IsVlanInRange(getParameters().getNetwork().getvlan_id())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_VLAN_OUT_OF_RANGE);
                return false;
            }
            // else if (false) //LINQ networks.FirstOrDefault(n => n.vlan_id ==
            // AddNetworkParameters.Network.vlan_id.Value
            // //LINQ && n.storage_pool_id ==
            // AddNetworkParameters.Network.storage_pool_id
            // //LINQ && n.id != AddNetworkParameters.Network.id) != null)
            else if (null != LinqUtils.firstOrNull(networks, new Predicate<network>() {
                @Override
                public boolean eval(network n) {
                    if (n.getvlan_id() != null) {
                        return n.getvlan_id().equals(getParameters().getNetwork().getvlan_id())
                                && n.getstorage_pool_id().equals(getParameters().getNetwork().getstorage_pool_id())
                                && !n.getId().equals(getParameters().getNetwork().getId());
                    }
                    return false;
                }
            })) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_VLAN_IN_USE);
                return false;
            }
        }

        // check that network not exsits
        // network oldNetwork = null; //LINQ networks.FirstOrDefault(n => n.id
        // == AddNetworkParameters.Network.id);
        network oldNetwork = LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network n) {
                return n.getId().equals(getParameters().getNetwork().getId());
            }
        });
        if (oldNetwork == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_NOT_EXISTS);
            return false;
        }

        // check defalut network name is not renamed
        String defaultNetwork = Config.<String> GetValue(ConfigValues.ManagementNetwork);
        if (oldNetwork.getname().equals(defaultNetwork) &&
                !getParameters().getNetwork().getname().equals(defaultNetwork)) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CAN_NOT_REMOVE_DEFAULT_NETWORK);
            return false;
        }

        // network net = null; //LINQ networks.FirstOrDefault(n =>
        // n.name.Trim().ToLower() ==
        // AddNetworkParameters.Network.name.Trim().ToLower()
        // LINQ && n.id != AddNetworkParameters.Network.id &&
        // AddNetworkParameters.Network.storage_pool_id == n.storage_pool_id);
        network net = LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network n) {
                return n.getname().trim().toLowerCase()
                        .equals(getParameters().getNetwork().getname().trim().toLowerCase())
                        && !n.getId().equals(getParameters().getNetwork().getId())
                        && getParameters().getNetwork().getstorage_pool_id().equals(n.getstorage_pool_id());
            }
        });
        if (net != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_IN_USE);
            return false;
        }

        // check if the network in use with running vm
        _clusters = DbFacade.getInstance().getVdsGroupDAO().getAllForStoragePool(getStoragePool().getId());
        for (VDSGroup cluster : _clusters) {
            List<VmStatic> vms = DbFacade.getInstance().getVmStaticDAO().getAllByGroupAndNetworkName(cluster.getID(),
                    getParameters().getNetwork().getname());
            if (vms.size() > 0) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VM);
                return false;
            }
        }

        return RemoveNetworkCommand.CommonNetworkValidation(oldNetwork, getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }
}
