package org.ovirt.engine.core.bll.storage.pool;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public abstract class AbstractSyncLunsCommand<T extends SyncLunsParameters> extends StorageHandlingCommandBase<T> {

    protected AbstractSyncLunsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!checkStoragePool()) {
            return false;
        }
        return super.validate();
    }

    protected List<LUNs> getDeviceList() {
        return getDeviceList(null);
    }

    protected List<LUNs> getDeviceList(List<String> lunsIds) {
        if (getParameters().getDeviceList() == null) {
            return runGetDeviceList(lunsIds);
        }
        if (lunsIds == null) {
            return getParameters().getDeviceList();
        }
        Set<String> lunsIdsSet = new HashSet<>(lunsIds);
        return getParameters().getDeviceList().stream()
                .filter(lun -> lunsIdsSet.contains(lun.getId()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected List<LUNs> runGetDeviceList(List<String> lunsIds) {
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), StorageType.UNKNOWN, false, lunsIds);
        return (List<LUNs>) VdsCommandsHelper.runVdsCommandWithoutFailover(
                VDSCommandType.GetDeviceList, parameters, getParameters().getStoragePoolId(), null)
                .getReturnValue();
    }
}
