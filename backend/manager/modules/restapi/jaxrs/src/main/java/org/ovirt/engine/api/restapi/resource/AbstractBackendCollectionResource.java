package org.ovirt.engine.api.restapi.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.util.ExpectationHelper;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBackendCollectionResource<R extends BaseResource, Q /* extends IVdcQueryable */>
        extends AbstractBackendResource<R, Q> {

    private static final String BLOCKING_EXPECTATION = "201-created";
    private static final String CREATION_STATUS_REL = "creation_status";
    public static final String FROM_CONSTRAINT_PARAMETER = "from";
    public static final String CASE_SENSITIVE_CONSTRAINT_PARAMETER = "case_sensitive";
    private static final Logger log = LoggerFactory.getLogger(AbstractBackendCollectionResource.class);

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
        HashMap<String, String> matrixConstraints = QueryHelper.getMatrixConstraints(getUriInfo(),
                                                                                     CASE_SENSITIVE_CONSTRAINT_PARAMETER,
                                                                                     FROM_CONSTRAINT_PARAMETER);

        //preserved in sake if backward compatibility until 4.0
        HashMap<String, String> queryConstraints = QueryHelper.getQueryConstraints(getUriInfo(),
                                                                                   FROM_CONSTRAINT_PARAMETER);

        String fromParameter = matrixConstraints.get(FROM_CONSTRAINT_PARAMETER);
        if (StringUtils.isNotBlank(fromParameter)) {
            try {
                searchParams.setSearchFrom(Long.parseLong(fromParameter));
            } catch (Exception ex) {
                log.error("Unwrapping of '{}' matrix search parameter failed: {}",
                        FROM_CONSTRAINT_PARAMETER, ex.getMessage());
                log.error("Exception", ex);
            }
        } else {
            //preserved in sake if backward compatibility until 4.0
            fromParameter = queryConstraints.get(FROM_CONSTRAINT_PARAMETER);
            if (StringUtils.isNotBlank(fromParameter)) {
                try {
                    searchParams.setSearchFrom(Long.parseLong(fromParameter));
                } catch (Exception ex) {
                    log.error("Unwrapping of '{}' query search parameter failed: {}",
                            FROM_CONSTRAINT_PARAMETER, ex.getMessage());
                    log.error("Exception", ex);
                }
            }
        }
        if (matrixConstraints.containsKey(CASE_SENSITIVE_CONSTRAINT_PARAMETER)) {
            try {
                searchParams.setCaseSensitive(Boolean.parseBoolean(matrixConstraints.get(CASE_SENSITIVE_CONSTRAINT_PARAMETER)));
            } catch (Exception ex) {
                log.error("Unwrapping of '{}' search parameter failed: {}",
                        CASE_SENSITIVE_CONSTRAINT_PARAMETER, ex.getMessage());
                log.error("Exception", ex);
            }
        }

        try {
            if (QueryHelper.hasMatrixParam(getUriInfo(), MAX) && getMaxResults()!=NO_LIMIT) {
                searchParams.setMaxCount(getMaxResults());
            }
        } catch (MalformedNumberException ex) {
            handleError(ex, false);
        }
        return searchParams;
    }

    protected List<Q> getBackendCollection(VdcQueryType query, VdcQueryParametersBase queryParams) {
        return getBackendCollection(entityType, query, queryParams);
    }

    /**
     * get the entities according to the filter and intersect them with those resulted from running the search query
     * @param query
     * @param queryParams
     * @param searchType
     * @return
     */
    protected List<Q> getBackendCollection(VdcQueryType query, VdcQueryParametersBase queryParams, SearchType searchType) {
        List<Q> filteredList = getBackendCollection(entityType, query, queryParams);
        // check if we got search expression in the URI
        if (QueryHelper.hasConstraint(getUriInfo(), QueryHelper.CONSTRAINT_PARAMETER)) {
            List<Q> searchList = getBackendCollection(searchType);
            return (List<Q>) CollectionUtils.intersection(filteredList, searchList);
        }
        else {
            return filteredList;
        }
    }

    protected final <T> Response performCreate(VdcActionType task,
            VdcActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            boolean block) {
        return performCreate(task, taskParams, entityResolver, block, null);
    }

    protected final <T> Response performCreate(VdcActionType task,
            VdcActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            boolean block,
            Class<? extends BaseResource> suggestedParentType) {

        // create (overridable)
        VdcReturnValueBase createResult = doCreateEntity(task, taskParams);

        // fetch + map
        return fetchCreatedEntity(entityResolver, block, suggestedParentType, createResult);
    }

    protected final <T> Response performCreate(VdcActionType task,
            VdcActionParametersBase taskParams,
            IResolver<T, Q> entityResolver) {
        return performCreate(task, taskParams, entityResolver, expectBlocking());
    }

    protected final <T> Response performCreate(VdcActionType task,
            VdcActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            Class<? extends BaseResource> suggestedParentType) {
        return performCreate(task, taskParams, entityResolver, expectBlocking(), suggestedParentType);
    }

    protected boolean expectBlocking() {
        Set<String> expectations = ExpectationHelper.getExpectations(httpHeaders);
        return expectations.contains(BLOCKING_EXPECTATION);
    }

    protected void handleAsynchrony(VdcReturnValueBase result, R model) {
        model.setCreationStatus(StatusUtils.create(getAsynchronousStatus(result)));
        linkSubResource(model, CREATION_STATUS_REL, asString(result.getVdsmTaskIdList()));
    }

    @SuppressWarnings("unchecked")
    protected <T> Q resolveCreated(VdcReturnValueBase result, IResolver<T, Q> entityResolver) {
        try {
            return entityResolver.resolve((T) result.getActionReturnValue());
        } catch (Exception e) {
            // Handling exception as we can't tolerate the failure
            return handleError(e, false);
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
                log.error("Could not find sub-resource in the collection resource");
            } else {
                Object entityResource = method.invoke(this, id);
                method = entityResource.getClass().getMethod("get");
                method.invoke(entityResource);
            }
        } catch (IllegalAccessException e) {
            log.error("Reflection Error", e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof WebApplicationException) {
                throw((WebApplicationException)e.getTargetException());
            } else {
                log.error("Reflection Error", e);
            }
        } catch (SecurityException e) {
            log.error("Reflection Error", e);
        } catch (NoSuchMethodException e) {
            log.error("Reflection Error", e);
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

    /**
     *
     * @param model the resource to add actions to
     * @return collection with action links
     */
    protected <C extends ActionableResource> C addActions(C model) {
        LinkHelper.addActions(getUriInfo(), model, this);
        return model;
    }

    private <T> Response fetchCreatedEntity(IResolver<T, Q> entityResolver,
            boolean block,
            Class<? extends BaseResource> suggestedParentType,
            VdcReturnValueBase createResult) {
        Q created = resolveCreated(createResult, entityResolver);
        R model = mapEntity(suggestedParentType, created);
        Response response = null;
        if (createResult.getHasAsyncTasks()) {
            if (block) {
                awaitCompletion(createResult);
                // refresh model state
                created = resolveCreated(createResult, entityResolver);
                model = mapEntity(suggestedParentType, created);
                response = Response.created(URI.create(model.getHref())).entity(model).build();
            } else {
                if (model == null) {
                    response = Response.status(ACCEPTED_STATUS).build();
                } else {
                    handleAsynchrony(createResult, model);
                    response = Response.status(ACCEPTED_STATUS).entity(model).build();
                }
            }
        } else {
            if (model == null) {
                response = Response.status(ACCEPTED_STATUS).build();
            } else {
                response = Response.created(URI.create(model.getHref())).entity(model).build();
            }
        }
        return response;
    }

    protected VdcReturnValueBase doCreateEntity(VdcActionType task, VdcActionParametersBase taskParams) {
        VdcReturnValueBase createResult;
        try {
            createResult = doAction(task, taskParams);
        } catch (Exception e) {
            return handleError(e, false);
        }
        return createResult;
    }

    private R mapEntity(Class<? extends BaseResource> suggestedParentType, Q created) {
        R model = map(created);
        model = deprecatedPopulate(model, created);
        model = doPopulate(model, created);
        return addLinks(model, suggestedParentType);
    }
}
