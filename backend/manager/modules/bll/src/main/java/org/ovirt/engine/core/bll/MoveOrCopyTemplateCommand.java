package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class MoveOrCopyTemplateCommand<T extends MoveOrCopyParameters> extends StorageDomainCommandBase<T> {

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected Map<Guid, DiskImage> imageFromSourceDomainMap;
    private  List<PermissionSubject> permissionCheckSubject;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected MoveOrCopyTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public MoveOrCopyTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplateId(parameters.getContainerId());
        parameters.setEntityId(getVmTemplateId());
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        imageFromSourceDomainMap = new HashMap<Guid, DiskImage>();
    }

    private storage_domains sourceDomain;
    private Guid sourceDomainId = Guid.Empty;

    protected storage_domains getSourceDomain() {
        if (sourceDomain == null && !Guid.Empty.equals(sourceDomainId)) {
            sourceDomain = getStorageDomainDAO().getForStoragePool(sourceDomainId, getStoragePool().getId());
        }
        return sourceDomain;
    }

    protected void SetSourceDomainId(Guid storageId) {
        sourceDomainId = storageId;
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return ImageOperation.Copy;

    }

    private List<DiskImage> _templateDisks;

    protected List<DiskImage> getTemplateDisks() {
        if (_templateDisks == null && getVmTemplate() != null) {
            _templateDisks = VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
        }
        return _templateDisks;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVmTemplate() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        } else if (getTemplateDisks() == null || getTemplateDisks().size() <= 0) {
            addCanDoActionMessage(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST);
            retValue = false;
        } else {
            ensureDomainMap(getTemplateDisks(), getParameters().getStorageDomainId());
            // check that images are ok
            ImagesHandler.fillImagesMapBasedOnTemplate(getVmTemplate(),
                    imageFromSourceDomainMap,
                    null, true);
            if (getVmTemplate().getDiskMap().values().size() != imageFromSourceDomainMap.size()) {
                log.errorFormat("Can not found any default active domain for one of the disks of template with id : {0}",
                        getVmTemplate().getId());
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MISSED_STORAGES_FOR_SOME_DISKS);
                retValue = false;
            }
            retValue = retValue
                    && VmTemplateCommand.isVmTemplateImagesReady(getVmTemplate(), null,
                            getReturnValue().getCanDoActionMessages(), true, true, true, false, getTemplateDisks());
            if (retValue) {
                setStoragePoolId(getVmTemplate().getstorage_pool_id());
                retValue =
                        CheckStorageDomain()
                                && checkStorageDomainStatus(StorageDomainStatus.Active)
                                && checkIfDisksExist(getTemplateDisks())
                                && checkFreeSpaceOnDestinationDomain(getStorageDomain(),
                                        (int) getVmTemplate().getActualDiskSize());
            }
            if (retValue
                    && DbFacade.getInstance()
                            .getStoragePoolIsoMapDAO()
                            .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                                    getVmTemplate().getstorage_pool_id().getValue())) == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
            }
        }
        return retValue;
    }

    @Override
    protected void setActionMessageParameters() {
        if (getMoveOrCopyImageOperation() == ImageOperation.Move) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
        } else {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__COPY);
        }
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
    }

    private boolean checkFreeSpaceOnDestinationDomain(storage_domains domain, int requestedSizeGB) {
        if (!StorageDomainSpaceChecker.hasSpaceForRequest(domain, requestedSizeGB)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        if (VmTemplateHandler.isTemplateStatusIsNotLocked(getVmTemplateId())) {
            VmTemplateHandler.lockVmTemplateInTransaction(getVmTemplateId(), getCompensationContext());
            MoveOrCopyAllImageGroups();
            setSucceeded(true);
        }
    }

    protected void MoveOrCopyAllImageGroups() {
        MoveOrCopyAllImageGroups(getVmTemplateId(), getTemplateDisks());
    }

    protected void MoveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    MoveOrCopyImageGroupParameters p = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getId(), disk.getImageId(), getParameters().getStorageDomainId(),
                            getMoveOrCopyImageOperation());
                    p.setParentCommand(getActionType());
                    p.setEntityId(getParameters().getEntityId());
                    p.setAddImageDomainMapping(getMoveOrCopyImageOperation() == ImageOperation.Copy);
                    p.setSourceDomainId(imageFromSourceDomainMap.get(disk.getImageId()).getstorage_ids().get(0));
                    p.setParentParemeters(getParameters());
                    VdcReturnValueBase vdcRetValue = getBackend().runInternalAction(
                                    VdcActionType.MoveOrCopyImageGroup,
                                    p,
                                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
                    getParameters().getImagesParameters().add(p);

                    getReturnValue().getTaskIdList().addAll(vdcRetValue.getInternalTaskIdList());
                }
                return null;
            }
        });
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? (getMoveOrCopyImageOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_TEMPLATE
                    : AuditLogType.USER_COPIED_TEMPLATE
                    : (getMoveOrCopyImageOperation() == ImageOperation.Move) ? AuditLogType.USER_FAILED_MOVE_TEMPLATE
                            : AuditLogType.USER_FAILED_COPY_TEMPLATE;

        case END_SUCCESS:
            return getSucceeded() ? (getMoveOrCopyImageOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_TEMPLATE_FINISHED_SUCCESS
                    : AuditLogType.USER_COPIED_TEMPLATE_FINISHED_SUCCESS
                    : (getMoveOrCopyImageOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_TEMPLATE_FINISHED_FAILURE
                            : AuditLogType.USER_COPIED_TEMPLATE_FINISHED_FAILURE;

        default:
            return (getMoveOrCopyImageOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_TEMPLATE_FINISHED_FAILURE
                    : AuditLogType.USER_COPIED_TEMPLATE_FINISHED_FAILURE;
        }
    }

    protected boolean checkIfDisksExist(Iterable<DiskImage> disksList) {
        for (DiskImage disk : disksList) {
            ArrayList<Guid> domains =
                    (ArrayList<Guid>) getBackend()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.GetImageDomainsList,
                                    new GetImageDomainsListVDSCommandParameters(getStoragePool().getId()
                                            .getValue(), disk
                                            .getimage_group_id().getValue()))
                            .getReturnValue();
            if (domains.contains(imageToDestinationDomainMap.get(disk.getImageId()))) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_CONTAINS_DISK);
                return false;
            }
        }
        return true;
    }

    protected void EndMoveOrCopyCommand() {
        EndActionOnAllImageGroups();

        if (getVmTemplate() != null) {
            VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
            VmDeviceUtils.setVmDevices(getVmTemplate());
            UpdateTemplateInSpm();
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("MoveOrCopyTemplateCommand::EndMoveOrCopyCommand: VmTemplate is null, not performing full EndAction");
        }

        setSucceeded(true);
    }

    protected void UpdateTemplateInSpm() {
        VmTemplate vmt = getVmTemplate();
        VmTemplateCommand.UpdateTemplateInSpm(vmt.getstorage_pool_id().getValue(),
                Arrays.asList(vmt));
    }

    @Override
    protected void EndSuccessfully() {
        EndMoveOrCopyCommand();
    }

    @Override
    protected void EndWithFailure() {
        EndMoveOrCopyCommand();
    }

    protected void EndActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            getBackend().EndAction(getImagesActionType(), p);
        }
    }

    protected VdcActionType getImagesActionType() {
        return VdcActionType.MoveOrCopyImageGroup;
    }

    protected storage_domains getStorageDomain(Guid domainId) {
        return getStorageDomainDAO().getForStoragePool(domainId, getStoragePool().getId());
    }

    protected Map<storage_domains, Integer> getSpaceRequirementsForStorageDomains(Collection<DiskImage> images) {
        Map<DiskImage, storage_domains> spaceMap = new HashMap<DiskImage,storage_domains>();
        for(DiskImage image : images) {
            storage_domains domain = getStorageDomain(imageToDestinationDomainMap.get(image.getImageId()));
            spaceMap.put(image, domain);
        }
        return StorageDomainValidator.getSpaceRequirementsForStorageDomains(spaceMap);
    }

    protected void ensureDomainMap(Collection<DiskImage> images, Guid defaultDomainId) {
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<Guid, Guid>();
        }
        if (imageToDestinationDomainMap.isEmpty() && images != null && defaultDomainId != null
                && !Guid.Empty.equals(defaultDomainId)) {
            for (DiskImage image : images) {
                imageToDestinationDomainMap.put(image.getImageId(), defaultDomainId);
            }
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permissionCheckSubject == null) {
            if (imageToDestinationDomainMap == null || imageToDestinationDomainMap.isEmpty()) {
                permissionCheckSubject = super.getPermissionCheckSubjects();
            } else {
                permissionCheckSubject = new ArrayList<PermissionSubject>();
                Set<PermissionSubject> permissionSet = new HashSet<PermissionSubject>();
                for (Guid storageId : imageToDestinationDomainMap.values()) {
                    permissionSet.add(new PermissionSubject(storageId,
                            VdcObjectType.Storage,
                            getActionType().getActionGroup()));
                }
                permissionCheckSubject.addAll(permissionSet);
            }

        }
        return permissionCheckSubject;
    }
 }
