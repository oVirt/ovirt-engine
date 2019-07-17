package org.ovirt.engine.core.bll.storage.domain;

import static org.ovirt.engine.core.bll.MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageBlockSize;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.vdscommands.CreateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class AddStorageDomainCommand<T extends StorageDomainManagementParameter> extends
        StorageDomainManagementCommandBase<T> {

    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private StoragePoolDao storagePoolDao;


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
        if(getStorageDomain().getWarningLowConfirmedSpaceIndicator() == null) {
            getStorageDomain().getStorageStaticData().setWarningLowConfirmedSpaceIndicator(
                    Config.<Integer>getValue(ConfigValues.WarningLowSpaceIndicator));
        }
        if(getStorageDomain().getCriticalSpaceActionBlocker() == null) {
            getStorageDomain().setCriticalSpaceActionBlocker(Config.<Integer>getValue(ConfigValues.CriticalSpaceActionBlocker));
        }
    }

    protected boolean addStorageDomainInIrs() {
        StorageDomainStatic storageDomainStatic = getStorageDomain().getStorageStaticData();
        if (isBlockSizeAutoDetectionSupported()) {
            storageDomainStatic.setBlockSize(StorageBlockSize.BLOCK_AUTO);
        }
        // No need to run in separate transaction - counting on rollback of external transaction wrapping the command
        return runVdsCommand(
                VDSCommandType.CreateStorageDomain,
                new CreateStorageDomainVDSCommandParameters(getVds().getId(),
                        storageDomainStatic,
                        getStorageArgs())).getSucceeded();
    }

    protected void addStorageDomainInDb() {
        TransactionSupport.executeInNewTransaction(() -> {
            StorageDomainStatic storageStaticData = getStorageDomain().getStorageStaticData();
            storageDomainStaticDao.save(storageStaticData);

            getCompensationContext().snapshotNewEntity(storageStaticData);
            StorageDomainDynamic newStorageDynamic =
                    new StorageDomainDynamic(null, getStorageDomain().getId(), null);
            getReturnValue().setActionReturnValue(getStorageDomain().getId());
            storageDomainDynamicDao.save(newStorageDynamic);
            getCompensationContext().snapshotNewEntity(newStorageDynamic);
            getCompensationContext().stateChanged();
            return null;
        });
        if (getStorageDomain().getStorageDomainType().isDataDomain()) {
            createDefaultDiskProfile();
        }
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
        } else if (addStorageDomainInIrs()) {
            updateStorageDomainFromIrs();
            setSucceeded(true);
        }
    }

    protected Pair<Boolean, Integer> connectStorage() {
        String connectionId = getStorageDomain().getStorage();
        StorageServerConnections connection = storageServerConnectionDao.get(connectionId);
        Map<String, String> result = (Map<String, String>) runVdsCommand(
                        VDSCommandType.ConnectStorageServer,
                        new StorageServerConnectionManagementVDSParameters(getParameters().getVdsId(), Guid.Empty,
                                connection.getStorageType(),
                                new ArrayList<>(Collections.singletonList(connection))))
                .getReturnValue();
        return new Pair<>(storageHelperDirector.getItem(connection.getStorageType())
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

        StorageDomainValidator sdValidator = getStorageDomainValidator();
        if (!validate(sdValidator.isNotIsoOrExportForBackup())) {
            return false;
        }

        ensureStorageFormatInitialized();
        if (!validate(sdValidator.isStorageFormatCompatibleWithDomain())) {
            return false;
        }

        if (!checkDomainVersionSupport()) {
            return false;
        }

        initStorageDomainDiscardAfterDeleteIfNeeded();
        if (!validateDiscardAfterDeleteLegal(sdValidator)) {
            return false;
        }

        if (!canAddDomain()) {
            return false;
        }
        if (isStorageWithSameNameExists()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NAME_ALREADY_EXIST);
        }
        if (!isSupportedByManagedBlockStorageDomain(getStorageDomain())) {
            return false;
        }
        return true;
    }

    private Boolean checkDomainVersionSupport() {
        StorageFormatType numericDomainFormat = getStorageDomain().getStorageFormat();
        List<String> haveNotSupportedVDSes = vdsDao.getAllForStoragePool(getStorageDomain().getStoragePoolId())
                .stream()
                .filter(vds -> !vds.getSupportedDomainVersions().contains(numericDomainFormat))
                .map(VDS::getName)
                .collect(Collectors.toList());

        if (!haveNotSupportedVDSes.isEmpty()) {
            List<String> replacements = new ArrayList<>(2);
            replacements.add(ReplacementUtils.createSetVariableString("hosts", haveNotSupportedVDSes));
            replacements.add(ReplacementUtils.createSetVariableString("formatversion", numericDomainFormat));
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_VERSION_UNSUPPORTED, replacements);
        }
        return true;
    }

    private Boolean isBlockSizeAutoDetectionSupported() {
        if (getStorageDomain().getStorageFormat().compareTo(StorageFormatType.V5) < 0) {
            // Block size auto detection is supported only for storage format >= V5
            return false;
        }
        StoragePool storagePool = getStoragePool();
        if (storagePool == null) {
            // In case of creating an unattached storage domain
            storagePool = storagePoolDao.get(getVds().getStoragePoolId());
        }
        return new StorageDomainToPoolRelationValidator(getStorageDomain().getStorageStaticData(), storagePool)
                .isBlockSizeAutoDetectionSupported().isValid();
    }

    private void ensureStorageFormatInitialized() {
        StorageDomain sd = getStorageDomain();
        if (sd.getStorageFormat() == null) {
            if (sd.getStorageDomainType().isDataDomain()) {
                sd.setStorageFormat(StorageFormatType.getLatest());
            } else {
                sd.setStorageFormat(StorageFormatType.V1);
            }
        }
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

    public StorageDomainValidator getStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }

    private void initStorageDomainDiscardAfterDeleteIfNeeded() {
        if (getStorageDomain().getDiscardAfterDelete() == null) {
            getStorageDomain().setDiscardAfterDelete(false);
        }
    }

    protected boolean validateDiscardAfterDeleteLegal(StorageDomainValidator storageDomainValidator) {
        /*
        Discard after delete is only relevant for block storage domains that should override this method.
        If it is enabled for a non block storage domain, the validation should fail.
         */
        if (getStorageDomain().getDiscardAfterDelete()) {
            return failValidation(
                    EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_SUPPORTED_ONLY_BY_BLOCK_DOMAINS);
        }
        return true;
    }
}
