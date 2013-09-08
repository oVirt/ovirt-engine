package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class SetVmTicketVDSCommand<P extends SetVmTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = Guid.Empty;
    private String mTicket;
    private int mValidTime; // in seconds
    private String connectionAction = "disconnect";

    public SetVmTicketVDSCommand(P parameters) {
        super(parameters, DbFacade.getInstance().getVdsDao().get(parameters.getVdsId()));
        mVmId = parameters.getVmId();
        mTicket = parameters.getTicket();
        mValidTime = parameters.getValidTime();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (Config.<Boolean> GetValue(ConfigValues.SendVmTicketUID,
                    getVds().getVdsGroupCompatibilityVersion().toString())) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("userName", getParameters().getUserName());
            params.put("userId", getParameters().getUserId().toString());
            status = getBroker().setVmTicket(mVmId.toString(), mTicket, String.valueOf(mValidTime),
                    connectionAction, params);
        }
        else {
            status = getBroker().setVmTicket(mVmId.toString(), mTicket, String.valueOf(mValidTime));
        }

        proceedProxyReturnValue();
    }
}
