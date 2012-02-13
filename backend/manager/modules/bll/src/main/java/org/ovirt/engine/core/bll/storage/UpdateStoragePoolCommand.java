package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.SetStoragePoolDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T> {
    public UpdateStoragePoolCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected UpdateStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    private storage_pool _oldStoragePool;

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getStoragePoolDAO().updatePartial(getStoragePool());
        if (getStoragePool().getstatus() == StoragePoolStatus.Up
                && !StringHelper.EqOp(_oldStoragePool.getname(), getStoragePool().getname())) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.SetStoragePoolDescription,
                            new SetStoragePoolDescriptionVDSCommandParameters(getStoragePool().getId(),
                                    getStoragePool().getname()));
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_POOL : AuditLogType.USER_UPDATE_STORAGE_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && CheckStoragePool();
        _oldStoragePool = DbFacade.getInstance().getStoragePoolDAO().get(getStoragePool().getId());
        if (returnValue && !StringHelper.EqOp(_oldStoragePool.getname(), getStoragePool().getname())
                && DbFacade.getInstance().getStoragePoolDAO().getByName(getStoragePool().getname()) != null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        }
        if (returnValue
                && _oldStoragePool.getstorage_pool_type() != getStoragePool()
                        .getstorage_pool_type()
                && DbFacade.getInstance()
                        .getStorageDomainStaticDAO()
                        .getAllForStoragePool(getStoragePool().getId())
                        .size() > 0) {
            returnValue = false;
            getReturnValue()
                    .getCanDoActionMessages()
                    .add(VdcBllMessages.ERROR_CANNOT_CHANGE_STORAGE_POOL_TYPE_WITH_DOMAINS
                            .toString());
        }
        returnValue = returnValue && CheckStoragePoolNameLengthValid();
        if (returnValue
                && Version.OpInequality(_oldStoragePool.getcompatibility_version(), getStoragePool()
                        .getcompatibility_version())) {
            if (!VersionSupport.checkVersionSupported(getStoragePool().getcompatibility_version())) {
                addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
                returnValue = false;
            }
            // decreasing of compatibility version is not allowed
            else if (getStoragePool().getcompatibility_version().compareTo(_oldStoragePool.getcompatibility_version()) < 0) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_DECREASE_COMPATIBILITY_VERSION);
            } else {
                // check all clusters has at least the same compatibility
                // version
                List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDAO().getAllForStoragePool(
                        getStoragePool().getId());
                for (VDSGroup cluster : clusters) {
                    if (getStoragePool().getcompatibility_version().compareTo(cluster.getcompatibility_version()) > 0) {
                        returnValue = false;
                        getReturnValue()
                                .getCanDoActionMessages()
                                .add(VdcBllMessages.ERROR_CANNOT_UPDATE_STORAGE_POOL_COMPATIBILITY_VERSION_BIGGER_THAN_CLUSTERS
                                        .toString());
                        break;
                    }
                }
            }
        }

        returnValue = returnValue && isNotLocalfsWithDefaultCluster();

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        return returnValue;
    }

    private boolean isNotLocalfsWithDefaultCluster() {
        if(getStoragePool().getstorage_pool_type() == StorageType.LOCALFS && containsDefaultCluster()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS);
            return false;
        }
        return true;
    }

    private boolean containsDefaultCluster() {
        List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDAO().getAllForStoragePool(getStoragePool().getId());
        boolean hasDefaultCluster = false;
        for(VDSGroup cluster : clusters) {
            if(cluster.getId().equals(VDSGroup.DEFAULT_VDS_GROUP_ID)) {
                hasDefaultCluster = true;
                break;
            }
        }
        return hasDefaultCluster;
    }
}
