package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.NullOrStringContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VmBase extends IVdcQueryable implements BusinessEntity<Guid>, Nameable {
    private static final long serialVersionUID = 1078548170257965614L;

    @EditableField
    private String name = "";

    @EditableField
    private ArrayList<DiskImage> images;

    @EditableField
    private List<VmNetworkInterface> interfaces;

    private ArrayList<DiskImage> diskList = new ArrayList<DiskImage>();
    private Map<Guid, VmDevice> managedDeviceMap = new HashMap<Guid, VmDevice>();
    private List<VmDevice> unmanagedDeviceList = new ArrayList<VmDevice>();

    private Guid id = Guid.Empty;

    @EditableOnVmStatusField
    private Guid vdsGroupId;

    @EditableField
    private int osId;

    @EditableField
    private Date creationDate = new Date(0);

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.VM_DESCRIPTION_SIZE)
    @ValidDescription(message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String description;

    @EditableOnVmStatusField
    private int memSizeMb;

    @EditableOnVmStatusField
    private int numOfSockets = 1;

    @EditableOnVmStatusField
    private int cpuPerSocket = 1;

    @EditableOnVmStatusField
    @IntegerContainedInConfigValueList(configValue = ConfigValues.ValidNumOfMonitors,
            message = "VALIDATION.VM.NUM_OF_MONITORS.EXCEEDED")
    private int numOfMonitors;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_DOMAIN_SIZE)
    private String domain;

    @EditableOnVmStatusField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String timeZone;

    @EditableField
    private VmType vmType = VmType.Desktop;

    @EditableField
    private UsbPolicy usbPolicy = UsbPolicy.DISABLED;

    private boolean failBack;

    @EditableField
    private BootSequence defaultBootSequence = BootSequence.C;

    @EditableOnVmStatusField
    private int niceLevel;

    @EditableField
    private int priority;

    @EditableField
    private boolean autoStartup;

    @EditableOnVmStatusField
    private boolean stateless;

    @EditableField
    private boolean deleteProtected;

    @EditableField
    private long dbGeneration;

    @EditableField
    private boolean smartcardEnabled;

    @EditableOnVmStatusField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath = "";

    private OriginType origin;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelUrl;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelParams;

    @EditableField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String initrdUrl;

    @EditableField
    private boolean allowConsoleReconnect;

    /**
     * if this field is null then value should be taken from cluster
     */
    @EditableField
    private Boolean tunnelMigration;

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

    @EditableField
    private Guid quotaId;


    /** Transient field for GUI presentation purposes. */
    @EditableField
    private String quotaName;

    @EditableField
    /** Transient field for GUI presentation purposes. */
    private boolean quotaDefault;

    /** Transient field for GUI presentation purposes. */
    @EditableField
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    @EditableField
    @OvfExportOnlyField(valueToIgnore = "MIGRATABLE", exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    private MigrationSupport migrationSupport = MigrationSupport.MIGRATABLE;

    @EditableField
    private Guid dedicatedVmForVds;

    @EditableOnVmStatusField
    protected DisplayType defaultDisplayType = DisplayType.qxl;

    @EditableOnVmStatusField
    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
        groups = { CreateEntity.class, UpdateEntity.class },
        message = "VALIDATION.VM.INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    @EditableField
    private int minAllocatedMem;

    @EditableField
    private boolean runAndPause = false;

    public VmBase(Guid id,
            Guid vdsGroupId,
            int osId,
            Date creationDate,
            String description,
            int memSizeMb,
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
            boolean deleteProtected,
            Boolean tunnelMigration,
            String vncKeyboardLayout,
            int minAllocatedMem,
            boolean runAndPause) {
        super();
        this.id = id;
        this.vdsGroupId = vdsGroupId;
        this.osId = osId;
        this.creationDate = creationDate;
        this.description = description;
        this.memSizeMb = memSizeMb;
        this.numOfSockets = numOfSockets;
        this.cpuPerSocket = cpusPerSocket;
        this.numOfMonitors = numOfMonitors;
        this.domain = domain;
        this.timeZone = timezone;
        this.vmType = vmType;
        this.usbPolicy = usbPolicy;
        this.failBack = failBack;
        this.defaultBootSequence = defaultBootSequence;
        this.niceLevel = niceLevel;
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
        this.tunnelMigration = tunnelMigration;
        this.vncKeyboardLayout = vncKeyboardLayout;
        this.minAllocatedMem = minAllocatedMem;
        this.runAndPause = runAndPause;
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

    public int getOsId() {
        return osId;
    }

    public void setOsId(int value) {
        osId = value;
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
        return memSizeMb;
    }

    public void setMemSizeMb(int value) {
        this.memSizeMb = value;
    }

    public int getNumOfSockets() {
        return numOfSockets;
    }

    public void setNumOfSockets(int value) {
        this.numOfSockets = value;
    }

    public int getCpuPerSocket() {
        return cpuPerSocket;
    }

    public void setCpuPerSocket(int value) {
        this.cpuPerSocket = value;
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
        return timeZone;
    }

    public void setTimeZone(String value) {
        timeZone = value;
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

    public String getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(String vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (autoStartup ? 1231 : 1237);
        result = prime * result + cpuPerSocket;
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((defaultBootSequence == null) ? 0 : defaultBootSequence.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + (failBack ? 1231 : 1237);
        result = prime * result + ((initrdUrl == null) ? 0 : initrdUrl.hashCode());
        result = prime * result + ((isoPath == null) ? 0 : isoPath.hashCode());
        result = prime * result + ((kernelParams == null) ? 0 : kernelParams.hashCode());
        result = prime * result + ((kernelUrl == null) ? 0 : kernelUrl.hashCode());
        result = prime * result + osId;
        result = prime * result + memSizeMb;
        result = prime * result + niceLevel;
        result = prime * result + numOfSockets;
        result = prime * result + numOfMonitors;
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + priority;
        result = prime * result + (stateless ? 1231 : 1237);
        result = prime * result + (smartcardEnabled ? 1231 : 1237);
        result = prime * result + ((timeZone == null) ? 0 : timeZone.hashCode());
        result = prime * result + ((usbPolicy == null) ? 0 : usbPolicy.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vmType == null) ? 0 : vmType.hashCode());
        result = prime * result + ((quotaId == null) ? 0 : quotaId.hashCode());
        result = prime * result + (allowConsoleReconnect ? 1231 : 1237);
        result = prime * result + ((dedicatedVmForVds == null) ? 0 : dedicatedVmForVds.hashCode());
        result = prime * result + ((migrationSupport == null) ? 0 : migrationSupport.hashCode());
        result = prime * result + ((tunnelMigration == null) ? 0 : tunnelMigration.hashCode());
        result = prime * result + ((vncKeyboardLayout == null) ? 0 : vncKeyboardLayout.hashCode());
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
        return (ObjectUtils.objectsEqual(id, other.id)
                && autoStartup == other.autoStartup
                && cpuPerSocket == other.cpuPerSocket
                && ObjectUtils.objectsEqual(creationDate, other.creationDate)
                && defaultBootSequence == other.defaultBootSequence
                && ObjectUtils.objectsEqual(description, other.description)
                && ObjectUtils.objectsEqual(domain, other.domain)
                && failBack == other.failBack
                && ObjectUtils.objectsEqual(initrdUrl, other.initrdUrl)
                && ObjectUtils.objectsEqual(isoPath, other.isoPath)
                && ObjectUtils.objectsEqual(kernelParams, other.kernelParams)
                && ObjectUtils.objectsEqual(kernelUrl, other.kernelUrl)
                && osId == other.osId
                && memSizeMb == other.memSizeMb
                && niceLevel == other.niceLevel
                && numOfSockets == other.numOfSockets
                && numOfMonitors == other.numOfMonitors
                && origin == other.origin
                && priority == other.priority
                && stateless == other.stateless
                && smartcardEnabled == other.smartcardEnabled
                && deleteProtected == other.deleteProtected
                && ObjectUtils.objectsEqual(timeZone, other.timeZone)
                && usbPolicy == other.usbPolicy
                && ObjectUtils.objectsEqual(vdsGroupId, other.vdsGroupId)
                && vmType == other.vmType
                && ObjectUtils.objectsEqual(quotaId, other.quotaId)
                && allowConsoleReconnect == other.allowConsoleReconnect
                && ObjectUtils.objectsEqual(dedicatedVmForVds, other.dedicatedVmForVds)
                && migrationSupport == other.migrationSupport
                && ObjectUtils.objectsEqual(tunnelMigration, other.tunnelMigration)
                && ObjectUtils.objectsEqual(vncKeyboardLayout, other.vncKeyboardLayout));
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
        return quotaDefault;
    }

    public void setQuotaDefault(boolean isQuotaDefault) {
        this.quotaDefault = isQuotaDefault;
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

    public Guid getDedicatedVmForVds() {
        return dedicatedVmForVds;
    }

    public void setDedicatedVmForVds(Guid value) {
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

    public Boolean getTunnelMigration() {
        return tunnelMigration;
    }

    public void setTunnelMigration(Boolean value) {
        tunnelMigration = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public int getMinAllocatedMem() {
        return minAllocatedMem;
    }

    public void setMinAllocatedMem(int value) {
        minAllocatedMem = value;
    }

    public boolean isRunAndPause() {
        return runAndPause;
    }

    public void setRunAndPause(boolean runAndPause) {
        this.runAndPause = runAndPause;
    }

}
