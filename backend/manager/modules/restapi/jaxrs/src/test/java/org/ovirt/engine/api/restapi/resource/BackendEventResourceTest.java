package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.Date;

import org.junit.Test;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveAuditLogByIdParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendEventResourceTest extends AbstractBackendSubResourceTest<Event, AuditLog, BackendEventResource> {
    private static final long[] LOG_IDS = { 1 };

    public BackendEventResourceTest() {
        super(new BackendEventResource(String.valueOf(LOG_IDS[0])));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveAuditLogById,
                RemoveAuditLogByIdParameters.class,
                new String[] { "AuditLogId" },
                new Object[] { LOG_IDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetAuditLogById,
            GetAuditLogByIdParameters.class,
            new String[] { "Id" },
            new Object[] { LOG_IDS[0] },
            getEntity(0)
        );
    }

    @Override
    protected AuditLog getEntity(int index) {
        AuditLog mock = control.createMock(AuditLog.class);
        expect(mock.getAuditLogId()).andReturn(LOG_IDS[index]).anyTimes();
        expect(mock.getLogType()).andReturn(AuditLogType.EXTERNAL_ALERT).anyTimes();
        expect(mock.getSeverity()).andReturn(AuditLogSeverity.ALERT).anyTimes();
        expect(mock.getLogTime()).andReturn(new Date()).anyTimes();
        return mock;
    }
}
