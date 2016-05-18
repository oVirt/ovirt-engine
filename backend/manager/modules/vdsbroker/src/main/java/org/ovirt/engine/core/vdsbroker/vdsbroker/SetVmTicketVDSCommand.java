package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class SetVmTicketVDSCommand<P extends SetVmTicketVDSCommandParameters> extends VdsBrokerCommand<P> {

    private String connectionAction = "disconnect";

    public SetVmTicketVDSCommand(P parameters) {
        super(parameters, DbFacade.getInstance().getVdsDao().get(parameters.getVdsId()));
    }

    @Override
    protected void executeVdsBrokerCommand() {
        setTicketUsingUpdateDevice();
        proceedProxyReturnValue();
    }

    /**
     * Sets console ticket using updateDevice command. This is used in VDSMs that support graphics framebuffer
     * as a device.
     */
    private void setTicketUsingUpdateDevice() {
        Map<String, Object> devStruct = new HashMap<>();

        devStruct.put("deviceType", "graphics");
        devStruct.put("graphicsType", getParameters().getGraphicsType().name().toLowerCase());
        devStruct.put("password", getParameters().getTicket());
        devStruct.put("ttl", getParameters().getValidTime());
        if (getParameters().getCompatibilityVersion().less(Version.v4_0)) {
            // Older Vdsm versions crash when this parameter is not present.
            devStruct.put("existingConnAction", connectionAction);
        }
        devStruct.put("disconnectAction", getParameters().getDisconnectAction());
        devStruct.put("params", getUidParams());

        status = getBroker().vmUpdateDevice(getParameters().getVmId().toString(), devStruct);
    }

    private Map<String, String> getUidParams() {
        Map<String, String> params = new HashMap<>();
        params.put("userName", getParameters().getUserName());
        params.put("userId", getParameters().getUserId().toString());
        return params;
    }

}
