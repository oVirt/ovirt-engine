package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_mgmt.constants.Const;
import org.ovirt.ovirt_host_mgmt.constants.PackagesEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployPackagesUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployPackagesUnit.class);

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                PackagesEnv.UPDATE_MODE,
                _checkOnly ? Const.PACKAGES_UPDATE_MODE_CHECK_UPDATE : Const.PACKAGES_UPDATE_MODE_UPDATE
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                PackagesEnv.PACKAGES,
                _packages
            );
            return true;
        }}
    );

    // BUG: Arrays.asList() cannot handle single element correctly
    private final List<Callable<Boolean>> TERMINATION_DIALOG = new ArrayList<Callable<Boolean>>() {{ add(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _packagesInfo = (String[])_deploy.getParser().cliEnvironmentGet(
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

    private VdsDeployBase _deploy;
    private boolean _checkOnly;
    private String[] _packages;
    private String[] _packagesInfo;

    public VdsDeployPackagesUnit(Collection<String> packages, boolean checkOnly) {
        _packages = packages.toArray(new String[0]);
        _checkOnly = checkOnly;
    }

    public Collection<String> getUpdates() {
        return Arrays.asList(_packagesInfo);
    }

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
        _deploy.addTerminationDialog(TERMINATION_DIALOG);
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        return true;
    }

}
