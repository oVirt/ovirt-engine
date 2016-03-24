package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>VdsSpmIdMapDaoImpl</code> provides an implementation of {@link VdsSpmIdMapDao} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@Named
@Singleton
public class VdsSpmIdMapDaoImpl extends BaseDao implements VdsSpmIdMapDao{


    @Override
    public VdsSpmIdMap get(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId);
        return getCallsHandler().executeRead("Getvds_spm_id_mapByvds_id",
                VdsSpmIdMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(VdsSpmIdMap vdsSpmIdMap) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                vdsSpmIdMap.getStoragePoolId()).addValue("vds_id", vdsSpmIdMap.getId()).addValue(
                "vds_spm_id", vdsSpmIdMap.getVdsSpmId());

        getCallsHandler().executeModification("Insertvds_spm_id_map", parameterSource);
    }

    @Override
    public void remove(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId);

        getCallsHandler().executeModification("Deletevds_spm_id_map", parameterSource);
    }

    @Override
    public List<VdsSpmIdMap> getAll(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                storagePoolId);
        return getCallsHandler().executeReadList("Getvds_spm_id_mapBystorage_pool_id",
                VdsSpmIdMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public void removeByVdsAndStoragePool(Guid vdsId, Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId).addValue("storage_pool_id",
                storagePoolId);

        getCallsHandler().executeModification("DeleteByPoolvds_spm_id_map", parameterSource);
    }

    @Override
    public VdsSpmIdMap get(Guid storagePoolId, int spmId ) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                storagePoolId).addValue("vds_spm_id", spmId);
        return getCallsHandler().executeRead("Getvds_spm_id_mapBystorage_pool_idAndByvds_spm_id",
                VdsSpmIdMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<VdsSpmIdMap> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(VdsSpmIdMap entity) {
        throw new UnsupportedOperationException();
    }

    private static final class VdsSpmIdMapRowMapper implements RowMapper<VdsSpmIdMap> {
        public static final VdsSpmIdMapRowMapper instance = new VdsSpmIdMapRowMapper();

        @Override
        public VdsSpmIdMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            VdsSpmIdMap entity = new VdsSpmIdMap();
            entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setVdsSpmId(rs.getInt("vds_spm_id"));
            return entity;
        }
    }
}
