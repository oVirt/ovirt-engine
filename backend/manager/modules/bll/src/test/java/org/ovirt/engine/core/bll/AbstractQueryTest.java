package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;

import org.junit.Before;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class AbstractQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> {

    private P params;
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
    }

    /** Sets up a mock for {@link #query} */
    private void setUpSpyQuery() throws Exception {
        Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType());
        query = spy(con.newInstance(getQueryParameters()));
    }

    /** Extract the {@link Class} object for the P generic parameter */
    @SuppressWarnings("unchecked")
    private Class<? extends P> getParameterType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<? extends P>) parameterizedType.getActualTypeArguments()[0];
    }

    /** Extract the {@link Class} object for the Q generic parameter */
    @SuppressWarnings("unchecked")
    private Class<? extends Q> getQueryType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        ParameterizedType queryParameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[1];
        return (Class<? extends Q>) queryParameterizedType.getRawType();
    }

    /** Power-Mocks {@link DbFacade#getInstance()} and returns a mock for it */
    protected DbFacade getDbFacadeMockInstance() {
        DbFacade dbFacadeMock = mock(DbFacade.class);
        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacadeMock);
        return dbFacadeMock;
    }

    /** @return The spied query to use in the test */
    protected Q getQuery() {
        return query;
    }

    /** @return The mock query parameters to use in the test */
    protected P getQueryParameters() {
        return params;
    }
}
