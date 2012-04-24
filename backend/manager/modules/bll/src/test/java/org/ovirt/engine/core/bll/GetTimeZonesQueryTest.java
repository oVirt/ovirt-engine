package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class })
public class GetTimeZonesQueryTest extends AbstractQueryTest<VdcQueryParametersBase, GetTimeZonesQuery<VdcQueryParametersBase>> {

    @Test
    public void testExecuteQuery() {
        // Mock away the static initializer
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.AdUserName)).thenReturn("");

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong number of time zones", SysprepHandler.timeZoneIndex.size(), result.size());

    }
}
