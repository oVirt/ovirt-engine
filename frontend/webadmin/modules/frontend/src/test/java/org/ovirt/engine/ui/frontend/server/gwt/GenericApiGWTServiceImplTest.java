package org.ovirt.engine.ui.frontend.server.gwt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@ExtendWith(MockitoExtension.class)
public class GenericApiGWTServiceImplTest {

    @Mock
    private BackendLocal backendLocal;
    @Mock
    private HttpSession session;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @Spy
    private GenericApiGWTServiceImpl underTest = new GenericApiGWTServiceImpl() {
        // add an instance initializer to insert request and response
        {
            this.perThreadRequest = new InheritableThreadLocal<>();
            this.perThreadResponse = new InheritableThreadLocal<>();
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);
            this.perThreadRequest.set(request);
            this.perThreadResponse.set(response);
        }
    };

    @BeforeEach
    public void setup() {
        underTest.setBackend(backendLocal);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void multiQueryWithNulls() {
        underTest.runMultipleQueries(null, null);
        verifyZeroInteractions(backendLocal);
    }

    @Test
    public void multiQueryWithOddParams() {
        ArrayList<QueryType> queryTypeList = new ArrayList<>(Arrays.asList(
                QueryType.Search));
        ArrayList<QueryParametersBase> queryParamsList = new ArrayList<>(Arrays.asList(
                new QueryParametersBase(),
                new QueryParametersBase()));

        underTest.runMultipleQueries(queryTypeList, queryParamsList);

        verifyZeroInteractions(backendLocal);
    }

    @Test
    public void multiQueryValid() {
        ArrayList<QueryType> queryTypeList = new ArrayList<>(Arrays.asList(
                QueryType.Search,
                QueryType.Search));
        ArrayList<QueryParametersBase> queryParamsList = new ArrayList<>(Arrays.asList(
                new QueryParametersBase(),
                new QueryParametersBase()));

        underTest.runMultipleQueries(queryTypeList, queryParamsList);

        verify(backendLocal, times(2)).runQuery(any(), any());
    }

}
