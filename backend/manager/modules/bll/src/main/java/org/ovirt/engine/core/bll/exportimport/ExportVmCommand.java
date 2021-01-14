package org.ovirt.engine.core.bll.exportimport;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryDisks;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfUpdateProcessHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExportVmCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfUpdateProcessHelper ovfUpdateProcessHelper;

    private List<DiskImage> disksImages;
    private Collection<Snapshot> snapshotsWithMemory;

    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    @Inject
    private OvfManager ovfManager;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private SnapshotsValidator snapshotsValidator;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private DiskDao diskDao;

    @Inject
    private ClusterUtils clusterUtils;

    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public ExportVmCommand(Guid commandId) {
        super(commandId);
    }

    public ExportVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getContainerId());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        setDescription(getVmName());
        setStoragePoolId(getVm().getStoragePoolId());

        // check that target domain exists
        StorageDomainValidator targetstorageDomainValidator = new StorageDomainValidator(getStorageDomain());
        if (!validate(targetstorageDomainValidator.isDomainExistAndActive())) {
            return false;
        }

        // load the disks of vm from database
        vmHandler.updateDisksFromDb(getVm());
        List<DiskImage> disksForExport = getDisksBasedOnImage();
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(disksForExport);
        if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                !validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        // update vm snapshots for storage free space check
        imagesHandler.fillImagesBySnapshots(getVm());

        // check that the target and source domain are in the same storage_pool
        if (storagePoolIsoMapDao.get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getVm().getStoragePoolId())) == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
        }

        // check if template exists only if asked for
        if (getParameters().getTemplateMustExists()) {
            if (!checkTemplateInStorageDomain(getVm().getStoragePoolId(), getVm().getVmtGuid())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_EXPORT_DOMAIN,
                        String.format("$TemplateName %1$s", getVm().getVmtName()));
            }
        }

        Map<Guid, ? extends Disk> images = getVm().getDiskMap();
        if (getParameters().getCopyCollapse()) {
            for (DiskImage img : disksForExport) {
                if (images.containsKey(img.getId())) {
                    // check that no RAW format exists (we are in collapse mode)
                    if (((DiskImage) images.get(img.getId())).getVolumeFormat() == VolumeFormat.RAW
                            && img.getVolumeFormat() != VolumeFormat.RAW) {
                        return failValidation(EngineMessage.VM_CANNOT_EXPORT_RAW_FORMAT);
                    }
                }
            }
        }

        // check destination storage is Export domain
        if (getStorageDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SPECIFY_DOMAIN_IS_NOT_EXPORT_DOMAIN,
                    String.format("$storageDomainName %1$s", getStorageDomainName()));
        }

        // get the snapshots that are going to be exported and have memory
        snapshotsWithMemory = getSnapshotsToBeExportedWithMemory();

        // check destination storage have free space
        if (!handleDestStorageDomain(disksForExport)) {
            return false;
        }
        MultipleStorageDomainsValidator storageDomainsValidator = new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksForExport));
        if (!(checkVmInStorageDomain()
                && validate(new StoragePoolValidator(getStoragePool()).existsAndUp())
                && validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotsValidator.vmNotInPreview(getVmId()))
                && validate(new VmValidator(getVm()).vmDown())
                && validate(storageDomainsValidator.allDomainsExistAndActive())
                && validate(storageDomainsValidator.isSupportedByManagedBlockStorageDomains(getActionType())))) {
            return false;
        }

        return true;
    }

    private boolean handleDestStorageDomain(List<DiskImage> disksList) {
        ensureDomainMap(disksList, getStorageDomainId());
        List<DiskImage> dummiesDisksList = createDiskDummiesForSpaceValidations(disksList);
        dummiesDisksList.addAll(getMemoryVolumes());
        return validateSpaceRequirements(dummiesDisksList);
    }

    /**
     * Space Validations are done using data extracted from the disks. The disks in question in this command
     * don't have all the needed data, and in order not to contaminate the command's data structures, an alter
     * one is created specifically fo this validation - hence dummy.
     */
    protected List<DiskImage> createDiskDummiesForSpaceValidations(List<DiskImage> disksList) {
        return disksList.stream().map(image -> imagesHandler.createDiskImageWithExcessData(image,
                imageToDestinationDomainMap.get(image.getId()))).collect(Collectors.toList());
    }

    private List<DiskImage> getMemoryVolumes() {
        int numOfSnapshots = snapshotsWithMemory.size();
        long memorySize = numOfSnapshots * vmOverheadCalculator.getSnapshotMemorySizeInBytes(getVm());
        long metadataSize = numOfSnapshots * MemoryUtils.METADATA_SIZE_IN_BYTES;
        MemoryDisks memoryDisks = MemoryUtils.createDiskDummies(memorySize, metadataSize);

        //Set target domain in memory disks
        memoryDisks.asList().forEach(d -> d.setStorageIds(Collections.singletonList(getStorageDomainId())));
        return memoryDisks.asList();
    }

    private Collection<Snapshot> getSnapshotsToBeExportedWithMemory() {
        if (getParameters().getCopyCollapse()) {
            Snapshot activeSnapshot = snapshotDao.get(getVmId(), SnapshotType.ACTIVE);
            return activeSnapshot.containsMemory() ? Collections.singleton(activeSnapshot) : Collections.emptyList();
        } else {
            return snapshotDao.getAll(getVmId()).stream().filter(Snapshot::containsMemory).collect(Collectors.toList());
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void executeCommand() {
        vmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
        freeLock();

        // update vm init
        vmHandler.updateVmInitFromDB(getVm().getStaticData(), true);

        // Means that there are no asynchronous tasks to execute - so we can end the command
        // immediately after the execution of the previous steps
        if (!hasSnappableDisks() && snapshotsWithMemory.isEmpty()) {
            endSuccessfully();
        } else {
            TransactionSupport.executeInNewTransaction(() -> {
                moveOrCopyAllImageGroups();
                return null;
            });
            setSucceeded(true);
        }
    }

    private boolean hasSnappableDisks() {
        return !getDisksBasedOnImage().isEmpty();
    }

    private void updateCopyVmInSpm(Guid storagePoolId, VM vm, Guid storageDomainId) {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndMetaDictionary = new HashMap<>();
        List<DiskImage> vmImages = new ArrayList<>();
        List<LunDisk> lunDisks = new ArrayList<>();
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces != null) {
            // TODO remove this when the API changes
            interfaces.clear();
            interfaces.addAll(vmNetworkInterfaceDao.getAllForVm(vm.getId()));
        }

        List<Guid> imageGroupIds = new ArrayList<>();
        for (Disk disk : getDisksBasedOnImage()) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.setParentId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                diskImage.setImageTemplateId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomainId)));
                DiskImage diskForVolumeInfo = getDiskForVolumeInfo(diskImage);
                diskImage.setVolumeFormat(diskForVolumeInfo.getVolumeFormat());
                diskImage.setVolumeType(diskForVolumeInfo.getVolumeType());
                VDSReturnValue vdsReturnValue = runVdsCommand(
                                    VDSCommandType.GetImageInfo,
                                    new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, diskImage
                                            .getId(), diskImage.getImageId()));
                if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
                    DiskImage fromVdsm = (DiskImage) vdsReturnValue.getReturnValue();
                    diskImage.setActualSizeInBytes(fromVdsm.getActualSizeInBytes());
                }
                vmImages.add(diskImage);
                imageGroupIds.add(disk.getId());
        }

        if (StringUtils.isEmpty(vm.getVmtName())) {
            VmTemplate t = vmTemplateDao.get(vm.getVmtGuid());
            vm.setVmtName(t.getName());
        }

        lunDisks.addAll(DisksFilter.filterLunDisks(getVm().getDiskMap().values(), ONLY_NOT_SHAREABLE));
        lunDisks.forEach(lun -> lun.getLun()
                .setLunConnections(storageServerConnectionDao.getAllForLun(lun.getLun().getId())));
        getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(vm);
        fullEntityOvfData.setClusterName(vm.getClusterName());
        fullEntityOvfData.setDiskImages(vmImages);
        fullEntityOvfData.setLunDisks(lunDisks);
        Version compatibilityVersion = clusterUtils.getCompatibilityVersion(vm);
        String vmMeta = ovfManager.exportVm(vm, fullEntityOvfData, compatibilityVersion);

        vmsAndMetaDictionary.put(vm.getId(), new KeyValuePairCompat<>(vmMeta, imageGroupIds));
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, vmsAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        runVdsCommand(VDSCommandType.UpdateVM, tempVar);
    }

    @Override
    protected void moveOrCopyAllImageGroups() {
        // Disks
        moveOrCopyAllImageGroups(getVm().getId(), getDisksBasedOnImage());
        // Memory volumes
        copyAllMemoryImages(getVm().getId());
    }

    private List<DiskImage> getDisksBasedOnImage() {
        if (disksImages == null) {
            disksImages = DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
        }
        return disksImages;
    }

    private void copyAllMemoryImages(Guid containerID) {
        for (Snapshot snapshot : snapshotsWithMemory) {
            // copy the memory dump image
            DiskImage dumpImage = (DiskImage) diskDao.get(snapshot.getMemoryDiskId());

            ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryDumpImage(containerID, dumpImage));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());

            // copy the memory configuration (of the VM) image
            // This volume is always of type 'sparse' and format 'cow' so no need to convert,
            // and there're no snapshots for it so no reason to use copy collapse
            DiskImage confImage = (DiskImage) diskDao.get(snapshot.getMetadataDiskId());
            vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParameters(containerID, confImage));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
            Guid containerID, DiskImage disk) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID, disk.getId(),
                disk.getImageId(), getParameters().getStorageDomainId(), ImageOperation.Copy);
        StorageDomainStatic sourceDomain = storageDomainStaticDao.get(disk.getStorageIds().get(0));

        // if the data domain is a block based storage, the memory volume type is preallocated
        // so we need to use copy collapse in order to convert it to be sparsed in the export domain
        if (sourceDomain.getStorageType().isBlockDomain()) {
            params.setUseCopyCollapse(true);
            params.setVolumeType(VolumeType.Sparse);
            params.setVolumeFormat(VolumeFormat.RAW);
        }

        return params;
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParameters(Guid containerID, DiskImage disk) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID, disk.getId(),
                disk.getImageId(), getParameters().getStorageDomainId(), ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setParentParameters(getParameters());
        params.setSourceDomainId(disk.getStorageIds().get(0));
        return params;
    }

    @Override
    protected void moveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        for (DiskImage disk : disks) {
            ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForDisk(containerID, disk));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForDisk(Guid containerID, DiskImage disk) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID, disk.getId(),
                disk.getImageId(), getParameters().getStorageDomainId(), ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setUseCopyCollapse(getParameters().getCopyCollapse());
        DiskImage diskForVolumeInfo = getDiskForVolumeInfo(disk);
        params.setVolumeFormat(diskForVolumeInfo.getVolumeFormat());
        params.setVolumeType(diskForVolumeInfo.getVolumeType());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setParentParameters(getParameters());
        return params;
    }

    /**
     * Return the correct disk to get the volume info (type & allocation) from. For copy collapse it's the ancestral
     * disk of the given disk, and otherwise it's the disk itself.
     *
     * @param disk
     *            The disk for which to get the disk with the info.
     * @return The disk with the correct volume info.
     */
    private DiskImage getDiskForVolumeInfo(DiskImage disk) {
        if (getParameters().getCopyCollapse()) {
            return diskImageDao.getAncestor(disk.getImageId());
        } else {
            return disk;
        }
    }

    /**
     * Check that vm is in export domain
     */
    protected boolean checkVmInStorageDomain() {
        GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getVm()
                .getStoragePoolId(), getParameters().getStorageDomainId());
        QueryReturnValue qretVal = runInternalQuery(QueryType.GetVmsFromExportDomain,
                tempVar);

        if (qretVal.getSucceeded()) {
            List<VM> vms = qretVal.getReturnValue();
            for (VM vm : vms) {
                if (vm.getId().equals(getVm().getId())) {
                    if (!getParameters().getForceOverride()) {
                        return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_GUID_ALREADY_EXIST);
                    }
                } else if (vm.getName().equals(getVm().getName())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                }
            }
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_EXPORT_VM : AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_VM_FAILED;
        }
    }

    protected void updateVmInSpm() {
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary = new HashMap<>();
        ovfUpdateProcessHelper.loadVmData(getVm());
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(getVm());
        fullEntityOvfData.setClusterName(getVm().getClusterName());
        fullEntityOvfData.setDiskImages(ovfUpdateProcessHelper.getVmImagesFromDb(getVm()));
        ovfUpdateProcessHelper.buildMetadataDictionaryForVm(getVm(),
                metaDictionary,
                fullEntityOvfData);
        ovfUpdateProcessHelper.executeUpdateVmInSpmCommand(getVm().getStoragePoolId(),
                metaDictionary, getParameters().getStorageDomainId());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void endSuccessfully() {
        endActionOnAllImageGroups();
        VM vm = getVm();
        populateVmData(vm);
        if (getParameters().getCopyCollapse()) {
            endCopyCollapseOperations(vm);
        } else {
            updateSnapshotOvf(vm);
        }
        vmHandler.unLockVm(vm);
        setSucceeded(true);
    }

    private void populateVmData(VM vm) {
        vmHandler.updateDisksFromDb(vm);
        vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        getVmDeviceUtils().setVmDevices(vm.getStaticData());
    }

    private void endCopyCollapseOperations(VM vm) {
        vm.setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        vm.setVmtName(null);
        Snapshot activeSnapshot = snapshotDao.get(snapshotDao.getId(vm.getId(), SnapshotType.ACTIVE));
        vm.setSnapshots(Collections.singletonList(activeSnapshot));

        try {
            updateCopyVmInSpm(getVm().getStoragePoolId(),
                    vm, getParameters()
                            .getStorageDomainId());
        } catch(EngineException e) {
            log.error("Updating VM OVF in export domain failed.", e);
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED_UPDATING_OVF);
        }
    }

    private void updateSnapshotOvf(VM vm) {
        vm.setSnapshots(snapshotDao.getAllWithConfiguration(getVm().getId()));
        updateVmInSpm();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void endWithFailure() {
        endActionOnAllImageGroups();
        VM vm = getVm();
        vmHandler.unLockVm(vm);
        setSucceeded(true);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
        }
        return jobProperties;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        permissionSubjects.addAll(super.getPermissionCheckSubjects());
        permissionSubjects.add(new PermissionSubject(getVmId(), VdcObjectType.VM, getActionType().getActionGroup()));
        return permissionSubjects;
    }

}
