package org.ovirt.engine.core.common.utils.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterCoreUtil {
    public static final List<String> getQualifiedBrickList(Collection<GlusterBrickEntity> bricks) {
        List<String> qualifiedBricks = new ArrayList<>();
        for (GlusterBrickEntity GlusterBrick : bricks) {
            qualifiedBricks.add(GlusterBrick.getQualifiedName());
        }
        return qualifiedBricks;
    }

    public static final GlusterBrickEntity getBrickByQualifiedName(List<GlusterBrickEntity> bricksList,
            String qualifiedBrickName) {
        for (GlusterBrickEntity brick : bricksList) {
            // Compare the brickname with the existing volume brick
            if (brick.getQualifiedName().equals(qualifiedBrickName)) {
                return brick;
            }
        }
        return null;
    }

    /**
     * Checks if given brick <code>searchBrick</code> exists in the given collection of bricks. Note that this method
     * checks only two (and most important) attributes of the brick: server id and brick directory
     *
     * @return true of the given <code>searchBrick</code> exists in the given collection of bricks, else false
     */
    public static boolean containsBrick(Collection<GlusterBrickEntity> bricks, GlusterBrickEntity searchBrick) {
        return findBrick(bricks, searchBrick) != null;
    }

    /**
     * Checks if given brick <code>searchBrick</code> exists in the given collection of bricks, and returns it if found. Note that this method
     * checks only two (and most important) attributes of the brick: server id and brick directory.
     *
     * @return the brick if found in the <code>bricks</code> collection, else null.
     */
    public static GlusterBrickEntity findBrick(Collection<GlusterBrickEntity> bricks, GlusterBrickEntity searchBrick) {
        return findBrick(bricks, searchBrick.getServerId(), searchBrick.getBrickDirectory());
    }

    /**
     * Checks if a brick with given server id and brick directory exists in the given collection of bricks, and returns
     * it if found.
     */
    public static GlusterBrickEntity findBrick(Collection<GlusterBrickEntity> bricks, Guid serverId, String brickDir) {
        for (GlusterBrickEntity brick : bricks) {
            if (brick.getServerId().equals(serverId)
                    && brick.getBrickDirectory().equals(brickDir)) {
                return brick;
            }
        }
        return null;
    }
}
