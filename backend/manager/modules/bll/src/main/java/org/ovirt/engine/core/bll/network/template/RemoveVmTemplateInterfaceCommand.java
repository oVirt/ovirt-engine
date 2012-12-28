package org.ovirt.engine.core.bll.network.template;

import org.ovirt.engine.core.bll.VmTemplateCommand;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SuppressWarnings("serial")
public class RemoveVmTemplateInterfaceCommand<T extends RemoveVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {

    public RemoveVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VmNetworkInterface iface = getVmNetworkInterfaceDao().get(getParameters().getInterfaceId());
        if (iface != null) {
            AddCustomValue("InterfaceName", iface.getName());
            AddCustomValue("InterfaceType", VmInterfaceType.forValue(iface.getType()).getDescription());
        }
        DbFacade.getInstance().getVmDeviceDao().remove(
                new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmTemplateId()));
        getVmNetworkInterfaceDao().remove(getParameters().getInterfaceId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_REMOVE_TEMPLATE_INTERFACE_FAILED;
    }
}
