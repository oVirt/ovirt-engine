package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

public class GetDefualtTimeZoneQueryTest extends AbstractSysprepQueryTest<VdcQueryParametersBase, GetDefualtTimeZoneQuery<VdcQueryParametersBase>> {

    /** The default time zone for the test */
    private static final String DEFAULT_TIME_ZONE = "Israel Standard Time";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(Config.GetValue(ConfigValues.DefaultTimeZone)).thenReturn(DEFAULT_TIME_ZONE);
    }

    @Test
    public void testExecuteQueryCommand() {
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        KeyValuePairCompat<String, String> result =
                (KeyValuePairCompat<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertTrue("Wrong default time zone", result.getValue().equals(DEFAULT_TIME_ZONE));
    }
}
