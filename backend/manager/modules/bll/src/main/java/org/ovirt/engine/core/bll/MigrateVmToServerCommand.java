package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MigrateVmToServerCommand<T extends MigrateVmToServerParameters> extends MigrateVmCommand<T> {
    public MigrateVmToServerCommand(T parameters) {
        super(parameters);
        setVdsDestinationId(parameters.getVdsId());
        getVdsSelector().setDestinationVdsId(getVdsDestinationId());
    }

    @Override
    protected boolean canDoAction() {
        VM vm = getVm();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__MIGRATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        Guid destinationId = getVdsDestinationId();
        VDS vds = DbFacade.getInstance().getVdsDAO().get(destinationId);
        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            return false;
        } else if (vm.getrun_on_vds() != null && vm.getrun_on_vds().equals(destinationId)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_TO_SAME_HOST);
            return false;
        } else if (!vm.getvds_group_id().equals(getDestinationVds().getvds_group_id())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATE_BETWEEN_TWO_CLUSTERS);
            return false;
        } else {
            return super.canDoAction();
        }
    }

    @Override
    protected void rerunInternal() {
        /**
         * In case we failed to migrate to that specific server, the VM should no longer be pending, and we
         * report failure, without an attempt to rerun
         */
        DecreasePendingVms(getDestinationVds().getId());
        _isRerun = false;
        setSucceeded(false);

        determineMigrationFailueForAuditLog();
        log();
    }
}
