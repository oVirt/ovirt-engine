package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.otopi.constants.NetEnv;
import org.ovirt.otopi.dialog.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployIptablesUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployIptablesUnit.class);

    private static final String IPTABLES_CUSTOM_RULES_PLACE_HOLDER = "@CUSTOM_RULES@";
    private static final String IPTABLES_VDSM_PORT_PLACE_HOLDER = "@VDSM_PORT@";
    private static final String IPTABLES_SSH_PORT_PLACE_HOLDER = "@SSH_PORT@";

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.IPTABLES_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.IPTABLES_RULES,
                _getIpTables().split("\n")
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;

    private String _getIpTables() {
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
            _deploy.getVds().getVdsGroupId()
        );

        String ipTablesConfig = Config.<String> getValue(ConfigValues.IPTablesConfig);

        String serviceIPTablesConfig = "";
        if (vdsGroup.supportsVirtService()) {
            serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigForVirt);
        }
        if (vdsGroup.supportsGlusterService()) {
            serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigForGluster);
        }
        serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigSiteCustom);

        ipTablesConfig = ipTablesConfig.replace(
            IPTABLES_CUSTOM_RULES_PLACE_HOLDER,
            serviceIPTablesConfig
        ).replace(
            IPTABLES_SSH_PORT_PLACE_HOLDER,
            Integer.toString(_deploy.getVds().getSshPort())
        ).replace(
            IPTABLES_VDSM_PORT_PLACE_HOLDER,
            Integer.toString(_deploy.getVds().getPort())
        );

        return ipTablesConfig;
    }

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }

}
