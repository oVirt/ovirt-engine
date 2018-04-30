package org.ovirt.engine.core.dao.dwh;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class OsInfoDaoImplTest extends BaseDaoTestCase<OsInfoDao> {

    private static final String OS_NAME = "test os name";
    private static final int OS_ID = 666;

    @Inject
    private JdbcTemplate jdbcTemplate;
    @Inject
    private SimpleJdbcCallsHandler simpleJdbcCallsHandler;
    @Inject
    private Provider<CustomMapSqlParameterSource> sqlParameterSourceProvider;

    @Test
    public void testPopulateDwhOsInfoEmptyTable() {
        emptyTable();

        final Map<Integer, String> expected = Collections.singletonMap(OS_ID, OS_NAME);
        dao.populateDwhOsInfo(expected);

        assertResult();
    }

    @Test
    public void testPopulateDwhOsInfoWithOldValues() {
        insertOldValues();

        final Map<Integer, String> expected = Collections.singletonMap(OS_ID, OS_NAME);
        dao.populateDwhOsInfo(expected);

        assertResult();
    }

    private void assertResult() {
        final Map<Integer, String> actual = loadTableData();

        assertThat(actual.keySet(), hasSize(1));
        assertThat(actual, hasEntry(OS_ID, OS_NAME));
    }

    private void emptyTable() {
        jdbcTemplate.execute("select clear_osinfo()");
    }

    private Map<Integer, String> loadTableData() {
        final Map<Integer, String> result = new HashMap<>();
        jdbcTemplate.query("SELECT * FROM dwh_osinfo",
                rs -> {
                    final int osId = rs.getInt("os_id");
                    result.put(osId, rs.getString("os_name"));
                });
        return result;
    }

    private void insertOldValues() {
        simpleJdbcCallsHandler.executeModification("insert_osinfo", prepareOldValues());
    }

    private MapSqlParameterSource prepareOldValues() {
        final MapSqlParameterSource mapSqlParameterSource = sqlParameterSourceProvider.get();
        mapSqlParameterSource.addValue("os_id", ~OS_ID);
        mapSqlParameterSource.addValue("os_name", "old " + OS_NAME);
        return mapSqlParameterSource;
    }

}
