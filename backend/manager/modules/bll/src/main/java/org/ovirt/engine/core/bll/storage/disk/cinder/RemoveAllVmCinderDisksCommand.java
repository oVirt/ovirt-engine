package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveAllVmCinderDisksCommand<T extends RemoveAllVmCinderDisksParameters> extends VmCommand<T> {

    public RemoveAllVmCinderDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        Collection<CinderDisk> failedRemoving = new LinkedList<>();
        for (final CinderDisk cinderDisk : getCinderDisksToBeRemoved()) {
            if (Boolean.TRUE.equals(cinderDisk.getActive())) {
                VdcReturnValueBase vdcReturnValue = removeCinderDisk(cinderDisk);
                if (vdcReturnValue == null || !vdcReturnValue.getSucceeded()) {
                    failedRemoving.add(cinderDisk);
                    logRemoveCinderDiskError(cinderDisk, vdcReturnValue);
                }
            }
        }
        setActionReturnValue(failedRemoving);
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
    }

    private void logRemoveCinderDiskError(CinderDisk cinderDisk, VdcReturnValueBase vdcReturnValue) {
        log.error("Can't remove cinder disk id '{}' for VM id '{}' from domain id '{}' due to: {}.",
                cinderDisk.getImageId(),
                getParameters().getVmId(),
                cinderDisk.getStorageIds().get(0),
                vdcReturnValue != null ? vdcReturnValue.getFault().getMessage() : "");
    }

    private VdcReturnValueBase removeCinderDisk(CinderDisk cinderDisk) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveCinderDisk,
                buildChildCommandParameters(cinderDisk),
                cloneContextAndDetachFromParent());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RemoveCinderDiskParameters buildChildCommandParameters(CinderDisk cinderDisk) {
        RemoveCinderDiskParameters removeDiskParams = new RemoveCinderDiskParameters(cinderDisk.getId());
        removeDiskParams.setParentCommand(getActionType());
        removeDiskParams.setParentParameters(getParameters());
        removeDiskParams.setShouldBeLogged(false);
        removeDiskParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return removeDiskParams;
    }

    private List<CinderDisk> getCinderDisksToBeRemoved() {
        List<CinderDisk> imageDisks = getParameters().cinderDisks;
        List<CinderDisk> cinderDisks = new ArrayList<>();
        if (imageDisks == null) {
            cinderDisks =
                    ImagesHandler.filterDisksBasedOnCinder(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()));
        } else {
            for (DiskImage diskImage : imageDisks) {
                cinderDisks.add((CinderDisk) diskImage);
            }
        }
        return cinderDisks;
    }

    @Override
    protected void endSuccessfully() {
        // handled by parent command
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // handled by parent command
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return new ConcurrentChildCommandsExecutionCallback();
    }
}
