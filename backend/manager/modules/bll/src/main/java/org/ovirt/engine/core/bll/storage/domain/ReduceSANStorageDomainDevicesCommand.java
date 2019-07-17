package org.ovirt.engine.core.bll.storage.domain;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.BlockStorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.ReduceSANStorageDomainDevicesCommandParameters;
import org.ovirt.engine.core.common.action.RemoveDeviceFromSANStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

@NonTransactiveCommandAttribute
public class ReduceSANStorageDomainDevicesCommand<T extends ReduceSANStorageDomainDevicesCommandParameters> extends StorageDomainCommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private BlockStorageDomainValidator blockSDValidator;
    @Inject
    private BlockStorageDomainHelper blockStorageDomainHelper;
    @Inject
    private LunDao lunDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ReduceSANStorageDomainDevicesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVdsId(getParameters().getVdsId());
    }

    @Override
    protected void executeCommand() {
        // TODO: The domain is currently being locked only in memory, that's because LOCKED domains are considered
        // as active in respect to the pool metadata and we don't want host to start monitor that domain while
        // its edited and on maintenance status from the engine perspective.
        setSucceeded(true);
    }

    private void connectHostToDomain() {
        if (!storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .connectStorageToDomainByVdsId(getStorageDomain(), getVdsId())) {
            throw new EngineException(EngineError.StorageServerConnectionError);
        }
    }

    private void disconnectHostFromDomain() {
        storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .disconnectStorageFromDomainByVdsId(getStorageDomain(), getVdsId());
    }

    private void prepareForRemove() {
        List<String> devices = lunDao.getAllForVolumeGroup(getStorageDomain().getStorage())
                .stream()
                .map(LUNs::getId)
                .filter(x -> !getParameters().getDevicesToReduce().contains(x))
                .collect(toList());
        getParameters().setDstDevices(devices);
        persistCommandIfNeeded();
    }


    // Includes validations that involve storage access and shouldn't be performed on the command synchronous part.
    private void validateRemove() {
        // Performed here in order to complete the information/perform validation for domains for which we don't have
        // the metadata device information (as we didn't have that information prior to v4.1). We don't want to perform
        // vdsm call in the synchronous validate() to get that information, therefore it's perform as part of the
        // asynchronous execution.
        if (getStorageDomain().getVgMetadataDevice() == null || getStorageDomain().getFirstMetadataDevice() == null) {
            blockStorageDomainHelper.fillMetadataDevicesInfo(getStorageDomain().getStorageStaticData(),
                    getParameters().getVdsId());
            validateRetrievedMetadataDevices();
            storageDomainStaticDao.update(getStorageDomain().getStorageStaticData());
            List<String> metadataDevices = blockStorageDomainHelper.findMetadataDevices(getStorageDomain(),
                    getParameters().getDevicesToReduce());
            if (!metadataDevices.isEmpty()) {
                setCustomCommaSeparatedValues("deviceIds", metadataDevices);
                auditLogDirector.log(this, AuditLogType.USER_REDUCE_DOMAIN_DEVICES_FAILED_METADATA_DEVICES);
                throw new EngineException(EngineError.GeneralException, "Cannot perform on metadata devices");
            }
        }

        // Performed here in order to avoid storage access during the validate() execution.
        validateFreeSpace();
    }

    private void validateRetrievedMetadataDevices() {
        if (!blockStorageDomainHelper.checkDomainMetadataDevices(getStorageDomain())) {
            throw new EngineException(EngineError.GeneralException, "Couldn't determine the domain metadata devices");
        }
    }

    public void validateFreeSpace() {
        List<LUNs> allLuns =
                blockStorageDomainHelper.getVgLUNsInfo(getStorageDomain().getStorageStaticData(), getVdsId());
        if (allLuns == null) {
            auditLogDirector.log(this, AuditLogType.USER_REDUCE_DOMAIN_DEVICES_FAILED_TO_GET_DOMAIN_INFO);
            throw new EngineException(EngineError.GeneralException, "Failed to get the vg info");
        }
        long freeExtents = allLuns.stream()
                .filter(l -> getParameters().getDstDevices().contains(l.getLUNId()))
                .mapToLong(l -> l.getPeCount() - l.getPeAllocatedCount())
                .sum();

        long neededExtents = allLuns.stream()
                .filter(l -> getParameters().getDevicesToReduce().contains(l.getLUNId()))
                .mapToLong(LUNs::getPeAllocatedCount)
                .sum();

        if (neededExtents > freeExtents) {
            auditLogDirector.log(this, AuditLogType.USER_REDUCE_DOMAIN_DEVICES_FAILED_NO_FREE_SPACE);
            throw new EngineException(EngineError.GeneralException, "Not enough free space on the destination devices");
        }
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getRemoveIndex() == 0) {
            prepareForRemove();
            connectHostToDomain();
            validateRemove();
        }

        if (getParameters().getRemoveIndex() < getParameters().getDevicesToReduce().size()) {
            runInternalActionWithTasksContext(ActionType.RemoveDeviceFromSANStorageDomain,
                    createRemoveParameters(getParameters().getDevicesToReduce().get(getParameters().getRemoveIndex())));
            getParameters().setRemoveIndex(getParameters().getRemoveIndex() + 1);
            persistCommandIfNeeded();
            return true;
        }

        return false;
    }

    private RemoveDeviceFromSANStorageDomainCommandParameters createRemoveParameters(String deviceId) {
        RemoveDeviceFromSANStorageDomainCommandParameters p =
                new RemoveDeviceFromSANStorageDomainCommandParameters(getParameters().getStorageDomainId(), deviceId);
        p.setEndProcedure(EndProcedure.PARENT_MANAGED);
        p.setVdsId(getParameters().getVdsId());
        p.setParentCommand(getActionType());
        p.setParentParameters(getParameters());
        p.setDestinationDevices(getParameters().getDstDevices());
        return p;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected boolean validate() {
        if (CollectionUtils.isEmpty(getParameters().getDevicesToReduce())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_DEVICES_PROVIDED);
        }

        if (!checkStorageDomain()) {
            return false;
        }

        if (!getStorageDomain().getStorageType().isBlockDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        if (!validateReduceDeviceSupported()) {
            return false;
        }

        if (!validateDevices()) {
            return false;
        }

        if (!checkStorageDomainStatus(StorageDomainStatus.Maintenance)) {
            return false;
        }

        if (!initializeVds()) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean initializeVds() {
        if (super.initializeVds()) {
            getParameters().setVdsId(getVds().getId());
            persistCommandIfNeeded();
            return true;
        }

        return false;
    }

    private boolean validateReduceDeviceSupported() {
        if (getStorageDomain().getStorageFormat() == StorageFormatType.V1) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL,
                    String.format("$storageFormat %1$s", StorageFormatType.V1.toString()));
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return AuditLogType.USER_REDUCE_DOMAIN_DEVICES_STARTED;
        case END_FAILURE:
            return AuditLogType.USER_REDUCE_DOMAIN_DEVICES_FAILED;
        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_REDUCE_DOMAIN_DEVICES_SUCCEEDED
                    : AuditLogType.USER_REDUCE_DOMAIN_DEVICES_FAILED;
        }

        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getStorageDomainId() != null) {
            return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                            EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_IS_BEING_REDUCED));
        }

        return super.getExclusiveLocks();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    private boolean validateDevices() {
        Set<String> devices = getParameters().getDevicesToReduce().stream().collect(toSet());
        if (devices.size() != getParameters().getDevicesToReduce().size()) {
            return validate(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DUPLICATE_DEVICE));
        }

        if (getParameters().getDevicesToReduce().size() ==
                lunDao.getAllForVolumeGroup(getStorageDomain().getStorage()).size()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_ALL_LUNS);
        }
        return validate(blockSDValidator.lunsInDomain(getStorageDomain(), devices)) &&
                validate(blockSDValidator.lunsEligibleForOperation(getStorageDomain(), devices));
    }

    protected void endActionOnDevices() {
        for (ActionParametersBase p : getParameters().getImagesParameters()) {
            backend.endAction(p.getCommandType(),
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    private void endOperation() {
        updateStorageDomainFromIrs();
        try {
            disconnectHostFromDomain();
        } catch (Exception e) {
            log.error("Failed to disconnect the host from the domain storage servers, ignoring", e);
        }
        // TODO: currently we need to execute the endAction() of the child commands from here after we disconnected from
        // the domain, that's because the connections filter code in the StorageHelper that currently assumes that if
        // lun id is passed there's no lun disk using the device. After that will be fixed we may let the child commands
        // end by themselves (EndProcedure = COMMAND_MANAGED) and remove the device from the domain/disconnect on that
        // phase before all the devices were reduced from the domain.
        endActionOnDevices();
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    @Override
    protected void endSuccessfully() {
        endOperation();
    }

    @Override
    protected void endWithFailure() {
        endOperation();
    }
}
