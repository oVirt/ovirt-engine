package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

@RunWith(MockitoJUnitRunner.class)
public class AddUnmanagedVmsCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Mock
    VmStaticDao vmStaticDao;

    @Spy
    @InjectMocks
    AddUnmanagedVmsCommand<AddUnmanagedVmsParameters> addUnamangedVmsCommand =
            new AddUnmanagedVmsCommand<>(new AddUnmanagedVmsParameters(), null);

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
        doNothing().when(addUnamangedVmsCommand).addDevices(anyMap(), anyLong());
        doNothing().when(addUnamangedVmsCommand).importHostedEngineVm(any(VM.class));
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
        verify(addUnamangedVmsCommand).addExternallyManagedVm(argThat(vmStatic -> {
            assertThat(vmStatic.getNumOfSockets(), is(4));
            assertThat(vmStatic.getMemSizeMb(), is(7052));
            return true;
        }));
    }

    @Test
    public void shouldDetectHostedEngineVM() throws IOException {
        addUnamangedVmsCommand.importHostedEngineVm(hosted_engine);
        verify(addUnamangedVmsCommand, times(0)).addExternallyManagedVm(any(VmStatic.class));
        verify(addUnamangedVmsCommand).importHostedEngineVm(argThat(new HEVmMatcher()));
    }

    private class HEVmMatcher implements ArgumentMatcher<VM> {

        @Override
        public boolean matches(VM argument) {
            VmStatic vmStatic = argument.getStaticData();
            assertThat(vmStatic.getNumOfSockets(), is(4));
            assertThat(vmStatic.getMemSizeMb(), is(7052));
            assertThat(vmStatic.getManagedDeviceMap().size(), is(2));
            for(VmDevice vmDevice : vmStatic.getManagedDeviceMap().values()){
                if(vmDevice instanceof GraphicsDevice){
                   GraphicsDevice device = (GraphicsDevice) vmDevice;
                   assertThat(device.getGraphicsType(), is(GraphicsType.VNC));
                   assertThat(vmStatic.getDefaultDisplayType(), is(DisplayType.vga));
                }
                else{
                    assertThat(vmDevice.getType(), is(VmDeviceGeneralType.CONSOLE));
                }

            }

            return true;
        }
    }
}
