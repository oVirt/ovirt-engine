package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostTagResource extends AbstractBackendSubResource<Tag, Tags> implements AssignedTagResource {
    private Guid hostId;

    public BackendHostTagResource(Guid hostId, String tagId) {
        super(tagId, Tag.class, Tags.class);
        this.hostId = hostId;
    }

    @Override
    public Tag get() {
        List<Tags> tags = getBackendCollection(
            Tags.class,
            QueryType.GetTagsByVdsId,
            new GetTagsByVdsIdParameters(hostId.toString())
        );
        for (Tags tag : tags) {
            if (tag.getTagId().equals(guid)) {
                return addLinks(populate(map(tag, null), tag));
            }
        }
        return notFound();
    }

    @Override
    protected Tag addParents(Tag tag) {
        Host host = new Host();
        host.setId(hostId.toString());
        tag.setHost(host);
        return tag;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.DetachVdsFromTag, new AttachEntityToTagParameters(guid, asList(hostId)));
    }
}
