package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public abstract class AbstractGraphicsDeviceCommand<T extends GraphicsParameters> extends CommandBase<T> {

    @Inject
    protected ResourceManager resourceManager;
    @Inject
    protected VmDeviceUtils vmDeviceUtils;

    public AbstractGraphicsDeviceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        if (parameters.isVm()) {
            setVmId(parameters.getDev().getVmId());
        } else {
            setVmTemplateId(parameters.getDev().getVmId());
        }
    }

    @Override
    protected boolean validate() {
        GraphicsDevice dev = getParameters().getDev();

        if (dev == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DEVICE_MUST_BE_SPECIFIED);
        }

        if (getParameters().isVm() && getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!getParameters().isVm() && getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (dev.getGraphicsType() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GRAPHIC_TYPE_MUST_BE_SPECIFIED);
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
        return getParameters().getDev().getVmId();
    }
}
