package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

/** A test case for the {@link FreezeVmCommand} class. */
public class FreezeVmCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private FreezeVmCommand<VmOperationParameterBase> cmd =
            new FreezeVmCommand<>(new VmOperationParameterBase(Guid.newGuid()), null);

    @Mock
    private VDSBrokerFrontend vdsBroker;

    @Mock
    private AuditLogDirector auditLogDirector;

    private VDSReturnValue vdsReturnValue(boolean succeeded) {
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setSucceeded(succeeded);
        return returnValue;
    }

    @Test
    public void testPerformLogsFreezeInitiatedAndSucceeds() {
        when(vdsBroker.runVdsCommand(eq(VDSCommandType.Freeze), any())).thenReturn(vdsReturnValue(true));

        cmd.perform();

        verify(auditLogDirector).log(any(), eq(AuditLogType.FREEZE_VM_INITIATED));
        assertTrue(cmd.getReturnValue().getSucceeded());
    }

    @Test
    public void testPerformLogsFreezeInitiatedOnFailureToo() {
        when(vdsBroker.runVdsCommand(eq(VDSCommandType.Freeze), any())).thenReturn(vdsReturnValue(false));

        cmd.perform();

        verify(auditLogDirector).log(any(), eq(AuditLogType.FREEZE_VM_INITIATED));
        assertFalse(cmd.getReturnValue().getSucceeded());
    }
}
