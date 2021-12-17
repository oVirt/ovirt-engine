package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapMinusOneToNull;
import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapNullToMinusOne;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.model.BiosType;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.CpuMode;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.DisplayDisconnectAction;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.model.Io;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VcpuPin;
import org.ovirt.engine.api.model.VcpuPins;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmBase;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmStorageErrorResumeBehaviour;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.UsbMapperUtils;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmBaseMapper {
    protected static final int BYTES_PER_MB = 1024 * 1024;

    /**
     * Common for VM, template and instance type
     */
    protected static void mapCommonModelToEntity(org.ovirt.engine.core.common.businessentities.VmBase entity, VmBase model) {
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetMemory()) {
            entity.setMemSizeMb((int) (model.getMemory() / BYTES_PER_MB));
        }

        if (model.isSetIo() && model.getIo().isSetThreads()) {
            entity.setNumOfIoThreads(model.getIo().getThreads());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetCpu() && model.getCpu().isSetTopology()) {
            if (model.getCpu().getTopology().getCores() != null) {
                entity.setCpuPerSocket(model.getCpu().getTopology().getCores());
            }
            if (model.getCpu().getTopology().getSockets() != null) {
                entity.setNumOfSockets(model.getCpu().getTopology().getSockets());
            }
            if (model.getCpu().getTopology().getThreads() != null) {
                entity.setThreadsPerCpu(model.getCpu().getTopology().getThreads());
            }
        }
        if (model.isSetHighAvailability()) {
            if (model.getHighAvailability().isSetEnabled()) {
                entity.setAutoStartup(model.getHighAvailability().isEnabled());
            }
            if (model.getHighAvailability().isSetPriority()) {
                entity.setPriority(model.getHighAvailability().getPriority());
            }
        }
        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetType()) {
                entity.setDefaultDisplayType(null); // let backend decide which video device to use
            }
            if (model.getDisplay().isSetMonitors()) {
                entity.setNumOfMonitors(model.getDisplay().getMonitors());
            }
            if (model.getDisplay().isSetSmartcardEnabled()) {
                entity.setSmartcardEnabled(model.getDisplay().isSmartcardEnabled());
            }
        }
        if (model.isSetMigrationDowntime()) {
            entity.setMigrationDowntime(mapMinusOneToNull(model.getMigrationDowntime()));
        }
        if (model.isSetMigration()) {
            MigrationOptionsMapper.copyMigrationOptions(model.getMigration(), entity);
        }
        if (model.isSetCustomCpuModel()) {
            entity.setCustomCpuName(model.getCustomCpuModel());
        }
        if (model.isSetCustomEmulatedMachine()) {
            entity.setCustomEmulatedMachine(model.getCustomEmulatedMachine());
        }
        if (model.isSetMemoryPolicy() && model.getMemoryPolicy().isSetGuaranteed()) {
            Long memGuaranteed = model.getMemoryPolicy().getGuaranteed() / BYTES_PER_MB;
            entity.setMinAllocatedMem(memGuaranteed.intValue());
        }
        if (model.isSetMemoryPolicy() && model.getMemoryPolicy().isSetMax()) {
            Long maxMemory = model.getMemoryPolicy().getMax() / BYTES_PER_MB;
            entity.setMaxMemorySizeMb(maxMemory.intValue());
        }
        if (model.isSetOs()) {
            Boot boot = model.getOs().getBoot();
            if (boot != null && boot.isSetDevices() && boot.getDevices().isSetDevices()) {
                entity.setDefaultBootSequence(VmMapper.map(model.getOs().getBoot(), null));
            }
        }
        if (model.isSetCustomCompatibilityVersion()) {
            Version entityMappedVersion = VersionMapper.map(model.getCustomCompatibilityVersion());
            entity.setCustomCompatibilityVersion(entityMappedVersion.isNotValid() ? null : entityMappedVersion);
        }
        if (model.isSetLease()) {
            entity.setLeaseStorageDomainId(StorageDomainLeaseMapper.map(model.getLease()));
        }

        if (model.isSetPlacementPolicy()) {
            if (model.getPlacementPolicy().isSetAffinity()) {
                // read migration policy
                entity.setMigrationSupport(map(model.getPlacementPolicy().getAffinity(), null));
            }
            // reset previous dedicated host or hosts
            Set<Guid> hostGuidsSet = new HashSet<>();

            // read multiple hosts if there are few
            if (model.getPlacementPolicy().isSetHosts()
                    && model.getPlacementPolicy().getHosts().getHosts().size() > 0) {
                for (Host currHost : model.getPlacementPolicy().getHosts().getHosts()) {
                    Guid hostGuid;
                    if (currHost.isSetId()) {
                        hostGuid = Guid.createGuidFromString(currHost.getId());
                    } else {
                        continue;
                    }
                    hostGuidsSet.add(hostGuid);
                }
            }
            entity.setDedicatedVmForVdsList(new LinkedList<>(hostGuidsSet));
        }
        if (model.isSetMemoryPolicy() && model.getMemoryPolicy().isSetBallooning()) {
            entity.setBalloonEnabled(model.getMemoryPolicy().isBallooning());
        }
    }

    /**
     * Common for VM and template
     */
    protected static void mapVmBaseModelToEntity(org.ovirt.engine.core.common.businessentities.VmBase entity, VmBase model) {
        mapCommonModelToEntity(entity, model);

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetCluster() && model.getCluster().getId() != null) {
            entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetOs()) {
            if (model.getOs().isSetType()) {
                entity.setOsId(VmMapper.mapOsType(model.getOs().getType()));
            }
            if (model.getOs().isSetKernel()) {
                entity.setKernelUrl(model.getOs().getKernel());
            }
            if (model.getOs().isSetInitrd()) {
                entity.setInitrdUrl(model.getOs().getInitrd());
            }
            if (model.getOs().isSetCmdline()) {
                entity.setKernelParams(model.getOs().getCmdline());
            }
        }
        if (model.isSetBios()) {
            if (model.getBios().isSetBootMenu()) {
                entity.setBootMenuEnabled(model.getBios().getBootMenu().isEnabled());
            }
            if (model.getBios().isSetType()) {
                entity.setBiosType(map(model.getBios().getType(), null));
            }
        }
        if (model.isSetCpuShares()) {
            entity.setCpuShares(model.getCpuShares());
        }

        if (model.isSetDisplay()) {
            if (model.getDisplay().isSetAllowOverride()) {
                entity.setAllowConsoleReconnect(model.getDisplay().isAllowOverride());
            }

            if (model.getDisplay().isSetKeyboardLayout()) {
                String layout = model.getDisplay().getKeyboardLayout();
                if (layout.isEmpty()) {
                    layout = null;  // uniquely represent unset keyboard layout as null
                }
                entity.setVncKeyboardLayout(layout);
            }

            if (model.getDisplay().isSetFileTransferEnabled()) {
                entity.setSpiceFileTransferEnabled(model.getDisplay().isFileTransferEnabled());
            }
            if (model.getDisplay().isSetCopyPasteEnabled()) {
                entity.setSpiceCopyPasteEnabled(model.getDisplay().isCopyPasteEnabled());
            }
            if (model.getDisplay().isSetDisconnectAction()) {
                DisplayDisconnectAction action = DisplayDisconnectAction.fromValue(model.getDisplay().getDisconnectAction());
                entity.setConsoleDisconnectAction(map(action, null));
            }
        }

        if (model.isSetTimeZone()) {
            if (model.getTimeZone().isSetName()) {
                String timezone = model.getTimeZone().getName();
                if (timezone.isEmpty()) {
                    timezone = null; // normalize default timezone representation
                }
                entity.setTimeZone(timezone);
            }
        }
        if (model.isSetOrigin()) {
            entity.setOrigin(VmMapper.map(model.getOrigin(), (OriginType) null));
        }
        if (model.isSetStateless()) {
            entity.setStateless(model.isStateless());
        }
        if (model.isSetDeleteProtected()) {
            entity.setDeleteProtected(model.isDeleteProtected());
        }
        if (model.isSetSso() && model.getSso().isSetMethods()) {
            entity.setSsoMethod(SsoMapper.map(model.getSso(), null));
        }
        if (model.isSetType()) {
            entity.setVmType(mapVmType(model.getType()));
        }
        if (model.isSetStorageErrorResumeBehaviour()) {
            entity.setResumeBehavior(mapResumeBehavior(model.getStorageErrorResumeBehaviour()));
        }
        if (model.isSetTunnelMigration()) {
            entity.setTunnelMigration(model.isTunnelMigration());
        }
        if (model.isSetSerialNumber()) {
            SerialNumberMapper.copySerialNumber(model.getSerialNumber(), entity);
        }
        if (model.isSetStartPaused()) {
            entity.setRunAndPause(model.isStartPaused());
        }
        if (model.isSetCpuProfile() && model.getCpuProfile().isSetId()) {
            entity.setCpuProfileId(GuidUtils.asGuid(model.getCpuProfile().getId()));
        }
        if (model.isSetCustomProperties()) {
            entity.setCustomProperties(CustomPropertiesParser.parse(model.getCustomProperties().getCustomProperties()));
        }
        if (model.isSetLargeIcon() && model.getLargeIcon().isSetId()) {
            entity.setLargeIconId(GuidUtils.asGuid(model.getLargeIcon().getId()));
        }
        if (model.isSetSmallIcon() && model.getSmallIcon().isSetId()) {
            entity.setSmallIconId(GuidUtils.asGuid(model.getSmallIcon().getId()));
        }

        if (model.isSetQuota()) {
            if (model.getQuota().isSetId()) {
                entity.setQuotaId(GuidUtils.asGuid(model.getQuota().getId()));
            } else {
                entity.setQuotaId(null);
            }
        }

        if (model.isSetLease()) {
            entity.setLeaseStorageDomainId(StorageDomainLeaseMapper.map(model.getLease()));
        }

        if (model.isSetMultiQueuesEnabled()) {
            entity.setMultiQueuesEnabled(model.isMultiQueuesEnabled());
        }

        if (model.isSetVirtioScsiMultiQueues()) {
            entity.setVirtioScsiMultiQueues(model.getVirtioScsiMultiQueues());
        } else if (model.isSetVirtioScsiMultiQueuesEnabled()) {
            entity.setVirtioScsiMultiQueues(
                    model.isVirtioScsiMultiQueuesEnabled() ? -1 : 0);
        }

        if (model.isSetCpu() && model.getCpu().isSetMode()) {
            entity.setUseHostCpuFlags(model.getCpu().getMode() == CpuMode.HOST_PASSTHROUGH);
        }
        if (model.isSetCpu() && model.getCpu().isSetCpuTune()) {
            entity.setCpuPinning(cpuTuneToString(model.getCpu().getCpuTune()));
        }

        if (model.isSetAutoPinningPolicy()) {
            entity.setCpuPinningPolicy(VmMapper.map(model.getAutoPinningPolicy()));
        }
        // Override the deprecated value if both given as input.
        if (model.isSetCpuPinningPolicy()) {
            entity.setCpuPinningPolicy(VmMapper.map(model.getCpuPinningPolicy()));
        }
    }

    /**
     * Common for VM, template and instance type
     */
    protected static void mapCommonEntityToModel(VmBase model, org.ovirt.engine.core.common.businessentities.VmBase entity) {
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setMemory((long) entity.getMemSizeMb() * BYTES_PER_MB);

        Io io = model.getIo();
        if (io == null) {
            io = new Io();
            model.setIo(io);
        }
        io.setThreads(entity.getNumOfIoThreads());

        if (entity.getCreationDate() != null) {
            model.setCreationTime(DateMapper.map(entity.getCreationDate(), null));
        }

        if (entity.getUsbPolicy() != null) {
            Usb usb = new Usb();
            usb.setEnabled(UsbMapperUtils.getIsUsbEnabled(entity.getUsbPolicy()));
            UsbType usbType = UsbMapperUtils.getUsbType(entity.getUsbPolicy());
            if (usbType != null) {
                usb.setType(usbType);
            }
            model.setUsb(usb);
        }

        CpuTopology topology = new CpuTopology();
        topology.setSockets(entity.getNumOfSockets());
        topology.setCores(entity.getCpuPerSocket());
        topology.setThreads(entity.getThreadsPerCpu());
        model.setCpu(new Cpu());
        model.getCpu().setTopology(topology);

        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());

        model.setMigrationDowntime(mapNullToMinusOne(entity.getMigrationDowntime()));
        model.setMigration(MigrationOptionsMapper.map(entity, null));

        if (entity.getCustomEmulatedMachine() != null) {
            model.setCustomEmulatedMachine(entity.getCustomEmulatedMachine());
        }

        if (entity.getCustomCpuName() != null) {
            model.setCustomCpuModel(entity.getCustomCpuName());
        }

        MemoryPolicy policy = new MemoryPolicy();
        policy.setGuaranteed((long)entity.getMinAllocatedMem() * (long)BYTES_PER_MB);
        policy.setMax((long)entity.getMaxMemorySizeMb() * (long)BYTES_PER_MB);
        policy.setBallooning(entity.isBalloonEnabled());
        model.setMemoryPolicy(policy);

        if (entity.getCustomCompatibilityVersion() != null) {
            model.setCustomCompatibilityVersion(VersionMapper.map(entity.getCustomCompatibilityVersion()));
        }

        model.setLease(StorageDomainLeaseMapper.map(entity.getLeaseStorageDomainId()));

        if (model.getPlacementPolicy() == null) {
            model.setPlacementPolicy(new VmPlacementPolicy());
        }
        VmAffinity vmAffinity = map(entity.getMigrationSupport(), null);
        if (vmAffinity != null) {
            model.getPlacementPolicy().setAffinity(vmAffinity);
        }
        if (!entity.getDedicatedVmForVdsList().isEmpty()) {
            Hosts hostsList = new Hosts();
            for (Guid hostGuid : entity.getDedicatedVmForVdsList()) {
                Host newHost = new Host();
                newHost.setId(hostGuid.toString());
                hostsList.getHosts().add(newHost);
            }
            model.getPlacementPolicy().setHosts(hostsList);
        }
    }

    /**
     * Common for VM and template
     */
    protected static void mapVmBaseEntityToModel(VmBase model, org.ovirt.engine.core.common.businessentities.VmBase entity) {
        mapCommonEntityToModel(model, entity);
        model.setComment(entity.getComment());

        if (entity.getClusterId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getClusterId().toString());
            model.setCluster(cluster);
        }

        if (entity.getVmType() != null) {
            model.setType(mapVmType(entity.getVmType()));
        }

        if (entity.getResumeBehavior() != null) {
            model.setStorageErrorResumeBehaviour(mapResumeBehavior(entity.getResumeBehavior()));
        }

        if (entity.getOrigin() != null) {
            model.setOrigin(map(entity.getOrigin(), null));
        }

        model.setBios(new Bios());
        model.getBios().setBootMenu(new BootMenu());
        model.getBios().getBootMenu().setEnabled(entity.isBootMenuEnabled());

        if (entity.getBiosType() != null) {
            model.getBios().setType(map(entity.getBiosType(), null));
        }

        if(entity.getTimeZone() != null) {
            model.setTimeZone(new TimeZone());
            model.getTimeZone().setName(entity.getTimeZone());
        }

        if (entity.getVmInit() != null && entity.getVmInit().getDomain() != null && StringUtils.isNotBlank(entity.getVmInit().getDomain())) {
            Domain domain = new Domain();
            domain.setName(entity.getVmInit().getDomain());
            model.setDomain(domain);
        }

        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        model.setSso(SsoMapper.map(entity.getSsoMethod(), null));

        model.setTunnelMigration(entity.getTunnelMigration());

        if (entity.getSerialNumberPolicy() != null) {
            model.setSerialNumber(SerialNumberMapper.map(entity, null));
        }

        model.setStartPaused(entity.isRunAndPause());
        if (entity.getCpuProfileId() != null) {
            CpuProfile cpuProfile = new CpuProfile();
            cpuProfile.setId(entity.getCpuProfileId().toString());
            model.setCpuProfile(cpuProfile);
        }

        if (!StringUtils.isEmpty(entity.getCustomProperties())) {
            CustomProperties hooks = new CustomProperties();
            hooks.getCustomProperties().addAll(CustomPropertiesParser.parse(entity.getCustomProperties(), false));
            model.setCustomProperties(hooks);
        }

        model.setCpuShares(entity.getCpuShares());

        if (entity.getLargeIconId() != null) {
            if (!model.isSetLargeIcon()) {
                model.setLargeIcon(new Icon());
            }
            model.getLargeIcon().setId(entity.getLargeIconId().toString());
        }
        if (entity.getSmallIconId() != null) {
            if (!model.isSetSmallIcon()) {
                model.setSmallIcon(new Icon());
            }
            model.getSmallIcon().setId(entity.getSmallIconId().toString());
        }

        if (entity.getQuotaId()!=null) {
            Quota quota = new Quota();
            quota.setId(entity.getQuotaId().toString());
            model.setQuota(quota);
        }

        model.setLease(StorageDomainLeaseMapper.map(entity.getLeaseStorageDomainId()));

        model.setMultiQueuesEnabled(entity.isMultiQueuesEnabled());

        switch (entity.getVirtioScsiMultiQueues()) {
        case -1:
            model.setVirtioScsiMultiQueuesEnabled(true);
            break;
        case 0:
            model.setVirtioScsiMultiQueuesEnabled(false);
            break;
        default:
            model.setVirtioScsiMultiQueuesEnabled(true);
            model.setVirtioScsiMultiQueues(entity.getVirtioScsiMultiQueues());
            break;
        }

        if(entity.isUseHostCpuFlags()) {
            model.getCpu().setMode(CpuMode.HOST_PASSTHROUGH);
        }
        model.getCpu().setCpuTune(stringToCpuTune(entity.getCpuPinning()));

        model.setAutoPinningPolicy(VmMapper.map(entity.getCpuPinningPolicy(), null));
        model.setCpuPinningPolicy(VmMapper.map(entity.getCpuPinningPolicy()));
    }

    @Mapping(from = DisplayDisconnectAction.class, to = ConsoleDisconnectAction.class)
    public static ConsoleDisconnectAction map(DisplayDisconnectAction action, ConsoleDisconnectAction incoming) {
        if (action == null) {
            return ConsoleDisconnectAction.LOCK_SCREEN;
        }
        switch (action) {
            case NONE:
                return ConsoleDisconnectAction.NONE;
            case LOCK_SCREEN:
                return ConsoleDisconnectAction.LOCK_SCREEN;
            case LOGOUT:
                return ConsoleDisconnectAction.LOGOUT;
            case REBOOT:
                return ConsoleDisconnectAction.REBOOT;
            case SHUTDOWN:
                return ConsoleDisconnectAction.SHUTDOWN;
            default:
                return null;
        }
    }

    @Mapping(from = ConsoleDisconnectAction.class, to = DisplayDisconnectAction.class)
    public static DisplayDisconnectAction map(ConsoleDisconnectAction action, DisplayDisconnectAction incoming) {
        if (action == null) {
            return DisplayDisconnectAction.LOCK_SCREEN;
        }
        switch (action) {
            case NONE:
                return DisplayDisconnectAction.NONE;
            case LOCK_SCREEN:
                return DisplayDisconnectAction.LOCK_SCREEN;
            case LOGOUT:
                return DisplayDisconnectAction.LOGOUT;
            case REBOOT:
                return DisplayDisconnectAction.REBOOT;
            case SHUTDOWN:
                return DisplayDisconnectAction.SHUTDOWN;
            default:
                return null;
        }
    }

    @Mapping(from = OriginType.class, to = String.class)
    public static String map(OriginType type, String incoming) {
        return type.name().toLowerCase();
    }

    public static org.ovirt.engine.core.common.businessentities.VmResumeBehavior mapResumeBehavior(VmStorageErrorResumeBehaviour resumeBehavior) {
        if (resumeBehavior == null) {
            return null;
        }
        switch (resumeBehavior) {
            case AUTO_RESUME:
                return org.ovirt.engine.core.common.businessentities.VmResumeBehavior.AUTO_RESUME;
            case LEAVE_PAUSED:
                return org.ovirt.engine.core.common.businessentities.VmResumeBehavior.LEAVE_PAUSED;
            case KILL:
                return org.ovirt.engine.core.common.businessentities.VmResumeBehavior.KILL;
            default:
                throw new IllegalArgumentException("Unknown resume behavior \"" + resumeBehavior + "\"");
        }
    }

    public static VmStorageErrorResumeBehaviour mapResumeBehavior(org.ovirt.engine.core.common.businessentities.VmResumeBehavior resumeBehavior) {
        if (resumeBehavior == null) {
            return null;
        }

        switch (resumeBehavior) {
            case AUTO_RESUME:
                return VmStorageErrorResumeBehaviour.AUTO_RESUME;
            case LEAVE_PAUSED:
                return VmStorageErrorResumeBehaviour.LEAVE_PAUSED;
            case KILL:
                return VmStorageErrorResumeBehaviour.KILL;
            default:
                throw new IllegalArgumentException("Unknown resume behavior \"" + resumeBehavior + "\"");
        }
    }

    public static org.ovirt.engine.core.common.businessentities.VmType mapVmType(VmType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
        case DESKTOP:
            return org.ovirt.engine.core.common.businessentities.VmType.Desktop;
        case SERVER:
            return org.ovirt.engine.core.common.businessentities.VmType.Server;
        case HIGH_PERFORMANCE:
            return org.ovirt.engine.core.common.businessentities.VmType.HighPerformance;
        default:
            throw new IllegalArgumentException("Unknown virtual machine type \"" + type + "\"");
        }
    }

    public static VmType mapVmType(org.ovirt.engine.core.common.businessentities.VmType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
        case Desktop:
            return VmType.DESKTOP;
        case Server:
            return VmType.SERVER;
        case HighPerformance:
            return VmType.HIGH_PERFORMANCE;
        default:
            throw new IllegalArgumentException("Unknown virtual machine type \"" + type + "\"");
        }
    }

    @Mapping(from = VmAffinity.class, to = MigrationSupport.class)
    public static MigrationSupport map(VmAffinity vmAffinity, MigrationSupport template) {
        if(vmAffinity!=null){
            switch (vmAffinity) {
            case MIGRATABLE:
                return MigrationSupport.MIGRATABLE;
            case USER_MIGRATABLE:
                return MigrationSupport.IMPLICITLY_NON_MIGRATABLE;
            case PINNED:
                return MigrationSupport.PINNED_TO_HOST;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = MigrationSupport.class, to = VmAffinity.class)
    public static VmAffinity map(MigrationSupport migrationSupport, VmAffinity template) {
        if(migrationSupport!=null){
            switch (migrationSupport) {
            case MIGRATABLE:
                return VmAffinity.MIGRATABLE;
            case IMPLICITLY_NON_MIGRATABLE:
                return VmAffinity.USER_MIGRATABLE;
            case PINNED_TO_HOST:
                return VmAffinity.PINNED;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = BiosType.class, to = org.ovirt.engine.core.common.businessentities.BiosType.class)
    public static org.ovirt.engine.core.common.businessentities.BiosType map(BiosType biosType, org.ovirt.engine.core.common.businessentities.BiosType template) {
        if (biosType == null) {
            return null;
        }
        switch (biosType) {
            case I440FX_SEA_BIOS:
                return org.ovirt.engine.core.common.businessentities.BiosType.I440FX_SEA_BIOS;
            case Q35_SEA_BIOS:
                return org.ovirt.engine.core.common.businessentities.BiosType.Q35_SEA_BIOS;
            case Q35_OVMF:
                return org.ovirt.engine.core.common.businessentities.BiosType.Q35_OVMF;
            case Q35_SECURE_BOOT:
                return org.ovirt.engine.core.common.businessentities.BiosType.Q35_SECURE_BOOT;
            default:
                return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.BiosType.class, to = BiosType.class)
    public static BiosType map(org.ovirt.engine.core.common.businessentities.BiosType biosType, org.ovirt.engine.api.model.BiosType template) {
        if (biosType == null) {
            return null;
        }
        switch (biosType) {
            case I440FX_SEA_BIOS:
                return BiosType.I440FX_SEA_BIOS;
            case Q35_SEA_BIOS:
                return BiosType.Q35_SEA_BIOS;
            case Q35_OVMF:
                return BiosType.Q35_OVMF;
            case Q35_SECURE_BOOT:
                return BiosType.Q35_SECURE_BOOT;
            default:
                return null;
        }
    }

    static String cpuTuneToString(final CpuTune tune) {
        if (tune.getVcpuPins() == null) {
            return "";
        }
        return tune.getVcpuPins().getVcpuPins().stream()
                .map(pin -> String.join("#", pin.getVcpu().toString(), pin.getCpuSet()))
                .collect(Collectors.joining("_"));
    }

    /**
     * Maps the stringified CPU-pinning to the API format.
     */
    static CpuTune stringToCpuTune(String cpuPinning) {
        if(StringUtils.isEmpty(cpuPinning)) {
            return null;
        }
        final CpuTune cpuTune = new CpuTune();
        VcpuPins pins = new VcpuPins();
        for(String strCpu : cpuPinning.split("_")) {
            VcpuPin pin = stringToVCpupin(strCpu);
            pins.getVcpuPins().add(pin);
        }
        cpuTune.setVcpuPins(pins);

        return cpuTune;
    }

    static VcpuPin stringToVCpupin(final String strCpu) {
        final String[] strPin = strCpu.split("#");
        if (strPin.length != 2) {
            throw new IllegalArgumentException("Bad format: " + strCpu);
        }
        final VcpuPin pin = new VcpuPin();
        try {
            pin.setVcpu(Integer.parseInt(strPin[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad format: " + strCpu, e);
        }
        if (strPin[1].matches("\\^?(\\d+(\\-\\d+)?)(,\\^?((\\d+(\\-\\d+)?)))*")) {
            pin.setCpuSet(strPin[1]);
        } else {
            throw new IllegalArgumentException("Bad format: " + strPin[1]);
        }
        return pin;
    }
}
