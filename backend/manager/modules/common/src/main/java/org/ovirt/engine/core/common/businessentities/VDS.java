package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    private VdsStatic mVdsStatic;
    private VdsDynamic mVdsDynamic;
    private VdsStatistics mVdsStatistics;
    private ArrayList<VdsNetworkInterface> mInterfaceList;
    private ArrayList<Network> mNetworkList;
    private String activeNic;
    private boolean countThreadsAsCores;

    /**
     * This map holds the disk usage reported by the host. The mapping is path to usage (in MB).
     */
    private Map<String, Long> localDisksUsage;

    public VDS() {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        storagePoolId = Guid.Empty;
        _spm_status = VdsSpmStatus.None;
        mInterfaceList = new ArrayList<VdsNetworkInterface>();
        mNetworkList = new ArrayList<Network>();
        this.setNumaNodeList(new ArrayList<VdsNumaNode>());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mVdsStatic == null) ? 0 : mVdsStatic.hashCode());
        result = prime * result + ((cpuName == null) ? 0 : cpuName.hashCode());
        result = prime * result + ((_spm_status == null) ? 0 : _spm_status.hashCode());
        result = prime * result + ((mImagesLastCheck == null) ? 0 : mImagesLastCheck.hashCode());
        result = prime * result + ((mImagesLastDelay == null) ? 0 : mImagesLastDelay.hashCode());
        result = prime * result + ((mInterfaceList == null) ? 0 : mInterfaceList.hashCode());
        result = prime * result + ((mNetworkList == null) ? 0 : mNetworkList.hashCode());
        result = prime * result + maxVdsMemoryOverCommit;
        result = prime * result + ((privateDomains == null) ? 0 : privateDomains.hashCode());
        result = prime * result + ((vdsSpmId == null) ? 0 : vdsSpmId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((storagePoolName == null) ? 0 : storagePoolName.hashCode());
        result = prime * result
                + ((vdsGroupCompatibilityVersion == null) ? 0 : vdsGroupCompatibilityVersion.hashCode());
        result = prime * result + ((vdsGroupCpuName == null) ? 0 : vdsGroupCpuName.hashCode());
        result = prime * result + ((vdsGroupDescription == null) ? 0 : vdsGroupDescription.hashCode());
        result = prime * result + ((vdsGroupName == null) ? 0 : vdsGroupName.hashCode());
        result = prime * result + ((vdsGroupVirtService == null) ? 0 : vdsGroupVirtService.hashCode());
        result = prime * result + ((vdsGroupGlusterService == null) ? 0 : vdsGroupGlusterService.hashCode());
        result = prime * result + (countThreadsAsCores ? 0 : 1);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VDS other = (VDS) obj;
        return (ObjectUtils.objectsEqual(mVdsStatic, other.mVdsStatic)
                && ObjectUtils.objectsEqual(cpuName, other.cpuName)
                && _spm_status == other._spm_status
                && ObjectUtils.objectsEqual(mImagesLastCheck, other.mImagesLastCheck)
                && ObjectUtils.objectsEqual(mImagesLastDelay, other.mImagesLastDelay)
                && ObjectUtils.objectsEqual(mInterfaceList, other.mInterfaceList)
                && ObjectUtils.objectsEqual(mNetworkList, other.mNetworkList)
                && maxVdsMemoryOverCommit == other.maxVdsMemoryOverCommit
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
        vds.setManagementIp(getManagementIp());
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
        vds.setpm_enabled(getpm_enabled());
        vds.setPmPassword(getPmPassword());
        vds.setPmPort(getPort());
        vds.setPmOptions(getPmOptions());
        vds.setPmType(getPmType());
        vds.setPmUser(getPmUser());
        vds.setPmSecondaryIp(getPmSecondaryIp());
        vds.setPmSecondaryType(getPmSecondaryType());
        vds.setPmSecondaryPort(getPmSecondaryPort());
        vds.setPmSecondaryOptions(getPmSecondaryOptions());
        vds.setPmSecondaryUser(getPmSecondaryUser());
        vds.setPmSecondaryPassword(getPmSecondaryPassword());
        vds.setPmSecondaryConcurrent(isPmSecondaryConcurrent());
        vds.setPmPort(getPmPort());
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
        vds.setNumaNodeList(getNumaNodeList());
        vds.setAutoNumaBalancing(getAutoNumaBalancing());

        return vds;
    }

    private Version vdsGroupCompatibilityVersion;

    public Version getVdsGroupCompatibilityVersion() {
        return this.vdsGroupCompatibilityVersion;
    }

    public boolean getContainingHooks() {
        // As VDSM reports the hooks in XMLRPCStruct that represents map of maps, we can assume that the string form of
        // the map begins with
        // { and ends with }
        String hooksStr = getHooksStr();
        return hooksStr != null && hooksStr.length() > 2;
    }

    public void setContainingHooks(boolean isContainingHooks) {
        // Empty setter - this is a calculated field
    }

    public void setHooksStr(String hooksStr) {
        getDynamicData().setHooksStr(hooksStr);
    }

    public String getHooksStr() {
        return getDynamicData().getHooksStr();
    }

    public void setVdsGroupCompatibilityVersion(Version value) {
        this.vdsGroupCompatibilityVersion = value;
    }

    public Guid getVdsGroupId() {
        return this.mVdsStatic.getVdsGroupId();
    }

    public void setVdsGroupId(Guid value) {
        this.mVdsStatic.setVdsGroupId(value);
    }

    private String vdsGroupName;

    public String getVdsGroupName() {
        return this.vdsGroupName;
    }

    public void setVdsGroupName(String value) {
        this.vdsGroupName = value;
    }

    private String vdsGroupDescription;

    public String getVdsGroupDescription() {
        return this.vdsGroupDescription;
    }

    public void setVdsGroupDescription(String value) {
        this.vdsGroupDescription = value;
    }

    private String vdsGroupCpuName;

    public String getVdsGroupCpuName() {
        return this.vdsGroupCpuName;
    }

    public void setVdsGroupCpuName(String value) {
        this.vdsGroupCpuName = value;
    }

    private Boolean vdsGroupVirtService;

    public Boolean getVdsGroupSupportsVirtService() {
        return this.vdsGroupVirtService;
    }

    public void setVdsGroupSupportsVirtService(Boolean value) {
        this.vdsGroupVirtService = value;
    }

    private Boolean vdsGroupGlusterService;

    public Boolean getVdsGroupSupportsGlusterService() {
        return this.vdsGroupGlusterService;
    }

    public void setVdsGroupSupportsGlusterService(Boolean value) {
        this.vdsGroupGlusterService = value;
    }

    @Override
    public Guid getId() {
        return this.mVdsStatic.getId();
    }

    @Override
    public void setId(Guid value) {
        this.mVdsStatic.setId(value);
        this.mVdsDynamic.setId(value);
        this.mVdsStatistics.setId(value);
    }

    @Override
    public String getName() {
        return this.mVdsStatic.getName();
    }

    public void setVdsName(String value) {
        this.mVdsStatic.setVdsName(value);
    }

    public String getManagementIp() {
        return this.mVdsStatic.getManagementIp();
    }

    public void setManagementIp(String value) {
        this.mVdsStatic.setManagementIp(value);
    }

    public String getUniqueId() {
        return mVdsStatic.getUniqueID();
    }

    public void setUniqueId(String value) {
        mVdsStatic.setUniqueID(value);
    }

    public String getHostName() {
        return this.mVdsStatic.getHostName();
    }

    public void setHostName(String value) {
        this.mVdsStatic.setHostName(value);
    }

    @Override
    public String getComment() {
        return mVdsStatic.getComment();
    }

    @Override
    public void setComment(String value) {
        mVdsStatic.setComment(value);
    }

    public int getPort() {
        return this.mVdsStatic.getPort();
    }

    public void setPort(int value) {
        this.mVdsStatic.setPort(value);
    }

    public VdsProtocol getProtocol() {
        return this.mVdsStatic.getProtocol();
    }

    public void setProtocol(VdsProtocol value) {
        this.mVdsStatic.setProtocol(value);
    }

    public int getSshPort() {
        return this.mVdsStatic.getSshPort();
    }

    public void setSshPort(int value) {
        this.mVdsStatic.setSshPort(value);
    }

    public String getSshUsername() {
        return this.mVdsStatic.getSshUsername();
    }

    public void setSshUsername(String value) {
        this.mVdsStatic.setSshUsername(value);
    }

    public boolean isServerSslEnabled() {
        return this.mVdsStatic.isServerSslEnabled();
    }

    public void setServerSslEnabled(boolean value) {
        this.mVdsStatic.setServerSslEnabled(value);
    }

    public VDSType getVdsType() {
        return this.mVdsStatic.getVdsType();
    }

    public void setVdsType(VDSType value) {
        this.mVdsStatic.setVdsType(value);
    }

    @Override
    public VDSStatus getStatus() {
        return this.mVdsDynamic.getStatus();
    }

    @Override
    public void setStatus(VDSStatus value) {
        this.mVdsDynamic.setStatus(value);
    }

    public Integer getCpuCores() {
        return this.mVdsDynamic.getcpu_cores();
    }

    public void setCpuCores(Integer value) {
        this.mVdsDynamic.setcpu_cores(value);
    }

    public Integer getCpuThreads() {
        return this.mVdsDynamic.getCpuThreads();
    }

    public void setCpuThreads(Integer value) {
        this.mVdsDynamic.setCpuThreads(value);
    }

    public String getHardwareUUID() {
        return this.mVdsDynamic.getHardwareUUID();
    }

    public String getHardwareManufacturer() {
        return this.mVdsDynamic.getHardwareManufacturer();
    }

    public String getHardwareFamily() {
        return this.mVdsDynamic.getHardwareFamily();
    }

    public String getHardwareSerialNumber() {
        return this.mVdsDynamic.getHardwareSerialNumber();
    }

    public String getHardwareProductName() {
        return this.mVdsDynamic.getHardwareProductName();
    }

    public String getHardwareVersion() {
        return this.mVdsDynamic.getHardwareVersion();
    }

    public void setHardwareUUID(String value) {
        this.mVdsDynamic.setHardwareUUID(value);
    }

    public void setHardwareFamily(String value) {
        this.mVdsDynamic.setHardwareFamily(value);
    }

    public void setHardwareSerialNumber(String value) {
        this.mVdsDynamic.setHardwareSerialNumber(value);
    }

    public void setHardwareVersion(String value) {
        this.mVdsDynamic.setHardwareVersion(value);
    }

    public void setHardwareProductName(String value) {
        this.mVdsDynamic.setHardwareProductName(value);
    }

    public void setHardwareManufacturer(String value) {
        this.mVdsDynamic.setHardwareManufacturer(value);
    }

    public Integer getCpuSockets() {
        return this.mVdsDynamic.getcpu_sockets();
    }

    public void setCpuSockets(Integer value) {
        this.mVdsDynamic.setcpu_sockets(value);
    }

    public String getCpuModel() {
        return this.mVdsDynamic.getcpu_model();
    }

    public void setCpuModel(String value) {
        this.mVdsDynamic.setcpu_model(value);
    }

    public String getOnlineCpus() {
        return this.mVdsDynamic.getOnlineCpus();
    }

    public void setOnlineCpus(String value) {
        this.mVdsDynamic.setOnlineCpus(value);
    }

    public Double getCpuSpeedMh() {
        return this.mVdsDynamic.getcpu_speed_mh();
    }

    public void setCpuSpeedMh(Double value) {
        this.mVdsDynamic.setcpu_speed_mh(value);
    }

    public String getIfTotalSpeed() {
        return this.mVdsDynamic.getif_total_speed();
    }

    public void setIfTotalSpeed(String value) {
        this.mVdsDynamic.setif_total_speed(value);
    }

    public Boolean getKvmEnabled() {
        return this.mVdsDynamic.getkvm_enabled();
    }

    public void setKvmEnabled(Boolean value) {
        this.mVdsDynamic.setkvm_enabled(value);
    }

    public Integer getPhysicalMemMb() {
        return this.mVdsDynamic.getphysical_mem_mb();
    }

    public void setPhysicalMemMb(Integer value) {
        this.mVdsDynamic.setphysical_mem_mb(value);
    }

    public String getSupportedClusterLevels() {
        return this.mVdsDynamic.getsupported_cluster_levels();
    }

    public void setSupportedClusterLevels(String value) {
        this.mVdsDynamic.setsupported_cluster_levels(value);
    }

    public HashSet<Version> getSupportedClusterVersionsSet() {
        return this.mVdsDynamic.getSupportedClusterVersionsSet();
    }

    public String getSupportedEngines() {
        return this.mVdsDynamic.getsupported_engines();
    }

    public void setSupportedEngines(String value) {
        this.mVdsDynamic.setsupported_engines(value);
    }

    public HashSet<Version> getSupportedENGINESVersionsSet() {
        return this.mVdsDynamic.getSupportedENGINESVersionsSet();
    }

    public Double getCpuIdle() {
        return this.mVdsStatistics.getcpu_idle();
    }

    public void setCpuIdle(Double value) {
        this.mVdsStatistics.setcpu_idle(value);
    }

    public Double getCpuLoad() {
        return this.mVdsStatistics.getcpu_load();
    }

    public void setCpuLoad(Double value) {
        this.mVdsStatistics.setcpu_load(value);
    }

    public Double getCpuSys() {
        return this.mVdsStatistics.getcpu_sys();
    }

    public void setCpuSys(Double value) {
        this.mVdsStatistics.setcpu_sys(value);
    }

    public Double getCpuUser() {
        return this.mVdsStatistics.getcpu_user();
    }

    public void setCpuUser(Double value) {
        this.mVdsStatistics.setcpu_user(value);
    }

    public Integer getMemCommited() {
        return this.mVdsDynamic.getmem_commited();
    }

    public void setMemCommited(Integer value) {
        this.mVdsDynamic.setmem_commited(value);
        calculateFreeVirtualMemory();
    }

    public Integer getVmActive() {
        return this.mVdsDynamic.getvm_active();
    }

    public void setVmActive(Integer value) {
        this.mVdsDynamic.setvm_active(value);
    }

    public int getHighlyAvailableScore() {
        return this.mVdsStatistics.getHighlyAvailableScore();
    }

    public void setHighlyAvailableScore(int value) {
        this.mVdsStatistics.setHighlyAvailableScore(value);
    }

    public boolean getHighlyAvailableIsConfigured() {
        return this.mVdsStatistics.getHighlyAvailableIsConfigured();
    }

    public void setHighlyAvailableIsConfigured(boolean value) {
        this.mVdsStatistics.setHighlyAvailableIsConfigured(value);
    }

    public boolean getHighlyAvailableIsActive() {
        return this.mVdsStatistics.getHighlyAvailableIsActive();
    }

    public void setHighlyAvailableIsActive(boolean value) {
        this.mVdsStatistics.setHighlyAvailableIsActive(value);
    }

    public boolean getHighlyAvailableGlobalMaintenance() {
        return this.mVdsStatistics.getHighlyAvailableGlobalMaintenance();
    }

    public void setHighlyAvailableGlobalMaintenance(boolean value) {
        this.mVdsStatistics.setHighlyAvailableGlobalMaintenance(value);
    }

    public boolean getHighlyAvailableLocalMaintenance() {
        return this.mVdsStatistics.getHighlyAvailableLocalMaintenance();
    }

    public void setHighlyAvailableLocalMaintenance(boolean value) {
        this.mVdsStatistics.setHighlyAvailableLocalMaintenance(value);
    }

    public int getVmCount() {
        return this.mVdsDynamic.getvm_count();
    }

    public void setVmCount(int value) {
        this.mVdsDynamic.setvm_count(value);
    }

    public Integer getVmsCoresCount() {
        return this.mVdsDynamic.getvms_cores_count();
    }

    public void setVmsCoresCount(Integer value) {
        this.mVdsDynamic.setvms_cores_count(value);
    }

    public Integer getVmMigrating() {
        return this.mVdsDynamic.getvm_migrating();
    }

    public void setVmMigrating(Integer value) {
        this.mVdsDynamic.setvm_migrating(value);
    }

    public Integer getUsageMemPercent() {
        return this.mVdsStatistics.getusage_mem_percent();
    }

    public void setUsageMemPercent(Integer value) {
        this.mVdsStatistics.setusage_mem_percent(value);
    }

    public Integer getUsageCpuPercent() {
        return this.mVdsStatistics.getusage_cpu_percent();
    }

    public void setUsageCpuPercent(Integer value) {
        this.mVdsStatistics.setusage_cpu_percent(value);
    }

    public Integer getUsageNetworkPercent() {
        return this.mVdsStatistics.getusage_network_percent();
    }

    public void setUsageNetworkPercent(Integer value) {
        this.mVdsStatistics.setusage_network_percent(value);
    }

    public Integer getGuestOverhead() {
        return this.mVdsDynamic.getguest_overhead();
    }

    public void setGuestOverhead(Integer value) {
        this.mVdsDynamic.setguest_overhead(value);
    }

    public Integer getReservedMem() {
        return this.mVdsDynamic.getreserved_mem();
    }
    public void setReservedMem(Integer value) {
        this.mVdsDynamic.setreserved_mem(value);
    }

    public Long getBootTime() {
        return this.mVdsStatistics.getboot_time();
    }

    public void setBootTime(Long value) {
        this.mVdsStatistics.setboot_time(value);
    }

    public VDSStatus getPreviousStatus() {
        return this.mVdsDynamic.getprevious_status();
    }

    public void setPreviousStatus(VDSStatus value) {
        this.mVdsDynamic.setprevious_status(value);
    }

    public Long getMemAvailable() {
        return this.mVdsStatistics.getmem_available();
    }

    public void setMemAvailable(Long value) {
        this.mVdsStatistics.setmem_available(value);
    }

    public Long getMemFree() {
        return this.mVdsStatistics.getMemFree();
    }

    public void setMemFree(Long value) {
        this.mVdsStatistics.setMemFree(value);
    }

    public Long getMemShared() {
        return this.mVdsStatistics.getmem_shared();
    }

    public void setMemShared(Long value) {
        this.mVdsStatistics.setmem_shared(value);
    }

    public String getConsoleAddress() {
        return mVdsStatic.getConsoleAddress();
    }

    public void setConsoleAddress(String value) {
        mVdsStatic.setConsoleAddress(value);
    }

    public Integer getMemCommitedPercent() {
        Integer commited = mVdsDynamic.getmem_commited();
        Integer physical = mVdsDynamic.getphysical_mem_mb();

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
        Long shared = mVdsStatistics.getmem_shared();
        Integer physical = mVdsDynamic.getphysical_mem_mb();

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
        return this.mVdsStatistics.getswap_free();
    }

    public void setSwapFree(Long value) {
        this.mVdsStatistics.setswap_free(value);
    }

    public Long getSwapTotal() {
        return this.mVdsStatistics.getswap_total();
    }

    public void setSwapTotal(Long value) {
        this.mVdsStatistics.setswap_total(value);
    }

    public Integer getKsmCpuPercent() {
        return this.mVdsStatistics.getksm_cpu_percent();
    }

    public void setKsmCpuPercent(Integer value) {
        this.mVdsStatistics.setksm_cpu_percent(value);
    }

    public Long getKsmPages() {
        return this.mVdsStatistics.getksm_pages();
    }

    public void setKsmPages(Long value) {
        this.mVdsStatistics.setksm_pages(value);
    }

    public Boolean getKsmState() {
        return this.mVdsStatistics.getksm_state();
    }

    public void setKsmState(Boolean value) {
        this.mVdsStatistics.setksm_state(value);
    }

    public String getSoftwareVersion() {
        return this.mVdsDynamic.getsoftware_version();
    }

    public void setSoftwareVersion(String value) {
        this.mVdsDynamic.setsoftware_version(value);
    }

    public String getVersionName() {
        return this.mVdsDynamic.getversion_name();
    }

    public void setVersionName(String value) {
        this.mVdsDynamic.setversion_name(value);
    }

    public String getBuildName() {
        return this.mVdsDynamic.getbuild_name();
    }

    public void setBuildName(String value) {
        this.mVdsDynamic.setbuild_name(value);
    }

    public String getCpuFlags() {
        return mVdsDynamic.getcpu_flags();
    }

    public void setCpuFlags(String value) {
        mVdsDynamic.setcpu_flags(value);
    }

    public Date getCpuOverCommitTimestamp() {
        return mVdsStatistics.getcpu_over_commit_time_stamp();
    }

    public void setCpuOverCommitTimestamp(Date value) {
        mVdsStatistics.setcpu_over_commit_time_stamp(value);
    }

    public int getVdsStrength() {
        return this.mVdsStatic.getVdsStrength();
    }

    public void setVdsStrength(int value) {
        this.mVdsStatic.setVdsStrength(value);
    }

    private Guid storagePoolId;

    @Override
    public Guid getStoragePoolId() {
        return this.storagePoolId;
    }

    @Override
    public void setStoragePoolId(Guid value) {
        this.storagePoolId = value;
    }

    private String storagePoolName;

    public String getStoragePoolName() {
        return this.storagePoolName;
    }

    public void setStoragePoolName(String value) {
        this.storagePoolName = value;
    }

    private int maxVdsMemoryOverCommit;

    public int getMaxVdsMemoryOverCommit() {
        return this.maxVdsMemoryOverCommit;
    }

    public void setMaxVdsMemoryOverCommit(int value) {
        this.maxVdsMemoryOverCommit = value;
    }

    public Integer getPendingVcpusCount() {
        return mVdsDynamic.getpending_vcpus_count();
    }

    public void setPendingVcpusCount(Integer value) {
        mVdsDynamic.setpending_vcpus_count(value);
    }

    public int getPendingVmemSize() {
        return mVdsDynamic.getpending_vmem_size();
    }

    public void setPendingVmemSize(int value) {
        mVdsDynamic.setpending_vmem_size(value);
    }

    public Boolean getNetConfigDirty() {
        return mVdsDynamic.getnet_config_dirty();
    }

    public void setNetConfigDirty(Boolean value) {
        mVdsDynamic.setnet_config_dirty(value);
    }

    public String getPmType() {
        return mVdsStatic.getPmType();
    }

    public void setPmType(String value) {
        mVdsStatic.setPmType(value);
    }

    public String getPmUser() {
        return mVdsStatic.getPmUser();
    }

    public void setPmUser(String value) {
        mVdsStatic.setPmUser(value);
    }

    public String getPmPassword() {
        return mVdsStatic.getPmPassword();
    }

    public void setPmPassword(String value) {
        mVdsStatic.setPmPassword(value);
    }

    public Integer getPmPort() {
        return mVdsStatic.getPmPort();
    }

    public void setPmPort(Integer value) {
        mVdsStatic.setPmPort(value);
    }

    public String getPmOptions() {
        return mVdsStatic.getPmOptions();
    }

    public void setPmOptions(String value) {
        mVdsStatic.setPmOptions(value);
    }

    public HashMap<String, String> getPmOptionsMap() {
        return mVdsStatic.getPmOptionsMap();
    }

    public HashMap<String, String> getPmSecondaryOptionsMap() {
        return mVdsStatic.getPmSecondaryOptionsMap();
    }

    public void setPmSecondaryOptionsMap(HashMap<String, String> value) {
        mVdsStatic.setPmSecondaryOptionsMap(value);
    }

    public boolean isPmKdumpDetection() {
        return mVdsStatic.isPmKdumpDetection();
    }

    public void setPmKdumpDetection(boolean pmKdumpDetection) {
        mVdsStatic.setPmKdumpDetection(pmKdumpDetection);
    }

    public void setPmOptionsMap(HashMap<String, String> value) {
        mVdsStatic.setPmOptionsMap(value);
    }

    public boolean getpm_enabled() {
        return mVdsStatic.isPmEnabled();
    }

    public void setpm_enabled(boolean value) {
        mVdsStatic.setPmEnabled(value);
    }

    public String getPmProxyPreferences() {
        return mVdsStatic.getPmProxyPreferences();
    }

    public void setPmProxyPreferences(String pmProxyPreferences) {
        mVdsStatic.setPmProxyPreferences(pmProxyPreferences);
    }

    public String getPmSecondaryIp() {
        return mVdsStatic.getPmSecondaryIp();
    }

    public void setPmSecondaryIp(String value) {
        mVdsStatic.setPmSecondaryIp(value);
    }

    public String getPmSecondaryType() {
        return mVdsStatic.getPmSecondaryType();
    }

    public void setPmSecondaryType(String value) {
        mVdsStatic.setPmSecondaryType(value);
    }

    public String getPmSecondaryUser() {
        return mVdsStatic.getPmSecondaryUser();
    }

    public void setPmSecondaryUser(String value) {
        mVdsStatic.setPmSecondaryUser(value);
    }

    public String getPmSecondaryPassword() {
        return mVdsStatic.getPmSecondaryPassword();
    }

    public void setPmSecondaryPassword(String value) {
        mVdsStatic.setPmSecondaryPassword(value);
    }

    public Integer getPmSecondaryPort() {
        return mVdsStatic.getPmSecondaryPort();
    }

    public void setPmSecondaryPort(Integer value) {
        mVdsStatic.setPmSecondaryPort(value);
    }

    public String getPmSecondaryOptions() {
        return mVdsStatic.getPmSecondaryOptions();
    }

    public void setPmSecondaryOptions(String value) {
        mVdsStatic.setPmSecondaryOptions(value);
    }

    public void setPmSecondaryPort(String value) {
        mVdsStatic.setPmSecondaryOptions(value);
    }

    public boolean isPmSecondaryConcurrent() {
        return mVdsStatic.isPmSecondaryConcurrent();
    }

    public void setPmSecondaryConcurrent(boolean value) {
        mVdsStatic.setPmSecondaryConcurrent(value);
    }

    public String getHostOs() {
        return this.mVdsDynamic.gethost_os();
    }

    public void setHostOs(String value) {
        this.mVdsDynamic.sethost_os(value);
    }

    public String getKvmVersion() {
        return this.mVdsDynamic.getkvm_version();
    }

    public void setKvmVersion(String value) {
        this.mVdsDynamic.setkvm_version(value);
    }

    public RpmVersion getLibvirtVersion() {
        return this.mVdsDynamic.getlibvirt_version();
    }

    public void setLibvirtVersion(RpmVersion value) {
        this.mVdsDynamic.setlibvirt_version(value);
    }

    public String getSpiceVersion() {
        return this.mVdsDynamic.getspice_version();
    }

    public void setSpiceVersion(String value) {
        this.mVdsDynamic.setspice_version(value);
    }

    public RpmVersion getGlusterVersion() {
        return this.mVdsDynamic.getGlusterVersion();
    }

    public void setGlusterVersion(RpmVersion value) {
        this.mVdsDynamic.setGlusterVersion(value);
    }

    public String getKernelVersion() {
        return this.mVdsDynamic.getkernel_version();
    }

    public void setKernelVersion(String value) {
        this.mVdsDynamic.setkernel_version(value);
    }

    public void setIScsiInitiatorName(String value) {
        this.mVdsDynamic.setIScsiInitiatorName(value);
    }

    public String getIScsiInitiatorName() {
        return this.mVdsDynamic.getIScsiInitiatorName();
    }

    public Map<String, List<Map<String, String>>> getHBAs() {
        return this.mVdsDynamic.getHBAs();
    }

    public void setHBAs(Map<String, List<Map<String, String>>> HBAs) {
        this.mVdsDynamic.setHBAs(HBAs);
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        this.mVdsDynamic.setTransparentHugePagesState(value);
    }

    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return this.mVdsDynamic.getTransparentHugePagesState();
    }

    public int getAnonymousHugePages() {
        return this.mVdsStatistics.getAnonymousHugePages();
    }

    public void setAnonymousHugePages(int value) {
        this.mVdsStatistics.setAnonymousHugePages(value);
    }

    public VdsStatic getStaticData() {
        return mVdsStatic;
    }

    public void setStaticData(VdsStatic value) {
        mVdsStatic = value;
    }

    public VdsDynamic getDynamicData() {
        return mVdsDynamic;
    }

    public void setDynamicData(VdsDynamic value) {
        mVdsDynamic = value;
    }

    public VdsStatistics getStatisticsData() {
        return mVdsStatistics;
    }

    public void setStatisticsData(VdsStatistics value) {
        mVdsStatistics = value;
    }

    public ArrayList<Network> getNetworks() {
        return this.mNetworkList;
    }

    public ArrayList<VdsNetworkInterface> getInterfaces() {
        return this.mInterfaceList;
    }

    private ArrayList<VDSDomainsData> privateDomains;

    public ArrayList<VDSDomainsData> getDomains() {
        return privateDomains;
    }

    public void setDomains(ArrayList<VDSDomainsData> value) {
        privateDomains = value;
    }

    private Double mImagesLastCheck;
    private Double mImagesLastDelay;

    public Double getImagesLastCheck() {
        return mImagesLastCheck;
    }

    public void setImagesLastCheck(Double value) {
        mImagesLastCheck = value;
    }

    public Double getImagesLastDelay() {
        return mImagesLastDelay;
    }

    public void setImagesLastDelay(Double value) {
        mImagesLastDelay = value;
    }

    public void setVersion(RpmVersion value) {
        mVdsDynamic.setVersion(value);
    }

    public RpmVersion getVersion() {
        return mVdsDynamic.getVersion();
    }

    public String getPartialVersion() {
        return mVdsDynamic.getVersion().getValue().substring(0, 2);
    }

    private ServerCpu cpuName;

    public ServerCpu getCpuName() {
        return cpuName;
    }

    public void setCpuName(ServerCpu value) {
        cpuName = value;
    }

    private Integer vdsSpmId;

    public Integer getVdsSpmId() {
        return vdsSpmId;
    }

    public void setVdsSpmId(Integer value) {
        vdsSpmId = value;
    }

    public long getOtpValidity() {
        return mVdsStatic.getOtpValidity();
    }

    public void setOtpValidity(long value) {
        mVdsStatic.setOtpValidity(value);
    }

    public int getVdsSpmPriority() {
        return mVdsStatic.getVdsSpmPriority();
    }

    public void setVdsSpmPriority(int value) {
        mVdsStatic.setVdsSpmPriority(value);
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private VdsSpmStatus _spm_status;

    public VdsSpmStatus getSpmStatus() {
        return _spm_status;
    }

    public void setSpmStatus(VdsSpmStatus value) {
        _spm_status = value;
    }

    public boolean isSpm() {
        return _spm_status == VdsSpmStatus.SPM;
    }

    public NonOperationalReason getNonOperationalReason() {
        return this.mVdsDynamic.getNonOperationalReason();
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.mVdsDynamic.setNonOperationalReason(nonOperationalReason);
    }

    public Map<String, Long> getLocalDisksUsage() {
        return localDisksUsage;
    }

    public void setLocalDisksUsage(Map<String, Long> localDiskUsage) {
        this.localDisksUsage = localDiskUsage;
    }

    public boolean isAutoRecoverable() {
        return mVdsStatic.isAutoRecoverable();
    }

    public void setAutoRecoverable(boolean autoRecoverable) {
        mVdsStatic.setAutoRecoverable(autoRecoverable);
    }

    public String getSshKeyFingerprint() {
        return mVdsStatic.getSshKeyFingerprint();
    }

    public void setSshKeyFingerprint(String sshKeyFingerprint) {
        mVdsStatic.setSshKeyFingerprint(sshKeyFingerprint);
    }

    public Guid getHostProviderId() {
        return mVdsStatic.getHostProviderId();
    }

    public void setHostProviderId(Guid hostProviderId) {
        mVdsStatic.setHostProviderId(hostProviderId);
    }

    private float maxSchedulingMemory;

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
        // note that mVdsStatic may be null, so the getName with no null protection
        // is not enough, remove this once mVdsStatic can not be null
        return "Host[" + (mVdsStatic == null ? "null" : (mVdsStatic.getName() + "," + mVdsStatic.getId())) + "]";
    }

    public String getActiveNic() {
        return activeNic;
    }

    public void setActiveNic(String activeNic) {
        this.activeNic = activeNic;
    }

    public void setSupportedEmulatedMachines(String supportedEmulatedMachines) {
        mVdsDynamic.setSupportedEmulatedMachines(supportedEmulatedMachines);
    }

    public String getSupportedEmulatedMachines() {
        return mVdsDynamic.getSupportedEmulatedMachines();
    }

    public boolean isPowerManagementControlledByPolicy() {
        return mVdsDynamic.isPowerManagementControlledByPolicy();
    }

    public void setPowerManagementControlledByPolicy(boolean powerManagementControlledByPolicy) {
        mVdsDynamic.setPowerManagementControlledByPolicy(powerManagementControlledByPolicy);
    }

    public boolean isDisablePowerManagementPolicy() {
        return mVdsStatic.isDisablePowerManagementPolicy();
    }

    public void setDisablePowerManagementPolicy(boolean disablePowerManagementPolicy) {
        mVdsStatic.setDisablePowerManagementPolicy(disablePowerManagementPolicy);
    }

    public Set<VmRngDevice.Source> getSupportedRngSources() {
        return mVdsDynamic.getSupportedRngSources();
    }

    public KdumpStatus getKdumpStatus() {
        return mVdsDynamic.getKdumpStatus();
    }

    public void setKdumpStatus(KdumpStatus kdumpStatus) {
        mVdsDynamic.setKdumpStatus(kdumpStatus);
    }

    public SELinuxMode getSELinuxEnforceMode() {
        return mVdsDynamic.getSELinuxEnforceMode();
    }

    public void setSELinuxEnforceMode(Integer value) {
        mVdsDynamic.setSELinuxEnforceMode(value);
    }

    public void setNumaNodeList(List<VdsNumaNode> numaNodeList) {
        mVdsDynamic.setNumaNodeList(numaNodeList);
    }

    public List<VdsNumaNode> getNumaNodeList() {
        return mVdsDynamic.getNumaNodeList();
    }

    /**
     * If host enables the feature of auto numa balancing.
     */
    public AutoNumaBalanceStatus getAutoNumaBalancing() {
        return mVdsDynamic.getAutoNumaBalancing();
    }

    public void setAutoNumaBalancing(AutoNumaBalanceStatus autoNumaBalancing) {
        mVdsDynamic.setAutoNumaBalancing(autoNumaBalancing);
    }

    /**
     * If host supports numa.
     */
    public boolean isNumaSupport() {
        return mVdsDynamic.isNumaSupport();
    }

    public void setNumaSupport(boolean numaSupport) {
        mVdsDynamic.setNumaSupport(numaSupport);
    }

    public void setLiveSnapshotSupport(Boolean value) {
        this.mVdsDynamic.setLiveSnapshotSupport(value);
    }

    public Boolean getLiveSnapshotSupport() {
        return this.mVdsDynamic.getLiveSnapshotSupport();
    }

    public void setLiveMergeSupport(boolean value) {
        this.mVdsDynamic.setLiveMergeSupport(value);
    }

    public boolean getLiveMergeSupport() {
        return this.mVdsDynamic.getLiveMergeSupport();
    }

    public void setCountThreadsAsCores(boolean value) {
        this.countThreadsAsCores = value;
    }

    public boolean getCountThreadsAsCores() {
        return countThreadsAsCores;
    }

}
