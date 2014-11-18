package org.ovirt.engine.core.bll;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.EngineSessionDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class AbstractQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.UserSessionTimeOutInterval, 60));

    protected P params;
    private Q query;

    /** Sets up a mock user a spy query with it, and the generic query parameters */
    @Before
    public void setUp() throws Exception {
        setUpMockQueryParameters();
        setUpSpyQuery();
    }

    /** Sets up a mock for {@link #params} */
    private void setUpMockQueryParameters() {
        params = mock(getParameterType());
        when(params.getSessionId()).thenReturn("test");
        when(params.getRefresh()).thenReturn(true);
    }

    /** Sets up a mock for {@link #query} */
    protected void setUpSpyQuery() throws Exception {
        setUpSpyQuery(getQueryParameters());
    }

    protected Q setUpSpyQuery(P parameters) throws Exception {
        DbFacade dbFacadeMock = mock(DbFacade.class);
        DbUser dbUserMock = mock(DbUser.class);

        EngineSessionDAO engineSessionDAOMock = mock(EngineSessionDAO.class);
        when(engineSessionDAOMock.save(any(EngineSession.class))).thenReturn(RandomUtils.instance().nextLong());
        when(engineSessionDAOMock.remove(any(Long.class))).thenReturn(1);
        when(dbFacadeMock.getEngineSessionDao()).thenReturn(engineSessionDAOMock);

        PermissionDAO permissionsDAOMock = mock(PermissionDAO.class);
        when(permissionsDAOMock.getAllForEntity(any(Guid.class), any(Long.class), any(Boolean.class))).thenReturn(new ArrayList<Permissions>());
        when(dbFacadeMock.getPermissionDao()).thenReturn(permissionsDAOMock);

        SessionDataContainer.getInstance().setDbFacade(dbFacadeMock);

        SessionDataContainer.getInstance().setUser(parameters.getSessionId(), dbUserMock);

        Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType());
        query = spy(con.newInstance(parameters));
        doReturn(dbFacadeMock).when(query).getDbFacade();
        doReturn(dbUserMock).when(query).initUser();
        return query;
    }

    /** Extract the {@link Class} object for the P generic parameter */
    @SuppressWarnings("unchecked")
    protected Class<? extends P> getParameterType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<? extends P>) parameterizedType.getActualTypeArguments()[0];
    }

    /** Extract the {@link Class} object for the Q generic parameter */
    @SuppressWarnings("unchecked")
    protected Class<? extends Q> getQueryType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        ParameterizedType queryParameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[1];
        return (Class<? extends Q>) queryParameterizedType.getRawType();
    }

    /** Power-Mocks {@link DbFacade#getInstance()} and returns a mock for it */
    protected DbFacade getDbFacadeMockInstance() {
        return getQuery().getDbFacade();
    }

    /** @return The spied query to use in the test */
    protected Q getQuery() {
        return query;
    }

    /** @return The mock query parameters to use in the test */
    protected P getQueryParameters() {
        return params;
    }

    @Test
    public void testQueryType() throws IllegalArgumentException, IllegalAccessException {
        assertNotSame("The query can't be found in the enum VdcQueryType",
                VdcQueryType.Unknown,
                TestHelperQueriesCommandType.getQueryTypeFieldValue(query));
    }
}
