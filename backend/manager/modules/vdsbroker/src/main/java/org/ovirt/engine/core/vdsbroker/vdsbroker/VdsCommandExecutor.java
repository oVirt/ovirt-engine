package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;

public interface VdsCommandExecutor {
    VDSReturnValue execute(VDSCommandBase<?> command, VDSCommandType commandType);
}
