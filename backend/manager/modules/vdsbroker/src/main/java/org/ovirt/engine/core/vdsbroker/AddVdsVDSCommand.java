package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.AddVdsVDSCommandParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class AddVdsVDSCommand<P extends AddVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public AddVdsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsIdCommand() {
        log.infoFormat("AddVds - entered , starting logic to add VDS {0}", getVdsId());
        VDS vds = DbFacade.getInstance().getVdsDao().get(getVdsId());
        log.infoFormat("AddVds - VDS {0} was added, will try to add it to the resource manager",
                getVdsId());
        ResourceManager.getInstance().AddVds(vds, false);
    }

    private static Log log = LogFactory.getLog(AddVdsVDSCommand.class);
}
