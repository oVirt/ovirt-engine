package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
public class ExportVmTemplateCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    private String cachedTemplateIsBeingExportedMessage;

    public ExportVmTemplateCommand(T parameters) {
        super(parameters);
        if (getVmTemplate() != null) {
            setDescription(getVmTemplateName());
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
    }

    protected ExportVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    // we force export template image to COW+Sparse but we don't update
                    // the ovf so the import
                    // will set the original format
                    MoveOrCopyImageGroupParameters p = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getId(), disk.getImageId(), getParameters().getStorageDomainId(),
                            getMoveOrCopyImageOperation());
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
                        throw new VdcBLLException(vdcRetValue.getFault().getError(), vdcRetValue.getFault()
                                .getMessage());
                    }

                    getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
                }
                return null;
            }
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
            StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED.name());
            if (getVmTemplate() != null) {
                builder.append(String.format("$TemplateName %1$s", getVmTemplate().getName()));
            }
            cachedTemplateIsBeingExportedMessage = builder.toString();
        }
        return cachedTemplateIsBeingExportedMessage;
    }

    @Override
    protected boolean canDoAction() {
        if (getVmTemplate() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        boolean retVal = validate(storageDomainValidator.isDomainExistAndActive());

        if (retVal) {
            // export must be to export domain
            if (getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN);
                retVal = false;
            }
        }

        retVal = retVal && super.canDoAction();

        // check if template (with no override option)
        if (retVal && !getParameters().getForceOverride()) {
            retVal = !ExportVmCommand.checkTemplateInStorageDomain(getVmTemplate().getStoragePoolId(),
                    getParameters().getStorageDomainId(), getVmTemplateId(), getContext().getEngineContext());
            if (!retVal) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            }
        }

        return retVal;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__EXPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
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
        }
        return super.getAuditLogTypeValue();
    }

    @Override
    protected void incrementDbGeneration() {
        // we want to export the Template's ovf only in case that all tasks has succeeded, otherwise we will attempt to
        // revert
        // and there's no need for exporting the template's ovf.
        if (getParameters().getTaskGroupSuccess()) {
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary =
                    new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>();
            OvfDataUpdater.getInstance().loadTemplateData(getVmTemplate());
            VmTemplateHandler.updateDisksFromDb(getVmTemplate());
            // update the target (export) domain
            OvfDataUpdater.getInstance().buildMetadataDictionaryForTemplate(getVmTemplate(), metaDictionary);
            OvfDataUpdater.getInstance().executeUpdateVmInSpmCommand(getVmTemplate().getStoragePoolId(),
                    metaDictionary,
                    getParameters().getStorageDomainId());
        }
    }

    @Override
    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            getBackend().endAction(getImagesActionType(),
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
