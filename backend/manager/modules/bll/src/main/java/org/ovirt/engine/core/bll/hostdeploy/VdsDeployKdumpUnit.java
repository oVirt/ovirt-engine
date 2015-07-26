package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.KdumpEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployKdumpUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployKdumpUnit.class);

    private static final String COND_KDUMP = "KDUMP";

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_KDUMP)
        public Boolean call() throws Exception {
            if (!(Boolean)_deploy.getParser().cliEnvironmentGet(KdumpEnv.SUPPORTED)) {
                _deploy.removeCustomizationCondition(COND_KDUMP);
            }
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_KDUMP)
        public Boolean call() throws Exception {
            if (
                _deploy.getVds().isPmEnabled() &&
                _deploy.getVds().isPmKdumpDetection()
            ) {
                _deploy.getParser().cliEnvironmentSet(
                    KdumpEnv.ENABLE,
                    true
                );
            } else {
                _deploy.userVisibleLog(
                    Level.INFO,
                    "Disabling Kdump integration"
                );
                _deploy.removeCustomizationCondition(COND_KDUMP);
                _deploy.getParser().cliNoop();
            }
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_KDUMP)
        public Boolean call() throws Exception {
            String destinationAddress = Config.<String>getValue(ConfigValues.FenceKdumpDestinationAddress);
            if (StringUtils.isBlank(destinationAddress)) {
                // destination address not entered, use engine FQDN
                destinationAddress = EngineLocalConfig.getInstance().getHost();
            }
            _deploy.getParser().cliEnvironmentSet(
                    KdumpEnv.DESTINATION_ADDRESS,
                    destinationAddress
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_KDUMP)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                    KdumpEnv.DESTINATION_PORT,
                    Config.<Integer>getValue(ConfigValues.FenceKdumpDestinationPort)
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_KDUMP)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                    KdumpEnv.MESSAGE_INTERVAL,
                    Config.<Integer>getValue(ConfigValues.FenceKdumpMessageInterval)
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
        _deploy.addCustomizationCondition(COND_KDUMP);
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }
}
