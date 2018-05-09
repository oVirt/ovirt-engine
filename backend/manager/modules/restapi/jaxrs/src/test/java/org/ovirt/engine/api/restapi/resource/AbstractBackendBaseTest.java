package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractBackendBaseTest {

    protected static final Guid[] GUIDS = { new Guid("00000000-0000-0000-0000-000000000000"),
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222"),
            new Guid("33333333-3333-3333-3333-333333333333") };

    /**
     * External identifiers used by directory users and groups.
     */
    protected static final String[] EXTERNAL_IDS = {
            DirectoryEntryIdUtils.encode("0"),
            DirectoryEntryIdUtils.encode("1"),
            DirectoryEntryIdUtils.encode("2"),
            DirectoryEntryIdUtils.encode("3"),
    };

    /**
     * External identifier of a non existing user or group.
     */
    protected static final String NON_EXISTANT_EXTERNAL_ID = DirectoryEntryIdUtils.encode("10");

    protected static final Guid EVERYONE = new Guid("EEE00000-0000-0000-0000-123456789EEE");
    protected static final String[] NAMES = { "sedna", "eris", "orcus" };
    protected static final String[] DESCRIPTIONS = { "top notch entity", "a fine example",
            "state of the art" };
    protected static final String URI_ROOT = "http://localhost:8088";
    protected static final String BASE_PATH = "/ovirt-engine/api";
    protected static final String URI_BASE = URI_ROOT + BASE_PATH;
    protected static final String BUNDLE_PATH = "org/ovirt/engine/api/restapi/logging/Messages";

    protected static final String CANT_DO = "circumstances outside our control";
    protected static final String FAILURE = "a fine mess";
    protected static final String BACKEND_FAILED_SERVER_LOCALE = "Hinteres Ende mit Gebietsschema gescheitert";
    protected static final String BACKEND_FAILED_CLIENT_LOCALE = "Theip ar an obair";
    protected static final String INCOMPLETE_PARAMS_REASON_SERVER_LOCALE = "Unvollstandig Parameter";
    protected static final String INCOMPLETE_PARAMS_DETAIL_SERVER_LOCALE = " erforderlich fur ";
    protected static final Locale CLIENT_LOCALE = new Locale("ga", "IE");

    protected static String USER_FILTER_HEADER = "filter";

    protected static int SERVER_ERROR = 500;
    protected static int BAD_REQUEST = 400;

    protected static final String USER = "Aladdin";
    protected static final String DOMAIN = "Maghreb.Maghreb.Maghreb.com";
    protected static final String NAMESPACE = "*";

    protected static final String SESSION_ID = Guid.newGuid().toString();

    @Mock
    protected BackendLocal backend;
    protected MappingLocator mapperLocator;
    protected Locale locale;
    @Mock
    protected HttpHeaders httpHeaders;
    protected List<Locale> locales;
    protected List<String> accepts;

    protected MessageBundle messageBundle;

    private List<Runnable> interactions = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        interactions.clear();

        DbUser currentUser = new DbUser();
        currentUser.setFirstName(USER);
        currentUser.setLastName(USER);
        currentUser.setDomain(DOMAIN);
        currentUser.setNamespace(NAMESPACE);
        currentUser.setId(GUIDS[0]);

        Current current = new Current();
        current.setUser(currentUser);
        current.setSessionId(SESSION_ID);
        current.setRoot(URI_ROOT);
        current.setPrefix(BASE_PATH);
        current.setPath("");
        current.setVersion("4");
        current.setVersionSource(VersionSource.DEFAULT);
        current.setBackend(backend);
        CurrentManager.put(current);

        locales = new ArrayList<>();
        when(httpHeaders.getAcceptableLanguages()).thenReturn(locales);
        accepts = new ArrayList<>();
        when(httpHeaders.getRequestHeader("Accept")).thenReturn(accepts);
        List<String> filterValue = new ArrayList<>();
        filterValue.add("false");
        when(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).thenReturn(filterValue);
        mapperLocator = new MappingLocator();
        mapperLocator.populate();
        locale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        init();
    }

    protected <F, T> Mapper<F, T> getMapper(Class<F> from, Class<T> to) {
        return mapperLocator.getMapper(from, to);
    }

    @AfterEach
    public void tearDown() {
        Locale.setDefault(locale);
        interactions.forEach(Runnable::run);
        CurrentManager.remove();
    }

    /**
     * For backwards compatibility with EasyMock this method can be used to enqueue verifications for a given mock
     * interaction. After tests these interactions will be checked in order of their enqueuing.
     *
     * Example: <code>
     *     enqueueInteraction(() ->
     *          verify(myMock, times(3)).sampleMethod());
     * </code>
     *
     * @param interaction
     *            Block of code containing a Mockito mock verification.
     */
    protected void enqueueInteraction(Runnable interaction) {
        interactions.add(interaction);
    }

    protected abstract void init();

    protected UriInfo setUpBasicUriExpectations() {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(URI.create(URI_BASE));
        return uriInfo;
    }

    protected UriInfo setUpBasicUriExpectations(String path) {
        UriInfo uriInfo = mock(UriInfo.class);
        URI baseUri = URI.create(URI_BASE + '/');

        when(uriInfo.getBaseUri()).thenReturn(baseUri);
        when(uriInfo.getPath()).thenReturn(path);

        Current current = CurrentManager.get();
        current.setPath(path);

        return uriInfo;
    }

    protected UriInfo addMatrixParameterExpectations(UriInfo mockUriInfo, String parameterName, String parameterValue) {
        return addMatrixParameterExpectations(mockUriInfo, Collections.singletonMap(parameterName, parameterValue));
    }

    protected UriInfo addMatrixParameterExpectations(UriInfo mockUriInfo, Map<String, String> parameters) {
        MultivaluedMap<String, String> matrixParams = new SimpleMultivaluedMap<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            matrixParams.putSingle(entry.getKey(), entry.getValue());
        }
        PathSegment segment = mock(PathSegment.class);
        when(segment.getMatrixParameters()).thenReturn(matrixParams);
        when(mockUriInfo.getPathSegments()).thenReturn(Collections.singletonList(segment));
        return mockUriInfo;
    }

    protected UriInfo addMatrixParameterExpectations(UriInfo mockUriInfo, String parameterName) {
        return addMatrixParameterExpectations(mockUriInfo, parameterName, "");
    }

    protected <E> void setUpGetEntityExpectations(QueryType query,
                                                  Class<? extends QueryParametersBase> clz,
                                                  String[] names,
                                                  Object[] values,
                                                  E entity) {
        setUpGetEntityExpectations(query, clz, names, values, entity, false);
    }

    protected <E> void setUpGetEntityExpectations(QueryType query,
            Class<? extends QueryParametersBase> clz,
            String[] names,
            Object[] values,
            E entity,
            boolean onceOnly) {
        QueryReturnValue queryResult = new QueryReturnValue();
        OngoingStubbing<QueryReturnValue> stubbing =
                when(backend.runQuery(eq(query), eqParams(clz, addSession(names), addSession(values))))
                        .thenReturn(queryResult);
        if (onceOnly) {
            stubbing.thenReturn(null);
        }
        enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(query),
                eqParams(clz, addSession(names), addSession(values))));
        queryResult.setSucceeded(true);
        queryResult.setReturnValue(entity);
    }

    protected <E> void setUpGetEntityExpectations(String query,
            SearchType type,
            E entity) {
        QueryReturnValue queryResult = new QueryReturnValue();
        SearchParameters params = new SearchParameters(query, type);
        when(backend.runQuery(eq(QueryType.Search), eqSearchParams(params))).thenReturn(queryResult);
        enqueueInteraction(
                () -> verify(backend, atLeastOnce()).runQuery(eq(QueryType.Search), eqSearchParams(params)));
        queryResult.setSucceeded(true);
        List<E> entities = new ArrayList<>();
        entities.add(entity);
        queryResult.setReturnValue(entities);
    }

    protected void setUpEntityQueryExpectations(QueryType query,
            Class<? extends QueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn) {
        setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn, null);
    }

    protected void setUpEntityQueryExpectations(QueryType query,
            Class<? extends QueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            Object failure) {
        QueryReturnValue queryResult = new QueryReturnValue();
        queryResult.setSucceeded(failure == null);
        if (failure == null) {
            queryResult.setReturnValue(queryReturn);
            when(backend.runQuery(eq(query),
                    eqParams(queryClass, addSession(queryNames), addSession(queryValues)))).thenReturn(queryResult);
        } else {
            if (failure instanceof String) {
                queryResult.setExceptionString((String) failure);
                setUpL10nExpectations((String) failure);
                when(backend.runQuery(eq(query),
                        eqParams(queryClass, addSession(queryNames), addSession(queryValues)))).thenReturn(queryResult);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(query), eqParams(queryClass, addSession(queryNames), addSession(queryValues))))
                        .thenThrow((Exception) failure);
            }
        }

        if (queryClass != GetPermissionsForObjectParameters.class) {
            enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(query),
                    eqParams(queryClass, addSession(queryNames), addSession(queryValues))));
        }
    }

    protected void setUpGetConsoleExpectations(int... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetConsoleDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>()); // we expect no consoles by default
                                        // for tests that expect a console, add more generic version of this method
        }
    }

    protected void setUpGetRngDeviceExpectations(int... idxs) {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(QueryType.GetRngDevice,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>());
        }
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, true, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            String errorMessage) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, true, errorMessage);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            boolean reply) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, reply, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            boolean replay) {
        return setUpActionExpectations(task, clz, names, values, valid, success, taskReturn, null, replay, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            boolean replay,
            String errorMessage) {
        return setUpActionExpectations(task,
                clz,
                names,
                values,
                valid,
                success,
                taskReturn,
                null,
                replay,
                errorMessage);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            String baseUri,
            boolean replay) {
        return setUpActionExpectations(task,
                clz,
                names,
                values,
                valid,
                success,
                taskReturn,
                null,
                null,
                null,
                null,
                baseUri,
                replay,
                CANT_DO);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            String baseUri,
            boolean replay,
            String errorMessage) {
        return setUpActionExpectations(task,
                clz,
                names,
                values,
                valid,
                success,
                taskReturn,
                null,
                null,
                null,
                null,
                baseUri,
                replay,
                errorMessage);
    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            ArrayList<Guid> asyncTasks,
            ArrayList<AsyncTaskStatus> asyncStatuses,
            Guid jobId,
            JobExecutionStatus jobStatus,
            String baseUri,
            boolean replay) {
        return setUpActionExpectations(task,
                clz,
                names,
                values,
                valid,
                success,
                taskReturn,
                asyncTasks,
                asyncStatuses,
                jobId,
                jobStatus,
                baseUri,
                replay,
                CANT_DO);

    }

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values,
            boolean valid,
            boolean success,
            Object taskReturn,
            ArrayList<Guid> asyncTasks,
            ArrayList<AsyncTaskStatus> asyncStatuses,
            Guid jobId,
            JobExecutionStatus jobStatus,
            String baseUri,
            boolean replay,
            String errorMessage) {
        ActionReturnValue result = new ActionReturnValue();
        result.setValid(valid);
        if (valid) {
            result.setSucceeded(success);
            if (success) {
                if (taskReturn != null) {
                    result.setActionReturnValue(taskReturn);
                }
            } else {
                result.setExecuteFailedMessages(asList(FAILURE));
                setUpL10nExpectations(asList(FAILURE));
            }
        } else {
            result.setValidationMessages(asList(errorMessage));
            setUpL10nExpectations(asList(errorMessage));
        }
        when(backend.runAction(eq(task), eqParams(clz, addSession(names), addSession(values)))).thenReturn(result);
        enqueueInteraction(() -> verify(backend, atLeastOnce()).runAction(eq(task),
                eqParams(clz, addSession(names), addSession(values))));

        QueryReturnValue monitorResult = new QueryReturnValue();
        monitorResult.setSucceeded(success);

        // simulate polling on async task's statuses, and/or job status.
        setAsyncTaskStatusExpectations(asyncTasks, asyncStatuses, monitorResult, result);
        setJobStatusExpectations(jobId, jobStatus, monitorResult, result);

        UriInfo uriInfo = setUpBasicUriExpectations();
        if (baseUri != null) {
            CurrentManager.get().setPath(baseUri);
            when(uriInfo.getPath()).thenReturn(baseUri);
        }

        return uriInfo;
    }

    protected void setUpL10nExpectations(String error) {
        ErrorTranslator translator = mock(ErrorTranslator.class);
        Answer<String> answer = invocation -> invocation.getArguments() != null && invocation.getArguments().length > 0
                ? mockl10n((String) invocation.getArguments()[0])
                : null;
        if (!locales.isEmpty()) {
            doAnswer(answer).when(translator).translateErrorTextSingle(eq(error), eq(locales.get(0)));
        } else {
            doAnswer(answer).when(translator).translateErrorTextSingle(eq(error));
        }
        when(backend.getErrorsTranslator()).thenReturn(translator);
    }

    protected void setUpL10nExpectations(ArrayList<String> errors) {
        ErrorTranslator errorTranslator = mock(ErrorTranslator.class);
        if (!locales.isEmpty()) {
            when(errorTranslator.translateErrorText(eq(errors), eq(locales.get(0)))).thenReturn(mockl10n(errors));
        } else {
            when(errorTranslator.translateErrorText(eq(errors))).thenReturn(mockl10n(errors));
        }
        when(backend.getErrorsTranslator()).thenReturn(errorTranslator);
        enqueueInteraction(() -> verify(backend, atLeastOnce()).getErrorsTranslator());
    }

    protected List<String> mockl10n(List<String> errors) {
        return errors.stream().map(this::mockl10n).collect(Collectors.toList());
    }

    protected String mockl10n(String s) {
        return s.startsWith("l10n...") ? s : "l10n..." + s;
    }

    protected void verifyRemove(Response response) {
        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    protected void verifyLinks(BaseResource model) {
        assertNotNull(model.getHref());
        assertTrue(model.getHref().startsWith("/ovirt-engine/api"));
        for (Link link : model.getLinks()) {
            assertTrue(link.getHref().startsWith("/ovirt-engine/api"));
        }
    }

    protected void verifyFault(WebApplicationException wae, String detail) {
        verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, asList(mockl10n(detail)).toString(), BAD_REQUEST);
    }

    protected void verifyFault(WebApplicationException wae, String detail, int status) {
        verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, asList(mockl10n(detail)).toString(), status);
    }

    protected void verifyFault(WebApplicationException wae, String reason, String detail, int status) {
        verifyFault(wae, status);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertEquals(detail, fault.getDetail());
    }

    protected void verifyFault(WebApplicationException wae, int status) {
        assertEquals(status, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
    }

    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(SERVER_ERROR, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue(fault.getDetail().contains(t.getMessage()),
                "expected detail to include: " + t.getMessage());
    }

    protected void verifyIncompleteException(WebApplicationException wae,
            String type,
            String method,
            String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals(INCOMPLETE_PARAMS_REASON_SERVER_LOCALE, fault.getReason());
        assertEquals(type + " " + Arrays.asList(fields) + INCOMPLETE_PARAMS_DETAIL_SERVER_LOCALE + method,
                fault.getDetail());
    }

    protected void verifyNotFoundException(WebApplicationException wae) {
        assertNotNull(wae.getResponse());
        assertEquals(404, wae.getResponse().getStatus());
    }

    protected void verifyBadRequest(WebApplicationException wae) {
        assertNotNull(wae.getResponse());
        assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
    }

    protected <T> ArrayList<T> asList(T element) {
        ArrayList<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }

    protected String[] addSession(String... names) {
        String[] ret = new String[names.length + 1];
        System.arraycopy(names, 0, ret, 0, names.length);
        ret[names.length] = "SessionId";
        return ret;
    }

    protected Object[] addSession(Object... values) {
        Object[] ret = new Object[values.length + 1];
        System.arraycopy(values, 0, ret, 0, values.length);
        ret[values.length] = SESSION_ID;
        return ret;
    }

    private void setAsyncTaskStatusExpectations(ArrayList<Guid> asyncTasks,
            ArrayList<AsyncTaskStatus> asyncStatuses,
            QueryReturnValue monitorResult,
            ActionReturnValue result) {
        if (asyncTasks != null) {
            result.setVdsmTaskIdList(asyncTasks);
            monitorResult.setReturnValue(asyncStatuses);
            when(backend.runQuery(eq(QueryType.GetTasksStatusesByTasksIDs),
                    eqParams(GetTasksStatusesByTasksIDsParameters.class,
                            addSession(),
                            addSession(new Object[] {})))).thenReturn(monitorResult);
        }
    }

    private void setJobStatusExpectations(Guid jobId,
            JobExecutionStatus jobStatus,
            QueryReturnValue monitorResult,
            ActionReturnValue result) {
        result.setJobId(jobId);
        if (jobId != null) {
            Job jobMock = mock(org.ovirt.engine.core.common.job.Job.class);
            when(jobMock.getStatus()).thenReturn(jobStatus);
            monitorResult.setReturnValue(jobMock);
            when(backend.runQuery(eq(QueryType.GetJobByJobId),
                    eqParams(IdQueryParameters.class,
                            addSession("Id"),
                            addSession(jobId)))).thenReturn(monitorResult);
            enqueueInteraction(() -> verify(backend, atLeastOnce()).runQuery(eq(QueryType.GetJobByJobId),
                    eqParams(IdQueryParameters.class,
                            addSession("Id"),
                            addSession(jobId))));
        }
    }

    protected UriInfo setUpGetMatrixConstraintsExpectations(String matrixConstraint,
            boolean matrixConstraintExist,
            String matrixConstraintValue,
            UriInfo uriInfo) {
        List<PathSegment> psl = new ArrayList<>();

        PathSegment ps = mock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = mock(MultivaluedMap.class);

        when(matrixParams.isEmpty()).thenReturn(!matrixConstraintExist);
        when(ps.getMatrixParameters()).thenReturn(matrixParams);

        if (matrixConstraintExist) {
            when(matrixParams.containsKey(matrixConstraint)).thenReturn(matrixConstraintExist);
            when(matrixParams.getFirst(matrixConstraint)).thenReturn(matrixConstraintValue);
        }

        psl.add(ps);

        when(uriInfo.getPathSegments()).thenReturn(psl);

        return uriInfo;
    }

    protected void initBackendResource(BackendResource resource) {
        // Set the references to the objects that JAX-RS injects via the @Context annotation:
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);

        // Set them in the root of the API, as in some cases calls are delegated via this root:
        BackendApiResource root = BackendApiResource.getInstance();
        root.setMessageBundle(messageBundle);
        root.setHttpHeaders(httpHeaders);
    }

    public static class SimpleMultivaluedMap<K, V> extends HashMap<K, List<V>> implements MultivaluedMap<K, V> {
        @Override
        public void putSingle(K key, V value) {
            List<V> values = new ArrayList<>(1);
            values.add(value);
            put(key, values);
        }

        @Override
        public void add(K key, V value) {
            List<V> values = get(key);
            if (values == null) {
                values = new ArrayList<>(1);
                put(key, values);
            }
            values.add(value);
        }

        @Override
        public void addAll(K key, V... newValues) {
            List<V> values = get(key);
            if (values == null) {
                values = new ArrayList<>(newValues.length);
                put(key, values);
            }
            Collections.addAll(values, newValues);
        }

        @Override
        public void addAll(K key, List<V> newValues) {
            List<V> values = get(key);
            if (values == null) {
                values = new ArrayList<>(newValues.size());
                put(key, values);
            }
            values.addAll(newValues);
        }

        @Override
        public void addFirst(K key, V value) {
            List<V> values = get(key);
            if (values == null) {
                values = new ArrayList<>(1);
                put(key, values);
            }
            values.add(0, value);
        }

        @Override
        public V getFirst(K key) {
            List<V> values = get(key);
            if (values == null) {
                return null;
            }
            if (values.isEmpty()) {
                return null;
            }
            return values.get(0);
        }

        @Override
        public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> other) {
            // Note that this implemetation is wrong, but that isn't relevant for the test.
            return false;
        }
    }
}
