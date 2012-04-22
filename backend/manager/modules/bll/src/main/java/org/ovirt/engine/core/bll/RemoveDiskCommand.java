package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@NonTransactiveCommandAttribute
public class RemoveDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T> {

    private static final long serialVersionUID = -4520874214339816607L;
    private DiskImage disk;
    private Map<String, Guid> sharedLockMap;

    public RemoveDiskCommand(T parameters) {
        super(parameters);
        setStorageDomainId(getParameters().getStorageDomainId());
        disk = getDiskImageDAO().getSnapshotById((Guid) getParameters().getEntityId());
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

        if (retValue && disk.getVmEntityType() == VmEntityType.TEMPLATE) {
            // Temporary fix until re factoring vm_images_view and image_storage_domain_view
            disk.setstorage_ids(getDiskImageDAO().get(disk.getId()).getstorage_ids());
        }

        if (retValue
                && disk.getVmEntityType() == VmEntityType.VM
                && (getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId()))) {
            getParameters().setStorageDomainId(disk.getstorage_ids().get(0));
            setStorageDomainId(disk.getstorage_ids().get(0));
        }

        if (retValue && !disk.getstorage_ids().contains(getParameters().getStorageDomainId())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STOARGE_DOMAIN_IS_WRONG);
        }

        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        retValue =
                retValue && validator.isDomainExistAndActive(getReturnValue().getCanDoActionMessages())
                        && validator.domainIsValidDestination(getReturnValue().getCanDoActionMessages());

        if (retValue && disk.getimageStatus() == ImageStatus.LOCKED) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
        }
        if (retValue) {
            if (disk.getVmEntityType() == VmEntityType.VM) {
                retValue = canRemoveVmDisk();
            } else if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
                retValue = canRemoveTemplateDisk();
            }
        }

        return retValue;
    }

    private void buildSharedLockMap() {
        if (disk.getVmEntityType() == VmEntityType.VM) {
            sharedLockMap = Collections.singletonMap(LockingGroup.VM.name(), disk.getvm_guid());
        } else if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
            sharedLockMap = Collections.singletonMap(LockingGroup.TEMPLATE.name(), disk.getvm_guid());
        }
    }

    private boolean canRemoveTemplateDisk() {
        boolean retValue = true;
        setVmTemplateId(disk.getvm_guid());
        if (getVmTemplate().getstatus() == VmTemplateStatus.Locked) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }
        if (retValue && disk.getstorage_ids().size() == 1) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_LAST_DOMAIN);
        }
        if (retValue) {
            List<String> problematicVmNames = new ArrayList<String>();
            List<VM> vms = DbFacade.getInstance().getVmDAO().getAllWithTemplate(getVmTemplateId());
            for (VM vm : vms) {
                List<DiskImage> vmDisks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(vm.getId());
                for (DiskImage vmDisk : vmDisks) {
                    if (vmDisk.getit_guid().equals(disk.getImageId())) {
                        if (vmDisk.getstorage_ids().contains(getParameters().getStorageDomainId())) {
                            retValue = false;
                            problematicVmNames.add(vm.getvm_name());
                        }
                        break;
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

    private boolean canRemoveVmDisk() {
        setVmId(disk.getvm_guid());
        VmDevice vmDevice = getVmDeviceDAO().get(new VmDeviceId(disk.getimage_group_id(), disk.getvm_guid()));
        return validate(new SnapshotsValidator().vmNotDuringSnapshot(getVmId()))
                        && ImagesHandler.PerformImagesChecks(getVm(),
                                getReturnValue().getCanDoActionMessages(),
                                getVm().getstorage_pool_id(),
                                getParameters().getStorageDomainId(),
                                false,
                                true,
                                false,
                                false,
                                vmDevice.getIsPlugged(),
                                vmDevice.getIsPlugged(),
                                false,
                                true,
                                Arrays.asList(disk));
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return DbFacade.getInstance()
                .getVmDeviceDAO();
    }

    protected DiskImageDAO getDiskImageDAO() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    @Override
    protected void executeCommand() {
        RemoveImageParameters p = new RemoveImageParameters(disk.getImageId(), getVmId());
        p.setTransactionScopeOption(TransactionScopeOption.Suppress);
        p.setDiskImage(disk);
        p.setParentCommand(VdcActionType.RemoveDisk);
        p.setEntityId(getParameters().getEntityId());
        p.setParentParemeters(getParameters());
        p.setStorageDomainId(getParameters().getStorageDomainId());
        p.setForceDelete(getParameters().getForceDelete());
        if (disk.getstorage_ids().size() == 1) {
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
        Backend.getInstance().EndAction(VdcActionType.RemoveImage, getParameters().getImagesParameters().get(0));
        setVmId(((RemoveImageParameters) getParameters().getImagesParameters().get(0)).getContainerId());
        if (getVm() != null && getVm().getstatus() == VMStatus.Down) {
            VmCommand.UpdateVmInSpm(getVm().getstorage_pool_id(),
                    Arrays.asList(getVm()));
        }
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
        if (disk != null) {
            if (disk.getVmEntityType() == VmEntityType.VM) {
                return Collections.singletonList(new PermissionSubject(disk.getvm_guid(),
                        VdcObjectType.VM,
                        ActionGroup.CONFIGURE_VM_STORAGE));
            }
            if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
                return Collections.singletonList(new PermissionSubject(disk.getvm_guid(),
                        VdcObjectType.VmTemplate,
                        ActionGroup.DELETE_TEMPLATE));
            }
        }
        return null;
    }

    @Override
    protected Map<String, Guid> getExclusiveLocks() {
        return Collections.singletonMap(LockingGroup.DISK.name(), (Guid) getParameters().getEntityId());
    }

    @Override
    protected Map<String, Guid> getSharedLocks() {
        return sharedLockMap;
    }
}
