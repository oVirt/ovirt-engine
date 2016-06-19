package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.validator.CpuPinningValidator.isCpuPinningValid;

import java.nio.charset.Charset;
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
import java.util.stream.Collectors;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.numa.vm.NumaValidator;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaSanityParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.IconValidator;
import org.ovirt.engine.core.bll.validator.InClusterUpgradeValidator;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AddVmToPoolParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotFromTemplateParameters;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmNumaNodeOperationParameters;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.ImageType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.validation.group.CreateVm;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class adds a thinly provisioned VM over a template
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmCommand<T extends AddVmParameters> extends VmManagementCommandBase<T>
        implements QuotaStorageDependent, QuotaVdsDependent {

    private static final Base64 BASE_64 = new Base64(0, null);
    protected HashMap<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, StorageDomain> destStorages = new HashMap<>();
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    private String cachedDiskSharedLockMessage;
    protected Guid imageTypeId;
    protected ImageType imageType;
    private Guid vmInterfacesSourceId;
    protected VmTemplate vmDisksSource;
    private Guid vmDevicesSourceId;
    private List<StorageDomain> poolDomains;

    private Map<Guid, Guid> srcDiskIdToTargetDiskIdMapping = new HashMap<>();
    private Map<Guid, Guid> srcVmNicIdToTargetVmNicIdMapping = new HashMap<>();
    private MacPool macPool;

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
                VmTemplate latest = getVmTemplateDao().getTemplateWithLatestVersionInChain(templateIdToUse);

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
            if (VmDeviceUtils.shouldOverrideSoundDevice(
                    getParameters().getVmStaticData(),
                    getEffectiveCompatibilityVersion(),
                    getParameters().isSoundDeviceEnabled())) {
                parameters.setSoundDeviceEnabled(true);
            }

            if (parameters.isConsoleEnabled() == null) {
                parameters.setConsoleEnabled(false);
            }

            vmDevicesSourceId = (getInstanceTypeId() != null) ?
                    getInstanceTypeId() : parameters.getVmStaticData().getVmtGuid();
            imageTypeId = parameters.getVmStaticData().getImageTypeId();
            vmInterfacesSourceId = parameters.getVmStaticData().getVmtGuid();
            vmDisksSource = getVmTemplate();
        }

        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

        // override values here for validate to run with correct values, has to come before init-disks
        updateVmObject();

        initTemplateDisks();
        initStoragePoolId();
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<>();
        }
        VmHandler.updateDefaultTimeZone(parameters.getVmStaticData());

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
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateSharedLockMessage()));
        for (DiskImage image: getImagesToCheckDestinationStorageDomains()) {
            locks.put(image.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskSharedLockMessage()));
        }
        if (getParameters().getPoolId() != null) {
            locks.put(getParameters().getPoolId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_POOL, getPoolSharedLockMessage()));
        }
        return locks;
    }

    private String getTemplateSharedLockMessage() {
        return new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_USED_FOR_CREATE_VM.name())
                .append(String.format("$VmName %1$s", getVmName()))
                .toString();
    }

    protected String getDiskSharedLockMessage() {
        if (cachedDiskSharedLockMessage == null) {
            cachedDiskSharedLockMessage = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_USED_FOR_CREATE_VM.name())
            .append(String.format("$VmName %1$s", getVmName()))
            .toString();
        }
        return cachedDiskSharedLockMessage;
    }

    private String getPoolSharedLockMessage() {
        return new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_POOL_IS_USED_FOR_CREATE_VM.name())
                .append(String.format("$VmName %1$s", getVmName()))
                .toString();
    }

    protected ImageType getImageType() {
        if (imageType == null && imageTypeId != null) {
            imageType = getVmTemplateDao().getImageType(imageTypeId);
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
            VmTemplateHandler.updateDisksFromDb(vmDisksSource);
        }
    }

    private Guid _vmSnapshotId = Guid.Empty;

    protected Guid getVmSnapshotId() {
        return _vmSnapshotId;
    }

    protected List<? extends VmNic> _vmInterfaces;

    protected List<? extends VmNic> getVmInterfaces() {
        if (_vmInterfaces == null) {
            List<VmNic> vmNetworkInterfaces = getVmNicDao().getAllForTemplate(vmInterfacesSourceId);
            _vmInterfaces = vmNetworkInterfaces == null ? new ArrayList<>() : vmNetworkInterfaces;
        }
        return _vmInterfaces;
    }

    protected Map<Guid, VmDevice> getVmInterfaceDevices() {
        List<VmDevice> vmInterfaceDevicesList = getVmDeviceDao().getVmDeviceByVmIdAndType(vmInterfacesSourceId, VmDeviceGeneralType.INTERFACE);
        Map<Guid, VmDevice> vmInterfaceDevices = new HashMap<>();
        for (VmDevice device : vmInterfaceDevicesList) {
            vmInterfaceDevices.put(device.getDeviceId(), device);
        }
        return vmInterfaceDevices;
    }

    private List<DiskVmElement> diskVmElements;

    protected List<DiskVmElement> getDiskVmElements() {
        if (diskVmElements == null) {
            diskVmElements = DbFacade.getInstance().getDiskVmElementDao()
                            .getAllForVm(vmDisksSource.getId());
        }

        return diskVmElements;
    }

    protected boolean canAddVm(List<String> reasons, Collection<StorageDomain> destStorages) {
        VmStatic vmStaticFromParams = getParameters().getVmStaticData();
        if (!canAddVm(reasons, vmStaticFromParams.getName(), getStoragePoolId(), vmStaticFromParams.getPriority())) {
            return false;
        }

        if (!validateCustomProperties(vmStaticFromParams, reasons)) {
            return false;
        }

        // check that template image and vm are on the same storage pool
        if (shouldCheckSpaceInStorageDomains()) {
            if (!getStoragePoolId().equals(getStoragePoolIdFromSourceImageContainer())) {
                reasons.add(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH.toString());
                return false;
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
        return VmHandler.validateDedicatedVdsExistOnSameCluster(vmStaticFromParams,
                getReturnValue().getValidationMessages());
    }

    protected boolean shouldCheckSpaceInStorageDomains() {
        return !getImagesToCheckDestinationStorageDomains().stream().map(DiskImage::getImageId)
                .findFirst().orElse(VmTemplateHandler.BLANK_VM_TEMPLATE_ID).equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
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
        return areParametersLegal(getReturnValue().getValidationMessages())
                && checkNumberOfMonitors() && checkSingleQxlDisplay()
                && checkPciAndIdeLimit(getParameters().getVm().getOs(),
                        getEffectiveCompatibilityVersion(),
                        getParameters().getVmStaticData().getNumOfMonitors(),
                        getVmInterfaces(),
                        getDiskVmElements(),
                        isVirtioScsiEnabled(),
                        hasWatchdog(),
                        isBalloonEnabled(),
                        isSoundDeviceEnabled(),
                        getReturnValue().getValidationMessages())
                && canAddVm(getReturnValue().getValidationMessages(), destStorages.values())
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

    private boolean validateDomainsThreshold(StorageDomainValidator storageDomainValidator) {
        return validate(storageDomainValidator.isDomainWithinThresholds());
    }

    /**
     * This validation is for thin provisioning, when done differently on other commands, this method should be overridden.
     */
    protected boolean validateFreeSpace(StorageDomainValidator storageDomainValidator, List<DiskImage> disksList) {
        Collection<DiskImage> disks = ImagesHandler.getDisksDummiesForStorageAllocations(disksList);
        return validate(storageDomainValidator.hasSpaceForNewDisks(disks));
    }

    protected boolean checkSingleQxlDisplay() {
        if (!getParameters().getVmStaticData().getSingleQxlPci()) {
            return true;
        }
        return VmHandler.isSingleQxlDeviceLegal(getParameters().getVm().getDefaultDisplayType(),
                        getParameters().getVm().getOs(),
                        getReturnValue().getValidationMessages());
    }

    protected boolean hostToRunExist() {
        List<Guid> dedicatedHostsList = getParameters().getVmStaticData().getDedicatedVmForVdsList();
        if (dedicatedHostsList.isEmpty()){
            return true;
        }
        for (Guid candidateHostGuid : dedicatedHostsList) {
            if (DbFacade.getInstance().getVdsDao().get(candidateHostGuid) == null) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
                return false;
            }
        }
        return true;
    }

    public static boolean checkCpuSockets(int num_of_sockets, int cpu_per_socket, int threadsPerCpu,
                                          String compatibility_version, List<String> validationMessages) {
        if ((num_of_sockets * cpu_per_socket * threadsPerCpu) >
                Config.<Integer> getValue(ConfigValues.MaxNumOfVmCpus, compatibility_version)) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_CPU.toString());
            return false;
        }
        if (num_of_sockets > Config.<Integer> getValue(ConfigValues.MaxNumOfVmSockets, compatibility_version)) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MAX_NUM_SOCKETS.toString());
            return false;
        }
        if (cpu_per_socket > Config.<Integer> getValue(ConfigValues.MaxNumOfCpuPerSocket, compatibility_version)) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MAX_CPU_PER_SOCKET.toString());
            return false;
        }
        if (threadsPerCpu > Config.<Integer> getValue(ConfigValues.MaxNumOfThreadsPerCpu, compatibility_version)) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MAX_THREADS_PER_CPU.toString());
            return false;
        }
        if (cpu_per_socket < 1) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MIN_CPU_PER_SOCKET.toString());
            return false;
        }
        if (num_of_sockets < 1) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MIN_NUM_SOCKETS.toString());
            return false;
        }
        if (threadsPerCpu < 1) {
            validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_MIN_THREADS_PER_CPU.toString());
            return false;
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
        macPool = getMacPool();
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getVmTemplate().isDisabled()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_DISABLED);
        }

        if (getStoragePool() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        if (!isExternalVM() && getStoragePool().getStatus() != StoragePoolStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
        }

        if (!isTemplateInValidDc()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_EXISTS_IN_CURRENT_DC);
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

        if (isBalloonEnabled() && !osRepository.isBalloonEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        if (isSoundDeviceEnabled() && !osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
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
            if (!checkPayload(getParameters().getVmPayload(),
                    getParameters().getVmStaticData().getIsoPath())) {
                return false;
            }

            // otherwise, we save the content in base64 string
            for (Map.Entry<String, String> entry : getParameters().getVmPayload().getFiles().entrySet()) {
                entry.setValue(new String(BASE_64.encode(entry.getValue().getBytes()), Charset.forName(CharEncoding.UTF_8)));
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

        // Check that the USB policy is legal
        if (!VmHandler.isUsbPolicyLegal(vmFromParams.getUsbPolicy(), vmFromParams.getOs(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        // check if the OS type is supported
        if (!VmHandler.isOsTypeSupported(vmFromParams.getOs(), getCluster().getArchitecture(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        if (!VmHandler.isCpuSupported(
                vmFromParams.getVmOsId(),
                getEffectiveCompatibilityVersion(),
                getCluster().getCpuName(),
                getReturnValue().getValidationMessages())) {
            return false;
        }

        // Check if the graphics and display from parameters are supported
        if (!VmHandler.isGraphicsAndDisplaySupported(getParameters().getVmStaticData().getOsId(),
                VmHandler.getResultingVmGraphics(VmDeviceUtils.getGraphicsTypesOfEntity(getVmTemplateId()), getParameters().getGraphicsDevices()),
                vmFromParams.getDefaultDisplayType(),
                getReturnValue().getValidationMessages(),
                getEffectiveCompatibilityVersion())) {
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

        if (vmFromParams.isUseHostCpuFlags()
                && vmFromParams.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            return failValidation(EngineMessage.VM_HOSTCPU_MUST_BE_PINNED_TO_HOST);
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

        if (!checkCpuSockets()){
            return false;
        }

        if (!isCpuSharesValid(vmFromParams)) {
            return failValidation(EngineMessage.QOS_CPU_SHARES_OUT_OF_RANGE);
        }

        if (Boolean.TRUE.equals(getParameters().isVirtioScsiEnabled())) {
            // Verify OS compatibility
            if (!VmHandler.isOsTypeSupportedForVirtioScsi(vmFromParams.getOs(), getEffectiveCompatibilityVersion(),
                    getReturnValue().getValidationMessages())) {
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

        if (getVmId() != null && getVmStaticDao().get(getVmId()) != null) {
            return failValidation(EngineMessage.VM_ID_EXISTS);
        }

        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(diskInfoDestinationMap.values());
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

        if (!validate(NumaValidator.checkVmNumaNodesIntegrity(getParameters().getVm(), getParameters().getVm().getvNumaNodeList()))) {
            return false;
        }

        if (getCluster().isInUpgradeMode()) {
            getParameters().getVm().setClusterCompatibilityVersion(getCluster().getCompatibilityVersion());
            if (!validate(Injector.get(InClusterUpgradeValidator.class)
                    .isVmReadyForUpgrade(getParameters().getVm()))) {
                return false;
            }
        }

        return true;
    }

    private boolean isExternalVM() {
        return getParameters().getVmStaticData().getOrigin() == OriginType.EXTERNAL;
    }

    protected Guid getSmallIconId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getSmallIconId();
        }
        return null;
    }

    protected Guid getLargeIconId() {
        if (getParameters().getVmStaticData() != null) {
            return getParameters().getVmStaticData().getLargeIconId();
        }
        return null;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = new HashMap<>();
            List<DiskImage> diskImages = ImagesHandler.filterImageDisks(diskInfoDestinationMap.values(), true, false, true);
            for (DiskImage diskImage : diskImages) {
                map.put(diskImage, diskImage.getStorageIds().get(0));
            }
            return validate(DiskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    protected boolean checkTemplateImages(List<String> reasons) {
        if (getParameters().getParentCommand() == VdcActionType.AddVmPoolWithVms) {
            return true;
        }

        for (StorageDomain storage : destStorages.values()) {
            if (!VmTemplateCommand.isVmTemplateImagesReady(vmDisksSource, storage.getId(),
                    reasons, false, false, true, true,
                    storageToDisksMap.get(storage.getId()))) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkCpuSockets() {
        return AddVmCommand.checkCpuSockets(getParameters().getVmStaticData().getNumOfSockets(),
                getParameters().getVmStaticData().getCpuPerSocket(), getParameters().getVmStaticData().getThreadsPerCpu(),
                getEffectiveCompatibilityVersion().toString(), getReturnValue().getValidationMessages());
    }

    protected boolean buildAndCheckDestStorageDomains() {
        boolean retValue;
        if (diskInfoDestinationMap.isEmpty()) {
            retValue = fillDestMap();
        } else {
            retValue = validateProvidedDestinations();
        }
        if (retValue && getImagesToCheckDestinationStorageDomains().size() != diskInfoDestinationMap.size()) {
            log.error("Can not find any default active domain for one of the disks of template with id '{}'",
                    vmDisksSource.getId());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            retValue = false;
        }

        return retValue && validateIsImagesOnDomains();
    }

    protected boolean verifySourceDomains() {
        return true;
    }

    protected void chooseDisksSourceDomains() {}

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
                StorageDomain storage = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                        storageDomainId, getStoragePoolId());
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
            poolDomains = getStorageDomainDao().getAllForStoragePool(vmDisksSource.getStoragePoolId());
        }
        return poolDomains;
    }

    protected void fillImagesMapBasedOnTemplate() {
        ImagesHandler.fillImagesMapBasedOnTemplate(vmDisksSource,
                getPoolDomains(),
                diskInfoDestinationMap,
                destStorages);
    }

    protected boolean validateIsImagesOnDomains() {
        for (DiskImage image : getImagesToCheckDestinationStorageDomains()) {
            if (!image.getStorageIds().containsAll(diskInfoDestinationMap.get(image.getId()).getStorageIds())) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
                return false;
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
        newImage.setDiskProfileId(image.getDiskProfileId());
        return newImage;
    }

    protected boolean canAddVm(List<String> reasons, String name, Guid storagePoolId,
            int vmPriority) {
        // Checking if a desktop with same name already exists
        if (isVmWithSameNameExists(name, storagePoolId)) {
            reasons.add(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED.name());
            return false;
        }

        if (!verifyAddVM(reasons, vmPriority)) {
            return false;
        }

        if (!checkTemplateImages(reasons)) {
            return false;
        }

        return true;
    }

    protected boolean verifyAddVM(List<String> reasons, int vmPriority) {
        return VmHandler.verifyAddVm(reasons, getVmInterfaces().size(), vmPriority, macPool);
    }

    @Override
    protected void executeVmCommand() {
        VmHandler.warnMemorySizeLegal(getParameters().getVm().getStaticData(), getEffectiveCompatibilityVersion());

        List<String> errorMessages = new ArrayList<>();
        if (!canAddVm(errorMessages, destStorages.values())) {
            log.error("Failed to add VM. The reasons are: {}", String.join(",", errorMessages));
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
            getCompensationContext().stateChanged();
            return null;
        });

        if (addVmImages()) {
            TransactionSupport.executeInNewTransaction(() -> {
                copyDiskVmElements();
                copyVmDevices();
                addDiskPermissions();
                addVmPayload();
                updateSmartCardDevices();
                addVmWatchdog();
                addGraphicsDevice();
                setActionReturnValue(getVm().getId());
                setSucceeded(true);
                return null;
            });
        }

        if (getParameters().getPoolId() != null) {
            addVmToPool();
        }
    }

    /**
     * After the copy of the images, copy the properties of the disk VM elements of the source disks to new
     * disk VM elements for the created destination disks and save them
     */
    protected void copyDiskVmElements() {
        for (Map.Entry<Guid, Guid> srcToDst : getSrcDiskIdToTargetDiskIdMapping().entrySet()) {
            DiskVmElement srcDve = getImagesToCheckDestinationStorageDomains().
                    stream().
                    filter(d -> d.getId().equals(srcToDst.getKey())).
                    findFirst().
                    get().
                    getDiskVmElementForVm(getSourceVmId());
            createAndSaveNewDiskVmElement(srcToDst.getValue(), getVmId(), srcDve);
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
            getBackend().runInternalAction(VdcActionType.AddGraphicsDevice, new GraphicsParameters(graphicsDevice));
        }
    }

    private void updateSmartCardDevices() {
        // if vm smartcard settings is different from device source's
        // add or remove the smartcard according to user request
        boolean smartcardOnDeviceSource = getInstanceTypeId() != null ? getInstanceType().isSmartcardEnabled() : getVmTemplate().isSmartcardEnabled();
        if (getVm().isSmartcardEnabled() != smartcardOnDeviceSource) {
            VmDeviceUtils.updateSmartcardDevice(getVm().getId(), getVm().isSmartcardEnabled());
        }
    }

    protected void addVmWatchdog() {
        VmWatchdog vmWatchdog = getParameters().getWatchdog();
        if (vmWatchdog != null) {
            VdcActionType actionType = VmDeviceUtils.hasWatchdog(getVmTemplateId()) ?
                    VdcActionType.UpdateWatchdog : VdcActionType.AddWatchdog;
            runInternalAction(
                    actionType,
                    buildWatchdogParameters(vmWatchdog),
                    cloneContextAndDetachFromParent());
        }
    }

    private WatchdogParameters buildWatchdogParameters(VmWatchdog vmWatchdog) {
        WatchdogParameters parameters = new WatchdogParameters();
        parameters.setId(getParameters().getVmId());
        parameters.setAction(vmWatchdog.getAction());
        parameters.setModel(vmWatchdog.getModel());
        return parameters;
    }

    private void addVmRngDevice() {
        VmRngDevice rngDev = getParameters().getRngDevice();
        if (rngDev != null) {
            rngDev.setVmId(getVmId());
            RngDeviceParameters params = new RngDeviceParameters(rngDev, true);
            VdcReturnValueBase result = runInternalAction(VdcActionType.AddRngDevice, params, cloneContextAndDetachFromParent());
            if (!result.getSucceeded()) {
                log.error("Couldn't add RNG device for new VM.");
                throw new IllegalArgumentException("Couldn't add RNG device for new VM.");
            }
        }
    }

    protected void addVmPayload() {
        VmPayload payload = getParameters().getVmPayload();

        if (payload != null) {
            VmDeviceUtils.addManagedDevice(new VmDeviceId(Guid.newGuid(), getParameters().getVmId()),
                    VmDeviceGeneralType.DISK,
                    payload.getDeviceType(),
                    payload.getSpecParams(),
                    true,
                    true);
        }
    }

    protected void copyVmDevices() {
        VmDeviceUtils.copyVmDevices(vmDevicesSourceId,
                getVmId(),
                getSrcDeviceIdToTargetDeviceIdMapping(),
                isSoundDeviceEnabled(),
                getParameters().isConsoleEnabled(),
                isVirtioScsiEnabled(),
                isBalloonEnabled(),
                getParameters().getGraphicsDevices().keySet(),
                false);

        if (getInstanceTypeId() != null) {
            copyDiskDevicesFromTemplate();
        }
    }

    /**
     * If both the instance type and the template is set, than all the devices has to be copied from instance type except the
     * disk devices which has to be copied from the template (since the instance type has no disks but the template does have).
     */
    private void copyDiskDevicesFromTemplate() {
        List<VmDevice> disks =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vmDisksSource.getId(),
                                VmDeviceGeneralType.DISK,
                                VmDeviceType.DISK.getName());
        VmDeviceUtils.copyDiskDevices(
                getVmId(),
                disks,
                getSrcDeviceIdToTargetDeviceIdMapping()
        );
    }

    protected static boolean isLegalClusterId(Guid clusterId, List<String> reasons) {
        // check given cluster id
        Cluster cluster = DbFacade.getInstance().getClusterDao().get(clusterId);
        boolean legalClusterId = cluster != null;
        if (!legalClusterId) {
            reasons.add(EngineError.VM_INVALID_SERVER_CLUSTER_ID.toString());
        }
        return legalClusterId;
    }

    protected boolean areParametersLegal(List<String> reasons) {
        boolean returnValue = false;
        final VmStatic vmStaticData = getParameters().getVmStaticData();

        if (vmStaticData != null) {

            returnValue = isLegalClusterId(vmStaticData.getClusterId(), reasons);

            if (!validatePinningAndMigration(reasons, vmStaticData, getParameters().getVm().getCpuPinning())) {
                returnValue = false;
            }

        }
        return returnValue;
    }

    protected void addVmNetwork() {
        List<? extends VmNic> nics = getVmInterfaces();
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(macPool);
        vmInterfaceManager.sortVmNics(nics, getVmInterfaceDevices());

        List<String> macAddresses = macPool.allocateMacAddresses(nics.size());

        // Add interfaces from template
        for (int i = 0; i < nics.size(); ++i) {
            VmNic iface = nics.get(i);
            Guid id = Guid.newGuid();
            srcVmNicIdToTargetVmNicIdMapping.put(iface.getId(), id);
            iface.setId(id);
            iface.setMacAddress(macAddresses.get(i));
            iface.setSpeed(VmInterfaceType.forValue(iface.getType()).getSpeed());
            iface.setVmTemplateId(null);
            iface.setVmId(getParameters().getVmStaticData().getId());
            updateProfileOnNic(iface);
            getVmNicDao().save(iface);
            getCompensationContext().snapshotNewEntity(iface);
            DbFacade.getInstance().getVmNetworkStatisticsDao().save(iface.getStatistics());
            getCompensationContext().snapshotNewEntity(iface.getStatistics());
        }
    }

    protected void addVmNumaNodes() {
        List<VmNumaNode> numaNodes = getParameters().getVm().getvNumaNodeList();
        if (numaNodes.isEmpty()) {
            return;
        }
        VmNumaNodeOperationParameters params = new VmNumaNodeOperationParameters(getParameters().getVm(), numaNodes);

        VdcReturnValueBase returnValueBase = getBackend().runInternalAction(VdcActionType.AddVmNumaNodes, params);
        if (!returnValueBase.getSucceeded()) {
            auditLogDirector.log(this, AuditLogType.NUMA_ADD_VM_NUMA_NODE_FAILED);
        }
    }

    protected void addVmInit() {
        VmHandler.addVmInitToDB(getParameters().getVmStaticData());
    }

    protected void addVmStatic() {
        VmStatic vmStatic = getParameters().getVmStaticData();

        if (vmStatic.getOrigin() == null) {
            vmStatic.setOrigin(OriginType.valueOf(Config.<String> getValue(ConfigValues.OriginType)));
        }
        vmStatic.setId(getVmId());
        vmStatic.setQuotaId(getQuotaId());
        vmStatic.setCreationDate(new Date());
        vmStatic.setCreatedByUserId(getUserId());
        setIconIds(vmStatic);
        // Parses the custom properties field that was filled by frontend to
        // predefined and user defined fields
        VmPropertiesUtils.getInstance().separateCustomPropertiesToUserAndPredefined(
                getEffectiveCompatibilityVersion(), vmStatic);

        updateOriginalTemplate(vmStatic);

        getVmStaticDao().save(vmStatic);
        getCompensationContext().snapshotNewEntity(vmStatic);
    }

    protected void updateOriginalTemplate(VmStatic vmStatic) {
        vmStatic.setOriginalTemplateGuid(vmStatic.getVmtGuid());
        vmStatic.setOriginalTemplateName(getVmTemplate().getName());
    }

    void addVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(getVmId());
        vmDynamic.setStatus(VMStatus.Down);
        vmDynamic.setVmHost("");
        vmDynamic.setVmIp("");
        vmDynamic.setVmFQDN("");
        vmDynamic.setLastStopTime(new Date());
        getDbFacade().getVmDynamicDao().save(vmDynamic);
        getCompensationContext().snapshotNewEntity(vmDynamic);
    }

    void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        DbFacade.getInstance().getVmStatisticsDao().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
    }

    protected boolean addVmImages() {
        if (!vmDisksSource.getDiskTemplateMap().isEmpty()) {
            if (getVm().getStatus() != VMStatus.Down) {
                log.error("Cannot add images. VM is not Down");
                throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
            }
            lockVM();
            Collection<DiskImage> templateDisks = getImagesToCheckDestinationStorageDomains();
            List<DiskImage> diskImages = ImagesHandler.filterImageDisks(templateDisks, true, false, true);
            for (DiskImage image : diskImages) {
                VdcReturnValueBase result = runInternalActionWithTasksContext(
                        getDiskCreationCommandType(),
                        buildDiskCreationParameters(image));

                /**
                 * if couldn't create snapshot then stop the transaction and the command
                 */
                if (!result.getSucceeded()) {
                    throw new EngineException(result.getFault().getError());
                } else {
                    getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
                    DiskImage newImage = result.getActionReturnValue();
                    srcDiskIdToTargetDiskIdMapping.put(image.getId(), newImage.getId());
                }
            }

            // Clone volumes for Cinder disk templates
            addVmCinderDisks(templateDisks);
        }
        return true;
    }

    protected VdcActionType getDiskCreationCommandType() {
        return VdcActionType.CreateSnapshotFromTemplate;
    }

    protected void lockVM() {
        VmHandler.lockVm(getVmId());
    }

    protected CreateSnapshotFromTemplateParameters buildDiskCreationParameters(DiskImage image) {
        CreateSnapshotFromTemplateParameters tempVar = new CreateSnapshotFromTemplateParameters(
                image.getImageId(), getParameters().getVmStaticData().getId());
        tempVar.setDestStorageDomainId(diskInfoDestinationMap.get(image.getId()).getStorageIds().get(0));
        tempVar.setDiskAlias(diskInfoDestinationMap.get(image.getId()).getDiskAlias());
        tempVar.setStorageDomainId(image.getStorageIds().get(0));
        tempVar.setVmSnapshotId(getVmSnapshotId());
        tempVar.setParentCommand(VdcActionType.AddVm);
        tempVar.setEntityInfo(getParameters().getEntityInfo());
        tempVar.setParentParameters(getParameters());
        tempVar.setQuotaId(diskInfoDestinationMap.get(image.getId()).getQuotaId());
        tempVar.setDiskProfileId(diskInfoDestinationMap.get(image.getId()).getDiskProfileId());

        return tempVar;
    }

    protected void createAndSaveNewDiskVmElement(Guid newDiskImageId, Guid newVmId, DiskVmElement oldDve) {
        DiskVmElement newDve = DiskVmElement.copyOf(oldDve, newDiskImageId, newVmId);
        getDiskVmElementDao().save(newDve);
    }

    protected void addVmCinderDisks(Collection<DiskImage> templateDisks) {
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(templateDisks);
        if (cinderDisks.isEmpty()) {
            return;
        }
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk cinderDisk : cinderDisks) {
            ImagesContainterParametersBase params = buildCloneCinderDiskCommandParameters(cinderDisk);
            VdcReturnValueBase vdcReturnValueBase = runInternalAction(
                    VdcActionType.CloneSingleCinderDisk,
                    params,
                    cloneContext().withoutExecutionContext().withoutLock());
            if (!vdcReturnValueBase.getSucceeded()) {
                log.error("Error cloning Cinder disk '{}': {}", cinderDisk.getDiskAlias());
                getReturnValue().setFault(vdcReturnValueBase.getFault());
                return;
            }
            Guid imageId = vdcReturnValueBase.getActionReturnValue();
            createAndSaveNewDiskVmElement(imageId, getVmId(), cinderDisk.getDiskVmElementForVm(getVmTemplateId()));

            diskImageMap.put(cinderDisk.getId(), imageId);
        }
        srcDiskIdToTargetDiskIdMapping.putAll(diskImageMap);
    }

    private ImagesContainterParametersBase buildCloneCinderDiskCommandParameters(CinderDisk cinderDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(cinderDisk.getImageId());
        DiskImage templateDisk = diskInfoDestinationMap.get(cinderDisk.getId());
        createParams.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setEntityInfo(getParameters().getEntityInfo());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        createParams.setVmSnapshotId(getVmSnapshotId());
        return createParams;
    }

    private void addVmToPool() {
        AddVmToPoolParameters parameters = new AddVmToPoolParameters(getParameters().getPoolId(), getVmId());
        parameters.setShouldBeLogged(false);
        VdcReturnValueBase result = runInternalActionWithTasksContext(
                VdcActionType.AddVmToPool,
                parameters);
        setSucceeded(result.getSucceeded());
        if (!result.getSucceeded()) {
            log.error("Error adding VM {} to Pool {}", getVmId(), getParameters().getPoolId());
            getReturnValue().setFault(result.getFault());
            return;
        }
        addVmPermission();
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
    protected VdcActionType getChildActionType() {
        return VdcActionType.CreateSnapshotFromTemplate;
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
     * @param permSubject permission subject
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

        PermissionSubject createInstanceSubject = new PermissionSubject(id, VdcObjectType.VmTemplate, ActionGroup.CREATE_INSTANCE);
        PermissionSubject actionGroupSubject = new PermissionSubject(id, VdcObjectType.VmTemplate, getActionType().getActionGroup());

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
                        VdcObjectType.Cluster, ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
            }
            //if the template is blank we ignore his pinned hosts
            if(vmTemplate.isBlank()){
                return;
            }
            Set<Guid> dedicatedVmForVdsFromUser = new HashSet<>(vmFromParams.getDedicatedVmForVdsList());
            Set<Guid> dedicatedVmForVdsFromTemplate = new HashSet<>(vmTemplate.getDedicatedVmForVdsList());
            // host-specific parameters can be changed by administration role only
            if (!dedicatedVmForVdsFromUser.equals(dedicatedVmForVdsFromTemplate)
                    || !StringUtils.isEmpty(vmFromParams.getCpuPinning())) {
                permissionList.add(new PermissionSubject(getClusterId(),
                        VdcObjectType.Cluster, ActionGroup.EDIT_ADMIN_VM_PROPERTIES));
            }
        }
    }

    protected void addVmPermission() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        if (isMakeCreatorExplicitOwner()) {
            permissionsToAdd.addPermission(getCurrentUser().getId(), PredefinedRoles.VM_OPERATOR.getId(),
                    getVmId(), VdcObjectType.VM);
        }

        if (getParameters().isCopyTemplatePermissions() && !getVmTemplateId().equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)) {
            copyTemplatePermissions(permissionsToAdd);
        }

        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            MultiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));

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
        PermissionDao dao = getDbFacade().getPermissionDao();

        List<Permission> templatePermissions = dao.getAllForEntity(getVmTemplateId(), getEngineSessionSeqId(), false);

        for (Permission templatePermission : templatePermissions) {
            boolean templateOwnerRole = templatePermission.getRoleId().equals(PredefinedRoles.TEMPLATE_OWNER.getId());
            boolean templateUserRole = templatePermission.getRoleId().equals(PredefinedRoles.TEMPLATE_USER.getId());

            if (templateOwnerRole || templateUserRole) {
                continue;
            }

            permissionsToAdd.addPermission(templatePermission.getAdElementId(), templatePermission.getRoleId(),
                    getVmId(), VdcObjectType.VM);
        }

    }

    protected void addDiskPermissions() {
        List<Guid> newDiskImageIds = new ArrayList<>(srcDiskIdToTargetDiskIdMapping.values());
        Permission[] permsArray = new Permission[newDiskImageIds.size()];

        for (int i = 0; i < newDiskImageIds.size(); i++) {
            permsArray[i] =
                    new Permission(getUserIdOfDiskOperator(),
                            PredefinedRoles.DISK_OPERATOR.getId(),
                            newDiskImageIds.get(i),
                            VdcObjectType.Disk);
        }
        MultiLevelAdministrationHandler.addPermission(permsArray);
    }

    private Guid getUserIdOfDiskOperator() {
        Guid diskOperatorIdFromParams = getParameters().getDiskOperatorAuthzPrincipalDbId();
        return diskOperatorIdFromParams != null ? diskOperatorIdFromParams : getCurrentUser().getId();
    }

    protected void addActiveSnapshot() {
        _vmSnapshotId = Guid.newGuid();
        new SnapshotsManager().addActiveSnapshot(_vmSnapshotId, getVm(), getCompensationContext());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!StringUtils.isBlank(getParameters().getVm().getName())) {
            return Collections.singletonMap(getParameters().getVm().getName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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
        return getParameters().getVmStaticData().getQuotaId();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return diskInfoDestinationMap.values()
                .stream()
                .map(disk -> new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        null,
                        QuotaStorageConsumptionParameter.QuotaAction.CONSUME,
                        disk.getStorageIds().get(0),
                        (double) disk.getSizeInGigabytes()))
                .collect(Collectors.toList());
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        list.add(new QuotaSanityParameter(getQuotaId(), null));
        return list;
    }

    public Map<Guid, Guid> getSrcDiskIdToTargetDiskIdMapping() {
        return srcDiskIdToTargetDiskIdMapping;
    }

    public Map<Guid, Guid> getSrcDeviceIdToTargetDeviceIdMapping() {
        Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = new HashMap<>();
        srcDeviceIdToTargetDeviceIdMapping.putAll(srcVmNicIdToTargetVmNicIdMapping);
        srcDeviceIdToTargetDeviceIdMapping.putAll(srcDiskIdToTargetDiskIdMapping);
        return srcDeviceIdToTargetDeviceIdMapping;
    }

    protected boolean isVirtioScsiEnabled() {
        Boolean virtioScsiEnabled = getParameters().isVirtioScsiEnabled();
        boolean isOsSupportedForVirtIoScsi = VmValidationUtils.isDiskInterfaceSupportedByOs(
                getParameters().getVm().getOs(), getEffectiveCompatibilityVersion(), DiskInterface.VirtIO_SCSI);

        return virtioScsiEnabled != null ? virtioScsiEnabled : isOsSupportedForVirtIoScsi;
    }

    protected boolean isBalloonEnabled() {
        Boolean balloonEnabled = getParameters().isBalloonEnabled();
        return balloonEnabled != null ? balloonEnabled :
            osRepository.isBalloonEnabled(getParameters().getVmStaticData().getOsId(),
                    getEffectiveCompatibilityVersion());
    }

    protected boolean isSoundDeviceEnabled() {
        Boolean soundDeviceEnabled = getParameters().isSoundDeviceEnabled();
        return soundDeviceEnabled != null ? soundDeviceEnabled :
                osRepository.isSoundDeviceEnabled(getParameters().getVmStaticData().getOsId(),
                        getEffectiveCompatibilityVersion());
    }

    protected boolean hasWatchdog() {
        return getParameters().getWatchdog() != null;
    }

    protected boolean isVirtioScsiControllerAttached(Guid vmId) {
        return VmDeviceUtils.hasVirtioScsiController(vmId);
    }

    /**
     * This method override vm values with the instance type values
     * in case instance type is selected for this vm
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
            vmDisksSource = (VmTemplate)imageType;
        }

        // Choose a proper default OS according to the cluster architecture
        if (getParameters().getVmStaticData().getOsId() == OsRepository.AUTO_SELECT_OS) {
            if (getCluster().getArchitecture() != ArchitectureType.undefined) {
                Integer defaultOs = osRepository.getDefaultOSes().get(getCluster().getArchitecture());

                getParameters().getVmStaticData().setOsId(defaultOs);
            }
        }

        VmHandler.autoSelectUsbPolicy(getParameters().getVmStaticData());
        // Choose a proper default display type according to the cluster architecture
        VmHandler.autoSelectDefaultDisplayType(vmDevicesSourceId,
            getParameters().getVmStaticData(),
            getCluster(),
            getParameters().getGraphicsDevices());

        // If not set by user, choose proper graphics device according to the cluster architecture
        VmHandler.autoSelectGraphicsDevice(vmDevicesSourceId,
                getParameters().getVmStaticData(),
                getCluster(),
                getParameters().getGraphicsDevices(),
                getEffectiveCompatibilityVersion());
    }

    protected boolean isTemplateInValidDc() {
        return VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVmTemplateId())
                || getVmTemplate().getStoragePoolId().equals(getStoragePoolId());
    }

    protected void updateProfileOnNic(VmNic iface) {
        Network network = NetworkHelper.getNetworkByVnicProfileId(iface.getVnicProfileId());
        if (network != null && !NetworkHelper.isNetworkInCluster(network, getClusterId())) {
            iface.setVnicProfileId(null);
        }
    }
    protected boolean checkNumberOfMonitors() {
        Collection<GraphicsType> graphicsTypes = VmHandler.getResultingVmGraphics(
                VmDeviceUtils.getGraphicsTypesOfEntity(getVmTemplateId()),
                getParameters().getGraphicsDevices());
        int numOfMonitors = getParameters().getVmStaticData().getNumOfMonitors();

        return VmHandler.isNumOfMonitorsLegal(graphicsTypes,
                numOfMonitors,
                getReturnValue().getValidationMessages());
    }

    /**
     * Icon processing policy:
     * <ul>
     *     <li>If there is an attached icon, it is used as large icon as base for computation of small icon.
     *         Predefined icons should not be sent in parameters.</li>
     *     <li>If there are no icon in parameters && both (small and large) icon ids are set then those ids are used.
     *         </li>
     *     <li>Otherwise (at least one icon id is null) both icon ids are copied from template.</li>
     * </ul>
     */
    public void setIconIds(VmStatic vmStatic) {
        if (getParameters().getVmLargeIcon() != null){
            final VmIconIdSizePair iconIds =
                    IconUtils.ensureIconPairInDatabase(getParameters().getVmLargeIcon());
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
        return getParameters().isUseCinderCommandCallback() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }

}
