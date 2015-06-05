package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.core.Response;
import java.util.List;

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
            VdcQueryType.GetTagsByVmId,
            new GetTagsByVmIdParameters(vmId.toString())
        );
        for (Tags tag : tags) {
            if (tag.gettag_id().equals(guid)) {
                return addLinks(populate(map(tag, null), tag));
            }
        }
        return notFound();
    }

    @Override
    protected Tag doPopulate(Tag model, Tags entity) {
        return model;
    }

    @Override
    protected Tag addParents(Tag tag) {
        VM vm = new VM();
        vm.setId(vmId.toString());
        tag.setVm(vm);
        return tag;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.DetachVmFromTag, new AttachEntityToTagParameters(guid, asList(vmId)));
    }
}
