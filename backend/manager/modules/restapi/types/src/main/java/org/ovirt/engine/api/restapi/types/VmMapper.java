package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.core.compat.Guid.createGuidFromString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.AuthorizedKey;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CpuMode;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.GuestInfo;
import org.ovirt.engine.api.model.GuestNicConfiguration;
import org.ovirt.engine.api.model.GuestNicsConfiguration;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.IPs;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.NumaTuneMode;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VCpuPin;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmMapper extends VmBaseMapper {

    // REVISIT retrieve from configuration
    private static final int DEFAULT_MEMORY_SIZE = 10 * 1024;

    // REVISIT once #712661 implemented by BE
    @Mapping(from = VmTemplate.class, to = VmStatic.class)
    public static VmStatic map(VmTemplate entity, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();
        staticVm.setId(Guid.Empty);
        staticVm.setVmtGuid(entity.getId());
        staticVm.setVdsGroupId(entity.getVdsGroupId());
        staticVm.setMemSizeMb(entity.getMemSizeMb());
        staticVm.setOsId(entity.getOsId());
        staticVm.setNiceLevel(entity.getNiceLevel());
        staticVm.setCpuShares(entity.getCpuShares());
        staticVm.setFailBack(entity.isFailBack());
        staticVm.setAutoStartup(entity.isAutoStartup());
        staticVm.setStateless(entity.isStateless());
        staticVm.setDeleteProtected(entity.isDeleteProtected());
        staticVm.setSsoMethod(entity.getSsoMethod());
        staticVm.setSmartcardEnabled(entity.isSmartcardEnabled());
        staticVm.setAutoStartup(entity.isAutoStartup());
        staticVm.setDefaultBootSequence(entity.getDefaultBootSequence());
        staticVm.setVmType(entity.getVmType());
        staticVm.setDefaultDisplayType(entity.getDefaultDisplayType());
        staticVm.setIsoPath(entity.getIsoPath());
        staticVm.setNumOfSockets(entity.getNumOfSockets());
        staticVm.setCpuPerSocket(entity.getCpuPerSocket());
        staticVm.setKernelUrl(entity.getKernelUrl());
        staticVm.setKernelParams(entity.getKernelParams());
        staticVm.setInitrdUrl(entity.getInitrdUrl());
        staticVm.setTimeZone(entity.getTimeZone());
        staticVm.setNumOfMonitors(entity.getNumOfMonitors());
        staticVm.setSingleQxlPci(entity.getSingleQxlPci());
        staticVm.setAllowConsoleReconnect(entity.isAllowConsoleReconnect());
        staticVm.setPriority(entity.getPriority());
        staticVm.setUsbPolicy(entity.getUsbPolicy());
        staticVm.setTunnelMigration(entity.getTunnelMigration());
        staticVm.setVncKeyboardLayout(entity.getVncKeyboardLayout());
        staticVm.setMigrationDowntime(entity.getMigrationDowntime());
        staticVm.setVmInit(entity.getVmInit());
        staticVm.setSerialNumberPolicy(entity.getSerialNumberPolicy());
        staticVm.setCustomSerialNumber(entity.getCustomSerialNumber());
        staticVm.setSpiceFileTransferEnabled(entity.isSpiceFileTransferEnabled());
        staticVm.setSpiceCopyPasteEnabled(entity.isSpiceCopyPasteEnabled());
        staticVm.setRunAndPause(entity.isRunAndPause());
        staticVm.setCpuProfileId(entity.getCpuProfileId());
        return staticVm;
    }

    @Mapping(from = VM.class, to = VmStatic.class)
    public static VmStatic map(VM vm, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();

        mapVmBaseModelToEntity(staticVm, vm);

        if (!vm.isSetMemory() && staticVm.getMemSizeMb()==0){
          //TODO: Get rid of this logic code when Backend supports default memory.
            staticVm.setMemSizeMb(DEFAULT_MEMORY_SIZE);
        }
        if (vm.isSetTemplate()) {
            if (vm.getTemplate().getId() != null) {
                staticVm.setVmtGuid(GuidUtils.asGuid(vm.getTemplate().getId()));
            }
            // There is no need to pass this property to backend if
            // no template was specified.
            // If user passes this property for a stateful vm which is not supported,
            // it will be handled by the backend.
            if (vm.isSetUseLatestTemplateVersion()) {
                staticVm.setUseLatestVersion(vm.isUseLatestTemplateVersion());
            }
        }
        if (vm.isSetCpu()) {
            if (vm.getCpu().isSetMode()) {
                staticVm.setUseHostCpuFlags(CpuMode.fromValue(vm.getCpu().getMode()) == CpuMode.HOST_PASSTHROUGH);
            }
            if (vm.getCpu().isSetCpuTune()) {
                staticVm.setCpuPinning(cpuTuneToString(vm.getCpu().getCpuTune()));
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
        if (vm.isSetMemoryPolicy() && vm.getMemoryPolicy().isSetGuaranteed()) {
            Long memGuaranteed = vm.getMemoryPolicy().getGuaranteed() / BYTES_PER_MB;
            staticVm.setMinAllocatedMem(memGuaranteed.intValue());
        }
        if (vm.isSetCustomProperties()) {
            staticVm.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetQuota() && vm.getQuota().isSetId()) {
            staticVm.setQuotaId(GuidUtils.asGuid(vm.getQuota().getId()));
        }
        if (vm.isSetInitialization()) {
            staticVm.setVmInit(map(vm.getInitialization(), new VmInit()));
        }
        // The Domain is now set to VmInit
        // we only set it for backward compatibility,
        // if the Domain set via VmInit we ignore it
        if (vm.isSetDomain() && vm.getDomain().isSetName()) {
            if (staticVm.getVmInit() == null) {
                staticVm.setVmInit(new VmInit());
            }
            // We don't want to override the domain if it set via the Initialization object
            if (!vm.isSetInitialization() || !vm.getInitialization().isSetDomain()) {
                staticVm.getVmInit().setDomain(vm.getDomain().getName());
            }
        }

        if (vm.isSetNumaTuneMode()) {
            NumaTuneMode mode = NumaTuneMode.fromValue(vm.getNumaTuneMode());
            if (mode != null) {
                staticVm.setNumaTuneMode(map(mode, null));
            }
        }

        return staticVm;
    }

    public static int mapOsType(String type) {
        //TODO remove this treatment when OsType enum is deleted.
        //backward compatibility code - UNASSIGNED is mapped to OTHER
        if (OsType.UNASSIGNED.name().equalsIgnoreCase(type)) {
            type = OsType.OTHER.name();
        }
        return SimpleDependecyInjector.getInstance().get(OsRepository.class).getOsIdByUniqueName(type);
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
        return map(entity, template, true);
    }

    public static VM map(org.ovirt.engine.core.common.businessentities.VM entity, VM template, boolean showDynamicInfo) {
        VM model = template != null ? template : new VM();

        mapVmBaseEntityToModel(model, entity.getStaticData());

        if (entity.getVmtGuid() != null) {
            model.setTemplate(new Template());
            model.getTemplate().setId(entity.getVmtGuid().toString());
            // display this property only if the vm is stateless
            // otherwise the value of this property is meaningless and misleading
            if(entity.isStateless()) {
                model.setUseLatestTemplateVersion(entity.isUseLatestVersion());
            }
        }
        if (entity.getInstanceTypeId() != null) {
            model.setInstanceType(new InstanceType());
            model.getInstanceType().setId(entity.getInstanceTypeId().toString());
        }
        if (entity.getStatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getStatus(), null)));
            if (entity.getStatus()==VMStatus.Paused) {
                model.getStatus().setDetail(entity.getVmPauseStatus().name().toLowerCase());
            }
        }
        if (entity.getStopReason() != null) {
            model.setStopReason(entity.getStopReason());
        }
        if (entity.getBootSequence() != null ||
            entity.getKernelUrl() != null ||
            entity.getInitrdUrl() != null ||
            entity.getKernelParams() != null) {
            OperatingSystem os = new OperatingSystem();

            os.setType(SimpleDependecyInjector.getInstance().get(OsRepository.class).getUniqueOsNames().get(entity.getVmOsId()));

            os.setKernel(entity.getKernelUrl());
            os.setInitrd(entity.getInitrdUrl());
            os.setCmdline(entity.getKernelParams());
            model.setOs(os);
        }
        if(entity.isUseHostCpuFlags()) {
            model.getCpu().setMode(CpuMode.HOST_PASSTHROUGH.value());
        }
        model.getCpu().setCpuTune(stringToCpuTune(entity.getCpuPinning()));

        model.getCpu().setArchitecture(CPUMapper.map(entity.getClusterArch(), null));

        if (entity.getVmPoolId() != null) {
            VmPool pool = new VmPool();
            pool.setId(entity.getVmPoolId().toString());
            model.setVmPool(pool);
        }

        // some fields (like boot-order,display..) have static value (= the permanent config)
        // and dynamic value (current/last run value, that can be different in case of run-once or edit while running)
        if (showDynamicInfo && entity.getDynamicData() != null && entity.getStatus().isRunningOrPaused()) {
           if (model.getOs() != null && entity.getBootSequence() != null) {
               for (Boot boot : map(entity.getBootSequence(), null)) {
                   model.getOs().getBoot().add(boot);
               }
           }

            model.setDisplay(new Display());
            model.getDisplay().setType(map(entity.getDisplayType(), null));
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

        // fill dynamic data
        if (entity.getDynamicData() != null && !entity.getStatus().isNotRunning()) {
            if(entity.getRunOnVds() != null) {
                model.setHost(new Host());
                model.getHost().setId(entity.getRunOnVds().toString());
            }
            final boolean hasIps = entity.getVmIp() != null && !entity.getVmIp().isEmpty();
            final boolean hasFqdn = entity.getVmFQDN() != null && !entity.getVmFQDN().isEmpty();
            if (hasIps || hasFqdn) {
                model.setGuestInfo(new GuestInfo());

                if (hasFqdn) {
                    model.getGuestInfo().setFqdn(entity.getVmFQDN());
                }

                if (hasIps){
                    IPs ips = new IPs();
                    for (String item : entity.getVmIp().split(" ")) {
                        if (!item.equals("")) {
                            IP ip = new IP();
                            ip.setAddress(item.trim());
                            ips.getIPs().add(ip);
                        }
                    }
                    if (!ips.getIPs().isEmpty()) {
                        model.getGuestInfo().setIps(ips);
                    }
                }
            }
            if (entity.getLastStartTime() != null) {
                model.setStartTime(DateMapper.map(entity.getLastStartTime(), null));
            }

            model.getDisplay().setAddress(entity.getDisplayIp());
            Integer displayPort = entity.getDisplay();
            model.getDisplay().setPort(displayPort==null || displayPort==-1 ? null : displayPort);
            Integer displaySecurePort = entity.getDisplaySecurePort();
            model.getDisplay().setSecurePort(displaySecurePort==null || displaySecurePort==-1 ? null : displaySecurePort);
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
            model.getDisplay().setSingleQxlPci(entity.getSingleQxlPci());
        }
        if (entity.getLastStopTime() != null) {
            model.setStopTime(DateMapper.map(entity.getLastStopTime(), null));
        }
        if (model.getDisplay() != null) {
            model.getDisplay().setMonitors(entity.getNumOfMonitors());
            model.getDisplay().setSingleQxlPci(entity.getSingleQxlPci());
            model.getDisplay().setAllowOverride(entity.getAllowConsoleReconnect());
            model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
            model.getDisplay().setKeyboardLayout(entity.getDefaultVncKeyboardLayout());
            model.getDisplay().setFileTransferEnabled(entity.isSpiceFileTransferEnabled());
            model.getDisplay().setCopyPasteEnabled(entity.isSpiceCopyPasteEnabled());
            model.getDisplay().setProxy(getEffectiveSpiceProxy(entity));
        }
        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        model.setSso(SsoMapper.map(entity.getSsoMethod(), null));
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());
        if (entity.getOrigin() != null) {
            model.setOrigin(map(entity.getOrigin(), null));
        }
        model.setPlacementPolicy(new VmPlacementPolicy());
        if(entity.getDedicatedVmForVds() !=null){
            model.getPlacementPolicy().setHost(new Host());
            model.getPlacementPolicy().getHost().setId(entity.getDedicatedVmForVds().toString());
        }
        VmAffinity vmAffinity = map(entity.getMigrationSupport(), null);
        if(vmAffinity !=null){
            model.getPlacementPolicy().setAffinity(vmAffinity.value());
        }
        MemoryPolicy policy = new MemoryPolicy();
        policy.setGuaranteed((long)entity.getMinAllocatedMem() * (long)BYTES_PER_MB);
        model.setMemoryPolicy(policy);
        if (!StringUtils.isEmpty(entity.getCustomProperties())) {
            CustomProperties hooks = new CustomProperties();
            hooks.getCustomProperty().addAll(CustomPropertiesParser.parse(entity.getCustomProperties(), false));
            model.setCustomProperties(hooks);
        }
        if (entity.getQuotaId()!=null) {
            Quota quota = new Quota();
            quota.setId(entity.getQuotaId().toString());
            model.setQuota(quota);
        }

        if (entity.getVmInit() != null) {
            model.setInitialization(map(entity.getVmInit(), null));
        }
        model.setNextRunConfigurationExists(entity.isNextRunConfigurationExists());
        model.setNumaTuneMode(map(entity.getNumaTuneMode(), null));
        return model;
    }

    private static String getEffectiveSpiceProxy(org.ovirt.engine.core.common.businessentities.VM entity) {
        if (StringUtils.isNotBlank(entity.getVmPoolSpiceProxy())) {
            return entity.getVmPoolSpiceProxy();
        }

        if (StringUtils.isNotBlank(entity.getVdsGroupSpiceProxy())) {
            return entity.getVdsGroupSpiceProxy();
        }

        String globalSpiceProxy = Config.getValue(ConfigValues.SpiceProxyDefault);
        if (StringUtils.isNotBlank(globalSpiceProxy)) {
            return globalSpiceProxy;
        }

        return null;
    }

    @Mapping(from = VM.class, to = RunVmOnceParams.class)
    public static RunVmOnceParams map(VM vm, RunVmOnceParams template) {
        RunVmOnceParams params = template != null ? template : new RunVmOnceParams();
        if (vm.isSetStateless() && vm.isStateless()) {
            params.setRunAsStateless(true);
        }
        if (vm.isSetDisplay()) {
            if (vm.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(vm.getDisplay().getType());
                if (displayType != null) {
                    params.setUseVnc(displayType == DisplayType.VNC);
                }
            }
            if (vm.getDisplay().isSetKeyboardLayout()) {
                String vncKeyboardLayout = vm.getDisplay().getKeyboardLayout();
                params.setVncKeyboardLayout(vncKeyboardLayout);
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
        if (vm.isSetCustomProperties()) {
            params.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetBios()) {
            if (vm.getBios().isSetBootMenu()) {
                params.setBootMenuEnabled(vm.getBios().getBootMenu().isEnabled());
            }
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().size() > 0) {
                params.setBootSequence(map(vm.getOs().getBoot(), null));
            }
            if (vm.getOs().isSetKernel()) {
                params.setKernelUrl(vm.getOs().getKernel());
            }
            if (vm.getOs().isSetInitrd()) {
                params.setInitrdUrl(vm.getOs().getInitrd());
            }
            if (vm.getOs().isSetCmdline()) {
                params.setKernelParams(vm.getOs().getCmdline());
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
        if (vm.isSetCpuShares()) {
            params.setCpuShares(vm.getCpuShares());
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

    @Mapping(from = ConfigurationType.class, to = org.ovirt.engine.core.common.businessentities.ConfigurationType.class)
    public static org.ovirt.engine.core.common.businessentities.ConfigurationType map(org.ovirt.engine.api.model.ConfigurationType configurationType, org.ovirt.engine.core.common.businessentities.ConfigurationType template) {
        switch (configurationType) {
            case OVF:            return org.ovirt.engine.core.common.businessentities.ConfigurationType.OVF;
            default:                return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.ConfigurationType.class, to = ConfigurationType.class)
    public static ConfigurationType map(org.ovirt.engine.core.common.businessentities.ConfigurationType configurationType, org.ovirt.engine.api.model.ConfigurationType template) {
        switch (configurationType) {
            case OVF:            return ConfigurationType.OVF;
            default:                return null;
        }
    }

    public static VM map(String configuration, ConfigurationType type, VM vm) {
        vm.setInitialization(new Initialization());
        vm.getInitialization().setConfiguration(new Configuration());
        vm.getInitialization().getConfiguration().setData(configuration);
        vm.getInitialization().getConfiguration().setType(type.value());
        return vm;
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

    @Mapping(from = VmPayload.class, to = Payload.class)
    public static Payload map(VmPayload entity, Payload template) {
        if (entity.getDeviceType() != null || entity.getFiles().isEmpty()) {
            Payload model = template != null ? template : new Payload();
            if (entity.getDeviceType() != null) {
                org.ovirt.engine.api.model.VmDeviceType deviceType = map(entity.getDeviceType(), null);
                if (deviceType != null) {
                    model.setType(deviceType.value());
                }
            }
            model.setVolumeId(entity.getVolumeId());
            if (entity.getFiles().size() > 0) {
                model.setFiles(new Files());
                for (Map.Entry<String, String> entry : entity.getFiles().entrySet()) {
                    File file = new File();
                    file.setName(entry.getKey());
                    file.setContent(entry.getValue());
                    model.getFiles().getFiles().add(file);
                }
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
                entity.setDeviceType(map(deviceType, null));
            }
        }
        if (model.isSetVolumeId()) {
            entity.setVolumeId(model.getVolumeId());
        }
        if (model.isSetFiles()) {
            for (File file : model.getFiles().getFiles()) {
                entity.getFiles().put(file.getName(), file.getContent());
            }
        }
        return entity;
    }

    @Mapping(from = Initialization.class, to = VmInit.class)
    public static VmInit map(Initialization model, VmInit template) {
        VmInit entity = template != null ? template : new VmInit();

        if (model.isSetHostName()) {
            entity.setHostname(model.getHostName());
        }

        if (model.isSetDomain()) {
            entity.setDomain(model.getDomain());
        }

        if (model.isSetTimezone()) {
            entity.setTimeZone(model.getTimezone());
        }

        if (model.isSetAuthorizedSshKeys()) {
            entity.setAuthorizedKeys(model.getAuthorizedSshKeys());
        }

        if (model.isSetRegenerateSshKeys()) {
            entity.setRegenerateKeys(model.isRegenerateSshKeys());
        }

        if (model.isSetDnsServers()) {
            entity.setDnsServers(model.getDnsServers());
        }

        if (model.isSetDnsSearch()) {
            entity.setDnsSearch(model.getDnsSearch());
        }

        if (model.isSetWindowsLicenseKey()) {
            entity.setWinKey(model.getWindowsLicenseKey());
        }

        if (model.isSetRootPassword()) {
            entity.setRootPassword(model.getRootPassword());
        }

        if (model.isSetCustomScript()) {
            entity.setCustomScript(model.getCustomScript());
        }

        if (model.isSetNicConfigurations()) {
            List<VmInitNetwork> networks = new ArrayList<VmInitNetwork>();
            for (GuestNicConfiguration nic : model.getNicConfigurations().getNicConfigurations()) {
                networks.add(map(nic, null));
            }
            entity.setNetworks(networks);
        }

        if (model.isSetInputLocale()) {
            entity.setInputLocale(model.getInputLocale());
        }

        if (model.isSetUiLanguage()) {
            entity.setUiLanguage(model.getUiLanguage());
        }

        if (model.isSetSystemLocale()) {
            entity.setSystemLocale(model.getSystemLocale());
        }

        if (model.isSetUserLocale()) {
            entity.setUserLocale(model.getUserLocale());
        }

        if (model.isSetUserName()) {
            entity.setUserName(model.getUserName());
        }

        if (model.isSetActiveDirectoryOu()) {
            entity.setUserName(model.getActiveDirectoryOu());
        }

        if (model.isSetOrgName()) {
            entity.setOrgName(model.getOrgName());
        }
        return entity;
    }

    @Mapping(from = VmInit.class, to = Initialization.class)
    public static Initialization map(VmInit entity, Initialization template) {
        Initialization model = template != null ? template :
            new Initialization();

        if (entity.getHostname() != null) {
            model.setHostName(entity.getHostname());
        }
        if (StringUtils.isNotBlank(entity.getDomain())) {
            model.setDomain(entity.getDomain());
        }
        if (entity.getTimeZone() != null) {
            model.setTimezone(entity.getTimeZone());
        }
        if (entity.getAuthorizedKeys() != null) {
            model.setAuthorizedSshKeys(entity.getAuthorizedKeys());
        }
        if (entity.getRegenerateKeys() != null) {
            model.setRegenerateSshKeys(entity.getRegenerateKeys());
        }
        if (entity.getDnsServers() != null) {
            model.setDnsServers(entity.getDnsServers());
        }
        if (entity.getDnsSearch() != null) {
            model.setDnsSearch(entity.getDnsSearch());
        }
        if (entity.getWinKey() != null) {
            model.setWindowsLicenseKey(entity.getWinKey());
        }
        if (entity.getRootPassword() != null || entity.isPasswordAlreadyStored()) {
            model.setRootPassword("******");
        }
        if (entity.getCustomScript() != null) {
            model.setCustomScript(entity.getCustomScript());
        }
        if (entity.getNetworks() != null) {
            model.setNicConfigurations(new GuestNicsConfiguration());
            for (VmInitNetwork network : entity.getNetworks()) {
                model.getNicConfigurations().getNicConfigurations().add(map(network, null));
            }
        }
        if (entity.getInputLocale() != null) {
            model.setInputLocale(entity.getInputLocale());
        }
        if (entity.getUiLanguage() != null) {
            model.setUiLanguage(entity.getUiLanguage());
        }
        if (entity.getSystemLocale() != null) {
            model.setSystemLocale(entity.getSystemLocale());
        }
        if (entity.getUserLocale() != null) {
            model.setUserLocale(entity.getUserLocale());
        }
        if (entity.getUserName() != null) {
            model.setUserName(entity.getUserName());
        }
        if (entity.getActiveDirectoryOU() != null) {
            model.setActiveDirectoryOu(entity.getActiveDirectoryOU());
        }
        if (entity.getOrgName() != null) {
            model.setOrgName(entity.getOrgName());
        }
        return model;
    }

    @Mapping(from = GuestNicConfiguration.class, to = VmInitNetwork.class)
    public static VmInitNetwork map(GuestNicConfiguration model, VmInitNetwork template) {
        VmInitNetwork entity = template != null ? template : new VmInitNetwork();

        if (model.isSetName()) {
            entity.setName(model.getName());
        }

        if (model.isOnBoot()) {
            entity.setStartOnBoot(model.isOnBoot());
        }

        if (model.isSetBootProtocol()) {
            entity.setBootProtocol(BootProtocolMapper.map(BootProtocol.fromValue(model.getBootProtocol()), NetworkBootProtocol.NONE));
        }

        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setIp(model.getIp().getAddress());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setNetmask(model.getIp().getNetmask());
            }

            if (model.getIp().isSetGateway()) {
                entity.setGateway(model.getIp().getGateway());
            }
        }

        return entity;
    }

    @Mapping(from = VmInitNetwork.class, to = GuestNicConfiguration.class)
    public static GuestNicConfiguration map(VmInitNetwork entity, GuestNicConfiguration template) {
        GuestNicConfiguration model = template != null ? template : new GuestNicConfiguration();

        model.setName(entity.getName());
        model.setOnBoot(entity.getStartOnBoot());
        if (entity.getBootProtocol() != null) {
            model.setBootProtocol(BootProtocolMapper.map(entity.getBootProtocol(), null).value());
        }
        IP ip = new IP();
        model.setIp(ip);
        ip.setAddress(entity.getIp());
        ip.setNetmask(entity.getNetmask());
        ip.setGateway(entity.getGateway());

        return model;
    }

    @Mapping(from = CloudInit.class, to = VmInit.class)
    public static VmInit map(CloudInit model, VmInit template) {
        VmInit entity = template != null ? template : new VmInit();

        if (model.isSetHost() && model.getHost().isSetAddress()) {
            entity.setHostname(model.getHost().getAddress());
        }

        if (model.isSetAuthorizedKeys()
                && model.getAuthorizedKeys().isSetAuthorizedKeys()
                && !model.getAuthorizedKeys().getAuthorizedKeys().isEmpty()) {
            StringBuilder keys = new StringBuilder();
            for (AuthorizedKey authKey : model.getAuthorizedKeys().getAuthorizedKeys()) {
                if (keys.length() > 0) {
                    keys.append("\n");
                }
                keys.append(authKey.getKey());
            }
            entity.setAuthorizedKeys(keys.toString());
        }

        if (model.isSetRegenerateSshKeys()) {
            entity.setRegenerateKeys(model.isRegenerateSshKeys());
        }

        if (model.isSetNetworkConfiguration()) {
            if (model.getNetworkConfiguration().isSetNics()) {
                List<VmInitNetwork> interfaces = new ArrayList<VmInitNetwork>();
                for (NIC iface : model.getNetworkConfiguration().getNics().getNics()) {
                    VmInitNetwork vmInitInterface = new VmInitNetwork();
                    if (iface.isSetName()) {
                        vmInitInterface.setName(iface.getName());
                    }
                    interfaces.add(vmInitInterface);
                    if (iface.isSetBootProtocol()) {
                        NetworkBootProtocol protocol = BootProtocolMapper.map
                                (BootProtocol.fromValue(iface.getBootProtocol()), vmInitInterface.getBootProtocol());
                        vmInitInterface.setBootProtocol(protocol);
                        if (protocol != NetworkBootProtocol.DHCP && iface.isSetNetwork() && iface.getNetwork().isSetIp()) {
                            if (iface.getNetwork().getIp().isSetAddress()) {
                                vmInitInterface.setIp(iface.getNetwork().getIp().getAddress());
                            }
                            if (iface.getNetwork().getIp().isSetNetmask()) {
                                vmInitInterface.setNetmask(iface.getNetwork().getIp().getNetmask());
                            }
                            if (iface.getNetwork().getIp().isSetGateway()) {
                                vmInitInterface.setGateway(iface.getNetwork().getIp().getGateway());
                            }
                        }
                        if (iface.isSetOnBoot() && iface.isOnBoot()) {
                            vmInitInterface.setStartOnBoot(true);
                        }
                    }
                }

                entity.setNetworks(interfaces);
            }
            if (model.getNetworkConfiguration().isSetDns()) {
                if (model.getNetworkConfiguration().getDns().isSetServers()
                        && model.getNetworkConfiguration().getDns().getServers().isSetHosts()
                        && !model.getNetworkConfiguration().getDns().getServers().getHosts().isEmpty()) {
                    StringBuilder dnsServers = new StringBuilder();
                    for (Host host : model.getNetworkConfiguration().getDns().getServers().getHosts()) {
                        if (host.isSetAddress()) {
                            dnsServers.append(host.getAddress());
                        }
                    }
                    entity.setDnsServers(dnsServers.toString());
                }

                if (model.getNetworkConfiguration().getDns().isSetSearchDomains()
                        && model.getNetworkConfiguration().getDns().getSearchDomains().isSetHosts()
                        && !model.getNetworkConfiguration().getDns().getSearchDomains().getHosts().isEmpty()) {
                    StringBuilder searchDomains = new StringBuilder();
                    for (Host host : model.getNetworkConfiguration().getDns().getSearchDomains().getHosts()) {
                        if (host.isSetAddress()) {
                            searchDomains.append(host.getAddress());
                        }
                    }
                    entity.setDnsSearch(searchDomains.toString());
                }
            }
        }

        if (model.isSetTimezone() && model.getTimezone() != null) {
            entity.setTimeZone(model.getTimezone());
        }

        if (model.isSetUsers()) {
            for (User user : model.getUsers().getUsers()) {
                entity.setRootPassword(user.getPassword());
            }
        }

        // files is Deprecated, we are using the Payload for passing files
        // We are storing the first file as a Cloud Init custom script
        // for RunOnce backward compatibility.
        if (model.isSetFiles() && model.getFiles().isSetFiles()
                && !model.getFiles().getFiles().isEmpty()) {
            File file = model.getFiles().getFiles().get(0);
            entity.setCustomScript(file.getContent());
        }

        return entity;
    }

    static String cpuTuneToString(final CpuTune tune) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(final VCpuPin pin : tune.getVCpuPin()) {
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
            cpuTune.getVCpuPin().add(pin);
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

    public static UsbPolicy getUsbPolicyOnCreate(Usb usb, Version vdsGroupVersion) {
        if (usb == null || !usb.isSetEnabled() || !usb.isEnabled()) {
            return UsbPolicy.DISABLED;
        }
        else {
            UsbType usbType = getUsbType(usb);
            if (usbType == null) {
                return getUsbPolicyAccordingToClusterVersion(vdsGroupVersion);
            } else {
                return getUsbPolicyAccordingToUsbType(usbType);
            }
        }
    }

    public static UsbPolicy getUsbPolicyOnUpdate(Usb usb, UsbPolicy currentPolicy, Version vdsGroupVersion) {
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
                            getUsbPolicyAccordingToClusterVersion(vdsGroupVersion)
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

    private static UsbPolicy getUsbPolicyAccordingToClusterVersion(Version vdsGroupVersion) {
        return vdsGroupVersion.compareTo(Version.v3_1) >= 0 ?
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

    /**
     * This method maps the VM's open sessions with users. Engine currently does not regard sessions as business
     * entities, and therefore a session doesn't have an ID. Generating IDs for sessions is outside the scope of this
     * method and should be done by the method's invoker.
     *
     * The session involves a user. Sometimes this is an ovirt-user, and sometimes not. Engine provides only the user
     * name, and this method maps it by placing it inside a 'User' object in the session. If invokers want to identify
     * the ovirt user and provide a link to it, it's their responsibility to do so; this is out of the scope of this
     * method.
     */
    public static Sessions map(org.ovirt.engine.core.common.businessentities.VM vm, Sessions sessions) {
        if (sessions == null) {
            sessions = new Sessions();
        }
        mapConsoleSession(vm, sessions);
        mapGuestSessions(vm, sessions);
        return sessions;
    }

    /**
     * This method maps the session of the 'console user', if exists. This is the ovirt user who opened a session
     * through the user-console; the one who is said to be 'logged in' (or 'have the ticket') to this VM. Currently
     * engine makes available only the name and IP of this user. In the future it may make available also the connection
     * protocol used in the session (spice/vnc).
     */
    private static Sessions mapConsoleSession(org.ovirt.engine.core.common.businessentities.VM vm, Sessions sessions) {
        String consoleUserName = vm.getConsoleCurentUserName(); // currently in format user@domain, so needs to be
                                                                // parsed.
        if (consoleUserName != null && !consoleUserName.isEmpty()) {
            String userName = parseUserName(consoleUserName);
            String domainName = parseDomainName(consoleUserName);
            User consoleUser = new User();
            consoleUser.setUserName(userName);
            consoleUser.setDomain(new Domain());
            consoleUser.getDomain().setName(domainName);
            Session consoleSession = new Session();
            consoleSession.setUser(consoleUser);
            if (vm.getClientIp()!=null && !vm.getClientIp().isEmpty()) {
                IP ip = new IP();
                ip.setAddress(vm.getClientIp());
                consoleSession.setIp(ip);
            }
            consoleSession.setConsoleUser(true);
            // TODO: in the future, map the connection protocol as well
            sessions.getSessions().add(consoleSession);
        }
        return sessions;
    }

    /**
     * Parse the user name out of the provided string. Expects 'user@domain', but if no '@' found, will assume that
     * domain was omitted and that the whole String is the user-name.
     */
    private static String parseDomainName(String consoleUserName) {
        return consoleUserName.contains("@") ?
                consoleUserName.substring(consoleUserName.indexOf("@") + 1, consoleUserName.length()) : null;
    }

    /**
     * Parse the domain name out of the provided string. Expects 'user@domain'. If no '@' found, will assume that domain
     * name was omitted and return null.
     */
    private static String parseUserName(String consoleUserName) {
        return consoleUserName.contains("@") ?
                consoleUserName.substring(0, consoleUserName.indexOf("@")) :
                consoleUserName;
    }

    /**
     * This method maps the sessions of users who are connected to the VM, but are not the 'logged-in'/'console' user.
     * Currently the information that engine supplies about these users is only a string, which contains the name of
     * only one such user, if exists (the user is not necessarily an ovirt user). In the future the engine may pass
     * multiple 'guest' users, along with their IPs and perhaps also the connection protocols that they are using (SSH,
     * RDP...)
     */
    private static Sessions mapGuestSessions(org.ovirt.engine.core.common.businessentities.VM vm, Sessions sessions) {
        String guestUserName = vm.getGuestCurentUserName();
        if (guestUserName != null && !guestUserName.isEmpty()) {
            Session guestSession = new Session();
            User user = new User();
            user.setUserName(guestUserName);
            guestSession.setUser(user);
            // TODO: in the future, map the user-IP and connection protocol as well
            sessions.getSessions().add(guestSession);
        }
        return sessions;
    }

    @Mapping(from = NumaTuneMode.class, to = org.ovirt.engine.core.common.businessentities.NumaTuneMode.class)
    public static org.ovirt.engine.core.common.businessentities.NumaTuneMode map(NumaTuneMode mode,
                                      org.ovirt.engine.core.common.businessentities.NumaTuneMode incoming) {
        switch (mode) {
        case STRICT:
            return org.ovirt.engine.core.common.businessentities.NumaTuneMode.STRICT;
        case INTERLEAVE:
            return org.ovirt.engine.core.common.businessentities.NumaTuneMode.INTERLEAVE;
        case PREFERRED:
            return org.ovirt.engine.core.common.businessentities.NumaTuneMode.PREFERRED;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.NumaTuneMode.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.NumaTuneMode mode, String incoming) {
        if (mode == null) {
            return null;
        }
        switch (mode) {
        case STRICT:
            return NumaTuneMode.STRICT.value();
        case INTERLEAVE:
            return NumaTuneMode.INTERLEAVE.value();
        case PREFERRED:
            return NumaTuneMode.PREFERRED.value();
        default:
            return null;
        }
    }
}
