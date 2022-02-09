package org.ovirt.engine.core.bll.storage.repoimage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageException;
import org.ovirt.engine.core.bll.provider.storage.OpenStackImageProviderProxy;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.DownloadImageCommandParameters;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class ImportRepoImageCommand<T extends ImportRepoImageParameters> extends BaseImagesCommand<T> implements SerialChildExecutingCommand, QuotaStorageDependent {

    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;
    @Inject
    private OsRepository osRepository;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    @Inject
    private ImagesHandler imagesHandler;

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

    @Override
    protected void executeCommand() {
        setupParameters();
        persistCommand(getParameters().getParentCommand(), true);
        backend.runInternalAction(ActionType.AddDisk,
                createAddDiskParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));
        getParameters().setNextPhase(ImportRepoImageParameters.Phase.DOWNLOAD);
        persistCommand(getParameters().getParentCommand(), true);
        setActionReturnValue(getParameters().getDiskImage().getId());
        setSucceeded(true);
    }

    private void setupParameters() {
        getParameters().setImageGroupID(Guid.newGuid());
        getParameters().setDestinationImageId(Guid.newGuid());
        if (getParameters().getImportAsTemplate()) {
            getParameters().setVmSnapshotId(Guid.newGuid());
        }
        getParameters().setEntityInfo(
                new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroupID()));
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    protected AddDiskParameters createAddDiskParameters() {
        DiskImage diskImage = getParameters().getDiskImage();
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(getParameters().getStorageDomainId());
        diskImage.setDiskAlias(getParameters().getDiskAlias());
        diskImage.setStorageIds(storageIds);
        diskImage.setId(getParameters().getImageGroupID());
        diskImage.setDiskProfileId(getParameters().getDiskProfileId());
        diskImage.setImageId(getParameters().getDestinationImageId());
        diskImage.setQuotaId(getParameters().getQuotaId());
        AddDiskParameters parameters = new AddDiskParameters(diskImage);
        parameters.setStorageDomainId(getParameters().getStorageDomainId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setShouldRemainIllegalOnFailedExecution(true);
        parameters.setShouldRemainLockedOnSuccesfulExecution(true);
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setUsePassedDiskId(true);
        parameters.setUsePassedImageId(true);
        parameters.setVmSnapshotId(getParameters().getVmSnapshotId());
        return parameters;
    }

    private HttpLocationInfo prepareRepoImageLocation() {
        return new HttpLocationInfo(
                getProviderProxy().getImageUrl(
                        imagesHandler.getSpmCompatibilityVersion(getParameters().getStoragePoolId()),
                        getParameters().getSourceRepoImageId()),
                getProviderProxy().getDownloadHeaders());
    }

    protected DownloadImageCommandParameters createDownloadImageParameters() {
        DownloadImageCommandParameters parameters = new DownloadImageCommandParameters();
        parameters.setDestinationImageId(getParameters().getDestinationImageId());
        parameters.setStoragePoolId(getParameters().getStoragePoolId());
        parameters.setStorageDomainId(getParameters().getStorageDomainId());
        parameters.setImageGroupID(getParameters().getImageGroupID());
        parameters.setHttpLocationInfo(prepareRepoImageLocation());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getImageGroupID()));
        return parameters;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getNextPhase() == ImportRepoImageParameters.Phase.DOWNLOAD) {
            getParameters().setNextPhase(ImportRepoImageParameters.Phase.END);
            persistCommand(getParameters().getParentCommand(), true);
            backend.runInternalAction(ActionType.DownloadImage,
                    createDownloadImageParameters(),
                    ExecutionHandler.createDefaultContextForTasks(getContext()));
            return true;
        }

        return false;
    }

    @Override
    public void handleFailure() {
        updateDiskStatus(ImageStatus.ILLEGAL);
        removeDisk();
    }

    public void removeDisk() {
        backend.runInternalAction(ActionType.RemoveDisk, new RemoveDiskParameters(getParameters().getImageGroupID()));
    }

    protected OpenStackImageProviderProxy getProviderProxy() {
        if (providerProxy == null) {
            providerProxy = OpenStackImageProviderProxy
                    .getFromStorageDomainId(getParameters().getSourceStorageDomainId(), providerProxyFactory);
        }
        return providerProxy;
    }

    @Override
    public void endSuccessfully() {
        super.endSuccessfully();
        setQcowCompatForQcowImage();
        if (getParameters().getImportAsTemplate()) {
            Guid newTemplateId = createTemplate();
            // No reason for this to happen, but checking just to make sure
            if (newTemplateId != null) {
                attachDiskToTemplate(newTemplateId);
            }
        }
        updateDiskStatus(ImageStatus.OK);
        setSucceeded(true);
    }

    private void setQcowCompatForQcowImage() {
        Image image = imageDao.get(getDiskImage().getImageId());
        if (getDiskImage().getDiskStorageType() == DiskStorageType.IMAGE
                && getDiskImage().getVolumeFormat() == VolumeFormat.COW) {
            image.setQcowCompat(getDiskImage().getQcowCompat());
            imageDao.update(image);
        }
    }

    @Override
    public void endWithFailure() {
        updateDiskStatus(ImageStatus.ILLEGAL);
        setSucceeded(true);
    }

    private void updateDiskStatus(ImageStatus status) {
        getParameters().getDiskImage().setImageStatus(status);
        imagesHandler.updateImageStatus(getParameters().getDestinationImageId(), status);
    }

    private Guid createTemplate() {

        VmTemplate blankTemplate = vmTemplateDao.get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        VmStatic masterVm = new VmStatic(blankTemplate);

        DiskImage templateDiskImage = getParameters().getDiskImage();
        String vmTemplateName = getParameters().getTemplateName();
        AddVmTemplateParameters parameters = new AddVmTemplateParameters(masterVm, vmTemplateName, templateDiskImage.getDiskDescription());

        // Setting the user from the parent command, as the session might already be invalid
        parameters.setParametersCurrentUser(getParameters().getParametersCurrentUser());

        // Setting the cluster ID, and other related properties derived from it
        if (getParameters().getClusterId() != null) {
            masterVm.setClusterId(getParameters().getClusterId());
            Cluster vdsGroup = getCluster(masterVm.getClusterId());
            masterVm.setOsId(osRepository.getDefaultOSes().get(vdsGroup.getArchitecture()));
            DisplayType defaultDisplayType =
                    osRepository.getGraphicsAndDisplays(masterVm.getOsId(), vdsGroup.getCompatibilityVersion()).get(0).getSecond();
            masterVm.setDefaultDisplayType(defaultDisplayType);
        }

        ActionReturnValue addVmTemplateReturnValue =
                backend.runInternalAction(ActionType.AddVmTemplate,
                        parameters,
                        ExecutionHandler.createDefaultContextForTasks(getContext()));

        // No reason for this to return null, but checking just to make sure, and returning the created template, or null if failed
        return addVmTemplateReturnValue.getActionReturnValue() != null ? (Guid) addVmTemplateReturnValue.getActionReturnValue() : null;
    }

    public Cluster getCluster(Guid clusterId) {
        return clusterDao.get(clusterId);
    }

    private void attachDiskToTemplate(Guid templateId) {
        DiskImage templateDiskImage = getParameters().getDiskImage();
        DiskVmElement dve = new DiskVmElement(templateDiskImage.getId(), templateId);
        dve.setBoot(true);
        dve.setDiskInterface(DiskInterface.VirtIO);
        diskVmElementDao.save(dve);
        vmDeviceUtils.addDiskDevice(templateId, templateDiskImage.getId());
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
                getParameters().getQuotaId(), QuotaConsumptionParameter.QuotaAction.CONSUME,
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
                if (getSucceeded()) {
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
        if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
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
            auditLog(this, AuditLogType.FAILED_IMPORT_IMAGE_FROM_REPOSITORY);
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
                case IMAGE_NOT_FOUND:
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_FIND_SPECIFIED_IMAGE);
            }
        } catch (RuntimeException rte) {
            auditLog(this, AuditLogType.FAILED_IMPORT_IMAGE_FROM_REPOSITORY);
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_IMPORT_IMAGE_FROM_REPOSITORY);
        }

        if (diskImage == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }
        diskImage.setStoragePoolId(getStoragePoolId());
        if (!validate(createDiskImagesValidator(diskImage).isQcowVersionSupportedForDcVersion())) {
            return false;
        }

        Guid domainStoragePoolId = getStorageDomain().getStoragePoolId();
        if (!getStoragePoolId().equals(domainStoragePoolId)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_BELONGS_TO_DIFFERENT_STORAGE_POOL,
                    String.format("$datacenter %s", domainStoragePoolId));
        }

        if (getCluster() != null) {
            Guid clusterStoragePoolId = getCluster().getStoragePoolId();
            if (!domainStoragePoolId.equals(clusterStoragePoolId)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_AND_CLUSTER_IN_DIFFERENT_POOL);
            }
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

    protected DiskImagesValidator createDiskImagesValidator(DiskImage diskImage) {
        return new DiskImagesValidator(Collections.singletonList(diskImage));
    }

    private void addAuditLogCustomValues() {
        if (getParameters().getImportAsTemplate()) {
            addCustomValue("TemplateName", getParameters().getTemplateName());
        }
    }
}
