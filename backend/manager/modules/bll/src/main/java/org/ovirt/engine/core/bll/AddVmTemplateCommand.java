package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateCommand<T extends AddVmTemplateParameters> extends VmTemplateCommand<T>
        implements QuotaStorageDependent, QuotaVdsDependent {

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;
    private final List<DiskImage> images = new ArrayList<>();
    private List<PermissionSubject> permissionCheckSubject;
    protected Map<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, List<DiskImage>> sourceImageDomainsImageMap;
    private boolean isVmInDb;
    private boolean pendingAsyncTasks;

    private static final String BASE_TEMPLATE_VERSION_NAME = "base version";
    private static Map<Guid, String> updateVmsJobIdMap = new ConcurrentHashMap<>();

    private VmTemplate cachedBaseTemplate;
    private Guid vmSnapshotId;
    private List<CinderDisk> cinderDisks;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected AddVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        T parameters = getParameters();
        super.setVmTemplateName(parameters.getName());
        VmStatic parameterMasterVm = parameters.getMasterVm();
        if (parameterMasterVm != null) {
            super.setVmId(parameterMasterVm.getId());
            setClusterId(parameterMasterVm.getClusterId());

            // API backward compatibility
            if (VmDeviceUtils.shouldOverrideSoundDevice(
                    getParameters().getMasterVm(),
                    getMasterVmCompatibilityVersion(),
                    getParameters().isSoundDeviceEnabled())) {
                parameters.setSoundDeviceEnabled(true);
            }

            if (getParameters().isSoundDeviceEnabled() == null) {
                parameters.setSoundDeviceEnabled(false);
            }

            if (getParameters().isConsoleEnabled() == null) {
                parameters.setConsoleEnabled(false);
            }
            VmHandler.updateDefaultTimeZone(parameterMasterVm);
            VmHandler.autoSelectUsbPolicy(getParameters().getMasterVm());

            VmHandler.autoSelectDefaultDisplayType(getVmId(),
                    getParameters().getMasterVm(),
                    getCluster(),
                    getParameters().getGraphicsDevices());

            VmHandler.autoSelectGraphicsDevice(getVmId(),
                    parameterMasterVm,
                    getCluster(),
                    getParameters().getGraphicsDevices(),
                    getMasterVmCompatibilityVersion());

            separateCustomProperties(parameterMasterVm);
        }
        if (getVm() != null) {
            updateVmDevices();
            images.addAll(getVmDisksFromDB());
            setStoragePoolId(getVm().getStoragePoolId());
            isVmInDb = true;
        } else if (getCluster() != null && parameterMasterVm != null) {
            VM vm = new VM(parameterMasterVm, new VmDynamic(), null);
            vm.setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            setVm(vm);
            setStoragePoolId(getCluster().getStoragePoolId());
        }
        updateDiskInfoDestinationMap();
        parameters.setUseCinderCommandCallback(!getCinderDisks().isEmpty());
    }

    private Version getMasterVmCompatibilityVersion() {
        return getVm() == null
                ? CompatibilityVersionUtils.getEffective(getParameters().getMasterVm(), getCluster())
                : getVm().getCompatibilityVersion();
    }

    protected void separateCustomProperties(VmStatic parameterMasterVm) {
        if (getCluster() != null) {
            // Parses the custom properties field that was filled by frontend to
            // predefined and user defined fields
            VmPropertiesUtils.getInstance().separateCustomPropertiesToUserAndPredefined(
                    getMasterVmCompatibilityVersion(), parameterMasterVm);
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    protected void updateDiskInfoDestinationMap() {
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<>();
        }
        sourceImageDomainsImageMap = new HashMap<>();
        for (DiskImage image : images) {
            MultiValueMapUtils.addToMap(image.getStorageIds().get(0), image, sourceImageDomainsImageMap);
            if (!diskInfoDestinationMap.containsKey(image.getId())) {
                // The volume's format and type were not specified and thus should be null.
                image.setVolumeFormat(null);
                image.setVolumeType(null);
                diskInfoDestinationMap.put(image.getId(), image);
            }
        }
    }

    protected void updateVmDevices() {
        VmDeviceUtils.setVmDevices(getVm().getStaticData());
    }

    protected List<DiskImage> getVmDisksFromDB() {
        VmHandler.updateDisksFromDb(getVm());
        VmHandler.filterImageDisksForVM(getVm());
        return getVm().getDiskList();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (isVmInDb) {
                if (pendingAsyncTasks) {
                    return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE : AuditLogType.USER_FAILED_ADD_VM_TEMPLATE;
                } else {
                    return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS : AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;
                }
            } else {
                return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_SUCCESS : AuditLogType.USER_ADD_VM_TEMPLATE_FAILURE;
            }

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_TEMPLATE_FINISHED_FAILURE;
        }
    }

    private CreateImageTemplateParameters buildChildCommandParameters(DiskImage diskImage, Guid vmSnapshotId) {
        DiskImage imageFromParams = diskInfoDestinationMap.get(diskImage.getId());
        CreateImageTemplateParameters createParams = new CreateImageTemplateParameters(diskImage.getImageId(),
                getVmTemplateId(), getVmTemplateName(), getVmId());
        createParams.setStorageDomainId(diskImage.getStorageIds().get(0));
        createParams.setVmSnapshotId(vmSnapshotId);
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setDestinationStorageDomainId(imageFromParams.getStorageIds().get(0));
        createParams.setDiskAlias(imageFromParams.getDiskAlias());
        createParams.setDescription(imageFromParams.getDiskDescription());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setQuotaId(getQuotaIdForDisk(diskImage));
        createParams.setDiskProfileId(imageFromParams.getDiskProfileId());
        createParams.setVolumeFormat(imageFromParams.getVolumeFormat());
        createParams.setVolumeType(imageFromParams.getVolumeType());
        return createParams;
    }

    @Override
    protected void executeCommand() {
        // get vm status from db to check its really down before locking
        // relevant only if template created from vm
        if (isVmInDb) {
            VmDynamic vmDynamic = DbFacade.getInstance().getVmDynamicDao().get(getVmId());
            if (!isVmStatusValid(vmDynamic.getStatus())) {
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }

            VmHandler.lockVm(vmDynamic, getCompensationContext());
        }
        setActionReturnValue(Guid.Empty);
        setVmTemplateId(Guid.newGuid());
        getParameters().setVmTemplateId(getVmTemplateId());
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));

        // set template id as base for new templates
        if (!isTemplateVersion()) {
            getParameters().setBaseTemplateId(getVmTemplateId());
            if (StringUtils.isEmpty(getParameters().getTemplateVersionName())) {
                getParameters().setTemplateVersionName(BASE_TEMPLATE_VERSION_NAME);
            }
        } else {
            // template version name should be the same as the base template name
            setVmTemplateName(getBaseTemplate().getName());
            String jobId = updateVmsJobIdMap.remove(getParameters().getBaseTemplateId());
            if (!StringUtils.isEmpty(jobId)) {
                log.info("Cancelling current running update for vms for base template id '{}'", getParameters().getBaseTemplateId());
                try {
                    getSchedulUtil().deleteJob(jobId);
                } catch (Exception e) {
                    log.warn("Failed deleting job '{}' at cancelRecoveryJob", jobId);
                }
            }
        }

        final Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = new HashMap<>();

        TransactionSupport.executeInNewTransaction(() -> {
            addVmTemplateToDb();
            getCompensationContext().stateChanged();
            return null;
        });

        if (getVm() != null && !addVmTemplateCinderDisks(srcDeviceIdToTargetDeviceIdMapping)) {
            // Error cloning Cinder disks for template
            return;
        }

        TransactionSupport.executeInNewTransaction(() -> {
            addPermission();
            addVmTemplateImages(srcDeviceIdToTargetDeviceIdMapping);
            addVmInterfaces(srcDeviceIdToTargetDeviceIdMapping);
            Set<GraphicsType> graphicsToSkip = getParameters().getGraphicsDevices().keySet();
            if (isVmInDb) {
                VmDeviceUtils.copyVmDevices(getVmId(),
                        getVmTemplateId(),
                        srcDeviceIdToTargetDeviceIdMapping,
                        getParameters().isSoundDeviceEnabled(),
                        getParameters().isConsoleEnabled(),
                        getParameters().isVirtioScsiEnabled(),
                        VmDeviceUtils.hasMemoryBalloon(getVmId()),
                        graphicsToSkip,
                        false);
            } else {
                // for instance type and new template without a VM
                VmDeviceUtils.copyVmDevices(VmTemplateHandler.BLANK_VM_TEMPLATE_ID,
                        getVmTemplateId(),
                        srcDeviceIdToTargetDeviceIdMapping,
                        getParameters().isSoundDeviceEnabled(),
                        getParameters().isConsoleEnabled(),
                        getParameters().isVirtioScsiEnabled(),
                        Boolean.TRUE.equals(getParameters().isBalloonEnabled()),
                        graphicsToSkip,
                        false);
            }

            updateWatchdog(getVmTemplateId());
            updateRngDevice(getVmTemplateId());
            addGraphicsDevice();

            setSucceeded(true);
            return null;
        });

        if (getParameters().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            VmHandler.warnMemorySizeLegal(getVmTemplate(), getVm().getCompatibilityVersion());
        }

        // means that there are no asynchronous tasks to execute and that we can
        // end the command synchronously
        pendingAsyncTasks = !getReturnValue().getVdsmTaskIdList().isEmpty() ||
                !CommandCoordinatorUtil.getChildCommandIds(getCommandId()).isEmpty();
        if (!pendingAsyncTasks) {
            endSuccessfullySynchronous();
        }
    }

    /**
     * Add graphics based on parameters.
     */
    private void addGraphicsDevice() {
        for (GraphicsDevice graphicsDevice : getParameters().getGraphicsDevices().values()) {
            if (graphicsDevice == null) {
                continue;
            }

            graphicsDevice.setVmId(getVmTemplateId());
            GraphicsParameters parameters = new GraphicsParameters(graphicsDevice).setVm(false);
            getBackend().runInternalAction(VdcActionType.AddGraphicsDevice, parameters);
        }
    }

    private boolean doClusterRelatedChecks() {
        // A Template cannot be added in a cluster without a defined architecture
        if (getCluster().getArchitecture() == ArchitectureType.undefined) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }

        if (!VmHandler.isOsTypeSupported(getParameters().getMasterVm().getOsId(),
                getCluster().getArchitecture(), getReturnValue().getValidationMessages())) {
            return false;
        }

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(getParameters().getVm().getUsbPolicy(),
                getParameters().getVm().getOs(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        // Check if the display type is supported
        Guid srcId = isVmInDb ? getVmId() : VmTemplateHandler.BLANK_VM_TEMPLATE_ID;
        if (!VmHandler.isGraphicsAndDisplaySupported(getParameters().getMasterVm().getOsId(),
                VmHandler.getResultingVmGraphics(VmDeviceUtils.getGraphicsTypesOfEntity(srcId),
                        getParameters().getGraphicsDevices()),
                getParameters().getMasterVm().getDefaultDisplayType(),
                getReturnValue().getValidationMessages(),
                getVm().getCompatibilityVersion())) {
            return false;
        }

        if (getParameters().getVm().getSingleQxlPci() &&
                !VmHandler.isSingleQxlDeviceLegal(getParameters().getVm().getDefaultDisplayType(),
                        getParameters().getVm().getOs(),
                        getReturnValue().getValidationMessages())) {
            return false;
        }

        // Check if the watchdog model is supported
        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator.VmWatchdogClusterDependentValidator(getParameters().getMasterVm().getOsId(),
                    getParameters().getWatchdog(),
                    getVm().getCompatibilityVersion()).isValid())) {
                return false;
            }
        }

        // Disallow cross-DC template creation
        if (!getStoragePoolId().equals(getCluster().getStoragePoolId())) {
            addValidationMessage(EngineMessage.VDS_CLUSTER_ON_DIFFERENT_STORAGE_POOL);
            return false;
        }

        if (!VmPropertiesUtils.getInstance().validateVmProperties(
                getVm().getCompatibilityVersion(),
                getParameters().getMasterVm().getCustomProperties(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        return imagesRelatedChecks() && AddVmCommand.checkCpuSockets(getParameters().getMasterVm().getNumOfSockets(),
                getParameters().getMasterVm().getCpuPerSocket(), getParameters().getMasterVm().getThreadsPerCpu(),
                getVm().getCompatibilityVersion().toString(), getReturnValue().getValidationMessages());
    }

    @Override
    protected boolean validate() {
        boolean isInstanceType = getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE;
        if (getCluster() == null && !isInstanceType) {
            return failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
        }

        if (!isVmPriorityValueLegal(getParameters().getMasterVm().getPriority(), getReturnValue()
                .getValidationMessages())) {
            return false;
        }

        if (isVmInDb && !isVmStatusValid(getVm().getStatus())) {
            return failValidation(EngineMessage.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
        }
        // validate uniqueness of template name. If template is a regular template, uniqueness
        // is considered in context of the datacenter. If template is an 'Instance' name must
        // be unique also across datacenters.
        if (!isTemplateVersion()) {
            if (isInstanceType) {
                if (isInstanceWithSameNameExists(getVmTemplateName())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                }
            } else {
                if (isVmTemlateWithSameNameExist(getVmTemplateName(), getCluster().getStoragePoolId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                }
            }
        }

        if (isTemplateVersion()) {
            VmTemplate userSelectedBaseTemplate = getBaseTemplate();
            if (userSelectedBaseTemplate == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            } else if (!userSelectedBaseTemplate.isBaseTemplate()) {
                // currently template version cannot be base template
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_VERSION_CANNOT_BE_BASE_TEMPLATE);

            }
        }

        if (isTemplateVersion() && getBaseTemplate().isBlank()) {
            return failValidation(EngineMessage.BLANK_TEMPLATE_CANT_HAVE_SUBTEMPLATES);
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if(!setAndValidateCpuProfile()) {
            return false;
        }

        if(!isDisksAliasNotEmpty()) {
            return false;
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        if (getParameters().getMasterVm().getSmallIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getMasterVm().getSmallIconId(), "Small"))) {
            return false;
        }

        if (getParameters().getMasterVm().getLargeIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getParameters().getMasterVm().getLargeIconId(), "Large"))) {
            return false;
        }

        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator.VmWatchdogClusterIndependentValidator(
                            getParameters().getWatchdog()).isValid())) {
                return false;
            }
        }

        if (isInstanceType) {
            return true;
        } else {
            return doClusterRelatedChecks();
        }
    }

    protected boolean isVmStatusValid(VMStatus status) {
        return status == VMStatus.Down;
    }

    protected boolean isDisksAliasNotEmpty() {
        // Check that all the template's allocated disk's aliases are not an empty string.
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            if (StringUtils.isEmpty(diskImage.getDiskAlias())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_CANNOT_BE_CREATED_WITH_EMPTY_DISK_ALIAS);
            }
        }
        return true;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                if (diskImage.getDiskStorageType() == DiskStorageType.IMAGE) {
                    map.put(diskImage, diskImage.getStorageIds().get(0));
                }
            }
            return validate(DiskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    private VmTemplate getBaseTemplate() {
        if (cachedBaseTemplate == null) {
            cachedBaseTemplate = getVmTemplateDao().get(getParameters().getBaseTemplateId());
        }
        return cachedBaseTemplate;
    }

    private boolean isTemplateVersion() {
        return getParameters().getBaseTemplateId() != null;
    }

    protected boolean imagesRelatedChecks() {
        // images related checks
        if (!images.isEmpty()) {
            if (!validateVmNotDuringSnapshot()) {
                return false;
            }

            if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
                return false;
            }

            List<CinderDisk> cinderDisks = getCinderDisks();
            CinderDisksValidator cinderDisksValidator = new CinderDisksValidator(cinderDisks);
            if (!validate(cinderDisksValidator.validateCinderDiskLimits())) {
                return false;
            }

            List<DiskImage> diskImagesToCheck = ImagesHandler.filterImageDisks(images, true, false, true);
            diskImagesToCheck.addAll(cinderDisks);
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImagesToCheck);
            if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }

            MultipleStorageDomainsValidator storageDomainsValidator =
                    getStorageDomainsValidator(getStoragePoolId(), sourceImageDomainsImageMap.keySet());
            if (!validate(storageDomainsValidator.allDomainsExistAndActive())) {
                return false;
            }

            Set<Guid> destImageDomains = getStorageGuidSet();
            destImageDomains.removeAll(sourceImageDomainsImageMap.keySet());
            for (Guid destImageDomain : destImageDomains) {
                StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                        destImageDomain, getVm().getStoragePoolId());
                if (storage == null) {
                    // if storage is null then we need to check if it doesn't exist or
                    // domain is not in the same storage pool as the vm
                    if (DbFacade.getInstance().getStorageDomainStaticDao().get(destImageDomain) == null) {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
                    } else {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_IN_STORAGE_POOL);
                    }
                    return false;
                }
                if (storage.getStatus() == null || storage.getStatus() != StorageDomainStatus.Active) {
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
                    return false;
                }

                if (storage.getStorageDomainType().isIsoOrImportExportDomain()) {

                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    return false;
                }
            }
            return validateSpaceRequirements();
        }
        return true;
    }

    protected boolean validateSpaceRequirements() {
        // update vm snapshots for storage free space check
        ImagesHandler.fillImagesBySnapshots(getVm());
        List<DiskImage>  disksList =  ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false, true);
        List<DiskImage> disksListForStorageChecks = createDiskDummiesForSpaceValidations(disksList);
        MultipleStorageDomainsValidator multipleSdValidator = getStorageDomainsValidator(
                getVm().getStoragePoolId(), getStorageGuidSet());

        return validate(multipleSdValidator.allDomainsWithinThresholds())
                && validate(multipleSdValidator.allDomainsHaveSpaceForClonedDisks(disksListForStorageChecks));
    }

    protected MultipleStorageDomainsValidator getStorageDomainsValidator(Guid spId, Set<Guid> disks) {
        return new MultipleStorageDomainsValidator(spId, disks);
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()));
    }

    private Set<Guid> getStorageGuidSet() {
        return diskInfoDestinationMap.values().stream().map(d -> d.getStorageIds().get(0)).collect(Collectors.toSet());
    }

    /**
     * Space Validations are done using data extracted from the disks. The disks in question in this command
     * don't have all the needed data, and in order not to contaminate the command's data structures, an alter
     * one is created specifically for this validation - hence dummy.
     */
    protected List<DiskImage> createDiskDummiesForSpaceValidations(Collection<DiskImage> disksList) {
        List<DiskImage> dummies = new ArrayList<>(disksList.size());
        for (DiskImage image : disksList) {
            Guid targetSdId = diskInfoDestinationMap.get(image.getId()).getStorageIds().get(0);
            DiskImage dummy = ImagesHandler.createDiskImageWithExcessData(image, targetSdId);
            dummies.add(dummy);
        }
        return dummies;
    }

    protected void addVmTemplateToDb() {
        // TODO: add timezone handling
        setVmTemplate(
                new VmTemplate(
                        0,
                        new Date(),
                        getParameters().getDescription(),
                        getParameters().getMasterVm().getComment(),
                        getParameters().getMasterVm().getMemSizeMb(),
                        getVmTemplateName(),
                        getParameters().getMasterVm().getNumOfSockets(),
                        getParameters().getMasterVm().getCpuPerSocket(),
                        getParameters().getMasterVm().getThreadsPerCpu(),
                        getParameters().getMasterVm().getOsId(),
                        getParameters().getMasterVm().getClusterId(),
                        getVmTemplateId(),
                        getParameters().getMasterVm().getNumOfMonitors(),
                        getParameters().getMasterVm().getSingleQxlPci(),
                        VmTemplateStatus.Locked.getValue(),
                        getParameters().getMasterVm().getUsbPolicy().getValue(),
                        getParameters().getMasterVm().getTimeZone(),
                        getParameters().getMasterVm().getNiceLevel(),
                        getParameters().getMasterVm().getCpuShares(),
                        getParameters().getMasterVm().isFailBack(),
                        getParameters().getMasterVm().getDefaultBootSequence(),
                        getParameters().getMasterVm().getVmType(),
                        getParameters().getMasterVm().isSmartcardEnabled(),
                        getParameters().getMasterVm().isDeleteProtected(),
                        getParameters().getMasterVm().getSsoMethod(),
                        getParameters().getMasterVm().getTunnelMigration(),
                        getParameters().getMasterVm().getVncKeyboardLayout(),
                        getParameters().getMasterVm().getMinAllocatedMem(),
                        getParameters().getMasterVm().isStateless(),
                        getParameters().getMasterVm().isRunAndPause(),
                        getUserId(),
                        getParameters().getTemplateType(),
                        getParameters().getMasterVm().isAutoStartup(),
                        getParameters().getMasterVm().getPriority(),
                        getParameters().getMasterVm().getDefaultDisplayType(),
                        getParameters().getMasterVm().getInitrdUrl(),
                        getParameters().getMasterVm().getKernelUrl(),
                        getParameters().getMasterVm().getKernelParams(),
                        getParameters().getMasterVm().getQuotaId(),
                        getParameters().getMasterVm().getDedicatedVmForVdsList(),
                        getParameters().getMasterVm().getMigrationSupport(),
                        getParameters().getMasterVm().isAllowConsoleReconnect(),
                        getParameters().getMasterVm().getIsoPath(),
                        getParameters().getMasterVm().getMigrationDowntime(),
                        getParameters().getBaseTemplateId(),
                        getParameters().getTemplateVersionName(),
                        getParameters().getMasterVm().getSerialNumberPolicy(),
                        getParameters().getMasterVm().getCustomSerialNumber(),
                        getParameters().getMasterVm().isBootMenuEnabled(),
                        getParameters().getMasterVm().isSpiceFileTransferEnabled(),
                        getParameters().getMasterVm().isSpiceCopyPasteEnabled(),
                        getParameters().getMasterVm().getCpuProfileId(),
                        getParameters().getMasterVm().getNumaTuneMode(),
                        getParameters().getMasterVm().getAutoConverge(),
                        getParameters().getMasterVm().getMigrateCompressed(),
                        getParameters().getMasterVm().getUserDefinedProperties(),
                        getParameters().getMasterVm().getPredefinedProperties(),
                        getParameters().getMasterVm().getCustomProperties(),
                        getParameters().getMasterVm().getCustomEmulatedMachine(),
                        getParameters().getMasterVm().getCustomCpuName(),
                        getParameters().getMasterVm().getSmallIconId(),
                        getParameters().getMasterVm().getLargeIconId(),
                        getParameters().getMasterVm().getNumOfIoThreads(),
                        getParameters().getMasterVm().getConsoleDisconnectAction(),
                        getParameters().getMasterVm().getCustomCompatibilityVersion(),
                        getParameters().getMasterVm().getMigrationPolicyId()));
        updateVmIcons();
        DbFacade.getInstance().getVmTemplateDao().save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        setActionReturnValue(getVmTemplate().getId());
        // Load Vm Init from DB and set it to the template
        VmHandler.updateVmInitFromDB(getParameters().getMasterVm(), false);
        getVmTemplate().setVmInit(getParameters().getMasterVm().getVmInit());
        VmHandler.addVmInitToDB(getVmTemplate());
    }

    private void updateVmIcons() {
        if (getParameters().getVmLargeIcon() != null) {
            final VmIconIdSizePair iconIdPair =
                    IconUtils.ensureIconPairInDatabase(getParameters().getVmLargeIcon());
            getVmTemplate().setSmallIconId(iconIdPair.getSmall());
            getVmTemplate().setLargeIconId(iconIdPair.getLarge());
        }
    }

    protected void addVmInterfaces(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<VmNic> interfaces = getVmNicDao().getAllForVm(getParameters().getMasterVm().getId());
        for (VmNic iface : interfaces) {
            VmNic iDynamic = new VmNic();
            iDynamic.setId(Guid.newGuid());
            iDynamic.setVmTemplateId(getVmTemplateId());
            iDynamic.setName(iface.getName());
            iDynamic.setVnicProfileId(iface.getVnicProfileId());
            iDynamic.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iDynamic.setType(iface.getType());
            iDynamic.setLinked(iface.isLinked());
            getVmNicDao().save(iDynamic);
            srcDeviceIdToTargetDeviceIdMapping.put(iface.getId(), iDynamic.getId());
        }
    }

    protected boolean addVmTemplateCinderDisks(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<CinderDisk> cinderDisks = getCinderDisks();
        if (cinderDisks.isEmpty()) {
            return true;
        }
        // Create Cinder disk templates
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk cinderDisk : cinderDisks) {
            ImagesContainterParametersBase params = buildCloneCinderDiskCommandParameters(cinderDisk);
            VdcReturnValueBase vdcReturnValueBase =
                    runInternalAction(VdcActionType.CloneSingleCinderDisk,
                            params,
                            cloneContext().withoutExecutionContext().withoutLock());
            if (!vdcReturnValueBase.getSucceeded()) {
                log.error("Error cloning Cinder disk '{}'", cinderDisk.getDiskAlias());
                getReturnValue().setFault(vdcReturnValueBase.getFault());
                return false;
            }
            Guid imageId = vdcReturnValueBase.getActionReturnValue();
            diskImageMap.put(cinderDisk.getId(), imageId);
        }
        srcDeviceIdToTargetDeviceIdMapping.putAll(diskImageMap);
        return true;
    }

    private ImagesContainterParametersBase buildCloneCinderDiskCommandParameters(CinderDisk cinderDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(cinderDisk.getImageId());
        DiskImage templateDisk = diskInfoDestinationMap.get(cinderDisk.getId());
        createParams.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setVmSnapshotId(getVmSnapshotId());
        return createParams;
    }

    protected void addVmTemplateImages(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<DiskImage> diskImages = ImagesHandler.filterImageDisks(images, true, false, true);
        for (DiskImage diskImage : diskImages) {
            addVmTemplateImage(srcDeviceIdToTargetDeviceIdMapping, diskImage);
        }
    }

    protected void addVmTemplateImage(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping, DiskImage diskImage) {
        // The return value of this action is the 'copyImage' task GUID:
        VdcReturnValueBase retValue = Backend.getInstance().runInternalAction(
                VdcActionType.CreateImageTemplate,
                buildChildCommandParameters(diskImage, Guid.newGuid()),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!retValue.getSucceeded()) {
            throw new EngineException(retValue.getFault().getError(), retValue.getFault().getMessage());
        }

        getReturnValue().getVdsmTaskIdList().addAll(retValue.getInternalVdsmTaskIdList());
        DiskImage newImage = retValue.getActionReturnValue();
        srcDeviceIdToTargetDeviceIdMapping.put(diskImage.getId(), newImage.getId());
        addTemplateDiskVmElement(newImage, diskImage);
    }


    private void addTemplateDiskVmElement(DiskImage newDisk, DiskImage oldDisk) {
        DiskVmElement oldDve = getDiskVmElementDao().get(new VmDeviceId(oldDisk.getId(), getVmId()));
        DiskVmElement newDve = DiskVmElement.copyOf(oldDve);
        newDve.setId(new VmDeviceId(newDisk.getId(), getVmTemplateId()));
        getDiskVmElementDao().save(newDve);
    }

    private Guid getVmIdFromImageParameters(){
        return getParameters().getMasterVm().getId();
    }

    @Override
    protected void endSuccessfully() {
        setVmTemplateId(getParameters().getVmTemplateId());
        setVmId(getVmIdFromImageParameters());
        isVmInDb = getVm() != null;

        getVmStaticDao().incrementDbGeneration(getVmTemplateId());
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            Backend.getInstance().endAction(p.getCommandType(),
                    p,
                    cloneContextAndDetachFromParent());
        }
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        checkTrustedService();
        setSucceeded(true);
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        logable.addCustomValue("VmTemplateName", getVmTemplateName());
        if (getVm().isTrustedService() && !getVmTemplate().isTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVm().isTrustedService() && getVmTemplate().isTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.USER_ADD_VM_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    private void endSuccessfullySynchronous() {
        if (reloadVmTemplateFromDB() != null) {
            endDefaultOperations();
        }
        setSucceeded(true);
    }

    private void endDefaultOperations() {
        endUnlockOps();

        // in case of new version of a template, update vms marked to use latest
        if (isTemplateVersion()) {
            updateVmsJobIdMap.put(getParameters().getBaseTemplateId(), StringUtils.EMPTY);
            String jobId = getSchedulUtil().scheduleAOneTimeJob(this, "updateVmVersion", new Class[0],
                    new Object[0], 0, TimeUnit.SECONDS);
            updateVmsJobIdMap.put(getParameters().getBaseTemplateId(), jobId);
        }
    }

    @OnTimerMethodAnnotation("updateVmVersion")
    public void updateVmVersion() {
        for (Guid vmId : getVmDao().getVmIdsForVersionUpdate(getParameters().getBaseTemplateId())) {
            // if the job was removed, stop executing, we probably have new version creation going on
            if (!updateVmsJobIdMap.containsKey(getParameters().getBaseTemplateId())) {
                break;
            }
            UpdateVmVersionParameters params = new UpdateVmVersionParameters(vmId);
            params.setSessionId(getParameters().getSessionId());
            getBackend().runInternalAction(VdcActionType.UpdateVmVersion, params, cloneContextAndDetachFromParent());
        }
        updateVmsJobIdMap.remove(getParameters().getBaseTemplateId());
    }

    private void endUnlockOps() {
        if (isVmInDb) {
            VmHandler.unLockVm(getVm());
        }
        VmTemplateHandler.unlockVmTemplate(getVmTemplateId());
    }

    private VmTemplate reloadVmTemplateFromDB() {
        // set it to null to reload the template from the db
        setVmTemplate(null);
        return getVmTemplate();
    }

    @Override
    protected void endWithFailure() {
        // We evaluate 'VmTemplate' so it won't be null in the last 'if'
        // statement.
        // (a template without images doesn't exist in the 'vm_template_view').
        setVmTemplateId(getParameters().getVmTemplateId());
        setVmId(getVmIdFromImageParameters());

        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(false);
            Backend.getInstance().endAction(p.getCommandType(),
                    p,
                    cloneContextAndDetachFromParent());
        }

        if (CommandCoordinatorUtil.getCommandExecutionStatus(getParameters().getCommandId()) == CommandExecutionStatus.EXECUTED) {
            // if template exist in db remove it
            if (getVmTemplate() != null) {
                DbFacade.getInstance().getVmTemplateDao().remove(getVmTemplateId());
                removeNetwork();
            }
        }

        if (!getVmId().equals(Guid.Empty) && getVm() != null) {
            VmHandler.unLockVm(getVm());
        }

        setSucceeded(true);
    }

    /**
     * in case of non-existing cluster the backend query will return a null
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionCheckSubject == null) {
            permissionCheckSubject = new ArrayList<>();
            if (getParameters().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
                Guid storagePoolId = getCluster() == null ? null : getCluster().getStoragePoolId();
                permissionCheckSubject.add(new PermissionSubject(storagePoolId,
                        VdcObjectType.StoragePool,
                        getActionType().getActionGroup()));

                // host-specific parameters can be changed by administration role only
                if (!new HashSet<>(getParameters().getMasterVm().getDedicatedVmForVdsList())
                        .equals(new HashSet<>(getVm().getDedicatedVmForVdsList()))
                        || !StringUtils.isEmpty(getParameters().getMasterVm().getCpuPinning())) {
                    permissionCheckSubject.add(
                            new PermissionSubject(storagePoolId,
                                    VdcObjectType.StoragePool,
                                    ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES));
                }
            } else {
                permissionCheckSubject.add(new PermissionSubject(Guid.SYSTEM,
                        VdcObjectType.System,
                        getActionType().getActionGroup()));
            }
        }

        return permissionCheckSubject;
    }

    private void addPermission() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        if (getCurrentUser() == null) {
            setCurrentUser(getParameters().getParametersCurrentUser());
        }
        addPermissionForTemplate(permissionsToAdd, getCurrentUser().getId(), PredefinedRoles.TEMPLATE_OWNER);
        // if the template is for public use, set EVERYONE as a TEMPLATE_USER.
        if (getParameters().isPublicUse()) {
            addPermissionForTemplate(permissionsToAdd, MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID, PredefinedRoles.TEMPLATE_USER);
        } else {
            addPermissionForTemplate(permissionsToAdd, getCurrentUser().getId(), PredefinedRoles.TEMPLATE_USER);
        }

        copyVmPermissions(permissionsToAdd);

        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            MultiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));
        }
    }

    private void copyVmPermissions(UniquePermissionsSet permissionsToAdd) {
        if (!isVmInDb || !getParameters().isCopyVmPermissions()) {
            return;
        }

        PermissionDao dao = getDbFacade().getPermissionDao();

        List<Permission> vmPermissions = dao.getAllForEntity(getVmId(), getEngineSessionSeqId(), false);

        for (Permission vmPermission : vmPermissions) {
            permissionsToAdd.addPermission(vmPermission.getAdElementId(), vmPermission.getRoleId(),
                    getParameters().getVmTemplateId(), VdcObjectType.VmTemplate);
        }

    }

    private void addPermissionForTemplate(UniquePermissionsSet permissionsToAdd, Guid userId, PredefinedRoles role) {
        permissionsToAdd.addPermission(userId, role.getId(), getParameters().getVmTemplateId(), VdcObjectType.VmTemplate);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    private Guid getQuotaIdForDisk(DiskImage diskImage) {
        // If the DiskInfoDestinationMap is available and contains information about the disk
        if (getParameters().getDiskInfoDestinationMap() != null
                && getParameters().getDiskInfoDestinationMap().get(diskImage.getId()) != null) {
            return  getParameters().getDiskInfoDestinationMap().get(diskImage.getId()).getQuotaId();
        }
        return diskImage.getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return getVm().getDiskList()
                .stream()
                .map(disk -> new QuotaStorageConsumptionParameter(
                        getQuotaIdForDisk(disk),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                        disk.getStorageIds().get(0),
                        (double) disk.getSizeInGigabytes()))
                .collect(Collectors.toList());
    }

    private Guid getQuotaId() {
        return getParameters().getMasterVm().getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(getQuotaId(), null));
        return list;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (isTemplateVersion()) {
            return Collections.singletonMap(getParameters().getBaseTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return super.getSharedLocks();
    }

    @Override
    protected boolean isQuotaDependant() {
        if (getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return false;
        }

        return super.isQuotaDependant();
    }

    protected boolean setAndValidateCpuProfile() {
        // cpu profile isn't supported for instance types.
        if (getParameters().getTemplateType() == VmEntityType.INSTANCE_TYPE) {
            return true;
        }
        return validate(CpuProfileHelper.setAndValidateCpuProfile(getParameters().getMasterVm()));
    }

    private Guid getVmSnapshotId() {
        if (vmSnapshotId == null) {
            vmSnapshotId = Guid.newGuid();
        }
        return vmSnapshotId;
    }

    private SchedulerUtil getSchedulUtil() {
        return schedulerUtil;
    }

    private List<CinderDisk> getCinderDisks() {
        if (cinderDisks == null) {
            cinderDisks = ImagesHandler.filterDisksBasedOnCinder(images);
        }
        return cinderDisks;
    }

    @Override
    public CommandCallback getCallback() {
        return getParameters().isUseCinderCommandCallback() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }
}
