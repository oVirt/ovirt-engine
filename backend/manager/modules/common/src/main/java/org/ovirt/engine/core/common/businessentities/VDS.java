package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

public class VDS implements Queryable, BusinessEntityWithStatus<Guid, VDSStatus>, HasStoragePool, HasErrata, Commented, Nameable, Cloneable {
    private static final long serialVersionUID = -7893976203379789926L;
    private VdsStatic vdsStatic;
    private VdsDynamic vdsDynamic;
    private VdsStatistics vdsStatistics;
    private ArrayList<VdsNetworkInterface> interfaces;
    private Set<String> networkNames;
    private String activeNic;
    private boolean balloonEnabled;
    private boolean countThreadsAsCores;
    private List<FenceAgent> fenceAgents;
    private VdsSpmStatus spmStatus;
    private Version clusterCompatibilityVersion;
    private String clusterName;
    private String clusterFlags;
    private String clusterVerb;
    private String clusterDescription;
    private String clusterCpuName;
    private Boolean clusterVirtService;
    private Guid storagePoolId;
    private String storagePoolName;
    private int maxVdsMemoryOverCommit;
    private ArrayList<VDSDomainsData> privateDomains;
    private Boolean clusterGlusterService;
    private Double imagesLastCheck;
    private Double imagesLastDelay;
    private ServerCpu cpuName;
    private Integer vdsSpmId;
    /**
     * Flags that are required by the CPU configuration but are missing on the host
     */
    private Set<String> cpuFlagsMissing;
    /**
     * Cpu names that are supported by this host. The list is ordered - see CpuFlagsManager for details.
     */
    private List<String> supportedCpus;
    /**
     * Maximal memory available for scheduling a new VM in MiB. This is a dynamic field
     * that is recomputed every time one of the inputs changes.
     *
     * See VDS#calculateFreeSchedulingMemoryCache for the current algorithm.
     */
    private float maxSchedulingMemory;
    private boolean hostedEngineHost;
    private boolean fencingEnabled;
    private PeerStatus glusterPeerStatus;

    /**
     * This map holds the disk usage reported by the host. The mapping is path to usage (in MB).
     */
    private Map<String, Long> localDisksUsage;
    private boolean networkOperationInProgress;
    private boolean isDefaultRouteRoleNetworkAttached;

    private boolean isClusterSmtDisabled;

    public VDS() {
        vdsStatic = new VdsStatic();
        vdsDynamic = new VdsDynamic();
        vdsStatistics = new VdsStatistics();
        storagePoolId = Guid.Empty;
        spmStatus = VdsSpmStatus.None;
        interfaces = new ArrayList<>();
        networkNames = new HashSet<>();
        fenceAgents = new LinkedList<>();
        cpuFlagsMissing = new HashSet<>();
        supportedCpus = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                vdsStatic,
                cpuName,
                spmStatus,
                imagesLastCheck,
                imagesLastDelay,
                interfaces,
                networkNames,
                maxVdsMemoryOverCommit,
                privateDomains,
                vdsSpmId,
                storagePoolId,
                storagePoolName,
                clusterCompatibilityVersion,
                clusterCpuName,
                clusterDescription,
                clusterName,
                clusterFlags,
                clusterVerb,
                clusterVirtService,
                clusterGlusterService,
                balloonEnabled,
                countThreadsAsCores,
                glusterPeerStatus,
                networkOperationInProgress,
                isDefaultRouteRoleNetworkAttached
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VDS)) {
            return false;
        }
        VDS other = (VDS) obj;
        return Objects.equals(vdsStatic, other.vdsStatic)
                && Objects.equals(cpuName, other.cpuName)
                && spmStatus == other.spmStatus
                && Objects.equals(imagesLastCheck, other.imagesLastCheck)
                && Objects.equals(imagesLastDelay, other.imagesLastDelay)
                && Objects.equals(interfaces, other.interfaces)
                && Objects.equals(networkNames, other.networkNames)
                && maxVdsMemoryOverCommit == other.maxVdsMemoryOverCommit
                && balloonEnabled == other.balloonEnabled
                && Objects.equals(privateDomains, other.privateDomains)
                && Objects.equals(vdsSpmId, other.vdsSpmId)
                && Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(storagePoolName, other.storagePoolName)
                && Objects.equals(clusterCompatibilityVersion, other.clusterCompatibilityVersion)
                && Objects.equals(clusterCpuName, other.clusterCpuName)
                && Objects.equals(clusterDescription, other.clusterDescription)
                && Objects.equals(clusterName, other.clusterName)
                && Objects.equals(clusterFlags, other.clusterFlags)
                && Objects.equals(clusterVerb, other.clusterVerb)
                && Objects.equals(clusterVirtService, other.clusterVirtService)
                && Objects.equals(clusterGlusterService, other.clusterGlusterService)
                && glusterPeerStatus == other.glusterPeerStatus
                && networkOperationInProgress == other.networkOperationInProgress
                && isDefaultRouteRoleNetworkAttached == other.isDefaultRouteRoleNetworkAttached
                && isClusterSmtDisabled == other.isClusterSmtDisabled;
    }


    public VDS clone() {
        VDS vds = new VDS();
        vds.setClusterId(getClusterId());
        vds.setClusterCpuName(getClusterCpuName());
        vds.setCpuName(getCpuName());
        vds.setClusterDescription(getClusterDescription());
        vds.setId(getId());
        vds.setVdsName(getName());
        vds.setHostName(getHostName());
        vds.setComment(getComment());
        vds.setPort(getPort());
        vds.setSshPort(getSshPort());
        vds.setSshUsername(getSshUsername());
        vds.setStatus(getStatus());
        vds.setExternalStatus(getExternalStatus());
        vds.setHardwareManufacturer(getHardwareManufacturer());
        vds.setHardwareProductName(getHardwareProductName());
        vds.setHardwareVersion(getHardwareVersion());
        vds.setHardwareSerialNumber(getHardwareSerialNumber());
        vds.setHardwareUUID(getHardwareUUID());
        vds.setHardwareFamily(getHardwareFamily());
        vds.setCpuCores(getCpuCores());
        vds.setCpuThreads(getCpuThreads());
        vds.setCpuModel(getCpuModel());
        vds.setOnlineCpus(getOnlineCpus());
        vds.setCpuSpeedMh(getCpuSpeedMh());
        vds.setIfTotalSpeed(getIfTotalSpeed());
        vds.setKvmEnabled(getKvmEnabled());
        vds.setPhysicalMemMb(getPhysicalMemMb());
        vds.setCpuIdle(getCpuIdle());
        vds.setCpuLoad(getCpuLoad());
        vds.setCpuSys(getCpuSys());
        vds.setCpuUser(getCpuUser());
        vds.setMemCommited(getMemCommited());
        vds.setVmActive(getVmActive());
        vds.setVmCount(getVmCount());
        vds.setVmMigrating(getVmMigrating());
        vds.setUsageMemPercent(getUsageMemPercent());
        vds.setUsageCpuPercent(getUsageCpuPercent());
        vds.setUsageNetworkPercent(getUsageNetworkPercent());
        vds.setReservedMem(getReservedMem());
        vds.setBootTime(getBootTime());
        vds.setGuestOverhead(getGuestOverhead());
        vds.setPreviousStatus(getPreviousStatus());
        vds.setMemShared(getMemShared());
        vds.setSoftwareVersion(getSoftwareVersion());
        vds.setVersionName(getVersionName());
        vds.setVersion(getVersion());
        vds.setServerSslEnabled(isServerSslEnabled());
        vds.setCpuFlags(getCpuFlags());
        vds.setNetConfigDirty(getNetConfigDirty());
        vds.setPmEnabled(isPmEnabled());
        vds.setPmKdumpDetection(isPmKdumpDetection());
        vds.setConsoleAddress(getConsoleAddress());
        vds.setHBAs(getHBAs());
        vds.setVdsSpmPriority(getVdsSpmPriority());
        vds.setOtpValidity(getOtpValidity());
        vds.setKernelVersion(getKernelVersion());
        vds.setKvmVersion(getKvmVersion());
        vds.setLibvirtVersion(getLibvirtVersion());
        vds.setGlusterfsCliVersion(getGlusterfsCliVersion());
        vds.setGlusterVersion(getGlusterVersion());
        vds.setLibrbdVersion(getLibrbdVersion());
        vds.setHooksStr(getHooksStr());
        vds.setActiveNic(getActiveNic());
        vds.setPowerManagementControlledByPolicy(isPowerManagementControlledByPolicy());
        vds.setDisablePowerManagementPolicy(isDisablePowerManagementPolicy());
        vds.setHighlyAvailableScore(getHighlyAvailableScore());
        vds.setHighlyAvailableIsConfigured(getHighlyAvailableIsConfigured());
        vds.setHighlyAvailableIsActive(getHighlyAvailableIsActive());
        vds.setHighlyAvailableGlobalMaintenance(getHighlyAvailableGlobalMaintenance());
        vds.setHighlyAvailableLocalMaintenance(getHighlyAvailableLocalMaintenance());
        vds.setBalloonEnabled(isBalloonEnabled());
        vds.setNumaNodeList(getNumaNodeList());
        vds.setAutoNumaBalancing(getAutoNumaBalancing());
        vds.setFenceAgents(getFenceAgents());
        vds.setClusterCompatibilityVersion(getClusterCompatibilityVersion());
        vds.setUpdateAvailable(isUpdateAvailable());
        vds.setHostDevicePassthroughEnabled(isHostDevicePassthroughEnabled());
        vds.setHostedEngineHost(isHostedEngineHost());
        vds.setCurrentKernelCmdline(getCurrentKernelCmdline());
        vds.setLastStoredKernelCmdline(getLastStoredKernelCmdline());
        vds.setKernelCmdlineParsable(isKernelCmdlineParsable());
        vds.setKernelCmdlineBlacklistNouveau(isKernelCmdlineBlacklistNouveau());
        vds.setKernelCmdlineIommu(isKernelCmdlineIommu());
        vds.setKernelCmdlineKvmNested(isKernelCmdlineKvmNested());
        vds.setKernelCmdlinePciRealloc(isKernelCmdlinePciRealloc());
        vds.setKernelCmdlineFips(isKernelCmdlineFips());
        vds.setKernelCmdlineSmtDisabled(isKernelCmdlineSmtDisabled());
        vds.setKernelCmdlineUnsafeInterrupts(isKernelCmdlineUnsafeInterrupts());
        vds.setGlusterPeerStatus(getGlusterPeerStatus());
        vds.setKernelFeatures(getKernelFeatures());
        vds.setOvsVersion(getOvsVersion());
        vds.setDomains(getDomains());

        DnsResolverConfiguration originalDnsResolverConfiguration = getReportedDnsResolverConfiguration();
        if (originalDnsResolverConfiguration != null) {
            vds.setReportedDnsResolverConfiguration(new DnsResolverConfiguration(originalDnsResolverConfiguration));
        }
        vds.setInFenceFlow(isInFenceFlow());
        vds.setNetworkOperationInProgress(isNetworkOperationInProgress());
        vds.setIsDefaultRouteRoleNetworkAttached(isDefaultRouteRoleNetworkAttached());
        vds.setClusterSmtDisabled(isClusterSmtDisabled());
        vds.setTscFrequency(getTscFrequency());
        vds.setTscScalingEnabled(isTscScalingEnabled());
        vds.setCpuFlagsMissing(getCpuFlagsMissing());
        vds.setSupportedCpus(getSupportedCpus());
        vds.setBootUuid(getBootUuid());
        vds.setCdChangePdiv(isCdChangePdiv());
        vds.setOvnConfigured(isOvnConfigured());
        return vds;
    }

    public Version getClusterCompatibilityVersion() {
        return clusterCompatibilityVersion;
    }

    public boolean isContainingHooks() {
        // As VDSM reports the hooks in XMLRPCStruct that represents map of maps, we can assume that the string form of
        // the map begins with
        // { and ends with }
        String hooksStr = getHooksStr();
        return hooksStr != null && hooksStr.length() > 2;
    }

    public void setHooksStr(String hooksStr) {
        getDynamicData().setHooksStr(hooksStr);
    }

    public String getHooksStr() {
        return getDynamicData().getHooksStr();
    }

    public void setClusterCompatibilityVersion(Version value) {
        clusterCompatibilityVersion = value;
    }

    public Guid getClusterId() {
        return vdsStatic.getClusterId();
    }

    public void setClusterId(Guid value) {
        vdsStatic.setClusterId(value);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String value) {
        clusterName = value;
    }

    public String getClusterFlags() {
        return clusterFlags;
    }

    public void setClusterFlags(String clusterFlags) {
        this.clusterFlags = clusterFlags;
    }

    public String getClusterVerb() {
        return clusterVerb;
    }

    public void setClusterVerb(String clusterVerb) {
        this.clusterVerb = clusterVerb;
    }

    public String getClusterDescription() {
        return clusterDescription;
    }

    public void setClusterDescription(String value) {
        clusterDescription = value;
    }

    public String getClusterCpuName() {
        return clusterCpuName;
    }

    public void setClusterCpuName(String value) {
        clusterCpuName = value;
    }

    public Boolean getClusterSupportsVirtService() {
        return clusterVirtService;
    }

    public void setClusterSupportsVirtService(Boolean value) {
        clusterVirtService = value;
    }

    public Boolean getClusterSupportsGlusterService() {
        return clusterGlusterService;
    }

    public void setClusterSupportsGlusterService(Boolean value) {
        clusterGlusterService = value;
    }

    @Override
    public Guid getId() {
        return vdsStatic.getId();
    }

    @Override
    public void setId(Guid value) {
        vdsStatic.setId(value);
        vdsDynamic.setId(value);
        vdsStatistics.setId(value);
    }

    @Override
    public String getName() {
        return vdsStatic.getName();
    }

    public void setVdsName(String value) {
        vdsStatic.setName(value);
    }

    public String getUniqueId() {
        return vdsStatic.getUniqueID();
    }

    public void setUniqueId(String value) {
        vdsStatic.setUniqueID(value);
    }

    public String getHostName() {
        return vdsStatic.getHostName();
    }

    public void setHostName(String value) {
        vdsStatic.setHostName(value);
    }

    @Override
    public String getComment() {
        return vdsStatic.getComment();
    }

    @Override
    public void setComment(String value) {
        vdsStatic.setComment(value);
    }

    public int getPort() {
        return vdsStatic.getPort();
    }

    public void setPort(int value) {
        vdsStatic.setPort(value);
    }

    public int getSshPort() {
        return vdsStatic.getSshPort();
    }

    public void setSshPort(int value) {
        vdsStatic.setSshPort(value);
    }

    public String getSshUsername() {
        return vdsStatic.getSshUsername();
    }

    public void setSshUsername(String value) {
        vdsStatic.setSshUsername(value);
    }

    public boolean isServerSslEnabled() {
        return vdsStatic.isServerSslEnabled();
    }

    public void setServerSslEnabled(boolean value) {
        vdsStatic.setServerSslEnabled(value);
    }

    public VDSType getVdsType() {
        return vdsStatic.getVdsType();
    }

    public void setVdsType(VDSType value) {
        vdsStatic.setVdsType(value);
    }

    @Override
    public VDSStatus getStatus() {
        return vdsDynamic.getStatus();
    }

    @Override
    public void setStatus(VDSStatus value) {
        vdsDynamic.setStatus(value);
    }

    public ExternalStatus getExternalStatus() {
        return vdsDynamic.getExternalStatus();
    }

    public  void setExternalStatus(ExternalStatus externalStatus) {
        vdsDynamic.setExternalStatus(externalStatus);
    }

    public Integer getCpuCores() {
        return vdsDynamic.getCpuCores();
    }

    public void setCpuCores(Integer value) {
        vdsDynamic.setCpuCores(value);
    }

    public Integer getCpuThreads() {
        return vdsDynamic.getCpuThreads();
    }

    public void setCpuThreads(Integer value) {
        vdsDynamic.setCpuThreads(value);
    }

    public String getHardwareUUID() {
        return vdsDynamic.getHardwareUUID();
    }

    public String getHardwareManufacturer() {
        return vdsDynamic.getHardwareManufacturer();
    }

    public String getHardwareFamily() {
        return vdsDynamic.getHardwareFamily();
    }

    public String getHardwareSerialNumber() {
        return vdsDynamic.getHardwareSerialNumber();
    }

    public String getHardwareProductName() {
        return vdsDynamic.getHardwareProductName();
    }

    public String getHardwareVersion() {
        return vdsDynamic.getHardwareVersion();
    }

    public void setHardwareUUID(String value) {
        vdsDynamic.setHardwareUUID(value);
    }

    public void setHardwareFamily(String value) {
        vdsDynamic.setHardwareFamily(value);
    }

    public void setHardwareSerialNumber(String value) {
        vdsDynamic.setHardwareSerialNumber(value);
    }

    public void setHardwareVersion(String value) {
        vdsDynamic.setHardwareVersion(value);
    }

    public void setHardwareProductName(String value) {
        vdsDynamic.setHardwareProductName(value);
    }

    public void setHardwareManufacturer(String value) {
        vdsDynamic.setHardwareManufacturer(value);
    }

    public Integer getCpuSockets() {
        return vdsDynamic.getCpuSockets();
    }

    public void setCpuSockets(Integer value) {
        vdsDynamic.setCpuSockets(value);
    }

    public String getCpuModel() {
        return vdsDynamic.getCpuModel();
    }

    public void setCpuModel(String value) {
        vdsDynamic.setCpuModel(value);
    }

    public String getOnlineCpus() {
        return vdsDynamic.getOnlineCpus();
    }

    public void setOnlineCpus(String value) {
        vdsDynamic.setOnlineCpus(value);
    }

    public Double getCpuSpeedMh() {
        return vdsDynamic.getCpuSpeedMh();
    }

    public void setCpuSpeedMh(Double value) {
        vdsDynamic.setCpuSpeedMh(value);
    }

    public String getIfTotalSpeed() {
        return vdsDynamic.getIfTotalSpeed();
    }

    public void setIfTotalSpeed(String value) {
        vdsDynamic.setIfTotalSpeed(value);
    }

    public Boolean getKvmEnabled() {
        return vdsDynamic.getKvmEnabled();
    }

    public void setKvmEnabled(Boolean value) {
        vdsDynamic.setKvmEnabled(value);
    }

    public Integer getPhysicalMemMb() {
        return vdsDynamic.getPhysicalMemMb();
    }

    public void setPhysicalMemMb(Integer value) {
        vdsDynamic.setPhysicalMemMb(value);
        calculateFreeSchedulingMemoryCache();
    }

    public String getSupportedClusterLevels() {
        return vdsDynamic.getSupportedClusterLevels();
    }

    public void setSupportedClusterLevels(String value) {
        vdsDynamic.setSupportedClusterLevels(value);
    }

    public Set<Version> getSupportedClusterVersionsSet() {
        return vdsDynamic.getSupportedClusterVersionsSet();
    }

    public String getSupportedEngines() {
        return vdsDynamic.getSupportedEngines();
    }

    public void setSupportedEngines(String value) {
        vdsDynamic.setSupportedEngines(value);
    }

    public Set<Version> getSupportedENGINESVersionsSet() {
        return vdsDynamic.getSupportedEngineVersionsSet();
    }

    public Double getCpuIdle() {
        return vdsStatistics.getCpuIdle();
    }

    public void setCpuIdle(Double value) {
        vdsStatistics.setCpuIdle(value);
    }

    public Double getCpuLoad() {
        return vdsStatistics.getCpuLoad();
    }

    public void setCpuLoad(Double value) {
        vdsStatistics.setCpuLoad(value);
    }

    public Double getCpuSys() {
        return vdsStatistics.getCpuSys();
    }

    public void setCpuSys(Double value) {
        vdsStatistics.setCpuSys(value);
    }

    public Double getCpuUser() {
        return vdsStatistics.getCpuUser();
    }

    public void setCpuUser(Double value) {
        vdsStatistics.setCpuUser(value);
    }

    public Integer getMemCommited() {
        return vdsDynamic.getMemCommited();
    }

    public void setMemCommited(Integer value) {
        vdsDynamic.setMemCommited(value);
        calculateFreeSchedulingMemoryCache();
    }

    public Integer getVmActive() {
        return vdsDynamic.getVmActive();
    }

    public void setVmActive(Integer value) {
        vdsDynamic.setVmActive(value);
    }

    public int getHighlyAvailableScore() {
        return vdsStatistics.getHighlyAvailableScore();
    }

    public void setHighlyAvailableScore(int value) {
        vdsStatistics.setHighlyAvailableScore(value);
    }

    public boolean getHighlyAvailableIsConfigured() {
        return vdsStatistics.getHighlyAvailableIsConfigured();
    }

    public void setHighlyAvailableIsConfigured(boolean value) {
        vdsStatistics.setHighlyAvailableIsConfigured(value);
    }

    public boolean getHighlyAvailableIsActive() {
        return vdsStatistics.getHighlyAvailableIsActive();
    }

    public void setHighlyAvailableIsActive(boolean value) {
        vdsStatistics.setHighlyAvailableIsActive(value);
    }

    public boolean getHighlyAvailableGlobalMaintenance() {
        return vdsStatistics.getHighlyAvailableGlobalMaintenance();
    }

    public void setHighlyAvailableGlobalMaintenance(boolean value) {
        vdsStatistics.setHighlyAvailableGlobalMaintenance(value);
    }

    public boolean getHighlyAvailableLocalMaintenance() {
        return vdsStatistics.getHighlyAvailableLocalMaintenance();
    }

    public void setHighlyAvailableLocalMaintenance(boolean value) {
        vdsStatistics.setHighlyAvailableLocalMaintenance(value);
    }

    public int getVmCount() {
        return vdsDynamic.getVmCount();
    }

    public void setVmCount(int value) {
        vdsDynamic.setVmCount(value);
    }

    public Integer getVmsCoresCount() {
        return vdsDynamic.getVmsCoresCount();
    }

    public void setVmsCoresCount(Integer value) {
        vdsDynamic.setVmsCoresCount(value);
    }

    public Integer getVmMigrating() {
        return vdsDynamic.getVmMigrating();
    }

    public void setVmMigrating(Integer value) {
        vdsDynamic.setVmMigrating(value);
    }

    public int getIncomingMigrations() {
        return vdsDynamic.getIncomingMigrations();
    }

    public void setIncomingMigrations(int value) {
        vdsDynamic.setIncomingMigrations(value);
    }

    public int getOutgoingMigrations() {
        return vdsDynamic.getOutgoingMigrations();
    }

    public void setOutgoingMigrations(int value) {
        vdsDynamic.setOutgoingMigrations(value);
    }

    public Integer getUsageMemPercent() {
        return vdsStatistics.getUsageMemPercent();
    }

    public void setUsageMemPercent(Integer value) {
        vdsStatistics.setUsageMemPercent(value);
    }

    public Integer getUsageCpuPercent() {
        return vdsStatistics.getUsageCpuPercent();
    }

    public void setUsageCpuPercent(Integer value) {
        vdsStatistics.setUsageCpuPercent(value);
    }

    public Integer getUsageNetworkPercent() {
        return vdsStatistics.getUsageNetworkPercent();
    }

    public void setUsageNetworkPercent(Integer value) {
        vdsStatistics.setUsageNetworkPercent(value);
    }

    public Integer getGuestOverhead() {
        return vdsDynamic.getGuestOverhead();
    }

    public void setGuestOverhead(Integer value) {
        vdsDynamic.setGuestOverhead(value);
    }

    public Integer getReservedMem() {
        return vdsDynamic.getReservedMem();
    }
    public void setReservedMem(Integer value) {
        vdsDynamic.setReservedMem(value);
        calculateFreeSchedulingMemoryCache();
    }

    public Long getBootTime() {
        return vdsStatistics.getBootTime();
    }

    public void setBootTime(Long value) {
        vdsStatistics.setBootTime(value);
    }

    public VDSStatus getPreviousStatus() {
        return vdsDynamic.getPreviousStatus();
    }

    public void setPreviousStatus(VDSStatus value) {
        vdsDynamic.setPreviousStatus(value);
    }

    public Long getMemFree() {
        return vdsStatistics.getMemFree();
    }

    public void setMemFree(Long value) {
        vdsStatistics.setMemFree(value);
    }

    public Long getMemShared() {
        return vdsStatistics.getMemShared();
    }

    public void setMemShared(Long value) {
        vdsStatistics.setMemShared(value);
    }

    public String getConsoleAddress() {
        return vdsStatic.getConsoleAddress();
    }

    public void setConsoleAddress(String value) {
        vdsStatic.setConsoleAddress(value);
    }

    public Integer getMemCommitedPercent() {
        Integer commited = vdsDynamic.getMemCommited();
        Integer physical = vdsDynamic.getPhysicalMemMb();

        if (commited == null || physical == null || physical == 0) {
            return 0;
        }

        return (commited * 100) / physical;
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     */
    @Deprecated
    public void setMemCommitedPercent(Integer value) {
    }

    public Integer getMemSharedPercent() {
        Long shared = vdsStatistics.getMemShared();
        Integer physical = vdsDynamic.getPhysicalMemMb();

        if (shared == null || physical == null || physical == 0) {
            return 0;
        }

        return (int) ((shared * 100) / physical);
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     */
    @Deprecated
    public void setMemSharedPercent(Integer value) {
    }

    public Long getSwapFree() {
        return vdsStatistics.getSwapFree();
    }

    public void setSwapFree(Long value) {
        vdsStatistics.setSwapFree(value);
    }

    public Long getSwapTotal() {
        return vdsStatistics.getSwapTotal();
    }

    public void setSwapTotal(Long value) {
        vdsStatistics.setSwapTotal(value);
    }

    public Integer getKsmCpuPercent() {
        return vdsStatistics.getKsmCpuPercent();
    }

    public void setKsmCpuPercent(Integer value) {
        vdsStatistics.setKsmCpuPercent(value);
    }

    public Long getKsmPages() {
        return vdsStatistics.getKsmPages();
    }

    public void setKsmPages(Long value) {
        vdsStatistics.setKsmPages(value);
    }

    public Boolean getKsmState() {
        return vdsStatistics.getKsmState();
    }

    public void setKsmState(Boolean value) {
        vdsStatistics.setKsmState(value);
    }

    public String getSoftwareVersion() {
        return vdsDynamic.getSoftwareVersion();
    }

    public void setSoftwareVersion(String value) {
        vdsDynamic.setSoftwareVersion(value);
    }

    public String getVersionName() {
        return vdsDynamic.getVersionName();
    }

    public void setVersionName(String value) {
        vdsDynamic.setVersionName(value);
    }

    public String getBuildName() {
        return vdsDynamic.getBuildName();
    }

    public void setBuildName(String value) {
        vdsDynamic.setBuildName(value);
    }

    public String getCpuFlags() {
        return vdsDynamic.getCpuFlags();
    }

    public void setCpuFlags(String value) {
        vdsDynamic.setCpuFlags(value);
    }

    public Date getCpuOverCommitTimestamp() {
        return vdsStatistics.getCpuOverCommitTimeStamp();
    }

    public void setCpuOverCommitTimestamp(Date value) {
        vdsStatistics.setCpuOverCommitTimeStamp(value);
    }

    @Override
    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
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
        calculateFreeSchedulingMemoryCache();
    }

    /**
     * Get the number of CPUs that were scheduled but not yet
     * assigned to a running VM.
     *
     * This field is a cache, use for reporting only.
     * The authoritative source for current value is the
     * {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}
     */
    public Integer getPendingVcpusCount() {
        return vdsDynamic.getPendingVcpusCount();
    }

    public void setPendingVcpusCount(Integer value) {
        vdsDynamic.setPendingVcpusCount(value);
    }

    /**
     * Get the amount of memory that was scheduled but not yet
     * assigned to a running VM.
     *
     * This field is a cache, use for reporting only.
     * The authoritative source for current value is the
     * {@link org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager}
     */
    public int getPendingVmemSize() {
        return vdsDynamic.getPendingVmemSize();
    }

    public void setPendingVmemSize(int value) {
        vdsDynamic.setPendingVmemSize(value);
        calculateFreeSchedulingMemoryCache();
    }

    public Boolean getNetConfigDirty() {
        return vdsDynamic.getNetConfigDirty();
    }

    public void setNetConfigDirty(Boolean value) {
        vdsDynamic.setNetConfigDirty(value);
    }

    public boolean isPmKdumpDetection() {
        return vdsStatic.isPmKdumpDetection();
    }

    public void setPmKdumpDetection(boolean pmKdumpDetection) {
        vdsStatic.setPmKdumpDetection(pmKdumpDetection);
    }

    public boolean isPmEnabled() {
        return vdsStatic.isPmEnabled();
    }

    public void setPmEnabled(boolean value) {
        vdsStatic.setPmEnabled(value);
    }

    public List<FenceProxySourceType> getFenceProxySources() {
        return vdsStatic.getFenceProxySources();
    }

    public void setFenceProxySources(List<FenceProxySourceType> fenceProxySources) {
        vdsStatic.setFenceProxySources(fenceProxySources);
    }

    public String getHostOs() {
        return vdsDynamic.getHostOs();
    }

    public void setHostOs(String value) {
        vdsDynamic.setHostOs(value);
    }

    public String getKvmVersion() {
        return vdsDynamic.getKvmVersion();
    }

    public void setKvmVersion(String value) {
        vdsDynamic.setKvmVersion(value);
    }

    public RpmVersion getLibvirtVersion() {
        return vdsDynamic.getLibvirtVersion();
    }

    public void setLibvirtVersion(RpmVersion value) {
        vdsDynamic.setLibvirtVersion(value);
    }

    public String getSpiceVersion() {
        return vdsDynamic.getSpiceVersion();
    }

    public void setSpiceVersion(String value) {
        vdsDynamic.setSpiceVersion(value);
    }

    public RpmVersion getGlusterVersion() {
        return vdsDynamic.getGlusterVersion();
    }

    public void setGlusterVersion(RpmVersion value) {
        vdsDynamic.setGlusterVersion(value);
    }

    public RpmVersion getLibrbdVersion() {
        return vdsDynamic.getLibrbdVersion();
    }

    public void setLibrbdVersion(RpmVersion value) {
        vdsDynamic.setLibrbdVersion(value);
    }

    public RpmVersion getGlusterfsCliVersion() {
        return vdsDynamic.getGlusterfsCliVersion();
    }

    public void setGlusterfsCliVersion(RpmVersion value) {
        vdsDynamic.setGlusterfsCliVersion(value);
    }

    public RpmVersion getOvsVersion() {
        return vdsDynamic.getOvsVersion();
    }

    public void setOvsVersion(RpmVersion ovsVersion) {
        vdsDynamic.setOvsVersion(ovsVersion);
    }

    public RpmVersion getNmstateVersion() {
        return vdsDynamic.getNmstateVersion();
    }

    public void setNmstateVersion(RpmVersion nmstateVersion) {
        vdsDynamic.setNmstateVersion(nmstateVersion);
    }

    public String getKernelVersion() {
        return vdsDynamic.getKernelVersion();
    }

    public void setKernelVersion(String value) {
        vdsDynamic.setKernelVersion(value);
    }

    public void setIScsiInitiatorName(String value) {
        vdsDynamic.setIScsiInitiatorName(value);
    }

    public String getIScsiInitiatorName() {
        return vdsDynamic.getIScsiInitiatorName();
    }

    public Map<String, List<Map<String, String>>> getHBAs() {
        return vdsDynamic.getHBAs();
    }

    public void setHBAs(Map<String, List<Map<String, String>>> HBAs) {
        vdsDynamic.setHBAs(HBAs);
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        vdsDynamic.setTransparentHugePagesState(value);
    }

    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return vdsDynamic.getTransparentHugePagesState();
    }

    public int getAnonymousHugePages() {
        return vdsStatistics.getAnonymousHugePages();
    }

    public void setAnonymousHugePages(int value) {
        vdsStatistics.setAnonymousHugePages(value);
    }

    public List<HugePage> getHugePages() {
        return vdsStatistics.getHugePages();
    }

    public void setHugePages(List<HugePage> value) {
        vdsStatistics.setHugePages(value);
    }

    public VdsStatic getStaticData() {
        return vdsStatic;
    }

    public void setStaticData(VdsStatic value) {
        vdsStatic = value;
    }

    public VdsDynamic getDynamicData() {
        return vdsDynamic;
    }

    public void setDynamicData(VdsDynamic value) {
        vdsDynamic = value;
    }

    public VdsStatistics getStatisticsData() {
        return vdsStatistics;
    }

    public void setStatisticsData(VdsStatistics value) {
        vdsStatistics = value;
    }

    public Set<String> getNetworkNames() {
        return networkNames;
    }

    public ArrayList<VdsNetworkInterface> getInterfaces() {
        return interfaces;
    }

    public ArrayList<VDSDomainsData> getDomains() {
        return privateDomains;
    }

    public void setDomains(ArrayList<VDSDomainsData> value) {
        privateDomains = value;
    }

    public Double getImagesLastCheck() {
        return imagesLastCheck;
    }

    public void setImagesLastCheck(Double value) {
        imagesLastCheck = value;
    }

    public Double getImagesLastDelay() {
        return imagesLastDelay;
    }

    public void setImagesLastDelay(Double value) {
        imagesLastDelay = value;
    }

    public void setVersion(RpmVersion value) {
        vdsDynamic.setVersion(value);
    }

    public RpmVersion getVersion() {
        return vdsDynamic.getVersion();
    }

    public ServerCpu getCpuName() {
        return cpuName;
    }

    public void setCpuName(ServerCpu value) {
        cpuName = value;
    }

    public Integer getVdsSpmId() {
        return vdsSpmId;
    }

    public void setVdsSpmId(Integer value) {
        vdsSpmId = value;
    }

    public long getOtpValidity() {
        return vdsStatic.getOtpValidity();
    }

    public void setOtpValidity(long value) {
        vdsStatic.setOtpValidity(value);
    }

    public int getVdsSpmPriority() {
        return vdsStatic.getVdsSpmPriority();
    }

    public void setVdsSpmPriority(int value) {
        vdsStatic.setVdsSpmPriority(value);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public VdsSpmStatus getSpmStatus() {
        return spmStatus;
    }

    public void setSpmStatus(VdsSpmStatus value) {
        spmStatus = value;
    }

    public boolean isSpm() {
        return spmStatus == VdsSpmStatus.SPM;
    }

    public NonOperationalReason getNonOperationalReason() {
        return vdsDynamic.getNonOperationalReason();
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        vdsDynamic.setNonOperationalReason(nonOperationalReason);
    }

    public Map<String, Long> getLocalDisksUsage() {
        return localDisksUsage;
    }

    public void setLocalDisksUsage(Map<String, Long> localDiskUsage) {
        localDisksUsage = localDiskUsage;
    }

    public boolean isAutoRecoverable() {
        return vdsStatic.isAutoRecoverable();
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        vdsStatic.setAutoRecoverable(autoRecoverable);
    }

    public String getSshKeyFingerprint() {
        return vdsStatic.getSshKeyFingerprint();
    }

    public void setSshKeyFingerprint(String sshKeyFingerprint) {
        vdsStatic.setSshKeyFingerprint(sshKeyFingerprint);
    }

    public String getSshPublicKey() {
        return vdsStatic.getSshPublicKey();
    }

    public void setSshPublicKey(String sshPublicKey) {
        vdsStatic.setSshPublicKey(sshPublicKey);
    }

    public Guid getHostProviderId() {
        return vdsStatic.getHostProviderId();
    }

    public void setHostProviderId(Guid hostProviderId) {
        vdsStatic.setHostProviderId(hostProviderId);
    }

    public List<FenceAgent> getFenceAgents() {
        return fenceAgents;
    }

    public void setFenceAgents(List<FenceAgent> fenceAgents) {
        this.fenceAgents = fenceAgents;
    }

    private void calculateFreeSchedulingMemoryCache() {
        if (getMemCommited() != null && getPhysicalMemMb() != null && getReservedMem() != null) {
            maxSchedulingMemory = getFreeVirtualMemory() - getPendingVmemSize();
            // avoid negative values
            maxSchedulingMemory = maxSchedulingMemory > 0 ? maxSchedulingMemory : 0;
        }
    }

    private float getFreeVirtualMemory() {
        if (getMemCommited() != null && getPhysicalMemMb() != null && getReservedMem() != null) {
            float freeMemory = (getMaxVdsMemoryOverCommit() * getPhysicalMemMb() / 100.0f)
                    - getMemCommited()
                    - getReservedMem();
            // avoid negative values
            return freeMemory > 0 ? freeMemory : 0;
        } else {
            return 0;
        }
    }

    public float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

    @Override
    public String toString() {
        // note that vdsStatic may be null, so the getName with no null protection
        // is not enough, remove this once vdsStatic can not be null
        return "Host[" + (vdsStatic == null ? "null" : (vdsStatic.getName() + "," + vdsStatic.getId())) + "]";
    }

    public String getActiveNic() {
        return activeNic;
    }

    public void setActiveNic(String activeNic) {
        this.activeNic = activeNic;
    }

    public void setSupportedEmulatedMachines(String supportedEmulatedMachines) {
        vdsDynamic.setSupportedEmulatedMachines(supportedEmulatedMachines);
    }

    public String getSupportedEmulatedMachines() {
        return vdsDynamic.getSupportedEmulatedMachines();
    }

    public boolean isPowerManagementControlledByPolicy() {
        return vdsDynamic.isPowerManagementControlledByPolicy();
    }

    public void setPowerManagementControlledByPolicy(boolean powerManagementControlledByPolicy) {
        vdsDynamic.setPowerManagementControlledByPolicy(powerManagementControlledByPolicy);
    }

    public boolean isDisablePowerManagementPolicy() {
        return vdsStatic.isDisablePowerManagementPolicy();
    }

    public void setDisablePowerManagementPolicy(boolean disablePowerManagementPolicy) {
        vdsStatic.setDisablePowerManagementPolicy(disablePowerManagementPolicy);
    }

    public Set<VmRngDevice.Source> getSupportedRngSources() {
        return vdsDynamic.getSupportedRngSources();
    }

    public KdumpStatus getKdumpStatus() {
        return vdsDynamic.getKdumpStatus();
    }

    public void setKdumpStatus(KdumpStatus kdumpStatus) {
        vdsDynamic.setKdumpStatus(kdumpStatus);
    }

    public SELinuxMode getSELinuxEnforceMode() {
        return vdsDynamic.getSELinuxEnforceMode();
    }

    public void setSELinuxEnforceMode(Integer value) {
        vdsDynamic.setSELinuxEnforceMode(value);
    }

    public void setNumaNodeList(List<VdsNumaNode> numaNodeList) {
        vdsDynamic.setNumaNodeList(numaNodeList);
    }

    public List<VdsNumaNode> getNumaNodeList() {
        return vdsDynamic.getNumaNodeList();
    }

    /**
     * If host enables the feature of auto numa balancing.
     */
    public AutoNumaBalanceStatus getAutoNumaBalancing() {
        return vdsDynamic.getAutoNumaBalancing();
    }

    public void setAutoNumaBalancing(AutoNumaBalanceStatus autoNumaBalancing) {
        vdsDynamic.setAutoNumaBalancing(autoNumaBalancing);
    }

    /**
     * If host supports numa.
     */
    public boolean isNumaSupport() {
        return vdsDynamic.isNumaSupport();
    }

    public void setNumaSupport(boolean numaSupport) {
        vdsDynamic.setNumaSupport(numaSupport);
    }

    public boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(boolean enableBalloon) {
        balloonEnabled = enableBalloon;
    }

    public void setCountThreadsAsCores(boolean value) {
        countThreadsAsCores = value;
    }

    public boolean getCountThreadsAsCores() {
        return countThreadsAsCores;
    }

    public boolean isFenceAgentsExist() {
        return !getFenceAgents().isEmpty();
    }

    public String getMaintenanceReason() {
        return vdsDynamic.getMaintenanceReason();
    }

    public void setMaintenanceReason(String value) {
        vdsDynamic.setMaintenanceReason(value);
    }

    public boolean isUpdateAvailable() {
        return vdsDynamic.isUpdateAvailable();
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        vdsDynamic.setUpdateAvailable(updateAvailable);
    }

    public Set<String> getAdditionalFeatures() {
        return vdsDynamic.getAdditionalFeatures();
    }

    public void setAdditionalFeatures(Set<String> additionalFeatures) {
        vdsDynamic.setAdditionalFeatures(additionalFeatures);
    }

    public String getKernelArgs() {
        return vdsDynamic.getKernelArgs();
    }

    public void setKernelArgs(String kernelArgs) {
        vdsDynamic.setKernelArgs(kernelArgs);
    }

    public String getPrettyName() {
        return vdsDynamic.getPrettyName();
    }

    public void setPrettyName(String prettyName) {
        vdsDynamic.setPrettyName(prettyName);
    }

    public boolean isOvirNode() {
        return getVdsType() == VDSType.oVirtNode;
    }

    public List<V2VJobInfo> getV2VJobs() {
        return vdsStatistics.getV2VJobs();
    }

    public void setV2VJobs(List<V2VJobInfo> v2vJobs) {
        vdsStatistics.setV2VJobs(v2vJobs);
    }

    public void setHostDevicePassthroughEnabled(boolean value) {
        vdsDynamic.setHostDevicePassthroughEnabled(value);
    }

    public boolean isHostDevicePassthroughEnabled() {
        return vdsDynamic.isHostDevicePassthroughEnabled();
    }

    public DnsResolverConfiguration getReportedDnsResolverConfiguration() {
        return vdsDynamic.getReportedDnsResolverConfiguration();
    }

    public void setReportedDnsResolverConfiguration(DnsResolverConfiguration dnsResolverConfiguration) {
        vdsDynamic.setReportedDnsResolverConfiguration(dnsResolverConfiguration);
    }

    public String getLastStoredKernelCmdline() {
        return getStaticData().getLastStoredKernelCmdline();
    }

    public void setLastStoredKernelCmdline(String lastSentKernelArguments) {
        getStaticData().setLastStoredKernelCmdline(lastSentKernelArguments);
    }

    public String getCurrentKernelCmdline() {
        return getStaticData().getCurrentKernelCmdline();
    }

    public void setCurrentKernelCmdline(String currentKernelArguments) {
        getStaticData().setCurrentKernelCmdline(currentKernelArguments);
    }

    public boolean isKernelCmdlineParsable() {
        return vdsStatic.isKernelCmdlineParsable();
    }

    public void setKernelCmdlineParsable(boolean kernelCmdlineParsable) {
        vdsStatic.setKernelCmdlineParsable(kernelCmdlineParsable);
    }

    public boolean isKernelCmdlineBlacklistNouveau() {
        return vdsStatic.isKernelCmdlineBlacklistNouveau();
    }

    public void setKernelCmdlineBlacklistNouveau(boolean kernelCmdlineBlacklistNouveau) {
        vdsStatic.setKernelCmdlineBlacklistNouveau(kernelCmdlineBlacklistNouveau);
    }

    public void setKernelCmdlineUnsafeInterrupts(boolean kernelCmdlineUnsafeInterrupts) {
        vdsStatic.setKernelCmdlineUnsafeInterrupts(kernelCmdlineUnsafeInterrupts);
    }

    public boolean isKernelCmdlineIommu() {
        return vdsStatic.isKernelCmdlineIommu();
    }

    public boolean isKernelCmdlinePciRealloc() {
        return vdsStatic.isKernelCmdlinePciRealloc();
    }

    public void setKernelCmdlinePciRealloc(boolean kernelCmdlinePciRealloc) {
        vdsStatic.setKernelCmdlinePciRealloc(kernelCmdlinePciRealloc);
    }

    public boolean isKernelCmdlineFips() {
        return vdsStatic.isKernelCmdlineFips();
    }

    public void setKernelCmdlineFips(boolean kernelCmdlineFips) {
        vdsStatic.setKernelCmdlineFips(kernelCmdlineFips);
    }

    public boolean isKernelCmdlineSmtDisabled() {
        return vdsStatic.isKernelCmdlineSmtDisabled();
    }

    public void setKernelCmdlineSmtDisabled (boolean kernelCmdlineSmt) {
        vdsStatic.setKernelCmdlineSmtDisabled(kernelCmdlineSmt);
    }

    public void setKernelCmdlineIommu(boolean kernelCmdlineIommu) {
        vdsStatic.setKernelCmdlineIommu(kernelCmdlineIommu);
    }

    public boolean isKernelCmdlineKvmNested() {
        return vdsStatic.isKernelCmdlineKvmNested();
    }

    public void setKernelCmdlineKvmNested(boolean kernelCmdlineKvmNested) {
        vdsStatic.setKernelCmdlineKvmNested(kernelCmdlineKvmNested);
    }

    public boolean isKernelCmdlineUnsafeInterrupts() {
        return vdsStatic.isKernelCmdlineUnsafeInterrupts();
    }

    public int getVgpuPlacement() {
        return vdsStatic.getVgpuPlacement();
    }

    public void setVgpuPlacement(int vgpuPlacement) {
        vdsStatic.setVgpuPlacement(vgpuPlacement);
    }

    public boolean shouldVdsBeFenced() {
        boolean result = false;

        switch (this.getStatus()) {
        case Down:
        case InstallFailed:
        case Maintenance:
        case NonOperational:
        case NonResponsive:
        case Kdumping:  // it should happen only after restart when host is stuck in status Kdumping
            result = true;
            break;

        default:
            break;
        }

        return result;
    }

    public boolean isHostedEngineHost() {
        return hostedEngineHost;
    }

    public void setHostedEngineHost(boolean value) {
        hostedEngineHost = value;
    }

    public boolean isHostedEngineConfigured() {
        return vdsDynamic.isHostedEngineConfigured();
    }

    public void setHostedEngineConfigured(boolean heConfigured) {
        vdsDynamic.setHostedEngineConfigured(heConfigured);
    }

    /**
     * Return true if hosted engine has been deployed on this host and false otherwise. Hosted
     * engine is considered deployed if a non-empty hosted engine configuration file is present.
     * The host's HA score and HA active status are used as a fallback check in case the check
     * for the configuration file fails.
     *
     * @return true if hosted engine has been deployed on this host and false otherwise
     */
    public boolean isHostedEngineDeployed() {
        // TODO Add running status of ovirt-ha-agent and ovirt-ha-broker once available from VDSM
        if (isHostedEngineConfigured()
                || (getHighlyAvailableScore() > 0 && getHighlyAvailableIsActive())) {
            return true;
        }

        return false;
    }

    public boolean isFencingEnabled() {
        return fencingEnabled;
    }

    public void setFencingEnabled(boolean fencingEnabled) {
        this.fencingEnabled = fencingEnabled;
    }

    public PeerStatus getGlusterPeerStatus() {
        return glusterPeerStatus;
    }

    public void setGlusterPeerStatus(PeerStatus glusterPeerStatus) {
        this.glusterPeerStatus = glusterPeerStatus;
    }

    public boolean isInFenceFlow() {
        return vdsDynamic.isInFenceFlow();
    }

    public void setInFenceFlow(boolean isInFenceFlow) {
        vdsDynamic.setInFenceFlow(isInFenceFlow);
    }

    public void setReinstallRequired(boolean reinstallRequired) {
        vdsStatic.setReinstallRequired(reinstallRequired);
    }

    public Map<String, Object> getKernelFeatures() {
        return vdsDynamic.getKernelFeatures();
    }

    public void setKernelFeatures(Map<String, Object> kernelFeatures) {
        vdsDynamic.setKernelFeatures(kernelFeatures);
    }

    public Map<String, Object> getOpenstackBindingHostIds() {
        return vdsDynamic.getOpenstackBindingHostIds();
    }

    public void setOpenstackBindingHostIds(Map<String, Object> openstackBindingHostIds) {
        vdsDynamic.setOpenstackBindingHostIds(openstackBindingHostIds);
    }

    public boolean isNetworkOperationInProgress() {
        return this.networkOperationInProgress;
    }

    public void setNetworkOperationInProgress(boolean networkOperationInProgress) {
        this.networkOperationInProgress = networkOperationInProgress;
    }

    public void setVncEncryptionEnabled(boolean vncEncryptionEnabled) {
        vdsDynamic.setVncEncryptionEnabled(vncEncryptionEnabled);
    }

    public boolean isVncEncryptionEnabled() {
        return vdsDynamic.isVncEncryptionEnabled();
    }

    public Map<String, Object> getConnectorInfo() {
        return vdsDynamic.getConnectorInfo();
    }

    public void setConnectorInfo(Map<String, Object> connectorInfo) {
        vdsDynamic.setConnectorInfo(connectorInfo);
    }

    public boolean isDefaultRouteRoleNetworkAttached() {
        return this.isDefaultRouteRoleNetworkAttached;
    }

    public void setIsDefaultRouteRoleNetworkAttached(boolean isDefaultRouteRoleNetworkAttached) {
        this.isDefaultRouteRoleNetworkAttached = isDefaultRouteRoleNetworkAttached;
    }

    public Boolean isBackupEnabled() {
        return vdsDynamic.isBackupEnabled();
    }

    public void setBackupEnabled(Boolean value) {
        vdsDynamic.setBackupEnabled(value);
    }

    public Boolean isColdBackupEnabled() {
        return vdsDynamic.isColdBackupEnabled();
    }

    public void setColdBackupEnabled(Boolean value) {
        vdsDynamic.setColdBackupEnabled(value);
    }

    public Boolean isClearBitmapsEnabled() {
        return vdsDynamic.isClearBitmapsEnabled();
    }

    public void setClearBitmapsEnabled(Boolean value) {
        vdsDynamic.setClearBitmapsEnabled(value);
    }

    public Set<StorageFormatType> getSupportedDomainVersions() {
        return vdsDynamic.getSupportedDomainVersions();
    }

    public void setSupportedDomainVersions(Set<StorageFormatType> supportedDomainVersions) {
        vdsDynamic.setSupportedDomainVersions(supportedDomainVersions);
    }

    public void setSupportedDomainVersionsAsString(String supportedDomainVersions) {
        vdsDynamic.setSupportedDomainVersionsAsString(supportedDomainVersions);
    }

    public Map<String, Object> getSupportedBlockSize() {
        return vdsDynamic.getSupportedBlockSize();
    }

    public void setSupportedBlockSize(Map<String, Object> supportedBlockSize) {
        vdsDynamic.setSupportedBlockSize(supportedBlockSize);
    }

    public boolean isClusterSmtDisabled() {
        return isClusterSmtDisabled;
    }

    public void setClusterSmtDisabled(boolean clusterSmtDisabled) {
        isClusterSmtDisabled = clusterSmtDisabled;
    }

    public boolean hasSmtDiscrepancyAlert() {
        int threadsPerCore = getThreadsPerCore();
        return isKernelCmdlineSmtDisabled() && threadsPerCore > 1;
    }

    public boolean hasSmtClusterDiscrepancyAlert() {
        int threadsPerCore = getThreadsPerCore();
        boolean settingsDifferent = isKernelCmdlineSmtDisabled() != isClusterSmtDisabled();
        boolean smtActual = threadsPerCore > 1;
        boolean disabledClusterButActual = smtActual && isClusterSmtDisabled();
        boolean disabledClusterEnabledHost = isKernelCmdlineSmtDisabled() && !isClusterSmtDisabled();

        return settingsDifferent && (disabledClusterButActual || disabledClusterEnabledHost);
    }

    private int getThreadsPerCore() {
        int threads = getCpuThreads() == null ? 1 : getCpuThreads();
        int cores = getCpuCores() == null ? 1 : getCpuCores();
        return threads / cores;
    }

    public String getTscFrequency() {
        return vdsDynamic.getTscFrequency();
    }

    public String getTscFrequencyIntegral() {
        String tscFrequency = getTscFrequency();
        if (tscFrequency == null) {
            return null;
        }

        return tscFrequency.split("\\.")[0];
    }

    public void setTscFrequency(String tscFrequency) {
        vdsDynamic.setTscFrequency(tscFrequency);
    }

    public Set<String> getCpuFlagsMissing() {
        return cpuFlagsMissing;
    }

    public void setCpuFlagsMissing(Set<String> cpuFlagsMissing) {
        if (cpuFlagsMissing == null) {
            this.cpuFlagsMissing = new HashSet<>();
        } else {
            this.cpuFlagsMissing = cpuFlagsMissing;
        }
    }

    public List<String> getSupportedCpus() {
        return supportedCpus;
    }

    public void setSupportedCpus(List<String> supportedCpus) {
        if (supportedCpus == null) {
            this.supportedCpus = new ArrayList<>();
        } else {
            this.supportedCpus = supportedCpus;
        }
    }

    public boolean isTscScalingEnabled() {
        return vdsDynamic.isTscScalingEnabled();
    }

    public void setTscScalingEnabled(boolean tscScalingEnabled) {
        vdsDynamic.setTscScalingEnabled(tscScalingEnabled);
    }

    public boolean isFipsEnabled() {
        return vdsDynamic.isFipsEnabled();
    }

    public void setFipsEnabled(boolean fipsEnabled) {
        vdsDynamic.setFipsEnabled(fipsEnabled);
    }

    public boolean isOvnConfigured() {
        return vdsDynamic.isOvnConfigured();
    }

    public void setOvnConfigured(boolean ovnConfigured) {
        vdsDynamic.setOvnConfigured(ovnConfigured);
    }

    public boolean isManaged() {
        return vdsStatic.isManaged();
    }

    public String getBootUuid() {
        return vdsDynamic.getBootUuid();
    }

    public void setBootUuid(String bootUuid) {
        vdsDynamic.setBootUuid(bootUuid);
    }

    public boolean isCdChangePdiv() {
        return vdsDynamic.isCdChangePdiv();
    }

    public void setCdChangePdiv(boolean cdChangePdiv) {
        vdsDynamic.setCdChangePdiv(cdChangePdiv);
    }
}
