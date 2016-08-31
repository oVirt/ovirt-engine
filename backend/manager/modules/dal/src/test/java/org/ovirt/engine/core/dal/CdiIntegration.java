package org.ovirt.engine.core.dal;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.ovirt.engine.core.dao.Dao;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CdiIntegration implements BeanDefinitionRegistryPostProcessor {
    private ConfigurableListableBeanFactory beanFactory;

    @Bean
    public Instance<Dao> daos() {
        // Since the Spring runner doesn't support the CDI @Any annotation - we need to hand it to him manually
        Map<String, Dao> daoMap = beanFactory.getBeansOfType(Dao.class);
        return new InstanceImpl(daoMap.values());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    private static class InstanceImpl implements Instance<Dao> {
        private Iterable<Dao> daos;

        public InstanceImpl(Iterable<Dao> daos) {
            super();
            this.daos = daos;
        }

        @Override
        public Dao get() {
            return daos.iterator().next();
        }

        @Override
        public Iterator<Dao> iterator() {
            return daos.iterator();
        }

        @Override
        public <U extends Dao> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends Dao> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
            return null;
        }

        @Override
        public Instance<Dao> select(Annotation... qualifiers) {
            return null;
        }

        @Override
        public boolean isUnsatisfied() {
            return false;
        }

        @Override
        public boolean isAmbiguous() {
            return false;
        }

        @Override
        public void destroy(Dao dao) {
        }
    }
}

