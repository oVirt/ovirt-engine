package org.ovirt.engine.core.dao.dialect;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;

public class EnginePostgreSQLDialect extends PostgreSQLDialect {
    public EnginePostgreSQLDialect() {
        super();

        this.registerColumnType(Types.OTHER, "uuid");
    }
}
