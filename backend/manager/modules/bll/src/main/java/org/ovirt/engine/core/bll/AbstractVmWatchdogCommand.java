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
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.errors.EngineMessage;

/**
 * Abstract base-class for watchdog manipulation commands.
 */
public abstract class AbstractVmWatchdogCommand<T extends WatchdogParameters> extends CommandBase<T> {

    public AbstractVmWatchdogCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected List<VmDevice> getWatchdogs() {
        return getVmDeviceDao().getVmDeviceByVmIdAndType(getParameters().getId(),
                VmDeviceGeneralType.WATCHDOG);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getId(),
                getParameters().isVm() ? VdcObjectType.VM : VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    /**
     * Create specParams from the parameters.
     */
    protected HashMap<String, Object> getSpecParams() {
        HashMap<String, Object> specParams = new HashMap<>();
        specParams.put("action", getParameters().getAction().name().toLowerCase());
        specParams.put("model", getParameters().getModel().name());
        return specParams;
    }

    @Override
    protected boolean validate() {
        if (!entityExists()) {
            return failValidation(getParameters().isVm() ? EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND
                    : EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
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

    protected VmWatchdogValidator getVmWatchdogValidator() {
        VmWatchdogValidator vmWatchdogValidator = null;
        VmWatchdog watchdog = createWatchdogFromParams();

        if (getParameters().isVm()) {
            vmWatchdogValidator = new VmWatchdogValidator(getVm().getOs(), watchdog,
                    getVm().getCompatibilityVersion());
        } else {
            if (getVmTemplate().getClusterId() != null) {
                vmWatchdogValidator = new VmWatchdogValidator(getVmTemplate().getOsId(), watchdog,
                        getVmTemplate().getCompatibilityVersion());
            }
        }

        return vmWatchdogValidator;
    }

    private VmWatchdog createWatchdogFromParams() {
        VmWatchdog watchdog = new VmWatchdog();
        watchdog.setAction(getParameters().getAction());
        watchdog.setModel(getParameters().getModel());
        watchdog.setVmId(getParameters().getId());
        return watchdog;
    }

    protected ValidationResult validateWatchdog() {
        if (!getParameters().isClusterIndependent()) {
            VmWatchdogValidator validator = getVmWatchdogValidator();
            if (validator != null) {
                return validator.isValid();
            } else {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            }
        } else {
            return new VmWatchdogValidator.VmWatchdogClusterIndependentValidator(createWatchdogFromParams()).isValid();
        }

    }

}
