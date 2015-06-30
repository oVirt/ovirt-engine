package org.ovirt.engine.core;

import javax.persistence.EntityManager;

import org.ovirt.engine.core.dao.jpa.EntityManagerHolder;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

public class EntityManagerTestListener extends DependencyInjectionTestExecutionListener {

    private EntityManager em;
    private EntityManagerHolder entityManagerHolder;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        entityManagerHolder = testContext.getApplicationContext().getBean(EntityManagerHolder.class);
        em = entityManagerHolder.getEntityManager();
        em.joinTransaction();
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);
        if (em.isOpen()) {
            em.close();
        }
        entityManagerHolder.nullEntityManager();
    }
}
