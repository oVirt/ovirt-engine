package org.ovirt.engine.core.bll.utils;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleConfigurationExtension implements Extension {
    private static final Logger log = LoggerFactory.getLogger(ModuleConfigurationExtension.class);

    /**
     * This method is automatically activated by CDI, and loads all classes in the org.ovirt package that has NAMED or
     * SINGLETON annotations.
     * @param bdd
     */
    void readAllConfigurations(final @Observes BeforeBeanDiscovery bdd, BeanManager bm) {
        log.info("Starting to load beans from modules");
        addBeansFromPackage(bdd, bm, "org.ovirt.engine.core.utils.transaction");
        addBeansFromPackage(bdd, bm, "org.ovirt.engine.core.dal");
        addBeansFromPackage(bdd, bm, "org.ovirt.engine.core.dao");
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
    private void addBeansFromPackage(final BeforeBeanDiscovery bdd, BeanManager bm, String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(Named.class);
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Singleton.class));
        beanClasses.addAll(reflections.getTypesAnnotatedWith(ApplicationScoped.class));

        for (Class<?> bean : beanClasses) {
            AnnotatedType<Object> annotatedType = new AnnotatedTypeBuilder().readFromType(bean).create();
            Set<Bean<?>> foundBeans = bm.getBeans(annotatedType.getBaseType(), new AnnotationLiteral<Any>() {
            });

            if (foundBeans.size() == 0) {
                bdd.addAnnotatedType(annotatedType);
            }
        }
    }
}
