package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.SealVmTemplateParameters;
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
public class SealVmTemplateCommand<T extends SealVmTemplateParameters> extends VmTemplateCommand<T> implements HostJobCommand {

    private List<DiskImage> diskImages;

    public SealVmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public SealVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (getParameters().getHostJobId() == null) {
            getParameters().setHostJobId(Guid.newGuid());
        }
    }

    private List<DiskImage> getDiskImages() {
        if (diskImages == null) {
            vmTemplateHandler.updateDisksFromDb(getVmTemplate());
            diskImages = DisksFilter.filterImageDisks(getVmTemplate().getDiskTemplateMap().values(),
                    DisksFilter.ONLY_NOT_SHAREABLE);
        }
        return diskImages;
    }

    @Override
    protected void executeCommand() {
        if (getDiskImages().isEmpty()) {
            setSucceeded(true);
            return;
        }

        VDSReturnValue vdsReturnValue = VdsCommandsHelper.runVdsCommandWithFailover(
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
        parameters.setTemplateId(getParameters().getVmTemplateId());
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
        return new VirtJobCallback();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SEAL);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

}
