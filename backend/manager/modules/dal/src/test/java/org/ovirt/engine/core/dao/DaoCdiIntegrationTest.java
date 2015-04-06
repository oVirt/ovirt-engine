package org.ovirt.engine.core.dao;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;

import static org.junit.Assert.assertTrue;

public class DaoCdiIntegrationTest {

    private static Set<Class<? extends DAO>> daos;

    @BeforeClass
    public static void setUp() throws Exception {
        final Reflections reflections = new Reflections("org.ovirt.engine");

        daos = Collections.unmodifiableSet(reflections.getSubTypesOf(DAO.class));
    }

    @Test
    public void testSingletonDaoAnnotation() {

        for (Class<? extends DAO> dao : daos) {
            if (isConcreteClass(dao)) {
                assertTrue("A concrete DAO class has to be annotated with @Singleton: " + dao.getCanonicalName(),
                        dao.isAnnotationPresent(Singleton.class));
            }
        }
    }

    private boolean isConcreteClass(Class<? extends DAO> clazz) {
        return !(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()));
    }
}
