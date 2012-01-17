package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.IImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@InternalCommandAttribute
public class MoveOrCopyImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends BaseImagesCommand<T> {
    public MoveOrCopyImageGroupCommand(T parameters) {
        super(parameters);
    }

    private DiskImage _diskImage;

    @Override
    protected IImage getImage() {
        switch (getActionState()) {
        case END_SUCCESS:
        case END_FAILURE:
            if (_diskImage == null) {
                if (getVm() != null) {
                    VmHandler.updateDisksFromDb(getVm());
                    // LINQ 29456
                    // _diskImage = Vm.DiskMap.Values.First(a =>
                    // a.image_group_id == MoveParameters.ImageGroupID);
                    _diskImage = LinqUtils.firstOrNull(getVm().getDiskMap().values(), new Predicate<DiskImage>() {
                        @Override
                        public boolean eval(DiskImage a) {
                            return a.getimage_group_id().equals(getImageGroupId());
                        }
                    });
                } else if (getVmTemplate() != null) {
                    VmTemplateHandler.UpdateDisksFromDb(getVmTemplate());
                    // LINQ 29456
                    // List<DiskImage> templateDisks =
                    // VmTemplate.DiskMap.Values.Select(a =>
                    // DbFacade.Instance.GetSnapshotById(a.image_guid)).ToList();

                    // _diskImage = templateDisks.First(a =>
                    // a.image_group_id == MoveParameters.ImageGroupID);
                    List<DiskImage> templateDisks = LinqUtils.foreach(getVmTemplate().getDiskMap().values(),
                            new Function<DiskImageTemplate, DiskImage>() {
                                @Override
                                public DiskImage eval(DiskImageTemplate a) {
                                    return DbFacade.getInstance().getDiskImageDAO().getSnapshotById(a.getId());
                                }
                            });

                    _diskImage = LinqUtils.firstOrNull(templateDisks, new Predicate<DiskImage>() {
                        @Override
                        public boolean eval(DiskImage a) {
                            return a.getimage_group_id().equals(getImageGroupId());
                        }
                    });

                }
            }

            return _diskImage;

        default:
            return super.getImage();
        }
    }

    @Override
    protected Guid getImageContainerId() {
        return getParameters() != null ? getParameters().getContainerId() : super.getImageContainerId();
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return getParameters().getOperation();
    }

    @Override
    protected void executeCommand() {
        LockImage();

        List<DiskImage> snapshots = DbFacade.getInstance().getDiskImageDAO()
                .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());

        VDSReturnValue vdsReturnValue = null;

        if (getParameters().getUseCopyCollapse()) {
            vdsReturnValue =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CopyImage,
                                    new CopyImageVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                                            getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                                    .getValue()
                                                    : snapshots.get(0).getstorage_id().getValue(),
                                            getParameters()
                                                    .getContainerId(),
                                            getParameters().getImageGroupID(),
                                            getImage()
                                                    .getId(),
                                            getImageGroupId(),
                                            getImage().getId(),
                                            getImage()
                                                    .getdescription(),
                                            getParameters().getStorageDomainId(),
                                            getParameters()
                                                    .getCopyVolumeType(),
                                            getParameters().getVolumeFormat(),
                                            getParameters()
                                                    .getVolumeType(),
                                            getParameters().getPostZero(),
                                            getParameters()
                                                    .getForceOverride(),
                                            getStoragePool().getcompatibility_version().toString()));
        } else {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.MoveImageGroup,
                            new MoveImageGroupVDSCommandParameters(snapshots.get(0).getstorage_pool_id().getValue(),
                                    getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                            .getValue() : snapshots.get(0).getstorage_id().getValue(), snapshots.get(0)
                                            .getimage_group_id().getValue(), getParameters().getStorageDomainId(),
                                    getParameters().getContainerId(), getParameters().getOperation(), getParameters()
                                            .getPostZero(), getParameters().getForceOverride(), getStoragePool()
                                            .getcompatibility_version().toString()));
        }

        if (vdsReturnValue.getSucceeded()) {
            AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(taskCreationInfo, getParameters().getParentCommand()));

            // change storage domain in db only if object moved
            if (getParameters().getOperation() == ImageOperation.Move
                    || getParameters().getParentCommand() == VdcActionType.ImportVm
                    || getParameters().getParentCommand() == VdcActionType.ImportVmTemplate) {
                for (DiskImage snapshot : snapshots) {
                    snapshot.setstorage_id(getParameters().getStorageDomainId());
                    DbFacade.getInstance().getDiskImageDAO().update(snapshot);
                }
            }

            if (getParameters().getAddImageDomainMapping()) {
                DbFacade.getInstance().getStorageDomainDAO().addImageGroupStorageDomainMap(
                        new image_group_storage_domain_map(getParameters().getImageGroupID(), getParameters()
                                .getStorageDomainId()));
            }

            setSucceeded(true);
        }
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase commandParams = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                commandParams));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.moveImage, p, false);

        return ret;
    }

    @Override
    protected void EndWithFailure() {
        if (getMoveOrCopyImageOperation() == ImageOperation.Copy) {
            UnLockImage();

            // remove iamge-storage mapping
            DbFacade.getInstance().getStorageDomainDAO().removeImageGroupStorageDomainMap(
                    new image_group_storage_domain_map(getParameters().getImageGroupID(), getParameters()
                            .getStorageDomainId()));
        }

        else {
            MarkImageAsIllegal();
        }

        setSucceeded(true);
    }
}
