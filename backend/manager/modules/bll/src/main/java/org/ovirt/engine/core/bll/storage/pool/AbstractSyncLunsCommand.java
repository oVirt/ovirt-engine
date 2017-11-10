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
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public abstract class AbstractSyncLunsCommand<T extends SyncLunsParameters> extends StorageHandlingCommandBase<T> {

    @Inject
    protected VdsCommandsHelper vdsCommandsHelper;

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
        return HostValidator.createInstance(getVds());
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
        if (getParameters().getDeviceList() == null) {
            return runGetDeviceList(lunsIds);
        }
        if (lunsIds == null) {
            return getParameters().getDeviceList();
        }
        return getParameters().getDeviceList().stream()
                .filter(lun -> lunsIds.contains(lun.getId()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected List<LUNs> runGetDeviceList(Set<String> lunsIds) {
        GetDeviceListVDSCommandParameters parameters = new GetDeviceListVDSCommandParameters(
                getParameters().getVdsId(), StorageType.UNKNOWN, false, lunsIds);
        return (List<LUNs>) vdsCommandsHelper.runVdsCommandWithoutFailover(
                VDSCommandType.GetDeviceList, parameters, getParameters().getStoragePoolId(), null)
                .getReturnValue();
    }
}
