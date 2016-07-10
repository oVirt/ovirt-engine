package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.GuestAgentStatus;
import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

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
                createBooleanMapper(),
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateGuestAgentStatus(Guid vmId, GuestAgentStatus guestAgentStatus) {
        getCallsHandler().executeModification("UpdateGuestAgentStatus",
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("guest_agent_status", guestAgentStatus.getValue()));
    }

    @Override
    public void updateVmsToUnknown(List<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", createArrayOfUUIDs(vmIds))
                .addValue("status", VMStatus.Unknown);

        getCallsHandler().executeModification("SetToUnknown", parameterSource);
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
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmDynamic vm) {
        GraphicsInfo spice = vm.getGraphicsInfos().get(GraphicsType.SPICE);
        GraphicsInfo vnc = vm.getGraphicsInfos().get(GraphicsType.VNC);
        return createIdParameterMapper(vm.getId())
                .addValue("app_list", vm.getAppList())
                .addValue("guest_cur_user_name", vm.getGuestCurrentUserName())
                .addValue("console_cur_user_name", vm.getConsoleCurrentUserName())
                .addValue("console_user_id", vm.getConsoleUserId())
                .addValue("guest_os", vm.getGuestOs())
                .addValue("migrating_to_vds", vm.getMigratingToVds())
                .addValue("run_on_vds", vm.getRunOnVds())
                .addValue("status", vm.getStatus())
                .addValue("vm_host", vm.getVmHost())
                .addValue("vm_ip", vm.getVmIp())
                .addValue("vm_fqdn", vm.getVmFQDN())
                .addValue("last_start_time", vm.getLastStartTime())
                .addValue("last_stop_time", vm.getLastStopTime())
                .addValue("vm_pid", vm.getVmPid())
                .addValue("acpi_enable", vm.getAcpiEnable())
                .addValue("session", vm.getSession())
                .addValue("kvm_enable", vm.getKvmEnable())
                .addValue("boot_sequence", vm.getBootSequence())
                .addValue("utc_diff", vm.getUtcDiff())
                .addValue("last_vds_run_on", vm.getLastVdsRunOn())
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
                .addValue("cpu_name", vm.getCpuName())
                .addValue("guest_agent_status", vm.getGuestAgentStatus().getValue())
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
                .addValue("guest_mem_buffered", vm.getGuestMemoryBuffered())
                .addValue("guest_mem_cached", vm.getGuestMemoryCached())
                .addValue("guest_mem_free", vm.getGuestMemoryFree())
                .addValue("guest_timezone_name", vm.getGuestOsTimezoneName())
                .addValue("guest_timezone_offset", vm.getGuestOsTimezoneOffset())
                .addValue("guestos_arch", vm.getGuestOsArch().getValue())
                .addValue("guestos_codename", vm.getGuestOsCodename())
                .addValue("guestos_distribution", vm.getGuestOsDistribution())
                .addValue("guestos_kernel_version", vm.getGuestOsKernelVersion())
                .addValue("guestos_type", vm.getGuestOsType().name())
                .addValue("guestos_version", vm.getGuestOsVersion())
                .addValue("guest_containers", toGuestContainersString(vm));
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
        return VmDynamicRowMapper.instance;
    }

    private static class VmDynamicRowMapper implements RowMapper<VmDynamic> {
        public static final VmDynamicRowMapper instance = new VmDynamicRowMapper();

        @Override
        public VmDynamic mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmDynamic entity = new VmDynamic();
            entity.setAppList(rs.getString("app_list"));
            entity.setGuestCurrentUserName(rs.getString("guest_cur_user_name"));
            entity.setConsoleCurrentUserName(rs.getString("console_cur_user_name"));
            entity.setConsoleUserId(getGuid(rs, "console_user_id"));
            entity.setGuestOs(rs.getString("guest_os"));
            entity.setMigratingToVds(getGuid(rs, "migrating_to_vds"));
            entity.setRunOnVds(getGuid(rs, "run_on_vds"));
            entity.setStatus(VMStatus.forValue(rs.getInt("status")));
            entity.setId(getGuidDefaultEmpty(rs, "vm_guid"));
            entity.setVmHost(rs.getString("vm_host"));
            entity.setVmIp(rs.getString("vm_ip"));
            entity.setVmFQDN(rs.getString("vm_fqdn"));
            entity.setLastStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_start_time")));
            entity.setLastStopTime(DbFacadeUtils.fromDate(rs.getTimestamp("last_stop_time")));
            entity.setVmPid((Integer) rs.getObject("vm_pid"));
            entity.setAcpiEnable((Boolean) rs.getObject("acpi_enable"));
            entity.setSession(SessionState.forValue(rs.getInt("session")));
            entity.setKvmEnable((Boolean) rs.getObject("kvm_enable"));
            entity.setBootSequence(BootSequence.forValue(rs.getInt("boot_sequence")));
            entity.setUtcDiff((Integer) rs.getObject("utc_diff"));
            entity.setLastVdsRunOn(getGuid(rs, "last_vds_run_on"));
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
            entity.setCpuName(rs.getString("cpu_name"));
            entity.setGuestAgentStatus(GuestAgentStatus.forValue(rs.getInt("guest_agent_status")));
            entity.setCurrentCd(rs.getString("current_cd"));
            entity.setStopReason(rs.getString("reason"));
            VmExitReason exitReason = VmExitReason.forValue(rs.getInt("exit_reason"));
            entity.setExitReason(exitReason);
            entity.setGuestCpuCount(rs.getInt("guest_cpu_count"));
            entity.setEmulatedMachine(rs.getString("emulated_machine"));
            setGraphicsToEntity(rs, entity);
            entity.setGuestMemoryBuffered(getLong(rs, "guest_mem_buffered"));
            entity.setGuestMemoryCached(getLong(rs, "guest_mem_cached"));
            entity.setGuestMemoryFree(getLong(rs, "guest_mem_free"));
            entity.setGuestOsTimezoneOffset(rs.getInt("guest_timezone_offset"));
            entity.setGuestOsTimezoneName(rs.getString("guest_timezone_name"));
            entity.setGuestOsArch(rs.getInt("guestos_arch"));
            entity.setGuestOsCodename(rs.getString("guestos_codename"));
            entity.setGuestOsDistribution(rs.getString("guestos_distribution"));
            entity.setGuestOsKernelVersion(rs.getString("guestos_kernel_version"));
            entity.setGuestOsType(rs.getString("guestos_type"));
            entity.setGuestOsVersion(rs.getString("guestos_version"));
            entity.setGuestContainers(fromContainersString(rs.getString("guest_containers")));
            return entity;
        }
    }

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

    protected static RowMapper<VmDynamic> getRowMapper() {
        return VmDynamicRowMapper.instance;
    }
}
