package org.ovirt.engine.core.bll.network;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transaction;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to use for adding/removing {@link VmNic}s.
 */
public class VmInterfaceManager {

    private Logger log = LoggerFactory.getLogger(getClass());
    private MacPool macPool;

    public VmInterfaceManager() {
    }

    public VmInterfaceManager(MacPool macPool) {
        this.macPool = macPool;
    }

    /**
     * Add a {@link VmNic} to the VM. Allocates a MAC from the
     * {@link MacPool} if necessary, otherwise, if
     * {@code ConfigValues.HotPlugEnabled} is true, forces adding the MAC address to the
     * {@link MacPool}. If
     * HotPlug is not enabled tries to add the {@link VmNic}'s MAC address to the
     * {@link MacPool}, and throws a
     * {@link EngineException} if it fails.
     *
     * @param iface
     *            The interface to save.
     * @param compensationContext
     *            Used to snapshot the saved entities.
     * @param reserveExistingMac
     *            Used to denote if we want to reserve the NIC's MAC address in the {@link MacPool}
     * @param assignNewMac
     *            Used to denote if we want to assign a new MAC address from the {@link MacPool} to the NIC. Note that
     *            <code>true</code> value in reserveExistingMac takes precedence over this one.
     * @param clusterCompatibilityVersion
     *            the compatibility version of the cluster
     */
    public void add(final VmNic iface,
            CompensationContext compensationContext,
            boolean reserveExistingMac,
            boolean assignNewMac,
            int osId,
            Version clusterCompatibilityVersion) {

        if (reserveExistingMac) {

            if (getOsRepository().hasNicHotplugSupport(osId, clusterCompatibilityVersion)) {
                macPool.forceAddMac(iface.getMacAddress());
            } else if (!macPool.addMac(iface.getMacAddress())) {
                auditLogMacInUse(iface);
                throw new EngineException(EngineError.MAC_ADDRESS_IS_IN_USE);
            }
        } else if (assignNewMac) {
            final String macAddress = macPool.allocateNewMac();
            iface.setMacAddress(macAddress);
        }

        getVmNicDao().save(iface);
        getVmNetworkStatisticsDao().save(iface.getStatistics());
        compensationContext.snapshotNewEntity(iface);
        compensationContext.snapshotNewEntity(iface.getStatistics());
    }

    public OsRepository getOsRepository() {
        return SimpleDependencyInjector.getInstance().get(OsRepository.class);
    }

    public void auditLogMacInUse(final VmNic iface) {
        TransactionSupport.executeInNewTransaction(() -> {
            AuditLogableBase logable = createAuditLog(iface);
            log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE);
            log.warn("Network Interface '{}' has MAC address '{}' which is in use, " +
                    "therefore the action for VM '{}' failed.", iface.getName(), iface.getMacAddress(),
                    iface.getVmId());
            return null;
        });
    }

    public void auditLogMacInUseUnplug(final VmNic iface, String vmName) {
        TransactionSupport.executeInNewTransaction(() -> {
            AuditLogableBase logable = createAuditLog(iface);
            logable.addCustomValue("VmName", vmName);
            log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE_UNPLUG);
            log.warn("Network Interface '{}' has MAC address '{}' which is in use, " +
                    "therefore it is being unplugged from VM '{}'.", iface.getName(), iface.getMacAddress(),
                    iface.getVmId());
            return null;
        });
    }

    /**
     * Remove all {@link VmNic}s from the VM, and remove the Mac addresses from {@link MacPool}.
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

    /***
     * Returns whether or not there are too many network interfaces with
     * the same MAC address as the given interface plugged in
     *
     * @param iface
     *            the network interface with MAC address
     * @return <code>true</code> if the MAC is used by too many other plugged network interface,
     *         <code>false</code> otherwise.
     */
    public boolean tooManyPluggedInterfaceWithSameMac(VmNic iface, ReadMacPool readMacPool) {
        return !readMacPool.isDuplicateMacAddressesAllowed() && existsPluggedInterfaceWithSameMac(iface);
    }

    /***
     * Returns whether or not there is a plugged network interface with the same MAC address as the given interface
     *
     * @param interfaceToPlug
     *            the network interface that needs to be plugged
     * @return <code>true</code> if the MAC is used by another plugged network interface, <code>false</code> otherwise.
     */
    private boolean existsPluggedInterfaceWithSameMac(VmNic interfaceToPlug) {
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
     */
    private void log(AuditLogableBase logable, AuditLogType auditLogType) {
        getAuditLogDirector().log(logable, auditLogType);
    }

    AuditLogDirector getAuditLogDirector() {
        return new AuditLogDirector();
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

    protected VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    private AuditLogableBase createAuditLog(final VmNic iface) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("MACAddr", iface.getMacAddress());
        logable.addCustomValue("IfaceName", iface.getName());
        return logable;
    }
}
