package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class UpdateWatchdogCommandTest extends BaseCommandTest {

    private VmWatchdogType vmWatchdogType = VmWatchdogType.i6300esb;

    private static final Set<VmWatchdogType> WATCHDOG_MODELS = Collections.singleton(VmWatchdogType.i6300esb);

    @Test
    public void getSpecParams() {
        WatchdogParameters params = new WatchdogParameters();
        params.setAction(VmWatchdogAction.RESET);
        params.setModel(vmWatchdogType);
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params, null);
        HashMap<String, Object> specParams = command.getSpecParams();
        assertNotNull(specParams);
        assertEquals("i6300esb", specParams.get("model"));
        assertEquals("reset", specParams.get("action"));
    }

    @Test
    public void testValidateNoVM() {
        WatchdogParameters params = new WatchdogParameters();
        params.setId(new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db"));
        params.setAction(VmWatchdogAction.PAUSE);
        params.setModel(vmWatchdogType);
        final VmDao vmDaoMock = mock(VmDao.class);
        when(vmDaoMock.get(new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db"))).thenReturn(null);
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params, null) {
            @Override
            public VmDao getVmDao() {
                return vmDaoMock;
            }
        };

        assertFalse(command.validate());
    }

    @Test
    public void testValidate() {
        WatchdogParameters params = new WatchdogParameters();
        Guid vmGuid = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
        params.setId(vmGuid);
        params.setAction(VmWatchdogAction.PAUSE);
        params.setModel(vmWatchdogType);
        final VmDao vmDaoMock = mock(VmDao.class);
        when(vmDaoMock.get(vmGuid)).thenReturn(new VM());
        final VmDeviceDao deviceDao = mock(VmDeviceDao.class);
        when(deviceDao.getVmDeviceByVmIdAndType(vmGuid, VmDeviceGeneralType.WATCHDOG)).thenReturn(Collections.singletonList(new VmDevice()));
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params, null) {
            @Override
            public VmDao getVmDao() {
                return vmDaoMock;
            }

            @Override
            public VmDeviceDao getVmDeviceDao() {
                return deviceDao;
            }
        };

        OsRepository osRepository = mock(OsRepository.class);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        when(osRepository.getVmWatchdogTypes(any(Integer.class), any(Version.class))).thenReturn(WATCHDOG_MODELS);
        VmWatchdog vmWatchdog = spy(new VmWatchdog());
        when(vmWatchdog.getModel()).thenReturn(vmWatchdogType);

        assertTrue(command.validate());
    }

}
