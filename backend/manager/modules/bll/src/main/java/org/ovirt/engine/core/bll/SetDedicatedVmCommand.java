package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetDedicatedVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class SetDedicatedVmCommand<T extends SetDedicatedVmParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 5238609117685190192L;

    public SetDedicatedVmCommand(T parameters) {
        super(parameters);
        super.setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeVmCommand() {
        List<VM> vms = getVmDAO().getAllForDedicatedPowerClientByVds(getVdsId());
        if (vms != null && vms.size() != 0) {
            vms.get(0).setDedicatedVmForVds(null);
            getVmStaticDAO().update(vms.get(0).getStaticData());
        }
        VM vm = getVmDAO().get(getVmId());

        vm.setDedicatedVmForVds(!(getVdsId().equals(Guid.Empty)) ? getVdsId() : null);

        getVmStaticDAO().update(vm.getStaticData());

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT
                : AuditLogType.USER_DEDICATE_VM_TO_POWERCLIENT_FAILED;
    }
}
