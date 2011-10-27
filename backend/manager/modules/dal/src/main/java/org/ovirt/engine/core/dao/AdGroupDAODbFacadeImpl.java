package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.AdRefStatus;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>AdGrupDAODbFacadeImpl</code> provides a concrete implementation of {@link AdGroupDAO} based on code from
 * {@link DbFacade}.
 *
 *
 */
public class AdGroupDAODbFacadeImpl extends BaseDAODbFacade implements AdGroupDAO {

    private static final class ADGroupRowMapper implements ParameterizedRowMapper<ad_groups> {
        @Override
        public ad_groups mapRow(ResultSet rs, int rowNum) throws SQLException {
            ad_groups entity = new ad_groups();
            entity.setid(Guid.createGuidFromString(rs.getString("id")));
            entity.setname(rs.getString("name"));
            entity.setstatus(AdRefStatus.forValue(rs.getInt("status")));
            entity.setdomain(rs.getString("domain"));
            entity.setDistinguishedName(rs.getString("distinguishedname"));

            return entity;
        }
    }

    @Override
    public ad_groups get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<ad_groups> mapper = new ADGroupRowMapper();
        return getCallsHandler().executeRead("Getad_groupsByid", mapper, parameterSource);
    }

    @Override
    public ad_groups getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name);

        ParameterizedRowMapper<ad_groups> mapper = new ADGroupRowMapper();
        return getCallsHandler().executeRead("Getad_groupsByName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ad_groups> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<ad_groups> mapper = new ADGroupRowMapper();

        return getCallsHandler().executeReadList("GetAllFromad_groups", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ad_groups> getAllTimeLeasedForPool(int id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vmPoolId", id);

        ParameterizedRowMapper<ad_groups> mapper = new ADGroupRowMapper();

        return getCallsHandler().executeReadList("Gettime_leasedad_groups_by_vm_pool_id", mapper, parameterSource);
    }

    @Override
    public void save(ad_groups group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", group.getid())
                .addValue("name", group.getname())
                .addValue("status", group.getstatus())
                .addValue("domain", group.getdomain())
                .addValue("distinguishedname", group.getDistinguishedName());

        getCallsHandler().executeModification("Insertad_groups", parameterSource);
    }

    @Override
    public void update(ad_groups group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", group.getid())
                .addValue("name", group.getname())
                .addValue("status", group.getstatus())
                .addValue("domain", group.getdomain())
                .addValue("distinguishedname", group.getDistinguishedName());

        getCallsHandler().executeModification("Updatead_groups", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletead_groups", parameterSource);
    }
}
