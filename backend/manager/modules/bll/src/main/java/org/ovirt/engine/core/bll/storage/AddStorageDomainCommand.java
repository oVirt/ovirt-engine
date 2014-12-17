package org.ovirt.engine.core.bll.storage;

import static org.ovirt.engine.core.bll.MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class AddStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T> {
    protected AddStorageDomainCommand(T parameters) {
        super(parameters);
    }

    protected AddStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    protected void initializeStorageDomain() {
        getStorageDomain().setId(Guid.newGuid());
        if(getStorageDomain().getStorageStaticData().getWipeAfterDelete() == null) {
            getStorageDomain().getStorageStaticData().setWipeAfterDelete(
                    WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(
                            getStorageDomain().getStorageStaticData().getStorageType()));
        }
    }

    protected boolean addStorageDomainInIrs() {
        // No need to run in separate transaction - counting on rollback of external transaction wrapping the command
        return runVdsCommand(
                        VDSCommandType.CreateStorageDomain,
                        new CreateStorageDomainVDSCommandParameters(getVds().getId(), getStorageDomain()
                                .getStorageStaticData(), getStorageArgs())).getSucceeded();
    }

    protected void addStorageDomainInDb() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                StorageDomainStatic storageStaticData = getStorageDomain().getStorageStaticData();
                DbFacade.getInstance().getStorageDomainStaticDao().save(storageStaticData);
                // create default disk profile for type master or data storage domains
                if (storageStaticData.getStorageDomainType().isDataDomain()) {
                    getDiskProfileDao().save(DiskProfileHelper.createDiskProfile(storageStaticData.getId(),
                            storageStaticData.getStorageName()));
                }
                getCompensationContext().snapshotNewEntity(storageStaticData);
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

    protected void updateStorageDomainDynamicFromIrs() {
        final StorageDomain sd =
                (StorageDomain) runVdsCommand(VDSCommandType.GetStorageDomainStats,
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
        initializeStorageDomain();
        addStorageDomainInDb();
        // check connection to storage
        Pair<Boolean, Integer> connectReturnValue = connectStorage();
        if (!connectReturnValue.getFirst()) {
            VdcFault fault = new VdcFault();
            fault.setError(VdcBllErrors.forValue(connectReturnValue.getSecond()));
            getReturnValue().setFault(fault);
            setSucceeded(false);
        }
        else if (addStorageDomainInIrs()) {
            updateStorageDomainDynamicFromIrs();
            setSucceeded(true);
        }
    }

    protected Pair<Boolean, Integer> connectStorage() {
        String connectionId = getStorageDomain().getStorage();
        StorageServerConnections connection = getStorageServerConnectionDAO().get(connectionId);
        Map<String, String> result = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getParameters().getVdsId(), Guid.Empty,
                                connection.getstorage_type(),
                                new ArrayList<>(Collections.singletonList(connection))))
                .getReturnValue();
        return new Pair<>(StorageHelperDirector.getInstance()
                .getItem(connection.getstorage_type())
                .isConnectSucceeded(result, Collections.singletonList(connection)),
                 Integer.parseInt(result.values().iterator().next()));

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_DOMAIN : AuditLogType.USER_ADD_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction() || !initializeVds() || !checkStorageDomainNameLengthValid()) {
            return false;
        }
        if (isStorageWithSameNameExists()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ISO
                && !getStorageDomain().getStorageType().isFileDomain()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ImportExport
                && getStorageDomain().getStorageType() == StorageType.LOCALFS) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!Guid.isNullOrEmpty(getParameters().getStoragePoolId()) && getTargetStoragePool() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        ensureStorageFormatInitialized();
        if (!isStorageFormatSupportedByStoragePool() || !isStorageFormatCompatibleWithDomain()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST);
            getReturnValue().getCanDoActionMessages().add(
                    String.format("$storageFormat %1$s", getStorageDomain().getStorageFormat()));
            return false;
        }
        return canAddDomain();
    }

    private void ensureStorageFormatInitialized() {
        StorageDomain sd = getStorageDomain();
        if (sd.getStorageFormat() == null) {
            if (sd.getStorageDomainType().isDataDomain()) {
                StoragePool sp = getTargetStoragePool();
                if (sp != null) {
                    sd.setStorageFormat(VersionStorageFormatUtil.getPreferredForVersion(
                            sp.getcompatibility_version(), sd.getStorageType())
                    );
                }
            } else {
                sd.setStorageFormat(StorageFormatType.V1);
            }
        }
    }

    private boolean isStorageFormatSupportedByStoragePool() {
        StorageFormatType storageFormat = getStorageDomain().getStorageFormat();
        StoragePool targetStoragePool = getTargetStoragePool();

        // No reason to check supported storage format if we don't have a pool, the storage format will be validated
        // upon the future attachment of the the created domain to a pool
        if (targetStoragePool == null) {
            return true;
        }

        Set<StorageFormatType> supportedStorageFormats =
                getSupportedStorageFormatSet(targetStoragePool.getcompatibility_version());
        return supportedStorageFormats.contains(storageFormat);

    }

    private StoragePool getTargetStoragePool() {
        StoragePool targetStoragePool = getStoragePool();

        if (targetStoragePool == null) {
            targetStoragePool = getStoragePoolDAO().get(getVds().getStoragePoolId());
        }
        return targetStoragePool;
    }

    private boolean isStorageFormatCompatibleWithDomain() {
        StorageFormatType storageFormat = getStorageDomain().getStorageFormat();
        StorageType storageType = getStorageDomain().getStorageType();
        StorageDomainType storageDomainFunction = getStorageDomain().getStorageDomainType();

        // V2 is applicable only for block data storage domains
        if (storageFormat == StorageFormatType.V2) {
            return storageDomainFunction.isDataDomain() && storageType.isBlockDomain();
        }

        // V3 is applicable only for data storage domains
        if (storageFormat == StorageFormatType.V3) {
            return storageDomainFunction.isDataDomain();
        }

        return true;
    }

    protected String getStorageArgs() {
        return getStorageDomain().getStorage();
    }

    protected abstract boolean canAddDomain();

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

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
    }
}
