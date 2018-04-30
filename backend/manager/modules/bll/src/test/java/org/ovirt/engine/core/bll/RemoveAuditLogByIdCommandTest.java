package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.AuditLogDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class RemoveAuditLogByIdCommandTest extends BaseCommandTest {

    @InjectMocks
    RemoveAuditLogByIdCommand<RemoveAuditLogByIdParameters> command =
            new RemoveAuditLogByIdCommand<>(new RemoveAuditLogByIdParameters(), null);

    @Mock
    private AuditLogDao auditLogDao;

    private static final String EXTERNAL_ORIGIN = "External";

    private static final long EVENT_ID_1 = 101;
    private static final long EVENT_ID_2 = 102;
    private static final long EVENT_ID_3 = 103;

    @BeforeEach
    public void prepareMocks() {
        doReturn(getEventWithOvirtOrigin()).when(auditLogDao).get(EVENT_ID_2);
        doReturn(getEventWithExternalOrigin()).when(auditLogDao).get(EVENT_ID_3);
    }

    private AuditLog getEventWithOvirtOrigin() {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditLogId(EVENT_ID_2);
        auditLog.setOrigin(AuditLog.OVIRT_ORIGIN);
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
        command.getParameters().setAuditLogId(EVENT_ID_1);
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.AUDIT_LOG_CANNOT_REMOVE_AUDIT_LOG_NOT_EXIST);
    }

    @Test
    public void validateSucceeds() {
        command.getParameters().setAuditLogId(EVENT_ID_3);
        assertTrue(command.validate());
    }

}
