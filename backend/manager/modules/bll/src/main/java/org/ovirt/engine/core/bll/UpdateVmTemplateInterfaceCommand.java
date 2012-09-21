package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName"), @CustomLogField("InterfaceName") })
public class UpdateVmTemplateInterfaceCommand<T extends AddVmTemplateInterfaceParameters> extends VmTemplateCommand<T> {
    public UpdateVmTemplateInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return getParameters().getInterface().getName();
    }

    @Override
    protected void executeCommand() {
        AddCustomValue("InterfaceType", (VmInterfaceType.forValue(getParameters().getInterface().getType()).getInterfaceTranslation()).toString());
        DbFacade.getInstance()
                .getVmNetworkInterfaceDao()
                .update(getParameters().getInterface());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        if (getParameters().getInterface().getVmId() != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_VM_CANNOT_BE_SET);
            return false;
        }

        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDao()
                    .getAllForTemplate(getParameters().getVmTemplateId());

        if (getVmTemplate() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            return false;
        }

        // Interface oldIface = interfaces.First(i => i.id ==
        // AddVmInterfaceParameters.Interface.id);
        VmNetworkInterface oldIface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterface().getId());
            }
        });

        if (!StringHelper.EqOp(oldIface.getName(), getParameters().getInterface().getName())) {
            if (!VmHandler.IsNotDuplicateInterfaceName(interfaces,
                            getParameters().getInterface().getName(),
                            getReturnValue().getCanDoActionMessages())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    /**
     * Set the parameters for bll messages, such as type and action,
     */
    @Override
    protected void setActionMessageParameters()
    {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__INTERFACE);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE
                : AuditLogType.NETWORK_UPDATE_TEMPLATE_INTERFACE_FAILED;
    }
}
