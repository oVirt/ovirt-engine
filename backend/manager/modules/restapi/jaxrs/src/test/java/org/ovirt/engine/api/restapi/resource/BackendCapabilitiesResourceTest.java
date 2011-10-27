package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.Capabilities;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.DiskInterface;
import org.ovirt.engine.api.model.DiskType;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.ErrorHandlingOptions;
import org.ovirt.engine.api.model.FenceType;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.SchedulingPolicyType;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VmAffinities;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.model.VersionCaps;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.api.restapi.utils.VersionUtils;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { Config.class })
public class BackendCapabilitiesResourceTest extends AbstractBackendResourceTest {

    BackendCapabilitiesResource resource;
    private static final Version VERSION_2_3 = new Version() {{ major = 2; minor = 3; }};

    public BackendCapabilitiesResourceTest() {
        resource = new BackendCapabilitiesResource();
    }

    protected BackendCapabilitiesResourceTest(BackendCapabilitiesResource resource) {
        this.resource = resource;
    }

    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Ignore
    @Test
    public void testGet() throws Exception {
        mockStatic(Config.class);

        HashSet<org.ovirt.engine.core.compat.Version> supportedVersions =
            new HashSet<org.ovirt.engine.core.compat.Version>();
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(1, 5));
        supportedVersions.add(new org.ovirt.engine.core.compat.Version(10, 3));

        //expect(Config.GetValue(ConfigValues.SupportedClusterLevels)).andReturn(supportedVersions);
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "ConfigValue"},
                new Object[] { ConfigurationValues.SupportedClusterLevels},
                supportedVersions);

        //expect(Config.GetValue(ConfigValues.ServerCPUList, "1.5")).andReturn("0:bar:0:foo");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.ServerCPUList },
                "0:bar:0:foo");

        //expect(Config.GetValue(ConfigValues.ServerCPUList, "10.3")).andReturn("15:foo:1,2,3:bar");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.ServerCPUList },
                "15:foo:1,2,3:bar");

        //expect(Config.GetValue(ConfigValues.VdsFenceOptionMapping, "1.5")).andReturn("foo:one=1,two=2");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        //expect(Config.GetValue(ConfigValues.VdsFenceOptionTypes, "1.5")).andReturn("one=int,two=bool");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        //expect(Config.GetValue(ConfigValues.VdsFenceOptionMapping, "10.3")).andReturn("foo:one=1,two=2");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionMapping },
                "foo:one=1,two=2");

        //expect(Config.GetValue(ConfigValues.VdsFenceOptionTypes, "10.3")).andReturn("one=int,two=bool");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.VdsFenceOptionTypes },
                "one=int,two=bool");

        //expect(Config.GetValue(ConfigValues.LocalStorageEnabled, "1.5")).andReturn(Boolean.FALSE);
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.LocalStorageEnabled },
                Boolean.FALSE);

        //expect(Config.GetValue(ConfigValues.LocalStorageEnabled, "10.3")).andReturn(Boolean.TRUE);
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.LocalStorageEnabled },
                Boolean.TRUE);

        //expect(Config.GetValue(ConfigValues.PredefinedVMProperties, "1.5")).andReturn("");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.PredefinedVMProperties },
                "");

        //expect(Config.GetValue(ConfigValues.UserDefinedVMProperties, "1.5")).andReturn("");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "1.5", ConfigurationValues.UserDefinedVMProperties },
                "");

        //expect(Config.GetValue(ConfigValues.PredefinedVMProperties, "10.3")).andReturn("foo=true|false");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.PredefinedVMProperties },
                "foo=true|false");

        //expect(Config.GetValue(ConfigValues.UserDefinedVMProperties, "10.3")).andReturn("bar=[a-z]");
        setUpGetEntityExpectations(VdcQueryType.GetConfigurationValue,
                GetConfigurationValueParameters.class,
                new String[] { "Version", "ConfigValue" },
                new Object[] { "10.3", ConfigurationValues.UserDefinedVMProperties },
                "bar=[a-z]");

        replayAll();

        verifyCapabilities(resource.get());
    }

    private void verifyCapabilities(Capabilities capabilities) {
        assertNotNull(capabilities);
        assertEquals(2, capabilities.getVersions().size());
        verifyVersion(capabilities.getVersions().get(0), 1, 5, false, "bar", 0, false, false, false);
        verifyVersion(capabilities.getVersions().get(1), 10, 3, true, "foo", 15, true, true, true);
        verifyPermits(capabilities);
        verifySchedulingPolicies(capabilities);
    }

    private void verifyVersion(VersionCaps version, int major, int minor, boolean current, String cpuName, int cpuLevel, boolean localStorage, boolean hooks, boolean thp) {
        assertEquals(major, version.getMajor());
        assertEquals(minor, version.getMinor());
        assertEquals(current, version.isCurrent());
        assertNotNull(version.getCPUs());
        assertTrue(version.getCPUs().getCPUs().size() == 1);
        verifyCPU(version.getCPUs().getCPUs().get(0), cpuName, cpuLevel);
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
        verifyDiskTypes(version.getDiskTypes().getDiskTypes());
        verifyDiskFormats(version.getDiskFormats().getDiskFormats());
        verifyDiskInterfaces(version.getDiskInterfaces().getDiskInterfaces());
        verifyVmAffinities(version,version.getVmAffinities());
        verifyMigrateOnErrorOptions(version, version.getErrorHandling());
        verifyOsTypes(version.getOsTypes().getOsTypes());

        if (hooks) {
            verifyHooksEnvs(version.getCustomProperties().getCustomProperty());
        }
        if (thp) {
            assertNotNull(version.getFeatures());
            assertNotNull(version.getFeatures().getTransparentHugepages());
        }
    }

    private void verifyOsTypes(List<String> osTypes) {
        assertEquals(OsType.values().length, osTypes.size());
        for (OsType osType : OsType.values()) {
            assertTrue(osTypes.contains(osType.value()));
        }
    }

    private void verifyVmAffinities(final VersionCaps version, VmAffinities vmAffinities) {
        if(VersionUtils.greaterOrEqual(
                new Version(){{major=version.getMajor();minor=version.getMinor();}},
                VERSION_2_3)){
            assertNotNull(vmAffinities);
        }
        else {
            assertEquals(null, vmAffinities);
        }
    }

    private void verifyMigrateOnErrorOptions(final VersionCaps version, ErrorHandlingOptions errorHandling) {
        if(greaterOrEqual(
                new Version(){{major=version.getMajor();minor=version.getMinor();}},
                VERSION_2_3)){
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

    private void verifyDiskTypes(List<String> diskTypes) {
        assertEquals(DiskType.values().length, diskTypes.size());
        for (DiskType diskType : DiskType.values()) {
            assertTrue(diskTypes.contains(diskType.value()));
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
        return a.getMajor() != b.getMajor() ? a.getMajor() >= b.getMajor() : a.getMinor() >= b.getMinor();
    }

    @Override
    protected Object getEntity(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String getSessionId() {
        return resource.getSessionHelper().getSessionId(principal);
    }

    @Override
    protected void init() {
        resource.setBackend(backend);
        resource.setMappingLocator(mapperLocator);
        resource.setSessionHelper(sessionHelper);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        current = createMock(Current.class);
        principal = new Principal(USER, SECRET, DOMAIN);
        expect(current.get(Principal.class)).andReturn(principal).anyTimes();

        sessionHelper = new SessionHelper();
        sessionHelper.setCurrent(current);
        resource.setSessionHelper(sessionHelper);

        backend = createMock(BackendLocal.class);
        resource.setBackend(backend);

        MessageBundle messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        resource.setMessageBundle(messageBundle);

        httpHeaders = createMock(HttpHeaders.class);
        List<Locale> locales = new ArrayList<Locale>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        resource.setHttpHeaders(httpHeaders);
        init();
    }
}
