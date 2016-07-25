package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.profiles.CpuProfileHelper;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfLogEventHandler;
import org.ovirt.engine.core.utils.ovf.VMStaticOvfLogHandler;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class ImportVmCommandBase<T extends ImportVmParameters> extends VmCommand<T> {

    private static final String COMMA_SEPARATOR = ", ";

    protected Map<Guid, Guid> imageToDestinationDomainMap;
    protected final Map<Guid, DiskImage> newDiskIdForDisk = new HashMap<>();
    private Guid sourceDomainId = Guid.Empty;
    private StorageDomain sourceDomain;
    private ImportValidator importValidator;
    private Version effectiveCompatibilityVersion;

    @Inject
    ExternalVmMacsFinder externalVmMacsFinder;

    private final List<String> macsAdded = new ArrayList<>();
    private static VmStatic vmStaticForDefaultValues = new VmStatic();
    private MacPool macPool;

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
        macPool = getMacPool();
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        return true;
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
        return effectiveCompatibilityVersion;
    }

    protected void setEffectiveCompatibilityVersion(Version effectiveCompatibilityVersion) {
        this.effectiveCompatibilityVersion = effectiveCompatibilityVersion;
    }

    protected ImportValidator getImportValidator() {
        if (importValidator == null) {
            importValidator = new ImportValidator(getParameters());
        }
        return importValidator;
    }

    protected boolean validateUniqueVmName() {
        return VmHandler.isVmWithSameNameExistStatic(getVm().getName(), getStoragePoolId()) ?
                failValidation(EngineMessage.VM_CANNOT_IMPORT_VM_NAME_EXISTS)
                : true;
    }

    /**
     * Validates that there is no duplicate VM.
     * @return <code>true</code> if the validation passes, <code>false</code> otherwise.
     */
    protected boolean validateNoDuplicateVm() {
        VmStatic duplicateVm = getVmStaticDao().get(getVm().getId());
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
        Cluster cluster = getClusterDao().get(getClusterId());
        return cluster == null ?
                failValidation(EngineMessage.VDS_CLUSTER_IS_NOT_VALID)
                : cluster.getArchitecture() != getVm().getClusterArch() ?
                        failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_CANNOT_IMPORT_VM_ARCHITECTURE_NOT_SUPPORTED_BY_CLUSTER)
                        : true;
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
                getReturnValue().getValidationMessages());
    }

    protected boolean validateGraphicsAndDisplay() {
        return VmHandler.isGraphicsAndDisplaySupported(getParameters().getVm().getOs(),
                getGraphicsTypesForVm(),
                getVm().getDefaultDisplayType(),
                getReturnValue().getValidationMessages(),
                getEffectiveCompatibilityVersion());
    }

    Set<GraphicsType> getGraphicsTypesForVm() {
        Set<GraphicsType> graphicsTypes = new HashSet<>();

        for (VmDevice graphics : getDevicesOfType(VmDeviceGeneralType.GRAPHICS)) {
            graphicsTypes.add(GraphicsType.fromVmDeviceType(VmDeviceType.getByName(graphics.getDevice())));
        }

        return graphicsTypes;
    }

    private List<VmDevice> getDevicesOfType(VmDeviceGeneralType type) {
        List<VmDevice> devices = new ArrayList<>();

        if (getVm() != null && getVm().getStaticData() != null && getVm().getStaticData().getManagedDeviceMap() != null) {
            for (VmDevice vmDevice : getVm().getStaticData().getManagedDeviceMap().values()) {
                if (vmDevice.getType() == type) {
                    devices.add(vmDevice);
                }
            }
        }
        return devices;
    }

    protected boolean setAndValidateCpuProfile() {
        getVm().getStaticData().setClusterId(getClusterId());
        getVm().getStaticData().setCpuProfileId(getParameters().getCpuProfileId());
        return validate(CpuProfileHelper.setAndValidateCpuProfile(getVm().getStaticData()));
    }

    protected boolean validateBallonDevice() {
        if (!VmDeviceCommonUtils.isBalloonDeviceExists(getVm().getManagedVmDeviceMap().values())) {
            return true;
        }

        if (!osRepository.isBalloonEnabled(getVm().getStaticData().getOsId(),
                getEffectiveCompatibilityVersion())) {
            addValidationMessageVariable("clusterArch", getCluster().getArchitecture());
            return failValidation(EngineMessage.BALLOON_REQUESTED_ON_NOT_SUPPORTED_ARCH);
        }

        return true;
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

    protected String getVmIsBeingImportedMessage() {
        StringBuilder builder = new StringBuilder(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_IMPORTED.name());
        if (getVmName() != null) {
            builder.append(String.format("$VmName %1$s", getVmName()));
        }
        return builder.toString();
    }

    @Override
    protected void init() {
        super.init();
        T parameters = getParameters();

        setClusterId(parameters.getClusterId());
        final Guid dcId = getDcId();
        setStoragePoolId(dcId);

        final VM vm = parameters.getVm();
        if (vm != null) {
            // before the execute phase, parameters.getVmId().equals(parameters.getVm().getId() == true
            // afterwards if will be false if parameters.isImportAsNewEntity() == true, and there is no
            // better way to check it (can't use the action-state since it will always be EXECUTE
            // in the postConstruct phase.
            if (parameters.isImportAsNewEntity() && parameters.getVmId().equals(vm.getId())) {
                vm.setId(Guid.newGuid());
            }
            vm.setStoragePoolId(dcId);
            setVm(vm);
        }
        initEffectiveCompatibilityVersion();
    }

    protected void initEffectiveCompatibilityVersion() {
        setEffectiveCompatibilityVersion(
                CompatibilityVersionUtils.getEffective(getParameters().getVm(), this::getCluster));
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
        for (DiskImage diskImage : diskImagesList) {
            diskImage.setId(generatedGuid);
        }
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
            DiskImage dummy = ImagesHandler.createDiskImageWithExcessData(image, targetSdId);
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
            sourceDomain = getStorageDomainDao().getForStoragePool(sourceDomainId, getStoragePool().getId());
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
        return getStorageDomainDao().getForStoragePool(domainId, getStoragePool().getId());
    }

    @Override
    protected void executeCommand() {
        try {
            addVmToDb();
            processImages();
            VmHandler.addVmInitToDB(getVm().getStaticData());
        } catch (RuntimeException e) {
            macPool.freeMacs(macsAdded);
            throw e;
        }

        setSucceeded(true);
    }

    private void reportExternalMacs() {
        final VM vm = getVm();
        final Set<String> externalMacAddresses = externalVmMacsFinder.findExternalMacAddresses(vm, getContext());
        if (CollectionUtils.isNotEmpty(externalMacAddresses)) {
            auditLog(createExternalMacsAuditLog(vm, externalMacAddresses), AuditLogType.MAC_ADDRESS_IS_EXTERNAL);
        }
    }

    private AuditLogableBase createExternalMacsAuditLog(VM vm, Set<String> externalMacs) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVmId(vm.getId());
        externalMacs.stream().forEach(mac -> logable.appendCustomValue("MACAddr", mac, COMMA_SEPARATOR));
        return logable;
    }

    protected abstract void processImages();

    protected void addVmToDb() {
        TransactionSupport.executeInNewTransaction(() -> {
            addVmStatic();
            addVmDynamic();
            addVmStatistics();
            addVmInterfaces();
            getCompensationContext().stateChanged();
            return null;
        });
    }

    protected void addVmStatic() {
        logImportEvents();
        getVm().getStaticData().setId(getVmId());
        getVm().getStaticData().setCreationDate(new Date());
        getVm().getStaticData().setClusterId(getParameters().getClusterId());
        getVm().getStaticData().setMinAllocatedMem(computeMinAllocatedMem());
        getVm().getStaticData().setQuotaId(getParameters().getQuotaId());

        // if "run on host" field points to a non existent vds (in the current cluster) -> remove field and continue
        if (!VmHandler.validateDedicatedVdsExistOnSameCluster(getVm().getStaticData(), null)) {
            getVm().setDedicatedVmForVdsList(Collections.<Guid>emptyList());
        }

        if (getVm().getOriginalTemplateGuid() != null && !VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVm().getOriginalTemplateGuid())) {
            // no need to check this for blank
            VmTemplate originalTemplate = getVmTemplateDao().get(getVm().getOriginalTemplateGuid());
            if (originalTemplate != null) {
                // in case the original template name has been changed in the meantime
                getVm().getStaticData().setOriginalTemplateName(originalTemplate.getName());
            }
        }

        if (getParameters().getCopyCollapse()) {
            getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        }
        getVmStaticDao().save(getVm().getStaticData());
        getCompensationContext().snapshotNewEntity(getVm().getStaticData());
    }

    protected Guid getDcId() {
        final Guid dcId = getParameters().getStoragePoolId();
        if (Guid.isNullOrEmpty(dcId)) {
            return getCluster().getStoragePoolId();
        } else {
            return dcId;
        }
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
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("FieldName", fieldName);
        logable.addCustomValue("VmName", vmName);
        logable.addCustomValue("FieldValue", fieldValue);
        auditLogDirector.log(logable, AuditLogType.VM_IMPORT_INFO);
    }

    protected void addVmInterfaces() {
        VmInterfaceManager vmInterfaceManager = new VmInterfaceManager(macPool);

        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getClusterId(),
                        getStoragePoolId(),
                        AuditLogType.IMPORTEXPORT_IMPORT_VM_INVALID_INTERFACES);

        List<VmNetworkInterface> nics = getVm().getInterfaces();

        vmInterfaceManager.sortVmNics(nics, getVm().getStaticData().getManagedDeviceMap());

        // If we import it as a new entity, then we allocate all MAC addresses in advance
        if (getParameters().isImportAsNewEntity()) {
            List<String> macAddresses = macPool.allocateMacAddresses(nics.size());
            for (int i = 0; i < nics.size(); ++i) {
                nics.get(i).setMacAddress(macAddresses.get(i));
            }
        } else {
            reportExternalMacs();
        }

        for (VmNetworkInterface iface : getVm().getInterfaces()) {
            initInterface(iface);
            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());

            vmInterfaceManager.add(iface,
                                   getCompensationContext(),
                                   !getParameters().isImportAsNewEntity(),
                                   getVm().getOs(),
                                   getEffectiveCompatibilityVersion());
            macsAdded.add(iface.getMacAddress());
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmName());
    }

    private void initInterface(VmNic iface) {
        if (iface.getId() == null) {
            iface.setId(Guid.newGuid());
        }
        fillMacAddressIfMissing(iface);
        iface.setVmTemplateId(null);
        iface.setVmId(getVmId());
    }

    private void fillMacAddressIfMissing(VmNic iface) {
        if (StringUtils.isEmpty(iface.getMacAddress())
                && macPool.getAvailableMacsCount() > 0) {
            iface.setMacAddress(macPool.allocateNewMac());
        }
    }

    private void addVmDynamic() {
        VmDynamic tempVar = createVmDynamic();
        getVmDynamicDao().save(tempVar);
        getCompensationContext().snapshotNewEntity(tempVar);
    }

    protected VmDynamic createVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(getVmId());
        vmDynamic.setStatus(VMStatus.ImageLocked);
        vmDynamic.setVmHost("");
        vmDynamic.setVmIp("");
        vmDynamic.setVmFQDN("");
        vmDynamic.setLastStopTime(new Date());
        vmDynamic.setAppList(getParameters().getVm().getAppList());
        return vmDynamic;
    }

    private void addVmStatistics() {
        VmStatistics stats = new VmStatistics();
        stats.setId(getVmId());
        getVmStatisticsDao().save(stats);
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
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Set<PermissionSubject> permissionSet = new HashSet<>();
        // Destination domains
        for (Guid storageId : imageToDestinationDomainMap.values()) {
            permissionSet.add(new PermissionSubject(storageId,
                    VdcObjectType.Storage,
                    getActionType().getActionGroup()));
        }
        return new ArrayList<>(permissionSet);
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

}
