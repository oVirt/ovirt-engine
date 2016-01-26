package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmNicDaoImpl extends DefaultGenericDao<VmNic, Guid> implements VmNicDao {

    public VmNicDaoImpl() {
        super("VmInterface");
    }

    @Override
    public List<VmNic> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", id);

        return getCallsHandler().executeReadList("GetVmInterfacesByVmId", VnicRowMapper.INSTANCE, parameterSource);
    }

    @Override
    public List<VmNic> getAllForTemplate(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("template_id", id);

        return getCallsHandler().executeReadList("GetVmInterfaceByTemplateId",
                VnicRowMapper.INSTANCE,
                parameterSource);
    }

    @Override
    public List<VmNic> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmInterfacesByNetworkId",
                VnicRowMapper.INSTANCE, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<VmNic> getAllForTemplatesByNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmTemplateInterfacesByNetworkId",
                VnicRowMapper.INSTANCE, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<String> getAllMacsByDataCenter(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetMacsByDataCenterId",
                macMapper, getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public List<VmNic> getPluggedForMac(String macAddress) {
        return getCallsHandler().executeReadList("GetPluggedVmInterfacesByMac",
                VnicRowMapper.INSTANCE, getCustomMapSqlParameterSource().addValue("mac_address", macAddress));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmNic entity) {
        MapSqlParameterSource parameterSource =
                createIdParameterMapper(entity.getId()).addValue("mac_addr", entity.getMacAddress())
                        .addValue("name", entity.getName())
                        .addValue("speed", entity.getSpeed())
                        .addValue("vm_guid", entity.getVmId())
                        .addValue("vnic_profile_id", entity.getVnicProfileId())
                        .addValue("vmt_guid", entity.getVmTemplateId())
                        .addValue("type", entity.getType())
                        .addValue("linked", entity.isLinked());
        return parameterSource;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<VmNic> createEntityRowMapper() {
        return VnicRowMapper.INSTANCE;
    }

    abstract static class VmNicRowMapperBase<T extends VmNic> implements RowMapper<T> {

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createVmNicEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setVmId(getGuid(rs, "vm_guid"));
            entity.setVmTemplateId(getGuid(rs, "vmt_guid"));
            entity.setType((Integer) rs.getObject("type"));
            entity.setMacAddress(rs.getString("mac_addr"));
            entity.setVnicProfileId(getGuid(rs, "vnic_profile_id"));
            entity.setSpeed((Integer) rs.getObject("speed"));
            entity.setLinked(rs.getBoolean("linked"));
            return entity;
        }

        protected abstract T createVmNicEntity();
    }

    private static class VnicRowMapper extends VmNicRowMapperBase<VmNic> {

        public static VnicRowMapper INSTANCE = new VnicRowMapper();

        @Override
        protected VmNic createVmNicEntity() {
            return new VmNic();
        }

    }

    protected final RowMapper<String> macMapper = new RowMapper<String>() {

        private static final int MAC_COLUMN_POSITION = 1;

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(MAC_COLUMN_POSITION);
        }
    };
}
