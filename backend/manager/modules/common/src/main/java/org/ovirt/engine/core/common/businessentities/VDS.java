package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

public class VDS extends IVdcQueryable implements Serializable, BusinessEntityWithStatus<Guid, VDSStatus>, HasStoragePool<Guid>, Commented, Nameable, Cloneable {
    private static final long serialVersionUID = -7893976203379789926L;
    private VdsStatic vdsStatic;
    private VdsDynamic vdsDynamic;
    private VdsStatistics vdsStatistics;
    private ArrayList<VdsNetworkInterface> interfaces;
    private ArrayList<Network> networks;
    private String activeNic;
    private boolean balloonEnabled;
    private boolean countThreadsAsCores;
    private List<FenceAgent> fenceAgents;
    private VdsSpmStatus spmStatus;
    private Version vdsGroupCompatibilityVersion;
    private String vdsGroupName;
    private String vdsGroupDescription;
    private String vdsGroupCpuName;
    private Boolean vdsGroupVirtService;
    private Guid storagePoolId;
    private String storagePoolName;
    private int maxVdsMemoryOverCommit;
    private ArrayList<VDSDomainsData> privateDomains;
    private Boolean vdsGroupGlusterService;
    private Double imagesLastCheck;
    private Double imagesLastDelay;
    private ServerCpu cpuName;
    private Integer vdsSpmId;
    private float maxSchedulingMemory;

    /**
     * This map holds the disk usage reported by the host. The mapping is path to usage (in MB).
     */
    private Map<String, Long> localDisksUsage;

    public VDS() {
        vdsStatic = new VdsStatic();
        vdsDynamic = new VdsDynamic();
        vdsStatistics = new VdsStatistics();
        storagePoolId = Guid.Empty;
        spmStatus = VdsSpmStatus.None;
        interfaces = new ArrayList<>();
        networks = new ArrayList<>();
        fenceAgents = new LinkedList<>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (vdsStatic == null ? 0 : vdsStatic.hashCode());
        result = prime * result + (cpuName == null ? 0 : cpuName.hashCode());
        result = prime * result + (spmStatus == null ? 0 : spmStatus.hashCode());
        result = prime * result + (imagesLastCheck == null ? 0 : imagesLastCheck.hashCode());
        result = prime * result + (imagesLastDelay == null ? 0 : imagesLastDelay.hashCode());
        result = prime * result + (interfaces == null ? 0 : interfaces.hashCode());
        result = prime * result + (networks == null ? 0 : networks.hashCode());
        result = prime * result + maxVdsMemoryOverCommit;
        result = prime * result + (privateDomains == null ? 0 : privateDomains.hashCode());
        result = prime * result + (vdsSpmId == null ? 0 : vdsSpmId.hashCode());
        result = prime * result + (storagePoolId == null ? 0 : storagePoolId.hashCode());
        result = prime * result + (storagePoolName == null ? 0 : storagePoolName.hashCode());
        result = prime * result
                + (vdsGroupCompatibilityVersion == null ? 0 : vdsGroupCompatibilityVersion.hashCode());
        result = prime * result + (vdsGroupCpuName == null ? 0 : vdsGroupCpuName.hashCode());
        result = prime * result + (vdsGroupDescription == null ? 0 : vdsGroupDescription.hashCode());
        result = prime * result + (vdsGroupName == null ? 0 : vdsGroupName.hashCode());
        result = prime * result + (vdsGroupVirtService == null ? 0 : vdsGroupVirtService.hashCode());
        result = prime * result + (vdsGroupGlusterService == null ? 0 : vdsGroupGlusterService.hashCode());
        result = prime * result + (balloonEnabled ? 0 : 1);
        result = prime * result + (countThreadsAsCores ? 0 : 1);
        return result;
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
        return (ObjectUtils.objectsEqual(vdsStatic, other.vdsStatic)
                && ObjectUtils.objectsEqual(cpuName, other.cpuName)
                && spmStatus == other.spmStatus
                && ObjectUtils.objectsEqual(imagesLastCheck, other.imagesLastCheck)
                && ObjectUtils.objectsEqual(imagesLastDelay, other.imagesLastDelay)
                && ObjectUtils.objectsEqual(interfaces, other.interfaces)
                && ObjectUtils.objectsEqual(networks, other.networks)
                && maxVdsMemoryOverCommit == other.maxVdsMemoryOverCommit
                && balloonEnabled == other.balloonEnabled
                && ObjectUtils.objectsEqual(privateDomains, other.privateDomains)
                && ObjectUtils.objectsEqual(vdsSpmId, other.vdsSpmId)
                && ObjectUtils.objectsEqual(storagePoolId, other.storagePoolId)
                && ObjectUtils.objectsEqual(storagePoolName, other.storagePoolName)
                && ObjectUtils.objectsEqual(vdsGroupCompatibilityVersion, other.vdsGroupCompatibilityVersion)
                && ObjectUtils.objectsEqual(vdsGroupCpuName, other.vdsGroupCpuName)
                && ObjectUtils.objectsEqual(vdsGroupDescription, other.vdsGroupDescription)
                && ObjectUtils.objectsEqual(vdsGroupName, other.vdsGroupName)
                && ObjectUtils.objectsEqual(vdsGroupVirtService, other.vdsGroupVirtService)
                && ObjectUtils.objectsEqual(vdsGroupGlusterService, other.vdsGroupGlusterService));
    }

    public VDS clone() {
        VDS vds = new VDS();
        vds.setVdsGroupId(getVdsGroupId());
        vds.setVdsGroupCpuName(getVdsGroupCpuName());
        vds.setCpuName(getCpuName());
        vds.setVdsGroupDescription(getVdsGroupDescription());
        vds.setId(getId());
        vds.setVdsName(getName());
        vds.setHostName(getHostName());
        vds.setComment(getComment());
        vds.setPort(getPort());
        vds.setProtocol(getProtocol());
        vds.setSshPort(getSshPort());
        vds.setSshUsername(getSshUsername());
        vds.setStatus(getStatus());
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
        vds.setMemAvailable(getMemAvailable());
        vds.setMemShared(getMemShared());
        vds.setSoftwareVersion(getSoftwareVersion());
        vds.setVersionName(getVersionName());
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
        vds.setGlusterVersion(getGlusterVersion());
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
        vds.setVdsGroupCompatibilityVersion(getVdsGroupCompatibilityVersion());
        return vds;
    }

    public Version getVdsGroupCompatibilityVersion() {
        return vdsGroupCompatibilityVersion;
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

    public void setVdsGroupCompatibilityVersion(Version value) {
        vdsGroupCompatibilityVersion = value;
    }

    public Guid getVdsGroupId() {
        return vdsStatic.getVdsGroupId();
    }

    public void setVdsGroupId(Guid value) {
        vdsStatic.setVdsGroupId(value);
    }

    public String getVdsGroupName() {
        return vdsGroupName;
    }

    public void setVdsGroupName(String value) {
        vdsGroupName = value;
    }

    public String getVdsGroupDescription() {
        return vdsGroupDescription;
    }

    public void setVdsGroupDescription(String value) {
        vdsGroupDescription = value;
    }

    public String getVdsGroupCpuName() {
        return vdsGroupCpuName;
    }

    public void setVdsGroupCpuName(String value) {
        vdsGroupCpuName = value;
    }

    public Boolean getVdsGroupSupportsVirtService() {
        return vdsGroupVirtService;
    }

    public void setVdsGroupSupportsVirtService(Boolean value) {
        vdsGroupVirtService = value;
    }

    public Boolean getVdsGroupSupportsGlusterService() {
        return vdsGroupGlusterService;
    }

    public void setVdsGroupSupportsGlusterService(Boolean value) {
        vdsGroupGlusterService = value;
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
        vdsStatic.setVdsName(value);
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

    public VdsProtocol getProtocol() {
        return vdsStatic.getProtocol();
    }

    public void setProtocol(VdsProtocol value) {
        vdsStatic.setProtocol(value);
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
    }

    public String getSupportedClusterLevels() {
        return vdsDynamic.getSupportedClusterLevels();
    }

    public void setSupportedClusterLevels(String value) {
        vdsDynamic.setSupportedClusterLevels(value);
    }

    public HashSet<Version> getSupportedClusterVersionsSet() {
        return vdsDynamic.getSupportedClusterVersionsSet();
    }

    public String getSupportedEngines() {
        return vdsDynamic.getSupportedEngines();
    }

    public void setSupportedEngines(String value) {
        vdsDynamic.setSupportedEngines(value);
    }

    public HashSet<Version> getSupportedENGINESVersionsSet() {
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
        calculateFreeVirtualMemory();
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

    public Long getMemAvailable() {
        return vdsStatistics.getMemAvailable();
    }

    public void setMemAvailable(Long value) {
        vdsStatistics.setMemAvailable(value);
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
     *
     * @param value
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
     *
     * @param value
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

    public int getVdsStrength() {
        return vdsStatic.getVdsStrength();
    }

    public void setVdsStrength(int value) {
        vdsStatic.setVdsStrength(value);
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
    }

    public Integer getPendingVcpusCount() {
        return vdsDynamic.getPendingVcpusCount();
    }

    public void setPendingVcpusCount(Integer value) {
        vdsDynamic.setPendingVcpusCount(value);
    }

    public int getPendingVmemSize() {
        return vdsDynamic.getPendingVmemSize();
    }

    public void setPendingVmemSize(int value) {
        vdsDynamic.setPendingVmemSize(value);
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

    public String getPmProxyPreferences() {
        return vdsStatic.getPmProxyPreferences();
    }

    public void setPmProxyPreferences(String pmProxyPreferences) {
        vdsStatic.setPmProxyPreferences(pmProxyPreferences);
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

    public ArrayList<Network> getNetworks() {
        return networks;
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

    public String getPartialVersion() {
        return vdsDynamic.getVersion().getValue().substring(0, 2);
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

    public void calculateFreeVirtualMemory() {
        if (getMemCommited() != null && getPhysicalMemMb() != null && getReservedMem() != null) {
            maxSchedulingMemory = (getMaxVdsMemoryOverCommit() * getPhysicalMemMb() / 100.0f)
                    - getMemCommited()
                    - getReservedMem()
                    - getPendingVmemSize();
            // avoid negative values
            maxSchedulingMemory = maxSchedulingMemory > 0 ? maxSchedulingMemory : 0;
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

    public void setLiveSnapshotSupport(Boolean value) {
        vdsDynamic.setLiveSnapshotSupport(value);
    }

    public Boolean getLiveSnapshotSupport() {
        return vdsDynamic.getLiveSnapshotSupport();
    }

    public void setLiveMergeSupport(boolean value) {
        vdsDynamic.setLiveMergeSupport(value);
    }

    public boolean getLiveMergeSupport() {
        return vdsDynamic.getLiveMergeSupport();
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
}
