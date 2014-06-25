package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageException;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@NonTransactiveCommandAttribute
public class ImportRepoImageCommand<T extends ImportRepoImageParameters> extends CommandBase<T>
        implements TaskHandlerCommand<ImportRepoImageParameters>, QuotaStorageDependent {

    private OpenStackImageProviderProxy providerProxy;

    public ImportRepoImageCommand(T parameters) {
        super(parameters);

        getParameters().setCommandType(getActionType());
        getParameters().setParametersCurrentUser(getCurrentUser());
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
    public T getParameters() {
        return super.getParameters();
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo,
                           VdcActionType parentCommand, VdcObjectType entityType, Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
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
        // Not implemented
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
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
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
            getParameters().setDiskImage(
                    getProviderProxy().getImageAsDiskImage(getParameters().getSourceRepoImageId()));
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
        }
        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
            case EXECUTE:
                if (!getParameters().getTaskGroupSuccess()) {
                    return AuditLogType.USER_IMPORT_IMAGE_FINISHED_FAILURE;
                }
                if (getParameters().getExecutionIndex() == 0 && getSucceeded()) {
                    return AuditLogType.USER_IMPORT_IMAGE;
                }
                break;
            case END_SUCCESS:
                return AuditLogType.USER_IMPORT_IMAGE_FINISHED_SUCCESS;
            case END_FAILURE:
                return AuditLogType.USER_IMPORT_IMAGE_FINISHED_FAILURE;
        }
        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected boolean canDoAction() {
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        if (getParameters().getImportAsTemplate()) {
            if (getParameters().getClusterId() == null) {
                addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
                return false;
            }

            setVdsGroupId(getParameters().getClusterId());
            if (getVdsGroup() == null) {
                addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
                return false;
            }

            // A Template cannot be added in a cluster without a defined architecture
            if (getVdsGroup().getArchitecture() == ArchitectureType.undefined) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
            }

            setStoragePoolId(getParameters().getStoragePoolId());
            if (!FeatureSupported.importGlanceImageAsTemplate(getStoragePool().getcompatibility_version())) {
                return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
            }
        }

        DiskImage diskImage = null;

        try {
            diskImage = getDiskImage();
        } catch (OpenStackImageException e) {
            log.errorFormat("Unable to get the disk image from the provider proxy: {0} ({1})",
                    e.getMessage(), e.getErrorType());
            switch (e.getErrorType()) {
                case UNSUPPORTED_CONTAINER_FORMAT:
                case UNSUPPORTED_DISK_FORMAT:
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_NOT_SUPPORTED);
                case UNABLE_TO_DOWNLOAD_IMAGE:
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_DOWNLOAD_ERROR);
                case UNRECOGNIZED_IMAGE_FORMAT:
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_UNRECOGNIZED);
            }
        }

        if (diskImage == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        return true;
    }
}
