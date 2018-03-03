package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class DestroyStoragePoolVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    @Inject
    private VdsDao vdsDao;

    public DestroyStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        VDS vds = vdsDao.get(this.getCurrentIrsProxy().getCurrentVdsId());
        status = getIrsProxy().destroyStoragePool(getParameters().getStoragePoolId().toString(),
                vds.getVdsSpmId(), getParameters().getStoragePoolId().toString());
        proceedProxyReturnValue();
        removeIrsProxy();
    }

    private void removeIrsProxy() {
        getIrsProxyManager().removeProxy(getParameters().getStoragePoolId());
    }

}
