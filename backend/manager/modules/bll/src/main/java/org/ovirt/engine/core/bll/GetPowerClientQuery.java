package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetPowerClientByClientInfoParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class GetPowerClientQuery<P extends GetPowerClientByClientInfoParameters> extends QueriesCommandBase<P> {
    public GetPowerClientQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(GetPowerClient(getParameters().getClientIp()));
    }

    private VDS GetPowerClient(String client_ip) {
        VDS powerClient = null;
        if (StringUtils.isNotEmpty(client_ip)) {
            boolean powerClientLogDetection =
                    Config.<Boolean> GetValue(ConfigValues.PowerClientLogDetection);
            if (powerClientLogDetection) {
                log.infoFormat("Checking if client is a power client. client IP={0}", client_ip);
            }

            List<VDS> targetVDS = DbFacade.getInstance().getVdsDao().getAllForHostname(client_ip);
            // DbFacade.Instance.GetVdsByHost(client_ip);
            if (targetVDS.size() == 1 && targetVDS.get(0).getvds_type() == VDSType.PowerClient) {
                if (powerClientLogDetection) {
                    log.infoFormat("Client is a power client. client IP={0}", client_ip);
                }
                powerClient = targetVDS.get(0); // DbFacade.Instance.GetVdsByVdsId(targetVDS.vds_id);
            }
        }
        return powerClient;
    }

    private static Log log = LogFactory.getLog(GetPowerClientQuery.class);
}
