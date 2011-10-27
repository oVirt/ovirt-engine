package org.ovirt.engine.core.dao.dialect;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQLDialect;

public class ENGINEPostgreSQLDialect extends PostgreSQLDialect {
    public ENGINEPostgreSQLDialect() {
        super();

        this.registerColumnType(Types.OTHER, "uuid");
    }
}
