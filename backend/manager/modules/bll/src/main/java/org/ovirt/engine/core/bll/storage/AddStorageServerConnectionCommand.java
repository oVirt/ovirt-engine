package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.NfsMountPointConstraint;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SuppressWarnings("serial")
@LockIdNameAttribute(isReleaseAtEndOfExecute = true)
@InternalCommandAttribute
public class AddStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends
        ConnectStorageToVdsCommand<T> {
    public AddStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        StorageServerConnections currConnection = getConnection();
        boolean isValidConnection = true;
        isValidConnection = connect(getVds().getId()).getFirst();

        // Add storage Connection to the database.
        if (isValidConnection && (StringUtils.isNotEmpty(currConnection.getid())
                || getDbFacade().getStorageServerConnectionDao().get(currConnection.getid()) == null)) {
            currConnection.setid(Guid.NewGuid().toString());
            getDbFacade().getStorageServerConnectionDao().save(currConnection);
        }
        getReturnValue().setActionReturnValue(getConnection().getid());
        setSucceeded(true);
    }

    @Override
    protected StorageServerConnections getConnection() {
        if (StringUtils.isEmpty(getParameters().getStorageServerConnection().getid())) {
            List<StorageServerConnections> connections;
            if ((connections = DbFacade.getInstance().getStorageServerConnectionDao().getAllForStorage(
                    getParameters().getStorageServerConnection().getconnection())).size() != 0) {
                getParameters().setStorageServerConnection(connections.get(0));
            }
        }
        return (getParameters()).getStorageServerConnection();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        StorageServerConnections paramConnection = getParameters().getStorageServerConnection();
        if (paramConnection.getstorage_type() == StorageType.NFS
                && !new NfsMountPointConstraint().isValid(paramConnection.getconnection(), null)) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.VALIDATION_STORAGE_CONNECTION_INVALID);
        }

        if (getParameters().getVdsId().equals(Guid.Empty)) {
            returnValue = InitializeVds();
        } else if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            returnValue = false;
        } else if (getVds().getStatus() != VDSStatus.Up) {
            addCanDoActionMessage(VdcBllMessages.VDS_ADD_STORAGE_SERVER_STATUS_MUST_BE_UP);
            returnValue = false;
        }

        if (returnValue) {
            if (paramConnection.getstorage_type() == StorageType.LOCALFS) {
                storage_pool storagePool = DbFacade.getInstance().getStoragePoolDao().getForVds(getVds().getId());
                if (storagePool == null || storagePool.getstorage_pool_type() != StorageType.LOCALFS) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_ADD_LOCAL_STORAGE_TO_NON_LOCAL_HOST);
                }
            }
        }
        return returnValue;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        // lock the path to NFS to avoid at the same time if some other user tries to:
        // add new storage domain to same path or edit another storage server connection to point to same path
        return Collections.singletonMap(getParameters().getStorageServerConnection().getconnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
