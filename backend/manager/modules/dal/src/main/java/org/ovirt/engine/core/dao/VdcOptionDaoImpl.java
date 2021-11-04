package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VdcOptionDaoImpl} provides a concrete implementation of {@link VdcOptionDao}.
 */
@Named
@Singleton
public class VdcOptionDaoImpl extends BaseDao implements VdcOptionDao {

    private static final RowMapper<VdcOption> vdcOptionRowMapper = (rs, rowNum) -> {
        VdcOption entity = new VdcOption();
        entity.setOptionName(rs.getString("option_name"));
        entity.setOptionValue(rs.getString("option_value"));
        entity.setOptionDefaultValue(rs.getString("default_value"));
        entity.setOptionId(rs.getInt("option_id"));
        entity.setVersion(rs.getString("version"));
        return entity;
    };

    @Override
    public VdcOption get(int id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_id", id);

        return getCallsHandler().executeRead("GetVdcOptionById", vdcOptionRowMapper, parameterSource);
    }

    @Override
    public VdcOption getByNameAndVersion(String name, String version) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", name).addValue("version", version);

        return getCallsHandler().executeRead("GetVdcOptionByName", vdcOptionRowMapper, parameterSource);
    }

    @Override
    public List<VdcOption> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromVdcOption", vdcOptionRowMapper, parameterSource);
    }

    @Override
    public void save(VdcOption option) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", option.getOptionName())
                .addValue("option_value", option.getOptionValue())
                .addValue("default_value", option.getOptionDefaultValue())
                .addValue("version", option.getVersion())
                .addValue("option_id", option.getOptionId());

        getCallsHandler().executeModification("InsertVdcOption", parameterSource);
    }

    @Override
    public void update(VdcOption option) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", option.getOptionName())
                .addValue("option_value", option.getOptionValue())
                .addValue("option_id", option.getOptionId())
                .addValue("version", option.getVersion());

        getCallsHandler().executeModification("UpdateVdcOption", parameterSource);
    }

    @Override
    public void remove(int id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_id", id);

        getCallsHandler().executeModification("DeleteVdcOption", parameterSource);
    }
}
