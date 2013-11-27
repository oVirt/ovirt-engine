package org.ovirt.engine.core.dal.dbbroker;

import javax.sql.DataSource;

import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

public class CustomSQLErrorCodeSQLExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator {

    CustomSQLErrorCodeSQLExceptionTranslator(DataSource datasource) {
        super(datasource);
    }
}
