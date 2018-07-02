package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddDiskToTemplateCommand<T extends AddDiskParameters> extends AddDiskCommand<T> {

    public AddDiskToTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public AddDiskToTemplateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmTemplateId(getParameters().getVmId());
    }

    @Override
    protected boolean validateVm() {
        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        return true;
    }

    protected void setDiskAlias() {
        // noop, disk is already set with a proper alias
    }

    @Override
    protected boolean shouldDiskBePlugged() {
        return true;
    }

    @Override
    protected void attachImage() {
        getCompensationContext().snapshotEntity(addDiskVmElementForDisk(getDiskVmElement()));
        getCompensationContext().snapshotNewEntity(addManagedDeviceForDisk(getParameters().getDiskInfo().getId()));
        getCompensationContext().stateChanged();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected List<PermissionSubject> getVmPermissionSubject() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getVmId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }
}
