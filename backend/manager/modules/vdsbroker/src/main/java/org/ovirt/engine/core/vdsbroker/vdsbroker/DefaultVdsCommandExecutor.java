package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;

@Alternative
public class DefaultVdsCommandExecutor implements VdsCommandExecutor {

    @Override
    public VDSReturnValue execute(final VDSCommandBase<?> command, final VDSCommandType commandType) {
        command.execute();
        return command.getVDSReturnValue();
    }
}
