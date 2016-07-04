package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfUpdateProcessHelper;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
public class ExportVmTemplateCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    private String cachedTemplateIsBeingExportedMessage;

    public ExportVmTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        if (getVmTemplate() != null) {
            setDescription(getVmTemplateName());
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
    }

    public ExportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(() -> {
            for (DiskImage disk : disks) {
                // we force export template image to COW+Sparse but we don't update
                // the ovf so the import
                // will set the original format
                MoveOrCopyImageGroupParameters p = new MoveOrCopyImageGroupParameters(containerID, disk
                        .getId(), disk.getImageId(), getParameters().getStorageDomainId(),
                        ImageOperation.Copy);
                p.setParentCommand(getActionType());
                p.setParentParameters(getParameters());
                p.setEntityInfo(getParameters().getEntityInfo());
                p.setUseCopyCollapse(true);
                p.setCopyVolumeType(CopyVolumeType.SharedVol);
                p.setVolumeFormat(disk.getVolumeFormat());
                p.setVolumeType(disk.getVolumeType());
                p.setForceOverride(getParameters().getForceOverride());
                p.setRevertDbOperationScope(ImageDbOperationScope.NONE);
                p.setShouldLockImageOnRevert(false);
                p.setSourceDomainId(imageFromSourceDomainMap.get(disk.getId()).getStorageIds().get(0));
                VdcReturnValueBase vdcRetValue =
                        runInternalActionWithTasksContext(VdcActionType.CopyImageGroup, p);

                if (!vdcRetValue.getSucceeded()) {
                    throw new EngineException(vdcRetValue.getFault().getError(), vdcRetValue.getFault()
                            .getMessage());
                }

                getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
            }
            return null;
        });
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.REMOTE_TEMPLATE, getTemplateIsBeingExportedMessage()));
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateIsBeingExportedMessage()));
    }

    @Override
    protected void executeCommand() {
        VmHandler.updateVmInitFromDB(getVmTemplate(), true);
        if (!getTemplateDisks().isEmpty()) {
            moveOrCopyAllImageGroups();
        } else {
            endVmTemplateRelatedOps();
        }
        setSucceeded(true);
    }

    private String getTemplateIsBeingExportedMessage() {
        if (cachedTemplateIsBeingExportedMessage == null) {
            StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED.name());
            if (getVmTemplate() != null) {
                builder.append(String.format("$TemplateName %1$s", getVmTemplate().getName()));
            }
            cachedTemplateIsBeingExportedMessage = builder.toString();
        }
        return cachedTemplateIsBeingExportedMessage;
    }

    @Override
    protected boolean validate() {
        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        if (!validate(storageDomainValidator.isDomainExistAndActive())) {
            return false;
        }

        // export must be to export domain
        if (getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN);
        }

        if (getTemplateDisks() != null && !getTemplateDisks().isEmpty()) {
            ensureDomainMap(getTemplateDisks(), getParameters().getStorageDomainId());
            // check that images are ok
            ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                    imageFromSourceDomainMap,
                    null);
            if (getVmTemplate().getDiskTemplateMap().values().size() != imageFromSourceDomainMap.size()) {
                log.error("Can not found any default active domain for one of the disks of template with id '{}'",
                        getVmTemplate().getId());
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            }

            if (VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(), null,
                    getReturnValue().getValidationMessages(), true, true, true, false, getTemplateDisks())) {
                setStoragePoolId(getVmTemplate().getStoragePoolId());
                StorageDomainValidator sdValidator = createStorageDomainValidator(getStorageDomain());
                if (!validate(sdValidator.isDomainExistAndActive())
                        || !validate(sdValidator.isDomainWithinThresholds())
                        || !(getParameters().getForceOverride()
                        || (!getParameters().isImagesExistOnTargetStorageDomain() && checkIfDisksExist(getTemplateDisks())))
                        || !validateFreeSpaceOnDestinationDomain(sdValidator, getTemplateDisks())) {
                    return false;
                }
            }

            if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                            getVmTemplate().getStoragePoolId())) == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
            }
        }

        // check if template (with no override option)
        if (!getParameters().getForceOverride()) {
            if (ExportVmCommand.checkTemplateInStorageDomain(getVmTemplate().getStoragePoolId(),
                    getParameters().getStorageDomainId(), getVmTemplateId(), getContext().getEngineContext())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            }
        }

        return true;
    }

    private StorageDomainValidator createStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }

    private boolean validateFreeSpaceOnDestinationDomain(StorageDomainValidator storageDomainValidator, List<DiskImage> disksList) {
        return validate(storageDomainValidator.hasSpaceForClonedDisks(disksList));
    }

    private boolean checkIfDisksExist(Iterable<DiskImage> disksList) {
        return validate(new DiskImagesValidator(disksList).diskImagesOnStorage(imageToDestinationDomainMap, getStoragePoolId()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_EXPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_FAILED;
        }
    }

    @Override
    protected void incrementDbGeneration() {
        // we want to export the Template's ovf only in case that all tasks has succeeded, otherwise we will attempt to
        // revert
        // and there's no need for exporting the template's ovf.
        if (getParameters().getTaskGroupSuccess()) {
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary = new HashMap<>();
            OvfUpdateProcessHelper ovfUpdateProcessHelper = new OvfUpdateProcessHelper();
            ovfUpdateProcessHelper.loadTemplateData(getVmTemplate());
            VmTemplateHandler.updateDisksFromDb(getVmTemplate());
            // update the target (export) domain
            ovfUpdateProcessHelper.buildMetadataDictionaryForTemplate(getVmTemplate(), metaDictionary);
            ovfUpdateProcessHelper.executeUpdateVmInSpmCommand(getVmTemplate().getStoragePoolId(),
                    metaDictionary,
                    getParameters().getStorageDomainId());
        }
    }

    @Override
    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            getBackend().endAction(VdcActionType.CopyImageGroup,
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
        }
        return jobProperties;
    }
}
