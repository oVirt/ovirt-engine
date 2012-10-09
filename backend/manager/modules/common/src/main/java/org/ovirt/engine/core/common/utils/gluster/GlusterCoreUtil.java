package org.ovirt.engine.core.common.utils.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;

public class GlusterCoreUtil {
    public static final List<String> getQualifiedBrickList(Collection<GlusterBrickEntity> bricks) {
        List<String> qualifiedBricks = new ArrayList<String>();
        for (GlusterBrickEntity GlusterBrick : bricks) {
            qualifiedBricks.add(GlusterBrick.getQualifiedName());
        }
        return qualifiedBricks;
    }
}
