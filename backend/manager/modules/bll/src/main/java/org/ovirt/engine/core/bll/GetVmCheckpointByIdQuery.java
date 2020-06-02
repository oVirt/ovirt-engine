package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmCheckpointDao;

public class GetVmCheckpointByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmCheckpointDao vmCheckpointDao;

    public GetVmCheckpointByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VmCheckpoint vmCheckpoint = vmCheckpointDao.get(getParameters().getId());
        if (vmCheckpoint != null) {
            vmCheckpoint.setDisks(vmCheckpointDao.getDisksByCheckpointId(vmCheckpoint.getId()));
        }
        setReturnValue(vmCheckpoint);
    }
}
