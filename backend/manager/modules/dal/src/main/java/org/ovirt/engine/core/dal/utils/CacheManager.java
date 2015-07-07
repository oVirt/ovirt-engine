package org.ovirt.engine.core.dal.utils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Singleton;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.ovirt.engine.core.common.BackendService;

@Singleton
public class CacheManager implements BackendService {

    public static final String TIMEOUT_BASE = "timeout-base";

    @Resource(lookup = "java:jboss/infinispan/ovirt-engine")
    private CacheContainer cacheContainer;
    private static Cache<String, String> cache;

    @PostConstruct
    private void init() {
        cache = cacheContainer.getCache(TIMEOUT_BASE);
    }

    public static Cache<String, String> getTimeoutBaseCache() {
        return cache;
    }
}
