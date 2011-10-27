package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.compat.NotImplementedException;
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
}
