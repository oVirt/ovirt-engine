package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class AddUnmanagedVmsCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.HostedEngineVmName,
                    ConfigCommon.defaultConfigurationVersion,
                    "HostedEngine")
    );

    @Mock
    Instance<HostedEngineImporter> hostedEngineImporterProvider;

    @Mock
    HostedEngineImporter hostedEngineImporter;

    @Mock
    DbFacade dbFacade;

    @InjectMocks
    AddUnmanagedVmsCommand addUnamangedVmsCommand =
            spy(new AddUnmanagedVmsCommand(new AddUnmanagedVmsParameters(), null) {
                @Override public DbFacade getDbFacade() {
                    return dbFacade;
                }
            });


    private static Map<String, Object> external_vm;

    private static Map<String, Object> hosted_engine;

    @BeforeClass
    public static void loadVmData() throws IOException {
        external_vm = loadVm("/external_vm.json");
        hosted_engine = loadVm("/he_vm.json");
    }

    @Before
    public void setup() {
        doNothing().when(addUnamangedVmsCommand).addExternallyManagedVm(any(VmStatic.class));
        doNothing().when(addUnamangedVmsCommand).addDevices(any(Map.class), anyLong());
        doNothing().when(addUnamangedVmsCommand).importHostedEngineVm(any(VM.class));
        doReturn(hostedEngineImporter).when(hostedEngineImporterProvider).get();
        doReturn(Mockito.mock(VmStaticDao.class)).when(dbFacade).getVmStaticDao();
    }

    private static Map<String, Object> loadVm(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(AddUnmanagedVmsCommand.class.getResourceAsStream(resourcePath));
        Map<String, Object> map = mapper.convertValue(node, Map.class);
        map.put(VdsProperties.Devices, ((List) map.get(VdsProperties.Devices)).toArray());
        return map;
    }

    @Test
    public void shouldConvertExternalVm() throws IOException {
        addUnamangedVmsCommand.convertVm(1, DisplayType.qxl, System.nanoTime(), external_vm);
        verify(addUnamangedVmsCommand).addExternallyManagedVm(argThat(new ArgumentMatcher<VmStatic>() {
            @Override
            public boolean matches(Object argument) {
                VmStatic vmStatic = (VmStatic) argument;
                assertThat(vmStatic.getNumOfSockets(), is(4));
                assertThat(vmStatic.getMemSizeMb(), is(7052));
                return true;
            }
        }));
    }

    @Test
    public void shouldDetectHostedEngineVM() throws IOException {
        addUnamangedVmsCommand.importHostedEngineVm(hosted_engine);
        verify(addUnamangedVmsCommand, times(0)).addExternallyManagedVm(any(VmStatic.class));
        verify(addUnamangedVmsCommand).importHostedEngineVm(argThat(new HEVmMatcher()));
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
            assertThat(vmStatic.getDefaultDisplayType(), is(DisplayType.vga));
            return true;
        }
    }
}
