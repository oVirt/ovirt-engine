package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.DesktopVM;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmBase")
public class VmBase extends IVdcQueryable implements INotifyPropertyChanged, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1078548170257965614L;
    private ArrayList<DiskImage> images;
    private ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();
    private List<VmNetworkInterface> interfaces;
    private Map<Guid, VmDevice> vmManagedDeviceMap = new HashMap<Guid, VmDevice>();
    private List<VmDevice> vmUnManagedDeviceList = new ArrayList<VmDevice>();

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vm_guid")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vds_group_id;

    private VmOsType mOs = VmOsType.Unassigned;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate = new Date(0);

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.ONLY_ASCII_OR_NONE,
            message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String description;

    @Column(name = "mem_size_mb", nullable = false)
    private int memSizeMB;

    @Column(name = "num_of_sockets", nullable = false)
    private int numOfSockets = 1;

    @Column(name = "cpu_per_socket", nullable = false)
    private int cpusPerSocket = 1;

    @Column(name = "num_of_monitors")
    @IntegerContainedInConfigValueList(configValue = ConfigValues.ValidNumOfMonitors, groups = DesktopVM.class,
            message = "VALIDATION.VM.NUM_OF_MONITORS.EXCEEDED")
    private int numOfMonitors;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    @Column(name = "domain", length = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    private String domain;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    @Column(name = "time_zone", length = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String timezone;

    @Column(name = "vm_type", nullable = false)
    @Enumerated
    private VmType vmType = VmType.Desktop;

    @Column(name = "usb_policy")
    @Enumerated
    private UsbPolicy usbPolicy = UsbPolicy.Enabled;

    @Column(name = "fail_back", nullable = false)
    private boolean fail_back;

    @Column(name = "default_boot_sequence", nullable = false)
    @Enumerated
    private BootSequence defaultBootSequence = BootSequence.C;

    @Column(name = "hypervisor_type", nullable = false)
    @Enumerated
    private HypervisorType hypervisorType = HypervisorType.KVM;

    @Column(name = "operation_mode", nullable = false)
    @Enumerated
    private OperationMode operationMode = OperationMode.FullVirtualized;

    @Column(name = "nice_level", nullable = false)
    private int niceLevel;

    @Column(name = "is_auto_suspend", nullable = false)
    private boolean autosuspend;

    @XmlElement(name = "priority")
    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "auto_startup")
    private boolean autoStartup;

    @Column(name = "is_stateless")
    private boolean stateless;

    @XmlElement(name = "iso_path")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "iso_path", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath;

    @XmlElement(name = "origin")
    @Column(name = "origin")
    @Enumerated
    private OriginType origin;

    @XmlElement(name = "kernel_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernelUrl;

    @XmlElement(name = "kernel_params")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_params", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String kernelParams;

    @XmlElement(name = "initrd_url")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "initrd_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String initrdUrl;

    // not persist to db
    private java.util.Date exportDate;

   public VmBase() {
   }

   private Guid quotaId;

   /**
    * Transient field for GUI presentation purposes.
    */
   private String quotaName;

    public VmBase(Guid id,
            Guid vds_group_id,
            VmOsType mOs,
            Date creationDate,
            String description,
            int memSizeMB,
            int numOfSockets,
            int cpusPerSocket,
            int numOfMonitors,
            String domain,
            String timezone,
            VmType vmType,
            UsbPolicy usbPolicy,
            boolean fail_back,
            BootSequence defaultBootSequence,
            HypervisorType hypervisorType,
            OperationMode operationMode,
            int niceLevel,
            boolean autosuspend,
            int priority,
            boolean autoStartup,
            boolean stateless,
            String isoPath,
            OriginType origin,
            String kernelUrl,
            String kernelParams,
            String initrdUrl,
            Guid quotaId) {
        super();
        this.id = id;
        this.vds_group_id = vds_group_id;
        this.mOs = mOs;
        this.creationDate = creationDate;
        this.description = description;
        this.memSizeMB = memSizeMB;
        this.numOfSockets = numOfSockets;
        this.cpusPerSocket = cpusPerSocket;
        this.numOfMonitors = numOfMonitors;
        this.domain = domain;
        this.timezone = timezone;
        this.vmType = vmType;
        this.usbPolicy = usbPolicy;
        this.fail_back = fail_back;
        this.defaultBootSequence = defaultBootSequence;
        this.hypervisorType = hypervisorType;
        this.operationMode = operationMode;
        this.niceLevel = niceLevel;
        this.autosuspend = autosuspend;
        this.priority = priority;
        this.autoStartup = autoStartup;
        this.stateless = stateless;
        this.isoPath = isoPath;
        this.origin = origin;
        this.kernelUrl = kernelUrl;
        this.kernelParams = kernelParams;
        this.initrdUrl = initrdUrl;
        this.setQuotaId(quotaId);
    }

    public List<VmNetworkInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<VmNetworkInterface> value) {
        interfaces = value;
    }

    public ArrayList<DiskImage> getImages() {
        return images;
    }

    public void setImages(java.util.ArrayList<DiskImage> value) {
        images = value;
    }

    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public Map<Guid, VmDevice> getManagedVmDeviceMap() {
        return vmManagedDeviceMap;
    }

    public void setManagedDeviceMap(Map<Guid, VmDevice> map) {
        this.vmManagedDeviceMap = map;
    }

    public List<VmDevice> getUnmanagedDeviceList() {
        return vmUnManagedDeviceList;
    }

    public void setUnmanagedDeviceList(List<VmDevice> list) {
        this.vmUnManagedDeviceList = list;
    }

    @XmlElement(name = "Id")
    public Guid getId() {
        return this.id;
    }

    public void setId(Guid value) {
        this.id = value;
    }

    @XmlElement
    public Guid getvds_group_id() {
        return this.vds_group_id;
    }

    public void setvds_group_id(Guid value) {
        this.vds_group_id = value;
    }

    @XmlElement
    public VmOsType getos() {
        return mOs;
    }

    public void setos(VmOsType value) {
        mOs = value;
    }

    @Deprecated
    public VmOsType getOsType() {
        return getos();
    }

    @Deprecated
    public void setOsType(VmOsType value) {
        setos(value);
    }

    @XmlElement
    public Date getcreation_date() {
        return this.creationDate;
    }

    public void setcreation_date(java.util.Date value) {
        this.creationDate = value;
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
        return this.memSizeMB;
    }

    public void setmem_size_mb(int value) {
        this.memSizeMB = value;
    }

    @XmlElement(name = "num_of_sockets")
    public int getnum_of_sockets() {
        return this.numOfSockets;
    }

    public void setnum_of_sockets(int value) {
        this.numOfSockets = value;
    }

    @XmlElement(name = "cpu_per_socket")
    public int getcpu_per_socket() {
        return this.cpusPerSocket;
    }

    public void setcpu_per_socket(int value) {
        this.cpusPerSocket = value;
    }

    @XmlElement
    public int getnum_of_monitors() {
        return numOfMonitors;
    }

    public void setnum_of_monitors(int value) {
        numOfMonitors = value;
    }

    @XmlElement
    public String getdomain() {
        return domain;
    }

    public void setdomain(String value) {
        domain = value;
    }

    @XmlElement
    public String gettime_zone() {
        return timezone;
    }

    public void settime_zone(String value) {
        timezone = value;
    }

    @XmlElement
    public VmType getvm_type() {
        return vmType;
    }

    public void setvm_type(VmType value) {
        vmType = value;
    }

    @XmlElement
    public UsbPolicy getusb_policy() {
        return usbPolicy;
    }

    public void setusb_policy(UsbPolicy value) {
        usbPolicy = value;
    }

    @XmlElement
    public boolean getfail_back() {
        return fail_back;
    }

    public void setfail_back(boolean value) {
        fail_back = value;
    }

    @XmlElement
    public BootSequence getdefault_boot_sequence() {
        return defaultBootSequence;
    }

    public void setdefault_boot_sequence(BootSequence value) {
        defaultBootSequence = value;
    }

    @XmlElement
    public HypervisorType gethypervisor_type() {
        return hypervisorType;
    }

    public void sethypervisor_type(HypervisorType value) {
        hypervisorType = value;
    }

    @XmlElement
    public OperationMode getoperation_mode() {
        return operationMode;
    }

    public void setoperation_mode(OperationMode value) {
        operationMode = value;
    }

    @XmlElement
    public int getnice_level() {
        return niceLevel;
    }

    public void setnice_level(int value) {
        niceLevel = value;
    }

    @XmlElement
    public boolean getis_auto_suspend() {
        return autosuspend;
    }

    public void setis_auto_suspend(boolean value) {
        autosuspend = value;
    }

    public int getpriority() {
        return priority;
    }

    public void setpriority(int value) {
        priority = value;
    }

    @XmlElement
    public boolean getauto_startup() {
        return autoStartup;
    }

    public void setauto_startup(boolean value) {
        autoStartup = value;
    }

    @XmlElement
    public boolean getis_stateless() {
        return stateless;
    }

    public void setis_stateless(boolean value) {
        stateless = value;
    }

    public String getiso_path() {
        return isoPath;
    }

    public void setiso_path(String value) {
        isoPath = value;
    }

    public OriginType getorigin() {
        return origin;
    }

    public void setorigin(OriginType value) {
        origin = value;
    }

    public String getkernel_url() {
        return kernelUrl;
    }

    public void setkernel_url(String value) {
        kernelUrl = value;
    }

    public String getkernel_params() {
        return kernelParams;
    }

    public void setkernel_params(String value) {
        kernelParams = value;
    }

    public String getinitrd_url() {
        return initrdUrl;
    }

    public void setinitrd_url(String value) {
        initrdUrl = value;
    }

    public void setExportDate(java.util.Date value) {
        this.exportDate = value;
    }

    public java.util.Date getExportDate() {
        return this.exportDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (autoStartup ? 1231 : 1237);
        result = prime * result + (autosuspend ? 1231 : 1237);
        result = prime * result + cpusPerSocket;
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((defaultBootSequence == null) ? 0 : defaultBootSequence.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + (fail_back ? 1231 : 1237);
        result = prime * result + ((hypervisorType == null) ? 0 : hypervisorType.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((initrdUrl == null) ? 0 : initrdUrl.hashCode());
        result = prime * result + ((isoPath == null) ? 0 : isoPath.hashCode());
        result = prime * result + ((kernelParams == null) ? 0 : kernelParams.hashCode());
        result = prime * result + ((kernelUrl == null) ? 0 : kernelUrl.hashCode());
        result = prime * result + ((mOs == null) ? 0 : mOs.hashCode());
        result = prime * result + memSizeMB;
        result = prime * result + niceLevel;
        result = prime * result + numOfSockets;
        result = prime * result + numOfMonitors;
        result = prime * result + ((operationMode == null) ? 0 : operationMode.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + priority;
        result = prime * result + (stateless ? 1231 : 1237);
        result = prime * result + ((timezone == null) ? 0 : timezone.hashCode());
        result = prime * result + ((usbPolicy == null) ? 0 : usbPolicy.hashCode());
        result = prime * result + ((vds_group_id == null) ? 0 : vds_group_id.hashCode());
        result = prime * result + ((vmType == null) ? 0 : vmType.hashCode());
        result = prime * result + ((quotaId== null) ? 0 : quotaId.hashCode());

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
        if (!(obj instanceof VmBase)) {
            return false;
        }
        VmBase other = (VmBase) obj;
        if (autoStartup != other.autoStartup) {
            return false;
        }
        if (autosuspend != other.autosuspend) {
            return false;
        }
        if (cpusPerSocket != other.cpusPerSocket) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (defaultBootSequence != other.defaultBootSequence) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!domain.equals(other.domain)) {
            return false;
        }
        if (fail_back != other.fail_back) {
            return false;
        }
        if (hypervisorType != other.hypervisorType) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (initrdUrl == null) {
            if (other.initrdUrl != null) {
                return false;
            }
        } else if (!initrdUrl.equals(other.initrdUrl)) {
            return false;
        }
        if (isoPath == null) {
            if (other.isoPath != null) {
                return false;
            }
        } else if (!isoPath.equals(other.isoPath)) {
            return false;
        }
        if (kernelParams == null) {
            if (other.kernelParams != null) {
                return false;
            }
        } else if (!kernelParams.equals(other.kernelParams)) {
            return false;
        }
        if (kernelUrl == null) {
            if (other.kernelUrl != null) {
                return false;
            }
        } else if (!kernelUrl.equals(other.kernelUrl)) {
            return false;
        }
        if (mOs != other.mOs) {
            return false;
        }
        if (memSizeMB != other.memSizeMB) {
            return false;
        }
        if (niceLevel != other.niceLevel) {
            return false;
        }
        if (numOfSockets != other.numOfSockets) {
            return false;
        }
        if (numOfMonitors != other.numOfMonitors) {
            return false;
        }
        if (operationMode != other.operationMode) {
            return false;
        }
        if (origin != other.origin) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (stateless != other.stateless) {
            return false;
        }
        if (timezone == null) {
            if (other.timezone != null) {
                return false;
            }
        } else if (!timezone.equals(other.timezone)) {
            return false;
        }
        if (usbPolicy != other.usbPolicy) {
            return false;
        }
        if (vds_group_id == null) {
            if (other.vds_group_id != null) {
                return false;
            }
        } else if (!vds_group_id.equals(other.vds_group_id)) {
            return false;
        }
        if (vmType != other.vmType) {
            return false;
        }
        if (quotaId == null) {
            if (other.quotaId != null)
                return false;
        } else if (!quotaId.equals(other.quotaId))
            return false;
        return true;
    }

    public Guid getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    public String getQuotaName() {
        return quotaName;
    }

    public void setQuotaName(String quotaName) {
        this.quotaName = quotaName;
    }
}
