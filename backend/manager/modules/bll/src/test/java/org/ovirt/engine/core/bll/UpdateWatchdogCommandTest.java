package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;

public class UpdateWatchdogCommandTest {
    @Test
    public void getSpecParams() {
        WatchdogParameters params = new WatchdogParameters();
        params.setAction(VmWatchdogAction.RESET);
        params.setModel(VmWatchdogType.i6300esb);
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params);
        HashMap<String, Object> specParams = command.getSpecParams();
        Assert.assertNotNull(specParams);
        Assert.assertEquals("i6300esb", specParams.get("model"));
        Assert.assertEquals("reset", specParams.get("action"));
    }

    @Test
    public void testCanDoActionNoVM() {
        WatchdogParameters params = new WatchdogParameters();
        params.setId(new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db"));
        params.setAction(VmWatchdogAction.PAUSE);
        params.setModel(VmWatchdogType.i6300esb);
        final VmDAO vmDaoMock = Mockito.mock(VmDAO.class);
        Mockito.when(vmDaoMock.get(new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db"))).thenReturn(null);
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params) {
            @Override
            public VmDAO getVmDAO() {
                return vmDaoMock;
            }
        };

        Assert.assertFalse(command.canDoAction());
    }

    @Test
    public void testCanDoAction() {
        WatchdogParameters params = new WatchdogParameters();
        Guid vmGuid = new Guid("a09f57b1-5739-4352-bf88-a6f834ed46db");
        params.setId(vmGuid);
        params.setAction(VmWatchdogAction.PAUSE);
        params.setModel(VmWatchdogType.i6300esb);
        final VmDAO vmDaoMock = Mockito.mock(VmDAO.class);
        Mockito.when(vmDaoMock.get(vmGuid)).thenReturn(new VM());
        final VmDeviceDAO deviceDAO = Mockito.mock(VmDeviceDAO.class);
        Mockito.when(deviceDAO.getVmDeviceByVmIdAndType(vmGuid, VmDeviceGeneralType.WATCHDOG)).thenReturn(Collections.singletonList(new VmDevice()));
        UpdateWatchdogCommand command = new UpdateWatchdogCommand(params) {
            @Override
            public VmDAO getVmDAO() {
                return vmDaoMock;
            }

            @Override
            protected VmDeviceDAO getVmDeviceDao() {
                return deviceDAO;
            }
        };

        Assert.assertTrue(command.canDoAction());
    }

}
