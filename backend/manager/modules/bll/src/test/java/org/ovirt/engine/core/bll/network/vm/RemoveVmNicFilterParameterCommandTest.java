package org.ovirt.engine.core.bll.network.vm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class RemoveVmNicFilterParameterCommandTest {
    private final Guid vmId = Guid.newGuid();
    private final Guid filterParameterId = Guid.newGuid();
    private final RemoveVmNicFilterParameterParameters param = new RemoveVmNicFilterParameterParameters(vmId, filterParameterId);

    @Mock
    VmNicFilterParameterValidator validator;

    @InjectMocks
    RemoveVmNicFilterParameterCommand command = new RemoveVmNicFilterParameterCommand(param, null);

    VM vm = new VM();

    @Before
    public void setup() {
        vm.setId(vmId);
        command.setVm(vm);
        validId(true);
    }

    private void validId(boolean isValid) {
        when(validator.parameterHavingIdExists(filterParameterId)).thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS));
    }

    @Test
    public void invalidId() {
        validId(false);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS);
    }

    @Test
    public void validateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void executionFailure() {
        try {
            command.executeVmCommand();
        } catch (Exception expected) {
            // An exception is expected here
        }

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.NETWORK_REMOVE_NIC_FILTER_PARAMETER_FAILED, command.getAuditLogTypeValue());
    }
}
