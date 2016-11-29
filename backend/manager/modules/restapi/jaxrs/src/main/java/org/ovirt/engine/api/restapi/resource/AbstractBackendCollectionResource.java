package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.util.ExpectationHelper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
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

    protected AbstractBackendCollectionResource(Class<R> modelType, Class<Q> entityType) {
        super(modelType, entityType);
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

            // Note that it is key to pass here first the search list and then the filtered list, as we want to make
            // sure that the order of the search list is preserved, as the user my have specified 'sort by ...' as one
            // of the search criteria.
            return safeIntersection(searchList, filteredList);
        }
        else {
            return filteredList;
        }
    }

    /**
     * Calculates the intersection of two lists of objects, comparing them by id, and preserving the objects and the
     * order that was used in the {@code sorted} parameter.
     *
     * @param sorted the list of objects whose order should be preserved
     * @param other the other list of objects
     * @return the intersection of both lists of objects
     */
    private List<Q> safeIntersection(List<Q> sorted, List<Q> other) {
        // Calculate the sets of ids of all the objects:
        Set<Object> sortedIds = sorted.stream().map(this::getId).collect(toSet());
        Set<Object> otherIds = other.stream().map(this::getId).collect(toSet());

        // Remove from the set of sorted ids the ids that aren't part also of the other ids, thus effectively
        // calculating the intersection of both sets of ids:
        sortedIds.retainAll(otherIds);

        // Remove from the sorted list all the objects that aren't part of the intersection, and return the result (this
        // way the result will be always from the sorted list, and the order will be preserved):
        return sorted.stream()
            .filter(object -> sortedIds.contains(getId(object)))
            .collect(toList());
    }

    /**
     * Obtains the identifier of a backend object. This id will be used to compare the objects instead of the
     * {@link Object#equals(Object)} method. Should be overridden by resources that manage objects that don't implement
     * the {@link IVdcQueryable} interface.
     *
     * @param entity the entity
     * @return the id of the entity, or {@code null} if the entity doesn't have an id
     */
    protected Object getId(Q entity) {
        Object id = null;
        if (entity != null && entity instanceof IVdcQueryable) {
            id = ((IVdcQueryable) entity).getQueryableId();
        }
        return id;
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
