package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

public class VdsDynamic implements BusinessEntityWithStatus<Guid, VDSStatus> {
    private static final long serialVersionUID = -6010035855157006935L;

    private Guid id;

    private VDSStatus status;

    private ExternalStatus externalStatus;

    private Integer cpuCores;

    private Integer cpuThreads;

    private String cpuModel;

    private String onlineCpus;

    private BigDecimal cpuSpeedMh;

    private String ifTotalSpeed;

    private Boolean kvmEnabled;

    // The physical amount of memory the host has in MiB
    private Integer physicalMemMb;

    // The amount of memory already occupied by running VMs in MiB
    private Integer memCommited;

    private Integer vmActive;

    private int vmCount;

    private Integer vmMigrating;

    private int incomingMigrations;

    private int outgoingMigrations;

    // Memory reserved for the host OS in MiB
    private Integer reservedMem;

    // Memory overhead per each VM in MiB
    private Integer guestOverhead;

    private String softwareVersion;

    private String versionName;

    private String buildName;

    private VDSStatus previousStatus;

    private String cpuFlags;

    private Integer vmsCoresCount;

    /**
     * Cached (best effort) value of the number of CPUs scheduled
     * as part of not yet running VM.
     *
     * Do not use for anything critical as scheduling, ask for the
     * exact data using {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}.
     */
    private Integer pendingVcpusCount;

    private Integer cpuSockets;

    private Boolean netConfigDirty;

    private String supportedClusterLevels;

    private String supportedEngines;

    private String hostOs;

    private String kvmVersion;

    private RpmVersion libvirtVersion;

    private String spiceVersion;

    private RpmVersion glusterVersion;

    private String kernelVersion;

    private RpmVersion librbdVersion;

    private RpmVersion glusterfsCliVersion;

    private String iScsiInitiatorName;

    private KdumpStatus kdumpStatus;

    private VdsTransparentHugePagesState transparentHugePagesState;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private Map<String, List<Map<String, String>>> HBAs; /* Store the list of HBAs */

    private String hooksStr;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwManufacturer;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwProductName;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwVersion;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwSerialNumber;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwUUID;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private String hwFamily;

    private NonOperationalReason nonOperationalReason;

    /**
     * Cached (best effort) amount of memory scheduled
     * as part of not yet running VM.
     *
     * Do not use for anything critical as scheduling, ask for the
     * exact data using {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}.
     */
    private Integer pendingVmemSize;

    private RpmVersion rpmVersion;

    private HashSet<Version> supportedClusterVersionsSet;

    private HashSet<Version> supportedEngineVersionsSet;

    private SELinuxMode selinuxEnforceMode;

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
    private boolean powerManagementControlledByPolicy;

    /**
     * comma separated list of emulated machines the host supports
     */
    private String supportedEmulatedMachines;

    private List<VdsNumaNode> numaNodeList;

    private AutoNumaBalanceStatus autoNumaBalancing;

    private boolean numaSupport;

    private Set<VmRngDevice.Source> supportedRngSources;

    private String maintenanceReason;

    private boolean updateAvailable;
    // Set of additional features supported by the VDSM.
    private Set<String> additionalFeatures;

    private boolean hostDevicePassthroughEnabled;

    private String kernelArgs;

    private String prettyName;

    public VdsDynamic() {
        rpmVersion = new RpmVersion();
        libvirtVersion = new RpmVersion();
        glusterVersion = new RpmVersion();
        librbdVersion = new RpmVersion();
        glusterfsCliVersion = new RpmVersion();
        status = VDSStatus.Unassigned;
        externalStatus = ExternalStatus.Ok;
        previousStatus = VDSStatus.Unassigned;
        nonOperationalReason = NonOperationalReason.NONE;
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
        kdumpStatus = KdumpStatus.UNKNOWN;
        numaNodeList = new ArrayList<>();
        autoNumaBalancing = AutoNumaBalanceStatus.UNKNOWN;
        supportedRngSources = new HashSet<>();
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
        return status;
    }

    @Override
    public void setStatus(VDSStatus value) {
        status = value;
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
        return previousStatus;
    }

    public void setPreviousStatus(VDSStatus value) {
        previousStatus = value;
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
    }

    public HashSet<Version> getSupportedClusterVersionsSet() {
        if (supportedClusterVersionsSet == null) {
            supportedClusterVersionsSet = parseSupportedVersions(getSupportedClusterLevels());
        }
        return supportedClusterVersionsSet;
    }

    public String getSupportedEngines() {
        return supportedEngines;
    }

    public void setSupportedEngines(String value) {
        supportedEngines = value;
    }

    public HashSet<Version> getSupportedEngineVersionsSet() {
        if (supportedEngineVersionsSet == null) {
            supportedEngineVersionsSet = parseSupportedVersions(getSupportedEngines());
        }
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

    public RpmVersion getLibrbdVersion() {
        return librbdVersion;
    }

    public void setLibrbdVersion(RpmVersion value) {
        librbdVersion = value;
    }

    public RpmVersion getGlusterfsCliVersion() {
        return glusterfsCliVersion;
    }

    public void setGlusterfsCliVersion(RpmVersion value) {
        glusterfsCliVersion = value;
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
        return nonOperationalReason;
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = nonOperationalReason == null ? NonOperationalReason.NONE : nonOperationalReason;
    }

    public boolean isPowerManagementControlledByPolicy() {
        return powerManagementControlledByPolicy;
    }

    public void setPowerManagementControlledByPolicy(boolean powerManagementControlledByPolicy) {
        this.powerManagementControlledByPolicy = powerManagementControlledByPolicy;
    }

    public KdumpStatus getKdumpStatus() {
        return kdumpStatus;
    }

    public void setKdumpStatus(KdumpStatus kdumpStatus) {
        this.kdumpStatus = kdumpStatus;
    }

    public SELinuxMode getSELinuxEnforceMode() {
        return selinuxEnforceMode;
    }

    public void setSELinuxEnforceMode(Integer value) {
        selinuxEnforceMode = SELinuxMode.fromValue(value);
    }

    public List<VdsNumaNode> getNumaNodeList() {
        return numaNodeList;
    }

    public void setNumaNodeList(List<VdsNumaNode> numaNodeList) {
        this.numaNodeList = numaNodeList;
    }

    public AutoNumaBalanceStatus getAutoNumaBalancing() {
        return autoNumaBalancing;
    }

    public void setAutoNumaBalancing(AutoNumaBalanceStatus autoNumaBalancing) {
        this.autoNumaBalancing = autoNumaBalancing;
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

    public String getKernelArgs() {
        return kernelArgs;
    }

    public void setKernelArgs(String kernelArgs) {
        this.kernelArgs = kernelArgs;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public void setPrettyName(String prettyName) {
        this.prettyName = prettyName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                supportedClusterVersionsSet,
                supportedEngineVersionsSet,
                buildName,
                cpuCores,
                cpuThreads,
                cpuFlags,
                cpuModel,
                cpuSockets,
                cpuSpeedMh,
                onlineCpus,
                guestOverhead,
                hooksStr,
                hostOs,
                iScsiInitiatorName,
                ifTotalSpeed,
                kernelVersion,
                kvmEnabled,
                kvmVersion,
                libvirtVersion,
                rpmVersion,
                memCommited,
                netConfigDirty,
                nonOperationalReason,
                pendingVcpusCount,
                pendingVmemSize,
                physicalMemMb,
                previousStatus,
                reservedMem,
                softwareVersion,
                spiceVersion,
                glusterVersion,
                status,
                supportedClusterLevels,
                supportedEngines,
                transparentHugePagesState,
                versionName,
                vmActive,
                vmCount,
                supportedRngSources,
                vmMigrating,
                incomingMigrations,
                outgoingMigrations,
                vmsCoresCount,
                hwManufacturer,
                hwProductName,
                hwVersion,
                hwSerialNumber,
                hwUUID,
                hwFamily,
                HBAs,
                powerManagementControlledByPolicy,
                kdumpStatus,
                selinuxEnforceMode,
                numaNodeList,
                autoNumaBalancing,
                numaSupport,
                additionalFeatures,
                maintenanceReason,
                updateAvailable,
                hostDevicePassthroughEnabled,
                kernelArgs,
                prettyName
        );
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
        return Objects.equals(id, other.id)
                && Objects.equals(getSupportedClusterVersionsSet(), other.getSupportedClusterVersionsSet())
                && Objects.equals(supportedEngineVersionsSet, other.supportedEngineVersionsSet)
                && Objects.equals(buildName, other.buildName)
                && Objects.equals(cpuCores, other.cpuCores)
                && Objects.equals(cpuThreads, other.cpuThreads)
                && Objects.equals(cpuFlags, other.cpuFlags)
                && Objects.equals(cpuModel, other.cpuModel)
                && Objects.equals(cpuSockets, other.cpuSockets)
                && Objects.equals(cpuSpeedMh, other.cpuSpeedMh)
                && Objects.equals(onlineCpus, other.onlineCpus)
                && Objects.equals(guestOverhead, other.guestOverhead)
                && Objects.equals(hooksStr, other.hooksStr)
                && Objects.equals(hostOs, other.hostOs)
                && Objects.equals(iScsiInitiatorName, other.iScsiInitiatorName)
                && Objects.equals(ifTotalSpeed, other.ifTotalSpeed)
                && Objects.equals(kernelVersion, other.kernelVersion)
                && Objects.equals(kvmEnabled, other.kvmEnabled)
                && Objects.equals(kvmVersion, other.kvmVersion)
                && Objects.equals(libvirtVersion, other.libvirtVersion)
                && Objects.equals(rpmVersion, other.rpmVersion)
                && Objects.equals(memCommited, other.memCommited)
                && Objects.equals(netConfigDirty, other.netConfigDirty)
                && nonOperationalReason == other.nonOperationalReason
                && Objects.equals(pendingVcpusCount, other.pendingVcpusCount)
                && Objects.equals(pendingVmemSize, other.pendingVmemSize)
                && Objects.equals(physicalMemMb, other.physicalMemMb)
                && previousStatus == other.previousStatus
                && Objects.equals(reservedMem, other.reservedMem)
                && Objects.equals(getSoftwareVersion(), other.getSoftwareVersion())
                && Objects.equals(spiceVersion, other.spiceVersion)
                && Objects.equals(glusterVersion, other.glusterVersion)
                && status == other.status
                && Objects.equals(supportedClusterLevels, other.supportedClusterLevels)
                && Objects.equals(supportedEngines, other.supportedEngines)
                && transparentHugePagesState == other.transparentHugePagesState
                && Objects.equals(versionName, other.versionName)
                && Objects.equals(vmActive, other.vmActive)
                && vmCount == other.vmCount
                && Objects.equals(vmMigrating, other.vmMigrating)
                && incomingMigrations == other.incomingMigrations
                && outgoingMigrations == other.outgoingMigrations
                && Objects.equals(vmsCoresCount, other.vmsCoresCount)
                && Objects.equals(hwManufacturer, other.hwManufacturer)
                && Objects.equals(hwProductName, other.hwProductName)
                && Objects.equals(hwVersion, other.hwVersion)
                && Objects.equals(hwSerialNumber, other.hwSerialNumber)
                && Objects.equals(hwUUID, other.hwUUID)
                && Objects.equals(hwFamily, other.hwFamily)
                && Objects.equals(HBAs, other.HBAs)
                && powerManagementControlledByPolicy == other.powerManagementControlledByPolicy
                && kdumpStatus == other.kdumpStatus
                && Objects.equals(selinuxEnforceMode, other.selinuxEnforceMode)
                && Objects.equals(numaNodeList, other.numaNodeList)
                && autoNumaBalancing.getValue() == other.autoNumaBalancing.getValue()
                && numaSupport == other.numaSupport
                && Objects.equals(supportedEmulatedMachines, other.supportedEmulatedMachines)
                && powerManagementControlledByPolicy == other.powerManagementControlledByPolicy
                && Objects.equals(supportedRngSources, other.supportedRngSources)
                && Objects.equals(maintenanceReason, other.maintenanceReason)
                && updateAvailable == other.updateAvailable
                && Objects.equals(additionalFeatures, other.additionalFeatures)
                && Objects.equals(kernelArgs, other.kernelArgs)
                && Objects.equals(hostDevicePassthroughEnabled, other.hostDevicePassthroughEnabled)
                && Objects.equals(prettyName, other.prettyName);
    }
}
