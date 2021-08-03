package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.AuthorizedKey;
import org.ovirt.engine.api.model.AutoPinningPolicy;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CloudInit;
import org.ovirt.engine.api.model.Configuration;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CpuPinningPolicy;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.DynamicCpu;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.ExternalVmProviderType;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;
import org.ovirt.engine.api.model.GuestOperatingSystem;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.Kernel;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmMapper extends VmBaseMapper {

    // REVISIT retrieve from configuration
    private static final int DEFAULT_MEMORY_SIZE = 10 * 1024;

    // REVISIT once #712661 implemented by BE
    @Mapping(from = VmTemplate.class, to = VmStatic.class)
    public static VmStatic map(VmTemplate entity, VmStatic template) {
        return map(entity, template, null);
    }

    public static VmStatic map(VmTemplate entity, VmStatic template, Version version) {
        VmStatic staticVm = template != null ? template : new VmStatic();

        staticVm.setId(Guid.Empty);
        staticVm.setVmtGuid(entity.getId());
        staticVm.setClusterId(entity.getClusterId());
        staticVm.setOsId(entity.getOsId());
        staticVm.setNiceLevel(entity.getNiceLevel());
        staticVm.setCpuShares(entity.getCpuShares());
        staticVm.setStateless(entity.isStateless());
        staticVm.setDeleteProtected(entity.isDeleteProtected());
        staticVm.setSsoMethod(entity.getSsoMethod());
        staticVm.setVmType(entity.getVmType());
        staticVm.setIsoPath(entity.getIsoPath());
        staticVm.setKernelUrl(entity.getKernelUrl());
        staticVm.setKernelParams(entity.getKernelParams());
        staticVm.setInitrdUrl(entity.getInitrdUrl());
        staticVm.setTimeZone(entity.getTimeZone());
        staticVm.setAllowConsoleReconnect(entity.isAllowConsoleReconnect());
        staticVm.setVncKeyboardLayout(entity.getVncKeyboardLayout());
        staticVm.setVmInit(entity.getVmInit());
        staticVm.setSerialNumberPolicy(entity.getSerialNumberPolicy());
        staticVm.setCustomSerialNumber(entity.getCustomSerialNumber());
        staticVm.setSpiceFileTransferEnabled(entity.isSpiceFileTransferEnabled());
        staticVm.setSpiceCopyPasteEnabled(entity.isSpiceCopyPasteEnabled());
        staticVm.setRunAndPause(entity.isRunAndPause());
        staticVm.setCpuProfileId(entity.getCpuProfileId());
        staticVm.setAutoConverge(entity.getAutoConverge());
        staticVm.setMigrateCompressed(entity.getMigrateCompressed());
        staticVm.setCustomProperties(entity.getCustomProperties());
        staticVm.setCustomEmulatedMachine(entity.getCustomEmulatedMachine());
        staticVm.setBiosType(entity.getBiosType());
        staticVm.setCustomCpuName(entity.getCustomCpuName());
        staticVm.setConsoleDisconnectAction(entity.getConsoleDisconnectAction());
        staticVm.setSmallIconId(entity.getSmallIconId());
        staticVm.setLargeIconId(entity.getLargeIconId());
        staticVm.setQuotaId(entity.getQuotaId());
        staticVm.setBootMenuEnabled(entity.isBootMenuEnabled());
        staticVm.setMultiQueuesEnabled(entity.isMultiQueuesEnabled());
        staticVm.setUseHostCpuFlags(entity.isUseHostCpuFlags());
        staticVm.setVirtioScsiMultiQueues(entity.getVirtioScsiMultiQueues());
        return doMapVmBaseHwPartToVmStatic(entity, staticVm, version);
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.InstanceType.class, to = VmStatic.class)
    public static VmStatic map(org.ovirt.engine.core.common.businessentities.InstanceType entity, VmStatic vmStatic, Version version) {
        return doMapVmBaseHwPartToVmStatic((VmBase) entity, vmStatic != null ? vmStatic : new VmStatic(), version);
    }

    private static VmStatic doMapVmBaseHwPartToVmStatic(VmBase entity, VmStatic staticVm, Version version) {
        staticVm.setMemSizeMb(entity.getMemSizeMb());
        staticVm.setMaxMemorySizeMb(entity.getMaxMemorySizeMb());
        staticVm.setAutoStartup(entity.isAutoStartup());
        staticVm.setSmartcardEnabled(entity.isSmartcardEnabled());
        staticVm.setDefaultBootSequence(entity.getDefaultBootSequence());
        staticVm.setDefaultDisplayType(entity.getDefaultDisplayType());
        staticVm.setNumOfSockets(entity.getNumOfSockets());
        staticVm.setCpuPerSocket(entity.getCpuPerSocket());
        staticVm.setThreadsPerCpu(entity.getThreadsPerCpu());
        staticVm.setNumOfMonitors(entity.getNumOfMonitors());
        staticVm.setPriority(entity.getPriority());
        staticVm.setUsbPolicy(entity.getUsbPolicy());
        staticVm.setTunnelMigration(entity.getTunnelMigration());
        staticVm.setMigrationSupport(entity.getMigrationSupport());
        staticVm.setMigrationDowntime(entity.getMigrationDowntime());
        staticVm.setDedicatedVmForVdsList(entity.getDedicatedVmForVdsList());
        staticVm.setMinAllocatedMem(entity.getMinAllocatedMem());
        staticVm.setNumOfIoThreads(entity.getNumOfIoThreads());

        return staticVm;
    }

    @Mapping(from = Vm.class, to = VmStatic.class)
    public static VmStatic map(Vm vm, VmStatic template) {
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

        if (vm.isSetInitialization()) {
            staticVm.setVmInit(InitializationMapper.map(vm.getInitialization(), staticVm.getVmInit()));
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
            staticVm.getvNumaNodeList()
                    .forEach(node -> node.setNumaTuneMode(NumaMapper.map(vm.getNumaTuneMode(), null)));
        }

        if (vm.isSetExternalHostProvider()) {
            String providerId = vm.getExternalHostProvider().getId();
            staticVm.setProviderId(providerId == null ? null : GuidUtils.asGuid(providerId));
        }

        return staticVm;
    }

    public static int mapOsType(String type) {
        //TODO remove this treatment when OsType enum is deleted.
        //backward compatibility code - UNASSIGNED is mapped to OTHER
        if (OsType.UNASSIGNED.name().equalsIgnoreCase(type)) {
            type = OsType.OTHER.name();
        }
        return SimpleDependencyInjector.getInstance().get(OsRepository.class).getOsIdByUniqueName(type);
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VM.class, to = Vm.class)
    public static Vm map(org.ovirt.engine.core.common.businessentities.VM entity, Vm template) {
        return map(entity, template, true);
    }

    public static Vm map(org.ovirt.engine.core.common.businessentities.VM entity, Vm template, boolean showDynamicInfo) {
        Vm model = template != null ? template : new Vm();

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
        if (entity.getOriginalTemplateGuid() != null) {
            model.setOriginalTemplate(new Template());
            model.getOriginalTemplate().setId(entity.getOriginalTemplateGuid().toString());
        }

        if (entity.getInstanceTypeId() != null) {
            model.setInstanceType(new InstanceType());
            model.getInstanceType().setId(entity.getInstanceTypeId().toString());
        }
        if (entity.getStatus() != null) {
            model.setStatus(mapVmStatus(entity.getStatus()));
            if (entity.getStatus() == VMStatus.Paused) {
                model.setStatusDetail(entity.getVmPauseStatus().name().toLowerCase());
            } else if (entity.getStatus() == VMStatus.Down && entity.getBackgroundOperationDescription() != null) {
                model.setStatusDetail(entity.getBackgroundOperationDescription());
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

            os.setType(SimpleDependencyInjector.getInstance().get(OsRepository.class).getUniqueOsNames().get(entity.getVmOsId()));

            os.setKernel(entity.getKernelUrl());
            os.setInitrd(entity.getInitrdUrl());
            os.setCmdline(entity.getKernelParams());
            model.setOs(os);
        }

        model.getCpu().setArchitecture(CPUMapper.map(entity.getClusterArch(), null));

        if (entity.getVmPoolId() != null) {
            VmPool pool = new VmPool();
            pool.setId(entity.getVmPoolId().toString());
            model.setVmPool(pool);
        }

        model.setDisplay(new Display());

        // some fields (like boot-order,display..) have static value (= the permanent config)
        // and dynamic value (current/last run value, that can be different in case of run-once or edit while running)
        if (showDynamicInfo && entity.getDynamicData() != null && entity.getStatus().isRunningOrPaused()) {
            if (model.getOs() != null && entity.getBootSequence() != null) {
                Boot boot = map(entity.getBootSequence(), null);
                model.getOs().setBoot(boot);
            }
            if (VmCpuCountHelper.isAutoPinning(entity)) {
                CpuTopology topology = new CpuTopology();
                topology.setSockets(entity.getCurrentSockets());
                topology.setCores(entity.getCurrentCoresPerSocket());
                topology.setThreads(entity.getCurrentThreadsPerCore());
                model.setDynamicCpu(new DynamicCpu());
                model.getDynamicCpu().setTopology(topology);
                model.getDynamicCpu().setCpuTune(stringToCpuTune(entity.getCurrentCpuPinning()));
            }
        } else {
            if (model.getOs() != null) {
                Boot boot = map(entity.getDefaultBootSequence(), null);
                model.getOs().setBoot(boot);
            }
        }

        if (entity.hasIllegalImages()) {
            model.setHasIllegalImages(true);
        }

        // fill dynamic data
        if (entity.getDynamicData() != null && !entity.getStatus().isNotRunning()) {
            if(entity.getRunOnVds() != null) {
                model.setHost(new Host());
                model.getHost().setId(entity.getRunOnVds().toString());
            }
            final boolean hasFqdn = entity.getFqdn() != null && !entity.getFqdn().isEmpty();
            if (hasFqdn) {
                model.setFqdn(entity.getFqdn());
            }
            final boolean hasGuestOsVersion = entity.getGuestOsVersion() != null && !entity.getGuestOsVersion().isEmpty();
            if (hasGuestOsVersion) {
                GuestOperatingSystem os = model.getGuestOperatingSystem();
                if(os == null) {
                    os = new GuestOperatingSystem();
                    model.setGuestOperatingSystem(os);
                }
                os.setArchitecture(entity.getGuestOsArch().name());
                os.setCodename(entity.getGuestOsCodename());
                os.setDistribution(entity.getGuestOsDistribution());
                String kernelVersionString = entity.getGuestOsKernelVersion();
                if(StringUtils.isNotEmpty(kernelVersionString)) {
                    org.ovirt.engine.api.model.Version kernelVersion = VersionMapper.fromVersionString(kernelVersionString);
                    if(kernelVersion != null) {
                        if(os.getKernel() == null) {
                            os.setKernel(new Kernel());
                        }
                        os.getKernel().setVersion(kernelVersion);
                        os.getKernel().getVersion().setFullVersion(entity.getGuestOsKernelVersion());
                    }
                }
                String osVersionString = entity.getGuestOsVersion();
                if(StringUtils.isNotEmpty(osVersionString)) {
                    os.setVersion(VersionMapper.fromVersionString(osVersionString));
                    if(os.getVersion() != null) {
                        os.getVersion().setFullVersion(entity.getGuestOsVersion());
                    }
                }
                os.setFamily(entity.getGuestOsType().name());
            }

            final boolean hasTimezoneName = entity.getGuestOsTimezoneName() != null && !entity.getGuestOsTimezoneName().isEmpty();
            if (hasTimezoneName) {
                TimeZone guestTz = model.getGuestTimeZone();
                if(guestTz == null) {
                    guestTz = new TimeZone();
                    model.setGuestTimeZone(guestTz);
                }
                guestTz.setName(entity.getGuestOsTimezoneName());
                guestTz.setUtcOffset(TimeZoneMapper.mapUtcOffsetToDisplayString(entity.getGuestOsTimezoneOffset()));
            }

            if (entity.getLastStartTime() != null) {
                model.setStartTime(DateMapper.map(entity.getLastStartTime(), null));
            }

            model.setRunOnce(entity.isRunOnce());
            org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType = deriveGraphicsType(entity.getGraphicsInfos());
            if (graphicsType != null) {
                model.getDisplay().setType(DisplayMapper.map(graphicsType, null));

                GraphicsInfo graphicsInfo = entity.getGraphicsInfos().get(graphicsType);
                model.getDisplay().setAddress(graphicsInfo == null ? null : graphicsInfo.getIp());
                Integer displayPort = graphicsInfo == null ? null : graphicsInfo.getPort();
                model.getDisplay().setPort(displayPort == null || displayPort.equals(-1) ? null : displayPort);
                Integer displaySecurePort = graphicsInfo == null ? null : graphicsInfo.getTlsPort();
                model.getDisplay().setSecurePort(displaySecurePort==null || displaySecurePort.equals(-1) ? null : displaySecurePort);
            }
        }
        if (entity.getLastStopTime() != null) {
            model.setStopTime(DateMapper.map(entity.getLastStopTime(), null));
        }
        model.getDisplay().setMonitors(entity.getNumOfMonitors());
        model.getDisplay().setAllowOverride(entity.getAllowConsoleReconnect());
        model.getDisplay().setSmartcardEnabled(entity.isSmartcardEnabled());
        model.getDisplay().setKeyboardLayout(entity.getDefaultVncKeyboardLayout());
        model.getDisplay().setFileTransferEnabled(entity.isSpiceFileTransferEnabled());
        model.getDisplay().setCopyPasteEnabled(entity.isSpiceCopyPasteEnabled());
        model.getDisplay().setProxy(getEffectiveSpiceProxy(entity));
        model.getDisplay().setDisconnectAction(map(entity.getConsoleDisconnectAction(), null).toString());

        model.setStateless(entity.isStateless());
        model.setDeleteProtected(entity.isDeleteProtected());
        model.setSso(SsoMapper.map(entity.getSsoMethod(), null));
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.isAutoStartup());
        model.getHighAvailability().setPriority(entity.getPriority());
        if (entity.getOrigin() != null) {
            model.setOrigin(map(entity.getOrigin(), null));
        }

        if (entity.getVmInit() != null) {
            model.setInitialization(InitializationMapper.map(entity.getVmInit(), null));
        }
        model.setNextRunConfigurationExists(entity.isNextRunConfigurationExists() || entity.isVnicsOutOfSync());
        model.setNumaTuneMode(NumaMapper.map(getVmNumaTuneIfApplies(entity.getvNumaNodeList()), null));

        if (entity.getProviderId() != null) {
            model.setExternalHostProvider(new ExternalHostProvider());
            model.getExternalHostProvider().setId(entity.getProviderId().toString());
        }

        return model;
    }

    private static NumaTuneMode getVmNumaTuneIfApplies(List<VmNumaNode> vmNumaNodes) {
        Set<NumaTuneMode> numaTuneModes = vmNumaNodes.stream().map(VmNumaNode::getNumaTuneMode).collect(Collectors.toSet());
        // if the size is one, all the vNodes having the same tuning. We can reflect it on the VM.
        if (numaTuneModes.size() == 1) {
            return numaTuneModes.stream().findFirst().get();
        }
        return null;
    }

    private static String getEffectiveSpiceProxy(org.ovirt.engine.core.common.businessentities.VM entity) {
        if (StringUtils.isNotBlank(entity.getVmPoolSpiceProxy())) {
            return entity.getVmPoolSpiceProxy();
        }

        if (StringUtils.isNotBlank(entity.getClusterSpiceProxy())) {
            return entity.getClusterSpiceProxy();
        }

        String globalSpiceProxy = Config.getValue(ConfigValues.SpiceProxyDefault);
        if (StringUtils.isNotBlank(globalSpiceProxy)) {
            return globalSpiceProxy;
        }

        return null;
    }

    // for backwards compatibility
    // returns graphics type of a running vm (can be different than static graphics in vm device due to run once)
    // if vm has multiple graphics, returns SPICE
    public static org.ovirt.engine.core.common.businessentities.GraphicsType deriveGraphicsType(Map<org.ovirt.engine.core.common.businessentities.GraphicsType, GraphicsInfo> graphicsInfos) {
        if (graphicsInfos != null) {
            if (graphicsInfos.containsKey(org.ovirt.engine.core.common.businessentities.GraphicsType.SPICE)) {
                return org.ovirt.engine.core.common.businessentities.GraphicsType.SPICE;
            }
            if (graphicsInfos.containsKey(org.ovirt.engine.core.common.businessentities.GraphicsType.VNC)) {
                return org.ovirt.engine.core.common.businessentities.GraphicsType.VNC;
            }
        }
        return null;
    }

    @Mapping(from = Vm.class, to = RunVmOnceParams.class)
    public static RunVmOnceParams map(Vm vm, RunVmOnceParams template) {
        RunVmOnceParams params = template != null ? template : new RunVmOnceParams();
        if (vm.isSetStateless() && vm.isStateless()) {
            params.setRunAsStateless(true);
        }
        if (vm.isSetDisplay()) {
            if (vm.getDisplay().isSetKeyboardLayout()) {
                String vncKeyboardLayout = vm.getDisplay().getKeyboardLayout();
                params.setVncKeyboardLayout(vncKeyboardLayout);
            }

            DisplayMapper.fillDisplayInParams(vm, params);
        }
        if (vm.isSetOs() && vm.getOs().isSetBoot() && vm.getOs().getBoot().isSetDevices() &&
                vm.getOs().getBoot().getDevices().isSetDevices()) {
            params.setBootSequence(map(vm.getOs().getBoot(), null));
        }
        if (vm.isSetCdroms() && vm.getCdroms().isSetCdroms()) {
            String file = vm.getCdroms().getCdroms().get(0).getFile().getId();
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
            params.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperties()));
        }
        if (vm.isSetBios()) {
            if (vm.getBios().isSetBootMenu()) {
                params.setBootMenuEnabled(vm.getBios().getBootMenu().isEnabled());
            }
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().isSetDevices() &&
                    vm.getOs().getBoot().getDevices().isSetDevices()) {
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

        if (vm.isSetCustomCpuModel()) {
            params.setCustomCpuName(vm.getCustomCpuModel());
        }

        if (vm.isSetCustomEmulatedMachine()) {
            params.setCustomEmulatedMachine(vm.getCustomEmulatedMachine());
        }

        return params;
    }

    @Mapping(from = Map.Entry.class, to = GraphicsConsole.class)
    public static GraphicsConsole map(Map.Entry<org.ovirt.engine.core.common.businessentities.GraphicsType, GraphicsInfo> graphicsInfo, GraphicsConsole template) {
        GraphicsConsole model = template != null ? template : new GraphicsConsole();

        GraphicsType graphicsType = map(graphicsInfo.getKey(), null);
        if (graphicsType != null) {
            model.setId(HexUtils.string2hex(graphicsType.value()));
            model.setProtocol(graphicsType);
        }

        if (graphicsInfo.getValue() != null) {
            model.setPort(graphicsInfo.getValue().getPort());
            model.setTlsPort(graphicsInfo.getValue().getTlsPort());
            model.setAddress(graphicsInfo.getValue().getIp());
        }

        return model;
    }

    @Mapping(from = GraphicsConsole.class, to = GraphicsDevice.class)
    public static GraphicsDevice map(GraphicsConsole graphicsConsole, GraphicsDevice template) {
        if (template != null) {
            return template;
        }

        switch (graphicsConsole.getProtocol()) {
            case SPICE:
                return new GraphicsDevice(VmDeviceType.SPICE);
            case VNC:
                return new GraphicsDevice(VmDeviceType.VNC);
            default:
                return template;
        }
    }

    @Mapping(from = GraphicsType.class, to = org.ovirt.engine.core.common.businessentities.GraphicsType.class)
    public static org.ovirt.engine.core.common.businessentities.GraphicsType map(GraphicsType graphicsType, org.ovirt.engine.core.common.businessentities.GraphicsType template) {
        if (graphicsType != null) {
            switch (graphicsType) {
                case SPICE:
                    return org.ovirt.engine.core.common.businessentities.GraphicsType.SPICE;
                case VNC:
                    return org.ovirt.engine.core.common.businessentities.GraphicsType.VNC;
            }
        }

        return null;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.GraphicsType.class, to = GraphicsType.class)
    public static GraphicsType map(org.ovirt.engine.core.common.businessentities.GraphicsType graphicsType, GraphicsType template) {
        if (graphicsType != null) {
            switch (graphicsType) {
                case SPICE:
                    return GraphicsType.SPICE;
                case VNC:
                    return GraphicsType.VNC;
            }
        }

        return null;
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
                    model.getCustomProperties().add(env);
                }
            }
        }
        return model;
    }

    @Mapping(from = CustomProperties.class, to = String.class)
    public static String map(CustomProperties model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (CustomProperty env : model.getCustomProperties()) {
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

    @Mapping(from = String.class, to = OriginType.class)
    public static OriginType map(String type, OriginType incoming) {
        try {
            return OriginType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static OriginType mapExternalVmProviderToOrigin(ExternalVmProviderType provider) {
        switch (provider) {
        case KVM:
            return OriginType.KVM;
        case XEN:
            return OriginType.XEN;
        case VMWARE:
            return OriginType.VMWARE;
        default:
            return null;
        }
    }

    @Mapping(from = ConfigurationType.class, to = org.ovirt.engine.core.common.businessentities.ConfigurationType.class)
    public static org.ovirt.engine.core.common.businessentities.ConfigurationType map(org.ovirt.engine.api.model.ConfigurationType configurationType, org.ovirt.engine.core.common.businessentities.ConfigurationType template) {
        switch (configurationType) {
            case OVF:
                return org.ovirt.engine.core.common.businessentities.ConfigurationType.OVF;
            case OVA:
                return org.ovirt.engine.core.common.businessentities.ConfigurationType.OVA;
            default:
                return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.ConfigurationType.class, to = ConfigurationType.class)
    public static ConfigurationType map(org.ovirt.engine.core.common.businessentities.ConfigurationType configurationType, org.ovirt.engine.api.model.ConfigurationType template) {
        switch (configurationType) {
            case OVF:
                return ConfigurationType.OVF;
            case OVA:
                return ConfigurationType.OVA;
            default:
                return null;
        }
    }

    public static Vm map(String data, ConfigurationType type, Vm vm) {
        Initialization initialization = vm.getInitialization();
        if (initialization == null) {
            initialization = new Initialization();
            vm.setInitialization(initialization);
        }
        Configuration configuration = initialization.getConfiguration();
        if (configuration == null) {
            configuration = new Configuration();
            initialization.setConfiguration(configuration);
        }
        configuration.setData(data);
        configuration.setType(type);
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

    private static VmStatus mapVmStatus(VMStatus status) {
        switch (status) {
        case Unassigned:
            return VmStatus.UNASSIGNED;
        case Down:
            return VmStatus.DOWN;
        case Up:
            return VmStatus.UP;
        case PoweringUp:
            return VmStatus.POWERING_UP;
        case Paused:
            return VmStatus.PAUSED;
        case MigratingFrom:
            return VmStatus.MIGRATING;
        case MigratingTo:
            return VmStatus.MIGRATING;
        case Unknown:
            return VmStatus.UNKNOWN;
        case NotResponding:
            return VmStatus.NOT_RESPONDING;
        case WaitForLaunch:
            return VmStatus.WAIT_FOR_LAUNCH;
        case RebootInProgress:
            return VmStatus.REBOOT_IN_PROGRESS;
        case SavingState:
            return VmStatus.SAVING_STATE;
        case RestoringState:
            return VmStatus.RESTORING_STATE;
        case Suspended:
            return VmStatus.SUSPENDED;
        case ImageLocked:
            return VmStatus.IMAGE_LOCKED;
        case PoweringDown:
            return VmStatus.POWERING_DOWN;
        default:
            return null;
        }
    }

    @Mapping(from = AutoPinningPolicy.class, to = CpuPinningPolicy.class)
    public static org.ovirt.engine.core.common.businessentities.CpuPinningPolicy map(AutoPinningPolicy autoPinningPolicy) {
        switch (autoPinningPolicy) {
        case ADJUST:
            return org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.RESIZE_AND_PIN_NUMA;
        default:
            return org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.NONE;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.class, to = AutoPinningPolicy.class)
    public static AutoPinningPolicy map(org.ovirt.engine.core.common.businessentities.CpuPinningPolicy cpuPinningPolicy, AutoPinningPolicy template) {
        switch (cpuPinningPolicy) {
            case RESIZE_AND_PIN_NUMA:
            return AutoPinningPolicy.ADJUST;
        default:
            return AutoPinningPolicy.DISABLED;
        }
    }

    @Mapping(from = CpuPinningPolicy.class, to = org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.class)
    public static org.ovirt.engine.core.common.businessentities.CpuPinningPolicy map(CpuPinningPolicy cpuPinningPolicy) {
        switch (cpuPinningPolicy) {
            case RESIZE_AND_PIN_NUMA:
                return org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.RESIZE_AND_PIN_NUMA;
            default:
                return org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.NONE;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.CpuPinningPolicy.class, to = CpuPinningPolicy.class)
    public static CpuPinningPolicy map(org.ovirt.engine.core.common.businessentities.CpuPinningPolicy cpuPinningPolicy) {
        switch (cpuPinningPolicy) {
            case RESIZE_AND_PIN_NUMA:
                return CpuPinningPolicy.RESIZE_AND_PIN_NUMA;
            default:
                return CpuPinningPolicy.NONE;
        }
    }

    @Mapping(from = BootSequence.class, to = Boot.class)
    public static Boot map(BootSequence bootSequence, Boot template) {
        Boot boot = template != null ? template : new Boot();
        Boot.DevicesList list = new Boot.DevicesList();
        boot.setDevices(list);
        List<BootDevice> devices = list.getDevices();
        switch (bootSequence) {
        case C:
            devices.add(BootDevice.HD);
            break;
        case DC:
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.HD);
            break;
        case N:
            devices.add(BootDevice.NETWORK);
            break;
        case CDN:
            devices.add(BootDevice.HD);
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.NETWORK);
            break;
        case CND:
            devices.add(BootDevice.HD);
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.CDROM);
            break;
        case DCN:
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.HD);
            devices.add(BootDevice.NETWORK);
            break;
        case DNC:
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.HD);
            break;
        case NCD:
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.HD);
            devices.add(BootDevice.CDROM);
            break;
        case NDC:
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.HD);
            break;
        case CD:
            devices.add(BootDevice.HD);
            devices.add(BootDevice.CDROM);
            break;
        case D:
            devices.add(BootDevice.CDROM);
            break;
        case CN:
            devices.add(BootDevice.HD);
            devices.add(BootDevice.NETWORK);
            break;
        case DN:
            devices.add(BootDevice.CDROM);
            devices.add(BootDevice.NETWORK);
            break;
        case NC:
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.HD);
            break;
        case ND:
            devices.add(BootDevice.NETWORK);
            devices.add(BootDevice.CDROM);
            break;
        }
        return boot;
    }

    @Mapping(from = Boot.class, to = List.class)
    public static BootSequence map(Boot boot, BootSequence template) {
        Set<BootDevice> devSet = new LinkedHashSet<>();
        for (BootDevice device : boot.getDevices().getDevices()) {
            if (device != null) {
                devSet.add(device);
            }
        }

        List<BootDevice> devs = new ArrayList<>(devSet);
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
                    model.setType(deviceType);
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
            entity.setDeviceType(map(model.getType(), null));
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
                List<VmInitNetwork> interfaces = new ArrayList<>();
                for (Nic iface : model.getNetworkConfiguration().getNics().getNics()) {
                    VmInitNetwork vmInitInterface = new VmInitNetwork();
                    if (iface.isSetName()) {
                        vmInitInterface.setName(iface.getName());
                    }
                    interfaces.add(vmInitInterface);
                    if (iface.isSetBootProtocol()) {
                        Ipv4BootProtocol protocol = Ipv4BootProtocolMapper.map(iface.getBootProtocol());
                        vmInitInterface.setBootProtocol(protocol);
                        if (protocol != Ipv4BootProtocol.DHCP && iface.isSetNetwork() && iface.getNetwork().isSetIp()) {
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
                    List<String> dnsServers = new ArrayList<>();
                    for (Host host : model.getNetworkConfiguration().getDns().getServers().getHosts()) {
                        if (host.isSetAddress()) {
                            dnsServers.add(host.getAddress());
                        }
                    }
                    entity.setDnsServers(String.join(" ", dnsServers));
                }

                if (model.getNetworkConfiguration().getDns().isSetSearchDomains()
                        && model.getNetworkConfiguration().getDns().getSearchDomains().isSetHosts()
                        && !model.getNetworkConfiguration().getDns().getSearchDomains().getHosts().isEmpty()) {
                    List<String> searchDomains = new ArrayList<>();
                    for (Host host : model.getNetworkConfiguration().getDns().getSearchDomains().getHosts()) {
                        if (host.isSetAddress()) {
                            searchDomains.add(host.getAddress());
                        }
                    }
                    entity.setDnsSearch(String.join(" ", searchDomains));
                }
            }
        }

        if (model.isSetTimezone() && model.getTimezone() != null) {
            entity.setTimeZone(model.getTimezone());
        }

        if (model.isSetUsers()) {
            for (User user : model.getUsers().getUsers()) {
                String userName = user.getUserName();
                if (StringUtils.equals(userName, "root")) {
                    entity.setUserName(userName);
                    String userPassword = user.getPassword();
                    if (userPassword != null) {
                        entity.setRootPassword(userPassword);
                    }
                }
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

    public static UsbPolicy getUsbPolicyOnCreate(Usb usb) {
        if (usb == null || !usb.isSetEnabled() || !usb.isEnabled()) {
            return UsbPolicy.DISABLED;
        } else {
            UsbType usbType = getUsbType(usb);
            if (usbType == null) {
                return null;
            } else {
                return getUsbPolicyAccordingToUsbType(usbType);
            }
        }
    }

    public static UsbPolicy getUsbPolicyOnUpdate(Usb usb, UsbPolicy currentPolicy) {
        if (usb == null) {
            return currentPolicy;
        }

        if (usb.isSetEnabled()) {
            if (!usb.isEnabled()) {
                return UsbPolicy.DISABLED;
            } else {
                UsbType usbType = getUsbType(usb);
                if (usbType != null) {
                    return getUsbPolicyAccordingToUsbType(usbType);
                } else {
                    return null;
                }
            }
        } else {
            if (currentPolicy == UsbPolicy.DISABLED) {
                return UsbPolicy.DISABLED;
            }

            UsbType usbType = getUsbType(usb);
            if (usbType != null) {
                return getUsbPolicyAccordingToUsbType(usb.getType());
            } else {
                return currentPolicy;
            }
        }
    }

    private static UsbType getUsbType(Usb usb) {
        return usb.isSetType() ? usb.getType() : null;
    }

    private static UsbPolicy getUsbPolicyAccordingToUsbType(UsbType usbType) {
        switch (usbType) {
        case LEGACY:
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
            if (vm.getConsoleUserId()!=null) {
                consoleUser.setId(vm.getConsoleUserId().toString());
            }
            Session consoleSession = new Session();
            consoleSession.setUser(consoleUser);
            if (vm.getClientIp()!=null && !vm.getClientIp().isEmpty()) {
                Ip ip = new Ip();
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
}
