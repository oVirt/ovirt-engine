package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class DetachUserFromVmFromPoolCommand<T extends VmPoolSimpleUserParameters> extends
        VmPoolSimpleUserCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected DetachUserFromVmFromPoolCommand(Guid commandId) {
        super(commandId);
    }

    public DetachUserFromVmFromPoolCommand(T parameters) {
        super(parameters);
        parameters.setEntityId(getVmId());

    }

    protected boolean IsUserAttachedToPool() {
        // Check first if user attached to pool directly
        boolean attached = getVmPoolId() != null
                && DbFacade.getInstance().getEntityPermissions(getAdUserId(), ActionGroup.VM_POOL_BASIC_OPERATIONS,
                        getVmPoolId().getValue(), VdcObjectType.VmPool) != null;
        return attached;
    }

    protected void DetachAllVmsFromUser() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllForUser(getAdUserId());
        for (VM vm : vms) {
            if (getVmPoolId() != null && getVmPoolId().equals(vm.getVmPoolId())) {
                permissions perm = DbFacade
                        .getInstance()
                        .getPermissionDAO()
                        .getForRoleAndAdElementAndObject(
                                PredefinedRoles.ENGINE_USER.getId(),
                                getAdUserId(), vm.getId());
                if (perm != null) {
                    DbFacade.getInstance().getPermissionDAO().remove(perm.getId());
                    RestoreVmFromBaseSnapshot(vm);
                }
            }
        }

    }

    private void RestoreVmFromBaseSnapshot(VM vm) {
        List<image_vm_pool_map> list = DbFacade.getInstance().getDiskImageDAO().getImageVmPoolMapByVmId(vm.getId());
        // java.util.ArrayList<DiskImage> imagesList = null; // LINQ 32934
        // list.Select(a =>
        // DbFacade.Instance.GetSnapshotById(a.image_guid)).ToList();
        List<DiskImage> imagesList = LinqUtils.foreach(list, new Function<image_vm_pool_map, DiskImage>() {
            @Override
            public DiskImage eval(image_vm_pool_map a) {
                return DbFacade.getInstance().getDiskImageDAO().getSnapshotById(a.getimage_guid());
            }
        });
        if (imagesList.size() > 0) {
            /**
             * restore all snapshots
             */
            RestoreAllSnapshotsParameters tempVar = new RestoreAllSnapshotsParameters(vm.getId(), Guid.Empty);
            tempVar.setShouldBeLogged(false);
            tempVar.setImagesList(imagesList);
            Backend.getInstance().runInternalAction(VdcActionType.RestoreAllSnapshots,
                    tempVar,
                    ExecutionHandler.createDefaultContexForTasks(executionContext));
        }
    }

    @Override
    protected void executeCommand() {
        if (IsUserAttachedToPool()) {
            DetachAllVmsFromUser();
        }
        setSucceeded(true);
    }
}
