package org.ovirt.engine.core.bll;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/** A test case for the {@link Backend} class */
public class BackendTest {

    private String sessionIdToUse;
    private Backend backend;
    private QueriesCommandBase<?> query;
    private VdcQueryParametersBase parameters;

    @Before
    public void setUp() {
        sessionIdToUse = RandomStringUtils.random(10);
        SessionDataContainer.getInstance().setUser(sessionIdToUse, mock(DbUser.class));

        parameters = mock(VdcQueryParametersBase.class);
        when(parameters.getHttpSessionId()).thenReturn(sessionIdToUse);

        query = mock(QueriesCommandBase.class);

        backend = spy(new Backend());
        doReturn(query).when(backend).createQueryCommand(any(VdcQueryType.class), any(VdcQueryParametersBase.class), null);
    }

    @After
    public void tearDown() {
        SessionDataContainer.getInstance().removeSessionOnLogout(sessionIdToUse);
    }
}
