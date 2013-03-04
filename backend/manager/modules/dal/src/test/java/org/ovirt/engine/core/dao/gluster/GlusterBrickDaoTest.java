package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;

public class GlusterBrickDaoTest extends BaseDAOTestCase {
    private static final Guid SERVER_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid EXISTING_VOL_ID = new Guid("0c3f45f6-3fe9-4b35-a30c-be0d1a835ea8");
    private static final Guid EXISTING_BRICK_ID = new Guid("6ccdc294-d77b-4929-809d-8afe7634b47d");

    private static final Guid BRICK_SERVER_ID = new Guid("23f6d691-5dfb-472b-86dc-9e1d2d3c18f3");
    private static final String BRICK_DIRECTORY = "/export/test-vol-distribute-1/dir1";

    private GlusterBrickDao dao;
    private VdsStatic server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterBrickDao();
        server = dbFacade.getVdsStaticDao().get(SERVER_ID);
    }

    @Test
    public void testSaveAndGetById() {
        GlusterBrickEntity brickToAdd = new GlusterBrickEntity(EXISTING_VOL_ID,
                server,
                "/export/test-vol-distribute-1/dir3",
                GlusterStatus.UP);
        brickToAdd.setBrickOrder(0);

        dao.save(brickToAdd);

        GlusterBrickEntity retrievedBrick = dao.getById(brickToAdd.getId());
        assertNotNull(retrievedBrick);
        assertEquals(brickToAdd, retrievedBrick);
    }

    @Test
    public void testGlusterVolumeBricksByServerId() {
        List<GlusterBrickEntity> bricks = dao.getGlusterVolumeBricksByServerId(SERVER_ID);
        assertNotNull(bricks);
    }

    @Test
    public void testGetBrickByServerIdAndDirectory() {
        GlusterBrickEntity brick = dao.getBrickByServerIdAndDirectory(BRICK_SERVER_ID, BRICK_DIRECTORY);
        assertNotNull(brick);
        assertEquals(brick.getServerId(), BRICK_SERVER_ID);
        assertEquals(brick.getBrickDirectory(), BRICK_DIRECTORY);
    }

    @Test
    public void testRemove() {
        GlusterBrickEntity existingBrick = dao.getById(EXISTING_BRICK_ID);
        assertNotNull(existingBrick);

        dao.removeBrick(EXISTING_BRICK_ID);

        assertNull(dao.getById(EXISTING_BRICK_ID));
    }

    @Test
    public void testRemoveMultiple() {
        List<GlusterBrickEntity> bricks = dao.getBricksOfVolume(EXISTING_VOL_ID);
        assertEquals(2, bricks.size());

        List<Guid> idsToRemove = new ArrayList<Guid>();
        idsToRemove.add(bricks.get(0).getId());
        idsToRemove.add(bricks.get(1).getId());
        dao.removeAll(idsToRemove);

        bricks = dao.getBricksOfVolume(EXISTING_VOL_ID);
        assertTrue(bricks.isEmpty());
    }

    @Test
    public void testReplaceBrick() {
        GlusterBrickEntity firstBrick = dao.getById(EXISTING_BRICK_ID);
        assertNotNull(firstBrick);

        GlusterBrickEntity newBrick =
                new GlusterBrickEntity(EXISTING_VOL_ID,
                        server,
                        "/export/test-vol-distribute-1/dir3",
                        GlusterStatus.UP);
        newBrick.setBrickOrder(0);

        assertNull(dao.getById(newBrick.getId()));

        dao.replaceBrick(firstBrick, newBrick);

        assertNull(dao.getById(EXISTING_BRICK_ID));

        GlusterBrickEntity retrievedBrick = dao.getById(newBrick.getId());
        assertNotNull(retrievedBrick);
        assertEquals(newBrick, retrievedBrick);
    }

    @Test
    public void testUpdateBrickStatus() {
        GlusterBrickEntity existingBrick = dao.getById(EXISTING_BRICK_ID);
        assertNotNull(existingBrick);
        assertEquals(GlusterStatus.UP, existingBrick.getStatus());

        dao.updateBrickStatus(EXISTING_BRICK_ID, GlusterStatus.DOWN);

        existingBrick = dao.getById(EXISTING_BRICK_ID);
        assertNotNull(existingBrick);
        assertEquals(GlusterStatus.DOWN, existingBrick.getStatus());
    }
}
