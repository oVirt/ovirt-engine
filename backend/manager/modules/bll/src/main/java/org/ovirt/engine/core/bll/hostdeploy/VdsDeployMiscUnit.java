package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.otopi.constants.NetEnv;
import org.ovirt.otopi.constants.SysEnv;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.GlusterEnv;
import org.ovirt.ovirt_host_deploy.constants.TuneEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployMiscUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployMiscUnit.class);

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                SysEnv.CLOCK_SET,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.SSH_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.SSH_USER,
                _deploy.getVds().getSshUsername()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                NetEnv.SSH_KEY,
                EngineEncryptionUtils.getEngineSSHPublicKey().replace("\n", "")
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            Cluster cluster = DbFacade.getInstance().getClusterDao().get(
                _deploy.getVds().getClusterId()
            );
            String tunedProfile = cluster.supportsGlusterService() ? cluster.getGlusterTunedProfile() : null;
            if (tunedProfile == null || tunedProfile.isEmpty()) {
                _deploy.getParser().cliNoop();
            } else {
                _deploy.getParser().cliEnvironmentSet(TuneEnv.TUNED_PROFILE, tunedProfile);
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            Cluster cluster = DbFacade.getInstance().getClusterDao().get(
                _deploy.getVds().getClusterId()
            );
            _deploy.getParser().cliEnvironmentSet(
                GlusterEnv.ENABLE,
                cluster.supportsGlusterService()
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
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }
}
