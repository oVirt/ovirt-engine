package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.MergeSnapshotParamenters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.All;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class MergeSnapshotCommand<T extends MergeSnapshotParamenters> extends VmCommand<T> {

    private static final long serialVersionUID = 3162100352844371734L;
    private List<DiskImage> _sourceImages = null;

    public MergeSnapshotCommand(T parameters) {
        super(parameters);
    }

    private void initializeObjectState() {
        String name = "";
        List<DiskImage> images = DbFacade
                        .getInstance()
                        .getDiskImageDAO()
                        .getAllSnapshotsForVmSnapshot(
                                getParameters().getSourceVmSnapshotId());
        if (images.size() > 0) {
            name = images.get(0).getdescription();
        }
        setSnapshotName(name);
    }

    private List<DiskImage> getSourceImages() {
        if (_sourceImages == null) {
            _sourceImages = DbFacade
                    .getInstance()
                    .getDiskImageDAO()
                    .getAllSnapshotsForVmSnapshot(
                            getParameters().getSourceVmSnapshotId());
        }
        return _sourceImages;
    }

    @Override
    protected void executeCommand() {
        if (getVm().getstatus() != VMStatus.Down) {
            log.error("Cannot merge VM snapshot. Vm is not Down");
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }
        VmHandler.LockVm(getVm().getDynamicData(), getCompensationContext());
        getParameters().setEntityId(getVmId());
        List<DiskImage> destImages = null;
        if (getParameters().getDestVmSnapshotId() != null) {
            destImages = DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForVmSnapshot(
                    getParameters().getDestVmSnapshotId().getValue());
        }

        for (final DiskImage source : getSourceImages()) {
            DiskImage dest;

            // if the MergeSnapshotParameters.DestImage is null
            // we assume that the source->parent is the dest
            if (getParameters().getDestVmSnapshotId() == null) {
                // The following line is ok because we have tested in the
                // candoaction that the vm
                // is not a template and the vm is not in preview mode and that
                // this is not the active snapshot.
                dest = DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForParent(source.getId()).get(0);
            } else {
                // LINQ 29456
                // dest = destImages.Where(a => a.internal_drive_mapping ==
                // source.internal_drive_mapping).FirstOrDefault();
                List<DiskImage> images = LinqUtils.filter(destImages, new Predicate<DiskImage>() {
                    @Override
                    public boolean eval(DiskImage a) {
                        return a.getinternal_drive_mapping().equals(source.getinternal_drive_mapping());
                    }
                });
                dest = LinqUtils.firstOrNull(images, new All<DiskImage>());
                // LINQ 29456
            }

            ImagesContainterParametersBase tempVar = new ImagesContainterParametersBase(source.getId(),
                    source.getinternal_drive_mapping(), getVmId());
            tempVar.setDestinationImageId(dest.getId());
            tempVar.setEntityId(getParameters().getEntityId());
            ImagesContainterParametersBase p = tempVar;
            VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                    VdcActionType.MergeSnapshotSingleDisk, p);
            getParameters().getImagesParameters().add(p);

            if (vdcReturnValue != null && vdcReturnValue.getInternalTaskIdList() != null) {
                getReturnValue().getTaskIdList().addAll(vdcReturnValue.getInternalTaskIdList());
            }
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        initializeObjectState();
        // Since 'VmId' is overriden, 'Vm' should be retrieved manually.
        setVm(DbFacade.getInstance().getVmDAO().getById(getVmId()));

        getReturnValue().setCanDoAction(
                ImagesHandler.PerformImagesChecks(getParameters().getVmId(), getReturnValue().getCanDoActionMessages(),
                        getVm().getstorage_pool_id(), getSourceImages().get(0).getstorage_id().getValue(), true, true,
                        true, true, true, true, true));

        // check that we are not deleting the template
        if (DbFacade.getInstance().getVmTemplateDAO().get(getSourceImages().get(0).getId()) != null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE);
            getReturnValue().setCanDoAction(false);
        }

        // check that we are not deleting the vm working snapshot
        if (DbFacade.getInstance().getDiskImageDAO().get(getSourceImages().get(0).getId()) != null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_IMAGE);
            getReturnValue().setCanDoAction(false);
        }

        if (!getReturnValue().getCanDoAction()) {
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MERGE);
        }

        return getReturnValue().getCanDoAction();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_MERGE_SNAPSHOT : AuditLogType.USER_FAILED_MERGE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_MERGE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_MERGE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_MERGE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.MergeSnapshotSingleDisk;
    }
}
