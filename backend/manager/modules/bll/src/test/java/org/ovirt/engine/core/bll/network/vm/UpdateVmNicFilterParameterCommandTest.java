package org.ovirt.engine.core.bll.network.vm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.errors.EngineMessage;

@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateVmNicFilterParameterCommandTest extends VmNicFilterParameterCommandTest {

    @InjectMocks
    protected AbstractVmNicFilterParameterCommand command = new UpdateVmNicFilterParameterCommand(param, null);

    @Override
    protected AbstractVmNicFilterParameterCommand getCommand() {
        return command;
    }

    @Test
    public void notIdIsValid() {
        validId(false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS);
    }

    @Test
    public void executionFailure() {
        assertEquals(AuditLogType.NETWORK_UPDATE_NIC_FILTER_PARAMETER_FAILED, executeFailure());
    }

}
