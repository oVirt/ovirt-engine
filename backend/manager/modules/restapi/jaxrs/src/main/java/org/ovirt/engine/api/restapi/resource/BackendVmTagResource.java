package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmTagResource extends AbstractBackendSubResource<Tag, Tags> implements AssignedTagResource {
    private Guid vmId;

    public BackendVmTagResource(Guid vmId, String tagId) {
        super(tagId, Tag.class, Tags.class);
        this.vmId = vmId;
    }

    @Override
    public Tag get() {
        List<Tags> tags = getBackendCollection(
            Tags.class,
            QueryType.GetTagsByVmId,
            new GetTagsByVmIdParameters(vmId.toString())
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
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        tag.setVm(vm);
        return tag;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.DetachVmFromTag, new AttachEntityToTagParameters(guid, asList(vmId)));
    }
}
