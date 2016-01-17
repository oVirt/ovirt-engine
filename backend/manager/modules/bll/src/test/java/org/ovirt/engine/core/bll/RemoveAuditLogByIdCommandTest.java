package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.AuditLogDao;

public class RemoveAuditLogByIdCommandTest extends BaseCommandTest {

    RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters> command;

    @Mock
    private AuditLogDao auditLogDao;

    private static final String OVIRT_ORIGIN = "oVirt";
    private static final String EXTERNAL_ORIGIN = "External";

    private static final long EVENT_ID_1 = 101;
    private static final long EVENT_ID_2 = 102;
    private static final long EVENT_ID_3 = 103;

    private void prepareMocks(RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters> command) {
        doReturn(auditLogDao).when(command).getAuditLogDao();
        doReturn(null).when(auditLogDao).get(EVENT_ID_1);
        doReturn(getEventWithOvirtOrigin()).when(auditLogDao).get(EVENT_ID_2);
        doReturn(getEventWithExternalOrigin()).when(auditLogDao).get(EVENT_ID_3);
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
    public void validateFailsOnNonExistingEvent() {
        command =
                spy(new RemoveAuditLogByIdCommand<>(new RemoveAuditLogByIdParameters(EVENT_ID_1), null));
        prepareMocks(command);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
    }

    @Test
    public void validateSucceeds() {
        command =
                spy(new RemoveAuditLogByIdCommand<>(new RemoveAuditLogByIdParameters(EVENT_ID_3), null));
        prepareMocks(command);
        assertTrue(command.validate());
    }

}
