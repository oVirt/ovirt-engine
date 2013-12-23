package org.ovirt.engine.core.bll.scheduling.commands;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigRule;

public class ClusterPolicyCRUDCommandTest {

    @Rule
    public MockConfigRule mockConfigRule =
            new MockConfigRule((MockConfigRule.mockConfig(ConfigValues.EnableVdsLoadBalancing, false)),
                    (MockConfigRule.mockConfig(ConfigValues.EnableVdsHaReservation, false)));

    @Test
    public void testCheckAddEditValidations() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy)) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        Assert.assertTrue(command.checkAddEditValidations());
    }

    @Test
    public void testCheckAddEditValidationsFailOnParameters() {
        Guid clusterPolicyId = new Guid("e754440b-76a6-4099-8235-4565ab4b5521");
        ClusterPolicy clusterPolicy = new ClusterPolicy();
        clusterPolicy.setId(clusterPolicyId);
        HashMap<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put("fail?", "sure, fail!");
        clusterPolicy.setParameterMap(parameterMap);
        ClusterPolicyCRUDCommand command =
                new ClusterPolicyCRUDCommand(new ClusterPolicyCRUDParameters(clusterPolicyId,
                        clusterPolicy)) {

                    @Override
                    protected void executeCommand() {
                    }
                };
        Assert.assertFalse(command.checkAddEditValidations());
    }

}
