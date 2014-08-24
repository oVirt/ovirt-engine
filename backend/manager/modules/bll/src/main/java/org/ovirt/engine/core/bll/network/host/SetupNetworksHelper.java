package org.ovirt.engine.core.bll.network.host;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.validator.NetworkQosValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.customprop.SimpleCustomPropertiesUtil;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class SetupNetworksHelper {
    protected static final String VIOLATING_ENTITIES_LIST_FORMAT = "${0}_LIST {1}";
    private static final Log log = LogFactory.getLog(SetupNetworksHelper.class);
    private SetupNetworksParameters params;
    private VDS vds;
    private Map<VdcBllMessages, List<String>> violations = new HashMap<VdcBllMessages, List<String>>();
    private Map<String, VdsNetworkInterface> existingIfaces;
    private Map<String, Network> existingClusterNetworks;

    private List<Network> modifiedNetworks = new ArrayList<Network>();
    private List<String> removedNetworks = new ArrayList<String>();
    private Map<String, VdsNetworkInterface> modifiedBonds = new HashMap<String, VdsNetworkInterface>();
    private Map<String, VdsNetworkInterface> removedBonds = new HashMap<String, VdsNetworkInterface>();
    private List<VdsNetworkInterface> modifiedInterfaces = new ArrayList<>();

    /** All interface`s names that were processed by the helper. */
    private Set<String> ifaceNames = new HashSet<String>();

    /** Map of all bonds which were processed by the helper. Key = bond name, Value = list of slave NICs. */
    private Map<String, List<VdsNetworkInterface>> bonds = new HashMap<String, List<VdsNetworkInterface>>();

    /** All network`s names that are attached to some sort of interface. */
    private Set<String> attachedNetworksNames = new HashSet<String>();

    private Map<String, List<NetworkType>> ifacesWithExclusiveNetwork = new HashMap<String, List<NetworkType>>();

    private boolean hostNetworkQosSupported;
    private boolean networkCustomPropertiesSupported;

    public SetupNetworksHelper(SetupNetworksParameters parameters, VDS vds) {
        params = parameters;
        this.vds = vds;

        setSupportedFeatures();
    }

    private void setSupportedFeatures() {
        hostNetworkQosSupported = FeatureSupported.hostNetworkQos(vds.getVdsGroupCompatibilityVersion());
        networkCustomPropertiesSupported =
                FeatureSupported.networkCustomProperties(vds.getVdsGroupCompatibilityVersion());
    }

    protected List<String> translateErrorMessages(List<String> messages) {
        return Backend.getInstance().getErrorsTranslator().TranslateErrorText(messages);
    }

    /**
     * validate and extract data from the list of interfaces sent. The general flow is:
     * <ul>
     * <li>create mapping of existing the current topology - interfaces and logical networks.
     * <li>create maps for networks bonds and bonds-slaves.
     * <li>iterate over the interfaces and extract network/bond/slave info as we go.
     * <li>validate the extracted information by using the pre-build mappings of the current topology.
     * <li>store and encapsulate the extracted lists to later be fetched by the calling command.
     * <li>error messages are aggregated
     * </ul>
     * TODO add fail-fast to exist on the first validation error.
     *
     * @return List of violations encountered (if none, list is empty).
     */
    public List<String> validate() {
        for (VdsNetworkInterface iface : params.getInterfaces()) {
            String name = iface.getName();
            if (addInterfaceToProcessedList(iface)) {
                if (isBond(iface)) {
                    extractBondIfModified(iface, name);
                } else if (StringUtils.isNotBlank(iface.getBondName())) {
                    extractBondSlave(iface);
                }

                // validate and extract to network map
                if (violations.isEmpty() && StringUtils.isNotBlank(iface.getNetworkName())) {
                    extractNetwork(iface);
                    validateGateway(iface);
                }
            }
        }

        validateInterfacesExist();
        validateBondSlavesCount();
        extractRemovedNetworks();
        extractRemovedBonds();
        extractModifiedInterfaces();
        detectSlaveChanges();
        validateMTU();
        validateNetworkQos();
        validateNotRemovingLabeledNetworks();
        validateCustomProperties();

        return translateViolations();
    }

    private void validateNotRemovingLabeledNetworks() {
        Map<String, VdsNetworkInterface> nicsByName = Entities.entitiesByName(params.getInterfaces());
        Map<String, VdsNetworkInterface> hostInterfacesByNetworkName =
                Entities.hostInterfacesByNetworkName(getExistingIfaces().values());

        for (String network : removedNetworks) {
            VdsNetworkInterface nic = hostInterfacesByNetworkName.get(network);
            if (nic != null && !removedBonds.containsKey(nic.getName())) {
                if (NetworkUtils.isVlan(nic)) {
                    nic = nicsByName.get(NetworkUtils.stripVlan(nic));
                    if (nic == null) {
                        continue;
                    }
                }

                Network removedNetwork = getExistingClusterNetworks().get(network);
                if (NetworkUtils.isLabeled(nic) && removedNetwork != null
                        && nic.getLabels().contains(removedNetwork.getLabel())) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_LABELED_NETWORK_FROM_NIC, network);
                }
            }
        }
    }

    private void extractModifiedInterfaces() {
        for (VdsNetworkInterface nic : params.getInterfaces()) {
            VdsNetworkInterface existingNic = getExistingIfaces().get(nic.getName());
            if (existingNic != null) {
                Set<String> newLabels = NetworkUtils.isLabeled(nic) ? nic.getLabels() : Collections.<String> emptySet();
                Set<String> existingLabels =
                        NetworkUtils.isLabeled(existingNic) ? existingNic.getLabels() : Collections.<String> emptySet();
                if (!CollectionUtils.isEqualCollection(newLabels, existingLabels)
                        || (StringUtils.isNotEmpty(nic.getNetworkName()) && qosOrCustomPropertiesChanged(nic, existingNic))) {
                    existingNic.setLabels(newLabels);
                    existingNic.setQosOverridden(nic.isQosOverridden());
                    existingNic.setCustomProperties(nic.getCustomProperties());
                    modifiedInterfaces.add(existingNic);
                }
            }
        }
    }

    private boolean qosOrCustomPropertiesChanged(VdsNetworkInterface nic, VdsNetworkInterface existingNic) {
        return nic.isQosOverridden() != existingNic.isQosOverridden() || customPropertiesChanged(nic, existingNic);
    }

    /**
     * Validates that all interfaces exist on the host, except bonds that may be created.
     */
    private void validateInterfacesExist() {

        for (VdsNetworkInterface iface : params.getInterfaces()) {
            updateBaseInterface(iface);

            String nameWithoutVlanId = NetworkUtils.stripVlan(iface);

            if (!getExistingIfaces().containsKey(nameWithoutVlanId) && !bonds.containsKey(nameWithoutVlanId)) {
                addViolation(VdcBllMessages.NETWORK_INTERFACES_DONT_EXIST, nameWithoutVlanId);
            }
        }
    }

    private void updateBaseInterface(VdsNetworkInterface nic) {
        if (StringUtils.isNotEmpty(nic.getBaseInterface())) {
            return;
        }

        if (NetworkUtils.isVlan(nic)) {
            String[] tokens = nic.getName().split("[.]", -1);
            if (tokens.length == 1) {
                nic.setBaseInterface(nic.getName());
                return;
            }

            nic.setBaseInterface(StringUtils.join(tokens, '.', 0, tokens.length -1));
        }
    }

    /**
     * Validates there is no differences on MTU value between non-VM network to Vlans over the same interface/bond
     */
    private void validateMTU() {
        Map<String, VdsNetworkInterface> ifacesByNetworkName =
                Entities.hostInterfacesByNetworkName(params.getInterfaces());
        Set<String> checkedNetworks = new HashSet<String>(getNetworks().size());

        for (Network network : getNetworks()) {
            if (!checkedNetworks.contains(network.getName())) {
                List<Network> networksOnInterface = findNetworksOnInterface(ifacesByNetworkName.get(network.getName()));
                boolean mtuMismatched = false;
                for (Network net : networksOnInterface) {
                    checkedNetworks.add(net.getName());
                    if (net.getMtu() != network.getMtu()
                            && (NetworkUtils.isNonVmNonVlanNetwork(network) || NetworkUtils.isNonVmNonVlanNetwork(net))) {
                        mtuMismatched = true;
                    }
                }
                if (mtuMismatched) {
                    reportMTUDifferences(networksOnInterface);
                }
            }
        }
    }

    private void reportMTUDifferences(List<Network> ifaceNetworks) {
        List<String> mtuDiffNetworks = new ArrayList<String>();
        for (Network net : ifaceNetworks) {
            mtuDiffNetworks.add(String.format("%s(%s)",
                    net.getName(),
                    net.getMtu() == 0 ? "default" : String.valueOf(net.getMtu())));
        }
        addViolation(VdcBllMessages.NETWORK_MTU_DIFFERENCES,
                String.format("[%s]", StringUtils.join(mtuDiffNetworks, ", ")));
    }

    /**
     * Validates that the feature is supported if any QoS configuration was specified, and that the values associated
     * with it are valid.
     */
    private void validateNetworkQos() {
        for (VdsNetworkInterface iface : params.getInterfaces()) {
            if (iface.isQosOverridden()) {
                if (!hostNetworkQosSupported) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED, iface.getNetworkName());
                }

                NetworkQosValidator qosValidator = new NetworkQosValidator(iface.getQos());
                if (qosValidator.allValuesPresent() != ValidationResult.VALID) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_MISSING_VALUES, iface.getNetworkName());
                }
                if (qosValidator.peakConsistentWithAverage() != ValidationResult.VALID) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_PEAK_LOWER_THAN_AVERAGE, iface.getNetworkName());
                }
            }
        }
    }

    private void validateCustomProperties() {
        String version = vds.getVdsGroupCompatibilityVersion().getValue();
        SimpleCustomPropertiesUtil util = SimpleCustomPropertiesUtil.getInstance();
        Map<String, String> validProperties =
                util.convertProperties(Config.<String> getValue(ConfigValues.PreDefinedNetworkCustomProperties, version));
        validProperties.putAll(util.convertProperties(Config.<String> getValue(ConfigValues.UserDefinedNetworkCustomProperties,
                version)));
        Map<String, String> validPropertiesNonVm = new HashMap<String, String>(validProperties);
        validPropertiesNonVm.remove("bridge_opts");
        for (VdsNetworkInterface iface : params.getInterfaces()) {
            String networkName = iface.getNetworkName();
            if (iface.hasCustomProperties() && StringUtils.isNotEmpty(networkName)) {
                if (!networkCustomPropertiesSupported) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_NOT_SUPPORTED, networkName);
                }

                Network network = existingClusterNetworks.get(networkName);
                List<ValidationError> errors =
                        util.validateProperties(network == null || network.isVmNetwork() ? validProperties
                                : validPropertiesNonVm, iface.getCustomProperties());
                if (!errors.isEmpty()) {
                    addViolation(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_CUSTOM_PROPERTIES_BAD_INPUT, networkName);
                    List<String> messages = new ArrayList<>();
                    util.handleCustomPropertiesError(errors, messages);
                    log.error(StringUtils.join(translateErrorMessages(messages), ','));
                }
            }
        }
    }

    /**
     * Finds all the networks on a specific network interface, directly on the interface or over a vlan.
     *
     * @param iface
     *            the underlying interface
     * @return a list of attached networks to the given underlying interface
     */
    private List<Network> findNetworksOnInterface(VdsNetworkInterface iface) {
        String nameWithoutVlanId = NetworkUtils.stripVlan(iface);
        List<Network> networks = new ArrayList<Network>();
        for (VdsNetworkInterface tmp : params.getInterfaces()) {
            if (NetworkUtils.stripVlan(tmp).equals(nameWithoutVlanId) && tmp.getNetworkName() != null) {
                if (getExistingClusterNetworks().containsKey(tmp.getNetworkName())) {
                    networks.add(getExistingClusterNetworks().get(tmp.getNetworkName()));
                }
            }
        }
        return networks;
    }

    private void addViolation(VdcBllMessages violation, String violatingEntity) {
        List<String> violatingEntities = violations.get(violation);
        if (violatingEntities == null) {
            violatingEntities = new ArrayList<String>();
            violations.put(violation, violatingEntities);
        }

        violatingEntities.add(violatingEntity);
    }

    private List<String> translateViolations() {
        List<String> violationMessages = new ArrayList<String>(violations.size() * 2);
        for (Map.Entry<VdcBllMessages, List<String>> v : violations.entrySet()) {
            String violationName = v.getKey().name();
            violationMessages.add(violationName);
            violationMessages.add(MessageFormat.format(VIOLATING_ENTITIES_LIST_FORMAT,
                    violationName,
                    StringUtils.join(v.getValue(), ", ")));
        }

        return violationMessages;
    }

    /**
     * Add the given interface to the list of processed interfaces, failing if it already existed.
     *
     * @param iface
     *            The interface to add.
     * @return <code>true</code> if interface wasn't in the list and was added to it, otherwise <code>false</code>.
     */
    private boolean addInterfaceToProcessedList(VdsNetworkInterface iface) {
        if (ifaceNames.contains(iface.getName())) {
            addViolation(VdcBllMessages.NETWORK_INTERFACES_ALREADY_SPECIFIED, iface.getName());
            return false;
        }

        ifaceNames.add(iface.getName());
        return true;
    }

    /**
     * Detect a bond that it's slaves have changed, to add to the modified bonds list.<br>
     * Make sure not to add bond that was removed entirely.
     */
    private void detectSlaveChanges() {
        for (VdsNetworkInterface newIface : params.getInterfaces()) {
            VdsNetworkInterface existingIface = getExistingIfaces().get(newIface.getName());
            if (existingIface != null && !isBond(existingIface) && existingIface.getVlanId() == null) {
                String bondNameInNewIface = newIface.getBondName();
                String bondNameInOldIface = existingIface.getBondName();

                if (!StringUtils.equals(bondNameInNewIface, bondNameInOldIface)) {
                    if (bondNameInNewIface != null && !modifiedBonds.containsKey(bondNameInNewIface)) {
                        modifiedBonds.put(bondNameInNewIface, getExistingIfaces().get(bondNameInNewIface));
                    }

                    if (bondNameInOldIface != null && !modifiedBonds.containsKey(bondNameInNewIface)
                            && !removedBonds.containsKey(bondNameInOldIface)) {
                        modifiedBonds.put(bondNameInOldIface, getExistingIfaces().get(bondNameInOldIface));
                    }
                }
            }
        }
    }

    private Map<String, Network> getExistingClusterNetworks() {
        if (existingClusterNetworks == null) {
            existingClusterNetworks = Entities.entitiesByName(
                    getDbFacade().getNetworkDao().getAllForCluster(vds.getVdsGroupId()));
        }

        return existingClusterNetworks;
    }

    private Map<String, VdsNetworkInterface> getExistingIfaces() {
        if (existingIfaces == null) {
            List<VdsNetworkInterface> ifaces =
                    getDbFacade().getInterfaceDao().getAllInterfacesForVds(params.getVdsId());
            NetworkQoSDao qosDao = getDbFacade().getNetworkQosDao();

            for (VdsNetworkInterface iface : ifaces) {
                Network network = getExistingClusterNetworks().get(iface.getNetworkName());
                iface.setNetworkImplementationDetails(NetworkUtils.calculateNetworkImplementationDetails(network,
                        network == null ? null : qosDao.get(network.getQosId()),
                        iface));
            }

            existingIfaces = Entities.entitiesByName(ifaces);
        }

        return existingIfaces;
    }

    private VdsNetworkInterface getExistingIfaceByNetwork(String network) {
        for (VdsNetworkInterface iface : getExistingIfaces().values()) {
            if (network.equals(iface.getNetworkName())) {
                return iface;
            }
        }

        return null;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * extracting a network is done by matching the desired network name with the network details from db on
     * clusterNetworksMap. The desired network is just a key and actual network configuration is taken from the db
     * entity.
     *
     * @param iface
     *            current iterated interface
     */
    private void extractNetwork(VdsNetworkInterface iface) {
        String networkName = iface.getNetworkName();

        // prevent attaching 2 interfaces to 1 network
        if (attachedNetworksNames.contains(networkName)) {
            addViolation(VdcBllMessages.NETWORKS_ALREADY_ATTACHED_TO_IFACES, networkName);
        } else {
            attachedNetworksNames.add(networkName);

            // check if network exists on cluster
            if (getExistingClusterNetworks().containsKey(networkName)) {
                Network network = getExistingClusterNetworks().get(networkName);
                iface.setVlanId(network.getVlanId());
                validateNetworkInternal(network);
                validateNetworkExclusiveOnIface(iface,
                        determineNetworkType(network.getVlanId(), network.isVmNetwork()));

                VdsNetworkInterface existingIface = getExistingIfaces().get(iface.getName());
                if (existingIface != null && !networkName.equals(existingIface.getNetworkName())) {
                    existingIface = getExistingIfaceByNetwork(networkName);
                }

                if (existingIface != null && existingIface.getNetworkImplementationDetails() != null
                        && !existingIface.getNetworkImplementationDetails().isInSync()) {
                    iface.setVlanId(existingIface.getVlanId());
                    if (networkShouldBeSynced(networkName)) {
                        modifiedNetworks.add(network);
                        if (network.getQosId() != null && !hostNetworkQosSupported) {
                            addViolation(VdcBllMessages.ACTION_TYPE_FAILED_HOST_NETWORK_QOS_NOT_SUPPORTED, networkName);
                        }
                    } else if (networkWasModified(iface)) {
                        addViolation(VdcBllMessages.NETWORKS_NOT_IN_SYNC, networkName);
                    }
                } else if (networkWasModified(iface)) {
                    if (networkIpAddressWasSameAsHostnameAndChanged(iface)) {
                        addViolation(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_ADDRESS_CANNOT_BE_CHANGED, networkName);
                    }
                    modifiedNetworks.add(network);
                }
            } else {
                VdsNetworkInterface existingIface = getExistingIfaces().get(iface.getName());
                existingIface = (existingIface == null ? iface : existingIface);
                iface.setVlanId(existingIface.getVlanId());
                validateNetworkExclusiveOnIface(iface,
                        determineNetworkType(existingIface.getVlanId(), existingIface.isBridged()));

                if (unmanagedNetworkChanged(iface)) {
                    addViolation(VdcBllMessages.NETWORKS_DONT_EXIST_IN_CLUSTER, networkName);
                }
            }
        }
    }

    /**
     * Checks if a network is configured incorrectly:
     * <ul>
     * <li>If the host was added to the system using its IP address as the computer name for the certification creation,
     * it is forbidden to modify the IP address without reinstalling the host.</li>
     * </ul>
     *
     * @param iface
     *            The network interface which carries the network
     * @return <code>true</code> if the network was reconfigured improperly
     */
    private boolean networkIpAddressWasSameAsHostnameAndChanged(VdsNetworkInterface iface) {
        if (iface.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            VdsNetworkInterface existingIface = getExistingIfaceByNetwork(iface.getNetworkName());
            if (existingIface != null) {
                String oldAddress = existingIface.getAddress();
                String hostName = vds.getHostName();
                return StringUtils.equals(oldAddress, hostName) && !StringUtils.equals(oldAddress, iface.getAddress());
            }
        }

        return false;
    }

    private NetworkType determineNetworkType(Integer vlanId, boolean vmNetwork) {
        return vlanId != null ? NetworkType.VLAN : vmNetwork ? NetworkType.VM : NetworkType.NON_VM;
    }

    /**
     * Make sure that if the given interface has a VM network on it then there is nothing else on the interface, or if
     * the given interface is a VLAN network, than there is no VM network on the interface.<br>
     * Other combinations are either legal or illegal but are not a concern of this method.
     *
     * @param iface
     *            The interface to check.
     * @param networkType
     *            The type of the network.
     */
    private void validateNetworkExclusiveOnIface(VdsNetworkInterface iface, NetworkType networkType) {
        String ifaceName = NetworkUtils.stripVlan(iface);
        List<NetworkType> networksOnIface = ifacesWithExclusiveNetwork.get(ifaceName);

        if (networksOnIface == null) {
            networksOnIface = new ArrayList<SetupNetworksHelper.NetworkType>();
            ifacesWithExclusiveNetwork.put(ifaceName, networksOnIface);
        }

        if ((networkType == NetworkType.VLAN && networksOnIface.contains(NetworkType.VM))
                || (networkType == NetworkType.VM && !networksOnIface.isEmpty())) {
            addViolation(VdcBllMessages.NETWORK_INTERFACES_NOT_EXCLUSIVELY_USED_BY_NETWORK, ifaceName);
        }

        networksOnIface.add(networkType);
    }

    /**
     * Make sure the network is not an external network, which we can't set up.
     *
     * @param iface
     *            The interface.
     * @param network
     *            The network to check.
     */
    private void validateNetworkInternal(Network network) {
        if (network.getProvidedBy() != null) {
            addViolation(VdcBllMessages.ACTION_TYPE_FAILED_EXTERNAL_NETWORKS_CANNOT_BE_PROVISIONED, network.getName());
        }
    }

    /**
     * Checks if an unmanaged network changed.<br>
     * This can be either if there is no existing interface for this network, i.e. it is a unmanaged VLAN which was
     * moved to a different interface, or if the network name on the existing interface is not the same as it was
     * before.
     *
     * @param iface
     *            The interface on which the unmanaged network is now defined.
     * @return <code>true</code> if the network changed, or <code>false</code> otherwise.
     */
    private boolean unmanagedNetworkChanged(VdsNetworkInterface iface) {
        VdsNetworkInterface existingIface = getExistingIfaces().get(iface.getName());
        return existingIface == null || !iface.getNetworkName().equals(existingIface.getNetworkName());
    }

    /**
     * Check if the network parameters on the given interface were modified (or network was added).
     *
     * @param iface
     *            The interface to check.
     * @return <code>true</code> if the network parameters were changed, or the network wan't on the given interface.
     *         <code>false</code> if it existed and didn't change.
     */
    private boolean networkWasModified(VdsNetworkInterface iface) {
        VdsNetworkInterface existingIface = getExistingIfaces().get(iface.getName());

        if (existingIface == null) {
            return true;
        }

        return !ObjectUtils.equals(iface.getNetworkName(), existingIface.getNetworkName())
                || iface.getBootProtocol() != existingIface.getBootProtocol()
                || staticBootProtoPropertiesChanged(iface, existingIface)
                || !ObjectUtils.equals(iface.getQos(), existingIface.getQos())
                || customPropertiesChanged(iface, existingIface);
    }

    /**
     * Check if network's logical configuration should be synchronized (as sent in parameters).
     *
     * @param network
     *            The network to check if synchronized.
     * @return <code>true</code> in case network should be sunchronized, <code>false</code> otherwise.
     */
    private boolean networkShouldBeSynced(String network) {
        return params.getNetworksToSync() != null && params.getNetworksToSync().contains(network);
    }

    /**
     * @param iface
     *            New interface definition.
     * @param existingIface
     *            Existing interface definition.
     * @return <code>true</code> if the boot protocol is static, and one of the properties has changed.
     *         <code>false</code> otherwise.
     */
    private boolean staticBootProtoPropertiesChanged(VdsNetworkInterface iface, VdsNetworkInterface existingIface) {
        return iface.getBootProtocol() == NetworkBootProtocol.STATIC_IP
                && (!ObjectUtils.equals(iface.getAddress(), existingIface.getAddress())
                        || !ObjectUtils.equals(iface.getGateway(), existingIface.getGateway())
                        || !ObjectUtils.equals(iface.getSubnet(), existingIface.getSubnet()));
    }

    /**
     * @param iface
     *            New interface definition.
     * @param existingIface
     *            Existing interface definition.
     * @return <code>true</code> iff the custom properties have changed (null and empty map are considered equal).
     */
    private boolean customPropertiesChanged(VdsNetworkInterface iface, VdsNetworkInterface existingIface) {
        return (iface.hasCustomProperties() != existingIface.hasCustomProperties())
                || (iface.hasCustomProperties()
                        && !iface.getCustomProperties().equals(existingIface.getCustomProperties()));
    }

    private boolean isBond(VdsNetworkInterface iface) {
        return Boolean.TRUE.equals(iface.getBonded());
    }

    /**
     * build mapping of the bond name - > list of slaves. slaves are interfaces with a pointer to the master bond by
     * bondName.
     *
     * @param iface
     */
    private void extractBondSlave(VdsNetworkInterface iface) {
        List<VdsNetworkInterface> slaves = bonds.get(iface.getBondName());
        if (slaves == null) {
            slaves = new ArrayList<VdsNetworkInterface>();
            bonds.put(iface.getBondName(), slaves);
        }

        slaves.add(iface);
    }

    /**
     * Extract the bond to the modified bonds list if it was added or the bond interface config has changed.
     *
     * @param iface
     *            The interface of the bond.
     * @param bondName
     *            The bond name.
     */
    private void extractBondIfModified(VdsNetworkInterface iface, String bondName) {
        if (!bonds.containsKey(bondName)) {
            bonds.put(bondName, new ArrayList<VdsNetworkInterface>());
        }

        if (bondWasModified(iface)) {
            modifiedBonds.put(bondName, iface);
        }
    }

    /**
     * Check if the given bond was modified (or added).<br>
     * Currently changes that are recognized are if bonding options changed, or the bond was added.
     *
     * @param iface
     *            The bond to check.
     * @return <code>true</code> if the bond was changed, or is a new one. <code>false</code> if it existed and didn't
     *         change.
     */
    private boolean bondWasModified(VdsNetworkInterface iface) {
        VdsNetworkInterface existingIface = getExistingIfaces().get(iface.getName());

        if (existingIface == null) {
            return true;
        }

        return !ObjectUtils.equals(iface.getBondOptions(), existingIface.getBondOptions());
    }

    /**
     * Extract the bonds to be removed. If a bond was attached to slaves but it's not attached to anything then it
     * should be removed. Otherwise, no point in removing it: Either it is still a bond, or it isn't attached to any
     * slaves either way so no need to touch it. If a bond is removed, its labels are also cleared.
     */
    private void extractRemovedBonds() {
        for (VdsNetworkInterface iface : getExistingIfaces().values()) {
            String bondName = iface.getBondName();
            if (StringUtils.isNotBlank(bondName) && !bonds.containsKey(bondName)) {
                VdsNetworkInterface existingBond = getExistingIfaces().get(bondName);
                existingBond.setLabels(null);
                removedBonds.put(bondName, existingBond);
            }
        }
    }

    private boolean validateBondSlavesCount() {
        boolean returnValue = true;
        for (Map.Entry<String, List<VdsNetworkInterface>> bondEntry : bonds.entrySet()) {
            if (bondEntry.getValue().size() < 2) {
                returnValue = false;
                addViolation(VdcBllMessages.NETWORK_BONDS_INVALID_SLAVE_COUNT, bondEntry.getKey());
            }
        }

        return returnValue;
    }

    /**
     * Calculate the networks that should be removed - If the network was attached to a NIC and is no longer attached to
     * it, then it will be removed.
     */
    private void extractRemovedNetworks() {
        for (VdsNetworkInterface iface : getExistingIfaces().values()) {
            String net = iface.getNetworkName();
            if (StringUtils.isNotBlank(net) && !attachedNetworksNames.contains(net)) {
                removedNetworks.add(net);
            }
        }

        List<String> vmNames =
                getVmInterfaceManager().findActiveVmsUsingNetworks(params.getVdsId(), removedNetworks);

        for (String vmName : vmNames) {
            addViolation(VdcBllMessages.NETWORK_CANNOT_DETACH_NETWORK_USED_BY_VMS, vmName);
        }
    }

    /**
     * Validates that gateway is set not on management network just if multiple gateways feature is supported
     */
    private void validateGateway(VdsNetworkInterface iface) {
        if (StringUtils.isNotEmpty(iface.getGateway())
                && !NetworkUtils.isManagementNetwork(iface.getNetworkName())
                && !FeatureSupported.multipleGatewaysSupported(vds.getVdsGroupCompatibilityVersion())) {

            addViolation(VdcBllMessages.NETWORK_ATTACH_ILLEGAL_GATEWAY, iface.getNetworkName());
        }
    }

    public List<Network> getNetworks() {
        return modifiedNetworks;
    }

    public List<String> getRemoveNetworks() {
        return removedNetworks;
    }

    public List<VdsNetworkInterface> getBonds() {
        return new ArrayList<VdsNetworkInterface>(modifiedBonds.values());
    }

    public Map<String, VdsNetworkInterface> getRemovedBonds() {
        return removedBonds;
    }

    public List<VdsNetworkInterface> getModifiedInterfaces() {
        return modifiedInterfaces;
    }

    public VmInterfaceManager getVmInterfaceManager() {
        return new VmInterfaceManager();
    }

    private enum NetworkType {
        VM,
        NON_VM,
        VLAN
    }
}
