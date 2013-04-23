package org.ovirt.engine.core.utils.cache;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

public class CacheManager {

    public static Cache<String, String> getTimeoutBaseCache() {
        CacheContainer cacheContainer = EjbUtils.findBean(BeanType.CACHE_CONTAINER, BeanProxyType.LOCAL);
        return cacheContainer.getCache("timeout-base");
    }
}
