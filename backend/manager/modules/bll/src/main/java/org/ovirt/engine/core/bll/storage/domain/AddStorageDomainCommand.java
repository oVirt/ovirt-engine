package org.ovirt.engine.core.bll.storage.domain;

import static org.ovirt.engine.core.bll.MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class AddStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T> {

    protected AddStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddStorageDomainCommand(T parameters,
            CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected void initializeStorageDomain() {
        getStorageDomain().setId(Guid.newGuid());
        updateStaticDataDefaults();
    }

    protected void updateStaticDataDefaults() {
        updateStorageDomainWipeAfterDelete();
        updateSpaceThresholds();
    }

    private void updateStorageDomainWipeAfterDelete() {
        if(getStorageDomain().getStorageStaticData().getWipeAfterDelete() == null) {
            getStorageDomain().getStorageStaticData().setWipeAfterDelete(
                    WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(
                            getStorageDomain().getStorageStaticData().getStorageType()));
        }
    }

    private void updateSpaceThresholds() {
        if(getStorageDomain().getWarningLowSpaceIndicator() == null) {
            getStorageDomain().setWarningLowSpaceIndicator(Config.<Integer>getValue(ConfigValues.WarningLowSpaceIndicator));
        }
        if(getStorageDomain().getCriticalSpaceActionBlocker() == null) {
            getStorageDomain().setCriticalSpaceActionBlocker(Config.<Integer>getValue(ConfigValues.CriticalSpaceActionBlocker));
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
        TransactionSupport.executeInNewTransaction(() -> {
            StorageDomainStatic storageStaticData = getStorageDomain().getStorageStaticData();
            DbFacade.getInstance().getStorageDomainStaticDao().save(storageStaticData);

            getCompensationContext().snapshotNewEntity(storageStaticData);
            StorageDomainDynamic newStorageDynamic =
                    new StorageDomainDynamic(null, getStorageDomain().getId(), null);
            getReturnValue().setActionReturnValue(getStorageDomain().getId());
            DbFacade.getInstance().getStorageDomainDynamicDao().save(newStorageDynamic);
            getCompensationContext().snapshotNewEntity(newStorageDynamic);
            getCompensationContext().stateChanged();
            return null;
        });
        if (getStorageDomain().getStorageDomainType().isDataDomain()) {
            createDefaultDiskProfile();
        }
    }

    protected void updateStorageDomainDynamicFromIrs() {
        final StorageDomain sd =
                (StorageDomain) runVdsCommand(VDSCommandType.GetStorageDomainStats,
                                new GetStorageDomainStatsVDSCommandParameters(getVds().getId(),
                                        getStorageDomain().getId()))
                        .getReturnValue();
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntity(getStorageDomain().getStorageDynamicData());
            DbFacade.getInstance().getStorageDomainDynamicDao().update(sd.getStorageDynamicData());
            getCompensationContext().stateChanged();
            return null;
        });
    }

    @Override
    protected void executeCommand() {
        initializeStorageDomain();
        addStorageDomainInDb();
        // check connection to storage
        Pair<Boolean, Integer> connectReturnValue = connectStorage();
        if (!connectReturnValue.getFirst()) {
            EngineFault fault = new EngineFault();
            fault.setError(EngineError.forValue(connectReturnValue.getSecond()));
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
        StorageServerConnections connection = getStorageServerConnectionDao().get(connectionId);
        Map<String, String> result = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getParameters().getVdsId(), Guid.Empty,
                                connection.getStorageType(),
                                new ArrayList<>(Collections.singletonList(connection))))
                .getReturnValue();
        return new Pair<>(StorageHelperDirector.getInstance()
                .getItem(connection.getStorageType())
                .isConnectSucceeded(result, Collections.singletonList(connection)),
                 Integer.parseInt(result.values().iterator().next()));

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_DOMAIN : AuditLogType.USER_ADD_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean validate() {
        if (!super.validate() || !initializeVds() || !checkStorageDomainNameLengthValid()) {
            return false;
        }
        if (isStorageWithSameNameExists()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ISO
                && !getStorageDomain().getStorageType().isFileDomain()) {
            addValidationMessageVariable("domainType", StorageConstants.ISO);
            addValidationMessageVariable("storageTypes", StorageConstants.FILE);

            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_CAN_BE_CREATED_ONLY_ON_SPECIFIC_STORAGE_DOMAINS);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.ImportExport
                && getStorageDomain().getStorageType().isBlockDomain()) {
            addValidationMessageVariable("domainType", StorageConstants.EXPORT);
            addValidationMessageVariable("storageTypes", StorageConstants.FILE);

            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_CAN_BE_CREATED_ONLY_ON_SPECIFIC_STORAGE_DOMAINS);
        }
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!Guid.isNullOrEmpty(getParameters().getStoragePoolId()) && getTargetStoragePool() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        ensureStorageFormatInitialized();
        StorageDomainToPoolRelationValidator storageDomainToPoolRelationValidator = getAttachDomainValidator();
        StorageDomainValidator sdValidator = getStorageDomainValidator();
        if ( !validate(storageDomainToPoolRelationValidator.isStorageDomainFormatCorrectForDC()) || !validate(sdValidator.isStorageFormatCompatibleWithDomain()) ) {
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
                    sd.setStorageFormat(VersionStorageFormatUtil.getForVersion(sp.getCompatibilityVersion()));
                }
            } else {
                sd.setStorageFormat(StorageFormatType.V1);
            }
        }
    }

    private StoragePool getTargetStoragePool() {
        StoragePool targetStoragePool = getStoragePool();

        if (targetStoragePool == null) {
            targetStoragePool = getStoragePoolDao().get(getVds().getStoragePoolId());
        }
        return targetStoragePool;
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
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
    }

    public StorageDomainToPoolRelationValidator getAttachDomainValidator() {
        return new StorageDomainToPoolRelationValidator(getStorageDomain().getStorageStaticData(), getTargetStoragePool());
    }

    public StorageDomainValidator getStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }
}
