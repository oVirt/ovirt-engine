package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@InternalCommandAttribute
public class CloneCinderDisksCommand<T extends CloneCinderDisksParameters> extends CommandBase<T> {

    public CloneCinderDisksCommand(T parameters) {
        this(parameters, null);
    }

    public CloneCinderDisksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        boolean isSucceeded = true;
        Map<Guid, Guid> diskImageMap = new HashMap<>();
        for (CinderDisk disk : getParameters().getCinderDisks()) {
            ImagesContainterParametersBase params = buildChildCommandParameters(disk);
            Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                    VdcActionType.CloneSingleCinderDisk,
                    params,
                    cloneContextAndDetachFromParent());
            try {
                VdcReturnValueBase vdcReturnValueBase = future.get();
                if (!vdcReturnValueBase.getSucceeded()) {
                    log.error("Error cloning Cinder disk '{}': {}", disk.getDiskAlias());
                    getReturnValue().setFault(vdcReturnValueBase.getFault());
                    isSucceeded = false;
                    break;
                }
                Guid imageId = vdcReturnValueBase.getActionReturnValue();
                diskImageMap.put(disk.getId(), imageId);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error cloning Cinder disk '{}': {}", disk.getDiskAlias(), e.getMessage());
                isSucceeded = false;
            }

        }
        getReturnValue().setActionReturnValue(diskImageMap);
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(isSucceeded);
    }

    private ImagesContainterParametersBase buildChildCommandParameters(CinderDisk cinderDisk) {
        ImagesContainterParametersBase createParams = new ImagesContainterParametersBase(cinderDisk.getId());
        DiskImage templateDisk = getParameters().getDisksMap().get(cinderDisk.getId());
        cinderDisk.setDiskAlias(templateDisk.getDiskAlias());
        createParams.setStorageDomainId(templateDisk.getStorageIds().get(0));
        createParams.setEntityInfo(getParameters().getEntityInfo());
        return withRootCommandInfo(createParams, getParameters().getParentCommand());
    }

    @Override
    public CommandCallback getCallback() {
        return new CloneCinderDisksCommandCallback();
    }

    @Override
    protected void endSuccessfully() {
        if (!getParameters().isParentHasTasks()) {
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
