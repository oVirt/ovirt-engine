package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

@Named
@Singleton
public class VmAndTemplatesGenerationsImpl extends BaseDao implements VmAndTemplatesGenerationsDao {
    private static final String ovfSeparator = "<<<ENDOVF>>>";

    @Override
    public void updateOvfGenerations(List<Guid> ids, List<Long> values, List<String> ovfData) {
        getCallsHandler().executeModification("UpdateOvfGenerations", getCustomMapSqlParameterSource()
                .addValue("vms_ids", StringUtils.join(ids, ','))
                .addValue("vms_db_generations", StringUtils.join(values, ','))
                .addValue("ovf_data", StringUtils.join(ovfData, ovfSeparator))
                .addValue("ovf_data_seperator", ovfSeparator));
    }

    @Override
    public Long getOvfGeneration(Guid id) {
        return getCallsHandler().executeRead("GetOvfGeneration",
                SingleColumnRowMapper.newInstance(Long.class), getCustomMapSqlParameterSource().addValue("vm_id", id));
    }

    @Override
    public void deleteOvfGenerations(List<Guid> ids) {
        getCallsHandler().executeModification("DeleteOvfGenerations", getCustomMapSqlParameterSource()
                .addValue("vms_ids", StringUtils.join(ids, ',')));
    }

    @Override
    public List<Guid> getVmsIdsForOvfUpdate(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetVmsIdsForOvfUpdate",
                createGuidMapper(),
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<Guid> getIdsForOvfDeletion(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetIdsForOvfDeletion",
                createGuidMapper(),
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public List<Guid> getVmTemplatesIdsForOvfUpdate(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetVmTemplatesIdsForOvfUpdate",
                createGuidMapper(),
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }

    private static final RowMapper<Pair<Guid, String>> ovfDataRowMapper =
            (resultSet, i) -> new Pair<>(getGuid(resultSet, "vm_guid"), resultSet.getString("ovf_data"));

    @Override
    public List<Pair<Guid, String>> loadOvfDataForIds(List<Guid> ids) {
        return getCallsHandler().executeReadList("LoadOvfDataForIds",
                ovfDataRowMapper,
                getCustomMapSqlParameterSource().addValue("ids", StringUtils.join(ids, ',')));
    }
}
