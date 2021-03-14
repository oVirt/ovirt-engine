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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.AddUnmanagedVmsParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class AddUnmanagedVmsCommandTest {
    @Mock
    private VmStaticDao vmStaticDao;
    @Spy
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    @Spy
    @InjectMocks
    private AddUnmanagedVmsCommand<AddUnmanagedVmsParameters> addUnamangedVmsCommand =
            new AddUnmanagedVmsCommand<>(new AddUnmanagedVmsParameters(), null);

    private static Map<String, Object> externalVm;

    private static Map<String, Object> hostedEngine;

    @BeforeAll
    public static void loadVmData() throws IOException {
        externalVm = loadVm("/external_vm.json");
        hostedEngine = loadVm("/he_vm.json");
    }

    @BeforeEach
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
    public void shouldConvertExternalVm() {
        addUnamangedVmsCommand.convertVm(1, DisplayType.qxl, System.nanoTime(), externalVm);
        verify(addUnamangedVmsCommand).addExternallyManagedVm(argThat(
                vmStatic -> vmStatic.getNumOfSockets() == 4 && vmStatic.getMemSizeMb() == 7052));
    }

    @Test
    public void shouldDetectHostedEngineVM() {
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
                } else {
                    if (vmDevice.getType() != VmDeviceGeneralType.CONSOLE) {
                        return false;
                    }
                }

            }

            return true;
        }));
    }
}
