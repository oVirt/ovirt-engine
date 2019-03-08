package org.ovirt.engine.core.bll.host;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.hostdeploy.VdsDeployBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleVerbosity;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.PKIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HostUpgradeManager implements UpdateAvailable, Updateable {

    public static final int MAX_NUM_OF_DISPLAYED_UPDATES = 10;

    private static Logger log = LoggerFactory.getLogger(HostUpgradeManager.class);

    private EngineLocalConfig config = EngineLocalConfig.getInstance();

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private AnsibleExecutor ansibleExecutor;

    @Inject
    private ClusterDao clusterDao;

    @Override
    public HostUpgradeManagerResult checkForUpdates(final VDS host) {
        AnsibleReturnValue ansibleReturnValue = null;
        try {
            AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(host.getHostName())
                .checkMode(true)
                .enableLogging(false)
                .verboseLevel(AnsibleVerbosity.LEVEL0)
                .stdoutCallback(AnsibleConstants.HOST_UPGRADE_CALLBACK_PLUGIN)
                .playbook(AnsibleConstants.HOST_UPGRADE_PLAYBOOK);

            ansibleReturnValue = ansibleExecutor.runCommand(command);
            if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
                String error = String.format(
                    "Failed to run check-update of host '%1$s'. Error: %2$s",
                    host.getHostName(),
                    ansibleReturnValue.getStderr()
                );
                log.error(error);
                throw new RuntimeException(error);
            }

            List<String> availablePackages = JsonHelper.jsonToList(ansibleReturnValue.getStdout());
            boolean updatesAvailable = !availablePackages.isEmpty();
            HostUpgradeManagerResult hostUpgradeManagerResult = new HostUpgradeManagerResult();
            hostUpgradeManagerResult.setUpdatesAvailable(updatesAvailable);
            if (updatesAvailable) {
                hostUpgradeManagerResult.setAvailablePackages(availablePackages);
                log.info("There are available package updates ({}) for host '{}'",
                         StringUtils.join(availablePackages, ", "),
                         host.getHostName());
                AuditLogable auditLog = new AuditLogableImpl();
                auditLog.setVdsId(host.getId());
                auditLog.setVdsName(host.getName());
                if (availablePackages.isEmpty()) {
                    auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE);
                } else {
                    if (availablePackages.size() > MAX_NUM_OF_DISPLAYED_UPDATES) {
                        auditLog.addCustomValue(
                            "Packages",
                            String.format(
                                "%1$s and %2$s others. To see all packages check engine.log.",
                                StringUtils.join(availablePackages.subList(0, MAX_NUM_OF_DISPLAYED_UPDATES), ", "),
                                availablePackages.size() - MAX_NUM_OF_DISPLAYED_UPDATES
                            )
                        );
                    } else {
                        auditLog.addCustomValue("Packages", StringUtils.join(availablePackages, ", "));
                    }
                    auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE_WITH_PACKAGES);
                }
            }
            return hostUpgradeManagerResult;
        } catch (final IOException e) {
            log.error("Failed to read host packages: {}", e.getMessage());
            if (ansibleReturnValue != null) {
                log.debug("Ansible packages output: {}", ansibleReturnValue.getStdout());
            }
            throw new RuntimeException(e.getMessage(), e);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public EnumSet<VDSType> getHostTypes() {
        return EnumSet.of(VDSType.VDS, VDSType.oVirtNode);
    }

    @Override
    public void update(final VDS host) {
        Cluster cluster = clusterDao.get(host.getClusterId());
        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
            .hostnames(host.getHostName())
            // /var/log/ovirt-engine/host-deploy/ovirt-host-mgmt-ansible-{hostname}-{correlationid}-{timestamp}.log
            .logFileDirectory(VdsDeployBase.HOST_DEPLOY_LOG_DIRECTORY)
            .logFilePrefix("ovirt-host-mgmt-ansible")
            .logFileName(host.getHostName())
            .logFileSuffix(CorrelationIdTracker.getCorrelationId())
            .variables(
                new Pair<>("host_deploy_vnc_restart_services", host.getVdsType() == VDSType.VDS),
                new Pair<>("host_deploy_vnc_tls", String.valueOf(cluster.isVncEncryptionEnabled())),
                // PKI variables:
                new Pair<>("ovirt_pki_dir", config.getPKIDir()),
                new Pair<>("ovirt_vds_hostname", host.getHostName()),
                new Pair<>("ovirt_engine_usr", config.getUsrDir()),
                new Pair<>("ovirt_organizationname", Config.getValue(ConfigValues.OrganizationName)),
                new Pair<>("ovirt_vdscertificatevalidityinyears", Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInYears)),
                new Pair<>("ovirt_signcerttimeoutinseconds", Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds)),
                new Pair<>("ovirt_ca_cert", PKIResources.getCaCertificate().toString(PKIResources.Format.X509_PEM)),
                new Pair<>("ovirt_ca_key",  PKIResources.getCaCertificate().toString(
                    PKIResources.Format.OPENSSH_PUBKEY
                ).replace("\n", ""))
            )
            .playbook(AnsibleConstants.HOST_UPGRADE_PLAYBOOK);
        if (ansibleExecutor.runCommand(command).getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            String error = String.format("Failed to update host '%1$s'.", host.getHostName());
            log.error(error);
            throw new RuntimeException(error);
        }
    }
}
