package org.ovirt.engine.core.dao.dwh;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.SimpleJdbcCallsHandler;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Named
@Singleton
class OsInfoDaoImpl implements OsInfoDao {

    private final SimpleJdbcCallsHandler callsHandler;
    private final Provider<CustomMapSqlParameterSource> sqlParameterSourceProvider;

    private final SimpleJdbcCall simpleJdbcCall;

    @Inject
    OsInfoDaoImpl(JdbcTemplate jdbcTemplate,
            SimpleJdbcCallsHandler callsHandler,
            Provider<CustomMapSqlParameterSource> sqlParameterSourceProvider) {

        Objects.requireNonNull(callsHandler);
        Objects.requireNonNull(sqlParameterSourceProvider);

        this.callsHandler = callsHandler;
        this.sqlParameterSourceProvider = sqlParameterSourceProvider;

        simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate);
    }

    /**
     * This call will populate a translation table of OS Ids to they're name.<br/>
     * The translation table shall be in use by DWH.
     *
     * @param osIdToName
     *            OS id to OS Name map
     */
    @Override
    public void populateDwhOsInfo(Map<Integer, String> osIdToName) {
        // first clear the table
        simpleJdbcCall.withProcedureName("clear_osinfo").execute();
        // batch populate
        List<MapSqlParameterSource> executions =
                osIdToName.entrySet()
                        .stream()
                        .map(e -> sqlParameterSourceProvider.get()
                                .addValue("os_id", e.getKey())
                                .addValue("os_name", e.getValue()))
                        .collect(Collectors.toList());
        callsHandler.executeStoredProcAsBatch("insert_osinfo", executions);
    }

}
