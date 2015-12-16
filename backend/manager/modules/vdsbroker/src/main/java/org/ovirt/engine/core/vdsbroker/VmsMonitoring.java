package org.ovirt.engine.core.vdsbroker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.IVdsEventListener;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.FullListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * invoke all Vm analyzers in hand and iterate over their report
 * and take actions - fire VDSM commands (destroy,run/rerun,migrate), report complete actions,
 * hand-over migration and save-to-db
 */
public class VmsMonitoring {

    private final boolean timeToUpdateVmStatistics;
    private final long fetchTime;
    private VdsManager vdsManager;
    /**
     * The Vms we want to monitor and analyze for changes.
     * VM object represent the persisted object(namely the one in db) and the VmInternalData
     * is the running one as reported from VDSM
     */
    private List<Pair<VM, VmInternalData>> monitoredVms;
    /**
     * A collection of VMs that has changes in devices.
     */
    private List<Pair<VM, VmInternalData>> vmsWithChangedDevices;
    private final AuditLogDirector auditLogDirector;
    /**
     * The managers of the monitored VMs in this cycle.
     */
    private Map<Guid, VmManager> vmManagers = new HashMap<>();

    /**
     * The analyzers which hold all the data per a VM
     */
    private List<VmAnalyzer> vmAnalyzers = new ArrayList<>();

    //*** data collectors ***//
    private final Map<Guid, VmDynamic> vmDynamicToSave = new HashMap<>();
    private final List<VmStatistics> vmStatisticsToSave = new ArrayList<>();
    private final List<List<VmNetworkInterface>> vmInterfaceStatisticsToSave = new ArrayList<>();
    private final Collection<Pair<Guid, DiskImageDynamic>> vmDiskImageDynamicToSave = new LinkedList<>();
    private final List<VmDevice> vmDeviceToSave = new ArrayList<>();
    private final Map<Guid, List<VmGuestAgentInterface>> vmGuestAgentNics = new HashMap<>();
    private final List<VmDevice> newVmDevices = new ArrayList<>();
    private final List<VmDeviceId> removedDeviceIds = new ArrayList<>();
    private final List<LUNs> vmLunDisksToSave = new ArrayList<>();
    private final List<Guid> autoVmsToRun = new ArrayList<>();
    private final List<Guid> coldRebootVmsToRun = new ArrayList<>();
    private final List<VmStatic> externalVmsToAdd = new ArrayList<>();
    private final Map<Guid, VmJob> vmJobsToUpdate = new HashMap<>();
    private final List<Guid> vmJobIdsToRemove = new ArrayList<>();
    private final List<Guid> existingVmJobIds = new ArrayList<>();
    private List<Pair<VM, VmInternalData>> externalVms = new ArrayList<>();
    //*** data collectors ***//

    private static final String EXTERNAL_VM_NAME_FORMAT = "external-%1$s";
    private static final Logger log = LoggerFactory.getLogger(VmsMonitoring.class);

    /**
     *  @param vdsManager the host manager related to this cycle.
     * @param monitoredVms the vms we want to monitor/analyze/react on. this structure is
     *                     a pair of the persisted (db currently) VM and the running VM which was reported from vdsm.
     *                     Analysis and reactions would be taken on those VMs only.
     * @param vmsWithChangedDevices
     * @param auditLogDirector
     */
    public VmsMonitoring(
            VdsManager vdsManager,
            List<Pair<VM, VmInternalData>> monitoredVms,
            List<Pair<VM, VmInternalData>> vmsWithChangedDevices,
            AuditLogDirector auditLogDirector,
            long fetchTime) {
        this(vdsManager, monitoredVms, vmsWithChangedDevices, auditLogDirector, fetchTime, false);
    }

    public VmsMonitoring(
            VdsManager vdsManager,
            List<Pair<VM, VmInternalData>> monitoredVms,
            List<Pair<VM, VmInternalData>> vmsWithChangedDevices,
            AuditLogDirector auditLogDirector,
            long fetchTime,
            boolean timeToUpdateVmStatistics) {
        this.vdsManager = vdsManager;
        this.monitoredVms = monitoredVms;
        this.vmsWithChangedDevices = vmsWithChangedDevices;
        this.auditLogDirector = auditLogDirector;
        this.fetchTime = fetchTime;
        this.timeToUpdateVmStatistics = timeToUpdateVmStatistics;
    }

    /**
     * analyze and react upon changes on the monitoredVms. relevant changes would
     * be persisted and state transitions and internal commands would
     * take place accordingly.
     */
    public void perform() {
        try {
            refreshExistingVmJobList();
            refreshVmStats();
            afterVMsRefreshTreatment();
            vdsManager.vmsMonitoringInitFinished();
        } catch (RuntimeException ex) {
            log.error("Failed during vms monitoring on host {} error is: {}", vdsManager.getVdsName(), ex);
            log.error("Exception:", ex);
        } finally {
            unlockVmsManager();
        }

    }

    protected boolean isTimeToUpdateVmStatistics() {
        return timeToUpdateVmStatistics;
    }

    /**
     * lock Vms which has db entity i.e they are managed by a VmManager
     * @param pair
     * @return true if lock acquired
     */
    private boolean tryLockVmForUpdate(Pair<VM, VmInternalData> pair) {
        Guid vmId = getVmId(pair);

        if (vmId != null) {
            VmManager vmManager = getResourceManager().getVmManager(vmId);

            if (vmManager.trylock()) {
                if (!vmManager.isLatestData(pair.getSecond(), vdsManager.getVdsId())) {
                    log.warn("skipping VM '{}' from this monitoring cycle" +
                            " - newer VM data was already processed", vmId);
                    vmManager.unlock();
                } else if (vmManager.getVmDataChangedTime() != null && fetchTime - vmManager.getVmDataChangedTime() <= 0) {
                    log.warn("skipping VM '{}' from this monitoring cycle" +
                            " - the VM data has changed since fetching the data", vmId);
                    vmManager.unlock();
                } else {
                    // store the locked managers to finally release them at the end of the cycle
                    vmManagers.put(vmId, vmManager);
                    return true;
                }
            } else {
                log.debug("skipping VM '{}' from this monitoring cycle" +
                        " - the VM is locked by its VmManager ", getVmId(pair));
            }
        }
        return false;
    }

    private void unlockVmsManager() {
        for (VmManager vmManager : vmManagers.values()) {
            vmManager.updateVmDataChangedTime();
            vmManager.unlock();
        }
    }

    /**
     * Analyze the VM data pair
     * Skip analysis on VMs which cannot be locked
     * note: metrics calculation like memCommited and vmsCoresCount should be calculated *before*
     *   this filtering.
     */
    private void refreshVmStats() {
        for (Pair<VM, VmInternalData> monitoredVm : monitoredVms) {
            // TODO filter out migratingTo VMs if no action is taken on them
            if (tryLockVmForUpdate(monitoredVm)) {
                VmAnalyzer vmAnalyzer = getVmAnalyzer(monitoredVm);
                vmAnalyzers.add(vmAnalyzer);
                vmAnalyzer.analyze();

                if (vmAnalyzer.isExternalVm()) {
                    externalVms.add(new Pair<>(vmAnalyzer.getDbVm(), vmAnalyzer.getVdsmVm()));
                }
            }
        }

        processExternallyManagedVms();
        processVmsWithDevicesChange();
        saveVmsToDb();
    }

    protected VmAnalyzer getVmAnalyzer(Pair<VM, VmInternalData> pair) {
        return new VmAnalyzer(
                pair.getFirst(),
                pair.getSecond(),
                this,
                auditLogDirector);
    }

    private void afterVMsRefreshTreatment() {
        Collection<Guid> movedToDownVms = new ArrayList<>();
        List<Guid> succeededToRunVms = new ArrayList<>();

        // now loop over the result and act
        for (VmAnalyzer vmAnalyzer : vmAnalyzers) {

            // rerun all vms from rerun list
            if (vmAnalyzer.isRerun()) {
                log.error("Rerun VM '{}'. Called from VDS '{}'", vmAnalyzer.getDbVm().getId(), vdsManager.getVdsName());
                getResourceManager().rerunFailedCommand(vmAnalyzer.getDbVm().getId(), vdsManager.getVdsId());
            }

            if (vmAnalyzer.isSuccededToRun()) {
                vdsManager.succeededToRunVm(vmAnalyzer.getDbVm().getId());
                succeededToRunVms.add(vmAnalyzer.getDbVm().getId());
            }

            // Refrain from auto-start HA VM during its re-run attempts.
            if (vmAnalyzer.isAutoVmToRun() && !vmAnalyzer.isRerun()) {
                autoVmsToRun.add(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isColdRebootVmToRun()) {
                coldRebootVmsToRun.add(vmAnalyzer.getDbVm().getId());
            }

            // process all vms that their ip changed.
            if (vmAnalyzer.isClientIpChanged()) {
                final VmDynamic vmDynamic = vmAnalyzer.getVdsmVm().getVmDynamic();
                getVdsEventListener().processOnClientIpChange(vmDynamic.getId(),
                        vmDynamic.getClientIp());
            }

            // process all vms that powering up.
            if (vmAnalyzer.isPoweringUp()) {
                getVdsEventListener().processOnVmPoweringUp(vmAnalyzer.getVdsmVm().getVmDynamic().getId());
            }

            if (vmAnalyzer.isMovedToDown()) {
                movedToDownVms.add(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isRemoveFromAsync()) {
                getResourceManager().removeAsyncRunningVm(vmAnalyzer.getDbVm().getId());
            }

            if (vmAnalyzer.isHostedEngineUnmanaged()) {
                // @since 3.6 - we take existing HE VM and reimport it
                importHostedEngineVM(getVmInfo(Collections.singletonList(vmAnalyzer.getVdsmVm()
                        .getVmDynamic()
                        .getId()
                        .toString()))[0]);
            }
        }

        getVdsEventListener().updateSlaPolicies(succeededToRunVms, vdsManager.getVdsId());

        // run all vms that crashed that marked with auto startup
        getVdsEventListener().runFailedAutoStartVMs(autoVmsToRun);

        // run all vms that went down as a part of cold reboot process
        getVdsEventListener().runColdRebootVms(coldRebootVmsToRun);

        // process all vms that went down
        getVdsEventListener().processOnVmStop(movedToDownVms, vdsManager.getVdsId());

        getVdsEventListener().refreshHostIfAnyVmHasHostDevices(succeededToRunVms, vdsManager.getVdsId());
    }

    private void importHostedEngineVM(Map<String, Object> vmStruct) {
        VM vm = VdsBrokerObjectsBuilder.buildVmsDataFromExternalProvider(vmStruct);
        if (vm != null) {
            vm.setImages(VdsBrokerObjectsBuilder.buildDiskImagesFromDevices(vmStruct));
            vm.setInterfaces(VdsBrokerObjectsBuilder.buildVmNetworkInterfacesFromDevices(vmStruct));
            for (DiskImage diskImage : vm.getImages()) {
                vm.getDiskMap().put(Guid.newGuid(), diskImage);
            }
            vm.setVdsGroupId(getVdsManager().getVdsGroupId());
            vm.setRunOnVds(getVdsManager().getVdsId());
            getVdsEventListener().importHostedEngineVm(vm);
        }
    }

    private void processVmsWithDevicesChange() {
        // Handle VM devices were changed (for 3.1 cluster and above)
        if (!VmDeviceCommonUtils.isOldClusterVersion(vdsManager.getGroupCompatibilityVersion())) {
            // If there are vms that require updating,
            // get the new info from VDSM in one call, and then update them all
            if (!vmsWithChangedDevices.isEmpty()) {
                ArrayList<String> vmsToUpdate = new ArrayList<>(vmsWithChangedDevices.size());
                for (Pair<VM, VmInternalData> pair : vmsWithChangedDevices) {
                    Guid vmId = pair.getFirst().getId();
                    // update only if the vm marked to change, otherwise it might have skipped because data invalidated
                    // this ensure the vmManager lock is taken
                    if (vmDynamicToSave.containsKey(vmId)) {
                        vmDynamicToSave.get(vmId).setHash(pair.getSecond().getVmDynamic().getHash());
                        vmsToUpdate.add(vmId.toString());
                    } else {
                        log.warn("VM '{}' not in changed list, skipping devices update.", vmId);
                    }
                }
                updateVmDevices(vmsToUpdate);
            }
        }
    }

    private void saveVmsToDb() {
        getDbFacade().getVmDynamicDao().updateAllInBatch(vmDynamicToSave.values());
        getDbFacade().getVmStatisticsDao().updateAllInBatch(vmStatisticsToSave);

        final List<VmNetworkStatistics> allVmInterfaceStatistics = new LinkedList<>();
        for (List<VmNetworkInterface> list : vmInterfaceStatisticsToSave) {
            for (VmNetworkInterface iface : list) {
                allVmInterfaceStatistics.add(iface.getStatistics());
            }
        }

        getDbFacade().getVmNetworkStatisticsDao().updateAllInBatch(allVmInterfaceStatistics);

        getDbFacade().getDiskImageDynamicDao().updateAllDiskImageDynamicWithDiskIdByVmId(vmDiskImageDynamicToSave);
        getDbFacade().getLunDao().updateAllInBatch(vmLunDisksToSave);
        getVdsEventListener().addExternallyManagedVms(externalVmsToAdd);
        saveVmDevicesToDb();
        saveVmGuestAgentNetworkDevices();
        saveVmJobsToDb();
    }

    protected void setOsId(VmStatic vmStatic, String guestOsNameFromVdsm, int defaultArchOsId) {
        if (StringUtils.isEmpty(guestOsNameFromVdsm)) {
            log.debug("VM '{}': setting default OS ID: '{}'", vmStatic.getName(), defaultArchOsId);
            vmStatic.setOsId(defaultArchOsId);
        }
    }

    protected void setDisplayType(VmStatic vmStatic, String displayTypeFromVdsm, DisplayType defaultDisplayType) {
        if (StringUtils.isEmpty(displayTypeFromVdsm)) {
            log.debug("VM '{}': setting default display type: '{}'", vmStatic.getName(), defaultDisplayType.getValue());
            vmStatic.setDefaultDisplayType(defaultDisplayType);
        }
    }

    private int getDefaultOsId(ArchitectureType architecture) {
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        Integer defaultArchOsId = osRepository.getDefaultOSes().get(architecture);
        return (defaultArchOsId == null) ? 0 : defaultArchOsId;
    }

    private DisplayType getDefaultDisplayType(int osId, Version clusterVersion) {
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        List<Pair<GraphicsType, DisplayType>> pairs = osRepository.getGraphicsAndDisplays(osId, clusterVersion);

        if (!pairs.isEmpty()) {
            Pair<GraphicsType, DisplayType> graphicsDisplayPair = pairs.get(0);
            return graphicsDisplayPair.getSecond();
        }

        return DisplayType.qxl;
    }

    protected void processExternallyManagedVms() {
        // Fetching for details from the host
        // and marking the VMs for addition
        List<String> vmsToQuery = new ArrayList<>(externalVms.size());
        for (Pair<VM, VmInternalData> pair : externalVms) {
            vmsToQuery.add(pair.getSecond().getVmDynamic().getId().toString());
        }
        if (!vmsToQuery.isEmpty()) {
            VDSGroup vdsGroup = getDbFacade().getVdsGroupDao().get(vdsManager.getVdsGroupId());
            int defaultOsId = getDefaultOsId(vdsGroup.getArchitecture());
            DisplayType defaultDisplayType = getDefaultDisplayType(defaultOsId, vdsGroup.getCompatibilityVersion());

            // Query VDSM for VMs info, and creating a proper VMStatic to be used when importing them
            Map<String, Object>[] vmsInfo = getVmInfo(vmsToQuery);
            for (Map<String, Object> vmInfo : vmsInfo) {
                Guid vmId = Guid.createGuidFromString((String) vmInfo.get(VdsProperties.vm_guid));
                VmStatic vmStatic = new VmStatic();
                vmStatic.setId(vmId);
                vmStatic.setCreationDate(new Date());
                vmStatic.setVdsGroupId(vdsManager.getVdsGroupId());
                String vmNameOnHost = (String) vmInfo.get(VdsProperties.vm_name);

                if (StringUtils.equals(Config.<String>getValue(ConfigValues.HostedEngineVmName), vmNameOnHost)) {
                    // its a hosted engine VM -> import it and skip the external VM phase
                    importHostedEngineVM(vmInfo);
                    continue;
                } else {
                    vmStatic.setName(String.format(EXTERNAL_VM_NAME_FORMAT, vmNameOnHost));
                    vmStatic.setOrigin(OriginType.EXTERNAL);
                }

                vmStatic.setNumOfSockets(VdsBrokerObjectsBuilder.parseIntVdsProperty(vmInfo.get(VdsProperties.num_of_cpus)));
                vmStatic.setMemSizeMb(VdsBrokerObjectsBuilder.parseIntVdsProperty(vmInfo.get(VdsProperties.mem_size_mb)));
                vmStatic.setSingleQxlPci(false);

                setOsId(vmStatic, (String) vmInfo.get(VdsProperties.guest_os), defaultOsId);
                setDisplayType(vmStatic, (String) vmInfo.get(VdsProperties.displayType), defaultDisplayType);

                externalVmsToAdd.add(vmStatic);
                log.info("Importing VM '{}' as '{}', as it is running on the on Host, but does not exist in the engine.", vmNameOnHost, vmStatic.getName());
            }
        }
    }

    // ***** DB interaction *****

    private void saveVmGuestAgentNetworkDevices() {
        if (!vmGuestAgentNics.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            for (Guid vmId : vmGuestAgentNics.keySet()) {
                                getDbFacade().getVmGuestAgentInterfaceDao().removeAllForVm(vmId);
                            }

                            for (List<VmGuestAgentInterface> nics : vmGuestAgentNics.values()) {
                                if (nics != null) {
                                    for (VmGuestAgentInterface nic : nics) {
                                        getDbFacade().getVmGuestAgentInterfaceDao().save(nic);
                                    }
                                }
                            }
                            return null;
                        }
                    }
            );
        }
    }

    private void saveVmDevicesToDb() {
        getDbFacade().getVmDeviceDao().updateAllInBatch(vmDeviceToSave);

        if (!removedDeviceIds.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().removeAll(removedDeviceIds);
                            return null;
                        }
                    });
        }

        if (!newVmDevices.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {

                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmDeviceDao().saveAll(newVmDevices);
                            return null;
                        }
                    });
        }
    }

    private void saveVmJobsToDb() {
        getDbFacade().getVmJobDao().updateAllInBatch(vmJobsToUpdate.values());

        if (!vmJobIdsToRemove.isEmpty()) {
            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                    new TransactionMethod<Void>() {
                        @Override
                        public Void runInTransaction() {
                            getDbFacade().getVmJobDao().removeAll(vmJobIdsToRemove);
                            return null;
                        }
                    });
        }
    }

    private void refreshExistingVmJobList() {
        existingVmJobIds.clear();
        existingVmJobIds.addAll(getDbFacade().getVmJobDao().getAllIds());
    }

    // ***** Helpers and sub-methods *****

    /**
     * Update the given list of VMs properties in DB
     *
     * @param vmsToUpdate
     */
    protected void updateVmDevices(List<String> vmsToUpdate) {
        if (vmsToUpdate.isEmpty()) {
            return;
        }
        Map[] vms = getVmInfo(vmsToUpdate);
        if (vms != null) {
            for (Map vm : vms) {
                processVmDevices(vm);
            }
        }
    }

    /**
     * Actually process the VM device update in DB.
     *
     * @param vm
     */
    private void processVmDevices(Map vm) {
        if (vm == null || vm.get(VdsProperties.vm_guid) == null) {
            log.error("Received NULL VM or VM id when processing VM devices, abort.");
            return;
        }

        Guid vmId = new Guid((String) vm.get(VdsProperties.vm_guid));
        Set<Guid> processedDevices = new HashSet<>();
        List<VmDevice> devices = getDbFacade().getVmDeviceDao().getVmDeviceByVmId(vmId);
        Map<VmDeviceId, VmDevice> deviceMap = Entities.businessEntitiesById(devices);

        for (Object o : (Object[]) vm.get(VdsProperties.Devices)) {
            Map device = (Map<String, Object>) o;
            if (device.get(VdsProperties.Address) == null) {
                logDeviceInformation(vmId, device);
                continue;
            }

            Guid deviceId = getDeviceId(device);
            VmDevice vmDevice = deviceMap.get(new VmDeviceId(deviceId, vmId));
            String logicalName = null;
            if (deviceId != null && FeatureSupported.reportedDisksLogicalNames(getVdsManager().getGroupCompatibilityVersion()) &&
                    VmDeviceType.DISK.getName().equals(device.get(VdsProperties.Device))) {
                try {
                    logicalName = getDeviceLogicalName((Map<?, ?>) vm.get(VdsProperties.GuestDiskMapping), deviceId);
                } catch (Exception e) {
                    log.error("error while getting device name when processing, vm '{}', device info '{}' with exception, skipping '{}'",
                            vmId, device, e.getMessage());
                    log.error("Exception", e);
                }
            }

            if (deviceId == null || vmDevice == null) {
                deviceId = addNewVmDevice(vmId, device, logicalName);
            } else {
                vmDevice.setIsPlugged(Boolean.TRUE);
                vmDevice.setAddress(device.get(VdsProperties.Address).toString());
                vmDevice.setAlias(StringUtils.defaultString((String) device.get(VdsProperties.Alias)));
                vmDevice.setLogicalName(logicalName);
                vmDevice.setHostDevice(StringUtils.defaultString((String) device.get(VdsProperties.HostDev)));
                addVmDeviceToList(vmDevice);
            }

            processedDevices.add(deviceId);
        }

        handleRemovedDevices(vmId, processedDevices, devices);
    }

    private String getDeviceLogicalName(Map<?, ?> diskMapping, Guid deviceId) {
        if (diskMapping == null) {
            return null;
        }

        Map<?, ?> deviceMapping = null;
        String modifiedDeviceId = deviceId.toString().substring(0, 20);
        for (Map.Entry<?, ?> entry : diskMapping.entrySet()) {
            String serial = (String) entry.getKey();
            if (serial != null && serial.contains(modifiedDeviceId)) {
                deviceMapping = (Map<?, ?>) entry.getValue();
                break;
            }
        }

        return deviceMapping == null ? null : (String) deviceMapping.get(VdsProperties.Name);
    }

    /**
     * Removes unmanaged devices from DB if were removed by libvirt. Empties device address with isPlugged = false
     *
     * @param vmId
     * @param processedDevices
     */
    private void handleRemovedDevices(Guid vmId, Set<Guid> processedDevices, List<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (processedDevices.contains(device.getDeviceId())) {
                continue;
            }

            if (deviceWithoutAddress(device)) {
                continue;
            }

            if (device.getIsManaged()) {
                if (device.getIsPlugged()) {
                    device.setIsPlugged(Boolean.FALSE);
                    device.setAddress("");
                    addVmDeviceToList(device);
                    log.debug("VM '{}' managed pluggable device was unplugged : '{}'", vmId, device);
                } else if (!devicePluggable(device)) {
                    log.error("VM '{}' managed non pluggable device was removed unexpectedly from libvirt: '{}'",
                            vmId, device);
                }
            } else {
                removedDeviceIds.add(device.getId());
                log.debug("VM '{}' unmanaged device was marked for remove : {1}", vmId, device);
            }
        }
    }

    private boolean devicePluggable(VmDevice device) {
        return VmDeviceCommonUtils.isDisk(device) || VmDeviceCommonUtils.isBridge(device);
    }

    /**
     * Libvirt gives no address to some special devices, and we know it.
     */
    private boolean deviceWithoutAddress(VmDevice device) {
        return VmDeviceCommonUtils.isGraphics(device);
    }

    /**
     * Adds new devices recognized by libvirt
     *
     * @param vmId
     * @param device
     */
    private Guid addNewVmDevice(Guid vmId, Map device, String logicalName) {
        Guid newDeviceId = Guid.Empty;
        String typeName = (String) device.get(VdsProperties.Type);
        String deviceName = (String) device.get(VdsProperties.Device);

        // do not allow null or empty device or type values
        if (StringUtils.isEmpty(typeName) || StringUtils.isEmpty(deviceName)) {
            log.error("Empty or NULL values were passed for a VM '{}' device, Device is skipped", vmId);
        } else {
            String address = device.get(VdsProperties.Address).toString();
            String alias = StringUtils.defaultString((String) device.get(VdsProperties.Alias));
            Object o = device.get(VdsProperties.SpecParams);
            newDeviceId = Guid.newGuid();
            VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
            VmDevice newDevice = new VmDevice(id, VmDeviceGeneralType.forValue(typeName), deviceName, address,
                    0,
                    o == null ? new HashMap<>() : (Map<String, Object>) o,
                    false,
                    true,
                    Boolean.getBoolean((String) device.get(VdsProperties.ReadOnly)),
                    alias,
                    null,
                    null,
                    logicalName);
            newVmDevices.add(newDevice);
            log.debug("New device was marked for adding to VM '{}' Devices : '{}'", vmId, newDevice);
        }

        return newDeviceId;
    }

    /**
     * gets the device id from the structure returned by VDSM device ids are stored in specParams map
     *
     * @param device
     * @return
     */
    private static Guid getDeviceId(Map device) {
        String deviceId = (String) device.get(VdsProperties.DeviceId);
        return deviceId == null ? null : new Guid(deviceId);
    }

    /**
     * gets VM full information for the given list of VMs
     *
     * @param vmsToUpdate
     * @return
     */
    protected Map<String, Object>[] getVmInfo(List<String> vmsToUpdate) {
        // TODO refactor commands to use vdsId only - the whole vds object here is useless
        VDS vds = new VDS();
        vds.setId(vdsManager.getVdsId());
        Map<String, Object>[] result = new Map[0];
        VDSReturnValue vdsReturnValue = getResourceManager().runVdsCommand(VDSCommandType.FullList,
                new FullListVDSCommandParameters(vds, vmsToUpdate));
        if (vdsReturnValue.getSucceeded()) {
            result = (Map<String, Object>[]) (vdsReturnValue.getReturnValue());
        }
        return result;
    }

    private boolean shouldLogDeviceDetails(String deviceType) {
        return !StringUtils.equalsIgnoreCase(deviceType, VmDeviceType.FLOPPY.getName());
    }

    private void logDeviceInformation(Guid vmId, Map device) {
        String message = "Received a {} Device without an address when processing VM {} devices, skipping device";
        String deviceType = (String) device.get(VdsProperties.Device);

        if (shouldLogDeviceDetails(deviceType)) {
            Map<String, Object> deviceInfo = device;
            log.info(message + ": {}", StringUtils.defaultString(deviceType), vmId, deviceInfo);
        } else {
            log.info(message, StringUtils.defaultString(deviceType), vmId);
        }
    }

    private Guid getVmId(Pair<VM, VmInternalData> pair) {
        return (pair.getFirst() != null) ?
                pair.getFirst().getId() :
                ((pair.getSecond() != null) ? pair.getSecond().getVmDynamic().getId() : null);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDynamic
     */
    protected void addVmDynamicToList(VmDynamic vmDynamic) {
        vmDynamicToSave.put(vmDynamic.getId(), vmDynamic);
    }

    /**
     * Add or update vmStatistics to save list
     *
     * @param vmStatistics
     */
    protected void addVmStatisticsToList(VmStatistics vmStatistics) {
        vmStatisticsToSave.add(vmStatistics);
    }

    protected void addVmInterfaceStatisticsToList(List<VmNetworkInterface> list) {
        if (list.isEmpty()) {
            return;
        }
        vmInterfaceStatisticsToSave.add(list);
    }

    /**
     * Add or update vmDynamic to save list
     *
     * @param vmDevice
     */
    private void addVmDeviceToList(VmDevice vmDevice) {
        vmDeviceToSave.add(vmDevice);
    }

    /**
     * An access method for test usages
     *
     * @return The LUNs to update in DB
     */
    protected List<LUNs> getVmLunDisksToSave() {
        return vmLunDisksToSave;
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected ResourceManager getResourceManager() {
        return ResourceManager.getInstance();
    }

    protected IVdsEventListener getVdsEventListener() {
        return ResourceManager.getInstance().getEventListener();
    }

    public void addDiskImageDynamicToSave(Pair<Guid, DiskImageDynamic> imageDynamicByVmId) {
        vmDiskImageDynamicToSave.add(imageDynamicByVmId);
    }

    public List<Guid> getExistingVmJobIds() {
        return existingVmJobIds;
    }

    public Map<Guid, VmJob> getVmJobsToUpdate() {
        return vmJobsToUpdate;
    }

    public List<Guid> getVmJobIdsToRemove() {
        return vmJobIdsToRemove;
    }

    public VdsManager getVdsManager() {
        return vdsManager;
    }

    public VmManager getVmManager(Guid vmId) {
        return vmManagers.get(vmId);
    }

    public void addVmGuestAgentNics(Guid id, List<VmGuestAgentInterface> vmGuestAgentInterfaces) {
        vmGuestAgentNics.put(id, vmGuestAgentInterfaces);
    }
}
