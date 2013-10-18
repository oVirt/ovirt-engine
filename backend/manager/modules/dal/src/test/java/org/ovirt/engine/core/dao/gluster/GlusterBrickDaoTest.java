package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAOTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class GlusterBrickDaoTest extends BaseDAOTestCase {
    private GlusterBrickDao dao;
    private VdsStatic server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getGlusterBrickDao();
        server = dbFacade.getVdsStaticDao().get(FixturesTool.GLUSTER_SERVER_UUID3);
    }

    @Test
    public void testSaveAndGetById() {
        GlusterBrickEntity brickToAdd = new GlusterBrickEntity();
        brickToAdd.setVolumeId(FixturesTool.GLUSTER_VOLUME_UUID1);
        brickToAdd.setServerId(server.getId());
        brickToAdd.setServerName(server.getHostName());
        brickToAdd.setBrickDirectory("/export/test-vol-distribute-1/dir3");
        brickToAdd.setStatus(GlusterStatus.UP);
        brickToAdd.setBrickOrder(0);

        dao.save(brickToAdd);

        GlusterBrickEntity retrievedBrick = dao.getById(brickToAdd.getId());
        assertNotNull(retrievedBrick);
        assertEquals(brickToAdd, retrievedBrick);
    }

    @Test
    public void testGlusterVolumeBricksByServerId() {
        List<GlusterBrickEntity> bricks = dao.getGlusterVolumeBricksByServerId(FixturesTool.GLUSTER_SERVER_UUID3);
        assertNotNull(bricks);
    }

    @Test
    public void testGetBrickByServerIdAndDirectory() {
        GlusterBrickEntity brick =
                dao.getBrickByServerIdAndDirectory(FixturesTool.GLUSTER_BRICK_SERVER1, FixturesTool.GLUSTER_BRICK_DIR1);
        assertNotNull(brick);
        assertEquals(brick.getServerId(), FixturesTool.GLUSTER_BRICK_SERVER1);
        assertEquals(brick.getBrickDirectory(), FixturesTool.GLUSTER_BRICK_DIR1);
    }

    @Test
    public void testRemove() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);

        dao.removeBrick(FixturesTool.GLUSTER_BRICK_UUID1);

        assertNull(dao.getById(FixturesTool.GLUSTER_BRICK_UUID1));
    }

    @Test
    public void testRemoveMultiple() {
        List<GlusterBrickEntity> bricks = dao.getBricksOfVolume(FixturesTool.GLUSTER_VOLUME_UUID1);
        assertEquals(2, bricks.size());

        List<Guid> idsToRemove = new ArrayList<Guid>();
        idsToRemove.add(bricks.get(0).getId());
        idsToRemove.add(bricks.get(1).getId());
        dao.removeAll(idsToRemove);

        bricks = dao.getBricksOfVolume(FixturesTool.GLUSTER_VOLUME_UUID1);
        assertTrue(bricks.isEmpty());
    }

    @Test
    public void testReplaceBrick() {
        GlusterBrickEntity firstBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(firstBrick);

        GlusterBrickEntity newBrick = new GlusterBrickEntity();
        newBrick.setVolumeId(FixturesTool.GLUSTER_VOLUME_UUID1);
        newBrick.setServerId(server.getId());
        newBrick.setServerName(server.getHostName());
        newBrick.setBrickDirectory("/export/test-vol-distribute-1/dir3");
        newBrick.setStatus(GlusterStatus.UP);
        newBrick.setBrickOrder(0);

        assertNull(dao.getById(newBrick.getId()));

        dao.replaceBrick(firstBrick, newBrick);

        assertNull(dao.getById(FixturesTool.GLUSTER_BRICK_UUID1));

        GlusterBrickEntity retrievedBrick = dao.getById(newBrick.getId());
        assertNotNull(retrievedBrick);
        assertEquals(newBrick, retrievedBrick);
    }

    @Test
    public void testUpdateBrickStatus() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertEquals(GlusterStatus.UP, existingBrick.getStatus());

        dao.updateBrickStatus(FixturesTool.GLUSTER_BRICK_UUID1, GlusterStatus.DOWN);

        existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertEquals(GlusterStatus.DOWN, existingBrick.getStatus());
    }

    @Test
    public void testUpdateBrickStatuses() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity existingBrick1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);
        assertNotNull(existingBrick);
        assertNotNull(existingBrick1);
        assertEquals(GlusterStatus.UP, existingBrick.getStatus());
        assertEquals(GlusterStatus.UP, existingBrick1.getStatus());

        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        bricks.add(existingBrick);
        bricks.add(existingBrick1);
        dao.updateBrickStatuses(bricks);
    }

    @Test
    public void testUpdateBrickTask() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a763"));

        assertNotNull(existingBrick);
        assertEquals(GlusterStatus.UP, existingBrick.getStatus());

        dao.updateBrickTask(existingBrick.getId(), FixturesTool.GLUSTER_ASYNC_TASK_ID1);

        GlusterBrickEntity newEnity = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);

        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEnity.getAsyncTask().getTaskId());
    }

    @Test
    public void testUpdateBrickTasksInBatch() {
        GlusterBrickEntity existingBrick1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity existingBrick2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);

        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a763"));

        assertNotNull(existingBrick1);
        assertNotNull(existingBrick2);
        assertEquals(GlusterStatus.UP, existingBrick1.getStatus());
        assertEquals(GlusterStatus.UP, existingBrick2.getStatus());

        existingBrick1.setAsyncTask(asyncTask);
        existingBrick2.setAsyncTask(asyncTask);

        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(existingBrick1);
        bricks.add(existingBrick2);

        dao.updateBrickTasksInBatch(bricks);

        GlusterBrickEntity newEnity1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity newEnity2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);

        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEnity1.getAsyncTask().getTaskId());
        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEnity2.getAsyncTask().getTaskId());
    }

    @Test
    public void testRemoveBricksInBatch() {
        GlusterBrickEntity existingBrick1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity existingBrick2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);

        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(existingBrick1);
        bricks.add(existingBrick2);

        dao.removeAllInBatch(bricks);

        GlusterBrickEntity newEnity1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity newEnity2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);
        assertNull(newEnity1);
        assertNull(newEnity2);
    }

    @Test
    public void testUpdateBrickTaskByHostIdBrickDir() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(FixturesTool.GLUSTER_ASYNC_TASK_ID1);

        dao.updateBrickTaskByHostIdBrickDir(existingBrick.getServerId(), existingBrick.getBrickDirectory(), FixturesTool.GLUSTER_ASYNC_TASK_ID1);

        GlusterBrickEntity newEnity = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);

        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEnity.getAsyncTask().getTaskId());
    }

    @Test
    public void testUpdateAllBrickTasksByHostIdBrickDir() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity existingBrick2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(FixturesTool.GLUSTER_ASYNC_TASK_ID1);

        GlusterBrickEntity updateBrick = new GlusterBrickEntity();
        updateBrick.setBrickDirectory(existingBrick.getBrickDirectory());
        updateBrick.setServerId(existingBrick.getServerId());
        updateBrick.setAsyncTask(asyncTask);

        GlusterBrickEntity updateBrick2 = new GlusterBrickEntity();
        updateBrick2.setBrickDirectory(existingBrick2.getBrickDirectory());
        updateBrick2.setServerId(existingBrick2.getServerId());
        updateBrick2.setAsyncTask(asyncTask);

        List<GlusterBrickEntity> bricks = new ArrayList<>();
        bricks.add(updateBrick);
        bricks.add(updateBrick2);


        dao.updateAllBrickTasksByHostIdBrickDirInBatch(bricks);

        GlusterBrickEntity newEntity1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity newEntity2 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);

        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEntity1.getAsyncTask().getTaskId());
        assertEquals(FixturesTool.GLUSTER_ASYNC_TASK_ID1, newEntity2.getAsyncTask().getTaskId());
    }
}
