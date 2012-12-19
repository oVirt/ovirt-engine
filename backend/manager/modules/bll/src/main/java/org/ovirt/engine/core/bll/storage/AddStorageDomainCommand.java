package org.ovirt.engine.core.bll.storage;

import static org.ovirt.engine.core.bll.MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class AddStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T> {
    public AddStorageDomainCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected AddStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    protected void InitializeStorageDomain() {
        getStorageDomain().setId(Guid.NewGuid());
    }

    protected boolean AddStorageDomainInIrs() {
        // No need to run in separate transaction - counting on rollback of external transaction wrapping the command
        return Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateStorageDomain,
                        new CreateStorageDomainVDSCommandParameters(getVds().getId(), getStorageDomain()
                                .getStorageStaticData(), getStorageArgs())).getSucceeded();
    }

    protected void AddStorageDomainInDb() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getStorageDomainStaticDao().save(getStorageDomain().getStorageStaticData());
                getCompensationContext().snapshotNewEntity(getStorageDomain().getStorageStaticData());
                StorageDomainDynamic newStorageDynamic =
                        new StorageDomainDynamic(null, getStorageDomain().getId(), null);
                getReturnValue().setActionReturnValue(getStorageDomain().getId());
                DbFacade.getInstance().getStorageDomainDynamicDao().save(newStorageDynamic);
                getCompensationContext().snapshotNewEntity(newStorageDynamic);
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    protected void UpdateStorageDomainDynamicFromIrs() {
        final storage_domains sd =
                (storage_domains) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.GetStorageDomainStats,
                                new GetStorageDomainStatsVDSCommandParameters(getVds().getId(),
                                        getStorageDomain().getId()))
                        .getReturnValue();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(getStorageDomain().getStorageDynamicData());
                DbFacade.getInstance().getStorageDomainDynamicDao().update(sd.getStorageDynamicData());
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    @Override
    protected void executeCommand() {
        InitializeStorageDomain();
        AddStorageDomainInDb();
        if (AddStorageDomainInIrs()) {
            UpdateStorageDomainDynamicFromIrs();
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_DOMAIN : AuditLogType.USER_ADD_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        boolean returnValue = super.canDoAction() && InitializeVds() && checkStorageDomainNameLengthValid();
        if (returnValue && IsStorageWithSameNameExists()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
            returnValue = false;
        }
        if (returnValue && getStorageDomain().getstorage_domain_type() == StorageDomainType.ISO
                && getStorageDomain().getstorage_type() != StorageType.NFS) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            returnValue = false;
        }
        if (returnValue && getStorageDomain().getstorage_domain_type() == StorageDomainType.ImportExport
                && getStorageDomain().getstorage_type() == StorageType.LOCALFS) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            returnValue = false;
        }
        if (returnValue && getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            returnValue = false;
        }

        boolean isSupportedStorageFormat =
                isStorageFormatSupportedByStoragePool() && isStorageFormatCompatibleWithDomain();
        if (returnValue && !isSupportedStorageFormat) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$storageFormat %1$s", getStorageDomain().getStorageFormat().toString()));
            returnValue = false;
        }
        return returnValue && CanAddDomain();
    }

    private boolean isStorageFormatSupportedByStoragePool() {
        StorageFormatType storageFormat = getStorageDomain().getStorageFormat();
        storage_pool targetStoragePool = getStoragePool();

        if (targetStoragePool == null) {
            targetStoragePool = getStoragePoolDAO().get(getVds().getStoragePoolId());
            if (targetStoragePool == null) {
                return false;
            }
        }

        Set<StorageFormatType> supportedStorageFormats =
                getSupportedStorageFormatSet(targetStoragePool.getcompatibility_version());
        if (!supportedStorageFormats.contains(storageFormat)) {
            return false;
        }

        return true;
    }

    private boolean isStorageFormatCompatibleWithDomain() {
        StorageFormatType storageFormat = getStorageDomain().getStorageFormat();
        StorageType storageType = getStorageDomain().getstorage_type();
        StorageDomainType storageDomainFunction = getStorageDomain().getstorage_domain_type();

        boolean isBlockStorage = storageType == StorageType.ISCSI || storageType == StorageType.FCP;
        boolean isDataStorageDomain = storageDomainFunction == StorageDomainType.Data;

        // V2 is applicable only for block data storage domains
        if (storageFormat == StorageFormatType.V2 && (!isBlockStorage || !isDataStorageDomain)) {
            return false;
        }

        // V3 is applicable only for data storage domains
        if (storageFormat == StorageFormatType.V3 && !isDataStorageDomain) {
            return false;
        }

        return true;
    }

    protected boolean CheckExistingStorageDomain() {
        boolean returnValue = true;
        // prevent importing DATA domain
        if (getParameters().getStorageDomain().getstorage_domain_type() == StorageDomainType.Data) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_DATA_DOMAIN_PROHIBITED);
            return false;
        }
        if (DbFacade.getInstance().getStorageDomainStaticDao().get(getStorageDomain().getId()) != null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            returnValue = false;
        }
        if (returnValue) {
            java.util.ArrayList<Guid> storageIds = (java.util.ArrayList<Guid>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.HSMGetStorageDomainsList,
                            new HSMGetStorageDomainsListVDSCommandParameters(getVdsId(), Guid.Empty, getStorageDomain()
                                    .getstorage_type(), getStorageDomain().getstorage_domain_type(), ""))
                    .getReturnValue();
            if (!storageIds.contains(getStorageDomain().getId())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
                returnValue = false;
            } else {
                Pair<StorageDomainStatic, SANState> domainFromIrs =
                        (Pair<StorageDomainStatic, SANState>) Backend
                                .getInstance()
                                .getResourceManager()
                                .RunVdsCommand(VDSCommandType.HSMGetStorageDomainInfo,
                                        new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(),
                                                getStorageDomain().getId()))
                                .getReturnValue();
                if (domainFromIrs != null
                        && domainFromIrs.getFirst().getstorage_domain_type() != getStorageDomain().getstorage_domain_type()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_CHANGE_STORAGE_DOMAIN_TYPE);
                    returnValue = false;
                }
                returnValue = returnValue && ConcreteCheckExistingStorageDomain(domainFromIrs);
            }
        }
        return returnValue;
    }

    protected boolean ConcreteCheckExistingStorageDomain(Pair<StorageDomainStatic, SANState> domainFromIrs) {
        return true;
    }

    protected String getStorageArgs() {
        return getStorageDomain().getstorage();
    }

    protected abstract boolean CanAddDomain();

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(SYSTEM_OBJECT_ID, VdcObjectType.System,
                getActionType().getActionGroup()));
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

}
