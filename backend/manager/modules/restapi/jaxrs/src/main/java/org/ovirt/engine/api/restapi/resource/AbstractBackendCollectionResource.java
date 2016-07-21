package org.ovirt.engine.api.restapi.resource;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.util.ExpectationHelper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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

    protected List<Q> getBackendCollection(SearchType searchType) {
        return getBackendCollection(searchType, QueryHelper.getConstraint(httpHeaders, uriInfo, "", modelType));
    }

    protected List<Q> getBackendCollection(SearchType searchType, String constraint) {
        return getBackendCollection(entityType,
                VdcQueryType.Search,
                getSearchParameters(searchType, constraint));
    }

    private SearchParameters getSearchParameters(SearchType searchType, String constraint) {
        SearchParameters searchParams = new SearchParameters(constraint, searchType);
        boolean caseSensitive = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, CASE_SENSITIVE_CONSTRAINT_PARAMETER, true, false);
        int from = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, FROM_CONSTRAINT_PARAMETER, -1, -1);
        int max = ParametersHelper.getIntegerParameter(httpHeaders, uriInfo, MAX, Integer.MAX_VALUE, Integer.MAX_VALUE);

        searchParams.setCaseSensitive(caseSensitive);
        if (from != -1) {
            searchParams.setSearchFrom(from);
        }
        searchParams.setMaxCount(max);
        return searchParams;
    }

    protected List<Q> getBackendCollection(VdcQueryType query, VdcQueryParametersBase queryParams) {
        return getBackendCollection(entityType, query, queryParams);
    }

    /**
     * get the entities according to the filter and intersect them with those resulted from running the search query
     */
    protected List<Q> getBackendCollection(VdcQueryType query, VdcQueryParametersBase queryParams, SearchType searchType) {
        List<Q> filteredList = getBackendCollection(entityType, query, queryParams);
        // check if we got search expression in the URI
        String search = ParametersHelper.getParameter(httpHeaders, uriInfo, QueryHelper.CONSTRAINT_PARAMETER);
        if (search != null) {
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
        model.setCreationStatus(getAsynchronousStatus(result).value());
        linkSubResource(model, CREATION_STATUS_REL, asString(result.getVdsmTaskIdList()));
    }

    @SuppressWarnings("unchecked")
    protected <T> Q resolveCreated(VdcReturnValueBase result, IResolver<T, Q> entityResolver) {
        try {
            return entityResolver.resolve(result.getActionReturnValue());
        } catch (Exception e) {
            // Handling exception as we can't tolerate the failure
            return handleError(e, false);
        }
    }

    /**
     *
     * @param model the resource to add actions to
     * @return collection with action links
     */
    protected <C extends ActionableResource> C addActions(C model) {
        LinkHelper.addActions(model, this);
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
            } else if (model.isSetHref()) {
                response = Response.created(URI.create(model.getHref())).entity(model).build();
            } else {
                response = Response.ok(Response.Status.CREATED).entity(model).build();
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
