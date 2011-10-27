package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetDedicatedVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class SetDedicatedVmCommand<T extends SetDedicatedVmParameters> extends VmCommand<T> {

    public SetDedicatedVmCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getVmId());
        super.setVdsId(parameters.getVdsId());
    }

    @Override
    protected void ExecuteVmCommand() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllForDedicatedPowerClientByVds(getVdsId());
        if (vms != null && vms.size() != 0) {
            vms.get(0).setdedicated_vm_for_vds(null);
            DbFacade.getInstance().getVmStaticDAO().update(vms.get(0).getStaticData());
        }
        VM vm = DbFacade.getInstance().getVmDAO().getById(getVmId());

        vm.setdedicated_vm_for_vds(!(getVdsId().equals(Guid.Empty)) ? getVdsId() : null);

        DbFacade.getInstance().getVmStaticDAO().update(vm.getStaticData());

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT
                : AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT_FAILED;
    }
}
