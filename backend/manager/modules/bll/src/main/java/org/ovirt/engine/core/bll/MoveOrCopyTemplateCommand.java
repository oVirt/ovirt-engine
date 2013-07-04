package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.StorageDomainCommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.GetImageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@Deprecated
public class MoveOrCopyTemplateCommand<T extends MoveOrCopyParameters> extends StorageDomainCommandBase<T> {

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected Map<Guid, DiskImage> imageFromSourceDomainMap;
    private List<PermissionSubject> permissionCheckSubject;
    private List<DiskImage> _templateDisks;
    private StorageDomain sourceDomain;
    private Guid sourceDomainId = Guid.Empty;
    private final static Pattern VALIDATE_MAC_ADDRESS = Pattern.compile(VmNetworkInterface.VALID_MAC_ADDRESS_FORMAT);

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
    }

    @Override
    protected void init(T parameters) {
        super.init(parameters);
        setVmTemplateId(parameters.getContainerId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        imageFromSourceDomainMap = new HashMap<Guid, DiskImage>();
    }

    protected StorageDomain getSourceDomain() {
        if (sourceDomain == null && !Guid.Empty.equals(sourceDomainId)) {
            sourceDomain = getStorageDomainDAO().getForStoragePool(sourceDomainId, getStoragePool().getId());
        }
        return sourceDomain;
    }

    protected void setSourceDomainId(Guid storageId) {
        sourceDomainId = storageId;
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return ImageOperation.Copy;
    }

    protected List<DiskImage> getTemplateDisks() {
        if (_templateDisks == null && getVmTemplate() != null) {
            VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
            _templateDisks = getVmTemplate().getDiskList();
        }
        return _templateDisks;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVmTemplate() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        } else if (getTemplateDisks() != null && !getTemplateDisks().isEmpty()) {
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
                setStoragePoolId(getVmTemplate().getStoragePoolId());
                retValue =
                        checkStorageDomain()
                                && checkStorageDomainStatus(StorageDomainStatus.Active)
                                && checkIfDisksExist(getTemplateDisks())
                                && checkFreeSpaceOnDestinationDomain(getStorageDomain(),
                                        (int) getVmTemplate().getActualDiskSize());
            }
            if (retValue
                    && DbFacade.getInstance()
                            .getStoragePoolIsoMapDao()
                            .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                                    getVmTemplate().getStoragePoolId())) == null) {
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

    private boolean checkFreeSpaceOnDestinationDomain(StorageDomain domain, int requestedSizeGB) {
        return validate(new StorageDomainValidator(domain).isDomainHasSpaceForRequest(requestedSizeGB));
    }

    @Override
    protected void executeCommand() {
    }

    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVmTemplateId(), getTemplateDisks());
    }

    protected void moveOrCopyAllImageGroups(final Guid containerID, final Iterable<DiskImage> disks) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (DiskImage disk : disks) {
                    VdcReturnValueBase vdcRetValue = getBackend().runInternalAction(
                            getImagesActionType(),
                            buildModeOrCopyImageGroupParameters(containerID, disk),
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));

                    getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
                }
                return null;
            }

            private MoveOrCopyImageGroupParameters buildModeOrCopyImageGroupParameters(
                    final Guid containerID, DiskImage disk) {
                MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(
                        containerID, disk.getId(), disk.getImageId(),
                        getParameters().getStorageDomainId(), getMoveOrCopyImageOperation());
                params.setParentCommand(getActionType());
                params.setEntityInfo(getParameters().getEntityInfo());
                params.setAddImageDomainMapping(getMoveOrCopyImageOperation() == ImageOperation.Copy);
                params.setSourceDomainId(imageFromSourceDomainMap.get(disk.getId()).getStorageIds().get(0));
                params.setParentParameters(getParameters());
                return params;
            }
        });
    }

    protected VM getVmFromSnapshot(Snapshot snapshot) {
        String vmConfiguration = snapshot.getVmConfiguration();
        // active snapshot
        if (vmConfiguration == null) {
            return getVm();
        }
        else {
            VM vm = new VM();
            new SnapshotsManager().updateVmFromConfiguration(vm, vmConfiguration);
            return vm;
        }
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
            VDSReturnValue runVdsCommand = getBackend()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.GetImageDomainsList,
                            new GetImageDomainsListVDSCommandParameters(getStoragePool().getId(), disk.getId()));
            if (runVdsCommand.getSucceeded()) {
                ArrayList<Guid> domains = (ArrayList<Guid>) runVdsCommand.getReturnValue();
                if (domains.contains(imageToDestinationDomainMap.get(disk.getId()))) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_CONTAINS_DISK);
                    return false;
                }
            } else if (runVdsCommand.getVdsError().getCode() == VdcBllErrors.GetStorageDomainListError) {
                addCanDoActionMessage(VdcBllMessages.ERROR_GET_STORAGE_DOMAIN_LIST);
                return false;
            }
        }
        return true;
    }

    protected void endMoveOrCopyCommand() {
        endActionOnAllImageGroups();
        endVmTemplateRelatedOps();
        setSucceeded(true);
    }

    protected final void endVmTemplateRelatedOps() {
        if (getVmTemplate() != null) {
            VmDeviceUtils.setVmDevices(getVmTemplate());
            incrementDbGeneration();
            VmTemplateHandler.UnLockVmTemplate(getVmTemplateId());
        }
        else {
            setCommandShouldBeLogged(false);
            log.warn("MoveOrCopyTemplateCommand::EndMoveOrCopyCommand: VmTemplate is null, not performing full EndAction");
        }
    }

    protected void incrementDbGeneration() {
        getVmStaticDAO().incrementDbGeneration(getVmTemplate().getId());
    }

    @Override
    protected void endSuccessfully() {
        endMoveOrCopyCommand();
    }

    @Override
    protected void endWithFailure() {
        endMoveOrCopyCommand();
    }

    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            getBackend().EndAction(getImagesActionType(), p);
        }
    }

    protected VdcActionType getImagesActionType() {
        return VdcActionType.CopyImageGroup;
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return getStorageDomainDAO().getForStoragePool(domainId, getStoragePool().getId());
    }

    protected Map<StorageDomain, Integer> getSpaceRequirementsForStorageDomains(Collection<DiskImage> images) {
        Map<DiskImage, StorageDomain> spaceMap = new HashMap<DiskImage, StorageDomain>();
        for (DiskImage image : images) {
            StorageDomain domain = getStorageDomain(imageToDestinationDomainMap.get(image.getId()));
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
                imageToDestinationDomainMap.put(image.getId(), defaultDomainId);
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

    protected void fillMacAddressIfMissing(VmNetworkInterface iface) {
        if (StringUtils.isEmpty(iface.getMacAddress())
                && (MacPoolManager.getInstance().getAvailableMacsCount() >= 1)) {
            iface.setMacAddress(MacPoolManager.getInstance().allocateNewMac());
        }
    }

    protected boolean validateMacAddress(List<VmNetworkInterface> ifaces) {
        int freeMacs = 0;
        for (VmNetworkInterface iface : ifaces) {
            if (!StringUtils.isEmpty(iface.getMacAddress())) {
                if(!VALIDATE_MAC_ADDRESS.matcher(iface.getMacAddress()).matches()) {
                    addCanDoActionMessage("$IfaceName " + iface.getName());
                    addCanDoActionMessage("$MacAddress " + iface.getMacAddress());
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_INTERFACE_MAC_INVALID);
                    return false;
                }
            }
            else {
                freeMacs++;
            }
        }
        if (freeMacs > 0 && !(MacPoolManager.getInstance().getAvailableMacsCount() >= freeMacs)) {
            addCanDoActionMessage(VdcBllMessages.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
            return false;
        }
        return true;
    }

    /**
     * Log to audit-log the list of VmInterfaces that are attached to invalid networks. A network is invalid if it is a
     * Non-VM Network, or if the Network is not in the Cluster
     *
     * @param invalidIfaceNames
     * @param invalidNetworkNames
     */
    protected void auditInvalidInterfaces(List<String> invalidNetworkNames, List<String> invalidIfaceNames) {
        if (invalidNetworkNames.size() > 0) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.addCustomValue("Networks", StringUtils.join(invalidNetworkNames, ','));
            logable.addCustomValue("Interfaces", StringUtils.join(invalidIfaceNames, ','));
            AuditLogDirector.log(logable, getAuditLogTypeForInvalidInterfaces());
        }
    }

    /**
     * Functionality of this method should be implemented in subclasses
     */
    protected AuditLogType getAuditLogTypeForInvalidInterfaces() {
        return AuditLogType.UNASSIGNED;
    }
}
