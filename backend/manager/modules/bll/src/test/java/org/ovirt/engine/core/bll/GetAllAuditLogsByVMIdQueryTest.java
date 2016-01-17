package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AuditLogDao;

/** A test case for the {@link GetAllAuditLogsByVMIdQuery} class. */
public class GetAllAuditLogsByVMIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllAuditLogsByVMIdQuery<? extends IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommand() {
        // Mock the Query Parameters
        Guid vmId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(vmId);

        // Set up the expected result
        AuditLog expectedResult = new AuditLog();
        expectedResult.setVmId(vmId);

        // Mock the Daos
        AuditLogDao auditLogDaoMock = mock(AuditLogDao.class);
        when(auditLogDaoMock.getAllByVMId(vmId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(Collections.singletonList(expectedResult));
        when(getDbFacadeMockInstance().getAuditLogDao()).thenReturn(auditLogDaoMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<AuditLog> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of audit logs in result", 1, result.size());
        assertEquals("Wrong audit log in result", expectedResult, result.get(0));
    }
}
