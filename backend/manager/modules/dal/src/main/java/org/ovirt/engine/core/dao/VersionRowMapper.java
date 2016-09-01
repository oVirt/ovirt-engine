package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.ovirt.engine.core.compat.Version;
import org.springframework.jdbc.core.RowMapper;

public class VersionRowMapper implements RowMapper<Version> {
    private final String versionColumnName;

    public VersionRowMapper(String versionColumnName) {
        this.versionColumnName = versionColumnName;
    }

    @Override
    public Version mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Optional.ofNullable(rs.getString(versionColumnName)).map(Version::new).orElse(null);
    }

}
