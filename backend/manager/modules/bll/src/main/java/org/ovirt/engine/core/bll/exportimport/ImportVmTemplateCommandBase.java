package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.bll.utils.CompatibilityVersionUpdater;
import org.ovirt.engine.core.bll.validator.VmNicMacsUtils;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

public abstract class ImportVmTemplateCommandBase<T extends ImportVmTemplateParameters> extends MoveOrCopyTemplateCommand<T>
        implements QuotaStorageDependent {

    @Inject
    private VmNicMacsUtils vmNicMacsUtils;
    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;
    @Inject
    private VmNetworkStatisticsDao vmNetworkStatisticsDao;
    @Inject
    protected ImageDao imageDao;
    @Inject
    private ImportUtils importUtils;
    @Inject
    private CloudInitHandler cloudInitHandler;

    protected final Map<Guid, Guid> originalDiskIdMap = new HashMap<>();
    protected final Map<Guid, Guid> originalDiskImageIdMap = new HashMap<>();

    private Guid sourceTemplateId;

    private Runnable logOnExecuteEndMethod = () -> {};

    public ImportVmTemplateCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmTemplate(parameters.getVmTemplate());
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId()));
        setStoragePoolId(parameters.getStoragePoolId());
        setClusterId(parameters.getClusterId());
        setStorageDomainId(parameters.getStorageDomainId());
    }

    public ImportVmTemplateCommandBase(Guid commandId) {
        super(commandId);
    }

    public Version getEffectiveCompatibilityVersion() {
        return CompatibilityVersionUtils.getEffective(getVmTemplate(), this::getCluster);
    }

    protected void updateTemplateVersion() {
        Version newVersion = CompatibilityVersionUtils.getEffective(getVmTemplate(), this::getCluster);

        // A Template can have custom compatibility version that is lower than
        // the DC version. In that case, the custom version has to be updated.
        if (newVersion.less(getCluster().getCompatibilityVersion())) {
            var dataCenterVersion = getStoragePool().getCompatibilityVersion();
            if (newVersion.less(dataCenterVersion)) {
                Version originalVersion = newVersion;
                logOnExecuteEnd(() -> {
                    addCustomValue("OriginalVersion", originalVersion.toString());
                    addCustomValue("NewVersion", dataCenterVersion.toString());
                    auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_CUSTOM_VERSION_CHANGE);
                });

                newVersion = dataCenterVersion;
            }
        }

        var updates = new CompatibilityVersionUpdater()
                .updateTemplateCompatibilityVersion(getVmTemplate(), newVersion, getCluster());

        if (!updates.isEmpty()) {
            logOnExecuteEnd(() -> {
                String updatesString = updates.stream()
                        .map(update -> update.getDisplayName())
                        .collect(Collectors.joining(", "));

                addCustomValue("Updates", updatesString);
                auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_UPDATED);
            });
        }
    }

    private void logOnExecuteEnd(Runnable method) {
        var oldLogMethod = logOnExecuteEndMethod;
        logOnExecuteEndMethod = () -> {
            oldLogMethod.run();
            method.run();
        };
    }

    @Override
    protected boolean validate() {
        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        // Update VM version, graphic devices and max memory before the rest of the validation
        updateTemplateVersion();
        importUtils.updateGraphicsDevices(getVmTemplate(), getEffectiveCompatibilityVersion());
        vmHandler.updateMaxMemorySize(getVmTemplate(), getEffectiveCompatibilityVersion());

        if (!getCluster().getStoragePoolId().equals(getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
        }
        setDescription(getVmTemplateName());

        // check that the storage pool is valid
        if (!validate(createStoragePoolValidator().existsAndUp())
                || !validateTemplateArchitecture()
                || !isClusterCompatible()) {
            return false;
        }

        if (!validateSourceStorageDomain()) {
            return false;
        }

        sourceTemplateId = getVmTemplateId();
        if (getParameters().isImportAsNewEntity()) {
            initImportClonedTemplate();
        }

        VmTemplate duplicateTemplate = vmTemplateDao.get(getParameters().getVmTemplate().getId());
        // check that the template does not exists in the target domain
        if (duplicateTemplate != null) {
            return failValidation(EngineMessage.VMT_CANNOT_IMPORT_TEMPLATE_EXISTS,
                    String.format("$TemplateName %1$s", duplicateTemplate.getName()));
        }
        if (getVmTemplate().isBaseTemplate() && isVmTemplateWithSameNameExist()) {
            return failValidation(EngineMessage.VM_CANNOT_IMPORT_TEMPLATE_NAME_EXISTS);
        }

        if (!validateNoDuplicateDiskImages(getImages())) {
            return false;
        }

        if (getImages() != null && !getImages().isEmpty() && !getParameters().isImagesExistOnTargetStorageDomain()) {
            if (!validateSpaceRequirements(getImages())) {
                return false;
            }
        }

        List<VmNetworkInterface> vmNetworkInterfaces = getVmTemplate().getInterfaces();
        vmNicMacsUtils.replaceInvalidEmptyStringMacAddressesWithNull(vmNetworkInterfaces);
        if (!validate(vmNicMacsUtils.validateMacAddress(vmNetworkInterfaces))) {
            return false;
        }

        // if this is a template version, check base template exist
        if (!getVmTemplate().isBaseTemplate()) {
            VmTemplate baseTemplate = vmTemplateDao.get(getVmTemplate().getBaseTemplateId());
            if (baseTemplate == null) {
                return failValidation(EngineMessage.VMT_CANNOT_IMPORT_TEMPLATE_VERSION_MISSING_BASE);
            }
        }

        if (!setAndValidateDiskProfiles()) {
            return false;
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }

        if (!validate(vmHandler.validateMaxMemorySize(getVmTemplate(), getEffectiveCompatibilityVersion()))) {
            return false;
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getVmTemplate().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        return true;
    }

    protected abstract boolean validateSourceStorageDomain();

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    protected boolean isClusterCompatible () {
        if (getCluster().getArchitecture() != getVmTemplate().getClusterArch()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER);
            return false;
        }
        return true;
    }

    private boolean validateTemplateArchitecture() {
        if (getVmTemplate().getClusterArch() == ArchitectureType.undefined) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_TEMPLATE_WITH_NOT_SUPPORTED_ARCHITECTURE);
            return false;
        }
        return true;
    }

    protected boolean isVmTemplateWithSameNameExist() {
        return vmTemplateDao.getByName(getParameters().getVmTemplate().getName(),
                getParameters().getStoragePoolId(),
                null,
                false) != null;
    }

    private void initImportClonedTemplate() {
        Guid newTemplateId = Guid.newGuid();
        getParameters().getVmTemplate().setId(newTemplateId);
        for (VmNetworkInterface iface : getParameters().getVmTemplate().getInterfaces()) {
            iface.setId(Guid.newGuid());
        }
        // cloned template is always base template, as its a new entity
        getParameters().getVmTemplate().setBaseTemplateId(newTemplateId);
    }

    protected abstract void initImportClonedTemplateDisks();

    protected Map<Guid, Guid> getImageMappings() {
        return getImages().stream().collect(Collectors.toMap(
                DiskImage::getImageId,
                d -> originalDiskImageIdMap.get(d.getId())));
    }

    protected boolean validateNoDuplicateDiskImages(Collection<DiskImage> images) {
        if (!getParameters().isImportAsNewEntity() && !getParameters().isImagesExistOnTargetStorageDomain()) {
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);
            return validate(diskImagesValidator.diskImagesAlreadyExist());
        }

        return true;
    }

    protected List<DiskImage> getImages() {
        return getParameters().getImages();
    }

    private boolean validateLeaseStorageDomain(Guid leaseStorageDomainId) {
        StorageDomain domain = storageDomainDao.getForStoragePool(leaseStorageDomainId, getStoragePoolId());
        StorageDomainValidator validator = new StorageDomainValidator(domain);
        return validate(validator.isDomainExistAndActive()) && validate(validator.isDataDomain());
    }

    private Guid getVmLeaseToDefaultStorageDomain() {
        return storageDomainStaticDao.getAllForStoragePool(getStoragePoolId()).stream()
                .map(StorageDomainStatic::getId)
                .filter(this::validateLeaseStorageDomain)
                .findFirst()
                .orElse(null);
    }

    private boolean shouldAddLease(VmTemplate vm) {
        return vm.getLeaseStorageDomainId() != null;
    }

    private void handleVmLease() {
        Guid importedLeaseStorageDomainId = getVmTemplate().getLeaseStorageDomainId();
        if (importedLeaseStorageDomainId == null) {
            return;
        }
        if (!getVmTemplate().isAutoStartup() || !shouldAddLease(getVmTemplate())) {
            getVmTemplate().setLeaseStorageDomainId(null);
            return;
        }
        if (validateLeaseStorageDomain(importedLeaseStorageDomainId)) {
            return;
        }
        getVmTemplate().setLeaseStorageDomainId(getVmLeaseToDefaultStorageDomain());
        if (getVmTemplate().getLeaseStorageDomainId() == null) {
            auditLog(this, AuditLogType.CANNOT_IMPORT_VM_TEMPLATE_WITH_LEASE_STORAGE_DOMAIN);
        } else {
            log.warn("Setting the lease for the VM Template '{}' to the storage domain '{}', because the storage domain '{}' is unavailable",
                    getVmTemplate().getId(), getVmTemplate().getLeaseStorageDomainId(), importedLeaseStorageDomainId);
        }
    }

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(() -> {
            handleVmLease();
            initImportClonedTemplateDisks();
            addVmTemplateToDb();
            addPermissionsToDB();
            updateOriginalTemplateNameOnDerivedVms();
            addVmInterfaces();
            getCompensationContext().stateChanged();
            vmHandler.addVmInitToDB(getVmTemplate().getVmInit());
            return null;
        });

        boolean doesVmTemplateContainImages = !getImages().isEmpty();
        if (doesVmTemplateContainImages && !getParameters().isImagesExistOnTargetStorageDomain()) {
            copyImagesToTargetDomain();
        }

        getVmDeviceUtils().addImportedDevices(getVmTemplate(), getParameters().isImportAsNewEntity(), false);

        if (!doesVmTemplateContainImages || getParameters().isImagesExistOnTargetStorageDomain()) {
            endMoveOrCopyCommand();
        }
        discardHelper.logIfDisksWithIllegalPassDiscardExist(getVmTemplateId());
        checkTrustedService();
        incrementDbGeneration();
        logOnExecuteEndMethod.run();
        setActionReturnValue(getVmTemplate());
        setSucceeded(true);
    }

    protected void addPermissionsToDB() {
        // Left empty to be overridden in ImportVmTemplateFromConfigurationCommand
    }

    private void updateOriginalTemplateNameOnDerivedVms() {
        if (!getParameters().isImportAsNewEntity()) {
            // in case it has been renamed
            vmDao.updateOriginalTemplateName(getVmTemplate().getId(), getVmTemplate().getName());
        }
    }

    private void checkTrustedService() {
        if (getVmTemplate().isTrustedService() && !getCluster().supportsTrustedService()) {
            auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_TRUSTED_TO_UNTRUSTED);
        } else if (!getVmTemplate().isTrustedService() && getCluster().supportsTrustedService()) {
            auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FROM_UNTRUSTED_TO_TRUSTED);
        }
    }

    protected abstract void copyImagesToTargetDomain();

    private void addVmTemplateToDb() {
        getVmTemplate().setClusterId(getParameters().getClusterId());

        // if "run on host" field points to a non existent vds (in the current cluster) -> remove field and continue
        if(!vmHandler.validateDedicatedVdsExistOnSameCluster(getVmTemplate()).isValid()) {
            getVmTemplate().setDedicatedVmForVdsList(Collections.emptyList());
            getVmTemplate().setCpuPinning(null);
        }

        getVmTemplate().setStatus(VmTemplateStatus.Locked);
        getVmTemplate().setQuotaId(getParameters().getQuotaId());
        vmHandler.autoSelectResumeBehavior(getVmTemplate());
        vmTemplateDao.save(getVmTemplate());
        getCompensationContext().snapshotNewEntity(getVmTemplate());
        addDisksToDb();
    }

    protected abstract void addDisksToDb();

    private void addVmInterfaces() {
        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getVmTemplate().getClusterId(),
                        getStoragePoolId(),
                        AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_INVALID_INTERFACES);

        for (VmNetworkInterface iface : getVmTemplate().getInterfaces()) {
            if (iface.getId() == null) {
                iface.setId(Guid.newGuid());
            }

            iface.setVmId(getVmTemplateId());
            VmNic nic = new VmNic();
            nic.setId(iface.getId());
            nic.setVmId(getVmTemplateId());
            nic.setName(iface.getName());
            nic.setLinked(iface.isLinked());
            nic.setSpeed(iface.getSpeed());
            nic.setType(iface.getType());

            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());
            nic.setVnicProfileId(iface.getVnicProfileId());
            vmNicDao.save(nic);
            getCompensationContext().snapshotNewEntity(nic);

            VmNetworkStatistics iStat = new VmNetworkStatistics();
            nic.setStatistics(iStat);
            iStat.setId(iface.getId());
            iStat.setVmId(getVmTemplateId());
            vmNetworkStatisticsDao.save(iStat);
            getCompensationContext().snapshotNewEntity(iStat);
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmTemplateName());
    }

    @Override
    protected void endMoveOrCopyCommand() {
        vmTemplateHandler.unlockVmTemplate(getVmTemplateId());
        endActionForImageGroups();
        setSucceeded(true);
    }

    private void removeNetwork() {
        List<VmNic> nics = vmNicDao.getAllForTemplate(getVmTemplateId());
        nics.stream().map(VmNic::getId).forEach(vmNicDao::remove);
    }

    private void endActionForImageGroups() {
        for (ActionParametersBase p : getParameters().getImagesParameters()) {
            p.setTaskGroupSuccess(getParameters().getTaskGroupSuccess());
            backend.endAction(ActionType.CopyImageGroup,
                    p,
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    protected void endWithFailure() {
        removeNetwork();
        endActionForImageGroups();
        vmTemplateDao.remove(getVmTemplateId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_STARTING_IMPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE
                    : AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_IMPORT_TEMPLATE_FAILED;
        }
    }

    @Override
    public Guid getVmTemplateId() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVmTemplate().getId();
        } else {
            return super.getVmTemplateId();
        }
    }

    @Override
    public VmTemplate getVmTemplate() {
        if (getParameters().isImportAsNewEntity()) {
            return getParameters().getVmTemplate();
        } else {
            return super.getVmTemplate();
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        if(getParameters().isImportAsNewEntity()){
            return addValidationGroup(ImportClonedEntity.class);
        }
        return addValidationGroup(ImportEntity.class);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties =  super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(),
                    (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }

    protected boolean setAndValidateDiskProfiles() {
        if (getParameters().getVmTemplate().getDiskList() != null) {
            Map<DiskImage, Guid> map = new HashMap<>();
            for (DiskImage diskImage : getParameters().getVmTemplate().getDiskList()) {
                map.put(diskImage, imageToDestinationDomainMap.get(diskImage.getId()));
            }
            return validate(diskProfileHelper.setAndValidateDiskProfiles(map, getCurrentUser()));
        }
        return true;
    }

    protected boolean setAndValidateCpuProfile() {
        getVmTemplate().setClusterId(getClusterId());
        getVmTemplate().setCpuProfileId(getParameters().getCpuProfileId());
        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getVmTemplate(),
                getUserIdIfExternal().orElse(null)));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        for (DiskImage disk : getParameters().getVmTemplate().getDiskList()) {
            //TODO: handle import more than once;
            list.add(new QuotaStorageConsumptionParameter(
                    disk.getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.CONSUME,
                    imageToDestinationDomainMap.get(disk.getId()),
                    (double)disk.getSizeInGigabytes()));
        }
        return list;
    }

    /**
     * Updating managed device map of VM, with the new disk {@link Guid}s.<br/>
     * The update of managedDeviceMap is based on the newDiskIdForDisk map,
     * so this method should be called only after newDiskIdForDisk is initialized.
     *
     * @param disk
     *            - The disk which is about to be cloned
     * @param managedDeviceMap
     *            - The managed device map contained in the VM.
     */
    protected void updateManagedDeviceMap(DiskImage disk, Map<Guid, VmDevice> managedDeviceMap) {
        Guid oldDiskId = originalDiskIdMap.get(disk.getId());
        managedDeviceMap.put(disk.getId(), managedDeviceMap.get(oldDiskId));
        managedDeviceMap.remove(oldDiskId);
    }

    /**
     * Cloning a new disk with a new generated id, with the same parameters as <code>disk</code>. Also
     * adding the disk to <code>newDiskGuidForDisk</code> map, so we will be able to link between the new cloned disk
     * and the old disk id.
     *
     * @param disk
     *            - The disk which is about to be cloned
     */
    protected void generateNewDiskId(DiskImage disk) {
        Guid newGuidForDisk = Guid.newGuid();

        originalDiskIdMap.put(newGuidForDisk, disk.getId());
        originalDiskImageIdMap.put(newGuidForDisk, disk.getImageId());
        disk.setId(newGuidForDisk);
        disk.setImageId(Guid.newGuid());
    }

    protected Guid getOriginalDiskIdMap(Guid diskId) {
        return originalDiskIdMap.get(diskId);
    }

    protected Guid getOriginalDiskImageIdMap(Guid diskId) {
        return originalDiskImageIdMap.get(diskId);
    }

    protected Guid getSourceTemplateId() {
        return sourceTemplateId;
    }
}
