package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@DisableInPrepareMode
@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class MoveOrCopyDiskCommand<T extends MoveOrCopyImageGroupParameters> extends MoveOrCopyImageGroupCommand<T>
        implements QuotaStorageDependent {

    private static final long serialVersionUID = -7219975636530710384L;
    private Map<String, String> sharedLockMap;
    private List<PermissionSubject> permsList = null;
    private List<VM> listVms;

    public MoveOrCopyDiskCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__COPY);
        } else {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        }
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        ArrayList<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();
        return isImageExist()
                && checkOperationIsCorrect()
                && canFindVmOrTemplate()
                && acquireLockInternal()
                && isImageNotLocked()
                && isSourceAndDestTheSame()
                && validateSourceStorageDomain()
                && validateDestStorage()
                && checkTemplateInDestStorageDomain()
                && validateSpaceRequirements()
                && checkImageConfiguration(canDoActionMessages)
                && checkCanBeMoveInVm()
                && checkIfNeedToBeOverride();
    }

    protected boolean isSourceAndDestTheSame() {
        if (getParameters().getOperation() == ImageOperation.Move
                && getParameters().getSourceDomainId().equals(getParameters().getStorageDomainId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
            return false;
        }
        return true;
    }

    protected boolean isImageExist() {
        if (getImage() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean isImageNotLocked() {
        DiskImage diskImage = getImage();
        if (diskImage.getimageStatus() == ImageStatus.LOCKED) {
            if (getParameters().getOperation() == ImageOperation.Move) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED);
                addCanDoActionMessage(String.format("$%1$s %2$s", "diskAliases", diskImage.getDiskAlias()));
            } else {
                addCanDoActionMessage(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
            return false;
        }
        return true;
    }

    /**
     * The following method will perform a check for correctness of operation
     * It is allow to copy only if it is image that belongs to template and
     * it is allow to move only if it is image of disk
     * @return
     */
    protected boolean checkOperationIsCorrect() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Copy
                && getImage().getVmEntityType() != VmEntityType.TEMPLATE) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK);
            retValue = false;
        }

        if (retValue && getParameters().getOperation() == ImageOperation.Move
                && getImage().getVmEntityType() == VmEntityType.TEMPLATE) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
            retValue = false;
        }
        return retValue;
    }

    protected boolean validateDestStorage() {
        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        return validate(validator.isDomainExistAndActive())
                && validate(validator.domainIsValidDestination());
    }

    /**
     * Check if destination storage has enough space
     * @return
     */
    protected boolean validateSpaceRequirements() {
        if (!isStorageDomainSpaceWithinThresholds()) {
            return false;
        }

        getImage().getSnapshots().addAll(getAllImageSnapshots());
        if (!isDomainHasStorageSpaceForRequest()) {
            return false;
        }
        return true;
    }

    private boolean isDomainHasStorageSpaceForRequest() {
        return validate(new StorageDomainValidator(getStorageDomain()).isDomainHasSpaceForRequest(Math.round(getImage().getActualDiskWithSnapshotsSize())));
    }

    protected boolean isStorageDomainSpaceWithinThresholds() {
        return validate(new StorageDomainValidator(getStorageDomain()).isDomainWithinThresholds());
    }

    protected List<DiskImage> getAllImageSnapshots() {
        return ImagesHandler.getAllImageSnapshots(getImage().getImageId(), getImage().getit_guid());
    }

    protected boolean doesStorageDomainHaveSpaceForRequest(long size) {
        return StorageDomainSpaceChecker.hasSpaceForRequest(getStorageDomain(), size);
    }

    protected boolean checkIfNeedToBeOverride() {
        if (getParameters().getOperation() == ImageOperation.Copy && !getParameters().getForceOverride()
                && getImage().getstorage_ids().contains(getStorageDomain().getId())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
            return false;
        }
        return true;
    }

    /**
     * Validate a source storage domain of image, when a source storage domain is not provided
     * any of the domains image will be used
     */
    protected boolean validateSourceStorageDomain() {
        NGuid sourceDomainId = getParameters().getSourceDomainId();
        if (sourceDomainId == null || Guid.Empty.equals(sourceDomainId)) {
            sourceDomainId = getImage().getstorage_ids().get(0);
            getParameters().setSourceDomainId(sourceDomainId);
        }
        StorageDomainValidator validator =
                new StorageDomainValidator(getStorageDomainDAO().getForStoragePool(sourceDomainId.getValue(),
                        getImage().getstorage_pool_id()));
        return validate(validator.isDomainExistAndActive());
    }

    protected boolean checkImageConfiguration(List<String> canDoActionMessages) {
        return ImagesHandler.CheckImageConfiguration(getStorageDomain().getStorageStaticData(),
                getImage(),
                canDoActionMessages);
    }

    /**
     * If a disk is attached to VM it can be moved when it is unplugged or at case that disk is plugged
     * vm should be down
     * @return
     */
    protected boolean checkCanBeMoveInVm() {
        List<VM> vmsForDisk = getVmsForDiskId();
        int vmCount = 0;
        boolean canMoveDisk = true;
        while (vmsForDisk.size() > vmCount && canMoveDisk) {
            VM currVm = vmsForDisk.get(vmCount++);
            if (VMStatus.Down != currVm.getStatus()) {
                VmDevice vmDevice =
                        getVmDeviceDAO().get(new VmDeviceId(getImage().getId(), currVm.getId()));
                if (vmDevice.getIsPlugged()) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                    canMoveDisk = false;
                }
            }
        }
        return canMoveDisk;
    }

    /**
     * Cache method to retrieve all the VMs related to image
     * @return List of Vms.
     */
    private List<VM> getVmsForDiskId() {
        if (listVms == null) {
            listVms = getVmDAO().getVmsListForDisk(getImage().getId());
        }
        return listVms;
    }

    /**
     * The following method will check, if we can move disk to destination storage domain, when
     * it is based on template
     * @return
     */
    protected boolean checkTemplateInDestStorageDomain() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Move
                && !Guid.Empty.equals(getImage().getit_guid())) {
            DiskImage templateImage = getDiskImageDao().get(getImage().getit_guid());
            if (!templateImage.getstorage_ids().contains(getParameters().getStorageDomainId())) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return getDbFacade().getVmDeviceDao();
    }

    @Override
    protected void executeCommand() {
        overrideParameters();
        VdcReturnValueBase vdcRetValue = getBackend().runInternalAction(
                VdcActionType.MoveOrCopyImageGroup,
                getParameters(),
                ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
        if (!vdcRetValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcRetValue.getFault());
        } else {
            setSucceeded(true);
            getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_FAILED_MOVED_VM_DISK
                            : AuditLogType.USER_FAILED_COPY_TEMPLATE_DISK;

        case END_SUCCESS:
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_SUCCESS
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_SUCCESS
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                            : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;

        default:
            return (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null) {
            permsList = new ArrayList<PermissionSubject>();

            DiskImage image = getImage();
            Guid diskId = image == null ? Guid.Empty : image.getId();
            permsList.add(new PermissionSubject(diskId, VdcObjectType.Disk, ActionGroup.CONFIGURE_DISK_STORAGE));

            addStoragePermissionByQuotaMode(permsList,
                    getStoragePoolId().getValue(),
                    getParameters().getStorageDomainId());
        }
        return permsList;
    }

    /**
     * The following method will override a parameters which are not relevant for the MoveOrCopyDiskCommand to the
     * correct values for these scenario in order to be used at parent class
     */
    private void overrideParameters() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            getParameters().setUseCopyCollapse(true);
            getParameters().setAddImageDomainMapping(true);
        } else {
            getParameters().setUseCopyCollapse(false);
        }
        getParameters().setDestinationImageId(getImageId());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setDestImageGroupId(getImageGroupId());
        getParameters().setVolumeFormat(getDiskImage().getvolume_format());
        getParameters().setVolumeType(getDiskImage().getvolume_type());
        getParameters().setCopyVolumeType(CopyVolumeType.SharedVol);
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParameters(getParameters());
    }

    /**
     * The following method will determine if a provided vm/template exists
     * @param retValue
     * @return
     */
    private boolean canFindVmOrTemplate() {
        boolean retValue = true;
        if (getParameters().getOperation() == ImageOperation.Copy) {
            Collection<VmTemplate> templates = getVmTemplateDAO().getAllForImage(getImage().getImageId()).values();
            if (templates.isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
                retValue = false;
            } else {
                VmTemplate vmTemplate = templates.iterator().next();
                setVmTemplate(vmTemplate);
                sharedLockMap = Collections.singletonMap(vmTemplate.getId().toString(), LockingGroup.TEMPLATE.name());
            }
        } else {
            List<VM> vmsForDisk = getVmsForDiskId();
            if (!vmsForDisk.isEmpty()) {
                Map<String, String> lockMap = new HashMap<String, String>();
                for (VM currVm : vmsForDisk) {
                    lockMap.put(currVm.getId().toString(), LockingGroup.VM.name());
                }
                lockForMove(lockMap);
            }
        }
        return retValue;
    }

    protected void lockForMove(Map<String, String> lockMap) {
        sharedLockMap = lockMap;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getImage().getId().toString(), LockingGroup.DISK.name());
    }

    @Override
    protected Map<String, String> getSharedLocks() {
        return sharedLockMap;
    }

    public String getDiskAlias() {
        return getImage().getDiskAlias();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();
        // If source and destination are in the same quota - return empty list
        if (getImage().getQuotaId() != null && getImage().getQuotaId().equals(getDestinationQuotaId())) {
            return list;
        }

        list.add(new QuotaStorageConsumptionParameter(
                getDestinationQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getParameters().getStorageDomainId(),
                (double)getImage().getSizeInGigabytes()));

        if (ImageOperation.Move == getParameters().getOperation()) {
            if (getImage().getQuotaId() != null && !Guid.Empty.equals(getImage().getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        getImage().getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getParameters().getSourceDomainId().getValue(),
                        (double)getImage().getSizeInGigabytes()));
            }
        }
        return list;
    }

    private Guid getDestinationQuotaId() {
        return getParameters().getQuotaId();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        List<storage_domains> storageDomains = getStorageDomainDAO().getAllForStorageDomain(getParameters().getSourceDomainId().getValue());
        String sourceSDName = StringUtils.EMPTY;

        if (storageDomains.size() > 0) {
            sourceSDName = storageDomains.get(0).getstorage_name();
        }
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("sourcesd", sourceSDName);
            jobProperties.put("targetsd", getStorageDomainName());
            jobProperties.put("diskalias", getDiskAlias());
            if (ImageOperation.Move == getParameters().getOperation()) {
                jobProperties.put("action", "Moving");
            } else {
                jobProperties.put("action", "Copying");
            }
        }
        return jobProperties;
    }

}
