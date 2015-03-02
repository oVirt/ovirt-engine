package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

public class VdsDynamic implements BusinessEntityWithStatus<Guid, VDSStatus> {
    private static final long serialVersionUID = -6010035855157006935L;

    private Guid id;

    private VDSStatus status;

    private Integer cpuCores;

    private Integer cpuThreads;

    private String cpuModel;

    private String onlineCpus;

    private BigDecimal cpuSpeedMh;

    private String ifTotalSpeed;

    private Boolean kvmEnabled;

    private Integer physicalMemMb;

    private Integer memCommited;

    private Integer vmActive;

    private int vmCount;

    private Integer vmMigrating;

    private int incomingMigrations;

    private int outgoingMigrations;

    private Integer reservedMem;

    private Integer guestOverhead;

    private String softwareVersion;

    private String versionName;

    private String buildName;

    private VDSStatus previousStatus;

    private String cpuFlags;

    private Integer vmsCoresCount;

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

    private String iScsiInitiatorName;

    private KdumpStatus kdumpStatus;

    private boolean liveSnapshotSupport;

    private boolean liveMergeSupport;

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

    public VdsDynamic() {
        rpmVersion = new RpmVersion();
        libvirtVersion = new RpmVersion();
        glusterVersion = new RpmVersion();
        status = VDSStatus.Unassigned;
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
        liveSnapshotSupport = true;  // usually supported, exceptional case if it isn't.
        liveMergeSupport = true;
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
        this.nonOperationalReason = (nonOperationalReason == null ? NonOperationalReason.NONE : nonOperationalReason);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (supportedClusterVersionsSet == null ? 0 : supportedClusterVersionsSet.hashCode());
        result = prime * result + (supportedEngineVersionsSet == null ? 0 : supportedEngineVersionsSet.hashCode());
        result = prime * result + (buildName == null ? 0 : buildName.hashCode());
        result = prime * result + (cpuCores == null ? 0 : cpuCores.hashCode());
        result = prime * result + (cpuThreads == null ? 0 : cpuThreads.hashCode());
        result = prime * result + (cpuFlags == null ? 0 : cpuFlags.hashCode());
        result = prime * result + (cpuModel == null ? 0 : cpuModel.hashCode());
        result = prime * result + (cpuSockets == null ? 0 : cpuSockets.hashCode());
        result = prime * result + (cpuSpeedMh == null ? 0 : cpuSpeedMh.hashCode());
        result = prime * result + (onlineCpus == null ? 0 : onlineCpus.hashCode());
        result = prime * result + (guestOverhead == null ? 0 : guestOverhead.hashCode());
        result = prime * result + (hooksStr == null ? 0 : hooksStr.hashCode());
        result = prime * result + (hostOs == null ? 0 : hostOs.hashCode());
        result = prime * result + (iScsiInitiatorName == null ? 0 : iScsiInitiatorName.hashCode());
        result = prime * result + (ifTotalSpeed == null ? 0 : ifTotalSpeed.hashCode());
        result = prime * result + (kernelVersion == null ? 0 : kernelVersion.hashCode());
        result = prime * result + (kvmEnabled == null ? 0 : kvmEnabled.hashCode());
        result = prime * result + (kvmVersion == null ? 0 : kvmVersion.hashCode());
        result = prime * result + (libvirtVersion == null ? 0 : libvirtVersion.hashCode());
        result = prime * result + (rpmVersion == null ? 0 : rpmVersion.hashCode());
        result = prime * result + (memCommited == null ? 0 : memCommited.hashCode());
        result = prime * result + (netConfigDirty == null ? 0 : netConfigDirty.hashCode());
        result = prime * result + (nonOperationalReason == null ? 0 : nonOperationalReason.hashCode());
        result = prime * result + (pendingVcpusCount == null ? 0 : pendingVcpusCount.hashCode());
        result = prime * result + (pendingVmemSize == null ? 0 : pendingVmemSize.hashCode());
        result = prime * result + (physicalMemMb == null ? 0 : physicalMemMb.hashCode());
        result = prime * result + (previousStatus == null ? 0 : previousStatus.hashCode());
        result = prime * result + (reservedMem == null ? 0 : reservedMem.hashCode());
        result = prime * result + (softwareVersion == null ? 0 : softwareVersion.hashCode());
        result = prime * result + (spiceVersion == null ? 0 : spiceVersion.hashCode());
        result = prime * result + (glusterVersion == null ? 0 : glusterVersion.hashCode());
        result = prime * result + (status == null ? 0 : status.hashCode());
        result = prime * result + (supportedClusterLevels == null ? 0 : supportedClusterLevels.hashCode());
        result = prime * result + (supportedEngines == null ? 0 : supportedEngines.hashCode());
        result = prime * result + (transparentHugePagesState == null ? 0 : transparentHugePagesState.hashCode());
        result = prime * result + (versionName == null ? 0 : versionName.hashCode());
        result = prime * result + (vmActive == null ? 0 : vmActive.hashCode());
        result = prime * result + vmCount;
        result = prime * result + (supportedRngSources == null ? 0 : supportedRngSources.hashCode());
        result = prime * result + (vmMigrating == null ? 0 : vmMigrating.hashCode());
        result = prime * result + incomingMigrations;
        result = prime * result + outgoingMigrations;
        result = prime * result + (vmsCoresCount == null ? 0 : vmsCoresCount.hashCode());
        result = prime * result + (hwManufacturer == null ? 0 : hwManufacturer.hashCode());
        result = prime * result + (hwProductName == null ? 0 : hwProductName.hashCode());
        result = prime * result + (hwVersion == null ? 0 : hwVersion.hashCode());
        result = prime * result + (hwSerialNumber == null ? 0 : hwSerialNumber.hashCode());
        result = prime * result + (hwUUID == null ? 0 : hwUUID.hashCode());
        result = prime * result + (hwFamily == null ? 0 : hwFamily.hashCode());
        result = prime * result + (HBAs == null ? 0 : HBAs.hashCode());
        result = prime * result + (powerManagementControlledByPolicy ? 0 : 1);
        result = prime * result + (kdumpStatus == null ? 0 : kdumpStatus.hashCode());
        result = prime * result + (selinuxEnforceMode == null ? 0 : selinuxEnforceMode.hashCode());
        result = prime * result + (numaNodeList == null ? 0 : numaNodeList.hashCode());
        result = prime * result + autoNumaBalancing.getValue();
        result = prime * result + (numaSupport ? 0 : 1);
        result = prime * result + (liveSnapshotSupport ? 0 : 1);
        result = prime * result + (liveMergeSupport ? 0 : 1);
        result = prime * result + (maintenanceReason == null ? 0 : maintenanceReason.hashCode());

        return result;
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
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(getSupportedClusterVersionsSet(), other.getSupportedClusterVersionsSet())
                && ObjectUtils.objectsEqual(supportedEngineVersionsSet, other.supportedEngineVersionsSet)
                && ObjectUtils.objectsEqual(buildName, other.buildName)
                && ObjectUtils.objectsEqual(cpuCores, other.cpuCores)
                && ObjectUtils.objectsEqual(cpuThreads, other.cpuThreads)
                && ObjectUtils.objectsEqual(cpuFlags, other.cpuFlags)
                && ObjectUtils.objectsEqual(cpuModel, other.cpuModel)
                && ObjectUtils.objectsEqual(cpuSockets, other.cpuSockets)
                && ObjectUtils.objectsEqual(cpuSpeedMh, other.cpuSpeedMh)
                && ObjectUtils.objectsEqual(onlineCpus, other.onlineCpus)
                && ObjectUtils.objectsEqual(guestOverhead, other.guestOverhead)
                && ObjectUtils.objectsEqual(hooksStr, other.hooksStr)
                && ObjectUtils.objectsEqual(hostOs, other.hostOs)
                && ObjectUtils.objectsEqual(iScsiInitiatorName, other.iScsiInitiatorName)
                && ObjectUtils.objectsEqual(ifTotalSpeed, other.ifTotalSpeed)
                && ObjectUtils.objectsEqual(kernelVersion, other.kernelVersion)
                && ObjectUtils.objectsEqual(kvmEnabled, other.kvmEnabled)
                && ObjectUtils.objectsEqual(kvmVersion, other.kvmVersion)
                && ObjectUtils.objectsEqual(libvirtVersion, other.libvirtVersion)
                && ObjectUtils.objectsEqual(rpmVersion, other.rpmVersion)
                && ObjectUtils.objectsEqual(memCommited, other.memCommited)
                && ObjectUtils.objectsEqual(netConfigDirty, other.netConfigDirty)
                && nonOperationalReason == other.nonOperationalReason
                && ObjectUtils.objectsEqual(pendingVcpusCount, other.pendingVcpusCount)
                && ObjectUtils.objectsEqual(pendingVmemSize, other.pendingVmemSize)
                && ObjectUtils.objectsEqual(physicalMemMb, other.physicalMemMb)
                && previousStatus == other.previousStatus
                && ObjectUtils.objectsEqual(reservedMem, other.reservedMem)
                && ObjectUtils.objectsEqual(getSoftwareVersion(), other.getSoftwareVersion())
                && ObjectUtils.objectsEqual(spiceVersion, other.spiceVersion)
                && ObjectUtils.objectsEqual(glusterVersion, other.glusterVersion)
                && status == other.status
                && ObjectUtils.objectsEqual(supportedClusterLevels, other.supportedClusterLevels)
                && ObjectUtils.objectsEqual(supportedEngines, other.supportedEngines)
                && transparentHugePagesState == other.transparentHugePagesState
                && ObjectUtils.objectsEqual(versionName, other.versionName)
                && ObjectUtils.objectsEqual(vmActive, other.vmActive)
                && vmCount == other.vmCount
                && ObjectUtils.objectsEqual(vmMigrating, other.vmMigrating)
                && incomingMigrations == other.incomingMigrations
                && outgoingMigrations == other.outgoingMigrations
                && ObjectUtils.objectsEqual(vmsCoresCount, other.vmsCoresCount)
                && ObjectUtils.objectsEqual(hwManufacturer, other.hwManufacturer)
                && ObjectUtils.objectsEqual(hwProductName, other.hwProductName)
                && ObjectUtils.objectsEqual(hwVersion, other.hwVersion)
                && ObjectUtils.objectsEqual(hwSerialNumber, other.hwSerialNumber)
                && ObjectUtils.objectsEqual(hwUUID, other.hwUUID)
                && ObjectUtils.objectsEqual(hwFamily, other.hwFamily)
                && ObjectUtils.objectsEqual(HBAs, other.HBAs)
                && powerManagementControlledByPolicy == other.powerManagementControlledByPolicy
                && kdumpStatus == other.kdumpStatus
                && ObjectUtils.objectsEqual(selinuxEnforceMode, other.selinuxEnforceMode)
                && ObjectUtils.objectsEqual(numaNodeList, other.numaNodeList)
                && autoNumaBalancing.getValue() == other.autoNumaBalancing.getValue()
                && numaSupport == other.numaSupport)
                && ObjectUtils.objectsEqual(supportedEmulatedMachines, other.supportedEmulatedMachines)
                && powerManagementControlledByPolicy == other.powerManagementControlledByPolicy
                && ObjectUtils.objectsEqual(supportedRngSources, other.supportedRngSources)
                && liveSnapshotSupport == other.liveSnapshotSupport
                && liveMergeSupport == other.liveMergeSupport
                && ObjectUtils.objectsEqual(maintenanceReason, other.maintenanceReason);
    }
}
