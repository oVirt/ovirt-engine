package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

public class GetProductVersionQueryTest extends AbstractQueryTest<QueryParametersBase, GetProductVersionQuery<QueryParametersBase>> {

    @Test
    @MockedConfig("justRPMVersion")
    public void testExecuteQuery() {
        GetProductVersionQuery<QueryParametersBase> query = getQuery();
        query.executeQueryCommand();
        Object returnValue = query.getQueryReturnValue().getReturnValue();
        verifyVersionEqual(returnValue, 11, 1, 12);
    }

    public static Stream<MockConfigDescriptor<?>> justRPMVersion() {
        return Stream.concat(
                mockConfiguration(),
                Stream.of(MockConfigDescriptor.of(ConfigValues.ProductRPMVersion, "11.1.12asdf."))
        );
    }

    @Test
    @MockedConfig("rpmAndVdcVersion")
    public void testExecuteQueryUseVdcVersion() {
        GetProductVersionQuery<QueryParametersBase> query = getQuery();
        query.executeQueryCommand();
        Object returnValue = query.getQueryReturnValue().getReturnValue();
        verifyVersionEqual(returnValue, 3, 3, 0);
    }

    public static Stream<MockConfigDescriptor<?>> rpmAndVdcVersion() {
        return Stream.concat(
                mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.ProductRPMVersion, "1unparsable1.1.12."),
                        MockConfigDescriptor.of(ConfigValues.VdcVersion, "3.3.0.0")
                )
        );
    }

    private void verifyVersionEqual(Object returnValue, int major, int minor, int build) {
        Version version = (Version) returnValue;
        assertEquals(version.getMajor(), major);
        assertEquals(version.getMinor(), minor);
        assertEquals(version.getBuild(), build);
        assertEquals(0, version.getRevision());
    }
}
