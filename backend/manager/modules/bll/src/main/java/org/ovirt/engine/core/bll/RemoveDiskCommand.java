package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
@NonTransactiveCommandAttribute
public class RemoveDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T>
        implements QuotaStorageDependent {

    private static final long serialVersionUID = -4520874214339816607L;
    private Disk disk;
    private List<PermissionSubject> permsList = null;
    private List<VM> listVms;
    private String cachedDiskIsBeingRemovedLockMessage;

    public RemoveDiskCommand(T parameters) {
        super(parameters);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        if (getDisk() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }

        return validateAllVmsForDiskAreDown() &&
                canRemoveDiskBasedOnStorageTypeCheck();
    }

    private boolean validateAllVmsForDiskAreDown() {
        if (getDisk().getVmEntityType() == VmEntityType.VM) {
            for (VM vm : getVmsForDiskId()) {
                if (vm.getStatus() != VMStatus.Down) {
                    VmDevice vmDevice = getVmDeviceDAO().get(new VmDeviceId(getDisk().getId(), vm.getId()));
                    if (vmDevice.getIsPlugged()) {
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean canRemoveDiskBasedOnStorageTypeCheck() {
        // currently, only images have specific checks.
        // In the future, if LUNs get specific checks,
        // or additional storage types are added, other else-if clauses should be added.
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            return canRemoveDiskBasedOnImageStorageCheck();
        }

        return true;
    }

    private boolean canRemoveDiskBasedOnImageStorageCheck() {
        boolean retValue = true;
        DiskImage diskImage = getDiskImage();
        if (diskImage.getVmEntityType() == VmEntityType.TEMPLATE) {
            // Temporary fix until re factoring vm_images_view and image_storage_domain_view
            diskImage.setStorageIds(getDiskImageDao().get(diskImage.getImageId()).getStorageIds());
        } else if ((getParameters().getStorageDomainId() == null) || (Guid.Empty.equals(getParameters().getStorageDomainId()))) {
            getParameters().setStorageDomainId(diskImage.getStorageIds().get(0));
            setStorageDomainId(diskImage.getStorageIds().get(0));
        }

        if (!diskImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STOARGE_DOMAIN_IS_WRONG);
        }

        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        retValue =
                retValue && validate(validator.isDomainExistAndActive())
                        && validate(validator.domainIsValidDestination());

        if (retValue && diskImage.getImageStatus() == ImageStatus.LOCKED) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
        }
        if (retValue) {
            if (getDisk().getVmEntityType() == VmEntityType.VM) {
                retValue = canRemoveVmImageDisk();
            } else if (getDisk().getVmEntityType() == VmEntityType.TEMPLATE) {
                retValue = canRemoveTemplateDisk();
            }
        }

        return retValue;
    }

    /**
     * Set the parent parameter vmTemplateId, based on the disk image id.
     */
    private void setVmTemplateIdParameter() {
        Map<Boolean, VmTemplate> templateMap =
                // Disk image is the only disk type that can be part of the template disks.
                getDbFacade().getVmTemplateDao().getAllForImage(getDiskImage().getImageId());

        if (!templateMap.isEmpty()) {
            setVmTemplateId(templateMap.values().iterator().next().getId());
        }
    }

    /**
     * Cache method to retrieve all the VMs related to image
     * @return List of Vms.
     */
    private List<VM> getVmsForDiskId() {
        if (listVms == null) {
            listVms = getVmDAO().getVmsListForDisk((Guid) getParameters().getEntityId());
        }
        return listVms;
    }

    private boolean canRemoveTemplateDisk() {
        boolean retValue = true;
        DiskImage diskImage = getDiskImage();
        if (getVmTemplate().getstatus() == VmTemplateStatus.Locked) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }
        if (retValue && diskImage.getStorageIds().size() == 1) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_LAST_DOMAIN);
        }
        if (retValue) {
            List<String> problematicVmNames = new ArrayList<String>();
            List<VM> vms = DbFacade.getInstance().getVmDao().getAllWithTemplate(getVmTemplateId());
            for (VM vm : vms) {
                List<Disk> vmDisks = DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId());
                for (Disk vmDisk : vmDisks) {
                    if (vmDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
                        DiskImage vmDiskImage = (DiskImage) vmDisk;
                        if (vmDiskImage.getImageTemplateId().equals(diskImage.getImageId())) {
                            if (vmDiskImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
                                retValue = false;
                                problematicVmNames.add(vm.getName());
                            }
                            break;
                        }
                    }
                }
            }
            if (!retValue) {
                addCanDoActionMessage(VdcBllMessages.VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM);
                addCanDoActionMessage(String.format("$vmsList %1$s", StringUtils.join(problematicVmNames, ",")));
            }
        }
        return retValue;
    }

    private boolean canRemoveVmImageDisk() {
        List<Disk> diskList = Arrays.asList(getDisk());

        if (!listVms.isEmpty()) {
            Guid storagePoolId = listVms.get(0).getStoragePoolId();
            storage_pool sp = getStoragePoolDAO().get(storagePoolId);
            if (!validate(new StoragePoolValidator(sp).isUp())) {
                return false;
            }

            if (!ImagesHandler.PerformImagesChecks(
                    getReturnValue().getCanDoActionMessages(),
                    storagePoolId,
                    getParameters().getStorageDomainId(),
                    false,
                    true,
                    false,
                    false,
                    false,
                    true,
                    diskList)) {
                return false;
            }
        }

        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        for (VM vm : listVms) {
            if (!validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId())) ||
                    !validate(snapshotsValidator.vmNotInPreview(vm.getId()))) {
                return false;
            }
        }
        return true;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return DbFacade.getInstance()
                .getVmDeviceDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    @Override
    protected void executeCommand() {
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = getDiskImage();
            RemoveImageParameters p = new RemoveImageParameters(diskImage.getImageId());
            p.setTransactionScopeOption(TransactionScopeOption.Suppress);
            p.setDiskImage(diskImage);
            p.setParentCommand(VdcActionType.RemoveDisk);
            p.setRemoveDuringExecution(false);
            p.setEntityId(getParameters().getEntityId());
            p.setParentParameters(getParameters());
            p.setStorageDomainId(getParameters().getStorageDomainId());
            p.setForceDelete(getParameters().getForceDelete());
            if (diskImage.getStorageIds().size() == 1) {
                p.setRemoveFromDB(true);
            }
            VdcReturnValueBase vdcReturnValue =
                            Backend.getInstance().runInternalAction(VdcActionType.RemoveImage,
                                    p,
                                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if (vdcReturnValue.getSucceeded()) {
                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
                setSucceeded(vdcReturnValue.getSucceeded());
            }
        } else {
            removeLunDisk();
        }
    }

    private void removeLunDisk() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                ImagesHandler.removeLunDisk((LunDisk) getDisk());
                return null;
            }
        });
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        endCommand();
    }

    @Override
    protected void endWithFailure() {
        endCommand();
    }

    private void endCommand() {
        List<VM> listVms = getVmsForDiskId();
        for (VM vm : listVms) {
            getVmStaticDAO().incrementDbGeneration(vm.getId());
        }
        // Get the disk before it is being deleted to use the disk alias in the audit log.
        getDisk();
        Backend.getInstance().EndAction(VdcActionType.RemoveImage, getParameters().getImagesParameters().get(0));
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
                return getSucceeded() ? AuditLogType.USER_REMOVE_DISK : AuditLogType.USER_FAILED_REMOVE_DISK;
            } else {
                return getSucceeded() ? AuditLogType.USER_FINISHED_REMOVE_DISK
                        : AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK;
            }
        case END_SUCCESS:
            return AuditLogType.USER_FINISHED_REMOVE_DISK;
        default:
            return AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null && getDisk() != null) {
            permsList = new ArrayList<PermissionSubject>();
            permsList.add(new PermissionSubject(getDisk().getId(),
                    VdcObjectType.Disk,
                    ActionGroup.DELETE_DISK));
        }
        return permsList;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingRemovedLockMessage()));
    }

    private String getDiskIsBeingRemovedLockMessage() {
        if (cachedDiskIsBeingRemovedLockMessage == null) {
            cachedDiskIsBeingRemovedLockMessage = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_BEING_REMOVED.name())
            .append(String.format("$DiskName %1$s", getDiskAlias()))
            .toString();
        }
        return cachedDiskIsBeingRemovedLockMessage;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getDisk() == null) {
            return null;
        }

        Map<String, Pair<String, String>> result = null;
        if (getDisk().getVmEntityType() == VmEntityType.VM) {
            List<VM> listVms = getVmsForDiskId();
            if (!listVms.isEmpty()) {
                result = new HashMap<String, Pair<String, String>>();
                for (VM vm : listVms) {
                    result.put(vm.getId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingRemovedLockMessage()));
                }
            }
        } else if (getDisk().getVmEntityType() == VmEntityType.TEMPLATE) {
            setVmTemplateIdParameter();
            result = Collections.singletonMap(getVmTemplateId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getDiskIsBeingRemovedLockMessage()));
        }
        return result;
    }

    protected Disk getDisk() {
        if (disk == null) {
            disk = getDiskDao().get((Guid) getParameters().getEntityId());
        }

        return disk;
    }

    protected DiskImage getDiskImage() {
        return (DiskImage) getDisk();
    }

    public String getDiskAlias() {
        if (getDisk() != null) {
            return getDisk().getDiskAlias();
        }
        return "";
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        if (getDisk() != null
                && DiskStorageType.IMAGE == getDisk().getDiskStorageType()
                && getQuotaId() != null
                && !Guid.Empty.equals(getQuotaId())) {
            list.add(new QuotaStorageConsumptionParameter(
                    getQuotaId(),
                    null,
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getStorageDomainId().getValue(),
                    (double) getDiskImage().getSizeInGigabytes()));
        }
        return list;
    }

    private Guid getQuotaId() {
        if (getDisk() != null
                && DiskStorageType.IMAGE == getDisk().getDiskStorageType()) {
            return getDiskImage().getQuotaId();
        }
        return null;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
    }
}
