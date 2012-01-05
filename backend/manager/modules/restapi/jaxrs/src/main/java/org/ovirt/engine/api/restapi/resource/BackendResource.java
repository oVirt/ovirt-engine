package org.ovirt.engine.api.restapi.resource;

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.invocation.MetaData;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class BackendResource extends BaseBackendResource {
    private static final String ASYNC_CONSTRAINT = "async";
    private static final String EXPECT_HEADER = "Expect";
    private static final String NON_BLOCKING_EXPECTATION = "202-accepted";

    protected <T> T getEntity(Class<T> clz, SearchType searchType, String constraint) {
        try {
            VdcQueryReturnValue result = backend.RunQuery(VdcQueryType.Search,
                                                          sessionize(new SearchParameters(constraint, searchType)));
            if (!result.getSucceeded()) {
                throw new BackendFailureException(localize(result.getExceptionString()));
            }

            T entity;
            if (List.class.isAssignableFrom(clz) && result.getReturnValue() instanceof List) {
                entity = clz.cast(result.getReturnValue());
            } else {
                List<T> list = asCollection(clz, result.getReturnValue());
                if (list == null || list.isEmpty()) {
                    throw new EntityNotFoundException(constraint);
                }
                entity = clz.cast(list.get(0));
            }
            return entity;
        } catch (Exception e) {
            return handleError(clz, e, false);
        }
    }

    protected <T> T getEntity(Class<T> clz, VdcQueryType query, VdcQueryParametersBase queryParams, String identifier) {
        return getEntity(clz, query, queryParams, identifier, false);
    }

    protected <T> T getEntity(Class<T> clz,
                              VdcQueryType query,
                              VdcQueryParametersBase queryParams,
                              String identifier,
                              boolean notFoundAs404) {
        try {
            return doGetEntity(clz, query, queryParams, identifier);
        } catch (Exception e) {
            return handleError(clz, e, notFoundAs404);
        }
    }

    protected <T> T doGetEntity(Class<T> clz,
                                VdcQueryType query,
                                VdcQueryParametersBase queryParams,
                                String identifier) throws BackendFailureException {
        VdcQueryReturnValue result = backend.RunQuery(query, sessionize(queryParams));
        if (!result.getSucceeded() || result.getReturnValue() == null) {
            if (result.getExceptionString() != null) {
                throw new BackendFailureException(localize(result.getExceptionString()));
            } else {
                throw new EntityNotFoundException(identifier);
            }
        }
        return clz.cast(result.getReturnValue());
    }

    protected <T> List<T> getBackendCollection(Class<T> clz, VdcQueryType query, VdcQueryParametersBase queryParams) {
        try {
            VdcQueryReturnValue result = backend.RunQuery(query, sessionize(queryParams));
            if (!result.getSucceeded()) {
                throw new BackendFailureException(localize(result.getExceptionString()));
            }
            return asCollection(clz, result.getReturnValue());
        } catch (Exception e) {
            return handleError(e, false);
        }
    }

    protected Response performAction(VdcActionType task, VdcActionParametersBase params) {
        try {
            if (QueryHelper.hasMatrixParam(getUriInfo(), ASYNC_CONSTRAINT) ||
                    expectNonBlocking()) {
                getCurrent().get(MetaData.class).set("async", true);
                return performNonBlockingAction(task, params);
            } else {
                doAction(task, params);
                return Response.ok().build();
            }
        } catch (Exception e) {
            return handleError(Response.class, e, false);
        }
    }

    protected boolean expectNonBlocking() {
        boolean expectNonBlocking = false;
        List<String> expect = httpHeaders.getRequestHeader(EXPECT_HEADER);
        if (expect != null && expect.size() > 0) {
            expectNonBlocking = expect.get(0).equalsIgnoreCase(NON_BLOCKING_EXPECTATION);
        }
        return expectNonBlocking;
    }
    protected Response performNonBlockingAction(VdcActionType task, VdcActionParametersBase params) {
        try {
            doNonBlockingAction(task, params);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            return handleError(Response.class, e, false);
        }
    }

    protected <T> T performAction(VdcActionType task, VdcActionParametersBase params, Class<T> resultType) {
        try {
            return resultType.cast(doAction(task, params).getActionReturnValue());
        } catch (Exception e) {
            return handleError(resultType, e, false);
        }
    }

    protected VdcReturnValueBase doAction(VdcActionType task,
                                          VdcActionParametersBase params) throws BackendFailureException {
        VdcReturnValueBase result = backend.RunAction(task, sessionize(params));
        if (!result.getCanDoAction()) {
            throw new BackendFailureException(localize(result.getCanDoActionMessages()));
        } else if (!result.getSucceeded()) {
            throw new BackendFailureException(localize(result.getExecuteFailedMessages()));
        }
        assert (result != null);
        return result;
    }

    protected void doNonBlockingAction(final VdcActionType task, final VdcActionParametersBase params) {
        ThreadPoolUtil.execute(new Runnable() {
            SessionHelper sh = getSessionHelper();
            VdcActionParametersBase sp = sessionize(params);
            VdcUser user = getCurrent().get(VdcUser.class);

            @Override
            public void run() {
                try {
                    backend.RunAction(task, sp);
                } finally {
                    if (user != null) {
                        backend.Logoff(sh.sessionize(new LogoutUserParameters(user.getUserId())));
                    }
                    sh.clean();
                }
            }
        });
    }

    @SuppressWarnings("serial")
    protected <T> T getConfigurationValue(Class<T> clz, ConfigurationValues config, final Version version) {
        return getEntity(clz,
                         VdcQueryType.GetConfigurationValue,
                         new GetConfigurationValueParameters(config){{setVersion(asString(version));}},
                         config.toString());
    }

    private static final String VERSION_FORMAT = "{0}.{1}";

    private String asString(Version version) {
        return version == null ? null : MessageFormat.format(VERSION_FORMAT, version.getMajor(), version.getMinor());
    }
}
