package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmTemplateInterfaceCommand<T extends RemoveVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {

    public RemoveVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        AddCustomValue("InterfaceName", ((getParameters().getInterface().getName())));
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getInterfaceTranslation()).toString());
        DbFacade.getInstance().getVmNetworkInterfaceDAO().remove(getParameters().getInterface().getId());
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
