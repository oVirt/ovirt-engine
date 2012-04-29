package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;

import org.junit.Before;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class AbstractQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> {

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
    }

    /** Sets up a mock for {@link #query} */
    protected void setUpSpyQuery() throws Exception {
        setUpSpyQuery(getQueryParameters());
    }

    protected Q setUpSpyQuery(P parameters) throws Exception {
        Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType());
        query = spy(con.newInstance(parameters));
        DbFacade dbFacadeMock = mock(DbFacade.class);
        doReturn(dbFacadeMock).when(query).getDbFacade();
        return query;
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
}
