package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;

public class LabelMapper {
    @Mapping(from = Label.class, to = NetworkLabel.class)
    public static NetworkLabel map(Label model, NetworkLabel template) {
        NetworkLabel entity = template != null ? template : new NetworkLabel();
        if (model.isSetId()) {
            entity.setId(model.getId());
        }

        return entity;
    }

    @Mapping(from = NetworkLabel.class, to = Label.class)
    public static Label map(NetworkLabel entity, Label template) {
        Label model = template != null ? template : new Label();
        if (entity.getId() != null) {
            model.setId(entity.getId());
        }

        return model;
    }
}
