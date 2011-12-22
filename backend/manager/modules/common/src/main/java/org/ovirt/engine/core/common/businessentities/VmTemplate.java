package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplate")
@Entity
@Table(name = "vm_templates")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VmTemplate extends VmBase implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -522552511046744989L;
    @Transient
    private List<VmNetworkInterface> _Interfaces = new ArrayList<VmNetworkInterface>();

    public VmTemplate() {
        autosuspend = false;
        niceLevel = 0;
        diskTemplateMap = new HashMap<String, DiskImageTemplate>();
    }

    public VmTemplate(int child_count, Date creation_date, String description, int mem_size_mb, String name,
            int num_of_sockets, int cpu_per_socket, VmOsType os, Guid vds_group_id, Guid vmt_guid, String domain,
            int num_of_monitors, int status, int usb_policy, String time_zone, boolean is_auto_suspend, int nice_level,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type, HypervisorType hypervisor_type,
            OperationMode operation_mode) {
        diskTemplateMap = new HashMap<String, DiskImageTemplate>();

        this.childCount = child_count;
        this.creationDate = creation_date;
        this.description = description;
        this.memSizeMB = mem_size_mb;
        this.name = name;
        this.numOfSockets = num_of_sockets;
        this.cpusPerSocket = cpu_per_socket;
        super.setos(os);
        this.vdsGroupId = vds_group_id;
        this.id = vmt_guid;
        this.domain = domain;
        this.setnum_of_monitors(num_of_monitors);
        this.setstatus(VmTemplateStatus.forValue(status));
        usbPolicy = UsbPolicy.forValue(usb_policy);
        this.timezone = time_zone;
        this.setis_auto_suspend(is_auto_suspend);
        this.setnice_level(nice_level);
        this.setfail_back(fail_back);
        this.setdefault_boot_sequence(default_boot_sequence);
        this.setvm_type(vm_type);
        this.sethypervisor_type(hypervisor_type);
        this.setoperation_mode(operation_mode);
    }

    @Column(name = "child_count", nullable = false)
    private int childCount;

    @XmlElement
    public int getchild_count() {
        return this.childCount;
    }

    public void setchild_count(int value) {
        this.childCount = value;
    }

    @Column(name = "creation_date", nullable = false)
    private java.util.Date creationDate = new java.util.Date(0);

    @XmlElement
    public java.util.Date getcreation_date() {
        return this.creationDate;
    }

    public void setcreation_date(java.util.Date value) {
        this.creationDate = value;
    }

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String description;

    @XmlElement
    public String getdescription() {
        return this.description;
    }

    public void setdescription(String value) {
        this.description = value;
    }

    @Column(name = "mem_size_mb", nullable = false)
    private int memSizeMB;

    @XmlElement
    public int getmem_size_mb() {
        return this.memSizeMB;
    }

    public void setmem_size_mb(int value) {
        this.memSizeMB = value;
    }

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION.VM_TEMPLATE.NAME.MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Column(name = "name", length = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE, nullable = false)
    private String name;

    @XmlElement
    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        if (!StringHelper.EqOp(this.name, value)) {
            this.name = value;
        }
    }

    // no need fo DataMember, it's setter and calculated from 2 other fields
    @XmlElement
    public int getnum_of_cpus() {
        return this.getcpu_per_socket() * this.getnum_of_sockets();
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setnum_of_cpus(int val) {
    }

    @Column(name = "num_of_sockets", nullable = false)
    private int numOfSockets = 1;

    @XmlElement(name = "num_of_sockets")
    public int getnum_of_sockets() {
        return this.numOfSockets;
    }

    public void setnum_of_sockets(int value) {
        this.numOfSockets = value;
    }

    @Column(name = "cpu_per_socket", nullable = false)
    private int cpusPerSocket = 1;

    @XmlElement(name = "cpu_per_socket")
    public int getcpu_per_socket() {
        return this.cpusPerSocket;
    }

    public void setcpu_per_socket(int value) {
        this.cpusPerSocket = value;

    }

    @Column(name = "vds_group_id", nullable = false)
    @Type(type = "guid")
    private Guid vdsGroupId;

    @XmlElement
    public Guid getvds_group_id() {
        return this.vdsGroupId;
    }

    public void setvds_group_id(Guid value) {
        this.vdsGroupId = value;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vmt_guid")
    @Type(type = "guid")
    private Guid id = new Guid();

    @XmlElement(name = "Id")
    public Guid getId() {
        return this.id;
    }

    public void setId(Guid value) {
        this.id = value;
    }

    @Size(max = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    @Column(name = "domain", length = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    private String domain;

    @XmlElement
    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
    }

    @Column(name = "num_of_monitors", nullable = false)
    private int numOfMonitors;

    @XmlElement
    public int getnum_of_monitors() {
        return numOfMonitors;
    }

    public void setnum_of_monitors(int value) {
        numOfMonitors = value;
    }

    @Column(name = "usb_policy")
    @Enumerated
    private UsbPolicy usbPolicy = UsbPolicy.forValue(0);

    @XmlElement
    public UsbPolicy getusb_policy() {
        return usbPolicy;
    }

    public void setusb_policy(UsbPolicy value) {
        usbPolicy = value;
    }

    @Column(name = "status", nullable = false)
    @Enumerated
    private VmTemplateStatus status = VmTemplateStatus.forValue(0);

    @XmlElement
    public VmTemplateStatus getstatus() {
        return status;
    }

    public void setstatus(VmTemplateStatus value) {
        if (status != value) {
            status = value;
        }
    }

    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    @Column(name = "time_zone", length = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String timezone;

    @XmlElement
    public String gettime_zone() {
        return timezone;
    }

    public void settime_zone(String value) {
        timezone = value;
    }

    @Column(name = "fail_back", nullable = false)
    private boolean fail_back;

    @XmlElement
    public boolean getfail_back() {
        return fail_back;
    }

    public void setfail_back(boolean value) {
        fail_back = value;
    }

    @Column(name = "is_auto_suspend", nullable = false)
    private boolean autosuspend;

    @XmlElement
    public boolean getis_auto_suspend() {
        return autosuspend;
    }

    public void setis_auto_suspend(boolean value) {
        autosuspend = value;
    }

    @Transient
    private String vdsGroupName;

    @XmlElement
    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;

    }

    @Column(name = "default_boot_sequence", nullable = false)
    @Enumerated
    private BootSequence defaultBootSequence = BootSequence.forValue(0);

    @XmlElement
    public BootSequence getdefault_boot_sequence() {
        return defaultBootSequence;
    }

    public void setdefault_boot_sequence(BootSequence value) {
        defaultBootSequence = value;
    }

    @Column(name = "vm_type", nullable = false)
    @Enumerated
    private VmType vmType = VmType.forValue(0);

    @XmlElement
    public VmType getvm_type() {
        return vmType;
    }

    public void setvm_type(VmType value) {
        vmType = value;
    }

    @Column(name = "hypervisor_type", nullable = false)
    @Enumerated
    private HypervisorType hypervisorType = HypervisorType.forValue(0);

    @XmlElement
    public HypervisorType gethypervisor_type() {
        return hypervisorType;
    }

    public void sethypervisor_type(HypervisorType value) {
        hypervisorType = value;
    }

    @Column(name = "operation_mode", nullable = false)
    @Enumerated
    private OperationMode operationMode = OperationMode.forValue(0);

    @XmlElement
    public OperationMode getoperation_mode() {
        return operationMode;
    }

    public void setoperation_mode(OperationMode value) {
        operationMode = value;
    }

    @Column(name = "nice_level", nullable = false)
    private int niceLevel;

    @XmlElement
    public int getnice_level() {
        return niceLevel;
    }

    public void setnice_level(int value) {
        niceLevel = value;
    }

    @XmlElement(name = "Interfaces")
    public List<VmNetworkInterface> getInterfaces() {
        return _Interfaces;
    }

    public void setInterfaces(List<VmNetworkInterface> value) {
        _Interfaces = value;
    }

    @XmlElement(name = "storage_pool_id")
    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storagePoolId;

    public NGuid getstorage_pool_id() {
        return storagePoolId;
    }

    public void setstorage_pool_id(NGuid value) {
        storagePoolId = value;
    }

    @XmlElement(name = "storage_pool_name")
    @Transient
    private String storagePoolName;

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    @XmlElement(name = "default_display_type")
    @Column(name = "default_display_type", nullable = false)
    @Enumerated
    private DisplayType defaultDisplayType = DisplayType.vnc;

    public DisplayType getdefault_display_type() {
        return defaultDisplayType;
    }

    public void setdefault_display_type(DisplayType value) {
        defaultDisplayType = value;
    }

    @XmlElement(name = "priority")
    @Column(name = "priority", nullable = false)
    private int priority;

    public int getpriority() {
        return priority;
    }

    public void setpriority(int value) {
        priority = value;
    }

    @Column(name = "auto_startup")
    private boolean autoStartup;

    @XmlElement
    public boolean getauto_startup() {
        return autoStartup;
    }

    public void setauto_startup(boolean value) {
        autoStartup = value;
    }

    @Column(name = "is_stateless")
    private boolean stateless;

    @XmlElement
    public boolean getis_stateless() {
        return stateless;
    }

    public void setis_stateless(boolean value) {
        stateless = value;
    }

    @XmlElement(name = "iso_path")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "iso_path", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath;

    public String getiso_path() {
        return isoPath;
    }

    public void setiso_path(String value) {
        isoPath = value;
    }

    @XmlElement(name = "origin")
    @Column(name = "origin")
    @Enumerated
    private OriginType origin = OriginType.forValue(0);

    public OriginType getorigin() {
        return origin;
    }

    public void setorigin(OriginType value) {
        origin = value;
    }

    @XmlElement(name = "initrd_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "initrd_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String initrdUrl;

    public String getinitrd_url() {
        return initrdUrl;
    }

    public void setinitrd_url(String value) {
        initrdUrl = value;
    }

    @XmlElement(name = "kernel_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernelUrl;

    public String getkernel_url() {
        return kernelUrl;
    }

    public void setkernel_url(String value) {
        kernelUrl = value;
    }

    @XmlElement(name = "kernel_params")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_params", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernelParams;

    public String getkernel_params() {
        return kernelParams;
    }

    public void setkernel_params(String value) {
        kernelParams = value;
    }

    @Transient
    private Map<String, DiskImage> diskMap = new HashMap<String, DiskImage>();

    @Transient
    private final ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();

    @Transient
    private HashMap<String, DiskImageTemplate> diskTemplateMap = new HashMap<String, DiskImageTemplate>();

    @XmlElement(name = "SizeGB")
    @Transient
    private double bootDiskSizeGB;

    public double getSizeGB() {
        return bootDiskSizeGB;
    }

    public void setSizeGB(double value) {
        bootDiskSizeGB = value;
    }

    public HashMap<String, DiskImageTemplate> getDiskMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private static final ArrayList<String> _vmProperties = new ArrayList<String>(
            Arrays.asList(new String[] { "name", "domain", "child_count", "description",
                    "default_display_type", "mem_size_mb", "vds_group_name", "status", "time_zone", "num_of_monitors",
                    "vds_group_id", "usb_policy", "num_of_sockets", "cpu_per_socket", "os", "is_auto_suspend",
                    "auto_startup", "priority", "default_boot_sequence", "is_stateless", "iso_path", "initrd_url",
                    "kernel_url", "kernel_params" }));

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _vmProperties;
    }

    @XmlElement(name = "ActualDiskSize")
    public double getActualDiskSize() {
        if (actualDiskSize == 0 && getDiskImageMap() != null) {
            for (DiskImage disk : getDiskImageMap().values()) {
                actualDiskSize += disk.getActualSize();
            }
        }
        return actualDiskSize;
    }

    /**
     * empty setters to fix CXF issue
     */
    public void setActualDiskSize(double actualDiskSize) {
    }

    public Map<String, DiskImage> getDiskImageMap() {
        return diskMap;
    }

    public void setDiskImageMap(Map<String, DiskImage> value) {
        diskMap = value;
    }

    @XmlElement(name = "DiskImageMap")
    public ValueObjectMap getSerializedDiskImageMap() {
        return new ValueObjectMap(diskMap, false);
    }

    public void setSerializedDiskImageMap(ValueObjectMap serializedDiskImageMap) {
        diskMap = (serializedDiskImageMap == null) ? null : serializedDiskImageMap.asMap();
    }

    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }


    @Transient
    private double actualDiskSize = 0;

    public boolean addDiskImageTemplate(DiskImageTemplate dit) {
        boolean retval = false;
        if (!getDiskMap().containsKey(dit.getinternal_drive_mapping())) {
            getDiskMap().put(dit.getinternal_drive_mapping(), dit);
            retval = true;
        }
        return retval;
    }

    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof VmTemplate) {
            returnValue = getId()
                    .equals(((VmTemplate) obj).getId());
        }
        return returnValue;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
