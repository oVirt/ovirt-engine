package org.ovirt.engine.core.bll.network.template;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@SuppressWarnings("serial")
@CustomLogFields({ "InterfaceName", "InterfaceType" })
public abstract class VmTemplateInterfaceCommandBase<T extends AddVmTemplateInterfaceParameters>
        extends VmTemplateCommand<T> {

    public VmTemplateInterfaceCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmTemplateInterfaceCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    public String getInterfaceType() {
        return VmInterfaceType.forValue(getParameters().getInterface().getType()).getDescription();
    }

    public String getNetworkName() {
        return getParameters().getInterface().getNetworkName();
    }

    protected boolean interfaceNameUnique(List<VmNetworkInterface> interfaces) {
        return VmHandler.IsNotDuplicateInterfaceName(interfaces,
                getInterfaceName(),
                getReturnValue().getCanDoActionMessages());
    }

    protected ValidationResult linkedToTemplate() {
        return getParameters().getInterface().getVmId() == null ? ValidationResult.VALID
                : new ValidationResult(VdcBllMessages.NETWORK_INTERFACE_VM_CANNOT_BE_SET);
    }
}
