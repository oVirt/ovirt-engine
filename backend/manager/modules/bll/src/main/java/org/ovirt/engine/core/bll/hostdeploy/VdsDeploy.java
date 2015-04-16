package org.ovirt.engine.core.bll.hostdeploy;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import javax.naming.TimeLimitExceededException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.utils.EngineSSHDialog;
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
import org.ovirt.engine.core.utils.archivers.tar.CachedTar;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.hostinstall.OpenSslCAWrapper;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.uutils.ssh.SSHDialog;
import org.ovirt.otopi.constants.BaseEnv;
import org.ovirt.otopi.constants.Confirms;
import org.ovirt.otopi.constants.CoreEnv;
import org.ovirt.otopi.constants.NetEnv;
import org.ovirt.otopi.constants.Queries;
import org.ovirt.otopi.constants.SysEnv;
import org.ovirt.otopi.dialog.Event;
import org.ovirt.otopi.dialog.MachineDialogParser;
import org.ovirt.otopi.dialog.SoftError;
import org.ovirt.ovirt_host_deploy.constants.Const;
import org.ovirt.ovirt_host_deploy.constants.Displays;
import org.ovirt.ovirt_host_deploy.constants.GlusterEnv;
import org.ovirt.ovirt_host_deploy.constants.KdumpEnv;
import org.ovirt.ovirt_host_deploy.constants.OpenStackEnv;
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
public class VdsDeploy implements SSHDialog.Sink, Closeable {

    public static enum DeployStatus {Complete, Incomplete, Failed, Reboot};
    private static final int THREAD_JOIN_TIMEOUT = 20 * 1000; // milliseconds
    private static final String IPTABLES_CUSTOM_RULES_PLACE_HOLDER = "@CUSTOM_RULES@";
    private static final String IPTABLES_VDSM_PORT_PLACE_HOLDER = "@VDSM_PORT@";
    private static final String IPTABLES_SSH_PORT_PLACE_HOLDER = "@SSH_PORT@";
    private static final String BOOTSTRAP_CUSTOM_ENVIRONMENT_PLACE_HOLDER = "@ENVIRONMENT@";

    private static final Logger log = LoggerFactory.getLogger(VdsDeploy.class);
    private static volatile CachedTar s_deployPackage;

    private SSHDialog.Control _control;
    private Thread _thread;
    private EngineSSHDialog _dialog;
    private MachineDialogParser _parser;
    private final InstallerMessages _messages;

    private VDS _vds;
    private boolean _isNode = false;
    private boolean _isLegacyNode = false;
    private boolean _reboot = false;
    private String _correlationId = null;
    private Exception _failException = null;
    private boolean _resultError = false;
    private boolean _goingToReboot = false;
    private boolean _aborted = false;
    private boolean _installIncomplete = false;
    private String _managementNetwork = null;
    private DeployStatus _deployStatus = DeployStatus.Failed;

    private String _certificate;
    private String _iptables = "";

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

    /**
     * Values to determine when customization should be performed.
     */
    private static enum CustomizationCondition {
        IPTABLES_OVERRIDE,
        NEUTRON_SETUP,
        NEUTRON_LINUX_BRIDGE_SETUP,
        NEUTRON_OPEN_VSWITCH_SETUP
    };
    /**
     * Special annotation to specify when the customization is necessary.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface CallWhen {
        /**
         * @return A condition that determines if the customization should run.
         */
        CustomizationCondition[] value();
    }
    /**
     * A set of conditions under which the conditional customizations should run.
     */
    private Set<CustomizationCondition> _customizationConditions = new HashSet<>();
    /**
     * Customization tick.
     */
    private int _customizationIndex = 0;
    /**
     * Customization aborting.
     */
    private boolean _customizationShouldAbort = false;
    /**
     * Customization vector.
     * This is tick based vector, every event execute the next
     * tick.
     */
    private final List<Callable<Boolean>> _customizationDialog = Arrays.asList(
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
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.IPTABLES_OVERRIDE)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                NetEnv.IPTABLES_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.IPTABLES_OVERRIDE)
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
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                    OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/host",
                    NetworkUtils.getUniqueHostName(_vds)
                );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getHostKey(),
                _messagingConfiguration.getAddress()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" + _messagingConfiguration.getBrokerType().getPortKey(),
                _messagingConfiguration.getPort()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                                    _messagingConfiguration.getBrokerType().getUsernameKey(),
                _messagingConfiguration.getUsername()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/" +
                            _messagingConfiguration.getBrokerType().getPasswordKey(),
            _messagingConfiguration.getPassword()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_CONFIG_PREFIX + "DEFAULT/rpc_backend",
                _messagingConfiguration.getBrokerType().getRpcBackendValue()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_LINUX_BRIDGE_SETUP)
        public Boolean call() throws Exception {
            _setCliEnvironmentIfNecessary(
                OpenStackEnv.NEUTRON_LINUXBRIDGE_CONFIG_PREFIX + "LINUX_BRIDGE/physical_interface_mappings",
                _openStackAgentProperties.getAgentConfiguration().getNetworkMappings()
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_OPEN_VSWITCH_SETUP)
        public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                OpenStackEnv.NEUTRON_OPENVSWITCH_ENABLE,
                true
            );
            return true;
        }},
        new Callable<Boolean>() {@CallWhen(CustomizationCondition.NEUTRON_OPEN_VSWITCH_SETUP)
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
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliInstall();
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
    /**
     * Execute the next customization vector entry.
     */
    private void _nextCustomizationEntry() throws Exception {
        try {
            if (_customizationShouldAbort) {
                _parser.cliAbort();
            }
            else {
                boolean skip = false;
                Callable<Boolean> customizationStep = _customizationDialog.get(_customizationIndex);
                Method callMethod = customizationStep.getClass().getDeclaredMethod("call");
                if (callMethod != null) {
                    CallWhen ann = callMethod.getAnnotation(CallWhen.class);
                    skip = ann != null && !_customizationConditions.containsAll(Arrays.asList(ann.value()));
                }

                if (skip) {
                    _customizationIndex++;
                    _parser.cliNoop();
                }
                else {
                    if (customizationStep.call()) {
                        _customizationIndex++;
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Protocol violation", e);
        }
        catch (SoftError e) {
            log.error(
                "Soft error during host {} customization dialog: {}",
                _vds.getHostName(),
                e.getMessage()
            );
            log.debug("Exception", e);
            _failException = e;
            _customizationShouldAbort = true;
        }
    }

    /*
     * Termination dialog.
     */

    /**
     * Termination dialog tick.
     */
    private int _terminationIndex = 0;
    /**
     * Termination vector.
     * This is tick based vector, every event execute the next
     * tick.
     */
    private final List<Callable<Boolean>> _terminationDialog = Arrays.asList(
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _resultError = (Boolean)_parser.cliEnvironmentGet(
                BaseEnv.ERROR
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _aborted = (Boolean)_parser.cliEnvironmentGet(
                BaseEnv.ABORTED
            );
            return true;
        }},
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
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            File logFile = new File(
                EngineLocalConfig.getInstance().getLogDir(),
                String.format(
                    "%1$s%2$sovirt-%3$s-%4$s-%5$s.log",
                    "host-deploy",
                    File.separator,
                    new SimpleDateFormat("yyyyMMddHHmmss").format(
                        Calendar.getInstance().getTime()
                    ),
                    _vds.getHostName(),
                    _correlationId
                )
            );
            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Retrieving installation logs to: '%1$s'",
                    logFile
                )
            );
            try (final OutputStream os = new FileOutputStream(logFile)) {
                _parser.cliDownloadLog(os);
            }
            catch (IOException e) {
                throw e;
            }
            catch (Exception e) {
                log.error("Unexpected exception", e);
                throw new RuntimeException(e);
            }
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliEnvironmentSet(
                CoreEnv.LOG_REMOVE_AT_EXIT,
                true
            );
            return true;
        }},
        new Callable<Boolean>() { public Boolean call() throws Exception {
            _parser.cliQuit();
            return true;
        }}
    );
    /**
     * Execute the next termination vector entry.
     */
    private void _nextTerminationEntry() throws Exception {
        try {
            if (_terminationDialog.get(_terminationIndex).call()) {
                _terminationIndex++;
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Protocol violation", e);
        }
    }

    /**
     * Dialog implementation.
     * Handle events incoming from host.
     */
    private void _threadMain() {
        try {
            boolean terminate = false;

            while(!terminate) {
                Event.Base bevent = _parser.nextEvent();

                log.debug(
                    "Installation of {}: Event {}",
                    _vds.getHostName(),
                    bevent
                );

                if (bevent instanceof Event.Terminate) {
                    terminate = true;
                }
                else if (bevent instanceof Event.Log) {
                    Event.Log event = (Event.Log)bevent;
                    InstallerMessages.Severity severity;
                    switch (event.severity) {
                    case INFO:
                        severity = InstallerMessages.Severity.INFO;
                        break;
                    case WARNING:
                        severity = InstallerMessages.Severity.WARNING;
                        break;
                    default:
                        severity = InstallerMessages.Severity.ERROR;
                        break;
                    }
                    _messages.post(severity, event.record);
                }
                else if (bevent instanceof Event.Confirm) {
                    Event.Confirm event = (Event.Confirm)bevent;

                    if (Confirms.GPG_KEY.equals(event.what)) {
                        _messages.post(InstallerMessages.Severity.WARNING, event.description);
                        event.reply = true;
                    }
                    else if (org.ovirt.ovirt_host_deploy.constants.Confirms.DEPLOY_PROCEED.equals(event.what)) {
                        event.reply = true;
                    }
                    else {
                        log.warn(
                            "Installation of {}: Not confirming {}: {}",
                            _vds.getHostName(),
                            event.what,
                            event.description
                        );
                    }

                    _parser.sendResponse(event);
                }
                else if (bevent instanceof Event.QueryString) {
                    Event.QueryString event = (Event.QueryString)bevent;

                    if (Queries.CUSTOMIZATION_COMMAND.equals(event.name)) {
                        _nextCustomizationEntry();
                    }
                    else if (Queries.TERMINATION_COMMAND.equals(event.name)) {
                        _nextTerminationEntry();
                    }
                    else {
                        throw new Exception(
                            String.format(
                                "Unexpected query %1$s",
                                event
                            )
                        );
                    }
                }
                else if (bevent instanceof Event.QueryValue) {
                    Event.QueryValue event = (Event.QueryValue)bevent;

                    if (Queries.TIME.equals(event.name)) {
                        _messages.post(
                            InstallerMessages.Severity.INFO,
                            "Setting time"
                        );
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssZ");
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        event.value = format.format(Calendar.getInstance().getTime());
                    }
                    else {
                        event.abort = true;
                    }
                    _parser.sendResponse(event);
                }
                else if (bevent instanceof Event.QueryMultiString) {
                    Event.QueryMultiString event = (Event.QueryMultiString)bevent;

                    if (org.ovirt.ovirt_host_deploy.constants.Queries.CERTIFICATE_CHAIN.equals(event.name)) {
                        event.value = (
                            PKIResources.Resource.CACertificate.toString(PKIResources.Format.X509_PEM) +
                            _certificate
                        ).split("\n");
                        _parser.sendResponse(event);
                    }
                    else {
                        event.abort = true;
                        _parser.sendResponse(event);
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
                    }
                }
                else {
                    throw new SoftError(
                        String.format(
                            "Unexpected event '%1$s'",
                            bevent
                        )
                    );
                }
            }
        }
        catch (Exception e) {
            _failException = e;
            log.error("Error during deploy dialog", e);
            try {
                _control.close();
            }
            catch (IOException ee) {
                log.error("Error during close", e);
            }
        }
    }

    /*
     * Constructor.
     * @param vds vds to install.
     */
    public VdsDeploy(VDS vds) {
        _vds = vds;

        _messages = new InstallerMessages(_vds);
        _dialog = new EngineSSHDialog();
        _parser = new MachineDialogParser();
        _thread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    _threadMain();
                }
            },
            "VdsDeploy"
        );

        if (s_deployPackage == null) {
            s_deployPackage = new CachedTar(
                new File(
                    EngineLocalConfig.getInstance().getCacheDir(),
                    Config.<String> getValue(ConfigValues.BootstrapPackageName)
                ),
                new File(Config.<String> getValue(ConfigValues.BootstrapPackageDirectory))
            );
        }
    }

    /**
     * Destructor.
     */
    @Override
    protected void finalize() {
        try {
            close();
        }
        catch (IOException e) {
            log.error("Exception during finalize", e);
        }
    }

    /**
     * Release resources.
     */
    public void close() throws IOException {
        stop();
        if (_dialog != null) {
            _dialog.close();
            _dialog = null;
        }
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

    public void setCorrelationId(String correlationId) {
        _correlationId = correlationId;
        _messages.setCorrelationId(_correlationId);
    }

    /**
     * Set user.
     * @param user user.
     */
    public void setUser(String user) {
        _dialog.setUser(user);
    }

    /**
     * Set key pair.
     * @param keyPair key pair.
     */
    public void setKeyPair(KeyPair keyPair) {
        _dialog.setKeyPair(keyPair);
    }

    /**
     * Use engine default key pairs.
     */
    public void useDefaultKeyPair() throws KeyStoreException {
        _dialog.useDefaultKeyPair();
    }

    /**
     * Set password.
     * @param password password.
     */
    public void setPassword(String password) {
        _dialog.setPassword(password);
    }

    /**
     * Enable firewall setup.
     * @param doFirewall enable.
     */
    public void setFirewall(boolean doFirewall) {
        if (doFirewall) {
            _iptables = _getIpTables();
            _customizationConditions.add(CustomizationCondition.IPTABLES_OVERRIDE);
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

    /**
     * Main method.
     * Execute the command and initiate the dialog.
     */
    public void execute() throws Exception {
        try {
            _dialog.setVds(_vds);
            _dialog.connect();
            _messages.post(
                InstallerMessages.Severity.INFO,
                String.format(
                    "Connected to host %1$s with SSH key fingerprint: %2$s",
                    _vds.getHostName(),
                    _dialog.getHostFingerprint()
                )
            );
            _dialog.authenticate();

            String command = Config.<String> getValue(ConfigValues.BootstrapCommand);

            // in future we should set here LANG, LC_ALL
            command = command.replace(
                BOOTSTRAP_CUSTOM_ENVIRONMENT_PLACE_HOLDER,
                ""
            );

            log.info(
                "Installation of {}. Executing command via SSH {} < {}",
                _vds.getHostName(),
                command,
                s_deployPackage.getFileNoUse()
            );

            try (final InputStream in = new FileInputStream(s_deployPackage.getFile())) {
                _dialog.executeCommand(
                    this,
                    command,
                    new InputStream[] {in}
                );
            }

            if (_failException != null) {
                throw _failException;
            }

            if (_resultError) {
                // This is unlikeley as the ssh command will exit with failure.
                throw new RuntimeException(
                    "Installation failed, please refer to installation logs"
                );
            }
            else if (_goingToReboot) {
                _deployStatus = DeployStatus.Reboot;
            }
            else if (_installIncomplete) {
                _deployStatus = DeployStatus.Incomplete;
            } else {
                _deployStatus = DeployStatus.Complete;
            }
        }
        catch (TimeLimitExceededException e){
            log.error(
                "Timeout during host {} install",
                _vds.getHostName(),
                e
            );
            _messages.post(
                InstallerMessages.Severity.ERROR,
                "Processing stopped due to timeout"
            );
            throw e;
        }
        catch(Exception e) {
            log.error(
                "Error during host {} install",
                _vds.getHostName(),
                e
            );
            if (_failException == null) {
                throw e;
            }
            else {
                _messages.post(
                    InstallerMessages.Severity.ERROR,
                    e.getMessage()
                );

                log.error(
                    "Error during host {} install, prefering first exception: {}",
                    _vds.getHostName(),
                    _failException.getMessage()
                );
                log.debug("Exception", _failException);
                throw _failException;
            }
        }
    }

    /*
     * SSHDialog.Sink
     */

    @Override
    public void setControl(SSHDialog.Control control) {
        _control = control;
    }

    @Override
    public void setStreams(InputStream incoming, OutputStream outgoing) {
        _parser.setStreams(incoming, outgoing);
    }

    @Override
    public void start() {
        _thread.start();
    }

    @Override
    public void stop() {
        if (_thread != null) {
            /*
             * We cannot just interrupt the thread as the
             * implementation of jboss connection pooling
             * drops the connection when interrupted.
             * As we may have log events pending to be written
             * to database, we wait for some time for thread
             * complete before interrupting.
             */
            try {
                _thread.join(THREAD_JOIN_TIMEOUT);
            }
            catch (InterruptedException e) {
                log.error("interrupted", e);
            }
            if (_thread.isAlive()) {
                _thread.interrupt();
                while(true) {
                    try {
                        _thread.join();
                        break;
                    }
                    catch (InterruptedException e) {}
                }
            }
            _thread = null;
        }
    }

    public void setOpenStackAgentProperties(OpenstackNetworkProviderProperties properties) {
        _openStackAgentProperties = properties;
        if (_openStackAgentProperties != null) {
            _messagingConfiguration = _openStackAgentProperties.getAgentConfiguration().getMessagingConfiguration();
            _customizationConditions.add(CustomizationCondition.NEUTRON_SETUP);
            if (_openStackAgentProperties.isLinuxBridge()) {
                _customizationConditions.add(CustomizationCondition.NEUTRON_LINUX_BRIDGE_SETUP);
            }
            else if (_openStackAgentProperties.isOpenVSwitch()) {
                _customizationConditions.add(CustomizationCondition.NEUTRON_OPEN_VSWITCH_SETUP);
            }
        }
    }
}
