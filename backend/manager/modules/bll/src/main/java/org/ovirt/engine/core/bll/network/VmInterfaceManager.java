package org.ovirt.engine.core.bll.network;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.di.Injector;
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
     * @param reassignMac Used to denote if we want to assign a new MAC address from the {@link MacPool} to the NIC. If
     *            this value is <code>false</code>, then <code>iface.getMacAddress</code> will be added to MAC pool.
     */
    public void add(final VmNic iface, CompensationContext compensationContext, boolean reassignMac) {
        registerIfaceInMacPool(iface, reassignMac);
        persistIface(iface, compensationContext);
    }

    private void registerIfaceInMacPool(VmNic iface, boolean reassignMac) {
        if (reassignMac) {
            iface.setMacAddress(macPool.allocateNewMac());
        } else if (!macPool.addMac(iface.getMacAddress())) {
            auditLogMacInUse(iface);
            throw new EngineException(EngineError.MAC_ADDRESS_IS_IN_USE);
        }
    }

    public void persistIface(VmNic iface, CompensationContext compensationContext) {
        getVmNicDao().save(iface);
        getVmNetworkStatisticsDao().save(iface.getStatistics());
        compensationContext.snapshotNewEntity(iface);
        compensationContext.snapshotNewEntity(iface.getStatistics());
    }

    public void auditLogMacInUse(final VmNic iface) {
        TransactionSupport.executeInNewTransaction(() -> {
            AuditLogable logable = createAuditLog(iface);
            log(logable, AuditLogType.MAC_ADDRESS_IS_IN_USE);
            log.warn("Network Interface '{}' has MAC address '{}' which is in use, " +
                    "therefore the action for VM '{}' failed.", iface.getName(), iface.getMacAddress(),
                    iface.getVmId());
            return null;
        });
    }

    public void auditLogMacInUseUnplug(final VmNic iface, String vmName) {
        TransactionSupport.executeInNewTransaction(() -> {
            AuditLogable logable = createAuditLog(iface);
            logable.setVmName(vmName);
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
    public void removeAllAndReleaseMacAddresses(Guid vmId) {
        removeAllAndReleaseMacAddresses(getVmNicDao().getAllForVm(vmId));
    }

    public void removeAllAndReleaseMacAddresses(List<? extends VmNic> interfaces) {
        removeAll(interfaces);
        getMacPool().freeMacs(interfaces.stream().map(VmNic::getMacAddress).collect(Collectors.toList()));
    }

    public void removeAll(List<? extends VmNic> interfaces) {
        if (interfaces == null) {
            return;
        }

        removeFromExternalNetworks(interfaces);

        for (VmNic iface : interfaces) {
            getVmNicDao().remove(iface.getId());
            getVmNetworkStatisticsDao().remove(iface.getId());
        }
    }

    public MacPool getMacPool() {
        return macPool;
    }

    protected void removeFromExternalNetworks(List<? extends VmNic> interfaces) {
        TransactionSupport.executeInSuppressed(() -> {
            for (VmNic iface : interfaces) {
                getExternalNetworkManagerFactory().create(iface).deallocateIfExternal();
            }
            return null;
        });
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
        return !readMacPool.isDuplicateMacAddressesAllowed() && findPluggedInterfaceWithSameMac(iface) != null;
    }

    /***
     * Returns an {@link Optional}&lt;{@link VM}&gt;  when duplicate MAC addresses are not allowed
     *
     * @param iface
     *              Interface to check for duplicate MAC address
     * @param readMacPool
     *              The pool to check if MAC address duplication is allowed
     * @return  {@link Optional}&lt;{@link VM}&gt; if duplicate MACs are not allowed, empty {@link Optional} otherwise.
     */
    public Optional<VM> getVmWithSameMacIfDuplicateIsNotAllowed(VmNic iface, ReadMacPool readMacPool) {
        if (!readMacPool.isDuplicateMacAddressesAllowed()) {
            return Optional.ofNullable(findPluggedInterfaceWithSameMac(iface))
                    .map(vmInterface -> getVmDao().get(vmInterface.getVmId()));
        }
        return Optional.empty();
    }

    /***
     * Returns {@link VmNic} or null of interface with same the MAC address as the given interface
     *
     * @param interfaceToPlug
     *            the network interface that needs to be plugged
     * @return {@link VmNic} of interface that is using the same MAC , <code>null</code> otherwise.
     */
    private VmNic findPluggedInterfaceWithSameMac(VmNic interfaceToPlug) {
        List<VmNic> vmNetworkIntrefaces = getVmNicDao().getPluggedForMac(interfaceToPlug.getMacAddress());
        for (VmNic vmNetworkInterface : vmNetworkIntrefaces) {
            if (!interfaceToPlug.getId().equals(vmNetworkInterface.getId())) {
                return vmNetworkInterface;
            }
        }
        return null;
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
    private void log(AuditLogable logable, AuditLogType auditLogType) {
        getAuditLogDirector().log(logable, auditLogType);
    }

    AuditLogDirector getAuditLogDirector() {
        return Injector.get(AuditLogDirector.class);
    }

    protected VmNetworkStatisticsDao getVmNetworkStatisticsDao() {
        return Injector.get(VmNetworkStatisticsDao.class);
    }

    protected VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return Injector.get(VmNetworkInterfaceDao.class);
    }

    protected VmNicDao getVmNicDao() {
        return Injector.get(VmNicDao.class);
    }

    protected VmDao getVmDao() {
        return Injector.get(VmDao.class);
    }

    private ExternalNetworkManagerFactory getExternalNetworkManagerFactory() {
        return Injector.get(ExternalNetworkManagerFactory.class);
    }

    private AuditLogable createAuditLog(final VmNic iface) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("MACAddr", iface.getMacAddress());
        logable.addCustomValue("IfaceName", iface.getName());
        logable.setVmId(iface.getVmId());
        return logable;
    }
}
