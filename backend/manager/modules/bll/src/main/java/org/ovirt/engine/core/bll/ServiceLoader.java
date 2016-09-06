package org.ovirt.engine.core.bll;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.BackendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ServiceLoader {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoader.class);

    @Inject
    @Any
    private Instance<BackendService> services;

    /**
     * Load CDI beans of type {@code BackendService} by simply getting their reference from
     * the bean manager. If the instance doesn't exist (which is the assumption) it will be created
     * and post-constructed (using {@code @PostConstruct} annotated method).
     *
     * After creation the {@link BackendService#onServiceLoad()} hook will be invoked where
     * the service can perform initialization that is dependant on the presence of fully constructed
     * service bean in the CDI context. Useful for breaking dependency-cycles where parent service
     * manually constructs CDI-managed children that depend on the parent via CDI.
     *
     * @param service a provider of {@code BackendService} instances. see {@linkplain Instance}
     */
    public void load(Class<? extends BackendService> service) {
        BackendService backendService = services.select(service).get();
        log.info("Start {} ", backendService);
    }
}
