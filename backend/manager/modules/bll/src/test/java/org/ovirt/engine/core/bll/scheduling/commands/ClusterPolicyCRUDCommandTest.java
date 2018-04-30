package org.ovirt.engine.core.bll.scheduling.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class ClusterPolicyCRUDCommandTest extends BaseCommandTest {
    @Mock
    private SchedulingManager schedulingManager;

    private ClusterPolicyCRUDParameters params = new ClusterPolicyCRUDParameters(Guid.newGuid(), new ClusterPolicy());

    @InjectMocks
    private ClusterPolicyCRUDCommand command = new ClusterPolicyCRUDCommand(params, null) {
        @Override
        protected void executeCommand() {
            // Do Nothing
        }
    };

    @Test
    public void testCheckAddEditValidations() {
        assertTrue(command.checkAddEditValidations());
    }

    @Test
    public void testCheckAddEditValidationsFailOnParameters() {
        params.getClusterPolicy().getParameterMap().put("fail?", "sure, fail!");
        assertFalse(command.checkAddEditValidations());
    }
}
