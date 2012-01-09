package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public abstract class AbstractBackendCollectionResource<R extends BaseResource, Q /* extends IVdcQueryable */>
        extends AbstractBackendResource<R, Q> {

    private static final String EXPECT_HEADER = "Expect";
    private static final String BLOCKING_EXPECTATION = "201-created";
    private static final String CREATION_STATUS_REL = "creation_status";
    public static final String FROM_CONSTRAINT_PARAMETER = "from";
    public static final String CASE_SENSITIVE_CONSTRAINT_PARAMETER = "case-sensitive";
    protected static final LogCompat LOG = LogFactoryCompat.getLog(AbstractBackendCollectionResource.class);

    protected AbstractBackendCollectionResource(Class<R> modelType, Class<Q> entityType, String... subCollections) {
        super(modelType, entityType, subCollections);
    }

    public Response remove(String id) {
        getEntity(id);  //will throw 404 if entity not found.
        return performRemove(id);
    }

    protected abstract Response performRemove(String id);

    protected List<Q> getBackendCollection(SearchType searchType) {
        return getBackendCollection(searchType, QueryHelper.getConstraint(getUriInfo(), "",  modelType));
    }

    protected List<Q> getBackendCollection(SearchType searchType, String constraint) {
        return getBackendCollection(entityType,
                                    VdcQueryType.Search,
                                    getSearchParameters(searchType, constraint));
    }

    private SearchParameters getSearchParameters(SearchType searchType, String constraint) {
        SearchParameters searchParams = new SearchParameters(constraint, searchType);
        HashMap<String, String> queryConstraints = QueryHelper.getQueryConstraints(getUriInfo(),
                                                                                   FROM_CONSTRAINT_PARAMETER);

        HashMap<String, String> matrixConstraints = QueryHelper.getMatrixConstraints(getUriInfo(),
                                                                                     CASE_SENSITIVE_CONSTRAINT_PARAMETER);

        if (queryConstraints.containsKey(FROM_CONSTRAINT_PARAMETER)) {
            try {
                searchParams.setSearchFrom(Long.parseLong(queryConstraints.get(FROM_CONSTRAINT_PARAMETER)));
            } catch (Exception ex) {
                LOG.error("Unwrapping of '"+FROM_CONSTRAINT_PARAMETER+"' search parameter failed.", ex);
            }
        }
        if (matrixConstraints.containsKey(CASE_SENSITIVE_CONSTRAINT_PARAMETER)) {
            try {
                searchParams.setCaseSensitive(Boolean.parseBoolean(matrixConstraints.get(CASE_SENSITIVE_CONSTRAINT_PARAMETER)));
            } catch (Exception ex) {
                LOG.error("Unwrapping of '"+CASE_SENSITIVE_CONSTRAINT_PARAMETER+"' search parameter failed.", ex);
            }
        }

        return searchParams;
    }

    protected List<Q> getBackendCollection(VdcQueryType query, VdcQueryParametersBase queryParams) {
        return getBackendCollection(entityType, query, queryParams);
    }


    protected Response performCreation(VdcActionType task,
                                       VdcActionParametersBase taskParams,
                                       EntityIdResolver entityResolver) {
        VdcReturnValueBase createResult;
        try {
            createResult = backend.RunAction(task, sessionize(taskParams));
            if (!createResult.getCanDoAction()) {
                throw new BackendFailureException(localize(createResult.getCanDoActionMessages()));
            } else if (!createResult.getSucceeded()) {
                throw new BackendFailureException(localize(createResult.getExecuteFailedMessages()));
            }
        } catch (Exception e) {
            return handleError(e, false);
        }

        R model = resolveCreated(createResult, entityResolver);
        Response response = null;
        if (createResult.getHasAsyncTasks()) {
            if (expectBlocking()) {
                awaitCompletion(createResult);
                // refresh model state
                model = resolveCreated(createResult, entityResolver);
                response = Response.created(URI.create(model.getHref())).entity(model).build();
            } else {
                if (model==null) {
                    response = Response.status(ACCEPTED_STATUS).build();
                } else {
                    handleAsynchrony(createResult, model);
                    response = Response.status(ACCEPTED_STATUS).entity(model).build();
                }
            }
        } else {
            if (model==null) {
                response = Response.status(ACCEPTED_STATUS).build();
            } else {
                response = Response.created(URI.create(model.getHref())).entity(model).build();
            }
        }
        return response;
    }

    protected boolean expectBlocking() {
        boolean expectBlocking = false;
        List<String> expect = httpHeaders.getRequestHeader(EXPECT_HEADER);
        if (expect != null && expect.size() > 0) {
            expectBlocking = expect.get(0).equalsIgnoreCase(BLOCKING_EXPECTATION);
        }
        return expectBlocking;
    }

    protected void handleAsynchrony(VdcReturnValueBase result, R model) {
        model.setCreationStatus(StatusUtils.create(getAsynchronousStatus(result)));
        linkSubResource(model, CREATION_STATUS_REL, asString(result.getTaskIdList()));
    }

    protected R resolveCreated(VdcReturnValueBase result, EntityIdResolver entityResolver) {
        try {
            Q created = entityResolver.resolve((Guid)result.getActionReturnValue());
            return addLinks(populate(map(created), created));
        } catch (Exception e) {
            // we tolerate a failure in the entity resolution
            // as the substantive action (entity creation) has
            // already succeeded
            e.printStackTrace();
            return null;
        }
    }

    protected String asString(VdcReturnValueBase result) {
        Guid guid = (Guid)result.getActionReturnValue();
        return guid != null ? guid.toString() : null;
    }

    protected void getEntity(String id) {
        try {
            Method method = getMethod(this.getClass(), SingleEntityResource.class);
            if (method==null) {
                LOG.error("Could not find sub-resource in the collection resource");
            } else {
                Object entityResource = method.invoke(this, id);
                method = entityResource.getClass().getMethod("get");
                if (method==null) {
                    LOG.warn("Could not find GET method in " + entityResource.getClass().getName());
                } else {
                    method.invoke(entityResource);
                }
            }
        } catch (IllegalAccessException e) {
            LOG.error("Reflection Error", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof WebApplicationException) {
                throw((WebApplicationException)e.getTargetException());
            } else {
                LOG.error("Reflection Error", e);
            }
        } catch (SecurityException e) {
            LOG.error("Reflection Error", e);
        } catch (NoSuchMethodException e) {
            LOG.error("Reflection Error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Method getMethod(Class<?> clazz, @SuppressWarnings("rawtypes") Class annotation) {
        for (Method m : clazz.getMethods()) {
            if (m.getAnnotation(annotation)!=null) {
                return m;
            }
        }
        return null;
    }
}
