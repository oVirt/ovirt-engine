package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** A test case for the {@link Backend} class */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CommandsFactory.class)
public class BackendTest {

    private String sessionIdToUse;

    @Before
    public void setUp() {
        sessionIdToUse = RandomStringUtils.random(10);
        SessionDataContainer.getInstance().setUser(sessionIdToUse, mock(IVdcUser.class));
    }

    @After
    public void tearDown() {
        SessionDataContainer.getInstance().removeSession(sessionIdToUse);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRunQueryExternal() {
        QueriesCommandBase query = mock(QueriesCommandBase.class);
        PowerMockito.mockStatic(CommandsFactory.class);
        when(CommandsFactory.CreateQueryCommand(any(VdcQueryType.class), any(VdcQueryParametersBase.class))).thenReturn(query);

        VdcQueryParametersBase parameters = mock(VdcQueryParametersBase.class);
        when(parameters.getHttpSessionId()).thenReturn(sessionIdToUse);

        Backend backend = new Backend();

        backend.RunQuery(VdcQueryType.Unknown, parameters);

        verify(query).setInternalExecution(false);
        verify(query).Execute();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRunQueryInternal() {
        QueriesCommandBase query = mock(QueriesCommandBase.class);
        PowerMockito.mockStatic(CommandsFactory.class);
        when(CommandsFactory.CreateQueryCommand(any(VdcQueryType.class), any(VdcQueryParametersBase.class))).thenReturn(query);

        VdcQueryParametersBase parameters = mock(VdcQueryParametersBase.class);
        when(parameters.getHttpSessionId()).thenReturn(sessionIdToUse);

        Backend backend = new Backend();

        backend.runInternalQuery(VdcQueryType.Unknown, parameters);

        verify(query).setInternalExecution(true);
        verify(query).Execute();
    }
}
