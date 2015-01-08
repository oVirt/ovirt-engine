package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmWatchdogValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.dao.VmDeviceDAO;

/**
 * Abstract base-class for watchdog manipulation commands.
 */
public abstract class AbstractVmWatchdogCommand<T extends WatchdogParameters> extends CommandBase<T> {

    public AbstractVmWatchdogCommand(T parameters) {
        super(parameters, null);
    }

    public AbstractVmWatchdogCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected List<VmDevice> getWatchdogs() {
        return getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceGeneralType.WATCHDOG);
    }
    protected VmDeviceDAO getVmDeviceDao() {
        return getDbFacade().getVmDeviceDao();
    }

    @Override
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
        if (!entityExists()) {
            return failCanDoAction(getParameters().isVm() ? VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND
                    : VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return true;
    }

    protected boolean entityExists() {
        if (getParameters().isVm()) {
            setVmId(getParameters().getId());
            return getVm() != null;
        } else {
            setVmTemplateId(getParameters().getId());
            return getVmTemplate() != null;
        }
    }

    private VmWatchdogValidator getVmWatchdogValidator() {
        VmWatchdogValidator vmWatchdogValidator = null;
        VmWatchdog watchdog = new VmWatchdog();
        watchdog.setAction(getParameters().getAction());
        watchdog.setModel(getParameters().getModel());
        watchdog.setVmId(getParameters().getId());

        if (getParameters().isVm()) {
            vmWatchdogValidator = new VmWatchdogValidator(getVm().getOs(), watchdog,
                    getVm().getVdsGroupCompatibilityVersion());
        } else {
            if (getVmTemplate().getVdsGroupId() != null) {
                vmWatchdogValidator = new VmWatchdogValidator(getVmTemplate().getOsId(), watchdog,
                        (getVdsGroupDAO().get(getVmTemplate().getVdsGroupId())).getCompatibilityVersion());
            }
        }

        return vmWatchdogValidator;
    }

    protected ValidationResult validateModelCompatibleWithOs() {
        VmWatchdogValidator validator = getVmWatchdogValidator();
        if (validator != null) {
            return validator.isModelCompatibleWithOs();
        } else {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }
    }

}
