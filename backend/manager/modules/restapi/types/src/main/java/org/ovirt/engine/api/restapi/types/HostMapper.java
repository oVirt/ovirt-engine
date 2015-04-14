package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.AutoNumaStatus;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.HardwareInformation;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostProtocol;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.HostType;
import org.ovirt.engine.api.model.HostedEngine;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.KSM;
import org.ovirt.engine.api.model.KdumpStatus;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.SELinux;
import org.ovirt.engine.api.model.SELinuxMode;
import org.ovirt.engine.api.model.SPM;
import org.ovirt.engine.api.model.SSH;
import org.ovirt.engine.api.model.SpmState;
import org.ovirt.engine.api.model.StorageManager;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.api.restapi.model.AuthenticationMethod;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.compat.Guid;


public class HostMapper {

    public final static Long BYTES_IN_MEGABYTE = 1024L * 1024L;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_VDSM_PORT = 54321;
    private static final String MD5_FILE_SIGNATURE = "md5";

    private static final String HOST_OS_DELEIMITER = " - ";

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
            entity.setVdsGroupId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetAddress()) {
            entity.setHostName(model.getAddress());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setPort(model.getPort());
        } else {
            entity.setPort(DEFAULT_VDSM_PORT);
        }
        if (model.isSetProtocol()) {
            map(model.getProtocol(), entity);
        }
        if (model.isSetSsh()) {
            map(model.getSsh(), entity);
        }
        if (model.isSetPowerManagement()) {
            entity = map(model.getPowerManagement(), entity);
        }
        if (model.isSetStorageManager()) {
            if (model.getStorageManager().getPriority() != null) {
                entity.setVdsSpmPriority(model.getStorageManager().getPriority());
            }
        }
        if (model.isSetSpm()) {
            if (model.getSpm().getPriority() != null) {
                entity.setVdsSpmPriority(model.getSpm().getPriority());
            }
        }
        if (model.isSetDisplay() && model.getDisplay().isSetAddress()) {
            entity.setConsoleAddress("".equals(model.getDisplay().getAddress()) ? null : model.getDisplay().getAddress());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetExternalHostProvider()) {
            String providerId = model.getExternalHostProvider().getId();
            entity.setHostProviderId(providerId == null ? null : GuidUtils.asGuid(providerId));
        }
        return entity;
    }

    @Mapping(from = SSH.class, to = VdsStatic.class)
    public static VdsStatic map(SSH model, VdsStatic template) {
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
            String delim = "";
            StringBuilder builder = new StringBuilder();
            for (PmProxy pmProxy : model.getPmProxies().getPmProxy()) {
                builder.append(delim);
                builder.append(pmProxy.getType());
                delim = ",";
            }
            entity.setPmProxyPreferences(builder.toString());
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
        if (model.isSetName() && (!model.getName().isEmpty()) && model.isSetValue() && (!model.getValue().isEmpty())) {
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
        if (entity.getVdsGroupId() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getVdsGroupId().toString());
            model.setCluster(cluster);
        }
        model.setAddress(entity.getHostName());
        if (entity.getPort() > 0) {
            model.setPort(entity.getPort());
        }
        HostProtocol protocol = map(entity.getProtocol(), null);
        model.setProtocol(protocol != null ? protocol.value() : null);
        HostStatus status = map(entity.getStatus(), null);
        model.setStatus(StatusUtils.create(status));
        if (status == HostStatus.NON_OPERATIONAL) {
            model.getStatus().setDetail(entity.getNonOperationalReason().name().toLowerCase());
        } else if (status == HostStatus.MAINTENANCE || status == HostStatus.PREPARING_FOR_MAINTENANCE) {
            model.getStatus().setDetail(entity.getMaintenanceReason());
        }
        StorageManager sm = new StorageManager();
        sm.setPriority(entity.getVdsSpmPriority());
        sm.setValue(entity.getSpmStatus() == VdsSpmStatus.SPM);
        model.setStorageManager(sm);
        SPM spm = new SPM();
        spm.setPriority(entity.getVdsSpmPriority());
        if (spm.getStatus() != null) {
            spm.setStatus(StatusUtils.create(map(entity.getSpmStatus(), null)));
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
        model.setOs(getHostOs(entity.getHostOs()));
        model.setKsm(new KSM());
        model.getKsm().setEnabled(Boolean.TRUE.equals(entity.getKsmState()));
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(!(entity.getTransparentHugePagesState() == null ||
                entity.getTransparentHugePagesState() == VdsTransparentHugePagesState.Never));
        if (entity.getIScsiInitiatorName() != null) {
            model.setIscsi(new IscsiDetails());
            model.getIscsi().setInitiator(entity.getIScsiInitiatorName());
        }
        PowerManagement pm = map(entity, (PowerManagement) null);
        model.setPowerManagement(DeprecatedPowerManagementMapper.map(entity.getFenceAgents(), pm));
        model.setHardwareInformation(map(entity, (HardwareInformation)null));
        model.setSsh(map(entity.getStaticData(), (SSH) null));
        CPU cpu = new CPU();
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
            model.setType(type != null ? type.value() : null);
        }
        model.setMemory(Long.valueOf(entity.getPhysicalMemMb() == null ? 0 : entity.getPhysicalMemMb()
                * BYTES_IN_MEGABYTE));
        model.setMaxSchedulingMemory((int) entity.getMaxSchedulingMemory() * BYTES_IN_MEGABYTE);

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
        model.setSelinux(map(entity, (SELinux) null));
        model.setAutoNumaStatus(map(entity.getAutoNumaBalancing(), null));
        model.setNumaSupported(entity.isNumaSupport());

        model.setLiveSnapshotSupport(entity.getLiveSnapshotSupport());

        if (entity.getHostProviderId() != null) {
            model.setExternalHostProvider(new ExternalHostProvider());
            model.getExternalHostProvider().setId(entity.getHostProviderId().toString());
        }

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

    private static OperatingSystem getHostOs(String hostOs) {
        if (hostOs == null || hostOs.trim().length() == 0) {
            return null;
        }
        String[] hostOsInfo = hostOs.split(HOST_OS_DELEIMITER);
        Version version = new Version();
        version.setMajor(getIntegerValue(hostOsInfo, 1));
        version.setMinor(getIntegerValue(hostOsInfo, 2));
        version.setFullVersion(getFullHostOsVersion(hostOsInfo));
        OperatingSystem os = new OperatingSystem();
        os.setType(hostOsInfo[0]);
        os.setVersion(version);
        return os;
    }

    private static Integer getIntegerValue(String[] hostOsInfo, int indx) {
        if (hostOsInfo.length <= indx) {
            return null;
        }
        try {
            return Integer.valueOf(hostOsInfo[indx]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String getFullHostOsVersion(String[] hostOsInfo) {
        StringBuilder buf = new StringBuilder("");
        for(int i = 1; i < hostOsInfo.length; i++) {
            if(i > 1) {
                buf.append(HOST_OS_DELEIMITER);
            }
            buf.append(hostOsInfo[i]);
        }
        return buf.toString();
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
        model.setSupportedRngSources(RngDeviceMapper.mapRngSources(entity.getSupportedRngSources(), null));
        return model;
    }

    @Mapping(from = VdsStatic.class, to = SSH.class)
    public static SSH map(VdsStatic entity, SSH template) {
        SSH model = template != null ? template : new SSH();
        model.setPort(entity.getSshPort());
        model.setUser(new User());
        model.getUser().setUserName(entity.getSshUsername());
        model.setFingerprint(entity.getSshKeyFingerprint());
        return model;
    }

    @Mapping(from = VDS.class, to = PowerManagement.class)
    public static PowerManagement map(VDS entity, PowerManagement template) {
        PowerManagement model = template != null ? template : new PowerManagement();
        if (entity.getPmProxyPreferences() != null) {
            PmProxies pmProxies = new PmProxies();
                String[] proxies = StringUtils.split(entity.getPmProxyPreferences(), ",");
                for (String proxy : proxies) {
                        PmProxy pmProxy = new PmProxy();
                pmProxy.setType(proxy);
                        pmProxies.getPmProxy().add(pmProxy);
                }
            model.setPmProxies(pmProxies);
        }
        model.setKdumpDetection(entity.isPmKdumpDetection());
        model.setEnabled(entity.isPmEnabled());
        model.setAutomaticPmEnabled(!entity.isDisablePowerManagementPolicy());
        if (entity.isPmEnabled()) {
            DeprecatedPowerManagementMapper.map(entity.getFenceAgents(), model);
        }
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
            return HostType.RHEV_H;
        default:
            return null;
        }
    }

    @Mapping(from = HashMap.class, to = Hooks.class)
    public static Hooks map(HashMap<String, HashMap<String, HashMap<String, String>>> dictionary, Hooks hooks) {
        if (hooks == null) {
            hooks = new Hooks();
        }
        for (Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair : dictionary.entrySet()) { // events
            for (Map.Entry<String, HashMap<String, String>> keyValuePair1 : keyValuePair.getValue() // hooks
                    .entrySet()) {
                Hook hook = createHook(keyValuePair, keyValuePair1);
                hooks.getHooks().add(hook);
            }
        }
        return hooks;
    }

    @Mapping(from = AuthenticationMethod.class, to = org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod.class)
    public static org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod map(AuthenticationMethod template,
            org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod authType) {
        switch (template) {
        case PASSWORD:
            return org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod.Password;

        case PUBLICKEY:
            return org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod.PublicKey;

        default:
            return org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod.Password;
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
            if (action.getSsh().isSetAuthenticationMethod()) {
                params.setAuthMethod(map(AuthenticationMethod.fromValue(action.getSsh().getAuthenticationMethod()), null));
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
            if (host.getSsh().isSetAuthenticationMethod()) {
                params.setAuthMethod(map(AuthenticationMethod.fromValue(host.getSsh().getAuthenticationMethod()), null));
            }
        }
        return params;
    }

    @Mapping(from = VDS.class, to = SELinux.class)
    public static SELinux map(VDS entity, SELinux template) {
        SELinux model = template != null ? template : new SELinux();
        if (entity.getSELinuxEnforceMode() == null) {
            return model;
        }

        String mode = null;
        switch (entity.getSELinuxEnforceMode()) {
            case DISABLED:
                mode = SELinuxMode.DISABLED.value();
                break;
            case PERMISSIVE:
                mode = SELinuxMode.PERMISSIVE.value();
                break;
            case ENFORCING:
                mode = SELinuxMode.ENFORCING.value();
        }
        model.setMode(mode);

        return model;
    }

    private static Hook createHook(Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair,
            Map.Entry<String, HashMap<String, String>> keyValuePair1) {
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

    @Mapping(from = org.ovirt.engine.core.common.businessentities.KdumpStatus.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.KdumpStatus kdumpStatus, String template) {
        String result = null;
        if (kdumpStatus != null) {
            switch (kdumpStatus) {
                case UNKNOWN:
                    result = KdumpStatus.UNKNOWN.value();
                    break;
                case DISABLED:
                    result = KdumpStatus.DISABLED.value();
                    break;
                case ENABLED:
                    result = KdumpStatus.ENABLED.value();
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @Mapping(from = AutoNumaBalanceStatus.class, to = String.class)
    public static String map(AutoNumaBalanceStatus autoNumaStatus, String template) {
        String result = null;
        if (autoNumaStatus != null) {
            switch (autoNumaStatus) {
            case DISABLE:
                result = AutoNumaStatus.DISABLE.value();
                break;
            case ENABLE:
                result = AutoNumaStatus.ENABLE.value();
                break;
            case UNKNOWN:
                result = AutoNumaStatus.UNKNOWN.value();
                break;
            default:
                break;
            }
        }
        return result;
    }

    @Mapping(from = VdsProtocol.class, to = HostProtocol.class)
    public static HostProtocol map(VdsProtocol protocol, HostProtocol template) {
        HostProtocol result = null;
        if (protocol != null) {
            switch (protocol) {
                case STOMP:
                    result =  HostProtocol.STOMP;
                    break;
                case AMQP:
                    result = HostProtocol.AMQP;
                    break;
                case XML:
                default:
                    result = HostProtocol.XML;
                    break;
            }
        }
        return result;
    }

    @Mapping(from = String.class, to = VdsStatic.class)
    public static VdsStatic map(String protocol, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        HostProtocol hostProtocol = HostProtocol.fromValue(protocol);
        VdsProtocol result = null;
        switch (hostProtocol) {
            case STOMP:
                result =  VdsProtocol.STOMP;
                break;
            case AMQP:
                result = VdsProtocol.AMQP;
                break;
            case XML:
            default:
                result = VdsProtocol.XML;
                break;
        }
        entity.setProtocol(result);
        return entity;
    }

    @Mapping(from = VdsSpmStatus.class, to = SpmState.class)
    public static SpmState map(VdsSpmStatus entityStatus, SpmState template) {
        switch (entityStatus) {
            case None:
                return SpmState.NONE;
            case Contending:
                return SpmState.CONTENDING;
            case SPM:
                return SpmState.SPM;
            default:
                return null;
        }
    }
}
