package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.MoveDiskParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.compat.Guid;

@SuppressWarnings("serial")
public class MoveDiskCommand<T extends MoveDiskParameters> extends BaseImagesCommand<T>
        implements QuotaStorageDependent {
    public MoveDiskCommand(Guid commandId) {
        super(commandId);
    }

    public MoveDiskCommand(T parameters) {
        super(parameters);
        VdcActionType internalCommandType = VdcActionType.MoveOrCopyDisk;
        MoveOrCopyImageGroupParameters internalCommandParams = getParameters();
        if (shouldLiveMigrationBeUsed()) {
            internalCommandType = VdcActionType.LiveMigrateDisk;
            internalCommandParams =
                    new LiveMigrateDiskParameters(getImageId(),
                            getParameters().getSourceDomainId(),
                            getParameters().getStorageDomainId(),
                            getVMByParameterDisk().getId());
            internalCommandParams.setSessionId(getParameters().getSessionId());
        }

        cmd = CommandsFactory.CreateCommand(internalCommandType, internalCommandParams);
    }

    private VM vmForDisk = null;
    private boolean isVmForDiskInitilized = false;
    private CommandBase<? extends MoveOrCopyImageGroupParameters> cmd;

    @Override
    protected void executeCommand() {
        cmd.execute();
        handleChildReturnValue();
    }

    protected boolean shouldLiveMigrationBeUsed() {
        VM vmWithDisk = getVMByParameterDisk();
        return vmWithDisk != null && vmWithDisk.getStatus() == VMStatus.Up && vmWithDisk.getRunOnVds() != null
                && !vmWithDisk.getRunOnVds().equals(Guid.Empty);
    }

    private void handleChildReturnValue() {
        getReturnValue().setSucceeded(cmd.getReturnValue().getSucceeded());
        getReturnValue().setCanDoAction(cmd.getReturnValue().getCanDoAction());
        getReturnValue().setCanDoActionMessages(cmd.getReturnValue().getCanDoActionMessages());
        getReturnValue().setFault(cmd.getReturnValue().getFault());
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = cmd.canDoAction();
        cmd.getReturnValue().setCanDoAction(retVal);
        handleChildReturnValue();
        return retVal;
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return cmd.isUserAuthorizedToRunAction();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        return cmd.getJobMessageProperties();
    }

    @Override
    protected void setActionMessageParameters() {
        cmd.setActionMessageParameters();
    }

    @Override
    protected VdcActionType getActionType() {
        return cmd.getActionType();
    }

    @Override
    protected void freeLock() {
        cmd.freeLock();
    }

    private VM getVMByParameterDisk() {
        if (isVmForDiskInitilized) {
            return vmForDisk;
        }

        isVmForDiskInitilized = true;
        Map<Boolean, List<VM>> allVms = getVmDAO().getForDisk(getDiskImage().getId());
        if (allVms.isEmpty()) {
            return null;
        }

        List<VM> pluggedVms = allVms.get(true);
        if (pluggedVms == null) {
            return null;
        }

        vmForDisk = pluggedVms.get(0);
        return vmForDisk;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        if (cmd instanceof QuotaStorageDependent) {
            return ((QuotaStorageDependent) cmd).getQuotaStorageConsumptionParameters();
        }
        return new ArrayList<QuotaConsumptionParameter>();
    }
}
