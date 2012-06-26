package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.group.DesktopVM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class VmManagementCommandBase<T extends VmManagementParametersBase> extends VmCommand<T> {

    private static final long serialVersionUID = -5731227168422981908L;

    public VmManagementCommandBase(T parameters) {
        super(parameters);
        if (parameters.getVmStaticData() != null) {
            setVmId(parameters.getVmStaticData().getId());
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
            Guid guid = vm.getdedicated_vm_for_vds().getValue();
            // get dedicated host cluster and comparing it to VM cluster
            VDS vds = getVdsDAO().get(guid);
            result = vds != null && (vm.getvds_group_id().equals(vds.getvds_group_id()));
        }
        if (!result) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.ACTION_TYPE_FAILED_DEDICATED_VDS_NOT_IN_SAME_CLUSTER.toString());
        }
        return result;
    }

    private final static Pattern cpuPinningPattern =
            Pattern.compile("\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*" +
                    "(_\\d+#(\\^\\d+|\\d+\\-\\d+|\\d+)(,(\\^\\d+|\\d+\\-\\d+|\\d+))*)*");

    static boolean isCpuPinningValid(final String cpuPinning) {
        return StringUtils.isEmpty(cpuPinning) || cpuPinningPattern.matcher(cpuPinning).matches();
    }

    static boolean validatePinningAndMigration(List<String> reasons, VmStatic vmStaticData, String cpuPinning) {
        final boolean cpuPinMigrationEnabled = Boolean.TRUE.equals(Config.<Boolean> GetValue(ConfigValues.CpuPinMigrationEnabled));
        if (!cpuPinMigrationEnabled
                && vmStaticData.getMigrationSupport() == MigrationSupport.MIGRATABLE
                && StringUtils.isNotEmpty(cpuPinning)) {
            reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_PINNED_TO_CPU_AND_MIGRATABLE.toString());
            return false;
        }
        return true;
    }

}
