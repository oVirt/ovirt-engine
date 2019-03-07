package org.ovirt.engine.core.bll.network.template;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.vm.BackwardCompatibilityVnicHelper;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskVmElementDao;

public abstract class VmTemplateInterfaceCommandBase<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateCommand<T> {

    @Inject
    private BackwardCompatibilityVnicHelper backwardCompatibilityVnicHelper;
    @Inject
    private DiskVmElementDao diskVmElementDao;

    public VmTemplateInterfaceCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmTemplateInterfaceCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__INTERFACE);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    public String getInterfaceType() {
        return VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription();
    }

    protected boolean interfaceNameUnique(List<VmNic> interfaces) {
        return validate(vmHandler.isNotDuplicateInterfaceName(interfaces, getInterfaceName()));
    }

    protected boolean updateVnicForBackwardCompatibility(VmNic oldNic) {
        if (!validate(
                backwardCompatibilityVnicHelper.updateNicForBackwardCompatibility(
                        getParameters().getInterface(),
                        oldNic,
                        getParameters().getNetworkName(),
                        getParameters().isPortMirroring(),
                        getVmTemplate(),
                        getCurrentUser()))) {
            return false;
        }

        return true;
    }

    protected boolean updateVnicForBackwardCompatibility() {
        return updateVnicForBackwardCompatibility(null);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getCluster() == null && getVmTemplate().getTemplateType() != VmEntityType.INSTANCE_TYPE) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
            return false;
        }

        return true;
    }

    protected List<DiskVmElement> getTemplateDiskVmElements() {
        return diskVmElementDao.getAllForVm(getVmTemplateId());
    }
}
