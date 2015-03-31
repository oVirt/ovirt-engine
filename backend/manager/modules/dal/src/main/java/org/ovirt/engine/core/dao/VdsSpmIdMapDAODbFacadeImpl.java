package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>VdsSpmIdMapDAODbFacadeImpl</code> provides an implementation of {@link VdsSpmIdMapDAO} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@Named
@Singleton
public class VdsSpmIdMapDAODbFacadeImpl extends BaseDAODbFacade implements VdsSpmIdMapDAO{


    @Override
    public vds_spm_id_map get(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId);
        return getCallsHandler().executeRead("Getvds_spm_id_mapByvds_id",
                VdsSpmIdMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(vds_spm_id_map vds_spm_id_map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                vds_spm_id_map.getstorage_pool_id()).addValue("vds_id", vds_spm_id_map.getId()).addValue(
                "vds_spm_id", vds_spm_id_map.getvds_spm_id());

        getCallsHandler().executeModification("Insertvds_spm_id_map", parameterSource);
    }

    @Override
    public void remove(Guid vdsId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vds_id", vdsId);

        getCallsHandler().executeModification("Deletevds_spm_id_map", parameterSource);
    }

    @Override
    public List<vds_spm_id_map> getAll(Guid storagePoolId) {
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
    public vds_spm_id_map get(Guid storagePoolId, int spmId ) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_pool_id",
                storagePoolId).addValue("vds_spm_id", spmId);
        return getCallsHandler().executeRead("Getvds_spm_id_mapBystorage_pool_idAndByvds_spm_id",
                VdsSpmIdMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<vds_spm_id_map> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public void update(vds_spm_id_map entity) {
        throw new NotImplementedException();
    }

    private static final class VdsSpmIdMapRowMapper implements RowMapper<vds_spm_id_map> {
        public static final VdsSpmIdMapRowMapper instance = new VdsSpmIdMapRowMapper();

        @Override
        public vds_spm_id_map mapRow(ResultSet rs, int rowNum) throws SQLException {
            vds_spm_id_map entity = new vds_spm_id_map();
            entity.setstorage_pool_id(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setId(getGuidDefaultEmpty(rs, "vds_id"));
            entity.setvds_spm_id(rs.getInt("vds_spm_id"));
            return entity;
        }
    }
}
