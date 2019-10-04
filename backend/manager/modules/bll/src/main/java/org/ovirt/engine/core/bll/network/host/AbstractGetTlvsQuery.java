package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.vdscommands.GetLldpVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public abstract class AbstractGetTlvsQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    private GetLldpVDSCommandParameters lldpVDSCommandParameters;
    private VDS vds;

    @Inject
    private VdsDao vdsDao;

    public AbstractGetTlvsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected VDS getHost() {
        if (vds == null) {
            vds = vdsDao.get(getHostId());
        }
        return vds;
    }

    @Override
    protected void executeQueryCommand() {
        VDSReturnValue command = runVdsCommand(VDSCommandType.GetLldp, lldpVDSCommandParameters);
        getQueryReturnValue().setReturnValue(command.getReturnValue());
    }

    public void setLldpVDSCommandParameters(GetLldpVDSCommandParameters lldpVDSCommandParameters) {
        this.lldpVDSCommandParameters = lldpVDSCommandParameters;
    }

    protected abstract Guid getHostId();
}
