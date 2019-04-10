package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@ValidateSupportsTransaction
public class AddVdsSpmIdCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddVdsSpmIdCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsSpmIdCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWaitForever();
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        List<VdsSpmIdMap> vdsSpmIdMapList = vdsSpmIdMapDao.getAll(getDcId());
        if (vdsSpmIdMapList.size() >= Config.<Integer> getValue(ConfigValues.MaxNumberOfHostsInStoragePool)) {
            buildFaultResult();
            return;
        }
        insertSpmIdToDb(vdsSpmIdMapList);

        setSucceeded(true);
    }

    protected void insertSpmIdToDb(List<VdsSpmIdMap> vdsSpmIdMapList) {
        int selectedId = 1;
        List<Integer> list = vdsSpmIdMapList.stream().map(VdsSpmIdMap::getVdsSpmId).sorted().collect(Collectors.toList());

        for (int id : list) {
            if (selectedId == id) {
                selectedId++;
            } else {
                break;
            }
        }
        VdsSpmIdMap newMap = new VdsSpmIdMap(getDcId(), getVdsId(), selectedId);
        vdsSpmIdMapDao.save(newMap);
        if (getParameters().isCompensationEnabled()) {
            getCompensationContext().snapshotNewEntity(newMap);
            getCompensationContext().stateChanged();
        }
    }

    /**
     * Gets the dc id from cluster if DC was removed and cluster is attached to a new DC
     */
    private Guid getDcId() {
        final Guid hostDcId = getVds().getStoragePoolId();
        return hostDcId.equals(Guid.Empty)
                ? getCluster().getStoragePoolId()
                : hostDcId;
    }

    private void buildFaultResult() {
        EngineFault fault = new EngineFault();
        fault.setError(EngineError.ReachedMaxNumberOfHostsInDC);
        fault.setMessage(backend.getVdsErrorsTranslator().translateErrorTextSingle(fault.getError().toString()));
        getReturnValue().setFault(fault);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVds().getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.REGISTER_VDS, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
