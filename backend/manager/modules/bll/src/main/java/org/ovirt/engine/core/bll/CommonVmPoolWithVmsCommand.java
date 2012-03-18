package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;

/**
 * This class responsible to create vmpool with vms within. This class not transactive, that mean that function Execute
 * not running in transaction. From other hand, each vm added to system and attached to vmpool in transaction(one
 * transaction for two operation). To make it work, Transaction generated in Execute function. Transactions isolated,
 * that mean if one of vms not added from some reason(image not exists, etc) - it not affect other vms generation Each
 * vm created with this format: {vm_name}_{number} where number runs from 1 to vms count. If one of vms to be created
 * already exists - number increased. For example if vm_8 exists - vm_9 will be created instead of it.
 */

@CustomLogFields({ @CustomLogField("VmsCount") })
public abstract class CommonVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends AddVmPoolCommand<T> {

    private HashMap<Guid, Guid> imageToDestinationDomainMap;
    protected Map<Guid, List<DiskImage>> storageToDisksMap;
    protected Map<Guid, storage_domains> destStorages = new HashMap<Guid, storage_domains>();
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
        setVmTemplateId(getParameters().getVmStaticData().getvmt_guid());
        if (getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        }
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        if(imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<Guid, Guid>();
        }
    }

    protected abstract Guid GetPoolId();

    /**
     * This operation may take much time, so transactions timeout increased to 2 minutes
     */
    @Override
    protected void executeCommand() {
        Guid poolId = GetPoolId();
        boolean isAtLeastOneVMCreationFailed = false;
        setActionReturnValue(poolId);

        VmTemplateHandler.lockVmTemplateInTransaction(getParameters().getVmStaticData().getvmt_guid(),
                getCompensationContext());

        String vmName = getParameters().getVmStaticData().getvm_name();
        int numChars = (Integer.toString(getParameters().getVmsCount())).length();
        for (int i = 1, j = 1; i <= getParameters().getVmsCount(); i++, j++) {
            String currentVmName;
            j--;
            do {
                j++;
                int curChars = ((Integer) j).toString().length();
                StringBuilder number = new StringBuilder();
                for (int k = 0; k < numChars - curChars; k++) {
                    number.append('0');
                }
                number.append(j);
                currentVmName = String.format("%1$s-%2$s", vmName, number);
            } while ((Boolean) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                            new IsVmWithSameNameExistParameters(currentVmName)).getReturnValue());

            VmStatic currVm = new VmStatic(getParameters().getVmStaticData());
            currVm.setvm_name(currentVmName);
            AddVmAndAttachToPoolParameters addVmAndAttachToPoolParams = new AddVmAndAttachToPoolParameters(currVm, poolId, currentVmName,
                    imageToDestinationDomainMap);
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
            }

            isAtLeastOneVMCreationFailed = isAtLeastOneVMCreationFailed || !_addVmsSucceded;
        }
        getReturnValue().setCanDoAction(!isAtLeastOneVMCreationFailed);
        setSucceeded(!isAtLeastOneVMCreationFailed);
        VmTemplateHandler.UnLockVmTemplate(getParameters().getVmStaticData().getvmt_guid());
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
                    getParameters().getVmPool().getvm_pool_name(),
                    e);
        }
        return commandCtx;
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__DESKTOP_POOL);
        if (!super.canDoAction()) {
            return false;
        }

        String vmPoolName = getParameters().getVmPool().getvm_pool_name();
        if (vmPoolName == null || vmPoolName.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
            return false;
        } else if (!isVmPoolNameValidLength(vmPoolName)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            return false;
        } else if (ValidationUtils.containsIlegalCharacters(vmPoolName)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS);
            return false;
        }

        VDSGroup grp = DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getVmPool().getvds_group_id());
        if (grp == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
            return false;
        }

        VmStatic vmStaticData = getParameters().getVmStaticData();
        if (!VmHandler.isMemorySizeLegal(vmStaticData.getos(), vmStaticData.getmem_size_mb(),
                getReturnValue().getCanDoActionMessages(), grp.getcompatibility_version().toString())) {
            return false;
        }

        vm_pools pool =
                DbFacade.getInstance().getVmPoolDAO().getByName(getParameters().getVmPool().getvm_pool_name());
        if (pool != null
                && (getActionType() == VdcActionType.AddVmPoolWithVms || !pool.getvm_pool_id().equals(
                        getParameters().getVmPoolId()))) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_DUPLICATE_NAME);
            return false;
        }

        if (!((Boolean) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.IsValid,
                        new IrsBaseVDSCommandParameters(grp.getstorage_pool_id().getValue())).getReturnValue())
                .booleanValue()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
            return false;
        }

        if (!VmHandler.VerifyAddVm(getReturnValue().getCanDoActionMessages(),
                getParameters().getVmsCount(),
                getVmTemplate(),
                grp.getstorage_pool_id().getValue(),
                getParameters().getVmStaticData().getpriority())) {
            return false;
        }

        if(!ensureDomainMap()) {
            return false;
        }
        storageToDisksMap = ImagesHandler.buildStorageToDiskMap(getVmTemplate().getDiskMap().values(),
                imageToDestinationDomainMap);
        for (Guid storageId : new HashSet<Guid>(imageToDestinationDomainMap.values())) {
            if (!VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(),
                    storageId,
                    getReturnValue().getCanDoActionMessages(),
                    false,
                    true,
                    true,
                    destStorages.isEmpty(),
                    storageToDisksMap.get(storageId))) {
                return false;
            }
        }

        if (getActionType() == VdcActionType.AddVmPoolWithVms && getParameters().getVmsCount() < 1) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_WITH_NO_VMS);
            return false;
        }

        if (getParameters().getVmStaticData().getis_stateless()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
            return false;
        }

        if (getParameters().getVmPool().getPrestartedVms() > getParameters().getVmPool().getvm_assigned_count()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_PRESTARTED_VMS_CANNOT_EXCEED_VMS_COUNT);
            return false;
        }

        return checkFreeSpaceAndTypeOnDestDomains();
    }

    private boolean ensureDomainMap() {
        if (imageToDestinationDomainMap.isEmpty()) {
            if (getParameters().getStorageDomainId() != null
                    && !Guid.Empty.equals(getParameters().getStorageDomainId())) {
                Guid storageId = getParameters().getStorageDomainId();
                for (DiskImage image : getVmTemplate().getDiskMap().values()) {
                    imageToDestinationDomainMap.put(image.getId(), storageId);
                }
            } else {
                ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                        imageToDestinationDomainMap,
                        destStorages);
            }
        }
        if (getVmTemplate().getDiskMap().values().size() != imageToDestinationDomainMap.size()) {
            log.errorFormat("Can not found any default active domain for one of the disks of template with id : {0}",
                    getVmTemplate().getId());
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
            return false;
        }
        return true;
    }

    public boolean checkFreeSpaceAndTypeOnDestDomains() {
        boolean retValue = true;
        for (Guid domainId : new HashSet<Guid>(imageToDestinationDomainMap.values())) {
            storage_domains domain = destStorages.get(domainId);
            if(domain == null) {
                domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domainId,
                    getVmTemplate().getstorage_pool_id());
            }
            int numOfDisksOnDomain = 0;
            if (storageToDisksMap.containsKey(domainId)) {
                numOfDisksOnDomain = storageToDisksMap.get(domainId).size();
            }
            if (numOfDisksOnDomain > 0) {
                if(domain.getstorage_domain_type() == StorageDomainType.ImportExport) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
                    retValue = false;
                    break;
                }
                if (!StorageDomainSpaceChecker.hasSpaceForRequest(domain, numOfDisksOnDomain
                            * getBlockSparseInitSizeInGB() * getParameters().getVmsCount())) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                    retValue = false;
                    break;
                }
            }
        }
        return retValue;
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
        VmOsType osType = getParameters().getVmStaticData().getos();

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
}
