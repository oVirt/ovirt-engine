package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link GetAllAuditLogsByVMTemplateNameQuery} class. */
public class GetAllAuditLogsByVMTemplateNameQueryTest
        extends AbstractUserQueryTest<NameQueryParameters, GetAllAuditLogsByVMTemplateNameQuery<? extends NameQueryParameters>> {

    @Test
    public void testExecuteQueryCommand() {
        // Mock the Query Parameters
        String vmTemplateName = RandomUtils.instance().nextString(10, true);
        when(getQueryParameters().getName()).thenReturn(vmTemplateName);

        // Set up the expected result
        AuditLog expectedResult = new AuditLog();
        expectedResult.setvm_template_name(vmTemplateName);

        // Mock the DAOs
        AuditLogDAO auditLogDAOMock = mock(AuditLogDAO.class);
        when(auditLogDAOMock.getAllByVMTemplateName(vmTemplateName,
                getUser().getUserId(),
                getQueryParameters().isFiltered())).thenReturn(Collections.singletonList(expectedResult));
        when(getDbFacadeMockInstance().getAuditLogDao()).thenReturn(auditLogDAOMock);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<AuditLog> result = (List<AuditLog>) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of audit logs in result", 1, result.size());
        assertEquals("Wrong audit log in result", expectedResult, result.get(0));
    }
}
