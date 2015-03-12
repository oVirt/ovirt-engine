package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NName;
import org.ovirt.engine.core.common.validation.annotation.ValidSerialNumberPolicy;
import org.ovirt.engine.core.common.validation.annotation.ValidUri;
import org.ovirt.engine.core.common.validation.annotation.ValidVdsGroup;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@ValidVdsGroup(groups = { CreateEntity.class })
@ValidSerialNumberPolicy(groups = {CreateEntity.class, UpdateEntity.class})
public class VDSGroup extends IVdcQueryable implements Serializable, BusinessEntity<Guid>, HasStoragePool<Guid>,
        Nameable, Commented, HasSerialNumberPolicy, HasMigrationOptions {

    private static final long serialVersionUID = 5659359762655478095L;

    private Guid id;

    @NotNull(message = "VALIDATION.VDS_GROUP.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.CLUSTER_NAME_SIZE, message = "VALIDATION.VDS_GROUP.NAME.MAX",
            groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidI18NName(message = "VALIDATION.VDS_GROUP.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private String comment;

    @Size(max = BusinessEntitiesDefinitions.CLUSTER_CPU_NAME_SIZE)
    private String cpuName;

    private Guid storagePoolId;

    @Size(max = BusinessEntitiesDefinitions.DATACENTER_NAME_SIZE)
    private String storagePoolName;

    private int maxVdsMemoryOverCommit;

    private boolean enableBallooning;

    private boolean enableKsm;

    private boolean countThreadsAsCores;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    private String compatibilityVersion;

    private Version compatVersion;

    private boolean transparentHugepages;

    private MigrateOnErrorOptions migrateOnError;

    private boolean virtService;

    private boolean glusterService;

    private boolean tunnelMigration;

    private String emulatedMachine;

    private boolean trustedService;

    private boolean haReservation;

    private boolean optionalReasonRequired;

    private boolean maintenanceReasonRequired;

    private Guid clusterPolicyId;

    private String clusterPolicyName;

    @ValidUri(message = "VALIDATION.VDS_GROUP.SPICE_PROXY.HOSTNAME_OR_IP",
            groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.SPICE_PROXY_ADDR_SIZE)
    private String spiceProxy;

    private Map<String, String> clusterPolicyProperties;
    private boolean detectEmulatedMachine;

    private ArchitectureType architecture;
    private OptimizationType optimizationType;

    private SerialNumberPolicy serialNumberPolicy;
    private VDSGroupHostsAndVMs groupHostsAndVms;

    @Size(max = BusinessEntitiesDefinitions.VM_SERIAL_NUMBER_SIZE)
    private String customSerialNumber;

    private Set<VmRngDevice.Source> requiredRngSources;

    private FencingPolicy fencingPolicy;

    private Boolean autoConverge;

    private Boolean migrateCompressed;

    private String glusterTunedProfile;

    public VDSGroup() {
        migrateOnError = MigrateOnErrorOptions.YES;
        name = "";
        virtService = true;
        optimizationType = OptimizationType.NONE;
        requiredRngSources = new HashSet<>();
        fencingPolicy = new FencingPolicy();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid value) {
        id = value;
    }

    public void setVdsGroupId(Guid value) {
        setId(value);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        description = value;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String value) {
        comment = value;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String value) {
        cpuName = value;
    }

    @Override
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String value) {
        storagePoolName = value;
    }

    public int getMaxVdsMemoryOverCommit() {
        return maxVdsMemoryOverCommit;
    }

    public void setMaxVdsMemoryOverCommit(int value) {
        maxVdsMemoryOverCommit = value;
    }

    public boolean getCountThreadsAsCores() {
        return countThreadsAsCores;
    }

    public void setCountThreadsAsCores(boolean value) {
        countThreadsAsCores = value;
    }

    public Version getCompatibilityVersion() {
        return compatVersion;
    }

    public void setCompatibilityVersion(Version value) {
        compatibilityVersion = value.getValue();
        compatVersion = value;
    }

    public boolean getTransparentHugepages() {
        return transparentHugepages;
    }

    public void setTransparentHugepages(boolean value) {
        transparentHugepages = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setMigrateOnError(MigrateOnErrorOptions migrateOnError) {
        this.migrateOnError = migrateOnError;
    }

    public MigrateOnErrorOptions getMigrateOnError() {
        return migrateOnError;
    }

    public void setVirtService(boolean virtService) {
        this.virtService = virtService;
    }

    public boolean supportsVirtService() {
        return virtService;
    }

    public void setGlusterService(boolean glusterService) {
        this.glusterService = glusterService;
    }

    public boolean supportsGlusterService() {
        return glusterService;
    }

    public boolean isTunnelMigration() {
        return tunnelMigration;
    }

    public void setTunnelMigration(boolean value) {
        tunnelMigration = value;
    }

    public String getEmulatedMachine() {
        return emulatedMachine;
    }

    public void setEmulatedMachine(String emulatedMachine) {
        this.emulatedMachine = emulatedMachine;
    }

    public void setTrustedService(boolean trustedService) {
        this.trustedService = trustedService;
    }

    public boolean supportsTrustedService() {
        return trustedService;
    }

    public boolean supportsHaReservation() {
        return haReservation;
    }

    public void setHaReservation(boolean haReservation) {
        this.haReservation = haReservation;
    }

    public boolean isOptionalReasonRequired() {
        return optionalReasonRequired;
    }

    public void setOptionalReasonRequired(boolean optionalReasonRequired) {
        this.optionalReasonRequired = optionalReasonRequired;
    }

    public boolean isMaintenanceReasonRequired() {
        return maintenanceReasonRequired;
    }

    public void setMaintenanceReasonRequired(boolean maintenanceReasonRequired) {
        this.maintenanceReasonRequired = maintenanceReasonRequired;
    }

    public Guid getClusterPolicyId() {
        return clusterPolicyId;
    }

    public void setClusterPolicyId(Guid clusterPolicyId) {
        this.clusterPolicyId = clusterPolicyId;
    }

    public String getClusterPolicyName() {
        return clusterPolicyName;
    }

    public void setClusterPolicyName(String clusterPolicyName) {
        this.clusterPolicyName = clusterPolicyName;
    }

    public Map<String, String> getClusterPolicyProperties() {
        return clusterPolicyProperties;
    }

    public void setClusterPolicyProperties(Map<String, String> clusterPolicyProperties) {
        this.clusterPolicyProperties = clusterPolicyProperties;
    }

    public boolean isEnableKsm() {
        return enableKsm;
    }

    public void setEnableKsm(boolean enableKsm) {
        this.enableKsm = enableKsm;
    }

    public boolean isEnableBallooning() {
        return enableBallooning;
    }

    public void setEnableBallooning(boolean enableBallooning) {
        this.enableBallooning = enableBallooning;
    }

    public void setDetectEmulatedMachine(boolean detectEmulatedMachine) {
        this.detectEmulatedMachine = detectEmulatedMachine;
    }

    public boolean isDetectEmulatedMachine() {
        return detectEmulatedMachine;
    }

    public ArchitectureType getArchitecture() {
        return architecture;
    }

    public void setArchitecture (ArchitectureType architecture) {
        this.architecture = architecture;
    }

    public OptimizationType getOptimizationType() {
        return optimizationType;
    }

    public void setOptimizationType(OptimizationType optimizationType) {
        this.optimizationType = optimizationType;
    }

    public String getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(String spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    @Override
    public String getCustomSerialNumber() {
        return customSerialNumber;
    }

    @Override
    public void setCustomSerialNumber(String customSerialNumber) {
        this.customSerialNumber = customSerialNumber;
    }

    @Override
    public SerialNumberPolicy getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    @Override
    public void setSerialNumberPolicy(SerialNumberPolicy serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
    }

    public Set<VmRngDevice.Source> getRequiredRngSources() {
        return requiredRngSources;
    }

    public FencingPolicy getFencingPolicy() {
        return fencingPolicy;
    }

    public void setFencingPolicy(FencingPolicy fencingPolicy) {
        this.fencingPolicy = fencingPolicy;
    }

    public Boolean getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(Boolean autoConverge) {
        this.autoConverge = autoConverge;
    }

    public Boolean getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(Boolean migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    public VDSGroupHostsAndVMs getGroupHostsAndVms() {
        return groupHostsAndVms;
    }

    public void setGroupHostsAndVms(VDSGroupHostsAndVMs groupHostsAndVms) {
        this.groupHostsAndVms = groupHostsAndVms;
    }

    public String getGlusterTunedProfile() {
        return glusterTunedProfile;
    }

    public void setGlusterTunedProfile(String glusterTunedProfile) {
        this.glusterTunedProfile = glusterTunedProfile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (compatVersion == null ? 0 : compatVersion.hashCode());
        result = prime * result + (compatibilityVersion == null ? 0 : compatibilityVersion.hashCode());
        result = prime * result + (cpuName == null ? 0 : cpuName.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + maxVdsMemoryOverCommit;
        result = prime * result + (countThreadsAsCores ? 1231 : 1237);
        result = prime * result + (migrateOnError == null ? 0 : migrateOnError.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (storagePoolId == null ? 0 : storagePoolId.hashCode());
        result = prime * result + (storagePoolName == null ? 0 : storagePoolName.hashCode());
        result = prime * result + (transparentHugepages ? 1231 : 1237);
        result = prime * result + (virtService ? 1231 : 1237);
        result = prime * result + (glusterService ? 1231 : 1237);
        result = prime * result + (tunnelMigration ? 1231 : 1237);
        result = prime * result + (emulatedMachine == null ? 0 : emulatedMachine.hashCode());
        result = prime * result + (trustedService ? 1231 : 1237);
        result = prime * result + (haReservation ? 1231 : 1237);
        result = prime * result + (clusterPolicyName == null ? 0 : clusterPolicyName.hashCode());
        result = prime * result + (clusterPolicyProperties == null ? 0 : clusterPolicyProperties.hashCode());
        result = prime * result + (requiredRngSources == null ? 0 : requiredRngSources.hashCode());
        result = prime * result + (enableKsm ? 1231 : 1237);
        result = prime * result + (enableBallooning ? 1231 : 1237);
        result = prime * result + (optimizationType == null ? 0 : optimizationType.hashCode());
        result = prime * result + (serialNumberPolicy == null ? 0 : serialNumberPolicy.hashCode());
        result = prime * result + (customSerialNumber == null ? 0 : customSerialNumber.hashCode());
        result = prime * result + (fencingPolicy == null ? 0 : fencingPolicy.hashCode());
        result = prime * result + (autoConverge == null ? 0 : autoConverge.hashCode());
        result = prime * result + (migrateCompressed == null ? 0 : migrateCompressed.hashCode());
        result = prime * result + (glusterTunedProfile == null ? 0 : glusterTunedProfile.hashCode());
        result = prime * result + (maintenanceReasonRequired ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VDSGroup)) {
            return false;
        }
        VDSGroup other = (VDSGroup) obj;
        // *ATTENTION* when adding fields to this, please make sure that equals still works, if not this will
        // cause all kinds of havoc in the UI when clusters are refreshed.
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(compatVersion, other.compatVersion)
                && ObjectUtils.objectsEqual(compatibilityVersion, other.compatibilityVersion)
                && ObjectUtils.objectsEqual(cpuName, other.cpuName)
                && ObjectUtils.objectsEqual(description, other.description)
                && maxVdsMemoryOverCommit == other.maxVdsMemoryOverCommit
                && countThreadsAsCores == other.countThreadsAsCores
                && migrateOnError == other.migrateOnError
                && ObjectUtils.objectsEqual(name, other.name)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && ObjectUtils.objectsEqual(storagePoolName, other.storagePoolName)
                && transparentHugepages == other.transparentHugepages
                && virtService == other.virtService
                && glusterService == other.glusterService
                && tunnelMigration == other.tunnelMigration
                && ObjectUtils.objectsEqual(emulatedMachine, other.emulatedMachine)
                && trustedService == other.trustedService
                && haReservation == other.haReservation
                && ObjectUtils.objectsEqual(clusterPolicyId, other.clusterPolicyId)
                && ObjectUtils.objectsEqual(clusterPolicyName, other.clusterPolicyName)
                && ObjectUtils.objectsEqual(clusterPolicyProperties, other.clusterPolicyProperties)
                && enableKsm == other.enableKsm
                && enableBallooning == other.enableBallooning
                && detectEmulatedMachine == other.detectEmulatedMachine
                && optimizationType == other.optimizationType)
                && serialNumberPolicy == other.serialNumberPolicy
                && ObjectUtils.objectsEqual(customSerialNumber, other.customSerialNumber)
                && ObjectUtils.objectsEqual(requiredRngSources, other.requiredRngSources)
                && ObjectUtils.objectsEqual(fencingPolicy, other.fencingPolicy)
                && ObjectUtils.objectsEqual(autoConverge, other.autoConverge)
                && ObjectUtils.objectsEqual(migrateCompressed, other.migrateCompressed)
                && ObjectUtils.objectsEqual(glusterTunedProfile, other.glusterTunedProfile)
                && ObjectUtils.objectsEqual(maintenanceReasonRequired, other.maintenanceReasonRequired);
    }

}
