package org.ovirt.engine.core.bll.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class ValidateSessionQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, ValidateSessionQuery<VdcQueryParametersBase>> {

    @Test
    public void testSuccessfulSessionId() {
        when(params.getSessionId()).thenReturn("good_session_id");

        DbUser result = new DbUser();
        result.setId(Guid.newGuid());
        result.setLoginName("myUser");
        result.setDomain("myDomain");
        when(getQuery().getSessionUser("good_session_id")).thenReturn(result);
        getQuery().execute();
        assertTrue(getQuery().getQueryReturnValue().getSucceeded());
        assertTrue(getQuery().getQueryReturnValue().getReturnValue() instanceof DbUser);
        DbUser user = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("myDomain", user.getDomain());
        assertEquals("myUser", user.getLoginName());
    }

    @Test
    public void testUnsuccessfulSessionId() {
        when(params.getSessionId()).thenReturn("bad_session_id");

        getQuery().execute();
        assertFalse(getQuery().getQueryReturnValue().getSucceeded());
    }

}
