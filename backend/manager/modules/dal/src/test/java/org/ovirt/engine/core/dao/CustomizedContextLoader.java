package org.ovirt.engine.core.dao;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;

/**
 * This class is used to provide load the application context for the spring framework in a custom way. The
 * customization is for providing the transaction manager that is using the data source from the BaseDAOTestCase.
 *
 */
public class CustomizedContextLoader extends AbstractContextLoader {

    @Override
    protected String getResourceSuffix() {
        return "dummy";
    }

    @Override
    public ApplicationContext loadContext(String ... locations) throws Exception {
        return doLoadContext();
    }

    @Override
    public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        return doLoadContext();
    }

    private ApplicationContext doLoadContext() throws Exception {
        // initialize spring application context to enable transaction support within
        // the Spring TestContext Framework
        GenericApplicationContext ctx = new GenericApplicationContext() {
            @SuppressWarnings("unchecked")
            @Override
            public Object getBean(String name, Class requiredType) throws BeansException {
                if ("transactionManager".equals(name)) {
                    DataSource dataSource = BaseDAOTestCase.getDataSource();
                    if (dataSource != null) {
                        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
                        transactionManager.afterPropertiesSet();
                        return transactionManager;
                    }
                    else {
                        System.err.println("loading context for transaction management but data source is not initializaed yet. "
                                +
                                "Please make sure datasource is initializaed in BaseDAOTestCase before loading the test context.");
                        throw new IllegalStateException("datasource is not initializaed");
                    }
                }
                return super.getBean(name, requiredType);
            }
        };
        return ctx;
    }
}
