package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@RunWith(MockitoJUnitRunner.class)
public class MigrateVmCommandTest {

    private Guid vmId = Guid.newGuid();

    @Mock
    DbFacade dbFacade;

    @Mock
    VmValidator vmValidator;

    @Spy
    @InjectMocks
    MigrateVmCommand command = new MigrateVmCommand<>(new MigrateVmParameters(false, vmId), null);


    @Before
    public void setUp() {
        VM vm = new VM();
        vm.setId(vmId);
        command.setVm(vm);
    }

    @Test
    public void testValidationFailsWhenVmHasDisksPluggedWithScsiReservation() {
        doReturn(vmValidator).when(command).getVmValidator();
        when(vmValidator.isVmPluggedDiskNotUsingScsiReservation()).
                thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION));

        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION);
    }
}
