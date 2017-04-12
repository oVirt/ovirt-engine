package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.dao.DiskDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveAllVmCinderDisksCommand<T extends RemoveAllVmCinderDisksParameters> extends VmCommand<T> {

    @Inject
    private DiskDao diskDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public RemoveAllVmCinderDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        Collection<CinderDisk> failedRemoving = new LinkedList<>();
        for (final CinderDisk cinderDisk : getCinderDisksToBeRemoved()) {
            if (Boolean.TRUE.equals(cinderDisk.getActive())) {
                ActionReturnValue actionReturnValuernValue = removeCinderDisk(cinderDisk);
                if (actionReturnValuernValue == null || !actionReturnValuernValue.getSucceeded()) {
                    failedRemoving.add(cinderDisk);
                    logRemoveCinderDiskError(cinderDisk, actionReturnValuernValue);
                }
            }
        }
        setActionReturnValue(failedRemoving);
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
    }

    private void logRemoveCinderDiskError(CinderDisk cinderDisk, ActionReturnValue actionReturnValue) {
        log.error("Can't remove cinder disk id '{}' for VM id '{}' from domain id '{}' due to: {}.",
                cinderDisk.getImageId(),
                getParameters().getVmId(),
                cinderDisk.getStorageIds().get(0),
                actionReturnValue != null ? actionReturnValue.getFault().getMessage() : "");
    }

    private ActionReturnValue removeCinderDisk(CinderDisk cinderDisk) {
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.RemoveCinderDisk,
                buildChildCommandParameters(cinderDisk),
                cloneContextAndDetachFromParent());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing Cinder disk", e);
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
            cinderDisks = DisksFilter.filterCinderDisks(diskDao.getAllForVm(getVmId()));
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
        return callbackProvider.get();
    }
}
