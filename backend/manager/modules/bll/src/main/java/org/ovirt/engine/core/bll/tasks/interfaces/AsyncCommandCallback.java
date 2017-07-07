package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.compat.Guid;

public interface AsyncCommandCallback {

    Map<Guid, AsyncTaskStatus> getAllTasksStatuses(Guid storagePoolID);

    ArrayList<AsyncTaskCreationInfo> getAllTasksInfo(Guid storagePoolID);

    ActionReturnValue endAction(SPMTask task);
}
