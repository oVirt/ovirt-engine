package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

public class VdsDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -6010035855157006935L;

    private Guid id;

    private VDSStatus status;

    private Integer cpu_cores;

    private Integer cpuThreads;

    private String cpu_model;

    private BigDecimal cpu_speed_mh;

    private String if_total_speed;

    private Boolean kvm_enabled;

    private Integer physical_mem_mb;

    private Integer mem_commited;

    private Integer vm_active;

    private int vm_count;

    private Integer vm_migrating;

    private Integer reserved_mem ;

    private Integer guest_overhead;

    private String softwareVersion;

    private String versionName;

    private String buildName;

    private VDSStatus previous_status;

    private String cpu_flags;

    private Date cpu_over_commit_time_stamp;

    private Integer vms_cores_count;

    private Integer pending_vcpus_count;

    private Integer cpu_sockets;

    private Boolean net_config_dirty;
    private String supported_cluster_levels;

    private String supported_engines;

    private String host_os;

    private String kvm_version;

    private RpmVersion libvirt_version;

    private String spice_version;

    private String kernel_version;

    private String iScsiInitiatorName;

    private VdsTransparentHugePagesState transparentHugePagesState;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    private Map<String, List<Map<String, String>>> HBAs; /* Store the list of HBAs */

    public Map<String, List<Map<String, String>>> getHBAs() {
        return HBAs;
    }

    public void setHBAs(Map<String, List<Map<String, String>>> HBAs) {
        this.HBAs = HBAs;
    }

    private int anonymousHugePages;

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

    private Integer pending_vmem_size;

    private RpmVersion rpmVersion;

    private java.util.HashSet<Version> _supportedClusterVersionsSet;

    private java.util.HashSet<Version> _supportedENGINESVersionsSet;

    /**
     * comma separated list of emulated machines the host supports
     */
    private String supportedEmulatedMachines;

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

    public VdsDynamic() {
        rpmVersion = new RpmVersion();
        libvirt_version = new RpmVersion();
        status = VDSStatus.Unassigned;
        previous_status = VDSStatus.Unassigned;
        nonOperationalReason = NonOperationalReason.NONE;
        cpu_speed_mh = BigDecimal.valueOf(0.0);
        mem_commited = 0;
        reserved_mem = 1024;
        pending_vcpus_count = 0;
        pending_vmem_size = 0;
        transparentHugePagesState = VdsTransparentHugePagesState.Never;
        vm_count = 0;
        vms_cores_count = 0;
        guest_overhead = 0;
    }

    public Integer getcpu_cores() {
        return this.cpu_cores;
    }

    public void setcpu_cores(Integer value) {
        this.cpu_cores = value;
    }

    public Integer getCpuThreads() {
        return this.cpuThreads;
    }

    public void setCpuThreads(Integer value) {
        this.cpuThreads = value;
    }

    public Integer getcpu_sockets() {
        return this.cpu_sockets;
    }

    public void setcpu_sockets(Integer value) {
        this.cpu_sockets = value;
    }

    public String getcpu_model() {
        return this.cpu_model;
    }

    public void setcpu_model(String value) {
        this.cpu_model = value;
    }

    public Double getcpu_speed_mh() {
        return this.cpu_speed_mh.doubleValue();
    }

    public void setcpu_speed_mh(Double value) {
        this.cpu_speed_mh = BigDecimal.valueOf(value);
    }

    public String getif_total_speed() {
        return this.if_total_speed;
    }

    public void setif_total_speed(String value) {
        this.if_total_speed = value;
    }

    public Boolean getkvm_enabled() {
        return this.kvm_enabled;
    }

    public void setkvm_enabled(Boolean value) {
        this.kvm_enabled = value;
    }

    public Integer getmem_commited() {
        return this.mem_commited;
    }

    public void setmem_commited(Integer value) {
        this.mem_commited = value;
    }

    public Integer getphysical_mem_mb() {
        return this.physical_mem_mb;
    }

    public void setphysical_mem_mb(Integer value) {
        this.physical_mem_mb = value;
    }

    public VDSStatus getstatus() {
        return status;
    }

    public void setstatus(VDSStatus value) {
        this.status = value;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Integer getvm_active() {
        return this.vm_active;
    }

    public void setvm_active(Integer value) {
        this.vm_active = value;
    }

    public int getvm_count() {
        return this.vm_count;
    }

    public void setvm_count(int value) {
        this.vm_count = value;
    }

    public Integer getvms_cores_count() {
        return this.vms_cores_count;
    }

    public void setvms_cores_count(Integer value) {
        this.vms_cores_count = value;
    }

    public Integer getvm_migrating() {
        return this.vm_migrating;
    }

    public void setvm_migrating(Integer value) {
        this.vm_migrating = value;
    }

    public Integer getreserved_mem() {
        return this.reserved_mem;
    }

    public void setreserved_mem(Integer value) {
        this.reserved_mem = value;
    }

    public Integer getguest_overhead() {
        return this.guest_overhead;
    }

    public void setguest_overhead(Integer value) {
        this.guest_overhead = value;
    }

    public VDSStatus getprevious_status() {
        return this.previous_status;
    }

    public void setprevious_status(VDSStatus value) {
        this.previous_status = value;
    }

    public String getsoftware_version() {
       return this.softwareVersion;
    }

    public void setsoftware_version(String value) {
        this.softwareVersion = value;
    }

    public String getversion_name() {
        return versionName;
    }

    public void setversion_name(String value) {
        this.versionName = value;
    }

    public String getcpu_flags() {
        return this.cpu_flags;
    }

    public void setcpu_flags(String value) {
        this.cpu_flags = value;
    }

    public Date getcpu_over_commit_time_stamp() {
        return this.cpu_over_commit_time_stamp;
    }

    public void setcpu_over_commit_time_stamp(Date value) {
        this.cpu_over_commit_time_stamp = value;
    }

    public Integer getpending_vcpus_count() {
        return this.pending_vcpus_count;
    }

    public void setpending_vcpus_count(Integer value) {
        this.pending_vcpus_count = value;
    }

    public int getpending_vmem_size() {
        return this.pending_vmem_size;
    }

    public void setpending_vmem_size(int value) {
        this.pending_vmem_size = value;
    }

    public Boolean getnet_config_dirty() {
        return this.net_config_dirty;
    }

    public void setnet_config_dirty(Boolean value) {
        this.net_config_dirty = value;
    }

    public String getsupported_cluster_levels() {
        return supported_cluster_levels;
    }

    public void setsupported_cluster_levels(String value) {
        supported_cluster_levels = value;
    }

    public HashSet<Version> getSupportedClusterVersionsSet() {
        if (_supportedClusterVersionsSet == null) {
            _supportedClusterVersionsSet = parseSupportedVersions(getsupported_cluster_levels());
        }
        return _supportedClusterVersionsSet;
    }

    public String getsupported_engines() {
        return supported_engines;
    }

    public void setsupported_engines(String value) {
        supported_engines = value;
    }

    public HashSet<Version> getSupportedENGINESVersionsSet() {
        if (_supportedENGINESVersionsSet == null) {
            _supportedENGINESVersionsSet = parseSupportedVersions(getsupported_engines());
        }
        return _supportedENGINESVersionsSet;
    }

    public String getHardwareUUID() {
        return this.hwUUID;
    }

    public void setHardwareUUID(String value) {
        this.hwUUID = value;
    }

    public String getHardwareFamily() {
        return this.hwFamily;
    }

    public void setHardwareFamily(String value) {
        this.hwFamily = value;
    }

    public String getHardwareSerialNumber() {
        return this.hwSerialNumber;
    }

    public void setHardwareSerialNumber(String value) {
        this.hwSerialNumber = value;
    }

    public String getHardwareVersion() {
        return this.hwVersion;
    }

    public void setHardwareVersion(String value) {
        this.hwVersion = value;
    }

    public String getHardwareProductName() {
        return this.hwProductName;
    }

    public void setHardwareProductName(String value) {
        this.hwProductName = value;
    }

    public String getHardwareManufacturer() {
        return this.hwManufacturer;
    }

    public void setHardwareManufacturer(String value) {
        this.hwManufacturer = value;
    }

    /**
     * Used to parse a string containing concatenated list of versions, delimited by a comma.
     *
     * @param supportedVersions
     *            a string contains a concatenated list of supported versions
     * @returns a set of the parsed versions, or null if {@code supportedVersions} provided empty.
     * @throws RuntimeException
     *             thrown in case and parsing a version fails
     */
    private HashSet<Version> parseSupportedVersions(String supportedVersions) {
        HashSet<Version> parsedVersions = null;
        if (!StringHelper.isNullOrEmpty(supportedVersions)) {
            parsedVersions = new HashSet<Version>();
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

    public String gethost_os() {
        return this.host_os;
    }

    public void sethost_os(String value) {
        this.host_os = value;
    }

    public String getkvm_version() {
        return this.kvm_version;
    }

    public void setkvm_version(String value) {
        this.kvm_version = value;
    }

    public RpmVersion getlibvirt_version() {
        return this.libvirt_version;
    }

    public void setlibvirt_version(RpmVersion value) {
        this.libvirt_version = value;
    }

    public String getspice_version() {
        return this.spice_version;
    }

    public void setspice_version(String value) {
        this.spice_version = value;
    }

    public String getkernel_version() {
        return this.kernel_version;
    }

    public void setkernel_version(String value) {
        this.kernel_version = value;
    }

    public String getbuild_name() {
        return this.buildName;
    }

    public void setbuild_name(String value) {
        this.buildName = value;
    }

    public String getIScsiInitiatorName() {
        return this.iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value) {
        this.iScsiInitiatorName = value;
    }

    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return this.transparentHugePagesState;
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        this.transparentHugePagesState = value;
    }

    public int getAnonymousHugePages() {
        return this.anonymousHugePages;
    }

    public void setAnonymousHugePages(int value) {
        this.anonymousHugePages = value;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((_supportedClusterVersionsSet == null) ? 0 : _supportedClusterVersionsSet.hashCode());
        result = prime * result + ((_supportedENGINESVersionsSet == null) ? 0 : _supportedENGINESVersionsSet.hashCode());
        result = prime * result + anonymousHugePages;
        result = prime * result + ((buildName == null) ? 0 : buildName.hashCode());
        result = prime * result + ((cpu_cores == null) ? 0 : cpu_cores.hashCode());
        result = prime * result + ((cpuThreads == null) ? 0 : cpuThreads.hashCode());
        result = prime * result + ((cpu_flags == null) ? 0 : cpu_flags.hashCode());
        result = prime * result + ((cpu_model == null) ? 0 : cpu_model.hashCode());
        result = prime * result + ((cpu_over_commit_time_stamp == null) ? 0 : cpu_over_commit_time_stamp.hashCode());
        result = prime * result + ((cpu_sockets == null) ? 0 : cpu_sockets.hashCode());
        result = prime * result + ((cpu_speed_mh == null) ? 0 : cpu_speed_mh.hashCode());
        result = prime * result + ((guest_overhead == null) ? 0 : guest_overhead.hashCode());
        result = prime * result + ((hooksStr == null) ? 0 : hooksStr.hashCode());
        result = prime * result + ((host_os == null) ? 0 : host_os.hashCode());
        result = prime * result + ((iScsiInitiatorName == null) ? 0 : iScsiInitiatorName.hashCode());
        result = prime * result + ((if_total_speed == null) ? 0 : if_total_speed.hashCode());
        result = prime * result + ((kernel_version == null) ? 0 : kernel_version.hashCode());
        result = prime * result + ((kvm_enabled == null) ? 0 : kvm_enabled.hashCode());
        result = prime * result + ((kvm_version == null) ? 0 : kvm_version.hashCode());
        result = prime * result + ((libvirt_version == null) ? 0 : libvirt_version.hashCode());
        result = prime * result + ((rpmVersion == null) ? 0 : rpmVersion.hashCode());
        result = prime * result + ((mem_commited == null) ? 0 : mem_commited.hashCode());
        result = prime * result + ((net_config_dirty == null) ? 0 : net_config_dirty.hashCode());
        result = prime * result + ((nonOperationalReason == null) ? 0 : nonOperationalReason.hashCode());
        result = prime * result + ((pending_vcpus_count == null) ? 0 : pending_vcpus_count.hashCode());
        result = prime * result + ((pending_vmem_size == null) ? 0 : pending_vmem_size.hashCode());
        result = prime * result + ((physical_mem_mb == null) ? 0 : physical_mem_mb.hashCode());
        result = prime * result + ((previous_status == null) ? 0 : previous_status.hashCode());
        result = prime * result + ((reserved_mem == null) ? 0 : reserved_mem.hashCode());
        result = prime * result + ((softwareVersion == null) ? 0 : softwareVersion.hashCode());
        result = prime * result + ((spice_version == null) ? 0 : spice_version.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((supported_cluster_levels == null) ? 0 : supported_cluster_levels.hashCode());
        result = prime * result + ((supported_engines == null) ? 0 : supported_engines.hashCode());
        result = prime * result + ((transparentHugePagesState == null) ? 0 : transparentHugePagesState.hashCode());
        result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
        result = prime * result + ((vm_active == null) ? 0 : vm_active.hashCode());
        result = prime * result + vm_count;
        result = prime * result + ((vm_migrating == null) ? 0 : vm_migrating.hashCode());
        result = prime * result + ((vms_cores_count == null) ? 0 : vms_cores_count.hashCode());
        result = prime * result + ((hwManufacturer == null) ? 0 : hwManufacturer.hashCode());
        result = prime * result + ((hwProductName == null) ? 0 : hwProductName.hashCode());
        result = prime * result + ((hwVersion == null) ? 0 : hwVersion.hashCode());
        result = prime * result + ((hwSerialNumber == null) ? 0 : hwSerialNumber.hashCode());
        result = prime * result + ((hwUUID == null) ? 0 : hwUUID.hashCode());
        result = prime * result + ((hwFamily == null) ? 0 : hwFamily.hashCode());
        result = prime * result + ((HBAs == null) ? 0 : HBAs.hashCode());
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
        VdsDynamic other = (VdsDynamic) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(_supportedClusterVersionsSet, other._supportedClusterVersionsSet)
                && ObjectUtils.objectsEqual(_supportedENGINESVersionsSet, other._supportedENGINESVersionsSet)
                && anonymousHugePages == other.anonymousHugePages
                && ObjectUtils.objectsEqual(buildName, other.buildName)
                && ObjectUtils.objectsEqual(cpu_cores, other.cpu_cores)
                && ObjectUtils.objectsEqual(cpuThreads, other.cpuThreads)
                && ObjectUtils.objectsEqual(cpu_flags, other.cpu_flags)
                && ObjectUtils.objectsEqual(cpu_model, other.cpu_model)
                && ObjectUtils.objectsEqual(cpu_over_commit_time_stamp, other.cpu_over_commit_time_stamp)
                && ObjectUtils.objectsEqual(cpu_sockets, other.cpu_sockets)
                && ObjectUtils.objectsEqual(cpu_speed_mh, other.cpu_speed_mh)
                && ObjectUtils.objectsEqual(guest_overhead, other.guest_overhead)
                && ObjectUtils.objectsEqual(hooksStr, other.hooksStr)
                && ObjectUtils.objectsEqual(host_os, other.host_os)
                && ObjectUtils.objectsEqual(iScsiInitiatorName, other.iScsiInitiatorName)
                && ObjectUtils.objectsEqual(if_total_speed, other.if_total_speed)
                && ObjectUtils.objectsEqual(kernel_version, other.kernel_version)
                && ObjectUtils.objectsEqual(kvm_enabled, other.kvm_enabled)
                && ObjectUtils.objectsEqual(kvm_version, other.kvm_version)
                && ObjectUtils.objectsEqual(libvirt_version, other.libvirt_version)
                && ObjectUtils.objectsEqual(rpmVersion, other.rpmVersion)
                && ObjectUtils.objectsEqual(mem_commited, other.mem_commited)
                && ObjectUtils.objectsEqual(net_config_dirty, other.net_config_dirty)
                && nonOperationalReason == other.nonOperationalReason
                && ObjectUtils.objectsEqual(pending_vcpus_count, other.pending_vcpus_count)
                && ObjectUtils.objectsEqual(pending_vmem_size, other.pending_vmem_size)
                && ObjectUtils.objectsEqual(physical_mem_mb, other.physical_mem_mb)
                && previous_status == other.previous_status
                && ObjectUtils.objectsEqual(reserved_mem, other.reserved_mem)
                && ObjectUtils.objectsEqual(getsoftware_version(), other.getsoftware_version())
                && ObjectUtils.objectsEqual(spice_version, other.spice_version)
                && status == other.status
                && ObjectUtils.objectsEqual(supported_cluster_levels, other.supported_cluster_levels)
                && ObjectUtils.objectsEqual(supported_engines, other.supported_engines)
                && transparentHugePagesState == other.transparentHugePagesState
                && ObjectUtils.objectsEqual(versionName, other.versionName)
                && ObjectUtils.objectsEqual(vm_active, other.vm_active)
                && vm_count == other.vm_count
                && ObjectUtils.objectsEqual(vm_migrating, other.vm_migrating)
                && ObjectUtils.objectsEqual(vms_cores_count, other.vms_cores_count)
                && ObjectUtils.objectsEqual(hwManufacturer, other.hwManufacturer)
                && ObjectUtils.objectsEqual(hwProductName, other.hwProductName)
                && ObjectUtils.objectsEqual(hwVersion, other.hwVersion)
                && ObjectUtils.objectsEqual(hwSerialNumber, other.hwSerialNumber)
                && ObjectUtils.objectsEqual(hwUUID, other.hwUUID)
                && ObjectUtils.objectsEqual(hwFamily, other.hwFamily)
                && ObjectUtils.objectsEqual(HBAs, other.HBAs)
                && ObjectUtils.objectsEqual(supportedEmulatedMachines, other.supportedEmulatedMachines));
    }

}
