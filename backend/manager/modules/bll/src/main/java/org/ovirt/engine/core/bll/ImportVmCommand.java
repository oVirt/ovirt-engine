package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.memory.MemoryImageRemover;
import org.ovirt.engine.core.bll.memory.MemoryUtils;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
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
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
@LockIdNameAttribute(isReleaseAtEndOfExecute = false)
public class ImportVmCommand extends MoveOrCopyTemplateCommand<ImportVmParameters>
        implements QuotaStorageDependent, TaskHandlerCommand<ImportVmParameters> {
    private static final Log log = LogFactory.getLog(ImportVmCommand.class);

    private static VmStatic vmStaticForDefaultValues = new VmStatic();
    private List<DiskImage> imageList;
    private final List<Guid> diskGuidList = new ArrayList<Guid>();
    private final List<Guid> imageGuidList = new ArrayList<Guid>();
    private final List<String> macsAdded = new ArrayList<String>();
    private final SnapshotsManager snapshotsManager = new SnapshotsManager();

    public ImportVmCommand(ImportVmParameters parameters) {
        super(parameters);
        setVmId(parameters.getContainerId());
        setVm(parameters.getVm());
        setVdsGroupId(parameters.getVdsGroupId());
        if (parameters.getVm() != null && getVm().getDiskMap() != null) {
            imageList = new ArrayList<DiskImage>();
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
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.REMOTE_VM,
                        getVmIsBeingImportedMessage()));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!StringUtils.isBlank(getParameters().getVm().getName())) {
            return Collections.singletonMap(getParameters().getVm().getName(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME, VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
        }
        return null;
    }

    private String getVmIsBeingImportedMessage() {
        StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_BEING_IMPORTED.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    protected ImportVmCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public Guid getVmId() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVm().getId();
        }
        return super.getVmId();
    }

    @Override
    public VM getVm() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVm();
        }
        return super.getVm();
    }

    @Override
    protected boolean canDoAction() {
        Map<Guid, StorageDomain> domainsMap = new HashMap<Guid, StorageDomain>();

        if (!canDoActionBeforeCloneVm(domainsMap)) {
            return false;
        }

        if (getParameters().isImportAsNewEntity()) {
            initImportClonedVm();
        }

        return canDoActionAfterCloneVm(domainsMap);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    private void initImportClonedVm() {
        Guid guid = Guid.newGuid();
        getVm().setId(guid);
        setVmId(guid);
        getVm().setName(getParameters().getVm().getName());
        getVm().setStoragePoolId(getParameters().getStoragePoolId());
        getParameters().setVm(getVm());
        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            iface.setId(Guid.newGuid());
        }
    }

    private boolean canDoActionBeforeCloneVm(Map<Guid, StorageDomain> domainsMap) {
        List<String> canDoActionMessages = getReturnValue().getCanDoActionMessages();

        if (getVm() != null) {
            setDescription(getVmName());
        }

        if (!checkStoragePool()) {
            return false;
        }

        Set<Guid> destGuids = new HashSet<Guid>(imageToDestinationDomainMap.values());
        for (Guid destGuid : destGuids) {
            StorageDomain storageDomain = getStorageDomain(destGuid);
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
            if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
                return false;
            }

            domainsMap.put(destGuid, storageDomain);
        }

        if (getParameters().isImportAsNewEntity() && !getParameters().getCopyCollapse()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMPORT_CLONE_NOT_COLLAPSED);
        }

        setSourceDomainId(getParameters().getSourceDomainId());
        StorageDomainValidator validator = new StorageDomainValidator(getSourceDomain());
        if (validator.isDomainExistAndActive().isValid() &&
                getSourceDomain().getStorageDomainType() != StorageDomainType.ImportExport) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }

        List<VM> vms = getVmsFromExportDomain();
        if (vms == null) {
            return false;
        }

        VM vm = LinqUtils.firstOrNull(vms, new Predicate<VM>() {
            @Override
            public boolean eval(VM evalVm) {
                return evalVm.getId().equals(getParameters().getVm().getId());
            }
        });

        if (vm != null) {
            // At this point we should work with the VM that was read from
            // the OVF
            setVm(vm);
            // Iterate over all the VM images (active image and snapshots)
            for (DiskImage image : getVm().getImages()) {
                if (Guid.Empty.equals(image.getVmSnapshotId())) {
                    return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
                }

                if (getParameters().getCopyCollapse()) {
                    // If copy collapse sent then iterate over the images got from the parameters, until we got
                    // a match with the image from the VM.
                    for (DiskImage p : imageList) {
                        // copy the new disk volume format/type if provided,
                        // only if requested by the user
                        if (p.getImageId().equals(image.getImageId())) {
                            if (p.getVolumeFormat() != null) {
                                image.setvolumeFormat(p.getVolumeFormat());
                            }
                            if (p.getVolumeType() != null) {
                                image.setVolumeType(p.getVolumeType());
                            }
                            // Validate the configuration of the image got from the parameters.
                            if (!validateImageConfig(canDoActionMessages, domainsMap, image)) {
                                return false;
                            }
                            break;
                        }
                    }
                } else {
                    // If no copy collapse sent, validate each image configuration (snapshot or active image).
                    if (!validateImageConfig(canDoActionMessages, domainsMap, image)) {
                        return false;
                    }
                }

                image.setStoragePoolId(getParameters().getStoragePoolId());
                // we put the source domain id in order that copy will
                // work properly.
                // we fix it to DestDomainId in
                // MoveOrCopyAllImageGroups();
                image.setStorageIds(new ArrayList<Guid>(Arrays.asList(getParameters().getSourceDomainId())));
            }

            Map<Guid, List<DiskImage>> images = ImagesHandler.getImagesLeaf(getVm().getImages());
            for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
                Guid id = entry.getKey();
                List<DiskImage> diskList = entry.getValue();
                getVm().getDiskMap().put(id, getActiveVolumeDisk(diskList));
            }
        }

        return true;
    }

    /**
     * Load images from Import/Export domain.
     * @return A {@link List} of {@link VM}s, or <code>null</code> if the query to the export domain failed.
     */
    protected List<VM> getVmsFromExportDomain() {
        GetAllFromExportDomainQueryParameters p =
                new GetAllFromExportDomainQueryParameters
                (getParameters().getStoragePoolId(), getParameters().getSourceDomainId());
        VdcQueryReturnValue qRetVal = getBackend().runInternalQuery(VdcQueryType.GetVmsFromExportDomain, p);
        return qRetVal.getSucceeded() ? (List<VM>) qRetVal.getReturnValue() : null;
    }

    private boolean validateImageConfig(List<String> canDoActionMessages,
            Map<Guid, StorageDomain> domainsMap,
            DiskImage image) {
        return ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(image.getId()))
                        .getStorageStaticData(),
                        image,
                        canDoActionMessages);
    }

    private boolean canDoActionAfterCloneVm(Map<Guid, StorageDomain> domainsMap) {
        VM vm = getParameters().getVm();

        // check that the imported vm guid is not in engine
        if (!validateNoDuplicateVm()) {
            return false;
        }

        if (!validateNoDuplicateDiskImages(imageList)) {
            return false;
        }

        if (!validateDiskInterface(imageList)) {
            return false;
        }

        setVmTemplateId(getVm().getVmtGuid());
        if (!templateExists() || !checkTemplateInStorageDomain() || !checkImagesGUIDsLegal() || !canAddVm()) {
            return false;
        }

        if (!VmTemplateHandler.BlankVmTemplateId.equals(getVm().getVmtGuid())
                && getVmTemplate() != null
                && getVmTemplate().getStatus() == VmTemplateStatus.Locked) {
            return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }

        if (getParameters().getCopyCollapse() && vm.getDiskMap() != null) {
            for (Disk disk : vm.getDiskMap().values()) {
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage key = (DiskImage) getVm().getDiskMap().get(disk.getId());

                    if (key != null) {
                        if (!ImagesHandler.CheckImageConfiguration(domainsMap.get(imageToDestinationDomainMap.get(key.getId()))
                                .getStorageStaticData(),
                                (DiskImageBase) disk,
                                getReturnValue().getCanDoActionMessages())) {
                            return false;
                        }
                    }
                }
            }
        }

        // if collapse true we check that we have the template on source
        // (backup) domain
        if (getParameters().getCopyCollapse() && !isTemplateExistsOnExportDomain()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_IMPORTED_TEMPLATE_IS_MISSING);
            addCanDoActionMessage(String.format("$DomainName %1$s",
                    getStorageDomainStaticDAO().get(getParameters().getSourceDomainId()).getStorageName()));
            return false;
        }

        if (!validateVdsCluster()) {
            return false;
        }

        Map<StorageDomain, Integer> domainMap = getSpaceRequirementsForStorageDomains(imageList);

        if (!setDomainsForMemoryImages(domainMap)) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_NO_SUITABLE_DOMAIN_FOUND);
        }

        for (Map.Entry<StorageDomain, Integer> entry : domainMap.entrySet()) {
            if (!doesStorageDomainhaveSpaceForRequest(entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        if (!validateUsbPolicy()) {
            return false;
        }

        if (!validateMacAddress(getVm().getInterfaces())) {
            return false;
        }

        return true;
    }

    /**
     * This method fills the given map of domain to the required size for storing memory images
     * within it, and also update the memory volume in each snapshot that has memory volume with
     * the right storage pool and storage domain where it is going to be imported.
     *
     * @param domain2requiredSize
     *           Maps domain to size required for storing memory volumes in it
     * @return true if we managed to assign storage domain for every memory volume, false otherwise
     */
    private boolean setDomainsForMemoryImages(Map<StorageDomain, Integer> domain2requiredSize) {
        Map<String, String> handledMemoryVolumes = new HashMap<String, String>();
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

            VM vm = getVmFromSnapshot(snapshot);
            int requiredSizeForMemory = (int) Math.ceil((vm.getTotalMemorySizeInBytes() +
                    HibernateVmCommand.META_DATA_SIZE_IN_BYTES) * 1.0 / BYTES_IN_GB);
            StorageDomain storageDomain = VmHandler.findStorageDomainForMemory(
                    getParameters().getStoragePoolId(),requiredSizeForMemory, domain2requiredSize);
            if (storageDomain == null) {
                return false;
            }
            domain2requiredSize.put(storageDomain,
                    domain2requiredSize.get(storageDomain) + requiredSizeForMemory);
            String modifiedMemoryVolume = MemoryUtils.changeStorageDomainAndPoolInMemoryState(
                    memoryVolume, storageDomain.getId(), getParameters().getStoragePoolId());
            // replace the volume representation with the one with the correct domain & pool
            snapshot.setMemoryVolume(modifiedMemoryVolume);
            // save it in case we'll find other snapshots with the same memory volume
            handledMemoryVolumes.put(memoryVolume, modifiedMemoryVolume);
        }
        return true;
    }

    /**
     * Validates that there is no duplicate VM.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateNoDuplicateVm() {
        VmStatic duplicateVm = getVmStaticDAO().get(getVm().getId());
        if (duplicateVm != null) {
            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_IMPORT_VM_EXISTS);
            addCanDoActionMessage(String.format("$VmName %1$s", duplicateVm.getName()));
            return false;
        }
        return true;
    }

    protected boolean isDiskExists(Guid id) {
        return getBaseDiskDao().exists(id);
    }

    protected boolean validateDiskInterface(Iterable<DiskImage> images) {
        for (DiskImage diskImage : images) {
            if (diskImage.getDiskInterface() == DiskInterface.VirtIO_SCSI &&
                    !FeatureSupported.virtIoScsi(getVdsGroup().getcompatibility_version())) {
                return failCanDoAction(VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
            }
        }

        return true;
    }

    protected boolean validateNoDuplicateDiskImages(Iterable<DiskImage> images) {
        if (!getParameters().isImportAsNewEntity()) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);
            return validate(diskImagesValidator.diskImagesAlreadyExist());
        }

        return true;
    }

    /**
     * Validates that that the required cluster exists.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateVdsCluster() {
        List<VDSGroup> groups = getVdsGroupDAO().getAllForStoragePool(getParameters().getStoragePoolId());
        for (VDSGroup group : groups) {
            if (group.getId().equals(getParameters().getVdsGroupId())) {
                return true;
            }
        }
        return failCanDoAction(VdcBllMessages.VDS_CLUSTER_IS_NOT_VALID);
    }

    /**
     * Validates the USB policy.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateUsbPolicy() {
        VM vm = getParameters().getVm();
        VmHandler.updateImportedVmUsbPolicy(vm.getStaticData());
        return VmHandler.isUsbPolicyLegal(vm.getUsbPolicy(),
                vm.getOs(),
                getVdsGroup(),
                getReturnValue().getCanDoActionMessages());
    }

    private boolean isTemplateExistsOnExportDomain() {
        if (VmTemplateHandler.BlankVmTemplateId.equals(getParameters().getVm().getVmtGuid())) {
            return true;
        }

        VdcQueryReturnValue qRetVal = Backend.getInstance().runInternalQuery(
                VdcQueryType.GetTemplatesFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId()));

        if (qRetVal.getSucceeded()) {
            Map<VmTemplate, ?> templates = (Map<VmTemplate, ?>) qRetVal.getReturnValue();

            for (VmTemplate template : templates.keySet()) {
                if (getParameters().getVm().getVmtGuid().equals(template.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean checkTemplateInStorageDomain() {
        boolean retValue = getParameters().isImportAsNewEntity() || checkIfDisksExist(imageList);
        if (retValue && !VmTemplateHandler.BlankVmTemplateId.equals(getVm().getVmtGuid())
                && !getParameters().getCopyCollapse()) {
            List<StorageDomain> domains = (List<StorageDomain>) Backend
                    .getInstance()
                    .runInternalQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                            new IdQueryParameters(getVm().getVmtGuid())).getReturnValue();
            List<Guid> domainsId = LinqUtils.foreach(domains, new Function<StorageDomain, Guid>() {
                @Override
                public Guid eval(StorageDomain storageDomainStatic) {
                    return storageDomainStatic.getId();
                }
            });

            if (Collections.disjoint(domainsId, imageToDestinationDomainMap.values())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return retValue;
    }

    private boolean templateExists() {
        if (getVmTemplate() == null && !getParameters().getCopyCollapse()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return true;
    }

    protected boolean checkImagesGUIDsLegal() {
        for (DiskImage image : getVm().getImages()) {
            Guid imageGUID = image.getImageId();
            Guid storagePoolId = image.getStoragePoolId() != null ? image.getStoragePoolId()
                    : Guid.Empty;
            Guid storageDomainId = getParameters().getSourceDomainId();
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;

            VDSReturnValue retValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DoesImageExist,
                            new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    imageGUID));

            if (Boolean.FALSE.equals(retValue.getReturnValue())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            }
        }
        return true;
    }

    protected boolean canAddVm() {
        // Checking if a desktop with same name already exists
        if (VmHandler.isVmWithSameNameExistStatic(getVm().getName())) {
            return failCanDoAction(VdcBllMessages.VM_CANNOT_IMPORT_VM_NAME_EXISTS);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        try {
            addVmToDb();
            processImages();
            // if there aren't tasks - we can just perform the end
            // vm related ops
            if (getReturnValue().getVdsmTaskIdList().isEmpty()) {
                endVmRelatedOps();
            }
        } catch (RuntimeException e) {
            MacPoolManager.getInstance().freeMacs(macsAdded);
            throw e;
        }

        setSucceeded(true);
    }

    private void addVmToDb() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addVmStatic();
                addVmDynamic();
                addVmInterfaces();
                addVmStatistics();
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    private void processImages() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                addVmImagesAndSnapshots();
                updateSnapshotsFromExport();
                moveOrCopyAllImageGroups();
                VmDeviceUtils.addImportedDevices(getVm().getStaticData(), getParameters().isImportAsNewEntity());
                VmHandler.LockVm(getVm().getId());
                if (getParameters().isImportAsNewEntity()) {
                    getParameters().setVm(getVm());
                    setVmId(getVm().getId());
                }
                return null;

            }
        });
    }

    @Override
    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVm().getId(),
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), false, false));
        copyAllMemoryImages(getVm().getId());
    }

    private void copyAllMemoryImages(Guid containerId) {
        for (String memoryVolumes : MemoryUtils.getMemoryVolumesFromSnapshots(getVm().getSnapshots())) {
            List<Guid> guids = GuidUtils.getGuidListFromString(memoryVolumes);

            // copy the memory dump image
            VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
                            containerId, guids.get(0), guids.get(2), guids.get(3)),
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if (!vdcRetValue.getSucceeded()) {
                throw new VdcBLLException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());

            // copy the memory configuration (of the VM) image
            vdcRetValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryConfImage(
                            containerId, guids.get(0), guids.get(4), guids.get(5)),
                            ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if (!vdcRetValue.getSucceeded()) {
                throw new VdcBLLException(vdcRetValue.getFault().getError(), "Failed during ExportVmCommand");
            }
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryDumpImage(Guid containerID,
            Guid storageId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                imageId, volumeId, imageId, volumeId, storageId, getMoveOrCopyImageOperation());
        params.setParentCommand(getActionType());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setSourceDomainId(getParameters().getSourceDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setImportEntity(true);
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        params.setParentParameters(getParameters());

        if (getStoragePool().getStorageType().isBlockDomain()) {
            params.setUseCopyCollapse(true);
            params.setVolumeType(VolumeType.Preallocated);
            params.setVolumeFormat(VolumeFormat.RAW);
        }

        return params;
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForMemoryConfImage(Guid containerID,
            Guid storageId, Guid imageId, Guid volumeId) {
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                imageId, volumeId, imageId, volumeId, storageId, getMoveOrCopyImageOperation());
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

    @Override
    protected void moveOrCopyAllImageGroups(Guid containerID, Iterable<DiskImage> disks) {
        int i = 0;
        for (DiskImage disk : disks) {
            VdcReturnValueBase vdcRetValue = Backend.getInstance().runInternalAction(
                    VdcActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForDisk(disk, containerID, i++),
                    ExecutionHandler.createDefaultContexForTasks(getExecutionContext()));
            if (!vdcRetValue.getSucceeded()) {
                throw new VdcBLLException(vdcRetValue.getFault().getError(),
                        "ImportVmCommand::MoveOrCopyAllImageGroups: Failed to copy disk!");
            }

            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private MoveOrCopyImageGroupParameters buildMoveOrCopyImageGroupParametersForDisk(DiskImage disk, Guid containerID, int i) {
        Guid destinationDomain = imageToDestinationDomainMap.get(diskGuidList.get(i));
        MoveOrCopyImageGroupParameters params = new MoveOrCopyImageGroupParameters(containerID,
                diskGuidList.get(i),
                imageGuidList.get(i),
                disk.getId(),
                disk.getImageId(),
                destinationDomain, getMoveOrCopyImageOperation());
        params.setParentCommand(getActionType());
        params.setUseCopyCollapse(getParameters().getCopyCollapse());
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setForceOverride(getParameters().getForceOverride());
        params.setSourceDomainId(getParameters().getSourceDomainId());
        params.setStoragePoolId(getParameters().getStoragePoolId());
        params.setImportEntity(true);
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVm().getId()));
        params.setQuotaId(disk.getQuotaId() != null ? disk.getQuotaId() : getParameters().getQuotaId());
        if (getParameters().getVm().getDiskMap() != null
                && getParameters().getVm().getDiskMap().containsKey(diskGuidList.get(i))) {
            DiskImageBase diskImageBase =
                    (DiskImageBase) getParameters().getVm().getDiskMap().get(diskGuidList.get(i));
            params.setVolumeType(diskImageBase.getVolumeType());
            params.setVolumeFormat(diskImageBase.getVolumeFormat());
        }
        params.setParentParameters(getParameters());
        return params;
    }

    protected void addVmImagesAndSnapshots() {
        Map<Guid, List<DiskImage>> images = ImagesHandler.getImagesLeaf(getVm().getImages());

        if (getParameters().getCopyCollapse()) {
            Guid snapshotId = Guid.newGuid();
            int aliasCounter = 0;
            for (List<DiskImage> diskList : images.values()) {
                DiskImage disk = getActiveVolumeDisk(diskList);
                disk.setParentId(VmTemplateHandler.BlankVmTemplateId);
                disk.setImageTemplateId(VmTemplateHandler.BlankVmTemplateId);
                disk.setVmSnapshotId(snapshotId);
                disk.setActive(true);

                if (getParameters().getVm().getDiskMap() != null
                        && getParameters().getVm().getDiskMap().containsKey(disk.getId())) {
                    DiskImageBase diskImageBase =
                            (DiskImageBase) getParameters().getVm().getDiskMap().get(disk.getId());
                    disk.setvolumeFormat(diskImageBase.getVolumeFormat());
                    disk.setVolumeType(diskImageBase.getVolumeType());
                }
                setDiskStorageDomainInfo(disk);

                diskGuidList.add(disk.getId());
                imageGuidList.add(disk.getImageId());
                if (getParameters().isImportAsNewEntity()) {
                    disk.setId(Guid.newGuid());
                    disk.setImageId(Guid.newGuid());
                    for (int i = 0; i < diskList.size() - 1; i++) {
                         diskList.get(i).setId(disk.getId());
                    }
                }
                disk.setCreationDate(new Date());
                saveImage(disk);
                ImagesHandler.setDiskAlias(disk, getVm(), ++aliasCounter);
                saveBaseDisk(disk);
                saveDiskImageDynamic(disk);
            }

            Snapshot snapshot = addActiveSnapshot(snapshotId);
            getVm().setSnapshots(Arrays.asList(snapshot));
        } else {
            Guid snapshotId = null;
            for (DiskImage disk : getVm().getImages()) {
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
                diskGuidList.add(disk.getId());
                imageGuidList.add(disk.getImageId());
                snapshotId = disk.getVmSnapshotId();
                disk.setActive(true);
                ImagesHandler.setDiskAlias(disk, getVm(), ++aliasCounter);
                updateImage(disk);
                saveBaseDisk(disk);
            }

            // Update active snapshot's data, since it was inserted as a regular snapshot.
            updateActiveSnapshot(snapshotId);
        }
    }

    private static DiskImage getActiveVolumeDisk(List<DiskImage> diskList) {
        return diskList.get(diskList.size() - 1);
    }

    private void setDiskStorageDomainInfo(DiskImage disk) {
        ArrayList<Guid> storageDomain = new ArrayList<Guid>();
        storageDomain.add(imageToDestinationDomainMap.get(disk.getId()));
        disk.setStorageIds(storageDomain);
    }

    /** Saves the base disk object */
    protected void saveBaseDisk(DiskImage disk) {
        getBaseDiskDao().save(disk);
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
     * Generates and saves a {@link DiskImageDynamic} for the given {@link #disk}.
     * @param disk The imported disk
     **/
    protected void saveDiskImageDynamic(DiskImage disk) {
        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(disk.getImageId());
        diskDynamic.setactual_size(disk.getActualSizeInBytes());
        getDiskImageDynamicDAO().save(diskDynamic);
    }

    /**
     * Saves a new active snapshot for the VM
     * @param snapshotId The ID to assign to the snapshot
     * @return The generated snapshot
     */
    protected Snapshot addActiveSnapshot(Guid snapshotId) {
        return snapshotsManager.addActiveSnapshot(snapshotId, getVm(),
                getMemoryVolumeForNewActiveSnapshot(), getCompensationContext());
    }

    private String getMemoryVolumeForNewActiveSnapshot() {
        return getParameters().isImportAsNewEntity() ?
                // We currently don't support using memory that was
                // saved when a snapshot was taken for VM with different id
                StringUtils.EMPTY
                : getMemoryVolumeFromActiveSnapshotInExportDomain();
    }

    private String getMemoryVolumeFromActiveSnapshotInExportDomain() {
        for (Snapshot snapshot : getVm().getSnapshots()) {
            if (snapshot.getType() == SnapshotType.ACTIVE)
                return snapshot.getMemoryVolume();
        }
        log.warnFormat("VM {0} doesn't have active snapshot in export domain", getVmId());
        return StringUtils.EMPTY;
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
            if (getSnapshotDao().exists(getVm().getId(), snapshot.getId())) {
                getSnapshotDao().update(snapshot);
            } else {
                getSnapshotDao().save(snapshot);
            }
        }
    }

    /**
     * Save a snapshot if it does not exist in the database.
     * @param snapshotId The snapshot to save.
     * @param disk The disk containing the snapshot's information.
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
     * @param snapshotId The snapshot to update.
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

    protected void addVmStatic() {

        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setCreationDate(new Date());
        getVm().getStaticData().setVdsGroupId(getParameters().getVdsGroupId());
        getVm().getStaticData().setMinAllocatedMem(computeMinAllocatedMem());
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());
        if (getParameters().getCopyCollapse()) {
            getVm().setVmtGuid(VmTemplateHandler.BlankVmTemplateId);
        }
        getVmStaticDAO().save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    private int computeMinAllocatedMem() {
        int vmMem = getVm().getMemSizeMb();
        int minAllocatedMem = vmMem;
        if (getVm().getMinAllocatedMem() > 0) {
            minAllocatedMem = getVm().getMinAllocatedMem();
        } else {
            // first get cluster memory over commit value
            VDSGroup vdsGroup = getVdsGroupDAO().get(getVm().getVdsGroupId());
            if (vdsGroup != null && vdsGroup.getmax_vds_memory_over_commit() > 0) {
                minAllocatedMem = (vmMem * 100) / vdsGroup.getmax_vds_memory_over_commit();
            }
        }
        return minAllocatedMem;
    }

    private void logImportEvents() {
        // Some values at the OVF file are used for creating events at the GUI
        // for the sake of providing information on the content of the VM that
        // was exported,
        // but not setting it in the imported VM
        VmStatic vmStaticFromOvf = getVm().getStaticData();

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vmStaticFromOvf);
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();

        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            logField(vmStaticFromOvf, fieldName, fieldValue);
        }

        handler.resetDefaults(vmStaticForDefaultValues);

    }

    private static void logField(VmStatic vmStaticFromOvf, String fieldName, String fieldValue) {
        String vmName = vmStaticFromOvf.getName();
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("FieldName", fieldName);
        logable.addCustomValue("VmName", vmName);
        logable.addCustomValue("FieldValue", fieldValue);
        AuditLogDirector.log(logable, AuditLogType.VM_IMPORT_INFO);
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager();
        List<String> invalidNetworkNames = new ArrayList<String>();
        List<String> invalidIfaceNames = new ArrayList<String>();
        Map<String, Network> networksInClusterByName =
                Entities.entitiesByName(getNetworkDAO().getAllForCluster(getVm().getVdsGroupId()));

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            initInterface(iface);
            if (!vmInterfaceManager.isValidVmNetwork(iface, networksInClusterByName)) {
                invalidNetworkNames.add(iface.getNetworkName());
                invalidIfaceNames.add(iface.getName());
                iface.setNetworkName(null);
            }

            vmInterfaceManager.add(iface, getCompensationContext(), getParameters().isImportAsNewEntity(),
                    getVdsGroup().getcompatibility_version());
            macsAdded.add(iface.getMacAddress());
        }

        auditInvalidInterfaces(invalidNetworkNames, invalidIfaceNames);
    }

    private void initInterface(VmNetworkInterface iface) {
        if (iface.getId() == null) {
            iface.setId(Guid.newGuid());
        }
        fillMacAddressIfMissing(iface);
        iface.setVmTemplateId(null);
        iface.setVmId(getVmId());
        iface.setVmName(getVm().getName());
    }

    private void addVmDynamic() {
        VmDynamic tempVar = new VmDynamic();
        tempVar.setId(getVmId());
        tempVar.setStatus(VMStatus.ImageLocked);
        tempVar.setVmHost("");
        tempVar.setVmIp("");
        tempVar.setAppList(getParameters().getVm().getDynamicData().getAppList());
        getVmDynamicDAO().save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        getVmStatisticsDAO().save(stats);
        getCompensationContext().snapshotNewEntity(stats);
        getCompensationContext().stateChanged();
    }

    @Override
    protected void endSuccessfully() {
        endImportCommand();
    }

    @Override
    protected void endActionOnAllImageGroups() {
        for (VdcActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            getBackend().EndAction(getImagesActionType(), p);
        }
    }

    @Override
    protected void endWithFailure() {
        // Going to try and refresh the VM by re-loading it form DB
        setVm(null);

        if (getVm() != null) {
            endActionOnAllImageGroups();
            removeVmNetworkInterfaces();
            removeVmSnapshots(getVm());
            getVmDynamicDAO().remove(getVmId());
            getVmStatisticsDAO().remove(getVmId());
            getVmStaticDAO().remove(getVmId());
            setSucceeded(true);
        } else {
            setVm(getParameters().getVm()); // Setting VM from params, for logging purposes
            // No point in trying to end action again, as the imported VM does not exist in the DB.
            getReturnValue().setEndActionTryAgain(false);
        }
    }

    private void removeVmSnapshots(VM vm) {
        Set<String> memoriesOfRemovedSnapshots =
                snapshotsManager.removeSnapshots(vm.getId());
        new MemoryImageRemover(vm, this).removeMemoryVolumes(memoriesOfRemovedSnapshots);
    }

    protected void removeVmNetworkInterfaces() {
        new VmInterfaceManager().removeAll(getVmId());
    }

    protected void endImportCommand() {
        endActionOnAllImageGroups();
        endVmRelatedOps();
        setSucceeded(true);
    }

    private void endVmRelatedOps() {
        setVm(null);
        if (getVm() != null) {
            VmHandler.UnLockVm(getVm());
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("ImportVmCommand::EndImportCommand: Vm is null - not performing full EndAction");
        }
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

        // special permission is needed to use custom properties
        if (getVm() != null && !StringUtils.isEmpty(getVm().getCustomProperties())) {
            permissionList.add(new PermissionSubject(getVm().getVdsGroupId(),
                    VdcObjectType.VdsGroups,
                    ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES));
        }
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.VdsGroups.name().toLowerCase(), getVdsGroupName());
        }
        return jobProperties;
    }

    @Override
    protected AuditLogType getAuditLogTypeForInvalidInterfaces() {
        return AuditLogType.IMPORTEXPORT_IMPORT_VM_INVALID_INTERFACES;
    }

    @Override
    public VDSGroup getVdsGroup() {
        return super.getVdsGroup();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        for (Disk disk : getParameters().getVm().getDiskMap().values()) {
            //TODO: handle import more than once;
            if(disk instanceof DiskImage){
                DiskImage diskImage = (DiskImage)disk;
                list.add(new QuotaStorageConsumptionParameter(
                        diskImage.getQuotaId(),
                        null,
                        QuotaConsumptionParameter.QuotaAction.CONSUME,
                        imageToDestinationDomainMap.get(diskImage.getId()),
                        (double)diskImage.getSizeInGigabytes()));
            }
        }
        return list;
    }

    ///////////////////////////////////////
    // TaskHandlerCommand Implementation //
    ///////////////////////////////////////

    public ImportVmParameters getParameters() {
        return super.getParameters();
    }

    public VdcActionType getActionType() {
        return super.getActionType();
    }

    public VdcReturnValueBase getReturnValue() {
        return super.getReturnValue();
    }

    public ExecutionContext getExecutionContext() {
        return super.getExecutionContext();
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        super.setExecutionContext(executionContext);
    }

    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTaskInCurrentTransaction(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    public void preventRollback() {
        throw new NotImplementedException();
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }
}
