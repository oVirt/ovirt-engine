package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetVmCustomPropertiesQueryTest extends AbstractQueryTest<VdcQueryParametersBase, GetVmCustomPropertiesQuery<VdcQueryParametersBase>> {

    /** Tests the query if both predefined and user properties exist */
    @Test
    public void testExecuteQueryCommandPredefinedAndUserDefinedProperties() {
        String predefinedProperties = RandomUtils.instance().nextString(10);
        String userProperties = RandomUtils.instance().nextString(10);

        String expectedResult = predefinedProperties + ";" + userProperties;

        assertExecuteQueryCommand(predefinedProperties, userProperties, expectedResult);
    }

    @Test
    public void testExecuteQueryCommandPredefinedPropertiesOnly() {
        String predefinedProperties = RandomUtils.instance().nextString(10);
        assertExecuteQueryCommand(predefinedProperties, "", predefinedProperties);
    }

    @Test
    public void testExecuteQueryCommandUserDefinedPropertiesOnly() {
        String userDefinedProperties = RandomUtils.instance().nextString(10);
        assertExecuteQueryCommand("", userDefinedProperties, userDefinedProperties);
    }

    @Test
    public void testExecuteQueryCommandUserNoProperties() {
        assertExecuteQueryCommand("", "", "");
    }

    /**
     * Asserts that the query returns the correct properties string
     *
     * @param predefinedProperties The predefined VM properties to be spied
     * @param userProperties The user defined VM properties to be spied
     * @param expectedResult The expected result of the query
     */
    private void assertExecuteQueryCommand(String predefinedProperties, String userProperties, String expectedResult) {
        // Spy the configuration
        doReturn(predefinedProperties).when(getQuery()).getPredefinedVMProperties();
        doReturn(userProperties).when(getQuery()).getUserDefinedVMProperties();

        getQuery().executeQueryCommand();

        assertEquals("Wrong properties string",
                expectedResult,
                getQuery().getQueryReturnValue().getReturnValue());
    }
}
