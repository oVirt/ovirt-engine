package org.ovirt.engine.core.bll.hostdeploy;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.CertificateUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.PKIResources;

@NonTransactiveCommandAttribute
public class HostEnrollCertificateInternalCommand extends VdsCommand<VdsActionParameters> {

    @Inject
    private AnsibleExecutor ansibleExecutor;

    private EngineLocalConfig config = EngineLocalConfig.getInstance();


    public HostEnrollCertificateInternalCommand(VdsActionParameters parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    public boolean validate() {
        HostValidator hostValidator = HostValidator.createInstance(getVds());
        return validate(hostValidator.hostExists())
                && validate(hostValidator.validateStatusForEnrollCertificate());
    }

    @Override
    protected void executeCommand() {
        setVdsStatus(VDSStatus.Installing);
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("ovirt_pki_dir", config.getPKIDir())
                .variable("ovirt_vds_hostname", getVds().getHostName())
                .variable("ovirt_san", CertificateUtils.getSan(getVds().getHostName()))
                .variable("ovirt_engine_usr", config.getUsrDir())
                .variable("ovirt_organizationname", Config.getValue(ConfigValues.OrganizationName))
                .variable("ovirt_vds_certificate_validity_in_days", Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInDays))
                .variable("ovirt_signcerttimeoutinseconds",
                        Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds).toString())
                .variable("ovirt_ca_cert", PKIResources.getCaCertificate().toString(PKIResources.Format.X509_PEM))
                .variable("ovirt_ca_key",
                        PKIResources.getCaCertificate()
                                .toString(PKIResources.Format.OPENSSH_PUBKEY)
                                .replace("\n", ""))
                .variable("ovirt_qemu_ca_cert", PKIResources.getQemuCaCertificate().toString(PKIResources.Format.X509_PEM))
                .variable("ovirt_qemu_ca_key",
                        PKIResources.getQemuCaCertificate()
                                .toString(PKIResources.Format.OPENSSH_PUBKEY)
                                .replace("\n", ""))
                // /var/log/ovirt-engine/host-deploy/ovirt-enroll-certs-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                .logFilePrefix("ovirt-enroll-certs-ansible")
                .logFileName(getVds().getHostName())
                .playbook(AnsibleConstants.HOST_ENROLL_CERTIFICATE);
        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(commandConfig);
        setSucceeded(ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK);
        if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            log.error(
                    "Failed to enroll certificate for host '{}': please check log for more details: {}",
                    getVds().getName(),
                    ansibleReturnValue.getLogFile());
            setVdsStatus(VDSStatus.InstallFailed);
        } else {
            setVdsStatus(VDSStatus.Maintenance);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_CERTIFICATION_ENROLLMENT_FINISHED
                : AuditLogType.HOST_CERTIFICATION_ENROLLMENT_FAILED;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ENROLL_CERTIFICATE);
        addValidationMessage(EngineMessage.VAR__TYPE__HOST);
    }

}
