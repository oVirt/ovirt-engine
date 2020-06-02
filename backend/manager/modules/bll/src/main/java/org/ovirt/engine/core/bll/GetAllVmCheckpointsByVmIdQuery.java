package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmCheckpointDao;

public class GetAllVmCheckpointsByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmCheckpointDao vmCheckpointDao;

    public GetAllVmCheckpointsByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmCheckpoint> vmCheckpoints = vmCheckpointDao.getAllForVm(getParameters().getId());
        vmCheckpoints.forEach(
                vmCheckpoint -> vmCheckpoint.setDisks(vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId())));
        setReturnValue(vmCheckpoints);
    }
}
