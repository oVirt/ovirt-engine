package org.ovirt.engine.api.restapi.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.utils.LinkCreator;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendResource<R extends BaseResource, Q /* extends IVdcQueryable */>
    extends BackendResource {

    protected static final String ID_SEPARATOR = ",";
    protected static final long MONITOR_DELAY = 1000L;
    protected static final javax.ws.rs.core.Response.Status ACCEPTED_STATUS =
        javax.ws.rs.core.Response.Status.ACCEPTED;

    protected Class<R> modelType;
    protected Class<Q> entityType;
    protected String[] subCollections;

    public static enum PollingType {
        VDSM_TASKS, JOB;
    }

    protected AbstractBackendResource(Class<R> modelType, Class<Q> entityType) {
        this.modelType = modelType;
        this.entityType = entityType;
    }

    protected AbstractBackendResource(Class<R> modelType, Class<Q> entityType, String... subCollections) {
        this(modelType, entityType);
        this.subCollections = subCollections;
    }

    protected <F, T> Mapper<F, T> getMapper(Class<F> from, Class<T> to) {
        return mappingLocator.getMapper(from, to);
    }

    protected R map(Q entity) {
        return map(entity, null);
    }

    protected R map(Q entity, R template) {
        return getMapper(entityType, modelType).map(entity, template);
    }

    protected Q map(R model) {
        return map(model, null);
    }

    protected Q map(R model, Q template) {
        return getMapper(modelType, entityType).map(model, template);
    }

    protected CreationStatus awaitCompletion(VdcReturnValueBase result) {
        return awaitCompletion(result, PollingType.VDSM_TASKS);
    }

    protected CreationStatus awaitCompletion(VdcReturnValueBase result, PollingType pollingType) {
        CreationStatus status = null;
        while (incomplete(status = getAsynchronousStatus(result, pollingType))) {
            delay(MONITOR_DELAY);
        }
        return status;
    }

    protected CreationStatus getAsynchronousStatus(VdcReturnValueBase result) {
        return getVdsmTasksStatus(result);
    }

    protected CreationStatus getAsynchronousStatus(VdcReturnValueBase result, PollingType pollingType) {
        CreationStatus asyncStatus = null;
        if (pollingType==PollingType.JOB) {
            asyncStatus = getJobIdStatus(result);
        } else if (pollingType==PollingType.VDSM_TASKS){
            asyncStatus = getVdsmTasksStatus(result);
        } else {
            throw new IllegalStateException("Unexpected Polling Status");
        }
        return asyncStatus;
    }

    private CreationStatus getVdsmTasksStatus(VdcReturnValueBase result) {
        CreationStatus asyncStatus = null;
        VdcQueryReturnValue monitorResult =
            runQuery(VdcQueryType.GetTasksStatusesByTasksIDs, new GetTasksStatusesByTasksIDsParameters(result.getVdsmTaskIdList()));
        if (monitorResult != null
            && monitorResult.getSucceeded()
            && monitorResult.getReturnValue() != null) {
            Mapper<AsyncTaskStatus, CreationStatus> mapper = getMapper(AsyncTaskStatus.class, CreationStatus.class);
            for (AsyncTaskStatus task : asCollection(AsyncTaskStatus.class, monitorResult.getReturnValue())) {
                asyncStatus = mapper.map(task, asyncStatus);
            }
        }
        return asyncStatus;
    }

    private CreationStatus getJobIdStatus(VdcReturnValueBase result) {
        Guid jobId = result.getJobId();
        if (jobId == null || jobId.equals(Guid.Empty)) {
            return CreationStatus.COMPLETE;
        } else {
            IdQueryParameters params = new IdQueryParameters(jobId);
            VdcQueryReturnValue queryResult = runQuery(VdcQueryType.GetJobByJobId, params);
            if (queryResult != null && queryResult.getSucceeded() && queryResult.getReturnValue() != null) {
                Job job = queryResult.getReturnValue();
                return job.getStatus()==JobExecutionStatus.STARTED ? CreationStatus.IN_PROGRESS : CreationStatus.COMPLETE;
            } else {
                //not supposed to happen
                return CreationStatus.COMPLETE;
            }
        }
    }

    protected boolean incomplete(CreationStatus status) {
        return status == null || status == CreationStatus.PENDING || status == CreationStatus.IN_PROGRESS;
    }

    protected void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            // ignore
        }
    }

    /**
     * Populates the entity with additional information, which is not returned by the main backend query. Checks for
     * "All-Content=true" header before populating. Population logic itself is implemented in doPopulate().
     */
    protected final R populate(R model, Q entity) {
        model = deprecatedPopulate(model, entity);
        return isPopulate() ? doPopulate(model, entity) : model;
    }

    @Deprecated
    protected R deprecatedPopulate(R model, Q entity) {
        return model;
    }

    /**
     * Populates the entity with additional information, which is not returned by the main backend query. Override this
     * method to add population logic to an entity.
     */
    protected R doPopulate(R model, Q entity) {
        return model;
    }

    /**
     * Add any parent resource references needed for constructing links.
     *
     * LinkHelper.addLinks() constructs the 'href' attribute from @model
     * using its 'id' attribute and the 'id' attribute of any parent
     * resources.
     *
     * This method provides the hook through which all sub-resource
     * classes should add references to parent resources so that
     * LinkHelper can do its job.

     * e.g. in order to get a URL like 'clusters/{cid}/networks/{nid}'
     * you would need to have:
     *
     *   protected Network addParents(Network network) {
     *       network.setCluster(new Cluster());
     *       network.getCluster().setId(clusterId);
     *       return network;
     *   }
     *
     * @param model the resource representation
     * @return the model with any parent references added
     */
    protected R addParents(R model) {
        return model;
    }

    protected R addLinks(R model, String... subCollectionMembersToExclude) {
        return addLinks(model, null, subCollectionMembersToExclude);
    }

    protected R addLinks(R model, boolean doNotLinkSubCollections) {
        return addLinks(model, null, doNotLinkSubCollections);
    }

    protected R addLinks(R model, Class<? extends BaseResource> suggestedParent, String... subCollectionMembersToExclude) {
        model = addParents(model);
        // linkSubCollections called first as addLinks unsets the grandparent model
        model = linkSubCollections(model, suggestedParent, subCollectionMembersToExclude);
        model = LinkHelper.addLinks(model, suggestedParent);
        return model;
    }

    protected R addLinks(R model, Class<? extends BaseResource> suggestedParent, boolean doNotLinkSubCollections) {
        return doNotLinkSubCollections?
                LinkHelper.addLinks(addParents(model), suggestedParent)
                :
                addLinks(model, suggestedParent);
    }

    protected List<Q> asCollection(Object o) {
        return asCollection(entityType, o);
    }

    protected String asString(List<Guid> list) {
        StringBuilder builder = new StringBuilder();
        for (Guid id : list) {
            if (builder.length() > 0) {
                builder.append(urlEncode(ID_SEPARATOR));
            }
            builder.append(id.toString());
        }
        return builder.toString();
    }

    protected String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should always be supported
            e.printStackTrace();
            return null;
        }
    }

    protected R newModel(String id) {
        R ret = null;
        try {
            ret = modelType.newInstance();
            ret.setId(id);
            ret = addParents(ret);
        } catch (Exception e) {
            // trivial construction, should not fail
        }
        return ret;
    }

    protected R linkSubResource(R model, String subResource, String oid) {
        String path = LinkHelper.getPath(model);
        if (path != null) {
            String href = String.join("/", path, oid, subResource);
            addOrUpdateLink(model, subResource, href);
        }
        return model;
    }

    protected R linkSubCollections(R model, Class<? extends BaseResource> suggestedParent, String... subCollectionMembersToExclude) {
        if (subCollections != null) {
            String path = LinkHelper.getPath(model, suggestedParent);
            for (String relation : subCollections) {
                if(!shouldExclude(relation, subCollectionMembersToExclude)) {
                    if (path != null) {
                        String href = String.join("/", path, relation);
                        addOrUpdateLink(model, relation, href);
                    }
                }
                else {
                    removeIfExist(model, relation);
                }
            }
        }
        return model;
    }

    private boolean shouldExclude(String member, String[] subCollectionMembersToExclude) {
        if(subCollectionMembersToExclude !=null && subCollectionMembersToExclude.length > 0){
            for(String excludeMember : subCollectionMembersToExclude){
                if (member.equals(excludeMember)) {
                        return true;
                }
            }
        }
        return false;
    }

    protected R injectSearchLinks(R resource, String[] rels){
        for(String rel : rels){
            resource.getLinks().add(LinkCreator.createSearchLink(resource.getHref(), rel));
        }
        return resource;
    }

    protected <B extends BaseResource >void removeIfExist(B model, String relation) {
        List<Link> linksCopy = new ArrayList<>(model.getLinks());

        for (Link link : model.getLinks()) {
            if (link.getRel().equals(relation)) {
                linksCopy.remove(link);
                break;
            }
        }

        model.getLinks().retainAll(linksCopy);
    }

    protected <B extends BaseResource >void addOrUpdateLink(B model, String relation, String href) {
        for (Link link : model.getLinks()) {
            if (link.getRel().equals(relation)) {
                link.setHref(href);
                return;
            }
        }

        Link link = new Link();
        link.setRel(relation);
        link.setHref(href);
        model.getLinks().add(link);
    }

    protected <T> VdcQueryParametersBase getQueryParams(Class<? extends VdcQueryParametersBase> queryParamsClass, T id) {
        VdcQueryParametersBase params = null;
        try {
            params = queryParamsClass.getConstructor(id.getClass()).newInstance(id);
        } catch (Exception e) {
            // trivial class construction
        }
        return params;
    }

    /**
     * Convert a string to a Guid, or return a 404 response.
     *
     * If an invalid UUID is supplied to a sub-resource locator, this
     * method will cause us to return a 404 response via the sub-resource
     * constructor.
     *
     * @param id the incoming UUID
     * @return a Guid
     * @throws WebApplicationException a 404 response, if the UUID is invalid
     */
    protected Guid asGuidOr404(String id) {
        try {
            return asGuid(id);
        } catch (IllegalArgumentException iae) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    protected R notFound() {
        return notFound(modelType);
    }

    protected Q entityNotFound() {
        return notFound(entityType);
    }

    protected <T> T notFound(Class<T> clz) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    protected Guid getHostId(Host host) {
        return host.isSetId()
                ? asGuid(host.getId())
               : getEntity(VDS.class,
                           VdcQueryType.GetVdsByName,
                           new NameQueryParameters(host.getName()),
                           host.getName()).getId();
    }

    protected abstract class EntityIdResolver<T> implements IResolver<T, Q> {

        public abstract Q lookupEntity(T id) throws BackendFailureException;

        @Override
        public Q resolve(T id) throws BackendFailureException {
            Q entity = lookupEntity(id);
            if (entity == null) {
                throw new EntityNotFoundException(id.toString());
            }
            return entity;
        }
    }

    protected abstract class EntityResolver<T> implements IResolver<T, Q> {

        public abstract Q lookupEntity(T id) throws BackendFailureException;

        @Override
        public Q resolve(T id) throws BackendFailureException {
            Q entity = lookupEntity(id);
            if (entity == null) {
                throw new EntityNotFoundException(id.toString());
            }
            return entity;
        }
    }

    public class QueryIdResolver<T> extends EntityIdResolver<T> {

        private final VdcQueryType query;
        private final Class<? extends VdcQueryParametersBase> queryParamsClass;

        public QueryIdResolver(VdcQueryType query, Class<? extends VdcQueryParametersBase> queryParamsClass) {
            this.query = query;
            this.queryParamsClass = queryParamsClass;
        }

        @Override
        public Q lookupEntity(T id) throws BackendFailureException {
            return doGetEntity(entityType, query, getQueryParams(queryParamsClass, id), id.toString());
        }
    }

}
