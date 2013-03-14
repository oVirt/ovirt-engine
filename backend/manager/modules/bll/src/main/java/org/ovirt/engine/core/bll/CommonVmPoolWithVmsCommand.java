package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.NameForVmInPoolGenerator;


/**
 * This class responsible to create vmpool with vms within. This class not transactive, that mean that function Execute
 * not running in transaction. From other hand, each vm added to system and attached to vmpool in transaction(one
 * transaction for two operation). To make it work, Transaction generated in Execute function. Transactions isolated,
 * that mean if one of vms not added from some reason(image not exists, etc) - it not affect other vms generation Each
 * vm created with this format: {vm_name}_{number} where number runs from 1 to vms count. If one of vms to be created
 * already exists - number increased. For example if vm_8 exists - vm_9 will be created instead of it.
 */

@CustomLogFields({ @CustomLogField("VmsCount") })
public abstract class CommonVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends AddVmPoolCommand<T>
        implements QuotaStorageDependent {

    protected HashMap<Guid, DiskImage> diskInfoDestinationMap;
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    protected Map<Guid, StorageDomain> destStorages = new HashMap<Guid, StorageDomain>();
    private boolean _addVmsSucceded = true;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    protected CommonVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public CommonVmPoolWithVmsCommand(T parameters) {
        super(parameters);
        setVmTemplateId(getParameters().getVmStaticData().getVmtGuid());
        initTemplate();
        diskInfoDestinationMap = getParameters().getDiskInfoDestinationMap();
        if (diskInfoDestinationMap == null) {
            diskInfoDestinationMap = new HashMap<Guid, DiskImage>();
        }
    }

    protected void initTemplate() {
        if (getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        }
    }

    protected abstract Guid getPoolId();

    /**
     * This operation may take much time, so transactions timeout increased to 2 minutes
     */
    @Override
    protected void executeCommand() {
        Guid poolId = getPoolId();
        boolean isAtLeastOneVMCreationFailed = false;
        setActionReturnValue(poolId);

        VmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getVmtGuid(),
                getCompensationContext());

        String poolName = getParameters().getVmStaticData().getName();
        int subsequentFailedAttempts = 0;
        int vmPoolMaxSubsequentFailures = Config.<Integer> GetValue(ConfigValues.VmPoolMaxSubsequentFailures);
        for (int i = 1, number = 1; i <= getParameters().getVmsCount(); i++, number++) {
            String currentVmName;
            number--;
            do {
                number++;
                currentVmName = NameForVmInPoolGenerator.generateVmName(poolName, number);
            } while ((Boolean) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                            new IsVmWithSameNameExistParameters(currentVmName)).getReturnValue());

            VmStatic currVm = new VmStatic(getParameters().getVmStaticData());
            currVm.setName(currentVmName);
            AddVmAndAttachToPoolParameters addVmAndAttachToPoolParams =
                    new AddVmAndAttachToPoolParameters(currVm, poolId, currentVmName,
                            diskInfoDestinationMap);
            addVmAndAttachToPoolParams.setSessionId(getParameters().getSessionId());
            addVmAndAttachToPoolParams.setParentCommand(VdcActionType.AddVmPoolWithVms);
            VdcReturnValueBase returnValue =
                    Backend.getInstance().runInternalAction(VdcActionType.AddVmAndAttachToPool,
                            addVmAndAttachToPoolParams,
                            createAddVmStepContext(currentVmName));
            if (returnValue != null && !returnValue.getSucceeded() && returnValue.getCanDoActionMessages().size() > 0) {
                for (String msg : returnValue.getCanDoActionMessages()) {
                    if (!getReturnValue().getCanDoActionMessages().contains(msg)) {
                        getReturnValue().getCanDoActionMessages().add(msg);
                    }
                }
                _addVmsSucceded = returnValue.getSucceeded() && _addVmsSucceded;
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
            isAtLeastOneVMCreationFailed = isAtLeastOneVMCreationFailed || !_addVmsSucceded;
        }
        getReturnValue().setCanDoAction(!isAtLeastOneVMCreationFailed);
        setSucceeded(!isAtLeastOneVMCreationFailed);
        VmTemplateHandler.UnLockVmTemplate(getParameters().getVmStaticData().getVmtGuid());
        getCompensationContext().resetCompensation();
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
            commandCtx = new CommandContext(ctx);
        } catch (RuntimeException e) {
            log.errorFormat("Failed to create command context of adding VM {0} to Pool {1}",
                    currentVmName,
                    getParameters().getVmPool().getName(),
                    e);
        }
        return commandCtx;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__DESKTOP_POOL);
    }

    @Override
    protected boolean canDoAction() {
        VDSGroup grp = getVdsGroupDAO().get(getParameters().getVmPool().getVdsGroupId());
        if (grp == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        if (!isMemorySizeLegal(grp.getcompatibility_version())) {
            return false;
        }

        VmPool pool = getVmPoolDAO().getByName(getParameters().getVmPool().getName());
        if (pool != null
                && (getActionType() == VdcActionType.AddVmPoolWithVms || !pool.getVmPoolId().equals(
                        getParameters().getVmPoolId()))) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }
        setStoragePoolId(grp.getStoragePoolId().getValue());

        if (getStoragePool() == null || getStoragePool().getstatus() != StoragePoolStatus.Up) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
        }

        if (!verifyAddVM()) {
            return false;
        }

        if (!ensureDestinationImageMap()) {
            return false;
        }
        storageToDisksMap = ImagesHandler.buildStorageToDiskMap(getVmTemplate().getDiskMap().values(),
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
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_WITH_NO_VMS);
            return false;
        }

        if (getParameters().getVmStaticData().isStateless()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
            return false;
        }

        if (getParameters().getVmPool().getPrestartedVms() > getParameters().getVmPool().getAssignedVmsCount()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_PRESTARTED_VMS_CANNOT_EXCEED_VMS_COUNT);
            return false;
        }

        return checkFreeSpaceAndTypeOnDestDomains();
    }

    protected boolean isMemorySizeLegal(Version version) {
        VmStatic vmStaticData = getParameters().getVmStaticData();
        return VmHandler.isMemorySizeLegal
                (vmStaticData.getOs(),
                        vmStaticData.getMemSizeMb(),
                        getReturnValue().getCanDoActionMessages(),
                        version.toString());
    }

    protected boolean verifyAddVM() {
        return VmHandler.VerifyAddVm
                (getReturnValue().getCanDoActionMessages(),
                        getParameters().getVmsCount()
                                * getVmNetworkInterfaceDao().getAllForTemplate(getVmTemplateId()).size(),
                        getParameters().getVmStaticData().getPriority());
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
                for (DiskImage image : getVmTemplate().getDiskMap().values()) {
                    image.setStorageIds(storageIds);
                    diskInfoDestinationMap.put(image.getId(), image);
                }
            } else {
                ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                        diskInfoDestinationMap,
                        destStorages, false);
            }
        }
        if (getVmTemplate().getDiskMap().values().size() != diskInfoDestinationMap.size()) {
            log.errorFormat("Can not found any default active domain for one of the disks of template with id : {0}",
                    getVmTemplate().getId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            return false;
        }
        return true;
    }

    public boolean checkFreeSpaceAndTypeOnDestDomains() {
        boolean retValue = true;
        List<Guid> validDomains = new ArrayList<Guid>();
        for (DiskImage diskImage : diskInfoDestinationMap.values()) {
            Guid domainId = diskImage.getStorageIds().get(0);
            if (validDomains.contains(domainId)) {
                continue;
            }
            StorageDomain domain = destStorages.get(domainId);
            if (domain == null) {
                domain = getStorageDomainDAO().getForStoragePool(domainId, getVmTemplate().getstorage_pool_id());
            }
            int numOfDisksOnDomain = 0;
            if (storageToDisksMap.containsKey(domainId)) {
                numOfDisksOnDomain = storageToDisksMap.get(domainId).size();
            }
            if (numOfDisksOnDomain > 0) {
                if (domain.getStorageDomainType() == StorageDomainType.ImportExport) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    retValue = false;
                    break;
                }
                if (!doesStorageDomainhaveSpaceForRequest(domain, numOfDisksOnDomain
                        * getBlockSparseInitSizeInGB() * getParameters().getVmsCount())) {
                    return false;
                }
            }
            validDomains.add(domainId);
        }
        return retValue;
    }

    protected boolean doesStorageDomainhaveSpaceForRequest(StorageDomain storageDomain, long sizeRequested) {
        return validate(new StorageDomainValidator(storageDomain).isDomainHasSpaceForRequest(sizeRequested));
    }

    private int getBlockSparseInitSizeInGB() {
        return Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    protected boolean getAddVmsSucceded() {
        return _addVmsSucceded;
    }

    /**
     * Check if the name of the VM-Pool has valid length, meaning it's not too long.
     * Since VMs in a pool are named like: 'SomePool_22', the max length allowed for the name is the max VM-name length
     * + room for the suffix: <Max Length of VM name> - (length(<MaxVmsInPool>) + 1)
     * In deciding the max length for a VM name, take into consideration if it's a Windows or non-Windows VM
     * @param vmPoolName
     *            name of pool
     * @return true if name has valid length; false if the name is too long
     */
    protected boolean isVmPoolNameValidLength(String vmPoolName) {

        // get VM-pool OS type
        VmOsType osType = getParameters().getVmStaticData().getOs();

        // determine the max length considering the OS and the max-VMs-in-pool
        // get the max VM name (configuration parameter)
        int maxVmNameLengthWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthWindows);
        int maxVmNameLengthNonWindows = Config.<Integer> GetValue(ConfigValues.MaxVmNameLengthNonWindows);

        int maxLength = osType.isWindows() ? maxVmNameLengthWindows : maxVmNameLengthNonWindows;
        Integer maxVmsInPool = Config.GetValue(ConfigValues.MaxVmsInPool);
        maxLength -= (String.valueOf(maxVmsInPool).length() + 1);

        // check if name is valid
        boolean nameLengthValid = (vmPoolName.length() <= maxLength);

        // return the result
        return nameLengthValid;
    }

    public String getVmsCount() {
        return Integer.toString(getParameters().getVmsCount());
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
