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
            Version version = getStoragePoolDAO().get(getParameters().getStoragePoolId()).getcompatibility_version();
            retVal = Config.<Boolean> GetValue(ConfigValues.NonVmNetworkSupported, version.getValue());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.NON_VM_NETWORK_NOT_SUPPORTED_FOR_POOL_LEVEL);
            }
        }

        return retVal;
    }
}
