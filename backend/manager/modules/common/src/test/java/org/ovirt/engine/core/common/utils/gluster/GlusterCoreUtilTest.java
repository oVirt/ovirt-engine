package org.ovirt.engine.core.common.utils.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterCoreUtilTest {
    private static final String SERVER_1 = "server1";
    private static final String SERVER_2 = "server2";
    private static final String SERVER_3 = "server3";
    private static final String DIR_1 = "dir1";
    private static final String DIR_2 = "dir2";
    private static final String DIR_3 = "dir3";
    private static final Guid UUID_1 = Guid.newGuid();
    private static final Guid UUID_2 = Guid.newGuid();
    private static final Guid UUID_3 = Guid.newGuid();
    private static final GlusterBrickEntity brick1 = createBrick(UUID_1, SERVER_1, DIR_1);
    private static final GlusterBrickEntity brick2 = createBrick(UUID_2, SERVER_2, DIR_2);
    private static final GlusterBrickEntity brick3 = createBrick(UUID_3, SERVER_3, DIR_3);

    @Test
    public void testGetQualifiedBrickList() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(brick1);
        bricks.add(brick2);
        bricks.add(brick3);

        List<String> qualifiedBrickList = GlusterCoreUtil.getQualifiedBrickList(bricks);

        assertEquals(3, qualifiedBrickList.size());
        assertTrue(qualifiedBrickList.contains(SERVER_1 + ":" + DIR_1));
        assertTrue(qualifiedBrickList.contains(SERVER_2 + ":" + DIR_2));
        assertTrue(qualifiedBrickList.contains(SERVER_3 + ":" + DIR_3));
    }

    @Test
    public void testGetBrickByQualifiedName() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(brick1);
        bricks.add(brick2);
        bricks.add(brick3);
        GlusterBrickEntity brick = GlusterCoreUtil.getBrickByQualifiedName(bricks, SERVER_1 + ":" + DIR_1);

        assertNotNull(brick);
        assertEquals(brick1.getId(), brick.getId());
    }

    @Test
    public void testContainsBrick() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(brick1);
        bricks.add(brick2);

        assertTrue(GlusterCoreUtil.containsBrick(bricks, brick1));
        assertTrue(GlusterCoreUtil.containsBrick(bricks, brick2));
        assertFalse(GlusterCoreUtil.containsBrick(bricks, brick3));
    }

    @Test
    public void testFindBrick() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(brick1);
        bricks.add(brick2);

        assertNotNull(GlusterCoreUtil.findBrick(bricks, brick1));
        assertNotNull(GlusterCoreUtil.findBrick(bricks, brick2));
        assertNull(GlusterCoreUtil.findBrick(bricks, brick3));
    }

    private static GlusterBrickEntity createBrick(Guid serverId, String serverName, String brickDir) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(Guid.newGuid());
        brick.setServerId(serverId);
        brick.setServerName(serverName);
        brick.setBrickDirectory(brickDir);
        return brick;
    }
}
