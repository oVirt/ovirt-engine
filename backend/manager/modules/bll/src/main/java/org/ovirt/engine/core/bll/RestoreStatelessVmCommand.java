package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class RestoreStatelessVmCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    private static final long serialVersionUID = 6917252644139242325L;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestoreStatelessVmCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreStatelessVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        boolean returnVal = true;
        List<stateless_vm_image_map> statelessMap = DbFacade.getInstance().getDiskImageDAO().getAllStatelessVmImageMapsForVm(
                getVmId());
        List<DiskImage> imagesList = new java.util.ArrayList<DiskImage>(statelessMap.size());

        for (stateless_vm_image_map sMap : statelessMap) {
            imagesList.add(DbFacade.getInstance().getDiskImageDAO().getSnapshotById(sMap.getimage_guid()));

            /**
             * remove from db
             */
            DbFacade.getInstance().getDiskImageDAO().removeStatelessVmImageMap(sMap.getimage_guid());
        }

        if (imagesList.size() > 0) {
            /**
             * restore all snapshots
             */
            RestoreAllSnapshotsParameters tempVar = new RestoreAllSnapshotsParameters(getVm().getvm_guid(), Guid.Empty);
            tempVar.setShouldBeLogged(false);
            tempVar.setImagesList(imagesList);
            VdcReturnValueBase vdcReturn =
                    Backend.getInstance().runInternalAction(VdcActionType.RestoreAllSnapshots,
                            tempVar,
                            ExecutionHandler.createDefaultContexForTasks(executionContext));
            returnVal = vdcReturn.getSucceeded();
        }
        setSucceeded(returnVal);
    }
}
