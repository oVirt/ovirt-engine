package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
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

public abstract class CommonVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends AddVmPoolCommand<T>
        implements QuotaStorageDependent {

    protected HashMap<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    protected Map<Guid, StorageDomain> destStorages = new HashMap<>();
    private boolean addVmsSucceeded = true;
    private NameForVmInPoolGenerator nameForVmInPoolGenerator;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    protected CommonVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public CommonVmPoolWithVmsCommand(T parameters) {
        super(parameters);

        Guid templateIdToUse = getParameters().getVmStaticData().getVmtGuid();
        // if set to use latest version, get it from db and use it as template
        if (parameters.getVmStaticData().isUseLatestVersion()) {
            VmTemplate latest = getVmTemplateDAO().getTemplateWithLatestVersionInChain(templateIdToUse);

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
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<>();
        }
        setVdsGroupId(parameters.getVmPool().getVdsGroupId());

        nameForVmInPoolGenerator = new NameForVmInPoolGenerator(getParameters().getVmPool().getName());
    }

    protected void initTemplate() {
        if (getVmTemplate() != null) {
            VmTemplateHandler.updateDisksFromDb(getVmTemplate());
        }
    }

    protected abstract Guid getPoolId();

    /**
     * This operation may take much time so the inner commands have fine-grained TX handling which
     * means they aim to make all calls to Vds commands (i.e VDSM calls) out of TX.
     */
    @Override
    protected void executeCommand() {
        updateVmInitPassword();
        VmHandler.warnMemorySizeLegal(getParameters().getVmStaticData(), getVdsGroup().getcompatibility_version());

        Guid poolId = getPoolId();
        setActionReturnValue(poolId);
        VmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getVmtGuid(),
                getCompensationContext());

        addVmsToPool(poolId);

        getReturnValue().setCanDoAction(isAddVmsSucceded());
        setSucceeded(isAddVmsSucceded());
        VmTemplateHandler.unlockVmTemplate(getParameters().getVmStaticData().getVmtGuid());
        getCompensationContext().resetCompensation();
    }

    private void addVmsToPool(Guid poolId) {
        int subsequentFailedAttempts = 0;
        int vmPoolMaxSubsequentFailures = Config.<Integer> getValue(ConfigValues.VmPoolMaxSubsequentFailures);

        for (int i=0; i<getParameters().getVmsCount(); i++) {
            String currentVmName = generateUniqueVmName();
            VdcReturnValueBase returnValue =
                    runInternalAction(VdcActionType.AddVmAndAttachToPool,
                            buildAddVmAndAttachToPoolParameters(poolId, currentVmName),
                            createAddVmStepContext(currentVmName));

            if (returnValue != null && !returnValue.getSucceeded() && !returnValue.getCanDoActionMessages().isEmpty()) {
                for (String msg : returnValue.getCanDoActionMessages()) {
                    if (!getReturnValue().getCanDoActionMessages().contains(msg)) {
                        getReturnValue().getCanDoActionMessages().add(msg);
                    }
                }
                addVmsSucceeded = false;
                subsequentFailedAttempts++;
            }
            else { // Succeed on that , reset subsequentFailedAttempts.
                subsequentFailedAttempts = 0;
            }
            // if subsequent attempts failure exceeds configuration value , abort the loop.
            if (subsequentFailedAttempts == vmPoolMaxSubsequentFailures) {
                AuditLogableBase logable = new AuditLogableBase();
                AuditLogDirector.log(logable, AuditLogType.USER_VM_POOL_MAX_SUBSEQUENT_FAILURES_REACHED);
                break;
            }
        }
    }

    private String generateUniqueVmName() {
        String currentVmName;
        do {
            currentVmName = nameForVmInPoolGenerator.generateVmName();
        } while (VmHandler.isVmWithSameNameExistStatic(currentVmName));

        return currentVmName;
    }

    private AddVmAndAttachToPoolParameters buildAddVmAndAttachToPoolParameters(Guid poolId, String vmName) {
        VmStatic currVm = new VmStatic(getParameters().getVmStaticData());
        currVm.setName(vmName);

        AddVmAndAttachToPoolParameters parameters = new AddVmAndAttachToPoolParameters(
                currVm, poolId, vmName, diskInfoDestinationMap);
        parameters.setSessionId(getParameters().getSessionId());
        parameters.setParentCommand(VdcActionType.AddVmPoolWithVms);
        parameters.setSoundDeviceEnabled(getParameters().isSoundDeviceEnabled() != null
                ? getParameters().isSoundDeviceEnabled()
                : VmType.Desktop == getParameters().getVmStaticData().getVmType());
        parameters.setConsoleEnabled(getParameters().isConsoleEnabled());
        parameters.setVirtioScsiEnabled(getParameters().isVirtioScsiEnabled());
        parameters.setBalloonEnabled(getParameters().isBalloonEnabled());

        VmRngDevice rngDevice = getParameters().getRngDevice();
        if (rngDevice != null) {
            parameters.setUpdateRngDevice(true);
            parameters.setRngDevice(rngDevice);
        }

        return parameters;
    }

    private void updateVmInitPassword() {
        // We are not passing the VmInit password to the UI,
        // so we need to update the VmInit password from its template.
        if (getParameters().getVmStaticData().getVmInit() != null &&
                getParameters().getVmStaticData().getVmInit().isPasswordAlreadyStored()) {
            VmBase temp = new VmBase();
            temp.setId(getParameters().getVmStaticData().getVmtGuid());
            VmHandler.updateVmInitFromDB(temp, false);
            getParameters().getVmStaticData().getVmInit().setRootPassword(temp.getVmInit().getRootPassword());
        }
    }

    private CommandContext createAddVmStepContext(String currentVmName) {
        CommandContext commandCtx = null;

        try {
            Map<String, String> values = new HashMap<String, String>();
            values.put(VdcObjectType.VM.name().toLowerCase(), currentVmName);
            Step addVmStep = ExecutionHandler.addSubStep(getExecutionContext(),
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
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__DESKTOP_POOL);
    }

    @Override
    protected boolean canDoAction() {
        if (getVdsGroup() == null) {
            return failCanDoAction(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
        }

        // A Pool cannot be added in a cluster without a defined architecture
        if (getVdsGroup().getArchitecture() == ArchitectureType.undefined) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_UNDEFINED_ARCHITECTURE);
        }

        VmPool pool = getVmPoolDAO().getByName(getParameters().getVmPool().getName());
        if (pool != null
                && (getActionType() == VdcActionType.AddVmPoolWithVms || !pool.getVmPoolId().equals(
                        getParameters().getVmPoolId()))) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
        }

        setStoragePoolId(getVdsGroup().getStoragePoolId());
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
            return false;
        }

        // check if the selected template is compatible with Cluster architecture.
        if (!getVmTemplate().getId().equals(VmTemplateHandler.BLANK_VM_TEMPLATE_ID)
                && getVdsGroup().getArchitecture() != getVmTemplate().getClusterArch()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_IS_INCOMPATIBLE);
        }

        if (!verifyAddVM()) {
            return false;
        }

        if (!ensureDestinationImageMap()) {
            return false;
        }
        storageToDisksMap = ImagesHandler.buildStorageToDiskMap(getVmTemplate().getDiskTemplateMap().values(),
                diskInfoDestinationMap);
        List<Guid> storageIds = new ArrayList<Guid>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            Guid storageId = diskImage.getStorageIds().get(0);
            if (!storageIds.contains(storageId) && !areTemplateImagesInStorageReady(storageId)) {
                return false;
            }
            storageIds.add(storageId);
        }

        if (getActionType() == VdcActionType.AddVmPoolWithVms && getParameters().getVmsCount() < 1) {
            return failCanDoAction(VdcBllMessages.VM_POOL_CANNOT_CREATE_WITH_NO_VMS);
        }

        if (getParameters().getVmStaticData().isStateless()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
        }

        if (getParameters().getVmPool().getPrestartedVms() > getParameters().getVmPool().getAssignedVmsCount()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_PRESTARTED_VMS_CANNOT_EXCEED_VMS_COUNT);
        }

        if (Boolean.TRUE.equals(getParameters().isVirtioScsiEnabled()) &&
                !FeatureSupported.virtIoScsi(getVdsGroup().getcompatibility_version())) {
            return failCanDoAction(VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
        }
        if (!setAndValidateDiskProfiles()) {
            return false;
        }
        if (!setAndValidateCpuProfile()) {
            return false;
        }
        return checkDestDomains();
    }

    protected boolean verifyAddVM() {
        final List<String> reasons = getReturnValue().getCanDoActionMessages();
        final int nicsCount = getParameters().getVmsCount()
                * getVmNicDao().getAllForTemplate(getVmTemplateId()).size();
        final int priority = getParameters().getVmStaticData().getPriority();

        return VmHandler.verifyAddVm(reasons, nicsCount, priority, getMacPool());

    }

    protected boolean areTemplateImagesInStorageReady(Guid storageId) {
        return VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(),
                storageId,
                getReturnValue().getCanDoActionMessages(),
                false,
                true,
                true,
                destStorages.isEmpty(),
                storageToDisksMap.get(storageId));
    }

    private boolean ensureDestinationImageMap() {
        if (diskInfoDestinationMap.isEmpty()) {
            if (getParameters().getStorageDomainId() != null
                    && !Guid.Empty.equals(getParameters().getStorageDomainId())) {
                Guid storageId = getParameters().getStorageDomainId();
                ArrayList<Guid> storageIds = new ArrayList<Guid>();
                storageIds.add(storageId);
                for (DiskImage image : getVmTemplate().getDiskTemplateMap().values()) {
                    image.setStorageIds(storageIds);
                    diskInfoDestinationMap.put(image.getId(), image);
                }
            } else {
                ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                        diskInfoDestinationMap,
                        destStorages, false);
            }
        }
        if (getVmTemplate().getDiskTemplateMap().values().size() != diskInfoDestinationMap.size()) {
            log.error("Can not found any default active domain for one of the disks of template with id '{}'",
                    getVmTemplate().getId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            return false;
        }
        return true;
    }

    public boolean checkDestDomains() {
        List<Guid> validDomains = new ArrayList<Guid>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            Guid domainId = diskImage.getStorageIds().get(0);
            if (validDomains.contains(domainId)) {
                continue;
            }
            StorageDomain domain = destStorages.get(domainId);
            if (domain == null) {
                domain = getStorageDomainDAO().getForStoragePool(domainId, getVmTemplate().getStoragePoolId());
                destStorages.put(domainId, domain);
            }
            if (storageToDisksMap.containsKey(domainId)) {
                int numOfDisksOnDomain = storageToDisksMap.get(domainId).size();
                if (numOfDisksOnDomain > 0
                    && (domain.getStorageDomainType() == StorageDomainType.ImportExport)) {
                        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                }
            }
            validDomains.add(domainId);
        }
        return true;
    }

    private int getBlockSparseInitSizeInGB() {
        return Config.<Integer> getValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    protected boolean isAddVmsSucceded() {
        return addVmsSucceeded;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (diskInfoDestinationMap != null && !diskInfoDestinationMap.isEmpty()) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                map.put(diskImage, diskImage.getStorageIds().get(0));
            }
            return validate(DiskProfileHelper.setAndValidateDiskProfiles(map,
                    getStoragePool().getcompatibility_version()));
        }
        return true;
    }

    protected boolean setAndValidateCpuProfile() {
        return validate(CpuProfileHelper.setAndValidateCpuProfile(getParameters().getVmStaticData(),
                getVdsGroup().getcompatibility_version()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        for (DiskImage disk : diskInfoDestinationMap.values()) {
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    disk.getStorageIds().get(0),
                    (double)(disk.getSizeInGigabytes() * getParameters().getVmsCount()
                            * getBlockSparseInitSizeInGB())));
        }

        return list;
    }

}
