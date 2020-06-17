package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public abstract class AbstractSyncLunsCommand<T extends SyncLunsParameters> extends StorageHandlingCommandBase<T> {

    @Inject
    protected VdsCommandsHelper vdsCommandsHelper;

    @Inject
    private VdsDao vdsDao;

    protected AbstractSyncLunsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        return validate(createStoragePoolValidator().existsAndUp());
    }

    protected boolean validateVds() {
        HostValidator hostValidator = getHostValidator();
        return validate(hostValidator.hostExists()) &&
                validate(hostValidator.isUp());
    }

    protected HostValidator getHostValidator() {
        VDS vds = vdsDao.get(getParameters().getVdsId());
        return HostValidator.createInstance(vds);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__SYNC);
    }

    protected List<LUNs> getDeviceList() {
        return getDeviceList(null);
    }

    protected List<LUNs> getDeviceList(Set<String> lunsIds) {
        return getDeviceList(lunsIds, null);
    }

    protected List<LUNs> getDeviceList(Set<String> lunsIds, Guid hostId) {
        if (getParameters().getDeviceList() == null) {
            return runGetDeviceList(lunsIds, hostId);
        }
        if (lunsIds == null) {
            return getParameters().getDeviceList();
        }
        return getParameters().getDeviceList().stream()
                .filter(lun -> lunsIds.contains(lun.getId()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected List<LUNs> runGetDeviceList(Set<String> lunsIds, Guid hostId) {
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), StorageType.UNKNOWN, false, lunsIds);
        if (hostId != null) {
            parameters.setVdsId(hostId);
        }
        return (List<LUNs>) vdsCommandsHelper.runVdsCommandWithoutFailover(
                VDSCommandType.GetDeviceList, parameters, getParameters().getStoragePoolId(), null)
                .getReturnValue();
    }
}
