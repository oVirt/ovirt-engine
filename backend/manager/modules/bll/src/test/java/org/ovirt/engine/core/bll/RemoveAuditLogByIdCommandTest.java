package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dao.AuditLogDAO;

@RunWith(MockitoJUnitRunner.class)
public class RemoveAuditLogByIdCommandTest {

    RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters> command;

    @Mock
    private AuditLogDAO auditLogDAO;

    private static final String OVIRT_ORIGIN = "oVirt";
    private static final String EXTERNAL_ORIGIN = "External";

    private static final long EVENT_ID_1 = 101;
    private static final long EVENT_ID_2 = 102;
    private static final long EVENT_ID_3 = 103;

    private void prepareMocks(RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters> command) {
        doReturn(auditLogDAO).when(command).getAuditLogDao();
        doReturn(null).when(auditLogDAO).get(EVENT_ID_1);
        doReturn(getEventWithOvirtOrigin()).when(auditLogDAO).get(EVENT_ID_2);
        doReturn(getEventWithExternalOrigin()).when(auditLogDAO).get(EVENT_ID_3);
    }

    private AuditLog getEventWithOvirtOrigin() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditLogId(EVENT_ID_2);
        auditLog.setOrigin(OVIRT_ORIGIN);
        return auditLog;
    }

    private AuditLog getEventWithExternalOrigin() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditLogId(EVENT_ID_3);
        auditLog.setOrigin(EXTERNAL_ORIGIN);
        return auditLog;
    }

    @Test
    public void canDoActionFailsOnNonExistingEvent() {
        command =
                spy(new RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters>(new RemoveAuditLogByIdParameters(EVENT_ID_1)));
        prepareMocks(command);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
    }

    @Test
    public void canDoActionFailsOnRemovingEventWithOvirtOrigin() {
        command =
                spy(new RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters>(new RemoveAuditLogByIdParameters(EVENT_ID_2)));
        prepareMocks(command);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(command,
                VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_EVENT_ILLEGAL_ORIGIN);
    }

    @Test
    public void canDoActionSucceeds() {
        command =
                spy(new RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters>(new RemoveAuditLogByIdParameters(EVENT_ID_3)));
        prepareMocks(command);
        assertTrue(command.canDoAction());
    }

}
