package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Named
@Singleton
public class VmDynamicDaoImpl extends MassOperationsGenericDao<VmDynamic, Guid>
        implements VmDynamicDao {

    public VmDynamicDaoImpl() {
        super("VmDynamic");
        setProcedureNameForGet("GetVmDynamicByVmGuid");
    }

    @Override
    public List<VmDynamic> getAllRunningForVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        RowMapper<VmDynamic> mapper = createEntityRowMapper();

        return getCallsHandler().executeReadList("GetVmsDynamicRunningOnVds", mapper, parameterSource);
    }

    @Override
    public boolean isAnyVmRunOnVds(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", vdsId);

        return getCallsHandler().executeRead("IsAnyVmRunOnVds",
                SingleColumnRowMapper.newInstance(Boolean.class),
                parameterSource);
    }

    @Override
    public void updateStatus(Guid vmGuid, VMStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vmGuid)
                .addValue("status", status);

        getCallsHandler().executeModification("UpdateVmDynamicStatus", parameterSource);
    }

    @Override
    public void clearMigratingToVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        getCallsHandler().executeModification("ClearMigratingToVds", parameterSource);
    }

    @Override
    public boolean updateConsoleUserWithOptimisticLocking(VmDynamic vm) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vm.getId())
                .addValue("console_user_id", vm.getConsoleUserId())
                .addValue("guest_cur_user_name", vm.getGuestCurrentUserName())
                .addValue("console_cur_user_name", vm.getConsoleCurrentUserName());

        Map<String, Object> results = getCallsHandler().executeModification("UpdateConsoleUserWithOptimisticLocking", parameterSource);

        return (Boolean) results.get("updated");
    }

    @Override
    public List<VmDynamic> getAll() {
        return getCallsHandler().executeReadList("GetAllFromVmDynamic",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public void updateOvirtGuestAgentStatus(Guid vmId, GuestAgentStatus ovirtGuestAgentStatus) {
        getCallsHandler().executeModification("UpdateOvirtGuestAgentStatus",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("ovirt_guest_agent_status", ovirtGuestAgentStatus.getValue()));
    }

    @Override
    public void updateQemuGuestAgentStatus(Guid vmId, GuestAgentStatus qemuGuestAgentStatus) {
        getCallsHandler().executeModification("UpdateQemuGuestAgentStatus",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("qemu_guest_agent_status", qemuGuestAgentStatus.getValue()));
    }

    @Override
    public void updateVmsToUnknown(List<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", createArrayOfUUIDs(vmIds))
                .addValue("status", VMStatus.Unknown);

        getCallsHandler().executeModification("SetToUnknown", parameterSource);
    }

    @Override
    public List<VmDynamic> getAllMigratingToHost(Guid vdsId) {
        return getCallsHandler().executeReadList("GetVmsMigratingToVds",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsId));
    }

    @Override
    public List<Pair<Guid, String>> getAllDevicesHashes() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllHashesFromVmDynamic",
                (rs, i) -> new Pair<>(new Guid(rs.getString("vm_guid")), rs.getString("hash")),
                parameterSource);
    }

    @Override
    public void updateDevicesHashes(List<Pair<Guid, String>> vmHashes) {
        getCallsHandler().executeStoredProcAsBatch("SetHashByVmGuid",
                vmHashes,
                pair -> getCustomMapSqlParameterSource()
                        .addValue("vm_guid", pair.getFirst())
                        .addValue("hash", pair.getSecond()));
    }

    @Override
    public void updateVmLeaseInfo(Guid vmId, Map<String, String> leaseInfo) {
        getCallsHandler().executeModification("UpdateVmLeaseInfo",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("lease_info", SerializationFactory.getSerializer().serialize(leaseInfo)));
    }

    @Override
    public List<VmDynamic> getAllRunningForUserAndActionGroup(Guid userID, ActionGroup actionGroup) {
        return getCallsHandler().executeReadList("GetAllRunningVmsForUserAndActionGroup",
                VmDynamicDaoImpl.getRowMapper(),
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("action_group_id", actionGroup.getId()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    /**
     * Note: we intentionally don't update lease_info here because it
     * should only be updated using {@link #updateVmLeaseInfo(Guid, Map)}
     */
    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmDynamic vm) {
        GraphicsInfo spice = vm.getGraphicsInfos().get(GraphicsType.SPICE);
        GraphicsInfo vnc = vm.getGraphicsInfos().get(GraphicsType.VNC);
        return createIdParameterMapper(vm.getId())
                .addValue("app_list", vm.getAppList())
                .addValue("guest_cur_user_name", vm.getGuestCurrentUserName())
                .addValue("console_cur_user_name", vm.getConsoleCurrentUserName())
                .addValue("runtime_name", vm.getRuntimeName())
                .addValue("console_user_id", vm.getConsoleUserId())
                .addValue("guest_os", vm.getGuestOs())
                .addValue("migrating_to_vds", vm.getMigratingToVds())
                .addValue("run_on_vds", vm.getRunOnVds())
                .addValue("status", vm.getStatus())
                .addValue("vm_host", vm.getVmHost())
                .addValue("vm_ip", vm.getIp())
                .addValue("vm_fqdn", vm.getFqdn())
                .addValue("last_start_time", vm.getLastStartTime())
                .addValue("boot_time", vm.getBootTime())
                .addValue("downtime", vm.getDowntime())
                .addValue("last_stop_time", vm.getLastStopTime())
                .addValue("acpi_enable", vm.getAcpiEnable())
                .addValue("session", vm.getSession())
                .addValue("boot_sequence", vm.getBootSequence())
                .addValue("utc_diff", vm.getUtcDiff())
                .addValue("client_ip", vm.getClientIp())
                .addValue("guest_requested_memory",
                        vm.getGuestRequestedMemory())
                .addValue("exit_status", vm.getExitStatus().getValue())
                .addValue("pause_status", vm.getPauseStatus().getValue())
                .addValue("exit_message", vm.getExitMessage())
                .addValue("guest_agent_nics_hash", vm.getGuestAgentNicsHash())
                .addValue("last_watchdog_event", vm.getLastWatchdogEvent())
                .addValue("last_watchdog_action", vm.getLastWatchdogAction())
                .addValue("is_run_once", vm.isRunOnce())
                .addValue("volatile_run", vm.isVolatileRun())
                .addValue("cpu_name", vm.getCpuName())
                .addValue("ovirt_guest_agent_status", vm.getOvirtGuestAgentStatus().getValue())
                .addValue("qemu_guest_agent_status", vm.getQemuGuestAgentStatus().getValue())
                .addValue("current_cd", vm.getCurrentCd())
                .addValue("reason", vm.getStopReason())
                .addValue("exit_reason", vm.getExitReason().getValue())
                .addValue("guest_cpu_count", vm.getGuestCpuCount())
                .addValue("emulated_machine", vm.getEmulatedMachine())
                .addValue("spice_port", spice != null ? spice.getPort() : null)
                .addValue("spice_tls_port", spice != null ? spice.getTlsPort() : null)
                .addValue("spice_ip", spice != null ? spice.getIp() : null)
                .addValue("vnc_port", vnc != null ? vnc.getPort() : null)
                .addValue("vnc_ip", vnc != null ? vnc.getIp() : null)
                .addValue("guest_timezone_name", vm.getGuestOsTimezoneName())
                .addValue("guest_timezone_offset", vm.getGuestOsTimezoneOffset())
                .addValue("guestos_arch", vm.getGuestOsArch().getValue())
                .addValue("guestos_codename", vm.getGuestOsCodename())
                .addValue("guestos_distribution", vm.getGuestOsDistribution())
                .addValue("guestos_kernel_version", vm.getGuestOsKernelVersion())
                .addValue("guestos_type", vm.getGuestOsType().name())
                .addValue("guestos_version", vm.getGuestOsVersion())
                .addValue("guest_containers", toGuestContainersString(vm))
                .addValue("current_cpu_pinning", vm.getCurrentCpuPinning())
                .addValue("current_sockets", vm.getCurrentSockets())
                .addValue("current_cores", vm.getCurrentCoresPerSocket())
                .addValue("current_threads", vm.getCurrentThreadsPerCore());
    }

    private static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static String toGuestContainersString(VmDynamic vm) {
        try {
            return JSON_MAPPER.writeValueAsString(vm.getGuestContainers());
        } catch(Exception e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private static List<GuestContainer> fromContainersString(String s) {
        try {
            return (List<GuestContainer>) JSON_MAPPER.readValue(s, new TypeReference<List<GuestContainer>>() {});
        } catch(Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    protected RowMapper<VmDynamic> createEntityRowMapper() {
        return vmDynamicRowMapper;
    }

    private static final RowMapper<VmDynamic> vmDynamicRowMapper = (rs, rowNum) -> {
        VmDynamic entity = new VmDynamic();
        entity.setAppList(rs.getString("app_list"));
        entity.setGuestCurrentUserName(rs.getString("guest_cur_user_name"));
        entity.setConsoleCurrentUserName(rs.getString("console_cur_user_name"));
        entity.setRuntimeName(rs.getString("runtime_name"));
        entity.setConsoleUserId(getGuid(rs, "console_user_id"));
        entity.setGuestOs(rs.getString("guest_os"));
        entity.setMigratingToVds(getGuid(rs, "migrating_to_vds"));
        entity.setRunOnVds(getGuid(rs, "run_on_vds"));
        entity.setStatus(VMStatus.forValue(rs.getInt("status")));
        entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
        entity.setVmHost(rs.getString("vm_host"));
        entity.setIp(rs.getString("vm_ip"));
        entity.setFqdn(rs.getString("vm_fqdn"));
        entity.setLastStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_start_time")));
        entity.setBootTime(DbFacadeUtils.fromDate(rs.getTimestamp("boot_time")));
        entity.setDowntime(getLong(rs, "downtime"));
        entity.setLastStopTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_stop_time")));
        entity.setAcpiEnable((Boolean) rs.getObject("acpi_enable"));
        entity.setSession(SessionState.forValue(rs.getInt("session")));
        entity.setBootSequence(BootSequence.forValue(rs.getInt("boot_sequence")));
        entity.setUtcDiff((Integer) rs.getObject("utc_diff"));
        entity.setClientIp(rs.getString("client_ip"));
        entity.setGuestRequestedMemory((Integer) rs.getObject("guest_requested_memory"));
        VmExitStatus exitStatus = VmExitStatus.forValue(rs.getInt("exit_status"));
        VmPauseStatus pauseStatus = VmPauseStatus.forValue(rs.getInt("pause_status"));
        entity.setExitMessage(rs.getString("exit_message"));
        entity.setExitStatus(exitStatus);
        entity.setPauseStatus(pauseStatus);
        entity.setGuestAgentNicsHash(rs.getInt("guest_agent_nics_hash"));
        entity.setLastWatchdogEvent(getLong(rs, "last_watchdog_event"));
        entity.setLastWatchdogAction(rs.getString("last_watchdog_action"));
        entity.setRunOnce(rs.getBoolean("is_run_once"));
        entity.setVolatileRun(rs.getBoolean("volatile_run"));
        entity.setCpuName(rs.getString("cpu_name"));
        entity.setOvirtGuestAgentStatus(GuestAgentStatus.forValue(rs.getInt("ovirt_guest_agent_status")));
        entity.setQemuGuestAgentStatus(GuestAgentStatus.forValue(rs.getInt("qemu_guest_agent_status")));
        entity.setCurrentCd(rs.getString("current_cd"));
        entity.setStopReason(rs.getString("reason"));
        VmExitReason exitReason = VmExitReason.forValue(rs.getInt("exit_reason"));
        entity.setExitReason(exitReason);
        entity.setGuestCpuCount(rs.getInt("guest_cpu_count"));
        entity.setEmulatedMachine(rs.getString("emulated_machine"));
        setGraphicsToEntity(rs, entity);
        entity.setGuestOsTimezoneOffset(rs.getInt("guest_timezone_offset"));
        entity.setGuestOsTimezoneName(rs.getString("guest_timezone_name"));
        entity.setGuestOsArch(rs.getInt("guestos_arch"));
        entity.setGuestOsCodename(rs.getString("guestos_codename"));
        entity.setGuestOsDistribution(rs.getString("guestos_distribution"));
        entity.setGuestOsKernelVersion(rs.getString("guestos_kernel_version"));
        entity.setGuestOsType(OsType.valueOf(rs.getString("guestos_type")));
        entity.setGuestOsVersion(rs.getString("guestos_version"));
        entity.setGuestContainers(fromContainersString(rs.getString("guest_containers")));
        entity.setLeaseInfo(SerializationFactory.getDeserializer().deserialize(rs.getString("lease_info"), HashMap.class));
        entity.setCurrentCpuPinning(rs.getString("current_cpu_pinning"));
        entity.setCurrentSockets(rs.getInt("current_sockets"));
        entity.setCurrentCoresPerSocket(rs.getInt("current_cores"));
        entity.setCurrentThreadsPerCore(rs.getInt("current_threads"));
        return entity;
    };

    private static void setGraphicsToEntity(ResultSet rs, VmDynamic entity) throws SQLException {
        GraphicsInfo graphicsInfo = new GraphicsInfo();
        graphicsInfo.setIp(rs.getString("spice_ip"));
        graphicsInfo.setPort((Integer) rs.getObject("spice_port"));
        graphicsInfo.setTlsPort((Integer) rs.getObject("spice_tls_port"));

        if (graphicsInfo.getPort() != null || graphicsInfo.getTlsPort() != null) {
            entity.getGraphicsInfos().put(GraphicsType.SPICE, graphicsInfo);
        }

        graphicsInfo = new GraphicsInfo();
        graphicsInfo.setIp(rs.getString("vnc_ip"));
        graphicsInfo.setPort((Integer) rs.getObject("vnc_port"));

        if (graphicsInfo.getPort() != null) {
            entity.getGraphicsInfos().put(GraphicsType.VNC, graphicsInfo);
        }
    }

    @Override
    public List<Guid> getAllIdsWithSpecificIsoAttached(Guid isoDiskId) {
        return getCallsHandler().executeReadList("GetVmIdsWithSpecificIsoAttached",
                SingleColumnRowMapper.newInstance(Guid.class),
                getCustomMapSqlParameterSource().addValue("iso_disk_id", isoDiskId));
    }

    protected static RowMapper<VmDynamic> getRowMapper() {
        return vmDynamicRowMapper;
    }
}
