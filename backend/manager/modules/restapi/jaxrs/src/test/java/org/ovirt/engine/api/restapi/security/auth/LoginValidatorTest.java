package org.ovirt.engine.api.restapi.security.auth;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqActionParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

import java.util.HashMap;
import java.util.Map;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.jboss.resteasy.core.ServerResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.invocation.MetaData;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;

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
    protected String sessionId = Guid.newGuid().toString();

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        backend = control.createMock(BackendLocal.class);
        current = control.createMock(Current.class);
        session = new SessionHelper();
        session.setCurrent(current);
        validator = spy(new LoginValidator());
        doReturn(null).when(validator).getCurrentSession(false);
        validator.setBackend(backend);
        validator.setCurrent(current);
        validator.setSessionHelper(session);
        validator.usePersistentSession(false);
        session.setSessionId(sessionId);
    }

    @After
    public void tearDown() {
        control.verify();
    }

    @Test
    public void testLogin() {
        assertTrue(validator.validate(setUpLoginExpectations(true, true), sessionId));
    }

    @Test
    public void testValidateSessionTrue() {
        setUpValidateSession(true);
        validator.validate(sessionId);
    }

    @Test
    public void testValidateSessionFalse() {
        setUpValidateSession(false);
        validator.validate(sessionId);
    }

    @Test
    public void testLoginCantDo() {
        assertFalse(validator.validate(setUpLoginExpectations(false, false), sessionId));
    }

    @Test
    public void testLoginfailed() {
        assertFalse(validator.validate(setUpLoginExpectations(true, false), sessionId));
    }

    @Test
    public void testLogoff() {
        setUpMetaDataExpectations(null);
        validator.postProcess(setUpLogoutExpectations());
    }

    @Test
    public void testNonBlockingLogoffCancelation() {
        setUpMetaDataExpectations(new HashMap<String, Object>(){{put("async", true);}});
        validator.postProcess(setUpAsyncLogoutExpectations());
    }

    protected void setUpMetaDataExpectations(Map<String, Object> metaItems) {
        MetaData meta = control.createMock(MetaData.class);
        if (metaItems != null && !metaItems.isEmpty()) {
            expect(current.get(MetaData.class)).andReturn(meta).anyTimes();
            for (Map.Entry<String, Object> item : metaItems.entrySet()) {
                expect(meta.hasKey(item.getKey())).andReturn(true);
                expect(meta.get(item.getKey())).andReturn(item.getValue());
            }
        } else {
            expect(current.get(MetaData.class)).andReturn(meta);
        }
    }

    private Principal setUpLoginExpectations(boolean canDo, boolean success) {
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        Principal principal = new Principal(USER, SECRET, DOMAIN);
        expect(
            backend.Login((LoginUserParameters) eqActionParams(LoginUserParameters.class,
                    new String[] { "UserName", "UserPassword", "Domain", "ActionType", "SessionId" },
                    new Object[] { USER, SECRET, DOMAIN, VdcActionType.LoginUser, session.getSessionId() }))).andReturn(result);
        expect(result.getCanDoAction()).andReturn(canDo);
        expect(result.getSucceeded()).andReturn(success).anyTimes();

        VdcUser user = control.createMock(VdcUser.class);
        if (canDo && success) {
            expect(result.getActionReturnValue()).andReturn(user);
            VdcQueryReturnValue appModeResult = new VdcQueryReturnValue();
            appModeResult.setReturnValue(255);
            appModeResult.setSucceeded(true);
            expect(backend.RunPublicQuery(eq(VdcQueryType.GetConfigurationValue),
                    eqQueryParams(GetConfigurationValueParameters.class, new String[] { "ConfigValue" },
                            new Object[] { ConfigurationValues.ApplicationMode }))).andReturn(appModeResult);
            current.set(user);
            EasyMock.expectLastCall();
        }
        control.replay();
        return principal;
    }

    private Principal setUpValidateSession(boolean success) {
        VdcQueryReturnValue queryReturnValue = control.createMock(VdcQueryReturnValue.class);
        Principal principal = new Principal(USER, SECRET, DOMAIN);
        VdcUser user = control.createMock(VdcUser.class);
        expect(backend.RunPublicQuery(eq(VdcQueryType.ValidateSession), eqQueryParams(VdcQueryParametersBase.class,
                new String[] { "SessionId" },
                new Object[] { sessionId }) )).andReturn(queryReturnValue);
        expect(queryReturnValue.getSucceeded()).andReturn(success).anyTimes();
        if (success) {
            expect(queryReturnValue.getReturnValue()).andReturn(user);
            current.set(user);
            EasyMock.expectLastCall();
        }
        control.replay();
        return principal;
    }

    private ServerResponse setUpLogoutExpectations() {
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        VdcUser user = control.createMock(VdcUser.class);
        expect(current.get(VdcUser.class)).andReturn(user);
        expect(user.getUserId()).andReturn(GUID);
        expect(
            backend.Logoff((LogoutUserParameters) eqActionParams(LogoutUserParameters.class,
                    new String[] { "UserId", "SessionId" },
                    new Object[] { GUID, session.getSessionId() }))).andReturn(result);
        ServerResponse response = control.createMock(ServerResponse.class);
        control.replay();
        return response;
    }

    private ServerResponse setUpAsyncLogoutExpectations() {
        ServerResponse response = control.createMock(ServerResponse.class);
        control.replay();
        return response;
    }
}
