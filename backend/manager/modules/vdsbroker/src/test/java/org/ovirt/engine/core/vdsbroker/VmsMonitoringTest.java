package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@RunWith(MockitoJUnitRunner.class)
public class VmsMonitoringTest {

    private static final Version vdsCompVersion = Version.v3_4;

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ReportedDisksLogicalNames,
                    vdsCompVersion.getValue(),
                    true),
            mockConfig(ConfigValues.HostedEngineVmName,
                    ConfigCommon.defaultConfigurationVersion,
                    "HostedEngine")
    );

    VmsMonitoring vmsMonitoring;

    @Mock
    DbFacade dbFacade;

    @Mock
    VmDeviceDao vmDeviceDao;

    @Mock
    AuditLogDirector auditLogDirector;

    @Mock
    private VdsManager vdsManager;

    @Mock
    private IVdsEventListener eventListener;

    private static Map<String, Object> external_vm;

    private static Map<String, Object> hosted_engine;

    private static Map<String, Object> internal_vm;

    @BeforeClass
    public static void loadVmData() throws IOException {
        external_vm = loadVm("/external_vm.json");
        internal_vm = loadVm("/internal_vm.json");
        hosted_engine = loadVm("/he_vm.json");
    }

    @Before
    public void setup() {
        when(dbFacade.getVmDeviceDao()).thenReturn(vmDeviceDao);
        when(vdsManager.getGroupCompatibilityVersion()).thenReturn(vdsCompVersion);
        vmsMonitoring = Mockito.spy(
                new VmsMonitoring(
                        vdsManager,
                        Arrays.asList(VmTestPairs.MIGRATION_DONE.build()),
                        Collections.<Pair<VM, VmInternalData>>emptyList(), auditLogDirector,
                        System.nanoTime()) {

                    @Override
                    public DbFacade getDbFacade() {
                        return dbFacade;
                    }

                    @Override
                    protected IVdsEventListener getVdsEventListener() {
                        return eventListener;
                    }
                }
        );
    }

    private static Map<String, Object> loadVm(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(VmsMonitoringTest.class.getResourceAsStream(resourcePath));
        Map<String, Object> map = mapper.convertValue(node, Map.class);
        map.put(VdsProperties.Devices, ((List) map.get(VdsProperties.Devices)).toArray());
        return map;
    }

    @Test
    public void shouldConvertExternalVm() throws IOException {
        vmsMonitoring.convertVm(1, DisplayType.qxl, external_vm);
        VmStatic vmStatic = vmsMonitoring.getExternalVmsToAdd().get(0);
        verifyZeroInteractions(eventListener);
        assertThat(vmStatic.getNumOfSockets(), is(4));
        assertThat(vmStatic.getMemSizeMb(), is(7052));
    }

    @Test
    public void shouldDetectHostedEngineVM() throws IOException {
        vmsMonitoring.importHostedEngineVM(hosted_engine);
        verify(eventListener).importHostedEngineVm(argThat(new HEVmMatcher()));
    }

    @Test
    public void shouldExtractExternalVmDevices() throws IOException {
        vmsMonitoring.processVmDevices(external_vm);
        List<VmDevice> newDevices = vmsMonitoring.getNewVmDevices();
        List<VmDeviceId> removedDevices = vmsMonitoring.getRemovedVmDevices();
        List<VmDeviceGeneralType> devices = getDeviceTypes(vmsMonitoring.getNewVmDevices());
        List<String> deviceNames = getDevice(vmsMonitoring.getNewVmDevices());

        assertThat(removedDevices.size(), is(0));
        assertThat(newDevices.size(), is(11));

        // A ballooning device model of type 'none' means that there is no such device
        assertThat(devices.contains(VmDeviceGeneralType.BALLOON), is(false));
        //TODO: Do we really not want to import devices like that?
        assertThat(devices.contains(VmDeviceGeneralType.CONSOLE), is(false));

        //Cirrus device should be there
        assertThat(deviceNames.contains(VmDeviceType.CIRRUS.name().toLowerCase()), is(true));
        //TODO: VNC and SPICE devices should also be imported for external VMs
        assertThat(deviceNames.contains(VmDeviceType.VNC.name().toLowerCase()), is(false));
    }

    @Test
    public void shouldExtractInternalVmDevices() throws IOException {
        vmsMonitoring.processVmDevices(internal_vm);
        List<VmDevice> newDevices = vmsMonitoring.getNewVmDevices();
        List<VmDeviceId> removedDevices = vmsMonitoring.getRemovedVmDevices();
        List<VmDeviceGeneralType> deviceTypes = getDeviceTypes(vmsMonitoring.getNewVmDevices());
        List<String> deviceNames = getDevice(vmsMonitoring.getNewVmDevices());

        assertThat(removedDevices.size(), is(0));
        assertThat(newDevices.size(), is(11));

        assertThat(deviceTypes.contains(VmDeviceGeneralType.BALLOON), is(true));
        //TODO: Do we really not want to import deviceTypes like that?
        assertThat(deviceTypes.contains(VmDeviceGeneralType.CONSOLE), is(false));

        //QXL device should be there
        assertThat(deviceNames.contains(VmDeviceType.QXL.name().toLowerCase()), is(true));
        //SPICE device details are fetched when needed, don't reimport the device
        assertThat(deviceNames.contains(VmDeviceType.SPICE.name().toLowerCase()), is(false));
    }

    private List<VmDeviceGeneralType> getDeviceTypes(List<VmDevice> devices) {
        List<VmDeviceGeneralType> deviceTypes = new ArrayList<>();
        for (VmDevice device : devices) {
            deviceTypes.add(device.getType());
        }
        return deviceTypes;
    }

    private List<String> getDevice(List<VmDevice> devices) {
        List<String> deviceNames = new ArrayList<>();
        for (VmDevice device : devices) {
            deviceNames.add(device.getDevice());
        }
        return deviceNames;
    }

    private class HEVmMatcher extends ArgumentMatcher<VM> {

        @Override
        public boolean matches(Object argument) {
            VmStatic vmStatic = ((VM) argument).getStaticData();
            assertThat(vmStatic.getNumOfSockets(), is(4));
            assertThat(vmStatic.getMemSizeMb(), is(7052));
            assertThat(vmStatic.getManagedDeviceMap().size(), is(1));
            GraphicsDevice device = (GraphicsDevice) vmStatic.getManagedDeviceMap().values().iterator().next();
            assertThat(device.getGraphicsType(), is(GraphicsType.VNC));
            return true;
        }
    }
}
