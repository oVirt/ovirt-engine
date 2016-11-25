package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmNicFilterParameterDaoImpl extends DefaultGenericDao<VmNicFilterParameter, Guid> implements
        VmNicFilterParameterDao {

    public VmNicFilterParameterDaoImpl() {
        super("VmInterfaceFilterParameter");
    }

    @Override
    public List<VmNicFilterParameter> getAllForVmNic(Guid vmInterfaceId) {
        return getAllForVmNic(vmInterfaceId, null, false);
    }

    @Override
    public List<VmNicFilterParameter> getAllForVmNic(Guid vmInterfaceId, Guid userId, boolean filtered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_interface_id", vmInterfaceId)
                .addValue("user_id", userId)
                .addValue("is_filtered", filtered);

        List<VmNicFilterParameter> results =
                getCallsHandler().executeReadList("GetVmInterfaceFilterParametersByVmInterfaceId",
                        VmNicFilterParameterRowMapper.INSTANCE,
                        parameterSource);
        return results;
    }

    @Override
    public VmNicFilterParameter get(Guid id, Guid userId, boolean filtered) {
        return getCallsHandler().executeRead(getProcedureNameForGet(),
                VmNicFilterParameterRowMapper.INSTANCE,
                createIdParameterMapper(id).addValue("user_id", userId).addValue("is_filtered", filtered));
    }

    @Override
    public List<VmNicFilterParameter> getAll(Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList(getProcedureNameForGetAll(),
                VmNicFilterParameterRowMapper.INSTANCE,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("is_filtered", filtered));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    public RowMapper<VmNicFilterParameter> createEntityRowMapper() {
        return VmNicFilterParameterRowMapper.INSTANCE;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmNicFilterParameter obj) {
        return createIdParameterMapper(
                obj.getId())
                .addValue("vm_interface_id", obj.getVmInterfaceId())
                .addValue("name", obj.getName())
                .addValue("value", obj.getValue());
    }

    public static class VmNicFilterParameterRowMapper implements RowMapper<VmNicFilterParameter>  {

        private static final VmNicFilterParameterRowMapper INSTANCE = new VmNicFilterParameterRowMapper();

        @Override
        public VmNicFilterParameter mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmNicFilterParameter entity = new VmNicFilterParameter();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setVmInterfaceId(getGuidDefaultEmpty(rs, "vm_interface_id"));
            entity.setName(rs.getString("name"));
            entity.setValue(rs.getString("value"));
            return entity;
        }
    }

}
