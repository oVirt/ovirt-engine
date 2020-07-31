package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;


public abstract class AbstractBackendCollectionResourceTest<R extends BaseResource, Q /* extends Queryable */, C extends AbstractBackendCollectionResource<R, Q>>
        extends AbstractBackendResourceTest<R, Q> {

    protected static final String QUERY = "name=s* AND id=*0";

    protected C collection;
    protected SearchType searchType;
    protected String prefix;

    protected AbstractBackendCollectionResourceTest(C collection, SearchType searchType,
            String prefix) {
        this.collection = collection;
        this.searchType = searchType;
        this.prefix = prefix;
    }

    protected abstract List<R> getCollection();

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false));
    }
    @Override
    protected void init() {
        initResource(collection);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("", FAILURE);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations("", t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
    }

    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations("", t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubResourceInjection() throws Exception {
        // walk super-interface hierarchy to find non-inherited method annotations
        for (Class<?> resourceInterface : collection.getClass().getInterfaces()) {
            for (Method method : resourceInterface.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Path.class) && isSubResourceLocator(method)) {
                    Object rawSubResource = method.invoke(collection, getSubResourceId());
                    if (rawSubResource instanceof AbstractBackendResource) {
                        AbstractBackendResource<R, Q> subResource = (AbstractBackendResource<R, Q>)rawSubResource;
                        assertNotNull(subResource.getBackend());
                        assertNotNull(subResource.getMappingLocator());
                    }
                }
            }
        }
    }

    protected String getSubResourceId() {
        return GUIDS[3].toString();
    }

    protected boolean isSubResourceLocator(Method method) {
        return method.getName().startsWith("get")
               && method.getName().toLowerCase().endsWith("resource")
               && method.getParameterTypes().length == 1
               && String.class.equals(method.getParameterTypes()[0])
               && method.getReturnType() != null;
    }

    protected void setUriInfo(UriInfo uriInfo) {
        collection.setUriInfo(uriInfo);
    }

    @SuppressWarnings("unchecked")
    protected UriInfo setUpUriExpectations(String query) {
        UriInfo uriInfo = setUpBasicUriExpectations();
        MultivaluedMap<String, String> queries = mock(MultivaluedMap.class);
        if (!(query == null || "".equals(query))) {
            query = QUERY;
        }
        when(queries.containsKey("search")).thenReturn(query != null);
        when(queries.getFirst("search")).thenReturn(query);
        when(uriInfo.getQueryParameters()).thenReturn(queries);
        return uriInfo;
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        QueryReturnValue queryResult = new QueryReturnValue();
        SearchParameters params = new SearchParameters(prefix + query, searchType);
        queryResult.setSucceeded(failure == null);
        if (failure == null) {
            List<Q> entities = new ArrayList<>();
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            queryResult.setReturnValue(entities);
            when(backend.runQuery(eq(QueryType.Search), eqSearchParams(params))).thenReturn(
                    queryResult);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String)failure);
                when(backend.runQuery(eq(QueryType.Search), eqSearchParams(params))).thenReturn(
                        queryResult);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(QueryType.Search), eqSearchParams(params))).thenThrow((Exception) failure);
            }
        }
        enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(QueryType.Search), eqSearchParams(params)));
    }

    protected void setUpCreationExpectations(ActionType task,
                                             Class<? extends ActionParametersBase> taskClass,
                                             String[] taskNames,
                                             Object[] taskValues,
                                             boolean valid,
                                             boolean success,
                                             Object taskReturn,
                                             QueryType query,
                                             Class<? extends QueryParametersBase> queryClass,
                                             String[] queryNames,
                                             Object[] queryValues,
                                             Object queryReturn) {
        setUpCreationExpectations(task,
                                  taskClass,
                                  taskNames,
                                  taskValues,
                                  valid,
                                  success,
                                  taskReturn,
                                  null,
                                  null,
                                  query,
                                  queryClass,
                                  queryNames,
                                  queryValues,
                                  queryReturn);
    }

    protected void setUpCreationExpectations(ActionType task,
                                             Class<? extends ActionParametersBase> taskClass,
                                             String[] taskNames,
                                             Object[] taskValues,
                                             boolean valid,
                                             boolean success,
                                             Object taskReturn,
                                             ArrayList<Guid> asyncTasks,
                                             ArrayList<AsyncTaskStatus> asyncStatuses,
                                             QueryType query,
                                             Class<? extends QueryParametersBase> queryClass,
                                             String[] queryNames,
                                             Object[] queryValues,
                                             Object queryReturn) {
        ActionReturnValue taskResult = new ActionReturnValue();
        taskResult.setValid(valid);
        if (valid) {
            taskResult.setSucceeded(success);
            if (success) {
                taskResult.setActionReturnValue(taskReturn);
            } else {
                taskResult.setExecuteFailedMessages(asList(FAILURE));
                setUpL10nExpectations(asList(FAILURE));
            }
        } else {
            taskResult.setValidationMessages(asList(CANT_DO));
            setUpL10nExpectations(asList(CANT_DO));
        }
        if (asyncTasks != null) {
            taskResult.setVdsmTaskIdList(asyncTasks);
            QueryReturnValue monitorResult = new QueryReturnValue();
            monitorResult.setSucceeded(success);
            monitorResult.setReturnValue(asyncStatuses);
            when(backend.runQuery(eq(QueryType.GetTasksStatusesByTasksIDs),
                                    eqParams(GetTasksStatusesByTasksIDsParameters.class,
                                                  addSession(),
                                                  addSession(new Object[]{})))).thenReturn(monitorResult);
            enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(QueryType.GetTasksStatusesByTasksIDs),
                    eqParams(GetTasksStatusesByTasksIDsParameters.class,
                            addSession(),
                            addSession(new Object[]{}))));
        }
        when(backend.runAction(eq(task), eqParams(taskClass, addSession(taskNames), addSession(taskValues))))
                .thenReturn(taskResult);
        enqueueInteraction(() ->
                verify(backend, atLeastOnce()).runAction(eq(task), eqParams(taskClass, addSession(taskNames), addSession(taskValues))));

        if (valid && success && query != null) {
            setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn);
        }
    }

    protected void setUpHttpHeaderExpectations(String name, String value) {
        when(httpHeaders.getRequestHeader(eq(name))).thenReturn(asList(value));
    }

    protected Cluster setUpCluster(Guid id) {
        Cluster cluster = mock(Cluster.class);
        when(cluster.getId()).thenReturn(id);
        when(cluster.getCompatibilityVersion()).thenReturn(Version.getLast());
        return cluster;
    }

    protected void verifyCollection(List<R> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }

    @Override
    protected void verifyRemove(Response response) {
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    protected void verifyFault(WebApplicationException wae) {
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
    }

    protected static Link getLinkByName(BaseResource model, String name) {
        for (Link link : model.getLinks()) {
            if (link.getRel().equals(name)) {
                return link;
            }
        }
        return null;
    }
}
