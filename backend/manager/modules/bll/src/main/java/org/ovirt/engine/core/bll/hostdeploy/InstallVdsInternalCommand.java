package org.ovirt.engine.core.bll.hostdeploy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.EngineSSHClient;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.SshHostRebootParameters;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.ReplaceHostConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
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
import org.ovirt.engine.core.common.utils.ansible.AnsibleRunnerHttpClient;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
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
    private VdsStaticDao vdsStaticDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private HostedEngineHelper hostedEngineHelper;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private GlusterUtil glusterUtil;

    @Inject
    private AnsibleExecutor ansibleExecutor;

    @Inject
    private AnsibleRunnerHttpClient runnerClient;

    @Inject
    private NetworkHelper networkHelper;

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

            runAnsibleHostDeployPlaybook();
            List<VDS> hostlist = vdsDao.getAllForCluster(getClusterId());
            hostlist =  getOnlyActiveHosts(hostlist);
            if(getVds().getClusterSupportsGlusterService() && getParameters().getReplaceHostConfiguration()!=null && getParameters().
                    getReplaceHostConfiguration().getDeployAction() != ReplaceHostConfiguration.Action.NONE && hostlist.size() >= 2) {

                String firstGlusterClusterNode = hostlist.get(0).getName();
                String secondGlusterClusterNode = hostlist.get(1).getName();
                String oldGlusterClusterNode = getVds().getName();
                String newGlusterClusterNode = StringUtils.EMPTY;

                Map<String, Network> stringNetworkMap = networkDao.getNetworksForCluster(getClusterId());
                Map<VDS, String> map = glusterUtil.getGlusterIpaddressAsMap(stringNetworkMap,
                        getVds(), hostlist);

                if(map != null){

                    oldGlusterClusterNode = map.get(getVds());
                    firstGlusterClusterNode = map.get(hostlist.get(0));
                    secondGlusterClusterNode = map.get(hostlist.get(1));
                }

                if(getParameters().getReplaceHostConfiguration().getDeployAction() == ReplaceHostConfiguration.Action.DIFFERENTFQDN) {

                    newGlusterClusterNode = getParameters().getFqdnBox(); //put the editor value here

                } else if (getParameters().getReplaceHostConfiguration().getDeployAction() == ReplaceHostConfiguration.Action.SAMEFQDN) {
                    newGlusterClusterNode = oldGlusterClusterNode; //same fqdn
                }

                VDS maintenanceVds = hostlist.get(0);
                runAnsibleReconfigureGluster(oldGlusterClusterNode, firstGlusterClusterNode,
                    secondGlusterClusterNode, newGlusterClusterNode, maintenanceVds);
            } else {
                if(getParameters().getReplaceHostConfiguration()!=null && getParameters().getReplaceHostConfiguration().getDeployAction() ==
                        ReplaceHostConfiguration.Action.NONE){
                    log.info("Replace host is disabled");
                } else if(!getVds().getClusterSupportsGlusterService()) {
                    log.info("Skipping Replace host since cluster does not support gluster");
                } else if(vdsDao.getAllForCluster(getClusterId()).size()<3) {
                    log.info("Skipping Replace host  since minimum of three hosts are required in the same cluster");
                }
            }

            markCurrentCmdlineAsStored();
            markVdsReinstalled();
            configureManagementNetwork();

            if (getParameters().getRebootHost()) {
                SshHostRebootParameters params = new SshHostRebootParameters(getParameters().getVdsId());
                params.setPrevVdsStatus(getParameters().getPrevVdsStatus());
                params.setWaitOnRebootSynchronous(true);
                ActionReturnValue returnValue = runInternalAction(ActionType.SshHostReboot,
                        params,
                        ExecutionHandler.createInternalJobContext());
                if (!returnValue.getSucceeded()) {
                    setVdsStatus(VDSStatus.InstallFailed);
                    log.error("Engine failed to restart via ssh host '{}' ('{}') after host install",
                            getVds().getName(),
                            getVds().getId());
                    return;
                }
            }
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
        String hostedEngineAction = StringUtils.EMPTY;
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
            String clusterVersion = hostCluster.getCompatibilityVersion().getValue();
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
                    .variable("host_deploy_post_tasks", AnsibleConstants.HOST_DEPLOY_POST_TASKS_FILE_PATH.toString())
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
                    .variable("ovirt_vds_certificate_validity_in_days", Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInDays))
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
                    .variable("host_deploy_origin_type", Config.getValue(ConfigValues.OriginType))
                    .playbook(AnsibleConstants.HOST_DEPLOY_PLAYBOOK)
                    // /var/log/ovirt-engine/host-deploy/ovirt-host-deploy-ansible-{hostname}-{correlationid}-{timestamp}.log
                    .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                    .logFilePrefix("ovirt-host-deploy-ansible")
                    .logFileName(vds.getHostName())
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
                ansibleReturnValue.setAnsibleRunnerServiceLogFile();
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

    private void runAnsibleReconfigureGluster(String oldGlusterClusterNode, String firstGlusterClusterNode,
                                              String secondGlusterClusterNode,
                                              String newGlusterClusterNode, VDS maintenanceVds) {
        log.info("Started Replace Host playbook");
        VDS vds = getVds();
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(maintenanceVds)
                .variable("gluster_maintenance_old_node", oldGlusterClusterNode)
                .variable("gluster_maintenance_new_node", newGlusterClusterNode)
                .variable("gluster_maintenance_cluster_node", firstGlusterClusterNode)
                .variable("gluster_maintenance_cluster_node_2", secondGlusterClusterNode)
                .playbook(AnsibleConstants.REPLACE_GLUSTER_PLAYBOOK)
                .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                .logFilePrefix("ovirt-replace-glusterd-ansible")
                .logFileName("replace-glusterd-host")
                .playAction(String.format("Installing Host %s", vds.getName()))
                .playbook(AnsibleConstants.REPLACE_GLUSTER_PLAYBOOK);

        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsName(vds.getName());
        logable.setVdsId(vds.getId());
        logable.setClusterId(getClusterId());
        logable.setCorrelationId(getCorrelationId());
        auditLogDirector.log(logable, AuditLogType.VDS_ANSIBLE_INSTALL_STARTED);
        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(
                commandConfig,
                log,
                (eventName, eventUrl) -> setVdsmId(eventName, eventUrl)
        );
        log.info("Command Configuration Done ->" + ansibleReturnValue.getAnsibleReturnCode());
        if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            throw new VdsInstallException(
                    VDSStatus.InstallFailed,
                    String.format(
                            "Failed to execute Ansible replace gluster role. Please check logs for more details: %1$s",
                            ansibleReturnValue.getLogFile()));
        }
    }

    private List<VDS> getOnlyActiveHosts(List<VDS> hostlist) {

        hostlist.removeIf(host -> host.getHostName().equals(getVds().getHostName()));
        hostlist.removeIf(host -> host.getStatus() == VDSStatus.Maintenance); // remove other hosts which are in
        // maintainance mode

        return hostlist;
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
        var provider = networkHelper.getOvirtProviderOvn(getCluster().getDefaultNetworkProviderId());
        if (provider == null) {
            return null;
        }

        String ovnCentral = NetworkUtils.getIpAddress(provider.getUrl());
        if (ovnCentral == null) {
            throw new VdsInstallException(VDSStatus.InstallFailed,
                    String.format("Failed to extract OVN central IP from %1$s", provider.getUrl()));
        }
        return ovnCentral;
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
                throw new RuntimeException(String.format("ssh-copy-id command failed: %1$s", ex.getMessage()));
            }
        } catch (IOException e) {
            log.error("Error opening SSH connection to '{}': {}",
                    host.getHostName(),
                    e.getMessage());
            log.debug("Exception", e);
            throw new RuntimeException(String.format("Error opening SSH connection: %1$s", e.getMessage()));
        }
    }
}
