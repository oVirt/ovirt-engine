package org.ovirt.engine.core.bll.network.template;

import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class RemoveVmTemplateInterfaceCommand<T extends RemoveVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {

    public RemoveVmTemplateInterfaceCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmNetworkInterface iface = getVmNetworkInterfaceDao().get(getParameters().getInterfaceId());
        if (iface != null) {
            addCustomValue("InterfaceName", iface.getName());
            addCustomValue("InterfaceType", VmInterfaceType.forValue(iface.getType()).getDescription());
        }
        getDbFacade().getVmDeviceDao().remove(new VmDeviceId(getParameters().getInterfaceId(),
                getParameters().getVmTemplateId()));
        getVmNicDao().remove(getParameters().getInterfaceId());
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE_FAILED;
    }
}
