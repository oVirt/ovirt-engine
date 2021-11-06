package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.IconUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.utils.NameForVmInPoolGenerator;

/**
 * This class is responsible for creation of a vmpool with vms within it. This class is not transactive,
 * which means that the 'execute' method does not run in transaction. On the other hand, each vm is added to the system
 * and attached to the vmpool in a transaction (one transaction for two operations).
 * To make this work, a Transaction is generated in the Execute function. Transactions are isolated,
 * which means that if one of vms is not added for some reason (image does not exists, etc) - it does not affect other
 * vms generation. Each vm is created with this format: {vm_name}_{number} where number runs from 1 to vms count. If one of vms to be created
 * already exists - the number is increased. For example if vm_8 exists - vm_9 will be created instead of it.
 */

public abstract class CommonVmPoolCommand<T extends AddVmPoolParameters> extends VmPoolCommandBase<T>
        implements QuotaStorageDependent {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private MacPoolPerCluster macPoolPerCluster;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    protected VmTemplateHandler vmTemplateHandler;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VmPoolDao vmPoolDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private DiskProfileDao diskProfileDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private IconUtils iconUtils;

    private Map<Guid, DiskImage> diskInfoDestinationMap;
    private Map<Guid, List<DiskImage>> storageToDisksMap;
    private Map<Guid, StorageDomain> destStorages = new HashMap<>();
    private Map<Guid, Long> targetDomainsSize;
    private List<Disk> templateDisks;
    private Map<Guid, List<Guid>> diskToStorageIds;
    private Map<Guid, List<Guid>> diskToProfileMap;
    /**
     * This flag is set to true if all of the VMs were added successfully, false otherwise.
     */
    private boolean allAddVmsSucceeded = true;
    /**
     * This flag is set to true if any of the VMs was added successfully, false otherwise.
     */
    private boolean anyAddVmSucceeded = false;
    private NameForVmInPoolGenerator nameForVmInPoolGenerator;
    private Version effectiveCompatibilityVersion;
    private MacPool macPool;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected CommonVmPoolCommand(Guid commandId) {
        super(commandId);
    }

    public CommonVmPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmPool(parameters.getVmPool());
        setClusterId(getVmPool().getClusterId());
    }

    /*
     * this method exist not to do caching, but to deal with init being called from constructor in class hierarchy. init
     * method is not called via Postconstruct, but from constructor, meaning, that in tests we're unable pro inject
     * 'macPoolPerCluster' soon enough.
     */
    protected MacPool getMacPool() {
        if (macPool == null) {
            macPool = macPoolPerCluster.getMacPoolForCluster(getClusterId(), getContext());
        }
        return macPool;
    }

    @Override
    protected void init() {
        if (getCluster() == null) {
            return;
        }

        setEffectiveCompatibilityVersion(
                CompatibilityVersionUtils.getEffective(getParameters().getVmStaticData(), this::getCluster));

        Guid templateIdToUse = getParameters().getVmStaticData().getVmtGuid();
        // if set to use latest version, get it from db and use it as template
        if (getParameters().getVmStaticData().isUseLatestVersion()) {
            VmTemplate latest = vmTemplateDao.getTemplateWithLatestVersionInChain(templateIdToUse);

            if (latest != null) {
                // if not using original template, need to override storage mappings
                // as it may have different set of disks
                if (!templateIdToUse.equals(latest.getId())) {
                    getParameters().setDiskInfoDestinationMap(null);
                }

                setVmTemplate(latest);
                templateIdToUse = latest.getId();
                getParameters().getVmStaticData().setVmtGuid(templateIdToUse);
            }
        }

        setVmTemplateId(templateIdToUse);
        initTemplate();
        if (getVmPool().isAutoStorageSelect()) {
            initTargetDomains();
        }

        nameForVmInPoolGenerator = new NameForVmInPoolGenerator(getParameters().getVmPool().getName());
    }

    protected void initTemplate() {
        if (getVmTemplate() != null) {
            vmTemplateHandler.updateDisksFromDb(getVmTemplate());
        }
    }

    protected abstract void createOrUpdateVmPool();

    protected Version getEffectiveCompatibilityVersion() {
        return effectiveCompatibilityVersion;
    }

    protected void setEffectiveCompatibilityVersion(Version effectiveCompatibilityVersion) {
        this.effectiveCompatibilityVersion = effectiveCompatibilityVersion;
    }

    /**
     * This operation may take much time so the inner commands have fine-grained TX handling which
     * means they aim to make all calls to Vds commands (i.e VDSM calls) out of TX.
     */
    @Override
    protected void executeCommand() {
        updateVmInitPassword();
        vmHandler.warnMemorySizeLegal(getParameters().getVmStaticData(), getEffectiveCompatibilityVersion());

        // Free exclusive VM_POOL lock, if taken. Further AddVmAndAttachToPool commands
        // require shared VM_POOL locks only.
        freeLock();

        createOrUpdateVmPool();
        setActionReturnValue(getVmPool().getVmPoolId());
        vmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getVmtGuid(),
                getCompensationContext());

        addVmsToPool();

        getReturnValue().setValid(isAllAddVmsSucceeded());
        setSucceeded(isAllAddVmsSucceeded());
        vmTemplateHandler.unlockVmTemplate(getParameters().getVmStaticData().getVmtGuid());
        if (!isAnyAddVmSucceeded()) {
            onNoVmsAdded();
        }
    }

    private void addVmsToPool() {
        int subsequentFailedAttempts = 0;
        int vmPoolMaxSubsequentFailures = Config.<Integer> getValue(ConfigValues.VmPoolMaxSubsequentFailures);

        for (int i = 0; i < getParameters().getVmsCount(); i++) {
            String currentVmName = generateUniqueVmName();
            ActionReturnValue returnValue =
                    runInternalAction(ActionType.AddVm,
                            buildAddVmParameters(currentVmName),
                            createAddVmStepContext(currentVmName));

            if (returnValue != null && !returnValue.getSucceeded() && !returnValue.getValidationMessages().isEmpty()) {
                for (String msg : returnValue.getValidationMessages()) {
                    if (!getReturnValue().getValidationMessages().contains(msg)) {
                        getReturnValue().getValidationMessages().add(msg);
                    }
                }
                allAddVmsSucceeded = false;
                subsequentFailedAttempts++;
            } else { // Succeed on that, reset subsequentFailedAttempts.
                subsequentFailedAttempts = 0;
                anyAddVmSucceeded = true;
            }
            // if subsequent attempts failure exceeds configuration value , abort the loop.
            if (subsequentFailedAttempts == vmPoolMaxSubsequentFailures) {
                auditLogDirector.log(this, AuditLogType.USER_VM_POOL_MAX_SUBSEQUENT_FAILURES_REACHED);
                break;
            }
        }
    }

    protected void onNoVmsAdded() {
    }

    private String generateUniqueVmName() {
        String currentVmName;
        do {
            currentVmName = nameForVmInPoolGenerator.generateVmName();
        } while (vmHandler.isVmWithSameNameExistStatic(currentVmName, getStoragePoolId()));

        return currentVmName;
    }

    private AddVmParameters buildAddVmParameters(String vmName) {
        VmStatic currVm = new VmStatic(getParameters().getVmStaticData());
        currVm.setName(vmName);
        currVm.setStateless(!getVmPool().isStateful());

        if (getParameters().getVmLargeIcon() != null) {
            final VmIconIdSizePair iconIds = iconUtils.ensureIconPairInDatabase(getParameters().getVmLargeIcon());
            currVm.setSmallIconId(iconIds.getSmall());
            currVm.setLargeIconId(iconIds.getLarge());
        }

        AddVmParameters parameters = new AddVmParameters(currVm);
        parameters.setPoolId(getVmPool().getVmPoolId());
        if (getVmPool().isAutoStorageSelect()) {
            parameters.setDiskInfoDestinationMap(autoSelectTargetDomainAndVolumeFormat());
        } else {
            parameters.setDiskInfoDestinationMap(diskInfoDestinationMap);
        }
        parameters.getDiskInfoDestinationMap().values().forEach(this::setVolumeFormat);
        if (StringUtils.isEmpty(getParameters().getSessionId())) {
            parameters.setParametersCurrentUser(getCurrentUser());
        } else {
            parameters.setSessionId(getParameters().getSessionId());
        }
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        // check if device is enabled or we need to override it to true
        parameters.setSoundDeviceEnabled(Boolean.TRUE.equals(getParameters().isSoundDeviceEnabled())
                || vmDeviceUtils.shouldOverrideSoundDevice(
                        getParameters().getVmStaticData(),
                        getEffectiveCompatibilityVersion(),
                        getParameters().isSoundDeviceEnabled()));
        parameters.setTpmEnabled(getParameters().isTpmEnabled());
        parameters.setConsoleEnabled(getParameters().isConsoleEnabled());
        parameters.setVirtioScsiEnabled(getParameters().isVirtioScsiEnabled());
        parameters.setSeal(getParameters().getSeal());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);

        VmRngDevice rngDevice = getParameters().getRngDevice();
        if (rngDevice != null) {
            parameters.setUpdateRngDevice(true);
            parameters.setRngDevice(rngDevice);
        }

        parameters.getGraphicsDevices().putAll(getParameters().getGraphicsDevices());

        return parameters;
    }

    protected abstract void updateVmInitPassword();

    private CommandContext createAddVmStepContext(String currentVmName) {
        CommandContext commandCtx = null;

        try {
            Map<String, String> values = new HashMap<>();
            values.put(VdcObjectType.VM.name().toLowerCase(), currentVmName);
            Step addVmStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    StepEnum.ADD_VM_TO_POOL,
                    ExecutionMessageDirector.resolveStepMessage(StepEnum.ADD_VM_TO_POOL, values));
            ExecutionContext ctx = new ExecutionContext();
            ctx.setStep(addVmStep);
            ctx.setMonitored(true);
            commandCtx = cloneContextAndDetachFromParent().withExecutionContext(ctx);
        } catch (RuntimeException e) {
            log.error("Failed to create command context of adding VM '{}' to Pool '{}': {}",
                    currentVmName,
                    getParameters().getVmPool().getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
        return commandCtx;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__DESKTOP_POOL);
    }

    @Override
    protected boolean validate() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID);
        }

        // A Pool cannot be added in a cluster without a defined architecture
        if (getCluster().getArchitecture() == ArchitectureType.undefined) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }

        VmPool pool = vmPoolDao.getByName(getParameters().getVmPool().getName());
        if (pool != null
                && (getActionType() == ActionType.AddVmPool || !pool.getVmPoolId().equals(
                        getParameters().getVmPoolId()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        setStoragePoolId(getCluster().getStoragePoolId());
        if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
            return false;
        }

        ensureDestinationImageMap();

        // check if the selected template is compatible with Cluster architecture.
        if (!getVmTemplate().getId().equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)
                && getCluster().getArchitecture() != getVmTemplate().getClusterArch()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_INCOMPATIBLE);
        }

        final int nicsCount = getParameters().getVmsCount() * vmNicDao.getAllForTemplate(getVmTemplateId()).size();
        if (!validate(vmHandler.verifyMacPool(nicsCount, getMacPool()))) {
            return false;
        }

        final int priority = getParameters().getVmStaticData().getPriority();
        if (!validate(vmHandler.isVmPriorityValueLegal(priority))) {
            return false;
        }

        if (getVmTemplate().getDiskTemplateMap().values().size() != diskInfoDestinationMap.size()) {
            log.error("Can not found any default active domain for one of the disks of template with id '{}'",
                    getVmTemplate().getId());
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            return false;
        }

        List<Guid> storageIds = new ArrayList<>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            Guid storageId = diskImage.getStorageIds().get(0);
            if (!storageIds.contains(storageId) && !areTemplateImagesInStorageReady(storageId)) {
                return false;
            }
            storageIds.add(storageId);
        }

        if (getActionType() == ActionType.AddVmPool && getParameters().getVmsCount() < 1) {
            return failValidation(EngineMessage.VM_POOL_CANNOT_CREATE_WITH_NO_VMS);
        }

        if (getParameters().getVmPool().getPrestartedVms() >
        getParameters().getVmPool().getAssignedVmsCount() + getParameters().getVmsCount()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_PRESTARTED_VMS_CANNOT_EXCEED_VMS_COUNT);
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        return checkDestDomains();
    }

    protected boolean areTemplateImagesInStorageReady(Guid storageId) {
        return validate(vmTemplateHandler.isVmTemplateImagesReady(getVmTemplate(),
                storageId,
                false,
                true,
                true,
                destStorages.isEmpty(),
                storageToDisksMap.get(storageId)));
    }

    private Guid findAvailableStorageDomain(long diskSize, List<Guid> storageIds) {
        Guid dest = storageIds.get(0);
        for (Guid storageId: storageIds) {
            if (targetDomainsSize.get(storageId) > targetDomainsSize.get(dest)) {
                dest = storageId;
            }
        }
        long destSize = targetDomainsSize.get(dest);
        targetDomainsSize.put(dest, destSize - diskSize);
        return dest;
    }

    private void initTargetDomains() {
        templateDisks = diskDao.getAllForVm(getParameters().getVmStaticData().getVmtGuid());
        targetDomainsSize = new HashMap<>();
        diskToProfileMap = new HashMap<>();
        diskToStorageIds = new HashMap<>();

        for (Disk disk: templateDisks) {
            DiskImage diskImage = (DiskImage) disk;
            diskToProfileMap.put(disk.getId(), diskImage.getDiskProfileIds());
            diskToStorageIds.put(disk.getId(), diskImage.getStorageIds());
            for (Guid storageId: diskImage.getStorageIds()) {
                if (!targetDomainsSize.containsKey(storageId)) {
                    StorageDomain domain = storageDomainDao.get(storageId);
                    targetDomainsSize.put(domain.getId(), domain.getAvailableDiskSizeInBytes());
                }
            }
        }
    }

    private Map<Guid, DiskImage> autoSelectTargetDomainAndVolumeFormat() {
        Map<Guid, DiskImage> destinationMap = new HashMap<>();
        for (Disk disk: templateDisks) {
            DiskImage diskImage = (DiskImage)disk;
            ArrayList<Guid> storageIds = new ArrayList<>();
            Guid storageId = findAvailableStorageDomain(disk.getSize(), diskToStorageIds.get(disk.getId()));
            storageIds.add(storageId);
            List<Guid> profileIds = diskToProfileMap.get(disk.getId());
            for (Guid profileId: profileIds) {
                DiskProfile profile = diskProfileDao.get(profileId);
                if (profile.getStorageDomainId().equals(storageId)) {
                    diskImage.setDiskProfileId(profile.getId());
                    break;
                }
            }
            // Set target domain
            diskImage.setStorageIds(storageIds);

            destinationMap.put(disk.getId(), diskImage);
        }
        return destinationMap;
    }

    private void setVolumeFormat(DiskImage diskImage) {
        // Note that the disks of VMs in a pool are essentially snapshots of the template's disks.
        // therefore when creating the VM's disks, the image parameters are overridden anyway.
        // We were required to change only the VolumeFormat here for passing the AddVMCommand's
        // validation
        if (diskImage.getDiskStorageType() == DiskStorageType.CINDER) {
            diskImage.setVolumeFormat(VolumeFormat.RAW);
        } else {
            diskImage.setVolumeFormat(VolumeFormat.COW);
        }
    }

    protected void ensureDestinationImageMap() {
        if (getVmPool().isAutoStorageSelect() || MapUtils.isEmpty(getParameters().getDiskInfoDestinationMap())) {
            diskInfoDestinationMap = new HashMap<>();

            if (getVmTemplate() == null) {
                return;
            }

            if (!Guid.isNullOrEmpty(getParameters().getStorageDomainId()) && !getVmPool().isAutoStorageSelect()) {
                Guid storageId = getParameters().getStorageDomainId();
                ArrayList<Guid> storageIds = new ArrayList<>();
                storageIds.add(storageId);
                for (DiskImage image : getVmTemplate().getDiskTemplateMap().values()) {
                    image.setStorageIds(storageIds);
                    diskInfoDestinationMap.put(image.getId(), image);
                }
            } else {
                imagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                        diskInfoDestinationMap,
                        destStorages);
            }
        } else {
            diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        }

        storageToDisksMap =
                imagesHandler.buildStorageToDiskMap(getVmTemplate().getDiskTemplateMap().values(),
                        diskInfoDestinationMap);
    }

    protected boolean checkDestDomains() {
        List<Guid> validDomains = new ArrayList<>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            Guid domainId = diskImage.getStorageIds().get(0);
            if (validDomains.contains(domainId)) {
                continue;
            }
            StorageDomain domain = destStorages.get(domainId);
            if (domain == null) {
                domain = this.storageDomainDao.getForStoragePool(domainId, getVmTemplate().getStoragePoolId());
                destStorages.put(domainId, domain);
            }
            if (storageToDisksMap.containsKey(domainId)) {
                int numOfDisksOnDomain = storageToDisksMap.get(domainId).size();
                if (numOfDisksOnDomain > 0
                        && (domain.getStorageDomainType() == StorageDomainType.ImportExport)) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                }
            }
            validDomains.add(domainId);
        }

        return validateSpaceRequirements();
    }

    private boolean validateSpaceRequirements() {
        int numOfVms = getParameters().getVmsCount();
        Collection<DiskImage> diskDummies = ImagesHandler.getDisksDummiesForStorageAllocations(diskInfoDestinationMap.values());
        Collection<DiskImage> disks = new ArrayList<>(numOfVms * diskDummies.size());
        // Number of added disks multiplies by the vms number
        for (int i = 0; i < numOfVms; ++i) {
            disks.addAll(diskDummies);
        }

        Guid spId = getVmTemplate().getStoragePoolId();
        Set<Guid> sdIds = destStorages.keySet();
        MultipleStorageDomainsValidator storageDomainsValidator = getStorageDomainsValidator(spId, sdIds);
        return validate(storageDomainsValidator.allDomainsWithinThresholds())
                && validate(storageDomainsValidator.allDomainsHaveSpaceForNewDisks(disks));
    }

    protected boolean isAllAddVmsSucceeded() {
        return allAddVmsSucceeded;
    }

    private boolean isAnyAddVmSucceeded() {
        return anyAddVmSucceeded;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                map.put(diskImage, diskImage.getStorageIds().get(0));
            }
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    protected boolean setAndValidateCpuProfile() {
        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getParameters().getVmStaticData(),
                getUserIdIfExternal().orElse(null)));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return diskInfoDestinationMap.values().stream()
                .map(disk -> new QuotaStorageConsumptionParameter(
                        disk.getQuotaId(),
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        disk.getStorageIds().get(0),
                        (double)(disk.getSizeInGigabytes() * getParameters().getVmsCount())))
                .collect(Collectors.toList());
    }

    protected MultipleStorageDomainsValidator getStorageDomainsValidator(Guid spId, Set<Guid> sdIds) {
        return new MultipleStorageDomainsValidator(spId, sdIds);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    /**
     * Required for the audit logging
     */
    public String getVmsCount() {
        return String.valueOf(getParameters().getVmsCount());
    }

}
