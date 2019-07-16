package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class MigrateVmCommandTest {

    private Guid vmId = Guid.newGuid();

    @Mock
    VmValidator vmValidator;

    @Spy
    private MigrateVmCommand<MigrateVmParameters> command = new MigrateVmCommand<>(new MigrateVmParameters(false, vmId), null);


    @BeforeEach
    public void setUp() {
        VM vm = new VM();
        vm.setId(vmId);
        command.setVm(vm);
    }

    @Test
    public void testValidationFailsWhenVmHasDisksPluggedWithScsiReservation() {
        doNothing().when(command).logValidationFailed();
        doReturn(false).when(command).isVmDuringBackup();
        doReturn(vmValidator).when(command).getVmValidator();
        when(vmValidator.isVmPluggedDiskNotUsingScsiReservation()).
                thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION);
    }

    @Test
    public void testValidationFailsWhenVmIsDuringBackup() {
        doNothing().when(command).logValidationFailed();
        doReturn(true).when(command).isVmDuringBackup();
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_IS_DURING_BACKUP);
    }
}
