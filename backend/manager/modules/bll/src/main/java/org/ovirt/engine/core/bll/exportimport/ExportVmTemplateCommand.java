package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfUpdateProcessHelper;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
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
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
public class ExportVmTemplateCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    @Inject
    private OvfUpdateProcessHelper ovfUpdateProcessHelper;

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    private String cachedTemplateIsBeingExportedMessage;

    public ExportVmTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public ExportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        if (getVmTemplate() != null) {
            setDescription(getVmTemplateName());
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
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
                ActionReturnValue vdcRetValue =
                        runInternalActionWithTasksContext(ActionType.CopyImageGroup, p);

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
        vmHandler.updateVmInitFromDB(getVmTemplate(), true);
        if (!getTemplateDisks().isEmpty()) {
            moveOrCopyAllImageGroups();
        } else {
            endVmTemplateRelatedOps();
        }
        setSucceeded(true);
    }

    private String getTemplateIsBeingExportedMessage() {
        if (cachedTemplateIsBeingExportedMessage == null) {
            cachedTemplateIsBeingExportedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED)
                    .withOptional("TemplateName", getVmTemplate() != null ? getVmTemplate().getName() : null)
                    .toString();
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
            imagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                    imageFromSourceDomainMap,
                    null);
            if (getVmTemplate().getDiskTemplateMap().values().size() != imageFromSourceDomainMap.size()) {
                log.error("Can not found any default active domain for one of the disks of template with id '{}'",
                        getVmTemplate().getId());
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            }

            if (validate(vmTemplateHandler.isVmTemplateImagesReady(getVmTemplate(), null,
                    true, true, true, false, getTemplateDisks()))) {
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

            MultipleStorageDomainsValidator multipleStorageDomainsValidator =
                    new MultipleStorageDomainsValidator(getStoragePoolId(), imageToDestinationDomainMap.values());
            if (!validate(multipleStorageDomainsValidator.isSupportedByManagedBlockStorageDomains(getActionType()))) {
                return false;
            }

            if (storagePoolIsoMapDao.get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                            getVmTemplate().getStoragePoolId())) == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
            }
        }

        // check if template (with no override option)
        if (!getParameters().getForceOverride()) {
            if (checkTemplateInStorageDomain(getVmTemplate().getStoragePoolId(), getVmTemplateId())) {
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

    private boolean checkIfDisksExist(Collection<DiskImage> disksList) {
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
            ovfUpdateProcessHelper.loadTemplateData(getVmTemplate());
            vmTemplateHandler.updateDisksFromDb(getVmTemplate());
            // update the target (export) domain
            ovfUpdateProcessHelper.buildMetadataDictionaryForTemplate(getVmTemplate(), metaDictionary);
            ovfUpdateProcessHelper.executeUpdateVmInSpmCommand(getVmTemplate().getStoragePoolId(),
                    metaDictionary,
                    getParameters().getStorageDomainId());
        }
    }

    @Override
    protected void endActionOnAllImageGroups() {
        for (ActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            backend.endAction(ActionType.CopyImageGroup,
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
