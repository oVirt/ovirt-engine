package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

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
}
