package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.AffinityValidator;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AddVmParameters.Phase;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.SealVmParameters;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.CreateVm;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

/**
 * This class adds a thinly provisioned VM over a template
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmCommand<T extends AddVmParameters> extends VmManagementCommandBase<T>
        implements QuotaStorageDependent, QuotaVdsDependent, SerialChildExecutingCommand {

    private static final Base64 BASE_64 = new Base64(0, null);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private DiskProfileHelper diskProfileHelper;

    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;

    @Inject
    private NetworkHelper networkHelper;

    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;

    @Inject
    private VmValidationUtils vmValidationUtils;

    @Inject
    private CloudInitHandler cloudInitHandler;

    @Inject
    private AffinityValidator affinityValidator;

    protected Map<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, StorageDomain> destStorages = new HashMap<>();
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    private String cachedDiskSharedLockMessage;
    private Guid imageTypeId;
    private ImageType imageType;
    private Guid vmInterfacesSourceId;
    private VmTemplate vmDisksSource;
    private VmBase vmDevicesSource;
    private List<StorageDomain> poolDomains;

    private Map<Guid, Guid> srcDiskIdToTargetDiskIdMapping = new HashMap<>();
    private Map<Guid, Guid> srcVmNicIdToTargetVmNicIdMapping = new HashMap<>();

    @Inject
    private InClusterUpgradeValidator clusterUpgradeValidator;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private PermissionDao permissionDao;
    @Inject
    private DiskProfileDao diskProfileDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private VmInitDao vmInitDao;

    @Inject
    private IconUtils iconUtils;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    private BiConsumer<AuditLogable, AuditLogDirector> affinityGroupLoggingMethod = (a, b) -> {
    };

    protected AddVmCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        // if we came from endAction the VmId is not null
        setVmId(parameters.getVmId().equals(Guid.Empty) ? Guid.newGuid() : parameters.getVmId());
        setVmName(parameters.getVm().getName());
        parameters.setVmId(getVmId());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void init() {
        super.init();

        T parameters = getParameters();
        if (parameters.getVmStaticData() != null) {
            Guid templateIdToUse = getParameters().getVmStaticData().getVmtGuid();

            if (parameters.getVmStaticData().isUseLatestVersion()) {
                VmTemplate latest = vmTemplateDao.getTemplateWithLatestVersionInChain(templateIdToUse);

                if (latest != null) {
                    // if not using original template, need to override storage mappings
                    // as it may have different set of disks
                    if (!templateIdToUse.equals(latest.getId())) {
                        getParameters().setDiskInfoDestinationMap(new HashMap<>());
                    }

                    setVmTemplate(latest);
                    templateIdToUse = latest.getId();
                    getParameters().getVmStaticData().setVmtGuid(templateIdToUse);
                }
            }

            setVmTemplateId(templateIdToUse);

            // API backward compatibility
            if (getVmDeviceUtils().shouldOverrideSoundDevice(
                    getParameters().getVmStaticData(),
                    getEffectiveCompatibilityVersion(),
                    getParameters().isSoundDeviceEnabled())) {
                parameters.setSoundDeviceEnabled(true);
            }

            if (parameters.isConsoleEnabled() == null) {
                parameters.setConsoleEnabled(false);
            }

            Guid vmDevicesSourceId =
                    (getInstanceTypeId() != null) ? getInstanceTypeId() : parameters.getVmStaticData().getVmtGuid();
            vmDevicesSource = getVmBase(vmDevicesSourceId);

            if (parameters.getVmStaticData().getBiosType() == null) {
                parameters.getVmStaticData().setBiosType(vmDevicesSource.getClusterId() == null ? getCluster().getBiosType() : vmDevicesSource.getBiosType());
            }

            imageTypeId = parameters.getVmStaticData().getImageTypeId();
            vmInterfacesSourceId = parameters.getVmStaticData().getVmtGuid();
            vmDisksSource = getVmTemplate();
        }

        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

        // override values here for validate to run with correct values, has to come before init-disks
        if (isCompatibilityVersionSupportedByCluster(getEffectiveCompatibilityVersion())) {
            updateVmObject();
        }

        if (getParameters().getVmStaticData().getDefaultDisplayType() == DisplayType.none
                && !parameters.isConsoleEnabled()) {
            parameters.getVmStaticData().setUsbPolicy(UsbPolicy.DISABLED);
        }

        initTemplateDisks();
        initStoragePoolId();
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<>();
        }
        vmHandler.updateDefaultTimeZone(parameters.getVmStaticData());

        vmHandler.autoSelectResumeBehavior(getParameters().getVmStaticData());

        // Fill the migration policy if it was omitted
        if (getParameters().getVmStaticData() != null &&
                getParameters().getVmStaticData().getMigrationSupport() == null) {
            setDefaultMigrationPolicy();
        }
        if (vmDisksSource != null) {
            parameters.setUseCinderCommandCallback(!vmDisksSource.getDiskTemplateMap().isEmpty());
        }
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        if (getVmTemplate().getOrigin() == OriginType.KUBEVIRT) {
            return lockProperties.withScope(Scope.None);
        }
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM)
                                .with("VmName", getVmName())));
        for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
            locks.put(image.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskSharedLockMessage()));
        }
        if (getParameters().getPoolId() != null) {
            locks.put(getParameters().getPoolId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL,
                            new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_USED_FOR_CREATE_VM)
                                    .with("VmName", getVmName())));
        }
        return locks;
    }

    protected String getDiskSharedLockMessage() {
        if (cachedDiskSharedLockMessage == null) {
            cachedDiskSharedLockMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_USED_FOR_CREATE_VM)
                    .with("VmName", getVmName())
                    .toString();
        }
        return cachedDiskSharedLockMessage;
    }

    protected ImageType getImageType() {
        if (imageType == null && imageTypeId != null) {
            imageType = vmTemplateDao.getImageType(imageTypeId);
        }
        return imageType;
    }

    protected void initStoragePoolId() {
        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId() != null ? getCluster().getStoragePoolId()
                    : Guid.Empty);
        }
    }

    protected void initTemplateDisks() {
        if (vmDisksSource != null) {
            vmTemplateHandler.updateDisksFromDb(vmDisksSource);
        }
    }

    private Guid _vmSnapshotId = Guid.Empty;

    protected Guid getVmSnapshotId() {
        return _vmSnapshotId;
    }

    protected List<? extends VmNic> _vmInterfaces;

    protected List<? extends VmNic> getVmInterfaces() {
        if (_vmInterfaces == null) {
            List<VmNic> vmNetworkInterfaces = vmNicDao.getAllForTemplate(vmInterfacesSourceId);
            _vmInterfaces = vmNetworkInterfaces == null ? new ArrayList<>() : vmNetworkInterfaces;
        }
        return _vmInterfaces;
    }

    protected Map<Guid, VmDevice> getVmInterfaceDevices() {
        List<VmDevice> vmInterfaceDevicesList =
                vmDeviceDao.getVmDeviceByVmIdAndType(vmInterfacesSourceId, VmDeviceGeneralType.INTERFACE);
        Map<Guid, VmDevice> vmInterfaceDevices = new HashMap<>();
        for (VmDevice device : vmInterfaceDevicesList) {
            vmInterfaceDevices.put(device.getDeviceId(), device);
        }
        return vmInterfaceDevices;
    }

    private List<DiskVmElement> diskVmElements;

    protected List<DiskVmElement> getDiskVmElements() {
        if (diskVmElements == null) {
            diskVmElements = diskVmElementDao.getAllForVm(vmDisksSource.getId());
        }

        return diskVmElements;
    }

    protected boolean canAddVm(Collection<StorageDomain> destStorages) {
        VmStatic vmStaticFromParams = getParameters().getVmStaticData();
        if (!canAddVm(vmStaticFromParams, getStoragePoolId(), vmStaticFromParams.getPriority())) {
            return false;
        }

        if (!validateCustomProperties(vmStaticFromParams)) {
            return false;
        }

        // check that template image and vm are on the same storage pool
        if (shouldCheckSpaceInStorageDomains()) {
            if (!getStoragePoolId().equals(getStoragePoolIdFromSourceImageContainer())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
            }
            for (StorageDomain domain : destStorages) {
                StorageDomainValidator storageDomainValidator = new StorageDomainValidator(domain);
                if (!validate(storageDomainValidator.isDomainExistAndActive())) {
                    return false;
                }
            }
            if (!validateSpaceRequirements()) {
                return false;
            }
        }
        return validate(vmHandler.validateDedicatedVdsExistOnSameCluster(vmStaticFromParams));
    }

    protected boolean shouldCheckSpaceInStorageDomains() {
        return !getImagesToCheckDestinationStorageDomains().stream()
                .map(DiskImage::getImageId)
                .findFirst()
                .orElse(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)
                .equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
    }

    protected void setDefaultMigrationPolicy() {
        if (getCluster() != null) {
            boolean isMigrationSupported =
                    FeatureSupported.isMigrationSupported(getCluster().getArchitecture(),
                            getEffectiveCompatibilityVersion());

            MigrationSupport migrationSupport =
                    isMigrationSupported ? MigrationSupport.MIGRATABLE : MigrationSupport.PINNED_TO_HOST;

            getParameters().getVmStaticData().setMigrationSupport(migrationSupport);
        }
    }

    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return vmDisksSource.getStoragePoolId();
    }

    protected boolean validateAddVmCommand() {
        return areParametersLegal()
                && checkNumberOfMonitors()
                && validate(VmValidator.checkPciAndIdeLimit(getParameters().getVm().getOs(),
                        getEffectiveCompatibilityVersion(),
                        getParameters().getVmStaticData().getNumOfMonitors(),
                        getVmInterfaces(),
                        getDiskVmElements(),
                        isVirtioScsiEnabled(),
                        hasWatchdog(),
                        isSoundDeviceEnabled()))
                && canAddVm(destStorages.values())
                && hostToRunExist();
    }

    /**
     * Check if destination storage has enough space
     */
    protected boolean validateSpaceRequirements() {
        for (Map.Entry<Guid, List<DiskImage>> sdImageEntry : storageToDisksMap.entrySet()) {
            StorageDomain destStorageDomain = destStorages.get(sdImageEntry.getKey());
            List<DiskImage> disksList = sdImageEntry.getValue();
            StorageDomainValidator storageDomainValidator = createStorageDomainValidator(destStorageDomain);
            if (!validateDomainsThreshold(storageDomainValidator) ||
                    !validateFreeSpace(storageDomainValidator, disksList)) {
                return false;
            }
        }
        return true;
    }

    protected StorageDomainValidator createStorageDomainValidator(StorageDomain storageDomain) {
        return new StorageDomainValidator(storageDomain);
    }

    protected boolean validateDomainsThreshold(StorageDomainValidator storageDomainValidator) {
        return validate(storageDomainValidator.isDomainWithinThresholds());
    }

    /**
     * This validation is for thin provisioning, when done differently on other commands, this method should be
     * overridden.
     */
    protected boolean validateFreeSpace(StorageDomainValidator storageDomainValidator, List<DiskImage> disksList) {
        Collection<DiskImage> disks = ImagesHandler.getDisksDummiesForStorageAllocations(disksList);
        return validate(storageDomainValidator.hasSpaceForNewDisks(disks));
    }

    protected boolean hostToRunExist() {
        List<Guid> dedicatedHostsList = getParameters().getVmStaticData().getDedicatedVmForVdsList();
        if (dedicatedHostsList.isEmpty()) {
            return true;
        }
        for (Guid candidateHostGuid : dedicatedHostsList) {
            if (vdsDao.get(candidateHostGuid) == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
            }
        }
        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateVm.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected boolean validate() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getVmTemplate().isDisabled()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_DISABLED);
        }

        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        if (!validate(spValidator.exists())) {
            return false;
        }

        if (!isExternalVM() && !validate(spValidator.isInStatus(StoragePoolStatus.Up))) {
            return false;
        }

        if (!isTemplateInValidDc()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_EXISTS_IN_CURRENT_DC);
        }

        if (!isDisksVolumeFormatValid()) {
            return false;
        }

        Version customCompatibilityVersionFromParams =
                getParameters().getVmStaticData().getCustomCompatibilityVersion();
        if (customCompatibilityVersionFromParams != null
                && !isCompatibilityVersionSupportedByCluster(customCompatibilityVersionFromParams)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CUSTOM_COMPATIBILITY_VERSION_NOT_SUPPORTED,
                    String.format("$Ccv %s", customCompatibilityVersionFromParams));
        }

        // A VM cannot be added in a cluster without a defined architecture
        if (getCluster().getArchitecture() == ArchitectureType.undefined) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }

        if (verifySourceDomains() && buildAndCheckDestStorageDomains()) {
            chooseDisksSourceDomains();
        } else {
            return false;
        }

        if (isSoundDeviceEnabled() && !osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        if (isTpmEnabled()
                && !getVmDeviceUtils().isTpmDeviceSupported(getParameters().getVmStaticData(), getCluster())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.TPM_DEVICE_REQUESTED_ON_NOT_SUPPORTED_PLATFORM);
        }

        if (!isTpmEnabled() && osRepository.requiresTpm(getParameters().getVmStaticData().getOsId())) {
            return failValidation(EngineMessage.TPM_DEVICE_REQUIRED_BY_OS);
        }

        if (!validateQuota(getParameters().getVmStaticData().getQuotaId())) {
            return false;
        }

        // otherwise..
        storageToDisksMap =
                ImagesHandler.buildStorageToDiskMap(getImagesToCheckDestinationStorageDomains(),
                        diskInfoDestinationMap);

        if (!validateAddVmCommand()) {
            return false;
        }

        VM vmFromParams = getParameters().getVm();

        // check if the selected template is compatible with Cluster architecture.
        if (!getVmTemplate().getId().equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)
                && getCluster().getArchitecture() != getVmTemplate().getClusterArch()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_INCOMPATIBLE);
        }

        if (StringUtils.isEmpty(vmFromParams.getName())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        }

        // check that VM name is not too long
        if (!isVmNameValidLength(vmFromParams)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }

        // check for Vm Payload
        if (getParameters().getVmPayload() != null) {
            if (!checkPayload(getParameters().getVmPayload())) {
                return false;
            }

            // otherwise, we save the content in base64 string
            for (Map.Entry<String, String> entry : getParameters().getVmPayload().getFiles().entrySet()) {
                entry.setValue(new String(BASE_64.encode(entry.getValue().getBytes()), StandardCharsets.UTF_8));
            }
        }

        // check for Vm Watchdog Model
        if (getParameters().getWatchdog() != null) {
            if (!validate(new VmWatchdogValidator(vmFromParams.getOs(),
                    getParameters().getWatchdog(),
                    getEffectiveCompatibilityVersion()).isValid())) {
                return false;
            }
        }

        // check if the OS type is supported
        if (!validate(vmHandler.isOsTypeSupported(vmFromParams.getOs(), getCluster().getArchitecture()))) {
            return false;
        }

        if (vmFromParams.getCustomCpuName() == null && !validate(vmHandler.isCpuSupported(
                vmFromParams.getVmOsId(),
                getEffectiveCompatibilityVersion(),
                getCluster().getCpuName()))) {
            return false;
        }

        // Check if the graphics and display from parameters are supported
        if (!validate(vmHandler.isGraphicsAndDisplaySupported(getParameters().getVmStaticData().getOsId(),
                vmHandler.getResultingVmGraphics(
                        getVmDeviceUtils().getGraphicsTypesOfEntity(getVmTemplateId()),
                        getParameters().getGraphicsDevices()),
                vmFromParams.getDefaultDisplayType(),
                vmFromParams.getBiosType(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        if (!validate(vmHandler.validateSmartCardDevice(getParameters().getVmStaticData()))) {
            return false;
        }

        if (!FeatureSupported.isMigrationSupported(getCluster().getArchitecture(), getEffectiveCompatibilityVersion())
                && vmFromParams.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failValidation(EngineMessage.VM_MIGRATION_IS_NOT_SUPPORTED);
        }

        // check cpuPinning if the check haven't failed yet
        if (!validate(isCpuPinningValid(vmFromParams.getCpuPinning(), vmFromParams.getStaticData()))) {
            return false;
        }

        if (vmFromParams.isUseHostCpuFlags() && (ArchitectureType.ppc == getCluster().getArchitecture().getFamily())) {
            return failValidation(EngineMessage.USE_HOST_CPU_REQUESTED_ON_UNSUPPORTED_ARCH);
        }

        if (!validateMemoryAlignment(getParameters().getVmStaticData())) {
            return false;
        }

        if (getInstanceTypeId() != null && getInstanceType() == null) {
            // invalid instance type
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INSTANCE_TYPE_DOES_NOT_EXIST);
        }

        if (imageTypeId != null && getImageType() == null) {
            // invalid image type
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_TYPE_DOES_NOT_EXIST);
        }

        if (!validate(VmValidator.validateCpuSockets(getParameters().getVmStaticData(),
                getEffectiveCompatibilityVersion(),
                getCluster().getArchitecture(),
                osRepository))) {
            return false;
        }

        if (!isCpuSharesValid(vmFromParams)) {
            return failValidation(EngineMessage.QOS_CPU_SHARES_OUT_OF_RANGE);
        }

        if (!VmCpuCountHelper.validateCpuCounts(vmFromParams, getEffectiveCompatibilityVersion(),
                getCluster().getArchitecture())) {
            return failValidation(EngineMessage.TOO_MANY_CPU_COMPONENTS);
        }

        if (Boolean.TRUE.equals(getParameters().isVirtioScsiEnabled())) {
            // Verify OS compatibility
            if (!validate(vmHandler.isOsTypeSupportedForVirtioScsi(vmFromParams.getOs(),
                    getEffectiveCompatibilityVersion()))) {
                return false;
            }
        }

        if (vmFromParams.getMinAllocatedMem() > vmFromParams.getMemSizeMb()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MIN_MEMORY_CANNOT_EXCEED_MEMORY_SIZE);
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        if (getVmId() != null && vmStaticDao.get(getVmId()) != null) {
            return failValidation(EngineMessage.VM_ID_EXISTS);
        }

        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(diskInfoDestinationMap.values());
        CinderDisksValidator cinderDisksValidator = new CinderDisksValidator(cinderDisks);
        if (!validate(cinderDisksValidator.validateCinderDiskLimits())) {
            return false;
        }

        if (getParameters().getVmLargeIcon() != null && !validate(IconValidator.validate(
                IconValidator.DimensionsType.LARGE_CUSTOM_ICON,
                getParameters().getVmLargeIcon()))) {
            return false;
        }

        if (getSmallIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getSmallIconId(), "Small"))) {
            return false;
        }

        if (getLargeIconId() != null
                && getParameters().getVmLargeIcon() == null // icon id is ignored if large icon is sent
                && !validate(IconValidator.validateIconId(getLargeIconId(), "Large"))) {
            return false;
        }

        if (!validate(getNumaValidator().checkVmNumaNodesIntegrity(
                getParameters().getVm(),
                getParameters().getVm().getvNumaNodeList()))) {
            return false;
        }

        if (getCluster().isInUpgradeMode()) {
            getParameters().getVm().setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            if (!validate(getClusterUpgradeValidator().isVmReadyForUpgrade(getParameters().getVm()))) {
                return false;
            }
        }

        if (!validate(vmHandler.validateMaxMemorySize(
                getParameters().getVmStaticData(),
                getEffectiveCompatibilityVersion()))) {
            return false;
        }

        if (shouldAddLease(getParameters().getVmStaticData()) && !canAddLease()) {
            return false;
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getParameters().getVmStaticData().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        if (vmDevicesSource.getClusterId() == null && (getCluster().getBiosType() == null)) {
            return failValidation(EngineMessage.CLUSTER_BIOS_TYPE_NOT_SET);
        }

        VmValidator vmValidator = new VmValidator(vmFromParams);

        if (!validate(vmValidator.isBiosTypeSupported(getCluster(), osRepository))) {
            return false;
        }

        if (!validate(validateAffinityGroups())) {
            return false;
        }
        if (!validate(vmHandler.validateCpuPinningPolicy(getParameters().getVmStaticData(),
                getParameters().isUpdateNuma()))) {
            return false;
        }
        return true;
    }

    protected boolean isDisksVolumeFormatValid() {
        if (diskInfoDestinationMap.values()
                .stream()
                .anyMatch(d -> d.getDiskStorageType() != DiskStorageType.CINDER &&
                        d.getVolumeFormat() != VolumeFormat.COW)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_THIN_TEMPLATE_DISKS_SHOULD_ONLY_BE_COW);
        }
        return true;
    }

    private boolean isExternalVM() {
        return getParameters().getVmStaticData().getOrigin() == OriginType.EXTERNAL;
    }

    private Guid getSmallIconId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getSmallIconId();
        }
        return null;
    }

    private Guid getLargeIconId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getLargeIconId();
        }
        return null;
    }

    private boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = new HashMap<>();
            List<DiskImage> diskImages = DisksFilter.filterImageDisks(diskInfoDestinationMap.values(),
                    ONLY_NOT_SHAREABLE,
                    ONLY_ACTIVE);
            for (DiskImage diskImage : diskImages) {
                map.put(diskImage, diskImage.getStorageIds().get(0));
            }
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    protected boolean checkTemplateImages() {
        if (getParameters().getParentCommand() == ActionType.AddVmPool) {
            return true;
        }

        for (StorageDomain storage : destStorages.values()) {
            if (!validate(vmTemplateHandler.isVmTemplateImagesReady(vmDisksSource,
                    storage.getId(),
                    false,
                    false,
                    true,
                    true,
                    storageToDisksMap.get(storage.getId())))) {
                return false;
            }
        }
        return true;
    }

    protected boolean buildAndCheckDestStorageDomains() {
        if (diskInfoDestinationMap.isEmpty()) {
            if (!fillDestMap()) {
                return false;
            }
        } else if (!validateProvidedDestinations()) {
            return false;
        }
        if (getImagesToCheckDestinationStorageDomains().size() != diskInfoDestinationMap.size()) {
            log.error("Can not find any default active domain for one of the disks of template with id '{}'",
                    vmDisksSource.getId());
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
        }

        return validateIsImagesOnDomains();
    }

    protected boolean verifySourceDomains() {
        return true;
    }

    protected void chooseDisksSourceDomains() {
    }

    protected Collection<DiskImage> getImagesToCheckDestinationStorageDomains() {
        return vmDisksSource.getDiskTemplateMap().values();
    }

    private boolean validateProvidedDestinations() {
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            if (diskImage.getStorageIds() == null || diskImage.getStorageIds().isEmpty()) {
                diskImage.setStorageIds(new ArrayList<>());
                diskImage.getStorageIds().add(getParameters().getStorageDomainId());
            }
            Guid storageDomainId = diskImage.getStorageIds().get(0);
            if (destStorages.get(storageDomainId) == null) {
                StorageDomain storage = storageDomainDao.getForStoragePool(storageDomainId, getStoragePoolId());
                StorageDomainValidator validator =
                        new StorageDomainValidator(storage);
                if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
                    return false;
                }
                destStorages.put(storage.getId(), storage);
            }
        }
        return true;
    }

    private boolean fillDestMap() {
        if (getParameters().getStorageDomainId() != null
                && !Guid.Empty.equals(getParameters().getStorageDomainId())) {
            Guid storageId = getParameters().getStorageDomainId();
            for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
                diskInfoDestinationMap.put(image.getId(), makeNewImage(storageId, image));
            }
            return validateProvidedDestinations();
        }
        fillImagesMapBasedOnTemplate();
        return true;
    }

    protected List<StorageDomain> getPoolDomains() {
        if (poolDomains == null) {
            poolDomains = storageDomainDao.getAllForStoragePool(vmDisksSource.getStoragePoolId());
        }
        return poolDomains;
    }

    private void fillImagesMapBasedOnTemplate() {
        ImagesHandler.fillImagesMapBasedOnTemplate(vmDisksSource,
                getPoolDomains(),
                diskInfoDestinationMap,
                destStorages);
    }

    protected boolean validateIsImagesOnDomains() {
        for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
            if (!image.getStorageIds().containsAll(diskInfoDestinationMap.get(image.getId()).getStorageIds())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return true;
    }

    private DiskImage makeNewImage(Guid storageId, DiskImage image) {
        DiskImage newImage = new DiskImage();
        newImage.setImageId(image.getImageId());
        newImage.setDiskAlias(image.getDiskAlias());
        newImage.setVolumeFormat(image.getVolumeFormat());
        newImage.setVolumeType(image.getVolumeType());
        ArrayList<Guid> storageIds = new ArrayList<>();
        storageIds.add(storageId);
        newImage.setStorageIds(storageIds);
        newImage.setQuotaId(image.getQuotaId());

        // Find out the correct disk profile for storage domain
        newImage.setDiskProfileId(diskProfileDao.getAllForStorageDomain(storageId)
                .stream()
                .filter(p -> image.getDiskProfileIds().contains(p.getId()))
                .findFirst()
                .map(p -> p.getId())
                .orElse(null));

        return newImage;
    }

    protected boolean canAddVm(VmStatic vm, Guid storagePoolId, int vmPriority) {
        // Checking if a desktop with same name already exists
        if (isVmWithSameNameExists(vm, storagePoolId)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        if (!validate(vmHandler.verifyMacPool(getVmInterfaces().size(), getMacPool()))) {
            return false;
        }

        if (!validate(vmHandler.isVmPriorityValueLegal(vmPriority))) {
            return false;
        }

        if (!checkTemplateImages()) {
            return false;
        }

        return true;
    }

    @Override
    protected void executeVmCommand() {
        vmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getEffectiveCompatibilityVersion());

        if (getActionType() != ActionType.CloneVmNoCollapse && !canAddVm(destStorages.values())) {
            log.error("Failed to add VM. The reasons are: {}",
                    String.join(",", getReturnValue().getValidationMessages()));
            return;
        }

        if (!addVmLease(getParameters().getVm().getLeaseStorageDomainId(), getVmId(), false)) {
            return;
        }

        TransactionSupport.executeInNewTransaction(() -> {
            addVmStatic();
            addVmDynamic();
            addVmNetwork();
            addVmNumaNodes();
            addVmStatistics();
            addActiveSnapshot();
            addVmPermission();
            addVmInit();
            addVmRngDevice();
            addAffinityGroupsAndLabels();
            getCompensationContext().stateChanged();
            return null;
        });

        addVmImages();

        TransactionSupport.executeInNewTransaction(() -> {
            copyDiskVmElements();
            copyVmDevices();
            addDiskPermissions();
            addVmPayload();
            updateSmartCardDevices();
            addVmWatchdog();
            addGraphicsDevice();
            getVmDeviceUtils().updateVirtioScsiController(getVm().getStaticData(),
                    getParameters().isVirtioScsiEnabled());
            updateVmDevicesOnChipsetChange();
            getVmDeviceUtils().copyVmExternalData(getVmTemplateId(), getVmId());
            return null;
        });

        if (getParameters().getPoolId() != null && !addVmToPool()) {
            log.error("Error adding VM {} to Pool {}", getVmId(), getParameters().getPoolId());
            return;
        }

        discardHelper.logIfDisksWithIllegalPassDiscardExist(getVmId());
        setActionReturnValue(getVm().getId());
        setSucceeded(true);
    }

    /**
     * After the copy of the images, copy the properties of the disk VM elements of the source disks to new disk VM
     * elements for the created destination disks and save them
     */
    private void copyDiskVmElements() {
        for (Map.Entry<Guid, Guid> srcToDst : getSrcDiskIdToTargetDiskIdMapping().entrySet()) {
            getImagesToCheckDestinationStorageDomains().stream()
                    .filter(d -> d.getId().equals(srcToDst.getKey()))
                    .findFirst()
                    .map(d -> d.getDiskVmElementForVm(getSourceVmId()))
                    .ifPresent(srcDve -> createAndSaveNewDiskVmElement(srcToDst.getValue(), getVmId(), srcDve));
        }
    }

    protected Guid getSourceVmId() {
        return getVmTemplateId();
    }

    private void addGraphicsDevice() {
        for (GraphicsDevice graphicsDevice : getParameters().getGraphicsDevices().values()) {
            if (graphicsDevice == null) {
                continue;
            }

            graphicsDevice.setVmId(getVmId());
            backend.runInternalAction(ActionType.AddGraphicsDevice, new GraphicsParameters(graphicsDevice));
        }
    }

    private void updateSmartCardDevices() {
        // if vm smartcard settings is different from device source's
        // add or remove the smartcard according to user request
        boolean smartcardOnDeviceSource = getInstanceTypeId() != null ? getInstanceType().isSmartcardEnabled()
                : getVmTemplate().isSmartcardEnabled();
        if (getVm().isSmartcardEnabled() != smartcardOnDeviceSource) {
            getVmDeviceUtils().updateSmartcardDevice(getVm().getId(), getVm().isSmartcardEnabled());
        }
    }

    private void addVmWatchdog() {
        VmWatchdog vmWatchdog = getParameters().getWatchdog();
        if (vmWatchdog != null) {
            ActionType actionType = getVmDeviceUtils().hasWatchdog(getSourceVmId()) ? ActionType.UpdateWatchdog
                    : ActionType.AddWatchdog;
            runInternalAction(
                    actionType,
                    buildWatchdogParameters(vmWatchdog),
                    cloneContextAndDetachFromParent());
        }
    }

    private WatchdogParameters buildWatchdogParameters(VmWatchdog vmWatchdog) {
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(getVmId());
        parameters.setAction(vmWatchdog.getAction());
        parameters.setModel(vmWatchdog.getModel());
        return parameters;
    }

    private void addVmRngDevice() {
        VmRngDevice rngDev = getParameters().getRngDevice();
        if (rngDev != null) {
            rngDev.setVmId(getVmId());
            RngDeviceParameters params = new RngDeviceParameters(rngDev, true);
            ActionReturnValue result =
                    runInternalAction(ActionType.AddRngDevice, params, cloneContextAndDetachFromParent());
            if (!result.getSucceeded()) {
                log.error("Couldn't add RNG device for new VM.");
                throw new IllegalArgumentException("Couldn't add RNG device for new VM.");
            }
        }
    }

    private void addVmPayload() {
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null) {
            getVmDeviceUtils().addManagedDevice(new VmDeviceId(Guid.newGuid(), getVmId()),
                    VmDeviceGeneralType.DISK,
                    payload.getDeviceType(),
                    payload.getSpecParams(),
                    true,
                    true);
        }
    }

    protected void copyVmDevices() {
        getVmDeviceUtils().copyVmDevices(vmDevicesSource.getId(),
                getVmId(),
                getSrcDeviceIdToTargetDeviceIdMapping(),
                isSoundDeviceEnabled(),
                getParameters().isTpmEnabled(),
                getParameters().isConsoleEnabled(),
                isVirtioScsiEnabled(),
                getParameters().getGraphicsDevices().keySet(),
                false,
                getEffectiveCompatibilityVersion());

        if (getInstanceTypeId() != null) {
            copyDiskDevicesFromTemplate();
        }
    }

    /**
     * If both the instance type and the template is set, than all the devices has to be copied from instance type
     * except the disk devices which has to be copied from the template (since the instance type has no disks but the
     * template does have).
     */
    private void copyDiskDevicesFromTemplate() {
        List<VmDevice> disks =
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(vmDisksSource.getId(),
                        VmDeviceGeneralType.DISK,
                        VmDeviceType.DISK);
        getVmDeviceUtils().copyDiskDevices(
                getVmId(),
                disks,
                getSrcDeviceIdToTargetDeviceIdMapping());
    }

    private void updateVmDevicesOnChipsetChange() {
        if (isChipsetChanged()) {
            log.info(
                    "BIOS chipset type of source VM/template ({}) is different than BIOS chipset type of destination VM/template ({}), the disks and devices will be converted to the new BIOS chipset type.",
                    vmDevicesSource.getId(),
                    getVm().getId());
            getVmHandler().convertVmToNewChipset(getVmId(),
                    getParameters().getVmStaticData().getBiosType().getChipsetType(),
                    getCompensationContextIfEnabledByCaller());
        }
    }

    private boolean isChipsetChanged() {
        if (vmDevicesSource.getClusterId() == null) {
            return false;
        }
        return getParameters().getVmStaticData().getBiosType().getChipsetType() != vmDevicesSource
                .getBiosType()
                .getChipsetType();
    }

    private boolean isLegalClusterId(Guid clusterId) {
        // check given cluster id
        Cluster cluster = clusterDao.get(clusterId);
        if (cluster == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
        }
        return true;
    }

    protected boolean areParametersLegal() {
        final VmStatic vmStaticData = getParameters().getVmStaticData();

        if (vmStaticData != null) {

            if (!isLegalClusterId(vmStaticData.getClusterId())) {
                return false;
            }
        }
        return true;
    }

    protected void addVmNetwork() {
        List<? extends VmNic> nics = getVmInterfaces();
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(getMacPool());
        vmInterfaceManager.sortVmNics(nics, getVmInterfaceDevices());

        List<String> macAddresses = getMacPool().allocateMacAddresses(nics.size());

        // Add interfaces from template
        for (int i = 0; i < nics.size(); ++i) {
            VmNic iface = nics.get(i);
            Guid id = Guid.newGuid();
            srcVmNicIdToTargetVmNicIdMapping.put(iface.getId(), id);
            iface.setId(id);
            iface.setMacAddress(macAddresses.get(i));
            iface.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iface.setVmId(getParameters().getVmStaticData().getId());
            updateProfileOnNic(iface);
            vmNicDao.save(iface);
            getCompensationContext().snapshotNewEntity(iface);
            vmNetworkStatisticsDao.save(iface.getStatistics());
            getCompensationContext().snapshotNewEntity(iface.getStatistics());
        }
    }

    private void addVmNumaNodes() {
        List<VmNumaNode> numaNodes = getParameters().getVm().getvNumaNodeList();
        if (numaNodes.isEmpty()) {
            return;
        }
        VmNumaNodeOperationParameters params = new VmNumaNodeOperationParameters(getParameters().getVm(), numaNodes);

        ActionReturnValue returnValueBase = backend.runInternalAction(ActionType.AddVmNumaNodes, params);
        if (!returnValueBase.getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.NUMA_ADD_VM_NUMA_NODE_FAILED);
        }
    }

    private void addVmInit() {
        final VmInit vmInit = getParameters().getVmStaticData().getVmInit();
        if (vmInit == null) {
            return;
        }
        if (vmInit.isPasswordAlreadyStored()) {
            final VmInit originalVmInit = loadOriginalVmInitWithRootPassword();
            if (originalVmInit != null) {
                vmInit.setPasswordAlreadyStored(false);
                vmInit.setRootPassword(originalVmInit.getRootPassword());
            }
        }
        vmHandler.addVmInitToDB(vmInit);
    }

    protected VmInit loadOriginalVmInitWithRootPassword() {
        return vmInitDao.get(getVmTemplateId());
    }

    private void addVmStatic() {
        VmStatic vmStatic = getParameters().getVmStaticData();

        if (vmStatic.getOrigin() == null) {
            vmStatic.setOrigin(OriginType.valueOf(Config.getValue(ConfigValues.OriginType)));
        }
        vmStatic.setId(getVmId());
        vmStatic.setQuotaId(getQuotaId());
        if (vmStatic.isManaged()) {
            vmStatic.setCreationDate(new Date());
            vmStatic.setCreatedByUserId(getUserId());
            updateOriginalTemplate(vmStatic);
        }

        setIconIds(vmStatic);
        // Parses the custom properties field that was filled by frontend to
        // predefined and user defined fields
        VmPropertiesUtils.getInstance()
                .separateCustomPropertiesToUserAndPredefined(
                        getEffectiveCompatibilityVersion(),
                        vmStatic);

        vmStaticDao.save(vmStatic);
        getCompensationContext().snapshotNewEntity(vmStatic);
    }

    protected void updateOriginalTemplate(VmStatic vmStatic) {
        vmStatic.setOriginalTemplateGuid(vmStatic.getVmtGuid());
        vmStatic.setOriginalTemplateName(getVmTemplate().getName());
    }

    private void addVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(getVmId());
        vmDynamic.setStatus(VMStatus.Down);
        vmDynamic.setVmHost("");
        vmDynamic.setIp("");
        vmDynamic.setFqdn("");
        vmDynamic.setLastStopTime(new Date());
        vmDynamicDao.save(vmDynamic);
        getCompensationContext().snapshotNewEntity(vmDynamic);
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics(getVmId());
        vmStatisticsDao.save(stats);
        getCompensationContext().snapshotNewEntity(stats);
    }

    protected void addVmImages() {
        if (!vmDisksSource.getDiskTemplateMap().isEmpty()) {
            if (getVm().getStatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }
            lockVM();
            Collection<DiskImage> templateDisks = getImagesToCheckDestinationStorageDomains();
            List<DiskImage> diskImages = DisksFilter.filterImageDisks(templateDisks,
                    ONLY_NOT_SHAREABLE,
                    ONLY_ACTIVE);
            for (DiskImage image : diskImages) {
                ActionReturnValue result = runInternalActionWithTasksContext(
                        getDiskCreationCommandType(),
                        buildDiskCreationParameters(image));

                // if couldn't create snapshot then stop the transaction and the command
                if (!result.getSucceeded()) {
                    throw new EngineException(result.getFault().getError());
                } else {
                    getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
                    DiskImage newImage = result.getActionReturnValue();
                    getParameters().getSrcDiskIdToTargetDiskIdMapping().put(image.getId(), newImage.getId());
                }
            }

            // Clone volumes for Cinder and Managed block disks templates
            addVmCinderDisks(templateDisks);
            addManagedBlockDisks(templateDisks);
        }
    }

    protected ActionType getDiskCreationCommandType() {
        return ActionType.CreateSnapshotFromTemplate;
    }

    protected void lockVM() {
        vmHandler.lockVm(getVmId());
    }

    protected CreateSnapshotFromTemplateParameters buildDiskCreationParameters(DiskImage image) {
        CreateSnapshotFromTemplateParameters tempVar = new CreateSnapshotFromTemplateParameters(
                image.getImageId(),
                getParameters().getVmStaticData().getId());
        tempVar.setDestStorageDomainId(diskInfoDestinationMap.get(image.getId()).getStorageIds().get(0));
        tempVar.setDiskAlias(diskInfoDestinationMap.get(image.getId()).getDiskAlias());
        tempVar.setStorageDomainId(image.getStorageIds().get(0));
        tempVar.setVmSnapshotId(getVmSnapshotId());
        tempVar.setParentCommand(ActionType.AddVm);
        tempVar.setEntityInfo(getParameters().getEntityInfo());
        tempVar.setParentParameters(getParameters());
        tempVar.setQuotaId(diskInfoDestinationMap.get(image.getId()).getQuotaId());
        tempVar.setDiskProfileId(diskInfoDestinationMap.get(image.getId()).getDiskProfileId());

        return tempVar;
    }

    private void createAndSaveNewDiskVmElement(Guid newDiskImageId, Guid newVmId, DiskVmElement oldDve) {
        DiskVmElement newDve = DiskVmElement.copyOf(oldDve, newDiskImageId, newVmId);
        diskVmElementDao.save(newDve);
    }

    protected void addVmCinderDisks(Collection<DiskImage> templateDisks) {
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(templateDisks);
        if (cinderDisks.isEmpty()) {
            return;
        }
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk cinderDisk : cinderDisks) {
            ImagesContainterParametersBase params = buildImagesContainterParameters(cinderDisk);
            ActionReturnValue actionReturnValue = runInternalAction(
                    ActionType.CloneSingleCinderDisk,
                    params,
                    cloneContext().withoutExecutionContext().withoutLock());
            if (!actionReturnValue.getSucceeded()) {
                log.error("Error cloning Cinder disk '{}': {}", cinderDisk.getDiskAlias());
                getReturnValue().setFault(actionReturnValue.getFault());
                return;
            }
            Guid imageId = actionReturnValue.getActionReturnValue();

            diskImageMap.put(cinderDisk.getId(), imageId);
        }
        srcDiskIdToTargetDiskIdMapping.putAll(diskImageMap);
    }

    protected void addManagedBlockDisks(Collection<DiskImage> templateDisks) {
        List<ManagedBlockStorageDisk> managedBlockDisks = DisksFilter.filterManagedBlockStorageDisks(templateDisks);
        if (managedBlockDisks.isEmpty()) {
            return;
        }
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (ManagedBlockStorageDisk managedBlockDisk : managedBlockDisks) {
            ImagesContainterParametersBase params = buildImagesContainterParameters(managedBlockDisk);
            ActionReturnValue actionReturnValue = runInternalAction(
                    ActionType.CloneSingleManagedBlockDisk,
                    params,
                    cloneContext().withoutExecutionContext().withoutLock());
            if (!actionReturnValue.getSucceeded()) {
                log.error("Error cloning Managed block disk '{}': {}", managedBlockDisk.getDiskAlias());
                getReturnValue().setFault(actionReturnValue.getFault());
                return;
            }
            Guid imageId = actionReturnValue.getActionReturnValue();
            diskImageMap.put(managedBlockDisk.getId(), imageId);
        }
        srcDiskIdToTargetDiskIdMapping.putAll(diskImageMap);
    }

    private ImagesContainterParametersBase buildImagesContainterParameters(DiskImage srcDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(srcDisk.getImageId());
        DiskImage templateDisk = diskInfoDestinationMap.get(srcDisk.getId());
        createParams.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setVmSnapshotId(getVmSnapshotId());
        return createParams;
    }

    private boolean addVmToPool() {
        AddVmToPoolParameters parameters = new AddVmToPoolParameters(getParameters().getPoolId(), getVmId());
        parameters.setShouldBeLogged(false);
        ActionReturnValue result = runInternalActionWithTasksContext(
                ActionType.AddVmToPool,
                parameters);
        if (!result.getSucceeded()) {
            getReturnValue().setFault(result.getFault());
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? !getReturnValue().getVdsmTaskIdList().isEmpty() ? AuditLogType.USER_ADD_VM_STARTED
                    : AuditLogType.USER_ADD_VM : AuditLogType.USER_FAILED_ADD_VM;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_ADD_VM_FINISHED_SUCCESS
                    : AuditLogType.USER_ADD_VM_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_ADD_VM_FINISHED_FAILURE;
        }
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.CreateSnapshotFromTemplate;
    }

    @Override
    protected void endWithFailure() {
        super.endActionOnDisks();
        removeVmRelatedEntitiesFromDb();
        setSucceeded(true);
    }

    protected void removeVmRelatedEntitiesFromDb() {
        removeVmUsers();
        removeVmNetwork();
        // Note that currently newly added vm never have memory state
        // In case it will be changed (clone vm from snapshot will clone the memory state),
        // we'll need to remove the memory state images here as well.
        removeVmSnapshots();
        removeVmStatic();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        if (getVmTemplate() != null && !getVmTemplate().getDiskList().isEmpty()) {
            permissionList.addAll(getParameters().getDiskInfoDestinationMap()
                    .values()
                    .stream()
                    .filter(disk -> disk.getStorageIds() != null && !disk.getStorageIds().isEmpty())
                    .map(disk -> new PermissionSubject(disk.getStorageIds().get(0),
                            VdcObjectType.Storage,
                            ActionGroup.CREATE_DISK))
                    .collect(Collectors.toList()));
        }

        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }

    /**
     * user need permission on each object used: template, instance type, image type.
     */
    @Override
    protected boolean checkPermissions(final List<PermissionSubject> permSubjects) {

        if (getInstanceTypeId() != null && !checkInstanceTypeImagePermissions(getInstanceTypeId())) {
            return false;
        }

        if (imageTypeId != null && !checkInstanceTypeImagePermissions(imageTypeId)) {
            return false;
        }

        for (PermissionSubject permSubject : permSubjects) {
            // if user is using instance type, then create_instance may be sufficient
            if (getInstanceTypeId() != null && checkCreateInstancePermission(permSubject)) {
                continue;
            }

            // create_vm is overriding in case no create_instance, try again with it
            if (!checkSinglePermission(permSubject, getReturnValue().getValidationMessages())) {
                logMissingPermission(permSubject);
                return false;
            }
        }
        return true;
    }

    /**
     * To create a vm either {@link ActionGroup#CREATE_VM} or {@link ActionGroup#CREATE_INSTANCE} permissions is
     * required for selected {@link VdcObjectType}s. However {@link #getPermissionCheckSubjects()} returns only
     * {@link ActionGroup#CREATE_VM} based permissions subjects. This method helps to mitigate this problem.
     *
     * @param permSubject
     *            permission subject
     * @return true if {@link ActionGroup#CREATE_INSTANCE} based permission is sufficient, false otherwise
     */
    private boolean checkCreateInstancePermission(PermissionSubject permSubject) {
        final List<VdcObjectType> overriddenPermissionObjectTypes = Arrays.asList(
                VdcObjectType.Cluster,
                VdcObjectType.VmTemplate);
        final boolean instanceCreateObjectType = overriddenPermissionObjectTypes.contains(permSubject.getObjectType());
        if (!instanceCreateObjectType) {
            return false;
        }
        final PermissionSubject alteredPermissionSubject = new PermissionSubject(permSubject.getObjectId(),
                permSubject.getObjectType(),
                ActionGroup.CREATE_INSTANCE,
                permSubject.getMessage());
        return checkSinglePermission(alteredPermissionSubject, getReturnValue().getValidationMessages());
    }

    /**
     * If using an instance type/image the user needs to have either CREATE_INSTANCE or the specific
     * getActionType().getActionGroup() on the instance type/image
     */
    private boolean checkInstanceTypeImagePermissions(Guid id) {
        Collection<String> createInstanceMessages = new ArrayList<>();
        Collection<String> actionGroupMessages = new ArrayList<>();

        PermissionSubject createInstanceSubject =
                new PermissionSubject(id, VdcObjectType.VmTemplate, ActionGroup.CREATE_INSTANCE);
        PermissionSubject actionGroupSubject =
                new PermissionSubject(id, VdcObjectType.VmTemplate, getActionType().getActionGroup());

        // it is enough if at least one of this two permissions are there
        if (!checkSinglePermission(createInstanceSubject, createInstanceMessages) &&
                !checkSinglePermission(actionGroupSubject, actionGroupMessages)) {
            getReturnValue().getValidationMessages().addAll(actionGroupMessages);
            return false;
        }

        return true;
    }

    protected void addPermissionSubjectForAdminLevelProperties(List<PermissionSubject> permissionList) {
        VmStatic vmFromParams = getParameters().getVmStaticData();
        VmTemplate vmTemplate = getVmTemplate();

        if (vmFromParams != null && vmTemplate != null) {
            // user needs specific permission to change custom properties
            if (!Objects.equals(vmFromParams.getCustomProperties(), vmTemplate.getCustomProperties())) {
                permissionList.add(new PermissionSubject(getClusterId(),
                        VdcObjectType.Cluster,
                        ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
            }
            // if the template is blank we ignore his pinned hosts
            if (vmTemplate.isBlank()) {
                return;
            }
            Set<Guid> dedicatedVmForVdsFromUser = new HashSet<>(vmFromParams.getDedicatedVmForVdsList());
            Set<Guid> dedicatedVmForVdsFromTemplate = new HashSet<>(vmTemplate.getDedicatedVmForVdsList());
            // host-specific parameters can be changed by administration role only
            if (!dedicatedVmForVdsFromUser.equals(dedicatedVmForVdsFromTemplate)
                    || !StringUtils.isEmpty(vmFromParams.getCpuPinning())) {
                permissionList.add(new PermissionSubject(getClusterId(),
                        VdcObjectType.Cluster,
                        ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }
    }

    private void addVmPermission() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        if (isMakeCreatorExplicitOwner()) {
            permissionsToAdd.addPermission(getCurrentUser().getId(),
                    PredefinedRoles.VM_OPERATOR.getId(),
                    getVmId(),
                    VdcObjectType.VM);
        }

        if (getParameters().isCopyTemplatePermissions()
                && !getVmTemplateId().equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)) {
            copyTemplatePermissions(permissionsToAdd);
        }

        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            multiLevelAdministrationHandler
                    .addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));

            getCompensationContext().snapshotNewEntities(permissionsList);
        }
    }

    private boolean isMakeCreatorExplicitOwner() {
        return getParameters().isMakeCreatorExplicitOwner()
                || (getCurrentUser() != null && getParameters().getPoolId() == null
                        && !checkUserAuthorization(getCurrentUser().getId(),
                                ActionGroup.MANIPULATE_PERMISSIONS,
                                getVmId(),
                                VdcObjectType.VM));
    }

    private void copyTemplatePermissions(UniquePermissionsSet permissionsToAdd) {
        List<Permission> templatePermissions =
                permissionDao.getAllForEntity(getVmTemplateId(), getEngineSessionSeqId(), false);

        for (Permission templatePermission : templatePermissions) {
            boolean templateOwnerRole = templatePermission.getRoleId().equals(PredefinedRoles.TEMPLATE_OWNER.getId());
            boolean templateUserRole = templatePermission.getRoleId().equals(PredefinedRoles.TEMPLATE_USER.getId());

            if (templateOwnerRole || templateUserRole) {
                continue;
            }

            permissionsToAdd.addPermission(templatePermission.getAdElementId(),
                    templatePermission.getRoleId(),
                    getVmId(),
                    VdcObjectType.VM);
        }

    }

    private void addDiskPermissions() {
        List<Guid> newDiskImageIds = new ArrayList<>(srcDiskIdToTargetDiskIdMapping.values());
        Permission[] permsArray = new Permission[newDiskImageIds.size()];

        for (int i = 0; i < newDiskImageIds.size(); i++) {
            permsArray[i] =
                    new Permission(getUserIdOfDiskOperator(),
                            PredefinedRoles.DISK_OPERATOR.getId(),
                            newDiskImageIds.get(i),
                            VdcObjectType.Disk);
        }
        multiLevelAdministrationHandler.addPermission(permsArray);
    }

    private Guid getUserIdOfDiskOperator() {
        Guid diskOperatorIdFromParams = getParameters().getDiskOperatorAuthzPrincipalDbId();
        return diskOperatorIdFromParams != null ? diskOperatorIdFromParams : getCurrentUser().getId();
    }

    private void addActiveSnapshot() {
        // We already have an active snapshot if we are cloning with snapshots
        if (getActionType() != ActionType.CloneVmNoCollapse) {
            _vmSnapshotId = Guid.newGuid();
            getSnapshotsManager().addActiveSnapshot(_vmSnapshotId,
                    getVm(),
                    Snapshot.SnapshotStatus.OK,
                    null,
                    null,
                    getCompensationContext());
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!StringUtils.isBlank(getParameters().getVm().getName())) {
            return Collections.singletonMap(getParameters().getVm().getName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
        }
        return jobProperties;
    }

    private Guid getQuotaId() {
        return getQuotaManager().getFirstQuotaForUser(
                getParameters().getVmStaticData().getQuotaId(),
                getStoragePoolId(),
                getCurrentUser());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return diskInfoDestinationMap.values()
                .stream()
                .map(disk -> new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                        disk.getStorageIds().get(0),
                        (double) disk.getSizeInGigabytes()))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(getQuotaId()));
        return list;
    }

    public Map<Guid, Guid> getSrcDiskIdToTargetDiskIdMapping() {
        return getParameters().getSrcDiskIdToTargetDiskIdMapping();
    }

    public Map<Guid, Guid> getSrcDeviceIdToTargetDeviceIdMapping() {
        Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = new HashMap<>();
        srcDeviceIdToTargetDeviceIdMapping.putAll(srcVmNicIdToTargetVmNicIdMapping);
        srcDeviceIdToTargetDeviceIdMapping.putAll(getParameters().getSrcDiskIdToTargetDiskIdMapping());
        return srcDeviceIdToTargetDeviceIdMapping;
    }

    protected boolean isVirtioScsiEnabled() {
        Boolean virtioScsiEnabled = getParameters().isVirtioScsiEnabled();
        boolean isOsSupportedForVirtIoScsi = vmValidationUtils.isDiskInterfaceSupportedByOs(
                getParameters().getVm().getOs(),
                getEffectiveCompatibilityVersion(),
                getParameters().getVmStaticData().getBiosType().getChipsetType(),
                DiskInterface.VirtIO_SCSI);

        return virtioScsiEnabled != null ? virtioScsiEnabled : isOsSupportedForVirtIoScsi;
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled
                : osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                        getEffectiveCompatibilityVersion());
    }

    protected boolean isTpmEnabled() {
        Boolean tpmEnabled = getParameters().isTpmEnabled();
        return tpmEnabled != null ? tpmEnabled : false;
    }

    private boolean hasWatchdog() {
        return getParameters().getWatchdog() != null;
    }

    protected boolean isVirtioScsiControllerAttached(Guid vmId) {
        return getVmDeviceUtils().hasVirtioScsiController(vmId);
    }

    /**
     * This method override vm values with the instance type values in case instance type is selected for this vm
     */
    private void updateVmObject() {
        updateParametersVmFromInstanceType();

        // set vm interface source id to be the instance type, vm interface are taken from it
        if (getInstanceType() != null) {
            vmInterfacesSourceId = getInstanceTypeId();
        }

        VmStatic vmStatic = getParameters().getVmStaticData();
        ImageType imageType = getImageType();
        if (imageType != null) {
            vmStatic.setOsId(imageType.getOsId());
            vmStatic.setIsoPath(imageType.getIsoPath());
            vmStatic.setInitrdUrl(imageType.getInitrdUrl());
            vmStatic.setKernelUrl(imageType.getKernelUrl());
            vmStatic.setKernelParams(imageType.getKernelParams());
            // set vm disks source to be the image type, vm disks are taken from it
            vmDisksSource = (VmTemplate) imageType;
        }

        // Choose a proper default OS according to the cluster architecture
        if (getParameters().getVmStaticData().getOsId() == OsRepository.AUTO_SELECT_OS) {
            if (getCluster().getArchitecture() != ArchitectureType.undefined) {
                Integer defaultOs = osRepository.getDefaultOSes().get(getCluster().getArchitecture());

                getParameters().getVmStaticData().setOsId(defaultOs);
            }
        }

        vmHandler.autoSelectUsbPolicy(getParameters().getVmStaticData());

        // Choose a proper default display type according to the cluster architecture
        vmHandler.autoSelectDefaultDisplayType(vmDevicesSource.getId(),
                getParameters().getVmStaticData(),
                getCluster(),
                getParameters().getGraphicsDevices());

        // If not set by user, choose proper graphics device according to the cluster architecture
        vmHandler.autoSelectGraphicsDevice(vmDevicesSource.getId(),
                getParameters().getVmStaticData(),
                getCluster(),
                getParameters().getGraphicsDevices(),
                getEffectiveCompatibilityVersion());
    }

    private boolean isTemplateInValidDc() {
        return VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVmTemplateId())
                || getVmTemplate().getStoragePoolId().equals(getStoragePoolId());
    }

    private void updateProfileOnNic(VmNic iface) {
        Network network = networkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
        if (network != null && !networkHelper.isNetworkInCluster(network, getClusterId())) {
            iface.setVnicProfileId(null);
        }
    }

    protected boolean checkNumberOfMonitors() {
        if (getParameters().getVmStaticData().getDefaultDisplayType() == DisplayType.none) {
            return true;
        }
        Collection<GraphicsType> graphicsTypes = vmHandler.getResultingVmGraphics(
                getVmDeviceUtils().getGraphicsTypesOfEntity(getVmTemplateId()),
                getParameters().getGraphicsDevices());
        int numOfMonitors = getParameters().getVmStaticData().getNumOfMonitors();

        return validate(vmHandler.isNumOfMonitorsLegal(graphicsTypes, numOfMonitors));
    }

    /**
     * Icon processing policy:
     * <ul>
     * <li>If there is an attached icon, it is used as large icon as base for computation of small icon. Predefined
     * icons should not be sent in parameters.</li>
     * <li>If there are no icon in parameters && both (small and large) icon ids are set then those ids are used.</li>
     * <li>Otherwise (at least one icon id is null) both icon ids are copied from template.</li>
     * </ul>
     */
    private void setIconIds(VmStatic vmStatic) {
        if (getParameters().getVmLargeIcon() != null) {
            final VmIconIdSizePair iconIds = iconUtils.ensureIconPairInDatabase(getParameters().getVmLargeIcon());
            vmStatic.setLargeIconId(iconIds.getLarge());
            vmStatic.setSmallIconId(iconIds.getSmall());
        } else {
            if (vmStatic.getLargeIconId() == null
                    || vmStatic.getSmallIconId() == null) {
                vmStatic.setSmallIconId(getVmTemplate().getSmallIconId());
                vmStatic.setLargeIconId(getVmTemplate().getLargeIconId());
            }
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    private InClusterUpgradeValidator getClusterUpgradeValidator() {
        return clusterUpgradeValidator;
    }

    private ValidationResult validateAffinityGroups() {
        AffinityValidator.Result result = affinityValidator.validateAffinityUpdateForVm(getClusterId(),
                getVmId(),
                getParameters().getAffinityGroups(),
                getParameters().getAffinityLabels());

        affinityGroupLoggingMethod = result.getLoggingMethod();
        return result.getValidationResult();
    }

    private void addAffinityGroupsAndLabels() {
        // TODO - check permissions to modify affinity groups
        List<AffinityGroup> affinityGroups = getParameters().getAffinityGroups();
        if (affinityGroups != null) {
            affinityGroupLoggingMethod.accept(this, auditLogDirector);
            affinityGroupDao.setAffinityGroupsForVm(getVmId(),
                    affinityGroups.stream()
                            .map(AffinityGroup::getId)
                            .collect(Collectors.toList()));
        }

        // TODO - check permissions to modify labels
        List<Label> affinityLabels = getParameters().getAffinityLabels();
        if (affinityLabels != null) {
            List<Guid> labelIds = affinityLabels.stream()
                    .map(Label::getId)
                    .collect(Collectors.toList());
            labelDao.addVmToLabels(getVmId(), labelIds);
        }
    }

    VmBase getVmBase(Guid vmId) {
        VmStatic vmStatic = vmStaticDao.get(vmId);
        return vmStatic != null ? vmStatic : vmTemplateDao.get(vmId);
    }

    private boolean shouldSealVm() {
        return getParameters().getSeal() != null ? getParameters().getSeal()
                : getVmTemplate().isSealed() && !osRepository.isWindows(getVm().getOs())
                        && getParameters().getPoolId() == null;
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (!shouldSealVm()) {
            return false;
        }

        switch (getParameters().getPhase()) {
            case CREATE_VM:
                getParameters().setPhase(Phase.SEAL);
                break;

            case SEAL:
                return false;
        }
        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    private void executeNextOperation() {
        switch (getParameters().getPhase()) {
            case SEAL:
                sealVm();
                break;
        }
    }

    private void sealVm() {
        ActionReturnValue returnValue = runInternalAction(ActionType.SealVm,
                buildSealVmParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }
    }

    private SealVmParameters buildSealVmParameters() {
        SealVmParameters parameters = new SealVmParameters();
        parameters.setVmId(getVmId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

}
