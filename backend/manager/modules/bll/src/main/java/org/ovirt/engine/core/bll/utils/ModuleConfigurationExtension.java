package org.ovirt.engine.core.bll.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class ModuleConfigurationExtension implements Extension {
    private static final Logger log = LoggerFactory.getLogger(ModuleConfigurationExtension.class);
    private final Map<String, AnnotatedType<Object>> beans = new HashMap<>();

    /**
     * This method is automatically activated by CDI, and loads all classes in the org.ovirt package that has NAMED or
     * SINGLETON annotations.
     * @param bdd
     */
    void readAllConfigurations(final @Observes BeforeBeanDiscovery bdd, BeanManager bm) {
        log.info("Starting to load beans from modules");
        addBeansFromPackage(bdd, bm, "org.ovirt.engine.core.dal");
        addBeansFromPackage(bdd, bm, "org.ovirt.engine.core.dao");
    }

    private void addBeansFromPackage(final BeforeBeanDiscovery bdd, BeanManager bm, String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> beanClasses = reflections.getTypesAnnotatedWith(Named.class);
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Singleton.class));

        for (Class<?> bean : beanClasses) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            AnnotatedType<Object> annotatedType = new AnnotatedTypeBuilder().readFromType(bean).create();
            Set<Bean<?>> foundBeans = bm.getBeans(annotatedType.getBaseType(), new AnnotationLiteral<Any>() {
            });

            if (foundBeans.size() == 0) {
                bdd.addAnnotatedType(annotatedType);
                String name;
                Named named = bean.getAnnotation(Named.class);
                if (named == null || Strings.isNullOrEmpty(named.value())) {
                    name = bean.getSimpleName();
                } else {
                    name = named.value();
                }
                beans.put(name, annotatedType);
            }
        }
    }

    /**
     * This method actually initializes the beans we discovered in <code>readAllConfigurations</code>. Again - this
     * method is automatically activated by CDI
     * @param abd
     * @param bm
     * @throws Exception
     */
    public void addCdiBeans(final @Observes AfterBeanDiscovery abd, final BeanManager bm) throws Exception {
        log.info("Starting to initialize beans from modules");

        for (Map.Entry<String, AnnotatedType<Object>> bean : beans.entrySet()) {
            Set<Bean<?>> foundBeans = bm.getBeans(bean.getValue().getBaseType());
            if (foundBeans.size() == 0) {
                final Bean<Object> cdiBean = createBean(bm, bean.getKey(), bean.getValue());
                abd.addBean(cdiBean);
                log.debug("Added bean " + cdiBean.getName());
            }
        }
    }

    private static Bean<Object> createBean(final BeanManager bm,
            final String name,
            final AnnotatedType<Object> annotatedType)
            throws Exception {
        final BeanBuilder<Object> beanBuilder = new BeanBuilder<Object>(bm).
                readFromType(annotatedType).
                name(name);

        return beanBuilder.create();
    }
}
