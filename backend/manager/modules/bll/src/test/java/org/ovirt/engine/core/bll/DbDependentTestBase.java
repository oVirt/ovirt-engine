package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.springframework.context.annotation.Bean;

public class DbDependentTestBase extends BaseCommandTest {
    private static DbFacade dbFacade;

    @BeforeClass
    public static void initDbFacade() throws Exception {
        dbFacade = mock(DbFacade.class);
        // As long as classes are calling DbFacade.getInstance,
        // we'll need this code
        Field instanceField = DbFacade.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, dbFacade);
    }

    @After
    public void done() {
        Mockito.reset(dbFacade);
    }

    @Bean
    public DbFacade dbFacade() {
        return dbFacade;
    }
}
