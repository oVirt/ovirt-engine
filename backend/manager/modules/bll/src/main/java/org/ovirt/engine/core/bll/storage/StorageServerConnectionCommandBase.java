package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageServerConnectionCommandBase<T extends StorageServerConnectionParametersBase> extends
        StorageHandlingCommandBase<T> {
    public StorageServerConnectionCommandBase(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    protected storage_server_connections getConnection() {
        return getParameters().getStorageServerConnection();
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID, VdcObjectType.System);
    }
}
