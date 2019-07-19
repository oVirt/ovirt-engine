package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;

public class AffinityLabelMapper {
    @Mapping(from = Label.class, to = org.ovirt.engine.api.model.AffinityLabel.class)
    public static org.ovirt.engine.api.model.AffinityLabel map(Label model, org.ovirt.engine.api.model.AffinityLabel template) {
        org.ovirt.engine.api.model.AffinityLabel entity = template != null ? template : new org.ovirt.engine.api.model.AffinityLabel();

        entity.setId(model.getId().toString());
        entity.setName(model.getName());
        entity.setReadOnly(model.isReadOnly());
        entity.setHasImplicitAffinityGroup(model.isImplicitAffinityGroup());

        return entity;
    }

    @Mapping(from = org.ovirt.engine.api.model.AffinityLabel.class, to = Label.class)
    public static Label map(org.ovirt.engine.api.model.AffinityLabel model, Label template) {
        LabelBuilder entity = template != null ? new LabelBuilder(template) : new LabelBuilder();

        if (model.isSetId()) {
            entity.id(GuidUtils.asGuid(model.getId()));
        }

        if (model.isSetName()) {
            entity.name(model.getName());
        }

        if (model.isSetReadOnly()) {
            entity.readOnly(model.isReadOnly());
        }

        if (model.isSetHasImplicitAffinityGroup()) {
            entity.implicitAffinityGroup(model.isHasImplicitAffinityGroup());
        }

        if (model.isSetVms() && model.getVms().isSetVms()) {
            model.getVms().getVms().forEach(vm -> entity.vm(GuidUtils.asGuid(vm.getId())));
        }

        if (model.isSetHosts() && model.getHosts().isSetHosts()) {
            model.getHosts().getHosts().forEach(host -> entity.host(GuidUtils.asGuid(host.getId())));
        }

        return entity.build();
    }
}
