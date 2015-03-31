package org.ovirt.engine.core;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.ovirt.engine.core.dao.DAO;
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
    public Instance<DAO> daos() {
        // Since the Spring runner doesn't support the CDI @Any annotation - we need to hand it to him manually
        Map<String, DAO> daoMap = beanFactory.getBeansOfType(DAO.class);
        Instance<DAO> daos = new InstanceImpl(daoMap.values());
        return daos;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    }

    private class InstanceImpl implements Instance<DAO> {
        private Iterable<DAO> daos;

        public InstanceImpl(Iterable<DAO> daos) {
            super();
            this.daos = daos;
        }

        @Override
        public DAO get() {
            return daos.iterator().next();
        }

        @Override
        public Iterator<DAO> iterator() {
            return daos.iterator();
        }

        @Override
        public <U extends DAO> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
            return null;
        }

        @Override
        public <U extends DAO> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
            return null;
        }

        @Override
        public Instance<DAO> select(Annotation... qualifiers) {
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
    }
}

