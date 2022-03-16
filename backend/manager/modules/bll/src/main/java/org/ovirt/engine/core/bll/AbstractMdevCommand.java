package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public abstract class AbstractMdevCommand<T extends MdevParameters> extends CommandBase<T> {

    @Inject
    protected ResourceManager resourceManager;
    @Inject
    protected VmDeviceUtils vmDeviceUtils;

    public AbstractMdevCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (parameters.isVm()) {
            setVmId(parameters.getDevice().getVmId());
        } else {
            setVmTemplateId(parameters.getDevice().getVmId());
        }
    }

    @Override
    protected boolean validate() {
        VmDevice device = getParameters().getDevice();
        if (device == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DEVICE_MUST_BE_SPECIFIED);
        }
        if (getParameters().isVm() && getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        if (!getParameters().isVm() && getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getVmBaseId(),
                getParameters().isVm() ? VdcObjectType.VM : VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected Guid getVmBaseId() {
        return getParameters().getDevice().getVmId();
    }
}
