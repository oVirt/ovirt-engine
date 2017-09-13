package org.ovirt.engine.core.bll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private VmStaticDao vmStaticDao;

    @Spy
    @InjectMocks
    private AddUnmanagedVmsCommand<AddUnmanagedVmsParameters> addUnamangedVmsCommand =
            new AddUnmanagedVmsCommand<>(new AddUnmanagedVmsParameters(), null);

    private static Map<String, Object> externalVm;

    private static Map<String, Object> hostedEngine;

    @BeforeClass
    public static void loadVmData() throws IOException {
        externalVm = loadVm("/external_vm.json");
        hostedEngine = loadVm("/he_vm.json");
    }

    @Before
    public void setup() {
        doNothing().when(addUnamangedVmsCommand).addExternallyManagedVm(any());
        doNothing().when(addUnamangedVmsCommand).addDevices(any(), anyLong());
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
        addUnamangedVmsCommand.convertVm(1, DisplayType.qxl, System.nanoTime(), externalVm);
        verify(addUnamangedVmsCommand).addExternallyManagedVm(argThat(
                vmStatic -> vmStatic.getNumOfSockets() == 4 && vmStatic.getMemSizeMb() == 7052));
    }

    @Test
    public void shouldDetectHostedEngineVM() throws IOException {
        addUnamangedVmsCommand.importHostedEngineVm(hostedEngine);
        verify(addUnamangedVmsCommand, times(0)).addExternallyManagedVm(any());
        verify(addUnamangedVmsCommand).importHostedEngineVm(argThat((VM argument) -> {
            VmStatic vmStatic = argument.getStaticData();
            if (vmStatic.getNumOfSockets() != 4 ||
                    vmStatic.getMemSizeMb() != 7052 ||
                    vmStatic.getManagedDeviceMap().size() != 2) {
                return false;
            }
            for (VmDevice vmDevice : vmStatic.getManagedDeviceMap().values()) {
                if (vmDevice instanceof GraphicsDevice) {
                   GraphicsDevice device = (GraphicsDevice) vmDevice;
                   if (device.getGraphicsType() != GraphicsType.VNC ||
                           vmStatic.getDefaultDisplayType() != DisplayType.vga) {
                       return false;
                   }
                }
                else{
                    if (vmDevice.getType() != VmDeviceGeneralType.CONSOLE) {
                        return false;
                    }
                }

            }

            return true;
        }));
    }
}
