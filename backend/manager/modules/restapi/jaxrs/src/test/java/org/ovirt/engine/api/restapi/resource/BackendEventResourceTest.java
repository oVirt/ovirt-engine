package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendEventResourceTest extends AbstractBackendSubResourceTest<Event, AuditLog, BackendEventResource> {
    private static final long[] LOG_IDS = { 1 };

    public BackendEventResourceTest() {
        super(new BackendEventResource(String.valueOf(LOG_IDS[0])));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations();
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveAuditLogById,
                RemoveAuditLogByIdParameters.class,
                new String[] { "AuditLogId" },
                new Object[] { LOG_IDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    private void setUpGetEntityExpectations() {
        setUpGetEntityExpectations(
            QueryType.GetAuditLogById,
            GetAuditLogByIdParameters.class,
            new String[] { "Id" },
            new Object[] { LOG_IDS[0] },
            getEntity(0)
        );
    }

    @Override
    protected AuditLog getEntity(int index) {
        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.getAuditLogId()).thenReturn(LOG_IDS[index]);
        when(auditLog.getLogType()).thenReturn(AuditLogType.EXTERNAL_ALERT);
        when(auditLog.getSeverity()).thenReturn(AuditLogSeverity.ALERT);
        when(auditLog.getLogTime()).thenReturn(new Date());
        return auditLog;
    }
}
