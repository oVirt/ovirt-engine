package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
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

        entity.setVms(new Vms());
        entity.setHosts(new Hosts());

        model.getVms().stream().forEach(vm -> {
            Vm restVm = new Vm();
            restVm.setId(vm.toString());
            entity.getVms().getVms().add(restVm);
        });

        model.getHosts().stream().forEach(host -> {
            Host restHost = new Host();
            restHost.setId(host.toString());
            entity.getHosts().getHosts().add(restHost);
        });

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

        if (model.isSetVms() && model.getVms().isSetVms()) {
            model.getVms().getVms().stream().forEach(v -> entity.vm(GuidUtils.asGuid(v.getId())));
        }

        if (model.isSetHosts() && model.getHosts().isSetHosts()) {
            model.getHosts().getHosts().stream().forEach(v -> entity.host(GuidUtils.asGuid(v.getId())));
        }

        return entity.build();
    }
}
