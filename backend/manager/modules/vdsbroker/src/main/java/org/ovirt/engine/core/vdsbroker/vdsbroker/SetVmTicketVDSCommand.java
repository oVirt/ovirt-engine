package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.vdscommands.SetVmTicketVDSCommandParameters;
import org.ovirt.engine.core.dao.VdsDao;

public class SetVmTicketVDSCommand<P extends SetVmTicketVDSCommandParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VdsDao vdsDao;

    public SetVmTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @PostConstruct
    public void init() {
        setVdsAndVdsStatic(vdsDao.get(getParameters().getVdsId()));
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
        devStruct.put("disconnectAction", getParameters().getDisconnectAction());
        devStruct.put("consoleDisconnectActionDelay", getParameters().getConsoleDisconnectActionDelay());
        devStruct.put("params", getUidParams());

        status = getBroker().vmUpdateDevice(getParameters().getVmId().toString(), devStruct);
    }

    private Map<String, String> getUidParams() {
        Map<String, String> params = new HashMap<>();
        params.put("userName", getParameters().getUserName());
        params.put("userId", getParameters().getUserId().toString());
        boolean fips = getVds().isFipsEnabled();
        params.put("fips", Boolean.toString(fips));
        params.put("vncUsername", ConfigureConsoleOptionsParams.VNC_USERNAME_PREFIX + getParameters().getVmId());
        return params;
    }

}
