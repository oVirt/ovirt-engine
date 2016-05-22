package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfUpdateProcessHelper;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.utils.VmUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ExportVmCommand<T extends MoveOrCopyParameters> extends MoveOrCopyTemplateCommand<T> {

    private List<DiskImage> disksImages;
    private Collection<Snapshot> snapshotsWithMemory;

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
        VmHandler.updateDisksFromDb(getVm());
        List<DiskImage> disksForExport = getDisksBasedOnImage();
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(disksForExport);
        if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                !validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        // update vm snapshots for storage free space check
        ImagesHandler.fillImagesBySnapshots(getVm());

        // check that the target and source domain are in the same storage_pool
        if (getStoragePoolIsoMapDao().get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getVm().getStoragePoolId())) == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_MATCH);
            return false;
        }

        // check if template exists only if asked for
        if (getParameters().getTemplateMustExists()) {
            if (!checkTemplateInStorageDomain(getVm().getStoragePoolId(), getParameters().getStorageDomainId(),
                    getVm().getVmtGuid(), getContext().getEngineContext())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_EXPORT_DOMAIN,
                        String.format("$TemplateName %1$s", getVm().getVmtName()));
            }
        }

        // check that the images requested format are valid (COW+Sparse)
        if (!ImagesHandler.checkImagesConfiguration(getParameters().getStorageDomainId(),
                disksForExport,
                getReturnValue().getValidationMessages())) {
            return false;
        }

        Map<Guid, ? extends Disk> images = getVm().getDiskMap();
        if (getParameters().getCopyCollapse()) {
            for (DiskImage img : disksForExport) {
                if (images.containsKey(img.getId())) {
                    // check that no RAW format exists (we are in collapse mode)
                    if (((DiskImage) images.get(img.getId())).getVolumeFormat() == VolumeFormat.RAW
                            && img.getVolumeFormat() != VolumeFormat.RAW) {
                        addValidationMessage(EngineMessage.VM_CANNOT_EXPORT_RAW_FORMAT);
                        return false;
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

        SnapshotsValidator snapshotValidator = new SnapshotsValidator();
        if (!(checkVmInStorageDomain()
                && validate(new StoragePoolValidator(getStoragePool()).isUp())
                && validate(snapshotValidator.vmNotDuringSnapshot(getVmId()))
                && validate(snapshotValidator.vmNotInPreview(getVmId()))
                && validate(new VmValidator(getVm()).vmDown())
                && validate(new MultipleStorageDomainsValidator(getVm().getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(disksForExport)).allDomainsExistAndActive()))) {
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
        List<DiskImage> dummies = new ArrayList<>(disksList.size());
        for (DiskImage image : disksList) {
            Guid targetSdId = imageToDestinationDomainMap.get(image.getId());
            DiskImage dummy = ImagesHandler.createDiskImageWithExcessData(image, targetSdId);
            dummies.add(dummy);
        }
        return dummies;
    }

    private List<DiskImage> getMemoryVolumes() {
        int numOfSnapshots = snapshotsWithMemory.size();
        long memorySize = numOfSnapshots * VmUtils.getSnapshotMemorySizeInBytes(getVm());
        long metadataSize = numOfSnapshots * MemoryUtils.METADATA_SIZE_IN_BYTES;
        List<DiskImage> memoryDisksList = MemoryUtils.createDiskDummies(memorySize, metadataSize);

        //Set target domain in memory disks
        ArrayList<Guid> sdId = new ArrayList<>(Collections.singletonList(getStorageDomainId()));
        for (DiskImage diskImage : memoryDisksList) {
            diskImage.setStorageIds(sdId);
        }
        return memoryDisksList;
    }

    private Collection<Snapshot> getSnapshotsToBeExportedWithMemory() {
        if (getParameters().getCopyCollapse()) {
            Snapshot activeSnapshot = getSnapshotDao().get(getVmId(), SnapshotType.ACTIVE);
            return !activeSnapshot.getMemoryVolume().isEmpty() ?
                    Collections.<Snapshot>singleton(activeSnapshot) : Collections.<Snapshot>emptyList();
        }
        else {
            Map<String, Snapshot> memory2snapshot = new HashMap<>();
            for (Snapshot snapshot : getSnapshotDao().getAll(getVmId())) {
                memory2snapshot.put(snapshot.getMemoryVolume(), snapshot);
            }
            memory2snapshot.remove(StringUtils.EMPTY);
            return memory2snapshot.values();
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void executeCommand() {
        VmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
        freeLock();

        // update vm init
        VmHandler.updateVmInitFromDB(getVm().getStaticData(), true);

        // Means that there are no asynchronous tasks to execute - so we can end the command
        // immediately after the execution of the previous steps
        if (!hasSnappableDisks() && snapshotsWithMemory.isEmpty()) {
            endSuccessfully();
        } else {
            TransactionSupport.executeInNewTransaction(() -> {
                moveOrCopyAllImageGroups();
                return null;
            });

            if (!getReturnValue().getVdsmTaskIdList().isEmpty()) {
                setSucceeded(true);
            }
        }
    }

    private boolean hasSnappableDisks() {
        return !getDisksBasedOnImage().isEmpty();
    }

    private boolean updateCopyVmInSpm(Guid storagePoolId, VM vm, Guid storageDomainId) {
        HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> vmsAndMetaDictionary = new HashMap<>();
        OvfManager ovfManager = new OvfManager();
        ArrayList<DiskImage> AllVmImages = new ArrayList<>();
        List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces != null) {
            // TODO remove this when the API changes
            interfaces.clear();
            interfaces.addAll(getVmNetworkInterfaceDao().getAllForVm(vm.getId()));
        }

        List<Guid> imageGroupIds = new ArrayList<>();
        for (Disk disk : getDisksBasedOnImage()) {
                DiskImage diskImage = (DiskImage) disk;
                diskImage.setParentId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                diskImage.setImageTemplateId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                diskImage.setStorageIds(new ArrayList<>(Arrays.asList(storageDomainId)));
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
                AllVmImages.add(diskImage);
                imageGroupIds.add(disk.getId());
        }

        if (StringUtils.isEmpty(vm.getVmtName())) {
            VmTemplate t = getVmTemplateDao().get(vm.getVmtGuid());
            vm.setVmtName(t.getName());
        }
        getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        String vmMeta = ovfManager.exportVm(vm, AllVmImages, ClusterUtils.getCompatibilityVersion(vm));

        vmsAndMetaDictionary.put(vm.getId(), new KeyValuePairCompat<>(vmMeta, imageGroupIds));
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId, vmsAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return runVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
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
            disksImages = ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, false, true);
        }
        return disksImages;
    }

    private void copyAllMemoryImages(Guid containerID) {
        for (Snapshot snapshot : snapshotsWithMemory) {
            List<Guid> guids = GuidUtils.getGuidListFromString(snapshot.getMemoryVolume());

            // copy the memory dump image
            VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
                            containerID, guids.get(0), guids.get(2), guids.get(3)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());

            // copy the memory configuration (of the VM) image
            vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryConfImage(
                            containerID, guids.get(0), guids.get(4), guids.get(5)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
            Guid containerID, Guid storageDomainId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID, imageId,
                volumeId, getParameters().getStorageDomainId(), ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setSourceDomainId(storageDomainId);
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setParentParameters(getParameters());

        StorageDomainStatic sourceDomain = getStorageDomainStaticDao().get(storageDomainId);

        // if the data domain is a block based storage, the memory volume type is preallocated
        // so we need to use copy collapse in order to convert it to be sparsed in the export domain
        if (sourceDomain.getStorageType().isBlockDomain()) {
            params.setUseCopyCollapse(true);
            params.setVolumeType(VolumeType.Sparse);
            params.setVolumeFormat(VolumeFormat.RAW);
        }

        return params;
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryConfImage(
            Guid containerID, Guid storageDomainId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID, imageId,
                volumeId, getParameters().getStorageDomainId(), ImageOperation.Copy);
        params.setParentCommand(getActionType());
        // This volume is always of type 'sparse' and format 'cow' so no need to convert,
        // and there're no snapshots for it so no reason to use copy collapse
        params.setUseCopyCollapse(false);
        params.setEntityInfo(getParameters().getEntityInfo());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setParentParameters(getParameters());
        params.setSourceDomainId(storageDomainId);
        return params;
    }

    @Override
    protected void moveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        for (DiskImage disk : disks) {
            VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForDisk(containerID, disk));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }

            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
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
            return getDiskImageDao().getAncestor(disk.getImageId());
        } else {
            return disk;
        }
    }

    /**
     * Check that vm is in export domain
     */
    protected boolean checkVmInStorageDomain() {
        boolean retVal = true;
        GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(getVm()
                .getStoragePoolId(), getParameters().getStorageDomainId());
        VdcQueryReturnValue qretVal = runInternalQuery(VdcQueryType.GetVmsFromExportDomain,
                tempVar);

        if (qretVal.getSucceeded()) {
            ArrayList<VM> vms = qretVal.getReturnValue();
            for (VM vm : vms) {
                if (vm.getId().equals(getVm().getId())) {
                    if (!getParameters().getForceOverride()) {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_GUID_ALREADY_EXIST);
                        retVal = false;
                        break;
                    }
                } else if (vm.getName().equals(getVm().getName())) {
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
                    retVal = false;
                    break;
                }
            }
        }
        return retVal;
    }

    public static boolean checkTemplateInStorageDomain(Guid storagePoolId,
            Guid storageDomainId,
            final Guid tmplId,
            EngineContext engineContext) {
        boolean retVal = false;
        GetAllFromExportDomainQueryParameters tempVar = new GetAllFromExportDomainQueryParameters(storagePoolId,
                storageDomainId);
        VdcQueryReturnValue qretVal = Backend.getInstance().runInternalQuery(VdcQueryType.GetTemplatesFromExportDomain,
                tempVar, engineContext);

        if (qretVal.getSucceeded()) {
            if (!VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(tmplId)) {
                Map<VmTemplate, List<DiskImage>> templates = qretVal.getReturnValue();
                retVal = templates.keySet().stream().anyMatch(vmTemplate -> vmTemplate.getId().equals(tmplId));
            } else {
                retVal = true;
            }
        }
        return retVal;
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

    protected boolean updateVmInSpm() {
        OvfUpdateProcessHelper ovfHelper = new OvfUpdateProcessHelper();
        Map<Guid, KeyValuePairCompat<String, List<Guid>>> metaDictionary = new HashMap<>();
        ovfHelper.loadVmData(getVm());
        ovfHelper.buildMetadataDictionaryForVm(getVm(),
                metaDictionary,
                ovfHelper.getVmImagesFromDb(getVm()));
        return ovfHelper.executeUpdateVmInSpmCommand(getVm().getStoragePoolId(),
                metaDictionary, getParameters().getStorageDomainId());
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
        VmHandler.unLockVm(vm);
        setSucceeded(true);
    }

    private void populateVmData(VM vm) {
        VmHandler.updateDisksFromDb(vm);
        VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        VmDeviceUtils.setVmDevices(vm.getStaticData());
    }

    private void endCopyCollapseOperations(VM vm) {
        vm.setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        vm.setVmtName(null);
        Snapshot activeSnapshot = getSnapshotDao().get(getSnapshotDao().getId(vm.getId(), SnapshotType.ACTIVE));
        vm.setSnapshots(Arrays.asList(activeSnapshot));

        try {
            updateCopyVmInSpm(getVm().getStoragePoolId(),
                    vm, getParameters()
                            .getStorageDomainId());
        }
        catch (EngineException e) {
            log.error("Updating VM OVF in export domain failed.", e);
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED_UPDATING_OVF);
        }
    }

    private void updateSnapshotOvf(VM vm) {
        vm.setSnapshots(getSnapshotDao().getAllWithConfiguration(getVm().getId()));
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
        VmHandler.unLockVm(vm);
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
}
