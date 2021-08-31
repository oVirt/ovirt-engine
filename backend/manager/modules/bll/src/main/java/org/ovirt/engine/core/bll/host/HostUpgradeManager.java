package org.ovirt.engine.core.bll.host;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CertificationValidityChecker;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.CertificateUtils;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.NetworkUtils;
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
    private AnsibleRunnerHttpClient runnerClient;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private NetworkHelper networkHelper;

    @Override
    public HostUpgradeManagerResult checkForUpdates(final VDS host) {
        AnsibleReturnValue ansibleReturnValue = null;
        try {
            AnsibleCommandConfig command = new AnsibleCommandConfig()
                    .hosts(host)

                    // /var/log/ovirt-engine/host-deploy/ovirt-host-mgmt-ansible-check-{hostname}-{correlationid}-{timestamp}.log
                    .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                    .logFilePrefix("ovirt-host-mgmt-ansible-check")
                    .logFileName(host.getHostName())
                    .playbook(AnsibleConstants.HOST_CHECK_UPGRADE_PLAYBOOK)
                    .playAction(String.format("Check for update of host %1$s", host.getName()));
            setAnsibleCommandConfigVars(command, host);

            List<String> availablePackages = new ArrayList<>();
            ansibleReturnValue = ansibleExecutor.runCommand(command,
                    log,
                    (eventName, eventUrl) -> availablePackages.addAll(runnerClient.getYumPackages(eventUrl)));

            if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
                String error = String.format(
                    "Failed to run check-update of host '%1$s'. Error: %2$s",
                    host.getHostName(),
                    ansibleReturnValue.getStderr()
                );
                log.error(error);
                throw new RuntimeException(error);
            }

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
        } catch (final Exception e) {
            log.error("Failed to read host packages: {}", e.getMessage());
            if (ansibleReturnValue != null) {
                log.debug("Ansible packages output: {}", ansibleReturnValue.getStdout());
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public EnumSet<VDSType> getHostTypes() {
        return EnumSet.of(VDSType.VDS, VDSType.oVirtNode);
    }

    @Override
    public void update(final VDS host, int timeout) {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(host)
                // /var/log/ovirt-engine/host-deploy/ovirt-host-mgmt-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                .logFilePrefix("ovirt-host-mgmt-ansible")
                .logFileName(host.getHostName())
                .playbook(AnsibleConstants.HOST_UPGRADE_PLAYBOOK)
                .playAction(String.format("Update of host %1$s", host.getName()));
        setAnsibleCommandConfigVars(commandConfig, host);

        if (ansibleExecutor.runCommand(commandConfig, timeout).getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            String error = String.format("Failed to update host '%1$s'.", host.getHostName());
            log.error(error);
            throw new RuntimeException(error);
        }
    }

    private void setAnsibleCommandConfigVars(AnsibleCommandConfig command, final VDS host) {
        //The number of days allowed before certificate expiration.
        //(less time left than this requires enrolling for new certificate).
        Integer daysAllowedUntilExpiration = Config.<Integer> getValue(ConfigValues.CertExpirationAlertPeriodInDays);
        //The date in the future (in seconds), against which to validate the certificates.
        //For example if we allow certificates which expire in 7 days or more, this
        //variable will contain a seconds-since-1/1/1970 representation 7 days from the current moment.
        long allowedExpirationDateInSeconds = TimeUnit.MILLISECONDS.toSeconds(
                CertificationValidityChecker.computeFutureExpirationDate(daysAllowedUntilExpiration).getTimeInMillis());
        Cluster cluster = clusterDao.get(host.getClusterId());
        String clusterVersion = cluster.getCompatibilityVersion().getValue();
        command
            .variable("host_deploy_vnc_restart_services", host.getVdsType() == VDSType.VDS)
            .variable("host_deploy_vnc_tls", String.valueOf(cluster.isVncEncryptionEnabled()))
            .variable("host_deploy_cluster_version", clusterVersion)
            // PKI variables:
            .variable("ovirt_pki_dir", config.getPKIDir())
            .variable("ovirt_vds_hostname", host.getHostName())
            .variable("ovirt_san", CertificateUtils.getSan(host.getHostName()))
            .variable("ovirt_engine_usr", config.getUsrDir())
            .variable("ovirt_organizationname", Config.getValue(ConfigValues.OrganizationName))
            .variable("ovirt_vds_certificate_validity_in_days", Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInDays))
            .variable("ovirt_signcerttimeoutinseconds",
                    Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds))
            .variable("ovirt_time_to_check", allowedExpirationDateInSeconds)
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
            .variable("host_deploy_origin_type", Config.getValue(ConfigValues.OriginType))
            .variable("host_deploy_ovn_central", getOvnCentral(cluster))
            .variable("host_deploy_ovn_tunneling_interface", NetworkUtils.getHostIp(host));
    }

    private String getOvnCentral(Cluster cluster) {
        var provider = networkHelper.getOvirtProviderOvn(cluster.getDefaultNetworkProviderId());
        if (provider == null) {
            return null;
        }
        String ovnCentral = NetworkUtils.getIpAddress(provider.getUrl());
        if (ovnCentral == null) {
            throw new RuntimeException(String.format("Failed to extract OVN central IP from %1$s", provider.getUrl()));
        }
        return ovnCentral;
    }
}
