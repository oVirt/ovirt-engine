package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsBalloonEnabledQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public IsBalloonEnabledQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = getDbFacade().getSnapshotDao().get(
                getParameters().getId(), Snapshot.SnapshotType.NEXT_RUN, getUserID(), getParameters().isFiltered());

        if (snapshot != null) {
            SnapshotVmConfigurationHelper snapshotVmConfigurationHelper = new SnapshotVmConfigurationHelper();
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
            getQueryReturnValue().setReturnValue(
                    VmDeviceCommonUtils.isVmDeviceExists(vm.getManagedVmDeviceMap(), VmDeviceType.MEMBALLOON));
        } else {
            VmDeviceDao dao = getDbFacade().getVmDeviceDao();
            getQueryReturnValue().setReturnValue(dao.isMemBalloonEnabled(getParameters().getId()));
        }
    }

}
