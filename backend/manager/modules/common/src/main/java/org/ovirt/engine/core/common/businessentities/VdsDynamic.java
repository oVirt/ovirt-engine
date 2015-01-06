package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

@Entity
@Table(name = "vds_dynamic")
@Cacheable(true)
@NamedQueries({
        @NamedQuery(name = "VdsDynamic.idByStatus", query = "select v.id from VdsDynamic v where v.status = :status")
})
public class VdsDynamic implements BusinessEntityWithStatus<Guid, VDSStatus> {
    private static final long serialVersionUID = -6010035855157006935L;

    @Id
    @Column(name = "vds_id")
    @Type(type = "org.ovirt.engine.core.dao.jpa.GuidUserType")
    private Guid id;

    @Column(name = "status")
    private int status;

    @Column(name = "external_status")
    @Enumerated(EnumType.ORDINAL)
    private ExternalStatus externalStatus;

    @Column(name = "cpu_cores")
    private Integer cpuCores;

    @Column(name = "cpu_threads")
    private Integer cpuThreads;

    @Column(name = "cpu_model")
    private String cpuModel;

    @Column(name = "online_cpus")
    private String onlineCpus;

    @Column(name = "cpu_speed_mh")
    private BigDecimal cpuSpeedMh;

    @Column(name = "if_total_speed")
    private String ifTotalSpeed;

    @Column(name = "kvm_enabled")
    private Boolean kvmEnabled;

    @Column(name = "physical_mem_mb")
    private Integer physicalMemMb;

    @Column(name = "mem_commited")
    private Integer memCommited;

    @Column(name = "vm_active")
    private Integer vmActive;

    @Column(name = "vm_count")
    private int vmCount;

    @Column(name = "vm_migrating")
    private Integer vmMigrating;

    @Column(name = "incoming_migrations")
    private int incomingMigrations;

    @Column(name = "outgoing_migrations")
    private int outgoingMigrations;

    @Column(name = "reserved_mem")
    private Integer reservedMem;

    @Column(name = "guest_overhead")
    private Integer guestOverhead;

    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "build_name")
    private String buildName;

    @Column(name = "previous_status")
    private int previousStatus;

    @Column(name = "cpu_flags")
    private String cpuFlags;

    @Column(name = "vms_cores_count")
    private Integer vmsCoresCount;

    /**
     * Cached (best effort) value of the number of CPUs scheduled
     * as part of not yet running VM.
     *
     * Do not use for anything critical as scheduling, ask for the
     * exact data using {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}.
     */
    @Column(name = "pending_vcpus_count")
    private Integer pendingVcpusCount;

    @Column(name = "cpu_sockets")
    private Integer cpuSockets;

    @Column(name = "net_config_dirty")
    private Boolean netConfigDirty;

    @Column(name = "supported_cluster_levels")
    private String supportedClusterLevels;

    @Column(name = "supported_engines")
    private String supportedEngines;

    @Column(name = "host_os")
    private String hostOs;

    @Column(name = "kvm_version")
    private String kvmVersion;

    @Column(name = "libvirt_version")
    @Type(type = "org.ovirt.engine.core.dao.jpa.RpmVersionUserType")
    private RpmVersion libvirtVersion;

    @Column(name = "spice_version")
    private String spiceVersion;

    @Column(name = "gluster_version")
    @Type(type = "org.ovirt.engine.core.dao.jpa.RpmVersionUserType")
    private RpmVersion glusterVersion;

    @Column(name = "kernel_version")
    private String kernelVersion;

    @Column(name = "iscsi_initiator_name")
    private String iScsiInitiatorName;

    @Column(name = "kdump_status")
    private int kdumpStatus;

    @Column(name = "is_live_snapshot_supported")
    private boolean liveSnapshotSupport;

    @Column(name = "is_live_merge_supported")
    private boolean liveMergeSupport;

    @Column(name = "transparent_hugepages_state")
    @Enumerated(EnumType.ORDINAL)
    private VdsTransparentHugePagesState transparentHugePagesState;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Type(type = "org.ovirt.engine.core.dao.jpa.HbaUserType")
    @Column(name = "hbas")
    private Map<String, List<Map<String, String>>> HBAs; /* Store the list of HBAs */

    @Column(name = "hooks")
    private String hooksStr;

    @Column(name = "hw_manufacturer")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwManufacturer;

    @Column(name = "hw_product_name")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwProductName;

    @Column(name = "hw_version")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwVersion;

    @Column(name = "hw_serial_number")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwSerialNumber;

    @Column(name = "hw_uuid")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwUUID;

    @Column(name = "hw_family")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwFamily;

    @Column(name = "non_operational_reason")
    private int nonOperationalReason;

    /**
     * Cached (best effort) amount of memory scheduled
     * as part of not yet running VM.
     *
     * Do not use for anything critical as scheduling, ask for the
     * exact data using {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}.
     */
    @Column(name = "pending_vmem_size")
    private Integer pendingVmemSize;

    @Column(name = "rpm_version")
    @Type(type = "org.ovirt.engine.core.dao.jpa.RpmVersionUserType")
    private RpmVersion rpmVersion;

    private transient HashSet<Version> supportedClusterVersionsSet = new HashSet<>();

    private transient HashSet<Version> supportedEngineVersionsSet = new HashSet<>();

    @Column(name = "selinux_enforce_mode")
    private Integer selinuxEnforceMode;

    /**
     * This flag is set to true if the host PM can be controlled
     * by policy. If a user triggered action puts the host
     * to maintenance or shuts it down, this flag is cleared.
     *
     * The flag should be re-set only by transitioning the host
     * back to Up state.
     *
     * In other words - all writes should behave as logical AND op,
     * except the one in InitVdsOnUp command.
     */
    @Column(name = "controlled_by_pm_policy")
    private boolean powerManagementControlledByPolicy;

    /**
     * comma separated list of emulated machines the host supports
     */
    @Column(name = "supported_emulated_machines")
    private String supportedEmulatedMachines;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vds_id", referencedColumnName = "vds_id")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<VdsNumaNode> numaNodeList;

    @Column(name = "auto_numa_balancing")
    private int autoNumaBalancing;

    @Column(name = "is_numa_supported")
    private boolean numaSupport;

    @Column(name = "supported_rng_sources")
    @Type(type = "org.ovirt.engine.core.dao.jpa.VmRngDeviceSourceUserType")
    private Set<VmRngDevice.Source> supportedRngSources;

    @Column(name = "maintenance_reason")
    private String maintenanceReason;

    @Column(name = "is_update_available")
    private boolean updateAvailable;
    // Set of additional features supported by the VDSM.
    private transient Set<String> additionalFeatures;

    @PostLoad
    protected void afterLoad() {
        supportedClusterVersionsSet = parseSupportedVersions(getSupportedClusterLevels());
        supportedEngineVersionsSet = parseSupportedVersions(getSupportedEngines());
    }

    @PrePersist
    protected void beforeStore() {
        supportedClusterLevels = supportedVersionsToString(supportedClusterVersionsSet);
        supportedEngines = supportedVersionsToString(supportedEngineVersionsSet);
    }

    @Column(name = "is_hostdev_enabled")
    private boolean hostDevicePassthroughEnabled;

    public VdsDynamic() {
        rpmVersion = new RpmVersion();
        libvirtVersion = new RpmVersion();
        glusterVersion = new RpmVersion();
        setExternalStatus(ExternalStatus.Ok);
        setStatus(VDSStatus.Unassigned);
        setPreviousStatus(VDSStatus.Unassigned);
        setNonOperationalReason(NonOperationalReason.NONE);
        cpuSpeedMh = BigDecimal.valueOf(0.0);
        memCommited = 0;
        reservedMem = 1024;
        pendingVcpusCount = 0;
        pendingVmemSize = 0;
        transparentHugePagesState = VdsTransparentHugePagesState.Never;
        vmCount = 0;
        vmsCoresCount = 0;
        guestOverhead = 0;
        powerManagementControlledByPolicy = false;
        setKdumpStatus(KdumpStatus.UNKNOWN);
        numaNodeList = new ArrayList<>();
        setAutoNumaBalancing(AutoNumaBalanceStatus.UNKNOWN);
        supportedRngSources = new HashSet<>();
        liveSnapshotSupport = true;  // usually supported, exceptional case if it isn't.
        liveMergeSupport = true;
        additionalFeatures = new HashSet<>();
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer value) {
        cpuCores = value;
    }

    public Integer getCpuThreads() {
        return cpuThreads;
    }

    public void setCpuThreads(Integer value) {
        cpuThreads = value;
    }

    public Integer getCpuSockets() {
        return cpuSockets;
    }

    public void setCpuSockets(Integer value) {
        cpuSockets = value;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String value) {
        cpuModel = value;
    }

    public String getOnlineCpus() {
        return onlineCpus;
    }

    public void setOnlineCpus(String value) {
        onlineCpus = value;
    }

    public Double getCpuSpeedMh() {
        return cpuSpeedMh.doubleValue();
    }

    public void setCpuSpeedMh(Double value) {
        cpuSpeedMh = BigDecimal.valueOf(value);
    }

    public String getIfTotalSpeed() {
        return ifTotalSpeed;
    }

    public void setIfTotalSpeed(String value) {
        ifTotalSpeed = value;
    }

    public Boolean getKvmEnabled() {
        return kvmEnabled;
    }

    public void setKvmEnabled(Boolean value) {
        kvmEnabled = value;
    }

    public Integer getMemCommited() {
        return memCommited;
    }

    public void setMemCommited(Integer value) {
        memCommited = value;
    }

    public Integer getPhysicalMemMb() {
        return physicalMemMb;
    }

    public void setPhysicalMemMb(Integer value) {
        physicalMemMb = value;
    }

    @Override
    public VDSStatus getStatus() {
        return VDSStatus.forValue(status);
    }

    @Override
    public void setStatus(VDSStatus value) {
        status = value.getValue();
    }

    public ExternalStatus getExternalStatus() {
        return externalStatus;
    }

    public void setExternalStatus(ExternalStatus externalStatus) {
        this.externalStatus = externalStatus;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Integer getVmActive() {
        return vmActive;
    }

    public void setVmActive(Integer value) {
        vmActive = value;
    }

    public int getVmCount() {
        return vmCount;
    }

    public void setVmCount(int value) {
        vmCount = value;
    }

    public Integer getVmsCoresCount() {
        return vmsCoresCount;
    }

    public void setVmsCoresCount(Integer value) {
        vmsCoresCount = value;
    }

    public Integer getVmMigrating() {
        return vmMigrating;
    }

    public void setVmMigrating(Integer value) {
        vmMigrating = value;
    }

    public int getIncomingMigrations() {
        return incomingMigrations;
    }

    public void setIncomingMigrations(int incomingMigrations) {
        this.incomingMigrations = incomingMigrations;
    }

    public int getOutgoingMigrations() {
        return outgoingMigrations;
    }

    public void setOutgoingMigrations(int outgoingMigrations) {
        this.outgoingMigrations = outgoingMigrations;
    }

    public Integer getReservedMem() {
        return reservedMem;
    }

    public void setReservedMem(Integer value) {
        reservedMem = value;
    }

    public Integer getGuestOverhead() {
        return guestOverhead;
    }

    public void setGuestOverhead(Integer value) {
        guestOverhead = value;
    }

    public VDSStatus getPreviousStatus() {
        return VDSStatus.forValue(previousStatus);
    }

    public void setPreviousStatus(VDSStatus value) {
        previousStatus = value.getValue();
    }

    public String getSoftwareVersion() {
       return softwareVersion;
    }

    public void setSoftwareVersion(String value) {
        softwareVersion = value;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String value) {
        versionName = value;
    }

    public String getCpuFlags() {
        return cpuFlags;
    }

    public void setCpuFlags(String value) {
        cpuFlags = value;
    }

    public Integer getPendingVcpusCount() {
        return pendingVcpusCount;
    }

    public void setPendingVcpusCount(Integer value) {
        pendingVcpusCount = value;
    }

    public int getPendingVmemSize() {
        return pendingVmemSize;
    }

    public void setPendingVmemSize(int value) {
        pendingVmemSize = value;
    }

    public Boolean getNetConfigDirty() {
        return netConfigDirty;
    }

    public void setNetConfigDirty(Boolean value) {
        netConfigDirty = value;
    }

    public String getSupportedClusterLevels() {
        return supportedClusterLevels;
    }

    public void setSupportedClusterLevels(String value) {
        supportedClusterLevels = value;
        supportedClusterVersionsSet = parseSupportedVersions(supportedClusterLevels);
    }

    public HashSet<Version> getSupportedClusterVersionsSet() {
        return supportedClusterVersionsSet;
    }

    public String getSupportedEngines() {
        return supportedEngines;
    }

    public void setSupportedEngines(String value) {
        supportedEngines = value;
        supportedEngineVersionsSet = parseSupportedVersions(supportedEngines);
    }

    public HashSet<Version> getSupportedEngineVersionsSet() {
        return supportedEngineVersionsSet;
    }

    public String getHardwareUUID() {
        return hwUUID;
    }

    public void setHardwareUUID(String value) {
        hwUUID = value;
    }

    public String getHardwareFamily() {
        return hwFamily;
    }

    public void setHardwareFamily(String value) {
        hwFamily = value;
    }

    public String getHardwareSerialNumber() {
        return hwSerialNumber;
    }

    public void setHardwareSerialNumber(String value) {
        hwSerialNumber = value;
    }

    public String getHardwareVersion() {
        return hwVersion;
    }

    public void setHardwareVersion(String value) {
        hwVersion = value;
    }

    public String getHardwareProductName() {
        return hwProductName;
    }

    public void setHardwareProductName(String value) {
        hwProductName = value;
    }

    public String getHardwareManufacturer() {
        return hwManufacturer;
    }

    public void setHardwareManufacturer(String value) {
        hwManufacturer = value;
    }

    /**
     * Used to parse a string containing concatenated list of versions, delimited by a comma.
     *
     * @param supportedVersions
     *            a string contains a concatenated list of supported versions
     * @return a set of the parsed versions, or an empty set if {@code supportedVersions} provided empty.
     * @throws RuntimeException
     *             thrown in case and parsing a version fails
     */
    private HashSet<Version> parseSupportedVersions(String supportedVersions) {
        HashSet<Version> parsedVersions = new HashSet<>();
        if (!StringHelper.isNullOrEmpty(supportedVersions)) {
            for (String ver : supportedVersions.split("[,]", -1)) {
                try {
                    parsedVersions.add(new Version(ver));
                } catch (Exception e) {
                    throw new RuntimeException(StringFormat.format("Could not parse supported version %s for vds %s",
                            ver,
                            getId()));
                }
            }
        }
        return parsedVersions;
    }

    private String supportedVersionsToString(Set<Version> versionSet) {
        if (versionSet == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Version version : versionSet) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(version.toString());
        }
        return sb.toString();
    }

    public String getHostOs() {
        return hostOs;
    }

    public void setHostOs(String value) {
        hostOs = value;
    }

    public String getKvmVersion() {
        return kvmVersion;
    }

    public void setKvmVersion(String value) {
        kvmVersion = value;
    }

    public RpmVersion getLibvirtVersion() {
        return libvirtVersion;
    }

    public void setLibvirtVersion(RpmVersion value) {
        libvirtVersion = value;
    }

    public String getSpiceVersion() {
        return spiceVersion;
    }

    public void setSpiceVersion(String value) {
        spiceVersion = value;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String value) {
        kernelVersion = value;
    }

    public RpmVersion getGlusterVersion() {
        return glusterVersion;
    }

    public void setGlusterVersion(RpmVersion value) {
        glusterVersion = value;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String value) {
        buildName = value;
    }

    public String getIScsiInitiatorName() {
        return iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value) {
        iScsiInitiatorName = value;
    }

    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return transparentHugePagesState;
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        transparentHugePagesState = value;
    }

    public Map<String, List<Map<String, String>>> getHBAs() {
        return HBAs;
    }

    public void setHBAs(Map<String, List<Map<String, String>>> HBAs) {
        this.HBAs = HBAs;
    }

    public void setHooksStr(String hooksStr) {
        this.hooksStr = hooksStr;
    }

    public String getHooksStr() {
        return hooksStr;
    }

    public NonOperationalReason getNonOperationalReason() {
        return NonOperationalReason.forValue(nonOperationalReason);
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason =
                (nonOperationalReason == null ? NonOperationalReason.NONE.getValue() : nonOperationalReason.getValue());
    }

    public boolean isPowerManagementControlledByPolicy() {
        return powerManagementControlledByPolicy;
    }

    public void setPowerManagementControlledByPolicy(boolean powerManagementControlledByPolicy) {
        this.powerManagementControlledByPolicy = powerManagementControlledByPolicy;
    }

    public KdumpStatus getKdumpStatus() {
        return KdumpStatus.valueOfNumber(kdumpStatus);
    }

    public void setKdumpStatus(KdumpStatus kdumpStatus) {
        this.kdumpStatus = kdumpStatus.getAsNumber();
    }

    public SELinuxMode getSELinuxEnforceMode() {
        return SELinuxMode.fromValue(selinuxEnforceMode);
    }

    public void setSELinuxEnforceMode(Integer value) {
        selinuxEnforceMode = value;
    }

    public boolean getLiveSnapshotSupport() {
        return liveSnapshotSupport;
    }

    public void setLiveSnapshotSupport(boolean liveSnapshotSupport) {
        this.liveSnapshotSupport = liveSnapshotSupport;
    }

    public boolean getLiveMergeSupport() {
        return liveMergeSupport;
    }

    public void setLiveMergeSupport(boolean liveMergeSupport) {
        this.liveMergeSupport = liveMergeSupport;
    }

    public List<VdsNumaNode> getNumaNodeList() {
        return numaNodeList;
    }

    public void setNumaNodeList(List<VdsNumaNode> numaNodeList) {
        this.numaNodeList = numaNodeList;
    }

    public AutoNumaBalanceStatus getAutoNumaBalancing() {
        return AutoNumaBalanceStatus.forValue(autoNumaBalancing);
    }

    public void setAutoNumaBalancing(AutoNumaBalanceStatus autoNumaBalancing) {
        this.autoNumaBalancing = autoNumaBalancing.getValue();
    }

    public boolean isNumaSupport() {
        return numaSupport;
    }

    public void setNumaSupport(boolean numaSupport) {
        this.numaSupport = numaSupport;
    }

    public Set<VmRngDevice.Source> getSupportedRngSources() {
        return supportedRngSources;
    }

    public void setVersion(RpmVersion value) {
        rpmVersion = value;
    }

    public RpmVersion getVersion() {
        return rpmVersion;
    }

    public String getSupportedEmulatedMachines() {
        return supportedEmulatedMachines;
    }

    public void setSupportedEmulatedMachines(String supportedEmulatedMachines) {
        this.supportedEmulatedMachines = supportedEmulatedMachines;
    }

    public String getMaintenanceReason() {
        return maintenanceReason;
    }

    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;

    }

    public Set<String> getAdditionalFeatures() {
        return additionalFeatures;
    }

    public void setAdditionalFeatures(Set<String> additionalFeatures) {
        this.additionalFeatures = additionalFeatures;
    }

    public void setHostDevicePassthroughEnabled(boolean hostDevicePassthroughEnabled) {
        this.hostDevicePassthroughEnabled = hostDevicePassthroughEnabled;
    }

    public boolean isHostDevicePassthroughEnabled() {
        return hostDevicePassthroughEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsDynamic)) {
            return false;
        }
        VdsDynamic other = (VdsDynamic) obj;
        return Objects.equals(id, other.id);
    }
}
