package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName") })
public class NetworkCommon<T extends AddNetworkStoragePoolParameters> extends StorageHandlingCommandBase<T> {
    public NetworkCommon(T parameters) {
        super(parameters);
        this.setStoragePoolId(getParameters().getNetwork().getstorage_pool_id());
    }

    public String getNetworkName() {
        return getParameters().getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        throw new NotImplementedException();
    }

    protected boolean validateVmNetwork() {
        boolean retVal = true;

        if (!getParameters().getNetwork().isVmNetwork()) {
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
        if (!getParameters().getNetwork().isVmNetwork() && getParameters().getNetwork().getstp()) {
            addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_CANNOT_SUPPORT_STP);
            stpIsAllowed = false;
        }
        return stpIsAllowed;
    }

    protected boolean validateMTUOverrideSupport() {
        boolean mtuSupported = true;

        if (getParameters().getNetwork().getMtu() != 0) {
            mtuSupported =
                    Config.<Boolean> GetValue(ConfigValues.MTUOverrideSupported,
                            getStoragePool().getcompatibility_version().getValue());
            if (!mtuSupported) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_MTU_OVERRIDE_NOT_SUPPORTED);
            }
        }
        return mtuSupported;
    }

    protected boolean validateVlanId(List<Network> networks) {
        if (getParameters().getNetwork().getvlan_id() != null) {
            if (!isVlanInRange(getParameters().getNetwork().getvlan_id())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_VLAN_OUT_OF_RANGE);
                return false;
            }

            else if (null != LinqUtils.firstOrNull(networks, new Predicate<Network>() {
                @Override
                public boolean eval(Network n) {
                    if (n.getvlan_id() != null) {
                        return n.getvlan_id().equals(getParameters().getNetwork().getvlan_id())
                                && n.getstorage_pool_id().equals(getParameters().getNetwork().getstorage_pool_id())
                                && !n.getId().equals(getParameters().getNetwork().getId());
                    }
                    return false;
                }
            })) {
                addCanDoActionMessage(String.format("$vlanId %d", getParameters().getNetwork().getvlan_id()));
                addCanDoActionMessage(VdcBllMessages.NETWORK_VLAN_IN_USE);
                return false;
            }
        }
        return true;
    }

    private boolean isVlanInRange(int vlanId) {
        return (vlanId >= 0 && vlanId <= 4095);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getParameters().getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
