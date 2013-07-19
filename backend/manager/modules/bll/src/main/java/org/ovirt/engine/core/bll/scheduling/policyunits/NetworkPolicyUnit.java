package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;

public class NetworkPolicyUnit extends PolicyUnitImpl {
    public NetworkPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    private boolean displayNetworkInitialized;
    private Network displayNetwork;

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, List<String> messages) {
        List<VDS> toRemoveHostList = new ArrayList<VDS>();
        List<VmNetworkInterface> vmNICs;
        List<Network> clusterNetworks;
        Map<String, Network> networksByName;
        if (hosts != null && hosts.size() > 0) {
            clusterNetworks = getNetworkDAO().getAllForCluster(hosts.get(0).getVdsGroupId());
            networksByName = Entities.entitiesByName(clusterNetworks);
            vmNICs = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
        } else {
            return null;
        }
        for (VDS host : hosts) {
            VdcBllMessages returnValue =
                    validateRequiredNetworksAvailable(host, vm, vmNICs, clusterNetworks, networksByName);
            if (VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS == returnValue) {
                StringBuilder sbBuilder = new StringBuilder();
                sbBuilder.append(Entities.vmInterfacesByNetworkName(vmNICs).keySet());
                log.debugFormat("host {0} is missing networks required by VM nics {1}",
                        host.getName(),
                        sbBuilder.toString());
            } else if (VdcBllMessages.ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK == returnValue) {
                log.debugFormat("host {0} is missing the cluster's display network", host.getName());
            }
            if (returnValue != null) {
                messages.add(returnValue.toString());
                toRemoveHostList.add(host);
            }
        }
        hosts.removeAll(toRemoveHostList);
        return hosts;
    }

    /**
     * Determine whether all required Networks are attached to the Host's Nics. A required Network, depending on
     * ConfigValue.OnlyRequiredNetworksMandatoryForVdsSelection, is defined as: 1. false: any network that is defined on
     * an Active vNic of the VM or the cluster's display network. 2. true: a Cluster-Required Network that is defined on
     * an Active vNic of the VM.
     * @param networksByName
     * @param clusterNetworks
     * @param vdsId
     *            The Host id.
     * @return <code>VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS</code> if a required network on an active vnic is
     *         not attached to the host.<br>
     *         <code>VdcBllMessages.ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK</code> if the cluster's display network
     *         is required and not attached to the host.<br>
     *         Otherwise, <code>null</code>.
     */
    private VdcBllMessages validateRequiredNetworksAvailable(VDS vds,
            VM vm,
            List<VmNetworkInterface> vmNICs,
            List<Network> clusterNetworks,
            Map<String, Network> networksByName) {
        // TODO: retrieve interfaces by cluster
        final List<VdsNetworkInterface> allInterfacesForVds = getInterfaceDAO().getAllInterfacesForVds(vds.getId());

        boolean onlyRequiredNetworks =
                Config.<Boolean> GetValue(ConfigValues.OnlyRequiredNetworksMandatoryForVdsSelection);
        for (final VmNetworkInterface vmIf : vmNICs) {
            boolean found = false;

            if (vmIf.getNetworkName() == null) {
                found = true;
            } else {
                for (final VdsNetworkInterface vdsIf : allInterfacesForVds) {
                    if (!networkRequiredOnVds(vmIf, networksByName, onlyRequiredNetworks)
                            || StringUtils.equals(vmIf.getNetworkName(), vdsIf.getNetworkName())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS;
            }
        }

        if (!isDisplayNetworkAvailable(vds, onlyRequiredNetworks, allInterfacesForVds, clusterNetworks)) {
            return VdcBllMessages.ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK;
        }

        return null;
    }

    private boolean networkRequiredOnVds(VmNetworkInterface vmIface,
            Map<String, Network> networksByName,
            boolean onlyRequiredNetworks) {
        boolean networkRequiredOnVds = true;
        if (!vmIface.isPlugged()) {
            networkRequiredOnVds = false;
        } else if (onlyRequiredNetworks) {
            networkRequiredOnVds = networksByName.get(vmIface.getNetworkName()).getCluster().isRequired();
        }
        return networkRequiredOnVds;
    }

    /**
     * Determines whether the cluster's display network is defined on the host.
     * @param host
     *            The host.
     * @return <c>true</c> if the cluster's display network is defined on the host or
     *         ConfigValue.OnlyRequiredNetworksMandatoryForVdsSelection is true; otherwise, <c>false</c>.
     */
    private boolean isDisplayNetworkAvailable(VDS host,
            boolean onlyRequiredNetworks,
            List<VdsNetworkInterface> allInterfacesForVds,
            List<Network> allNetworksInCluster) {
        if (onlyRequiredNetworks) {
            return true;
        }

        if (!displayNetworkInitialized) {
            resolveClusterDisplayNetwork(host, allNetworksInCluster);
        }

        if (displayNetwork == null) {
            return true;
        }

        // Check if display network attached to host
        for (VdsNetworkInterface nic : allInterfacesForVds) {
            if (displayNetwork.getName().equals(nic.getNetworkName())) {
                return true;
            }
        }

        return false;
    }

    private void resolveClusterDisplayNetwork(VDS host, List<Network> allNetworksInCluster) {
        // Find the cluster's display network
        for (Network tempNetwork : allNetworksInCluster) {
            if (tempNetwork.getCluster().isDisplay()) {
                displayNetwork = tempNetwork;
                break;
            }
        }

        displayNetworkInitialized = true;
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
