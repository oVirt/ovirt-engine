package org.ovirt.engine.core.bll.scheduling.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;

public class ClusterPolicyCRUDCommandTest extends BaseCommandTest {

    @Rule
    public MockConfigRule mockConfigRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.EnableVdsLoadBalancing, false));

    @Test
    public void testCheckAddEditValidations() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy), null) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        command.schedulingManager = mockScheduler();
        Assert.assertTrue(command.checkAddEditValidations());
    }

    @Test
    public void testCheckAddEditValidationsFailOnParameters() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        HashMap<String, String> parameterMap = new HashMap<>();
        parameterMap.put("fail?", "sure, fail!");
        clusterPolicy.setParameterMap(parameterMap);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy), null) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        command.schedulingManager = mockScheduler();
        Assert.assertFalse(command.checkAddEditValidations());
    }

    private SchedulingManager mockScheduler() {
        SchedulingManager mock = mock(SchedulingManager.class);
        when(mock.getClusterPolicies()).thenReturn(Collections.<ClusterPolicy>emptyList());
        return mock;
    }

}
