package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.InjectedMock;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateWatchdogCommandTest extends BaseCommandTest {

    private VmWatchdogType vmWatchdogType = VmWatchdogType.i6300esb;

    private static final Set<VmWatchdogType> WATCHDOG_MODELS = Collections.singleton(VmWatchdogType.i6300esb);

    @Mock
    private VmDao vmDaoMock;

    @Mock
    private VmDeviceDao vmDeviceDaoMock;

    @Mock
    @InjectedMock
    public OsRepository osRepository;

    @InjectMocks
    private UpdateWatchdogCommand command = new UpdateWatchdogCommand(new WatchdogParameters(), null);

    @BeforeEach
    public void setUpOsRepository() {
        when(osRepository.getVmWatchdogTypes(anyInt(), any())).thenReturn(WATCHDOG_MODELS);
    }

    @Test
    public void getSpecParams() {
        command.getParameters().setAction(VmWatchdogAction.RESET);
        command.getParameters().setModel(vmWatchdogType);
        Map<String, Object> specParams = command.getSpecParams();
        assertNotNull(specParams);
        assertEquals("i6300esb", specParams.get("model"));
        assertEquals("reset", specParams.get("action"));
    }

    @Test
    public void testValidateNoVM() {
        command.getParameters().setId(new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db"));
        command.getParameters().setAction(VmWatchdogAction.PAUSE);
        command.getParameters().setModel(vmWatchdogType);
        assertFalse(command.validate());
    }

    @Test
    public void testValidate() {
        Guid vmGuid = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
        command.getParameters().setId(vmGuid);
        command.getParameters().setAction(VmWatchdogAction.PAUSE);
        command.getParameters().setModel(vmWatchdogType);
        when(vmDaoMock.get(vmGuid)).thenReturn(new VM());
        when(vmDeviceDaoMock.getVmDeviceByVmIdAndType(vmGuid, VmDeviceGeneralType.WATCHDOG)).thenReturn(Collections.singletonList(new VmDevice()));
        VmWatchdog vmWatchdog = spy(new VmWatchdog());
        when(vmWatchdog.getModel()).thenReturn(vmWatchdogType);

        assertTrue(command.validate());
    }
}
