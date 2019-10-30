package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AmendImageGroupVolumesCommandParameters;
import org.ovirt.engine.core.common.action.AmendVolumeCommandParameters;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class AmendImageGroupVolumesCommand<T extends AmendImageGroupVolumesCommandParameters>
        extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDao vmDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    private DiskImage diskImage;

    @Inject
    private ImagesHandler imagesHandler;

    public AmendImageGroupVolumesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private DiskImage getDiskImage() {
        if (diskImage == null) {
            diskImage = (DiskImage) diskDao.get(getParameters().getImageGroupID());
        }
        return diskImage;
    }

    @Override
    protected boolean validate() {
        DiskValidator diskValidator = createDiskValidator();
        if (!validate(diskValidator.isDiskExists())) {
            return false;
        }
        if (!validate(diskValidator.isDiskPluggedToAnyNonDownVm(false))) {
            return false;
        }
        setStoragePoolId(getDiskImage().getStoragePoolId());
        if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
            return false;
        }
        VmEntityType vmEntityType = getDiskImage().getVmEntityType();
        if (vmEntityType != null && vmEntityType.isTemplateType()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_AMEND_TEMPLATE_DISK);
        }
        setStorageDomainId(getDiskImage().getStorageIds().get(0));
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        if (!validate(storageDomainValidator.isDomainExistAndActive())) {
            return false;
        }
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(Collections.singletonList(getDiskImage()));
        if (!isInternalExecution() && !validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }
        return validate(diskImagesValidator.diskImagesNotIllegal());
    }

    protected DiskValidator createDiskValidator() {
        return new DiskValidator(getDiskImage());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> images = diskImageDao
                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
        getParameters().setImageIds(ImagesHandler.getDiskImageIds(images.stream()
                .filter(disk -> disk.isQcowFormat() && disk.getQcowCompat() != getParameters().getQcowCompat())
                .collect(Collectors.toList())));
        persistCommand(getActionType(), getCallback() != null);
        setSucceeded(true);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }
        jobProperties.put("action", "Amending");
        jobProperties.put("diskalias", getDiskImage().getDiskAlias());
        return jobProperties;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildren) {
        if (completedChildren == getParameters().getImageIds().size()) {
            return false;
        }

        Guid imageId = getParameters().getImageIds().get(completedChildren);
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1, getParameters().getImageIds().size(), imageId);

        amendVolume(imageId);
        return true;
    }

    private void amendVolume(Guid imageId) {
        AmendVolumeCommandParameters parameters =
                new AmendVolumeCommandParameters(getDiskImage().getStoragePoolId(),
                        buildImageLocationInfo(getDiskImage().getStorageIds().get(0), getDiskImage().getId(), imageId),
                        getParameters().getQcowCompat());

        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setJobWeight(getParameters().getOperationsJobWeight().get(imageId.toString()));
        runInternalActionWithTasksContext(ActionType.AmendVolume, parameters);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        DiskImage diskImage = getDiskImage();
        if (diskImage == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                new PermissionSubject(diskImage.getId(),
                        VdcObjectType.Disk,
                        getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__AMEND);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DiskAlias", getDiskImage().getDiskAlias());

        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_AMEND_IMAGE_START
                    : AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_AMEND_IMAGE_FINISH_SUCCESS
                    : AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;

        default:
            return AuditLogType.USER_AMEND_IMAGE_FINISH_FAILURE;
        }
    }

    private VdsmImageLocationInfo buildImageLocationInfo(Guid domId, Guid imageGroupId, Guid imageId) {
        return new VdsmImageLocationInfo(domId, imageGroupId, imageId, null);
    }
}
