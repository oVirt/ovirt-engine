package org.ovirt.engine.core.bll.network.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.AuditLogType;

@MockitoSettings(strictness = Strictness.LENIENT)
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
