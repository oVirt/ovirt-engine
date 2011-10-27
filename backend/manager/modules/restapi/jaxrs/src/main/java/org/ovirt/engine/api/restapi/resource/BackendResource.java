package org.ovirt.engine.api.restapi.resource;

import java.text.MessageFormat;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Version;
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

public class BackendResource extends BaseBackendResource {
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
            doAction(task, params);
            return Response.ok().build();
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
