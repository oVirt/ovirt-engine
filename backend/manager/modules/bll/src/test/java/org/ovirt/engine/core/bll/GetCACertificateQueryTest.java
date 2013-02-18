package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.MockConfigRule;

public class GetCACertificateQueryTest extends
AbstractQueryTest<VdcQueryParametersBase, GetCACertificateQuery<VdcQueryParametersBase>> {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.ConfigDir, "src/test/resources"),
            mockConfig(ConfigValues.CABaseDirectory, "ca"),
            mockConfig(ConfigValues.CACertificatePath, "certs/ca.pem"));

    @Test
    public void testExecuteQuery() {
        getQuery().executeQueryCommand();
        Object result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals ("Wrong text read from ca file", "dummy text for testing", result);
    }

}
