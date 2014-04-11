package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.common.predicates.VmNetworkCanBeUpdatedPredicate;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Helper class to use for adding/removing {@link VmNic}s.
 */
public class VmInterfaceManager {

    private Log log = LogFactory.getLog(getClass());
    private MacPoolManagerStrategy macPool;

    public VmInterfaceManager() {
    }

    public VmInterfaceManager(MacPoolManagerStrategy macPool) {
        this.macPool = macPool;
    }

    /**
     * Add a {@link VmNic} to the VM. Allocates a MAC from the
     * {@link MacPoolManagerStrategy} if necessary, otherwise, if
     * {@code ConfigValues.HotPlugEnabled} is true, forces adding the MAC address to the
     * {@link MacPoolManagerStrategy}. If
     * HotPlug is not enabled tries to add the {@link VmNic}'s MAC address to the
     * {@link MacPoolManagerStrategy}, and throws a
     * {@link VdcBllException} if it fails.
     *
     * @param iface
     *            The interface to save.
     * @param compensationContext
     *            Used to snapshot the saved entities.
     * @param reserveExistingMac
     *            Used to denote if we want to reserve the NIC's MAC address in the {@link MacPoolManagerStrategy}
     * @param clusterCompatibilityVersion
     *            the compatibility version of the cluster
     * @return <code>true</code> if the MAC wasn't used, <code>false</code> if it was.
     */
    public void add(final VmNic iface,
            CompensationContext compensationContext,
            boolean reserveExistingMac,
            int osId,
            Version clusterCompatibilityVersion) {

        if (reserveExistingMac) {

            if (FeatureSupported.hotPlug(clusterCompatibilityVersion)
                && getOsRepository().hasNicHotplugSupport(osId, clusterCompatibilityVersion)) {
                macPool.forceAddMac(iface.getMacAddress());
            } else if (!macPool.addMac(iface.getMacAddress())) {
                auditLogMacInUse(iface);
                throw new VdcBLLException(VdcBllErrors.MAC_ADDRESS_IS_IN_USE);
            }
        }

        getVmNicDao().save(iface);
        getVmNetworkStatisticsDao().save(iface.getStatistics());
        compensationContext.snapshotNewEntity(iface);
        compensationContext.snapshotNewEntity(iface.getStatistics());
    }

    public OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
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
     * Remove all {@link VmNic}s from the VM, and remove the Mac addresses from {@link MacPoolManagerStrategy}.
     *
     * @param vmId
     *            The ID of the VM to remove from.
     */
    public void removeAll(Guid vmId) {
        List<VmNic> interfaces = getVmNicDao().getAllForVm(vmId);
        if (interfaces != null) {
            removeFromExternalNetworks(interfaces);

            for (VmNic iface : interfaces) {
                macPool.freeMac(iface.getMacAddress());
                getVmNicDao().remove(iface.getId());
                getVmNetworkStatisticsDao().remove(iface.getId());
            }
        }
    }

    protected void removeFromExternalNetworks(List<VmNic> interfaces) {
        Transaction transaction = TransactionSupport.suspend();
        for (VmNic iface : interfaces) {
            new ExternalNetworkManager(iface).deallocateIfExternal();
        }

        TransactionSupport.resume(transaction);
    }

    /**
     * Finds active VMs which actively uses a network from a given networks list
     *
     * @param vdsId
     *            The host id on which VMs are running
     * @param networks
     *            the networks to check if used
     * @return A list of VM names which uses the networks
     */
    public List<String> findActiveVmsUsingNetworks(Guid vdsId, List<String> networks) {
        if (networks.isEmpty()) {
            return Collections.emptyList();
        }

        List<VM> runningVms = getVmDAO().getAllRunningForVds(vdsId);
        List<String> vmNames = new ArrayList<String>();
        for (VM vm : runningVms) {
            List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(vm.getId());
            for (VmNetworkInterface vmNic : vmInterfaces) {
                if (VmNetworkCanBeUpdatedPredicate.getInstance().eval(vmNic) &&
                    vmNic.getNetworkName() != null &&
                    networks.contains(vmNic.getNetworkName())) {
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
     * Sorts the list of NICs. The comparison is done either via PCI address, MAC address, or name, depending
     * on the information we have available
     *
     * @param nics
     *            The list of NICs to sort
     * @param vmInterfaceDevices
     *            The device information we have on those NICs
     */
    public void sortVmNics(List<? extends VmNic> nics, final Map<Guid, VmDevice> vmInterfaceDevices) {
        Collections.sort(nics, new Comparator<VmNic>() {
            @Override
            public int compare(VmNic nic1, VmNic nic2) {
                if (vmInterfaceDevices != null) {
                    // If both devices have a PCI address then we compare by it
                    // Otherwise if both devices have a MAC address then we compare by it
                    // Otherwise we compare by name
                    VmDevice nic1Device = vmInterfaceDevices.get(nic1.getId());
                    VmDevice nic2Device = vmInterfaceDevices.get(nic2.getId());

                    if (nic1Device != null && nic2Device != null) {
                        if (StringUtils.isNotEmpty(nic1Device.getAddress()) && StringUtils.isNotEmpty(nic2Device.getAddress())) {
                            return nic1Device.getAddress().compareTo(nic2Device.getAddress());
                        }
                    }
                }
                if (StringUtils.isNotEmpty(nic1.getMacAddress()) && StringUtils.isNotEmpty(nic2.getMacAddress())) {
                    return nic1.getMacAddress().compareTo(nic2.getMacAddress());
                }
                return nic1.getName().compareTo(nic2.getName());
            }
        });
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

    private AuditLogableBase createAuditLog(final VmNic iface) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.setVmId(iface.getVmId());
        logable.addCustomValue("MACAddr", iface.getMacAddress());
        logable.addCustomValue("IfaceName", iface.getName());
        return logable;
    }
}
