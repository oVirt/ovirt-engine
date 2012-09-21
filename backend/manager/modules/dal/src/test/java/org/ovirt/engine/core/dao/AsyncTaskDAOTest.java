package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

/**
 * <code>AsyncTaskDAOTest</code> performs tests against the {@link AsyncTaskDAO} type.
 *
 *
 */
public class AsyncTaskDAOTest extends BaseDAOTestCase {
    private static final int TASK_COUNT = 2;
    private AsyncTaskDAO dao;
    private async_tasks newAsyncTask;
    private async_tasks existingAsyncTask;

    private VdcActionParametersBase params;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getAsyncTaskDao());
        params = new VdcActionParametersBase();
        params.setSessionId("ASESSIONID");
        params.setTransactionScopeOption(TransactionScopeOption.RequiresNew);

        // create some test data
        newAsyncTask = new async_tasks();
        newAsyncTask.settask_id(Guid.NewGuid());
        newAsyncTask.setaction_type(VdcActionType.AddDisk);
        newAsyncTask.setstatus(AsyncTaskStatusEnum.running);
        newAsyncTask.setresult(AsyncTaskResultEnum.success);
        newAsyncTask.setaction_parameters(params);
        newAsyncTask.setCommandId(Guid.NewGuid());

        existingAsyncTask = dao.get(FixturesTool.EXISTING_TASK_ID);
    }

    /**
     * Ensures that if the id is invalid then no AsyncTask is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        async_tasks result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    @Test
    public void testGetAsyncTaskIdsByEntity() {
        List<Guid> guids = dao.getAsyncTaskIdsByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(guids);
        assertEquals(guids.size(), 1);
    }

    @Test
    public void testGetAsyncTaskIdsByInvalidEntity() {
        List<Guid> guids = dao.getAsyncTaskIdsByEntity(Guid.NewGuid());
        assertNotNull(guids);
        assertTrue(guids.isEmpty());
    }

    @Test
    public void testGetAsyncTaskIdsByStoragePoolId() {
        // According to fixtures.xml , STORAGE_POOL_RHEL6_ISCSI_OTHER is the storage pool
        // that has storage domain associated to which has one task on it
        List<Guid> guids = dao.getAsyncTaskIdsByStoragePoolId(FixturesTool.STORAGE_POOL_RHEL6_ISCSI_OTHER);
        assertNotNull(guids);
        assertEquals(guids.size(), 1);
    }

    @Test
    public void testGetAsyncTaskIdsByInvalidStoragePoolId() {
        List<Guid> guids = dao.getAsyncTaskIdsByStoragePoolId(Guid.NewGuid());
        assertNotNull(guids);
        assertEquals(guids.size(), 0);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a AsyncTask works as expected.
     */
    @Test
    public void testGet() {
        async_tasks result = dao.get(existingAsyncTask.gettask_id());

        assertNotNull(result);
        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that finding all AsyncTasks works as expected.
     */
    @Test
    public void testGetAll() {
        List<async_tasks> result = dao.getAll();

        assertEquals(TASK_COUNT, result.size());
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newAsyncTask);

        async_tasks result = dao.get(newAsyncTask.gettask_id());

        assertEquals(newAsyncTask, result);
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSaveWithEntities() {
        Guid storageId = Guid.NewGuid();
        dao.save(newAsyncTask, VdcObjectType.Storage, storageId);
        List<Guid> asyncTasks = dao.getAsyncTaskIdsByEntity(storageId);
        assertNotNull(asyncTasks);
        assertEquals(asyncTasks.size(),1);
        assertEquals(asyncTasks.get(0),newAsyncTask.gettask_id());
    }

    /**
     * Ensures that updating a ad_group works as expected.
     */
    @Test
    public void testUpdate() {
        existingAsyncTask.setstatus(AsyncTaskStatusEnum.aborting);
        existingAsyncTask.setresult(AsyncTaskResultEnum.failure);
        existingAsyncTask.setaction_type(VdcActionType.AddDisk);
        AddDiskParameters addDiskToVmParams = new AddDiskParameters();
        addDiskToVmParams.setSessionId("SESSION_ID");
        existingAsyncTask.setaction_parameters(addDiskToVmParams);
        dao.update(existingAsyncTask);

        async_tasks result = dao.get(existingAsyncTask.gettask_id());

        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that removing a ad_group works as expected.
     */
    @Test
    public void testRemove() {
        async_tasks result = dao.get(existingAsyncTask.gettask_id());
        assertNotNull(result);

        assertEquals(dao.remove(existingAsyncTask.gettask_id()), 1);
        result = dao.get(existingAsyncTask.gettask_id());

        assertNull(result);
        assertEquals(dao.remove(existingAsyncTask.gettask_id()), 0);

        // The removed task is associated with an entity, try to fetch
        // tasks for the entity, and see no task is returned
        List<Guid> taskIds = dao.getAsyncTaskIdsByEntity(FixturesTool.ENTITY_WITH_TASKS_ID);
        assertNotNull(taskIds);
        assertTrue(taskIds.isEmpty());
    }
}
