package org.ovirt.engine.core.common.businessentities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Template
 * <p>Blank template has id {@link org.ovirt.engine.core.compat.Guid#Empty}</p>
 */
public class VmTemplate extends VmBase implements BusinessEntityWithStatus<Guid, VmTemplateStatus>, InstanceType, ImageType {
    private static final long serialVersionUID = -5238366659716600486L;
    public static final int BASE_VERSION_NUMBER = 1;

    private int childCount;

    private VmTemplateStatus status;

    @EditableVmTemplateField
    private boolean sealed;

    private String clusterName;

    private Version clusterCompatibilityVersion;

    private Guid storagePoolId;

    private boolean trustedService;

    private String storagePoolName;

    @EditableVmTemplateField
    private Map<Guid, DiskImage> diskImageMap;

    private Map<Guid, DiskImage> diskTemplateMap;

    private VmEntityType templateType;

    private ArchitectureType clusterArch;

    private Guid baseTemplateId;

    private int templateVersionNumber;

    @EditableVmTemplateField
    private String templateVersionName;

    private double cachedActualSize = -1.0;

    @TransientField
    private BiosType clusterBiosType;

    private Map<VmExternalDataKind, String> vmExternalData;

    public VmTemplate() {
        setNiceLevel(0);
        setCpuShares(0);
        diskTemplateMap = new HashMap<>();
        status = VmTemplateStatus.OK;
        diskImageMap = new HashMap<>();
        templateType = VmEntityType.TEMPLATE;
        baseTemplateId = Guid.Empty;
        setTemplateVersionNumber(BASE_VERSION_NUMBER);
        setDescription(this.getDescription());
    }

    @EditableVmTemplateField
    private boolean disabled;

    public VmTemplate(int childCount, Date creationDate, String description, String comment,
                      int memSizeMb,
                      int maxMemorySize,
                      String name,
                      int numOfSockets, int cpuPerSocket, int threadsPerCpu, int osId,
                      Guid clusterId, Guid vmtGuid, int numOfMonitors, int status, int usbPolicy,
                      String timeZone, int niceLevel, int cpuShares, BootSequence defaultBootSequence,
                      VmType vmType, boolean smartcardEnabled, boolean deleteProtected, SsoMethod ssoMethod,
                      Boolean tunnelMigration, String vncKeyboardLayout, int minAllocatedMem, boolean stateless,
                      boolean runAndPause, Guid createdByUserId, VmEntityType templateType,
                      boolean autoStartup, int priority, DisplayType defaultDisplayType, String initrdUrl, String kernelUrl,
                      String kernelParams, Guid quotaId, List<Guid> dedicatedVmForVdsList, MigrationSupport migrationSupport,
                      boolean allowConsoleReconnect, String isoPath, Integer migrationDowntime,
                      Guid baseTemplateId, String templateVersionName,
                      SerialNumberPolicy serialNumberPolicy, String customSerialNumber,
                      boolean bootMenuEnabled, boolean spiceFIleTransferEnabled, boolean spiceCopyPasteEnabled,
                      Guid cpuProfileId,
                      Boolean autoConverge, Boolean migrateCompressed, Boolean migrateEncrypted,
                      String userDefinedProperties,
                      String predefinedProperties,
                      String customProperties,
                      String emulatedMachine, String customCpuName, boolean useHostCpuFlags,
                      Guid smallIconId,
                      Guid largeIconId,
                      int numOfIoThreads,
                      ConsoleDisconnectAction consoleDisconnectAction,
                      int consoleDisconnectActionDelay,
                      Version customCompatibilityVersion, Guid migrationPolicyId,
                      Guid leaseStorageDomainId,
                      VmResumeBehavior resumeBehavior,
                      boolean multiQueuesEnabled,
                      boolean useTscFrequency,
                      String cpuPinning,
                      int virtioScsiMultiQueues,
                      boolean balloonEnabled,
                      BiosType biosType,
                      CpuPinningPolicy cpuPinningPolicy) {
        super(name,
                vmtGuid,
                clusterId,
                osId,
                creationDate,
                description,
                comment,
                memSizeMb,
                maxMemorySize,
                numOfSockets,
                cpuPerSocket,
                threadsPerCpu,
                numOfMonitors,
                timeZone,
                vmType,
                UsbPolicy.forValue(usbPolicy),
                defaultBootSequence,
                niceLevel,
                cpuShares,
                priority,
                autoStartup,
                stateless,
                isoPath,
                OriginType.valueOf(Config.getValue(ConfigValues.OriginType)),
                kernelUrl,
                kernelParams,
                initrdUrl,
                quotaId,
                smartcardEnabled,
                deleteProtected,
                ssoMethod,
                tunnelMigration,
                vncKeyboardLayout,
                minAllocatedMem,
                runAndPause,
                createdByUserId,
                migrationSupport,
                allowConsoleReconnect,
                dedicatedVmForVdsList,
                defaultDisplayType,
                migrationDowntime,
                null,
                serialNumberPolicy,
                customSerialNumber,
                bootMenuEnabled,
                spiceFIleTransferEnabled,
                spiceCopyPasteEnabled,
                cpuProfileId,
                autoConverge,
                migrateCompressed,
                migrateEncrypted,
                userDefinedProperties,
                predefinedProperties,
                customProperties,
                emulatedMachine,
                customCpuName,
                useHostCpuFlags,
                smallIconId,
                largeIconId,
                numOfIoThreads,
                consoleDisconnectAction,
                consoleDisconnectActionDelay,
                customCompatibilityVersion,
                migrationPolicyId,
                leaseStorageDomainId,
                resumeBehavior,
                multiQueuesEnabled,
                useTscFrequency,
                cpuPinning,
                virtioScsiMultiQueues,
                balloonEnabled,
                biosType,
                cpuPinningPolicy);

        diskTemplateMap = new HashMap<>();
        diskImageMap = new HashMap<>();

        this.childCount = childCount;
        this.setStatus(VmTemplateStatus.forValue(status));
        setTemplateType(templateType);
        setBaseTemplateId(baseTemplateId);
        setTemplateVersionNumber(BASE_VERSION_NUMBER);
        setTemplateVersionName(templateVersionName);
        setDescription(this.getDescription());
    }

    public VmTemplate(VmTemplate template) {
        super(template);

        diskTemplateMap = new HashMap<>();
        diskImageMap = new HashMap<>();

        setChildCount(template.getChildCount());
        setStatus(template.getStatus());
        setTemplateType(template.getTemplateType());
        setBaseTemplateId(template.getBaseTemplateId());
        setTemplateVersionName(template.getTemplateVersionName());
        setTemplateVersionNumber(template.getTemplateVersionNumber());
        setDisabled(template.isDisabled());
        setDescription(this.getDescription());
    }

    public ArchitectureType getClusterArch() {
        return clusterArch;
    }

    public void setClusterArch(ArchitectureType clusterArch) {
        this.clusterArch = clusterArch;
    }

    public int getChildCount() {
        return this.childCount;
    }

    public void setChildCount(int value) {
        this.childCount = value;
    }

    @Override
    public VmTemplateStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(VmTemplateStatus value) {
        status = value;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String value) {
        clusterName = value;
    }

    public Version getClusterCompatibilityVersion() {
        return this.clusterCompatibilityVersion;
    }

    public void setClusterCompatibilityVersion(Version value) {
        this.clusterCompatibilityVersion = value;
    }

    public Version getCompatibilityVersion() {
        return getCustomCompatibilityVersion() != null ? getCustomCompatibilityVersion() : getClusterCompatibilityVersion();
    }

    public void setTrustedService(boolean trustedService) {
        this.trustedService = trustedService;
    }

    public boolean isTrustedService() {
        return trustedService;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String value) {
        storagePoolName = value;
    }

    @JsonIgnore
    public Map<Guid, DiskImage> getDiskTemplateMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public double getActualDiskSize() {
        if (cachedActualSize < 0.0) {
            cachedActualSize = getDiskImageMap().values().stream().mapToDouble(DiskImage::getActualSize).sum();
        }
        return cachedActualSize;
    }

    @JsonIgnore
    public Map<Guid, DiskImage> getDiskImageMap() {
        return diskImageMap;
    }

    public void setDiskImageMap(Map<Guid, DiskImage> value) {
        diskImageMap = value;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof VmTemplate) {
            returnValue = getId()
                    .equals(((VmTemplate) obj).getId());
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION_VM_TEMPLATE_NAME_MAX",
            groups = { Default.class, ImportEntity.class, ImportClonedEntity.class })
    @ValidI18NName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class, ImportEntity.class, ImportClonedEntity.class })
    public String getName() {
        return super.getName();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public VmEntityType getTemplateType() {
        return templateType;
    }

    public void setTemplateType(VmEntityType templateType) {
        this.templateType = templateType;
    }

    public Guid getBaseTemplateId() {
        return baseTemplateId;
    }

    public void setBaseTemplateId(Guid baseTemplateId) {
        this.baseTemplateId = baseTemplateId;
    }

    public int getTemplateVersionNumber() {
        return templateVersionNumber;
    }

    public void setTemplateVersionNumber(int templateVersionNumber) {
        this.templateVersionNumber = templateVersionNumber;
    }

    public String getTemplateVersionName() {
        return templateVersionName;
    }

    public void setTemplateVersionName(String templateVersionName) {
        this.templateVersionName = templateVersionName;
    }

    /**
     * Check if template is base template or version of a template
     * @return true if template is a base template, and false if its a version of a template
     */
    public boolean isBaseTemplate() {
        return getId().equals(getBaseTemplateId());
    }

    /**
     * Check if template is special 'Blank' template or its version.
     * <p>Blank template is recognized by id being {@link org.ovirt.engine.core.compat.Guid#Empty}</p>
     * @return true if this is Blank template, false otherwise
     */
    public boolean isBlank() {
        return Guid.Empty.equals(getBaseTemplateId());
    }

    @Override
    public boolean isManaged() {
        return getOrigin() != OriginType.KUBEVIRT;
    }

    public void setSealed(boolean isSealed) {
        this.sealed = isSealed;
    }

    public boolean isSealed() {
        return sealed;
    }

    public BiosType getClusterBiosType() {
        return clusterBiosType;
    }

    public void setClusterBiosType(BiosType clusterBiosType) {
        this.clusterBiosType = clusterBiosType;
    }

    public Map<VmExternalDataKind, String> getVmExternalData() {
        return vmExternalData;
    }

    public void setVmExternalData(Map<VmExternalDataKind, String> vmExternalData) {
        this.vmExternalData = vmExternalData;
    }

}
