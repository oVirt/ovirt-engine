package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.core.compat.NGuid.createGuidFromString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuMode;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.GuestInfo;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.IPs;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.PayloadFile;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VCpuPin;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.UsbMapperUtils;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

public class VmMapper {

    private static final int BYTES_PER_MB = 1024 * 1024;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_MEMORY_SIZE = 10 * 1024;

    // REVISIT once #712661 implemented by BE
    @Mapping(from = VmTemplate.class, to = VmStatic.class)
    public static VmStatic map(VmTemplate entity, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();
        staticVm.setId(NGuid.Empty);
        staticVm.setVmtGuid(entity.getId());
        staticVm.setDomain(entity.getDomain());
        staticVm.setVdsGroupId(entity.getVdsGroupId());
        staticVm.setMemSizeMb(entity.getMemSizeMb());
        staticVm.setOs(entity.getOs());
        staticVm.setNiceLevel(entity.getNiceLevel());
        staticVm.setFailBack(entity.isFailBack());
        staticVm.setAutoStartup(entity.isAutoStartup());
        staticVm.setStateless(entity.isStateless());
        staticVm.setDeleteProtected(entity.isDeleteProtected());
        staticVm.setSmartcardEnabled(entity.isSmartcardEnabled());
        staticVm.setAutoStartup(entity.isAutoStartup());
        staticVm.setDefaultBootSequence(entity.getDefaultBootSequence());
        staticVm.setVmType(entity.getVmType());
        entity.setDefaultDisplayType(entity.getDefaultDisplayType());
        staticVm.setIsoPath(entity.getIsoPath());
        staticVm.setNumOfSockets(entity.getNumOfSockets());
        staticVm.setCpuPerSocket(entity.getCpuPerSocket());
        staticVm.setKernelUrl(entity.getKernelUrl());
        staticVm.setKernelParams(entity.getKernelParams());
        staticVm.setInitrdUrl(entity.getInitrdUrl());
        staticVm.setTimeZone(entity.getTimeZone());
        staticVm.setNumOfMonitors(entity.getNumOfMonitors());
        staticVm.setAllowConsoleReconnect(entity.isAllowConsoleReconnect());
        staticVm.setPriority(entity.getPriority());
        staticVm.setUsbPolicy(entity.getUsbPolicy());
        staticVm.setTunnelMigration(entity.getTunnelMigration());
        staticVm.setVncKeyboardLayout(entity.getVncKeyboardLayout());
        return staticVm;
    }

    @Mapping(from = VM.class, to = VmStatic.class)
    public static VmStatic map(VM vm, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();
        if (vm.isSetName()) {
            staticVm.setName(vm.getName());
        }
        if (vm.isSetId()) {
            staticVm.setId(GuidUtils.asGuid(vm.getId()));
        }
        if (vm.isSetDescription()) {
            staticVm.setDescription(vm.getDescription());
        }
        if (vm.isSetMemory()) {
            staticVm.setMemSizeMb((int) (vm.getMemory() / BYTES_PER_MB));
        } else if (staticVm.getMemSizeMb()==0){
          //TODO: Get rid of this logic code when Backend supports default memory.
            staticVm.setMemSizeMb(DEFAULT_MEMORY_SIZE);
        }
        if (vm.isSetTemplate() && vm.getTemplate().getId() != null) {
            staticVm.setVmtGuid(GuidUtils.asGuid(vm.getTemplate().getId()));
        }
        if (vm.isSetCluster() && vm.getCluster().getId() != null) {
            staticVm.setVdsGroupId(GuidUtils.asGuid(vm.getCluster().getId()));
        }
        if (vm.isSetCpu()) {
            if (vm.getCpu().isSetMode()) {
                staticVm.setUseHostCpuFlags(CpuMode.fromValue(vm.getCpu().getMode()) == CpuMode.HOST_PASSTHROUGH);
            }
            if (vm.getCpu().isSetTopology()) {
                if (vm.getCpu().getTopology().getCores()!=null) {
                    staticVm.setCpuPerSocket(vm.getCpu().getTopology().getCores());
                }
                if (vm.getCpu().getTopology().getSockets()!=null) {
                    staticVm.setNumOfSockets(vm.getCpu().getTopology().getSockets());
                }
            }
            if (vm.getCpu().isSetCpuTune()) {
                staticVm.setCpuPinning(cpuTuneToString(vm.getCpu().getCpuTune()));
            }
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetType()) {
                OsType osType = OsType.fromValue(vm.getOs().getType());
                if (osType != null) {
                    staticVm.setOs(map(osType, null));
                }
            }
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().size() > 0) {
                staticVm.setDefaultBootSequence(map(vm.getOs().getBoot(), null));
            }
            if (vm.getOs().isSetKernel()) {
                staticVm.setKernelUrl(vm.getOs().getKernel());
            }
            if (vm.getOs().isSetInitrd()) {
                staticVm.setInitrdUrl(vm.getOs().getInitrd());
            }
            if (vm.getOs().isSetCmdline()) {
                staticVm.setKernelParams(vm.getOs().getCmdline());
            }
        }
        if (vm.isSetType()) {
            VmType vmType = VmType.fromValue(vm.getType());
            if (vmType != null) {
                staticVm.setVmType(map(vmType, null));
            }
        }
        if (vm.isSetStateless()) {
            staticVm.setStateless(vm.isStateless());
        }
        if (vm.isSetDeleteProtected()) {
            staticVm.setDeleteProtected(vm.isDeleteProtected());
        }
        if (vm.isSetHighAvailability()) {
            HighAvailability ha = vm.getHighAvailability();
            if (ha.isSetEnabled()) {
                staticVm.setAutoStartup(ha.isEnabled());
            }
            if (ha.isSetPriority()) {
                staticVm.setPriority(ha.getPriority());
            }
        }
        if (vm.isSetOrigin()) {
            staticVm.setOrigin(map(vm.getOrigin(), (OriginType)null));
        }
        if (vm.isSetDisplay()) {
            if (vm.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(vm.getDisplay().getType());
                if (displayType != null) {
                    staticVm.setDefaultDisplayType(map(displayType, null));
                }
            }
            if (vm.getDisplay().isSetMonitors()) {
                staticVm.setNumOfMonitors(vm.getDisplay().getMonitors());
            }
            if (vm.getDisplay().isSetAllowOverride()) {
                staticVm.setAllowConsoleReconnect(vm.getDisplay().isAllowOverride());
            }
            if (vm.getDisplay().isSetSmartcardEnabled()) {
                staticVm.setSmartcardEnabled(vm.getDisplay().isSmartcardEnabled());
            }
            if (vm.getDisplay().isSetKeyboardLayout()) {
                String layout = vm.getDisplay().getKeyboardLayout();
                if (layout.isEmpty()) {
                    layout = null;  // uniquely represent unset keyboard layout as null
                }
                staticVm.setVncKeyboardLayout(layout);
            }
        }
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetAffinity()) {
            VmAffinity vmAffinity = VmAffinity.fromValue(vm.getPlacementPolicy().getAffinity());
            if (vmAffinity!=null) {
                staticVm.setMigrationSupport(map(vmAffinity, null));
            }
        }
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()) {
            staticVm.setDedicatedVmForVds(createGuidFromString(vm.getPlacementPolicy().getHost().getId()));
        }
        if (vm.isSetDomain() && vm.getDomain().isSetName()) {
            staticVm.setDomain(vm.getDomain().getName());
        }
        if (vm.isSetMemoryPolicy() && vm.getMemoryPolicy().isSetGuaranteed()) {
            Long memGuaranteed = vm.getMemoryPolicy().getGuaranteed() / BYTES_PER_MB;
            staticVm.setMinAllocatedMem(memGuaranteed.intValue());
        }
        if (vm.isSetTimezone()) {
            staticVm.setTimeZone(vm.getTimezone());
        }
        if (vm.isSetCustomProperties() && vm.getCustomProperties().isSetCustomProperty()) {
            staticVm.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetQuota() && vm.getQuota().isSetId()) {
            staticVm.setQuotaId(GuidUtils.asGuid(vm.getQuota().getId()));
        }
        if (vm.isSetTunnelMigration()) {
            staticVm.setTunnelMigration(vm.isTunnelMigration());
        }
        return staticVm;
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

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VM.class, to = org.ovirt.engine.api.model.VM.class)
    public static VM map(org.ovirt.engine.core.common.businessentities.VM entity, VM template) {
        VM model = template != null ? template : new VM();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getVmDescription());
        model.setMemory((long) entity.getMemSizeMb() * BYTES_PER_MB);
        if (entity.getVmtGuid() != null) {
            model.setTemplate(new Template());
            model.getTemplate().setId(entity.getVmtGuid().toString());
        }
        if (entity.getStatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getStatus(), null)));
            if (entity.getStatus()==VMStatus.Paused) {
                model.getStatus().setDetail(entity.getVmPauseStatus().name().toLowerCase());
            }
        }
        if (entity.getVmOs() != null ||
            entity.getBootSequence() != null ||
            entity.getKernelUrl() != null ||
            entity.getInitrdUrl() != null ||
            entity.getKernelParams() != null) {
            OperatingSystem os = new OperatingSystem();
            if (entity.getVmOs() != null) {
                OsType osType = VmMapper.map(entity.getOs(), null);
                if (osType != null) {
                    os.setType(osType.value());
                }
            }
            os.setKernel(entity.getKernelUrl());
            os.setInitrd(entity.getInitrdUrl());
            os.setCmdline(entity.getKernelParams());
            model.setOs(os);
        }
        if (entity.getVdsGroupId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getVdsGroupId().toString());
            model.setCluster(cluster);
        }
        CpuTopology topology = new CpuTopology();
        topology.setSockets(entity.getNumOfSockets());
        topology.setCores(entity.getNumOfCpus() / entity.getNumOfSockets());
        final CPU cpu = new CPU();
        model.setCpu(cpu);
        if(entity.isUseHostCpuFlags()) {
            cpu.setMode(CpuMode.HOST_PASSTHROUGH.value());
        }
        cpu.setCpuTune(stringToCpuTune(entity.getCpuPinning()));
        cpu.setTopology(topology);
        if (entity.getVmPoolId() != null) {
            VmPool pool = new VmPool();
            pool.setId(entity.getVmPoolId().toString());
            model.setVmPool(pool);
        }
        if (entity.getDynamicData() != null && entity.getStatus().isRunningOrPaused()) {
            if (model.getOs() != null && entity.getBootSequence() != null) {
                for (Boot boot : map(entity.getBootSequence(), null)) {
                    model.getOs().getBoot().add(boot);
                }
            }
            if(entity.getRunOnVds() != null) {
                model.setHost(new Host());
                model.getHost().setId(entity.getRunOnVds().toString());
            }
            if (entity.getVmIp()!=null && !entity.getVmIp().isEmpty()) {
                model.setGuestInfo(new GuestInfo());
                model.getGuestInfo().setIps(new IPs());
                for (String item : entity.getVmIp().split(" ")) {
                    if (!item.equals("")) {
                        IP ip = new IP();
                        ip.setAddress(item.trim());
                        model.getGuestInfo().getIps().getIPs().add(ip);
                    }
                }
            }
            if (entity.getLastStartTime() != null) {
                model.setStartTime(DateMapper.map(entity.getLastStartTime(), null));
            }
            model.setDisplay(new Display());
            model.getDisplay().setType(map(entity.getDisplayType(), null));
            model.getDisplay().setAddress(entity.getDisplayIp());
            Integer displayPort = entity.getDisplay();
            model.getDisplay().setPort(displayPort==null || displayPort==-1 ? null : displayPort);
            Integer displaySecurePort = entity.getDisplaySecurePort();
            model.getDisplay().setSecurePort(displaySecurePort==null || displaySecurePort==-1 ? null : displaySecurePort);
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
        } else {
            if (model.getOs() != null) {
                for (Boot boot : map(entity.getDefaultBootSequence(), null)) {
                    model.getOs().getBoot().add(boot);
                }
            }
            if (entity.getDefaultDisplayType() != null) {
                model.setDisplay(new Display());
                model.getDisplay().setType(map(entity.getDefaultDisplayType(), null));
            }
        }
        if (model.getDisplay() != null) {
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
            model.getDisplay().setAllowOverride(entity.getAllowConsoleReconnect());
            model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
            model.getDisplay().setKeyboardLayout(entity.getVncKeyboardLayout());
        }
        model.setType(map(entity.getVmType(), null));
        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());
        if (entity.getOrigin() != null) {
            model.setOrigin(map(entity.getOrigin(), null));
        }
        if (entity.getVmCreationDate() != null) {
            model.setCreationTime(DateMapper.map(entity.getVmCreationDate(), null));
        }
        model.setPlacementPolicy(new VmPlacementPolicy());
        if(entity.getDedicatedVmForVds() !=null){
            model.getPlacementPolicy().setHost(new Host());
            model.getPlacementPolicy().getHost().setId(entity.getDedicatedVmForVds().toString());
        }
        VmAffinity vmAffinity = map(entity.getMigrationSupport(),null);
        if(vmAffinity !=null){
            model.getPlacementPolicy().setAffinity(vmAffinity.value());
        }
        if (entity.getVmDomain()!=null && !entity.getVmDomain().isEmpty()) {
            Domain domain = new Domain();
            domain.setName(entity.getVmDomain());
            model.setDomain(domain);
        }
        MemoryPolicy policy = new MemoryPolicy();
        policy.setGuaranteed((long)entity.getMinAllocatedMem() * (long)BYTES_PER_MB);
        model.setMemoryPolicy(policy);
        model.setTimezone(entity.getTimeZone());
        if (!StringUtils.isEmpty(entity.getCustomProperties())) {
            CustomProperties hooks = new CustomProperties();
            hooks.getCustomProperty().addAll(CustomPropertiesParser.parse(entity.getCustomProperties(), false));
            model.setCustomProperties(hooks);
        }
        if (entity.getUsbPolicy()!=null) {
            Usb usb = new Usb();
            usb.setEnabled(UsbMapperUtils.getIsUsbEnabled(entity.getUsbPolicy()));
            UsbType usbType = UsbMapperUtils.getUsbType(entity.getUsbPolicy());
            if (usbType != null) {
                usb.setType(usbType.value());
            }
            model.setUsb(usb);
        }
        if (entity.getQuotaId()!=null) {
            Quota quota = new Quota();
            quota.setId(entity.getQuotaId().toString());
            model.setQuota(quota);
        }
        model.setTunnelMigration(entity.getTunnelMigration());
        return model;
    }

    @Mapping(from = VM.class, to = RunVmOnceParams.class)
    public static RunVmOnceParams map(VM vm, RunVmOnceParams template) {
        RunVmOnceParams params = template != null ? template : new RunVmOnceParams();
        if (vm.isSetStateless() && vm.isStateless()) {
            params.setRunAsStateless(true);
        }
        if (vm.isSetDisplay() && vm.getDisplay().isSetType()) {
            DisplayType displayType = DisplayType.fromValue(vm.getDisplay().getType());
            if (displayType != null) {
                params.setUseVnc(displayType == DisplayType.VNC);
            }
        }
        if (vm.isSetOs() && vm.getOs().getBoot().size() > 0) {
            params.setBootSequence(map(vm.getOs().getBoot(), null));
        }
        if (vm.isSetCdroms() && vm.getCdroms().isSetCdRoms()) {
            String file = vm.getCdroms().getCdRoms().get(0).getFile().getId();
            if (file != null) {
                params.setDiskPath(file);
            }
        }
        if (vm.isSetFloppies() && vm.getFloppies().isSetFloppies()) {
            String file = vm.getFloppies().getFloppies().get(0).getFile().getId();
            if (file != null) {
                params.setFloppyPath(file);
            }
        }
        if (vm.isSetCustomProperties() && vm.getCustomProperties().isSetCustomProperty()) {
            params.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().size() > 0) {
                params.setBootSequence(map(vm.getOs().getBoot(), null));
            }
            if (vm.getOs().isSetKernel()) {
                params.setkernel_url(vm.getOs().getKernel());
            }
            if (vm.getOs().isSetInitrd()) {
                params.setinitrd_url(vm.getOs().getInitrd());
            }
            if (vm.getOs().isSetCmdline()) {
                params.setkernel_params(vm.getOs().getCmdline());
            }
        }
        if (vm.isSetDomain() && vm.getDomain().isSetName()) {
            params.setSysPrepDomainName(vm.getDomain().getName());
            if (vm.getDomain().isSetUser()) {
                if (vm.getDomain().getUser().isSetUserName()) {
                    params.setSysPrepUserName(vm.getDomain().getUser().getUserName());
                }
                if (vm.getDomain().getUser().isSetPassword()) {
                    params.setSysPrepPassword(vm.getDomain().getUser().getPassword());
                }
            }
        }

        return params;
    }

    @Mapping(from = String.class, to = CustomProperties.class)
    public static CustomProperties map(String entity, CustomProperties template) {
        CustomProperties model = template != null ? template : new CustomProperties();
        if (entity != null) {
            for (String envStr : entity.split(";", -1)) {
                String[] parts = envStr.split("=", 2);
                if (parts.length >= 1) {
                    CustomProperty env = new CustomProperty();
                    env.setName(parts[0]);
                    if (parts.length == 1) {
                        env.setValue(parts[1]);
                    }
                    model.getCustomProperty().add(env);
                }
            }
        }
        return model;
    }

    @Mapping(from = CustomProperties.class, to = String.class)
    public static String map(CustomProperties model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (CustomProperty env : model.getCustomProperty()) {
            String envStr = map(env, null);
            if (envStr != null) {
                if (buf.length() > 0) {
                    buf.append(";");
                }
                buf.append(envStr);
            }
        }
        return buf.toString();
    }

    @Mapping(from = CustomProperty.class, to = String.class)
    public static String map(CustomProperty model, String template) {
        if (model.isSetName()) {
            String ret = model.getName() + "=";
            if (model.isSetValue()) {
                ret += model.getValue();
            }
            return ret;
        } else {
            return template;
        }
    }

    @Mapping(from = VmType.class, to = org.ovirt.engine.core.common.businessentities.VmType.class)
    public static org.ovirt.engine.core.common.businessentities.VmType map(VmType type,
                                      org.ovirt.engine.core.common.businessentities.VmType incoming) {
        switch (type) {
        case DESKTOP:
            return org.ovirt.engine.core.common.businessentities.VmType.Desktop;
        case SERVER:
            return org.ovirt.engine.core.common.businessentities.VmType.Server;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.VmType type, String incoming) {
        switch (type) {
        case Desktop:
            return VmType.DESKTOP.value();
        case Server:
            return VmType.SERVER.value();
        default:
            return null;
        }
    }

    @Mapping(from = DisplayType.class, to = org.ovirt.engine.core.common.businessentities.DisplayType.class)
    public static org.ovirt.engine.core.common.businessentities.DisplayType map(DisplayType type, org.ovirt.engine.core.common.businessentities.DisplayType incoming) {
        switch(type) {
        case VNC:
            return org.ovirt.engine.core.common.businessentities.DisplayType.vnc;
        case SPICE:
            return org.ovirt.engine.core.common.businessentities.DisplayType.qxl;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DisplayType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.DisplayType type, String incoming) {
        switch(type) {
        case vnc:
            return DisplayType.VNC.value();
        case qxl:
            return DisplayType.SPICE.value();
        default:
            return null;
        }
    }

    @Mapping(from = String.class, to = OriginType.class)
    public static OriginType map(String type, OriginType incoming) {
        try {
            return OriginType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Mapping(from = OriginType.class, to = String.class)
    public static String map(OriginType type, String incoming) {
        return type.name().toLowerCase();
    }

    @Mapping(from = org.ovirt.engine.api.model.VmDeviceType.class, to = VmDeviceType.class)
    public static VmDeviceType map(org.ovirt.engine.api.model.VmDeviceType deviceType, VmDeviceType template) {
        switch (deviceType) {
            case FLOPPY:            return VmDeviceType.FLOPPY;
            case CDROM:             return VmDeviceType.CDROM;
            default:                return null;
        }
    }

    @Mapping(from = VmDeviceType.class, to = org.ovirt.engine.api.model.VmDeviceType.class)
    public static org.ovirt.engine.api.model.VmDeviceType map(VmDeviceType deviceType, org.ovirt.engine.api.model.VmDeviceType template) {
        switch (deviceType) {
            case FLOPPY:            return org.ovirt.engine.api.model.VmDeviceType.FLOPPY;
            case CDROM:             return org.ovirt.engine.api.model.VmDeviceType.CDROM;
            default:                return null;
        }
    }

    @Mapping(from = VMStatus.class, to = VmStatus.class)
    public static VmStatus map(VMStatus entityStatus, VmStatus template) {
        switch (entityStatus) {
        case Unassigned:            return VmStatus.UNASSIGNED;
        case Down:                  return VmStatus.DOWN;
        case Up:                    return VmStatus.UP;
        case PoweringUp:            return VmStatus.POWERING_UP;
        case PoweredDown:           return VmStatus.POWERED_DOWN;
        case Paused:                return VmStatus.PAUSED;
        case MigratingFrom:         return VmStatus.MIGRATING;
        case MigratingTo:           return VmStatus.MIGRATING;
        case Unknown:               return VmStatus.UNKNOWN;
        case NotResponding:         return VmStatus.NOT_RESPONDING;
        case WaitForLaunch:         return VmStatus.WAIT_FOR_LAUNCH;
        case RebootInProgress:      return VmStatus.REBOOT_IN_PROGRESS;
        case PreparingForHibernate:
        case SavingState:           return VmStatus.SAVING_STATE;
        case RestoringState:        return VmStatus.RESTORING_STATE;
        case Suspended:             return VmStatus.SUSPENDED;
        case ImageLocked:           return VmStatus.IMAGE_LOCKED;
        case PoweringDown:          return VmStatus.POWERING_DOWN;
        default:                    return null;
        }
    }

    @Mapping(from = BootSequence.class, to = List.class)
    public static List<Boot> map(BootSequence bootSequence,
            List<Boot> template) {
        List<Boot> boots = template != null ? template
                : new ArrayList<Boot>();
        switch (bootSequence) {
        case C:
            boots.add(getBoot(BootDevice.HD));
            break;
        case DC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case N:
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CDN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CND:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case DCN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DNC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case NCD:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case NDC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case CD:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case D:
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case CN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case NC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case ND:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        }
        return boots;
    }

    private static Boot getBoot(BootDevice device) {
        Boot boot = new Boot();
        boot.setDev(device.value());
        return boot;
    }

    @Mapping(from = Boot.class, to = List.class)
    public static BootSequence map(List<Boot> boot, BootSequence template) {
        Set<BootDevice> devSet = new LinkedHashSet<BootDevice>();
        for (Boot b : boot) {
            if (b.isSetDev()) {
                BootDevice dev = BootDevice.fromValue(b.getDev());
                if (dev != null) {
                    devSet.add(dev);
                }
            }
        }

        List<BootDevice> devs = new ArrayList<BootDevice>(devSet);
        if (devs.size() == 1) {
            switch (devs.get(0)) {
            case CDROM:
                return BootSequence.D;
            case HD:
                return BootSequence.C;
            case NETWORK:
                return BootSequence.N;
            }
        } else if (devs.size() == 2) {
            switch (devs.get(0)) {
            case CDROM:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.DC;
                case NETWORK:
                    return BootSequence.DN;
                }
                break;
            case HD:
                switch (devs.get(1)) {
                case CDROM:
                    return BootSequence.CD;
                case NETWORK:
                    return BootSequence.CN;
                }
                break;
            case NETWORK:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.NC;
                case CDROM:
                    return BootSequence.ND;
                }
                break;
            }
        } else if (devs.size() == 3) {
            switch (devs.get(0)) {
            case CDROM:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.DCN;
                case NETWORK:
                    return BootSequence.DNC;
                }
                break;
            case HD:
                switch (devs.get(1)) {
                case CDROM:
                    return BootSequence.CDN;
                case NETWORK:
                    return BootSequence.CND;
                }
                break;
            case NETWORK:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.NCD;
                case CDROM:
                    return BootSequence.NDC;
                }
                break;
            }
        }
        return null;
    }

    @Mapping(from = VmOsType.class, to = OsType.class)
    public static OsType map(VmOsType type, OsType incoming) {
        switch (type) {
        case Unassigned:
            return OsType.UNASSIGNED;
        case WindowsXP:
            return OsType.WINDOWS_XP;
        case Windows2003:
            return OsType.WINDOWS_2003;
        case Windows2008:
            return OsType.WINDOWS_2008;
        case Other:
            return OsType.OTHER;
        case OtherLinux:
            return OsType.OTHER_LINUX;
        case RHEL5:
            return OsType.RHEL_5;
        case RHEL4:
            return OsType.RHEL_4;
        case RHEL3:
            return OsType.RHEL_3;
        case Windows2003x64:
            return OsType.WINDOWS_2003X64;
        case Windows7:
            return OsType.WINDOWS_7;
        case Windows7x64:
            return OsType.WINDOWS_7X64;
        case RHEL5x64:
            return OsType.RHEL_5X64;
        case RHEL4x64:
            return OsType.RHEL_4X64;
        case RHEL3x64:
            return OsType.RHEL_3X64;
        case Windows2008x64:
            return OsType.WINDOWS_2008X64;
        case Windows2008R2x64:
            return OsType.WINDOWS_2008R2;
        case RHEL6:
            return OsType.RHEL_6;
        case RHEL6x64:
            return OsType.RHEL_6X64;
        case Windows8:
            return OsType.WINDOWS_8;
        case Windows8x64:
            return OsType.WINDOWS_8X64;
        case Windows2012x64:
            return OsType.WINDOWS_2012X64;

        default:
            return null;
        }
    }

    @Mapping(from = OsType.class, to = VmOsType.class)
    public static VmOsType map(OsType type, VmOsType incoming) {
        switch (type) {
        case UNASSIGNED:
            return VmOsType.Unassigned;
        case WINDOWS_XP:
            return VmOsType.WindowsXP;
        case WINDOWS_2003:
            return VmOsType.Windows2003;
        case WINDOWS_2008:
            return VmOsType.Windows2008;
        case OTHER:
            return VmOsType.Other;
        case OTHER_LINUX:
            return VmOsType.OtherLinux;
        case RHEL_5:
            return VmOsType.RHEL5;
        case RHEL_4:
            return VmOsType.RHEL4;
        case RHEL_3:
            return VmOsType.RHEL3;
        case WINDOWS_2003X64:
            return VmOsType.Windows2003x64;
        case WINDOWS_7:
            return VmOsType.Windows7;
        case WINDOWS_7X64:
            return VmOsType.Windows7x64;
        case RHEL_5X64:
            return VmOsType.RHEL5x64;
        case RHEL_4X64:
            return VmOsType.RHEL4x64;
        case RHEL_3X64:
            return VmOsType.RHEL3x64;
        case WINDOWS_2008X64:
            return VmOsType.Windows2008x64;
        case WINDOWS_2008R2:
            return VmOsType.Windows2008R2x64;
        case RHEL_6:
            return VmOsType.RHEL6;
        case RHEL_6X64:
            return VmOsType.RHEL6x64;
        case WINDOWS_8:
            return VmOsType.Windows8;
        case WINDOWS_8X64:
            return VmOsType.Windows8x64;
        case WINDOWS_2012X64:
            return VmOsType.Windows2012x64;

        default:
            return null;
        }
    }

    @Mapping(from = VmPayload.class, to = Payload.class)
    public static Payload map(VmPayload entity, Payload template) {
        if (entity.getType() != null || entity.getFileName() != null) {
            Payload model = template != null ? template : new Payload();
            if (entity.getType() != null) {
                org.ovirt.engine.api.model.VmDeviceType deviceType = map(entity.getType(), null);
                if (deviceType != null) {
                    model.setType(deviceType.value());
                }
            }
            if (entity.getFileName() != null) {
                PayloadFile file = new PayloadFile();
                file.setName(entity.getFileName());
                file.setContent(entity.getContent());
                model.setFile(file);
            }
            return model;
        }
        return null;
    }

    @Mapping(from = Payload.class, to = VmPayload.class)
    public static VmPayload map(Payload model, VmPayload template) {
        VmPayload entity = template != null ? template : new VmPayload();
        if (model.getType() != null) {
            org.ovirt.engine.api.model.VmDeviceType deviceType = org.ovirt.engine.api.model.VmDeviceType.fromValue(model.getType());
            if (deviceType!=null) {
                entity.setType(map(deviceType, null));
            }
        }
        if (model.getFile() != null) {
            entity.setFileName(model.getFile().getName());
            entity.setContent(model.getFile().getContent());
        }
        return entity;
    }

    static String cpuTuneToString(final CpuTune tune) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(final VCpuPin pin : tune.getVcpuPin()) {
            if(first) {
                first = false;
            } else {
                builder.append("_");
            }
            builder.append(pin.getVcpu()).append('#').append(pin.getCpuSet());
        }
        return builder.toString();
    }

    /**
     * Maps the stringified CPU-pinning to the API format.
     * @param string
     * @return
     */
    static CpuTune stringToCpuTune(String cpuPinning) {
        if(cpuPinning == null || cpuPinning.equals("")) {
            return null;
        }
        final CpuTune cpuTune = new CpuTune();
        for(String strCpu : cpuPinning.split("_")) {
            VCpuPin pin = stringToVCpupin(strCpu);
            cpuTune.getVcpuPin().add(pin);
        }

        return cpuTune;
    }

    static VCpuPin stringToVCpupin(final String strCpu) {
        final String[] strPin = strCpu.split("#");
        if (strPin.length != 2) {
            throw new IllegalArgumentException("Bad format: " + strCpu);
        }
        final VCpuPin pin = new VCpuPin();
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

    public static UsbPolicy getUsbPolicyOnCreate(Usb usb, VDSGroup vdsGroup) {
        if (usb == null || !usb.isSetEnabled() || !usb.isEnabled()) {
            return UsbPolicy.DISABLED;
        }
        else {
            UsbType usbType = getUsbType(usb);
            if (usbType == null) {
                return getUsbPolicyAccordingToClusterVersion(vdsGroup);
            } else {
                return getUsbPolicyAccordingToUsbType(usbType);
            }
        }
    }

    public static UsbPolicy getUsbPolicyOnUpdate(Usb usb, UsbPolicy currentPolicy, VDSGroup vdsGroup) {
        if (usb == null)
            return currentPolicy;

        if (usb.isSetEnabled()) {
            if (!usb.isEnabled())
                return UsbPolicy.DISABLED;
            else {
                UsbType usbType = getUsbType(usb);
                if (usbType != null) {
                    return getUsbPolicyAccordingToUsbType(usbType);
                }
                else {
                    return currentPolicy == UsbPolicy.DISABLED ?
                            getUsbPolicyAccordingToClusterVersion(vdsGroup)
                            : currentPolicy;
                }
            }
        }
        else {
            if (currentPolicy == UsbPolicy.DISABLED)
                return UsbPolicy.DISABLED;

            UsbType usbType = getUsbType(usb);
            if (usbType != null) {
                return getUsbPolicyAccordingToUsbType(UsbType.fromValue(usb.getType()));
            }
            else {
                return currentPolicy;
            }
        }
    }

    private static UsbType getUsbType(Usb usb) {
        return usb.isSetType() ? UsbType.fromValue(usb.getType()) : null;
    }

    private static UsbPolicy getUsbPolicyAccordingToClusterVersion(VDSGroup vdsGroup) {
        return vdsGroup.getcompatibility_version().compareTo(Version.v3_1) >= 0 ?
                UsbPolicy.ENABLED_NATIVE : UsbPolicy.ENABLED_LEGACY;
    }

    private static UsbPolicy getUsbPolicyAccordingToUsbType(UsbType usbType) {
        switch (usbType) {
        case LEGACY:
            return UsbPolicy.ENABLED_LEGACY;
        case NATIVE:
            return UsbPolicy.ENABLED_NATIVE;
        default:
            return null; // Should never get here
        }
    }
}
