package org.ovirt.engine.core.bll.hostdeploy;

import static org.ovirt.engine.core.common.businessentities.ExternalNetworkPluginType.OVIRT_PROVIDER_OVN;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.NetworkConfigurator;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class InstallVdsInternalCommand<T extends InstallVdsParameters> extends VdsCommand<T> {

    private static Logger log = LoggerFactory.getLogger(InstallVdsInternalCommand.class);
    private VDSStatus vdsInitialStatus;

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private ResourceManager resourceManager;
    @Inject
    private ProviderDao providerDao;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private ClusterDao clusterDao;

    @Inject
    private AnsibleExecutor ansibleExecutor;

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

        vdsInitialStatus = getVds().getStatus();
        installHost();
    }

    private void installHost() {
        try (final VdsDeploy deploy = new VdsDeploy("ovirt-host-deploy", getVds(), true)) {
            log.info(
                "Before Installation host {}, {}",
                getVds().getId(),
                getVds().getName()
            );

            T parameters = getParameters();
            deploy.setCorrelationId(getCorrelationId());

            Cluster hostCluster = clusterDao.get(getClusterId());

            deploy.addUnit(
                new VdsDeployMiscUnit(),
                new VdsDeployVdsmUnit(hostCluster.getCompatibilityVersion()),
                new VdsDeployPKIUnit()
            );

            if (MapUtils.isNotEmpty(parameters.getHostedEngineConfiguration())) {
                deploy.addUnit(new VdsDeployHostedEngineUnit(parameters.getHostedEngineConfiguration()));
            }

            switch (getParameters().getAuthMethod()) {
                case Password:
                    deploy.setPassword(parameters.getPassword());
                break;
                case PublicKey:
                    deploy.useDefaultKeyPair();
                break;
                default:
                    throw new Exception("Invalid authentication method value was sent to InstallVdsInternalCommand");
            }

            setVdsStatus(VDSStatus.Installing);
            deploy.execute();

            switch (deploy.getDeployStatus()) {
                case Failed:
                    throw new VdsInstallException(VDSStatus.InstallFailed, StringUtils.EMPTY);
                case Incomplete:
                    markCurrentCmdlineAsStored();
                    throw new VdsInstallException(VDSStatus.InstallFailed, "Partial installation");
                case Reboot:
                    markCurrentCmdlineAsStored();
                    markVdsReinstalled();
                    setVdsStatus(VDSStatus.Reboot);
                    runSleepOnReboot(getStatusOnReboot());
                break;
                case Complete:
                    markCurrentCmdlineAsStored();
                    markVdsReinstalled();

                    // TODO: When more logic goes to ovirt-host-deploy role,
                    // this code should be moved to appropriate place, currently
                    // we run this playbook only after successful run of otopi host-deploy
                    runAnsibleHostDeployPlaybook(hostCluster);

                    configureManagementNetwork();
                    if (!getParameters().getActivateHost()) {
                        setVdsStatus(VDSStatus.Maintenance);
                    } else {
                        setVdsStatus(VDSStatus.Initializing);
                    }
                break;
            }

            log.info(
                "After Installation host {}, {}",
                getVds().getName(),
                getVds().getVdsType().name()
            );
            setSucceeded(true);
        } catch (VdsInstallException e) {
            handleError(e, e.getStatus());
        } catch (Exception e) {
            handleError(e, VDSStatus.InstallFailed);
        }
    }

    private void runAnsibleHostDeployPlaybook(Cluster hostCluster) {
        String kdumpDestinationAddress = Config.getValue(ConfigValues.FenceKdumpDestinationAddress);
        if (StringUtils.isBlank(kdumpDestinationAddress)) {
            // destination address not entered, use engine FQDN
            kdumpDestinationAddress = EngineLocalConfig.getInstance().getHost();
        }
        VDS vds = getVds();
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(vds)
                .variable("host_deploy_cluster_version", hostCluster.getCompatibilityVersion())
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
                .variable("ovirt_vdscertificatevalidityinyears",
                        Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInYears))
                .variable("ovirt_signcerttimeoutinseconds",
                        Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds))
                .variable("ovirt_ca_key",
                        PKIResources.getCaCertificate()
                                .toString(PKIResources.Format.OPENSSH_PUBKEY)
                                .replace("\n", ""))
                .variable("ovirt_engine_usr", config.getUsrDir())
                .variable("ovirt_organizationname", Config.getValue(ConfigValues.OrganizationName))
                .variable("host_deploy_iptables_rules", getIptablesRules(vds, hostCluster))
                .playbook(AnsibleConstants.HOST_DEPLOY_PLAYBOOK)
                // /var/log/ovirt-engine/host-deploy/ovirt-host-deploy-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(VdsDeployBase.HOST_DEPLOY_LOG_DIRECTORY)
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

        AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(commandConfig);
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

    private VDSStatus getStatusOnReboot() {
        if (getParameters().getActivateHost()) {
            return VDSStatus.NonResponsive;
        }
        return VDSStatus.Maintenance.equals(vdsInitialStatus) ? VDSStatus.Maintenance : VDSStatus.NonResponsive;
    }
}
