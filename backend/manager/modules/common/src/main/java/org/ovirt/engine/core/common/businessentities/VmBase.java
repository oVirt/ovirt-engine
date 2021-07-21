package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.IntegerContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.NullOrStringContainedInConfigValueList;
import org.ovirt.engine.core.common.validation.annotation.SizeFromConfigValue;
import org.ovirt.engine.core.common.validation.annotation.ValidDescription;
import org.ovirt.engine.core.common.validation.annotation.ValidI18NExtraName;
import org.ovirt.engine.core.common.validation.annotation.ValidSerialNumberPolicy;
import org.ovirt.engine.core.common.validation.annotation.ValidTimeZone;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.CreateVm;
import org.ovirt.engine.core.common.validation.group.ImportClonedEntity;
import org.ovirt.engine.core.common.validation.group.ImportEntity;
import org.ovirt.engine.core.common.validation.group.StartEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateVm;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

@ValidTimeZone(groups = {CreateEntity.class, UpdateEntity.class, ImportEntity.class, ImportClonedEntity.class, StartEntity.class})
@ValidSerialNumberPolicy(groups = {CreateEntity.class, UpdateEntity.class, ImportEntity.class, StartEntity.class})
public class VmBase implements Queryable, BusinessEntity<Guid>, Nameable, Commented, HasSerialNumberPolicy, HasMigrationOptions, Comparable<VmBase> {
    private static final long serialVersionUID = 1078548170257965614L;

    @EditableVmField(onHostedEngine = true)
    @EditableVmTemplateField
    private String name;

    @EditableVmField
    private ArrayList<DiskImage> images;

    @EditableVmField(onHostedEngine = true)
    private List<VmNetworkInterface> interfaces;


    @EditableVmField(onHostedEngine = true)
    private ArrayList<DiskImage> diskList;

    @TransientField
    private Map<Guid, VmDevice> managedDeviceMap;

    @TransientField
    private List<VmDevice> unmanagedDeviceList;

    private Guid id;

    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private Guid clusterId;

    @CopyOnNewVersion
    @EditableVmField
    private int osId;

    @CopyOnNewVersion
    @EditableVmField(onHostedEngine = true)
    private Guid smallIconId;

    @CopyOnNewVersion
    @EditableVmField(onHostedEngine = true)
    private Guid largeIconId;

    // This is not actually editable fields,
    // its a workaround that we enforce in RunVmCommand().validate() method due
    // to different formats in snapshot ovf and the database format.
    @EditableVmField
    private Date creationDate;

    @EditableVmField(onHostedEngine = true)
    @Size(max = BusinessEntitiesDefinitions.VM_DESCRIPTION_SIZE)
    @ValidDescription(message = "ACTION_TYPE_FAILED_DESCRIPTION_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class })
    @NotNull
    private String description;

    @EditableVmField(onHostedEngine = true)
    @NotNull
    private String comment;

    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            hotSettableOnStatus = VMStatus.Group.UP,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Min(1)
    private int memSizeMb;

    /**
     * Memory size up to which memory hotplug can be performed.
     * <p>Bounds {@link #memSizeMb} <= maxMemorySizeMb <=
     * {@link org.ovirt.engine.core.common.utils.VmCommonUtils#maxMemorySizeWithHotplugInMb(VM)}</p>
     */
    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Min(1)
    private int maxMemorySizeMb;

    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @SizeFromConfigValue(
            maxConfig = ConfigValues.MaxIoThreadsPerVm,
            min = 0,
            groups = { CreateEntity.class, UpdateEntity.class, CreateVm.class, UpdateVm.class },
            message = "ACTION_TYPE_FAILED_NUM_OF_IOTHREADS_INCORRECT"
    )
    private int numOfIoThreads;

    @EditableVmField(
            onHostedEngine = true,
            hotSettableOnStatus = VMStatus.Group.UP,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @CopyOnNewVersion
    private int numOfSockets;

    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private int cpuPerSocket;

    /**
     * Guest's threads per core. For virtual hyper threading tuning.
     * Useful for PPC tuning.
     * Reasonable value for x86 is 1.
     */
    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private int threadsPerCpu;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.VM_EMULATED_MACHINE_SIZE)
    @ValidI18NExtraName(message = "ACTION_TYPE_FAILED_EMULATED_MACHINE_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String customEmulatedMachine;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    // can be null only for Blank template and instance types
    private BiosType biosType;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.VM_CPU_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.CUSTOM_CPU_NAME,
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "ACTION_TYPE_FAILED_CPU_NAME_MAY_NOT_CONTAIN_SPECIAL_CHARS",
            groups = { CreateEntity.class, UpdateEntity.class })
    private String customCpuName; // overrides cluster cpu. (holds the actual vdsVerb)

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean useHostCpuFlags; // Pass-Through Host CPU

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @IntegerContainedInConfigValueList(configValue = ConfigValues.ValidNumOfMonitors,
            message = "VALIDATION_VM_NUM_OF_MONITORS_EXCEEDED")
    private int numOfMonitors;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_TIME_ZONE_SIZE)
    private String timeZone;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private VmType vmType;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private UsbPolicy usbPolicy;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private BootSequence defaultBootSequence;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    private int niceLevel;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private int cpuShares;

    @CopyOnNewVersion
    @EditableVmField
    private int priority;

    @CopyOnNewVersion
    @EditableVmField
    private boolean autoStartup;

    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean stateless;

    @CopyOnNewVersion
    @EditableVmField
    private boolean deleteProtected;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private SsoMethod ssoMethod;

    @EditableVmField(onHostedEngine = true)
    private long dbGeneration;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean smartcardEnabled;

    @CopyOnNewVersion
    @EditableVmField
    @Pattern(regexp = ValidationUtils.ISO_SUFFIX_PATTERN + "|" + ValidationUtils.GUID,
            flags = Pattern.Flag.CASE_INSENSITIVE, message = "ACTION_TYPE_FAILED_INVALID_CDROM_DISK_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String isoPath;

    private OriginType origin;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelUrl;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String kernelParams;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Pattern(regexp = ValidationUtils.NO_TRIMMING_WHITE_SPACES_PATTERN,
            message = "ACTION_TYPE_FAILED_LINUX_BOOT_PARAMS_MAY_NOT_CONTAIN_TRIMMING_WHITESPACES", groups = { CreateEntity.class,
                    UpdateEntity.class })
    private String initrdUrl;

    @CopyOnNewVersion
    @EditableVmField
    private boolean allowConsoleReconnect;

    /**
     * if this field is null then value should be taken from cluster
     */
    @CopyOnNewVersion
    @EditableVmField
    private Boolean tunnelMigration;

    /**
     * this field is used to save the ovf version,
     * in case the vm object was built from ovf.
     */
    @TransientField
    private String ovfVersion;

    @TransientField
    private Date exportDate;

    /**
     * The cluster version in which the VM configuration was created.
     * Used by snapshots. Stored in OVF as OvfProperties.CLUSTER_COMPATIBILITY_VERSION.
     *
     * The clusterCompatibilityVersionOrigin is not really editable but since it is saved on next-run
     * snapshot as well, it blocks VM updates.
     */
    @TransientField
    @EditableVmField
    private Version clusterCompatibilityVersionOrigin;

    /**
     * Maximum allowed downtime for live migration in milliseconds.
     * Value of null indicates that the {@link ConfigValues.DefaultMaximumMigrationDowntime} value will be used.
     *
     * Special value of 0 for migration downtime specifies that no value will be sent to VDSM and the default
     * VDSM behavior will be used.
     */
    @EditableVmField
    @Min(value = 0, message = "VALIDATION_VM_MIGRATION_DOWNTIME_RANGE")
    private Integer migrationDowntime;

    @EditableVmField(onStatuses = VMStatus.Down)
    private List<VmNumaNode> vNumaNodeList;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String userDefinedProperties;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @OvfExportOnlyField(exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String predefinedProperties;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private String customProperties;

    @CopyOnNewVersion
    @EditableVmField
    private ConsoleDisconnectAction consoleDisconnectAction;

    @CopyOnNewVersion
    @EditableVmField
    @EditableVmTemplateField
    private int consoleDisconnectActionDelay;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private Version customCompatibilityVersion;

    @CopyOnNewVersion
    @EditableVmField(
            hotSettableOnStatus = VMStatus.Group.RUNNING_OR_PAUSED,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private Guid leaseStorageDomainId;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private VmResumeBehavior resumeBehavior;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean multiQueuesEnabled;


    /**
     * Number of virtio-scsi multi-queues, possible values are:
     * -1 - virtio-scsi multi-queues will be automatically set
     * > 0 - number of queues
     */

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private int virtioScsiMultiQueues;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean balloonEnabled;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private CpuPinningPolicy cpuPinningPolicy;

    public VmBase() {
        name = "";
        interfaces = new ArrayList<>();
        images = new ArrayList<>();
        diskList = new ArrayList<>();
        managedDeviceMap = new HashMap<>();
        unmanagedDeviceList = new ArrayList<>();
        id = Guid.Empty;
        creationDate = new Date(0);
        numOfSockets = 1;
        cpuPerSocket = 1;
        threadsPerCpu = 1;
        numOfIoThreads = 1;
        usbPolicy = UsbPolicy.DISABLED;
        isoPath = "";
        defaultBootSequence = BootSequence.C;
        migrationSupport = MigrationSupport.MIGRATABLE;
        vmType = VmType.Server;
        defaultDisplayType = DisplayType.qxl;
        ssoMethod = SsoMethod.GUEST_AGENT;
        spiceFileTransferEnabled = true;
        spiceCopyPasteEnabled = true;
        vNumaNodeList = new ArrayList<>();
        customProperties = "";
        consoleDisconnectAction = ConsoleDisconnectAction.LOCK_SCREEN;
        consoleDisconnectActionDelay = 0;
        resumeBehavior = VmResumeBehavior.AUTO_RESUME;
        description = "";
        comment = "";
        multiQueuesEnabled = true;
        virtioScsiMultiQueues = 0;
        balloonEnabled = true;
        cpuPinningPolicy = CpuPinningPolicy.NONE;
    }

    @EditableVmField
    private Guid quotaId;


    /** Transient field for GUI presentation purposes. */
    @TransientField
    private String quotaName;

    @TransientField
    /** Transient field for GUI presentation purposes. */
    private boolean quotaDefault;

    /** Transient field for GUI presentation purposes. */
    @TransientField
    private QuotaEnforcementTypeEnum quotaEnforcementType;

    @CopyOnNewVersion
    @EditableVmField(onHostedEngine = true)
    @EditableVmTemplateField
    @OvfExportOnlyField(valueToIgnore = "MIGRATABLE", exportOption = ExportOption.EXPORT_NON_IGNORED_VALUES)
    private MigrationSupport migrationSupport;

    /**
     * Host with ID's contained in this list will be preferred by the scheduler on VM run attempts.
     * If none of them are available the VM may be run on a host not contained in this list.
     *
     * In the case of usage for direct host device passthrough, this list shall contain ID
     * of exactly one host and that one will be used for all hostdev passthrough purposes
     * - i.e. determining which host's devices are assigned to the VM and
     * more strictly restraining the set of hosts available for scheduling.
     */
    @CopyOnNewVersion
    @EditableVmField
    @EditableVmTemplateField
    private List<Guid> dedicatedVmForVdsList;

    @EditableVmField(
            onHostedEngine = true,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private DisplayType defaultDisplayType;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @NullOrStringContainedInConfigValueList(configValue = ConfigValues.VncKeyboardLayoutValidValues,
        groups = { CreateEntity.class, UpdateEntity.class },
        message = "VALIDATION_VM_INVALID_KEYBOARD_LAYOUT")
    private String vncKeyboardLayout;

    @CopyOnNewVersion
    @EditableVmField(
            onHostedEngine = true,
            hotSettableOnStatus = VMStatus.Group.RUNNING_OR_PAUSED,
            onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Min(0)
    private int minAllocatedMem;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean runAndPause;

    private Guid createdByUserId;

    @EditableVmField
    @CopyOnNewVersion
    private VmInit vmInit;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private SerialNumberPolicy serialNumberPolicy;

    /**
     * Serial number used when {@link serialNumberPolicy} is set to {@link SerialNumberPolicy.CUSTOM}
     */
    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    @Size(max = BusinessEntitiesDefinitions.VM_SERIAL_NUMBER_SIZE)
    private String customSerialNumber;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean bootMenuEnabled;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean spiceFileTransferEnabled;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean spiceCopyPasteEnabled;

    @CopyOnNewVersion
    @EditableVmField
    @EditableVmTemplateField
    private Guid cpuProfileId;

    @CopyOnNewVersion
    @EditableVmField
    private Boolean autoConverge;

    @CopyOnNewVersion
    @EditableVmField
    private Boolean migrateCompressed;

    @CopyOnNewVersion
    @EditableVmField(onHostedEngine = true)
    private Boolean migrateEncrypted;

    @CopyOnNewVersion
    @EditableVmField(onHostedEngine = true)
    private Guid migrationPolicyId;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @EditableVmTemplateField
    private boolean useTscFrequency;

    @CopyOnNewVersion
    @EditableVmField(onStatuses = VMStatus.Down)
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @EditableVmTemplateField
    private String cpuPinning;

    public VmBase(VmBase vmBase) {
        this(vmBase.getName(),
                vmBase.getId(),
                vmBase.getClusterId(),
                vmBase.getOsId(),
                vmBase.getCreationDate(),
                vmBase.getDescription(),
                vmBase.getComment(),
                vmBase.getMemSizeMb(),
                vmBase.getMaxMemorySizeMb(),
                vmBase.getNumOfSockets(),
                vmBase.getCpuPerSocket(),
                vmBase.getThreadsPerCpu(),
                vmBase.getNumOfMonitors(),
                vmBase.getTimeZone(),
                vmBase.getVmType(),
                vmBase.getUsbPolicy(),
                vmBase.getDefaultBootSequence(),
                vmBase.getNiceLevel(),
                vmBase.getCpuShares(),
                vmBase.getPriority(),
                vmBase.isAutoStartup(),
                vmBase.isStateless(),
                vmBase.getIsoPath(),
                vmBase.getOrigin(),
                vmBase.getKernelUrl(),
                vmBase.getKernelParams(),
                vmBase.getInitrdUrl(),
                vmBase.getQuotaId(),
                vmBase.isSmartcardEnabled(),
                vmBase.isDeleteProtected(),
                vmBase.getSsoMethod(),
                vmBase.getTunnelMigration(),
                vmBase.getVncKeyboardLayout(),
                vmBase.getMinAllocatedMem(),
                vmBase.isRunAndPause(),
                vmBase.getCreatedByUserId(),
                vmBase.getMigrationSupport(),
                vmBase.isAllowConsoleReconnect(),
                vmBase.getDedicatedVmForVdsList(),
                vmBase.getDefaultDisplayType(),
                vmBase.getMigrationDowntime(),
                vmBase.getVmInit(),
                vmBase.getSerialNumberPolicy(),
                vmBase.getCustomSerialNumber(),
                vmBase.isBootMenuEnabled(),
                vmBase.isSpiceFileTransferEnabled(),
                vmBase.isSpiceCopyPasteEnabled(),
                vmBase.getCpuProfileId(),
                vmBase.getAutoConverge(),
                vmBase.getMigrateCompressed(),
                vmBase.getMigrateEncrypted(),
                vmBase.getUserDefinedProperties(),
                vmBase.getPredefinedProperties(),
                vmBase.getCustomProperties(),
                vmBase.getCustomEmulatedMachine(),
                vmBase.getCustomCpuName(),
                vmBase.isUseHostCpuFlags(),
                vmBase.getSmallIconId(),
                vmBase.getLargeIconId(),
                vmBase.getNumOfIoThreads(),
                vmBase.getConsoleDisconnectAction(),
                vmBase.getConsoleDisconnectActionDelay(),
                vmBase.getCustomCompatibilityVersion(),
                vmBase.getMigrationPolicyId(),
                vmBase.getLeaseStorageDomainId(),
                vmBase.getResumeBehavior(),
                vmBase.isMultiQueuesEnabled(),
                vmBase.getUseTscFrequency(),
                vmBase.getCpuPinning(),
                vmBase.getVirtioScsiMultiQueues(),
                vmBase.isBalloonEnabled(),
                vmBase.getBiosType(),
                vmBase.getCpuPinningPolicy());
    }

    public VmBase(
            String name,
            Guid id,
            Guid clusterId,
            int osId,
            Date creationDate,
            String description,
            String comment,
            int memSizeMb,
            int maxMemorySizeMb,
            int numOfSockets,
            int cpusPerSocket,
            int threadsPerCpu,
            int numOfMonitors,
            String timezone,
            VmType vmType,
            UsbPolicy usbPolicy,
            BootSequence defaultBootSequence,
            int niceLevel,
            int cpuShares,
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
            SsoMethod ssoMethod,
            Boolean tunnelMigration,
            String vncKeyboardLayout,
            int minAllocatedMem,
            boolean runAndPause,
            Guid createdByUserId,
            MigrationSupport migrationSupport,
            boolean allowConsoleReconnect,
            List<Guid> dedicatedVmForVdsList,
            DisplayType defaultDisplayType,
            Integer migrationDowntime,
            VmInit vmInit,
            SerialNumberPolicy serialNumberPolicy,
            String customSerialNumber,
            boolean bootMenuEnabled,
            boolean spiceFileTransferEnabled,
            boolean spiceCopyPasteEnabled,
            Guid cpuProfileId,
            Boolean autoConverge,
            Boolean migrateCompressed,
            Boolean migrateEncrypted,
            String userDefinedProperties,
            String predefinedProperties,
            String customProperties,
            String customEmulatedMachine,
            String customCpuName,
            boolean useHostCpuFlags,
            Guid smallIconId,
            Guid largeIconId,
            int numOfIoThreads,
            ConsoleDisconnectAction consoleDisconnectAction,
            int consoleDisconnectActionDelay,
            Version customCompatibilityVersion,
            Guid migrationPolicyId,
            Guid leaseStorageDomainId,
            VmResumeBehavior resumeBehavior,
            boolean multiQueuesEnabled,
            boolean useTscFrequency,
            String cpuPinning,
            int virtioScsiMultiQueues,
            boolean balloonEnabled,
            BiosType biosType,
            CpuPinningPolicy cpuPinningPolicy) {
        this();
        this.name = name;
        this.id = id;
        this.clusterId = clusterId;
        this.osId = osId;
        this.creationDate = creationDate;
        this.description = description;
        this.comment = comment;
        this.memSizeMb = memSizeMb;
        this.maxMemorySizeMb = maxMemorySizeMb;
        this.numOfSockets = numOfSockets;
        this.cpuPerSocket = cpusPerSocket;
        this.threadsPerCpu = threadsPerCpu;
        this.numOfMonitors = numOfMonitors;
        this.timeZone = timezone;
        this.vmType = vmType;
        this.usbPolicy = usbPolicy;
        this.defaultBootSequence = defaultBootSequence;
        this.niceLevel = niceLevel;
        this.cpuShares = cpuShares;
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
        this.ssoMethod = ssoMethod;
        this.tunnelMigration = tunnelMigration;
        this.vncKeyboardLayout = vncKeyboardLayout;
        this.minAllocatedMem = minAllocatedMem;
        this.runAndPause = runAndPause;
        this.createdByUserId = createdByUserId;
        this.defaultDisplayType = defaultDisplayType;
        setQuotaId(quotaId);
        this.migrationSupport = migrationSupport;
        this.allowConsoleReconnect = allowConsoleReconnect;
        this.dedicatedVmForVdsList = dedicatedVmForVdsList;
        this.migrationDowntime = migrationDowntime;
        this.vmInit = vmInit != null ? new VmInit(vmInit) : null;
        this.serialNumberPolicy = serialNumberPolicy;
        this.customSerialNumber = customSerialNumber;
        this.bootMenuEnabled = bootMenuEnabled;
        this.spiceFileTransferEnabled = spiceFileTransferEnabled;
        this.spiceCopyPasteEnabled = spiceCopyPasteEnabled;
        this.cpuProfileId = cpuProfileId;
        this.autoConverge = autoConverge;
        this.migrateCompressed = migrateCompressed;
        this.migrateEncrypted = migrateEncrypted;
        this.userDefinedProperties = userDefinedProperties;
        this.predefinedProperties = predefinedProperties;
        this.customProperties = customProperties;
        this.customEmulatedMachine = customEmulatedMachine;
        this.customCpuName = customCpuName;
        this.useHostCpuFlags = useHostCpuFlags;
        this.smallIconId = smallIconId;
        this.largeIconId = largeIconId;
        this.numOfIoThreads = numOfIoThreads;
        this.consoleDisconnectAction = consoleDisconnectAction;
        this.consoleDisconnectActionDelay = consoleDisconnectActionDelay;
        this.customCompatibilityVersion = customCompatibilityVersion;
        this.migrationPolicyId = migrationPolicyId;
        this.leaseStorageDomainId = leaseStorageDomainId;
        this.resumeBehavior = resumeBehavior;
        this.multiQueuesEnabled = multiQueuesEnabled;
        this.virtioScsiMultiQueues = virtioScsiMultiQueues;
        this.useTscFrequency = useTscFrequency;
        this.cpuPinning = cpuPinning;
        this.balloonEnabled = balloonEnabled;
        this.biosType = biosType;
        this.cpuPinningPolicy = cpuPinningPolicy;
    }

    @Override
    public Object getQueryableId() {
        return getId();
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

    @JsonIgnore
    public ArrayList<DiskImage> getDiskList() {
        return diskList;
    }

    public void setDiskList(ArrayList<DiskImage> diskList) {
        this.diskList = diskList;
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

    public int getNumOfCpus(boolean countThreadsAsCPU) {
        return this.getCpuPerSocket() * this.getNumOfSockets()
                * (countThreadsAsCPU ? this.getThreadsPerCpu() : 1);
    }

    public int getNumOfCpus() {
        return getNumOfCpus(true);
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid value) {
        this.clusterId = value;
    }

    public int getOsId() {
        return osId;
    }

    public void setOsId(int value) {
        osId = value;
    }

    public Guid getSmallIconId() {
        return smallIconId;
    }

    public void setSmallIconId(Guid smallIconId) {
        this.smallIconId = smallIconId;
    }

    public Guid getLargeIconId() {
        return largeIconId;
    }

    public void setLargeIconId(Guid largeIconId) {
        this.largeIconId = largeIconId;
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
        this.description = value == null ? "" : value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value == null ? "" : value;
    }

    public int getMemSizeMb() {
        return memSizeMb;
    }

    public void setMemSizeMb(int value) {
        this.memSizeMb = value;
    }

    public int getMaxMemorySizeMb() {
        return maxMemorySizeMb;
    }

    public void setMaxMemorySizeMb(int maxMemorySizeMb) {
        this.maxMemorySizeMb = maxMemorySizeMb;
    }

    public int getNumOfIoThreads() {
        return numOfIoThreads;
    }

    public void setNumOfIoThreads(int numOfIoThreads) {
        this.numOfIoThreads = numOfIoThreads;
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

    public int getThreadsPerCpu() {
        return threadsPerCpu;
    }

    public void setThreadsPerCpu(int value) {
        this.threadsPerCpu = value;
    }

    public int getNumOfMonitors() {
        return numOfMonitors;
    }

    public void setNumOfMonitors(int value) {
        numOfMonitors = value;
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

    public Version getClusterCompatibilityVersionOrigin() {
        return this.clusterCompatibilityVersionOrigin;
    }

    public void setClusterCompatibilityVersionOrigin(Version value) {
        this.clusterCompatibilityVersionOrigin = value;
    }

    public boolean isMultiQueuesEnabled() {
        return multiQueuesEnabled;
    }

    public int getVirtioScsiMultiQueues() {
        return virtioScsiMultiQueues;
    }

    public void setMultiQueuesEnabled(boolean multiQueuesEnabled) {
        this.multiQueuesEnabled = multiQueuesEnabled;
    }

    public String getCpuPinning() {
        return cpuPinning;
    }

    public void setCpuPinning(String cpuPinning) {
        this.cpuPinning = cpuPinning;
    }

    public void setVirtioScsiMultiQueues(int virtioScsiMultiQueues) {
        this.virtioScsiMultiQueues = virtioScsiMultiQueues;
    }

    public boolean isBalloonEnabled() {
        return balloonEnabled;
    }

    public void setBalloonEnabled(boolean balloonEnabled) {
        this.balloonEnabled = balloonEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                autoStartup,
                cpuPerSocket,
                threadsPerCpu,
                creationDate,
                defaultBootSequence,
                description,
                initrdUrl,
                isoPath,
                kernelParams,
                kernelUrl,
                osId,
                memSizeMb,
                maxMemorySizeMb,
                niceLevel,
                cpuShares,
                numOfSockets,
                numOfMonitors,
                origin,
                priority,
                stateless,
                smartcardEnabled,
                timeZone,
                usbPolicy,
                clusterId,
                vmType,
                quotaId,
                allowConsoleReconnect,
                dedicatedVmForVdsList,
                migrationSupport,
                tunnelMigration,
                vncKeyboardLayout,
                createdByUserId,
                defaultDisplayType,
                migrationDowntime,
                serialNumberPolicy,
                customSerialNumber,
                bootMenuEnabled,
                spiceFileTransferEnabled,
                spiceCopyPasteEnabled,
                cpuProfileId,
                vNumaNodeList,
                autoConverge,
                migrateCompressed,
                migrateEncrypted,
                predefinedProperties,
                userDefinedProperties,
                customEmulatedMachine,
                customCpuName,
                useHostCpuFlags,
                smallIconId,
                largeIconId,
                consoleDisconnectAction,
                consoleDisconnectActionDelay,
                customCompatibilityVersion,
                resumeBehavior,
                multiQueuesEnabled,
                useTscFrequency,
                cpuPinning,
                virtioScsiMultiQueues,
                balloonEnabled,
                cpuPinningPolicy
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmBase)) {
            return false;
        }
        VmBase other = (VmBase) obj;
        return Objects.equals(id, other.id)
                && autoStartup == other.autoStartup
                && cpuPerSocket == other.cpuPerSocket
                && Objects.equals(creationDate, other.creationDate)
                && defaultBootSequence == other.defaultBootSequence
                && Objects.equals(description, other.description)
                && Objects.equals(initrdUrl, other.initrdUrl)
                && Objects.equals(isoPath, other.isoPath)
                && Objects.equals(kernelParams, other.kernelParams)
                && Objects.equals(kernelUrl, other.kernelUrl)
                && osId == other.osId
                && memSizeMb == other.memSizeMb
                && maxMemorySizeMb == other.maxMemorySizeMb
                && niceLevel == other.niceLevel
                && numOfSockets == other.numOfSockets
                && threadsPerCpu == other.threadsPerCpu
                && numOfMonitors == other.numOfMonitors
                && origin == other.origin
                && priority == other.priority
                && stateless == other.stateless
                && smartcardEnabled == other.smartcardEnabled
                && deleteProtected == other.deleteProtected
                && Objects.equals(timeZone, other.timeZone)
                && usbPolicy == other.usbPolicy
                && Objects.equals(clusterId, other.clusterId)
                && vmType == other.vmType
                && Objects.equals(quotaId, other.quotaId)
                && allowConsoleReconnect == other.allowConsoleReconnect
                && Objects.equals(dedicatedVmForVdsList, other.dedicatedVmForVdsList)
                && migrationSupport == other.migrationSupport
                && Objects.equals(tunnelMigration, other.tunnelMigration)
                && Objects.equals(vncKeyboardLayout, other.vncKeyboardLayout)
                && Objects.equals(createdByUserId, other.createdByUserId)
                && cpuShares == other.cpuShares
                && Objects.equals(migrationDowntime, other.migrationDowntime)
                && serialNumberPolicy == other.serialNumberPolicy
                && Objects.equals(customSerialNumber, other.customSerialNumber)
                && bootMenuEnabled == other.bootMenuEnabled
                && spiceFileTransferEnabled == other.spiceFileTransferEnabled
                && spiceCopyPasteEnabled == other.spiceCopyPasteEnabled
                && consoleDisconnectActionDelay == other.consoleDisconnectActionDelay
                && Objects.equals(cpuProfileId, other.cpuProfileId)
                && Objects.equals(vNumaNodeList, other.vNumaNodeList)
                && Objects.equals(autoConverge, other.autoConverge)
                && Objects.equals(migrateCompressed, other.migrateCompressed)
                && Objects.equals(migrateEncrypted, other.migrateEncrypted)
                && Objects.equals(predefinedProperties, other.predefinedProperties)
                && Objects.equals(userDefinedProperties, other.userDefinedProperties)
                && Objects.equals(customEmulatedMachine, other.customEmulatedMachine)
                && Objects.equals(customCpuName, other.customCpuName)
                && Objects.equals(useHostCpuFlags, other.useHostCpuFlags)
                && Objects.equals(smallIconId, other.smallIconId)
                && Objects.equals(largeIconId, other.largeIconId)
                && Objects.equals(consoleDisconnectAction, other.consoleDisconnectAction)
                && Objects.equals(resumeBehavior, other.resumeBehavior)
                && Objects.equals(customCompatibilityVersion, other.customCompatibilityVersion)
                && Objects.equals(multiQueuesEnabled, other.multiQueuesEnabled)
                && Objects.equals(useTscFrequency, other.useTscFrequency)
                && Objects.equals(cpuPinning, other.cpuPinning)
                && virtioScsiMultiQueues == other.virtioScsiMultiQueues
                && Objects.equals(balloonEnabled, other.balloonEnabled)
                && cpuPinningPolicy == other.cpuPinningPolicy;
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

    public List<Guid> getDedicatedVmForVdsList() {
        if (dedicatedVmForVdsList == null){
            dedicatedVmForVdsList = new LinkedList<>();
        }
        return dedicatedVmForVdsList;
    }

    @JsonIgnore
    public void setDedicatedVmForVdsList(List<Guid> value) {
        dedicatedVmForVdsList = value;
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

    public ConsoleDisconnectAction getConsoleDisconnectAction() {
        return consoleDisconnectAction;
    }

    public void setConsoleDisconnectAction(ConsoleDisconnectAction consoleDisconnectAction) {
        this.consoleDisconnectAction = consoleDisconnectAction;
    }

    public int getConsoleDisconnectActionDelay() {
        return consoleDisconnectActionDelay;
    }

    public void setConsoleDisconnectActionDelay(int consoleDisconnectActionDelay) {
        this.consoleDisconnectActionDelay = consoleDisconnectActionDelay;
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

    public Guid getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Guid createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public int getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(int cpuShares) {
        this.cpuShares = cpuShares;
    }

    public SsoMethod getSsoMethod() {
        return ssoMethod;
    }

    public void setSsoMethod(SsoMethod ssoMethod) {
        this.ssoMethod = ssoMethod;
    }

    public void setMigrationDowntime(Integer migrationDowntime) {
        this.migrationDowntime = migrationDowntime;
    }

    public Integer getMigrationDowntime() {
        return this.migrationDowntime;
    }

    public VmInit getVmInit() {
        if (vmInit != null) {
            vmInit.setId(id);
        }
        return vmInit;
    }

    public void setVmInit(VmInit vmInit) {
        this.vmInit = vmInit;
    }

    public SerialNumberPolicy getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    public void setSerialNumberPolicy(SerialNumberPolicy serialNumberPolicy) {
        this.serialNumberPolicy = serialNumberPolicy;
    }

    public String getCustomSerialNumber() {
        return customSerialNumber;
    }

    public void setCustomSerialNumber(String customSerialNumber) {
        if (serialNumberPolicy == SerialNumberPolicy.CUSTOM) {
            this.customSerialNumber = customSerialNumber;
        } else {
            this.customSerialNumber = null;
        }

    }

    public boolean isBootMenuEnabled() {
        return bootMenuEnabled;
    }

    public void setBootMenuEnabled(boolean bootMenuEnabled) {
        this.bootMenuEnabled = bootMenuEnabled;
    }

    public boolean isSpiceFileTransferEnabled() {
        return spiceFileTransferEnabled;
    }

    public void setSpiceFileTransferEnabled(boolean spiceFileTransferEnabled) {
        this.spiceFileTransferEnabled = spiceFileTransferEnabled;
    }

    public boolean isSpiceCopyPasteEnabled() {
        return spiceCopyPasteEnabled;
    }

    public void setSpiceCopyPasteEnabled(boolean spiceCopyPasteEnabled) {
        this.spiceCopyPasteEnabled = spiceCopyPasteEnabled;
    }

    public Guid getCpuProfileId() {
        return cpuProfileId;
    }

    public void setCpuProfileId(Guid cpuProfileId) {
        this.cpuProfileId = cpuProfileId;
    }

    public List<VmNumaNode> getvNumaNodeList() {
        return vNumaNodeList;
    }

    public void setvNumaNodeList(List<VmNumaNode> vNumaNodeList) {
        if (vNumaNodeList != null){
            this.vNumaNodeList = vNumaNodeList;
        } else {
            this.vNumaNodeList = new ArrayList<>();
        }
    }

    public Boolean getAutoConverge() {
        return autoConverge;
    }

    public void setAutoConverge(Boolean autoConverge) {
        this.autoConverge = autoConverge;
    }

    public Boolean getMigrateCompressed() {
        return migrateCompressed;
    }

    public void setMigrateCompressed(Boolean migrateCompressed) {
        this.migrateCompressed = migrateCompressed;
    }

    @Override
    public Boolean getMigrateEncrypted() {
        return migrateEncrypted;
    }

    @Override
    public void setMigrateEncrypted(Boolean value) {
        this.migrateEncrypted = value;
    }

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

    public String getCustomEmulatedMachine() {
        return customEmulatedMachine;
    }

    public void setCustomEmulatedMachine(String customEmulatedMachine) {
        this.customEmulatedMachine = customEmulatedMachine == null || customEmulatedMachine.trim().isEmpty() ? null : customEmulatedMachine;
    }

    public BiosType getBiosType() {
        return biosType;
    }

    public void setBiosType(BiosType biosType) {
        this.biosType = biosType;
    }

    public String getCustomCpuName() {
        return customCpuName;
    }

    public void setCustomCpuName(String customCpuName) {
        this.customCpuName = customCpuName==null || customCpuName.trim().isEmpty() ? null : customCpuName;
    }

    public boolean isUseHostCpuFlags() {
        return useHostCpuFlags;
    }

    public void setUseHostCpuFlags(boolean useHostCpuFlags) {
        this.useHostCpuFlags = useHostCpuFlags;
    }


    public void setCustomCompatibilityVersion(Version customCompatibilityVersion) {
        this.customCompatibilityVersion = customCompatibilityVersion;
    }

    public Version getCustomCompatibilityVersion() {
        return customCompatibilityVersion;
    }

    public boolean isManagedHostedEngine() {
        return OriginType.MANAGED_HOSTED_ENGINE == getOrigin();
    }

    public boolean isHostedEngine() {
        return OriginType.HOSTED_ENGINE == getOrigin() || OriginType.MANAGED_HOSTED_ENGINE == getOrigin();
    }

    public Guid getMigrationPolicyId() {
        return migrationPolicyId;
    }

    public void setMigrationPolicyId(Guid migrationPolicyId) {
        this.migrationPolicyId = migrationPolicyId;
    }

    public Guid getLeaseStorageDomainId() {
        return leaseStorageDomainId;
    }

    public void setLeaseStorageDomainId(Guid leaseStorageDomainId) {
        this.leaseStorageDomainId = leaseStorageDomainId;
    }

    @Override
    public int compareTo(VmBase other) {
        return id.compareTo(other.id);
    }

    public VmResumeBehavior getResumeBehavior() {
        return resumeBehavior;
    }

    public void setResumeBehavior(VmResumeBehavior resumeBehavior) {
        this.resumeBehavior = resumeBehavior;
    }

    public boolean getUseTscFrequency() {
        return useTscFrequency;
    }

    public void setUseTscFrequency(boolean useTscFrequency) {
        this.useTscFrequency = useTscFrequency;
    }

    public void setCpuPinningPolicy(CpuPinningPolicy cpuPinningPolicy) {
        this.cpuPinningPolicy = cpuPinningPolicy;
    }

    public CpuPinningPolicy getCpuPinningPolicy() {
        return cpuPinningPolicy;
    }
}
