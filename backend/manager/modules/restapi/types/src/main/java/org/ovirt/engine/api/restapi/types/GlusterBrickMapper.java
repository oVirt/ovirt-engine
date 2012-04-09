package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Mapper for mapping between the REST model class {@link GlusterBrick} and the business entity class
 * {@link GlusterBrickEntity}
 *
 * @see GlusterBrick
 * @see GlusterBrickEntity
 */
public class GlusterBrickMapper {
    @Mapping(from = GlusterBrick.class, to = GlusterBrickEntity.class)
    public static GlusterBrickEntity map(GlusterBrick fromBrick, GlusterBrickEntity toBrick) {
        GlusterBrickEntity brick = (toBrick == null) ? new GlusterBrickEntity() : toBrick;
        if(fromBrick.isSetServerId()) {
            brick.setServerId(Guid.createGuidFromString(fromBrick.getServerId()));
        }
        if(fromBrick.isSetBrickDir()) {
            brick.setBrickDirectory(fromBrick.getBrickDir());
        }
        return brick;
    }

    @Mapping(from = GlusterBrickEntity.class, to = GlusterBrick.class)
    public static GlusterBrick map(GlusterBrickEntity fromBrick, GlusterBrick toBrick) {
        GlusterBrick brick = (toBrick == null) ? new GlusterBrick() : toBrick;

        if(fromBrick.getServerId() != null) {
            brick.setServerId(fromBrick.getServerId().toString());
        }

        if(fromBrick.getBrickDirectory() != null) {
            brick.setBrickDir(fromBrick.getBrickDirectory());
        }

        if(fromBrick.getStatus() != null) {
            brick.setState(fromBrick.getStatus().name());
        }
        return brick;
    }
}
