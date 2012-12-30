package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields({ @CustomLogField("NetworkName") })
public abstract class NetworkCommon<T extends AddNetworkStoragePoolParameters> extends CommandBase<T> {
    public NetworkCommon(T parameters) {
        super(parameters);
        this.setStoragePoolId(getNetwork().getstorage_pool_id());
    }

    protected Network getNetwork() {
        return getParameters().getNetwork();
    }

    public String getNetworkName() {
        return getNetwork().getname();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);
    }

    protected boolean validateVmNetwork() {
        boolean retVal = true;

        if (!getNetwork().isVmNetwork()) {
            Version version = getStoragePool().getcompatibility_version();
            retVal = Config.<Boolean> GetValue(ConfigValues.NonVmNetworkSupported, version.getValue());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
            }
        }

        return retVal;
    }

    protected boolean validateStpProperty() {
        boolean stpIsAllowed = true;
        if (!getNetwork().isVmNetwork() && getNetwork().getstp()) {
            addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP);
            stpIsAllowed = false;
        }
        return stpIsAllowed;
    }

    protected boolean validateMTUOverrideSupport() {
        boolean mtuSupported = true;

        if (getNetwork().getMtu() != 0) {
            mtuSupported =
                    Config.<Boolean> GetValue(ConfigValues.MTUOverrideSupported,
                            getStoragePool().getcompatibility_version().getValue());
            if (!mtuSupported) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED);
            }
        }
        return mtuSupported;
    }

    protected boolean vlanIsFree(List<Network> networks) {
        if (getNetwork().getvlan_id() != null) {
            for (Network network : networks) {
                if (network.getvlan_id() != null
                        && network.getvlan_id().equals(getNetwork().getvlan_id())
                        && network.getstorage_pool_id().equals(getNetwork().getstorage_pool_id())
                        && !network.getId().equals(getNetwork().getId())) {
                    addCanDoActionMessage(String.format("$vlanId %d", getNetwork().getvlan_id()));
                    addCanDoActionMessage(VdcBllMessages.NETWORK_VLAN_IN_USE);
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean networkNotAttachedToCluster(final Network network) {
        if (!getNetworkClusterDAO().getAllForNetwork(network.getId()).isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_NETWORK_IN_USE);
            return false;
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
