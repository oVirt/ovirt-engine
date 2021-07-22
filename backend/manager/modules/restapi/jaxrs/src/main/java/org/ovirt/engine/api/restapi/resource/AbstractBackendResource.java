package org.ovirt.engine.api.restapi.resource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.rsdl.ServiceTree;
import org.ovirt.engine.api.rsdl.ServiceTreeNode;
import org.ovirt.engine.api.utils.LinkCreator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendResource<R extends BaseResource, Q>
    extends BackendResource {

    protected static final String ID_SEPARATOR = ",";
    protected static final long MONITOR_DELAY = 1000L;
    protected static final javax.ws.rs.core.Response.Status ACCEPTED_STATUS =
        javax.ws.rs.core.Response.Status.ACCEPTED;

    protected Class<R> modelType;
    protected Class<Q> entityType;
    protected String[] subCollections;

    public enum PollingType {
        VDSM_TASKS, JOB;
    }

    protected AbstractBackendResource(Class<R> modelType, Class<Q> entityType) {
        this.modelType = modelType;
        this.entityType = entityType;
        this.subCollections = getSubCollections();
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

    protected CreationStatus awaitCompletion(ActionReturnValue result) {
        return awaitCompletion(result, PollingType.VDSM_TASKS);
    }

    protected CreationStatus awaitCompletion(ActionReturnValue result, PollingType pollingType) {
        CreationStatus status = null;
        while (incomplete(status = getAsynchronousStatus(result, pollingType))) {
            delay(MONITOR_DELAY);
        }
        return status;
    }

    protected CreationStatus getAsynchronousStatus(ActionReturnValue result) {
        return getVdsmTasksStatus(result);
    }

    protected CreationStatus getAsynchronousStatus(ActionReturnValue result, PollingType pollingType) {
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

    private CreationStatus getVdsmTasksStatus(ActionReturnValue result) {
        CreationStatus asyncStatus = null;
        QueryReturnValue monitorResult =
            runQuery(QueryType.GetTasksStatusesByTasksIDs, new GetTasksStatusesByTasksIDsParameters(result.getVdsmTaskIdList()));
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

    protected CreationStatus getJobIdStatus(ActionReturnValue result) {
        Guid jobId = result.getJobId();
        if (jobId == null || jobId.equals(Guid.Empty)) {
            return CreationStatus.COMPLETE;
        } else {
            IdQueryParameters params = new IdQueryParameters(jobId);
            QueryReturnValue queryResult = runQuery(QueryType.GetJobByJobId, params);
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
            String href = String.join("/", path, subResource, oid);
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
                } else {
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

    protected <T> QueryParametersBase getQueryParams(Class<? extends QueryParametersBase> queryParamsClass, T id) {
        QueryParametersBase params = null;
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

    /**
     * This method will raise an WebFaultException with message describing the entity was not found.
     *
     * @param name The name of the entity
     */
    protected void notFound(String name) {
        throw new WebFaultException(
            null,
            localize(Messages.BACKEND_FAILED),
            localize(Messages.ENTITY_NOT_FOUND_TEMPLATE, name),
            Response.Status.NOT_FOUND
        );
    }

    protected <T> T notFound(Class<T> clz) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    protected Guid getHostId(Host host) {
        Guid clusterId = host.getCluster() != null ? GuidUtils.asGuid(host.getCluster().getId()) : null;
        return host.isSetId()
                ? asGuid(host.getId())
               : getEntity(VDS.class,
                           QueryType.GetVdsByName,
                           new IdAndNameQueryParameters(clusterId, host.getName()),
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

        private final QueryType query;
        private final Class<? extends QueryParametersBase> queryParamsClass;

        public QueryIdResolver(QueryType query, Class<? extends QueryParametersBase> queryParamsClass) {
            this.query = query;
            this.queryParamsClass = queryParamsClass;
        }

        @Override
        public Q lookupEntity(T id) throws BackendFailureException {
            return doGetEntity(entityType, query, getQueryParams(queryParamsClass, id), id.toString());
        }
    }

    /**
     * Returns an array with the names of subcollections of this resource,
     * e.g, for BackendVmResource: ["affinitylabels", "applications", "cdroms",
     * "diskattachments", "graphicsconsoles", "hostdevices", "katelloerrata"...]
     */
    protected String[] getSubCollections() {
        Set<Class<?>> interfaces = getInterfaces();
        List<String> subCollections = new ArrayList<>();
        for (Class<?> clazz : interfaces) {
            ServiceTreeNode node = getRequiredNode(clazz);
            if (node != null) {
                addSubCollections(node, subCollections);
            }
        }
        return subCollections.toArray(new String[0]);
    }

    /**
     * Returns the set of service interfaces implemented by this resource. This needs to make a recursive search in
     * order to find the interfaces implemented directly and also the interfaces implemented by base classes.
     */
    private Set<Class<?>> getInterfaces() {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> clazz = getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            for (Class<?> iface : clazz.getInterfaces()) {
                if (iface.getName().endsWith("Resource")) {
                    result.add(iface);
                }
            }
        }
        return result;
    }

    /**
     * Get the node in the API ServiceTree which contains sub-collections info.
     * For 'collection' services (such as VmsService) the sub-collections info
     * is held in the 'son' node (VmService). So for both the collection and
     * 'single' service, the node of the 'single' service is returned.
     */
    private ServiceTreeNode getRequiredNode(Class<?> clazz) {
        ServiceTreeNode node = ServiceTree.getNode(clazz);
        return node==null ? null : node.getSon()!=null ? node.getSon() : node;
    }

    private static void addSubCollections(ServiceTreeNode node, List<String> subCollections) {
        for (ServiceTreeNode innerNode : node.getSubServices()) {
            subCollections.add(innerNode.getPath());
        }
    }

}
