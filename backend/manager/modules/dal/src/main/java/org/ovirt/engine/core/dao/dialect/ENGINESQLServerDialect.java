package org.ovirt.engine.core.dao.dialect;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;

public class ENGINESQLServerDialect extends SQLServerDialect {
    public ENGINESQLServerDialect() {
        super();

        this.registerColumnType(Types.OTHER, "uniqueidentifier");
    }
}
