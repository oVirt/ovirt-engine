package org.ovirt.engine.ui.frontend.server.gwt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import com.google.gwt.user.client.rpc.SerializationException;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setup() throws ServletException, IOException, SerializationException {
        underTest.setBackend(backendLocal);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void multiQueryWithNulls() {
        underTest.runMultipleQueries(null, null);
        Mockito.verifyZeroInteractions(backendLocal);
    }

    @Test
    public void multiQueryWithOddParams() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>(Arrays.asList(
                VdcQueryType.Search));
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>(Arrays.asList(
                new VdcQueryParametersBase(),
                new VdcQueryParametersBase()));

        underTest.runMultipleQueries(queryTypeList, queryParamsList);

        Mockito.verifyZeroInteractions(backendLocal);
    }

    @Test
    public void multiQueryValid() {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>(Arrays.asList(
                VdcQueryType.Search,
                VdcQueryType.Search));
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>(Arrays.asList(
                new VdcQueryParametersBase(),
                new VdcQueryParametersBase()));

        underTest.runMultipleQueries(queryTypeList, queryParamsList);

        Mockito.verify(backendLocal, Mockito.times(2)).runQuery(any(VdcQueryType.class),
                any(VdcQueryParametersBase.class));
    }

}
