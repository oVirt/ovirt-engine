package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.utils.VmUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmCommand<T extends ImportVmParameters> extends ImportVmCommandBase<T>
        implements QuotaStorageDependent {

    private static final Logger log = LoggerFactory.getLogger(ImportVmCommand.class);

    private List<DiskImage> imageList;

    private final SnapshotsManager snapshotsManager = new SnapshotsManager();
    private MacPool macPool;

    @Override
    protected void init() {
        super.init();
        setVmId(getParameters().getContainerId());
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        if (getParameters().getVm() != null && getVm().getDiskMap() != null) {
            imageList = new ArrayList<>();
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    imageList.add((DiskImage) disk);
                }
            }
        }
        ensureDomainMap(imageList, getParameters().getDestDomainId());
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getContainerId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.REMOTE_VM,
                        getVmIsBeingImportedMessage()));
    }

    public ImportVmCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        macPool = getMacPool();
        Map<Guid, StorageDomain> domainsMap = new HashMap<>();
        if (!validateBeforeCloneVm(domainsMap)) {
            return false;
        }

        if (getParameters().isImportAsNewEntity()) {
            initImportClonedVm();

            if (getVm().getInterfaces().size() > macPool.getAvailableMacsCount()) {
                return failValidation(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
            }
        }

        if (!validateBallonDevice()) {
            return false;
        }

        if (!validateSoundDevice()) {
            return false;
        }

        return validateAfterCloneVm(domainsMap);
    }

    private void initImportClonedVm() {
        Guid guid = getParameters().getVm().getId();
        getVm().setId(guid);
        setVmId(guid);
        getVm().setName(getParameters().getVm().getName());
        getVm().setStoragePoolId(getStoragePoolId());
        getParameters().setVm(getVm());
        for (VmNic iface : getVm().getInterfaces()) {
            iface.setId(Guid.newGuid());
        }
    }

    protected boolean validateBeforeCloneVm(Map<Guid, StorageDomain> domainsMap) {
        if (getVm() != null) {
            setDescription(getVmName());
        }

        if (getStoragePool() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        Set<Guid> destGuids = new HashSet<>(imageToDestinationDomainMap.values());
        for (Guid destGuid : destGuids) {
            StorageDomain storageDomain = getStorageDomain(destGuid);
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
                return false;
            }

            domainsMap.put(destGuid, storageDomain);
        }

        if (!isImagesAlreadyOnTarget() && getParameters().isImportAsNewEntity()
                && isCopyCollapseDisabledWithSnapshotsOrWithTemplate()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMPORT_CLONE_NOT_COLLAPSED,
                    String.format("$VmName %1$s", getVmName()));
        }

        // Register can never happen with copyCollapse = true since there's no copy operation involved.
        if (isImagesAlreadyOnTarget() && getParameters().getCopyCollapse()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMPORT_UNREGISTERED_NOT_COLLAPSED);
        }

        if (!isImagesAlreadyOnTarget()) {
            setSourceDomainId(getParameters().getSourceDomainId());
            StorageDomainValidator validator = new StorageDomainValidator(getSourceDomain());
            if (validator.isDomainExistAndActive().isValid()
                    && getSourceDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
            }
            if (!validateAndSetVmFromExportDomain()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND_ON_EXPORT_DOMAIN);
            }
        }

        if (!validateImages(domainsMap)) {
            return false;
        }

        return true;
    }

    private boolean isCopyCollapseDisabledWithSnapshotsOrWithTemplate() {
        // If there are no snapshots we may not care if copyCollapse = false
        // There's always at least one snapshot (Active).
        // In case the VM is based on a template, we need to take copyCollapse in account
        return ((getParameters().getVm().getSnapshots().size() > 1) ||
                (!VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getVmtGuid())
                        && getVmTemplate() != null))
                && !getParameters().getCopyCollapse();
    }

    private boolean isCopyCollapseOrNoSnapshots() {
        return !isCopyCollapseDisabledWithSnapshotsOrWithTemplate();
    }

    protected boolean validateAndSetVmFromExportDomain() {
        VM vm = getVmFromExportDomain(getParameters().getVmId());
        if (vm == null) {
            return false;
        }
        // At this point we should work with the VM that was read from
        // the OVF because the VM from the parameters may lack images
        setVmFromExportDomain(vm);
        return true;
    }

    protected boolean validateImages(Map<Guid, StorageDomain> domainsMap) {
        List<String> validationMessages = getReturnValue().getValidationMessages();

        // Iterate over all the VM images (active image and snapshots)
        for (DiskImage image : getImages()) {
            if (Guid.Empty.equals(image.getVmSnapshotId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
            }

            if (getParameters().getCopyCollapse()) {
                // If copy collapse sent then iterate over the images got from the parameters, until we got
                // a match with the image from the VM.
                for (DiskImage p : imageList) {
                    // copy the new disk volume format/type if provided,
                    // only if requested by the user
                    if (p.getImageId().equals(image.getImageId())) {
                        if (p.getVolumeFormat() != null) {
                            image.setVolumeFormat(p.getVolumeFormat());
                        }
                        if (p.getVolumeType() != null) {
                            image.setVolumeType(p.getVolumeType());
                        }
                        // Validate the configuration of the image got from the parameters.
                        if (!validateImageConfig(validationMessages, domainsMap, image)) {
                            return false;
                        }
                        break;
                    }
                }
            }

            image.setStoragePoolId(getParameters().getStoragePoolId());
            // we put the source domain id in order that copy will
            // work properly.
            // we fix it to DestDomainId in
            // MoveOrCopyAllImageGroups();
            image.setStorageIds(new ArrayList<>(Arrays.asList(getSourceDomainId(image))));
        }

        Map<Guid, List<DiskImage>> images = ImagesHandler.getImagesLeaf(getImages());
        for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
            Guid id = entry.getKey();
            List<DiskImage> diskList = entry.getValue();
            getVm().getDiskMap().put(id, getActiveVolumeDisk(diskList));
        }

        return true;
    }

    private void setVmFromExportDomain(VM vm) {
        // preserve the given name
        if (getVmName() != null) {
            vm.setName(getVmName());
        }
        if (Guid.isNullOrEmpty(vm.getStoragePoolId())) {
            vm.setStoragePoolId(getStoragePoolId());
        }
        setVm(vm);
        initGraphicsData();
    }

    private void initGraphicsData() {
        ImportUtils.updateGraphicsDevices(getVm().getStaticData(), getEffectiveCompatibilityVersion());
    }

    protected DiskImage getActiveVolumeDisk(List<DiskImage> diskList) {
        return diskList.get(diskList.size() - 1);
    }

    protected VM getVmFromExportDomain(Guid vmId) {
        for (VM vm : getVmsFromExportDomain()) {
            if (vmId.equals(vm.getId())) {
                return vm;
            }
        }

        return null;
    }

    /**
     * Load images from Import/Export domain.
     *
     * @return A {@link List} of {@link VM}s from the export domain.
     */
    @SuppressWarnings("unchecked")
    protected List<VM> getVmsFromExportDomain() {
        VdcQueryReturnValue qRetVal = runInternalQuery(
                VdcQueryType.GetVmsFromExportDomain,
                new GetAllFromExportDomainQueryParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId()));
        return (List<VM>) (qRetVal.getSucceeded() ? qRetVal.getReturnValue() : Collections.emptyList());
    }

    private boolean validateImageConfig(List<String> validationMessages,
            Map<Guid, StorageDomain> domainsMap,
            DiskImage image) {
        return ImagesHandler.checkImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(image.getId()))
                .getStorageStaticData(),
                image,
                validationMessages);
    }

    protected boolean validateAfterCloneVm(Map<Guid, StorageDomain> domainsMap) {
        VM vmFromParams = getParameters().getVm();

        // check that the imported vm guid is not in engine
        if (!validateNoDuplicateVm()) {
            return false;
        }

        if (!validateNoDuplicateDiskImages(imageList)) {
            return false;
        }

        setVmTemplateId(getVm().getVmtGuid());
        if (!templateExists() || !checkTemplateInStorageDomain() || !checkImagesGUIDsLegal() || !validateUniqueVmName()) {
            return false;
        }

        if (!VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getVmtGuid())
                && getVmTemplate() != null
                && getVmTemplate().getStatus() == VmTemplateStatus.Locked) {
            return failValidation(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }

        if (getParameters().getCopyCollapse() && vmFromParams.getDiskMap() != null) {
            for (Disk disk : vmFromParams.getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage key = (DiskImage) getVm().getDiskMap().get(disk.getId());

                    if (key != null) {
                        if (!ImagesHandler.checkImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(key.getId()))
                                .getStorageStaticData(),
                                (DiskImageBase) disk,
                                getReturnValue().getValidationMessages())) {
                            return false;
                        }
                    }
                }
            }
        }

        // if collapse true we check that we have the template on source
        // (backup) domain
        if (getParameters().getCopyCollapse() && !isTemplateExistsOnExportDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING,
                    String.format("$DomainName %1$s",
                            getStorageDomainStaticDao().get(getParameters().getSourceDomainId()).getStorageName()));
        }

        if (!validateVmArchitecture()) {
            return false;
        }

        if (!validateVdsCluster()) {
            return false;
        }

        if (!isImagesAlreadyOnTarget()) {
            if (!handleDestStorageDomains()) {
                return false;
            }
        }

        if (!validateUsbPolicy()) {
            return false;
        }

        if (!validateGraphicsAndDisplay()) {
            return false;
        }

        if (!validate(getImportValidator().validateMacAddress(getVm().getInterfaces()))) {
            return false;
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        return true;
    }

    protected boolean handleDestStorageDomains() {
        List<DiskImage> dummiesDisksList = createDiskDummiesForSpaceValidations(imageList);
        if (getParameters().getCopyCollapse()) {
            Snapshot activeSnapshot = getActiveSnapshot();
            if (activeSnapshot != null && activeSnapshot.containsMemory()) {
                // Checking space for memory volume of the active image (if there is one)
                StorageDomain storageDomain = updateStorageDomainInMemoryVolumes(dummiesDisksList);
                if (storageDomain == null) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
                }
            }
        } else { // Check space for all the snapshot's memory volumes
            if (!updateDomainsForMemoryImages(dummiesDisksList)) {
                return false;
            }
        }
        return validate(getImportValidator().validateSpaceRequirements(dummiesDisksList));
    }

    /**
     * For each snapshot that has memory volume, this method updates the memory volume with the storage pool and storage
     * domain it's going to be imported to.
     *
     * @return true if we managed to assign storage domain for every memory volume, false otherwise
     */
    private boolean updateDomainsForMemoryImages(List<DiskImage> disksList) {
        Map<String, String> handledMemoryVolumes = new HashMap<>();
        for (Snapshot snapshot : getVm().getSnapshots()) {
            String memoryVolume = snapshot.getMemoryVolume();
            if (memoryVolume.isEmpty()) {
                continue;
            }

            if (handledMemoryVolumes.containsKey(memoryVolume)) {
                // replace the volume representation with the one with the correct domain & pool
                snapshot.setMemoryVolume(handledMemoryVolumes.get(memoryVolume));
                continue;
            }

            StorageDomain storageDomain = updateStorageDomainInMemoryVolumes(disksList);
            if (storageDomain == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
            }
            String modifiedMemoryVolume = MemoryUtils.changeStorageDomainAndPoolInMemoryState(
                    memoryVolume, storageDomain.getId(), getParameters().getStoragePoolId());
            // replace the volume representation with the one with the correct domain & pool
            snapshot.setMemoryVolume(modifiedMemoryVolume);
            // save it in case we'll find other snapshots with the same memory volume
            handledMemoryVolumes.put(memoryVolume, modifiedMemoryVolume);
        }
        return true;
    }

    private StorageDomain updateStorageDomainInMemoryVolumes(List<DiskImage> disksList) {
        List<DiskImage> memoryDisksList =
                MemoryUtils.createDiskDummies(VmUtils.getSnapshotMemorySizeInBytes(getVm()),
                        MemoryUtils.METADATA_SIZE_IN_BYTES);
        StorageDomain storageDomain = MemoryStorageHandler.getInstance().findStorageDomainForMemory(
                getParameters().getStoragePoolId(), memoryDisksList, getVmDisksDummies(), getVm());
        disksList.addAll(memoryDisksList);
        return storageDomain;
    }

    private Collection<DiskImage> getVmDisksDummies() {
        Collection<DiskImage> disksDummies = new LinkedList<>();
        for (Guid storageDomainId : getParameters().getImageToDestinationDomainMap().values()) {
            DiskImage diskImage = new DiskImage();
            diskImage.setStorageIds(new ArrayList<>(Arrays.asList(storageDomainId)));
            disksDummies.add(diskImage);
        }
        return disksDummies;
    }

    protected boolean validateNoDuplicateDiskImages(Iterable<DiskImage> images) {
        if (!getParameters().isImportAsNewEntity()) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);
            return validate(diskImagesValidator.diskImagesAlreadyExist());
        }

        return true;
    }

    private boolean isTemplateExistsOnExportDomain() {
        if (VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getParameters().getVm().getVmtGuid())) {
            return true;
        }

        VdcQueryReturnValue qRetVal = runInternalQuery(
                VdcQueryType.GetTemplatesFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId()));

        if (qRetVal.getSucceeded()) {
            Map<VmTemplate, ?> templates = qRetVal.getReturnValue();

            for (VmTemplate template : templates.keySet()) {
                if (getParameters().getVm().getVmtGuid().equals(template.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkTemplateInStorageDomain() {
        boolean retValue = validate(getImportValidator().verifyDisks(imageList, imageToDestinationDomainMap));
        if (retValue && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getVmtGuid())
                && !getParameters().getCopyCollapse()) {
            List<StorageDomain> domains = runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                    new IdQueryParameters(getVm().getVmtGuid())).getReturnValue();
            Set<Guid> domainsId = domains.stream().map(StorageDomain::getId).collect(Collectors.toSet());

            if (Collections.disjoint(domainsId, imageToDestinationDomainMap.values())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    private boolean templateExists() {
        if (getVmTemplate() == null && !getParameters().getCopyCollapse()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return true;
    }

    protected Guid getSourceDomainId(DiskImage image) {
        return getParameters().getSourceDomainId();
    }

    protected boolean checkImagesGUIDsLegal() {
        for (DiskImage image : getImages()) {
            Guid imageGUID = image.getImageId();
            Guid storagePoolId = image.getStoragePoolId() != null ? image.getStoragePoolId()
                    : Guid.Empty;
            Guid storageDomainId = getSourceDomainId(image);
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;

            VDSReturnValue retValue = runVdsCommand(
                    VDSCommandType.DoesImageExist,
                    new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                            imageGUID));

            if (Boolean.FALSE.equals(retValue.getReturnValue())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            }
        }
        return true;
    }

    @Override
    protected void processImages() {
        processImages(!isImagesAlreadyOnTarget());
        // if there are no tasks, we can just unlock the VM
        if (getReturnValue().getVdsmTaskIdList().isEmpty()) {
            VmHandler.unLockVm(getVm());
        }
    }

    private void processImages(final boolean useCopyImages) {
        TransactionSupport.executeInNewTransaction(() -> {
            addVmImagesAndSnapshots();
            addMemoryImages();
            updateSnapshotsFromExport();
            if (useCopyImages) {
                moveOrCopyAllImageGroups();
            }
            VmDeviceUtils.addImportedDevices(getVm().getStaticData(), getParameters().isImportAsNewEntity());
            if (getParameters().isImportAsNewEntity()) {
                getParameters().setVm(getVm());
                setVmId(getVm().getId());
            }
            return null;

        });
    }

    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVm().getId(),
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false, true));
        copyAllMemoryImages(getVm().getId());
    }

    private void copyAllMemoryImages(Guid containerId) {
        for (String memoryVolumes : MemoryUtils.getMemoryVolumesFromSnapshots(getVm().getSnapshots())) {
            List<Guid> guids = GuidUtils.getGuidListFromString(memoryVolumes);

            // copy the memory dump image
            VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
                            containerId, guids.get(0), guids.get(2), guids.get(3)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy memory image");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());

            // copy the memory configuration (of the VM) image
            vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryConfImage(
                            containerId, guids.get(0), guids.get(4), guids.get(5)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy metadata image");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryDumpImage(Guid containerID,
            Guid storageId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                imageId, volumeId, imageId, volumeId, storageId, ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setSourceDomainId(getParameters().getSourceDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setImportEntity(true);
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        params.setParentParameters(getParameters());

        StorageDomainStatic storageDomain = getStorageDomainStaticDao().get(storageId);
        if (storageDomain.getStorageType().isBlockDomain()) {
            params.setUseCopyCollapse(true);
            params.setVolumeType(VolumeType.Preallocated);
            params.setVolumeFormat(VolumeFormat.RAW);
        }

        return params;
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryConfImage(Guid containerID,
            Guid storageId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                imageId, volumeId, imageId, volumeId, storageId, ImageOperation.Copy);
        params.setParentCommand(getActionType());
        // This volume is always of type 'sparse' and format 'cow' so no need to convert,
        // and there're no snapshots for it so no reason to use copy collapse
        params.setUseCopyCollapse(false);
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setParentParameters(getParameters());
        params.setSourceDomainId(getParameters().getSourceDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setImportEntity(true);
        return params;
    }

    protected void moveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        for (DiskImage disk : disks) {
            VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForDisk(disk, containerID));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy disk!");
            }

            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForDisk(DiskImage disk, Guid containerID) {
        Guid originalDiskId = newDiskIdForDisk.get(disk.getId()).getId();
        Guid destinationDomain = imageToDestinationDomainMap.get(originalDiskId);
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                originalDiskId,
                newDiskIdForDisk.get(disk.getId()).getImageId(),
                disk.getId(),
                disk.getImageId(),
                destinationDomain, ImageOperation.Copy);
        params.setParentCommand(getActionType());
        params.setUseCopyCollapse(isCopyCollapseOrNoSnapshots());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setSourceDomainId(getParameters().getSourceDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setImportEntity(true);
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        params.setRevertDbOperationScope(ImageDbOperationScope.IMAGE);
        params.setQuotaId(disk.getQuotaId() != null ? disk.getQuotaId() : getParameters().getQuotaId());
        params.setDiskProfileId(disk.getDiskProfileId());
        if (getParameters().getVm().getDiskMap() != null
                && getParameters().getVm().getDiskMap().containsKey(originalDiskId)) {
            DiskImageBase diskImageBase =
                    (DiskImageBase) getParameters().getVm().getDiskMap().get(originalDiskId);
            params.setVolumeType(diskImageBase.getVolumeType());
            params.setVolumeFormat(diskImageBase.getVolumeFormat());
        }
        params.setParentParameters(getParameters());
        return params;
    }

    protected void addVmImagesAndSnapshots() {
        Map<Guid, List<DiskImage>> images = ImagesHandler.getImagesLeaf(getImages());

        if (isCopyCollapseOrNoSnapshots()) {
            Guid snapshotId = Guid.newGuid();
            int aliasCounter = 0;
            for (List<DiskImage> diskList : images.values()) {
                DiskImage disk = getActiveVolumeDisk(diskList);
                disk.setParentId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                disk.setImageTemplateId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
                disk.setVmSnapshotId(snapshotId);
                disk.setActive(true);

                if (getParameters().getVm().getDiskMap() != null
                        && getParameters().getVm().getDiskMap().containsKey(disk.getId())) {
                    DiskImageBase diskImageBase =
                            (DiskImageBase) getParameters().getVm().getDiskMap().get(disk.getId());
                    disk.setVolumeFormat(diskImageBase.getVolumeFormat());
                    disk.setVolumeType(diskImageBase.getVolumeType());
                }
                setDiskStorageDomainInfo(disk);

                if (getParameters().isImportAsNewEntity()) {
                    generateNewDiskId(diskList, disk);
                    updateManagedDeviceMap(disk, getVm().getStaticData().getManagedDeviceMap());
                } else {
                    newDiskIdForDisk.put(disk.getId(), disk);
                }
                disk.setCreationDate(new Date());
                saveImage(disk);
                ImagesHandler.setDiskAlias(disk, getVm(), ++aliasCounter);
                saveBaseDisk(disk);
                saveDiskVmElement(disk.getId(), getVmId(), disk.getDiskVmElementForVm(getParameters().getVmId()));
                saveDiskImageDynamic(disk);
            }

            Snapshot snapshot = addActiveSnapshot(snapshotId);
            getVm().setSnapshots(Arrays.asList(snapshot));
        } else {
            Guid snapshotId = null;
            for (DiskImage disk : getImages()) {
                disk.setActive(false);
                setDiskStorageDomainInfo(disk);

                saveImage(disk);
                snapshotId = disk.getVmSnapshotId();
                saveSnapshotIfNotExists(snapshotId, disk);
                saveDiskImageDynamic(disk);
            }

            int aliasCounter = 0;
            for (List<DiskImage> diskList : images.values()) {
                DiskImage disk = getActiveVolumeDisk(diskList);
                newDiskIdForDisk.put(disk.getId(), disk);
                snapshotId = disk.getVmSnapshotId();
                disk.setActive(true);
                ImagesHandler.setDiskAlias(disk, getVm(), ++aliasCounter);
                updateImage(disk);
                saveBaseDisk(disk);
                saveDiskVmElement(disk.getId(), getVmId(), disk.getDiskVmElementForVm(getParameters().getVmId()));
            }

            // Update active snapshot's data, since it was inserted as a regular snapshot.
            updateActiveSnapshot(snapshotId);
        }
    }

    private void setDiskStorageDomainInfo(DiskImage disk) {
        ArrayList<Guid> storageDomain = new ArrayList<>();
        storageDomain.add(imageToDestinationDomainMap.get(disk.getId()));
        disk.setStorageIds(storageDomain);
    }

    /** Saves the base disk object */
    protected void saveBaseDisk(DiskImage disk) {
        getBaseDiskDao().save(disk);
    }

    protected void saveDiskVmElement(Guid diskId, Guid vmId, DiskVmElement diskVmElement) {
        DiskVmElement dve = DiskVmElement.copyOf(diskVmElement, diskId, vmId);
        getDiskVmElementDao().save(dve);
    }

    /** Save the entire image, including it's storage mapping */
    protected void saveImage(DiskImage disk) {
        BaseImagesCommand.saveImage(disk);
    }

    /** Updates an image of a disk */
    protected void updateImage(DiskImage disk) {
        getImageDao().update(disk.getImage());
    }

    /**
     * Generates and saves a {@link DiskImageDynamic} for the given <code>disk</code>
     *
     * @param disk
     *            The imported disk
     **/
    protected void saveDiskImageDynamic(DiskImage disk) {
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setActualSize(disk.getActualSizeInBytes());
        getDiskImageDynamicDao().save(diskDynamic);
    }

    /**
     * Saves a new active snapshot for the VM
     *
     * @param snapshotId
     *            The ID to assign to the snapshot
     * @return The generated snapshot
     */
    protected Snapshot addActiveSnapshot(Guid snapshotId) {
        Snapshot activeSnapshot = getActiveSnapshot();
        // We currently don't support using memory from a
        // snapshot that was taken for VM with different id
        String memoryVolume = activeSnapshot != null && !getParameters().isImportAsNewEntity() ?
                activeSnapshot.getMemoryVolume() : StringUtils.EMPTY;
        return snapshotsManager.addActiveSnapshot(
                snapshotId,
                getVm(),
                memoryVolume,
                getCompensationContext());
    }

    @Override
    protected Snapshot getActiveSnapshot() {
        Snapshot activeSnapshot = VmHandler.getActiveSnapshot(getVm());
        if (activeSnapshot == null) {
            log.warn("VM '{}' doesn't have active snapshot in export domain", getVmId());
        }
        return activeSnapshot;
    }

    /**
     * Go over the snapshots that were read from the export data. If the snapshot exists (since it was added for the
     * images), it will be updated. If it doesn't exist, it will be saved.
     */
    private void updateSnapshotsFromExport() {
        if (getVm().getSnapshots() == null) {
            return;
        }

        for (Snapshot snapshot : getVm().getSnapshots()) {
            if (!StringUtils.isEmpty(snapshot.getMemoryVolume())) {
                updateMemoryDisks(snapshot);
            }

            if (getSnapshotDao().exists(getVm().getId(), snapshot.getId())) {
                getSnapshotDao().update(snapshot);
            } else {
                getSnapshotDao().save(snapshot);
            }
        }
    }

    private void updateMemoryDisks(Snapshot snapshot) {
        List<Guid> guids = GuidUtils.getGuidListFromString(snapshot.getMemoryVolume());
        snapshot.setMemoryDiskId(guids.get(2));
        snapshot.setMetadataDiskId(guids.get(4));
    }

    private void addMemoryImages() {
        getVm().getSnapshots().stream()
        .filter(snapshot -> !StringUtils.isEmpty(snapshot.getMemoryVolume()))
        .forEach(snapshot -> {
            addDisk(createMemoryDisk(snapshot));
            addDisk(createMetadaaDisk(snapshot));
        });
    }

    private DiskImage createMemoryDisk(Snapshot snapshot) {
        List<Guid> guids = GuidUtils.getGuidListFromString(snapshot.getMemoryVolume());
        SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = new SnapshotVmConfigurationHelper();
        VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                snapshot.getVmConfiguration(),
                snapshot.getVmId(), snapshot.getId());
        DiskImage memoryDisk = MemoryUtils.createMemoryDisk(
                vm,
                getStorageDomainStaticDao().get(guids.get(0)).getStorageType());
        memoryDisk.setId(guids.get(2));
        memoryDisk.setImageId(guids.get(3));
        memoryDisk.setStorageIds(new ArrayList<>(Collections.singletonList(guids.get(0))));
        memoryDisk.setStoragePoolId(guids.get(1));
        memoryDisk.setCreationDate(snapshot.getCreationDate());
        return memoryDisk;
    }

    private DiskImage createMetadaaDisk(Snapshot snapshot) {
        List<Guid> guids = GuidUtils.getGuidListFromString(snapshot.getMemoryVolume());
        DiskImage memoryDisk = MemoryUtils.createMetadataDisk();
        memoryDisk.setId(guids.get(4));
        memoryDisk.setImageId(guids.get(5));
        memoryDisk.setStorageIds(new ArrayList<>(Collections.singletonList(guids.get(0))));
        memoryDisk.setStoragePoolId(guids.get(1));
        memoryDisk.setCreationDate(snapshot.getCreationDate());
        return memoryDisk;
    }

    private void addDisk(DiskImage disk) {
        saveImage(disk);
        saveBaseDisk(disk);
        saveDiskImageDynamic(disk);
    }

    /**
     * Save a snapshot if it does not exist in the database.
     *
     * @param snapshotId
     *            The snapshot to save.
     * @param disk
     *            The disk containing the snapshot's information.
     */
    protected void saveSnapshotIfNotExists(Guid snapshotId, DiskImage disk) {
        if (!getSnapshotDao().exists(getVm().getId(), snapshotId)) {
            getSnapshotDao().save(
                    new Snapshot(snapshotId,
                            SnapshotStatus.OK,
                            getVm().getId(),
                            null,
                            SnapshotType.REGULAR,
                            disk.getDescription(),
                            disk.getLastModifiedDate(),
                            disk.getAppList()));
        }
    }

    /**
     * Update a snapshot and make it the active snapshot.
     *
     * @param snapshotId
     *            The snapshot to update.
     */
    protected void updateActiveSnapshot(Guid snapshotId) {
        getSnapshotDao().update(
                new Snapshot(snapshotId,
                        SnapshotStatus.OK,
                        getVm().getId(),
                        null,
                        SnapshotType.ACTIVE,
                        "Active VM snapshot",
                        new Date(),
                        null));
    }

    @Override
    protected void endSuccessfully() {
        checkTrustedService();
        endActionOnAllImageGroups();
        VmHandler.unLockVm(getVm());
        setSucceeded(true);
    }

    private void checkTrustedService() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VmName", getVmName());
        if (getVm().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVm().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            getBackend().endAction(VdcActionType.CopyImageGroup,
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    protected void endWithFailure() {
        // Going to try and refresh the VM by re-loading it form DB
        setVm(null);

        if (getVm() != null) {
            removeVmSnapshots();
            endActionOnAllImageGroups();
            removeVmNetworkInterfaces();
            getVmDynamicDao().remove(getVmId());
            getVmStatisticsDao().remove(getVmId());
            getVmStaticDao().remove(getVmId());
            setSucceeded(true);
        } else {
            setVm(getParameters().getVm()); // Setting VM from params, for logging purposes
            // No point in trying to end action again, as the imported VM does not exist in the DB.
            getReturnValue().setEndActionTryAgain(false);
        }
    }

    @Override
    protected void removeVmSnapshots() {
        Guid vmId = getVmId();
        Set<String> memoryStates = snapshotsManager.removeSnapshots(vmId);
        for (String memoryState : memoryStates) {
            removeMemoryVolumes(memoryState, vmId);
        }
    }

    private void removeMemoryVolumes(String memoryVolume, Guid vmId) {
        VdcReturnValueBase retVal = runInternalAction(
                VdcActionType.RemoveMemoryVolumes,
                new RemoveMemoryVolumesParameters(memoryVolume, vmId), cloneContextAndDetachFromParent());

        if (!retVal.getSucceeded()) {
            log.error("Failed to remove memory volumes '{}'", memoryVolume);
        }
    }

    protected void removeVmNetworkInterfaces() {
        new VmInterfaceManager(macPool).removeAll(getVmId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_IMPORT_VM : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        }
        return super.getAuditLogTypeValue();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        if (getParameters().isImportAsNewEntity()) {
            return addValidationGroup(ImportClonedEntity.class);
        }
        return addValidationGroup(ImportEntity.class);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        // Source domain
        permissionList.add(new PermissionSubject(getParameters().getSourceDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        // special permission is needed to use custom properties
        if (getVm() != null && !StringUtils.isEmpty(getVm().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getClusterId(),
                    VdcObjectType.Cluster,
                    ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }
        return permissionList;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (getParameters().getVm().getDiskMap() != null) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (Disk disk : getParameters().getVm().getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    map.put(diskImage, imageToDestinationDomainMap.get(diskImage.getId()));
                }
            }
            return validate(DiskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        for (Disk disk : getParameters().getVm().getDiskMap().values()) {
            // TODO: handle import more than once;
            if (disk instanceof DiskImage) {
                DiskImage diskImage = (DiskImage) disk;
                list.add(new QuotaStorageConsumptionParameter(
                        diskImage.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        imageToDestinationDomainMap.get(diskImage.getId()),
                        (double) diskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    protected List<DiskImage> getImages() {
        return getVm().getImages();
    }

    @Override
    protected MacPool getMacPool() {
        return super.getMacPool();
    }
}
