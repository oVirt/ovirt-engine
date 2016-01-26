package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.utils.hostinstall.OpenSslCAWrapper;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.Displays;
import org.ovirt.ovirt_host_deploy.constants.VdsmEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployPKIUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployPKIUnit.class);

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = new ArrayList() {{ add(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VdsmEnv.CERTIFICATE_ENROLLMENT,
                Const.CERTIFICATE_ENROLLMENT_INLINE
            );
            return true;
        }}
    );}};

    private VdsDeployBase _deploy;
    private String _certificate;

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
        boolean unknown = true;

        if (bevent instanceof Event.QueryMultiString) {
            Event.QueryMultiString event = (Event.QueryMultiString)bevent;

            if (org.ovirt.ovirt_host_deploy.constants.Queries.CERTIFICATE_CHAIN.equals(event.name)) {
                event.value = (
                    PKIResources.getCaCertificate().toString(PKIResources.Format.X509_PEM) +
                    _certificate
                ).split("\n");
                unknown = false;
            }
        }
        else if (bevent instanceof Event.DisplayMultiString) {
            Event.DisplayMultiString event = (Event.DisplayMultiString)bevent;

            if (Displays.CERTIFICATE_REQUEST.equals(event.name)) {
                _deploy.userVisibleLog(
                    Level.INFO,
                    "Enrolling certificate"
                );
                _certificate = OpenSslCAWrapper.signCertificateRequest(
                    StringUtils.join(event.value, "\n"),
                    _deploy.getVds().getHostName(),
                    _deploy.getVds().getHostName()
                );
                unknown = false;
            }
        }

        return unknown;
    }

}
