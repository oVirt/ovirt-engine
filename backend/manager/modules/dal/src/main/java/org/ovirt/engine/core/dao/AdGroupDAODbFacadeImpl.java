package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapRefStatus;
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

    @Override
    public LdapGroup get(Guid id) {
        return getCallsHandler().executeRead("Getad_groupsByid",
                new ADGroupRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("id", id));
    }

    @Override
    public LdapGroup getByName(String name) {
        return getCallsHandler().executeRead("Getad_groupsByName",
                new ADGroupRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("name", name));
    }

    @Override
    public List<LdapGroup> getAll() {
        return getCallsHandler().executeReadList("GetAllFromad_groups",
                new ADGroupRowMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<LdapGroup> getAllTimeLeasedForPool(int id) {
        return getCallsHandler().executeReadList("Gettime_leasedad_groups_by_vm_pool_id",
                new ADGroupRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vmPoolId", id));
    }

    @Override
    public void save(LdapGroup group) {
        insertOrUpdate(group, "Insertad_groups");
    }

    @Override
    public void update(LdapGroup group) {
        insertOrUpdate(group, "Updatead_groups");
    }

    private void insertOrUpdate(final LdapGroup group, final String storedProcName) {
        getCallsHandler().executeModification(storedProcName, getCustomMapSqlParameterSource()
                .addValue("id", group.getid())
                .addValue("name", group.getname())
                .addValue("status", group.getstatus())
                .addValue("domain", group.getdomain())
                .addValue("distinguishedname", group.getDistinguishedName()));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletead_groups", parameterSource);
    }

    private static final class ADGroupRowMapper implements ParameterizedRowMapper<LdapGroup> {
        @Override
        public LdapGroup mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            LdapGroup entity = new LdapGroup();
            entity.setid(Guid.createGuidFromString(rs.getString("id")));
            entity.setname(rs.getString("name"));
            entity.setstatus(LdapRefStatus.forValue(rs.getInt("status")));
            entity.setdomain(rs.getString("domain"));
            entity.setDistinguishedName(rs.getString("distinguishedname"));

            return entity;
        }
    }

}
