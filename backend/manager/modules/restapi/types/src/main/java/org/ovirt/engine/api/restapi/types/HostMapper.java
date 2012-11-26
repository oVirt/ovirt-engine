package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Hooks;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.HostType;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.KSM;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.PmProxies;
import org.ovirt.engine.api.model.PmProxy;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.StorageManager;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class HostMapper {

    public static Long BYTES_IN_MEGABYTE = 1024L * 1024L;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_VDSM_PORT = 54321;
    private static final String MD5_FILE_SIGNATURE = "md5";
    private static final String MD5_SECURITY_ALGORITHM = "MD5";

    private static final String HOST_OS_DELEIMITER = " - ";

    @Mapping(from = Host.class, to = VdsStatic.class)
    public static VdsStatic map(Host model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setvds_name(model.getName());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setvds_group_id(new Guid(model.getCluster().getId()));
        }
        if (model.isSetAddress()) {
            entity.sethost_name(model.getAddress());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setport(model.getPort());
        } else {
            entity.setport(DEFAULT_VDSM_PORT);
        }
        if (model.isSetPowerManagement()) {
            entity = map(model.getPowerManagement(), entity);
        }
        if (model.isSetStorageManager()) {
            if (model.getStorageManager().getPriority() != null) {
                entity.setVdsSpmPriority(model.getStorageManager().getPriority());
            }
        }

        return entity;
    }

    @Mapping(from = PowerManagement.class, to = VdsStatic.class)
    public static VdsStatic map(PowerManagement model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetType()) {
            entity.setpm_type(model.getType());
        }
        if (model.isSetEnabled()) {
            entity.setpm_enabled(model.isEnabled());
        }
        if (model.isSetAddress()) {
            entity.setManagmentIp(model.getAddress());
        }
        if (model.isSetUsername()) {
            entity.setpm_user(model.getUsername());
        }
        if (model.isSetPassword()) {
            entity.setpm_password(model.getPassword());
        }
        if (model.isSetOptions()) {
            entity.setpm_options(map(model.getOptions(), null));
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
        model.setName(entity.getvds_name());
        if (entity.getvds_group_id() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getvds_group_id().toString());
            model.setCluster(cluster);
        }
        model.setAddress(entity.gethost_name());
        if (entity.getport() > 0) {
            model.setPort(entity.getport());
        }
        HostStatus status = map(entity.getstatus(), null);
        model.setStatus(StatusUtils.create(status));
        if (status==HostStatus.NON_OPERATIONAL) {
            model.getStatus().setDetail(entity.getNonOperationalReason().name().toLowerCase());
        }
        StorageManager sm = new StorageManager();
        sm.setPriority(entity.getVdsSpmPriority());
        sm.setValue(entity.getspm_status() == VdsSpmStatus.SPM);
        model.setStorageManager(sm);
        if (entity.getVersion() != null) {
            Version version = new Version();
            version.setMajor(entity.getVersion().getMajor());
            version.setMinor(entity.getVersion().getMinor());
            version.setRevision(entity.getVersion().getRevision());
            version.setBuild(entity.getVersion().getBuild());
            version.setFullVersion(entity.getVersion().getRpmName());
            model.setVersion(version);
        }
        model.setOs(getHostOs(entity.gethost_os()));
        model.setKsm(new KSM());
        model.getKsm().setEnabled(Boolean.TRUE.equals(entity.getksm_state()));
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(!(entity.getTransparentHugePagesState() == null ||
                entity.getTransparentHugePagesState() == VdsTransparentHugePagesState.Never));
        if (entity.getIScsiInitiatorName() != null) {
            model.setIscsi(new IscsiDetails());
            model.getIscsi().setInitiator(entity.getIScsiInitiatorName());
        }
        model.setPowerManagement(map(entity, (PowerManagement) null));
        CPU cpu = new CPU();
        CpuTopology cpuTopology = new CpuTopology();
        if (entity.getcpu_sockets() != null) {
            cpuTopology.setSockets(entity.getcpu_sockets());
            if (entity.getcpu_cores()!=null) {
                cpuTopology.setCores(entity.getcpu_cores()/entity.getcpu_sockets());
            }
        }
        cpu.setTopology(cpuTopology);
        cpu.setName(entity.getcpu_model());
        if (entity.getcpu_speed_mh()!=null) {
            cpu.setSpeed(new BigDecimal(entity.getcpu_speed_mh()));
        }
        model.setCpu(cpu);
        VmSummary vmSummary = new VmSummary();
        vmSummary.setActive(entity.getvm_active());
        vmSummary.setMigrating(entity.getvm_migrating());
        vmSummary.setTotal(entity.getvm_count());
        model.setSummary(vmSummary);
        if (entity.getvds_type() != null) {
            HostType type = map(entity.getvds_type(), null);
            model.setType(type != null ? type.value() : null);
        }
        model.setMemory(Long.valueOf(entity.getphysical_mem_mb() == null ? 0 : entity.getphysical_mem_mb()
                * BYTES_IN_MEGABYTE));
        model.setMaxSchedulingMemory((int) entity.getMaxSchedulingMemory() * BYTES_IN_MEGABYTE);
        return model;
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

    @Mapping(from = VDS.class, to = PowerManagement.class)
    public static PowerManagement map(VDS entity, PowerManagement template) {
        PowerManagement model = template != null ? template : new PowerManagement();
        model.setType(entity.getpm_type());
        model.setEnabled(entity.getpm_enabled());
        model.setAddress(entity.getManagmentIp());
        model.setUsername(entity.getpm_user());
        if (entity.getPmOptionsMap() != null) {
            model.setOptions(map(entity.getPmOptionsMap(), null));
        }
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
        return model;
    }

    @Mapping(from = ValueObjectMap.class, to = Options.class)
    public static Options map(ValueObjectMap entity, Options template) {
        Options model = template != null ? template : new Options();
        for (ValueObjectPair option : entity.getValuePairs()) {
            model.getOptions().add(map(option, null));
        }
        return model;
    }

    @Mapping(from = ValueObjectPair.class, to = Option.class)
    public static Option map(ValueObjectPair entity, Option template) {
        Option model = template != null ? template : new Option();
        model.setName((String)entity.getKey());
        model.setValue((String)entity.getValue());
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
        NGuid guid = generateHookId(eventName, hookName, md5);
        hook.setId(guid != null ? guid.toString() : null);
    }

    public static NGuid generateHookId(String eventName, String hookName, String md5) {
        String idString = eventName + hookName + md5;
        try {
            byte[] hash = MessageDigest.getInstance(MD5_SECURITY_ALGORITHM).digest(idString.getBytes());
            NGuid guid = new NGuid(hash, true);
            return guid;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // never happens, MD5 algorithm exists
        }
    }
}
