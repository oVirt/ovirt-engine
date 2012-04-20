package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.QuotaHelper;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SetStoragePoolDescriptionVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.Pair;

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
        updateDefaultQuota();
        getStoragePoolDAO().updatePartial(getStoragePool());
        if (getStoragePool().getstatus() == StoragePoolStatus.Up
                && !StringHelper.EqOp(_oldStoragePool.getname(), getStoragePool().getname())) {
            getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.SetStoragePoolDescription,
                            new SetStoragePoolDescriptionVDSCommandParameters(getStoragePool().getId(),
                                    getStoragePool().getname()));
        }
        setSucceeded(true);
    }

    /**
     * If the storage pool enforcement type has been changed to disable, make sure there is a default quota for it.
     */
    private void updateDefaultQuota() {
        if (wasQuotaEnforcementDisabled()) {
            Pair<Quota, Boolean> defaultQuotaPair = getQutoaHelper().getUnlimitedQuota(getStoragePool(), true, true);
            Quota defaultQuota = defaultQuotaPair.getFirst();
            boolean isQuotaReused = defaultQuotaPair.getSecond();
            if (isQuotaReused) {
                log.debugFormat("Reusing quota {0} as the default quota for Storage Pool {1}",
                        defaultQuota.getId(),
                        defaultQuota.getStoragePoolId());
            } else {
                defaultQuota.setQuotaName(getQutoaHelper().getDefaultQuotaName(getStoragePool()));
            }
            getQutoaHelper().saveOrUpdateQuotaForUser(defaultQuota,
                    MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID,
                    isQuotaReused);
        }
    }

    /**
     * Checks whether part of the update was disabling quota enforcement on the Data Center
     */
    private boolean wasQuotaEnforcementDisabled() {
        return _oldStoragePool.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED
                && getStoragePool().getQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_STORAGE_POOL : AuditLogType.USER_UPDATE_STORAGE_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && checkStoragePool();
        _oldStoragePool = getStoragePoolDAO().get(getStoragePool().getId());
        if (returnValue && !StringHelper.EqOp(_oldStoragePool.getname(), getStoragePool().getname())
                && getStoragePoolDAO().getByName(getStoragePool().getname()) != null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        }
        if (returnValue
                && _oldStoragePool.getstorage_pool_type() != getStoragePool()
                        .getstorage_pool_type()
                && getStorageDomainStaticDAO().getAllForStoragePool(getStoragePool().getId()).size() > 0) {
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
            if (!isStoragePoolVersionSupported()) {
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
                List<VDSGroup> clusters = getVdsGroupDAO().getAllForStoragePool(getStoragePool().getId());
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

        StoragePoolValidator validator =
                new StoragePoolValidator(getStoragePool(), getReturnValue().getCanDoActionMessages());
        if (returnValue) {
            returnValue = validator.isNotLocalfsWithDefaultCluster();
        }
        if (returnValue) {
            returnValue = validator.isPosixDcAndMatchingCompatiblityVersion();
        }
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        return returnValue;
    }

    protected boolean isStoragePoolVersionSupported() {
        return VersionSupport.checkVersionSupported(getStoragePool().getcompatibility_version());
    }
    /* Getters for external resources and handlers */

    protected VDSBrokerFrontend getResourceManager() {
        return Backend.getInstance().getResourceManager();
    }

    protected QuotaHelper getQutoaHelper() {
        return QuotaHelper.getInstance();
    }

    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return DbFacade.getInstance().getStorageDomainStaticDAO();
    }

    /**
      * Overriding in order to make the method visible to tests in the current package.
      * @see org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase#getVdsGroupDAO()
      */
    @Override
    protected VdsGroupDAO getVdsGroupDAO() {
        return super.getVdsGroupDAO();
    }
}
