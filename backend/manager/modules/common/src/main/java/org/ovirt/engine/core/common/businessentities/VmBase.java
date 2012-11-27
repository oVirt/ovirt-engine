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
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.DesktopVM;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class VmBase extends IVdcQueryable implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 1078548170257965614L;
    private ArrayList<DiskImage> images;
    private final ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();
    private List<VmNetworkInterface> interfaces;
    private Map<Guid, VmDevice> managedDeviceMap = new HashMap<Guid, VmDevice>();
    private List<VmDevice> unmanagedDeviceList = new ArrayList<VmDevice>();

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vm_guid")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vdsGroupId;

    private VmOsType mOs = VmOsType.Unassigned;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate = new Date(0);

    @Size(max = BusinessEntitiesDefinitions.VM_DESCRIPTION_SIZE)
    @Column(name = "description", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @ValidDescription(message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class })
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
    private UsbPolicy usbPolicy = UsbPolicy.DISABLED;

    @Column(name = "fail_back", nullable = false)
    private boolean failBack;

    @Column(name = "default_boot_sequence", nullable = false)
    @Enumerated
    private BootSequence defaultBootSequence = BootSequence.C;

    @Column(name = "nice_level", nullable = false)
    private int niceLevel;

    @Column(name = "is_auto_suspend", nullable = false)
    private boolean autosuspend;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "auto_startup")
    private boolean autoStartup;

    @Column(name = "is_stateless")
    private boolean stateless;

    @Column(name = "is_delete_protected")
    private boolean deleteProtected;

    @Column(name = "db_generation")
    private long dbGeneration;

    @Column(name = "is_smartcard_enabled")
    private boolean smartcardEnabled;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "iso_path", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath = "";

    @Column(name = "origin")
    @Enumerated
    private OriginType origin;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelUrl;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "kernel_params", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelParams;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "initrd_url", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String initrdUrl;

    @Column(name = "allow_console_reconnect")
    private boolean allowConsoleReconnect;

    /**
     * this field is used to save the ovf version,
     * in case the vm object was built from ovf.
     * not persisted to db.
     */
    private String ovfVersion;

    // not persisted to db
    private Date exportDate;

    public VmBase() {
    }

    @Column(name = "quota_id")
    private Guid quotaId;

    /** Transient field for GUI presentation purposes. */
    private String quotaName;

    /** Transient field for GUI presentation purposes. */
    private boolean isQuotaDefault;

    /** Transient field for GUI presentation purposes. */
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    @OvfExportOnlyField(valueToIgnore = "MIGRATABLE", exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Column(name = "migration_support")
    private MigrationSupport migrationSupport = MigrationSupport.MIGRATABLE;

    @Column(name = "dedicated_vm_for_vds")
    @Type(type = "guid")
    private NGuid dedicatedVmForVds;

    @Column(name = "default_display_type")
    @Enumerated
    protected DisplayType defaultDisplayType = DisplayType.qxl;

    public VmBase(Guid id,
            Guid vdsGroupId,
            VmOsType os,
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
            boolean failBack,
            BootSequence defaultBootSequence,
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
            Guid quotaId,
            boolean smartcardEnabled,
            boolean deleteProtected) {
        super();
        this.id = id;
        this.vdsGroupId = vdsGroupId;
        this.mOs = os;
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
        this.failBack = failBack;
        this.defaultBootSequence = defaultBootSequence;
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
        this.smartcardEnabled = smartcardEnabled;
        this.deleteProtected = deleteProtected;
        setQuotaId(quotaId);
    }

    public long getDbGeneration() {
        return dbGeneration;
    }

    public void setDbGeneration(long value) {
        this.dbGeneration = value;
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

    public void setImages(ArrayList<DiskImage> value) {
        images = value;
    }

    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public Map<Guid, VmDevice> getManagedDeviceMap() {
        return managedDeviceMap;
    }

    public void setManagedDeviceMap(Map<Guid, VmDevice> map) {
        this.managedDeviceMap = map;
    }

    public List<VmDevice> getUnmanagedDeviceList() {
        return unmanagedDeviceList;
    }

    public void setUnmanagedDeviceList(List<VmDevice> list) {
        this.unmanagedDeviceList = list;
    }

    public int getNumOfCpus() {
        return this.getCpuPerSocket() * this.getNumOfSockets();
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        this.vdsGroupId = value;
    }

    public VmOsType getOs() {
        return mOs;
    }

    public void setOs(VmOsType value) {
        mOs = value;
    }

    @Deprecated
    public VmOsType getOsType() {
        return getOs();
    }

    @Deprecated
    public void setOsType(VmOsType value) {
        setOs(value);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date value) {
        this.creationDate = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public int getMemSizeMb() {
        return memSizeMB;
    }

    public void setMemSizeMb(int value) {
        this.memSizeMB = value;
    }

    public int getNumOfSockets() {
        return numOfSockets;
    }

    public void setNumOfSockets(int value) {
        this.numOfSockets = value;
    }

    public int getCpuPerSocket() {
        return cpusPerSocket;
    }

    public void setCpuPerSocket(int value) {
        this.cpusPerSocket = value;
    }

    public int getNumOfMonitors() {
        return numOfMonitors;
    }

    public void setNumOfMonitors(int value) {
        numOfMonitors = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public String getTimeZone() {
        return timezone;
    }

    public void setTimeZone(String value) {
        timezone = value;
    }

    public VmType getVmType() {
        return vmType;
    }

    public void setVmType(VmType value) {
        vmType = value;
    }

    public UsbPolicy getUsbPolicy() {
        return usbPolicy;
    }

    public void setUsbPolicy(UsbPolicy value) {
        usbPolicy = value;
    }

    public boolean isFailBack() {
        return failBack;
    }

    public void setFailBack(boolean value) {
        failBack = value;
    }

    public BootSequence getDefaultBootSequence() {
        return defaultBootSequence;
    }

    public void setDefaultBootSequence(BootSequence value) {
        defaultBootSequence = value;
    }

    public int getNiceLevel() {
        return niceLevel;
    }

    public void setNiceLevel(int value) {
        niceLevel = value;
    }

    public boolean isAutoSuspend() {
        return autosuspend;
    }

    public void setAutoSuspend(boolean value) {
        autosuspend = value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int value) {
        priority = value;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean value) {
        autoStartup = value;
    }

    public boolean isStateless() {
        return stateless;
    }

    public void setStateless(boolean value) {
        stateless = value;
    }

    public String getIsoPath() {
        return isoPath;
    }

    public void setIsoPath(String value) {
        isoPath = value;
    }

    public OriginType getOrigin() {
        return origin;
    }

    public void setOrigin(OriginType value) {
        origin = value;
    }

    public String getKernelUrl() {
        return kernelUrl;
    }

    public void setKernelUrl(String value) {
        kernelUrl = value;
    }

    public String getKernelParams() {
        return kernelParams;
    }

    public void setKernelParams(String value) {
        kernelParams = value;
    }

    public String getInitrdUrl() {
        return initrdUrl;
    }

    public void setInitrdUrl(String value) {
        initrdUrl = value;
    }

    public boolean isAllowConsoleReconnect() {
        return allowConsoleReconnect;
    }

    public void setAllowConsoleReconnect(boolean value) {
        allowConsoleReconnect = value;
    }

    public void setExportDate(Date value) {
        this.exportDate = value;
    }

    public Date getExportDate() {
        return exportDate;
    }

    public boolean isSmartcardEnabled() {
        return smartcardEnabled;
    }

    public void setSmartcardEnabled(boolean smartcardEnabled) {
        this.smartcardEnabled = smartcardEnabled;
    }

    public boolean isDeleteProtected() {
        return deleteProtected;
    }

    public void setDeleteProtected(boolean deleteProtected) {
        this.deleteProtected = deleteProtected;
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
        result = prime * result + (failBack ? 1231 : 1237);
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
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + priority;
        result = prime * result + (stateless ? 1231 : 1237);
        result = prime * result + (smartcardEnabled ? 1231 : 1237);
        result = prime * result + ((timezone == null) ? 0 : timezone.hashCode());
        result = prime * result + ((usbPolicy == null) ? 0 : usbPolicy.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vmType == null) ? 0 : vmType.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + (allowConsoleReconnect ? 1231 : 1237);
        result = prime * result + ((migrationSupport == null) ? 0 : migrationSupport.hashCode());
        result = prime * result + ((dedicatedVmForVds == null) ? 0 : dedicatedVmForVds.hashCode());

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
        if (failBack != other.failBack) {
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
        if (origin != other.origin) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        if (stateless != other.stateless) {
            return false;
        }
        if (smartcardEnabled != other.smartcardEnabled) {
            return false;
        }
        if (deleteProtected != other.deleteProtected) {
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
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null) {
                return false;
            }
        } else if (!vdsGroupId.equals(other.vdsGroupId)) {
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
        if (allowConsoleReconnect != other.allowConsoleReconnect) {
            return false;
        }
        if (dedicatedVmForVds == null) {
            if (other.dedicatedVmForVds != null) {
                return false;
            }
        } else {
            if (!dedicatedVmForVds.equals(other.dedicatedVmForVds)) {
                return false;
            }
        }
        if (migrationSupport != other.migrationSupport) {
            return false;
        }
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

    public boolean isQuotaDefault() {
        return isQuotaDefault;
    }

    public void setQuotaDefault(boolean isQuotaDefault) {
        this.isQuotaDefault = isQuotaDefault;
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum quotaEnforcementType) {
        this.quotaEnforcementType = quotaEnforcementType;
    }

    public MigrationSupport getMigrationSupport() {
        return migrationSupport;
    }

    public void setMigrationSupport(MigrationSupport migrationSupport) {
        this.migrationSupport = migrationSupport;
    }

    public NGuid getDedicatedVmForVds() {
        return dedicatedVmForVds;
    }

    public void setDedicatedVmForVds(NGuid value) {
        dedicatedVmForVds = value;
    }

    public DisplayType getDefaultDisplayType() {
        return defaultDisplayType;
    }

    public void setDefaultDisplayType(DisplayType value) {
        defaultDisplayType = value;
    }

    public String getOvfVersion() {
        return ovfVersion;
    }

    public void setOvfVersion(String ovfVersion) {
        this.ovfVersion = ovfVersion;
    }
}
