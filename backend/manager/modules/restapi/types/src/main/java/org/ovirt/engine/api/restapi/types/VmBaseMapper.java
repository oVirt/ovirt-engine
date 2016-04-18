package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapMinusOneToNull;
import static org.ovirt.engine.api.restapi.types.IntegerMapper.mapNullToMinusOne;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Bios;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootMenu;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.DisplayDisconnectAction;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.model.Io;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VmBase;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.UsbMapperUtils;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.OriginType;

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
            if (model.getDisplay().isSetSingleQxlPci()) {
                entity.setSingleQxlPci(model.getDisplay().isSingleQxlPci());
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
        if (model.isSetOs()) {
            Boot boot = model.getOs().getBoot();
            if (boot != null && boot.isSetDevices() && boot.getDevices().isSetDevices()) {
                entity.setDefaultBootSequence(VmMapper.map(model.getOs().getBoot(), null));
            }
        }
        if (model.isSetCustomCompatibilityVersion()) {
            entity.setCustomCompatibilityVersion(VersionMapper.map(model.getCustomCompatibilityVersion()));
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
            entity.setVmType(VmMapper.map(model.getType(), null));
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
        model.setMemoryPolicy(policy);

        if (entity.getCustomCompatibilityVersion() != null) {
            model.setCustomCompatibilityVersion(VersionMapper.map(entity.getCustomCompatibilityVersion()));
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
            model.setType(map(entity.getVmType(), null));
        }

        if (entity.getOrigin() != null) {
            model.setOrigin(map(entity.getOrigin(), null));
        }

        model.setBios(new Bios());
        model.getBios().setBootMenu(new BootMenu());
        model.getBios().getBootMenu().setEnabled(entity.isBootMenuEnabled());

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

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmType.class, to = VmType.class)
    public static VmType map(org.ovirt.engine.core.common.businessentities.VmType type, VmType incoming) {
        switch (type) {
        case Desktop:
            return VmType.DESKTOP;
        case Server:
            return VmType.SERVER;
        default:
            return null;
        }
    }
}
