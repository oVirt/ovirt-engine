package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

public abstract class ImageSpmCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {
    private Guid cachedSpmId;

    public ImageSpmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private Guid getPoolSpmId() {
        if (cachedSpmId == null) {
            cachedSpmId = getStoragePool().getSpmVdsId();
        }
        return cachedSpmId;
    }

    @Override
    protected boolean validate() {
        if (getPoolSpmId() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_SPM);
        }

        setStoragePool(null);
        if (getStoragePool() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }

        if (!getPoolSpmId().equals(getStoragePool().getSpmVdsId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SPM_CHANGED);
        }

        VdsDynamic vdsDynamic = getVdsDynamicDao().get(getPoolSpmId());
        if (vdsDynamic == null || vdsDynamic.getStatus() != VDSStatus.Up) {
            addValidationMessage(EngineMessage.VAR__HOST_STATUS__UP);
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
        }

        setVdsId(vdsDynamic.getId());

        if (!commandSpecificValidate()) {
            return false;
        }

        return true;
    }

    protected boolean commandSpecificValidate() {
        return true;
    }

    protected abstract VDSReturnValue executeVdsCommand();

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = executeVdsCommand();

        if (vdsReturnValue.getSucceeded()) {
            Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
            getTaskIdList().add(createTask(taskId,
                    vdsReturnValue.getCreationInfo(),
                    getParameters().getParentCommand(),
                    VdcObjectType.Storage,
                    getParameters().getStorageDomainId(),
                    getParameters().getDestinationImageId()));

            setSucceeded(true);
        }
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getStoragePool() != null && getPoolSpmId() != null) {
            return Collections.singletonMap(getPoolSpmId().toString(),
                    new Pair<>(LockingGroup.VDS_EXECUTION.toString(),
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.toString()));
        }
        return null;
    }
}
