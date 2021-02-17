package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "72163d1c-9468-4480-99d9-0888664eb143",
        name = "Network",
        description = "Filters out hosts that are missing networks required by VM NICs, or missing cluster's display network",
        type = PolicyUnitType.FILTER
)
public class NetworkPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(NetworkPolicyUnit.class);

    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;
    @Inject
    private VnicProfileViewDao vnicProfileViewDao;

    public NetworkPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        if (hosts == null || hosts.isEmpty()) {
            return Collections.emptyList();
        }

        List<VDS> toRemoveHostList = new ArrayList<>();
        List<VmNetworkInterface> vmNICs = vmNetworkInterfaceDao.getAllForVm(vm.getId());
        Guid clusterId = hosts.get(0).getClusterId();
        List<Network> clusterNetworks = networkDao.getAllForCluster(clusterId);
        Map<String, Network> networksByName = Entities.entitiesByName(clusterNetworks);
        Map<Guid, List<String>> hostNics = interfaceDao.getHostNetworksByCluster(clusterId);
        Network displayNetwork = NetworkUtils.getDisplayNetwork(clusterNetworks);
        Map<Guid, VdsNetworkInterface> hostDisplayNics = getDisplayNics(displayNetwork);

        for (VDS host : hosts) {
            ValidationResult result =
                    validateRequiredNetworksAvailable(host,
                            vm,
                            vmNICs,
                            displayNetwork,
                            networksByName,
                            hostNics.get(host.getId()),
                            hostDisplayNics.get(host.getId()));

            if (result.isValid()) {
                result = validatePassthroughVnics(vm.getId(), host, vmNICs);
            }

            if (!result.isValid()) {
                toRemoveHostList.add(host);
                messages.addMessages(host.getId(), result.getVariableReplacements());
                messages.addMessages(host.getId(), result.getMessagesAsStrings());
            }
        }
        hosts.removeAll(toRemoveHostList);
        return hosts;
    }

    public Map<Guid, VdsNetworkInterface> getDisplayNics(Network displayNetwork) {
        Map<Guid, VdsNetworkInterface> displayNics = new HashMap<>();
        if (displayNetwork != null) {
            List<VdsNetworkInterface> nics = interfaceDao.getVdsInterfacesByNetworkId(displayNetwork.getId());
            for (VdsNetworkInterface nic : nics) {
                displayNics.put(nic.getVdsId(), nic);
            }
        }

        return displayNics;
    }

    /**
     * Determine whether all required Networks are attached to the Host's Nics. A required Network, depending on
     * ConfigValue.OnlyRequiredNetworksMandatoryForVdsSelection, is defined as: 1. false: any network that is defined on
     * an Active vNic of the VM or the cluster's display network. 2. true: a Cluster-Required Network that is defined on
     * an Active vNic of the VM.
     * Vnic configured for 'passthrough' will skip the network existence check for the host and will be validated later by
     * {@code validatePassthroughVnics(...)}
     *
     * @param vds
     *            the Host
     * @param vm
     *            the VM
     * @param hostNetworks
     *            the Host network names
     * @param displayNic
     *            the interface on top the display network is configured
     * @return the result of network compatibility check
     */
    private ValidationResult validateRequiredNetworksAvailable(VDS vds,
            VM vm,
            List<VmNetworkInterface> vmNICs,
            Network displayNetwork,
            Map<String, Network> networksByName,
            List<String> hostNetworks,
            VdsNetworkInterface displayNic) {

        List<String> missingIfs = new ArrayList<>();

        boolean onlyRequiredNetworks =
                Config.<Boolean> getValue(ConfigValues.OnlyRequiredNetworksMandatoryForVdsSelection);
        for (final VmNetworkInterface vmIf : vmNICs) {
            boolean found = false;
            boolean skipNetworkExistenceCheckForVnicPassthrough = vmIf.isPassthrough();

            if (vmIf.getNetworkName() == null) {
                found = true;
            } else {
                for (String networkName : hostNetworks) {
                    if (skipNetworkExistenceCheckForVnicPassthrough
                            || !networkRequiredOnVds(vmIf, networksByName, onlyRequiredNetworks)
                            || StringUtils.equals(vmIf.getNetworkName(), networkName)) {
                        found = true;
                        break;
                    }
                }

                Network network = networksByName.get(vmIf.getNetworkName());
                if (network.isExternal()) {
                    findPhysicalNetworkNotConnectedAndLinkedTo(network, hostNetworks).ifPresent(missingIfs::add);
                }
                if (skipNetworkExistenceCheckForVnicPassthrough) {
                    findFailoverNetworkNotConnected(vmIf.getVnicProfileId(), networksByName, hostNetworks).ifPresent(
                            missingIfs::add);
                }
            }

            if (!found) {
                missingIfs.add(vmIf.getNetworkName());
            }
        }

        if (!missingIfs.isEmpty()) {
            String nics = StringUtils.join(missingIfs, ", ");
            log.warn("host {} is missing networks required by VM nics {}",
                    vds.getName(), nics);
            return new ValidationResult(EngineMessage.VAR__DETAIL__NETWORK_MISSING,
                    String.format("$networkNames %1$s", nics));
        }

        return validateDisplayNetworkAvailability(vds, onlyRequiredNetworks, displayNic, displayNetwork);
    }

    private boolean networkRequiredOnVds(VmNetworkInterface vmIface,
            Map<String, Network> networksByName,
            boolean onlyRequiredNetworks) {
        if (!vmIface.isPlugged()) {
            return false;
        }

        Network network = networksByName.get(vmIface.getNetworkName());
        if (onlyRequiredNetworks) {
            return network.getCluster().isRequired();
        }

        return !network.isExternal();
    }

    private Optional<String> findPhysicalNetworkNotConnectedAndLinkedTo(Network network, List<String> hostNetworks) {
        if (!network.getProvidedBy().isSetPhysicalNetworkId()) {
            return Optional.empty();
        }

        Network physicalNetwork = networkDao.get(network.getProvidedBy().getPhysicalNetworkId());

        return Optional.ofNullable(hostNetworks.contains(physicalNetwork.getName()) ? null : physicalNetwork.getName());
    }

    private Optional<String> findFailoverNetworkNotConnected(Guid vnicProfileId,
            Map<String, Network> networksByName,
            List<String> hostNetworks) {
        var profile = vnicProfileViewDao.get(vnicProfileId);
        if (profile.getFailoverVnicProfileId() == null) {
            return Optional.empty();
        }

        var failoverNetwork =
                networksByName.get(vnicProfileViewDao.get(profile.getFailoverVnicProfileId()).getNetworkName());

        return Optional.ofNullable(hostNetworks.contains(failoverNetwork.getName()) ? null : failoverNetwork.getName());
    }

    /**
     * Determines whether the cluster's display network is defined on the host.
     *
     * @param host
     *            the host
     * @param onlyRequiredNetworks
     *            should be false, in order the method to be non-trivial.
     * @param displayNic
     *            the interface on top the display network is configured
     * @param displayNetwork
     *            the cluster's display network
     * @return the result of the display network validity check on the given host
     */
    private ValidationResult validateDisplayNetworkAvailability(VDS host,
            boolean onlyRequiredNetworks,
            VdsNetworkInterface displayNic,
            Network displayNetwork) {
        if (onlyRequiredNetworks) {
            return ValidationResult.VALID;
        }

        if (displayNetwork == null) {
            return ValidationResult.VALID;
        }

        // Check if display network attached to host and has a proper boot protocol
        if (displayNic == null) {
            log.warn("host {} is missing the cluster's display network {}",
                    host.getName(),
                    displayNetwork.getName());
            return new ValidationResult(EngineMessage.VAR__DETAIL__DISPLAY_NETWORK_MISSING,
                    String.format("$DisplayNetwork %1$s", displayNetwork.getName()));
        }

        if (displayNic.getIpv4BootProtocol() == Ipv4BootProtocol.NONE
                && displayNic.getIpv6BootProtocol() == Ipv6BootProtocol.NONE) {
            log.warn("Host {} has the display network {} configured with improper boot protocol on interface {}.",
                    host.getName(),
                    displayNetwork.getName(),
                    displayNic.getName());
            return new ValidationResult(EngineMessage.VAR__DETAIL__DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL,
                    String.format("$DisplayNetwork %1$s", displayNetwork.getName()));
        }

        return ValidationResult.VALID;
    }

    private ValidationResult validatePassthroughVnics(Guid vmId, VDS host,
            List<VmNetworkInterface> vnics) {

        VfScheduler vfScheduler = Injector.get(VfScheduler.class);
        List<String> problematicVnics = vfScheduler.validatePassthroughVnics(vmId, host.getId(), vnics);

        if (!problematicVnics.isEmpty()) {
            String vnicsString = StringUtils.join(problematicVnics, ", ");
            log.warn("host {} doesn't contain suitable virtual functions for VM nics {}",
                    host.getName(), vnicsString);
            return new ValidationResult(EngineMessage.VAR__DETAIL__NO_SUITABLE_VF,
                    String.format("$vnicNames %1$s", vnicsString));
        }

        return ValidationResult.VALID;
    }
}
