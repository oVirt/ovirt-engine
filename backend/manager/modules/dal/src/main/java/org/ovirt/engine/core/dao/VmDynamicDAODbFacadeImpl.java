package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class VmDynamicDAODbFacadeImpl extends MassOperationsGenericDaoDbFacade<VmDynamic, Guid>
        implements VmDynamicDAO {

    public VmDynamicDAODbFacadeImpl() {
        super("VmDynamic");
        setProcedureNameForGet("GetVmDynamicByVmGuid");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VmDynamic> getAllRunningForVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        ParameterizedRowMapper<VmDynamic> mapper = createEntityRowMapper();

        return getCallsHandler().executeReadList("GetVmsDynamicRunningOnVds", mapper, parameterSource);
    }

    @Override
    public void updateStatus(Guid vmGuid, VMStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vmGuid)
                .addValue("status", status);

        getCallsHandler().executeModification("UpdateVmDynamicStatus", parameterSource);
    }

    public boolean updateConsoleUserWithOptimisticLocking(VmDynamic vm) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vm.getId())
                .addValue("console_user_id", vm.getConsoleUserId())
                .addValue("guest_cur_user_name", vm.getguest_cur_user_name());

        Map<String, Object> results = getCallsHandler().executeModification("UpdateConsoleUserWithOptimisticLocking", parameterSource);

        return (Boolean) results.get("updated");
    }

    @Override
    public List<VmDynamic> getAll() {
        throw new NotImplementedException();
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("vm_guid", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmDynamic vm) {
        return createIdParameterMapper(vm.getId())
                .addValue("app_list", vm.getapp_list())
                .addValue("guest_cur_user_id", vm.getguest_cur_user_id())
                .addValue("guest_cur_user_name", vm.getguest_cur_user_name())
                .addValue("console_user_id", vm.getConsoleUserId())
                .addValue("guest_last_login_time",
                        vm.getguest_last_login_time())
                .addValue("guest_last_logout_time",
                        vm.getguest_last_logout_time())
                .addValue("guest_os", vm.getguest_os())
                .addValue("migrating_to_vds", vm.getmigrating_to_vds())
                .addValue("run_on_vds", vm.getrun_on_vds())
                .addValue("status", vm.getstatus())
                .addValue("vm_host", vm.getvm_host())
                .addValue("vm_ip", vm.getvm_ip())
                .addValue("last_start_time", vm.getLastStartTime())
                .addValue("vm_pid", vm.getvm_pid())
                .addValue("display", vm.getdisplay())
                .addValue("acpi_enable", vm.getacpi_enable())
                .addValue("session", vm.getsession())
                .addValue("display_ip", vm.getdisplay_ip())
                .addValue("display_type", vm.getdisplay_type())
                .addValue("kvm_enable", vm.getkvm_enable())
                .addValue("boot_sequence", vm.getboot_sequence())
                .addValue("display_secure_port", vm.getdisplay_secure_port())
                .addValue("utc_diff", vm.getutc_diff())
                .addValue("last_vds_run_on", vm.getlast_vds_run_on())
                .addValue("client_ip", vm.getclient_ip())
                .addValue("guest_requested_memory",
                        vm.getguest_requested_memory())
                .addValue("hibernation_vol_handle",
                        vm.gethibernation_vol_handle())
                .addValue("exit_status", vm.getExitStatus().getValue())
                .addValue("pause_status", vm.getPauseStatus().getValue())
                .addValue("exit_message", vm.getExitMessage())
                .addValue("hash", vm.getHash())
                .addValue("guest_agent_nics_hash", vm.getGuestAgentNicsHash());
    }

    @Override
    protected ParameterizedRowMapper<VmDynamic> createEntityRowMapper() {
        return new ParameterizedRowMapper<VmDynamic>() {
            @Override
            public VmDynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VmDynamic entity = new VmDynamic();
                entity.setapp_list(rs.getString("app_list"));
                entity.setguest_cur_user_id(NGuid.createGuidFromString(rs
                        .getString("guest_cur_user_id")));
                entity.setguest_cur_user_name(rs
                        .getString("guest_cur_user_name"));
                entity.setConsoleUserId(NGuid.createGuidFromString(rs.getString("console_user_id")));
                entity.setguest_last_login_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("guest_last_login_time")));
                entity.setguest_last_logout_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("guest_last_logout_time")));
                entity.setguest_os(rs.getString("guest_os"));
                entity.setmigrating_to_vds(NGuid.createGuidFromString(rs
                        .getString("migrating_to_vds")));
                entity.setrun_on_vds(NGuid.createGuidFromString(rs
                        .getString("run_on_vds")));
                entity.setstatus(VMStatus.forValue(rs.getInt("status")));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("vm_guid")));
                entity.setvm_host(rs.getString("vm_host"));
                entity.setvm_ip(rs.getString("vm_ip"));
                entity.setLastStartTime(DbFacadeUtils.fromDate(rs
                        .getTimestamp("last_start_time")));
                entity.setvm_pid((Integer) rs.getObject("vm_pid"));
                entity.setdisplay((Integer) rs.getObject("display"));
                entity.setacpi_enable((Boolean) rs.getObject("acpi_enable"));
                entity.setsession(SessionState.forValue(rs.getInt("session")));
                entity.setdisplay_ip(rs.getString("display_ip"));
                entity.setdisplay_type(DisplayType.forValue(rs
                        .getInt("display_type")));
                entity.setkvm_enable((Boolean) rs.getObject("kvm_enable"));
                entity.setboot_sequence(BootSequence.forValue(rs
                        .getInt("boot_sequence")));
                entity.setdisplay_secure_port((Integer) rs
                        .getObject("display_secure_port"));
                entity.setutc_diff((Integer) rs.getObject("utc_diff"));
                entity.setlast_vds_run_on(NGuid.createGuidFromString(rs
                        .getString("last_vds_run_on")));
                entity.setclient_ip(rs.getString("client_ip"));
                entity.setguest_requested_memory((Integer) rs
                        .getObject("guest_requested_memory"));
                entity.sethibernation_vol_handle(rs
                        .getString("hibernation_vol_handle"));
                VmExitStatus exitStatus = VmExitStatus.forValue(rs
                        .getInt("exit_status"));
                VmPauseStatus pauseStatus = VmPauseStatus.forValue(rs
                        .getInt("pause_status"));
                entity.setExitMessage(rs.getString("exit_message"));
                entity.setExitStatus(exitStatus);
                entity.setPauseStatus(pauseStatus);
                entity.setHash(rs.getString("hash"));
                entity.setGuestAgentNicsHash(rs.getInt("guest_agent_nics_hash"));
                return entity;
            }
        };
    }
}
