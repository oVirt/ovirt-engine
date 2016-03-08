package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.config.ConfigValues;

@RunWith(Parameterized.class)
public class FenceConfigValueHelperTest {

    private FenceConfigValueHelper validator;
    private String fenceAgentMapping;
    private String fenceAgentDefault;
    private String vdsFenceOptionMapping;
    private String vdsFenceType;
    private boolean expectedResult;

    public FenceConfigValueHelperTest(String fenceAgentMapping, String fenceAgentDefault, String vdsFenceOptionMapping, String vdsFenceType, Boolean expectedResult) {

        this.fenceAgentMapping = fenceAgentMapping;
        this.fenceAgentDefault = fenceAgentDefault;
        this.vdsFenceOptionMapping = vdsFenceOptionMapping;
        this.vdsFenceType = vdsFenceType;
        this.expectedResult = expectedResult;
        validator = new FenceConfigValueHelper();
    }

    @Test
    public void validateFenceAgentMappingConfig() {
        assertEquals(expectedResult, validator.validate(ConfigValues.FenceAgentMapping.name(), fenceAgentMapping).isOk());
    }

    @Test
    public void validateFenceAgentDefaultParamsConfig() {
        assertEquals(expectedResult, validator.validate(ConfigValues.FenceAgentDefaultParams.name(), fenceAgentDefault).isOk());
    }

    @Test
    public void validateVdsFenceOptionMappingConfig() {
        assertEquals(expectedResult, validator.validate(ConfigValues.VdsFenceOptionMapping.name(), vdsFenceOptionMapping).isOk());
    }

    @Test
    public void validateVdsFenceTypeConfig() {
        assertEquals(expectedResult, validator.validate(ConfigValues.VdsFenceType.name(), vdsFenceType).isOk());
    }

    @Parameterized.Parameters()
    public static Collection<Object[]> fenceAgentMappingParams() {
        return Arrays.asList(new Object[][]{
                {"agent1=agent2", "key1=val1", "agent1:secure=secure", "agent1", true},
                {"agent1=agent2,agent3=agent4", "key1=val1,flag", "agent1:secure=secure,port=port;agent2:", "agent1,agent2,agent3", true},
                {"agent1", "key1=val1,,flag", "agent1:secure", "agent1,,", false},
                {"agent1=", "key1==val1", "agent1:slot=slot,port", "agent1,,agent2", false},
                {null, null, null, null, false},
                {"", "", "", "", false},
                {" ", " ", " ", " ", false},
        });
    }
}
