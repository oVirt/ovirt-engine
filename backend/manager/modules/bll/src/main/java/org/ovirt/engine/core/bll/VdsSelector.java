package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsVersion;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsSelector {
    private final List<Guid> privateRunVdssList = new ArrayList<Guid>();

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

    private VM getVm() {
        return privateVm;
    }

    private void setVm(VM value) {
        privateVm = value;
    }

    public VdsSelector(VM vm, NGuid destinationVdsId, boolean dedicatedFirst) {
        setVm(vm);
        setDestinationVdsId(destinationVdsId);
        setCheckDestinationFirst(dedicatedFirst);
    }

    public Guid GetVdsToRunOn() {
        Guid result = Guid.Empty;
        if (getDestinationVdsId() != null) {
            if (getCheckDestinationFirst()) {
                result = GetVdsRunOnDestination();
                if (result.equals(Guid.Empty) && privateVm.getMigrationSupport() != MigrationSupport.PINNED_TO_HOST) {
                    result = GetAnyVdsToRunOn();
                }
            } else {
                result = GetAnyVdsToRunOn();
                if (result.equals(Guid.Empty)) {
                    result = GetVdsRunOnDestination();
                }
            }
        } else {
            result = GetAnyVdsToRunOn();
        }

        return result;
    }

    public boolean CanFindVdsToRunOn(List<String> messages, boolean isMigrate) {
        boolean returnValue = false;
        if (getDestinationVdsId() != null) {
            returnValue = CanRunOnDestinationVds(messages, isMigrate);
        }

        if (!returnValue) {
            if (privateVm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                if (messages.size() > 0) {
                    messages.set(0, VdcBllMessages.VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS.toString());
                } else {
                    messages.add(VdcBllMessages.VM_PINNED_TO_HOST_CANNOT_RUN_ON_THE_DEFAULT_VDS.toString());
                }
                return false;
            }
            returnValue = CanFindAnyVds(messages, isMigrate);
        }

        return returnValue;
    }

    private Guid GetVdsRunOnDestination() {
        if (getDestinationVdsId() == null) {
            return Guid.Empty;
        }
        final VDS target_vds = getVdsDAO().get(getDestinationVdsId());
        log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                getDestinationVdsId(), target_vds.getvds_name(), target_vds.gethost_name());
        VmHandler.UpdateVmGuestAgentVersion(getVm());
        if (target_vds.getvds_type() == VDSType.PowerClient
                && !Config.<Boolean> GetValue(ConfigValues.PowerClientAllowRunningGuestsWithoutTools)
                && getVm() != null && getVm().getHasAgent()) {
            log.infoFormat(
                    "VdcBLL.RunVmCommandBase.getVdsToRunOn - VM {0} has no tools - skipping power client check",
                    getVm().getId());
            return Guid.Empty;
        } else if (target_vds.getvds_group_id().equals(getVm().getvds_group_id())
                && target_vds.getstatus() != VDSStatus.Up
                && target_vds.getcpu_cores() != null
                && target_vds.getcpu_cores() >= getVm().getnum_of_cpus()) {
            return getVdsToRunOn(Arrays.asList(target_vds));
        }

        return Guid.Empty;
    }

    private Guid GetAnyVdsToRunOn() {
        return getVdsToRunOn(getVdsDAO()
                .getVdsToRun(new VDSType[] { VDSType.VDS, VDSType.oVirtNode },
                        getVm().getvds_group_id(),
                        VDSStatus.Up,
                        getVm().getnum_of_cpus()));
    }

    private boolean CanRunOnDestinationVds(List<String> messages, boolean isMigrate) {
        boolean returnValue = false;
        if (getDestinationVdsId() != null) {
            VDS target_vds = getVdsDAO().get(getDestinationVdsId());
            log.infoFormat("Checking for a specific VDS only - id:{0}, name:{1}, host_name(ip):{2}",
                    getDestinationVdsId(), target_vds.getvds_name(), target_vds.gethost_name());
            returnValue = CanFindVdsToRun(messages, isMigrate,
                    new ArrayList<VDS>(Arrays.asList(new VDS[] { target_vds })));
        }
        return returnValue;
    }

    private boolean CanFindAnyVds(List<String> messages, boolean isMigrate) {
        return CanFindVdsToRun(messages,
                isMigrate,
                getVdsDAO().getVdsToRun(new VDSType[] { VDSType.VDS, VDSType.oVirtNode },
                        getVm().getvds_group_id(),
                        VDSStatus.Up,
                        getVm().getnum_of_cpus()));
    }

    VdsDAO getVdsDAO() {
        return DbFacade.getInstance().getVdsDAO();
    }

    /**
     * This function used in CanDoAction function. Purpose is to check if there
     * are Vds avalable to run vm in CanDoAction - before concrete running
     * action This function goes over all available vdss and check if current
     * vds can run vm. If vds cannot running vm - reason stored. If there is no
     * any vds, avalable too run vm - returning reason with highest value.
     * Reasons sorted in VdcBllMessages by their priorities
     */
    private boolean CanFindVdsToRun(List<String> messages, boolean isMigrate, Iterable<VDS> vdss) {
        VdcBllMessages messageToReturn = VdcBllMessages.Unassigned;

        /**
         * save vdsVersion in order to know vds version that was wrong
         */
        VdsVersion vdsVersion = null;
        boolean noVDSs = true;
        for (VDS curVds : vdss) {
            if (isMigrate && getVm().getrun_on_vds() != null && getVm().getrun_on_vds().equals(curVds.getId())) {
                continue;
            }

            noVDSs = false;

            ValidationResult result = validateHostIsReadyToRun(curVds);
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

        if (noVDSs) {
            if (messages != null) {
                messageToReturn = VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_AVAILABLE_IN_CLUSTER;
            }
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
                messages.add("$serverVersion " + vdsVersion.getPartialVersion());

            }
        }
        return false;
    }

    private ValidationResult validateHostIsReadyToRun(final VDS vds) {
        if ((!vds.getvds_group_id().equals(getVm().getvds_group_id())) || (vds.getstatus() != VDSStatus.Up)
                || isVdsFailedToRunVm(vds.getId())) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER);
        }
        // If Vm in Paused mode - no additional memory allocation needed
        else if (getVm().getstatus() != VMStatus.Paused && !RunVmCommandBase.hasMemoryToRunVM(vds, getVm())) {
            // not enough memory
            // In case we are using this function in migration we make sure we
            // don't allocate the same VDS
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_MEMORY);
        }
        // if vm has more vCpus then vds physical cpus - dont allow to run
        else if (vds.getcpu_cores() != null && getVm().getnum_of_cpus() > vds.getcpu_cores()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS);
        }
        else if (!IsVMSwapValueLegal(vds)) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_SWAP);
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
    private static boolean IsVMSwapValueLegal(VDS vds) {
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

    private Guid getVdsToRunOn(Iterable<VDS> vdss) {
        final List<VDS> readyToRun = new ArrayList<VDS>();
        final List<VmNetworkInterface> vmNICs = getVmNetworkInterfaceDAO().getAllForVm(getVm().getId());
        for (VDS curVds : vdss) {

            // apply limit on vds memory over commit.
            if (!RunVmCommandBase.hasMemoryToRunVM(curVds, getVm())) {
                continue;
            }

            // In case we are using this function in migration we make sure we
            // don't allocate the same VDS
            if ((getVm().getrun_on_vds() != null && getVm().getrun_on_vds().equals(curVds.getId()))
                    || isVdsFailedToRunVm(curVds.getId()) ||
                    // RunVmCommandBase.isVdsVersionOld(curVds, getVm()) ||
                    !RunVmCommandBase.hasCapacityToRunVM(curVds)) {
                continue;
            }

            if (!IsVMSwapValueLegal(curVds)) {
                continue;
            }

            if(!areRequiredNetworksAvailable(vmNICs, getInterfaceDAO().getAllInterfacesForVds(curVds.getId()))) {
                continue;
            }

            readyToRun.add(curVds);
        }

        return readyToRun.isEmpty() ? Guid.Empty : getBestVdsToRun(readyToRun);
    }

    boolean areRequiredNetworksAvailable(final List<VmNetworkInterface> vmNetworkInterfaces,
            final List<VdsNetworkInterface> allInterfacesForVds) {
        for (final VmNetworkInterface vmIf : vmNetworkInterfaces) {
            boolean found = false;
            for (final VdsNetworkInterface vdsIf : allInterfacesForVds) {
                if (StringUtils.equals(vmIf.getNetworkName(), vdsIf.getNetworkName())) {
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

    VmNetworkInterfaceDAO getVmNetworkInterfaceDAO() {
        return DbFacade.getInstance().getVmNetworkInterfaceDAO();
    }

    InterfaceDAO getInterfaceDAO() {
        return DbFacade.getInstance().getInterfaceDAO();
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

    private static Log log = LogFactory.getLog(VdsSelector.class);
}
