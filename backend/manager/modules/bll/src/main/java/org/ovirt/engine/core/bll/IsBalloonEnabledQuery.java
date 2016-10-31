package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

public class IsBalloonEnabledQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private VmDeviceDao vmDeviceDao;

    public IsBalloonEnabledQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Snapshot snapshot = snapshotDao.get(
                getParameters().getId(), Snapshot.SnapshotType.NEXT_RUN, getUserID(), getParameters().isFiltered());

        if (snapshot != null) {
            VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                    snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
            getQueryReturnValue().setReturnValue(
                    VmDeviceCommonUtils.isVmDeviceExists(vm.getManagedVmDeviceMap(), VmDeviceType.MEMBALLOON));
        } else {
            getQueryReturnValue().setReturnValue(vmDeviceDao.isMemBalloonEnabled(getParameters().getId()));
        }
    }
}
