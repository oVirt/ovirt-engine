package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>ImageVmMapDAODbFacadeImpl</code> provides an implementation of {@link ImageVmMapDAO} that uses previously
 * developed code from DbFacade.
 */
public class ImageVmMapDAODbFacadeImpl extends BaseDAODbFacade implements ImageVmMapDAO {

    @Override
    public image_vm_map get(image_vm_map_id id) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("image_id", id.getImageId()).addValue("vm_id", id.getVmId());

        ParameterizedRowMapper<image_vm_map> mapper = new ParameterizedRowMapper<image_vm_map>() {
            @Override
            public image_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                image_vm_map entity = new image_vm_map();
                entity.setactive((Boolean) rs.getObject("active"));
                entity.setimage_id(Guid.createGuidFromString(rs.getString("image_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getimage_vm_mapByimage_idAndByvm_id", mapper, parameterSource);
    }

    @Override
    public List<image_vm_map> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public image_vm_map getByImageId(Guid imageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_id", imageId);

        ParameterizedRowMapper<image_vm_map> mapper = new ParameterizedRowMapper<image_vm_map>() {
            @Override
            public image_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                image_vm_map entity = new image_vm_map();
                entity.setactive((Boolean) rs.getObject("active"));
                entity.setimage_id(Guid.createGuidFromString(rs.getString("image_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getimage_vm_mapByimage_id", mapper, parameterSource);
    }

    @Override
    public void save(image_vm_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("active", map.getactive())
                .addValue("image_id", map.getimage_id()).addValue("vm_id", map.getvm_id());

        getCallsHandler().executeModification("Insertimage_vm_map", parameterSource);
    }

    @Override
    public void update(image_vm_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("active", map.getactive())
                .addValue("image_id", map.getimage_id()).addValue("vm_id", map.getvm_id());

        getCallsHandler().executeModification("Updateimage_vm_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<image_vm_map> getByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmId);

        ParameterizedRowMapper<image_vm_map> mapper = new ParameterizedRowMapper<image_vm_map>() {
            @Override
            public image_vm_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                image_vm_map entity = new image_vm_map();
                entity.setactive((Boolean) rs.getObject("active"));
                entity.setimage_id(Guid.createGuidFromString(rs.getString("image_id")));
                entity.setvm_id(Guid.createGuidFromString(rs.getString("vm_id")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getimage_vm_mapByvm_id", mapper, parameterSource);
    }

    @Override
    public void remove(image_vm_map_id id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_id", id.getImageId())
                .addValue("vm_id", id.getVmId());

        getCallsHandler().executeModification("Deleteimage_vm_map", parameterSource);
    }
}
