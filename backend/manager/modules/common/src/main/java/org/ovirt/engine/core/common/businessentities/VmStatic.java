package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.DesktopVM;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@XmlType(name = "VmStatic")
@Entity
@Table(name = "vm_static")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VmStatic extends VmBase implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -2753306386502558044L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vm_guid")
    @Type(type = "guid")
    @XmlElement(name = "Id")
    private Guid id = new Guid();

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_NAME_SIZE)
    @Column(name = "vm_name")
    @ValidName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class })
    private String name = ""; // GREGM, otherwise NPE

    @Column(name = "mem_size_mb")
    private int mem_size_mb;

    @Column(name = "vmt_guid")
    @Type(type = "guid")
    private Guid vmt_guid = new Guid();

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description")
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String description;

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vds_group_id;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    @Column(name = "domain", length = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    private String domain;

    @Column(name = "creation_date")
    private java.util.Date creation_date = new java.util.Date(0);

    @Column(name = "num_of_monitors")
    @IntegerContainedInConfigValueList(configValue = ConfigValues.ValidNumOfMonitors, groups = DesktopVM.class,
            message = "VALIDATION.VM.NUM_OF_MONITORS.EXCEEDED")
    private int num_of_monitors;

    @Column(name = "is_initialized")
    private boolean is_initialized;

    @Column(name = "is_auto_suspend")
    private boolean is_auto_suspend;

    @Column(name = "num_of_sockets")
    private int num_of_sockets = 1;

    @Column(name = "cpu_per_socket")
    private int cpu_per_socket = 1;

    @Column(name = "usb_policy")
    private UsbPolicy usb_policy = UsbPolicy.Enabled;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    @Column(name = "time_zone", length = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String time_zone;

    @Column(name = "is_stateless")
    private boolean is_stateless;

    @XmlElement(name = "fail_back")
    @Column(name = "fail_back")
    private boolean fail_back;

    @Column(name = "dedicated_vm_for_vds")
    @Type(type = "guid")
    private NGuid dedicated_vm_for_vds;

    @Column(name = "auto_startup")
    private boolean auto_startup;

    @XmlElement(name = "vm_type")
    @Column(name = "vm_type")
    private VmType vm_type = VmType.Desktop;

    @Column(name = "hypervisor_type")
    private HypervisorType hypervisor_type = HypervisorType.KVM;

    @Column(name = "operation_mode")
    private OperationMode operation_mode = OperationMode.FullVirtualized;

    @Column(name = "nice_level")
    private int nice_level;

    @XmlElement(name = "default_boot_sequence")
    @Column(name = "default_boot_sequence")
    private BootSequence default_boot_sequence = BootSequence.C;

    @XmlElement(name = "default_display_type")
    @Column(name = "default_display_type")
    private DisplayType default_display_type = DisplayType.qxl;

    @XmlElement(name = "priority")
    @Column(name = "priority")
    private int priority;

    @XmlElement(name = "iso_path")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "iso_path", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String iso_path;

    @XmlElement(name = "origin")
    @Column(name = "origin")
    private OriginType origin = OriginType.ENGINE;

    @XmlElement(name = "initrd_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "initrd_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String initrd_url;

    @XmlElement(name = "kernel_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernel_url;

    @XmlElement(name = "kernel_params")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_params", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernel_params;

    @OvfExportOnlyField(valueToIgnore = "MIGRATABLE", exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Column(name = "migration_support")
    private MigrationSupport migrationSupport;

    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "userdefined_properties")
    private String userDefinedProperties;

    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "predefined_properties")
    private String predefinedProperties;

    /**
     * Disk size in sectors of 512 bytes
     */
    @Transient
    private int m_nDiskSize;

    @XmlElement(name = "MinAllocatedMem")
    @Transient
    private int minAllocatedMemField;

    @Transient
    private String customProperties;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (auto_startup ? 1231 : 1237);
        result = prime * result + cpu_per_socket;
        result = prime
                * result
                + ((creation_date == null) ? 0 : creation_date
                        .hashCode());
        result = prime
                * result
                + ((customProperties == null) ? 0 : customProperties.hashCode());
        result = prime
                * result
                + ((dedicated_vm_for_vds == null) ? 0
                        : dedicated_vm_for_vds.hashCode());
        result = prime
                * result
                + ((default_boot_sequence == null) ? 0
                        : default_boot_sequence.hashCode());
        result = prime
                * result
                + default_display_type.hashCode() * prime;
        result = prime
                * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result
                + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + (fail_back ? 1231 : 1237);
        result = prime
                * result
                + hypervisor_type.hashCode() * prime;
        result = prime * result
                + ((initrd_url == null) ? 0 : initrd_url.hashCode());
        result = prime * result + (is_auto_suspend ? 1231 : 1237);
        result = prime * result + (is_initialized ? 1231 : 1237);
        result = prime * result + (is_stateless ? 1231 : 1237);
        result = prime * result
                + ((iso_path == null) ? 0 : iso_path.hashCode());
        result = prime
                * result
                + ((kernel_params == null) ? 0 : kernel_params
                        .hashCode());
        result = prime * result
                + ((kernel_url == null) ? 0 : kernel_url.hashCode());
        result = prime * result + m_nDiskSize;
        result = prime * result + mem_size_mb;
        result = prime
                * result
                + ((migrationSupport == null) ? 0 : migrationSupport.hashCode());
        result = prime * result + nice_level;
        result = prime * result + num_of_monitors;
        result = prime * result + num_of_sockets;
        result = prime
                * result
                + operation_mode.hashCode() * prime;
        result = prime * result
                + origin.hashCode() * prime;
        result = prime
                * result
                + ((predefinedProperties == null) ? 0 : predefinedProperties
                        .hashCode());
        result = prime * result + priority;
        result = prime * result
                + ((time_zone == null) ? 0 : time_zone.hashCode());
        result = prime * result
                + usb_policy.hashCode() * prime;
        result = prime
                * result
                + ((userDefinedProperties == null) ? 0 : userDefinedProperties
                        .hashCode());
        result = prime
                * result
                + ((vds_group_id == null) ? 0 : vds_group_id
                        .hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + vm_type.hashCode() * prime;
        result = prime * result
                + ((vmt_guid == null) ? 0 : vmt_guid.hashCode());
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
        VmStatic other = (VmStatic) obj;
        if (auto_startup != other.auto_startup)
            return false;
        if (cpu_per_socket != other.cpu_per_socket)
            return false;
        if (creation_date == null) {
            if (other.creation_date != null)
                return false;
        } else if (!creation_date.equals(other.creation_date))
            return false;
        if (dedicated_vm_for_vds == null) {
            if (other.dedicated_vm_for_vds != null)
                return false;
        } else if (!dedicated_vm_for_vds
                .equals(other.dedicated_vm_for_vds))
            return false;
        if (default_boot_sequence != other.default_boot_sequence)
            return false;
        if (default_display_type != other.default_display_type)
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (fail_back != other.fail_back)
            return false;
        if (hypervisor_type != other.hypervisor_type)
            return false;
        if (initrd_url == null) {
            if (other.initrd_url != null)
                return false;
        } else if (!initrd_url.equals(other.initrd_url))
            return false;
        if (is_auto_suspend != other.is_auto_suspend)
            return false;
        if (is_initialized != other.is_initialized)
            return false;
        if (is_stateless != other.is_stateless)
            return false;
        if (iso_path == null) {
            if (other.iso_path != null)
                return false;
        } else if (!iso_path.equals(other.iso_path))
            return false;
        if (kernel_params == null) {
            if (other.kernel_params != null)
                return false;
        } else if (!kernel_params.equals(other.kernel_params))
            return false;
        if (kernel_url == null) {
            if (other.kernel_url != null)
                return false;
        } else if (!kernel_url.equals(other.kernel_url))
            return false;
        if (m_nDiskSize != other.m_nDiskSize)
            return false;
        if (mem_size_mb != other.mem_size_mb)
            return false;
        if (migrationSupport != other.migrationSupport)
            return false;
        if (nice_level != other.nice_level)
            return false;
        if (num_of_monitors != other.num_of_monitors)
            return false;
        if (num_of_sockets != other.num_of_sockets)
            return false;
        if (operation_mode != other.operation_mode)
            return false;
        if (origin != other.origin)
            return false;
        if (predefinedProperties == null) {
            if (other.predefinedProperties != null)
                return false;
        } else if (!predefinedProperties.equals(other.predefinedProperties))
            return false;
        if (priority != other.priority)
            return false;
        if (time_zone == null) {
            if (other.time_zone != null)
                return false;
        } else if (!time_zone.equals(other.time_zone))
            return false;
        if (usb_policy != other.usb_policy)
            return false;
        if (userDefinedProperties == null) {
            if (other.userDefinedProperties != null)
                return false;
        } else if (!userDefinedProperties.equals(other.userDefinedProperties))
            return false;
        if (vds_group_id == null) {
            if (other.vds_group_id != null)
                return false;
        } else if (!vds_group_id.equals(other.vds_group_id))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (vm_type != other.vm_type)
            return false;
        if (vmt_guid == null) {
            if (other.vmt_guid != null)
                return false;
        } else if (!vmt_guid.equals(other.vmt_guid))
            return false;
        return true;
    }

    @XmlElement(name = "CustomProperties")
    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    public String getPredefinedProperties() {
        return predefinedProperties;
    }

    public void setPredefinedProperties(String predefinedProperties) {
        this.predefinedProperties = predefinedProperties;
    }

    public String getUserDefinedProperties() {
        return userDefinedProperties;
    }

    public void setUserDefinedProperties(String userDefinedProperties) {
        this.userDefinedProperties = userDefinedProperties;
    }

    public VmStatic(VmStatic vmStatic) {
        nice_level = vmStatic.getnice_level();
        name = vmStatic.getvm_name();
        description = vmStatic.getdescription();
        mem_size_mb = vmStatic.getmem_size_mb();
        setos(vmStatic.getos());
        vds_group_id = vmStatic.getvds_group_id();
        id = vmStatic.getId();
        setcreation_date(vmStatic.getcreation_date());
        setdomain(vmStatic.getdomain());
        vmt_guid = vmStatic.getvmt_guid();
        this.setnum_of_sockets(vmStatic.getnum_of_sockets());
        this.setcpu_per_socket(vmStatic.getcpu_per_socket());
        setnum_of_monitors(vmStatic.getnum_of_monitors());
        setis_initialized(vmStatic.getis_initialized());
        setis_auto_suspend(vmStatic.getis_auto_suspend());
        setauto_startup(vmStatic.getauto_startup());
        setusb_policy(vmStatic.getusb_policy());
        settime_zone(vmStatic.gettime_zone());
        setis_stateless(vmStatic.getis_stateless());
        setfail_back(vmStatic.getfail_back());
        setdefault_boot_sequence(vmStatic.getdefault_boot_sequence());
        setvm_type(vmStatic.getvm_type());
        sethypervisor_type(vmStatic.gethypervisor_type());
        setoperation_mode(vmStatic.getoperation_mode());
        setdefault_display_type(vmStatic.getdefault_display_type());
        setdedicated_vm_for_vds(vmStatic.getdedicated_vm_for_vds());
        setiso_path(vmStatic.getiso_path());
        setorigin(vmStatic.getorigin());
        setpriority(vmStatic.getpriority());
        setinitrd_url(vmStatic.getinitrd_url());
        setkernel_url(vmStatic.getkernel_url());
        setkernel_params(vmStatic.getkernel_params());
        setMigrationSupport(vmStatic.getMigrationSupport());
        setMinAllocatedMem(vmStatic.getMinAllocatedMem());
    }

    @XmlElement(name = "DiskSize")
    public int getDiskSize() {
        return m_nDiskSize;
    }

    public void setDiskSize(int value) {
        m_nDiskSize = value;
    }

    public boolean getIsFirstRun() {
        return !getis_initialized();
    }

    @XmlElement(name = "MigrationSupport")
    public MigrationSupport getMigrationSupport() {
        return migrationSupport;
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.migrationSupport = migrationSupport;
    }

    public VmStatic() {
        num_of_monitors = 1;
        is_initialized = false;
        is_auto_suspend = false;
        nice_level = 0;
        default_boot_sequence = BootSequence.C;
        default_display_type = DisplayType.qxl;
        vm_type = VmType.Desktop;
        hypervisor_type = HypervisorType.KVM;
        operation_mode = OperationMode.FullVirtualized;
        migrationSupport = MigrationSupport.MIGRATABLE;
    }

    public VmStatic(String description, int mem_size_mb, VmOsType os, Guid vds_group_id, Guid vm_guid, String vm_name,
            Guid vmt_guid, String domain, java.util.Date creation_date, int num_of_monitors, boolean is_initialized,
            boolean is_auto_suspend, Guid dedicated_vm_for_vds, int num_of_sockets, int cpu_per_socket,
            UsbPolicy usb_policy, String time_zone, boolean auto_startup, boolean is_stateless, boolean fail_back,
            BootSequence default_boot_sequence, VmType vm_type, HypervisorType hypervisor_type,
            OperationMode operation_mode, int minAllocatedMem) {
        nice_level = 0;

        this.description = description;
        this.mem_size_mb = mem_size_mb;
        super.setos(os);
        this.vds_group_id = vds_group_id;
        this.id = vm_guid;
        this.name = vm_name;
        this.vmt_guid = vmt_guid;
        this.domain = domain;
        this.creation_date = creation_date;
        this.setnum_of_monitors(num_of_monitors);
        this.setis_initialized(is_initialized);
        this.setis_auto_suspend(is_auto_suspend);
        this.setnum_of_sockets(num_of_sockets);
        this.setcpu_per_socket(cpu_per_socket);
        this.usb_policy = usb_policy;
        this.time_zone = time_zone;
        this.setauto_startup(auto_startup);
        this.setis_stateless(is_stateless);
        this.setdedicated_vm_for_vds(dedicated_vm_for_vds);
        this.setfail_back(fail_back);
        this.setdefault_boot_sequence(default_boot_sequence);
        this.setvm_type(vm_type);
        this.sethypervisor_type(hypervisor_type);
        this.setoperation_mode(operation_mode);
        this.setMinAllocatedMem(minAllocatedMem);
    }

    @XmlElement
    public String getdescription() {
        return this.description;
    }

    public void setdescription(String value) {
        this.description = value;
    }

    @XmlElement
    public int getmem_size_mb() {
        return this.mem_size_mb;
    }

    public void setmem_size_mb(int value) {
        this.mem_size_mb = value;
    }

    @XmlElement
    public Guid getvds_group_id() {
        return this.vds_group_id;
    }

    public void setvds_group_id(Guid value) {
        this.vds_group_id = value;
    }

    @XmlElement
    public String getvm_name() {
        return this.name;
    }

    public void setvm_name(String value) {
        this.name = value;
    }

    @XmlElement
    public Guid getvmt_guid() {
        return this.vmt_guid;
    }

    public void setvmt_guid(Guid value) {
        this.vmt_guid = value;
    }

    @XmlElement
    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
    }

    @XmlElement
    public java.util.Date getcreation_date() {
        return creation_date;
    }

    public void setcreation_date(java.util.Date value) {
        creation_date = value;
    }

    @XmlElement
    public int getnum_of_monitors() {
        return num_of_monitors;
    }

    public void setnum_of_monitors(int value) {
        num_of_monitors = value;
    }

    @XmlElement
    public boolean getis_initialized() {
        return is_initialized;
    }

    public void setis_initialized(boolean value) {
        is_initialized = value;
    }

    @XmlElement
    public boolean getis_auto_suspend() {
        return is_auto_suspend;
    }

    public void setis_auto_suspend(boolean value) {
        is_auto_suspend = value;
    }

    @XmlElement
    public int getnum_of_cpus() {
        return getcpu_per_socket() * getnum_of_sockets();
    }

    @XmlElement
    public int getnum_of_sockets() {
        return num_of_sockets;
    }

    public void setnum_of_sockets(int value) {
        num_of_sockets = value;
    }

    @XmlElement
    public int getcpu_per_socket() {
        return cpu_per_socket;
    }

    public void setcpu_per_socket(int value) {
        cpu_per_socket = value;
    }

    @XmlElement
    public UsbPolicy getusb_policy() {
        return usb_policy;
    }

    public void setusb_policy(UsbPolicy value) {
        usb_policy = value;
    }

    @XmlElement
    public String gettime_zone() {
        return time_zone;
    }

    public void settime_zone(String value) {
        time_zone = value;
    }

    @XmlElement
    public boolean getauto_startup() {
        return auto_startup;
    }

    public void setauto_startup(boolean value) {
        auto_startup = value;
    }

    @XmlElement
    public boolean getis_stateless() {
        return is_stateless;
    }

    public void setis_stateless(boolean value) {
        is_stateless = value;
    }

    @XmlElement(nillable = true)
    public NGuid getdedicated_vm_for_vds() {
        return dedicated_vm_for_vds;
    }

    public void setdedicated_vm_for_vds(NGuid value) {
        dedicated_vm_for_vds = value;
    }

    @XmlElement
    public int getnice_level() {
        return nice_level;
    }

    public void setnice_level(int value) {
        nice_level = value;
    }

    public boolean getfail_back() {
        return fail_back;
    }

    public void setfail_back(boolean value) {
        fail_back = value;
    }

    public BootSequence getdefault_boot_sequence() {
        return default_boot_sequence;
    }

    public void setdefault_boot_sequence(BootSequence value) {
        default_boot_sequence = value;
    }

    public VmType getvm_type() {
        return vm_type;
    }

    public void setvm_type(VmType value) {
        vm_type = value;
    }

    public HypervisorType gethypervisor_type() {
        return hypervisor_type;
    }

    public void sethypervisor_type(HypervisorType value) {
        hypervisor_type = value;
    }

    public OperationMode getoperation_mode() {
        return operation_mode;
    }

    public void setoperation_mode(OperationMode value) {
        operation_mode = value;
    }

    public DisplayType getdefault_display_type() {
        return default_display_type;
    }

    public void setdefault_display_type(DisplayType value) {
        default_display_type = value;
    }

    public int getpriority() {
        return priority;
    }

    public void setpriority(int value) {
        priority = value;
    }

    public String getiso_path() {
        return iso_path;
    }

    public void setiso_path(String value) {
        iso_path = value;
    }

    public OriginType getorigin() {
        return origin;
    }

    public void setorigin(OriginType value) {
        origin = value;
    }

    public String getinitrd_url() {
        return initrd_url;
    }

    public void setinitrd_url(String value) {
        initrd_url = value;
    }

    public String getkernel_url() {
        return kernel_url;
    }

    public void setkernel_url(String value) {
        kernel_url = value;
    }

    public String getkernel_params() {
        return kernel_params;
    }

    public void setkernel_params(String value) {
        kernel_params = value;
    }

    public int getMinAllocatedMem() {
        if (minAllocatedMemField > 0) {
            return minAllocatedMemField;
        } else {
            return mem_size_mb;
        }
    }

    public void setMinAllocatedMem(int value) {
        minAllocatedMemField = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        // Actual implementation is TBD
        return null;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }
}
