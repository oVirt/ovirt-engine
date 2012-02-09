package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.AddVdsVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ThreadUtils;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddVdsVDSCommand<P extends AddVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public AddVdsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        VDS vds = null;
        log.infoFormat("AddVds - entered , starting logic to add VDS {0}", getVdsId());
        do {
            vds = TransactionSupport.executeInNewTransaction(new TransactionMethod<VDS>() {
                @Override
                public VDS runInTransaction() {
                    return DbFacade.getInstance().getVdsDAO().get(getVdsId());
                }
            });

            if (vds == null) {
                log.infoFormat(
                        "AddVds - failed to get VDS by Id {0}, it was not yet added, going to sleep for 1.5 sec",
                        getVdsId());
                ThreadUtils.sleep(1500);
            }
        } while (vds == null);
        log.infoFormat("AddVds - VDS {0} was added, will try to add it to the resource manager",
                getVdsId());
        ResourceManager.getInstance().AddVds(vds, false);
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddVdsVDSCommand.class);
}
