package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsSelector {
    private final List<Guid> privateRunVdssList = new ArrayList<Guid>();
    private List<VmNetworkInterface> vmNICs;

    public List<Guid> getRunVdssList() {
        return privateRunVdssList;
    }

    private boolean privateCheckDestinationFirst;

    public boolean getCheckDestinationFirst() {
        return privateCheckDestinationFirst;
    }

    public void setCheckDestinationFirst(boolean value) {
        privateCheckDestinationFirst = value;
    }

    private NGuid privateDestinationVdsId;

    public NGuid getDestinationVdsId() {
        return privateDestinationVdsId;
    }

    public void setDestinationVdsId(NGuid value) {
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

    public VdsSelector(VM vm, NGuid destinationVdsId, boolean dedicatedFirst, VdsFreeMemoryChecker memoryChecker) {
        setVm(vm);
        setDestinationVdsId(destinationVdsId);
        setCheckDestinationFirst(dedicatedFirst);
        this.memoryChecker = memoryChecker;
    }

    public Guid getVdsToRunOn(boolean isMigrate) {
        Guid result = Guid.Empty;
        if (getDestinationVdsId() != null) {
            if (getCheckDestinationFirst()) {
                result = getVdsRunOnDestination(isMigrate);
                if (result.equals(Guid.Empty) && privateVm.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
                    result = getAnyVdsToRunOn(isMigrate);
                }
            } else {
                result = getAnyVdsToRunOn(isMigrate);
                if (result.equals(Guid.Empty)) {
                    result = getVdsRunOnDestination(isMigrate);
                }
            }
        } else {
            result = getAnyVdsToRunOn(isMigrate);
        }

        return result;
    }

    public boolean canFindVdsToRunOn(List<String> messages, boolean isMigrate) {
        boolean returnValue = false;
        if (getDestinationVdsId() != null) {
            returnValue = canRunOnDestinationVds(messages, isMigrate);
        }

        if (!returnValue) {
            if (privateVm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                // we are checking for 2 because the first 2 (0 and 1) are the messaged for
                // object and action type and we don't want to override them,
                // we do override any previous message because this one has priority.
                if (messages.size() > 2) {
                    messages.set(2, VdcBllMessages.VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS.toString());
                } else {
                    messages.add(VdcBllMessages.VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS.toString());
                }
                return false;
            }
            returnValue = canFindAnyVds(messages, isMigrate);
        }

        return returnValue;
    }

    /**
     * Get the ID of the VDS.
     * getDestinationVdsId() must not be null.
     * @return
     */
    private Guid getVdsRunOnDestination(boolean isMigrate) {
        Guid result = Guid.Empty;
        if (getDestinationVdsId() != null) {
            VDS target_vds = DbFacade.getInstance().getVdsDao().get(getDestinationVdsId());
            log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                    getDestinationVdsId(), target_vds.getvds_name(), target_vds.gethost_name());
            VmHandler.UpdateVmGuestAgentVersion(getVm());
            if (target_vds.getvds_type() == VDSType.PowerClient
                    && !Config.<Boolean> GetValue(ConfigValues.PowerClientAllowRunningGuestsWithoutTools)
                    && getVm() != null && getVm().getHasAgent()) {
                log.infoFormat(
                        "VdcBLL.RunVmCommandBase.getVdsToRunOn - VM {0} has no tools - skipping power client check",
                        getVm().getId());
            } else {
                result = getVdsToRunOn(new ArrayList<VDS>(Arrays.asList(new VDS[] { target_vds })), isMigrate);
            }
        }
        return result;
    }

    private Guid getAnyVdsToRunOn(boolean isMigrate) {
        return getVdsToRunOn(DbFacade.getInstance()
                .getVdsDao()
                .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode }), isMigrate);
    }

    private boolean canRunOnDestinationVds(List<String> messages, boolean isMigrate) {
        boolean returnValue = false;
        if (getDestinationVdsId() != null) {
            VDS target_vds = DbFacade.getInstance().getVdsDao().get(getDestinationVdsId());
            log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                    getDestinationVdsId(), target_vds.getvds_name(), target_vds.gethost_name());
            returnValue = canFindVdsToRun(messages, isMigrate,
                    new ArrayList<VDS>(Arrays.asList(target_vds)));
        }
        return returnValue;
    }

    private boolean canFindAnyVds(List<String> messages, boolean isMigrate) {
        return canFindVdsToRun(messages, isMigrate,
                DbFacade.getInstance().getVdsDao().getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode }));
    }

    /**
     * This function used in CanDoAction function. Purpose is to check if there
     * are Vds avalable to run vm in CanDoAction - before concrete running
     * action This function goes over all available vdss and check if current
     * vds can run vm. If vds cannot running vm - reason stored. If there is no
     * any vds, avalable too run vm - returning reason with highest value.
     * Reasons sorted in VdcBllMessages by their priorities
     */
    private boolean canFindVdsToRun(List<String> messages, boolean isMigrate, Iterable<VDS> vdss) {
        VdcBllMessages messageToReturn = VdcBllMessages.Unassigned;

        /**
         * save vdsVersion in order to know vds version that was wrong
         */
        RpmVersion vdsVersion = null;
        boolean noVDSs = true;
        StringBuilder sb = new StringBuilder();
        for (VDS curVds : vdss) {
            noVDSs = false;

            ValidationResult result = validateHostIsReadyToRun(curVds, sb, isMigrate);
            if (result.isValid()) {
                return true;
            } else {
                if (messageToReturn.getValue() < result.getMessage().getValue()) {
                    messageToReturn = result.getMessage();
                    /**
                     * save version of current vds for later use
                     */
                    vdsVersion = curVds.getVersion();
                }
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
                VmHandler.UpdateVmGuestAgentVersion(getVm());
                messages.add("$toolsVersion " + getVm().getPartialVersion());
                messages.add("$serverVersion " + vdsVersion.getRpmName());

            }
        }
        log.info(sb.toString());
        return false;
    }

    interface HostValidator {
        VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate);
    }

    @SuppressWarnings("serial")
    final List<HostValidator> hostValidators = Collections.unmodifiableList(new ArrayList<HostValidator>(){
        {
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
                    if ((!vds.getvds_group_id().equals(getVm().getVdsGroupId())) || (vds.getstatus() != VDSStatus.Up)) {
                        sb.append("is not in up status or belongs to the VM's cluster");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
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
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
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
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
                    // check capacity to run power clients
                    if (!RunVmCommandBase.hasCapacityToRunVM(vds)) {
                        sb.append("has insuffient capacity to run power client");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS;
                    }
                    return null;
                }
            });
            add(new HostValidator() {
                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
                    if (vds.getcpu_cores() != null && getVm().getNumOfCpus() > vds.getcpu_cores()) {
                        sb.append("has less cores(").append(vds.getcpu_cores()).append(") than ").append(getVm().getNumOfCpus());
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
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
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
                    if (!areRequiredNetworksAvailable(vds)) {
                        sb.append("is missing networks required by VM nics ").append(Entities.interfacesByNetworkName(getVmNICs())
                                .keySet());
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_NETWORKS;
                    }
                    return null;
                }
            });
            add(new HostValidator() {

                @Override
                public VdcBllMessages validate(VDS vds, StringBuilder sb, boolean isMigrate) {
                    if (isVdsFailedToRunVm(vds.getId())) {
                        sb.append("have failed running this VM in the current selection cycle");
                        return VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER;
                    }
                    return null;
                }
            });
        }
    });

    private ValidationResult validateHostIsReadyToRun(final VDS vds, StringBuilder sb, boolean isMigrate) {
        // buffer the mismatches as we go
        sb.append(" VDS ").append(vds.getvds_name()).append(" ").append(vds.getId()).append(" ");

        for(HostValidator validator : this.hostValidators) {
            VdcBllMessages result = validator.validate(vds, sb, isMigrate);
            if(result != null) {
                return new ValidationResult(result);
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Determine if specific vds already failed to run vm - to prevent
     * sequentual running of vm on problematic vds
     *
     * @param vdsId
     * @return
     */
    private boolean isVdsFailedToRunVm(Guid vdsId) {
        boolean retValue = false;
        if (getRunVdssList() != null && getRunVdssList().contains(vdsId)) {
            retValue = true;
        }
        return retValue;
    }

    /**
     * Determines whether [is VM swap value legal] [the specified VDS].
     *
     * @param vds
     *            The VDS.
     * @return <c>true</c> if [is VM swap value legal] [the specified VDS];
     *         otherwise, <c>false</c>.
     */
    private static boolean isVMSwapValueLegal(VDS vds) {
        if (!Config.<Boolean> GetValue(ConfigValues.EnableSwapCheck)) {
            return true;
        }

        if (vds.getswap_total() == null || vds.getswap_free() == null || vds.getmem_available() == null
                || vds.getmem_available() <= 0 || vds.getphysical_mem_mb() == null || vds.getphysical_mem_mb() <= 0) {
            return true;
        }

        long swap_total = vds.getswap_total();
        long swap_free = vds.getswap_free();
        long mem_available = vds.getmem_available();
        long physical_mem_mb = vds.getphysical_mem_mb();

        return ((swap_total - swap_free - mem_available) * 100 / physical_mem_mb) <= Config
                .<Integer> GetValue(ConfigValues.BlockMigrationOnSwapUsagePercentage);
    }

    private Guid getVdsToRunOn(Iterable<VDS> vdss, boolean isMigrate) {
        StringBuilder sb = new StringBuilder();
        final List<VDS> readyToRun = new ArrayList<VDS>();
        for (VDS curVds : vdss) {
            if (validateHostIsReadyToRun(curVds, sb, isMigrate) == ValidationResult.VALID) {
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
    * ConfigValue.OnlyRequiredNetworksMandatoryForVdsSelection, is defined as:
    * 1. false: any network that is defined on an Active vNic of the VM.
    * 2. true: a Cluster-Required Network that is defined on an Active vNic of the VM.
    * @param vdsId
    *            The Host id.
    * @return <code>true</code> if all required Networks are attached to a Host Nic, otherwise, <code>false</code>.
    */
    private boolean areRequiredNetworksAvailable(VDS vds) {
        final List<VdsNetworkInterface> allInterfacesForVds = getInterfaceDAO().getAllInterfacesForVds(vds.getId());
        final List<Network> clusterNetworks = getNetworkDAO().getAllForCluster(vds.getvds_group_id());
        final Map<String, Network> networksByName = Entities.entitiesByName(clusterNetworks);

        boolean onlyRequiredNetworks =
                Config.<Boolean> GetValue(ConfigValues.OnlyRequiredNetworksMandatoryForVdsSelection);
        for (final VmNetworkInterface vmIf : getVmNICs()) {
            boolean found = false;
            for (final VdsNetworkInterface vdsIf : allInterfacesForVds) {
                if (!networkRequiredOnVds(vmIf, networksByName, onlyRequiredNetworks)
                        || StringUtils.equals(vmIf.getNetworkName(), vdsIf.getNetworkName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private NetworkDAO getNetworkDAO() {
        return DbFacade.getInstance().getNetworkDao();
    }

    private boolean networkRequiredOnVds(VmNetworkInterface vmIface,
            Map<String, Network> networksByName,
            boolean onlyRequiredNetworks) {
        boolean networkRequiredOnVds = true;
        if (!vmIface.isActive()) {
            networkRequiredOnVds = false;
        } else if (onlyRequiredNetworks) {
            networkRequiredOnVds = networksByName.get(vmIface.getNetworkName()).getCluster().isRequired();
        }
        return networkRequiredOnVds;
    }

    VmNetworkInterfaceDAO getVmNetworkInterfaceDAO() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    InterfaceDAO getInterfaceDAO() {
        return DbFacade.getInstance().getInterfaceDao();
    }

    private Guid getBestVdsToRun(List<VDS> list) {
        VdsComparer comparer = VdsComparer.CreateComparer(list.get(0).getselection_algorithm());
        VDS bestVDS = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            VDS curVds = list.get(i);
            if (comparer.IsBetter(bestVDS, curVds, getVm()))
            // if (((bestVDS.physical_mem_mb - bestVDS.mem_commited) <
            // (curVds.physical_mem_mb - curVds.mem_commited)))
            {
                bestVDS = curVds;
            }
        }
        /**
         * add chosen vds to running vdss list.
         */
        comparer.BestVdsProcedure(bestVDS);
        getRunVdssList().add(bestVDS.getId());
        return bestVDS.getId();
    }

    private List<VmNetworkInterface> getVmNICs() {
        if (vmNICs == null) {
            vmNICs = getVmNetworkInterfaceDAO().getAllForVm(getVm().getId());
        }
        return vmNICs;
    }

    private static final Log log = LogFactory.getLog(VdsSelector.class);
}
