package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterState;
import org.ovirt.engine.api.model.GlusterVolume;
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

        if(fromBrick.isSetId()) {
            brick.setId(Guid.createGuidFromStringDefaultEmpty(fromBrick.getId()));
        }

        if(fromBrick.isSetServerId()) {
            brick.setServerId(Guid.createGuidFromStringDefaultEmpty(fromBrick.getServerId()));
        }

        if(fromBrick.isSetBrickDir()) {
            brick.setBrickDirectory(fromBrick.getBrickDir());
        }
        return brick;
    }

    @Mapping(from = GlusterBrickEntity.class, to = GlusterBrick.class)
    public static GlusterBrick map(GlusterBrickEntity fromBrick, GlusterBrick toBrick) {
        GlusterBrick brick = (toBrick == null) ? new GlusterBrick() : toBrick;

        if(fromBrick.getId() != null) {
            brick.setId(fromBrick.getId().toString());
        }

        if(fromBrick.getServerId() != null) {
            brick.setServerId(fromBrick.getServerId().toString());
        }

        if(StringUtils.isNotEmpty(fromBrick.getQualifiedName())) {
           brick.setName(fromBrick.getQualifiedName());
        }

        if(fromBrick.getBrickDirectory() != null) {
            brick.setBrickDir(fromBrick.getBrickDirectory());
        }

        if(fromBrick.getStatus() != null) {
            brick.setStatus(StatusUtils.create(map(fromBrick.getStatus(), null)));
        }

        if(fromBrick.getVolumeId() != null) {
            brick.setGlusterVolume(new GlusterVolume());
            brick.getGlusterVolume().setId(fromBrick.getVolumeId().toString());
        }
        return brick;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus.class, to = GlusterState.class)
    public static GlusterState map(org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus glusterVolumeStatus,
            String template) {
        switch (glusterVolumeStatus) {
        case UP:
            return GlusterState.UP;
        case DOWN:
            return GlusterState.DOWN;
        default:
            return null;
        }
    }
}
