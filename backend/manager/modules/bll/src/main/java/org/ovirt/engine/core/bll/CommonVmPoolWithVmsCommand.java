package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.action.AddVmAndAttachToPoolParameters;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.All;
import org.ovirt.engine.core.utils.linq.LinqUtils;

/**
 * This class responsible to create vmpool with vms within. This class not
 * transactive, that mean that function Execute not running in transaction. From
 * other hand, each vm added to system and attached to vmpool in transaction(one
 * transaction for two operation). To make it work, Transaction generated in
 * Execute function. Transactions isolated, that mean if one of vms not added
 * from some reason(image not exists, etc) - it not affect other vms generation
 * Each vm created with this format: {vm_name}_{number} where number runs from 1
 * to vms count. If one of vms to be created already exists - number increased.
 * For example if vm_8 exists - vm_9 will be created instead of it.
 */

@CustomLogFields({ @CustomLogField("VmsCount") })
public abstract class CommonVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends AddVmPoolCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
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
    }

    public int getVmsCount() {
        return getParameters().getVmsCount();
    }

    private boolean _addVmsSucceded = true;

    protected abstract Guid GetPoolId();

    /**
     * This operation may take much time, so transactions timeout increased to 2
     * minutes
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

            VmStatic tempVar = new VmStatic(getParameters().getVmStaticData());
            tempVar.setvm_name(currentVmName);
            VmStatic currVm = tempVar;
            AddVmAndAttachToPoolParameters tempVar2 = new AddVmAndAttachToPoolParameters(currVm, poolId, currentVmName,
                    getStorageDomainId().getValue());
            tempVar2.setSessionId(getParameters().getSessionId());
            tempVar2.setParentCommand(VdcActionType.AddVmPoolWithVms);
            VdcReturnValueBase returnValue = Backend.getInstance().runInternalAction(
                    VdcActionType.AddVmAndAttachToPool, tempVar2);
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

    public static boolean CanAddVmPoolWithVms(Object vmTemplateId, java.util.ArrayList<String> reasons, int vmsCount,
                                              Guid storagePoolId, Guid storageDomainId, int vmPriority) {
        return VmHandler.VerifyAddVm(reasons, vmsCount, vmTemplateId, storagePoolId, storageDomainId,
                true, true, vmPriority);
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

        if (!CanAddVmPoolWithVms(getParameters().getVmStaticData().getvmt_guid(), getReturnValue()
                        .getCanDoActionMessages(), getParameters().getVmsCount(), grp.getstorage_pool_id()
                        .getValue(), getStorageDomainId().getValue(), getParameters().getVmStaticData()
                        .getpriority())) {
            return false;
        }

        if (getActionType() == VdcActionType.AddVmPoolWithVms && getParameters().getVmsCount() < 1) {
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_WITH_NO_VMS);
            return false;
        }

        if (getParameters().getVmStaticData().getis_stateless()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
            return false;
        }

        return CheckFreeSpaceOnDestinationDomains();
    }

    public boolean CheckFreeSpaceOnDestinationDomains() {
        boolean retValue = true;
        VmTemplate vmTemplate = DbFacade.getInstance().getVmTemplateDAO()
                .get(getParameters().getVmStaticData().getvmt_guid());
        VmTemplateHandler.UpdateDisksFromDb(vmTemplate);
        double size = 0.0;
        java.util.ArrayList<Guid> domainsList;
        if (getStorageDomainId() == null || getStorageDomainId().getValue().equals(Guid.Empty)) {
            domainsList = (java.util.ArrayList<Guid>) Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageDomainsList,
                            new GetImageDomainsListVDSCommandParameters(vmTemplate.getstorage_pool_id().getValue(),
                                    vmTemplate.getDiskList().get(0).getimage_group_id().getValue())).getReturnValue();
        } else {
            domainsList = new java.util.ArrayList<Guid>();
            domainsList.add(getStorageDomainId().getValue());
        }
        for (Guid domainId : domainsList) {
            storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domainId,
                    vmTemplate.getstorage_pool_id());
            if (domain != null && domain.getstorage_domain_type() != StorageDomainType.ImportExport
                    && domain.getstatus() == StorageDomainStatus.Active && domain.getavailable_disk_size() != null &&
                    StorageDomainSpaceChecker.hasSpaceForRequest(domain, getBlockSparseInitSizeInGB())) {
                size += domain.getavailable_disk_size() - getFreeSpaceCriticalLowInGB();
            }
        }

        if (size < (getBlockSparseInitSizeInGB() * getParameters().getVmsCount() * vmTemplate.getDiskMap().size())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
        }
        return retValue;
    }

    private Integer getFreeSpaceCriticalLowInGB() {
        return Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
    }

    private int getBlockSparseInitSizeInGB() {
        return Config.<Integer> GetValue(ConfigValues.InitStorageSparseSizeInGB).intValue();
    }

    // TODO: Need to think of balancing between vms of pool over storage domains
    @Override
    public NGuid getStorageDomainId() {
        if (getParameters().getStorageDomainId().equals(Guid.Empty) && getVmTemplate() != null
                // LINQ && VmTemplate.DiskMap.Values.First().image_guid !=
                // VmTemplateHandler.BlankVmTemplateId)
                && !LinqUtils.firstOrNull(getVmTemplate().getDiskMap().values(), new All<DiskImageTemplate>())
                        .getId().equals(VmTemplateHandler.BlankVmTemplateId)) {
            getParameters().setStorageDomainId(AddVmCommand.SelectStorageDomain(getVmTemplate()));
        }
        return getParameters().getStorageDomainId();
    }

    protected boolean getAddVmsSucceded() {
        return _addVmsSucceded;
    }

    /**
     * Check if the name of the VM-Pool has valid length, meaning it's not too
     * long.
     *
     * Since VMs in a pool are named like: 'SomePool_22', the max length allowed
     * for the name is the max VM-name length + room for the suffix: <Max Length
     * of VM name> - (length(<MaxVmsInPool>) + 1)
     *
     * In deciding the max length for a VM name, take into consideration if it's
     * a Windows or non-Windows VM
     *
     * @param vmPoolName
     *            name of pool
     *
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
