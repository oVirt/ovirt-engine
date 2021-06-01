package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.SealVmParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.SealDisksVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute(forceCompensation = true)
@InternalCommandAttribute
public class SealVmCommand<T extends SealVmParameters> extends VmCommand<T> implements HostJobCommand {

    private List<DiskImage> diskImages;

    @Inject
    @Typed(VirtJobCallback.class)
    private Instance<VirtJobCallback> callbackProvider;

    @Inject
    private VdsCommandsHelper vdsCommandsHelper;

    public SealVmCommand(Guid commandId) {
        super(commandId);
    }

    public SealVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getParameters().getHostJobId() == null) {
            getParameters().setHostJobId(Guid.newGuid());
        }
    }

    private List<DiskImage> getDiskImages() {
        if (diskImages == null) {
            vmHandler.updateDisksFromDb(getVm());
            diskImages = DisksFilter.filterImageDisks(getVm().getDiskList(), DisksFilter.ONLY_NOT_SHAREABLE);
        }
        return diskImages;
    }

    @Override
    protected void executeCommand() {
        if (getDiskImages().isEmpty()) {
            setSucceeded(true);
            return;
        }

        VDSReturnValue vdsReturnValue = vdsCommandsHelper.runVdsCommandWithFailover(
                VDSCommandType.SealDisks,
                buildSealDisksVDSCommandParameters(),
                getDiskImages().get(0).getStoragePoolId(),
                this);
        if (!vdsReturnValue.getSucceeded()) {
            setCommandStatus(CommandStatus.FAILED);
        }

        setSucceeded(vdsReturnValue.getSucceeded());
    }

    private SealDisksVDSCommandParameters buildSealDisksVDSCommandParameters() {
        SealDisksVDSCommandParameters parameters = new SealDisksVDSCommandParameters();
        parameters.setEntityId(getParameters().getVmId());
        parameters.setJobId(getParameters().getHostJobId());
        parameters.setStoragePoolId(getDiskImages().get(0).getStoragePoolId());
        getDiskImages().forEach(parameters::addImage);
        return parameters;
    }

    @Override
    public HostJobStatus handleJobError(EngineError error) {
        return HostJobStatus.failed;
    }

    @Override
    public boolean failJobWithUndeterminedStatus() {
        return false;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SEAL);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

}
