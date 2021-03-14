package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ExternalLeaseParameters;
import org.ovirt.engine.core.common.vdscommands.FenceLeaseJobVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class FenceLeaseJobCommand<T extends ExternalLeaseParameters> extends CommandBase<T> {
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    public FenceLeaseJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public FenceLeaseJobCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        FenceLeaseJobVDSParameters parameters = new FenceLeaseJobVDSParameters(getParameters().getStorageDomainId(),
                getParameters().getJobId(),
                getParameters().getStoragePoolId(),
                getParameters().getLeaseId(),
                getParameters().getLeaseMetadata());

        vdsCommandsHelper.runVdsCommandWithoutFailover(VDSCommandType.FenceLeaseJob,
                parameters,
                getParameters().getStoragePoolId(),
                this);

        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
