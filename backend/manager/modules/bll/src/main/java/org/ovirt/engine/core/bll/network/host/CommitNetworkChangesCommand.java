package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@SuppressWarnings("serial")
public class CommitNetworkChangesCommand<T extends VdsActionParameters> extends VdsCommand<T> {
    public CommitNetworkChangesCommand(T param) {
        super(param);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue retVal = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetSafeNetworkConfig,
                        new VdsIdVDSCommandParametersBase(getParameters().getVdsId()));

        getVds().setnet_config_dirty(false);
        DbFacade.getInstance().getVdsDynamicDao().update(getVds().getDynamicData());
        setSucceeded(retVal.getSucceeded());
    }

    @Override
    protected boolean canDoAction() {
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_COMMINT_NETWORK_CHANGES
                : AuditLogType.NETWORK_COMMINT_NETWORK_CHANGES_FAILED;
    }
}
