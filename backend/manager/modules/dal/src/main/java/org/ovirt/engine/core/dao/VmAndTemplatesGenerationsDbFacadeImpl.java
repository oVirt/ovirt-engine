package org.ovirt.engine.core.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.compat.Guid;

public class VmAndTemplatesGenerationsDbFacadeImpl extends BaseDAODbFacade implements VmAndTemplatesGenerationsDAO {
    @Override
    public void updateOvfGenerations(List<Guid> ids, List<Long> values) {
        getCallsHandler().executeModification("UpdateOvfGenerations", getCustomMapSqlParameterSource()
                .addValue("vms_ids", StringUtils.join(ids, ','))
                .addValue("vms_db_generations", StringUtils.join(values,',')));
    }

    @Override
    public Long getOvfGeneration(Guid id) {
        return getCallsHandler().executeRead("GetOvfGeneration",
                getLongMapper(), getCustomMapSqlParameterSource()
                        .addValue("vm_id", id));
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
}
