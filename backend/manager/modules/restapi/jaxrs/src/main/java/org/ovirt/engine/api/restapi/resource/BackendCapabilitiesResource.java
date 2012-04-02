package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootDevices;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.BootProtocols;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.CPUs;
import org.ovirt.engine.api.model.Capabilities;
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
import org.ovirt.engine.api.model.Features;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.FenceTypes;
import org.ovirt.engine.api.model.HostNICStates;
import org.ovirt.engine.api.model.HostNonOperationalDetails;
import org.ovirt.engine.api.model.HostStates;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.MigrateOnError;
import org.ovirt.engine.api.model.NetworkStates;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.NicInterfaces;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.OsTypes;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.PowerManagementStates;
import org.ovirt.engine.api.model.PowerManagementStatus;
import org.ovirt.engine.api.model.PowerManagers;
import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.api.model.StorageDomainStates;
import org.ovirt.engine.api.model.StorageDomainStatus;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageDomainTypes;
import org.ovirt.engine.api.model.StorageFormats;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.StorageTypes;
import org.ovirt.engine.api.model.TemplateStates;
import org.ovirt.engine.api.model.TemplateStatus;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.api.model.VmAffinities;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPauseDetails;
import org.ovirt.engine.api.model.VmStates;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.model.VmTypes;
import org.ovirt.engine.api.resource.CapabilitiesResource;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.util.FencingOptionsParser;
import org.ovirt.engine.api.restapi.util.ServerCpuParser;
import org.ovirt.engine.api.restapi.util.VersionHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.VersionUtils;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.queries.ConfigurationValues;

public class BackendCapabilitiesResource extends BackendResource implements CapabilitiesResource {

    private MappingLocator mappingLocator;

    public void setMappingLocator(MappingLocator mappingLocator) {
        this.mappingLocator = mappingLocator;
    }

    private static final Version VERSION_3_0 = new Version() {{ major = 3; minor = 0; }};
    private static Version currentVersion = null;

    public Capabilities get() {
    Capabilities caps = new Capabilities();
        VersionCaps current = null;

        for (Version v : getSupportedClusterLevels()) {
            VersionCaps version = new VersionCaps();

            version.setMajor(v.getMajor());
            version.setMinor(v.getMinor());

            version.setCPUs(new CPUs());

            for (ServerCpu sc : getServerCpuList(v)) {
                CPU cpu = new CPU();
                cpu.setId(sc.getCpuName());
                cpu.setLevel(sc.getLevel());
                version.getCPUs().getCPUs().add(cpu);
            }

            addPowerManagers(version, getPowerManagers(v));
            addVmTypes(version, VmType.values());
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
            addnetworkBootProtocols(version, BootProtocol.values());
            addMigrateOnErrorOptions(version, MigrateOnError.values());
            addStorageFormatOptions(version, StorageFormat.values());
            addOsTypes(version, OsType.values());

            //Add States. User can't update States, but he still needs to know which exist.
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

            version.setFeatures(getFeatures(v));

            caps.getVersions().add(version);

            if (current == null && VersionHelper.equals(v, getCurrentVersion())) {
                current = version;
                current.setCurrent(true);
            } else {
                version.setCurrent(false);
            }
        }

        caps.setPermits(getPermits());
        caps.setSchedulingPolicies(getSchedulingPolicies());

        return caps;
    }

    private Version getCurrentVersion() {
        if (currentVersion == null) {
            currentVersion = VersionHelper.parseVersion(getConfigurationValue(String.class,
                                                                              ConfigurationValues.VdcVersion,
                                                                              null));
        }
        return currentVersion;
    }

    private void addOsTypes(VersionCaps version, OsType[] types) {
        version.setOsTypes(new OsTypes());
        for (OsType type : types) {
            version.getOsTypes().getOsTypes().add(type.value());
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
        return FencingOptionsParser.parse(getConfigurationValue(String.class, ConfigurationValues.VdsFenceOptionMapping, version),
                                          getConfigurationValue(String.class, ConfigurationValues.VdsFenceOptionTypes, version),
                                          true);
    }

    private List<StorageType> getStorageTypes(Version version) {
        List<StorageType> ret = new ArrayList<StorageType>();
        ret.add(StorageType.ISCSI);
        ret.add(StorageType.FCP);
        ret.add(StorageType.NFS);
        if (localStorageEnabled(version)) {
            ret.add(StorageType.LOCALFS);
        }
        return ret;
    }

    private boolean localStorageEnabled(Version version) {
        return getConfigurationValue(Boolean.class, ConfigurationValues.LocalStorageEnabled, version);
    }

    private List<CustomProperty> getVmHooksEnvs(Version version) {
        List<CustomProperty> ret = new ArrayList<CustomProperty>();
        ret.addAll(CustomPropertiesParser.parse(getConfigurationValue(String.class, ConfigurationValues.PredefinedVMProperties, version), true));
        ret.addAll(CustomPropertiesParser.parse(getConfigurationValue(String.class, ConfigurationValues.UserDefinedVMProperties, version), true));
        return ret;
    }

    private Features getFeatures(Version version) {
        Features ret = new Features();
        if (VersionUtils.greaterOrEqual(version, VERSION_3_0)) {
            ret.setTransparentHugepages(new TransparentHugePages());
        }
        return ret;
    }

    private List<ServerCpu> getServerCpuList(Version version) {
        return ServerCpuParser.parseCpus(getConfigurationValue(String.class, ConfigurationValues.ServerCPUList, version));
    }

    private List<Version> getSupportedClusterLevels() {
        List<Version> versions = new ArrayList<Version>();
        for (org.ovirt.engine.core.compat.Version v :
                    (Set<org.ovirt.engine.core.compat.Version>)getConfigurationValue(Set.class, ConfigurationValues.SupportedClusterLevels, null)){
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

}
