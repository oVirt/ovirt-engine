package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;

public class ImageMapper {
    @Mapping(from = RepoImage.class, to = Image.class)
    public static Image map(RepoImage entity, Image template) {
        Image model = template != null ? template : new Image();
        model.setId(entity.getRepoImageId());
        model.setName(entity.getRepoImageName());
        return model;
    }
}
