package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

@Entity
@Table(name = "vds_dynamic")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VdsDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -6010035855157006935L;

    @Id
    // @GeneratedValue(generator = "system-uuid")
    // @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id;

    @Column(name = "status")
    private VDSStatus status = VDSStatus.Unassigned;

    @Column(name = "cpu_cores")
    private Integer cpu_cores;

    @Column(name = "cpu_threads")
    private Integer cpuThreads;

    @Column(name = "cpu_model")
    private String cpu_model;

    @Column(name = "cpu_speed_mh", scale = 18, precision = 0)
    private BigDecimal cpu_speed_mh = BigDecimal.valueOf(0.0);

    @Column(name = "if_total_speed")
    private String if_total_speed;

    @Column(name = "kvm_enabled")
    private Boolean kvm_enabled;

    @Column(name = "physical_mem_mb")
    private Integer physical_mem_mb;

    @Column(name = "mem_commited")
    private Integer mem_commited;

    @Column(name = "vm_active")
    private Integer vm_active;

    @Column(name = "vm_count")
    private int vm_count;

    @Column(name = "vm_migrating")
    private Integer vm_migrating;

    @Column(name = "reserved_mem")
    private Integer reserved_mem ;

    @Column(name = "guest_overhead")
    private Integer guest_overhead;

    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "build_name")
    private String buildName;

    @Column(name = "previous_status")
    private VDSStatus previous_status = VDSStatus.Unassigned;

    @Column(name = "cpu_flags")
    private String cpu_flags;

    @Column(name = "cpu_over_commit_time_stamp")
    private Date cpu_over_commit_time_stamp;

    @Column(name = "vms_cores_count")
    private Integer vms_cores_count;

    @Column(name = "pending_vcpus_count")
    private Integer pending_vcpus_count;

    @Column(name = "cpu_sockets")
    private Integer cpu_sockets;

    @Column(name = "net_config_dirty")
    private Boolean net_config_dirty;

    @Column(name = "supported_cluster_levels")
    private String supported_cluster_levels;

    @Column(name = "supported_engines")
    private String supported_engines;

    @Column(name = "host_os")
    private String host_os;

    @Column(name = "kvm_version")
    private String kvm_version;

    @Column(name = "libvirt_version")
    private String libvirt_version;

    @Column(name = "spice_version")
    private String spice_version;

    @Column(name = "kernel_version")
    private String kernel_version;

    @Column(name = "iscsi_initiator_name")
    private String iScsiInitiatorName;

    @Column(name = "transparent_hugepages_state")
    private VdsTransparentHugePagesState transparentHugePagesState = VdsTransparentHugePagesState.Never;
    @Column(name = "anonymous_hugepages")
    private int anonymousHugePages;

    @Column(name = "hooks")
    private String hooksStr;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwManufacturer")
    private String hwManufacturer;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwProductName")
    private String hwProductName;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwVersion")
    private String hwVersion;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwSerialNumber")
    private String hwSerialNumber;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwUUID")
    private String hwUUID;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)
    @Column(name = "hwFamily")
    private String hwFamily;

    @Column(name = "non_operational_reason")
    private NonOperationalReason nonOperationalReason = NonOperationalReason.NONE;

    @Transient
    private Integer pending_vmem_size;

    @Transient
    private RpmVersion rpmVersion;

    @Transient
    private java.util.HashSet<Version> _supportedClusterVersionsSet;

    @Transient
    private java.util.HashSet<Version> _supportedENGINESVersionsSet;

    public void setVersion(RpmVersion value) {
        rpmVersion = value;
    }

    public RpmVersion getVersion() {
        return rpmVersion;
    }

    public VdsDynamic() {
        rpmVersion = new RpmVersion();
        mem_commited = 0;
        reserved_mem = 1024;
        pending_vcpus_count = 0;
        pending_vmem_size = 0;
        transparentHugePagesState = VdsTransparentHugePagesState.Never;
    }

    public VdsDynamic(Integer cpu_cores, Integer cpuThreads, String cpu_model, Double cpu_speed_mh, String if_total_speed,
                      Boolean kvm_enabled, Integer mem_commited, Integer physical_mem_mb, int status, Guid vds_id,
                      Integer vm_active, int vm_count, Integer vm_migrating, Integer reserved_mem, Integer guest_overhead,
                      VDSStatus previous_status, String software_version, String version_name, String build_name,
                      Date cpu_over_commit_time_stamp, Integer pending_vcpus_count,
                      Integer pending_vmem_sizeField, Boolean net_config_dirty, String hwManufacturer,
                      String hwProductName, String hwVersion, String hwSerialNumber,
                      String hwUUID, String hwFamily) {
        rpmVersion = new RpmVersion();
        this.cpu_cores = cpu_cores;
        this.cpuThreads = cpuThreads;
        this.cpu_model = cpu_model;
        this.cpu_speed_mh = BigDecimal.valueOf(cpu_speed_mh);
        this.if_total_speed = if_total_speed;
        this.kvm_enabled = kvm_enabled;
        this.mem_commited = mem_commited;
        this.physical_mem_mb = physical_mem_mb;
        this.status = VDSStatus.forValue(status);
        this.id = vds_id;
        this.vm_active = vm_active;
        this.vm_count = vm_count;
        this.vm_migrating = vm_migrating;
        this.reserved_mem = reserved_mem;
        this.guest_overhead = guest_overhead;
        this.previous_status = previous_status;
        this.setsoftware_version(software_version);
        this.setversion_name(version_name);
        this.setcpu_over_commit_time_stamp(cpu_over_commit_time_stamp);
        this.pending_vcpus_count = pending_vcpus_count;
        this.pending_vmem_size = pending_vmem_sizeField;
        this.net_config_dirty = net_config_dirty;
        this.transparentHugePagesState = VdsTransparentHugePagesState.Never;
        this.hwUUID = hwUUID;
        this.hwFamily = hwFamily;
        this.hwSerialNumber = hwSerialNumber;
        this.hwVersion = hwVersion;
        this.hwProductName = hwProductName;
        this.hwManufacturer = hwManufacturer;
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

    public String getlibvirt_version() {
        return this.libvirt_version;
    }

    public void setlibvirt_version(String value) {
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
        result =
                prime * result + ((_supportedClusterVersionsSet == null) ? 0 : _supportedClusterVersionsSet.hashCode());
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
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        VdsDynamic other = (VdsDynamic) obj;
        if (_supportedClusterVersionsSet == null) {
            if (other._supportedClusterVersionsSet != null)
                return false;
        } else if (!_supportedClusterVersionsSet.equals(other._supportedClusterVersionsSet))
            return false;
        if (_supportedENGINESVersionsSet == null) {
            if (other._supportedENGINESVersionsSet != null)
                return false;
        } else if (!_supportedENGINESVersionsSet.equals(other._supportedENGINESVersionsSet))
            return false;
        if (anonymousHugePages != other.anonymousHugePages)
            return false;
        if (buildName == null) {
            if (other.buildName != null)
                return false;
        } else if (!buildName.equals(other.buildName))
            return false;
        if (cpu_cores == null) {
            if (other.cpu_cores != null)
                return false;
        } else if (!cpu_cores.equals(other.cpu_cores))
            return false;
        if (cpuThreads == null) {
            if (other.cpuThreads != null)
                return false;
        } else if (!cpuThreads.equals(other.cpuThreads))
            return false;
        if (cpu_flags == null) {
            if (other.cpu_flags != null)
                return false;
        } else if (!cpu_flags.equals(other.cpu_flags))
            return false;
        if (cpu_model == null) {
            if (other.cpu_model != null)
                return false;
        } else if (!cpu_model.equals(other.cpu_model))
            return false;
        if (cpu_over_commit_time_stamp == null) {
            if (other.cpu_over_commit_time_stamp != null)
                return false;
        } else if (!cpu_over_commit_time_stamp.equals(other.cpu_over_commit_time_stamp))
            return false;
        if (cpu_sockets == null) {
            if (other.cpu_sockets != null)
                return false;
        } else if (!cpu_sockets.equals(other.cpu_sockets))
            return false;
        if (cpu_speed_mh == null) {
            if (other.cpu_speed_mh != null)
                return false;
        } else if (!cpu_speed_mh.equals(other.cpu_speed_mh))
            return false;
        if (guest_overhead == null) {
            if (other.guest_overhead != null)
                return false;
        } else if (!guest_overhead.equals(other.guest_overhead))
            return false;
        if (hooksStr == null) {
            if (other.hooksStr != null)
                return false;
        } else if (!hooksStr.equals(other.hooksStr))
            return false;
        if (host_os == null) {
            if (other.host_os != null)
                return false;
        } else if (!host_os.equals(other.host_os))
            return false;
        if (iScsiInitiatorName == null) {
            if (other.iScsiInitiatorName != null)
                return false;
        } else if (!iScsiInitiatorName.equals(other.iScsiInitiatorName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (if_total_speed == null) {
            if (other.if_total_speed != null)
                return false;
        } else if (!if_total_speed.equals(other.if_total_speed))
            return false;
        if (kernel_version == null) {
            if (other.kernel_version != null)
                return false;
        } else if (!kernel_version.equals(other.kernel_version))
            return false;
        if (kvm_enabled == null) {
            if (other.kvm_enabled != null)
                return false;
        } else if (!kvm_enabled.equals(other.kvm_enabled))
            return false;
        if (kvm_version == null) {
            if (other.kvm_version != null)
                return false;
        } else if (!kvm_version.equals(other.kvm_version))
            return false;
        if (libvirt_version == null) {
            if (other.libvirt_version != null)
                return false;
        } else if (!libvirt_version.equals(other.libvirt_version))
            return false;
        if (rpmVersion == null) {
            if (other.rpmVersion != null)
                return false;
        } else if (!rpmVersion.equals(other.rpmVersion))
            return false;
        if (mem_commited == null) {
            if (other.mem_commited != null)
                return false;
        } else if (!mem_commited.equals(other.mem_commited))
            return false;
        if (net_config_dirty == null) {
            if (other.net_config_dirty != null)
                return false;
        } else if (!net_config_dirty.equals(other.net_config_dirty))
            return false;
        if (nonOperationalReason != other.nonOperationalReason)
            return false;
        if (pending_vcpus_count == null) {
            if (other.pending_vcpus_count != null)
                return false;
        } else if (!pending_vcpus_count.equals(other.pending_vcpus_count))
            return false;
        if (pending_vmem_size == null) {
            if (other.pending_vmem_size != null)
                return false;
        } else if (!pending_vmem_size.equals(other.pending_vmem_size))
            return false;
        if (physical_mem_mb == null) {
            if (other.physical_mem_mb != null)
                return false;
        } else if (!physical_mem_mb.equals(other.physical_mem_mb))
            return false;
        if (previous_status != other.previous_status)
            return false;
        if (reserved_mem == null) {
            if (other.reserved_mem != null)
                return false;
        } else if (!reserved_mem.equals(other.reserved_mem))
            return false;
        if (getsoftware_version() == null) {
            if (other.getsoftware_version() != null)
                return false;
        } else if (!getsoftware_version().equals(other.getsoftware_version()))
            return false;
        if (spice_version == null) {
            if (other.spice_version != null)
                return false;
        } else if (!spice_version.equals(other.spice_version))
            return false;
        if (status != other.status)
            return false;
        if (supported_cluster_levels == null) {
            if (other.supported_cluster_levels != null)
                return false;
        } else if (!supported_cluster_levels.equals(other.supported_cluster_levels))
            return false;
        if (supported_engines == null) {
            if (other.supported_engines != null)
                return false;
        } else if (!supported_engines.equals(other.supported_engines))
            return false;
        if (transparentHugePagesState != other.transparentHugePagesState)
            return false;
        if (versionName == null) {
            if (other.versionName != null)
                return false;
        } else if (!versionName.equals(other.versionName))
            return false;
        if (vm_active == null) {
            if (other.vm_active != null)
                return false;
        } else if (!vm_active.equals(other.vm_active))
            return false;
        if (vm_count != other.vm_count)
            return false;
        if (vm_migrating == null) {
            if (other.vm_migrating != null)
                return false;
        } else if (!vm_migrating.equals(other.vm_migrating))
            return false;
        if (vms_cores_count == null) {
            if (other.vms_cores_count != null)
                return false;
        } else if (!vms_cores_count.equals(other.vms_cores_count))
            return false;
        if (hwManufacturer == null) {
            if (other.hwManufacturer != null)
                return false;
        } else if (!hwManufacturer.equals(other.hwManufacturer))
            return false;
        if (hwProductName == null) {
            if (other.hwProductName != null)
                return false;
        } else if (!hwProductName.equals(other.hwProductName))
            return false;
        if (hwVersion == null) {
            if (other.hwVersion != null)
                return false;
        } else if (!hwVersion.equals(other.hwVersion))
            return false;
        if (hwSerialNumber == null) {
            if (other.hwSerialNumber != null)
                return false;
        } else if (!hwSerialNumber.equals(other.hwSerialNumber))
            return false;
        if (hwUUID == null) {
            if (other.hwUUID != null)
                return false;
        } else if (!hwUUID.equals(other.hwUUID))
            return false;
        if (hwFamily == null) {
            if (other.hwFamily != null)
                return false;
        } else if (!hwFamily.equals(other.hwFamily))
            return false;
        return true;
    }
}
