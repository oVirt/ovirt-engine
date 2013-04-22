package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import java.util.HashMap;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class SetVmTicketVDSCommand<P extends SetVmTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = new Guid();
    private String mTicket;
    private int mValidTime; // in seconds
    private String connectionAction = "disconnect";

    public SetVmTicketVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mTicket = parameters.getTicket();
        mValidTime = parameters.getValidTime();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
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

        ProceedProxyReturnValue();
    }
}
