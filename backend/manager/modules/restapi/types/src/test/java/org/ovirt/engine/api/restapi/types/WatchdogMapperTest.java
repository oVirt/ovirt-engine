package org.ovirt.engine.api.restapi.types;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.compat.Guid;

public class WatchdogMapperTest {
    @Test
    public void mapVmWatchdog() {
        VmWatchdog entity = new VmWatchdog();
        entity.setAction(VmWatchdogAction.RESET);
        entity.setModel(VmWatchdogType.i6300esb);
        entity.setId(Guid.Empty);
        Watchdog model = WatchdogMapper.map(entity, null);
        Assert.assertNotNull(model);
        Assert.assertEquals(model.getAction(), WatchdogAction.RESET);
        Assert.assertEquals(model.getModel(), WatchdogModel.I6300ESB);
    }

    @Test
    public void mapWatchdog() {
        Watchdog model = new Watchdog();
        model.setAction(WatchdogAction.RESET);
        model.setModel(WatchdogModel.I6300ESB);
        model.setId(Guid.Empty.toString());
        VmWatchdog entity = WatchdogMapper.map(model, null);
        Assert.assertNotNull(entity);
        Assert.assertEquals(entity.getAction(), VmWatchdogAction.RESET);
        Assert.assertEquals(entity.getModel(), VmWatchdogType.i6300esb);
    }

    @Test
    public void mapModel() {
        for (WatchdogModel model : WatchdogModel.values()) {
            VmWatchdogType backendModel = WatchdogMapper.map(model);
            Assert.assertNotNull(backendModel);
            Assert.assertEquals(backendModel.name().toLowerCase(), model.name().toLowerCase());
        }
    }

    @Test
    public void mapBackendModel() {
        for (VmWatchdogType type : VmWatchdogType.values()) {
            WatchdogModel model = WatchdogMapper.map(type);
            Assert.assertNotNull(model);
            Assert.assertEquals(model.name().toLowerCase(), type.name().toLowerCase());
        }
    }

    @Test
    public void mapAction() {
        for (WatchdogAction action : WatchdogAction.values()) {
            VmWatchdogAction backendAction = WatchdogMapper.map(action);
            Assert.assertNotNull(backendAction);
            Assert.assertEquals(backendAction.name().toLowerCase(), action.name().toLowerCase());
        }
    }

    @Test
    public void mapBackendAction() {
        for (VmWatchdogAction action : VmWatchdogAction.values()) {
            WatchdogAction apiAction = WatchdogMapper.map(action);
            Assert.assertNotNull(apiAction);
            Assert.assertEquals(apiAction.name().toLowerCase(), action.name().toLowerCase());
        }
    }
}
