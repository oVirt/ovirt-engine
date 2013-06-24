package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsSelector {
    private List<VmNetworkInterface> vmNICs;
    private boolean displayNetworkInitialized;
    private Network displayNetwork;

    private Guid privateDestinationVdsId;

    public Guid getDestinationVdsId() {
        return privateDestinationVdsId;
    }

    public void setDestinationVdsId(Guid value) {
        privateDestinationVdsId = value;
    }

    private VM privateVm;
    private final VdsFreeMemoryChecker memoryChecker;

    private VM getVm() {
        return privateVm;
    }

    private void setVm(VM value) {
        privateVm = value;
    }

    public VdsSelector(VM vm, Guid destinationVdsId, VdsFreeMemoryChecker memoryChecker) {
        setVm(vm);
        setDestinationVdsId(destinationVdsId);
        this.memoryChecker = memoryChecker;
    }

    public Guid getVdsToRunOn(List<VDS> vdsList,
            List<Guid> vdsBlackList,
            boolean isMigrate) {
        Guid result = Guid.Empty;
        if (getDestinationVdsId() != null) {
            VDS targetVds = null;
            if (vdsList != null) {
                for (VDS vds : vdsList) {
                    if (vds.getId().equals(getDestinationVdsId())) {
                        targetVds = vds;
                        break;
                    }
                }
            }
            if (targetVds != null) {
                log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                        getDestinationVdsId(), targetVds.getName(), targetVds.getHostName());
                result =
                        filterBestVds(new ArrayList<VDS>(Arrays.asList(new VDS[] { targetVds })),
                                isMigrate,
                                vdsBlackList);
            }
        }
        if (getDestinationVdsId() == null || result.equals(Guid.Empty)
                && privateVm.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
            result = filterBestVds(vdsList, isMigrate, vdsBlackList);
        }

        return result;
    }

    public boolean canFindVdsToRunOn(List<VDS> vdsList,
            List<Guid> vdsBlackList,
            List<String> messages,
            boolean isMigrate) {
        boolean returnValue = false;
        if (getDestinationVdsId() != null) {
            VDS targetVds = null;
            if (vdsList != null) {
                for (VDS vds : vdsList) {
                    if (vds.getId().equals(getDestinationVdsId())) {
                        targetVds = vds;
                        break;
                    }
                }
            }

            if (targetVds != null) {
                log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                        getDestinationVdsId(), targetVds.getName(), targetVds.getHostName());
                returnValue = canFindVdsToRun(messages, isMigrate,
                        new ArrayList<VDS>(Arrays.asList(targetVds)), vdsBlackList);
            }
            if (!returnValue && privateVm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                messages.add(VdcBllMessages.VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS.toString());
                messages.add(targetVds == null ? VdcBllMessages.HOST_NAME_NOT_AVAILABLE.toString()
                        : String.format("$VdsName %1$s", targetVds.getName()));

                return false;
            }
        }

        if (!returnValue) {
            returnValue = canFindVdsToRun(messages, isMigrate,
                    vdsList, vdsBlackList);
        }

        return returnValue;
    }

    /**
     * This function used in CanDoAction function. Purpose is to check if there
     * are Vds available to run vm in CanDoAction - before concrete running
     * action This function goes over all available vdss and check if current
     * vds can run vm. If vds cannot running vm - reason stored. If there is no
     * any vds, available too run vm - returning reason with highest value.
     * Reasons sorted in VdcBllMessages by their priorities
     */
    private boolean canFindVdsToRun(List<String> messages,
            boolean isMigrate,
            Iterable<VDS> vdss,
            List<Guid> vdsBlackList) {
        VdcBllMessages messageToReturn = VdcBllMessages.Unassigned;

        /**
         * save vdsVersion in order to know vds version that was wrong
         */
        RpmVersion vdsVersion = null;
        boolean noVDSs = true;
        StringBuilder sb = new StringBuilder();
        for (VDS curVds : vdss) {
            noVDSs = false;

            VdcBllMessages result = validateHostIsReadyToRun(curVds, sb, isMigrate, vdsBlackList);
            if (result == null) {
                return true;
            } else {
                messageToReturn = result;
                /**
                 * save version of current vds for later use
                 */
                vdsVersion = curVds.getVersion();
            }
        }

        if (noVDSs && messages != null) {
            messageToReturn = VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_AVAILABLE_IN_CLUSTER;
        }

        if (messages != null) {
            messages.add(messageToReturn.toString());
            /**
             * if error due to versions, add versions information to can do
             * action message
             */
            if (messageToReturn == VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_VERSION && vdsVersion != null) {
                messages.add("$toolsVersion " + getVm().getPartialVersion());
                messages.add("$serverVersion " + vdsVersion.getRpmName());

            }
        }
        log.info(sb.toString());
        return false;
    }

    interface HostValidator {
        VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList);
    }

    @SuppressWarnings("serial")
    final List<HostValidator> hostValidators = Collections.unmodifiableList(new ArrayList<HostValidator>(){
        {
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    if ((!vds.getVdsGroupId().equals(getVm().getVdsGroupId())) || (vds.getStatus() != VDSStatus.Up)) {
                        sb.append("is not in up status or belongs to the VM's cluster");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    // If Vm in Paused mode - no additional memory allocation needed
                    if (getVm().getStatus() != VMStatus.Paused && !memoryChecker.evaluate(vds, getVm())) {
                        // not enough memory
                        sb.append("has insufficient memory to run the VM");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_MEMORY;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    // In case we are using this function in migration we make sure we
                    // don't allocate the same VDS
                    if (isMigrate && (getVm().getRunOnVds() != null && getVm().getRunOnVds().equals(vds.getId()))) {
                        sb.append("is the same host the VM is currently running on");
                        return VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_TO_SAME_HOST;
                    }
                    return null;
                }
            });
            add(new HostValidator() {
                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    Integer cores = SlaValidator.getEffectiveCpuCores(vds);
                    if (cores != null && getVm().getNumOfCpus() > cores) {
                        sb.append("has less cores(").append(cores).append(") than ").append(getVm().getNumOfCpus());
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    // if vm has more vCpus then vds physical cpus - dont allow to run
                    if (!isVMSwapValueLegal(vds)) {
                        sb.append("swap value is illegal");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_SWAP;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    VdcBllMessages returnValue = validateRequiredNetworksAvailable(vds);
                    if (VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS == returnValue) {
                        sb.append("is missing networks required by VM nics ").append(Entities.interfacesByNetworkName(getVmNICs())
                                        .keySet());
                    } else if (VdcBllMessages.ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK == returnValue) {
                        sb.append("is missing the cluster's display network");
                    }
                    return returnValue;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate, List<Guid> vdsBlackList) {
                    if (vdsBlackList != null && vdsBlackList.contains(vds.getId())) {
                        sb.append("have failed running this VM in the current selection cycle");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER;
                    }
                    return null;
                }
            });
        }
    });

    private VdcBllMessages validateHostIsReadyToRun(final VDS vds,
            StringBuilder sb,
            boolean isMigrate,
            List<Guid> vdsBlackList) {
        // buffer the mismatches as we go
        sb.append(" VDS ").append(vds.getName()).append(" ").append(vds.getId()).append(" ");

        for(HostValidator validator : this.hostValidators) {
            VdcBllMessages result = validator.validate(vds, sb, isMigrate, vdsBlackList);
            if(result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Determines whether [is VM swap value legal] [the specified VDS].
     * @param vds
     *            The VDS.
     * @return <c>true</c> if [is VM swap value legal] [the specified VDS]; otherwise, <c>false</c>.
     */
    private static boolean isVMSwapValueLegal(VDS vds) {
        if (!Config.<Boolean> GetValue(ConfigValues.EnableSwapCheck)) {
            return true;
        }

        if (vds.getSwapTotal() == null || vds.getSwapFree() == null || vds.getMemAvailable() == null
                || vds.getMemAvailable() <= 0 || vds.getPhysicalMemMb() == null || vds.getPhysicalMemMb() <= 0) {
            return true;
        }

        long swap_total = vds.getSwapTotal();
        long swap_free = vds.getSwapFree();
        long mem_available = vds.getMemAvailable();
        long physical_mem_mb = vds.getPhysicalMemMb();

        return ((swap_total - swap_free - mem_available) * 100 / physical_mem_mb) <= Config
                .<Integer> GetValue(ConfigValues.BlockMigrationOnSwapUsagePercentage);
    }

    /**
     * Determines whether the cluster's display network is defined on the host.
     *
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

    private Guid filterBestVds(Iterable<VDS> vdss, boolean isMigrate, List<Guid> vdsBlackList) {
        StringBuilder sb = new StringBuilder();
        final List<VDS> readyToRun = new ArrayList<VDS>();
        for (VDS curVds : vdss) {
            if (validateHostIsReadyToRun(curVds, sb, isMigrate, vdsBlackList) == null) {
                readyToRun.add(curVds);
            }
        }

        if (readyToRun.isEmpty()) {
            log.info(sb.toString());
            return Guid.Empty;
        } else {
            if (log.isDebugEnabled()) {
                log.debug(sb.toString());
            }
            return getBestVdsToRun(readyToRun);
        }
    }

    /**
     * Determine whether all required Networks are attached to the Host's Nics. A required Network, depending on
     * ConfigValue.OnlyRequiredNetworksMandatoryForVdsSelection, is defined as: 1. false: any network that is defined on
     * an Active vNic of the VM or the cluster's display network. 2. true: a Cluster-Required Network that is defined on
     * an Active vNic of the VM.
     *
     * @param vdsId
     *            The Host id.
     * @return <code>VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS</code> if a required network on an active vnic is
     *         not attached to the host.<br>
     *         <code>VdcBllMessages.ACTION_TYPE_FAILED_MISSING_DISPLAY_NETWORK</code> if the cluster's display network
     *         is required and not attached to the host.<br>
     *         Otherwise, <code>null</code>.
     */
    private VdcBllMessages validateRequiredNetworksAvailable(VDS vds) {
        final List<VdsNetworkInterface> allInterfacesForVds = getInterfaceDAO().getAllInterfacesForVds(vds.getId());
        final List<Network> clusterNetworks = getNetworkDAO().getAllForCluster(vds.getVdsGroupId());
        final Map<String, Network> networksByName = Entities.entitiesByName(clusterNetworks);

        boolean onlyRequiredNetworks =
                Config.<Boolean> GetValue(ConfigValues.OnlyRequiredNetworksMandatoryForVdsSelection);
        for (final VmNetworkInterface vmIf : getVmNICs()) {
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

    private NetworkDao getNetworkDAO() {
        return DbFacade.getInstance().getNetworkDao();
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

    VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    InterfaceDao getInterfaceDAO() {
        return DbFacade.getInstance().getInterfaceDao();
    }

    private Guid getBestVdsToRun(List<VDS> list) {
        VdsComparer comparer = VdsComparer.CreateComparer(list.get(0).getSelectionAlgorithm());
        VDS bestVDS = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            VDS curVds = list.get(i);
            if (comparer.isBetter(bestVDS, curVds, getVm()))
            // if (((bestVDS.physical_mem_mb - bestVDS.mem_commited) <
            // (curVds.physical_mem_mb - curVds.mem_commited)))
            {
                bestVDS = curVds;
            }
        }
        /**
         * add chosen vds to running vdss list.
         */
        comparer.bestVdsProcedure(bestVDS);
        return bestVDS.getId();
    }

    private List<VmNetworkInterface> getVmNICs() {
        if (vmNICs == null) {
            vmNICs = getVmNetworkInterfaceDao().getAllForVm(getVm().getId());
        }
        return vmNICs;
    }

    private static final Log log = LogFactory.getLog(VdsSelector.class);
}
