package org.ovirt.engine.core.bll.hostdeploy;

import static org.ovirt.engine.core.common.businessentities.ExternalNetworkPluginType.OVIRT_PROVIDER_OVN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
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
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHTTPClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class InstallVdsInternalCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Logger log = LoggerFactory.getLogger(InstallVdsInternalCommand.class);

    @Inject
    private ProviderDao providerDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private HostedEngineHelper hostedEngineHelper;

    @Inject
    private AnsibleExecutor ansibleExecutor;

    @Inject
    private AnsibleRunnerHTTPClient runnerClient;

    private EngineLocalConfig config = EngineLocalConfig.getInstance();

    public InstallVdsInternalCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        if (Guid.isNullOrEmpty(getVdsId())) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }
        if (getVds() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType result = null;
        if (getSucceeded()) {
            result = AuditLogType.VDS_INSTALL;
        } else {
            // In case of failure - add to audit log the error as achieved from
            // the host
            addCustomValue("FailedInstallMessage", getErrorMessage(_failureMessage));
            result = AuditLogType.VDS_INSTALL_FAILED;
        }
        return result;
    }

    @Override
    protected void executeCommand() {
        if (getVds() == null) {
            return;
        }

        log.info("Before Installation host {}, {}", getVds().getId(), getVds().getName());

        try {
            setVdsStatus(VDSStatus.Installing);

            if (getParameters().getAuthMethod() == AuthenticationMethod.Password) {
                copyEngineSshId();
            }

            /*
             * TODO: do we have a way to pass correlationId to ansible playbook, so it will be logged to audit log when
             * we start using ansible-runner?
             */
            // deploy.setCorrelationId(getCorrelationId());

            runAnsibleHostDeployPlaybook();
            markCurrentCmdlineAsStored();
            markVdsReinstalled();
            configureManagementNetwork();
            if (!getParameters().getActivateHost()) {
                setVdsStatus(VDSStatus.Maintenance);
            } else {
                setVdsStatus(VDSStatus.Initializing);
            }

            log.info("After Installation host {}, {}", getVds().getName(), getVds().getVdsType().name());
            setSucceeded(true);
        } catch (VdsInstallException e) {
            handleError(e, e.getStatus());
        } catch (Exception e) {
            handleError(e, VDSStatus.InstallFailed);
        }
    }

    private void runAnsibleHostDeployPlaybook() {
        String hostedEngineAction = "";
        File hostedEngineTmpCfgFile = null;
        if (getParameters().getHostedEngineDeployConfiguration() != null) {
            hostedEngineAction =
                    getParameters().getHostedEngineDeployConfiguration().getDeployAction().name().toLowerCase();
        }

        try {
            hostedEngineTmpCfgFile = Files.createTempFile("temp-he-config", "").toFile();
        } catch (Exception ex) {
            throw new VdsInstallException(
                    VDSStatus.InstallFailed,
                    String.format(
                            "Failed to create temporary file for hosted engine configuration: %s",
                            ex.getMessage()));
        }
        try {
            if (HostedEngineDeployConfiguration.Action.DEPLOY.name().equalsIgnoreCase(hostedEngineAction)) {
                fetchHostedEngineConfigFile(hostedEngineTmpCfgFile.getPath());
            }

            String kdumpDestinationAddress = Config.getValue(ConfigValues.FenceKdumpDestinationAddress);
            if (StringUtils.isBlank(kdumpDestinationAddress)) {
                // destination address not entered, use engine FQDN
                kdumpDestinationAddress = EngineLocalConfig.getInstance().getHost();
            }
            Cluster hostCluster = clusterDao.get(getClusterId());
            VDS vds = getVds();
            boolean isGlusterServiceSupported = hostCluster.supportsGlusterService();
            String tunedProfile = isGlusterServiceSupported ? hostCluster.getGlusterTunedProfile() : null;
            Version clusterVersion = hostCluster.getCompatibilityVersion();
            AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                    .hosts(vds)
                    .variable("host_deploy_cluster_version", clusterVersion)
                    .variable("host_deploy_cluster_name", hostCluster.getName())
                    .variable("host_deploy_cluster_switch_type",
                            hostCluster.getRequiredSwitchTypeForCluster().getOptionValue())
                    .variable("host_deploy_gluster_enabled", hostCluster.supportsGlusterService())
                    .variable("host_deploy_virt_enabled", hostCluster.supportsVirtService())
                    .variable("host_deploy_vdsm_port", vds.getPort())
                    .variable("host_deploy_override_firewall", getParameters().getOverrideFirewall())
                    .variable("host_deploy_firewall_type", hostCluster.getFirewallType().name())
                    .variable("ansible_port", vds.getSshPort())
                    .variable("host_deploy_post_tasks", AnsibleConstants.HOST_DEPLOY_POST_TASKS_FILE_PATH)
                    .variable("host_deploy_ovn_tunneling_interface", NetworkUtils.getHostIp(vds))
                    .variable("host_deploy_ovn_central", getOvnCentral())
                    .variable("host_deploy_vnc_tls", hostCluster.isVncEncryptionEnabled() ? "true" : "false")
                    .variable("host_deploy_kdump_integration", vds.isPmEnabled() && vds.isPmKdumpDetection())
                    .variable("host_deploy_kdump_destination_address", kdumpDestinationAddress)
                    .variable("host_deploy_kdump_destination_port",
                            Config.getValue(ConfigValues.FenceKdumpDestinationPort))
                    .variable("host_deploy_kdump_message_interval",
                            Config.getValue(ConfigValues.FenceKdumpMessageInterval))
                    .variable("host_deploy_kernel_cmdline_new", vds.getCurrentKernelCmdline())
                    .variable("host_deploy_kernel_cmdline_old", vds.getLastStoredKernelCmdline())
                    .variable("ovirt_pki_dir", config.getPKIDir())
                    .variable("ovirt_vds_hostname", vds.getHostName())
                    .variable("ovirt_san", CertificateUtils.getSan(vds.getHostName()))
                    .variable("ovirt_vdscertificatevalidityinyears",
                            Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInYears))
                    .variable("ovirt_signcerttimeoutinseconds",
                            Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds))
                    .variable("ovirt_ca_key",
                            PKIResources.getCaCertificate()
                                    .toString(PKIResources.Format.OPENSSH_PUBKEY)
                                    .replace("\n", ""))
                    .variable("ovirt_ca_cert", PKIResources.getCaCertificate().toString(PKIResources.Format.X509_PEM))
                    .variable("ovirt_qemu_ca_cert", PKIResources.getQemuCaCertificate().toString(PKIResources.Format.X509_PEM))
                    .variable("ovirt_engine_usr", config.getUsrDir())
                    .variable("ovirt_organizationname", Config.getValue(ConfigValues.OrganizationName))
                    .variable("host_deploy_iptables_rules", getIptablesRules(vds, hostCluster))
                    .variable("host_deploy_gluster_supported", isGlusterServiceSupported)
                    .variable("host_deploy_tuned_profile", tunedProfile)
                    .variable("host_deploy_vdsm_encrypt_host_communication",
                            Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication).toString())
                    .variable("host_deploy_vdsm_ssl_ciphers", Config.<String> getValue(ConfigValues.VdsmSSLCiphers))
                    .variable("host_deploy_vdsm_min_version", Config.getValue(ConfigValues.BootstrapMinimalVdsmVersion))
                    .variable("hosted_engine_deploy_action", hostedEngineAction)
                    .variable("hosted_engine_tmp_cfg_file", hostedEngineTmpCfgFile)
                    .variable("hosted_engine_host_id", hostedEngineHelper.offerHostId(vds.getId()))
                    .playbook(AnsibleConstants.HOST_DEPLOY_PLAYBOOK)
                    // /var/log/ovirt-engine/host-deploy/ovirt-host-deploy-ansible-{hostname}-{correlationid}-{timestamp}.log
                    .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                    .logFilePrefix("ovirt-host-deploy-ansible")
                    .logFileName(vds.getHostName())
                    .logFileSuffix(getCorrelationId())
                    .correlationId(getCorrelationId())
                    .playAction(String.format("Installing Host %s", vds.getName()))
                    .playbook(AnsibleConstants.HOST_DEPLOY_PLAYBOOK);

            AuditLogable logable = new AuditLogableImpl();
            logable.setVdsName(vds.getName());
            logable.setVdsId(vds.getId());
            logable.setCorrelationId(getCorrelationId());
            auditLogDirector.log(logable, AuditLogType.VDS_ANSIBLE_INSTALL_STARTED);

            AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(
                    commandConfig,
                    log,
                    (eventName, eventUrl) -> setVdsmId(eventName, eventUrl)
            );
            if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
                throw new VdsInstallException(
                    VDSStatus.InstallFailed,
                    String.format(
                        "Failed to execute Ansible host-deploy role: %1$s. Please check logs for more details: %2$s",
                        ansibleReturnValue.getStderr(),
                        ansibleReturnValue.getLogFile()
                    )
                );
            }

            auditLogDirector.log(logable, AuditLogType.VDS_ANSIBLE_INSTALL_FINISHED);
        } finally {
            if (hostedEngineTmpCfgFile != null) {
                try {
                    hostedEngineTmpCfgFile.delete();
                } catch (Exception ex) {
                    log.error("Error deleting temporary file '{}': {}",
                            hostedEngineTmpCfgFile.toString(),
                            ex.getMessage());
                }
            }
        }
    }

    private void fetchHostedEngineConfigFile(String tmpHEConfigFileName) {
        VDS vds = vdsDao.get(hostedEngineHelper.getRunningHostId());
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(vds)
                .variable("temp_he_config_file", tmpHEConfigFileName)
                // /var/log/ovirt-engine/host-deploy/ovirt-host-deploy-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                .logFilePrefix("ovirt-host-deploy-ansible")
                .logFileName(vds.getHostName())
                .logFileSuffix(getCorrelationId())
                .correlationId(getCorrelationId())
                .playAction(String.format("Fetching HE configuration from %s", vds.getName()))
                .playbook(AnsibleConstants.FETCH_HE_CONFIG_FILE_PLAYBOOK);

        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(
                commandConfig,
                log,
                (eventName, eventUrl) -> setVdsmId(eventName, eventUrl));
        if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            throw new VdsInstallException(
                    VDSStatus.InstallFailed,
                    String.format(
                            "Failed to fetch hosted engine configuration file. Please check logs for more details: %s",
                            ansibleReturnValue.getLogFile()));
        }

    }

    private void setVdsmId(String eventName, String eventUrl) {
        if (!eventName.equals(AnsibleConstants.TASK_VDSM_ID)) {
            return;
        }
        String vdsmid = runnerClient.getVdsmId(eventUrl);
        log.info(
                "Host {} reports unique id {}",
                getVds().getHostName(),
                vdsmid
        );

        final String hosts = vdsDao.getAllWithUniqueId(vdsmid)
                .stream()
                .filter(vds -> !vds.getId().equals(getVds().getId()))
                .map(VDS::getName)
                .collect(Collectors.joining(","));

        if (!hosts.isEmpty()) {
            log.error(
                    "Host {} reports duplicate unique id {} of following hosts {}",
                    getVds().getHostName(),
                    vdsmid,
                    hosts
            );
            throw new RuntimeException(
                    String.format(
                            "Host %1$s reports unique id which already registered for %2$s",
                            getVds().getHostName(),
                            hosts
                    )
            );
        }

        log.info("Assigning unique id {} to Host {}", vdsmid, getVds().getHostName());
        getVds().setUniqueId(vdsmid);

        TransactionSupport.executeInNewTransaction(() -> {
            vdsStaticDao.update(getVds().getStaticData());
            return null;
        });
    }

    private String getIptablesRules(VDS host, Cluster cluster) {
        String ipTablesConfig = Config.getValue(ConfigValues.IPTablesConfig);

        String serviceIPTablesConfig = "";
        if (cluster.supportsVirtService()) {
            serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigForVirt);
        }
        if (cluster.supportsGlusterService()) {
            serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigForGluster);
        }
        serviceIPTablesConfig += Config.getValue(ConfigValues.IPTablesConfigSiteCustom);

        ipTablesConfig = ipTablesConfig
                .replace("@CUSTOM_RULES@", serviceIPTablesConfig)
                .replace("@VDSM_PORT@", Integer.toString(host.getSshPort()))
                .replace("@SSH_PORT@", Integer.toString(host.getPort()));

        return ipTablesConfig;
    }

    private void markCurrentCmdlineAsStored() {
        final VdsStatic vdsStatic = getVds().getStaticData();
        vdsStaticDao.updateLastStoredKernelCmdline(vdsStatic.getId(), vdsStatic.getCurrentKernelCmdline());
    }

    private void configureManagementNetwork() {
        final NetworkConfigurator networkConfigurator = new NetworkConfigurator(getVds(), getContext());
        if (!networkConfigurator.awaitVdsmResponse()) {
            throw new VdsInstallException(
                VDSStatus.NonResponsive,
                "Network error during communication with the host"
            );
        }

        try {
            networkConfigurator.refreshNetworkConfiguration();
            networkConfigurator.createManagementNetworkIfRequired();
        } catch (VDSNetworkException e) {
            log.error("Exception", e);
            throw new VdsInstallException(
                VDSStatus.NonResponsive,
                "Network error during communication with the host",
                e
            );
        } catch (Exception e) {
            log.error("Exception", e);
            throw new VdsInstallException(
                VDSStatus.NonOperational,
                "Failed to configure management network on the host",
                e
            );
        }
    }

    private String getOvnCentral() {
        Guid providerId = getParameters().getNetworkProviderId();
        if (providerId != null) {
            Provider provider = providerDao.get(providerId);
            if (provider.getType() == ProviderType.EXTERNAL_NETWORK ) {
                OpenstackNetworkProviderProperties properties =
                        (OpenstackNetworkProviderProperties)provider.getAdditionalProperties();
                if (OVIRT_PROVIDER_OVN.toString().equals(properties.getPluginType())) {
                    String ovnCentral = NetworkUtils.getIpAddress(provider.getUrl());
                    if (ovnCentral == null) {
                        throw new VdsInstallException(
                                VDSStatus.InstallFailed,
                                String.format(
                                        "Failed to extract OVN central IP from %1$s",
                                        provider.getUrl()));
                    }
                    return ovnCentral;
                }
            }
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(
                getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VDS,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED
                )
        );
    }

    private void copyEngineSshId() {
        VDS host = getVds();
        String sshCopyIdCommand =
                "exec sh -c 'cd ;"
                        + " umask 077 ;"
                        + " mkdir -p .ssh && "
                        + "{ [ -z \"'`tail -1c .ssh/authorized_keys 2>/dev/null`'\" ] "
                        + "|| echo >> .ssh/authorized_keys || exit 1; }"
                        + " && cat >> .ssh/authorized_keys || exit 1 ; "
                        + "if type restorecon >/dev/null 2>&1 ; then restorecon -F .ssh .ssh/authorized_keys ; fi'";
        try (
                final EngineSSHClient sshClient = new EngineSSHClient();
                final ByteArrayInputStream cmdIn = new ByteArrayInputStream(
                        EngineEncryptionUtils.getEngineSSHPublicKey().getBytes());
                final ByteArrayOutputStream cmdOut = new ByteArrayOutputStream();
                final ByteArrayOutputStream cmdErr = new ByteArrayOutputStream()) {
            try {
                log.info("Opening ssh-copy-id session on host {}", host.getHostName());
                sshClient.setVds(host);
                sshClient.setPassword(getParameters().getPassword());
                sshClient.connect();
                sshClient.authenticate();

                log.info("Executing ssh-copy-id command on host {}", host.getHostName());
                sshClient.executeCommand(sshCopyIdCommand, cmdIn, cmdOut, cmdErr);
            } catch (Exception ex) {
                log.error("ssh-copy-id command failed on host '{}': {}\nStdout: {}\nStderr: {}",
                        host.getHostName(),
                        ex.getMessage(),
                        cmdOut,
                        cmdErr);
                log.debug("Exception", ex);
            }
        } catch (IOException e) {
            log.error("Error opening SSH connection to '{}': {}",
                    host.getHostName(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }
}
