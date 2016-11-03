package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.MockConfigDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.utils.MockConfigRule;

public abstract class AbstractQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends BaseCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule();

    @Mock (answer = RETURNS_DEEP_STUBS)
    protected DbUser dbUserMock;

    protected P params = createMockQueryParameters();

    @Spy
    @InjectMocks
    private Q query = createQuery();

    /** Sets up a mock user a spy query with it, and the generic query parameters */
    @Before
    public void setUp() throws Exception {
        initQuery(getQuery());
        for (MockConfigDescriptor<?> mcd : getExtraConfigDescriptors()) {
            mcr.mockConfigValue(mcd);
        }
    }

    protected <T> Set<MockConfigDescriptor<T>> getExtraConfigDescriptors() {
        return Collections.emptySet();
    }

    /** Sets up a mock for {@link #params} */
    private P createMockQueryParameters() {
        P params = mock(getParameterType());
        when(params.getSessionId()).thenReturn("test");
        when(params.getRefresh()).thenReturn(true);
        return params;
    }

    private Q createQuery() {
        try {
            Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType());
            return con.newInstance(getQueryParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Hook for initialization */
    protected void initQuery(Q query) {
        sessionDataContainer.setUser(query.getParameters().getSessionId(), dbUserMock);
        query.postConstruct();
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

    /** @return The spied query to use in the test */
    protected Q getQuery() {
        return query;
    }

    /** @return The mock query parameters to use in the test */
    protected P getQueryParameters() {
        return params;
    }

    /** @return The mocked user to use in the test */
    protected DbUser getUser() {
        return dbUserMock;
    }

    @Test
    public void testQueryType() throws IllegalArgumentException, IllegalAccessException {
        assertNotSame("The query can't be found in the enum VdcQueryType",
                VdcQueryType.Unknown,
                TestHelperQueriesCommandType.getQueryTypeFieldValue(query));
    }
}
