package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmOvfByVmIdParameters;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmOvfByVmIdQuery<P extends GetVmOvfByVmIdParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;

    @Inject
    private OvfHelper ovfHelper;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private VmDeviceUtils vmDeviceUtils;

    public GetVmOvfByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (vm == null || vm.getDbGeneration() != getParameters().getRequiredGeneration()) {
            getQueryReturnValue().setSucceeded(false);
            return;
        }

        vm.setSnapshots(snapshotDao.getAllWithConfiguration(vm.getId()));
        vmDeviceUtils.setVmDevices(vm.getStaticData());
        String ovfData = generateOvfConfig(vm, getParameters().isAsOva());

        if (ovfData == null) {
            return;
        }

        getQueryReturnValue().setReturnValue(ovfData);
    }

    protected String generateOvfConfig(VM vm, boolean asOva) {
        return ovfHelper.generateOvfConfigurationForVm(vm, asOva);
    }
}
