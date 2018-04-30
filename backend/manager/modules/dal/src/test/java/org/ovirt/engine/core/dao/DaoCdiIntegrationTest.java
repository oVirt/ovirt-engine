package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

public class DaoCdiIntegrationTest {

    private static Set<Class<? extends Dao>> daoClasses;

    @BeforeAll
    public static void setUp() {
        final Reflections reflections = new Reflections("org.ovirt.engine");

        daoClasses = Collections.unmodifiableSet(reflections.getSubTypesOf(Dao.class));
    }

    @Test
    public void testSingletonDaoAnnotationPresent() {

        daoClasses.stream().filter(this::isConcreteClass).forEach(daoClass ->
                assertTrue(
                        daoClass.isAnnotationPresent(Singleton.class),
                        "A concrete DAO class has to be annotated with @Singleton: " + daoClass.getCanonicalName()));
    }

    @Test
    public void testSingletonDaoAnnotationNotPresentOnAbstractClass() {
        daoClasses.stream().filter(this::isAbstractClass).forEach(daoClass ->
            assertFalse(
                    daoClass.isAnnotationPresent(Singleton.class),
                    "An abstract DAO class cannot be annotated with @Singleton: " + daoClass.getCanonicalName()));
    }

    @Test
    public void testSingletonDaoAnnotationNotPresentOnParametrizedClass() {
        daoClasses.stream().filter(this::isParametrizedClass).forEach(daoClass ->
            assertFalse(
                    daoClass.isAnnotationPresent(Singleton.class),
                    "A parametrized DAO class cannot be annotated with @Singleton: " + daoClass.getCanonicalName()));
    }

    private boolean isParametrizedClass(Class<?> clazz) {
        return clazz.getTypeParameters().length > 0;
    }

    private boolean isAbstractClass(Class<?> clazz) {
        return clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers());
    }

    private boolean isConcreteClass(Class<?> daoClass) {
        return !isAbstractClass(daoClass);
    }
}
