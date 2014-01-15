package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@RunWith(MockitoJUnitRunner.class)
public class GetWatchdogQueryTest extends AbstractQueryTest<IdQueryParameters, GetWatchdogQuery<IdQueryParameters>> {

    @Mock
    VmDeviceDAO vmDeviceDAO;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Mockito.when(getDbFacadeMockInstance().getVmDeviceDao()).thenReturn(vmDeviceDAO);
    }

    @Test
    public void executeQueryCommandWithNull() {
        Mockito.when(getQueryParameters().getId()).thenReturn(new Guid("ee655a4d-effc-4aab-be2b-2f80ff40cd1c"));
        getQuery().executeQueryCommand();
        Assert.assertTrue(((List<?>) getQuery().getQueryReturnValue().getReturnValue()).isEmpty());
    }

    @Test
    public void executeQueryCommandWithWatchdog() {
        final Guid vmId = new Guid("ee655a4d-effc-4aab-be2b-2f80ff40cd1c");
        HashMap<String, Object> watchdogSpecParams = new HashMap<String, Object>();
        watchdogSpecParams.put("model", "i6300esb");
        watchdogSpecParams.put("action", "reset");
        VmDevice vmDevice = new VmDevice(new VmDeviceId(new Guid("6f86b8a4-e721-4149-b2df-056eb621b16a"),
                vmId), VmDeviceGeneralType.WATCHDOG, VmDeviceType.WATCHDOG.getName(), "", 1, watchdogSpecParams, true,
                true, true, "", null, null);
        Mockito.when(vmDeviceDAO.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.WATCHDOG))
                .thenReturn(Arrays.asList(vmDevice));
        Mockito.when(getQueryParameters().getId()).thenReturn(vmId);

        getQuery().executeQueryCommand();

        List<VmWatchdog> result = getQuery().getQueryReturnValue().getReturnValue();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        VmWatchdog watchdog = result.get(0);
        Assert.assertEquals(watchdog.getAction().name().toLowerCase(), "reset");
        Assert.assertEquals(watchdog.getModel().name().toLowerCase(), "i6300esb");
    }

}
