package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePools;

import java.net.URI;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.utils.LinkCreator;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBackendActionableResource <R extends BaseResource, Q /* extends Queryable */ >
    extends AbstractBackendSubResource<R, Q> {
    private static final Logger log = LoggerFactory.getLogger(AbstractBackendActionableResource.class);

    public AbstractBackendActionableResource(String id, Class<R> modelType, Class<Q> entityType) {
        super(id, modelType, entityType);
    }

    protected Response doAction(final ActionType task, final ActionParametersBase params, final Action action, AbstractBackendResource.PollingType pollingType, EntityResolver entityResolver) {
        awaitGrace(action);
        try {
            ActionReturnValue actionResult = doAction(task, params);
            if (actionResult.getHasAsyncTasks()) {
                if (expectBlocking(action)) {
                    CreationStatus status = awaitCompletion(actionResult, pollingType);
                    if (status != CreationStatus.FAILED){
                        Object model = resolveCreated(actionResult, entityResolver, null);
                        return actionStatus(status, action, model);
                    }
                    return actionStatus(status, action, addLinks(newModel(id)));
                } else {
                    return actionAsync(actionResult, action);
                }
            } else {
                Object model = resolveCreated(actionResult, entityResolver, null);
                return actionSuccess(action, model);
            }
        } catch (Exception e) {
            return handleError(e, action);
        }
    }

    protected Object resolveCreated(ActionReturnValue result, EntityResolver entityResolver, Class<? extends BaseResource> suggestedParentType) {
        try {
            return entityResolver.resolve(result.getActionReturnValue());
        } catch (Exception e) {
            // we tolerate a failure in the entity resolution
            // as the substantive action (entity creation) has
            // already succeeded
            return null;
        }
    }

    protected Response doAction(final ActionType task, final ActionParametersBase params, final Action action, AbstractBackendResource.PollingType pollingType) {
        awaitGrace(action);
        try {
            ActionReturnValue actionResult = doAction(task, params);
            if (actionResult.getJobId() != null) {
                setJobLink(action, actionResult);
            }
            if (actionResult.getHasAsyncTasks()) {
                if (expectBlocking(action)) {
                    CreationStatus status = awaitCompletion(actionResult, pollingType);
                    return actionStatus(status, action, addLinks(newModel(id)));
                } else {
                    return actionAsync(actionResult, action);
                }
            } else {
                return actionSuccess(action, addLinks(newModel(id)));
            }
        } catch (Exception e) {
            return handleError(e, action);
        }
    }

    /**
     * Perform an action, managing asynchrony and returning an appropriate
     * response with entity returned by backend in action body.
     *
     * @param task     the backend task
     * @param params   the task parameters
     * @param action   action representation
     * @param entityResolver   backend response resolver
     */
    protected Response doAction(final ActionType task, final ActionParametersBase params, final Action action, EntityResolver entityResolver) {
        return doAction(task, params, action, PollingType.VDSM_TASKS, entityResolver);
    }

    /**
     * Perform an action, managing asynchrony and returning an appropriate
     * response.
     *
     * @param task     the backend task
     * @param params   the task parameters
     * @param action   action representation
     */
    protected Response doAction(final ActionType task, final ActionParametersBase params, final Action action) {
        return doAction(task, params, action, PollingType.VDSM_TASKS);
    }

    protected void awaitGrace(Action action) {
        if (action.isSetGracePeriod() && action.getGracePeriod().isSetExpiry()) {
            delay(action.getGracePeriod().getExpiry());
        }
    }

    protected boolean expectBlocking(Action action) {
        return action.isSetAsync() && !action.isAsync();
    }

    public ActionResource getActionResource(String action, String oid) {
        // redirect back to the target resource if action no longer cached
        // if not getActionResource() not overridden in resource sub-class
        // (in which case async actions are not supported, and the action
        // resource should never be queried)
        //
        return new ActionResource() {
                    @Override
                    public Response get() {
                        URI uri = URI.create(LinkHelper.addLinks(newModel(id)).getHref());
                        Response.Status status = Response.Status.MOVED_PERMANENTLY;
                        return Response.status(status).location(uri).build();
                    }
                    @Override
                    public Action getAction() {
                        return null;
                    }
                };
    }


    protected Guid getHostId(Action action) {
        return getHostId(action.getHost());
    }

    protected Response handleError(Exception e, Action action) {
        try {
            return handleError(e, false);
        } catch (WebFaultException wfe) {
            action.setFault(wfe.getFault());
            return actionFailure(action, wfe);
        } catch (WebApplicationException wae) {
            return actionFailure(action, wae);
        }
    }

    protected Response actionFailure(Action action, WebApplicationException wae) {
        action.setStatus(CreationStatus.FAILED.value());
        return Response.fromResponse(wae.getResponse()).entity(action).build();
    }

    protected Response actionSuccess(Action action) {
        action.setStatus(CreationStatus.COMPLETE.value());
        return Response.ok().entity(action).build();
    }

    private Response actionSuccess(Action action, Object result) {
        setActionItem(action, result);
        action.setStatus(CreationStatus.COMPLETE.value());
        return Response.ok().entity(action).build();
    }

    protected Response actionAsync(ActionReturnValue actionResult, Action action) {
        action.setAsync(true);

        String ids = asString(actionResult.getVdsmTaskIdList());
        action.setId(ids);

        Current current = CurrentManager.get();
        String path = current.getRelativePath();
        action.setHref(path + "/" + ids);
        addOrUpdateLink(action, "parent", path.substring(0, path.lastIndexOf("/")));
        addOrUpdateLink(action, "replay", path);

        CreationStatus status = getAsynchronousStatus(actionResult);
        if (status != null) {
            action.setStatus(status.value());
        }
        return Response.status(ACCEPTED_STATUS).entity(action).build();
    }

    private String getPath(UriInfo uriInfo) {
        return LinkCreator.combine(uriInfo.getBaseUri().getPath(), uriInfo.getPath());
    }

    protected Guid getStorageDomainId(Action action) {
        if (action.getStorageDomain().isSetId()) {
            return asGuid(action.getStorageDomain().getId());
        } else {
            return lookupStorageDomainIdByName(action.getStorageDomain().getName());
        }
    }

    protected Guid getClusterId(Action action) {
        if (action.getCluster().isSetId()) {
            return asGuid(action.getCluster().getId());
        } else {
            Cluster cluster = lookupClusterByName(action.getCluster().getName());
            return cluster != null ? cluster.getId() : null;
        }
    }

    protected Cluster lookupClusterByName(String name) {
        return getEntity(Cluster.class, QueryType.GetClusterByName, new NameQueryParameters(name), "Cluster: name=" + name);
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        if (!isFiltered()) {
            StorageDomainStatic storageDomain =
                    getEntity(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class,
                    QueryType.GetStorageDomainByName,
                    new NameQueryParameters(name),
                    "Storage: name=" + name);

            if (storageDomain != null) {
                return storageDomain.getId();
            }

            return null;
        } else {
            List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains =
                    getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                            QueryType.GetAllStorageDomains,
                            new QueryParametersBase());
            for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
                if (storageDomain.getStorageName().equals(name)) {
                    return storageDomain.getId();
                }
            }
            return null;
        }
    }

    public Guid getDataCenterId(Guid storageDomainId) {
        List<StoragePool> storagepools = getStoragePools(storageDomainId, this);
        return storagepools.size() > 0 ? storagepools.get(0).getId() : null;
    }

    /**
     * This class can be used as a resolver that binds business logic objects to API models by ID.
     * The from and to classes are determined by parameters passed to the constructor.
     * The returned model object will contain only the ID field of the object.
     * */
    protected class SimpleIdResolver extends EntityResolver {
        private Class<? extends BaseResource> apiClass;
        private Class<? extends BusinessEntity<?>> blClass;
        private QueryType query;
        private Class<? extends QueryParametersBase> queryParamsClass;

        /**
         * Create a resolver.
         *
         * @param apiClass          Model class whose instance will be returned from lookupEntity.
         * @param blClass           Business logic class whose ID will be passed to lookupEntity.
         * @param query             Same as in EntityResolver's constructor.
         * @param queryParamsClass  Same as in EntityResolver's constructor.
         * */
        public SimpleIdResolver(
                Class<? extends BaseResource> apiClass,
                Class<? extends BusinessEntity<?>> blClass,
                QueryType query,
                Class<? extends QueryParametersBase> queryParamsClass
        ) {
            this.apiClass = apiClass;
            this.blClass = blClass;
            this.query = query;
            this.queryParamsClass = queryParamsClass;
        }

        @Override
        public Object lookupEntity(Object id) throws BackendFailureException {
            BusinessEntity<?> blEntity = doGetEntity(blClass,
                    query, getQueryParams(queryParamsClass, id), id.toString());
            try {
                BaseResource apiObject = apiClass.getConstructor().newInstance();
                apiObject.setId(blEntity.getId().toString());
                return LinkHelper.addLinks(apiObject);
            } catch (ReflectiveOperationException e) {
                // Shouldn't happen, all models have public no-args constructor.
                log.error("Failed to instantiate BackendResource", e);
                return null;
            }
        }
    }
}
