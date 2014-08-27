package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.model.ArchitectureCapabilities;
import org.ovirt.engine.api.model.ArchitectureCapability;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootDevices;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.BootProtocols;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.CPUs;
import org.ovirt.engine.api.model.Capabilities;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.ConfigurationTypes;
import org.ovirt.engine.api.model.ContentTypes;
import org.ovirt.engine.api.model.CpuMode;
import org.ovirt.engine.api.model.CpuModes;
import org.ovirt.engine.api.model.CreationStates;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.DataCenterStates;
import org.ovirt.engine.api.model.DataCenterStatus;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskFormats;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskInterfaces;
import org.ovirt.engine.api.model.DiskStates;
import org.ovirt.engine.api.model.DiskStatus;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.DisplayTypes;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.FenceTypes;
import org.ovirt.engine.api.model.GlusterState;
import org.ovirt.engine.api.model.GlusterStates;
import org.ovirt.engine.api.model.GlusterVolumeType;
import org.ovirt.engine.api.model.GlusterVolumeTypes;
import org.ovirt.engine.api.model.HookContentType;
import org.ovirt.engine.api.model.HookStage;
import org.ovirt.engine.api.model.HookStates;
import org.ovirt.engine.api.model.HookStatus;
import org.ovirt.engine.api.model.HostNICStates;
import org.ovirt.engine.api.model.HostNonOperationalDetails;
import org.ovirt.engine.api.model.HostStates;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.InheritableBooleans;
import org.ovirt.engine.api.model.IpVersions;
import org.ovirt.engine.api.model.KdumpStates;
import org.ovirt.engine.api.model.KdumpStatus;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.NetworkStates;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.NfsVersion;
import org.ovirt.engine.api.model.NfsVersions;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.NicInterfaces;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.OsTypeUtils;
import org.ovirt.engine.api.model.OsTypes;
import org.ovirt.engine.api.model.PayloadEncoding;
import org.ovirt.engine.api.model.PayloadEncodings;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.PmProxyType;
import org.ovirt.engine.api.model.PmProxyTypes;
import org.ovirt.engine.api.model.PolicyUnitType;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStates;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.PowerManagers;
import org.ovirt.engine.api.model.QosTypes;
import org.ovirt.engine.api.model.ReportedDeviceType;
import org.ovirt.engine.api.model.ReportedDeviceTypes;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.RngSources;
import org.ovirt.engine.api.model.SELinuxMode;
import org.ovirt.engine.api.model.SELinuxModes;
import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.api.model.SchedulingPolicyUnitTypes;
import org.ovirt.engine.api.model.ScsiGenericIO;
import org.ovirt.engine.api.model.ScsiGenericIoOptions;
import org.ovirt.engine.api.model.SerialNumberPolicies;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.model.SnapshotStatus;
import org.ovirt.engine.api.model.SnapshotStatuses;
import org.ovirt.engine.api.model.SpmState;
import org.ovirt.engine.api.model.SpmStates;
import org.ovirt.engine.api.model.Stages;
import org.ovirt.engine.api.model.StepEnum;
import org.ovirt.engine.api.model.StepTypes;
import org.ovirt.engine.api.model.StorageDomainStates;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageDomainTypes;
import org.ovirt.engine.api.model.StorageFormats;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.StorageTypes;
import org.ovirt.engine.api.model.TemplateStates;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.TransportType;
import org.ovirt.engine.api.model.TransportTypes;
import org.ovirt.engine.api.model.Usages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.api.model.VmAffinities;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmDeviceType;
import org.ovirt.engine.api.model.VmDeviceTypes;
import org.ovirt.engine.api.model.VmPauseDetails;
import org.ovirt.engine.api.model.VmStates;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.model.VmTypes;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogActions;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.model.WatchdogModels;
import org.ovirt.engine.api.resource.CapabilitiesResource;
import org.ovirt.engine.api.resource.CapabiliyResource;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.restapi.resource.utils.FeaturesHelper;
import org.ovirt.engine.api.restapi.types.CPUMapper;
import org.ovirt.engine.api.restapi.types.IpVersion;
import org.ovirt.engine.api.restapi.types.NetworkUsage;
import org.ovirt.engine.api.restapi.util.FenceOptionsParser;
import org.ovirt.engine.api.restapi.util.VersionHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.VersionUtils;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendCapabilitiesResource extends BackendResource implements CapabilitiesResource {

    private final FeaturesHelper featuresHelper = new FeaturesHelper();

    public static final Version VERSION_3_0 = new Version() {
        {
            major = 3;
            minor = 0;
        }
    };
    public static final Version VERSION_3_1 = new Version() {
        {
            major = 3;
            minor = 1;
        }
    };
    public static final Version VERSION_3_2 = new Version() {
        {
            major = 3;
            minor = 2;
        }
    };
    public static final Version VERSION_3_3 = new Version() {
        {
            major = 3;
            minor = 3;
        }
    };
    public static final Version VERSION_3_4 = new Version() {
        {
            major = 3;
            minor = 4;
        }
    };
    public static final Version VERSION_3_5 = new Version() {
        {
            major = 3;
            minor = 5;
        }
    };
    public static final Version VERSION_3_6 = new Version() {
        {
            major = 3;
            minor = 6;
        }
    };
    private static Version currentVersion = null;

    @Override
    public Capabilities list() {
        Capabilities caps = new Capabilities();
        for (Version v : getSupportedClusterLevels()) {
            caps.getVersions().add(generateVersionCaps(v));
        }

        caps.setPermits(getPermits());
        caps.setSchedulingPolicies(getSchedulingPolicies());
        return caps;
    }

    public VersionCaps generateVersionCaps(Version v) {
        VersionCaps current = null;
        VersionCaps version = new VersionCaps();

        version.setMajor(v.getMajor());
        version.setMinor(v.getMinor());
        version.setId(generateId(v));

        // Not exposing CPU list and power managers on filtered queries
        if (!isFiltered()) {
            version.setCpus(new CPUs());
            for (ServerCpu sc : getServerCpuList(v)) {
                CPU cpu = new CPU();
                cpu.setId(sc.getCpuName());
                cpu.setLevel(sc.getLevel());
                cpu.setArchitecture(CPUMapper.map(sc.getArchitecture(), null));
                version.getCpus().getCPUs().add(cpu);
            }
            addPowerManagers(version, getPowerManagers(v));
        }

        addVmTypes(version, VmType.values());
        addAuthenticationMethods(version, AuthenticationMethod.values());
        addStorageTypes(version, getStorageTypes(v));
        addStorageDomainTypes(version, StorageDomainType.values());
        addFenceTypes(version, FenceType.values());
        addBootDevices(version, BootDevice.values());
        addDisplayTypes(version, DisplayType.values());
        addNicInterfaces(version, NicInterface.values());
        addDiskFormats(version, DiskFormat.values());
        addDiskInterfaces(version, DiskInterface.values());
        addCustomProperties(version, getVmHooksEnvs(v));
        addVmAffinities(version, VmAffinity.values());
        addVmDeviceType(version, VmDeviceType.values());
        addnetworkBootProtocols(version, BootProtocol.values());
        addMigrateOnErrorOptions(version, MigrateOnError.values());
        addStorageFormatOptions(version, StorageFormat.values());
        addOsTypes(version);
        addNfsVersions(version, NfsVersion.values());
        addKdumpStates(version, KdumpStatus.values());
        addSupportedQosTypes(version);

        addGlusterTypesAndStates(version);

        // Add States. User can't update States, but he still needs to know which exist.
        addCreationStates(version, CreationStatus.values());
        addStorageDomaintStates(version, StorageDomainStatus.values());
        addPowerManagementStateses(version, PowerManagementStatus.values());
        addHostStates(version, HostStatus.values());
        addHostNonOperationalDetails(version, NonOperationalReason.values());
        addNetworkStates(version, NetworkStatus.values());
        addTemplateStates(version, TemplateStatus.values());
        addVmStates(version, VmStatus.values());
        addVmPauseDetails(version, VmPauseStatus.values());
        addDiskStates(version, DiskStatus.values());
        addHostNICStates(version, NicStatus.values());
        addDataCenterStates(version, DataCenterStatus.values());
        addPermits(version, PermitType.values());
        addSchedulingPolicies(version, SchedulingPolicyType.values());
        addNetworkUsages(version, NetworkUsage.values());
        addPmProxyTypes(version, PmProxyType.values());
        addReportedDeviceTypes(version, ReportedDeviceType.values());
        addIpVersions(version, IpVersion.values());
        addCpuModes(version, CpuMode.values());
        addScsiGenericIoOptions(version, ScsiGenericIO.values());
        addWatchdogActions(version, WatchdogAction.values());
        addWatchdogModels(version, WatchdogModel.values());
        addConfigurationTypes(version, ConfigurationType.values());
        addSnapshotStatuses(version, SnapshotStatus.values());
        addPayloadEncodings(version, PayloadEncoding.values());
        addArchitectureCapabilities(version);
        addSerialNumberPolicies(version, SerialNumberPolicy.values());
        addSELinuxModes(version, SELinuxMode.values());
        addRngSources(version, RngSource.values());
        addPolicyUnitTypes(version, PolicyUnitType.values());
        addSpmStates(version, SpmState.values());
        // External tasks types
        addStepEnumTypes(version, StepEnum.values());
        addInheritableBooleans(version, InheritableBoolean.values());

        version.setFeatures(featuresHelper.getFeatures(v));

        if (current == null && VersionHelper.equals(v, getCurrentVersion())) {
            current = version;
            current.setCurrent(true);
        } else {
            version.setCurrent(false);
        }

        LinkHelper.<VersionCaps> addLinks(getUriInfo(), version);

        return version;
    }

    private void addArchitectureCapabilities(VersionCaps version) {
        org.ovirt.engine.core.compat.Version backendVersion =
                new org.ovirt.engine.core.compat.Version(version.getMajor(), version.getMinor());

        version.setArchitectureCapabilities(new ArchitectureCapabilities());

        ArchitectureCapability migrationFeature = new ArchitectureCapability();

        migrationFeature.setName("migration");

        for (ArchitectureType arch : ArchitectureType.values()) {
            if (FeatureSupported.isMigrationSupported(arch, backendVersion)) {
                migrationFeature.getArchitectures().add(arch.name());
            }
        }

        version.getArchitectureCapabilities().getCapabilities().add(migrationFeature);

        ArchitectureCapability memorySnapshotFeature = new ArchitectureCapability();

        memorySnapshotFeature.setName("memory snapshot");

        for (ArchitectureType arch : ArchitectureType.values()) {
            if (FeatureSupported.isMemorySnapshotSupportedByArchitecture(arch, backendVersion)) {
                memorySnapshotFeature.getArchitectures().add(arch.name());
            }
        }

        version.getArchitectureCapabilities().getCapabilities().add(memorySnapshotFeature);

        ArchitectureCapability suspendFeature = new ArchitectureCapability();

        suspendFeature.setName("suspend");

        for (ArchitectureType arch : ArchitectureType.values()) {
            if (FeatureSupported.isSuspendSupportedByArchitecture(arch, backendVersion)) {
                suspendFeature.getArchitectures().add(arch.name());
            }
        }

        version.getArchitectureCapabilities().getCapabilities().add(suspendFeature);
    }

    private void addSnapshotStatuses(VersionCaps version, SnapshotStatus[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_2)) {
            version.setSnapshotStatuses(new SnapshotStatuses());
            for (SnapshotStatus mode : values) {
                version.getSnapshotStatuses().getSnapshotStatuses().add(mode.value());
            }
        }
    }

    private void addPayloadEncodings(VersionCaps version, PayloadEncoding[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setPayloadEncodings(new PayloadEncodings());
            for (PayloadEncoding mode : values) {
                version.getPayloadEncodings().getPayloadEncodings().add(mode.value());
            }
        }
    }

    private void addCpuModes(VersionCaps version, CpuMode[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_2)) {
            version.setCpuModes(new CpuModes());
            for (CpuMode mode : values) {
                version.getCpuModes().getCpuModes().add(mode.value());
            }
        }
    }

    private void addScsiGenericIoOptions(VersionCaps version, ScsiGenericIO[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setSgioOptions(new ScsiGenericIoOptions());
            for (ScsiGenericIO mode : values) {
                version.getSgioOptions().getScsiGenericIoOptions().add(mode.value());
            }
        }
    }

    private void addWatchdogModels(VersionCaps version, WatchdogModel[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setWatchdogModels(new WatchdogModels());
            for (WatchdogModel watchdogModel : values) {
                version.getWatchdogModels().getWatchdogModels().add(watchdogModel.value());
            }
        }
    }

    private void addWatchdogActions(VersionCaps version, WatchdogAction[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setWatchdogActions(new WatchdogActions());
            for (WatchdogAction watchdogAction : values) {
                version.getWatchdogActions().getWatchdogActions().add(watchdogAction.value());
            }
        }
    }

    private void addReportedDeviceTypes(VersionCaps version, ReportedDeviceType[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_2)) {
            version.setReportedDeviceTypes(new ReportedDeviceTypes());
            for (ReportedDeviceType reportedDeviceType : values) {
                version.getReportedDeviceTypes().getReportedDeviceTypes().add(reportedDeviceType.value());
            }
        }
    }

    private void addIpVersions(VersionCaps version, IpVersion[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_2)) {
            version.setIpVersions(new IpVersions());
            for (IpVersion ipVersion : values) {
                version.getIpVersions().getIpVersions().add(ipVersion.value());
            }
        }
    }

    public String generateId(Version v) {
        Guid guid = asGuid((v.getMajor() + "." + v.getMinor()).getBytes(), true);
        return guid.toString();
    }

    private void addNetworkUsages(VersionCaps version, NetworkUsage[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_1)) {
            version.setUsages(new Usages());
            for (NetworkUsage usage : values) {
                version.getUsages().getUsages().add(usage.value());
            }
        }
    }

    private void addSchedulingPolicies(VersionCaps version, SchedulingPolicyType[] values) {
        version.setSchedulingPolicies(new SchedulingPolicies());
        for (SchedulingPolicyType policy : values) {
            version.getSchedulingPolicies().getPolicy().add(policy.value());
        }
    }

    private void addPermits(VersionCaps version, PermitType[] values) {
        version.setPermits(new Permits());
        for (PermitType permit : values) {
            version.getPermits().getPermits().add(map(permit));
        }
    }

    private void addGlusterTypesAndStates(VersionCaps version) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_1)) {
            addGlusterVolumeTypes(version, GlusterVolumeType.values());
            addTransportTypes(version, TransportType.values());
            addGlusterVolumeStates(version, GlusterState.values());
            addGlusterBrickStates(version, GlusterState.values());
        }
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            addGlusterHookContentTypes(version, HookContentType.values());
            addStages(version, HookStage.values());
            addGlusterHookStates(version, HookStatus.values());
        }
    }

    private Version getCurrentVersion() {
        if (currentVersion == null) {
            currentVersion = VersionHelper.parseVersion(getConfigurationValueDefault(String.class,
                    ConfigurationValues.VdcVersion));
        }
        return currentVersion;
    }

    private void addOsTypes(VersionCaps version) {
        version.setOsTypes(new OsTypes());
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.getOsTypes().getOsTypes().addAll(OsTypeUtils.getAllValues());
        } else {
            for (OsType type : OsType.values()) {
                version.getOsTypes().getOsTypes().add(type.name());
            }
        }
    }

    private void addNfsVersions(VersionCaps version, NfsVersion[] nfsVersions) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_1)) {
            version.setNfsVersions(new NfsVersions());
            for (NfsVersion nfsVersion : nfsVersions) {
                version.getNfsVersions().getNfsVersions().add(nfsVersion.value());
            }
        }
    }

    private void addnetworkBootProtocols(VersionCaps version, BootProtocol[] values) {
        version.setBootProtocols(new BootProtocols());
        for (BootProtocol bootProtocol : values) {
            version.getBootProtocols().getBootProtocols().add(bootProtocol.value());
        }
    }

    private void addVmAffinities(VersionCaps version, VmAffinity[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_0)) {
            version.setVmAffinities(new VmAffinities());
            for (VmAffinity affinity : values) {
                version.getVmAffinities().getVmAffinities().add(affinity.value());
            }
        }
    }

    private void addVmDeviceType(VersionCaps version, VmDeviceType[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_1)) {
            version.setVmDeviceTypes(new VmDeviceTypes());
            for (VmDeviceType type : values) {
                version.getVmDeviceTypes().getVmDeviceTypes().add(type.value());
            }
        }
    }

    private void addPowerManagers(VersionCaps version, List<PowerManagement> powerManagers) {
        version.setPowerManagers(new PowerManagers());
        version.getPowerManagers().getPowerManagers().addAll(powerManagers);
    }

    private void addVmTypes(VersionCaps version, VmType... types) {
        version.setVmTypes(new VmTypes());
        for (VmType type : types) {
            version.getVmTypes().getVmTypes().add(type.value());
        }
    }

    private void addStorageTypes(VersionCaps version, List<StorageType> types) {
        version.setStorageTypes(new StorageTypes());
        for (StorageType type : types) {
            version.getStorageTypes().getStorageTypes().add(type.value());
        }
    }

    private void addConfigurationTypes(VersionCaps version, ConfigurationType[] types) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setConfigurationTypes(new ConfigurationTypes());
            for (ConfigurationType type : types) {
                version.getConfigurationTypes().getConfigurationTypes().add(type.value());
            }
        }
    }

    private void addStorageDomainTypes(VersionCaps version, StorageDomainType... types) {
        version.setStorageDomainTypes(new StorageDomainTypes());
        for (StorageDomainType type : types) {
            version.getStorageDomainTypes().getStorageDomainTypes().add(type.value());
        }
    }

    private void addFenceTypes(VersionCaps version, FenceType... types) {
        version.setFenceTypes(new FenceTypes());
        for (FenceType type : types) {
            version.getFenceTypes().getFenceTypes().add(type.value());
        }
    }

    private void addBootDevices(VersionCaps version, BootDevice... devs) {
        version.setBootDevices(new BootDevices());
        for (BootDevice dev : devs) {
            version.getBootDevices().getBootDevices().add(dev.value());
        }
    }

    private void addDisplayTypes(VersionCaps version, DisplayType... types) {
        version.setDisplayTypes(new DisplayTypes());
        for (DisplayType type : types) {
            version.getDisplayTypes().getDisplayTypes().add(type.value());
        }
    }

    private void addNicInterfaces(VersionCaps version, NicInterface... types) {
        version.setNicInterfaces(new NicInterfaces());
        for (NicInterface type : types) {
            version.getNicInterfaces().getNicInterfaces().add(type.value());
        }
    }

    private void addDiskFormats(VersionCaps version, DiskFormat... types) {
        version.setDiskFormats(new DiskFormats());
        for (DiskFormat type : types) {
            version.getDiskFormats().getDiskFormats().add(type.value());
        }
    }

    private void addDiskInterfaces(VersionCaps version, DiskInterface... interfaces) {
        version.setDiskInterfaces(new DiskInterfaces());
        for (DiskInterface iface : interfaces) {
            version.getDiskInterfaces().getDiskInterfaces().add(iface.value());
        }
    }

    private void addCustomProperties(VersionCaps version, List<CustomProperty> envs) {
        version.setCustomProperties(new CustomProperties());
        version.getCustomProperties().getCustomProperty().addAll(envs);
    }

    private List<PowerManagement> getPowerManagers(Version version) {
        return FenceOptionsParser.parse(getFenceConfigurationValue(String.class,
                ConfigurationValues.VdsFenceOptionMapping,
                version),
                getConfigurationValue(String.class, ConfigurationValues.VdsFenceOptionTypes, version),
                true);
    }

    private List<StorageType> getStorageTypes(Version version) {
        List<StorageType> ret = new ArrayList<StorageType>();
        ret.add(StorageType.ISCSI);
        ret.add(StorageType.FCP);
        ret.add(StorageType.NFS);

        if (VersionUtils.greaterOrEqual(version, VERSION_3_0)) {
            ret.add(StorageType.LOCALFS);
        }

        if (VersionUtils.greaterOrEqual(version, VERSION_3_1)) {
            ret.add(StorageType.POSIXFS);
        }

        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            ret.add(StorageType.GLUSTERFS);
        }
        return ret;
    }

    private List<CustomProperty> getVmHooksEnvs(Version version) {
        List<CustomProperty> ret = new ArrayList<CustomProperty>();
        ret.addAll(CustomPropertiesParser.parse(getConfigurationValue(String.class,
                ConfigurationValues.PredefinedVMProperties,
                version),
                true));
        ret.addAll(CustomPropertiesParser.parse(getConfigurationValue(String.class,
                ConfigurationValues.UserDefinedVMProperties,
                version),
                true));
        return ret;
    }

    private List<ServerCpu> getServerCpuList(Version version) {
        return getEntity(List.class, VdcQueryType.GetAllServerCpuList,
                new GetAllServerCpuListParameters(new org.ovirt.engine.core.compat.Version(asString(version))),
                "List<ServerCpu>");
    }

    public List<Version> getSupportedClusterLevels() {
        List<Version> versions = new ArrayList<Version>();
        for (org.ovirt.engine.core.compat.Version v : (Set<org.ovirt.engine.core.compat.Version>) getConfigurationValueDefault(Set.class,
                ConfigurationValues.SupportedClusterLevels)) {
            Version version = new Version();
            version.setMajor(v.getMajor());
            version.setMinor(v.getMinor());
            versions.add(version);
        }
        return versions;
    }

    private Permit map(PermitType entity) {
        return mappingLocator.getMapper(PermitType.class, Permit.class).map(entity, null);
    }

    private Permits getPermits() {
        Permits permits = new Permits();
        for (PermitType permit : PermitType.values()) {
            permits.getPermits().add(map(permit));
        }
        return permits;
    }

    private SchedulingPolicies getSchedulingPolicies() {
        SchedulingPolicies policies = new SchedulingPolicies();
        for (SchedulingPolicyType policy : SchedulingPolicyType.values()) {
            policies.getPolicy().add(policy.value());
        }
        return policies;
    }

    private void addMigrateOnErrorOptions(VersionCaps version, MigrateOnError[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_0)) {
            version.setErrorHandling(new org.ovirt.engine.api.model.ErrorHandlingOptions());
            for (MigrateOnError option : values) {
                version.getErrorHandling().getErrorHandling().add(option.value());
            }
        }
    }

    private void addStorageFormatOptions(VersionCaps version, StorageFormat... formats) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_0)) {
            version.setStorageFormats(new StorageFormats());
            for (StorageFormat format : formats) {
                version.getStorageFormats().getStorageFormats().add(format.value());
            }
        }
    }

    private void addCreationStates(VersionCaps version, CreationStatus[] values) {
        version.setCreationStates(new CreationStates());
        for (CreationStatus status : values) {
            version.getCreationStates().getCreationStates().add(status.value());
        }
    }

    private void addDataCenterStates(VersionCaps version, DataCenterStatus[] values) {
        version.setDataCenterStates(new DataCenterStates());
        for (DataCenterStatus status : values) {
            version.getDataCenterStates().getDataCenterStates().add(status.value());
        }
    }

    private void addHostNICStates(VersionCaps version, NicStatus[] values) {
        version.setHostNicStates(new HostNICStates());
        for (NicStatus status : values) {
            version.getHostNicStates().getHostNICStates().add(status.value());
        }
    }

    private void addDiskStates(VersionCaps version, DiskStatus[] values) {
        version.setDiskStates(new DiskStates());
        for (DiskStatus status : values) {
            version.getDiskStates().getDiskStates().add(status.value());
        }
    }

    private void addVmStates(VersionCaps version, VmStatus[] values) {
        version.setVmStates(new VmStates());
        for (VmStatus status : values) {
            version.getVmStates().getVmStates().add(status.value());
        }
    }

    private void addVmPauseDetails(VersionCaps version, VmPauseStatus[] values) {
        version.setVmPauseDetails(new VmPauseDetails());
        for (VmPauseStatus detail : values) {
            version.getVmPauseDetails().getVmPauseDetails().add(detail.name().toLowerCase());
        }
    }

    private void addTemplateStates(VersionCaps version, TemplateStatus[] values) {
        version.setTemplateStates(new TemplateStates());
        for (TemplateStatus status : values) {
            version.getTemplateStates().getTemplateStates().add(status.value());
        }
    }

    private void addNetworkStates(VersionCaps version, NetworkStatus[] values) {
        version.setNetworkStates(new NetworkStates());
        for (NetworkStatus status : values) {
            version.getNetworkStates().getNetworkStates().add(status.value());
        }
    }

    private void addHostStates(VersionCaps version, HostStatus[] values) {
        version.setHostStates(new HostStates());
        for (HostStatus status : values) {
            version.getHostStates().getHostStates().add(status.value());
        }
    }

    private void addHostNonOperationalDetails(VersionCaps version, NonOperationalReason[] values) {
        version.setHostNonOperationalDetails(new HostNonOperationalDetails());
        for (NonOperationalReason reason : values) {
            version.getHostNonOperationalDetails().getHostNonOperationalDetails().add(reason.name().toLowerCase());
        }
    }

    private void addPowerManagementStateses(VersionCaps version, PowerManagementStatus[] values) {
        version.setPowerManagementStates(new PowerManagementStates());
        for (PowerManagementStatus status : values) {
            version.getPowerManagementStates().getPowerManagementStates().add(status.value());
        }
    }

    private void addStorageDomaintStates(VersionCaps version, StorageDomainStatus[] values) {
        version.setStorageDomainStates(new StorageDomainStates());
        for (StorageDomainStatus status : values) {
            version.getStorageDomainStates().getStorageDomainStates().add(status.value());
        }
    }

    // Gluster related types and states
    private void addGlusterVolumeTypes(VersionCaps version, GlusterVolumeType[] types) {
        version.setGlusterVolumeTypes(new GlusterVolumeTypes());
        for (GlusterVolumeType type : types) {
            version.getGlusterVolumeTypes().getGlusterVolumeTypes().add(type.value());
        }
    }

    private void addTransportTypes(VersionCaps version, TransportType[] types) {
        version.setTransportTypes(new TransportTypes());
        for (TransportType type : types) {
            version.getTransportTypes().getTransportTypes().add(type.value());
        }
    }

    private void addGlusterVolumeStates(VersionCaps version, GlusterState[] states) {
        version.setGlusterVolumeStates(new GlusterStates());
        for (GlusterState type : states) {
            version.getGlusterVolumeStates().getGlusterStates().add(type.value());
        }
    }

    private void addGlusterBrickStates(VersionCaps version, GlusterState[] states) {
        version.setBrickStates(new GlusterStates());
        for (GlusterState type : states) {
            version.getBrickStates().getGlusterStates().add(type.value());
        }
    }

    private void addGlusterHookContentTypes(VersionCaps version, HookContentType[] values) {
        version.setContentTypes(new ContentTypes());
        for (HookContentType type : values) {
            version.getContentTypes().getContentTypes().add(type.value());
        }
    }

    private void addGlusterHookStates(VersionCaps version, HookStatus[] values) {
        version.setHookStates(new HookStates());
        for (HookStatus status : values) {
            version.getHookStates().getHookStates().add(status.value());
        }
    }

    private void addStages(VersionCaps version, HookStage[] values) {
        version.setStages(new Stages());
        for (HookStage stage : values) {
            version.getStages().getStages().add(stage.value());
        }
    }

    private void addPmProxyTypes(VersionCaps version, PmProxyType[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_2)) {
            version.setPmProxyTypes(new PmProxyTypes());
            for (PmProxyType pmProxyType : values) {
                version.getPmProxyTypes().getPmProxyTypes().add(pmProxyType.value());
            }
        }
    }

    private void addStepEnumTypes(VersionCaps version, StepEnum[] states) {
        version.setStepTypes(new StepTypes());
        for (StepEnum type : states) {
            version.getStepTypes().getStepType().add(type.value());
        }
    }

    private void addAuthenticationMethods(VersionCaps version, AuthenticationMethod[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.setAuthenticationMethods(new org.ovirt.engine.api.model.AuthenticationMethod());
            for (AuthenticationMethod authType : values) {
                version.getAuthenticationMethods().getAuthenticationMethod().add(authType.value());
            }
        }
    }

    private void addSerialNumberPolicies(VersionCaps version, SerialNumberPolicy[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_5)) {
            version.setSerialNumberPolicies(new SerialNumberPolicies());
            for (SerialNumberPolicy mode : values) {
                version.getSerialNumberPolicies().getSerialNumberPolicies().add(mode.value());
            }
        }
    }

    private void addKdumpStates(VersionCaps version, KdumpStatus... values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_5)) {
            version.setKdumpStates(new KdumpStates());
            for (KdumpStatus status : values) {
                version.getKdumpStates().getKdumpStates().add(status.value());
            }
        }
    }

    private void addSELinuxModes(VersionCaps version, SELinuxMode[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_5)) {
            version.setSelinuxModes(new SELinuxModes());
            for (SELinuxMode mode : values) {
                version.getSelinuxModes().getSELinuxModes().add(mode.value());
            }
        }
    }

    private void addRngSources(VersionCaps ver, RngSource[] vals) {
        if (VersionUtils.greaterOrEqual(ver, VERSION_3_5)) {
            ver.setRngSources(new RngSources());
            for (RngSource src : vals) {
                ver.getRngSources().getRngSources().add(src.name());
            }
        }
    }

    private void addPolicyUnitTypes(VersionCaps version, PolicyUnitType[] values) {
        version.setSchedulingPolicyUnitTypes(new SchedulingPolicyUnitTypes());
        for (PolicyUnitType policyUnitType : values) {
            version.getSchedulingPolicyUnitTypes()
                    .getSchedulingPolicyUnitTypes()
                    .add(policyUnitType.name().toLowerCase());
        }
    }

    private void addSupportedQosTypes(VersionCaps version) {
        version.setQosTypes(new QosTypes());
        if (VersionUtils.greaterOrEqual(version, VERSION_3_3)) {
            version.getQosTypes().getQosTypes().add(org.ovirt.engine.api.model.QosType.NETWORK.name().toLowerCase());
        }
        if (VersionUtils.greaterOrEqual(version, VERSION_3_5)) {
            version.getQosTypes().getQosTypes().add(org.ovirt.engine.api.model.QosType.STORAGE.name().toLowerCase());
            version.getQosTypes().getQosTypes().add(org.ovirt.engine.api.model.QosType.CPU.name().toLowerCase());
        }
    }

    private void addSpmStates(VersionCaps version, SpmState[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_5)) {
            SpmStates states = new SpmStates();
            for (SpmState state : values) {
                states.getSpmStates().add(state.value());
            }
            version.setSpmStates(states);
        }
    }

    private void addInheritableBooleans(VersionCaps version, InheritableBoolean[] values) {
        if (VersionUtils.greaterOrEqual(version, VERSION_3_6)) {
            version.setInheritableBooleans(new InheritableBooleans());
            for (InheritableBoolean bool : values) {
                version.getInheritableBooleans().getInheritableBooleans().add(bool.value());
            }
        }
    }

    @Override
    public CapabiliyResource getCapabilitiesSubResource(String id) {
        return new BackendCapabilityResource(id, this);
    }
}
