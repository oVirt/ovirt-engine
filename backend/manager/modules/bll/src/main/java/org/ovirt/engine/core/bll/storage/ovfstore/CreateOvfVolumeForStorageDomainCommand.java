package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.CreateOvfVolumeForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateOvfVolumeForStorageDomainCommand<T extends CreateOvfVolumeForStorageDomainCommandParameters> extends StorageDomainCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private StorageDomainOvfInfoDao storageDomainOvfInfoDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public CreateOvfVolumeForStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    public CreateOvfVolumeForStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void executeCommand() {
        AddDiskParameters diskParameters = new AddDiskParameters(createDisk(getStorageDomainId()));
        diskParameters.setStorageDomainId(getStorageDomainId());
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setSkipDomainCheck(getParameters().isSkipDomainChecks());
        ActionReturnValue actionReturnValue =
                runInternalActionWithTasksContext(ActionType.AddDisk, diskParameters);
        Guid createdId = actionReturnValue.getActionReturnValue();

        if (createdId != null) {
            setActionReturnValue(createdId);
            addStorageDomainOvfInfoToDb(createdId);
        }

        setSucceeded(actionReturnValue.getSucceeded());
    }

    private boolean shouldOvfStoreBeShareable() {
        // we don't create shareable disks on gluster domains to avoid
        // split brain - see BZ 1024654
        return getStorageDomain().getStorageType() != StorageType.GLUSTERFS;
    }

    public DiskImage createDisk(Guid domainId) {
        DiskImage createdDiskImage = new DiskImage();
        createdDiskImage.setContentType(DiskContentType.OVF_STORE);
        createdDiskImage.setWipeAfterDelete(false);
        createdDiskImage.setDiskAlias(OvfInfoFileConstants.OvfStoreDescriptionLabel);
        createdDiskImage.setDiskDescription(OvfInfoFileConstants.OvfStoreDescriptionLabel);
        createdDiskImage.setShareable(shouldOvfStoreBeShareable());
        createdDiskImage.setStorageIds(new ArrayList<>(Collections.singletonList(domainId)));
        createdDiskImage.setSize(SizeConverter.BYTES_IN_MB * 128);
        createdDiskImage.setVolumeFormat(VolumeFormat.RAW);
        createdDiskImage.setVolumeType(getStorageDomain().getStorageType().isBlockDomain() ?
                VolumeType.Preallocated : VolumeType.Sparse);
        createdDiskImage.setDescription("OVF store for domain " + domainId);
        Date creationDate = new Date();
        createdDiskImage.setCreationDate(creationDate);
        createdDiskImage.setLastModified(creationDate);
        return createdDiskImage;
    }

    private void addStorageDomainOvfInfoToDb(Guid diskId) {
        StorageDomainOvfInfo storageDomainOvfInfo =
                new StorageDomainOvfInfo(getStorageDomainId(), null, diskId, StorageDomainOvfInfoStatus.DISABLED, null);
        storageDomainOvfInfoDao.save(storageDomainOvfInfo);
    }

    private void logFailure() {
        Guid createdDiskId = getActionReturnValue();
        if (createdDiskId != null) {
            addCustomValue("DiskId", createdDiskId.toString());
            auditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_FAILED);
        } else {
            auditLogDirector.log(this, AuditLogType.CREATE_OVF_STORE_FOR_STORAGE_DOMAIN_INITIATE_FAILED);
        }
    }

    private void endChildCommand(boolean succeeded) {
        if (!getParameters().getImagesParameters().isEmpty()) {
            ActionParametersBase childParams = getParameters().getImagesParameters().get(0);
            childParams.setTaskGroupSuccess(succeeded);
            backend.endAction(childParams.getCommandType(), childParams,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    protected void endSuccessfully() {
        Guid createdDiskId = getActionReturnValue();
        endChildCommand(true);
        StorageDomainOvfInfo storageDomainOvfInfoDb = storageDomainOvfInfoDao.get(createdDiskId);

        if (storageDomainOvfInfoDb == null
                || storageDomainOvfInfoDb.getStatus() != StorageDomainOvfInfoStatus.DISABLED) {
            return;
        }

        storageDomainOvfInfoDb.setStatus(StorageDomainOvfInfoStatus.OUTDATED);
        storageDomainOvfInfoDao.update(storageDomainOvfInfoDb);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        endChildCommand(false);
        logFailure();
        setSucceeded(true);
    }
}
