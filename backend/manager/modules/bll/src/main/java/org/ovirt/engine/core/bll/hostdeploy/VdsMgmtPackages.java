package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.otopi.constants.Confirms;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_mgmt.constants.Const;
import org.ovirt.ovirt_host_mgmt.constants.PackagesEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsMgmtPackages extends VdsDeployBase {

    private static final Logger log = LoggerFactory.getLogger(VdsMgmtPackages.class);

    private boolean _checkOnly;
    private String[] _packages;
    private String[] _packagesInfo;

    private final List<Callable<Boolean>> _deployCustomizationDialog = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                PackagesEnv.UPDATE_MODE,
                _checkOnly ? Const.PACKAGES_UPDATE_MODE_CHECK_UPDATE : Const.PACKAGES_UPDATE_MODE_UPDATE
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                PackagesEnv.PACKAGES,
                _packages
            );
            return true;
        }}
    );

    // BUG: Arrays.asList() cannot handle single element correctly
    private final List<Callable<Boolean>> _deployTerminationDialog = new ArrayList<Callable<Boolean>>() {{ add(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _packagesInfo = (String[])_parser.cliEnvironmentGet(
                PackagesEnv.PACKAGES_INFO
            );
            if (_packagesInfo.length > 0) {
                log.info(
                    String.format("Available packages for update: %s", StringUtils.join(_packagesInfo, ","))
                );
            }
            return true;
        }}
    );}};

    protected boolean processEvent(Event.Base bevent) throws IOException {
        boolean unknown = true;

        if (bevent instanceof Event.Confirm) {
            Event.Confirm event = (Event.Confirm)bevent;

            if (Confirms.GPG_KEY.equals(event.what)) {
                log.warn(event.description);
                event.reply = true;
                unknown = false;
            }
        }

        return unknown;
    }

    public VdsMgmtPackages(VDS vds, boolean checkOnly) {
        super("host-mgmt", "ovirt-host-mgmt", vds);

        _checkOnly = checkOnly;

        addCustomizationDialog(_deployCustomizationDialog);
        addCustomizationDialog(CUSTOMIZATION_DIALOG_EPILOG);
        addTerminationDialog(TERMINATION_DIALOG_PROLOG);
        addTerminationDialog(_deployTerminationDialog);
        addTerminationDialog(TERMINATION_DIALOG_EPILOG);
    }

    public void setPackages(Collection<String> packages) {
        _packages = packages.toArray(new String[0]);
    }

    public Collection<String> getUpdates() {
        return Arrays.asList(_packagesInfo);
    }

    private static final Map<Level, AuditLogType> _levelToType = new HashMap<Level, AuditLogType>() {{
        put(Level.INFO, AuditLogType.VDS_PACKAGES_IN_PROGRESS);
        put(Level.WARNING, AuditLogType.VDS_PACKAGES_IN_PROGRESS_WARNING);
        put(Level.SEVERE, AuditLogType.VDS_PACKAGES_IN_PROGRESS_ERROR);
    }};

    @Override
    protected void userVisibleLog(Level level, String message) {
        if (_checkOnly) {
            super.userVisibleLog(level, message);
        } else {
            AuditLogType type = _levelToType.get(level);
            if (type == null) {
                log.debug(message);
            } else {
                AuditLogableBase logable = new AuditLogableBase(_vds.getId());
                logable.setCorrelationId(_correlationId);
                logable.addCustomValue("Message", message);
                new AuditLogDirector().log(logable, _levelToType.get(level));
            }
        }
    }
}
