package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.File;

public class FileMapper {
    @Mapping(from = String.class, to = File.class)
    public static File map(String entity, File template) {
        File model = template != null ? template : new File();
        model.setId(entity);
        model.setName(entity);
        return model;
    }
}
