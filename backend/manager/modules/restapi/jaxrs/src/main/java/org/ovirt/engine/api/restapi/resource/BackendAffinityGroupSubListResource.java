package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendAffinityGroupSubListResource<R extends BaseResource, Q>
        extends AbstractBackendCollectionResource<R, Q> {

    private final Guid affinityGroupId;

    public BackendAffinityGroupSubListResource(Guid affinityGroupId, Class<R> modelType,
            Class<Q> entityType) {
        super(modelType, entityType);
        this.affinityGroupId = affinityGroupId;
    }

    protected List<R> listResources(Function<AffinityGroup, List<Guid>> idsExtractor,
            Function<AffinityGroup, List<String>> namesExtractor,
            BiFunction<Guid, String, R> resourceCreator) {
        AffinityGroup affinityGroup = getAffinityGroup();

        List<Guid> ids = idsExtractor.apply(affinityGroup);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> names = namesExtractor.apply(affinityGroup);
        List<R> result = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); ++i) {
            R resource = resourceCreator.apply(ids.get(i), names.get(i));
            resource = addLinks(populate(resource, null));
            // remove vm actions, not relevant to this context
            resource.setActions(null);
            result.add(resource);
        }

        return result;
    }

    public Guid getAffinityGroupId() {
        return affinityGroupId;
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getAffinityGroup() {
        AffinityGroup affinityGroup = getEntity(AffinityGroup.class,
                QueryType.GetAffinityGroupById,
                new IdQueryParameters(affinityGroupId),
                affinityGroupId.toString());
        if (affinityGroup == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return affinityGroup;
    }
}
