package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.NetworkLabel;

public class NetworkLabelMapper {
    @Mapping(from = NetworkLabel.class,
        to = org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class)
    public static org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel map(NetworkLabel model,
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel template) {
        org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel entity = template != null ? template :
            new org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel();
        if (model.isSetId()) {
            entity.setId(model.getId());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class,
        to = NetworkLabel.class)
    public static NetworkLabel map(org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel entity,
            NetworkLabel template) {
        NetworkLabel model = template != null ? template : new NetworkLabel();
        if (entity.getId() != null) {
            model.setId(entity.getId());
        }
        return model;
    }
}
