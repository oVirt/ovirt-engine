package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.HostedEngineEnv;

public class VdsDeployHostedEngineUnit implements VdsDeployUnit {

    private VdsDeployBase _deploy;
    private Map<String, String> _hostedEngineConfiguration;

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                HostedEngineEnv.ACTION,
                // get rid of the action value. The rest of the map is a pure he configuration.
                _hostedEngineConfiguration.remove(HostedEngineEnv.ACTION)
            );
            return true;
        }},
        new Callable<Boolean>() {
        public Boolean call() throws Exception {
            if (_hostedEngineConfiguration.isEmpty()) {
                _deploy.getParser().cliNoop();
                return true;
            }
            Map.Entry<String, String> entry = _hostedEngineConfiguration.entrySet().iterator().next();
            _hostedEngineConfiguration.remove(entry.getKey());
            _deploy.getParser().cliEnvironmentSet(
                HostedEngineEnv.HOSTED_ENGINE_CONFIG_PREFIX + entry.getKey(),
                entry.getValue()
            );
            return false;
        }}
    );

    public VdsDeployHostedEngineUnit(Map<String, String> hostedEngineConfiguration) {
        _hostedEngineConfiguration = hostedEngineConfiguration;
    }

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
