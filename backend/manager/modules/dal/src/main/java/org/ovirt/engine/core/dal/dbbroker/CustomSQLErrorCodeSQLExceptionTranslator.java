package org.ovirt.engine.core.dal.dbbroker;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

@Singleton
public class CustomSQLErrorCodeSQLExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator {

    @Inject
    CustomSQLErrorCodeSQLExceptionTranslator(DataSource datasource) {
        super(datasource);
    }
}
