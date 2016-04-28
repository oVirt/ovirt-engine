package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqActionParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
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
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
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
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;


public abstract class AbstractBackendBaseTest extends Assert {
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

    protected BackendLocal backend;
    protected MappingLocator mapperLocator;
    protected Locale locale;
    protected HttpHeaders httpHeaders;
    protected List<Locale> locales;
    protected List<String> accepts;

    protected MessageBundle messageBundle;
    protected IMocksControl control;

    @Rule
    public final MockConfigRule mcr = new MockConfigRule();

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        backend = control.createMock(BackendLocal.class);

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

        httpHeaders = control.createMock(HttpHeaders.class);
        locales = new ArrayList<>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        accepts = new ArrayList<>();
        expect(httpHeaders.getRequestHeader("Accept")).andReturn(accepts).anyTimes();
        List<String> filterValue = new ArrayList<>();
        filterValue.add("false");
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue).anyTimes();
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

    @After
    public void tearDown() {
        Locale.setDefault(locale);
        control.verify();
        CurrentManager.remove();
    }

    protected abstract void init();

    protected UriInfo setUpBasicUriExpectations() {
        UriInfo uriInfo = control.createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(URI_BASE)).anyTimes();
        return uriInfo;
    }

    protected UriInfo setUpBasicUriExpectations(String path) {
        UriInfo uriInfo = control.createMock(UriInfo.class);
        URI baseUri = URI.create(URI_BASE + '/');

        expect(uriInfo.getBaseUri()).andReturn(baseUri).anyTimes();
        expect(uriInfo.getPath()).andReturn(path).anyTimes();

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
        PathSegment segment = control.createMock(PathSegment.class);
        expect(segment.getMatrixParameters()).andReturn(matrixParams).anyTimes();
        expect(mockUriInfo.getPathSegments()).andReturn(Collections.singletonList(segment)).anyTimes();
        return mockUriInfo;
    }

    protected UriInfo addMatrixParameterExpectations(UriInfo mockUriInfo, String parameterName) {
        return addMatrixParameterExpectations(mockUriInfo, parameterName, "");
    }

    protected <E> void setUpGetEntityExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> clz, String[] names, Object[] values, E entity)
            throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(backend.runQuery(eq(query), eqQueryParams(clz, addSession(names), addSession(values)))).andReturn(
                queryResult);
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(entity).anyTimes();
    }

    protected <E> void setUpGetEntityExpectations(String query,
            SearchType type,
            E entity) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(query, type);
        expect(backend.runQuery(eq(VdcQueryType.Search),
                eqSearchParams(params))).andReturn(queryResult);
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        List<E> entities = new ArrayList<>();
        entities.add(entity);
        expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
    }

    protected void setUpEntityQueryExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn) {
        setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn, null);
    }

    protected void setUpEntityQueryExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> queryClass,
            String[] queryNames,
            Object[] queryValues,
            Object queryReturn,
            Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            expect(queryResult.getReturnValue()).andReturn(queryReturn).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        if(queryClass == GetPermissionsForObjectParameters.class) {
            expect(backend.runQuery(eq(query),
                eqQueryParams(queryClass,
                        addSession(queryNames),
                        addSession(queryValues)))).andReturn(queryResult).anyTimes();
        } else {
            expect(backend.runQuery(eq(query),
                eqQueryParams(queryClass,
                        addSession(queryNames),
                        addSession(queryValues)))).andReturn(queryResult);
        }
    }

    protected void setUpGetConsoleExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetConsoleDevices,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[idxs[i]] },
                    new ArrayList<>()); // we expect no consoles by default
                                        // for tests that expect a console, add more generic version of this method
        }
    }

    protected void setUpGetRngDeviceExpectations(int ... idxs) throws Exception {
        for (int i = 0; i < idxs.length; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetRngDevice,
                        IdQueryParameters.class,
                        new String[] { "Id" },
                        new Object[] { GUIDS[idxs[i]] },
            new ArrayList<>());
        }
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean valid, boolean success) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, true, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean valid, boolean success, String errorMessage) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, true, errorMessage);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean valid, boolean success, boolean reply) {
        return setUpActionExpectations(task, clz, names, values, valid, success, null, reply, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean valid, boolean success, Object taskReturn, boolean replay) {
        return setUpActionExpectations(task, clz, names, values, valid, success, taskReturn, null, replay, CANT_DO);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean valid, boolean success, Object taskReturn, boolean replay, String errorMessage) {
        return setUpActionExpectations(task, clz, names, values, valid, success, taskReturn, null, replay, errorMessage);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
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

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
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

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
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

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
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
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        expect(result.isValid()).andReturn(valid).anyTimes();
        if (valid) {
            expect(result.getSucceeded()).andReturn(success).anyTimes();
            if (success) {
                if (taskReturn != null) {
                    expect(result.getActionReturnValue()).andReturn(taskReturn).anyTimes();
                }
            } else {
                expect(result.getExecuteFailedMessages()).andReturn(asList(FAILURE)).anyTimes();
                setUpL10nExpectations(asList(FAILURE));
            }
        } else {
            expect(result.getValidationMessages()).andReturn(asList(errorMessage)).anyTimes();
            setUpL10nExpectations(asList(errorMessage));
        }
        expect(backend.runAction(eq(task), eqActionParams(clz, addSession(names), addSession(values)))).andReturn(result);

        VdcQueryReturnValue monitorResult = control.createMock(VdcQueryReturnValue.class);
        expect(monitorResult.getSucceeded()).andReturn(success).anyTimes();

        expect(result.getHasAsyncTasks()).andReturn(asyncTasks != null || jobId!=null).anyTimes();
        //simulate polling on async task's statuses, and/or job status.
        setAsyncTaskStatusExpectations(asyncTasks, asyncStatuses, monitorResult, result);
        setJobStatusExpectations(jobId, jobStatus, monitorResult, result);

        UriInfo uriInfo = setUpBasicUriExpectations();
        if (baseUri != null) {
            expect(uriInfo.getPath()).andReturn(baseUri).anyTimes();
        }

        if (replay) {
            control.replay();
        }
        return uriInfo;
    }

    protected void setUpL10nExpectations(String error) {
        ErrorTranslator translator = control.createMock(ErrorTranslator.class);
        IAnswer<String> answer = new IAnswer<String>() {
            @Override
            public String answer() {
                return EasyMock.getCurrentArguments() != null && EasyMock.getCurrentArguments().length > 0
                        ? mockl10n((String) EasyMock.getCurrentArguments()[0])
                        : null;
            }
        };
        if (!locales.isEmpty()) {
            expect(translator.translateErrorTextSingle(eq(error), eq(locales.get(0)))).andAnswer(answer).anyTimes();
        } else {
            expect(translator.translateErrorTextSingle(eq(error))).andAnswer(answer).anyTimes();
        }
        expect(backend.getErrorsTranslator()).andReturn(translator).anyTimes();
    }

    protected void setUpL10nExpectations(ArrayList<String> errors) {
        ErrorTranslator errorTranslator = control.createMock(ErrorTranslator.class);
        if (!locales.isEmpty()) {
            expect(errorTranslator.translateErrorText(eq(errors), eq(locales.get(0)))).andReturn(mockl10n(errors))
                    .anyTimes();
        } else {
            expect(errorTranslator.translateErrorText(eq(errors))).andReturn(mockl10n(errors)).anyTimes();
        }
        expect(backend.getErrorsTranslator()).andReturn(errorTranslator);
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
        assertTrue("expected detail to include: " + t.getMessage(), fault.getDetail().contains(t.getMessage()));
    }

    protected void verifyIncompleteException(WebApplicationException wae, String type, String method, String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals(INCOMPLETE_PARAMS_REASON_SERVER_LOCALE, fault.getReason());
        assertEquals(type + " " + Arrays.asList(fields) + INCOMPLETE_PARAMS_DETAIL_SERVER_LOCALE + method,
                fault.getDetail());
    }

    protected void verifyNotFoundException(WebApplicationException wae) {
        assertEquals(404, wae.getResponse().getStatus());
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
            VdcQueryReturnValue monitorResult,
            VdcReturnValueBase result) {
        if (asyncTasks != null) {
            expect(result.getVdsmTaskIdList()).andReturn(asyncTasks).anyTimes();
            expect(monitorResult.getReturnValue()).andReturn(asyncStatuses).anyTimes();
            expect(backend.runQuery(eq(VdcQueryType.GetTasksStatusesByTasksIDs),
                    eqQueryParams(GetTasksStatusesByTasksIDsParameters.class,
                            addSession(),
                            addSession(new Object[]{})))).andReturn(monitorResult);
        }
    }

    private void setJobStatusExpectations(Guid jobId,
            JobExecutionStatus jobStatus,
            VdcQueryReturnValue monitorResult,
            VdcReturnValueBase result) {
        expect(result.getJobId()).andReturn(jobId).anyTimes();
        if (jobId!=null) {
            Job jobMock = control.createMock(org.ovirt.engine.core.common.job.Job.class);
            expect(jobMock.getStatus()).andReturn(jobStatus);
            expect(monitorResult.getReturnValue()).andReturn(jobMock).anyTimes();
            expect(backend.runQuery(eq(VdcQueryType.GetJobByJobId),
                    eqQueryParams(IdQueryParameters.class,
                            addSession("Id"),
                            addSession(jobId)))).andReturn(monitorResult);
        }
    }



    protected UriInfo setUpGetMatrixConstraintsExpectations(String matrixConstraint,
            boolean matrixConstraintExist,
            String matrixConstraintValue, UriInfo uriInfo, boolean replay) {
        List<PathSegment> psl = new ArrayList<>();

        PathSegment ps = control.createMock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = control.createMock(MultivaluedMap.class);

        expect(matrixParams.isEmpty()).andReturn(!matrixConstraintExist);
        expect(ps.getMatrixParameters()).andReturn(matrixParams).anyTimes();

        if (matrixConstraintExist) {
            expect(matrixParams.containsKey(matrixConstraint)).andReturn(matrixConstraintExist).anyTimes();
            expect(matrixParams.getFirst(matrixConstraint)).andReturn(matrixConstraintValue).anyTimes();
        }

        psl.add(ps);

        expect(uriInfo.getPathSegments()).andReturn(psl).anyTimes();

        if (replay) {
            control.replay();
        }

        return uriInfo;
    }

    protected void initBackendResource(BackendResource resource) {
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
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
