package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.UniquePermissionsSet;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.predicate.VnicWithBadMacPredicate;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.bll.utils.CompatibilityVersionUpdater;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmUpdateType;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.VmInfoBuildUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;

public abstract class ImportVmCommandBase<T extends ImportVmParameters> extends VmCommand<T> {

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected final Map<Guid, DiskImage> newDiskIdForDisk = new HashMap<>();
    private Guid sourceDomainId = Guid.Empty;
    private StorageDomain sourceDomain;
    private ImportValidator importValidator;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    ExternalVmMacsFinder externalVmMacsFinder;
    @Inject
    private CpuProfileHelper cpuProfileHelper;
    @Inject
    private BlockStorageDiscardFunctionalityHelper discardHelper;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private VmStatisticsDao vmStatisticsDao;
    @Inject
    private MultiLevelAdministrationHandler multiLevelAdministrationHandler;
    @Inject
    private CloudInitHandler cloudInitHandler;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;

    private final List<String> macsAdded = new ArrayList<>();
    private static VmStatic vmStaticForDefaultValues = new VmStatic();
    private MacPool macPool;

    private Runnable logOnExecuteEndMethod = () -> {};

    ImportVmCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    ImportVmCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        if (getCluster().getBiosType() == null) {
            return failValidation(EngineMessage.CLUSTER_BIOS_TYPE_NOT_SET);
        }

        updateVmVersion();

        if (!validate(VmValidator.isBiosTypeSupported(getVm().getStaticData(), getCluster(), osRepository))) {
            return false;
        }

        if (getParameters().getStoragePoolId() != null
                && !getParameters().getStoragePoolId().equals(getCluster().getStoragePoolId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_IS_NOT_VALID);
        }
        macPool = getMacPool();

        List<String> nicsUnableToBeImported = getVm().getInterfaces()
                .stream()
                .filter(this::ifaceMacCannotBeAddedToMacPool)
                .map(v -> v.getName())
                .collect(Collectors.toList());

        if (!nicsUnableToBeImported.isEmpty()) {
            EngineMessage engineMessage = EngineMessage.ACTION_TYPE_FAILED_CANNOT_ADD_IFACE_DUE_TO_MAC_DUPLICATES;
            Collection<String> replacements =
                    ReplacementUtils.getListVariableAssignmentString(engineMessage, nicsUnableToBeImported);

            return validate(new ValidationResult(engineMessage, replacements));
        }

        List<EngineMessage> msgs = cloudInitHandler.validate(getVm().getVmInit());
        if (!CollectionUtils.isEmpty(msgs)) {
            return failValidation(msgs);
        }

        return true;
    }

    private boolean ifaceMacCannotBeAddedToMacPool(VmNetworkInterface iface) {
        return !macPool.isDuplicateMacAddressesAllowed()
                && !shouldReassignMac(iface)
                && macPool.isMacInUse(iface.getMacAddress());
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

    protected Version getEffectiveCompatibilityVersion() {
        return CompatibilityVersionUtils.getEffective(getParameters().getVm(), this::getCluster);
    }

    protected ImportValidator getImportValidator() {
        if (importValidator == null) {
            importValidator = new ImportValidator(getParameters());
        }
        return importValidator;
    }

    protected boolean validateUniqueVmName() {
        return vmHandler.isVmWithSameNameExistStatic(getVm().getName(), getStoragePoolId()) ?
                failValidation(EngineMessage.VM_CANNOT_IMPORT_VM_NAME_EXISTS)
                : true;
    }

    /**
     * Validates that there is no duplicate VM.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateNoDuplicateVm() {
        VmStatic duplicateVm = vmStaticDao.get(getVm().getId());
        return duplicateVm == null ? true :
            failValidation(EngineMessage.VM_CANNOT_IMPORT_VM_EXISTS, String.format("$VmName %1$s", duplicateVm.getName()));
    }

    /**
     * Validates if the VM being imported has a valid architecture.
     */
    protected boolean validateVmArchitecture () {
        return getVm().getClusterArch() == ArchitectureType.undefined ?
            failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_VM_WITH_NOT_SUPPORTED_ARCHITECTURE)
            : true;
    }

    /**
     * Validates that that the required cluster exists and is compatible
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateVdsCluster() {
        Cluster cluster = clusterDao.get(getClusterId());
        return cluster == null ?
                failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID)
                : cluster.getArchitecture() != getVm().getClusterArch() ?
                        failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_VM_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER)
                        : true;
    }

    protected boolean validateGraphicsAndDisplay() {
        return validate(vmHandler.isGraphicsAndDisplaySupported(getParameters().getVm().getOs(),
                getGraphicsTypesForVm(),
                getVm().getDefaultDisplayType(),
                getVm().getBiosType(),
                getEffectiveCompatibilityVersion()));
    }

    Set<GraphicsType> getGraphicsTypesForVm() {
        return getDevicesOfType(VmDeviceGeneralType.GRAPHICS)
                .stream()
                .map(g -> GraphicsType.fromVmDeviceType(VmDeviceType.getByName(g.getDevice())))
                .collect(Collectors.toSet());
    }

    private List<VmDevice> getDevicesOfType(VmDeviceGeneralType type) {
        if (getVm() == null || getVm().getStaticData() == null || getVm().getStaticData().getManagedDeviceMap() == null) {
            return Collections.emptyList();
        }
        return getVm().getStaticData()
                .getManagedDeviceMap()
                .values()
                .stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }

    protected boolean setAndValidateCpuProfile() {
        getVm().getStaticData().setClusterId(getClusterId());
        getVm().getStaticData().setCpuProfileId(getParameters().getCpuProfileId());
        return validate(cpuProfileHelper.setAndValidateCpuProfile(
                getVm().getStaticData(),
                getUserIdIfExternal().orElse(null)));
    }

    protected boolean validateSoundDevice() {
        if (!VmDeviceCommonUtils.isSoundDeviceExists(getVm().getManagedVmDeviceMap().values())) {
            return true;
        }

        if (!osRepository.isSoundDeviceEnabled(getVm().getStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.SOUND_DEVICE_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        return true;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    @SuppressWarnings("serial")
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getParameters().getVm() != null && !StringUtils.isBlank(getParameters().getVm().getName())) {
            return new HashMap<String, Pair<String, String>>() {
                {
                    put(getParameters().getVm().getName(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_NAME,
                                    EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));

                    put(getParameters().getVm().getId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM,
                                    getVmIsBeingImportedMessage()));
                }
            };
        }
        return null;
    }

    protected LockMessage getVmIsBeingImportedMessage() {
        return new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_IMPORTED)
                .withOptional("VmName", getVmName());
    }

    @Override
    protected void init() {
        super.init();
        T parameters = getParameters();
        // before the execute phase, parameters.getVmId().equals(parameters.getVm().getId() == true
        // afterwards if will be false if parameters.isImportAsNewEntity() == true, and there is no
        // better way to check it (can't use the action-state since it will always be EXECUTE
        // in the postConstruct phase.
        if (parameters.isImportAsNewEntity() && parameters.getVmId().equals(parameters.getVm().getId())) {
            parameters.getVm().setId(Guid.newGuid());
        }
        setClusterId(parameters.getClusterId());
        setVm(parameters.getVm());
        if (parameters.getVm() != null) {
            setVmId(parameters.getVm().getId());
            initBiosType();
        }
    }

    protected void initBiosType() {
        // override in subclasses if needed
    }

    protected void updateVmVersion() {
        Version newVersion = CompatibilityVersionUtils.getEffective(getVm(), this::getCluster);

        // A VM can have custom compatibility version that is lower than
        // the DC version. In that case, the custom version has to be updated.
        if (newVersion.less(getCluster().getCompatibilityVersion())) {
            var dataCenterVersion = getStoragePool().getCompatibilityVersion();
            if (newVersion.less(dataCenterVersion)) {
                Version originalVersion = newVersion;
                logOnExecuteEnd(() -> {
                    addCustomValue("OriginalVersion", originalVersion.toString());
                    addCustomValue("NewVersion", dataCenterVersion.toString());
                    auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_CUSTOM_VERSION_CHANGE);
                });

                newVersion = dataCenterVersion;
            }
        }

        var updates = new CompatibilityVersionUpdater().updateVmCompatibilityVersion(getVm(), newVersion, getCluster());

        if (!updates.isEmpty()) {
            logOnExecuteEnd(() -> {
                String updatesString = updates.stream()
                        .map(VmUpdateType::getDisplayName)
                        .collect(Collectors.joining(", "));

                addCustomValue("Updates", updatesString);
                auditLog(this, AuditLogType.IMPORTEXPORT_IMPORT_VM_UPDATED);
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

    /**
     * Cloning a new disk and all its volumes with a new generated id.
     * The disk will have the same parameters as disk.
     * Also adding the disk to newDiskGuidForDisk map, so we will be able to link between
     * the new cloned disk and the old disk id.
     *
     * @param diskImagesList
     *            - All the disk volumes
     * @param disk
     *            - The disk which is about to be cloned
     */
    protected void generateNewDiskId(List<DiskImage> diskImagesList, DiskImage disk) {
        Guid generatedGuid = generateNewDiskId(disk);
        diskImagesList.forEach(diskImage ->  diskImage.setId(generatedGuid));
    }

    /**
     * Cloning a new disk with a new generated id, with the same parameters as <code>disk</code>. Also
     * adding the disk to <code>newDiskGuidForDisk</code> map, so we will be able to link between the new cloned disk
     * and the old disk id.
     *
     * @param disk
     *            - The disk which is about to be cloned
     */
    protected Guid generateNewDiskId(DiskImage disk) {
        Guid newGuidForDisk = Guid.newGuid();

        // Copy the disk so it will preserve the old disk id and image id.
        newDiskIdForDisk.put(newGuidForDisk, DiskImage.copyOf(disk));
        disk.setId(newGuidForDisk);
        disk.setImageId(Guid.newGuid());
        return newGuidForDisk;
    }

    /**
     * Updating managed device map of VM, with the new disk {@link Guid}.
     * The update of managedDeviceMap is based on the newDiskIdForDisk map,
     * so this method should be called only after newDiskIdForDisk is initialized.
     *
     * @param disk
     *            - The disk which is about to be cloned
     * @param managedDeviceMap
     *            - The managed device map contained in the VM.
     */
    protected void updateManagedDeviceMap(DiskImage disk, Map<Guid, VmDevice> managedDeviceMap) {
        Guid oldDiskId = newDiskIdForDisk.get(disk.getId()).getId();
        managedDeviceMap.put(disk.getId(), managedDeviceMap.get(oldDiskId));
        managedDeviceMap.remove(oldDiskId);
    }

    protected void ensureDomainMap(Collection<DiskImage> images, Guid defaultDomainId) {
        if (imageToDestinationDomainMap == null) {
            imageToDestinationDomainMap = new HashMap<>();
        }
        if (imageToDestinationDomainMap.isEmpty() && images != null && defaultDomainId != null) {
            for (DiskImage image : images) {
                if (isImagesAlreadyOnTarget()) {
                    imageToDestinationDomainMap.put(image.getId(), image.getStorageIds().get(0));
                } else if (!Guid.Empty.equals(defaultDomainId)) {
                    imageToDestinationDomainMap.put(image.getId(), defaultDomainId);
                }
            }
        }
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
            DiskImage dummy = imagesHandler.createDiskImageWithExcessData(image, targetSdId);
            dummies.add(dummy);
        }
        return dummies;
    }

    protected void setImagesWithStoragePoolId(Guid storagePoolId, List<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            diskImage.setStoragePoolId(storagePoolId);
        }
    }

    protected StorageDomain getSourceDomain() {
        if (sourceDomain == null && !Guid.Empty.equals(sourceDomainId)) {
            sourceDomain = storageDomainDao.getForStoragePool(sourceDomainId, getStoragePool().getId());
        }
        return sourceDomain;
    }

    protected void setSourceDomainId(Guid storageId) {
        sourceDomainId = storageId;
    }

    protected boolean isImagesAlreadyOnTarget() {
        return getParameters().isImagesExistOnTargetStorageDomain();
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return storageDomainDao.getForStoragePool(domainId, getStoragePool().getId());
    }

    @Override
    protected void executeVmCommand() {
        try {
            addVmToDb();
            addVmToAffinityGroups();
            addVmToAffinityLabels();
            addPermissionsToDB();
            processImages();
            vmHandler.addVmInitToDB(getVm().getStaticData().getVmInit());
            discardHelper.logIfDisksWithIllegalPassDiscardExist(getVmId());
        } catch (RuntimeException e) {
            macPool.freeMacs(macsAdded);
            throw e;
        }

        logOnExecuteEndMethod.run();
        setSucceeded(true);
        getReturnValue().setActionReturnValue(getVm());
    }

    public void addVmToAffinityGroups() {
        // Left empty to override in ImportVmFromConfiguration
    }

    public void addVmToAffinityLabels() {
        // Left empty to override in ImportVmFromConfiguration
    }

    protected void addPermissionsToDB() {
        // Left empty to be overridden by ImportVmFromConfiguration
    }

    private void reportExternalMacs() {
        final VM vm = getVm();
        final Set<String> externalMacAddresses = externalVmMacsFinder.findExternalMacAddresses(vm);
        if (CollectionUtils.isNotEmpty(externalMacAddresses)) {
            auditLog(createExternalMacsAuditLog(vm, externalMacAddresses), AuditLogType.MAC_ADDRESS_IS_EXTERNAL);
        }
    }

    private AuditLogable createExternalMacsAuditLog(VM vm, Set<String> externalMacs) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVmId(vm.getId());
        logable.setVmName(vm.getName());
        logable.addCustomValue("MACAddr", externalMacs.stream().collect(Collectors.joining(", ")));
        return logable;
    }

    protected abstract void processImages();

    protected void addVmToDb() {
        TransactionSupport.executeInNewTransaction(() -> {
            addVmStatic();
            addVmDynamic();
            addVmStatistics();
            addVmInterfaces();
            addVmPermission();
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private void addVmPermission() {
        UniquePermissionsSet permissionsToAdd = new UniquePermissionsSet();
        if (isMakeCreatorExplicitOwner()) {
            permissionsToAdd.addPermission(
                    getCurrentUser().getId(),
                    PredefinedRoles.VM_OPERATOR.getId(),
                    getVmId(), VdcObjectType.VM);
        }

        if (!permissionsToAdd.isEmpty()) {
            List<Permission> permissionsList = permissionsToAdd.asPermissionList();
            multiLevelAdministrationHandler.addPermission(permissionsList.toArray(new Permission[permissionsList.size()]));

            getCompensationContext().snapshotNewEntities(permissionsList);
        }
    }

    private boolean isMakeCreatorExplicitOwner() {
        return getCurrentUser() != null && !checkUserAuthorization(
                getCurrentUser().getId(),
                ActionGroup.MANIPULATE_PERMISSIONS,
                getVmId(),
                VdcObjectType.VM);
    }

    protected void addVmStatic() {
        logImportEvents();
        getVm().setId(getVmId());
        getVm().setVmCreationDate(new Date());
        getVm().setClusterId(getParameters().getClusterId());
        getVm().setMinAllocatedMem(computeMinAllocatedMem());
        getVm().setQuotaId(getParameters().getQuotaId());

        // if "run on host" field points to a non existent vds (in the current cluster) -> remove field and continue
        if (!vmHandler.validateDedicatedVdsExistOnSameCluster(getVm().getStaticData()).isValid()) {
            getVm().setDedicatedVmForVdsList(Collections.emptyList());
            getVm().setCpuPinning(null);
        }

        if (getVm().getOriginalTemplateGuid() != null && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getOriginalTemplateGuid())) {
            // no need to check this for blank
            VmTemplate originalTemplate = vmTemplateDao.get(getVm().getOriginalTemplateGuid());
            if (originalTemplate != null) {
                // in case the original template name has been changed in the meantime
                getVm().setOriginalTemplateName(originalTemplate.getName());
            }
        }

        if (StringUtils.isEmpty(getVm().getTimeZone())) {
            getVm().setTimeZone(vmInfoBuildUtils.getTimeZoneForVm(getVm()));
        }

        if (getParameters().getCopyCollapse()) {
            getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        }

        vmHandler.autoSelectResumeBehavior(getVm().getStaticData());

        vmStaticDao.save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    private void logImportEvents() {
        // Some values at the OVF file are used for creating events at the GUI
        // for the sake of providing information on the content of the VM that
        // was exported,
        // but not setting it in the imported VM
        VmStatic vmStaticFromOvf = getVm().getStaticData();

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vmStaticFromOvf);
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();

        if (aliasesValuesMap != null) {
            for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();
                logField(vmStaticFromOvf, fieldName, fieldValue);
            }
        }

        handler.resetDefaults(vmStaticForDefaultValues);

    }

    private void logField(VmStatic vmStaticFromOvf, String fieldName, String fieldValue) {
        String vmName = vmStaticFromOvf.getName();
        addCustomValue("FieldName", fieldName);
        addCustomValue("VmName", vmName);
        addCustomValue("FieldValue", fieldValue);
        auditLogDirector.log(this, AuditLogType.VM_IMPORT_INFO);
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(macPool);

        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getClusterId(),
                        getStoragePoolId(),
                        AuditLogType.IMPORTEXPORT_IMPORT_VM_INVALID_INTERFACES);

        List<VmNetworkInterface> nics = getVm().getInterfaces();

        vmInterfaceManager.sortVmNics(nics, getVm().getStaticData().getManagedDeviceMap());

        if (!getParameters().isImportAsNewEntity() && isExternalMacsToBeReported()) {
            reportExternalMacs();
        }

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            initInterface(iface);
            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());

            vmInterfaceManager.add(iface, getCompensationContext(), shouldReassignMac(iface));
            macsAdded.add(iface.getMacAddress());
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmName());
    }

    private boolean shouldReassignMac(VmNetworkInterface iface) {
        return StringUtils.isEmpty(iface.getMacAddress())
                || (getParameters().isReassignBadMacs() && vNicHasBadMac(iface))
                || getParameters().isImportAsNewEntity();
    }

    protected boolean vNicHasBadMac(VmNetworkInterface vnic) {
        MacPool macPool = getMacPool();
        Predicate<VmNetworkInterface> vnicWithBadMacPredicate = new VnicWithBadMacPredicate(macPool);
        return vnicWithBadMacPredicate.test(vnic);
    }

    protected boolean isExternalMacsToBeReported() {
        return true;
    }

    private void initInterface(VmNic iface) {
        if (iface.getId() == null) {
            iface.setId(Guid.newGuid());
        }
        iface.setVmId(getVmId());
    }

    private void addVmDynamic() {
        VmDynamic tempVar = createVmDynamic();
        vmDynamicDao.save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    protected VmDynamic createVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(getVmId());
        vmDynamic.setStatus(VMStatus.ImageLocked);
        vmDynamic.setVmHost("");
        vmDynamic.setIp("");
        vmDynamic.setFqdn("");
        vmDynamic.setLastStopTime(new Date());
        vmDynamic.setAppList(getParameters().getVm().getAppList());
        return vmDynamic;
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics(getVmId());
        vmStatisticsDao.save(stats);
        getCompensationContext().snapshotNewEntity(stats);
        getCompensationContext().stateChanged();
    }

    private int computeMinAllocatedMem() {
        if (getVm().getMinAllocatedMem() > 0) {
            return getVm().getMinAllocatedMem();
        }

        Cluster cluster = getCluster();
        if (cluster != null && cluster.getMaxVdsMemoryOverCommit() > 0) {
            return (getVm().getMemSizeMb() * 100) / cluster.getMaxVdsMemoryOverCommit();
        }

        return getVm().getMemSizeMb();
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
        List<PermissionSubject> permissionList = new ArrayList<>();
        // Destination cluster
        permissionList.add(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                ActionGroup.CREATE_VM));
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.Cluster.name().toLowerCase(), getClusterName());
        }
        return jobProperties;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_IMPORT_VM
                    : AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        case END_FAILURE:
            return AuditLogType.IMPORTEXPORT_IMPORT_VM_FAILED;
        }
        return super.getAuditLogTypeValue();
    }
}
