package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.otopi.constants.NetEnv;
import org.ovirt.otopi.dialog.Event;

/**
 * This class serve as a unit for set up iptables on host using otopi host-deploy process.
 * Using @param deployIpTables user can configure if he really need to setup iptables
 * by otopi host-deploy proccess or if he setup it in any different way(ie. firewalld using Ansible).
 *
 * Setting @param deployIpTables to false means skip the configuration of iptables.
 */
public class VdsDeployIptablesUnit implements VdsDeployUnit {

    private static final String IPTABLES_CUSTOM_RULES_PLACE_HOLDER = "@CUSTOM_RULES@";
    private static final String IPTABLES_VDSM_PORT_PLACE_HOLDER = "@VDSM_PORT@";
    private static final String IPTABLES_SSH_PORT_PLACE_HOLDER = "@SSH_PORT@";

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.IPTABLES_ENABLE,
                deployIpTables
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.IPTABLES_RULES,
                getIpTables().split("\n")
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;

    private boolean deployIpTables;

    public VdsDeployIptablesUnit(boolean deployIpTables) {
        this.deployIpTables = deployIpTables;
    }

    private String getIpTables() {
        Cluster cluster = Injector.get(ClusterDao.class).get(
            _deploy.getVds().getClusterId()
        );

        String ipTablesConfig = Config.getValue(ConfigValues.IPTablesConfig);

        String serviceIPTablesConfig = "";
        if (cluster.supportsVirtService()) {
            serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigForVirt);
        }
        if (cluster.supportsGlusterService()) {
            serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigForGluster);
        }
        serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigSiteCustom);

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
