package org.ovirt.engine.core.bll.network.vm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.validator.VmNicFilterParameterValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmNicFilterParameterParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmNicFilterParameterCommandTest<T extends VmCommand> extends BaseCommandTest {

    @Mock
    VmNicFilterParameterValidator validator;
    private final Guid vmId = Guid.newGuid();

    private final VmNicFilterParameter vmNicFilterParameter = new VmNicFilterParameter();
    protected final VmNicFilterParameterParameters param = new VmNicFilterParameterParameters(vmId, vmNicFilterParameter);
    private final VM vm = new VM();

    @BeforeEach
    public void setup() {
        setupCommand();
        setAllValidationsValid();
    }

    private void setupCommand() {
        vm.setId(vmId);
        getCommand().setVm(vm);
    }

    protected abstract AbstractVmNicFilterParameterCommand getCommand();

    private void setAllValidationsValid() {
        validId(true);
        validVmInterfaceId(true);
        interfaceExistsOnVm(true);
    }

    protected void validId(boolean isValid) {
        when(validator.parameterHavingIdExists(vmNicFilterParameter.getId())).thenReturn(
                isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NIC_FILTER_PARAMETER_ID_NOT_EXISTS));
    }

    private void validVmInterfaceId(boolean isValid) {
        vmNicFilterParameter.setVmInterfaceId(isValid? Guid.newGuid() : null);
        when(validator.vmInterfaceHavingIdExists(vmNicFilterParameter.getVmInterfaceId())).thenReturn(
                isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE));
    }

    private void interfaceExistsOnVm(boolean isValid) {
        when(validator.vmInterfaceHavingIdExistsOnVmHavingId(vmNicFilterParameter.getVmInterfaceId(), vmId)).
                thenReturn(isValid ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE));
    }

    @Test
    public void validateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(getCommand());
    }

    @Test
    public void noValidVmInterfaceId() {
        validVmInterfaceId(false);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE);
    }

    @Test
    public void interfaceNotExistsOnVm() {
        interfaceExistsOnVm(false);
        ValidateTestUtils.runAndAssertValidateFailure(getCommand(),
                EngineMessage.ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_INTERFACE);
    }

    protected AuditLogType executeFailure() {
        try {
            getCommand().executeVmCommand();
        } catch (Exception expected) {
            // An exception is expected here
        }

        assertFalse(getCommand().getReturnValue().getSucceeded());
        return getCommand().getAuditLogTypeValue();
    }
}
