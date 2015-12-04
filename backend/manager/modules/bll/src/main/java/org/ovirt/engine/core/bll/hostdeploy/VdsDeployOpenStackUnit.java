package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.MessagingConfiguration;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.OpenStackEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployOpenStackUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployOpenStackUnit.class);

    private static final String COND_NEUTRON_LINUX_BRIDGE_SETUP = "NEUTRON_LINUX_BRIDGE_SETUP";
    private static final String COND_NEUTRON_OPEN_VSWITCH_SETUP = "NEUTRON_OPEN_VSWITCH_SETUP";

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                OpenStackEnv.NEUTRON_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                    OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/host",
                    NetworkUtils.getUniqueHostName(_deploy.getVds())
                );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getHostKey(),
                _messagingConfiguration.getAddress()
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getPortKey(),
                _messagingConfiguration.getPort()
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                                    _messagingConfiguration.getBrokerType().getUsernameKey(),
                _messagingConfiguration.getUsername()
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                            _messagingConfiguration.getBrokerType().getPasswordKey(),
            _messagingConfiguration.getPassword()
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/rpc_backend",
                _messagingConfiguration.getBrokerType().getRpcBackendValue()
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_CONFIG_PREFIX + "LINUX_BRIDGE/physical_interface_mappings",
                _openStackAgentProperties.getAgentConfiguration().getNetworkMappings()
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_NEUTRON_OPEN_VSWITCH_SETUP)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                OpenStackEnv.NEUTRON_OPENVSWITCH_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_NEUTRON_OPEN_VSWITCH_SETUP)
        public Boolean call() throws Exception {
            setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_OPENVSWITCH_CONFIG_PREFIX + "OVS/bridge_mappings",
                _openStackAgentProperties.getAgentConfiguration().getNetworkMappings()
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;
    private OpenstackNetworkProviderProperties _openStackAgentProperties = null;
    private MessagingConfiguration _messagingConfiguration = null;

    private void setCliEnvironmentIfNecessary(String name, Object value) throws IOException {
        if (value == null) {
            _deploy.getParser().cliNoop();
        }
        else {
            _deploy.getParser().cliEnvironmentSet(name, value);
        }
    }

    public VdsDeployOpenStackUnit(OpenstackNetworkProviderProperties properties) {
        _openStackAgentProperties = properties;
    }

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
        _messagingConfiguration = _openStackAgentProperties.getAgentConfiguration().getMessagingConfiguration();
        if (_openStackAgentProperties.isLinuxBridge()) {
            _deploy.addCustomizationCondition(COND_NEUTRON_LINUX_BRIDGE_SETUP);
        }
        else if (_openStackAgentProperties.isOpenVSwitch()) {
            _deploy.addCustomizationCondition(COND_NEUTRON_OPEN_VSWITCH_SETUP);
        }
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }

}
