package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class MoveOrCopyTemplateCommand<T extends MoveOrCopyParameters> extends StorageDomainCommandBase<T> {

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
    }

    private storage_domains _sourceDomain;
    private Guid _sourceDomainId = new Guid();

    protected storage_domains getSourceDomain() {
        if (_sourceDomain == null && !_sourceDomainId.equals(Guid.Empty)) {
            _sourceDomain = getStorageDomainDAO().getForStoragePool(_sourceDomainId, getStoragePool().getId());
        } else if (_sourceDomain == null) {
            java.util.ArrayList<storage_domains> result = (java.util.ArrayList<storage_domains>) getBackend()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new GetStorageDomainsByVmTemplateIdQueryParameters(getVmTemplateId())).getReturnValue();
            if (result != null) {
                for (storage_domains domain : result) {
                    if (domain.getstatus() != null && domain.getstatus() == StorageDomainStatus.Active) {
                        _sourceDomain = domain;
                    }
                }
            }
        }
        return _sourceDomain;
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected void SetSourceDomainId(Guid storageId) {
        _sourceDomainId = storageId;
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return ImageOperation.Copy;

    }

    private java.util.ArrayList<DiskImage> _templateDisks;

    protected java.util.ArrayList<DiskImage> getTemplateDisks() {
        if (_templateDisks == null && getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
            _templateDisks = new java.util.ArrayList<DiskImage>();
            for (DiskImageTemplate diTemplate : getVmTemplate().getDiskMap().values()) {
                _templateDisks.add(DbFacade.getInstance().getDiskImageDAO().getSnapshotById(diTemplate.getId()));
            }
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
        }
        // check that images are ok
        if (retValue && getSourceDomain() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            retValue = false;
        }
        retValue = retValue
                && VmTemplateCommand.isVmTemplateImagesReady(getVmTemplateId(), getSourceDomain().getid(),
                        getReturnValue().getCanDoActionMessages(), true, true, true, false);
        if (retValue) {
            setStoragePoolId(getVmTemplate().getstorage_pool_id());
            retValue = CheckStorageDomain()
                    && checkStorageDomainStatus(StorageDomainStatus.Active)
                    && CheckIfDisksExist(getTemplateDisks())
                    && checkFreeSpaceOnDestinationDomain(getStorageDomain(), (int) getVmTemplate().getActualDiskSize());
        }
        if (retValue
                && DbFacade.getInstance()
                        .getStoragePoolIsoMapDAO()
                        .get(new StoragePoolIsoMapId(getStorageDomain().getid(),
                                getVmTemplate().getstorage_pool_id().getValue())) == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }

        if (!retValue) {
            if (getMoveOrCopyImageOperation() == ImageOperation.Move) {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MOVE);
            } else {
                addCanDoActionMessage(VdcBllMessages.VAR__ACTION__COPY);
            }
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_TEMPLATE);
        }
        return retValue;
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
                    MoveOrCopyImageGroupParameters tempVar = new MoveOrCopyImageGroupParameters(containerID, disk
                            .getimage_group_id().getValue(), disk.getId(), getParameters().getStorageDomainId(),
                            getMoveOrCopyImageOperation());
                    tempVar.setParentCommand(getActionType());
                    tempVar.setEntityId(getParameters().getEntityId());
                    tempVar.setAddImageDomainMapping(getMoveOrCopyImageOperation() == ImageOperation.Copy);
                    MoveOrCopyImageGroupParameters p = tempVar;
                    // if copying template then AddImageDomainMapping should be true
                    if (getSourceDomain() != null) {
                        p.setSourceDomainId(getSourceDomain().getid());
                    }
                    p.setParentParemeters(getParameters());
                    VdcReturnValueBase vdcRetValue = getBackend().runInternalAction(
                            VdcActionType.MoveOrCopyImageGroup, p);
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

    protected boolean CheckIfDisksExist(Iterable<DiskImage> disksList) {
        for (DiskImage disk : disksList) {
            java.util.ArrayList<Guid> domains = (java.util.ArrayList<Guid>) getBackend()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageDomainsList,
                            new GetImageDomainsListVDSCommandParameters(disk.getstorage_pool_id().getValue(), disk
                                    .getimage_group_id().getValue())).getReturnValue();
            if (domains.contains(getParameters().getStorageDomainId())) {
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
            UpdateTemplateInSpm();
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("MoveOrCopyTemplateCommand::EndMoveOrCopyCommand: VmTemplate is null, not performing full EndAction");
        }

        setSucceeded(true);
    }

    protected void UpdateTemplateInSpm() {
        VmTemplateCommand.UpdateTemplateInSpm(getVmTemplate().getstorage_pool_id().getValue(),
                new java.util.ArrayList<VmTemplate>(java.util.Arrays.asList(new VmTemplate[] { getVmTemplate() })));
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

    private static LogCompat log = LogFactoryCompat.getLog(MoveOrCopyTemplateCommand.class);

    protected boolean domainIsValidDestination(storage_domains domain) {
        StorageDomainValidator validator = new StorageDomainValidator(domain);
        return validator.domainIsValidDestination();
    }

    protected storage_domains getStorageDomain(Guid domainId) {
        return getStorageDomainDAO().getForStoragePool(domainId.getValue(), getStoragePool().getId());
    }

    protected Map<storage_domains, Integer> getSpaceRequirementsForStorageDomains(List<DiskImage> images) {
        Map<DiskImage, storage_domains> spaceMap = new HashMap<DiskImage,storage_domains>();
        Map<Guid, Guid> imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        for(DiskImage image : images) {
            storage_domains domain = getStorageDomain(imageToDestinationDomainMap.get(image.getId()));
            spaceMap.put(image, domain);
        }
        return StorageDomainValidator.getSpaceRequirementsForStorageDomains(spaceMap);
    }

    protected Set<Guid> getTargetDomains() {
        Set<Guid> retVal = new HashSet<Guid>();
        for(Guid guid : getParameters().getImageToDestinationDomainMap().values()) {
            retVal.add(guid);
        }
        return retVal;
    }
}
