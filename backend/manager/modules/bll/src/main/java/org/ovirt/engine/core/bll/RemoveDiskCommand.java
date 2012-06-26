package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
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
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
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

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class RemoveDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T> {

    private static final long serialVersionUID = -4520874214339816607L;
    private final Disk disk;
    private Map<Guid, String> sharedLockMap;
    private List<PermissionSubject> permsList = null;
    private List<VM> listVms;

    public RemoveDiskCommand(T parameters) {
        super(parameters);
        setStorageDomainId(getParameters().getStorageDomainId());
        disk = getDiskDao().get((Guid) getParameters().getEntityId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;

        if (disk == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }

        if (retValue) {
            buildSharedLockMap();
            retValue = acquireLockInternal();
        }

        retValue =
                retValue
                        && (disk.getDiskStorageType() != DiskStorageType.IMAGE || canRemoveDiskBasedOnImageStorageCheck());
        return retValue;
    }

    private boolean canRemoveDiskBasedOnImageStorageCheck() {
        boolean retValue = true;
        DiskImage diskImage = (DiskImage) disk;
        if (diskImage.getVmEntityType() == VmEntityType.TEMPLATE) {
            // Temporary fix until re factoring vm_images_view and image_storage_domain_view
            diskImage.setstorage_ids(getDiskImageDAO().get(diskImage.getImageId()).getstorage_ids());
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
                retValue && validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages())
                        && validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages());

        if (retValue && diskImage.getimageStatus() == ImageStatus.LOCKED) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
        }
        if (retValue) {
            if (disk.getVmEntityType() == VmEntityType.VM) {
                retValue = canRemoveVmImageDisk();
            } else if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
                retValue = canRemoveTemplateDisk();
            }
        }

        return retValue;
    }

    private void buildSharedLockMap() {
        if (disk.getVmEntityType() == VmEntityType.VM) {
            List<VM> listVms = getVmsForDiskId();
            if (!listVms.isEmpty()) {
                sharedLockMap = new HashMap<Guid, String>();
                for (VM vm : listVms) {
                    sharedLockMap.put(vm.getId(), LockingGroup.VM.name());
                }
            }
        } else if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
            sharedLockMap = Collections.singletonMap(disk.getvm_guid(), LockingGroup.TEMPLATE.name());
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
        DiskImage diskImage = (DiskImage) disk;
        setVmTemplateId(diskImage.getvm_guid());
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
            List<VM> vms = DbFacade.getInstance().getVmDAO().getAllWithTemplate(getVmTemplateId());
            for (VM vm : vms) {
                List<Disk> vmDisks = DbFacade.getInstance().getDiskDao().getAllForVm(vm.getId());
                for (Disk vmDisk : vmDisks) {
                    if (vmDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
                        DiskImage vmDiskImage = (DiskImage) vmDisk;
                        if (vmDiskImage.getit_guid().equals(diskImage.getImageId())) {
                            if (vmDiskImage.getstorage_ids().contains(getParameters().getStorageDomainId())) {
                                retValue = false;
                                problematicVmNames.add(vm.getvm_name());
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
        setVmId(disk.getvm_guid());
        boolean validDisktoDelete = true;
        int vmCount = 0;
        while (getVmsForDiskId().size() > vmCount && validDisktoDelete) {
            VM vm = listVms.get(vmCount++);
            VmDevice vmDevice = getVmDeviceDAO().get(new VmDeviceId(disk.getId(), vm.getId()));
            validDisktoDelete = validate(new SnapshotsValidator().vmNotDuringSnapshot(vm.getId()));
            // Validate image only in the first image of the iteration since we check the same image every time.
            validDisktoDelete = validDisktoDelete && ImagesHandler.PerformImagesChecks(vm,
                    getReturnValue().getCanDoActionMessages(),
                    vm.getstorage_pool_id(),
                    getParameters().getStorageDomainId(),
                    false,
                    vmCount == 1,
                    false,
                    false,
                    vmDevice.getIsPlugged() && disk.isAllowSnapshot(),
                    vmDevice.getIsPlugged(),
                    false,
                    vmCount == 1,
                    Arrays.asList(disk));
        }
        return validDisktoDelete;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return DbFacade.getInstance()
                .getVmDeviceDAO();
    }

    protected DiskImageDAO getDiskImageDAO() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    @Override
    protected void executeCommand() {
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            RemoveImageParameters p = new RemoveImageParameters(diskImage.getImageId());
            p.setTransactionScopeOption(TransactionScopeOption.Suppress);
            p.setDiskImage(diskImage);
            p.setParentCommand(VdcActionType.RemoveDisk);
            p.setEntityId(getParameters().getEntityId());
            p.setParentParemeters(getParameters());
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
                ImagesHandler.removeLunDisk((LunDisk) disk);
                return null;
            }
        });
        setSucceeded(true);
    }

    @Override
    protected void EndSuccessfully() {
        endCommand();
    }

    @Override
    protected void EndWithFailure() {
        endCommand();
    }

    private void endCommand() {
        List<VM> listVms = getVmsForDiskId();
        Backend.getInstance().EndAction(VdcActionType.RemoveImage, getParameters().getImagesParameters().get(0));
        VmCommand.UpdateVmInSpm(getStoragePoolId().getValue(), listVms);
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
        if (permsList == null && disk != null) {
            permsList = new ArrayList<PermissionSubject>();
            permsList.add(new PermissionSubject(disk.getId(),
                    VdcObjectType.Disk,
                    ActionGroup.DELETE_DISK));
        }
        return permsList;
    }

    @Override
    protected Map<Guid, String> getExclusiveLocks() {
        return Collections.singletonMap((Guid) getParameters().getEntityId(), LockingGroup.DISK.name());
    }

    @Override
    protected Map<Guid, String> getSharedLocks() {
        return sharedLockMap;
    }

    public String getDiskAlias() {
        if (disk != null) {
            return disk.getDiskAlias();
        }
        return "";
    }
}
