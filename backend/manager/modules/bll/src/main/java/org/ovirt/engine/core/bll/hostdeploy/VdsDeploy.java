package org.ovirt.engine.core.bll.hostdeploy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.otopi.constants.Confirms;
import org.ovirt.otopi.dialog.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeploy extends VdsDeployBase {

    private static final Logger log = LoggerFactory.getLogger(VdsDeploy.class);

    private static final Map<Level, AuditLogType> _levelToType = new HashMap<Level, AuditLogType>() {{
        put(Level.INFO, AuditLogType.VDS_INSTALL_IN_PROGRESS);
        put(Level.WARNING, AuditLogType.VDS_INSTALL_IN_PROGRESS_WARNING);
        put(Level.SEVERE, AuditLogType.VDS_INSTALL_IN_PROGRESS_ERROR);
    }};

    boolean _alertLog;

    public VdsDeploy(String entry, VDS vds, boolean alertLog) {
        super(entry, entry, vds);
        _alertLog = alertLog;
    }

    @Override
    protected boolean processEvent(Event.Base bevent) throws Exception {
        boolean unknown = super.processEvent(bevent);

        if (unknown) {
            if (bevent instanceof Event.Confirm) {
                Event.Confirm event = (Event.Confirm)bevent;

                if (Confirms.GPG_KEY.equals(event.what)) {
                    userVisibleLog(
                        Level.WARNING,
                        event.description
                    );
                    event.reply = true;
                    unknown = false;
                }
                else if (org.ovirt.ovirt_host_deploy.constants.Confirms.DEPLOY_PROCEED.equals(event.what)) {
                    event.reply = true;
                    unknown = false;
                }
            }
        }

        return unknown;
    }

    @Override
    public void userVisibleLog(Level level, String message) {
        if (!_alertLog) {
            super.userVisibleLog(level, message);
        } else {
            AuditLogType type = _levelToType.get(level);
            if (type == null) {
                log.debug(message);
            } else {
                AuditLogableBase logable = new AuditLogableBase(getVds().getId());
                logable.setCorrelationId(getCorrelationId());
                logable.addCustomValue("Message", message);
                new AuditLogDirector().log(logable, _levelToType.get(level));
            }
        }
    }

}
