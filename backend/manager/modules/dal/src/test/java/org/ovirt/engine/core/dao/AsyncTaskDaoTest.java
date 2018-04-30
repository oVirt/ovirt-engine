package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

/**
 * {@code AsyncTaskDaoTest} performs tests against the {@link AsyncTaskDao} type.
 */
public class AsyncTaskDaoTest extends BaseDaoTestCase<AsyncTaskDao> {
    private static final int TASK_COUNT = 2;
    private AsyncTask newAsyncTask;
    private AsyncTask existingAsyncTask;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        ActionParametersBase params = new ActionParametersBase();
        params.setSessionId("ASESSIONID");
        params.setTransactionScopeOption(TransactionScopeOption.RequiresNew);

        ActionParametersBase taskParams = new ActionParametersBase();
        taskParams.setSessionId("ASESSIONID");
        taskParams.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        taskParams.setParentParameters(params);

        // create some test data
        newAsyncTask = new AsyncTask();
        newAsyncTask.setTaskId(Guid.newGuid());
        newAsyncTask.setVdsmTaskId(Guid.newGuid());
        newAsyncTask.setStartTime(new Date());
        newAsyncTask.setActionType(ActionType.AddDisk);
        newAsyncTask.setstatus(AsyncTaskStatusEnum.running);
        newAsyncTask.setresult(AsyncTaskResultEnum.success);
        newAsyncTask.setActionParameters(params);
        newAsyncTask.setTaskParameters(taskParams);
        newAsyncTask.setCommandId(Guid.newGuid());
        newAsyncTask.setRootCommandId(Guid.newGuid());
        newAsyncTask.setTaskType(AsyncTaskType.copyImage);
        newAsyncTask.setStoragePoolId(Guid.newGuid());

        existingAsyncTask = dao.get(FixturesTool.EXISTING_TASK_ID);
    }

    /**
     * Ensures that if the id is invalid then no AsyncTask is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        AsyncTask result = dao.get(Guid.newGuid());

        assertNull(result);
    }

    @Test
    public void testGetAsyncTaskIdsByEntity() {
        List<Guid> guids = dao.getAsyncTaskIdsByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(guids);
        assertEquals(1, guids.size());
    }

    @Test
    public void testGetAsyncTaskEntitiesById() {
        List<AsyncTask> tasks = dao.getTasksByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
    }

    @Test
    public void testGetAsyncTaskIdsByInvalidEntity() {
        List<Guid> guids = dao.getAsyncTaskIdsByEntity(Guid.newGuid());
        assertNotNull(guids);
        assertTrue(guids.isEmpty());
    }

    @Test
    public void testGetAsyncTaskIdsByStoragePoolId() {
        // According to fixtures.xml , STORAGE_POOL_RHEL6_ISCSI_OTHER is the storage pool
        // that has storage domain associated to which has one task on it
        List<AsyncTask> tasks = dao.getAsyncTaskIdsByStoragePoolId(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
    }

    @Test
    public void testGetAsyncTaskIdsByInvalidStoragePoolId() {
        List<AsyncTask> tasks = dao.getAsyncTaskIdsByStoragePoolId(Guid.newGuid());
        assertNotNull(tasks);
        assertEquals(0, tasks.size());
    }

    /**
     * Ensures that, if the id is valid, then retrieving a AsyncTask works as expected.
     */
    @Test
    public void testGet() {
        AsyncTask result = dao.get(existingAsyncTask.getTaskId());

        assertNotNull(result);
        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that finding all AsyncTasks works as expected.
     */
    @Test
    public void testGetAll() {
        List<AsyncTask> result = dao.getAll();

        assertEquals(TASK_COUNT, result.size());
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newAsyncTask);

        AsyncTask result = dao.get(newAsyncTask.getTaskId());
        /*
         * //Setting startTime to null is required as DB auto generates //the value of start time //Without this, the
         * comparison would fail result.setStartTime(null);
         */
        assertEquals(newAsyncTask, result);
    }

    /**
     * Ensures that updating a ad_group works as expected.
     */
    @Test
    public void testUpdate() {
        existingAsyncTask.setstatus(AsyncTaskStatusEnum.aborting);
        existingAsyncTask.setresult(AsyncTaskResultEnum.failure);
        existingAsyncTask.setActionType(ActionType.AddDisk);
        dao.update(existingAsyncTask);

        AsyncTask result = dao.get(existingAsyncTask.getTaskId());

        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that removing a ad_group works as expected.
     */
    @Test
    public void testRemove() {
        AsyncTask result = dao.get(existingAsyncTask.getTaskId());
        assertNotNull(result);

        assertEquals(1, dao.remove(existingAsyncTask.getTaskId()));
        result = dao.get(existingAsyncTask.getTaskId());

        assertNull(result);
        assertEquals(0, dao.remove(existingAsyncTask.getTaskId()));

        // The removed task is associated with an entity, try to fetch
        // tasks for the entity, and see no task is returned
        List<Guid> taskIds = dao.getAsyncTaskIdsByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(taskIds);
        assertTrue(taskIds.isEmpty());
    }

    @Test
    public void testGetTaskByVdsmTaskId() {
        AsyncTask result = dao.getByVdsmTaskId(FixturesTool.EXISTING_VDSM_TASK_ID);
        assertNotNull(result);
        assertEquals(existingAsyncTask, result);

    }

    @Test
    public void testRemoveByVdsmTaskId() {
        AsyncTask result = dao.getByVdsmTaskId(FixturesTool.EXISTING_VDSM_TASK_ID);
        assertNotNull(result);

        assertEquals(1, dao.removeByVdsmTaskId(existingAsyncTask.getVdsmTaskId()));
        result = dao.getByVdsmTaskId(existingAsyncTask.getVdsmTaskId());

        assertNull(result);
        assertEquals(0, dao.removeByVdsmTaskId(existingAsyncTask.getVdsmTaskId()));

        // The removed task is associated with an entity, try to fetch
        // tasks for the entity, and see no task is returned
        List<Guid> taskIds = dao.getAsyncTaskIdsByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(taskIds);
        assertTrue(taskIds.isEmpty());
    }

    @Test
    public void testSaveOrUpdate() {
        existingAsyncTask.setstatus(AsyncTaskStatusEnum.aborting);
        existingAsyncTask.setresult(AsyncTaskResultEnum.failure);
        existingAsyncTask.setActionType(ActionType.AddDisk);
        List<AsyncTask> tasks = dao.getAll();
        assertNotNull(tasks);
        int tasksNumber = tasks.size();
        dao.saveOrUpdate(existingAsyncTask);
        tasks = dao.getAll();
        assertEquals(tasksNumber, tasks.size());
        AsyncTask taskFromDb = dao.get(existingAsyncTask.getTaskId());
        assertNotNull(taskFromDb);
        assertEquals(taskFromDb, existingAsyncTask);
        dao.saveOrUpdate(newAsyncTask);
        tasks = dao.getAll();
        assertNotNull(tasks);
        assertEquals(tasksNumber + 1, tasks.size());
        taskFromDb = dao.get(newAsyncTask.getTaskId());
        assertEquals(taskFromDb, newAsyncTask);

    }

    @Test
    public void testInsertAsyncTaskEntitities() {
        dao.save(newAsyncTask);
        Set<AsyncTaskEntity> asyncTaskEntities = new HashSet<>();
        asyncTaskEntities.add(new AsyncTaskEntity(newAsyncTask.getTaskId(), VdcObjectType.Storage, Guid.newGuid()));
        asyncTaskEntities.add(new AsyncTaskEntity(newAsyncTask.getTaskId(), VdcObjectType.Disk, Guid.newGuid()));
        dao.insertAsyncTaskEntities(asyncTaskEntities);
        List<AsyncTaskEntity> entities = dao.getAllAsyncTaskEntitiesByTaskId(newAsyncTask.getTaskId());
        assertNotNull(entities);
        assertEquals(2, entities.size());
        for (AsyncTaskEntity entity : entities) {
            assertTrue(entities.contains(entity));
        }

    }
}
