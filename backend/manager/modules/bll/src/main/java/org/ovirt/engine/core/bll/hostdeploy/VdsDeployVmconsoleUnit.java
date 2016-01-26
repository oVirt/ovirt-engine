package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.utils.hostinstall.OpenSslCAWrapper;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.Displays;
import org.ovirt.ovirt_host_deploy.constants.VMConsoleEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsDeployVmconsoleUnit implements VdsDeployUnit {

    private static final Logger log = LoggerFactory.getLogger(VdsDeployVmconsoleUnit.class);

    private static final String COND_VMCONSOLE_DEPLOY = "VMCONSOLE_DEPLOY";
    private static final String COND_VMCONSOLE_PKI = "VMCONSOLE_PKI";

    private final List<Callable<Boolean>> CUSTOMIZATION_DIALOG = Arrays.asList(
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_VMCONSOLE_DEPLOY)
        public Boolean call() throws Exception {
            Integer support = (Integer)_deploy.getParser().cliEnvironmentGet(
                VMConsoleEnv.SUPPORT
            );
            if (support == null || support != Const.VMCONSOLE_SUPPORT_V1) {
                _deploy.removeCustomizationCondition(COND_VMCONSOLE_DEPLOY);
                _deploy.removeCustomizationCondition(COND_VMCONSOLE_PKI);
            }
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_VMCONSOLE_DEPLOY)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VMConsoleEnv.ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_VMCONSOLE_PKI)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VMConsoleEnv.CERTIFICATE_ENROLLMENT,
                Const.CERTIFICATE_ENROLLMENT_INLINE
            );
            return true;
        }},
        new Callable<Boolean>() {@VdsDeployUnit.CallWhen(COND_VMCONSOLE_PKI)
        public Boolean call() throws Exception {
            _deploy.getParser().cliEnvironmentSet(
                VMConsoleEnv.CAKEY,
                PKIResources.getCaCertificate().toString(
                    PKIResources.Format.OPENSSH_PUBKEY
                ).replace("\n", "")
            );
            return true;
        }}
    );

    private VdsDeployBase _deploy;
    private boolean _pkionly;
    private String _sercon_certificate;

    public VdsDeployVmconsoleUnit(boolean pkionly) {
        _pkionly = pkionly;
    }

    public VdsDeployVmconsoleUnit() {
        this(false);
    }

    // VdsDeployUnit interface

    @Override
    public void setVdsDeploy(VdsDeployBase deploy) {
        _deploy = deploy;
    }

    @Override
    public void init() {
        _deploy.addCustomizationDialog(CUSTOMIZATION_DIALOG);
        _deploy.addCustomizationCondition(COND_VMCONSOLE_PKI);
        if (!_pkionly) {
            _deploy.addCustomizationCondition(COND_VMCONSOLE_DEPLOY);
        }
    }

    @Override
    public boolean processEvent(Event.Base bevent) throws IOException {
        boolean unknown = true;

        if (bevent instanceof Event.QueryValue) {
            Event.QueryValue event = (Event.QueryValue)bevent;

            if (org.ovirt.ovirt_host_deploy.constants.Queries.VMCONSOLE_CERTIFICATE.equals(event.name)) {
                event.value = _sercon_certificate.replace("\n", "");
                unknown = false;
            }
        }
        else if (bevent instanceof Event.DisplayMultiString) {
            Event.DisplayMultiString event = (Event.DisplayMultiString)bevent;

            if (Displays.VMCONSOLE_CERTIFICATE_REQUEST.equals(event.name)) {
                _deploy.userVisibleLog(
                    Level.INFO,
                    "Enrolling serial console certificate"
                );
                String name = String.format("%s-ssh", _deploy.getVds().getHostName());
                OpenSslCAWrapper.signCertificateRequest(
                    StringUtils.join(event.value, "\n"),
                    name,
                    _deploy.getVds().getHostName()
                );
                _sercon_certificate = OpenSslCAWrapper.signOpenSSHCertificate(
                    name,
                    _deploy.getVds().getHostName(),
                    _deploy.getVds().getHostName()
                );
                unknown = false;
            }
        }

        return unknown;
    }

}
