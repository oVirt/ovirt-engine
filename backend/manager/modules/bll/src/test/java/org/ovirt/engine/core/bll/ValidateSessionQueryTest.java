package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class ValidateSessionQueryTest extends AbstractUserQueryTest<VdcQueryParametersBase, ValidateSessionQuery<VdcQueryParametersBase>> {

    @Test
    public void testSuccessfulSessionId() {
        when(params.getSessionId()).thenReturn("good_session_id");

        when(getQuery().getSessionUser("good_session_id")).thenReturn(new VdcUser(Guid.NewGuid(), "myUser", "myDomain"));
        getQuery().execute();
        assertTrue(getQuery().getQueryReturnValue().getSucceeded());
        assertTrue(getQuery().getQueryReturnValue().getReturnValue() instanceof VdcUser);
        VdcUser user = (VdcUser) getQuery().getQueryReturnValue().getReturnValue();
        assertTrue(user.getDomainControler().equals("myDomain"));
        assertTrue(user.getUserName().equals("myUser"));
    }

    @Test
    public void testUnsuccessfulSessionId() {
        when(params.getSessionId()).thenReturn("bad_session_id");

        getQuery().execute();
        assertFalse(getQuery().getQueryReturnValue().getSucceeded());
    }

}
