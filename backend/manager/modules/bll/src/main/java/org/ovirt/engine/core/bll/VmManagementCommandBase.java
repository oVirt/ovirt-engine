package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.validation.group.DesktopVM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {
    public VmManagementCommandBase(T parameters) {
        super(parameters);
        if (parameters.getVmStaticData() != null) {
            super.setVmId(parameters.getVmStaticData().getId());
            setVdsGroupId(parameters.getVmStaticData().getvds_group_id());
        }
    }

    protected VmManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        if (getParameters().getVmStaticData().getvm_type() == VmType.Desktop) {
            addValidationGroup(DesktopVM.class);
        }
        return super.getValidationGroups();
    }

    /**
     * Checks that dedicated host is on the same cluster as the VM
     *
     * @param vm
     *            - the VM to check
     * @return
     */
    protected boolean isDedicatedVdsOnSameCluster(VmStatic vm) {
        boolean result = true;
        if (vm.getdedicated_vm_for_vds() != null) {
            // get dedicated host id
            Guid guid = new Guid(vm.getdedicated_vm_for_vds().getUuid());
            // get dedicated host cluster and comparing it to VM cluster
            VdsStatic vds = DbFacade.getInstance().getVdsStaticDAO().get(guid);
            result = (vm.getvds_group_id().equals(vds.getvds_group_id()));
        }
        if (!result) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER.toString());
        }
        return result;
    }

}
