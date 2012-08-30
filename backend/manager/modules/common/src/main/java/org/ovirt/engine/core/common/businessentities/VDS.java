package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
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
        result = prime * result + ((_cpuName == null) ? 0 : _cpuName.hashCode());
        result = prime * result + ((_spm_status == null) ? 0 : _spm_status.hashCode());
        result = prime * result + cpu_over_commit_duration_minutesField;
        result = prime * result + high_utilizationField;
        result = prime * result + low_utilizationField;
        result = prime * result + ((mImagesLastCheck == null) ? 0 : mImagesLastCheck.hashCode());
        result = prime * result + ((mImagesLastDelay == null) ? 0 : mImagesLastDelay.hashCode());
        result = prime * result + ((mInterfaceList == null) ? 0 : mInterfaceList.hashCode());
        result = prime * result + ((mNetworkList == null) ? 0 : mNetworkList.hashCode());
        result = prime * result + ((mVdsDynamic == null) ? 0 : mVdsDynamic.hashCode());
        result = prime * result + ((mVdsStatic == null) ? 0 : mVdsStatic.hashCode());
        result = prime * result + ((mVdsStatistics == null) ? 0 : mVdsStatistics.hashCode());
        result = prime * result + max_vds_memory_over_commitField;
        result = prime * result + ((privateDomains == null) ? 0 : privateDomains.hashCode());
        result = prime * result + ((privatevds_spm_id == null) ? 0 : privatevds_spm_id.hashCode());
        result = prime * result + ((selection_algorithmField == null) ? 0 : selection_algorithmField.hashCode());
        result = prime * result + ((storage_pool_idField == null) ? 0 : storage_pool_idField.hashCode());
        result = prime * result + ((storage_pool_nameField == null) ? 0 : storage_pool_nameField.hashCode());
        result =
                prime
                        * result
                        + ((vds_group_compatibility_versionField == null) ? 0
                                : vds_group_compatibility_versionField.hashCode());
        result = prime * result + ((vds_group_cpu_nameField == null) ? 0 : vds_group_cpu_nameField.hashCode());
        result = prime * result + ((vds_group_descriptionField == null) ? 0 : vds_group_descriptionField.hashCode());
        result = prime * result + ((vds_group_nameField == null) ? 0 : vds_group_nameField.hashCode());
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
        if (_cpuName == null) {
            if (other._cpuName != null)
                return false;
        } else if (!_cpuName.equals(other._cpuName))
            return false;
        if (_spm_status != other._spm_status)
            return false;
        if (cpu_over_commit_duration_minutesField != other.cpu_over_commit_duration_minutesField)
            return false;
        if (high_utilizationField != other.high_utilizationField)
            return false;
        if (low_utilizationField != other.low_utilizationField)
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
        if (max_vds_memory_over_commitField != other.max_vds_memory_over_commitField)
            return false;
        if (privateDomains == null) {
            if (other.privateDomains != null)
                return false;
        } else if (!privateDomains.equals(other.privateDomains))
            return false;
        if (privatevds_spm_id == null) {
            if (other.privatevds_spm_id != null)
                return false;
        } else if (!privatevds_spm_id.equals(other.privatevds_spm_id))
            return false;
        if (selection_algorithmField != other.selection_algorithmField)
            return false;
        if (storage_pool_idField == null) {
            if (other.storage_pool_idField != null)
                return false;
        } else if (!storage_pool_idField.equals(other.storage_pool_idField))
            return false;
        if (storage_pool_nameField == null) {
            if (other.storage_pool_nameField != null)
                return false;
        } else if (!storage_pool_nameField.equals(other.storage_pool_nameField))
            return false;
        if (vds_group_compatibility_versionField == null) {
            if (other.vds_group_compatibility_versionField != null)
                return false;
        } else if (!vds_group_compatibility_versionField.equals(other.vds_group_compatibility_versionField))
            return false;
        if (vds_group_cpu_nameField == null) {
            if (other.vds_group_cpu_nameField != null)
                return false;
        } else if (!vds_group_cpu_nameField.equals(other.vds_group_cpu_nameField))
            return false;
        if (vds_group_descriptionField == null) {
            if (other.vds_group_descriptionField != null)
                return false;
        } else if (!vds_group_descriptionField.equals(other.vds_group_descriptionField))
            return false;
        if (vds_group_nameField == null) {
            if (other.vds_group_nameField != null)
                return false;
        } else if (!vds_group_nameField.equals(other.vds_group_nameField))
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
            String pmSecondaryOptions, boolean pmSecondaryConcurrent)
    {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        mInterfaceList = new java.util.ArrayList<VdsNetworkInterface>();
        mNetworkList = new java.util.ArrayList<Network>();
        this.setvds_group_id(vds_group_id);
        this.vds_group_nameField = vds_group_name;
        this.vds_group_descriptionField = vds_group_description;
        this.setId(vds_id);
        this.setvds_name(vds_name);
        this.setManagmentIp(ip);
        this.sethost_name(host_name);
        this.setport(port);
        this.setstatus(VDSStatus.forValue(status));
        this.setHardwareManufacturer(hwManufacturer);
        this.setHardwareProductName(hwProductName);
        this.setHardwareVersion(hwVersion);
        this.setHardwareSerialNumber(hwSerialNumber);
        this.setHardwareUUID(hwUUID);
        this.setHardwareFamily(hwFamily);
        this.setcpu_cores(cpu_cores);
        this.setCpuThreads(cpuThreads);
        this.setcpu_model(cpu_model);
        this.setcpu_speed_mh(cpu_speed_mh);
        this.setif_total_speed(if_total_speed);
        this.setkvm_enabled(kvm_enabled);
        this.setphysical_mem_mb(physical_mem_mb);
        this.setcpu_idle(cpu_idle);
        this.setcpu_load(cpu_load);
        this.setcpu_sys(cpu_sys);
        this.setcpu_user(cpu_user);
        this.setmem_commited(mem_commited);
        this.setvm_active(vm_active);
        this.setvm_count(vm_count);
        this.setvm_migrating(vm_migrating);
        this.setusage_mem_percent(usage_mem_percent);
        this.setusage_cpu_percent(usage_cpu_percent);
        this.setusage_network_percent(usage_network_percent);
        this.setreserved_mem(reserved_mem);
        this.setguest_overhead(guest_overhead);
        this.setprevious_status(previous_status);
        this.setmem_available(mem_available);
        this.setmem_shared(mem_shared);
        this.setsoftware_version(software_version);
        this.setversion_name(version_name);
        this.setserver_SSL_enabled(server_SSL_enabled);
        this.vds_group_cpu_nameField = vds_group_cpu_name;
        this.setcpu_flags(getcpu_flags());
        this.setnet_config_dirty(net_config_dirty);
        // Power Management
        this.setpm_enabled(pm_enabled);
        this.setpm_password(pm_password);
        this.setpm_port(pm_port);
        this.setpm_options(pm_options);
        this.setpm_type(pm_type);
        this.setpm_user(pm_user);
        this.setPmSecondaryIp(pmSecondaryIp);
        this.setPmSecondaryType(pmSecondaryType);
        this.setPmSecondaryPort(pmSecondaryPort);
        this.setPmSecondaryUser(pmSecondaryUser);
        this.setPmSecondaryPassword(pmSecondaryPassword);
        this.setPmSecondaryConcurrent(pmSecondaryConcurrent);
    }

    public VDS(VdsStatic vdsStatic, VdsDynamic vdsDynamic, VdsStatistics vdsStatistics) {
        this.mVdsStatic = vdsStatic;
        this.mVdsDynamic = vdsDynamic;
        this.mVdsStatistics = vdsStatistics;
    }

    public VDS clone() {
        VDS vds =
                new VDS(Guid.createGuidFromString(getvds_group_id().toString()),
                        getvds_group_name(),
                        getvds_group_description(),
                        Guid.createGuidFromString(getId().toString()),
                        getvds_name(),
                        getManagmentIp(),
                        gethost_name(),
                        getport(),
                        getstatus().getValue(),
                        getcpu_cores(),
                        getCpuThreads(),
                        getcpu_model(),
                        getHardwareManufacturer(),
                        getHardwareProductName(),
                        getHardwareVersion(),
                        getHardwareSerialNumber(),
                        getHardwareUUID(),
                        getHardwareFamily(),
                        getcpu_speed_mh(),
                        getif_total_speed(),
                        getkvm_enabled(),
                        getphysical_mem_mb(),
                        getcpu_idle(),
                        getcpu_load(),
                        getcpu_sys(),
                        getcpu_user(),
                        getmem_commited(),
                        getvm_active(),
                        getvm_count(),
                        getvm_migrating(),
                        getusage_mem_percent(),
                        getusage_cpu_percent(),
                        getusage_network_percent(),
                        getreserved_mem(),
                        getguest_overhead(),
                        getprevious_status(),
                        getsoftware_version(),
                        getversion_name(),
                        getbuild_name(),
                        getmem_available(),
                        getmem_shared(),
                        getserver_SSL_enabled(),
                        getvds_group_cpu_name(),
                        "",
                        getnet_config_dirty(),
                        getpm_type(),
                        getpm_user(),
                        getpm_password(),
                        getpm_port(),
                        getpm_options(),
                        getpm_enabled(),
                        getPmSecondaryIp(),
                        getPmSecondaryType(),
                        getPmSecondaryuser(),
                        getPmSecondaryPassword(),
                        getPmSecondaryPort(),
                        getPmSecondaryOptions(),
                        isPmSecondaryConcurrent());

        vds.setcpu_flags(getcpu_flags());
        vds.setVdsSpmPriority(getVdsSpmPriority());
        vds.setOtpValidity(getOtpValidity());
        vds.setkernel_version(getkernel_version());
        vds.setkvm_version(getkvm_version());
        vds.setlibvirt_version(getlibvirt_version());
        vds.setHooksStr(getHooksStr());

        return vds;
    }

    private Version vds_group_compatibility_versionField;

    public Version getvds_group_compatibility_version() {
        return this.vds_group_compatibility_versionField;
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

    public void setvds_group_compatibility_version(Version value) {
        if (Version.OpInequality(vds_group_compatibility_versionField, value)) {
            this.vds_group_compatibility_versionField = value;
        }
    }

    public Guid getvds_group_id() {
        return this.mVdsStatic.getvds_group_id();
    }

    public void setvds_group_id(Guid value) {
        this.mVdsStatic.setvds_group_id(value);
    }

    private String vds_group_nameField;

    public String getvds_group_name() {
        return this.vds_group_nameField;
    }

    public void setvds_group_name(String value) {
        this.vds_group_nameField = value;
    }

    private String vds_group_descriptionField;

    public String getvds_group_description() {
        return this.vds_group_descriptionField;
    }

    public void setvds_group_description(String value) {
        this.vds_group_descriptionField = value;
    }

    private String vds_group_cpu_nameField;

    public String getvds_group_cpu_name() {
        return this.vds_group_cpu_nameField;
    }

    public void setvds_group_cpu_name(String value) {
        this.vds_group_cpu_nameField = value;
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

    public String getvds_name() {
        return this.mVdsStatic.getvds_name();
    }

    public void setvds_name(String value) {
        this.mVdsStatic.setvds_name(value);
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

    public String gethost_name() {
        return this.mVdsStatic.gethost_name();
    }

    public void sethost_name(String value) {
        this.mVdsStatic.sethost_name(value);
    }

    public int getport() {
        return this.mVdsStatic.getport();
    }

    public void setport(int value) {
        this.mVdsStatic.setport(value);
    }

    public boolean getserver_SSL_enabled() {
        return this.mVdsStatic.getserver_SSL_enabled();
    }

    public void setserver_SSL_enabled(boolean value) {
        this.mVdsStatic.setserver_SSL_enabled(value);
    }

    public VDSType getvds_type() {
        return this.mVdsStatic.getvds_type();
    }

    public void setvds_type(VDSType value) {
        this.mVdsStatic.setvds_type(value);
    }

    public VDSStatus getstatus() {
        return this.mVdsDynamic.getstatus();
    }

    public void setstatus(VDSStatus value) {
        this.mVdsDynamic.setstatus(value);
    }

    public Integer getcpu_cores() {
        return this.mVdsDynamic.getcpu_cores();
    }

    public void setcpu_cores(Integer value) {
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

    public Integer getcpu_sockets() {
        return this.mVdsDynamic.getcpu_sockets();
    }

    public void setcpu_sockets(Integer value) {
        this.mVdsDynamic.setcpu_sockets(value);
    }

    public String getcpu_model() {
        return this.mVdsDynamic.getcpu_model();
    }

    public void setcpu_model(String value) {
        this.mVdsDynamic.setcpu_model(value);
    }

    public Double getcpu_speed_mh() {
        return this.mVdsDynamic.getcpu_speed_mh();
    }

    public void setcpu_speed_mh(Double value) {
        this.mVdsDynamic.setcpu_speed_mh(value);
    }

    public String getif_total_speed() {
        return this.mVdsDynamic.getif_total_speed();
    }

    public void setif_total_speed(String value) {
        this.mVdsDynamic.setif_total_speed(value);
    }

    public Boolean getkvm_enabled() {
        return this.mVdsDynamic.getkvm_enabled();
    }

    public void setkvm_enabled(Boolean value) {
        this.mVdsDynamic.setkvm_enabled(value);
    }

    public Integer getphysical_mem_mb() {
        return this.mVdsDynamic.getphysical_mem_mb();
    }

    public void setphysical_mem_mb(Integer value) {
        this.mVdsDynamic.setphysical_mem_mb(value);
    }

    public String getsupported_cluster_levels() {
        return this.mVdsDynamic.getsupported_cluster_levels();
    }

    public void setsupported_cluster_levels(String value) {
        this.mVdsDynamic.setsupported_cluster_levels(value);
    }

    public java.util.HashSet<Version> getSupportedClusterVersionsSet() {
        return this.mVdsDynamic.getSupportedClusterVersionsSet();
    }

    public String getsupported_engines() {
        return this.mVdsDynamic.getsupported_engines();
    }

    public void setsupported_engines(String value) {
        this.mVdsDynamic.setsupported_engines(value);
    }

    public java.util.HashSet<Version> getSupportedENGINESVersionsSet() {
        return this.mVdsDynamic.getSupportedENGINESVersionsSet();
    }

    public Double getcpu_idle() {
        return this.mVdsStatistics.getcpu_idle();
    }

    public void setcpu_idle(Double value) {
        this.mVdsStatistics.setcpu_idle(value);
    }

    public Double getcpu_load() {
        return this.mVdsStatistics.getcpu_load();
    }

    public void setcpu_load(Double value) {
        this.mVdsStatistics.setcpu_load(value);
    }

    public Double getcpu_sys() {
        return this.mVdsStatistics.getcpu_sys();
    }

    public void setcpu_sys(Double value) {
        this.mVdsStatistics.setcpu_sys(value);
    }

    public Double getcpu_user() {
        return this.mVdsStatistics.getcpu_user();
    }

    public void setcpu_user(Double value) {
        this.mVdsStatistics.setcpu_user(value);
    }

    public Integer getmem_commited() {
        return this.mVdsDynamic.getmem_commited();
    }

    public void setmem_commited(Integer value) {
        this.mVdsDynamic.setmem_commited(value);
        calculateFreeVirtualMemory();
    }

    public Integer getvm_active() {
        return this.mVdsDynamic.getvm_active();
    }

    public void setvm_active(Integer value) {
        this.mVdsDynamic.setvm_active(value);
    }

    public int getvm_count() {
        return this.mVdsDynamic.getvm_count();
    }

    public void setvm_count(int value) {
        this.mVdsDynamic.setvm_count(value);
    }

    public Integer getvms_cores_count() {
        return this.mVdsDynamic.getvms_cores_count();
    }

    public void setvms_cores_count(Integer value) {
        this.mVdsDynamic.setvms_cores_count(value);
    }

    public Integer getvm_migrating() {
        return this.mVdsDynamic.getvm_migrating();
    }

    public void setvm_migrating(Integer value) {
        this.mVdsDynamic.setvm_migrating(value);
    }

    public Integer getusage_mem_percent() {
        return this.mVdsStatistics.getusage_mem_percent();
    }

    public void setusage_mem_percent(Integer value) {
        this.mVdsStatistics.setusage_mem_percent(value);
    }

    public Integer getusage_cpu_percent() {
        return this.mVdsStatistics.getusage_cpu_percent();
    }

    public void setusage_cpu_percent(Integer value) {
        this.mVdsStatistics.setusage_cpu_percent(value);
    }

    public Integer getusage_network_percent() {
        return this.mVdsStatistics.getusage_network_percent();
    }

    public void setusage_network_percent(Integer value) {
        this.mVdsStatistics.setusage_network_percent(value);
    }

    public Integer getguest_overhead() {
        return this.mVdsDynamic.getguest_overhead();
    }

    public void setguest_overhead(Integer value) {
        this.mVdsDynamic.setguest_overhead(value);
    }

    public Integer getreserved_mem() {
        return this.mVdsDynamic.getreserved_mem();
    }

    public void setreserved_mem(Integer value) {
        this.mVdsDynamic.setreserved_mem(value);
    }

    public VDSStatus getprevious_status() {
        return this.mVdsDynamic.getprevious_status();
    }

    public void setprevious_status(VDSStatus value) {
        this.mVdsDynamic.setprevious_status(value);
    }

    public Long getmem_available() {
        return this.mVdsStatistics.getmem_available();
    }

    public void setmem_available(Long value) {
        this.mVdsStatistics.setmem_available(value);
    }

    public Long getmem_shared() {
        return this.mVdsStatistics.getmem_shared();
    }

    public void setmem_shared(Long value) {
        this.mVdsStatistics.setmem_shared(value);
    }

    public Integer getmem_commited_percent() {
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
    public void setmem_commited_percent(Integer value) {

    }

    public Integer getmem_shared_percent() {
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
    public void setmem_shared_percent(Integer value) {

    }

    public Long getswap_free() {
        return this.mVdsStatistics.getswap_free();
    }

    public void setswap_free(Long value) {
        this.mVdsStatistics.setswap_free(value);
    }

    public Long getswap_total() {
        return this.mVdsStatistics.getswap_total();
    }

    public void setswap_total(Long value) {
        this.mVdsStatistics.setswap_total(value);
    }

    public Integer getksm_cpu_percent() {
        return this.mVdsStatistics.getksm_cpu_percent();
    }

    public void setksm_cpu_percent(Integer value) {
        this.mVdsStatistics.setksm_cpu_percent(value);
    }

    public Long getksm_pages() {
        return this.mVdsStatistics.getksm_pages();
    }

    public void setksm_pages(Long value) {
        this.mVdsStatistics.setksm_pages(value);
    }

    public Boolean getksm_state() {
        return this.mVdsStatistics.getksm_state();
    }

    public void setksm_state(Boolean value) {
        this.mVdsStatistics.setksm_state(value);
    }

    public String getsoftware_version() {
        return this.mVdsDynamic.getsoftware_version();
    }

    public void setsoftware_version(String value) {
        this.mVdsDynamic.setsoftware_version(value);
    }

    public String getversion_name() {
        return this.mVdsDynamic.getversion_name();
    }

    public void setversion_name(String value) {
        this.mVdsDynamic.setversion_name(value);
    }

    public String getbuild_name() {
        return this.mVdsDynamic.getbuild_name();
    }

    public void setbuild_name(String value) {
        this.mVdsDynamic.setbuild_name(value);
    }

    public String getcpu_flags() {
        return mVdsDynamic.getcpu_flags();
    }

    public void setcpu_flags(String value) {
        mVdsDynamic.setcpu_flags(value);
    }

    public java.util.Date getcpu_over_commit_time_stamp() {
        return mVdsDynamic.getcpu_over_commit_time_stamp();
    }

    public void setcpu_over_commit_time_stamp(java.util.Date value) {
        mVdsDynamic.setcpu_over_commit_time_stamp(value);
    }

    public int getvds_strength() {
        return this.mVdsStatic.getvds_strength();
    }

    public void setvds_strength(int value) {
        this.mVdsStatic.setvds_strength(value);
    }

    private int high_utilizationField;

    public int gethigh_utilization() {
        return this.high_utilizationField;
    }

    public void sethigh_utilization(int value) {
        this.high_utilizationField = value;
    }

    private int low_utilizationField;

    public int getlow_utilization() {
        return this.low_utilizationField;
    }

    public void setlow_utilization(int value) {
        this.low_utilizationField = value;
    }

    private int cpu_over_commit_duration_minutesField;

    public int getcpu_over_commit_duration_minutes() {
        return this.cpu_over_commit_duration_minutesField;
    }

    public void setcpu_over_commit_duration_minutes(int value) {
        this.cpu_over_commit_duration_minutesField = value;
    }

    private Guid storage_pool_idField = new Guid();

    @Override
    public Guid getStoragePoolId() {
        return this.storage_pool_idField;
    }

    @Override
    public void setStoragePoolId(Guid value) {
        this.storage_pool_idField = value;
    }

    private String storage_pool_nameField;

    public String getstorage_pool_name() {
        return this.storage_pool_nameField;
    }

    public void setstorage_pool_name(String value) {
        this.storage_pool_nameField = value;
    }

    private VdsSelectionAlgorithm selection_algorithmField = VdsSelectionAlgorithm.forValue(0);

    public VdsSelectionAlgorithm getselection_algorithm() {
        return this.selection_algorithmField;
    }

    public void setselection_algorithm(VdsSelectionAlgorithm value) {
        this.selection_algorithmField = value;
    }

    private int max_vds_memory_over_commitField;

    public int getmax_vds_memory_over_commit() {
        return this.max_vds_memory_over_commitField;
    }

    public void setmax_vds_memory_over_commit(int value) {
        this.max_vds_memory_over_commitField = value;
    }

    public Integer getpending_vcpus_count() {
        return mVdsDynamic.getpending_vcpus_count();
    }

    public void setpending_vcpus_count(Integer value) {
        mVdsDynamic.setpending_vcpus_count(value);
    }

    public int getpending_vmem_size() {
        return mVdsDynamic.getpending_vmem_size();
    }

    public void setpending_vmem_size(int value) {
        mVdsDynamic.setpending_vmem_size(value);
    }

    public Boolean getnet_config_dirty() {
        return mVdsDynamic.getnet_config_dirty();
    }

    public void setnet_config_dirty(Boolean value) {
        mVdsDynamic.setnet_config_dirty(value);
    }

    public String getpm_type() {
        return mVdsStatic.getpm_type();
    }

    public void setpm_type(String value) {
        mVdsStatic.setpm_type(value);
    }

    public String getpm_user() {
        return mVdsStatic.getpm_user();
    }

    public void setpm_user(String value) {
        mVdsStatic.setpm_user(value);
    }

    public String getpm_password() {
        return mVdsStatic.getpm_password();
    }

    public void setpm_password(String value) {
        mVdsStatic.setpm_password(value);
    }

    public Integer getpm_port() {
        return mVdsStatic.getpm_port();
    }

    public void setpm_port(Integer value) {
        mVdsStatic.setpm_port(value);
    }

    public String getpm_options() {
        return mVdsStatic.getpm_options();
    }

    public void setpm_options(String value) {
        mVdsStatic.setpm_options(value);
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
        return mVdsStatic.getpm_enabled();
    }

    public void setpm_enabled(boolean value) {
        mVdsStatic.setpm_enabled(value);
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

    public String getPmSecondaryuser() {
        return mVdsStatic.getPmSecondaryuser();
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

    public String gethost_os() {
        return this.mVdsDynamic.gethost_os();
    }

    public void sethost_os(String value) {
        this.mVdsDynamic.sethost_os(value);
    }

    public String getkvm_version() {
        return this.mVdsDynamic.getkvm_version();
    }

    public void setkvm_version(String value) {
        this.mVdsDynamic.setkvm_version(value);
    }

    public RpmVersion getlibvirt_version() {
        return this.mVdsDynamic.getlibvirt_version();
    }

    public void setlibvirt_version(RpmVersion value) {
        this.mVdsDynamic.setlibvirt_version(value);
    }

    public String getspice_version() {
        return this.mVdsDynamic.getspice_version();
    }

    public void setspice_version(String value) {
        this.mVdsDynamic.setspice_version(value);
    }

    public String getkernel_version() {
        return this.mVdsDynamic.getkernel_version();
    }

    public void setkernel_version(String value) {
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

    private ServerCpu _cpuName;

    public ServerCpu getCpuName() {
        return _cpuName;
    }

    public void setCpuName(ServerCpu value) {
        _cpuName = value;
    }

    private Integer privatevds_spm_id;

    public Integer getvds_spm_id() {
        return privatevds_spm_id;
    }

    public void setvds_spm_id(Integer value) {
        privatevds_spm_id = value;
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

    public VdsSpmStatus getspm_status() {
        return _spm_status;
    }

    public void setspm_status(VdsSpmStatus value) {
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
        if (getmem_commited() != null && getphysical_mem_mb() != null && getreserved_mem() != null) {
            maxSchedulingMemory = (getmax_vds_memory_over_commit() * getphysical_mem_mb() / 100.0f) -
                    (getmem_commited() + getreserved_mem());
            // avoid negative values
            maxSchedulingMemory = maxSchedulingMemory > 0 ? maxSchedulingMemory : 0;
        }
    }

    public float getMaxSchedulingMemory() {
        return maxSchedulingMemory;
    }

    @Override
    public String getName() {
        return getvds_name();
    }

    public String toString() {
        // note that mVdsStatic may be null, so the getName with no null protection
        // is not enough, remove this once mVdsStatic can not be null
        return "Host[" + (mVdsStatic == null ? "null" : mVdsStatic.getvds_name()) + "]";
    }
}
