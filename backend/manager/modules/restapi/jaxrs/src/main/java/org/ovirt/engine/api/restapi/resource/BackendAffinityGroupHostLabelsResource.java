package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.model.AffinityLabels;
import org.ovirt.engine.api.resource.AffinityGroupHostLabelResource;
import org.ovirt.engine.api.resource.AffinityGroupHostLabelsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupHostLabelsResource
        extends BackendAffinityGroupSubListResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AffinityGroupHostLabelsResource {

    public BackendAffinityGroupHostLabelsResource(Guid affinityGroupId) {
        super(affinityGroupId, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class);
    }

    @Override
    public AffinityLabels list() {
        AffinityLabels affinityLabels = new AffinityLabels();
        affinityLabels.getAffinityLabels().addAll(listResources(
                AffinityGroup::getHostLabels,
                AffinityGroup::getHostLabelNames,
                (id, name) -> {
                    AffinityLabel label = new AffinityLabel();
                    label.setId(id.toString());
                    label.setName(name);
                    return label;
                }));

        return affinityLabels;
    }

    @Override
    public Response add(AffinityLabel label) {
        return performAction(ActionType.AddHostLabelToAffinityGroup,
                new AffinityGroupMemberChangeParameters(getAffinityGroupId(), asGuid(label.getId())));
    }

    @Override
    public AffinityGroupHostLabelResource getLabelResource(String id) {
        return inject(new BackendAffinityGroupHostLabelResource(getAffinityGroupId(), id));
    }
}
