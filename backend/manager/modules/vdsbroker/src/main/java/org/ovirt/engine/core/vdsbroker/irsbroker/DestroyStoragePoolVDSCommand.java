package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DestroyStoragePoolVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public DestroyStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        VDS vds = DbFacade.getInstance().getVdsDao().get(this.getCurrentIrsProxyData().getCurrentVdsId());
        status = getIrsProxy().destroyStoragePool(getParameters().getStoragePoolId().toString(),
                vds.getVdsSpmId(), getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        removeIrsProxy();
    }
}
