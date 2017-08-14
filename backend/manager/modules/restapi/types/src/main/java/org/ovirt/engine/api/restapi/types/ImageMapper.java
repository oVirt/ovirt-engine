package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMapper {
    private static final Logger log = LoggerFactory.getLogger(ImageMapper.class);

    @Mapping(from = RepoImage.class, to = Image.class)
    public static Image map(RepoImage entity, Image template) {
        Image model = template != null ? template : new Image();
        model.setId(entity.getRepoImageId());
        model.setName(entity.getRepoImageName());
        model.setSize(entity.getSize());
        model.setType(mapImageType(entity.getFileType()));

        return model;
    }

    public static ImageFileType mapImageType(org.ovirt.engine.core.common.businessentities.storage.ImageFileType imageFileType) {
        switch (imageFileType) {
        case ISO:
            return ImageFileType.ISO;
        case Floppy:
            return ImageFileType.FLOPPY;
        case Disk:
            return ImageFileType.DISK;
        case Unknown:
            // Explicitly map UNKNOWN to null, as it doesn't make
            // sense in the API.
            return null;
        default:
            log.warn("Don't know how to map image type '{}', will return null.", imageFileType);
            return null;
        }
    }
}
