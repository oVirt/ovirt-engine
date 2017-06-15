package org.ovirt.engine.core.bll.network.vm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.ovirt.engine.core.common.AuditLogType;

public class AddVmNicFilterParameterCommandTest extends VmNicFilterParameterCommandTest {

    @InjectMocks
    protected AbstractVmNicFilterParameterCommand command = new AddVmNicFilterParameterCommand(param, null);

    @Override
    protected AbstractVmNicFilterParameterCommand getCommand() {
        return command;
    }

    @Test
    public void executionFailure() {
        assertEquals(AuditLogType.NETWORK_ADD_NIC_FILTER_PARAMETER_FAILED, executeFailure());
    }
}
