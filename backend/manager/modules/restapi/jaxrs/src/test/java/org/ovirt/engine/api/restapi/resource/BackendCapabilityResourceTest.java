package org.ovirt.engine.api.restapi.resource;

import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Capabilities;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.ErrorHandlingOptions;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.api.model.VmAffinities;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.VersionUtils;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendCapabilityResourceTest extends AbstractBackendResourceTest {

    BackendCapabilitiesResource parent;
    BackendCapabilityResource resource;

    private static final Version VERSION_2_3 = new Version() {
        {
            major = 2;
            minor = 3;
        }
    };

    public BackendCapabilityResourceTest() {
        parent = new BackendCapabilitiesResource();
        resource = new BackendCapabilityResource(parent.generateId(VERSION_2_3), parent);
    }

    protected BackendCapabilityResourceTest(BackendCapabilityResource resource) {
        this.resource = resource;
    }

    protected void setUriInfo(UriInfo uriInfo) {
        parent.setUriInfo(uriInfo);
    }

    @Ignore
    @Test
    public void testGet() throws Exception {
        HashSet<org.ovirt.engine.core.compat.Version> supportedVersions =
                new HashSet<org.ovirt.engine.core.compat.Version>();
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(1, 5));
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(10, 3));

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "ConfigValue" },
                new Object[] { ConfigurationValues.SupportedClusterLevels },
                supportedVersions);

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.PredefinedVMProperties },
                "");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.UserDefinedVMProperties },
                "");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.PredefinedVMProperties },
                "foo=true|false");

        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.UserDefinedVMProperties },
                "bar=[a-z]");

        verifyCapabilities(resource.get());
    }

    private void verifyCapabilities(VersionCaps capabilities) {
        assertNotNull(capabilities);
        //        assertEquals(2, capabilities.getVersions().size());
        //        verifyVersion(capabilities.getVersions().get(0), 1, 5, false, "bar", 0, false, false, false);
        //        verifyVersion(capabilities.getVersions().get(1), 10, 3, true, "foo", 15, true, true, true);
        //        verifyPermits(capabilities);
        //        verifySchedulingPolicies(capabilities);
    }

    private void verifyVersion(VersionCaps version,
            int major,
            int minor,
            boolean current,
            String cpuName,
            int cpuLevel,
            boolean localStorage,
            boolean hooks,
            boolean thp) {
        assertEquals(major, version.getMajor().intValue());
        assertEquals(minor, version.getMinor().intValue());
        assertEquals(current, version.isCurrent());
        assertNotNull(version.getCpus());
        assertTrue(version.getCpus().getCPUs().size() == 1);
        verifyCPU(version.getCpus().getCPUs().get(0), cpuName, cpuLevel);
        assertNotNull(version.getPowerManagers());
        assertEquals(1, version.getPowerManagers().getPowerManagers().size());
        verifyPowerManagement(version.getPowerManagers().getPowerManagers().get(0));
        verifyVmTypes(version.getVmTypes().getVmTypes());
        verifyStorageTypes(version.getStorageTypes().getStorageTypes(), localStorage);
        verifyStorageDomainTypes(version.getStorageDomainTypes().getStorageDomainTypes());
        verifyFenceTypes(version.getFenceTypes().getFenceTypes());
        verifyBootDevices(version.getBootDevices().getBootDevices());
        verifyDisplayTypes(version.getDisplayTypes().getDisplayTypes());
        verifyNicTypes(version.getNicInterfaces().getNicInterfaces());
        verifyDiskFormats(version.getDiskFormats().getDiskFormats());
        verifyDiskInterfaces(version.getDiskInterfaces().getDiskInterfaces());
        verifyVmAffinities(version, version.getVmAffinities());
        verifyMigrateOnErrorOptions(version, version.getErrorHandling());
        verifyOsTypes(version.getOsTypes().getOsTypes());

        if (hooks) {
            verifyHooksEnvs(version.getCustomProperties().getCustomProperty());
        }
        if (thp) {
            assertNotNull(version.getFeatures());
            assertFalse(version.getFeatures().getFeature().isEmpty());
            assertNotNull(version.getFeatures().getFeature().get(0).getTransparentHugepages());
        }
    }

    private void verifyOsTypes(List<String> osTypes) {
        assertEquals(OsType.values().length, osTypes.size());
        for (OsType osType : OsType.values()) {
            assertTrue(osTypes.contains(osType.value()));
        }
    }

    private void verifyVmAffinities(final VersionCaps version, VmAffinities vmAffinities) {
        if (VersionUtils.greaterOrEqual(
                new Version() {
                    {
                        major = version.getMajor();
                        minor = version.getMinor();
                    }
                },
                VERSION_2_3)) {
            assertNotNull(vmAffinities);
        }
        else {
            assertEquals(null, vmAffinities);
        }
    }

    private void verifyMigrateOnErrorOptions(final VersionCaps version, ErrorHandlingOptions errorHandling) {
        if (greaterOrEqual(
                new Version() {
                    {
                        major = version.getMajor();
                        minor = version.getMinor();
                    }
                },
                VERSION_2_3)) {
            assertNotNull(errorHandling);
        }
        else {
            assertNull(errorHandling);
        }
    }

    private void verifyCPU(CPU cpu, String name, Integer level) {
        assertNotNull(cpu);
        assertEquals(name, cpu.getId());
        assertEquals(level, cpu.getLevel());
    }

    private void verifyPowerManagement(PowerManagement pm) {
        assertNotNull(pm);
        assertEquals("foo", pm.getType());
        assertEquals(2, pm.getOptions().getOptions().size());
        verifyOption(pm.getOptions().getOptions().get(0), "one", "int");
        verifyOption(pm.getOptions().getOptions().get(1), "two", "bool");
    }

    private void verifyOption(Option option, String name, String type) {
        assertEquals(name, option.getName());
        assertEquals(type, option.getType());
        assertNull(option.getValue());
    }

    private void verifyVmTypes(List<String> vmTypes) {
        assertEquals(VmType.values().length, vmTypes.size());
        for (VmType vmType : VmType.values()) {
            assertTrue(vmTypes.contains(vmType.value()));
        }
    }

    private void verifyStorageTypes(List<String> storageTypes, boolean localStorage) {
        assertTrue(storageTypes.contains(StorageType.ISCSI.value()));
        assertTrue(storageTypes.contains(StorageType.FCP.value()));
        assertTrue(storageTypes.contains(StorageType.NFS.value()));
        if (localStorage) {
            assertTrue(storageTypes.contains(StorageType.LOCALFS.value()));
        }
    }

    private void verifyStorageDomainTypes(List<String> storageDomainTypes) {
        assertEquals(StorageDomainType.values().length, storageDomainTypes.size());
        for (StorageDomainType storageDomainType : StorageDomainType.values()) {
            assertTrue(storageDomainTypes.contains(storageDomainType.value()));
        }
    }

    private void verifyFenceTypes(List<String> fenceTypes) {
        assertEquals(FenceType.values().length, fenceTypes.size());
        for (FenceType fenceType : FenceType.values()) {
            assertTrue(fenceTypes.contains(fenceType.value()));
        }
    }

    private void verifyBootDevices(List<String> bootDevices) {
        assertEquals(BootDevice.values().length, bootDevices.size());
        for (BootDevice bootDevice : BootDevice.values()) {
            assertTrue(bootDevices.contains(bootDevice.value()));
        }
    }

    private void verifyDisplayTypes(List<String> displayTypes) {
        assertEquals(DisplayType.values().length, displayTypes.size());
        for (DisplayType displayType : DisplayType.values()) {
            assertTrue(displayTypes.contains(displayType.value()));
        }
    }

    private void verifyNicTypes(List<String> nicTypes) {
        assertEquals(NicInterface.values().length, nicTypes.size());
        for (NicInterface nicType : NicInterface.values()) {
            assertTrue(nicTypes.contains(nicType.value()));
        }
    }

    private void verifyDiskFormats(List<String> diskFormats) {
        assertEquals(DiskFormat.values().length, diskFormats.size());
        for (DiskFormat diskFormat : DiskFormat.values()) {
            assertTrue(diskFormats.contains(diskFormat.value()));
        }
    }

    private void verifyDiskInterfaces(List<String> diskInterfaces) {
        assertEquals(DiskInterface.values().length, diskInterfaces.size());
        for (DiskInterface diskInterface : DiskInterface.values()) {
            assertTrue(diskInterfaces.contains(diskInterface.value()));
        }
    }

    private void verifyHooksEnvs(List<CustomProperty> envs) {
        assertEquals(2, envs.size());
        verifyHooksEnv(envs.get(0), "foo", "true|false");
        verifyHooksEnv(envs.get(1), "bar", "[a-z]");
    }

    private void verifyHooksEnv(CustomProperty env, String name, String regexp) {
        assertNotNull(env);
        assertEquals(name, env.getName());
        assertEquals(regexp, env.getRegexp());
    }

    private void verifyPermits(Capabilities capabilities) {
        assertTrue(capabilities.isSetPermits());
        assertTrue(capabilities.getPermits().isSetPermits());
        assertFalse(capabilities.getPermits().getPermits().isEmpty());
        assertEquals(ActionGroup.values().length, capabilities.getPermits().getPermits().size());
    }

    private void verifySchedulingPolicies(Capabilities capabilities) {
        assertTrue(capabilities.isSetSchedulingPolicies());
        assertTrue(capabilities.getSchedulingPolicies().isSetPolicy());
        assertFalse(capabilities.getSchedulingPolicies().getPolicy().isEmpty());
        assertEquals(SchedulingPolicyType.values().length, capabilities.getSchedulingPolicies().getPolicy().size());
        for (SchedulingPolicyType policy : SchedulingPolicyType.values()) {
            assertTrue(capabilities.getSchedulingPolicies().getPolicy().contains(policy.value()));
        }
    }

    private boolean greaterOrEqual(Version a, Version b) {
        int aMajor = a.getMajor() == null ? 0 : a.getMajor().intValue();
        int aMinor = a.getMinor() == null ? 0 : a.getMinor().intValue();
        int bMajor = b.getMajor() == null ? 0 : b.getMajor().intValue();
        int bMinor = b.getMinor() == null ? 0 : b.getMinor().intValue();
        return aMajor != bMajor ? aMajor >= bMajor : aMinor >= bMinor;
    }

    @Override
    protected Object getEntity(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void init() {
        parent.setBackend(backend);
        parent.setMappingLocator(mapperLocator);
        parent.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }
}
