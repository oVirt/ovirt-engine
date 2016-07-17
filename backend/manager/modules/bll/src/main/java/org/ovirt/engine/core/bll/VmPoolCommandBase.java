package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmPoolDao;

public abstract class VmPoolCommandBase<T extends VmPoolParametersBase> extends CommandBase<T> {

    private VmPool vmPool;
    @Inject
    private VmPoolDao vmPoolDao;

    protected VmPool getVmPool() {
        if (vmPool == null && getVmPoolId() != null) {
            vmPool = getVmPoolDao().get(getVmPoolId());
        }
        return vmPool;
    }

    protected void setVmPool(VmPool value) {
        vmPool = value;
    }

    protected Guid getVmPoolId() {
        return getParameters().getVmPoolId();
    }

    protected void setVmPoolId(Guid value) {
        getParameters().setVmPoolId(value);
    }

    protected String getVmPoolName() {
        return getVmPool() != null ? getVmPool().getName() : null;
    }

    @Override
    protected String getDescription() {
        return getVmPoolName();
    }

    protected VmPoolDao getVmPoolDao() {
        return vmPoolDao;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmPoolCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getVmPoolId(),
                VdcObjectType.VmPool,
                getActionType().getActionGroup()));
        return permissionList;
    }

}
