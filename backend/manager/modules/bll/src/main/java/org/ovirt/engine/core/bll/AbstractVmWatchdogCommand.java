package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDAO;

/**
 * Abstract base-class for watchdog manipulation commands.
 */
public abstract class AbstractVmWatchdogCommand<T extends WatchdogParameters> extends CommandBase<T> {

    public AbstractVmWatchdogCommand(T parameters) {
        super(parameters);
    }

    protected List<VmDevice> getWatchdogs() {
        return getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceType.WATCHDOG.getName());
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getParameters().getId(),
                getParameters().isVm() ? VdcObjectType.VM : VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    /**
     * Create specParams from the parameters.
     */
    protected HashMap<String, Object> getSpecParams() {
        HashMap<String, Object> specParams = new HashMap<String, Object>();
        specParams.put("action", getParameters().getAction().name().toLowerCase());
        specParams.put("model", getParameters().getModel().name());
        return specParams;
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getId() == null || !entityExists()) {
            return failCanDoAction(getParameters().isVm() ? VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND
                    : VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return true;
    }

    protected boolean entityExists() {
        if (getParameters().isVm()) {
            return getVmDAO().get(getParameters().getId()) != null;
        } else {
            return getVmTemplateDAO().get(getParameters().getId()) != null;
        }
    }
}
