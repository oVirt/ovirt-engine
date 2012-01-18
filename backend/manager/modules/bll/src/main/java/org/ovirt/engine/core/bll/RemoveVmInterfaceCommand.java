package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("InterfaceName") })
public class RemoveVmInterfaceCommand<T extends RemoveVmInterfaceParameters> extends VmCommand<T> {
    private String _interfaceName = "";

    public RemoveVmInterfaceCommand(T parameters) {
        super(parameters);
    }

    public String getInterfaceName() {
        return _interfaceName;
    }

    @Override
    protected void ExecuteVmCommand() {
        this.setVmName(DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmId()).getvm_name());

        // return mac to pool
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getParameters().getVmId());

        // LINQ 29456
        // Interface iface = interfaces.FirstOrDefault(i => i.id ==
        // RemoveVmInterfaceParameters.InterfaceId);
        VmNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
            @Override
            public boolean eval(VmNetworkInterface i) {
                return i.getId().equals(getParameters().getInterfaceId());
            }
        });
        // LINQ 29456
        if (iface != null) {
            MacPoolManager.getInstance().freeMac(iface.getMacAddress());
            _interfaceName = iface.getName();

            // Get Interface type.
            String interType = VmInterfaceType.forValue(iface.getType()).getInterfaceTranslation().toString();
            if (interType != null) {
                AddCustomValue("InterfaceType", interType);
            }
        }

        // remove from db
        DbFacade dbFacade = DbFacade.getInstance();
        dbFacade.getVmNetworkInterfaceDAO().remove(getParameters().getInterfaceId());
        dbFacade.getVmNetworkStatisticsDAO().remove(getParameters().getInterfaceId());
        dbFacade.getVmDeviceDAO()
                .remove(new VmDeviceId(getParameters().getInterfaceId(), getParameters().getVmId()));
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        VmDynamic vm = DbFacade.getInstance().getVmDynamicDAO().get(getParameters().getVmId());
        if (vm.getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CHANGE_STATUS_WHEN_NOT_DOWN);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_REMOVE_VM_INTERFACE
                : AuditLogType.NETWORK_REMOVE_VM_INTERFACE_FAILED;
    }
}
