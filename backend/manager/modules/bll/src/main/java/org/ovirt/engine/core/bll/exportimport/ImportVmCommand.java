package org.ovirt.engine.core.bll.exportimport;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
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
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.domain.LunHelper;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmNicMacsUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
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
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.VmOverheadCalculator;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmCommand<T extends ImportVmParameters> extends ImportVmCommandBase<T>
        implements QuotaStorageDependent {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VmOverheadCalculator vmOverheadCalculator;

    private static final Logger log = LoggerFactory.getLogger(ImportVmCommand.class);

    @Inject
    private VmNicMacsUtils vmNicMacsUtils;
    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;
    @Inject
    private MemoryStorageHandler memoryStorageHandler;
    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private DiskLunMapDao diskLunMapDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private LunHelper lunHelper;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private ImportUtils importUtils;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private List<DiskImage> imageList;

    protected Map<Guid, String> failedDisksToImportForAuditLog = new HashMap<>();

    @Override
    protected void init() {
        super.init();
        setVmId(getParameters().getContainerId());
        setStoragePoolId(getParameters().getStoragePoolId());
        imageToDestinationDomainMap = getParameters().getImageToDestinationDomainMap();
        if (getParameters().getVm() != null && getVm().getDiskMap() != null) {
            imageList = getVm().getDiskMap().values().stream().filter(DisksFilter.ONLY_IMAGES)
                    .map(DiskImage.class::cast).collect(Collectors.toList());
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

        Map<Guid, StorageDomain> domainsMap = new HashMap<>();
        if (!validateBeforeCloneVm(domainsMap)) {
            return false;
        }

        // Since methods #validateBeforeCloneVm > #validateAndSetVmFromExportDomain > #setVmFromExportDomain may
        // change this.vm instance, following code can't be in #init() method and has to follow call of
        // #validateBeforeCloneVm.
        vmHandler.updateMaxMemorySize(getVm().getStaticData(), getEffectiveCompatibilityVersion());

        if (getParameters().isImportAsNewEntity()) {
            initImportClonedVm();

            if (getVm().getInterfaces().size() > getMacPool().getAvailableMacsCount()) {
                return failValidation(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
            }
        }

        if (!validateBallonDevice()) {
            return false;
        }

        if (!validateSoundDevice()) {
            return false;
        }

        if (!validate(vmHandler.validateMaxMemorySize(getVm().getStaticData(), getEffectiveCompatibilityVersion()))) {
            return false;
        }

        return validateAfterCloneVm(domainsMap);
    }

    private List<EngineMessage> validateLunDisk(LunDisk lunDisk) {
        DiskValidator diskValidator = getDiskValidator(lunDisk);
        LUNs lun = lunDisk.getLun();
        StorageType storageType;
        if (lun.getLunConnections() != null && !lun.getLunConnections().isEmpty()) {
            // We set the storage type based on the first connection since connections should be with the same
            // storage type
            storageType = lun.getLunConnections().get(0).getStorageType();
        } else {
            storageType = StorageType.FCP;
        }
        if (storageType == StorageType.ISCSI) {
            ValidationResult connectionsInLunResult = diskValidator.validateConnectionsInLun(storageType);
            if (!connectionsInLunResult.isValid()) {
                return connectionsInLunResult.getMessages();
            }
        }

        ValidationResult lunAlreadyInUseResult = diskValidator.validateLunAlreadyInUse();
        if (!lunAlreadyInUseResult.isValid()) {
            return lunAlreadyInUseResult.getMessages();
        }

        DiskVmElementValidator diskVmElementValidator =
                new DiskVmElementValidator(lunDisk, lunDisk.getDiskVmElementForVm(getVmId()));
        ValidationResult virtIoScsiResult = isVirtIoScsiValid(getVm(), diskVmElementValidator);
        if (!virtIoScsiResult.isValid()) {
            return virtIoScsiResult.getMessages();
        }

        ValidationResult diskInterfaceResult = diskVmElementValidator.isDiskInterfaceSupported(getVm());
        if (!diskInterfaceResult.isValid()) {
            return diskInterfaceResult.getMessages();
        }

        Guid vdsId = vdsCommandsHelper.getHostForExecution(getStoragePoolId());
        if (!validateLunExistsAndInitDeviceData(lun, storageType, vdsId)) {
            return Arrays.asList(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
        }

        ValidationResult usingScsiReservationResult = diskValidator.isUsingScsiReservationValid(getVm(),
                lunDisk.getDiskVmElementForVm(getVmId()),
                lunDisk);
        if (!usingScsiReservationResult.isValid()) {
            return usingScsiReservationResult.getMessages();
        }
        return Collections.emptyList();
    }

    private boolean validateLunExistsAndInitDeviceData(LUNs lun, StorageType storageType, Guid vdsId) {
        List<LUNs> lunFromStorage = null;
        try {
            StorageServerConnectionManagementVDSParameters connectParams =
                    new StorageServerConnectionManagementVDSParameters(vdsId,
                            Guid.Empty,
                            storageType,
                            lun.getLunConnections());
            runVdsCommand(VDSCommandType.ConnectStorageServer, connectParams);
            GetDeviceListVDSCommandParameters parameters =
                    new GetDeviceListVDSCommandParameters(vdsId,
                            storageType,
                            false,
                            Collections.singleton(lun.getLUNId()));
            lunFromStorage = (List<LUNs>) runVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();
        } catch (EngineException e) {
            log.debug("Exception while validating LUN disk: '{}'", e);
            return false;
        }
        if (lunFromStorage == null || lunFromStorage.isEmpty()) {
            return false;
        } else {
            LUNs luns = lunFromStorage.get(0);
            lun.setSerial(luns.getSerial());
            lun.setLunMapping(luns.getLunMapping());
            lun.setVendorId(luns.getVendorId());
            lun.setProductId(luns.getProductId());
            lun.setProductId(luns.getProductId());
            lun.setDiscardMaxSize(luns.getDiscardMaxSize());
            lun.setPvSize(luns.getPvSize());
        }
        return true;
    }

    private ValidationResult isVirtIoScsiValid(VM vm, DiskVmElementValidator diskVmElementValidator) {
        ValidationResult result = diskVmElementValidator.verifyVirtIoScsi(vm);
        if (!result.isValid()) {
            return result;
        }

        if (vm != null) {
            if (!VmDeviceCommonUtils.isVirtIoScsiDeviceExists(getVm().getManagedVmDeviceMap().values())) {
                return new ValidationResult(EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
            } else {
                return diskVmElementValidator.isDiskInterfaceSupported(vm);
            }
        }
        return ValidationResult.VALID;
    }

    protected DiskValidator getDiskValidator(Disk disk) {
        return new DiskValidator(disk);
    }

    private void initImportClonedVm() {
        Guid guid = getParameters().getVm().getId();
        getVm().setId(guid);
        setVmId(guid);
        getVm().setName(getParameters().getVm().getName());
        getVm().setStoragePoolId(getParameters().getStoragePoolId());
        getParameters().setVm(getVm());
        getVm().getInterfaces().forEach(iface -> iface.setId(Guid.newGuid()));
    }

    protected boolean validateBeforeCloneVm(Map<Guid, StorageDomain> domainsMap) {
        if (getVm() != null) {
            setDescription(getVmName());
        }

        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        if (!validate(spValidator.exists())) {
            return false;
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

        List<VmNetworkInterface> vmNetworkInterfaces = getVm().getInterfaces();
        vmNicMacsUtils.replaceInvalidEmptyStringMacAddressesWithNull(vmNetworkInterfaces);

        return true;
    }

    @Override
    protected void executeVmCommand() {
        if (getVm().isAutoStartup() && shouldAddLease(getVm().getStaticData())) {
            if (FeatureSupported.isVmLeasesSupported(getEffectiveCompatibilityVersion())) {
                if (validateLeaseStorageDomain(getVm().getLeaseStorageDomainId())) {
                    if (!addVmLease(getVm().getLeaseStorageDomainId(), getVm().getId(), false)) {
                        getVm().setLeaseStorageDomainId(null);
                    }
                } else {
                    getVm().setLeaseStorageDomainId(null);
                    auditLogDirector.log(this, AuditLogType.CANNOT_IMPORT_VM_WITH_LEASE_STORAGE_DOMAIN);
                }
            }
            else {
                getVm().setLeaseStorageDomainId(null);
                auditLogDirector.log(this, AuditLogType.CANNOT_IMPORT_VM_WITH_LEASE_COMPAT_VERSION);
            }
        }
        else {
            getVm().setLeaseStorageDomainId(null);
        }
        super.executeVmCommand();
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
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
        images.entrySet().stream().forEach(e -> getVm().getDiskMap().put(e.getKey(), getActiveVolumeDisk(e.getValue())));

        return true;
    }

    private void setVmFromExportDomain(VM vm) {
        // preserve the given name
        if (getVmName() != null) {
            vm.setName(getVmName());
        }
        setVm(vm);
        initGraphicsData();
    }

    private void initGraphicsData() {
        importUtils.updateGraphicsDevices(getVm().getStaticData(), getEffectiveCompatibilityVersion());
    }

    protected DiskImage getActiveVolumeDisk(List<DiskImage> diskList) {
        return diskList.get(diskList.size() - 1);
    }

    protected VM getVmFromExportDomain(Guid vmId) {
        return getVmsFromExportDomain().stream().filter(v -> vmId.equals(v.getId())).findFirst().orElse(null);
    }

    /**
     * Load images from Import/Export domain.
     *
     * @return A {@link List} of {@link VM}s from the export domain.
     */
    @SuppressWarnings("unchecked")
    protected List<VM> getVmsFromExportDomain() {
        QueryReturnValue qRetVal = runInternalQuery(
                QueryType.GetVmsFromExportDomain,
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

        if (!validateLunDisksForVm(vmFromParams)) {
            return false;
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
                            storageDomainStaticDao.get(getParameters().getSourceDomainId()).getStorageName()));
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

        if (!validateGraphicsAndDisplay()) {
            return false;
        }

        if (!getParameters().isImportAsNewEntity()) {
            List<VmNetworkInterface> vmNetworkInterfaces = getVm().getInterfaces();
            if (!validate(vmNicMacsUtils.validateThereIsEnoughOfFreeMacs(vmNetworkInterfaces,
                    getMacPool(),
                    getVnicRequiresNewMacPredicate()))) {
                return false;
            }

            if (!validate(vmNicMacsUtils.validateMacAddress(vmNetworkInterfaces))) {
                return false;
            }
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        return true;
    }

    private boolean validateLunDisksForVm(VM vmFromParams) {
        if (vmFromParams.getDiskMap() != null) {
            List<LunDisk> lunDisks = DisksFilter.filterLunDisks(vmFromParams.getDiskMap().values());
            for (LunDisk lunDisk : lunDisks) {
                List<EngineMessage> lunValidationMessages = validateLunDisk(lunDisk);
                if (lunValidationMessages.isEmpty()) {
                    getVm().getDiskMap().put(lunDisk.getId(), lunDisk);
                } else if (!getParameters().isAllowPartialImport()) {
                    return failValidation(lunValidationMessages);
                } else {
                    log.warn("Skipping validation for external LUN disk '{}' since partialImport flag is true." +
                            " Invalid external LUN disk might reflect on the run VM process",
                            lunDisk.getId());
                    vmFromParams.getDiskMap().remove(lunDisk.getId());
                    failedDisksToImportForAuditLog.putIfAbsent(lunDisk.getId(), lunDisk.getDiskAlias());
                }
            }
        }
        return true;
    }

    private Predicate<VmNetworkInterface> getVnicRequiresNewMacPredicate() {
        return ((Predicate<VmNetworkInterface>) this::nicWithoutMacAddress)
                .or(iface -> getParameters().isReassignBadMacs() && vNicHasBadMac(iface));
    }

    private boolean nicWithoutMacAddress(VmNic vmNic) {
        return vmNic.getMacAddress() == null;
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
                MemoryUtils.createDiskDummies(vmOverheadCalculator.getSnapshotMemorySizeInBytes(getVm()),
                        MemoryUtils.METADATA_SIZE_IN_BYTES);
        StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(
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

        QueryReturnValue qRetVal = runInternalQuery(
                QueryType.GetTemplatesFromExportDomain,
                new GetAllFromExportDomainQueryParameters(getParameters().getStoragePoolId(),
                        getParameters().getSourceDomainId()));

        if (!qRetVal.getSucceeded()) {
            return false;
        }

        Map<VmTemplate, ?> templates = qRetVal.getReturnValue();
        return templates.keySet().stream().anyMatch(t -> getParameters().getVm().getVmtGuid().equals(t.getId()));
    }

    protected boolean checkTemplateInStorageDomain() {
        boolean retValue = validate(getImportValidator().verifyDisks(imageList, imageToDestinationDomainMap));
        if (retValue && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getVmtGuid())
                && !getParameters().getCopyCollapse()) {
            List<StorageDomain> domains = runInternalQuery(QueryType.GetStorageDomainsByVmTemplateId,
                    new IdQueryParameters(getVm().getVmtGuid())).getReturnValue();
            Set<Guid> domainsId = domains.stream().map(StorageDomain::getId).collect(Collectors.toSet());

            if (!domainsId.isEmpty() && Collections.disjoint(domainsId, imageToDestinationDomainMap.values())) {
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
        for (DiskImage image : new ArrayList<>(getImages())) {
            Guid imageGUID = image.getImageId();
            Guid storagePoolId = image.getStoragePoolId() != null ? image.getStoragePoolId()
                    : Guid.Empty;
            Guid storageDomainId = getSourceDomainId(image);
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;

            VDSReturnValue retValue = runVdsCommand(
                    VDSCommandType.GetImageInfo,
                    new GetImageInfoVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                            imageGUID));

            if (!retValue.getSucceeded()) {
                if (!getParameters().isAllowPartialImport()) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
                }
                log.warn("Disk image '{}/{}' doesn't exist on storage domain '{}'. Ignoring since force flag in on",
                        imageGroupId,
                        imageGUID,
                        storageDomainId);
                getVm().getImages().remove(image);
                failedDisksToImportForAuditLog.putIfAbsent(image.getId(), image.getDiskAlias());
            }
        }
        return true;
    }

    @Override
    protected void processImages() {
        processImages(!isImagesAlreadyOnTarget());
        // if there are no tasks, we can just unlock the VM
        if (getReturnValue().getVdsmTaskIdList().isEmpty()) {
            vmHandler.unLockVm(getVm());
        }
    }

    private void processImages(final boolean useCopyImages) {
        TransactionSupport.executeInNewTransaction(() -> {
            addVmImagesAndSnapshots();
            addVmExternalLuns();
            addMemoryImages();
            updateSnapshotsFromExport();
            if (useCopyImages) {
                moveOrCopyAllImageGroups();
            }
            getVmDeviceUtils().addImportedDevices(
                    getVm().getStaticData(), getParameters().isImportAsNewEntity(), false);
            if (getParameters().isImportAsNewEntity()) {
                getParameters().setVm(getVm());
                setVmId(getVm().getId());
            }
            vmStaticDao.incrementDbGeneration(getVmId());
            return null;

        });
    }

    protected void moveOrCopyAllImageGroups() {
        moveOrCopyAllImageGroups(getVm().getId(),
                DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_ACTIVE));
        copyAllMemoryImages(getVm().getId());
    }

    private void copyAllMemoryImages(Guid containerId) {
        for (String memoryVolumes : MemoryUtils.getMemoryVolumesFromSnapshots(getVm().getSnapshots())) {
            List<Guid> guids = Guid.createGuidListFromString(memoryVolumes);

            // copy the memory dump image
            ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryDumpImage(
                            containerId, guids.get(0), guids.get(2), guids.get(3)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy memory image");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());

            // copy the memory configuration (of the VM) image
            vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForMemoryConfImage(
                            containerId, guids.get(0), guids.get(4), guids.get(5)));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy metadata image");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());
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

        StorageDomainStatic storageDomain = storageDomainStaticDao.get(storageId);
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
            ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                    ActionType.CopyImageGroup,
                    buildMoveOrCopyImageGroupParametersForDisk(disk, containerID));
            if (!vdcRetValue.getSucceeded()) {
                throw new EngineException(vdcRetValue.getFault().getError(), "Failed to copy disk!");
            }
            // TODO: Currently REST-API doesn't support coco for async commands, remove when bug 1199011 fixed
            getTaskIdList().addAll(vdcRetValue.getVdsmTaskIdList());
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

    protected void setQcowCompat(DiskImage diskImage) {
        diskImage.setQcowCompat(QcowCompat.QCOW2_V2);
        if (FeatureSupported.qcowCompatSupported(getStoragePool().getCompatibilityVersion())) {
            QemuImageInfo qemuImageInfo =
                    imagesHandler.getQemuImageInfoFromVdsm(diskImage.getStoragePoolId(),
                            diskImage.getStorageIds().get(0),
                            diskImage.getId(),
                            diskImage.getImageId(),
                            null,
                            true);
            if (qemuImageInfo != null) {
                diskImage.setQcowCompat(qemuImageInfo.getQcowCompat());
            }
        }
        imageDao.update(diskImage.getImage());
    }

    protected void addVmExternalLuns() {
        if (getParameters().getVm().getDiskMap() != null) {
            List<LunDisk> lunDisks = DisksFilter.filterLunDisks(getParameters().getVm().getDiskMap().values());
            for (LunDisk lun : lunDisks) {
                StorageType storageType = StorageType.UNKNOWN;
                if (lun.getLun().getLunConnections() != null && !lun.getLun().getLunConnections().isEmpty()) {
                    // We set the storage type based on the first connection since connections should be with the same
                    // storage type
                    storageType = lun.getLun().getLunConnections().get(0).getStorageType();
                }
                lunHelper.proceedDirectLUNInDb(lun.getLun(), storageType);

                // Only if the LUN disk does not exists in the setup add it.
                if (baseDiskDao.get(lun.getId()) == null) {
                    baseDiskDao.save(lun);
                }
                if (diskLunMapDao.get(new DiskLunMapId(lun.getId(), lun.getLun().getLUNId())) == null) {
                    diskLunMapDao.save(new DiskLunMap(lun.getId(), lun.getLun().getLUNId()));
                }

                // Add disk VM element to attach the disk to the VM.
                DiskVmElement diskVmElement = lun.getDiskVmElementForVm(getVmId());
                diskVmElementDao.save(diskVmElement);
                getVmDeviceUtils().addDiskDevice(
                        getVmId(),
                        lun.getId(),
                        diskVmElement.isPlugged(),
                        diskVmElement.isReadOnly());
            }
        }
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
        baseDiskDao.save(disk);
    }

    protected void saveDiskVmElement(Guid diskId, Guid vmId, DiskVmElement diskVmElement) {
        DiskVmElement dve = DiskVmElement.copyOf(diskVmElement, diskId, vmId);
        updatePassDiscardForDiskVmElement(dve);
        diskVmElementDao.save(dve);
    }

    /** Save the entire image, including it's storage mapping */
    protected void saveImage(DiskImage disk) {
        imagesHandler.saveImage(disk);
    }

    /** Updates an image of a disk */
    protected void updateImage(DiskImage disk) {
        imageDao.update(disk.getImage());
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
        diskImageDynamicDao.save(diskDynamic);
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
        return getSnapshotsManager().addActiveSnapshot(
                snapshotId,
                getVm(),
                memoryVolume,
                getCompensationContext());
    }

    @Override
    protected Snapshot getActiveSnapshot() {
        Optional<Snapshot> activeSnapshot =
                getVm().getSnapshots().stream().filter(s -> s.getType() == SnapshotType.ACTIVE).findFirst();

        if (!activeSnapshot.isPresent()) {
            log.warn("VM '{}' doesn't have active snapshot in export domain", getVmId());
        }
        return activeSnapshot.orElse(null);
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

            if (snapshotDao.exists(getVm().getId(), snapshot.getId())) {
                snapshotDao.update(snapshot);
            } else {
                snapshotDao.save(snapshot);
            }
        }
    }

    private void updateMemoryDisks(Snapshot snapshot) {
        List<Guid> guids = Guid.createGuidListFromString(snapshot.getMemoryVolume());
        snapshot.setMemoryDiskId(guids.get(2));
        snapshot.setMetadataDiskId(guids.get(4));
    }

    private void addMemoryImages() {
        getVm().getSnapshots().stream()
        .filter(snapshot -> !StringUtils.isEmpty(snapshot.getMemoryVolume()))
        .forEach(snapshot -> {
            addDisk(createMemoryDisk(snapshot));
            addDisk(createMetadataDisk(getVm(), snapshot));
        });
    }

    private DiskImage createMemoryDisk(Snapshot snapshot) {
        List<Guid> guids = Guid.createGuidListFromString(snapshot.getMemoryVolume());
        StorageDomainStatic sd = validateStorageDomainExistsInDb(snapshot, guids.get(0), guids.get(2), guids.get(3));
        DiskImage disk = isMemoryDiskAlreadyExistsInDb(snapshot, guids.get(2), guids.get(3));
        if (sd == null || disk != null) {
            return null;
        }
        VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                snapshot.getVmConfiguration(),
                snapshot.getVmId(), snapshot.getId());
        DiskImage memoryDisk = MemoryUtils.createSnapshotMemoryDisk(
                vm,
                sd.getStorageType(),
                vmOverheadCalculator, MemoryUtils.generateMemoryDiskDescription(vm, snapshot.getDescription()));
        memoryDisk.setId(guids.get(2));
        memoryDisk.setImageId(guids.get(3));
        memoryDisk.setStorageIds(new ArrayList<>(Collections.singletonList(guids.get(0))));
        memoryDisk.setStoragePoolId(guids.get(1));
        memoryDisk.setCreationDate(snapshot.getCreationDate());
        memoryDisk.setActive(true);
        memoryDisk.setWipeAfterDelete(vm.getDiskList().stream().anyMatch(DiskImage::isWipeAfterDelete));
        return memoryDisk;
    }

    private DiskImage createMetadataDisk(VM vm, Snapshot snapshot) {
        List<Guid> guids = Guid.createGuidListFromString(snapshot.getMemoryVolume());
        StorageDomainStatic sd = validateStorageDomainExistsInDb(snapshot, guids.get(0), guids.get(4), guids.get(5));
        DiskImage disk = isMemoryDiskAlreadyExistsInDb(snapshot, guids.get(4), guids.get(5));
        if (sd == null || disk != null) {
            return null;
        }
        DiskImage memoryDisk = MemoryUtils.createSnapshotMetadataDisk(MemoryUtils.generateMemoryDiskDescription(vm, snapshot.getDescription()));
        memoryDisk.setId(guids.get(4));
        memoryDisk.setImageId(guids.get(5));
        memoryDisk.setStorageIds(new ArrayList<>(Collections.singletonList(guids.get(0))));
        memoryDisk.setStoragePoolId(guids.get(1));
        memoryDisk.setCreationDate(snapshot.getCreationDate());
        memoryDisk.setActive(true);
        memoryDisk.setWipeAfterDelete(vm.getDiskList().stream().anyMatch(DiskImage::isWipeAfterDelete));
        return memoryDisk;
    }

    private DiskImage isMemoryDiskAlreadyExistsInDb(Snapshot snapshot, Guid diskId, Guid imageId) {
        DiskImage disk = diskImageDao.get(imageId);
        if (disk != null) {
            log.info("Memory disk '{}'/'{}' of snapshot '{}'(id: '{}') already exists on storage domain '{}'",
                    diskId,
                    imageId,
                    snapshot.getDescription(),
                    snapshot.getId(),
                    disk.getStoragesNames().get(0));
        }
        return disk;
    }

    private StorageDomainStatic validateStorageDomainExistsInDb(Snapshot snapshot, Guid storageDomainId, Guid diskId, Guid imageId) {
        StorageDomainStatic sd = storageDomainStaticDao.get(storageDomainId);
        if (sd == null) {
            log.error("Memory disk '{}'/'{}' of snapshot '{}'(id: '{}') could not be added since storage domain id '{}' does not exists",
                    diskId,
                    imageId,
                    snapshot.getDescription(),
                    snapshot.getId(),
                    storageDomainId);
        }
        return sd;
    }

    private void addDisk(DiskImage disk) {
        if (disk != null) {
            saveImage(disk);
            saveBaseDisk(disk);
            saveDiskImageDynamic(disk);
        } else {
            log.error("Memory metadata/dump disk could not be added");
        }
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
        if (!snapshotDao.exists(getVm().getId(), snapshotId)) {
            snapshotDao.save(
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
        snapshotDao.update(
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
        vmHandler.unLockVm(getVm());
        setSucceeded(true);
    }

    private void checkTrustedService() {
        if (getVm().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_TRUSTED_TO_UNTRUSTED);
        }
        else if (!getVm().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    protected void endActionOnAllImageGroups() {
        for (ActionParametersBase p : getParameters().getImagesParameters()) {
            if (p instanceof MoveOrCopyImageGroupParameters) {
                p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
                getBackend().endAction(ActionType.CopyImageGroup,
                        p,
                        getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
            }
        }
    }

    protected void initQcowVersionForDisks(Guid imageGroupId) {
        List<DiskImage> diskVolumes = diskImageDao.getAllSnapshotsForImageGroup(imageGroupId);
        diskVolumes.stream().filter(volume -> volume.getVolumeFormat() == VolumeFormat.COW).forEach(volume -> {
            try {
                setQcowCompat(volume);
            } catch (Exception e) {
                log.error("Could not set qcow compat version for disk '{} with id '{}/{}'",
                        volume.getDiskAlias(),
                        volume.getId(),
                        volume.getImageId());
            }
        });
    }

    @Override
    protected void endWithFailure() {
        // Going to try and refresh the VM by re-loading it form DB
        setVm(null);

        if (getVm() != null) {
            removeVmSnapshots();
            endActionOnAllImageGroups();
            removeVmNetworkInterfaces();
            vmDynamicDao.remove(getVmId());
            vmStatisticsDao.remove(getVmId());
            vmStaticDao.remove(getVmId());
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
        Set<String> memoryStates = getSnapshotsManager().removeSnapshots(vmId);
        for (String memoryState : memoryStates) {
            removeMemoryVolumes(memoryState, vmId);
        }
    }

    private void removeMemoryVolumes(String memoryVolume, Guid vmId) {
        ActionReturnValue retVal = runInternalAction(
                ActionType.RemoveMemoryVolumes,
                new RemoveMemoryVolumesParameters(memoryVolume, vmId), cloneContextAndDetachFromParent());

        if (!retVal.getSucceeded()) {
            log.error("Failed to remove memory volumes '{}'", memoryVolume);
        }
    }

    protected void removeVmNetworkInterfaces() {
        new VmInterfaceManager(getMacPool()).removeAllAndReleaseMacAddresses(getVmId());
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
            Map<DiskImage, Guid> map = getDisksForDiskProfileValidation().stream().filter(DisksFilter.ONLY_IMAGES)
                    .collect(Collectors.toMap(DiskImage.class::cast, d -> imageToDestinationDomainMap.get(d.getId())));
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    /**
     * If the allow partial import flag is true we should filter out the invalid disks which did not pass the
     * validation from the VM's disk map, since those disks will not be part of the VM once it will be imported.
     *
     * @return All the valid disks to use for disk profile.
     */
    private Collection<Disk> getDisksForDiskProfileValidation() {
        Collection<Disk> disks = getParameters().getVm().getDiskMap().values();
        if (getParameters().isAllowPartialImport()) {
            disks = disks.stream().filter(disk -> getImages().stream()
                    .anyMatch(diskFromImagesList -> diskFromImagesList.getId().equals(disk.getId())))
                    .collect(Collectors.toList());
        }
        return disks;
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

    private void updatePassDiscardForDiskVmElement(DiskVmElement diskVmElement) {
        if (diskVmElement.isPassDiscard() &&
                !FeatureSupported.passDiscardSupported(getStoragePool().getCompatibilityVersion())) {
            diskVmElement.setPassDiscard(false);
        }
    }
}
