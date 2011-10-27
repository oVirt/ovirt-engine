package org.ovirt.engine.core.itests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Stateless;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.backendinterfaces.IResourceManager;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This resource manager is needed to run successful commands on mock test hosts.
 *
 */
@Stateless(name = "VdsBroker")
public class VoidResourceManager implements IResourceManager {

    @Override
    public VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) {
        // TODO consider a way to manipulate returnValue of VdsCommands - return
        // random value,set it a as a parameter on the threadLocal etc
        // currently return true will suffice
        VDSReturnValue val = new VDSReturnValue();
        val.setSucceeded(true);

        switch (commandType) {
        case GetImageInfo:
            GetImageInfoVDSCommandParameters p = (GetImageInfoVDSCommandParameters) parameters;
            DiskImage image = DbFacade.getInstance().getDiskImageDAO().get(p.getImageId());
            image.setimageStatus(ImageStatus.OK);
            val.setReturnValue(image);
            break;
        case ValidateStorageServerConnection:
        case ConnectStorageServer:
            HashMap<String, String> resultMap = new HashMap<String, String>();
            resultMap.put(Guid.Empty.toString(), "0");
            val.setReturnValue(resultMap);
            break;
        case CreateImage: // Same as for IsoList, with the addition of task creation info)
            CreateImageVDSCommandParameters createImageParams = (CreateImageVDSCommandParameters) parameters;
            val.setCreationInfo(
                    new AsyncTaskCreationInfo(Guid.NewGuid(),
                            AsyncTaskType.createVolume,
                            createImageParams.getStoragePoolId()));
        case GetIsoList:
            List resultList = new ArrayList<String>();
            val.setReturnValue(resultList);
            break;
        case CopyImage:
            val.setCreationInfo(new AsyncTaskCreationInfo());
            break;
        default:
            val.setReturnValue(true);
            break;
        }

        return val;
    }

    @Override
    public void setup() {
        // TODO Auto-generated method stub

    }
}
