package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public abstract class AbstractQueryTest<P extends QueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends BaseCommandTest {

    @Mock (answer = RETURNS_DEEP_STUBS)
    protected DbUser dbUserMock;

    protected P params = createMockQueryParameters();

    @Spy
    @InjectMocks
    private Q query = createQuery();

    /** Sets up a mock user a spy query with it, and the generic query parameters */
    @BeforeEach
    public void setUp() throws Exception {
        initQuery(getQuery());
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.UserSessionTimeOutInterval, 30));
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
            Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType(), EngineContext.class);
            return con.newInstance(getQueryParameters(), null);
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
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void testQueryType() throws IllegalArgumentException, IllegalAccessException {
        assertNotSame(QueryType.Unknown, TestHelperQueriesCommandType.getQueryTypeFieldValue(query),
                "The query can't be found in the enum QueryType");
    }
}
