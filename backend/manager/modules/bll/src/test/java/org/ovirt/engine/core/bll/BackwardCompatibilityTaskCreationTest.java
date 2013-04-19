package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.sql.Connection;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;

/**
 * A test for task creation in the various commands.
 * It's mainly intended to observe that future refactoring of these commands will not break current behavior.
 */
@RunWith(Theories.class)
public class BackwardCompatibilityTaskCreationTest {

    @Rule
    public static final RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @ClassRule
    public static final MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.AsyncTaskPollingRate, 10),
            mockConfig(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds, 10),
            mockConfig(ConfigValues.AsyncTaskStatusCachingTimeInMinutes, 10)
            );

    @ClassRule
    public static MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.SCHEDULER, mock(SchedulerUtil.class));

    @BeforeClass
    public static void beforeClass() {
        ejbRule.mockResource(ContainerManagedResourceType.DATA_SOURCE, mock(Connection.class));
        DbFacade dbFacade = spy(new DbFacade());
        DbFacadeLocator.setDbFacade(dbFacade);
        AsyncTaskDAO asyncTaskDao = mock(AsyncTaskDAO.class);
        when(asyncTaskDao.getAll()).thenReturn(Collections.EMPTY_LIST);
        when(dbFacade.getAsyncTaskDao()).thenReturn(asyncTaskDao);
    }

    @SuppressWarnings({ "unchecked", "rawtypes"})
    @DataPoints
    public static CommandBase<? extends VdcActionParametersBase>[] data() {
        return new CommandBase<?>[] {
                new CreateSnapshotCommand(new ImagesActionsParametersBase()),
                new AddImageFromScratchCommand(new AddImageFromScratchParameters()),
                new CreateImageTemplateCommand(new CreateImageTemplateParameters()),
                new CreateCloneOfTemplateCommand(new CreateCloneOfTemplateParameters()),
                new RemoveVmCommand(new RemoveVmParameters()),
                new HibernateVmCommand(new HibernateVmParameters()) {
                    @Override
                    public VM getVm() {
                        VM vm = new VM();
                        vm.setId(Guid.newGuid());
                        vm.setStoragePoolId(Guid.newGuid());
                        return vm;
                    }
                },
                new CopyImageGroupCommand(new MoveOrCopyImageGroupParameters()) {
                    @Override
                    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                        // No op for test
                    }
                },
                new RemoveImageCommand(new RemoveImageParameters()) {
                    @Override
                    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                        // No op for test
                    }

                    @Override
                    protected void initImage() {
                        // No op for test
                    }

                    @Override
                    protected void initStoragePoolId() {
                        // No op for test
                    }

                    @Override
                    protected void initStorageDomainId() {
                        // No op for test
                    }

                },
                new RemoveSnapshotSingleDiskCommand(new ImagesContainterParametersBase()) {
                    @Override
                    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                        // No op for test
                    }
                },
                new RemoveTemplateSnapshotCommand(new ImagesContainterParametersBase()) {
                    @Override
                    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                        // No op for test
                    }
                },
                new RestoreFromSnapshotCommand(new RestoreFromSnapshotParameters()) {
                    @Override
                    protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                        // No op for test
                    }
                }
        };
    }

    @Theory
    public void testConcreateCreateTaskBackwardsComaptibility(CommandBase<? extends VdcActionParametersBase> cmd) {
        VdcActionParametersBase params = cmd.getParameters();
        params.setEntityId(Guid.newGuid());
        params.setParentCommand(RandomUtils.instance().nextEnum(VdcActionType.class));
        params.setParentParameters(params);

        AsyncTaskCreationInfo info = nextAsyncTaskCreationInfo();

        SPMAsyncTask spmAsyncTask = cmd.concreteCreateTask(info, cmd.getParameters().getParentCommand());
        assertEquals("wrong storage pool ID", info.getStoragePoolID(), spmAsyncTask.getStoragePoolID());
        assertEquals("wrong task ID", info.getVdsmTaskId(), spmAsyncTask.getVdsmTaskId());
        assertEquals("wrong task result", AsyncTaskResultEnum.success, spmAsyncTask.getLastTaskStatus().getResult());
        assertEquals("wrong task status", AsyncTaskStatusEnum.init, spmAsyncTask.getLastTaskStatus().getStatus());
        assertEquals("wrong task state", AsyncTaskState.Initializing, spmAsyncTask.getState());
        assertTrue("wrong task type", spmAsyncTask instanceof EntityAsyncTask);
    }

    /**
     * Tests that a purely engine command, with no async tasks throws the
     * correct exception when
     * {@link CommandBase#concreteCreateTask(AsyncTaskCreationInfo, VdcActionType)}
     * is called.
     *
     * Note: {@link AddPermissionCommand} is merely used as an example here.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testExceptionForCommandWithNoTasks() {
        PermissionsOperationsParametes params = new PermissionsOperationsParametes();
        AddPermissionCommand<PermissionsOperationsParametes> cmd = spy(
                new AddPermissionCommand<PermissionsOperationsParametes>(params));
        cmd.concreteCreateTask(nextAsyncTaskCreationInfo(), VdcActionType.Unknown);
    }

    /** @return A randomly generated {@link AsyncTaskCreationInfo} object */
    private static AsyncTaskCreationInfo nextAsyncTaskCreationInfo() {
        AsyncTaskCreationInfo info = new AsyncTaskCreationInfo();
        info.setStepId(Guid.newGuid());
        info.setStoragePoolID(Guid.newGuid());
        info.setVdsmTaskId(Guid.newGuid());
        info.setTaskType(RandomUtils.instance().nextEnum(AsyncTaskType.class));
        return info;
    }
}
