package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

public class VDS extends IVdcQueryable implements Serializable, BusinessEntity<Guid>, HasStoragePool<Guid>, Nameable {
    private static final long serialVersionUID = -7893976203379789926L;
    private VdsStatic mVdsStatic;
    private VdsDynamic mVdsDynamic;
    private VdsStatistics mVdsStatistics;
    private ArrayList<VdsNetworkInterface> mInterfaceList;
    private java.util.ArrayList<Network> mNetworkList;

    /**
     * This map holds the disk usage reported by the host. The mapping is path to usage (in MB).
     */
    private Map<String, Long> localDisksUsage;

    public VDS() {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        mInterfaceList = new java.util.ArrayList<VdsNetworkInterface>();
        mNetworkList = new java.util.ArrayList<Network>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cpuName == null) ? 0 : cpuName.hashCode());
        result = prime * result + ((_spm_status == null) ? 0 : _spm_status.hashCode());
        result = prime * result + cpuOverCommitDurationMinutes;
        result = prime * result + highUtilization;
        result = prime * result + lowUtilization;
        result = prime * result + ((mImagesLastCheck == null) ? 0 : mImagesLastCheck.hashCode());
        result = prime * result + ((mImagesLastDelay == null) ? 0 : mImagesLastDelay.hashCode());
        result = prime * result + ((mInterfaceList == null) ? 0 : mInterfaceList.hashCode());
        result = prime * result + ((mNetworkList == null) ? 0 : mNetworkList.hashCode());
        result = prime * result + ((mVdsDynamic == null) ? 0 : mVdsDynamic.hashCode());
        result = prime * result + ((mVdsStatic == null) ? 0 : mVdsStatic.hashCode());
        result = prime * result + ((mVdsStatistics == null) ? 0 : mVdsStatistics.hashCode());
        result = prime * result + maxVdsMemoryOverCommit;
        result = prime * result + ((privateDomains == null) ? 0 : privateDomains.hashCode());
        result = prime * result + ((vdsSpmId == null) ? 0 : vdsSpmId.hashCode());
        result = prime * result + ((selectionAlgorithm == null) ? 0 : selectionAlgorithm.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        result = prime * result + ((storagePoolName == null) ? 0 : storagePoolName.hashCode());
        result =
                prime
                        * result
                        + ((vdsGroupCompatibilityVersion == null) ? 0
                                : vdsGroupCompatibilityVersion.hashCode());
        result = prime * result + ((vdsGroupCpuName == null) ? 0 : vdsGroupCpuName.hashCode());
        result = prime * result + ((vdsGroupDescription == null) ? 0 : vdsGroupDescription.hashCode());
        result = prime * result + ((vdsGroupName == null) ? 0 : vdsGroupName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VDS other = (VDS) obj;
        if (cpuName == null) {
            if (other.cpuName != null)
                return false;
        } else if (!cpuName.equals(other.cpuName))
            return false;
        if (_spm_status != other._spm_status)
            return false;
        if (cpuOverCommitDurationMinutes != other.cpuOverCommitDurationMinutes)
            return false;
        if (highUtilization != other.highUtilization)
            return false;
        if (lowUtilization != other.lowUtilization)
            return false;
        if (mImagesLastCheck == null) {
            if (other.mImagesLastCheck != null)
                return false;
        } else if (!mImagesLastCheck.equals(other.mImagesLastCheck))
            return false;
        if (mImagesLastDelay == null) {
            if (other.mImagesLastDelay != null)
                return false;
        } else if (!mImagesLastDelay.equals(other.mImagesLastDelay))
            return false;
        if (mInterfaceList == null) {
            if (other.mInterfaceList != null)
                return false;
        } else if (!mInterfaceList.equals(other.mInterfaceList))
            return false;
        if (mNetworkList == null) {
            if (other.mNetworkList != null)
                return false;
        } else if (!mNetworkList.equals(other.mNetworkList))
            return false;
        if (mVdsStatic == null) {
            if (other.mVdsStatic != null)
                return false;
        } else if (!mVdsStatic.equals(other.mVdsStatic))
            return false;
        if (maxVdsMemoryOverCommit != other.maxVdsMemoryOverCommit)
            return false;
        if (privateDomains == null) {
            if (other.privateDomains != null)
                return false;
        } else if (!privateDomains.equals(other.privateDomains))
            return false;
        if (vdsSpmId == null) {
            if (other.vdsSpmId != null)
                return false;
        } else if (!vdsSpmId.equals(other.vdsSpmId))
            return false;
        if (selectionAlgorithm != other.selectionAlgorithm)
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        if (storagePoolName == null) {
            if (other.storagePoolName != null)
                return false;
        } else if (!storagePoolName.equals(other.storagePoolName))
            return false;
        if (vdsGroupCompatibilityVersion == null) {
            if (other.vdsGroupCompatibilityVersion != null)
                return false;
        } else if (!vdsGroupCompatibilityVersion.equals(other.vdsGroupCompatibilityVersion))
            return false;
        if (vdsGroupCpuName == null) {
            if (other.vdsGroupCpuName != null)
                return false;
        } else if (!vdsGroupCpuName.equals(other.vdsGroupCpuName))
            return false;
        if (vdsGroupDescription == null) {
            if (other.vdsGroupDescription != null)
                return false;
        } else if (!vdsGroupDescription.equals(other.vdsGroupDescription))
            return false;
        if (vdsGroupName == null) {
            if (other.vdsGroupName != null)
                return false;
        } else if (!vdsGroupName.equals(other.vdsGroupName))
            return false;
        return true;
    }

    public VDS(Guid vds_group_id, String vds_group_name, String vds_group_description, Guid vds_id, String vds_name,
            String ip, String host_name, int port, int status, Integer cpu_cores, Integer cpuThreads, String cpu_model,
            String hwManufacturer, String hwProductName,
            String hwVersion, String hwSerialNumber, String hwUUID, String hwFamily,
            Double cpu_speed_mh, String if_total_speed, Boolean kvm_enabled, Integer physical_mem_mb,
            Double cpu_idle, Double cpu_load, Double cpu_sys,
            Double cpu_user, Integer mem_commited, Integer vm_active, int vm_count,
            Integer vm_migrating, Integer usage_mem_percent, Integer usage_cpu_percent, Integer usage_network_percent,
            Integer reserved_mem, Integer guest_overhead, VDSStatus previous_status, String software_version,
            String version_name, String build_name, Long mem_available, Long mem_shared, boolean server_SSL_enabled,
            String vds_group_cpu_name, String cpu_name, Boolean net_config_dirty, String pm_type, String pm_user,
            String pm_password, Integer pm_port, String pm_options, boolean pm_enabled, String pmSecondaryIp,
            String pmSecondaryType, String pmSecondaryUser, String pmSecondaryPassword, Integer pmSecondaryPort,
            String pmSecondaryOptions, boolean pmSecondaryConcurrent, String consoleAddress)
    {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        mInterfaceList = new java.util.ArrayList<VdsNetworkInterface>();
        mNetworkList = new java.util.ArrayList<Network>();
        this.setVdsGroupId(vds_group_id);
        this.vdsGroupName = vds_group_name;
        this.vdsGroupDescription = vds_group_description;
        this.setId(vds_id);
        this.setVdsName(vds_name);
        this.setManagmentIp(ip);
        this.setHostName(host_name);
        this.setPort(port);
        this.setStatus(VDSStatus.forValue(status));
        this.setHardwareManufacturer(hwManufacturer);
        this.setHardwareProductName(hwProductName);
        this.setHardwareVersion(hwVersion);
        this.setHardwareSerialNumber(hwSerialNumber);
        this.setHardwareUUID(hwUUID);
        this.setHardwareFamily(hwFamily);
        this.setCpuCores(cpu_cores);
        this.setCpuThreads(cpuThreads);
        this.setCpuModel(cpu_model);
        this.setCpuSpeedMh(cpu_speed_mh);
        this.setIfTotalSpeed(if_total_speed);
        this.setKvmEnabled(kvm_enabled);
        this.setPhysicalMemMb(physical_mem_mb);
        this.setCpuIdle(cpu_idle);
        this.setCpuLoad(cpu_load);
        this.setCpuSys(cpu_sys);
        this.setCpuUser(cpu_user);
        this.setMemCommited(mem_commited);
        this.setVmActive(vm_active);
        this.setVmCount(vm_count);
        this.setVmMigrating(vm_migrating);
        this.setUsageMemPercent(usage_mem_percent);
        this.setUsageCpuPercent(usage_cpu_percent);
        this.setUsageNetworkPercent(usage_network_percent);
        this.setReservedMem(reserved_mem);
        this.setGuestOverhead(guest_overhead);
        this.setPreviousStatus(previous_status);
        this.setMemAvailable(mem_available);
        this.setMemShared(mem_shared);
        this.setSoftwareVersion(software_version);
        this.setVersionName(version_name);
        this.setServerSslEnabled(server_SSL_enabled);
        this.vdsGroupCpuName = vds_group_cpu_name;
        this.setCpuFlags(getCpuFlags());
        this.setNetConfigDirty(net_config_dirty);
        // Power Management
        this.setpm_enabled(pm_enabled);
        this.setPmPassword(pm_password);
        this.setPmPort(pm_port);
        this.setPmOptions(pm_options);
        this.setPmType(pm_type);
        this.setPmUser(pm_user);
        this.setPmSecondaryIp(pmSecondaryIp);
        this.setPmSecondaryType(pmSecondaryType);
        this.setPmSecondaryPort(pmSecondaryPort);
        this.setPmSecondaryUser(pmSecondaryUser);
        this.setPmSecondaryPassword(pmSecondaryPassword);
        this.setPmSecondaryConcurrent(pmSecondaryConcurrent);
        this.setConsoleAddress(consoleAddress);
    }

    public VDS(VdsStatic vdsStatic, VdsDynamic vdsDynamic, VdsStatistics vdsStatistics) {
        this.mVdsStatic = vdsStatic;
        this.mVdsDynamic = vdsDynamic;
        this.mVdsStatistics = vdsStatistics;
    }

    public VDS clone() {
        VDS vds =
                new VDS(Guid.createGuidFromString(getVdsGroupId().toString()),
                        getVdsGroupName(),
                        getVdsGroupDescription(),
                        Guid.createGuidFromString(getId().toString()),
                        getName(),
                        getManagmentIp(),
                        getHostName(),
                        getPort(),
                        getStatus().getValue(),
                        getCpuCores(),
                        getCpuThreads(),
                        getCpuModel(),
                        getHardwareManufacturer(),
                        getHardwareProductName(),
                        getHardwareVersion(),
                        getHardwareSerialNumber(),
                        getHardwareUUID(),
                        getHardwareFamily(),
                        getCpuSpeedMh(),
                        getIfTotalSpeed(),
                        getKvmEnabled(),
                        getPhysicalMemMb(),
                        getCpuIdle(),
                        getCpuLoad(),
                        getCpuSys(),
                        getCpuUser(),
                        getMemCommited(),
                        getVmActive(),
                        getVmCount(),
                        getVmMigrating(),
                        getUsageMemPercent(),
                        getUsageCpuPercent(),
                        getUsageNetworkPercent(),
                        getReservedMem(),
                        getGuestOverhead(),
                        getPreviousStatus(),
                        getSoftwareVersion(),
                        getVersionName(),
                        getBuildName(),
                        getMemAvailable(),
                        getMemShared(),
                        isServerSslEnabled(),
                        getVdsGroupCpuName(),
                        "",
                        getNetConfigDirty(),
                        getPmType(),
                        getPmUser(),
                        getPmPassword(),
                        getPmPort(),
                        getPmOptions(),
                        getpm_enabled(),
                        getPmSecondaryIp(),
                        getPmSecondaryType(),
                        getPmSecondaryUser(),
                        getPmSecondaryPassword(),
                        getPmSecondaryPort(),
                        getPmSecondaryOptions(),
                        isPmSecondaryConcurrent(),
                        getConsoleAddress());

        vds.setCpuFlags(getCpuFlags());
        vds.setVdsSpmPriority(getVdsSpmPriority());
        vds.setOtpValidity(getOtpValidity());
        vds.setKernelVersion(getKernelVersion());
        vds.setKvmVersion(getKvmVersion());
        vds.setLibvirtVersion(getLibvirtVersion());
        vds.setHooksStr(getHooksStr());

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
        if (Version.OpInequality(vdsGroupCompatibilityVersion, value)) {
            this.vdsGroupCompatibilityVersion = value;
        }
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

    public String getManagmentIp() {
        return this.mVdsStatic.getManagmentIp();
    }

    public void setManagmentIp(String value) {
        this.mVdsStatic.setManagmentIp(value);
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

    public int getPort() {
        return this.mVdsStatic.getPort();
    }

    public void setPort(int value) {
        this.mVdsStatic.setPort(value);
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

    public VDSStatus getStatus() {
        return this.mVdsDynamic.getstatus();
    }

    public void setStatus(VDSStatus value) {
        this.mVdsDynamic.setstatus(value);
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

    public java.util.HashSet<Version> getSupportedClusterVersionsSet() {
        return this.mVdsDynamic.getSupportedClusterVersionsSet();
    }

    public String getSupportedEngines() {
        return this.mVdsDynamic.getsupported_engines();
    }

    public void setSupportedEngines(String value) {
        this.mVdsDynamic.setsupported_engines(value);
    }

    public java.util.HashSet<Version> getSupportedENGINESVersionsSet() {
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

        return ((int) (shared * 100) / physical);
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
        return mVdsDynamic.getcpu_over_commit_time_stamp();
    }

    public void setCpuOverCommitTimestamp(java.util.Date value) {
        mVdsDynamic.setcpu_over_commit_time_stamp(value);
    }

    public int getVdsStrength() {
        return this.mVdsStatic.getVdsStrength();
    }

    public void setVdsStrength(int value) {
        this.mVdsStatic.setVdsStrength(value);
    }

    private int highUtilization;

    public int getHighUtilization() {
        return this.highUtilization;
    }

    public void setHighUtilization(int value) {
        this.highUtilization = value;
    }

    private int lowUtilization;

    public int getLowUtilization() {
        return this.lowUtilization;
    }

    public void setLowUtilization(int value) {
        this.lowUtilization = value;
    }

    private int cpuOverCommitDurationMinutes;

    public int getCpuOverCommitDurationMinutes() {
        return this.cpuOverCommitDurationMinutes;
    }

    public void setCpuOverCommitDurationMinutes(int value) {
        this.cpuOverCommitDurationMinutes = value;
    }

    private Guid storagePoolId = new Guid();

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

    private VdsSelectionAlgorithm selectionAlgorithm = VdsSelectionAlgorithm.forValue(0);

    public VdsSelectionAlgorithm getSelectionAlgorithm() {
        return this.selectionAlgorithm;
    }

    public void setSelectionAlgorithm(VdsSelectionAlgorithm value) {
        this.selectionAlgorithm = value;
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

    public ValueObjectMap getPmOptionsMap() {
        return mVdsStatic.getPmOptionsMap();
    }

    public ValueObjectMap getPmSecondaryOptionsMap() {
        return mVdsStatic.getPmSecondaryOptionsMap();
    }

    public void setPmSecondaryOptionsMap(ValueObjectMap value) {
        mVdsStatic.setPmSecondaryOptionsMap(value);
    }
    public void setPmOptionsMap(ValueObjectMap value) {
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

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        this.mVdsDynamic.setTransparentHugePagesState(value);
    }

    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return this.mVdsDynamic.getTransparentHugePagesState();
    }

    public int getAnonymousHugePages() {
        return this.mVdsDynamic.getAnonymousHugePages();
    }

    public void setAnonymousHugePages(int value) {
        this.mVdsDynamic.setAnonymousHugePages(value);
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

    public java.util.ArrayList<Network> getNetworks() {
        return this.mNetworkList;
    }

    public java.util.ArrayList<VdsNetworkInterface> getInterfaces() {
        return this.mInterfaceList;
    }

    private java.util.ArrayList<VDSDomainsData> privateDomains;

    public java.util.ArrayList<VDSDomainsData> getDomains() {
        return privateDomains;
    }

    public void setDomains(java.util.ArrayList<VDSDomainsData> value) {
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

    private VdsSpmStatus _spm_status = VdsSpmStatus.forValue(0);

    public VdsSpmStatus getSpmStatus() {
        return _spm_status;
    }

    public void setSpmStatus(VdsSpmStatus value) {
        _spm_status = value;
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

    public String getSSHKeyFingerprint() {
        return mVdsStatic.getSSHKeyFingerprint();
    }

    public void setSSHKeyFingerprint(String sshKeyFingerprint) {
        mVdsStatic.setSSHKeyFingerprint(sshKeyFingerprint);
    }

    private float maxSchedulingMemory;

    public void calculateFreeVirtualMemory() {
        if (getMemCommited() != null && getPhysicalMemMb() != null && getReservedMem() != null) {
            maxSchedulingMemory = (getMaxVdsMemoryOverCommit() * getPhysicalMemMb() / 100.0f) -
                    (getMemCommited() + getReservedMem());
            // avoid negative values
            maxSchedulingMemory = maxSchedulingMemory > 0 ? maxSchedulingMemory : 0;
        }
    }

    public float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

    public String toString() {
        // note that mVdsStatic may be null, so the getName with no null protection
        // is not enough, remove this once mVdsStatic can not be null
        return "Host[" + (mVdsStatic == null ? "null" : mVdsStatic.getName()) + "]";
    }

}
