package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MergeStatusCommandCallback extends CommandCallBack {
    public static final Log log = LogFactory.getLog(MergeStatusCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).attemptResolution();
    }

    private MergeStatusCommand<MergeParameters> getCommand(Guid cmdId) {
        return (MergeStatusCommand<MergeParameters>) CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
