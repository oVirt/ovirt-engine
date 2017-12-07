package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandActionState;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.action.ImportVmFromExternalProviderParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromExternalProviderCommand<T extends ImportVmFromExternalProviderParameters> extends ImportVmCommandBase<T>
implements QuotaStorageDependent {

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
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private ImportUtils importUtils;

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
        setSingleQxlPci();
        checkImageTarget();
        vmHandler.updateMaxMemorySize(getVm().getStaticData(), getEffectiveCompatibilityVersion());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

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

        if (!validateBallonDevice()) {
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

        if (getParameters().getVirtioIsoName() != null && getActiveIsoDomainId() == null) {
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
        for (DiskImage diskImage : getVm().getImages()) {
            map.put(diskImage, getStorageDomainId());
        }
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

    private boolean isHostInSupportedClusterForProxyHost(VDS host) {
        return clusterDao.get(host.getClusterId()).getArchitecture() != ArchitectureType.ppc64;
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
                    null,
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    getStorageDomainId(),
                    (double)diskImage.getSizeInGigabytes()));
        }
        return list;
    }

    @Override
    protected void processImages() {
        ArrayList<Guid> diskIds = new ArrayList<>();
        boolean isFirstDisk = true;
        for (DiskImage image : getVm().getImages()) {
            Guid diskId = createDisk(image, getVm().getOrigin() == OriginType.KVM && isFirstDisk);
            diskIds.add(diskId);
            isFirstDisk = false;
        }
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
                Guid.newGuid(), getVm(), "", getCompensationContext());
    }

    @Override
    protected void addVmInterfaces() {
        super.addVmInterfaces();
        addNetworkInterfaceDevices();
    }

    protected void addNetworkInterfaceDevices() {
        getVm().getInterfaces().forEach(iface
                -> getVmDeviceUtils().addInterface(getVmId(), iface.getId(), iface.isPlugged(), false));
    }

    protected Guid createDisk(DiskImage image, boolean isBoot) {

        ActionReturnValue actionReturnValue =
                runInternalActionWithTasksContext(ActionType.AddDisk, buildAddDiskParameters(image, isBoot));

        if (!actionReturnValue.getSucceeded()) {
            throw new EngineException(actionReturnValue.getFault().getError(),
                    "Failed to create disk!");
        }

        getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
        return actionReturnValue.getActionReturnValue();
    }

    protected AddDiskParameters buildAddDiskParameters(DiskImage image, boolean isBoot) {
        image.setDiskAlias(renameDiskAlias(getVm().getOrigin(), image.getDiskAlias()));

        AddDiskParameters diskParameters = new AddDiskParameters(new DiskVmElement(null, getVmId()), image);
        diskParameters.setStorageDomainId(getStorageDomainId());
        diskParameters.setParentCommand(getActionType());
        diskParameters.setParentParameters(getParameters());
        diskParameters.setShouldRemainIllegalOnFailedExecution(true);
        diskParameters.setStorageDomainId(getParameters().getDestDomainId());

        DiskVmElement dve = new DiskVmElement(image.getId(), getVmId());
        dve.setDiskInterface(DiskInterface.VirtIO);
        dve.setBoot(isBoot);
        diskParameters.setDiskVmElement(dve);

        return diskParameters;
    }

    private void checkImageTarget() {
        // TODO:
        // This is a workaround until bz 1332019 will be merged
        // since KVM is currently the only source for which we use Libvirt streaming API.
        // Libvirt currently reports the actual disk size and the virtual one,
        // when using preallocated qcow2 on block domain the size that the Libvirt stream emits
        // can be larger then the actual size.
        if (getVm().getOrigin() == OriginType.KVM && getActionState() == CommandActionState.EXECUTE) {
            StorageDomain domain = storageDomainDao.get(getStorageDomainId());
            if (domain.getStorageType().isBlockDomain()) {
                getVm().getImages().forEach(image -> image.setActualSizeInBytes(image.getSize()));
            }
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
    protected void endSuccessfully() {
        endActionOnDisks();

        // Lock will be acquired by the convert command.
        // Note that the VM is not locked for a short period of time. This should be fixed
        // when locks that are passed by caller could be released by command's callback.
        freeLock();
        convert();

        setSucceeded(true);
    }

    @Override
    protected void addVmToDb() {
        super.addVmToDb();
        if (getVm().getOrigin() == OriginType.KVM) {
            importUtils.updateGraphicsDevices(getVm().getStaticData(), getStoragePool().getCompatibilityVersion());
            if (getParameters().isImportAsNewEntity()) {
                for (VmDevice device : getVm().getStaticData().getManagedDeviceMap().values()) {
                    device.getId().setVmId(getVmId());
                }
            }
            vmDeviceDao.saveAll(getVm().getStaticData().getManagedDeviceMap().values());
        }
    }

    protected void convert() {
        commandCoordinatorUtil.executeAsyncCommand(
                ActionType.ConvertVm,
                buildConvertVmParameters(),
                cloneContextAndDetachFromParent());
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

    @Override
    protected void endWithFailure() {
        // Since AddDisk is called internally, its audit log on end-action will not be logged
        auditLog(this, AuditLogType.ADD_DISK_INTERNAL_FAILURE);
        // This command uses compensation so if we won't execute the following block in a new
        // transaction then the images might be updated within this transaction scope and block
        // RemoveVm that also tries to update the images later on
        TransactionSupport.executeInNewTransaction(() -> {
            endActionOnDisks();
            return null;
        });
        removeVm();
        setSucceeded(true);
    }

    protected void removeVm() {
        runInternalActionWithTasksContext(
                ActionType.RemoveVm,
                new RemoveVmParameters(getVmId(), true),
                getLock());
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
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_SUCCESS:
        default:
            return super.getAuditLogTypeValue();
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Set<PermissionSubject> permissionSet = new HashSet<>();
        // Destination domain
        permissionSet.add(new PermissionSubject(getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return new ArrayList<>(permissionSet);
    }

    protected Guid getActiveIsoDomainId() {
        return isoDomainListSynchronizer.findActiveISODomain(getStoragePoolId());
    }

    private void setSingleQxlPci() {
        if (!osRepository.isSingleQxlDeviceEnabled(getVm().getVmOsId())
                || getVm().getDefaultDisplayType() != DisplayType.qxl) {
            getVm().getStaticData().setSingleQxlPci(false);
        }
    }

}
