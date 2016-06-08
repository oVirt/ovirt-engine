package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageException;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("unused")
@NonTransactiveCommandAttribute
public class ImportRepoImageCommand<T extends ImportRepoImageParameters> extends BaseImagesCommand<T>
        implements TaskHandlerCommand<ImportRepoImageParameters>, QuotaStorageDependent {

    private OpenStackImageProviderProxy providerProxy;

    public ImportRepoImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        getParameters().setCommandType(getActionType());
        setDefaultTemplateName();
        addAuditLogCustomValues();
    }

    private void setDefaultTemplateName() {
        if (getParameters().getImportAsTemplate() && getParameters().getTemplateName() == null) {
            // Following the same convention as the glance disk name,
            // using a GlanceTemplate prefix, followed by a short identifier.
            getParameters().setTemplateName("GlanceTemplate-" + Guid.newGuid().toString().substring(0, 7));
        }
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }

    protected OpenStackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenStackImageProviderProxy
                    .getFromStorageDomainId(getParameters().getSourceStorageDomainId());
        }
        return providerProxy;
    }

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return Arrays.<SPMAsyncTaskHandler> asList(
                new ImportRepoImageCreateTaskHandler(this),
                new ImportRepoImageCopyTaskHandler(this)
        );
    }

    /* Overridden stubs declared as public in order to implement ITaskHandlerCommand */

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
                           VdcActionType parentCommand, VdcObjectType entityType, Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public void preventRollback() {
        getParameters().setExecutionIndex(0);
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void taskEndSuccessfully() {
        super.endSuccessfully();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        // NOTE: there's no read-permission from a storage domain
        permissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage, ActionGroup.CREATE_DISK));
        permissionSubjects.add(new PermissionSubject(getParameters().getSourceStorageDomainId(),
                VdcObjectType.Storage, ActionGroup.ACCESS_IMAGE_STORAGE));
        return permissionSubjects;
    }

    @Override
    protected void executeCommand() {
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    public Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaStorageConsumptionParameter(
                getParameters().getQuotaId(), null, QuotaConsumptionParameter.QuotaAction.CONSUME,
                getParameters().getStorageDomainId(), (double) getDiskImage().getSizeInGigabytes()));
        return list;
    }

    protected DiskImage getDiskImage() {
        if (getParameters().getDiskImage() == null) {
            DiskImage diskImage = getProviderProxy().getImageAsDiskImage(getParameters().getSourceRepoImageId());
            if (diskImage != null) {
                if (diskImage.getVolumeFormat() == VolumeFormat.RAW &&
                        getStorageDomain().getStorageType().isBlockDomain()) {
                    diskImage.setVolumeType(VolumeType.Preallocated);
                } else {
                    diskImage.setVolumeType(VolumeType.Sparse);
                }
                if (getParameters().getDiskAlias() == null) {
                    diskImage.setDiskAlias(RepoImage.getRepoImageAlias(
                            StorageConstants.GLANCE_DISK_ALIAS_PREFIX, getParameters().getSourceRepoImageId()));
                } else {
                    diskImage.setDiskAlias(getParameters().getDiskAlias());
                }
            }
            getParameters().setDiskImage(diskImage);
        }
        return getParameters().getDiskImage();
    }

    public String getRepoImageName() {
        return getDiskImage() != null ? getDiskImage().getDiskAlias() : "";
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("repoimagename", getRepoImageName());
            jobProperties.put("storage", getStorageDomainName());
        }
        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
            case EXECUTE:
                if (!getParameters().getTaskGroupSuccess()) {
                    return getParameters().getImportAsTemplate() ?
                            AuditLogType.USER_IMPORT_IMAGE_AS_TEMPLATE_FINISHED_FAILURE :
                            AuditLogType.USER_IMPORT_IMAGE_FINISHED_FAILURE;
                }
                if (getParameters().getExecutionIndex() == 0 && getSucceeded()) {
                    return getParameters().getImportAsTemplate() ? AuditLogType.USER_IMPORT_IMAGE_AS_TEMPLATE :
                            AuditLogType.USER_IMPORT_IMAGE;
                }
                break;
            case END_SUCCESS:
                return getParameters().getImportAsTemplate() ?
                        AuditLogType.USER_IMPORT_IMAGE_AS_TEMPLATE_FINISHED_SUCCESS :
                        AuditLogType.USER_IMPORT_IMAGE_FINISHED_SUCCESS;
            case END_FAILURE:
                return getParameters().getImportAsTemplate() ?
                        AuditLogType.USER_IMPORT_IMAGE_AS_TEMPLATE_FINISHED_FAILURE :
                        AuditLogType.USER_IMPORT_IMAGE_FINISHED_FAILURE;
        }
        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected boolean validate() {
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (getParameters().getImportAsTemplate()) {
            if (getParameters().getClusterId() == null) {
                addValidationMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
                return false;
            }

            setClusterId(getParameters().getClusterId());
            if (getCluster() == null) {
                addValidationMessage(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
                return false;
            }

            // A Template cannot be added in a cluster without a defined architecture
            if (getCluster().getArchitecture() == ArchitectureType.undefined) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
            }

            setStoragePoolId(getParameters().getStoragePoolId());
        }

        DiskImage diskImage = null;

        try {
            diskImage = getDiskImage();
        } catch (OpenStackImageException e) {
            log.error("Unable to get the disk image from the provider proxy: ({}) {}",
                    e.getErrorType(),
                    e.getMessage());
            switch (e.getErrorType()) {
                case UNSUPPORTED_CONTAINER_FORMAT:
                case UNSUPPORTED_DISK_FORMAT:
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_NOT_SUPPORTED);
                case UNABLE_TO_DOWNLOAD_IMAGE:
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_DOWNLOAD_ERROR);
                case UNRECOGNIZED_IMAGE_FORMAT:
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_UNRECOGNIZED);
            }
        }

        if (diskImage == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        return validateSpaceRequirements(diskImage);
    }

    protected boolean validateSpaceRequirements(DiskImage diskImage) {
        diskImage.getSnapshots().add(diskImage); // Added for validation purposes.
        StorageDomainValidator sdValidator = createStorageDomainValidator();
        boolean result = validate(sdValidator.isDomainExistAndActive())
                && validate(sdValidator.isDomainWithinThresholds())
                && validate(sdValidator.hasSpaceForClonedDisk(diskImage));
        diskImage.getSnapshots().remove(diskImage);
        return result;
    }

    protected StorageDomainValidator createStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }

    private void addAuditLogCustomValues() {
        if (getParameters().getImportAsTemplate()) {
            addCustomValue("TemplateName", getParameters().getTemplateName());
        }
    }
}
