package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.CopyVolumeDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CopyDataCommand<T extends CopyDataCommandParameters> extends
        StorageJobCommand<T> {

    public CopyDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.CopyVolumeData,
                new CopyVolumeDataVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getSrcInfo(),
                        getParameters().getDstInfo(),
                        getParameters().isCollapse()),
                getParameters().getStoragePoolId(), this);
        setSucceeded(true);
    }

    @Override
    protected StepEnum getCommandStep() {
        return StepEnum.COPY_VOLUME;
    }

    @Override
    public boolean shouldUpdateStepProgress() {
        return true;
    }

    @Override
    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        if (getParameters().getJobWeight() != null && getParameters().getDstInfo() instanceof VdsmImageLocationInfo) {
            return Collections.singletonList(new StepSubjectEntity(VdcObjectType.Disk,
                    ((VdsmImageLocationInfo) getParameters().getDstInfo()).getImageGroupId(),
                    getParameters().getJobWeight()));
        }

        return super.getCommandStepSubjectEntities();
    }
}
