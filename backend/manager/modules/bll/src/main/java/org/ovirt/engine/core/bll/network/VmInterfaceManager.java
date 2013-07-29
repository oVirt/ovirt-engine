package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Helper class to use for adding/removing {@link VmNic}s.
 */
public class VmInterfaceManager {

    private Log log = LogFactory.getLog(getClass());

    /**
     * Add a {@link VmNic} to the VM. Allocates a MAC from the {@link MacPoolManager} if necessary, otherwise, if
     * {@code ConfigValues.HotPlugEnabled} is true, forces adding the MAC address to the {@link MacPoolManager}. If
     * HotPlug is not enabled tries to add the {@link VmNic}'s MAC address to the {@link MacPoolManager}, and throws a
     * {@link VdcBllException} if it fails.
     *
     * @param iface
     *            The interface to save.
     * @param compensationContext
     *            Used to snapshot the saved entities.
     * @param clusterCompatibilityVersion
     *            the compatibility version of the cluster
     * @return <code>true</code> if the MAC wasn't used, <code>false</code> if it was.
     */
    public void add(final VmNic iface, CompensationContext compensationContext, boolean allocateMac,
            Version clusterCompatibilityVersion) {

        if (allocateMac) {
            iface.setMacAddress(getMacPoolManager().allocateNewMac());
        } else if (FeatureSupported.hotPlug(clusterCompatibilityVersion)) {
            getMacPoolManager().forceAddMac(iface.getMacAddress());
        } else if (!getMacPoolManager().addMac(iface.getMacAddress())) {
            auditLogMacInUse(iface);
            throw new VdcBLLException(VdcBllErrors.MAC_ADDRESS_IS_IN_USE);
        }

        getVmNicDao().save(iface);
        getVmNetworkStatisticsDao().save(iface.getStatistics());
        compensationContext.snapshotNewEntity(iface);
        compensationContext.snapshotNewEntity(iface.getStatistics());
    }

    public void auditLogMacInUse(final VmNic iface) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                AuditLogableBase logable = createAuditLog(iface);
                log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE);
                log.warnFormat("Network Interface {0} has MAC address {1} which is in use, " +
                        "therefore the action for VM {2} failed.", iface.getName(), iface.getMacAddress(),
                        iface.getVmId());
                return null;
            }
        });
    }

    public void auditLogMacInUseUnplug(final VmNic iface) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                AuditLogableBase logable = createAuditLog(iface);
                log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE_UNPLUG);
                log.warnFormat("Network Interface {0} has MAC address {1} which is in use, " +
                        "therefore it is being unplugged from VM {2}.", iface.getName(), iface.getMacAddress(),
                        iface.getVmId());
                return null;
            }
        });
    }

    /**
     * Remove all {@link VmNic}s from the VM, and remove the Mac addresses from {@link MacPoolManager}.
     *
     * @param vmId
     *            The ID of the VM to remove from.
     */
    public void removeAll(Guid vmId) {
        List<VmNic> interfaces = getVmNicDao().getAllForVm(vmId);
        if (interfaces != null) {
            for (VmNic iface : interfaces) {
                getMacPoolManager().freeMac(iface.getMacAddress());
                getVmNicDao().remove(iface.getId());
                getVmNetworkStatisticsDao().remove(iface.getId());
            }
        }
    }

    /**
     * Finds active VMs which uses a network from a given networks list
     *
     * @param vdsId
     *            The host id on which VMs are running
     * @param networks
     *            the networks to check if used
     * @return A list of VM names which uses the networks
     */
    public List<String> findActiveVmsUsingNetworks(Guid vdsId, List<String> networks) {
        List<VM> runningVms = getVmDAO().getAllRunningForVds(vdsId);
        List<String> vmNames = new ArrayList<String>();
        for (VM vm : runningVms) {
            List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
            for (VmNetworkInterface vmNic : vmInterfaces) {
                if (vmNic.getNetworkName() != null && networks.contains(vmNic.getNetworkName())) {
                    vmNames.add(vm.getName());
                    break;
                }
            }
        }
        return vmNames;
    }

    /***
     * Returns whether or not there is a plugged network interface with the same MAC address as the given interface
     *
     * @param interfaceToPlug
     *            the network interface that needs to be plugged
     * @return <code>true</code> if the MAC is used by another plugged network interface, <code>false</code> otherwise.
     */
    public boolean existsPluggedInterfaceWithSameMac(VmNic interfaceToPlug) {
        List<VmNic> vmNetworkIntrefaces = getVmNicDao().getPluggedForMac(interfaceToPlug.getMacAddress());
        for (VmNic vmNetworkInterface : vmNetworkIntrefaces) {
            if (!interfaceToPlug.getId().equals(vmNetworkInterface.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the vnic profile id of a given {@link VmNic} by a network name and vnic profile name.
     *
     * @param iface
     *            The vm network interface to be updated
     * @param compatibilityVersion
     *            The compatibility version of the cluster in which the VM exists
     * @param networksInClusterByName
     *            The networks which are assigned to the cluster
     * @param vnicProfilesInDc
     *            The vnic profiles for the data-center in which the VM exists
     * @param userId
     *            The id of the user which performs the action
     * @return {@code true} if the vnic profile id is updated, else {@code false}
     */
    public boolean updateNicWithVnicProfile(VmNetworkInterface iface,
            Version compatibilityVersion,
            Map<String, Network> networksInClusterByName,
            List<VnicProfileView> vnicProfilesInDc,
            Guid userId) {

        if (iface.getNetworkName() == null) {
            if (FeatureSupported.networkLinking(compatibilityVersion)) {
                iface.setVnicProfileId(null);
                return true;
            } else {
                return false;
            }
        }

        Network network = networksInClusterByName.get(iface.getNetworkName());
        if (network == null || !network.isVmNetwork()) {
            return false;
        }

        VnicProfile vnicProfile = getVnicProfileForNetwork(vnicProfilesInDc, network, iface.getVnicProfileName());
        if (vnicProfile == null) {
            vnicProfile = findVnicProfileForUser(userId, network);
            if (vnicProfile == null) {
                return false;
            }
        }

        iface.setVnicProfileId(vnicProfile.getId());
        return true;
    }

    private VnicProfile findVnicProfileForUser(Guid userId, Network network) {
        List<VnicProfile> networkProfiles = getVnicProfileDao().getAllForNetwork(network.getId());

        for (VnicProfile profile : networkProfiles) {
            if (profile.isPortMirroring()) {
                if (isVnicProfilePermitted(userId, profile, ActionGroup.PORT_MIRRORING)) {
                    return profile;
                }
            } else {
                if (isVnicProfilePermitted(userId, profile, ActionGroup.CONFIGURE_VM_NETWORK)) {
                    return profile;
                }
            }
        }

        return null;
    }

    private VnicProfile getVnicProfileForNetwork(List<VnicProfileView> vnicProfiles,
            Network network,
            String vnicProfileName) {

        if (vnicProfileName == null) {
            return null;
        }

        for (VnicProfileView vnicProfile : vnicProfiles) {
            if (ObjectUtils.equals(vnicProfile.getNetworkId(), network.getId())
                    && vnicProfileName.equals(vnicProfile.getName())) {
                return vnicProfile;
            }
        }

        return null;
    }

    private boolean isVnicProfilePermitted(Guid userId, VnicProfile profile, ActionGroup actionGroup) {
        return getPermissionDAO().getEntityPermissions(userId,
                actionGroup,
                profile.getId(),
                VdcObjectType.VnicProfile) != null;
    }

    /**
     * Log the given loggable & message to the {@link AuditLogDirector}.
     *
     * @param logable
     * @param auditLogType
     */
    protected void log(AuditLogableBase logable, AuditLogType auditLogType) {
        AuditLogDirector.log(logable, auditLogType);
    }

    protected MacPoolManager getMacPoolManager() {
        return MacPoolManager.getInstance();
    }

    protected VmNetworkStatisticsDao getVmNetworkStatisticsDao() {
        return DbFacade.getInstance().getVmNetworkStatisticsDao();
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDao();
    }

    protected VmNicDao getVmNicDao() {
        return DbFacade.getInstance().getVmNicDao();
    }

    protected VmDAO getVmDAO() {
        return DbFacade.getInstance().getVmDao();
    }

    private VnicProfileDao getVnicProfileDao() {
        return DbFacade.getInstance().getVnicProfileDao();
    }

    private PermissionDAO getPermissionDAO() {
        return DbFacade.getInstance().getPermissionDao();
    }

    private AuditLogableBase createAuditLog(final VmNic iface) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVmId(iface.getVmId());
        logable.addCustomValue("MACAddr", iface.getMacAddress());
        logable.addCustomValue("IfaceName", iface.getName());
        return logable;
    }
}
