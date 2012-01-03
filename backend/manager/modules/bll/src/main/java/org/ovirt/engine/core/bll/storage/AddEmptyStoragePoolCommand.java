package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.AddVdsGroupCommand;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddEmptyStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T> {
    public AddEmptyStoragePoolCommand(T parameters) {
        super(parameters);
    }

    protected void AddStoragePoolToDb() {
        getStoragePool().setId(Guid.NewGuid());
        getStoragePool().setstatus(StoragePoolStatus.Uninitialized);
        DbFacade.getInstance().getStoragePoolDAO().save(getStoragePool());
    }

    @Override
    protected void executeCommand() {
        AddStoragePoolToDb();
        getReturnValue().setActionReturnValue(getStoragePool().getId());
        AddDefaultNetworks();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_POOL : AuditLogType.USER_ADD_STORAGE_POOL_FAILED;
    }

    private void AddDefaultNetworks() {
        network net = new network();
        net.setId(Guid.NewGuid());
        net.setname(Config.<String> GetValue(ConfigValues.ManagementNetwork));
        net.setdescription(AddVdsGroupCommand.DefaultNetworkDescription);
        net.setstorage_pool_id(getStoragePool().getId());
        DbFacade.getInstance().getNetworkDAO().save(net);
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        boolean result = super.canDoAction();
        if (result && DbFacade.getInstance().getStoragePoolDAO().getByName(getStoragePool().getname()) != null) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        } else if (!CheckStoragePoolNameLengthValid()) {
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getStoragePool().getcompatibility_version()
        )) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        } else if (getStoragePool().getstorage_pool_type() == StorageType.LOCALFS
                && !Config.<Boolean> GetValue(ConfigValues.LocalStorageEnabled, getStoragePool()
                        .getcompatibility_version().toString())) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.DATA_CENTER_LOCAL_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return result;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID, VdcObjectType.System);
    }
}
