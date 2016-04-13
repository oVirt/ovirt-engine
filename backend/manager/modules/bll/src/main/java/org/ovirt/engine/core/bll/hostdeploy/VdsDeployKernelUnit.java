package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.KernelEnv;

public class VdsDeployKernelUnit implements VdsDeployUnit {

    private VdsDeployBase deploy;

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        this.deploy = deploy;
    }

    @Override
    public void init() {
        final List<Callable<Boolean>> customizationDialog = Arrays.asList(
                () -> {
                    deploy.getParser().cliEnvironmentSet(
                            KernelEnv.CMDLINE_NEW,
                            deploy.getVds().getCurrentKernelCmdline()
                    );
                    return true;
                },
                () -> {
                    deploy.getParser().cliEnvironmentSet(
                            KernelEnv.CMDLINE_OLD,
                            deploy.getVds().getLastStoredKernelCmdline()
                    );
                    return true;
                }
        );
        deploy.addCustomizationDialog(customizationDialog);
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }
}
