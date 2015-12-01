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
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class GlusterBrickDaoTest extends BaseDaoTestCase {
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
    public void testAddBrickProperties() {
        Guid brickId = new Guid("6ccdc294-d77b-4929-809d-8afe7634b47d");
        GlusterBrickEntity brickBefore = dao.getById(brickId);
        assertNotNull(brickBefore);
        assertNull(brickBefore.getBrickProperties());

        BrickProperties brickProperties = new BrickProperties();
        brickProperties.setBrickId(brickId);
        brickProperties.setFreeSize(Long.valueOf("75000"));
        brickProperties.setTotalSize(Long.valueOf("250000"));
        dao.addBrickProperties(brickProperties);

        GlusterBrickEntity brickAfter = dao.getById(brickId);
        assertNotNull(brickAfter);
        assertNotNull(brickAfter.getBrickProperties());
        assertTrue(brickAfter.getBrickProperties().getTotalSize() == 250000);
        assertTrue(brickAfter.getBrickProperties().getFreeSize() == 75000);
    }

    @Test
    public void testAddAllBrickProperties() {
        // 61c94fc7-26b0-43e3-9d26-fc9d8cd6a754
        Guid brickId1 = new Guid("6ccdc294-d77b-4929-809d-8afe7634b47d");
        Guid brickId2 = new Guid("61c94fc7-26b0-43e3-9d26-fc9d8cd6a754");
        GlusterBrickEntity brick1 = dao.getById(brickId1);
        GlusterBrickEntity brick2 = dao.getById(brickId2);

        brick1.setBrickDetails(new BrickDetails());
        BrickProperties brickProperties1 = new BrickProperties();
        brickProperties1.setBrickId(brickId1);
        brickProperties1.setFreeSize(Long.valueOf("75000"));
        brickProperties1.setTotalSize(Long.valueOf("250000"));
        brick1.getBrickDetails().setBrickProperties(brickProperties1);

        brick2.setBrickDetails(new BrickDetails());
        BrickProperties brickProperties2 = new BrickProperties();
        brickProperties2.setBrickId(brickId2);
        brickProperties2.setFreeSize(Long.valueOf("175000"));
        brickProperties2.setTotalSize(Long.valueOf("275000"));
        brick2.getBrickDetails().setBrickProperties(brickProperties2);

        List<GlusterBrickEntity> bricksToAdd = new ArrayList<>();
        bricksToAdd.add(brick1);
        bricksToAdd.add(brick2);
        dao.addBrickProperties(bricksToAdd);

        brick1 = dao.getById(brickId1);
        assertNotNull(brick1);
        assertNotNull(brick1.getBrickProperties());
        assertTrue(brick1.getBrickProperties().getTotalSize() == 250000);
        assertTrue(brick1.getBrickProperties().getFreeSize() == 75000);

        brick2 = dao.getById(brickId2);
        assertNotNull(brick2);
        assertNotNull(brick2.getBrickProperties());
        assertTrue(brick2.getBrickProperties().getTotalSize() == 275000);
        assertTrue(brick2.getBrickProperties().getFreeSize() == 175000);
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
    public void testGetBricksByTaskId() {
        List<GlusterBrickEntity> bricks = dao.getGlusterVolumeBricksByTaskId(FixturesTool.GLUSTER_ASYNC_TASK_ID1);
        assertNotNull(bricks);
        assertEquals(2, bricks.size());
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

        List<Guid> idsToRemove = new ArrayList<>();
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
        GlusterAsyncTask asyncTask = new GlusterAsyncTask();
        asyncTask.setTaskId(FixturesTool.GLUSTER_ASYNC_TASK_ID1);
        newBrick.setAsyncTask(asyncTask);
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
    public void testGetBrickProperties() {
        Guid GLUSTER_BRICK_UUID1 = new Guid("2ab384da-6190-4668-aa2e-37bc5c5ff014");
        Guid GLUSTER_BRICK_UUID2 = new Guid("2702bb49-3037-405c-81c5-14a38793164e");
        GlusterBrickEntity brick = dao.getById(GLUSTER_BRICK_UUID1);
        assertNotNull(brick);
        assertNotNull(brick.getBrickProperties());
        assertTrue(brick.getBrickProperties().getFreeSize() == 20000);
        assertTrue(brick.getBrickProperties().getTotalSize() == 100000);

        brick = dao.getById(GLUSTER_BRICK_UUID2);
        assertNotNull(brick);
        assertNotNull(brick.getBrickProperties());
        assertTrue(brick.getBrickProperties().getFreeSize() == 0);
        assertTrue(brick.getBrickProperties().getTotalSize() == 0);
    }

    @Test
    public void testUpdateBrickProperties() {
        Guid GLUSTER_BRICK_UUID1 = new Guid("2ab384da-6190-4668-aa2e-37bc5c5ff014");
        GlusterBrickEntity existingBrick = dao.getById(GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertNotNull(existingBrick.getBrickProperties());

        BrickProperties brickProperties = existingBrick.getBrickProperties();
        brickProperties.setBrickId(GLUSTER_BRICK_UUID1);
        brickProperties.setFreeSize(100000);
        brickProperties.setTotalSize(200000);

        dao.updateBrickProperties(brickProperties);

        existingBrick = dao.getById(GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertNotNull(existingBrick.getBrickProperties());
        assertTrue(existingBrick.getBrickProperties().getFreeSize() == 100000);
        assertTrue(existingBrick.getBrickProperties().getTotalSize() == 200000);
    }

    @Test
    public void testUpdateMultiBrickProperties() {

        Guid GLUSTER_BRICK_UUID1 = new Guid("2ab384da-6190-4668-aa2e-37bc5c5ff014");
        Guid GLUSTER_BRICK_UUID2 = new Guid("65d327f8-5864-4330-be04-fe27e1ffb553");
        GlusterBrickEntity existingBrick1 = dao.getById(GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick1);
        assertNotNull(existingBrick1.getBrickProperties());

        GlusterBrickEntity existingBrick2 = dao.getById(GLUSTER_BRICK_UUID2);
        assertNotNull(existingBrick2);
        assertNotNull(existingBrick2.getBrickProperties());

        BrickProperties brickProperties1 = existingBrick1.getBrickProperties();
        brickProperties1.setBrickId(GLUSTER_BRICK_UUID1);
        brickProperties1.setFreeSize(1000);
        brickProperties1.setTotalSize(2000);

        BrickProperties brickProperties2 = existingBrick2.getBrickProperties();
        brickProperties2.setBrickId(GLUSTER_BRICK_UUID2);
        brickProperties2.setFreeSize(1000);
        brickProperties2.setTotalSize(3000);

        List<GlusterBrickEntity> bricksList = new ArrayList<>();
        bricksList.add(existingBrick1);
        bricksList.add(existingBrick2);

        dao.updateBrickProperties(bricksList);

        existingBrick1 = dao.getById(GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick1);
        assertNotNull(existingBrick1.getBrickProperties());
        assertTrue(existingBrick1.getBrickProperties().getFreeSize() == 1000);
        assertTrue(existingBrick1.getBrickProperties().getTotalSize() == 2000);

        existingBrick2 = dao.getById(GLUSTER_BRICK_UUID2);
        assertNotNull(existingBrick2);
        assertNotNull(existingBrick2.getBrickProperties());
        assertTrue(existingBrick2.getBrickProperties().getFreeSize() == 1000);
        assertTrue(existingBrick2.getBrickProperties().getTotalSize() == 3000);
    }

    @Test
    public void testUpdateBrickStatuses() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        GlusterBrickEntity existingBrick1 = dao.getById(FixturesTool.GLUSTER_BRICK_UUID2);
        assertNotNull(existingBrick);
        assertNotNull(existingBrick1);
        assertEquals(GlusterStatus.UP, existingBrick.getStatus());
        assertEquals(GlusterStatus.UP, existingBrick1.getStatus());

        List<GlusterBrickEntity> bricks = new ArrayList<>();
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

        dao.updateBrickTaskByHostIdBrickDir(existingBrick.getServerId(),
                existingBrick.getBrickDirectory(),
                FixturesTool.GLUSTER_ASYNC_TASK_ID1);

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

    @Test
    public void testUpdateBrickNetworkId() {
        GlusterBrickEntity existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertNull(existingBrick.getNetworkId());

        dao.updateBrickNetworkId(FixturesTool.GLUSTER_BRICK_UUID1, FixturesTool.NETWORK_ENGINE);

        existingBrick = dao.getById(FixturesTool.GLUSTER_BRICK_UUID1);
        assertNotNull(existingBrick);
        assertEquals(FixturesTool.NETWORK_ENGINE, existingBrick.getNetworkId());

        List<GlusterBrickEntity> bricks =
                dao.getAllByClusterAndNetworkId(FixturesTool.GLUSTER_CLUSTER_ID, FixturesTool.NETWORK_ENGINE);
        assertNotNull(bricks);
    }

    @Test
    public void testGetAllByClusterAndNetworkId() {
        dao.updateBrickNetworkId(FixturesTool.GLUSTER_BRICK_UUID1, FixturesTool.NETWORK_ENGINE);
        List<GlusterBrickEntity> bricks =
                dao.getAllByClusterAndNetworkId(FixturesTool.GLUSTER_CLUSTER_ID, FixturesTool.NETWORK_ENGINE);
        assertNotNull(bricks);
        assertEquals(1, bricks.size());
        assertEquals(FixturesTool.GLUSTER_BRICK_UUID1, bricks.get(0).getId());
    }
}
