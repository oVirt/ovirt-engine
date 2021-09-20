package org.ovirt.engine.api.restapi.types;

import static java.util.stream.Collectors.toCollection;
import static org.ovirt.engine.core.compat.Version.VERSION_NOT_SET;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.AutoNumaStatus;
import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.ExternalStatus;
import org.ovirt.engine.api.model.HardwareInformation;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostDevicePassthrough;
import org.ovirt.engine.api.model.HostProtocol;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.HostType;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.KdumpStatus;
import org.ovirt.engine.api.model.Ksm;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PmProxyType;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.SeLinux;
import org.ovirt.engine.api.model.SeLinuxMode;
import org.ovirt.engine.api.model.Spm;
import org.ovirt.engine.api.model.SpmStatus;
import org.ovirt.engine.api.model.Ssh;
import org.ovirt.engine.api.model.SshAuthenticationMethod;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VgpuPlacement;
import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.CertificateSubjectHelper;
import org.ovirt.engine.core.utils.OS;

public class HostMapper {

    public static final Long BYTES_IN_MEGABYTE = 1024L * 1024L;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_VDSM_PORT = 54321;
    // MD5 file signature is not necessary MD5. It actually can be SHA256
    // The name in the Host class is kept to avoid changing the API
    private static final String MD5_FILE_SIGNATURE = "checksum";

    @Mapping(from = Host.class, to = VdsStatic.class)
    public static VdsStatic map(Host model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetAddress()) {
            entity.setHostName(model.getAddress());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setPort(model.getPort());
        } else {
            entity.setPort(DEFAULT_VDSM_PORT);
        }
        if (model.isSetSsh()) {
            map(model.getSsh(), entity);
        }
        if (model.isSetPowerManagement()) {
            entity = map(model.getPowerManagement(), entity);
        }
        if (model.isSetSpm()) {
            if (model.getSpm().getPriority() != null) {
                entity.setVdsSpmPriority(model.getSpm().getPriority());
            }
        }
        if (model.isSetDisplay() && model.getDisplay().isSetAddress()) {
            entity.setConsoleAddress("".equals(model.getDisplay().getAddress()) ? null : model.getDisplay().getAddress());
        }
        if (model.isSetVgpuPlacement()) {
            entity.setVgpuPlacement(mapVgpuPlacement(model.getVgpuPlacement()));
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetExternalHostProvider()) {
            String providerId = model.getExternalHostProvider().getId();
            entity.setHostProviderId(providerId == null ? null : GuidUtils.asGuid(providerId));
        }
        if (model.isSetOs()) {
            mapOperatingSystem(model.getOs(), entity);
        }

        return entity;
    }

    public static VdsStatic mapOperatingSystem(OperatingSystem model, VdsStatic template) {
        final VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetCustomKernelCmdline()) {
            entity.setCurrentKernelCmdline(model.getCustomKernelCmdline());
            entity.setKernelCmdlineParsable(false);
        }
        return entity;
    }

    @Mapping(from = Ssh.class, to = VdsStatic.class)
    public static VdsStatic map(Ssh model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetUser() && model.getUser().isSetUserName()) {
            entity.setSshUsername(model.getUser().getUserName());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setSshPort(model.getPort());
        }
        if (model.isSetFingerprint()) {
            entity.setSshKeyFingerprint(model.getFingerprint());
        }
        if (model.isSetPublicKey()) {
            entity.setSshPublicKey(model.getPublicKey());
        }
        return entity;
    }

    @Mapping(from = PowerManagement.class, to = VdsStatic.class)
    public static VdsStatic map(PowerManagement model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetEnabled()) {
            entity.setPmEnabled(model.isEnabled());
        }
        if (model.isSetAutomaticPmEnabled()) {
            entity.setDisablePowerManagementPolicy(!model.isAutomaticPmEnabled());
        }
        if (model.isSetPmProxies()) {
            List<FenceProxySourceType> fenceProxySources =
                    model.getPmProxies()
                            .getPmProxies()
                            .stream()
                            .map(pmProxy -> FenceProxySourceType.forValue(pmProxy.getType().toString()))
                            .collect(toCollection(LinkedList::new));
            entity.setFenceProxySources(fenceProxySources);
        }
        if (model.isSetKdumpDetection()) {
            entity.setPmKdumpDetection(model.isKdumpDetection());
        }
        return entity;
    }

    @Mapping(from = Options.class, to = String.class)
    public static String map(Options model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (Option option : model.getOptions()) {
            String opt = map(option, null);
            if (opt != null) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(opt);
            }
        }
        return buf.toString();
    }

    @Mapping(from = Option.class, to = String.class)
    public static String map(Option model, String template) {
        if (model.isSetName() && !model.getName().isEmpty() && model.isSetValue() && !model.getValue().isEmpty()) {
            return model.getName() + "=" + model.getValue();
        } else {
            return template;
        }
    }

    @Mapping(from = VDS.class, to = Host.class)
    public static Host map(VDS entity, Host template) {
        Host model = template != null ? template : new Host();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setComment(entity.getComment());
        if (entity.getClusterId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getClusterId().toString());
            model.setCluster(cluster);
        }
        model.setAddress(entity.getHostName());
        if (entity.getPort() > 0) {
            model.setPort(entity.getPort());
        }
        // We return always STOMP because support for XML-RPC was removed in version 4.1 of the engine.
        model.setProtocol(HostProtocol.STOMP);
        HostStatus status = map(entity.getStatus(), null);
        model.setStatus(status);
        if (entity.getExternalStatus() != null) {
            ExternalStatus externalStatus = ExternalStatusMapper.map(entity.getExternalStatus());
            model.setExternalStatus(externalStatus);
        }
        if (status == HostStatus.NON_OPERATIONAL) {
            model.setStatusDetail(entity.getNonOperationalReason().name().toLowerCase());
        } else if (status == HostStatus.MAINTENANCE || status == HostStatus.PREPARING_FOR_MAINTENANCE) {
            model.setStatusDetail(entity.getMaintenanceReason());
        }
        Spm spm = new Spm();
        spm.setPriority(entity.getVdsSpmPriority());
        if (entity.getSpmStatus() != null) {
            spm.setStatus(mapSpmStatus(entity.getSpmStatus()));
        }
        model.setSpm(spm);
        if (entity.getVersion() != null &&
                entity.getVersion().getMajor() != -1 &&
                entity.getVersion().getMinor() != -1 &&
                entity.getVersion().getRevision() != -1 &&
                entity.getVersion().getBuild() != -1) {
            Version version = new Version();
            version.setMajor(entity.getVersion().getMajor());
            version.setMinor(entity.getVersion().getMinor());
            version.setRevision(entity.getVersion().getRevision());
            version.setBuild(entity.getVersion().getBuild());
            version.setFullVersion(entity.getVersion().getRpmName());
            model.setVersion(version);
        }
        model.setOs(mapOperatingSystem(entity));
        model.setKsm(new Ksm());
        model.getKsm().setEnabled(Boolean.TRUE.equals(entity.getKsmState()));
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(!(entity.getTransparentHugePagesState() == null ||
                entity.getTransparentHugePagesState() == VdsTransparentHugePagesState.Never));
        if (entity.getIScsiInitiatorName() != null) {
            model.setIscsi(new IscsiDetails());
            model.getIscsi().setInitiator(entity.getIScsiInitiatorName());
        }
        model.setPowerManagement(map(entity, (PowerManagement) null));
        model.setHardwareInformation(map(entity, (HardwareInformation)null));
        model.setSsh(map(entity.getStaticData(), null));
        Cpu cpu = new Cpu();
        CpuTopology cpuTopology = new CpuTopology();
        if (entity.getCpuSockets() != null) {
            cpuTopology.setSockets(entity.getCpuSockets());
            if (entity.getCpuCores()!=null) {
                cpuTopology.setCores(entity.getCpuCores()/entity.getCpuSockets());
                if (entity.getCpuThreads() != null) {
                    cpuTopology.setThreads(entity.getCpuThreads()/entity.getCpuCores());
                }
            }
        }
        cpu.setTopology(cpuTopology);
        cpu.setName(entity.getCpuModel());
        if (entity.getCpuName() != null) {
            cpu.setType(entity.getCpuName().getCpuName());
        }
        if (entity.getCpuSpeedMh()!=null) {
            cpu.setSpeed(new BigDecimal(entity.getCpuSpeedMh()));
        }
        model.setCpu(cpu);
        VmSummary vmSummary = new VmSummary();
        vmSummary.setActive(entity.getVmActive());
        vmSummary.setMigrating(entity.getVmMigrating());
        vmSummary.setTotal(entity.getVmCount());
        model.setSummary(vmSummary);
        if (entity.getVdsType() != null) {
            HostType type = map(entity.getVdsType(), null);
            model.setType(type);
        }
        model.setMemory(Long.valueOf(entity.getPhysicalMemMb() == null ? 0 : entity.getPhysicalMemMb()
                * BYTES_IN_MEGABYTE));
        model.setMaxSchedulingMemory((int) entity.getMaxSchedulingMemory() * BYTES_IN_MEGABYTE);
        model.setVgpuPlacement(mapVgpuPlacement(entity.getVgpuPlacement()));

        if (entity.getLibvirtVersion() != null &&
                entity.getLibvirtVersion().getMajor() != -1 &&
                entity.getLibvirtVersion().getMinor() != -1 &&
                entity.getLibvirtVersion().getRevision() != -1 &&
                entity.getLibvirtVersion().getBuild() != -1) {
            Version version = new Version();
            version.setMajor(entity.getLibvirtVersion().getMajor());
            version.setMinor(entity.getLibvirtVersion().getMinor());
            version.setRevision(entity.getLibvirtVersion().getRevision());
            version.setBuild(entity.getLibvirtVersion().getBuild());
            version.setFullVersion(entity.getLibvirtVersion().getRpmName());
            model.setLibvirtVersion(version);
        }

        if (entity.getConsoleAddress() != null && !"".equals(entity.getConsoleAddress())) {
            model.setDisplay(new Display());
            model.getDisplay().setAddress(entity.getConsoleAddress());
        }

        model.setKdumpStatus(map(entity.getKdumpStatus(), null));
        model.setSeLinux(map(entity, (SeLinux) null));
        model.setAutoNumaStatus(map(entity.getAutoNumaBalancing(), null));
        model.setNumaSupported(entity.isNumaSupport());

        if (entity.getHostProviderId() != null) {
            model.setExternalHostProvider(new ExternalHostProvider());
            model.getExternalHostProvider().setId(entity.getHostProviderId().toString());
        }

        model.setUpdateAvailable(entity.isUpdateAvailable());

        HostDevicePassthrough devicePassthrough = model.getDevicePassthrough();
        if (devicePassthrough == null) {
            devicePassthrough = new HostDevicePassthrough();
            model.setDevicePassthrough(devicePassthrough);
        }
        devicePassthrough.setEnabled(entity.isHostDevicePassthroughEnabled());

        if(entity.getHostName() != null) {
            String subject = CertificateSubjectHelper.getCertificateSubject(entity.getHostName());
            model.setCertificate(new Certificate());
            model.getCertificate().setSubject(subject);
            model.getCertificate().setOrganization(subject.split(",")[0].replace("O=", ""));
        }

        model.setReinstallationRequired(entity.getStaticData().isReinstallRequired());
        model.setOvnConfigured(entity.isOvnConfigured());

        return model;
    }

    @Mapping(from = VDS.class, to = HostedEngine.class)
    public static HostedEngine map(VDS entity, HostedEngine template) {
        HostedEngine hostedEngine = template != null ? template : new HostedEngine();
        hostedEngine.setConfigured(entity.getHighlyAvailableIsConfigured());
        hostedEngine.setActive(entity.getHighlyAvailableIsActive());
        hostedEngine.setScore(entity.getHighlyAvailableScore());
        hostedEngine.setGlobalMaintenance(entity.getHighlyAvailableGlobalMaintenance());
        hostedEngine.setLocalMaintenance(entity.getHighlyAvailableLocalMaintenance());
        return hostedEngine;
    }

    private static OperatingSystem mapOperatingSystem(VDS entity) {
        final OperatingSystem model = new OperatingSystem();
        if (StringUtils.isNotBlank(entity.getHostOs())) {
            OS hostOs = OS.fromPackageVersionString(entity.getHostOs());
            Version version = new Version();

            if (hostOs.getVersion().getMajor() != VERSION_NOT_SET) {
                version.setMajor(hostOs.getVersion().getMajor());
            }

            if (hostOs.getVersion().getMinor() != VERSION_NOT_SET) {
                version.setMinor(hostOs.getVersion().getMinor());
            }

            if (hostOs.getVersion().getBuild() != VERSION_NOT_SET) {
                version.setBuild(hostOs.getVersion().getBuild());
            }
            version.setFullVersion(hostOs.getFullVersion());
            model.setVersion(version);
            model.setType(hostOs.getName());
        }
        model.setCustomKernelCmdline(Objects.toString(entity.getCurrentKernelCmdline(), ""));
        model.setReportedKernelCmdline(entity.getKernelArgs());
        return model;
    }

    @Mapping(from = String.class, to = OsType.class)
    public static OsType map(String osType, OsType template) {
        return OsType.fromValue(osType);
    }

    @Mapping(from = VDS.class, to = HardwareInformation.class)
    public static HardwareInformation map(VDS entity, HardwareInformation template) {
        HardwareInformation model = template != null ? template : new HardwareInformation();
        model.setManufacturer(entity.getHardwareManufacturer());
        model.setFamily(entity.getHardwareFamily());
        model.setProductName(entity.getHardwareProductName());
        model.setSerialNumber(entity.getHardwareSerialNumber());
        model.setUuid(entity.getHardwareUUID());
        model.setVersion(entity.getHardwareVersion());
        model.setSupportedRngSources(new HardwareInformation.SupportedRngSourcesList());
        model.getSupportedRngSources().getSupportedRngSources().addAll(RngDeviceMapper.mapRngSources(entity.getSupportedRngSources()));
        return model;
    }

    @Mapping(from = VdsStatic.class, to = Ssh.class)
    public static Ssh map(VdsStatic entity, Ssh template) {
        Ssh model = template != null ? template : new Ssh();
        model.setPort(entity.getSshPort());
        model.setUser(new User());
        model.getUser().setUserName(entity.getSshUsername());
        model.setFingerprint(entity.getSshKeyFingerprint());
        model.setPublicKey(entity.getSshPublicKey());
        return model;
    }

    @Mapping(from = VDS.class, to = PowerManagement.class)
    public static PowerManagement map(VDS entity, PowerManagement template) {
        PowerManagement model = template != null ? template : new PowerManagement();
        if (entity.getFenceProxySources() != null) {
            PmProxies pmProxies = new PmProxies();
            for (FenceProxySourceType fenceProxySource : entity.getFenceProxySources()) {
                PmProxy pmProxy = new PmProxy();
                pmProxy.setType(map(fenceProxySource, null));
                pmProxies.getPmProxies().add(pmProxy);
            }
            model.setPmProxies(pmProxies);
        }
        model.setKdumpDetection(entity.isPmKdumpDetection());
        model.setEnabled(entity.isPmEnabled());
        model.setAutomaticPmEnabled(!entity.isDisablePowerManagementPolicy());
        return model;
    }

    @Mapping(from = Map.class, to = Options.class)
    public static Options map(Map<String, String> entity, Options template) {
        Options model = template != null ? template : new Options();
        for (Map.Entry<String, String> option : entity.entrySet()) {
            model.getOptions().add(map(option, null));
        }
        return model;
    }

    @Mapping(from = Map.Entry.class, to = Option.class)
    public static Option map(Map.Entry<String, String> entity, Option template) {
        Option model = template != null ? template : new Option();
        model.setName(entity.getKey());
        model.setValue(entity.getValue());
        return model;
    }

    @Mapping(from = VDSStatus.class, to = HostStatus.class)
    public static HostStatus map(VDSStatus entityStatus, HostStatus template) {
        switch (entityStatus) {
        case Unassigned:
            return HostStatus.UNASSIGNED;
        case Down:
            return HostStatus.DOWN;
        case Maintenance:
            return HostStatus.MAINTENANCE;
        case Up:
            return HostStatus.UP;
        case NonResponsive:
            return HostStatus.NON_RESPONSIVE;
        case Error:
            return HostStatus.ERROR;
        case Installing:
            return HostStatus.INSTALLING;
        case InstallFailed:
            return HostStatus.INSTALL_FAILED;
        case Reboot:
            return HostStatus.REBOOT;
        case PreparingForMaintenance:
            return HostStatus.PREPARING_FOR_MAINTENANCE;
        case NonOperational:
            return HostStatus.NON_OPERATIONAL;
        case PendingApproval:
            return HostStatus.PENDING_APPROVAL;
        case Initializing:
            return HostStatus.INITIALIZING;
        case Connecting:
            return HostStatus.CONNECTING;
        case InstallingOS:
            return HostStatus.INSTALLING_OS;
        case Kdumping:
            return HostStatus.KDUMPING;
        default:
            return null;
        }
    }

    @Mapping(from = VDSType.class, to = HostType.class)
    public static HostType map(VDSType type, HostType template) {
        switch (type) {
        case VDS:
            return HostType.RHEL;
        case oVirtNode:
            return HostType.OVIRT_NODE;
        default:
            return null;
        }
    }

    @Mapping(from = Map.class, to = Hooks.class)
    public static Hooks map(Map<String, Map<String, Map<String, String>>> dictionary, Hooks hooks) {
        if (hooks == null) {
            hooks = new Hooks();
        }
        for (Map.Entry<String, Map<String, Map<String, String>>> keyValuePair : dictionary.entrySet()) { // events
            for (Map.Entry<String, Map<String, String>> keyValuePair1 : keyValuePair.getValue() // hooks
                    .entrySet()) {
                Hook hook = createHook(keyValuePair, keyValuePair1);
                hooks.getHooks().add(hook);
            }
        }
        return hooks;
    }

    private static VdsOperationActionParameters.AuthenticationMethod mapSshAuthenticationMethod(SshAuthenticationMethod method) {
        switch (method) {
        case PASSWORD:
            return VdsOperationActionParameters.AuthenticationMethod.Password;

        case PUBLICKEY:
            return VdsOperationActionParameters.AuthenticationMethod.PublicKey;

        default:
            return VdsOperationActionParameters.AuthenticationMethod.Password;
        }
    }

    @Mapping(from = Action.class, to = VdsOperationActionParameters.class)
    public static VdsOperationActionParameters map(Action action, VdsOperationActionParameters params) {
        params.setPassword(action.getRootPassword());
        if (action.isSetSsh()) {
            if (action.getSsh().isSetUser()) {
                if (action.getSsh().getUser().isSetPassword()) {
                    // For backward compatibility giving priority to rootPassword field
                    if (params.getPassword() == null) {
                        params.setPassword(action.getSsh().getUser().getPassword());
                    }
                }
                if (action.getSsh().getUser().isSetUserName()) {
                      params.getvds().setSshUsername(action.getSsh().getUser().getUserName());
                }
            }
            if (action.getSsh().isSetPort()) {
                params.getvds().setSshPort(action.getSsh().getPort());
            }
            if (action.getSsh().isSetFingerprint()) {
                params.getvds().setSshKeyFingerprint(action.getSsh().getFingerprint());
            }
            if (action.getSsh().isSetPublicKey()){
                params.getvds().setSshPublicKey(action.getSsh().getPublicKey());
            }
            if (action.getSsh().isSetAuthenticationMethod()) {
                params.setAuthMethod(mapSshAuthenticationMethod(action.getSsh().getAuthenticationMethod()));
            }
        }
        if (action.isSetHost()) {
            if (action.getHost().isSetOverrideIptables()) {
                params.setOverrideFirewall(action.getHost().isOverrideIptables());
            }
        }
        return params;
    }

    @Mapping(from = Host.class, to = VdsOperationActionParameters.class)
    public static VdsOperationActionParameters map(Host host, VdsOperationActionParameters params) {
        params.setPassword(host.getRootPassword());
        if (host.isSetSsh()) {
            if (host.getSsh().isSetUser()) {
                if (host.getSsh().getUser().isSetPassword()) {
                    // For backward compatibility giving priority to rootPassword field
                    if (params.getPassword() == null) {
                        params.setPassword(host.getSsh().getUser().getPassword());
                    }
                }
                if (host.getSsh().getUser().isSetUserName()) {
                      params.getvds().setSshUsername(host.getSsh().getUser().getUserName());
                }
            }
            if (host.getSsh().isSetPort()) {
                params.getvds().setSshPort(host.getSsh().getPort());
            }
            if (host.getSsh().isSetFingerprint()) {
                params.getvds().setSshKeyFingerprint(host.getSsh().getFingerprint());
            }
            if (host.getSsh().isSetPublicKey()){
                params.getvds().setSshPublicKey(host.getSsh().getPublicKey());
            }

            if (host.getSsh().isSetAuthenticationMethod()) {
                params.setAuthMethod(mapSshAuthenticationMethod(host.getSsh().getAuthenticationMethod()));
            }
        }
        return params;
    }

    @Mapping(from = VDS.class, to = SeLinux.class)
    public static SeLinux map(VDS entity, SeLinux template) {
        SeLinux model = template != null ? template : new SeLinux();
        if (entity.getSELinuxEnforceMode() == null) {
            return model;
        }

        SeLinuxMode mode = null;
        switch (entity.getSELinuxEnforceMode()) {
            case DISABLED:
                mode = SeLinuxMode.DISABLED;
                break;
            case PERMISSIVE:
                mode = SeLinuxMode.PERMISSIVE;
                break;
            case ENFORCING:
                mode = SeLinuxMode.ENFORCING;
        }
        model.setMode(mode);

        return model;
    }

    private static Hook createHook(Map.Entry<String, Map<String, Map<String, String>>> keyValuePair,
            Map.Entry<String, Map<String, String>> keyValuePair1) {
        String hookName = keyValuePair1.getKey();
        String eventName = keyValuePair.getKey();
        String md5 = keyValuePair1.getValue().get(MD5_FILE_SIGNATURE);
        Hook hook = new Hook();
        hook.setName(hookName);
        hook.setEventName(eventName);
        hook.setMd5(md5);
        setHookId(hook, hookName, eventName, md5);
        return hook;
    }

    private static void setHookId(Hook hook, String hookName, String eventName, String md5) {
        Guid guid = GuidUtils.generateGuidUsingMd5(eventName, hookName, md5);
        hook.setId(guid.toString());
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.KdumpStatus.class, to = KdumpStatus.class)
    public static KdumpStatus map(org.ovirt.engine.core.common.businessentities.KdumpStatus kdumpStatus, KdumpStatus template) {
        KdumpStatus result = null;
        if (kdumpStatus != null) {
            switch (kdumpStatus) {
                case UNKNOWN:
                    result = KdumpStatus.UNKNOWN;
                    break;
                case DISABLED:
                    result = KdumpStatus.DISABLED;
                    break;
                case ENABLED:
                    result = KdumpStatus.ENABLED;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @Mapping(from = AutoNumaBalanceStatus.class, to = AutoNumaStatus.class)
    public static AutoNumaStatus map(AutoNumaBalanceStatus autoNumaStatus, AutoNumaStatus template) {
        AutoNumaStatus result = null;
        if (autoNumaStatus != null) {
            switch (autoNumaStatus) {
            case DISABLE:
                result = AutoNumaStatus.DISABLE;
                break;
            case ENABLE:
                result = AutoNumaStatus.ENABLE;
                break;
            case UNKNOWN:
                result = AutoNumaStatus.UNKNOWN;
                break;
            default:
                break;
            }
        }
        return result;
    }

    public static SpmStatus mapSpmStatus(VdsSpmStatus status) {
        switch (status) {
            case None:
                return SpmStatus.NONE;
            case Contending:
                return SpmStatus.CONTENDING;
            case SPM:
                return SpmStatus.SPM;
            default:
                return null;
        }
    }

    public static VgpuPlacement mapVgpuPlacement(int vgpuPlacement) {
        switch (org.ovirt.engine.core.common.businessentities.VgpuPlacement.forValue(vgpuPlacement)) {
            case CONSOLIDATED:
                return VgpuPlacement.CONSOLIDATED;
            case SEPARATED:
                return VgpuPlacement.SEPARATED;
            default:
                throw new IllegalArgumentException("Unknown vGPU placement \"" + vgpuPlacement + "\"");
        }
    }

    public static int mapVgpuPlacement(VgpuPlacement vgpuPlacement) {
        switch (vgpuPlacement) {
            case CONSOLIDATED:
                return org.ovirt.engine.core.common.businessentities.VgpuPlacement.CONSOLIDATED.getValue();
            case SEPARATED:
                return org.ovirt.engine.core.common.businessentities.VgpuPlacement.SEPARATED.getValue();
            default:
                throw new IllegalArgumentException("Unknown vGPU placement \"" + vgpuPlacement + "\"");
        }
    }

    @Mapping(from = FenceProxySourceType.class, to = PmProxyType.class)
    private static PmProxyType map(FenceProxySourceType fenceProxySource, PmProxyType template) {
        switch (fenceProxySource) {
        case CLUSTER :
            return PmProxyType.CLUSTER;
        case DC:
            return PmProxyType.DC;
        case OTHER_DC:
            return PmProxyType.OTHER_DC;
        default:
            return null;
        }
    }

 }
