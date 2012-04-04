package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

public class ValidateSessionQueryTest extends AbstractQueryTest<VdcQueryParametersBase, ValidateSessionQuery<VdcQueryParametersBase>> {

    @Test
    public void testSuccessfulSessionId() {
        VdcQueryParametersBase params = new VdcQueryParametersBase();;
        params.setHttpSessionId("good_session_id");

        ValidateSessionQuery<VdcQueryParametersBase> query = spy(new ValidateSessionQuery<VdcQueryParametersBase>(params));
        when(query.getSessionUser("good_session_id")).thenReturn(new VdcUser(Guid.NewGuid(), "myUser", "myDomain"));
        query.Execute();
        assertTrue(query.getQueryReturnValue().getSucceeded());
        assertTrue(query.getQueryReturnValue().getReturnValue() instanceof VdcUser);
        VdcUser user = (VdcUser) query.getQueryReturnValue().getReturnValue();
        assertTrue(user.getDomainControler().equals("myDomain"));
        assertTrue(user.getUserName().equals("myUser"));
    }

    @Test
    public void testUnsuccessfulSessionId() {
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setHttpSessionId("bad_session_id");

        ValidateSessionQuery<VdcQueryParametersBase> query = new ValidateSessionQuery<VdcQueryParametersBase>(params);
        query.Execute();
        assertFalse(query.getQueryReturnValue().getSucceeded());
    }

}
