package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.VdcOption;

/**
 * <code>VdcOptionDAODbFacadeImpl</code> provides a concrete implementation of {@link VdcOptionDAO} using code
 * refactored from {@link DbFacade}.
 *
 *
 */
public class VdcOptionDAODbFacadeImpl extends BaseDAODbFacade implements VdcOptionDAO {

    @Override
    public VdcOption get(int id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_id", id);

        ParameterizedRowMapper<VdcOption> mapper = new ParameterizedRowMapper<VdcOption>() {
            @Override
            public VdcOption mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VdcOption entity = new VdcOption();
                entity.setoption_name(rs.getString("option_name"));
                entity.setoption_value(rs.getString("option_value"));
                entity.setoption_id(rs.getInt("option_id"));
                entity.setversion(rs.getString("version"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVdcOptionById", mapper, parameterSource);
    }

    @Override
    public VdcOption getByNameAndVersion(String name, String version) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", name).addValue("version", version);

        ParameterizedRowMapper<VdcOption> mapper = new ParameterizedRowMapper<VdcOption>() {
            @Override
            public VdcOption mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VdcOption entity = new VdcOption();
                entity.setoption_name(rs.getString("option_name"));
                entity.setoption_value(rs.getString("option_value"));
                entity.setoption_id(rs.getInt("option_id"));
                entity.setversion(rs.getString("version"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetVdcOptionByName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VdcOption> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<VdcOption> mapper = new ParameterizedRowMapper<VdcOption>() {
            @Override
            public VdcOption mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VdcOption entity = new VdcOption();
                entity.setoption_name(rs.getString("option_name"));
                entity.setoption_value(rs.getString("option_value"));
                entity.setoption_id(rs.getInt("option_id"));
                entity.setversion(rs.getString("version"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromVdcOption", mapper, parameterSource);
    }

    @Override
    public void save(VdcOption option) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", option.getoption_name())
                .addValue("option_value", option.getoption_value())
                .addValue("version", option.getversion())
                .addValue("option_id", option.getoption_id());

        getCallsHandler().executeModification("InsertVdcOption", parameterSource);
    }

    @Override
    public void update(VdcOption option) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_name", option.getoption_name())
                .addValue("option_value", option.getoption_value())
                .addValue("option_id", option.getoption_id())
                .addValue("version", option.getversion());

        getCallsHandler().executeModification("UpdateVdcOption", parameterSource);
    }

    @Override
    public void remove(int id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("option_id", id);

        getCallsHandler().executeModification("DeleteVdcOption", parameterSource);
    }
}
