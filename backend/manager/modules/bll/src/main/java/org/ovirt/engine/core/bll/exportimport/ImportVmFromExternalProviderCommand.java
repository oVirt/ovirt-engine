package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters;
import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters.Phase;
import org.ovirt.engine.core.common.action.RemoveAllVmImagesParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.VdsDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class ImportVmFromExternalProviderCommand<T extends ImportVmFromExternalProviderParameters> extends ImportVmCommandBase<T>
implements SerialChildExecutingCommand, QuotaStorageDependent {

    private static final Pattern VMWARE_DISK_NAME_PATTERN = Pattern.compile("\\[.*?\\] .*/(.*).vmdk");
    private static final Pattern DISK_NAME_PATTERN = Pattern.compile(".*/([^.]+).*");

    private static final String VDSM_COMPAT_VERSION_1_1 = "1.1";

    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private ImportUtils importUtils;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ImportVmFromExternalProviderCommand(Guid cmdId) {
        super(cmdId);
    }

    public ImportVmFromExternalProviderCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmName(getParameters().getExternalName());
        setVdsId(getParameters().getProxyHostId());
        setStorageDomainId(getParameters().getDestDomainId());
        setStoragePoolId(getCluster() != null ? getCluster().getStoragePoolId() : null);
        adjustKVMDataForBlockSD();
    }

    @Override
    protected void initBiosType() {
        if (getVm().getBiosType() == null) {
            getVm().setBiosType(BiosType.I440FX_SEA_BIOS);
        }
        super.initBiosType();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        vmHandler.updateMaxMemorySize(getVm().getStaticData(), getEffectiveCompatibilityVersion());

        if (getStorageDomain() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (!getStorageDomain().getStoragePoolId().equals(getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_AND_CLUSTER_IN_DIFFERENT_POOL);
        }

        if (!validate(new StoragePoolValidator(getStoragePool()).isInStatus(StoragePoolStatus.Up))) {
            return false;
        }

        if (getStorageDomain().getStatus() != StorageDomainStatus.Active) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }

        if (!Guid.isNullOrEmpty(getVdsId()) && !validate(validateRequestedProxyHost())) {
            return false;
        }

        if (Guid.isNullOrEmpty(getVdsId()) && !validate(validateEligibleProxyHostExists())) {
            return false;
        }

        if (!validateSoundDevice()) {
            return false;
        }

        if (!validateNoDuplicateVm()) {
            return false;
        }

        if (!validateUniqueVmName()) {
            return false;
        }

        if (!validateVmArchitecture()) {
            return false;
        }

        if (!validateVdsCluster()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!validateStorageSpace()) {
            return false;
        }

        if (getVm().getOrigin() != OriginType.KVM &&
                getParameters().getVirtioIsoName() != null &&
                getActiveIsoDomainId() == null) {
            return failValidation(EngineMessage.ERROR_CANNOT_FIND_ISO_IMAGE_PATH);
        }

        if (!validate(vmHandler.validateMaxMemorySize(getVm().getStaticData(), getEffectiveCompatibilityVersion()))) {
            return false;
        }

        return true;
    }

    protected boolean validateStorageSpace() {
        List<DiskImage> dummiesDisksList = createDiskDummiesForSpaceValidations(getVm().getImages());
        return validate(getImportValidator().validateSpaceRequirements(dummiesDisksList));
    }

    protected boolean setAndValidateDiskProfiles() {
        Map<DiskImage, Guid> map = new HashMap<>();
        getVm().getImages().forEach(diskImage -> map.put(diskImage, getStorageDomainId()));
        return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
    }

    private ValidationResult validateRequestedProxyHost() {
        if (getVds() == null) {
            return new ValidationResult(EngineMessage.VDS_DOES_NOT_EXIST);
        }

        if (!isHostInSupportedClusterForProxyHost(getVds())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOST_CANNOT_BE_PROXY_FOR_IMPORT_VM,
                    String.format("$vdsName %s", getVdsName()));
        }

        if (!getStoragePoolId().equals(getVds().getStoragePoolId())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_NOT_IN_DEST_STORAGE_POOL);
        }

        if (getVds().getStatus() != VDSStatus.Up) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validateEligibleProxyHostExists() {
        for (VDS host : vdsDao.getAllForStoragePoolAndStatus(getStoragePoolId(), VDSStatus.Up)) {
            if (isHostInSupportedClusterForProxyHost(host)) {
                return ValidationResult.VALID;
            }
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NO_HOST_CAN_BE_PROXY_FOR_IMPORT_VM,
                String.format("$storagePoolName %s", getStoragePoolName()));
    }

    protected boolean isHostInSupportedClusterForProxyHost(VDS host) {
        // virt-v2v is not available on PPC
        return clusterDao.get(host.getClusterId()).getArchitecture() != ArchitectureType.ppc64;
    }

    private void regenerateDiskIds() {
        if (getParameters().isImportAsNewEntity()) {
            Map<Guid, Guid> imageMappings = new HashMap<>();

            for (DiskImage disk : getVm().getImages()) {
                Guid oldImageId = disk.getImageId();
                generateNewDiskId(disk);
                updateManagedDeviceMap(disk, getVm().getStaticData().getManagedDeviceMap());
                // TBD: The OldImageId should be the key for the map, but in the runAnsibleImportOvaPlaybook function
                // the value is set first and the key is set second. The ordering should be corrected accordingly in the future.
                imageMappings.put(disk.getImageId(), oldImageId);
            }

            getParameters().setImageMappings(imageMappings);
        }
    }

    @Override
    protected List<DiskImage> createDiskDummiesForSpaceValidations(List<DiskImage> disksList) {
        List<DiskImage> dummies = new ArrayList<>(disksList.size());
        ArrayList<Guid> emptyStorageIds = new ArrayList<>();
        for (DiskImage image : disksList) {
            image.setStorageIds(emptyStorageIds);
            dummies.add(imagesHandler.createDiskImageWithExcessData(image, getStorageDomainId()));
        }
        return dummies;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        for (DiskImage diskImage : getVm().getImages()) {
            list.add(new QuotaStorageConsumptionParameter(
                    diskImage.getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getStorageDomainId(),
                    (double)diskImage.getSizeInGigabytes()));
        }
        return list;
    }

    @Override
    protected void processImages() {
        regenerateDiskIds();
        ArrayList<Guid> diskIds = getVm().getImages().stream()
                .map(this::adjustDisk)
                .map(this::createDisk)
                .collect(Collectors.toCollection(ArrayList::new));
        getParameters().setDisks(diskIds);
        setSucceeded(true);
    }

    @Override
    protected VmDynamic createVmDynamic() {
        VmDynamic vmDynamic = super.createVmDynamic();
        vmDynamic.setStatus(VMStatus.Down);
        return vmDynamic;
    }

    @Override
    protected void addVmStatic() {
        super.addVmStatic();
        getSnapshotsManager().addActiveSnapshot(
                Guid.newGuid(), getVm(), SnapshotStatus.OK, null, null, getCompensationContext());
    }

    protected DiskImage adjustDisk(DiskImage image) {
        image.setDiskAlias(renameDiskAlias(getVm().getOrigin(), image.getDiskAlias()));
        image.setDiskVmElements(image.getDiskVmElements().stream()
                .map(dve -> DiskVmElement.copyOf(dve, image.getId(), getVmId()))
                .collect(Collectors.toList()));
        return image;
    }

    protected Guid createDisk(DiskImage image) {
        ActionReturnValue actionReturnValue = runInternalActionWithTasksContext(
                ActionType.AddDisk,
                buildAddDiskParameters(image));

        if (!actionReturnValue.getSucceeded()) {
            throw new EngineException(actionReturnValue.getFault().getError(),
                    "Failed to create disk!");
        }

        getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
        return actionReturnValue.getActionReturnValue();
    }

    protected AddDiskParameters buildAddDiskParameters(DiskImage image) {
        AddDiskParameters diskParameters = new AddDiskParameters(image.getDiskVmElementForVm(getVmId()), image);
        diskParameters.setStorageDomainId(getStorageDomainId());
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setStorageDomainId(getParameters().getDestDomainId());
        diskParameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return diskParameters;
    }

    private void adjustKVMDataForBlockSD() {
        // This is a workaround until bz 1332019 will be merged
        // since KVM is currently the only source for which we use Libvirt streaming API.
        // Libvirt currently reports the actual disk size and the virtual one,
        // when using preallocated qcow2 on block domain the size that the Libvirt stream emits
        // can be larger then the actual size.
        // Separately, setting the Volume Type to Preallocated avoids Sparseness when the
        // Destination is a Block SD
        if (getVm().getOrigin() == OriginType.KVM
                && getActionState() == CommandActionState.EXECUTE
                && getStorageDomain() != null
                && getStorageDomain().getStorageType().isBlockDomain()) {
            getVm().getImages().forEach(image -> {
                if (image.getVolumeFormat() == VolumeFormat.RAW) {
                    image.setActualSizeInBytes(image.getSize());
                    image.setVolumeType(VolumeType.Preallocated);
                }
            });

            log.info("Block Storage Domains do not support Sparseness for RAW files. The Importing of VM: '{}'({}) may default to Preallocated.",
                    getVm().getName(), getVm().getId());
        }
    }

    private static String replaceInvalidDiskAliasChars(String alias) {
        return alias.replace(' ', '_');
    }

    protected static String renameDiskAlias(OriginType originType, String alias) {
        Matcher matcher;
        if (originType == OriginType.VMWARE) {
            matcher = VMWARE_DISK_NAME_PATTERN.matcher(alias);
        } else {
            matcher = DISK_NAME_PATTERN.matcher(alias);
        }
        if (matcher.matches()) {
            return replaceInvalidDiskAliasChars(matcher.group(1));
        }

        return replaceInvalidDiskAliasChars(alias);
    }

    @Override
    protected void addVmToDb() {
        super.addVmToDb();
        if (getVm().getOrigin() == OriginType.KVM || getVm().getOrigin() == OriginType.OVIRT) {
            addImportedDevicesExceptDisks();
        }
    }

    private void addImportedDevicesExceptDisks() {
        importUtils.updateGraphicsDevices(getVm().getStaticData(), getStoragePool().getCompatibilityVersion());
        ArrayList<DiskImage> images = getVm().getImages();
        getVm().setImages(new ArrayList<>());
        getVmDeviceUtils().addImportedDevices(getVm().getStaticData(), getParameters().isImportAsNewEntity(), false);
        getVm().setImages(images);
    }

    protected void updateVm() {
        runInternalAction(ActionType.UpdateConvertedVm, buildConvertVmParameters());
    }

    protected void convert() {
        runInternalAction(ActionType.ConvertVm,
                buildConvertVmParameters(),
                createConversionStepContext(StepEnum.CONVERTING_VM));
    }

    private ConvertVmParameters buildConvertVmParameters() {
        ConvertVmParameters parameters = new ConvertVmParameters(getVmId());
        parameters.setUrl(getParameters().getUrl());
        parameters.setUsername(getParameters().getUsername());
        parameters.setPassword(getParameters().getPassword());
        parameters.setVmName(getVmName());
        parameters.setOriginType(getVm().getOrigin());
        parameters.setDisks(getDisks());
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setCompatVersion(getCompatVersion());
        parameters.setStorageDomainId(getStorageDomainId());
        parameters.setProxyHostId(getParameters().getProxyHostId());
        parameters.setClusterId(getClusterId());
        parameters.setVirtioIsoName(getParameters().getVirtioIsoName());
        parameters.setNetworkInterfaces(getParameters().getVm().getInterfaces());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    private String getCompatVersion() {
        int version = Integer.parseInt(getStoragePool().getStoragePoolFormatType().getValue());
        // compat version 1.1 supported from storage version 4
        if (version >= 4 && getVm().getOrigin() != OriginType.KVM) {
            return VDSM_COMPAT_VERSION_1_1;
        }
        return null;
    }

    protected List<DiskImage> getDisks() {
        return getParameters().getDisks().stream()
                .map(this::getDisk)
                .collect(Collectors.toList());
    }

    private DiskImage getDisk(Guid diskId) {
        return runInternalQuery(
                QueryType.GetDiskByDiskId,
                new IdQueryParameters(diskId))
                .getReturnValue();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        // Destination domain
        permissionList.add(new PermissionSubject(getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected Guid getActiveIsoDomainId() {
        return isoDomainListSynchronizer.findActiveISODomain(getStoragePoolId());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getImportPhase()) {
        case CREATE_DISKS:
            getParameters().setImportPhase(Phase.CONVERT);
            if (getParameters().getProxyHostId() == null) {
                getParameters().setProxyHostId(selectProxyHost());
            }
            break;

        case CONVERT:
            if (getVm().getOrigin() == OriginType.OVIRT) {
                return false;
            }

            getParameters().setImportPhase(Phase.POST_CONVERT);
            break;

        case POST_CONVERT:
            return false;

        default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private void executeNextOperation() {
        switch (getParameters().getImportPhase()) {
            case CONVERT:
                convert();
                break;

            case POST_CONVERT:
                if (getVm().getOrigin() == OriginType.KVM) {
                    deleteV2VJob();
                } else {
                    updateVm();
                }
                break;
        }
    }

    private Guid selectProxyHost() {
        Iterator<VDS> activeHostsIterator = vdsDao.getAllForStoragePoolAndStatus(getStoragePoolId(), VDSStatus.Up).iterator();
        return activeHostsIterator.hasNext() ? activeHostsIterator.next().getId() : null;
    }

    @Override
    protected void endWithFailure() {
        removeVmImages();
        setSucceeded(true);
    }

    protected void removeVmImages() {
        runInternalAction(ActionType.RemoveAllVmImages,
                buildRemoveAllVmImagesParameters(),
                cloneContextAndDetachFromParent());
    }

    private RemoveAllVmImagesParameters buildRemoveAllVmImagesParameters() {
        return new RemoveAllVmImagesParameters(
                getVmId(),
                diskDao.getAllForVm(getVmId()).stream().map(DiskImage.class::cast).collect(Collectors.toList()));
    }

    protected CommandContext createConversionStepContext(StepEnum step) {
        CommandContext commandCtx = null;

        try {
            Map<String, String> values = Collections.singletonMap(VdcObjectType.VM.name().toLowerCase(), getVmName());

            Step conversionStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    step,
                    ExecutionMessageDirector.resolveStepMessage(step, values));

            ExecutionContext ctx = new ExecutionContext();
            ctx.setStep(conversionStep);
            ctx.setMonitored(true);

            commandCtx = cloneContext().withoutCompensationContext().withExecutionContext(ctx).withoutLock();

        } catch (RuntimeException e) {
            log.error("Failed to create command context of converting VM '{}': {}", getVmName(), e.getMessage());
            log.debug("Exception", e);
        }

        return commandCtx;
    }

    private void deleteV2VJob() {
        runVdsCommand(VDSCommandType.DeleteV2VJob,
                new VdsAndVmIDVDSParametersBase(getVdsId(), getVmId()));
    }
}
