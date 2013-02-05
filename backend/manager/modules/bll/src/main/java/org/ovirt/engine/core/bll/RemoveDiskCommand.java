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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class RemoveDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T>
        implements QuotaStorageDependent {

    private static final long serialVersionUID = -4520874214339816607L;
    private Disk disk;
    private Map<String, Pair<String, String>> sharedLockMap;
    private List<PermissionSubject> permsList = null;
    private List<VM> listVms;

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

        buildSharedLockMap();

        return acquireLockInternal() &&
                validateAllVmsForDiskAreDown() &&
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
            diskImage.setstorage_ids(getDiskImageDao().get(diskImage.getImageId()).getstorage_ids());
        } else if ((getParameters().getStorageDomainId() == null) || (Guid.Empty.equals(getParameters().getStorageDomainId()))) {
            getParameters().setStorageDomainId(diskImage.getstorage_ids().get(0));
            setStorageDomainId(diskImage.getstorage_ids().get(0));
        }

        if (!diskImage.getstorage_ids().contains(getParameters().getStorageDomainId())) {
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

    private void buildSharedLockMap() {
        if (getDisk().getVmEntityType() == VmEntityType.VM) {
            List<VM> listVms = getVmsForDiskId();
            if (!listVms.isEmpty()) {
                sharedLockMap = new HashMap<String, Pair<String, String>>();
                for (VM vm : listVms) {
                    sharedLockMap.put(vm.getId().toString(), LockMessagesMatchUtil.VM);
                }
            }
        } else if (getDisk().getVmEntityType() == VmEntityType.TEMPLATE) {
            setVmTemplateIdParameter();
            sharedLockMap = Collections.singletonMap(getVmTemplateId().toString(), LockMessagesMatchUtil.TEMPLATE);
        }
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
        if (retValue && diskImage.getstorage_ids().size() == 1) {
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
                        if (vmDiskImage.getit_guid().equals(diskImage.getImageId())) {
                            if (vmDiskImage.getstorage_ids().contains(getParameters().getStorageDomainId())) {
                                retValue = false;
                                problematicVmNames.add(vm.getVmName());
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
        boolean firstTime = true;
        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        List<Disk> diskList = Arrays.asList(getDisk());

        if (!listVms.isEmpty()) {
            storage_pool sp = getStoragePoolDAO().get(listVms.get(0).getStoragePoolId());
            if (!validate(new StoragePoolValidator(sp).isUp())) {
                return false;
            }
        }

        for (VM vm : listVms) {
            if (!validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId())) ||
                    !validate(snapshotsValidator.vmNotInPreview(vm.getId())) ||
                    !ImagesHandler.PerformImagesChecks(
                            getReturnValue().getCanDoActionMessages(),
                            vm.getStoragePoolId(),
                            getParameters().getStorageDomainId(),
                            false,
                            firstTime,
                            false,
                            false,
                            false,
                            firstTime,
                            diskList)) {
                return false;
            }
            firstTime = false;
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
            if (diskImage.getstorage_ids().size() == 1) {
                p.setRemoveFromDB(true);
            }
            VdcReturnValueBase vdcReturnValue =
                            Backend.getInstance().runInternalAction(VdcActionType.RemoveImage,
                                    p,
                                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext(), getLock()));
            // Setting lock to null because the lock is released in the child command (RemoveImage)
            setLock(null);
            if (vdcReturnValue.getSucceeded()) {
                getParameters().getImagesParameters().add(p);
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
        Backend.getInstance().EndAction(VdcActionType.RemoveImage, getParameters().getImagesParameters().get(0));
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_REMOVE_DISK : AuditLogType.USER_FAILED_REMOVE_DISK;
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
        return Collections.singletonMap(getParameters().getEntityId().toString(), LockMessagesMatchUtil.DISK);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return sharedLockMap;
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
