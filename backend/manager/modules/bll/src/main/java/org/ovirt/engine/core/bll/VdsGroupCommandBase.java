package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;

public abstract class VdsGroupCommandBase<T extends VdsGroupParametersBase> extends CommandBase<T> {
    private VDSGroup _vdsGroup;

    protected VdsGroupCommandBase(T parameters) {
        this(parameters, null);
    }

    protected VdsGroupCommandBase(Guid commandId) {
        super(commandId);
    }

    public VdsGroupCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public VDSGroup getVdsGroup() {
        if (_vdsGroup == null) {
            _vdsGroup = getVdsGroupDAO().get(getParameters().getVdsGroupId());
        }
        return _vdsGroup;
    }

    @Override
    public String getVdsGroupName() {
        if (getVdsGroup() != null) {
            return getVdsGroup().getName();
        } else {
            return null;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        return permissionList;
    }
}
