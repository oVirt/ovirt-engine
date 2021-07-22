package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.restapi.util.ExpectationHelper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBackendCollectionResource<R extends BaseResource, Q /* extends Queryable */>
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
                QueryType.Search,
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

    protected List<Q> getBackendCollection(QueryType query, QueryParametersBase queryParams) {
        return getBackendCollection(entityType, query, queryParams);
    }

    /**
     * get the entities according to the filter and intersect them with those resulted from running the search query
     */
    protected List<Q> getBackendCollection(QueryType query, QueryParametersBase queryParams, SearchType searchType) {
        // get the search predicate from the URL
        String search = ParametersHelper.getParameter(httpHeaders, uriInfo, QueryHelper.CONSTRAINT_PARAMETER);
        // if no search predicate provided - return the result of the filtered query as is
        if (search == null) {
            return getBackendCollection(entityType, query, queryParams);
        } else { // if search predicate is provided - proceed by checking for 'max' parameter:
            int max = ParametersHelper
                    .getIntegerParameter(httpHeaders, uriInfo, MAX, Integer.MAX_VALUE, Integer.MAX_VALUE);
            // if 'max' parameter does not exist - return the intersection between the
            // filtered-query and the search query with no need for additional manipulation
            if (max == Integer.MAX_VALUE) {
                return intersect(query, queryParams, searchType);
            } else { // if 'max' parameter does exists:
                // 1. Remove 'max' from parameters
                ParametersHelper.removeParameter(MAX);
                // 2. Get filtered-query and search-query results, and intersect them
                List<Q> results = intersect(query, queryParams, searchType);
                // 3. Manually apply 'max' to the result set
                return results.subList(0, max <= results.size() ? max : results.size());
            }
        }
    }

    /**
     * Execute the filtered-query, execute the search query and return an intersection of the results - i.e the elements
     * which exist in both lists result-sets (identified by their ids)
     */
    private List<Q> intersect(QueryType query, QueryParametersBase queryParams, SearchType searchType) {
        List<Q> filteredList = getBackendCollection(entityType, query, queryParams);
        List<Q> searchList = getBackendCollection(searchType);
        return safeIntersection(searchList, filteredList);
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
     * the {@link Queryable} interface.
     *
     * @param entity the entity
     * @return the id of the entity, or {@code null} if the entity doesn't have an id
     */
    protected Object getId(Q entity) {
        Object id = null;
        if (entity != null && entity instanceof Queryable) {
            id = ((Queryable) entity).getQueryableId();
        }
        return id;
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            boolean block) {
        return performCreate(task, taskParams, entityResolver, PollingType.VDSM_TASKS, block);
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            PollingType pollingType,
            boolean block) {
        return performCreate(task, taskParams, entityResolver, pollingType, block, null);
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            PollingType pollingType,
            boolean block,
            Class<? extends BaseResource> suggestedParentType) {

        // create (overridable)
        ActionReturnValue createResult = doCreateEntity(task, taskParams);

        // fetch + map
        return fetchCreatedEntity(entityResolver, block, pollingType, suggestedParentType, createResult);
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            IResolver<T, Q> entityResolver) {
        return performCreate(task, taskParams, PollingType.VDSM_TASKS, entityResolver);
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            PollingType pollingType,
            IResolver<T, Q> entityResolver) {
        return performCreate(task, taskParams, entityResolver, pollingType, expectBlocking());
    }

    protected final <T> Response performCreate(ActionType task,
            ActionParametersBase taskParams,
            IResolver<T, Q> entityResolver,
            Class<? extends BaseResource> suggestedParentType) {
        return performCreate(task, taskParams, entityResolver, PollingType.VDSM_TASKS, expectBlocking(), suggestedParentType);
    }

    protected boolean expectBlocking() {
        Set<String> expectations = ExpectationHelper.getExpectations(httpHeaders);
        return expectations.contains(BLOCKING_EXPECTATION);
    }

    protected void handleAsynchrony(ActionReturnValue result, R model) {
        CreationStatus status = getAsynchronousStatus(result);
        if (status != null) {
            model.setCreationStatus(status.value());
        }
        linkSubResource(model, CREATION_STATUS_REL, asString(result.getVdsmTaskIdList()));
    }

    @SuppressWarnings("unchecked")
    protected <T> Q resolveCreated(ActionReturnValue result, IResolver<T, Q> entityResolver) {
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
            PollingType pollingType,
            Class<? extends BaseResource> suggestedParentType,
            ActionReturnValue createResult) {
        Q created = resolveCreated(createResult, entityResolver);
        R model = mapEntity(suggestedParentType, created);
        modifyCreatedEntity(model);
        Response response = null;
        if (isAsyncTaskOrJobExists(pollingType, createResult)) {
            if (block) {
                awaitCompletion(createResult, pollingType);
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

    /**
     * Returns true if there are still processes running in the
     * background, associated with the current request.
     *
     * For vdsm-task polling, the indication is the existence of running
     * vdsm tasks.
     *
     * For job polling, the indication is that the job is in PENDING or
     * IN_PROGRESS status.
     */
    private boolean isAsyncTaskOrJobExists(PollingType pollingType, ActionReturnValue createResult) {
        if (pollingType==PollingType.VDSM_TASKS) {
            //when the polling-type is vdsm_tasks, check for existing async-tasks
            return createResult.getHasAsyncTasks();
        } else if (pollingType==PollingType.JOB) {
            //when the polling-type is job, check if the job is pending or in progress
            CreationStatus status = getJobIdStatus(createResult);
            return status==CreationStatus.PENDING || status==CreationStatus.IN_PROGRESS;
        }
        return false; //shouldn't reach here
    }

    /**
     * In rare cases, after entity creation there is a need to make modifications
     * to the created entity. Such changes should be done here
     *
     * @param model the entity
     */
    protected void modifyCreatedEntity(R model) {
        // do nothing by default
    }

    protected ActionReturnValue doCreateEntity(ActionType task, ActionParametersBase taskParams) {
        ActionReturnValue createResult;
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
