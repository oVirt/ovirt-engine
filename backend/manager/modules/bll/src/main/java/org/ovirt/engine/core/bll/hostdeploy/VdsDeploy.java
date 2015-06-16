package org.ovirt.engine.core.bll.hostdeploy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties.MessagingConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.PKIResources;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.hostinstall.OpenSslCAWrapper;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.otopi.constants.Confirms;
import org.ovirt.otopi.constants.CoreEnv;
import org.ovirt.otopi.constants.NetEnv;
import org.ovirt.otopi.constants.SysEnv;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.otopi.dialog.SoftError;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.Displays;
import org.ovirt.ovirt_host_deploy.constants.GlusterEnv;
import org.ovirt.ovirt_host_deploy.constants.TuneEnv;
import org.ovirt.ovirt_host_deploy.constants.KdumpEnv;
import org.ovirt.ovirt_host_deploy.constants.OpenStackEnv;
import org.ovirt.ovirt_host_deploy.constants.VMConsoleEnv;
import org.ovirt.ovirt_host_deploy.constants.VdsmEnv;
import org.ovirt.ovirt_host_deploy.constants.VirtEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Host deploy implementation.
 * Executed if:
 * <ul>
 * <li>Host install.</li>
 * <li>Host re-install.</li>
 * <li>Node install.</li>
 * <li>Node approve.</li>
 * </ul>
 *
 * The deploy process is done via the ovirt-host-deploy component using
 * otopi machine dialog interface, refer to otopi documentation.
 * The installer environment is set according to the ovirt-host-deploy
 * documentation.
 */
public class VdsDeploy extends VdsDeployBase {

    public static enum DeployStatus {Complete, Incomplete, Failed, Reboot};

    private static final String IPTABLES_CUSTOM_RULES_PLACE_HOLDER = "@CUSTOM_RULES@";
    private static final String IPTABLES_VDSM_PORT_PLACE_HOLDER = "@VDSM_PORT@";
    private static final String IPTABLES_SSH_PORT_PLACE_HOLDER = "@SSH_PORT@";

    private static final String COND_IPTABLES_OVERRIDE = "IPTABLES_OVERRIDE";
    private static final String COND_NEUTRON_SETUP = "NEUTRON_SETUP";
    private static final String COND_NEUTRON_LINUX_BRIDGE_SETUP = "NEUTRON_LINUX_BRIDGE_SETUP";
    private static final String COND_NEUTRON_OPEN_VSWITCH_SETUP = "NEUTRON_OPEN_VSWITCH_SETUP";

    private static final Logger log = LoggerFactory.getLogger(VdsDeploy.class);

    private boolean _isNode = false;
    private boolean _isLegacyNode = false;
    private boolean _reboot = false;
    private boolean _goingToReboot = false;
    private boolean _installIncomplete = false;
    private String _managementNetwork = null;
    private DeployStatus _deployStatus = DeployStatus.Failed;

    private String _certificate;
    private String _iptables = "";

    private String _sercon_certificate;

    private OpenstackNetworkProviderProperties _openStackAgentProperties = null;
    private MessagingConfiguration _messagingConfiguration = null;

    private boolean fenceKdumpSupported;

    /**
     * set vds object with unique id.
     * Check if vdsmid is unique, if not, halt installation, otherwise
     * update the vds object.
     * @param vdsmid unique id read from host.
     */
    private void _setVdsmId(String vdsmid) {
        if (vdsmid == null) {
            throw new SoftError("Cannot acquire node id");
        }

        log.info(
            "Host {} reports unique id {}",
            _vds.getHostName(),
            vdsmid
        );

        final List<VDS> list = LinqUtils.filter(
            DbFacade.getInstance().getVdsDao().getAllWithUniqueId(vdsmid),
            new Predicate<VDS>() {
                @Override
                public boolean eval(VDS vds) {
                    return !vds.getId().equals(_vds.getId());
                }
            }
        );

        if (!list.isEmpty()) {
            final StringBuilder hosts = new StringBuilder(1024);
            for (VDS v : list) {
                if (hosts.length() > 0) {
                    hosts.append(", ");
                }
                hosts.append(v.getName());
            }

            log.error(
                "Host {} reports duplicate unique id {} of following hosts {}",
                _vds.getHostName(),
                vdsmid,
                hosts
            );
            throw new SoftError(
                String.format(
                    "Host %1$s reports unique id which already registered for %2$s",
                    _vds.getHostName(),
                    hosts
                )
            );
        }

        log.info("Assigning unique id {} to Host {}", vdsmid, _vds.getHostName());
        _vds.setUniqueId(vdsmid);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDao().update(_vds.getStaticData());
                return null;
            }
        });
    }

    /**
     * Set host to be node.
     */
    private void _setNode() {
        _isNode = true;

        _vds.setVdsType(VDSType.oVirtNode);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                DbFacade.getInstance().getVdsStaticDao().update(_vds.getStaticData());
                return null;
            }
        });
    }

    /**
     * Construct iptables to send.
     */
    private String _getIpTables() {
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
            _vds.getVdsGroupId()
        );

        String ipTablesConfig = Config.<String> getValue(ConfigValues.IPTablesConfig);

        String serviceIPTablesConfig = "";
        if (vdsGroup.supportsVirtService()) {
            serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigForVirt);
        }
        if (vdsGroup.supportsGlusterService()) {
            serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigForGluster);
        }
        serviceIPTablesConfig += Config.<String> getValue(ConfigValues.IPTablesConfigSiteCustom);

        ipTablesConfig = ipTablesConfig.replace(
            IPTABLES_CUSTOM_RULES_PLACE_HOLDER,
            serviceIPTablesConfig
        ).replace(
            IPTABLES_SSH_PORT_PLACE_HOLDER,
            Integer.toString(_vds.getSshPort())
        ).replace(
            IPTABLES_VDSM_PORT_PLACE_HOLDER,
            Integer.toString(_vds.getPort())
        );

        return ipTablesConfig;
    }

    /*
     * Customization dialog.
     */

    private final List<Callable<Boolean>> _deployCustomizationDialog = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                "OVIRT_ENGINE/correlationId",
                _correlationId
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (
                (Boolean)_parser.cliEnvironmentGet(
                    VdsmEnv.OVIRT_NODE
                )
            ) {
                _messages.post(
                    InstallerMessages.Severity.INFO,
                    "Host is hypervisor"
                );
                _setNode();
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_isNode) {
                _isLegacyNode = (Boolean)_parser.cliEnvironmentGet(
                    VdsmEnv.OVIRT_NODE_HAS_OWN_BRIDGES
                );
            }
            else {
                _parser.cliNoop();
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Logs at host located at: '%1$s'",
                    _parser.cliEnvironmentGet(
                        CoreEnv.LOG_FILE_NAME
                    )
                )
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                SysEnv.CLOCK_SET,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.SSH_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.SSH_USER,
                _vds.getSshUsername()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.SSH_KEY,
                EngineEncryptionUtils.getEngineSSHPublicKey().replace("\n", "")
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_IPTABLES_OVERRIDE)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.IPTABLES_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_IPTABLES_OVERRIDE)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.IPTABLES_RULES,
                _iptables.split("\n")
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _setVdsmId((String)_parser.cliEnvironmentGet(VdsmEnv.VDSM_ID));
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                String.format(
                    "%svars/ssl",
                    VdsmEnv.CONFIG_PREFIX
                ),
                Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication).toString()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                String.format(
                    "%saddresses/management_port",
                    VdsmEnv.CONFIG_PREFIX
                ),
                Integer.toString(_vds.getPort())
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                VdsmEnv.ENGINE_HOST,
                EngineLocalConfig.getInstance().getHost()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                VdsmEnv.ENGINE_PORT,
                EngineLocalConfig.getInstance().getExternalHttpPort()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_managementNetwork != null) {
                _parser.cliEnvironmentSet(
                    VdsmEnv.MANAGEMENT_BRIDGE_NAME,
                    _managementNetwork
                );
            }
            else if (_isLegacyNode) {
                final ManagementNetworkUtil managmentNetworkUtil = Injector.get(ManagementNetworkUtil.class);
                final Guid clusterId = _vds.getVdsGroupId();
                final Network managementNetwork = managmentNetworkUtil.getManagementNetwork(clusterId);
                _parser.cliEnvironmentSet(
                    VdsmEnv.MANAGEMENT_BRIDGE_NAME,
                    managementNetwork.getName());
            }
            else {
                _parser.cliNoop();
            }
            return true;
        }},
        new Callable<Boolean>() {
            public Boolean call() throws Exception {
                String minimal = Config.<String> getValue(ConfigValues.BootstrapMinimalVdsmVersion);
                if (minimal.trim().length() == 0) {
                    _parser.cliNoop();
            }
            else {
                _parser.cliEnvironmentSet(
                    VdsmEnv.VDSM_MINIMUM_VERSION,
                    minimal
                );
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _vds.getVdsGroupId()
            );
            _parser.cliEnvironmentSet(
                VdsmEnv.CHECK_VIRT_HARDWARE,
                vdsGroup.supportsVirtService()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                VdsmEnv.CERTIFICATE_ENROLLMENT,
                Const.CERTIFICATE_ENROLLMENT_INLINE
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _vds.getVdsGroupId()
            );
            _parser.cliEnvironmentSet(
                VirtEnv.ENABLE,
                vdsGroup.supportsVirtService()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _vds.getVdsGroupId()
            );
            _parser.cliEnvironmentSet(
                GlusterEnv.ENABLE,
                vdsGroup.supportsGlusterService()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(
                _vds.getVdsGroupId()
            );
            String tunedProfile = vdsGroup.supportsGlusterService() ? vdsGroup.getGlusterTunedProfile() : null;
            if (tunedProfile == null || tunedProfile.isEmpty()) {
                _parser.cliNoop();
            } else {
                _parser.cliEnvironmentSet(TuneEnv.TUNED_PROFILE, tunedProfile);
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            /**
             * Legacy logic
             * Force reboot only if not node.
             */
            boolean reboot = _reboot && !_isNode;
            if (reboot) {
                _messages.post(
                    InstallerMessages.Severity.INFO,
                    "Enforcing host reboot"
                );
            }
            _parser.cliEnvironmentSet(
                org.ovirt.ovirt_host_deploy.constants.CoreEnv.FORCE_REBOOT,
                reboot
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                    OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/host",
                    NetworkUtils.getUniqueHostName(_vds)
                );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getHostKey(),
                _messagingConfiguration.getAddress()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getPortKey(),
                _messagingConfiguration.getPort()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                                    _messagingConfiguration.getBrokerType().getUsernameKey(),
                _messagingConfiguration.getUsername()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                            _messagingConfiguration.getBrokerType().getPasswordKey(),
            _messagingConfiguration.getPassword()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/rpc_backend",
                _messagingConfiguration.getBrokerType().getRpcBackendValue()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_CONFIG_PREFIX + "LINUX_BRIDGE/physical_interface_mappings",
                _openStackAgentProperties.getAgentConfiguration().getNetworkMappings()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_OPEN_VSWITCH_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_OPENVSWITCH_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(COND_NEUTRON_OPEN_VSWITCH_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_OPENVSWITCH_CONFIG_PREFIX + "OVS/bridge_mappings",
                _openStackAgentProperties.getAgentConfiguration().getNetworkMappings()
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            fenceKdumpSupported = (Boolean)_parser.cliEnvironmentGet(KdumpEnv.SUPPORTED);
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            boolean enabled = _vds.isPmEnabled() &&
                    _vds.isPmKdumpDetection() &&
                    fenceKdumpSupported;
            if (!enabled) {
                _messages.post(
                        InstallerMessages.Severity.INFO,
                        "Disabling Kdump integration"
                );
            }

            _parser.cliEnvironmentSet(
                    KdumpEnv.ENABLE,
                    enabled
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            String destinationAddress = Config.<String>getValue(ConfigValues.FenceKdumpDestinationAddress);
            if (StringUtils.isBlank(destinationAddress)) {
                // destination address not entered, use engine FQDN
                destinationAddress = EngineLocalConfig.getInstance().getHost();
            }
            _parser.cliEnvironmentSet(
                    KdumpEnv.DESTINATION_ADDRESS,
                    destinationAddress
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                    KdumpEnv.DESTINATION_PORT,
                    Config.<Integer>getValue(ConfigValues.FenceKdumpDestinationPort)
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                    KdumpEnv.MESSAGE_INTERVAL,
                    Config.<Integer>getValue(ConfigValues.FenceKdumpMessageInterval)
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen("VMCONSOLE_ENABLE")
        public Boolean call() throws Exception {
            Integer support = (Integer)_parser.cliEnvironmentGet(
                VMConsoleEnv.SUPPORT
            );
            if (support == null || support != Const.VMCONSOLE_SUPPORT_V1) {
                removeCustomizationCondition("VMCONSOLE_ENABLE");
            }
            return true;
        }},
        new Callable<Boolean>() {@CallWhen("VMCONSOLE_ENABLE")
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                VMConsoleEnv.ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen("VMCONSOLE_ENABLE")
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                VMConsoleEnv.CAKEY,
                PKIResources.Resource.CACertificate.toString(
                    PKIResources.Format.OPENSSH_PUBKEY
                ).replace("\n", "")
            );
            return true;
        }}
    );

    /**
     * Set the CLI environment variable if it's not <code>null</code>, otherwise perform a no-op so the dialog can
     * advance.
     * @param name The name of the variable to set.
     * @param value The value to set (can be <code>null</code>).
     * @throws IOException In case of error while communicating with the parser
     */
    private void _setCliEnvironmentIfNecessary(String name, Object value) throws IOException {
        if (value == null) {
            _parser.cliNoop();
        }
        else {
            _parser.cliEnvironmentSet(name, value);
        }
    }

    private final List<Callable<Boolean>> _deployTerminationDialog = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _installIncomplete = (Boolean)_parser.cliEnvironmentGet(
                org.ovirt.ovirt_host_deploy.constants.CoreEnv.INSTALL_INCOMPLETE
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            if (_resultError || !_installIncomplete) {
                _parser.cliNoop();
            }
            else {
                String[] msgs = (String[])_parser.cliEnvironmentGet(
                    org.ovirt.ovirt_host_deploy.constants.CoreEnv.INSTALL_INCOMPLETE_REASONS
                );
                _messages.post(
                    InstallerMessages.Severity.WARNING,
                    "Installation is incomplete, manual intervention is required"
                );
                for (String m : msgs) {
                    _messages.post(
                        InstallerMessages.Severity.WARNING,
                        m
                    );
                }
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _goingToReboot = (Boolean)_parser.cliEnvironmentGet(
                SysEnv.REBOOT
            );
            if (_goingToReboot) {
                _messages.post(
                    InstallerMessages.Severity.INFO,
                    "Reboot scheduled"
                );
            }
            return true;
        }}
    );

    protected boolean processEvent(Event.Base bevent) throws IOException {
        boolean unknown = true;

        if (bevent instanceof Event.Confirm) {
            Event.Confirm event = (Event.Confirm)bevent;

            if (Confirms.GPG_KEY.equals(event.what)) {
                _messages.post(InstallerMessages.Severity.WARNING, event.description);
                event.reply = true;
                unknown = false;
            }
            else if (org.ovirt.ovirt_host_deploy.constants.Confirms.DEPLOY_PROCEED.equals(event.what)) {
                event.reply = true;
                unknown = false;
            }
        }
        else if (bevent instanceof Event.QueryValue) {
            Event.QueryValue event = (Event.QueryValue)bevent;

            if (org.ovirt.ovirt_host_deploy.constants.Queries.VMCONSOLE_CERTIFICATE.equals(event.name)) {
                event.value = _sercon_certificate.replace("\n", "");
                unknown = false;
            }
        }
        else if (bevent instanceof Event.QueryMultiString) {
            Event.QueryMultiString event = (Event.QueryMultiString)bevent;

            if (org.ovirt.ovirt_host_deploy.constants.Queries.CERTIFICATE_CHAIN.equals(event.name)) {
                event.value = (
                    PKIResources.Resource.CACertificate.toString(PKIResources.Format.X509_PEM) +
                    _certificate
                ).split("\n");
                unknown = false;
            }
        }
        else if (bevent instanceof Event.DisplayMultiString) {
            Event.DisplayMultiString event = (Event.DisplayMultiString)bevent;

            if (Displays.CERTIFICATE_REQUEST.equals(event.name)) {
                _messages.post(
                    InstallerMessages.Severity.INFO,
                    "Enrolling certificate"
                );
                _certificate = OpenSslCAWrapper.signCertificateRequest(
                    StringUtils.join(event.value, "\n"),
                    _vds.getHostName()
                );
                unknown = false;
            }
            else if (Displays.VMCONSOLE_CERTIFICATE_REQUEST.equals(event.name)) {
                _messages.post(
                    InstallerMessages.Severity.INFO,
                    "Enrolling serial console certificate"
                );
                String name = String.format("%s-ssh", _vds.getHostName());
                OpenSslCAWrapper.signCertificateRequest(
                    StringUtils.join(event.value, "\n"),
                    name
                );
                _sercon_certificate = OpenSslCAWrapper.signOpenSSHCertificate(
                    name,
                    _vds.getHostName()
                );
                unknown = false;
            }
        }

        return unknown;
    }

    @Override
    protected void postExecute() {
        if (_goingToReboot) {
            _deployStatus = DeployStatus.Reboot;
        }
        else if (_installIncomplete) {
            _deployStatus = DeployStatus.Incomplete;
        } else {
            _deployStatus = DeployStatus.Complete;
        }
    }

    public VdsDeploy(VDS vds) {
        super("host-deploy", "ovirt-host-deploy", vds);
        addCustomizationDialog(_deployCustomizationDialog);
        addCustomizationDialog(CUSTOMIZATION_DIALOG_EPILOG);
        addTerminationDialog(TERMINATION_DIALOG_PROLOG);
        addTerminationDialog(_deployTerminationDialog);
        addTerminationDialog(TERMINATION_DIALOG_EPILOG);
    }

    /**
     * Set reboot.
     * @param reboot reboot.
     */
    public void setReboot(boolean reboot) {
        _reboot = reboot;
    }

    /**
     * Set the management network name to be configured on the host. If set <code>null</code>, the network will not be
     * configured on the host.
     *
     * @param managementNetwork
     */
    public void setManagementNetwork(String managementNetwork) {
        _managementNetwork = managementNetwork;
    }

    /**
     * Enable firewall setup.
     * @param doFirewall enable.
     */
    public void setFirewall(boolean doFirewall) {
        if (doFirewall) {
            _iptables = _getIpTables();
            addCustomizationCondition(COND_IPTABLES_OVERRIDE);
        }
    }

    /**
     * Enable serial console setup.
     * @param doVMConsole enable.
     */
    public void setVMConsole(boolean doVMConsole) {
        if (doVMConsole) {
            addCustomizationCondition("VMCONSOLE_ENABLE");
        }
    }

    /**
     * Returns the installation status
     *
     * @return the installation status
     */
    public DeployStatus getDeployStatus() {
        return _deployStatus;
    }

    public void setOpenStackAgentProperties(OpenstackNetworkProviderProperties properties) {
        _openStackAgentProperties = properties;
        if (_openStackAgentProperties != null) {
            _messagingConfiguration = _openStackAgentProperties.getAgentConfiguration().getMessagingConfiguration();
            addCustomizationCondition(COND_NEUTRON_SETUP);
            if (_openStackAgentProperties.isLinuxBridge()) {
                addCustomizationCondition(COND_NEUTRON_LINUX_BRIDGE_SETUP);
            }
            else if (_openStackAgentProperties.isOpenVSwitch()) {
                addCustomizationCondition(COND_NEUTRON_OPEN_VSWITCH_SETUP);
            }
        }
    }
}
