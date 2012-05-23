package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

public class GetDefualtTimeZoneQueryTest extends AbstractSysprepQueryTest<VdcQueryParametersBase, GetDefualtTimeZoneQuery<VdcQueryParametersBase>> {

    /** The default time zone for the test */
    private static final String DEFAULT_TIME_ZONE = "Israel Standard Time";

    @Test
    public void testExecuteQueryCommand() {
        mcr.mockConfigValue(ConfigValues.DefaultTimeZone, DEFAULT_TIME_ZONE);
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        KeyValuePairCompat<String, String> result =
                (KeyValuePairCompat<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertTrue("Wrong default time zone", result.getValue().equals(DEFAULT_TIME_ZONE));
    }
}
