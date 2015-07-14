package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;

@RunWith(MockitoJUnitRunner.class)
public class GetProductVersionQueryTest extends AbstractQueryTest<VdcQueryParametersBase, GetProductVersionQuery<VdcQueryParametersBase>> {

    @ClassRule
    public static MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule();

    @Test
    public void testExecuteQuery() {
        mcr.mockConfigValue(ConfigValues.ProductRPMVersion, "11.1.12asdf.");
        GetProductVersionQuery<VdcQueryParametersBase> query = getQuery();
        query.executeQueryCommand();
        Object returnValue = query.getQueryReturnValue().getReturnValue();
        verifyVersionEqual(returnValue, 11, 1, 12);
    }

    @Test
    public void testExecuteQueryUseVdcVersion() {
        mcr.mockConfigValue(ConfigValues.ProductRPMVersion, "1unparsable1.1.12.");
        mcr.mockConfigValue(ConfigValues.VdcVersion, "3.3.0.0.");
        GetProductVersionQuery<VdcQueryParametersBase> query = getQuery();
        query.executeQueryCommand();
        Object returnValue = query.getQueryReturnValue().getReturnValue();
        verifyVersionEqual(returnValue, 3, 3, 0);
    }

    private void verifyVersionEqual(Object returnValue, int major, int minor, int build) {
        Version version = (Version) returnValue;
        assertEquals(version.getMajor(), major);
        assertEquals(version.getMinor(), minor);
        assertEquals(version.getBuild(), build);
        assertEquals(version.getRevision(), 0);
    }
}
