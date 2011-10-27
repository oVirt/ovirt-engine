package org.ovirt.engine.core.common.businessentities;

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
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;

//using VdcDAL.DbBroker;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmTemplate")
@Entity
@Table(name = "vm_templates")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VmTemplate extends VmBase implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -522552511046744989L;
    @Transient
    private List<VmNetworkInterface> _Interfaces = new java.util.ArrayList<VmNetworkInterface>();

    public VmTemplate() {
        autosuspend = false;
        niceLevel = 0;
        diskTemplateMap = new java.util.HashMap<String, DiskImageTemplate>();
    }

    public VmTemplate(int child_count, java.util.Date creation_date, String description, int mem_size_mb, String name,
            int num_of_sockets, int cpu_per_socket, VmOsType os, Guid vds_group_id, Guid vmt_guid, String domain,
            int num_of_monitors, int status, int usb_policy, String time_zone, boolean is_auto_suspend, int nice_level,
            boolean fail_back, BootSequence default_boot_sequence, VmType vm_type, HypervisorType hypervisor_type,
            OperationMode operation_mode) {
        diskTemplateMap = new java.util.HashMap<String, DiskImageTemplate>();

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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getchild_count() {
        return this.childCount;
    }

    public void setchild_count(int value) {
        this.childCount = value;
        OnPropertyChanged(new PropertyChangedEventArgs("child_count"));
    }

    @Column(name = "creation_date", nullable = false)
    private java.util.Date creationDate = new java.util.Date(0);

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:

    @XmlElement
    public String getdescription() {
        return this.description;
    }

    public void setdescription(String value) {
        this.description = value;
        OnPropertyChanged(new PropertyChangedEventArgs("description"));
    }

    @Column(name = "mem_size_mb", nullable = false)
    private int memSizeMB;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getmem_size_mb() {
        return this.memSizeMB;
    }

    public void setmem_size_mb(int value) {
        this.memSizeMB = value;
        OnPropertyChanged(new PropertyChangedEventArgs("mem_size_mb"));
    }

    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE,
            message = "VALIDATION.VM_TEMPLATE.NAME.MAX",
            groups = { CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "ACTION_TYPE_FAILED_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Column(name = "name", length = BusinessEntitiesDefinitions.VM_TEMPLATE_NAME_SIZE, nullable = false)
    private String name;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        if (!StringHelper.EqOp(this.name, value)) {
            this.name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("name"));
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "num_of_sockets")
    public int getnum_of_sockets() {
        return this.numOfSockets;
    }

    public void setnum_of_sockets(int value) {
        this.numOfSockets = value;
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_sockets"));
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_cpus"));
    }

    @Column(name = "cpu_per_socket", nullable = false)
    private int cpusPerSocket = 1;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "cpu_per_socket")
    public int getcpu_per_socket() {
        return this.cpusPerSocket;
    }

    public void setcpu_per_socket(int value) {
        this.cpusPerSocket = value;
        OnPropertyChanged(new PropertyChangedEventArgs("cpu_per_socket"));
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_cpus"));

    }

    // private System.String osField;

    // public System.String os
    // {
    // get { return this.osField; }
    // set { this.osField = value; }
    // }

    @Column(name = "vds_group_id", nullable = false)
    @Type(type = "guid")
    private Guid vdsGroupId;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
        OnPropertyChanged(new PropertyChangedEventArgs("domain"));
    }

    @Column(name = "num_of_monitors", nullable = false)
    private int numOfMonitors;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getnum_of_monitors() {
        return numOfMonitors;
    }

    public void setnum_of_monitors(int value) {
        numOfMonitors = value;
        OnPropertyChanged(new PropertyChangedEventArgs("num_of_monitors"));
    }

    @Column(name = "usb_policy")
    @Enumerated
    private UsbPolicy usbPolicy = UsbPolicy.forValue(0);

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public VmTemplateStatus getstatus() {
        return status;
    }

    public void setstatus(VmTemplateStatus value) {
        if (status != value) {
            status = value;
            OnPropertyChanged(new PropertyChangedEventArgs("status"));
        }
    }

    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    @Column(name = "time_zone", length = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String timezone;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public String gettime_zone() {
        return timezone;
    }

    public void settime_zone(String value) {
        timezone = value;
        OnPropertyChanged(new PropertyChangedEventArgs("time_zone"));
    }

    @Column(name = "fail_back", nullable = false)
    private boolean fail_back;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public boolean getfail_back() {
        return fail_back;
    }

    public void setfail_back(boolean value) {
        fail_back = value;
    }

    @Column(name = "is_auto_suspend", nullable = false)
    private boolean autosuspend;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public boolean getis_auto_suspend() {
        return autosuspend;
    }

    public void setis_auto_suspend(boolean value) {
        autosuspend = value;
    }

    @Transient
    private String vdsGroupName;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public String getvds_group_name() {
        return vdsGroupName;
    }

    public void setvds_group_name(String value) {
        vdsGroupName = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vds_group_name"));

    }

    @Column(name = "default_boot_sequence", nullable = false)
    @Enumerated
    private BootSequence defaultBootSequence = BootSequence.forValue(0);

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public BootSequence getdefault_boot_sequence() {
        return defaultBootSequence;
    }

    public void setdefault_boot_sequence(BootSequence value) {
        defaultBootSequence = value;
        OnPropertyChanged(new PropertyChangedEventArgs("default_boot_sequence"));
    }

    @Column(name = "vm_type", nullable = false)
    @Enumerated
    private VmType vmType = VmType.forValue(0);

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public OperationMode getoperation_mode() {
        return operationMode;
    }

    public void setoperation_mode(OperationMode value) {
        operationMode = value;
    }

    @Column(name = "nice_level", nullable = false)
    private int niceLevel;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getnice_level() {
        return niceLevel;
    }

    public void setnice_level(int value) {
        niceLevel = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "Interfaces")
    public List<VmNetworkInterface> getInterfaces() {
        return _Interfaces;
    }

    public void setInterfaces(List<VmNetworkInterface> value) {
        _Interfaces = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "storage_pool_name")
    @Transient
    private String storagePoolName;

    public String getstorage_pool_name() {
        return storagePoolName;
    }

    public void setstorage_pool_name(String value) {
        storagePoolName = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "default_display_type")
    @Column(name = "default_display_type", nullable = false)
    @Enumerated
    private DisplayType defaultDisplayType = DisplayType.forValue(0);

    public DisplayType getdefault_display_type() {
        return defaultDisplayType;
    }

    public void setdefault_display_type(DisplayType value) {
        defaultDisplayType = value;
        OnPropertyChanged(new PropertyChangedEventArgs("default_display_type"));
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "priority")
    @Column(name = "priority", nullable = false)
    private int priority;

    public int getpriority() {
        return priority;
    }

    public void setpriority(int value) {
        priority = value;
        OnPropertyChanged(new PropertyChangedEventArgs("priority"));
    }

    @Column(name = "auto_startup")
    private boolean autoStartup;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public boolean getauto_startup() {
        return autoStartup;
    }

    public void setauto_startup(boolean value) {
        autoStartup = value;
        OnPropertyChanged(new PropertyChangedEventArgs("auto_startup"));
    }

    @Column(name = "is_stateless")
    private boolean stateless;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public boolean getis_stateless() {
        return stateless;
    }

    public void setis_stateless(boolean value) {
        stateless = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "iso_path")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "iso_path", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath;

    public String getiso_path() {
        return isoPath;
    }

    public void setiso_path(String value) {
        isoPath = value;
        OnPropertyChanged(new PropertyChangedEventArgs("iso_path"));
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
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
        OnPropertyChanged(new PropertyChangedEventArgs("initrd_url"));
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
        OnPropertyChanged(new PropertyChangedEventArgs("kernel_url"));
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
        OnPropertyChanged(new PropertyChangedEventArgs("kernel_params"));
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @Transient
    private Map<String, DiskImage> diskMap = new java.util.HashMap<String, DiskImage>();
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @Transient
    private final java.util.ArrayList<DiskImage> diskList = new java.util.ArrayList<DiskImage>();
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    // @XmlElement
    @Transient
    private java.util.HashMap<String, DiskImageTemplate> diskTemplateMap = new java.util.HashMap<String, DiskImageTemplate>();
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "SizeGB")
    @Transient
    private double bootDiskSizeGB;

    public double getSizeGB() {
        return bootDiskSizeGB;
    }

    public void setSizeGB(double value) {
        bootDiskSizeGB = value;
    }

    public java.util.HashMap<String, DiskImageTemplate> getDiskMap() {
        return diskTemplateMap;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private static final java.util.ArrayList<String> _vmProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "name", "domain", "child_count", "description",
                    "default_display_type", "mem_size_mb", "vds_group_name", "status", "time_zone", "num_of_monitors",
                    "vds_group_id", "usb_policy", "num_of_sockets", "cpu_per_socket", "os", "is_auto_suspend",
                    "auto_startup", "priority", "default_boot_sequence", "is_stateless", "iso_path", "initrd_url",
                    "kernel_url", "kernel_params" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
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

    // This property was added for Serialization purposes (input/output
    // parameter from the c#)
    @XmlElement(name = "DiskImageMap")
    public ValueObjectMap getSerializedDiskImageMap() {
        return new ValueObjectMap(diskMap, false);
    }

    public void setSerializedDiskImageMap(ValueObjectMap serializedDiskImageMap) {
        diskMap = (serializedDiskImageMap == null) ? null : serializedDiskImageMap.asMap();
    }

    public java.util.ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:

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

    // public void updateDisksToDb()
    // {
    // foreach (DiskImageTemplate dit in DiskMap.Values)
    // {
    // DbFacade.Instance.saveVmTemplateDIsk(dit);
    // }
    // }

    // public void addDisksToDb()
    // {
    // foreach (DiskImageTemplate dit in DiskMap.Values)
    // {
    // DbFacade.Instance.addVmTemplateDisk(dit);
    // }
    // }

    // public void removeDisksFromDb()
    // {
    // foreach (DiskImageTemplate dit in DiskMap.Values)
    // {
    // DbFacade.Instance.removeVmTemplateDisk(dit.it_guid, dit.vmt_guid);
    // }
    // }

    // override object.Equals
    @Override
    public boolean equals(Object obj) {
        boolean returnValue = super.equals(obj);
        if (!returnValue && obj != null && obj instanceof VmTemplate) {
            returnValue = getId()
                    .equals(((VmTemplate) obj).getId());
        }
        return returnValue;
    }

    // override object.GetHashCode
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    // public IEnumerator<DiskImageTemplate> GetEnumerator()
    // {
    // return mDiskMap.Values.GetEnumerator();
    // }

    // /#endregion

    // /#region IEnumerable Members

    // System.Collections.IEnumerator
    // System.Collections.IEnumerable.GetEnumerator()
    // {
    // //throw new
    // RuntimeException("The method or operation is not implemented.");
    // return mDiskMap.Values.GetEnumerator();
    // }

}
