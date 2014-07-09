package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(NetworkPolicyUnit.class);

    public NetworkPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        if (hosts == null || hosts.isEmpty()) {
            return null;
        }

        List<VDS> toRemoveHostList = new ArrayList<VDS>();
        List<VmNetworkInterface> vmNICs = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        Guid clusterId = hosts.get(0).getVdsGroupId();
        List<Network> clusterNetworks = getNetworkDAO().getAllForCluster(clusterId);
        Map<String, Network> networksByName = Entities.entitiesByName(clusterNetworks);
        Map<Guid, List<String>> hostNics = getInterfaceDAO().getHostNetworksByCluster(clusterId);
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

            if (!result.isValid()) {
                toRemoveHostList.add(host);
                messages.addMessages(host.getId(), result.getVariableReplacements());
                messages.addMessage(host.getId(), result.getMessage().name());
            }
        }
        hosts.removeAll(toRemoveHostList);
        return hosts;
    }

    public Map<Guid, VdsNetworkInterface> getDisplayNics(Network displayNetwork) {
        Map<Guid, VdsNetworkInterface> displayNics = new HashMap<>();
        if (displayNetwork != null) {
            List<VdsNetworkInterface> nics = getInterfaceDAO().getVdsInterfacesByNetworkId(displayNetwork.getId());
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
     *
     * @param vds
     *            the Host
     * @param vm
     *            the VM
     * @param vmNICs
     * @param displayNetwork
     * @param networksByName
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

            if (vmIf.getNetworkName() == null) {
                found = true;
            } else {
                for (String networkName : hostNetworks) {
                    if (!networkRequiredOnVds(vmIf, networksByName, onlyRequiredNetworks)
                            || StringUtils.equals(vmIf.getNetworkName(), networkName)) {
                        found = true;
                        break;
                    }
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
            return new ValidationResult(VdcBllMessages.VAR__DETAIL__NETWORK_MISSING,
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
            return new ValidationResult(VdcBllMessages.VAR__DETAIL__DISPLAY_NETWORK_MISSING,
                    String.format("$DisplayNetwork %1$s", displayNetwork.getName()));
        }

        if (displayNic.getBootProtocol() == NetworkBootProtocol.NONE) {
            log.warn("Host {} has the display network {} configured with improper boot protocol on interface {}.",
                    host.getName(),
                    displayNetwork.getName(),
                    displayNic.getName());
            return new ValidationResult(VdcBllMessages.VAR__DETAIL__DISPLAY_NETWORK_HAS_NO_BOOT_PROTOCOL,
                    String.format("$DisplayNetwork %1$s", displayNetwork.getName()));
        }

        return ValidationResult.VALID;
    }

    private VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    private InterfaceDao getInterfaceDAO() {
        return DbFacade.getInstance().getInterfaceDao();
    }

    private NetworkDao getNetworkDAO() {
        return DbFacade.getInstance().getNetworkDao();
    }
}
