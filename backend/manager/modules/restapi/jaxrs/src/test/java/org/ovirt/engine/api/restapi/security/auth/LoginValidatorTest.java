package org.ovirt.engine.api.restapi.security.auth;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.util.SessionHelper;

import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqActionParams;
import static org.easymock.classextension.EasyMock.expect;

public class LoginValidatorTest extends Assert {

    private static final String USER = "Aladdin";
    private static final Guid GUID = new Guid("9b9002d1-ec33-4083-8a7b-31f6b8931648");
    private static final String SECRET = "open sesame";
    private static final String DOMAIN = "Maghreb";

    protected BackendLocal backend;
    protected Current current;
    protected LoginValidator validator;
    protected SessionHelper session;
    protected IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        backend = control.createMock(BackendLocal.class);
        current = control.createMock(Current.class);
        session = new SessionHelper();
        session.setCurrent(current);
        validator = new LoginValidator();
        validator.setBackend(backend);
        validator.setCurrent(current);
        validator.setSessionHelper(session);
    }

    @After
    public void tearDown() {
        control.verify();
    }

    @Test
    public void testLogin() {
        assertTrue(validator.validate(setUpLoginExpectations(true, true)));
    }

    @Test
    public void testLoginCantDo() {
        assertFalse(validator.validate(setUpLoginExpectations(false, false)));
    }

    @Test
    public void testLoginfailed() {
        assertFalse(validator.validate(setUpLoginExpectations(true, false)));
    }

    @Test
    public void testLogoff() {
        validator.postProcess(setUpLogoutExpectations());
    }

    private Principal setUpLoginExpectations(boolean canDo, boolean success) {
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        Principal principal = new Principal(USER, SECRET, DOMAIN);
        expect(
            backend.Login((LoginUserParameters) eqActionParams(LoginUserParameters.class,
                    new String[] { "UserName", "UserPassword", "Domain", "ActionType", "SessionId" },
                    new Object[] { USER, SECRET, DOMAIN, VdcActionType.LoginAdminUser, session.getSessionId(principal) }))).andReturn(result);
        expect(result.getCanDoAction()).andReturn(canDo);
        expect(result.getSucceeded()).andReturn(success).anyTimes();
        VdcUser user = control.createMock(VdcUser.class);
        if (canDo && success) {
            expect(result.getActionReturnValue()).andReturn(user);
            current.set(user);
            EasyMock.expectLastCall();
        }
        control.replay();
        return principal;
    }

    private ServerResponse setUpLogoutExpectations() {
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        Principal principal = new Principal(USER, SECRET, DOMAIN);
        expect(current.get(Principal.class)).andReturn(principal);
        VdcUser user = control.createMock(VdcUser.class);
        expect(current.get(VdcUser.class)).andReturn(user);
        expect(user.getUserId()).andReturn(GUID);
        expect(
            backend.Logoff((LogoutUserParameters) eqActionParams(LogoutUserParameters.class,
                    new String[] { "UserId", "SessionId" },
                    new Object[] { GUID, session.getSessionId(principal) }))).andReturn(result);
        ServerResponse response = control.createMock(ServerResponse.class);
        control.replay();
        return response;
    }

}
