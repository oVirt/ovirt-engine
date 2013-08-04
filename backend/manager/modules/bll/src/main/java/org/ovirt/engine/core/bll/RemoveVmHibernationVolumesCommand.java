package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.RemoveVmHibernationVolumesParameters;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveVmHibernationVolumesCommand<T extends RemoveVmHibernationVolumesParameters> extends CommandBase<T> {

    public static final String DELETE_PRIMARY_IMAGE_TASK_KEY = "DELETE_PRIMARY_IMAGE_TASK_KEY";
    public static final String DELETE_SECONDARY_IMAGES_TASK_KEY = "DELETE_SECONDARY_IMAGES_TASK_KEY";

    public RemoveVmHibernationVolumesCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    protected RemoveVmHibernationVolumesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(removeMemoryVolumes(getVm().getHibernationVolHandle()));
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    protected boolean removeMemoryVolumes(String memVols) {
        // this is temp code until it will be implemented in SPM
        List<Guid> guids = GuidUtils.getGuidListFromString(memVols);

        if (guids.size() == 6) {
            // get all vm disks in order to check post zero - if one of the
            // disks is marked with wipe_after_delete
            boolean postZero =
                    LinqUtils.filter(getDbFacade().getDiskDao().getAllForVm(getVm().getId()),
                            new Predicate<Disk>() {
                                @Override
                                public boolean eval(Disk disk) {
                                    return disk.isWipeAfterDelete();
                                }
                            }).size() > 0;

            Guid taskId1 = persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), DELETE_PRIMARY_IMAGE_TASK_KEY);

            // delete first image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            VDSReturnValue vdsRetValue = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(guids.get(1),
                            guids.get(0), guids.get(2), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                return false;
            }

            Guid guid1 =
                    createTask(taskId1, vdsRetValue.getCreationInfo(), getParameters().getParentCommand(), VdcObjectType.Storage, guids.get(0));
            getTaskIdList().add(guid1);

            Guid taskId2 = persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), DELETE_SECONDARY_IMAGES_TASK_KEY);
            // delete second image
            // the next 'DeleteImageGroup' command should also take care of the image removal:
            vdsRetValue = runVdsCommand(
                    VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(guids.get(1),
                            guids.get(0), guids.get(4), postZero, false));

            if (!vdsRetValue.getSucceeded()) {
                return false;
            }

            Guid guid2 = createTask(taskId2, vdsRetValue.getCreationInfo(), getParameters().getParentCommand());
            getTaskIdList().add(guid2);
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
