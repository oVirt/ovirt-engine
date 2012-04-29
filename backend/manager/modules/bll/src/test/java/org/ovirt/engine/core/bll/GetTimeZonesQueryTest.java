package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetTimeZonesQueryTest extends AbstractSysprepQueryTest<VdcQueryParametersBase, GetTimeZonesQuery<VdcQueryParametersBase>> {

    @Test
    public void testExecuteQuery() {
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong number of time zones", SysprepHandler.timeZoneIndex.size(), result.size());

    }

}
