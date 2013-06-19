package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.provider.OpenstackImageProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
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

    private OpenstackImageProviderProxy providerProxy;

    public ImportRepoImageCommand(T parameters) {
        super(parameters);

        getParameters().setCommandType(getActionType());
    }

    protected ProviderProxyFactory getProviderProxyFactory() {
        return ProviderProxyFactory.getInstance();
    }

    protected OpenstackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenstackImageProviderProxy
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
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        // NOTE: there's no read-permission from a storage domain
        permissionSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage, ActionGroup.CREATE_DISK));
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

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("repoimagename", getDiskImage() != null ? getDiskImage().getDiskAlias() : "");
        }
        return jobProperties;
    }

    @Override
    protected boolean canDoAction() {
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        DiskImage diskImage = null;

        try {
            diskImage = getDiskImage();
        } catch (Exception e) {
            log.error("Unable to get the disk image from the provider proxy", e);
        }

        if (diskImage == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }

        return true;
    }
}
